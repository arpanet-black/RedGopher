package black.arpanet.util.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ArpanetHashUtil {
	
	public static final String DIGEST_ALG = "SHA1";

	public static byte[] hash(String str) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance(DIGEST_ALG);		
		return md.digest(str.getBytes());
	}
}
