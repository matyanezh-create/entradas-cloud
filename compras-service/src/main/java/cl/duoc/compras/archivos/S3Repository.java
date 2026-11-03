package cl.duoc.compras.archivos;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.core.sync.RequestBody;
import org.springframework.http.HttpStatus;
@Repository
public class S3Repository {
 private final S3Client s3;
 private final String bucket;
 public S3Repository(S3Client s3,@Value("${app.bucket}") String bucket) { this.s3=s3; this.bucket=bucket; }
 private void configured() { if(bucket.isBlank()) throw new ApiError(HttpStatus.SERVICE_UNAVAILABLE,"Configure S3_BUCKET para usar archivos en la nube"); }
 public String key(long numero) { return numero+"/resumen-"+numero+".txt"; }
 public HeadObjectResponse head(long numero) {
  configured();
  return s3.headObject(HeadObjectRequest.builder().bucket(bucket).key(key(numero)).build());
 }
 public void crear(long numero,byte[] bytes) {
  configured();
  s3.putObject(PutObjectRequest.builder().bucket(bucket).key(key(numero)).contentType("text/plain; charset=UTF-8").ifNoneMatch("*").serverSideEncryption(ServerSideEncryption.AES256).build(),RequestBody.fromBytes(bytes));
 }
 public void actualizar(long numero,byte[] bytes) {
  configured();
  String etag=head(numero).eTag();
  s3.putObject(PutObjectRequest.builder().bucket(bucket).key(key(numero)).contentType("text/plain; charset=UTF-8").ifMatch(etag).serverSideEncryption(ServerSideEncryption.AES256).build(),RequestBody.fromBytes(bytes));
 }
 public byte[] descargar(long numero) {
  configured();
  HeadObjectResponse meta=head(numero);
  if(meta.contentLength()>1048576) throw new ApiError(HttpStatus.PAYLOAD_TOO_LARGE,"El objeto supera 1 MiB");
  return s3.getObjectAsBytes(GetObjectRequest.builder().bucket(bucket).key(key(numero)).ifMatch(meta.eTag()).build()).asByteArray();
 }
 public void borrar(long numero) {
  configured();
  head(numero);
  s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key(numero)).build());
 }
}
