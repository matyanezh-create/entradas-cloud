package cl.duoc.compras;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;
@RestControllerAdvice
public class ApiErrors {
 @ExceptionHandler(MethodArgumentNotValidException.class)
 public ProblemDetail validation(MethodArgumentNotValidException ex) {
 var p=ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,"Datos de entrada inválidos");
 Map<String,String> errors=new TreeMap<>();
 ex.getBindingResult().getFieldErrors().forEach(e->errors.put(e.getField(),e.getDefaultMessage()));
 p.setProperty("errors",errors);return p;
 }
 @ExceptionHandler(ResponseStatusException.class)
 public ProblemDetail status(ResponseStatusException ex) { return ProblemDetail.forStatusAndDetail(ex.getStatusCode(),ex.getReason()); }
 @ExceptionHandler(DataIntegrityViolationException.class)
 public ProblemDetail conflict(DataIntegrityViolationException ex) { return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,"Conflicto de datos"); }
}
