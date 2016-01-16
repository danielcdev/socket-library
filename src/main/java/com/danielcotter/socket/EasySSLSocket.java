package com.danielcotter.socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class EasySSLSocket extends EasySocket {

    private SSLSocketFactory factory;
    private boolean retry = false;

    @Override
    public boolean connect(String address, int port) {
	try {
	    lastError = null;

	    char[] passphrase = "harbleflarblesticksandstems".toCharArray();
	    File file = new File(System.getProperty("user.home"), "sccacerts");
	    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

	    if (!file.exists() || file.length() < 1) {
		file.createNewFile();
		ks.load(null, passphrase);
	    } else {
		InputStream in = new FileInputStream(file);
		ks.load(in, passphrase);
		in.close();
	    }

	    SSLContext context = SSLContext.getInstance("TLS");
	    TrustManagerFactory tmf = TrustManagerFactory
		    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
	    tmf.init(ks);
	    X509TrustManager defaultTrustManager = (X509TrustManager) tmf
		    .getTrustManagers()[0];
	    SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
	    context.init(null, new TrustManager[] { tm }, null);

	    factory = context.getSocketFactory();
	    socket = (SSLSocket) factory.createSocket();
	    socket.connect(new InetSocketAddress(
		    InetAddress.getByName(address), port), 5000);
	    try {
		((SSLSocket) socket).startHandshake();
	    } catch (SSLException e) {
		socket.close();

		if (retry)
		    return false;

		retry = true;
		X509Certificate[] chain = tm.chain;
		X509Certificate cert = chain[0];
		ks.setCertificateEntry(address, cert);
		OutputStream out = new FileOutputStream(file);
		ks.store(out, passphrase);
		out.close();
		connect(address, port);
	    }

	    if (!socket.isConnected() || !isOkay())
		return false;

	    input = new BufferedReader(new InputStreamReader(
		    socket.getInputStream()));
	    output = new BufferedWriter(new OutputStreamWriter(
		    socket.getOutputStream()));

	    return true;
	} catch (Exception e) {
	    String error;

	    if ((error = e.getMessage()) != null)
		lastError = error;

	    return false;
	}
    }

    private static class SavingTrustManager implements X509TrustManager {

	private final X509TrustManager tm;
	private X509Certificate[] chain;

	SavingTrustManager(X509TrustManager tm) {
	    this.tm = tm;
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
	    return new X509Certificate[0];
	}

	@Override
	public void checkClientTrusted(final X509Certificate[] chain,
		final String authType) throws CertificateException {
	    throw new UnsupportedOperationException();
	}

	@Override
	public void checkServerTrusted(final X509Certificate[] chain,
		final String authType) throws CertificateException {
	    this.chain = chain;
	    this.tm.checkServerTrusted(chain, authType);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime * result + ((factory == null) ? 0 : factory.hashCode());
	result = prime * result + (retry ? 1231 : 1237);
	return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (!super.equals(obj))
	    return false;
	if (!(obj instanceof EasySSLSocket))
	    return false;
	EasySSLSocket other = (EasySSLSocket) obj;
	if (factory == null) {
	    if (other.factory != null)
		return false;
	} else if (!factory.equals(other.factory))
	    return false;
	if (retry != other.retry)
	    return false;
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return "EasySSLSocket [socket=" + socket + ", input=" + input
		+ ", output=" + output + ", lastError=" + lastError
		+ ", blocking=" + blocking + ", factory=" + factory
		+ ", retry=" + retry + "]";
    }
}
