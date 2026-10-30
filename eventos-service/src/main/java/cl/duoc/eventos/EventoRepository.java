package cl.duoc.eventos;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface EventoRepository extends JpaRepository<Evento,Long> {
 List<Evento> findByActivoTrueAndFechaAfterOrderByFechaAsc(java.time.OffsetDateTime fecha);
}
