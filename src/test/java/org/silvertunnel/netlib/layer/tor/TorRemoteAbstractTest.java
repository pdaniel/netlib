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
package org.silvertunnel.netlib.layer.tor;

import java.util.logging.Logger;

import org.silvertunnel.netlib.api.NetFactory;
import org.silvertunnel.netlib.api.NetLayer;
import org.silvertunnel.netlib.api.NetLayerIDs;
import org.silvertunnel.netlib.layer.logger.LoggingNetLayer;
import org.silvertunnel.netlib.layer.tcpip.TcpipNetLayer;
import org.silvertunnel.netlib.layer.tls.TLSNetLayer;
import org.silvertunnel.netlib.util.TempfileStringStorage;

/**
 * Base class of Tor RemoteTest classes.
 * 
 * Contains Tor startup as separate test case.
 * 
 * @author hapke
 */
public abstract class TorRemoteAbstractTest {
    private static final Logger log = Logger.getLogger(TorRemoteAbstractTest.class.getName());

    protected static NetLayer loggingTcpipNetLayer;
    protected static NetLayer loggingTlsNetLayer;
    protected static TorNetLayer torNetLayer;
    
    //@Test(timeout=600000)
    protected void initializeTor() throws Exception {
        // do it only once
        if (loggingTcpipNetLayer==null) {
            // create TCP/IP layer
            NetLayer tcpipNetLayer = new TcpipNetLayer();
            loggingTcpipNetLayer = new LoggingNetLayer(tcpipNetLayer, "upper tcpip  ");
            NetFactory.getInstance().registerNetLayer(NetLayerIDs.TCPIP, loggingTcpipNetLayer);
            
            // create TLS/SSL over TCP/IP layer
            TLSNetLayer tlsNetLayer = new TLSNetLayer(loggingTcpipNetLayer);
            loggingTlsNetLayer = new LoggingNetLayer(tlsNetLayer, "upper tls/ssl");
            NetFactory.getInstance().registerNetLayer(NetLayerIDs.TLS_OVER_TCPIP, loggingTlsNetLayer);
    
            // create TCP/IP layer for directory access (use different layer here to get different logging output)
            NetLayer tcpipDirNetLayer = new TcpipNetLayer();
            NetLayer loggingTcpipDirNetLayer = new LoggingNetLayer(tcpipDirNetLayer, "upper tcpip tor-dir  ");
    
            // create Tor over TLS/SSL over TCP/IP layer
            torNetLayer = new TorNetLayer(loggingTlsNetLayer, /*loggingT*/tcpipDirNetLayer, TempfileStringStorage.getInstance());
            NetFactory.getInstance().registerNetLayer(NetLayerIDs.TOR_OVER_TLS_OVER_TCPIP, torNetLayer);
            torNetLayer.waitUntilReady();
        }
        
        // refresh net layer registration
        NetFactory.getInstance().registerNetLayer(NetLayerIDs.TCPIP, loggingTcpipNetLayer);
        NetFactory.getInstance().registerNetLayer(NetLayerIDs.TLS_OVER_TCPIP, loggingTlsNetLayer);
        NetFactory.getInstance().registerNetLayer(NetLayerIDs.TOR_OVER_TLS_OVER_TCPIP, torNetLayer);
    }

    
    ///////////////////////////////////////////////////////
    // helper methods
    ///////////////////////////////////////////////////////
    
    public static String removeHtmlTags(String htmlText) {
        String result = htmlText;
        result = result.replaceAll("<style.+?</style>", "");
        result = result.replaceAll("<.+?>", "");
        return result;
    }
}
