package cl.duoc.compras;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class CompraApiTest {
 @Autowired MockMvc mvc; @Autowired CompraRepository repo;
 @MockitoBean EventoClient eventos;
 @BeforeEach void clean(){repo.deleteAll();}
 EventoClient.EventoDetalle evento(String nombre,String precio){return new EventoClient.EventoDetalle(1L,nombre,OffsetDateTime.now().plusDays(30),new BigDecimal(precio),true);}
 @Test void calculaTotalYConservaPrecioHistorico() throws Exception {
 when(eventos.obtener(1L)).thenReturn(evento("Concierto","12500.50"));
 mvc.perform(post("/api/compras").contentType(MediaType.APPLICATION_JSON).content("{\"eventoId\":1,\"cantidad\":3}"))
 .andExpect(status().isCreated()).andExpect(header().exists("Location"))
 .andExpect(jsonPath("$.evento").value("Concierto")).andExpect(jsonPath("$.cantidad").value(3))
 .andExpect(jsonPath("$.precioUnitario").value(12500.50)).andExpect(jsonPath("$.precioTotal").value(37501.50));
 var c=repo.findAll().get(0); assertThat(c.precioTotal).isEqualByComparingTo("37501.50");
 when(eventos.obtener(1L)).thenReturn(evento("Nuevo nombre","99"));
 mvc.perform(get("/api/compras/"+c.id)).andExpect(status().isOk()).andExpect(jsonPath("$.evento").value("Concierto"))
 .andExpect(jsonPath("$.precioUnitario").value(12500.50));
 }
 @Test void rechazaCantidadCero() throws Exception {
 mvc.perform(post("/api/compras").contentType(MediaType.APPLICATION_JSON).content("{\"eventoId\":1,\"cantidad\":0}"))
 .andExpect(status().isBadRequest());verifyNoInteractions(eventos);assertThat(repo.count()).isZero();
 }
 @Test void rechazaCantidadExcesiva() throws Exception {
 mvc.perform(post("/api/compras").contentType(MediaType.APPLICATION_JSON).content("{\"eventoId\":1,\"cantidad\":1001}"))
 .andExpect(status().isBadRequest());assertThat(repo.count()).isZero();
 }
 @Test void rechazaCantidadFraccionaria() throws Exception {
 mvc.perform(post("/api/compras").contentType(MediaType.APPLICATION_JSON).content("{\"eventoId\":1,\"cantidad\":1.5}"))
 .andExpect(status().isBadRequest());assertThat(repo.count()).isZero();
 }
 @Test void eventoInexistenteNoGeneraCompra() throws Exception {
 when(eventos.obtener(999L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,"Evento no encontrado"));
 mvc.perform(post("/api/compras").contentType(MediaType.APPLICATION_JSON).content("{\"eventoId\":999,\"cantidad\":2}"))
 .andExpect(status().isNotFound());assertThat(repo.count()).isZero();
 }
 @Test void servicioCaidoNoGeneraCompra() throws Exception {
 when(eventos.obtener(1L)).thenThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Servicio no disponible"));
 mvc.perform(post("/api/compras").contentType(MediaType.APPLICATION_JSON).content("{\"eventoId\":1,\"cantidad\":2}"))
 .andExpect(status().isServiceUnavailable());assertThat(repo.count()).isZero();
 }
 @Test void noAceptaPrecioDelCliente() throws Exception {
 mvc.perform(post("/api/compras").contentType(MediaType.APPLICATION_JSON).content("{\"eventoId\":1,\"cantidad\":2,\"precioUnitario\":1}"))
 .andExpect(status().isBadRequest());assertThat(repo.count()).isZero();
 }
 @Test void compraInexistenteDevuelve404() throws Exception {mvc.perform(get("/api/compras/999999")).andExpect(status().isNotFound());}
}
