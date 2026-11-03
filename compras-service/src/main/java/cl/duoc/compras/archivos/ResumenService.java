package cl.duoc.compras.archivos;
import cl.duoc.compras.Compra;
import cl.duoc.compras.CompraService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
@Service
public class ResumenService {
 private final Path dir;
 private final CompraService compras;
 public ResumenService(@Value("${app.local-dir}") String dir,CompraService compras) {
  this.dir=Path.of(dir).toAbsolutePath().normalize();this.compras=compras;
 }
 public synchronized byte[] generar(long numero) throws IOException {
  Compra c=compras.buscar(numero);
  // Se usan exclusivamente los datos historicos de la venta persistida.
  String text="RESUMEN DE COMPRA\nNumero: "+c.id+"\nFecha: "+c.fechaCompra+"\nEvento ID: "+c.eventoId+"\nEvento: "+c.evento+"\nCantidad: "+c.cantidad+"\nPrecio unitario CLP: "+c.precioUnitario.setScale(2).toPlainString()+"\nTOTAL CLP: "+c.precioTotal.setScale(2).toPlainString()+"\n";
  byte[] data=text.getBytes(StandardCharsets.UTF_8);
  Files.createDirectories(dir);
  Files.write(dir.resolve("resumen-"+numero+".txt"),data,StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
  return data;
 }
}

