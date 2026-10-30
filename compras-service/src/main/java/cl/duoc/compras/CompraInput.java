package cl.duoc.compras;
import jakarta.validation.constraints.*;
public record CompraInput(@NotNull @Positive Long eventoId,@NotNull @Min(1) @Max(1000) Integer cantidad) {}
