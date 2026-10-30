package cl.duoc.compras;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
@Entity @Table(name="compras")
public class Compra {
 @Id @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="compra_seq")
 @SequenceGenerator(name="compra_seq",sequenceName="compra_seq",allocationSize=1) public Long id;
 @Column(nullable=false) public Long eventoId;
 @Column(nullable=false,length=150) public String evento;
 @Column(nullable=false) public Integer cantidad;
 @Column(nullable=false,precision=12,scale=2) public BigDecimal precioUnitario;
 @Column(nullable=false,precision=16,scale=2) public BigDecimal precioTotal;
 @Column(nullable=false) public OffsetDateTime fechaCompra;
 protected Compra() {}
 public Compra(EventoClient.EventoDetalle e,int cantidad) {
 this.eventoId=e.id();this.evento=e.nombre();this.cantidad=cantidad;this.precioUnitario=e.precio();
 this.precioTotal=e.precio().multiply(BigDecimal.valueOf(cantidad));this.fechaCompra=OffsetDateTime.now();
 }
}
