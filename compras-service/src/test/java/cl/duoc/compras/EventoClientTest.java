package cl.duoc.compras;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.*;
import org.springframework.web.server.ResponseStatusException;
import static org.assertj.core.api.Assertions.*;
class EventoClientTest {
 HttpServer server; EventoClient client;
 @BeforeEach void start() throws Exception {
  server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
  client=new EventoClient("http://127.0.0.1:"+server.getAddress().getPort());
 }
 void response(int code,String body) {
  server.createContext("/api/eventos/1",exchange->{
   byte[] bytes=body.getBytes(StandardCharsets.UTF_8);
   exchange.getResponseHeaders().add("Content-Type","application/json");
   exchange.sendResponseHeaders(code,bytes.length);
   try(var out=exchange.getResponseBody()){out.write(bytes);}
  });server.start();
 }
 @AfterEach void stop(){server.stop(0);}
 @Test void obtieneEventoPorHttp() {
  response(200,"{\"id\":1,\"nombre\":\"Concierto\",\"fecha\":\"2099-01-01T00:00:00Z\",\"precio\":100.50,\"activo\":true}");
  assertThat(client.obtener(1L).precio()).isEqualByComparingTo("100.50");
 }
 @Test void traduce404() {
  response(404,"{}");assertThatThrownBy(()->client.obtener(1L)).isInstanceOfSatisfying(ResponseStatusException.class,e->assertThat(e.getStatusCode().value()).isEqualTo(404));
 }
 @Test void traduceFalloRemoto() {
  response(500,"{}");assertThatThrownBy(()->client.obtener(1L)).isInstanceOfSatisfying(ResponseStatusException.class,e->assertThat(e.getStatusCode().value()).isEqualTo(503));
 }
 @Test void rechazaEventoVencido() {
  response(200,"{\"id\":1,\"nombre\":\"Pasado\",\"fecha\":\"2020-01-01T00:00:00Z\",\"precio\":100,\"activo\":true}");
  assertThatThrownBy(()->client.obtener(1L)).isInstanceOfSatisfying(ResponseStatusException.class,e->assertThat(e.getStatusCode().value()).isEqualTo(409));
 }
 @Test void rechazaEventoInactivo() {
  response(200,"{\"id\":1,\"nombre\":\"Inactivo\",\"fecha\":\"2099-01-01T00:00:00Z\",\"precio\":100,\"activo\":false}");
  assertThatThrownBy(()->client.obtener(1L)).isInstanceOfSatisfying(ResponseStatusException.class,e->assertThat(e.getStatusCode().value()).isEqualTo(409));
 }
 @Test void rechazaRespuestaIncompleta() {
  response(200,"{}");assertThatThrownBy(()->client.obtener(1L)).isInstanceOfSatisfying(ResponseStatusException.class,e->assertThat(e.getStatusCode().value()).isEqualTo(502));
 }
}

