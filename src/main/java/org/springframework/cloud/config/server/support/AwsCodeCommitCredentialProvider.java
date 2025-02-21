package org.springframework.cloud.config.server.support;
/*
import com.amazonaws.auth.*;
import com.amazonaws.util.ValidationUtils;
import com.yuvaraj.configserver.ConfigServerApplication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.springframework.util.StringUtils.hasText;
public class AwsCodeCommitCredentialProvider extends CredentialsProvider {

    private static final String SHA_256 = "SHA-256"; 

    private static final String UTF8 = "UTF8"; 

    private static final String HMAC_SHA256 = "HmacSHA256"; 

    private static final char[] hexArray = "0123456789abcdef".toCharArray(); 

    protected Log logger = LogFactory.getLog(getClass());

    private AWSCredentialsProvider awsCredentialProvider;

    private String username;

    private String password;

    protected static String calculateCodeCommitPassword(URIish uri, String awsSecretKey) {
        System.out.println("\n\n !!!! YUVARAJ8 !!!! \n\n\n");
        String[] split = uri.getHost().split("\\.");
        if (split.length < 4) {
            throw new CredentialException("Cannot detect AWS region from URI", null);
        }
        String region = split[1];

        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        String dateStamp = dateFormat.format(now);
        String shortDateStamp = dateStamp.substring(0, 8);

        String codeCommitPassword;
        try {
            StringBuilder stringToSign = new StringBuilder();
            stringToSign.append("AWS4-HMAC-SHA256\n").append(dateStamp).append("\n")
                    .append(shortDateStamp).append("/").append(region)
                    .append("/codecommit/aws4_request\n")
                    .append(bytesToHexString(canonicalRequestDigest(uri)));

            byte[] signedRequest = sign(awsSecretKey, shortDateStamp, region,
                    stringToSign.toString());
            codeCommitPassword = dateStamp + "Z" + bytesToHexString(signedRequest);
        }
        catch (Exception e) {
            throw new CredentialException("Error calculating AWS CodeCommit password", e);
        }

        return codeCommitPassword;
    }

    private static byte[] hmacSha256(String data, byte[] key) throws Exception {
        System.out.println("\n\n !!!! YUVARAJ7 !!!! \n\n\n");
        String algorithm = HMAC_SHA256;
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key, algorithm));
        return mac.doFinal(data.getBytes(UTF8));
    }

    private static byte[] sign(String secret, String shortDateStamp, String region,
                               String toSign) throws Exception {
                                System.out.println("\n\n !!!! YUVARAJ6 !!!! \n\n\n");
        byte[] kSecret = ("AWS4" + secret).getBytes(UTF8);
        byte[] kDate = hmacSha256(shortDateStamp, kSecret);
        byte[] kRegion = hmacSha256(region, kDate);
        byte[] kService = hmacSha256("codecommit", kRegion);
        byte[] kSigning = hmacSha256("aws4_request", kService);
        return hmacSha256(toSign, kSigning);
    }

    private static byte[] canonicalRequestDigest(URIish uri)
            throws NoSuchAlgorithmException {
                System.out.println("\n\n !!!! YUVARAJ5 !!!! \n\n\n");
        StringBuilder canonicalRequest = new StringBuilder();
        canonicalRequest.append("GIT\n") 
                .append(uri.getPath()).append("\n")
                .append("\n") 
                .append("host:").append(uri.getHost()).append("\n").append("\n") 
                .append("host\n"); 

        MessageDigest digest = MessageDigest.getInstance(SHA_256);

        return digest.digest(canonicalRequest.toString().getBytes());
    }

    private static String bytesToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static boolean canHandle(String uri) {
        System.out.println("\n\n !!!! YUVARAJ1 !!!! \n\n\n");
        if (!hasText(uri)) {
            return false;
        }

        try {
            URL url = new URL(uri);
            URI u = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(),
                    url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            if (u.getScheme().equals("https")) {
                String host = u.getHost();
                if (host.endsWith(".amazonaws.com")
                        && host.startsWith("git-codecommit.")) {
                    return true;
                }
            }
        }
        catch (Throwable t) {
        }

        return false;
    }

    @Override
    public boolean isInteractive() {
        return false;
    }

    @Override
    public boolean supports(CredentialItem... items) {
        System.out.println("\n\n !!!! YUVARAJ4 !!!! \n\n\n");
        for (CredentialItem i : items) {
            if (i instanceof CredentialItem.Username) {
                continue;
            }
            else if (i instanceof CredentialItem.Password) {
                continue;
            }
            else {
                return false;
            }
        }
        return true;
    }

    private AWSCredentials retrieveAwsCredentials() {
        System.out.println("\n\n !!!! YUVARAJ2 !!!! \n\n\n");
        if (this.awsCredentialProvider == null) {
            if (this.username != null && this.password != null) {
                this.logger.debug("Creating a static AWSCredentialsProvider");
                this.awsCredentialProvider = new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(this.username, this.password));
            }
            else if (ConfigServerApplication.getApplicationContext().containsBean("yuvarajCredentialsProvider")) {
                logger.debug("Using Yuvaraj credentials provider");
                awsCredentialProvider = ConfigServerApplication.getApplicationContext().getBean("oidCredentialsProvider", AWSCredentialsProvider.class);
            }
            else {
                this.logger.debug("Creating a default AWSCredentialsProvider");
                this.awsCredentialProvider = new DefaultAWSCredentialsProviderChain();
            }
        }
        return this.awsCredentialProvider.getCredentials();
    }

    @Override
    public boolean get(URIish uri, CredentialItem... items)
            throws UnsupportedCredentialItem {
                System.out.println("\n\n !!!! YUVARAJ3 !!!! \n\n\n");
        String codeCommitPassword;
        String awsAccessKey;
        String awsSecretKey;
        try {
            AWSCredentials awsCredentials = retrieveAwsCredentials();
            StringBuilder awsKey = new StringBuilder();
            awsKey.append(awsCredentials.getAWSAccessKeyId());
            awsSecretKey = awsCredentials.getAWSSecretKey();
            if (awsCredentials instanceof AWSSessionCredentials) {
                AWSSessionCredentials sessionCreds = (AWSSessionCredentials) awsCredentials;
                if (sessionCreds.getSessionToken() != null) {
                    awsKey.append('%').append(sessionCreds.getSessionToken());
                }
            }
            awsAccessKey = awsKey.toString();
        }
        catch (Throwable t) {
            this.logger.warn("Unable to retrieve AWS Credentials", t);
            return false;
        }
        try {
            codeCommitPassword = calculateCodeCommitPassword(uri, awsSecretKey);
        }
        catch (Throwable t) {
            this.logger.warn("Error calculating the AWS CodeCommit password", t);
            return false;
        }

        for (CredentialItem i : items) {
            if (i instanceof CredentialItem.Username) {
                ((CredentialItem.Username) i).setValue(awsAccessKey);
                this.logger.trace("Returning username " + awsAccessKey);
                continue;
            }
            if (i instanceof CredentialItem.Password) {
                ((CredentialItem.Password) i).setValue(codeCommitPassword.toCharArray());
                this.logger.trace("Returning password " + codeCommitPassword);
                continue;
            }
            if (i instanceof CredentialItem.StringType
                    && i.getPromptText().equals("Password: ")) { 
                ((CredentialItem.StringType) i).setValue(codeCommitPassword);
                this.logger.trace("Returning password string " + codeCommitPassword);
                continue;
            }
            throw new UnsupportedCredentialItem(uri,
                    i.getClass().getName() + ":" + i.getPromptText()); 
        }

        return true;
    }

    @Override
    public void reset(URIish uri) {
    }

    public AWSCredentialsProvider getAwsCredentialProvider() {
        return this.awsCredentialProvider;
    }

    public void setAwsCredentialProvider(AWSCredentialsProvider awsCredentialProvider) {
        this.awsCredentialProvider = awsCredentialProvider;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public class AWSStaticCredentialsProvider implements AWSCredentialsProvider {

        private final AWSCredentials credentials;

        public AWSStaticCredentialsProvider(AWSCredentials credentials) {
            this.credentials = ValidationUtils.assertNotNull(credentials, "credentials");
        }

        public AWSCredentials getCredentials() {
            return this.credentials;
        }

        public void refresh() {
        }

    }

}
*/
