package cl.duoc.eventos;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/eventos")
public class EventoController {
 private final EventoService service;
 public EventoController(EventoService service) { this.service=service; }
 @GetMapping public List<Evento> listar() { return service.disponibles(); }
 @GetMapping("/{id}") public Evento obtener(@PathVariable Long id) { return service.buscar(id); }
 @PostMapping public ResponseEntity<Evento> crear(@Valid @RequestBody EventoInput in) { var e=service.crear(in);return ResponseEntity.created(URI.create("/api/eventos/"+e.id)).body(e); }
 @PutMapping("/{id}") public Evento actualizar(@PathVariable Long id,@Valid @RequestBody EventoInput in) { return service.actualizar(id,in); }
 @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable Long id) { service.eliminar(id);return ResponseEntity.noContent().build(); }
}
