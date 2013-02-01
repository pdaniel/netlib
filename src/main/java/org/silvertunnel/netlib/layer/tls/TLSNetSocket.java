/*
 * silvertunnel.org Netlib - Java library to easily access anonymity networks
 * Copyright (c) 2009-2012 silvertunnel.org
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses/>.
 */

package org.silvertunnel.netlib.layer.tls;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.net.ssl.SSLSession;

import org.silvertunnel.netlib.api.NetSocket;

/**
 * NetSocket of TLSNetLayer.
 * 
 * @author hapke
 */
public class TLSNetSocket implements NetSocket {
    /** NetSocket that is doing the communication */
    protected NetSocket innerNetSocket;
    
    protected String lowerLayerSocketInfoMsg;
    
    // additional fields
    /** SSLSession */
    protected SSLSession sslSession;
    
    
    protected TLSNetSocket(NetSocket innerNetSocket, SSLSession sslSession, String lowerLayerSocketInfoMsg) {
        this.innerNetSocket = innerNetSocket;
        this.sslSession = sslSession;
        this.lowerLayerSocketInfoMsg = lowerLayerSocketInfoMsg;
    }
    
    public SSLSession getSSLSession() {
        return sslSession;
    }
    
    @Override
    public String toString() {
        return "TLSNetSocket("+lowerLayerSocketInfoMsg+")";
    }
    
    ///////////////////////////////////////////////////////
    // method calls forwarded to innerNetSocket 
    ///////////////////////////////////////////////////////
    
    public void close() throws IOException {
        innerNetSocket.close();
    }

    public InputStream getInputStream() throws IOException {
        return innerNetSocket.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return innerNetSocket.getOutputStream();
    }
}
