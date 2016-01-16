package com.danielcotter.socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class EasySocket implements EzSocket {

    protected Socket socket;
    protected BufferedReader input;
    protected BufferedWriter output;
    protected String lastError;
    protected boolean blocking = true;

    @Override
    public boolean connect(String address, int port) {
	try {
	    lastError = null;
	    socket = new Socket();
	    socket.connect(new InetSocketAddress(
		    InetAddress.getByName(address), port), 5000);

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

    @Override
    public String read() {
	try {
	    lastError = null;

	    if (!isOkay())
		return (String) null;

	    return input.readLine();
	} catch (Exception e) {
	    String error;

	    if ((error = e.getMessage()) != null)
		lastError = error;

	    return (String) null;
	}
    }

    @Override
    public boolean write(String message) {
	try {
	    lastError = null;

	    if (!isOkay())
		return (Boolean) false;

	    output.write(message, 0, message.length());
	    output.flush();

	    return true;
	} catch (Exception e) {
	    String error;

	    if ((error = e.getMessage()) != null)
		lastError = error;

	    return false;
	}
    }

    @Override
    public boolean close() {
	try {
	    input.close();
	    output.close();
	    socket.close();

	    return true;
	} catch (Exception e) {
	    return false;
	}
    }

    protected boolean isOkay() {
	if (socket == null || socket.isClosed())
	    return false;

	return true;
    }

    /**
     * @return the lastError
     */
    public String getLastError() {
	return lastError;
    }

    /**
     * @return blocking
     */
    public boolean isBlocking() {
	return blocking;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + (blocking ? 1231 : 1237);
	result = prime * result + ((input == null) ? 0 : input.hashCode());
	result = prime * result
		+ ((lastError == null) ? 0 : lastError.hashCode());
	result = prime * result + ((output == null) ? 0 : output.hashCode());
	result = prime * result + ((socket == null) ? 0 : socket.hashCode());
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
	if (obj == null)
	    return false;
	if (!(obj instanceof EasySocket))
	    return false;
	EasySocket other = (EasySocket) obj;
	if (blocking != other.blocking)
	    return false;
	if (input == null) {
	    if (other.input != null)
		return false;
	} else if (!input.equals(other.input))
	    return false;
	if (lastError == null) {
	    if (other.lastError != null)
		return false;
	} else if (!lastError.equals(other.lastError))
	    return false;
	if (output == null) {
	    if (other.output != null)
		return false;
	} else if (!output.equals(other.output))
	    return false;
	if (socket == null) {
	    if (other.socket != null)
		return false;
	} else if (!socket.equals(other.socket))
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
	return "EasySocket [socket=" + socket + ", input=" + input
		+ ", output=" + output + ", lastError=" + lastError
		+ ", blocking=" + blocking + "]";
    }
}
