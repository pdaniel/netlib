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

package org.silvertunnel.netlib;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;
import org.silvertunnel.netlib.api.NetFactory;
import org.silvertunnel.netlib.api.NetLayer;
import org.silvertunnel.netlib.api.NetLayerIDs;
import org.silvertunnel.netlib.api.NetSocket;
import org.silvertunnel.netlib.api.util.TcpipNetAddress;
import org.silvertunnel.netlib.layer.tcpip.TcpipNetLayer;
import org.silvertunnel.netlib.layer.tls.TLSNetLayer;
import org.silvertunnel.netlib.layer.tor.TorNetLayer;
import org.silvertunnel.netlib.util.TempfileStringStorage;

/**
 * This class summarizes some code fragments to demonstrate the usage of
 * silvertunnel.org Netlib Generic API.
 * 
 * This class directly is visible from Wiki page
 *     http://sourceforge.net/apps/trac/silvertunnel/wiki/NetlibGenericApi
 * and is part of the silvertunnel.org Netlib documentation. The code fragments
 * are collected here instead and not in the Wiki to ensure that they are
 * always valid/correct and compile against the current version of the API.
 * (The author saw wrong/non compilable examples in other
 *  projects documentation too often - therefore we document it here)  
 * 
 * @author hapke
 */
public class NetlibTcpIpUsageExamplesRemoteTest {

    /**
     * Example: Establish a plain TCP/IP connection
     * to host httptest.silvertunnel.org at TCP port 80.
     * 
     * This example shows the preferred way
     * by using the JVM-global NetLayer registry with NetFactory.
     * 
     * More documentation:
     * http://sourceforge.net/apps/trac/silvertunnel/wiki/NetlibGenericApi
     */
    @Test
    public void create_ordinary_TCP_IP_connection_to_a_remote_server() {
        try {
            // define remote address
            String remoteHostname = "httptest.silvertunnel.org";
            int remotePort = 80;
            TcpipNetAddress remoteAddress = new TcpipNetAddress(remoteHostname, remotePort);
            
            // open connection to remote address
            NetSocket netSocket = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TCPIP).
            		createNetSocket(/*without localProperties*/null, /*any localAddress*/null, remoteAddress);
            
            // send and receive data
            // hint: to avoid dead locks: use separate threads for each direction
            OutputStream os = netSocket.getOutputStream();
            InputStream is = netSocket.getInputStream();
            // ... 
            
            // close connection
            netSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
   }

    /**
     * Example: Establish a plain TCP/IP connection
     * to host httptest.silvertunnel.org at TCP port 80.
     * 
     * This example does NOT show preferred way.
     * It does not use the JVM-global NetLayer registry with NetFactory.
     * Instead, it shows the manual creation of the NetLayer.
     * 
     * More documentation:
     * http://sourceforge.net/apps/trac/silvertunnel/wiki/NetlibGenericApi
     */
    @Test
    public void create_ordinary_TCP_IP_connection_to_a_remote_server_without_preinitialized_layerinstance() {
        try {
            // define remote address
            String remoteHostname = "httptest.silvertunnel.org";
            int remotePort = 80;
            TcpipNetAddress remoteAddress = new TcpipNetAddress(remoteHostname, remotePort);
            
            // open connection to remote address
            NetLayer netLayer = new TcpipNetLayer();
            NetSocket netSocket = netLayer.createNetSocket(/*without localProperties*/null, /*any localAddress*/null, remoteAddress);
            
            // send and receive data
            // hint: to avoid dead locks: use separate threads for each direction
            OutputStream os = netSocket.getOutputStream();
            InputStream is = netSocket.getInputStream();
            // ... 
            
            // close connection
            netSocket.close();
    
        } catch (IOException e) {
            e.printStackTrace();
        }
   }


    /**
     * Example: Establish a TCP/IP connection,
     * tunneled through Tor anonymity network,
     * to host httptest.silvertunnel.org at TCP port 80.
     * 
     * This example shows the preferred way
     * by using the JVM-global NetLayer registry with NetFactory.
     * 
     * More documentation:
     * http://sourceforge.net/apps/trac/silvertunnel/wiki/NetlibGenericApi
     */
    // to save time: do not execute @Test
    public void create_TCP_IP_connection_to_a_remote_server_through_Tor() {
        try {
            // define remote address
            String remoteHostname = "httptest.silvertunnel.org";
            int remotePort = 80;
            TcpipNetAddress remoteAddress = new TcpipNetAddress(remoteHostname, remotePort);
    
            // get TorNetLayer instance and wait until it is ready
            NetLayer netLayer = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TOR);
            netLayer.waitUntilReady();
            
            // open connection to remote address - this connection is tunneled through the TOR anonymity network
            NetSocket netSocket = netLayer.createNetSocket(null, null, remoteAddress);
    
            // send and receive data
            // hint: to avoid dead locks: use separate threads for each direction
            OutputStream os = netSocket.getOutputStream();
            InputStream is = netSocket.getInputStream();
            // ... 
            
            // close connection
            netSocket.close();
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Example: Establish a TCP/IP connection,
     * tunneled through Tor anonymity network,
     * to host httptest.silvertunnel.org at TCP port 80.
     * 
     * This example does NOT show the preferred way.
     * It does not use the JVM-global NetLayer registry with NetFactory.
     * Instead, it shows the manual creation of the NetLayer.
     * 
     * More documentation:
     * http://sourceforge.net/apps/trac/silvertunnel/wiki/NetlibGenericApi
     */
    // to save time: do not execute @Test
    public void create_TCP_IP_connection_to_a_remote_server_via_TOR_without_preinitialized_layerinstance() {
        try {
            String remoteHostname = "httptest.silvertunnel.org";
            int remotePort = 80;
            TcpipNetAddress remoteAddress = new TcpipNetAddress(remoteHostname, remotePort);
            
            // create network stack:
            // TOR runs on TLS/SSL layer which runs on TCP/IP layer - let's construct this network stack
            TcpipNetLayer tcpipNetLayer = new TcpipNetLayer();
            TLSNetLayer tlsNetLayer = new TLSNetLayer(tcpipNetLayer);
            TorNetLayer torNetLayer = new TorNetLayer(tlsNetLayer, tcpipNetLayer,
                    /*use local file system to store temporary data:*/TempfileStringStorage.getInstance());
    
            // wait until TOR is ready (optional)
            torNetLayer.waitUntilReady();
            
            // open connection to remote address - this connection is tunneled through the TOR anonymity network;
            NetSocket netSocket = torNetLayer.createNetSocket(/*without localProperties*/null, /*any localAddress*/null, remoteAddress);
            
            // send and receive data
            // hint: to avoid dead locks: use separate threads for each direction
            OutputStream os = netSocket.getOutputStream();
            InputStream is = netSocket.getInputStream();
            // ... 
            
            // close connection
            netSocket.close();
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
