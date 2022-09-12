
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import com.google.gson.GsonBuilder;
import org.bouncycastle.util.encoders.Encoder;

import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class StringUtil {

	// Applies Sha256 to a string and returns the result.
	public static String applySha256(String input) {

		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");

			// Applies sha256 to our input,
			byte[] hash = digest.digest(input.getBytes("UTF-8"));

			StringBuffer hexString = new StringBuffer(); // This will contain hash as hexidecimal
			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// Applies ECDSA Signature and returns the result ( as bytes ).
	public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
		Signature dsa;
		byte[] output = new byte[0];
		try {
			dsa = Signature.getInstance("ECDSA", "BC");
			dsa.initSign(privateKey);
			byte[] strByte = input.getBytes();
			dsa.update(strByte);
			byte[] realSig = dsa.sign();
			output = realSig;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}

	// Verifies a String signature
	public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
		try {
			Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
			ecdsaVerify.initVerify(publicKey);
			ecdsaVerify.update(data.getBytes());
			return ecdsaVerify.verify(signature);
		} catch (Exception e) {
			System.out.println(StringUtil.getStringFromSignature(signature));
			throw new RuntimeException(e);
		}
	}

	public static String getStringFromSignature(byte[] signature) {

		String sign = new String(org.bouncycastle.util.encoders.Base64.encode(signature));
		
		return sign;

	}

	public static byte[] getSignatureFromString(String signature) {		
		byte[] sign = org.bouncycastle.util.encoders.Base64.decode(signature.getBytes());
		return sign;

	}

	// Short hand helper to turn Object into a json string
	public static String getJson(Object o) {
		return new GsonBuilder().setPrettyPrinting().create().toJson(o);
	}

	// Returns difficulty string target, to compare to hash. eg difficulty of 5 will
	// return "00000"
	public static String getDificultyString(int difficulty) {
		return new String(new char[difficulty]).replace('\0', '0');
	}

	public static String getStringFromKey(Key key) {
		if (key == null) {
			return null;
		}
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}

	public static PublicKey getPublicKeyFromString(String part) {
		byte[] publicBytes;
		try {
			publicBytes = Base64.getDecoder().decode((part.getBytes()));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
		KeyFactory keyFactory;
		PublicKey reciepient = null;
		//if error comes here remove below line
		Security.addProvider(new BouncyCastleProvider());
		try {
			//remove BC if error comes here
			keyFactory = KeyFactory.getInstance("ECDSA", "BC");
			reciepient = keyFactory.generatePublic(keySpec);

		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {

			e.printStackTrace();
		}
		return reciepient;
	}

	public static String getMerkleRoot(ArrayList<Transaction> transactions) {
		int count = transactions.size();

		List<String> previousTreeLayer = new ArrayList<String>();
		for (Transaction transaction : transactions) {
			previousTreeLayer.add(transaction.transactionId);
		}
		List<String> treeLayer = previousTreeLayer;

		while (count > 1) {
			treeLayer = new ArrayList<String>();
			for (int i = 1; i < previousTreeLayer.size(); i += 2) {
				treeLayer.add(applySha256(previousTreeLayer.get(i - 1) + previousTreeLayer.get(i)));
			}
			count = treeLayer.size();
			previousTreeLayer = treeLayer;
		}

		String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
		return merkleRoot;
	}

	public static PrivateKey getPrivateKeyFromString(String part) {
		// System.out.println(part);
		byte[] privateBytes;
		try {
			privateBytes = Base64.getDecoder().decode((part.getBytes()));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateBytes);
		KeyFactory keyFactory;
		PrivateKey reciepient = null;
		try {
			keyFactory = KeyFactory.getInstance("EC");
			reciepient = keyFactory.generatePrivate(keySpec);

		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return reciepient;
	}

	public static void main(String[] args) {
		String privateKey = "MHsCAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQEEYTBfAgEBBBjAUS0uRYI1YhsOIYmdxbK6Y4HS76WleEGgCgYIKoZIzj0DAQGhNAMyAATwcaH0Oj7nYE7YFFfMkqhS3YaTe3dZyQ2xq/5HQ6FlpNRgvHgJcUxC/JBE/vJ5hRs=";
		String publicKey = "MEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAE8HGh9Do+52BO2BRXzJKoUt2Gk3t3WckNsav+R0OhZaTUYLx4CXFMQvyQRP7yeYUb";

		PublicKey publicKey1 = StringUtil.getPublicKeyFromString(publicKey);
		PrivateKey privateKey1 = StringUtil.getPrivateKeyFromString(privateKey);

		String data = "Hello World";
		byte[] signature = StringUtil.applyECDSASig(privateKey1, data);
		// String sign = StringUtil.getStringFromSignaure(signature);
		String sign = Base64.getEncoder().encodeToString(signature);
		System.out.println(sign);

		// byte[] sign_byte = StringUtil.getSignatureFromString(sign);
		byte[] sign_byte = Base64.getDecoder().decode(sign);
		System.out.println(StringUtil.verifyECDSASig(publicKey1, data, sign_byte));

		System.out.println(StringUtil.verifyECDSASig(publicKey1, data, sign_byte));
	}
}