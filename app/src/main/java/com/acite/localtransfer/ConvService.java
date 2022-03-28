package com.acite.localtransfer;

import java.io.IOException;
import java.net.ServerSocket;

public class ConvService {
    private ServerSocket Server;
    public ConvService() throws IOException
    {
        Server = new ServerSocket(5544);
    }
}
