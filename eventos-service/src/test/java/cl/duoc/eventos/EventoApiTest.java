package cl.duoc.eventos;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class EventoApiTest {
 @Autowired MockMvc mvc; @Autowired EventoRepository repo;
 @BeforeEach void clean(){repo.deleteAll();}
 String input(String nombre,String precio){ return "{\"nombre\":\""+nombre+"\",\"fecha\":\""+OffsetDateTime.now().plusDays(30)+"\",\"precio\":"+precio+"}"; }
 @Test void creaPersisteYLista() throws Exception {
 mvc.perform(post("/api/eventos").contentType(MediaType.APPLICATION_JSON).content(input("Concierto","12500.50")))
 .andExpect(status().isCreated()).andExpect(header().exists("Location")).andExpect(jsonPath("$.nombre").value("Concierto"));
 assertThat(repo.findAll()).hasSize(1);
 assertThat(repo.findAll().get(0).precio).isEqualByComparingTo("12500.50");
 mvc.perform(get("/api/eventos")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
 }
 @Test void actualizaYEliminaLogicamente() throws Exception {
 var e=repo.save(new Evento("Inicial",OffsetDateTime.now().plusDays(30),new BigDecimal("10")));
 mvc.perform(put("/api/eventos/"+e.id).contentType(MediaType.APPLICATION_JSON).content(input("Actualizado","20")))
 .andExpect(status().isOk()).andExpect(jsonPath("$.precio").value(20));
 mvc.perform(delete("/api/eventos/"+e.id)).andExpect(status().isNoContent());
 mvc.perform(get("/api/eventos/"+e.id)).andExpect(status().isNotFound());
 assertThat(repo.findById(e.id).orElseThrow().activo).isFalse();
 }
 @Test void rechazaDatosInvalidos() throws Exception {
 mvc.perform(post("/api/eventos").contentType(MediaType.APPLICATION_JSON).content(input(" ","-1")))
 .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.nombre").exists()).andExpect(jsonPath("$.errors.precio").exists());
 assertThat(repo.count()).isZero();
 }
 @Test void rechazaFechaPasada() throws Exception {
 mvc.perform(post("/api/eventos").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Pasado\",\"fecha\":\"2020-01-01T00:00:00Z\",\"precio\":100}"))
 .andExpect(status().isBadRequest());assertThat(repo.count()).isZero();
 }
 @Test void noListaEventosVencidos() throws Exception {
 repo.save(new Evento("Vencido",OffsetDateTime.now().minusDays(1),BigDecimal.TEN));
 mvc.perform(get("/api/eventos")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
 }
 @Test void inexistenteDevuelve404() throws Exception {mvc.perform(get("/api/eventos/999999")).andExpect(status().isNotFound());}
 @Test void rechazaJsonMalformado() throws Exception {mvc.perform(post("/api/eventos").contentType(MediaType.APPLICATION_JSON).content("{" )).andExpect(status().isBadRequest());}
}
