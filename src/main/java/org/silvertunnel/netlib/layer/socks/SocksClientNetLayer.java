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

package org.silvertunnel.netlib.layer.socks;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.silvertunnel.netlib.api.NetAddress;
import org.silvertunnel.netlib.api.NetAddressNameService;
import org.silvertunnel.netlib.api.NetLayer;
import org.silvertunnel.netlib.api.NetLayerStatus;
import org.silvertunnel.netlib.api.NetServerSocket;
import org.silvertunnel.netlib.api.NetSocket;
import org.silvertunnel.netlib.api.impl.DataNetSocket;
import org.silvertunnel.netlib.api.impl.DataNetSocketPair;
import org.silvertunnel.netlib.api.impl.DataNetSocketUtil;
import org.silvertunnel.netlib.api.impl.DataNetSocketWrapper;
import org.silvertunnel.netlib.api.impl.InterconnectUtil;
import org.silvertunnel.netlib.api.util.TcpipNetAddress;

import static org.silvertunnel.netlib.util.ByteArrayUtil.getByteArray;

/**
 * NetLayer that implements a Socks5 client.
 * 
 * This is a very simple implementation
 * (without authentication, without server socket handling).
 *
 * @author hapke
 */
public class SocksClientNetLayer implements NetLayer {
    private static final int BUFFER_SIZE = 1024; 

    private NetLayer lowerNetLayer;
    
    
    /**
     * @param lowerNetLayer    layer that is or forwards to a Socks5 server
     */
    public SocksClientNetLayer(NetLayer lowerNetLayer) {
        this.lowerNetLayer = lowerNetLayer;
    }
    
