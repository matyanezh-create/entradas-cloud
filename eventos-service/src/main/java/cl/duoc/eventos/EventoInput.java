package cl.duoc.eventos;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
public record EventoInput(@NotBlank @Size(max=150) String nombre,@NotNull @Future OffsetDateTime fecha,
 @NotNull @DecimalMin("0.01") @Digits(integer=10,fraction=2) BigDecimal precio) {}
