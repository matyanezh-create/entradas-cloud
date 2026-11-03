package cl.duoc.compras.archivos;
import cl.duoc.compras.CompraService;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
import java.io.IOException;
import java.util.Map;
@RestController
@RequestMapping("/api/compras/{numero}/resumen")
@Validated
public class ResumenController {
 private final ResumenService resumen;
 private final ArchivoService archivos;
 private final CompraService compras;
 public ResumenController(ResumenService resumen,ArchivoService archivos,CompraService compras) { this.resumen=resumen;this.archivos=archivos;this.compras=compras; }
 @PostMapping("/generar")
 public ResponseEntity<byte[]> generar(@PathVariable @Positive long numero) throws IOException {
  return archivo(numero,resumen.generar(numero),HttpStatus.CREATED);
 }
 @PostMapping(value="/archivo",consumes="multipart/form-data")
 public ResponseEntity<?> subir(@PathVariable @Positive long numero,@RequestPart("file") MultipartFile file) throws IOException {
  compras.buscar(numero);
  return ResponseEntity.status(201).body(Map.of("key",archivos.guardar(numero,file,false),"estado","creado"));
 }
 @PutMapping(value="/archivo",consumes="multipart/form-data")
 public Object modificar(@PathVariable @Positive long numero,@RequestPart("file") MultipartFile file) throws IOException {
  compras.buscar(numero);
  return Map.of("key",archivos.guardar(numero,file,true),"estado","actualizado");
 }
 @GetMapping
 public Object consultar(@PathVariable @Positive long numero) { compras.buscar(numero);return archivos.consultar(numero); }
 @GetMapping("/archivo")
 public ResponseEntity<byte[]> descargar(@PathVariable @Positive long numero) { compras.buscar(numero);return archivo(numero,archivos.descargar(numero),HttpStatus.OK); }
 @DeleteMapping("/archivo")
 public ResponseEntity<Void> borrar(@PathVariable @Positive long numero) { compras.buscar(numero);archivos.borrar(numero);return ResponseEntity.noContent().build(); }
 private ResponseEntity<byte[]> archivo(long n,byte[] bytes,HttpStatus status) {
  return ResponseEntity.status(status).contentType(MediaType.parseMediaType("text/plain;charset=UTF-8"))
   .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"resumen-"+n+".txt\"").body(bytes);
 }
}
