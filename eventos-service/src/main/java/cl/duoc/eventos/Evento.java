package cl.duoc.eventos;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
@Entity @Table(name="eventos")
public class Evento {
 @Id @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="evento_seq")
 @SequenceGenerator(name="evento_seq",sequenceName="evento_seq",allocationSize=1) public Long id;
 @Column(nullable=false,length=150) public String nombre;
 @Column(nullable=false) public OffsetDateTime fecha;
 @Column(nullable=false,precision=12,scale=2) public BigDecimal precio;
 @Convert(converter=org.hibernate.type.NumericBooleanConverter.class)
 @Column(nullable=false,columnDefinition="NUMBER(1)") public boolean activo=true;
 protected Evento() {}
 public Evento(String nombre,OffsetDateTime fecha,BigDecimal precio) { this.nombre=nombre.trim();this.fecha=fecha;this.precio=precio; }
}

