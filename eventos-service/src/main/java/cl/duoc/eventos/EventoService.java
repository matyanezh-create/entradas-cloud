package cl.duoc.eventos;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
@Service @Transactional
public class EventoService {
 private final EventoRepository repo;
 public EventoService(EventoRepository repo) { this.repo=repo; }
 @Transactional(readOnly=true)
 public List<Evento> disponibles() { return repo.findByActivoTrueAndFechaAfterOrderByFechaAsc(OffsetDateTime.now()); }
 @Transactional(readOnly=true)
 public Evento buscar(Long id) { return repo.findById(id).filter(e->e.activo).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Evento no encontrado")); }
 public Evento crear(EventoInput in) { return repo.save(new Evento(in.nombre(),in.fecha(),in.precio())); }
 public Evento actualizar(Long id,EventoInput in) { var e=buscar(id);e.nombre=in.nombre().trim();e.fecha=in.fecha();e.precio=in.precio();return repo.save(e); }
 public void eliminar(Long id) { var e=buscar(id);e.activo=false;repo.save(e); }
}
