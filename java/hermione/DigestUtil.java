package hermione;

import org.apache.commons.codec.binary.Base64;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by zjh on 15/10/13.
 */
public class DigestUtil {
    public static String base64EncodeBytes(byte[] b) {
        Base64 base64 = new Base64();
        b = base64.encode(b);
        return new String(b);
    }

    public static String getFileHash(String algorithmName, String filePath) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        byte[] dataBytes = new byte[1024];

        int nread = 0;
        try {
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] mdbytes = md.digest();

        return base64EncodeBytes(mdbytes);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(getFileHash("SHA-256", "/Users/zjh/Downloads/test.docx"));
    }
}
