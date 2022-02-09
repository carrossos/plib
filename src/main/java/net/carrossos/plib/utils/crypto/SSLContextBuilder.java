package net.carrossos.plib.utils.crypto;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class SSLContextBuilder {

	private static final char[] STD_PASSWORD = "changeit".toCharArray();

	private SSLContextBuilder() {
	}

	private static void loadKeyStore(KeyStore ks, Path path, char[] password)
			throws FileNotFoundException, IOException {
		try (InputStream input = new FileInputStream(path.toFile())) {
			try {
				ks.load(input, password);
			} catch (NoSuchAlgorithmException | CertificateException e) {
				throw new IOException("Failed to load keystore", e);
			}
		}
	}

	public static SSLContext newMutualAuthContext(Path keyStore, char[] keyStorePassword, Path trustStore)
			throws IOException {
		try {
			KeyStore ks = KeyStore.getInstance("JKS");
			loadKeyStore(ks, keyStore, keyStorePassword);

			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, keyStorePassword);

			KeyStore ts = KeyStore.getInstance("JKS");
			loadKeyStore(ts, trustStore, STD_PASSWORD);

			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(ts);

			SSLContext context = SSLContext.getInstance("TLSv1.2");
			context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

			return context;
		} catch (UnrecoverableKeyException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException e) {
			throw new IOException("Failed to construct SSL context", e);
		}
	}
}
