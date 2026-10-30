package cl.duoc.compras;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;
@Component
public class EventoClient {
 private final RestClient client;
 public EventoClient(@Value("${eventos.base-url}") String baseUrl) {
 var http=java.net.http.HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
 var factory=new JdkClientHttpRequestFactory(http);factory.setReadTimeout(Duration.ofSeconds(5));
 this.client=RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
 }
 public EventoDetalle obtener(Long id) {
 try {
 var e=client.get().uri("/api/eventos/{id}",id).retrieve().body(EventoDetalle.class);
 if(e==null || e.id()==null || e.nombre()==null || e.fecha()==null || e.precio()==null)
 throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Respuesta inválida del servicio de eventos");
 if(!e.activo() || !e.fecha().isAfter(OffsetDateTime.now()))
 throw new ResponseStatusException(HttpStatus.CONFLICT,"El evento no está disponible para compra");
 return e;
 } catch(HttpClientErrorException.NotFound ex) {
 throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Evento no encontrado");
 } catch(RestClientException ex) {
 throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Servicio de eventos no disponible; intenta más tarde");
 }
 }
 public record EventoDetalle(Long id,String nombre,OffsetDateTime fecha,BigDecimal precio,boolean activo) {}
}
