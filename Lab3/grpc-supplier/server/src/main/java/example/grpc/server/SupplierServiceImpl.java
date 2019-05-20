package example.grpc.server;

/* helper to print binary in hexadecimal */
import static javax.xml.bind.DatatypeConverter.printHexBinary;

/* predefined types */
import com.google.protobuf.ByteString;
import com.google.type.Money;

/* these classes are generated from protobuf definitions */
import example.grpc.Product;
import example.grpc.ProductsRequest;
import example.grpc.ProductsResponse;
import example.grpc.SupplierGrpc;

/* grpc library */
import io.grpc.stub.StreamObserver;

// KEY STUFF
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//Signature things
import example.grpc.SignedResponse;
import example.grpc.Signature;

public class SupplierServiceImpl extends SupplierGrpc.SupplierImplBase {

	public static Key readKey(String resourcePath) throws Exception {
		System.out.println("Reading key from resource " + resourcePath + " ...");

		InputStream fis = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
		byte[] encoded = new byte[fis.available()];
		fis.read(encoded);
		fis.close();

		System.out.println("Key:");
		System.out.println(printHexBinary(encoded));
		SecretKeySpec keySpec = new SecretKeySpec(encoded, "AES");

		return keySpec;
	}

	@Override
	public void listProducts(ProductsRequest request, StreamObserver<SignedResponse> responseObserver){

		System.out.println("listProducts called");
		System.out.println("Received request:");
		System.out.println("in binary hexadecimals:");
		byte[] requestBinary = request.toByteArray();
		System.out.println(printHexBinary(requestBinary));
		System.out.printf("%d bytes%n", requestBinary.length);

		// build response
		ProductsResponse.Builder responseBuilder = ProductsResponse.newBuilder();
		responseBuilder.setSupplierIdentifier("Tagus Sports Store");
		{
			Product.Builder productBuilder = Product.newBuilder();
			productBuilder.setIdentifier("A1");
			productBuilder.setDescription("Soccer ball");
			productBuilder.setQuantity(22);
			Money.Builder moneyBuilder = Money.newBuilder();
			moneyBuilder.setCurrencyCode("EUR").setUnits(10);
			productBuilder.setPrice(moneyBuilder.build());
			responseBuilder.addProduct(productBuilder.build());
		}
		{
			Product.Builder productBuilder = Product.newBuilder();
			productBuilder.setIdentifier("B2");
			productBuilder.setDescription("Basketball");
			productBuilder.setQuantity(100);
			Money.Builder moneyBuilder = Money.newBuilder();
			moneyBuilder.setCurrencyCode("EUR").setUnits(12);
			productBuilder.setPrice(moneyBuilder.build());
			responseBuilder.addProduct(productBuilder.build());
		}
		{
			Product.Builder productBuilder = Product.newBuilder();
			productBuilder.setIdentifier("C3");
			productBuilder.setDescription("Volley ball");
			productBuilder.setQuantity(7);
			Money.Builder moneyBuilder = Money.newBuilder();
			moneyBuilder.setCurrencyCode("EUR").setUnits(8);
			productBuilder.setPrice(moneyBuilder.build());
			responseBuilder.addProduct(productBuilder.build());
		}

		ProductsResponse response = responseBuilder.build();

		System.out.println("Response to send:");
		System.out.println(response);
		System.out.println("in binary hexadecimals:");
		byte[] responseBinary = response.toByteArray();
		System.out.println(printHexBinary(responseBinary));
		System.out.printf("%d bytes%n", responseBinary.length);

		final String DIGEST_ALGO = "SHA-256";
		final String SYM_CIPHER = "AES/ECB/PKCS5Padding";

		//For creating signature
		SignedResponse.Builder signedResponseBuilder = SignedResponse.newBuilder();

		try{
			MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGO);
			messageDigest.update(responseBinary);
			byte[] digest = messageDigest.digest();


			Cipher cipher = Cipher.getInstance(SYM_CIPHER);

			Key key = readKey("secret.key");
			cipher.init(Cipher.ENCRYPT_MODE, key);

			byte[] cipherBytes = cipher.doFinal(digest);
			ByteString.Output rep = ByteString.newOutput();

			rep.write(cipherBytes,0,cipherBytes.length);

			Signature.Builder signatureBuilder = Signature.newBuilder();
			signatureBuilder.setSignerId("Server");
			signatureBuilder.setValue(rep.toByteString());

			rep.reset();

			signedResponseBuilder.setSignature(signatureBuilder.build());

		} catch (NoSuchAlgorithmException e){
			System.out.println("Alghoritm " + DIGEST_ALGO + " does not exist!");
		} catch (NoSuchPaddingException e){
			System.out.println("Ciphers " + SYM_CIPHER + " does not exist!");
		} catch (Exception e){
			System.out.println(e);
			System.out.println("Something went wrong with key!");
		}
		
		signedResponseBuilder.setResponse(response);

		// send single response back
		responseObserver.onNext(signedResponseBuilder.build());
		// complete call
		responseObserver.onCompleted();
	}

}
