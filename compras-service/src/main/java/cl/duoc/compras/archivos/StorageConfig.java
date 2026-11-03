package cl.duoc.compras.archivos;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
@Configuration
public class StorageConfig {
 @Bean S3Client s3Client(@Value("${app.region}") String region) {
  return S3Client.builder().region(Region.of(region)).build();
 }
}
