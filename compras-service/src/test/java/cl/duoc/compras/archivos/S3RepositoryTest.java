package cl.duoc.compras.archivos;
import org.junit.jupiter.api.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.core.sync.RequestBody;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class S3RepositoryTest {
 S3Client s3=mock(S3Client.class);
 S3Repository repo=new S3Repository(s3,"bucket-test");
 @Test void crearRespetaCarpetaYNoSobreescribe() {
  repo.crear(1001,"texto".getBytes());
  verify(s3).putObject(argThat((PutObjectRequest r)->r.key().equals("1001/resumen-1001.txt")&&r.bucket().equals("bucket-test")&&"*".equals(r.ifNoneMatch())&&r.serverSideEncryption()==ServerSideEncryption.AES256),any(RequestBody.class));
 }
 @Test void reemplazoExigeVersionExistente() {
  when(s3.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder().eTag("etag-1").build());
  repo.actualizar(1001,"corregido".getBytes());
  verify(s3).putObject(argThat((PutObjectRequest r)->"etag-1".equals(r.ifMatch())&&r.key().equals("1001/resumen-1001.txt")),any(RequestBody.class));
 }
 @Test void noBorraUnArchivoInexistente() {
  when(s3.headObject(any(HeadObjectRequest.class))).thenThrow(S3Exception.builder().statusCode(404).build());
  assertThatThrownBy(()->repo.borrar(1001)).isInstanceOf(S3Exception.class);
  verify(s3,never()).deleteObject(any(DeleteObjectRequest.class));
 }
 @Test void borrarValidaExistencia() {
  when(s3.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder().build());
  repo.borrar(1001);
  var order=inOrder(s3); order.verify(s3).headObject(any(HeadObjectRequest.class)); order.verify(s3).deleteObject(argThat((DeleteObjectRequest r)->r.key().equals("1001/resumen-1001.txt")));
 }
}
