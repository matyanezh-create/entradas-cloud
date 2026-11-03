package cl.duoc.compras.archivos;
import cl.duoc.compras.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
class ResumenServiceTest {
 @TempDir Path dir;
 CompraService compras=mock(CompraService.class);
 @Test void usaDatosHistoricosYGeneraArchivoFisico() throws Exception {
  var c=new Compra(new EventoClient.EventoDetalle(7L,"Evento historico",OffsetDateTime.now().plusDays(1),new BigDecimal("12500.50"),true),3);c.id=1001L;
  when(compras.buscar(1001L)).thenReturn(c);
  byte[] b=new ResumenService(dir.toString(),compras).generar(1001);
  assertThat(Files.readAllBytes(dir.resolve("resumen-1001.txt"))).isEqualTo(b);
  assertThat(new String(b,java.nio.charset.StandardCharsets.UTF_8)).contains("TOTAL CLP: 37501.50","Numero: 1001","Evento historico");
 }
 @Test void regenerarConservaMismosDatos() throws Exception {
  var c=new Compra(new EventoClient.EventoDetalle(7L,"Evento",OffsetDateTime.now().plusDays(1),BigDecimal.ONE,true),2);c.id=1L;
  when(compras.buscar(1L)).thenReturn(c);
  var service=new ResumenService(dir.toString(),compras);
  assertThat(service.generar(1)).isEqualTo(service.generar(1));
 }
}