    /**
     * Create a connection using the Socks5 client protocol
     * over the lowerNetLayer,
     * i.e. the lowerNetLayer should be or should forward to a Socks5 server.
     * 
     * @see NetLayer#createNetSocket(Map, NetAddress, NetAddress)
     */
    public NetSocket createNetSocket(Map<String,Object> localProperties, NetAddress localAddress, NetAddress remoteAddress) throws IOException {
        // create connection to socks server
        DataNetSocket socksServerSocket = new DataNetSocketWrapper(lowerNetLayer.createNetSocket((Map<String,Object>)null, (NetAddress)null, (NetAddress)null));
        DataOutputStream socksOut = socksServerSocket.getDataOutputStream();
        DataInputStream socksIn = socksServerSocket.getDataInputStream();

        //
        // socks negotiation (via socks server)
        //
        final byte[] request1 = getByteArray(
                0x05, 0x01, /*auth method:*/ 0x00);
        final byte[] expectedResponse1 = getByteArray(
                0x05, /*auth method:*/ 0x00);
        socksOut.write(request1);
        socksOut.flush();
        byte[] response1 = new byte[expectedResponse1.length];
        socksIn.readFully(response1);
        if (!Arrays.equals(expectedResponse1, response1)) {
            throw new IOException("could not create connection: socks negotiation failed");
        }
        
        //
        // connection setup (via socks server)
        //
        if (remoteAddress==null) {
            throw new IllegalArgumentException("invalid remoteAddress=null");
        }
        if (!(remoteAddress instanceof TcpipNetAddress)) {
            throw new IllegalArgumentException("not of type TcpipNetAddress: remoteAddress="+remoteAddress);
        }
        // prepare 2nd socks request
        byte[] request2;
        TcpipNetAddress ra = (TcpipNetAddress)remoteAddress;
        int i=0;
        if (ra.getIpaddress()!=null) {
            // connect with IP address
            int addressLen = ra.getIpaddress().length;
            request2 = new byte[4+addressLen+2];
            request2[i++] = 0x05;
            request2[i++] = /*TCP client:*/ 0x01;
            request2[i++] = 0x00;
            request2[i++] = (byte)((addressLen==4) ? /* IPv4*/ 0x01 : /*IPv6*/ 0x04);
            for (int j=0; j<addressLen;) {
                request2[i++] = ra.getIpaddress()[j++];
            }
        } else if (ra.getHostname()!=null) {
            // connect with host name
            int nameLen = ra.getHostname().length();
            if (nameLen>255) {
                throw new IllegalArgumentException("name too long in remoteAddress="+remoteAddress);
            }
            request2 = new byte[4+1+nameLen+2];
            request2[i++] = 0x05;
            request2[i++] = /*TCP client:*/ 0x01;
            request2[i++] = 0x00;
            request2[i++] = /*with domain name:*/ 0x03;
            request2[i++] = /*domain name len:*/ (byte)nameLen;
            char[] name = ra.getHostname().toCharArray();
            for (int j=0; j<nameLen;) {
                request2[i++] = (byte)name[j++];
            }
        } else {
            throw new IllegalArgumentException("invalid remoteAddress="+remoteAddress);
        }
        // append port number to 2nd request
        request2[i++] = (byte)(ra.getPort()/256);
        request2[i++] = (byte)(ra.getPort()%256);
        // prepare 2nd expected response
        byte[] expectedResponse2 = new byte[request2.length];
        System.arraycopy(request2, 0, expectedResponse2, 0, request2.length);
        expectedResponse2[1] = 0x00;
        // action
        socksOut.write(request2);
        socksOut.flush();
        // tolerate responses with different name/address
        byte[] response2 = new byte[5];
        socksIn.readFully(response2);
        if (response2[1]!=0) {
            throw new IOException("could not create connection: socks connection setup failed with response="+response2[1]+" for remoteAddress="+remoteAddress);
        }
        // read the rest of the response
        int remainingByteLen;
        switch (response2[3]) {
        case 0x01: // read IPv4+port
            remainingByteLen = 4+2-1;
            break;
        case 0x04: // read IPv6+port
            remainingByteLen = 16+2-1;
            break;
        case 0x03: // read IPv6+port
            remainingByteLen = 1+response2[4]+2-1;
            break;
        default:
            throw new IOException("could not create connection: socks connection setup failed with response address type="+response2[3]+" for remoteAddress="+remoteAddress);
        }
        socksIn.readFully(new byte[remainingByteLen]);
        /*
        // check for exact response 
        byte[] response2 = new byte[expectedResponse2.length];
        socksIn.readFully(response2);
        if (!Arrays.equals(expectedResponse2, response2)) {
            throw new IOException("could not create connection: socks connection setup failed with response="+response2[1]+" for remoteAddress="+remoteAddress);
        }
        */
        
        // -> connection successfully established 
        
        //
        // copy the streams
        //
        DataNetSocketPair dataNetSocketPair = DataNetSocketUtil.createDataNetSocketPair();
        DataNetSocket higherLayerSocketExported = dataNetSocketPair.getSocket();
        DataNetSocket higherLayerSocketInternallyUsed = dataNetSocketPair.getInvertedSocked();
        DataInputStream higherIn   = higherLayerSocketInternallyUsed.getDataInputStream();
        DataOutputStream higherOut = higherLayerSocketInternallyUsed.getDataOutputStream();
        InterconnectUtil.relayNonBlocking(higherIn, socksOut, socksIn, higherOut, BUFFER_SIZE);

        // result: the new higher layer socket
        return higherLayerSocketExported;
    }

    /**
     * Not implemented.
     * 
     * @see NetLayer#createNetServerSocket(Map, NetAddress)
     * 
     * @throws UnsupportedOperationException
     */
    public NetServerSocket createNetServerSocket(Map<String,Object> properties, NetAddress localListenAddress) {
        throw new UnsupportedOperationException();
    }



    /** @see NetLayer#getStatus() */
    public NetLayerStatus getStatus() {
        return NetLayerStatus.READY;
    }
    
    /** @see NetLayer#waitUntilReady() */
    public void waitUntilReady() {
        // nothing to do
    }

    /** @see NetLayer#clear() */
    public void clear() throws IOException {
        // nothing to do
    }
    
    /** @see NetLayer#getNetAddressNameService() */
    public NetAddressNameService getNetAddressNameService() {
        throw new UnsupportedOperationException();
    }
}
