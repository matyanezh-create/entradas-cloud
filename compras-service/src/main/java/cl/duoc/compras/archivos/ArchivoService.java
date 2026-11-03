package cl.duoc.compras.archivos;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.*;
@Service
public class ArchivoService {
 private final S3Repository repo;
 public ArchivoService(S3Repository repo) { this.repo=repo; }
 public String guardar(long numero,MultipartFile file,boolean actualizar) throws IOException {
  if(file.isEmpty()) throw new ApiError(HttpStatus.BAD_REQUEST,"El archivo esta vacio");
  if(file.getSize()>1048576) throw new ApiError(HttpStatus.PAYLOAD_TOO_LARGE,"El archivo supera 1 MiB");
  byte[] bytes=file.getBytes(); String text;
  try { text=StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString(); }
  catch(CharacterCodingException e) { throw new ApiError(HttpStatus.BAD_REQUEST,"Se requiere texto UTF-8"); }
  if(!text.startsWith("RESUMEN DE COMPRA\nNumero: "+numero+"\n") || text.indexOf(0)>=0)
   throw new ApiError(HttpStatus.BAD_REQUEST,"Formato incorrecto o numero del archivo distinto de la ruta");
  if(actualizar) repo.actualizar(numero,bytes); else repo.crear(numero,bytes);
  return repo.key(numero);
 }
 public byte[] descargar(long numero) { return repo.descargar(numero); }
 public void borrar(long numero) { repo.borrar(numero); }
 public Object consultar(long numero) {
  var h=repo.head(numero);
  return java.util.Map.of("numero",numero,"key",repo.key(numero),"bytes",h.contentLength(),"etag",h.eTag(),"ultimaModificacion",h.lastModified().toString());
 }
}
