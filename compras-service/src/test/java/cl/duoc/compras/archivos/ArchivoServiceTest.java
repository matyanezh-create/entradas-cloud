package cl.duoc.compras.archivos;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockMultipartFile;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
class ArchivoServiceTest {
 S3Repository repo=mock(S3Repository.class);
 ArchivoService service=new ArchivoService(repo);
 @Test void rechazaNumeroDistinto() {
  var f=new MockMultipartFile("file","x.txt","text/plain","RESUMEN DE COMPRA\nNumero: 999\n".getBytes());
  assertThatThrownBy(()->service.guardar(1001,f,false)).isInstanceOf(ApiError.class); verifyNoInteractions(repo);
 }
 @Test void rechazaVacio() {
  assertThatThrownBy(()->service.guardar(1001,new MockMultipartFile("file",new byte[0]),false)).isInstanceOf(ApiError.class); verifyNoInteractions(repo);
 }
 @Test void actualizaBytesSinCambiarClave() throws Exception {
  byte[] b="RESUMEN DE COMPRA\nNumero: 1001\nTOTAL CLP: 90000.00\n".getBytes();
  when(repo.key(1001)).thenReturn("1001/resumen-1001.txt");
  assertThat(service.guardar(1001,new MockMultipartFile("file",b),true)).isEqualTo("1001/resumen-1001.txt"); verify(repo).actualizar(1001,b);
 }
 @Test void rechazaBinario() {
  assertThatThrownBy(()->service.guardar(1001,new MockMultipartFile("file",new byte[]{(byte)255}),false)).isInstanceOf(ApiError.class); verifyNoInteractions(repo);
 }
}
