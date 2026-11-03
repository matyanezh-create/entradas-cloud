package cl.duoc.compras.archivos;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.http.*;
import jakarta.validation.ConstraintViolationException;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.core.exception.SdkClientException;
import java.io.IOException;
import java.util.Map;
@RestControllerAdvice(assignableTypes=ResumenController.class)
public class ExceptionHandlerAdvice {
 private ResponseEntity<?> error(int status,String message) { return ResponseEntity.status(status).body(Map.of("status",status,"error",message)); }
 @ExceptionHandler(ApiError.class) public ResponseEntity<?> api(ApiError e) { return error(e.status.value(),e.getMessage()); }
 @ExceptionHandler({MethodArgumentNotValidException.class,ConstraintViolationException.class})
 public ResponseEntity<?> invalid(Exception e) { return error(400,"Datos invalidos: revise numero positivo, cliente, correo, evento y entradas"); }
 @ExceptionHandler(MaxUploadSizeExceededException.class) public ResponseEntity<?> size(Exception e) { return error(413,"El archivo supera el limite permitido"); }
 @ExceptionHandler(S3Exception.class) public ResponseEntity<?> aws(S3Exception e) {
  return switch(e.statusCode()) {
   case 404 -> error(404,"Resumen no encontrado en S3");
   case 409,412 -> error(409,"El objeto ya existe o cambio durante la operacion; consulte y reintente");
   case 403 -> error(502,"AWS rechazo el acceso; revise credenciales y permisos del bucket");
   default -> error(502,"No se pudo completar la operacion en S3");
  };
 }
 @ExceptionHandler(SdkClientException.class) public ResponseEntity<?> connection(Exception e) { return error(503,"No se pudo conectar a AWS; revise red, region y credenciales temporales"); }
 @ExceptionHandler(IOException.class) public ResponseEntity<?> io(Exception e) { return error(500,"No se pudo leer o guardar el archivo local"); }
}
