package cl.duoc.compras.archivos;
import cl.duoc.compras.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import java.nio.file.*;
import java.math.BigDecimal;
import java.time.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class ResumenApiTest {
 static final Path dir=createDir();
 static Path createDir() { try {return Files.createTempDirectory("resumenes-test-");}catch(Exception e){throw new RuntimeException(e);} }
 @DynamicPropertySource static void config(DynamicPropertyRegistry r) {r.add("app.bucket",()->"bucket-test");r.add("app.local-dir",()->dir.toString());}
 @Autowired MockMvc mvc;
 @Autowired CompraRepository repo;
 @MockitoBean S3Client s3;
 @MockitoBean EventoClient eventos;
 long id;
 @BeforeEach void setup() {
  repo.deleteAll();
  var c=new Compra(new EventoClient.EventoDetalle(1L,"Festival",OffsetDateTime.now().plusDays(1),new BigDecimal("12500.50"),true),3);
  id=repo.save(c).id;
 }
 String base(){return "/api/compras/"+id+"/resumen";}
 byte[] data(){return ("RESUMEN DE COMPRA\nNumero: "+id+"\nTOTAL CLP: 37501.50\n").getBytes();}
 MockMultipartFile file(){return new MockMultipartFile("file","r.txt","text/plain",data());}
 @Test void generaDesdeCompraPersistida() throws Exception {
  mvc.perform(post(base()+"/generar")).andExpect(status().isCreated()).andExpect(header().string("Content-Disposition","attachment; filename=\"resumen-"+id+".txt\"")).andExpect(content().string(org.hamcrest.Matchers.containsString("TOTAL CLP: 37501.50")));
  assertThat(Files.readString(dir.resolve("resumen-"+id+".txt"))).contains("Festival");verifyNoInteractions(s3);
 }
 @Test void compraInexistenteNoAccedeAS3() throws Exception {
  mvc.perform(get("/api/compras/999999/resumen/archivo")).andExpect(status().isNotFound());verifyNoInteractions(s3);
 }
 @Test void cargaOrganizadaPorNumero() throws Exception {
  mvc.perform(multipart(base()+"/archivo").file(file())).andExpect(status().isCreated()).andExpect(jsonPath("$.key").value(id+"/resumen-"+id+".txt"));
  verify(s3).putObject(argThat((PutObjectRequest x)->x.key().equals(id+"/resumen-"+id+".txt")&&"*".equals(x.ifNoneMatch())),any(RequestBody.class));
 }
 @Test void descargaAdjunto() throws Exception {
  when(s3.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder().eTag("e1").contentLength((long)data().length).build());
  when(s3.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(ResponseBytes.fromByteArray(GetObjectResponse.builder().build(),data()));
  mvc.perform(get(base()+"/archivo")).andExpect(status().isOk()).andExpect(content().bytes(data())).andExpect(header().exists("Content-Disposition"));
 }
 @Test void inexistenteDevuelve404() throws Exception {
  when(s3.headObject(any(HeadObjectRequest.class))).thenThrow(S3Exception.builder().statusCode(404).build());
  mvc.perform(delete(base()+"/archivo")).andExpect(status().isNotFound());verify(s3,never()).deleteObject(any(DeleteObjectRequest.class));
 }
 @Test void reemplazoExigeObjetoPrevio() throws Exception {
  when(s3.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder().eTag("e1").build());
  mvc.perform(multipart(base()+"/archivo").file(file()).with(x->{x.setMethod("PUT");return x;})).andExpect(status().isOk());
  verify(s3).putObject(argThat((PutObjectRequest x)->"e1".equals(x.ifMatch())),any(RequestBody.class));
 }
 @Test void cargaDuplicadaDevuelve409() throws Exception {
  when(s3.putObject(any(PutObjectRequest.class),any(RequestBody.class))).thenThrow(S3Exception.builder().statusCode(412).build());
  mvc.perform(multipart(base()+"/archivo").file(file())).andExpect(status().isConflict());
 }
 @Test void eliminarDevuelve204() throws Exception {
  when(s3.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder().build());
  mvc.perform(delete(base()+"/archivo")).andExpect(status().isNoContent());verify(s3).deleteObject(any(DeleteObjectRequest.class));
 }
 @Test void consultarMetadatos() throws Exception {
  when(s3.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder().contentLength(50L).eTag("e1").lastModified(Instant.now()).build());
  mvc.perform(get(base())).andExpect(status().isOk()).andExpect(jsonPath("$.key").value(id+"/resumen-"+id+".txt"));
 }
}
