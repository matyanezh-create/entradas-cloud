package cl.duoc.compras;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
@Service
public class CompraService {
 private final CompraRepository repo;private final EventoClient eventos;
 public CompraService(CompraRepository repo,EventoClient eventos) { this.repo=repo;this.eventos=eventos; }
 // La consulta remota ocurre antes de abrir la transacción de escritura del repositorio.
 public Compra comprar(CompraInput in) { return repo.save(new Compra(eventos.obtener(in.eventoId()),in.cantidad())); }
 @Transactional(readOnly=true)
 public Compra buscar(Long id) { return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Compra no encontrada")); }
}
