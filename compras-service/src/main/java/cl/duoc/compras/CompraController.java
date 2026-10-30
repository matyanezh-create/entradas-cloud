package cl.duoc.compras;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/compras")
public class CompraController {
 private final CompraService service;
 public CompraController(CompraService service) { this.service=service; }
 @PostMapping public ResponseEntity<Compra> comprar(@Valid @RequestBody CompraInput in) { var c=service.comprar(in);return ResponseEntity.created(URI.create("/api/compras/"+c.id)).body(c); }
 @GetMapping("/{id}") public Compra obtener(@PathVariable Long id) { return service.buscar(id); }
}
