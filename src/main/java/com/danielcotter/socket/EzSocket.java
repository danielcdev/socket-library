package com.danielcotter.socket;

public interface EzSocket {

    public boolean connect(String serverAddress, int port);

    public String read();

    public boolean write(String message);

    public boolean close();
}