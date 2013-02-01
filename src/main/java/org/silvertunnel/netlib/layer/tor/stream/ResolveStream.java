/**
 * OnionCoffee - Anonymous Communication through TOR Network
 * Copyright (C) 2005-2007 RWTH Aachen University, Informatik IV
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package org.silvertunnel.netlib.layer.tor.stream;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.silvertunnel.netlib.api.util.IpNetAddress;
import org.silvertunnel.netlib.layer.tor.circuit.CellRelay;
import org.silvertunnel.netlib.layer.tor.circuit.CellRelayResolve;
import org.silvertunnel.netlib.layer.tor.circuit.Circuit;
import org.silvertunnel.netlib.layer.tor.circuit.Queue;
import org.silvertunnel.netlib.layer.tor.common.TorConfig;
import org.silvertunnel.netlib.layer.tor.util.TorException;


/**
 * used to anonymously resolve hostnames
 * 
 * @author Lexi Pimenidis
 */
public class ResolveStream extends TCPStream {
    private static final Logger log = Logger.getLogger(ResolveStream.class.getName());
    
    private static String UTF8 = "UTF-8";
    
    /** wait x seconds for answer */
    private int queueTimeoutS = TorConfig.queueTimeoutResolve;

    public ResolveStream(Circuit c) {
        super(c);
    }

    /**
     * creates a new stream and does an anonymous DNS-Lookup. <br>
     * FIXME: RESOLVED-cells can transport an arbitrary amount of answer-records. currently only the first is returned
     * 
     * @param hostname
     *            a host name to be resolved,
     *            or for a reverse lookup: A.B.C.D.in-addr.arpa
     * @return either an IpNetAddress (normal query),
     *            or a String (reverse-DNS-lookup)
     */
    public Object resolve(String hostname) throws TorException, IOException {
        circuit.assignStreamId(this);
        // add resolved hostname to the history
        circuit.getStreamHistory().add(hostname);
        queue = new Queue(queueTimeoutS);
        setClosed(false);
        if (log.isLoggable(Level.FINE)) {
            log.fine("resolving hostname " + hostname + " on stream " + toString());
        }
        // send RELAY-RESOLV
        sendCell(new CellRelayResolve(this, hostname));
        // wait for RELAY_RESOLVED
        CellRelay relay = queue.receiveRelayCell(CellRelay.RELAY_RESOLVED);
        // read payload
        int len = ((int) (relay.getData()[1]) & 0xff);
        byte[] value = new byte[len];
        byte[] relayData = relay.getData();
        System.arraycopy(relayData, 2, value, 0, value.length);
        // check for error
        if (relayData[0] == (byte) 0xf0) {
            throw new TorException("transient error: " + new String(value));
        }
        if (relayData[0] == (byte) 0xf1) {
            throw new TorException("non transient error: " + new String(value));
        }
        // check return code
        if ((relayData[0] != 0) && (relayData[0] != 4) && (relayData[0] != 6)) {
            throw new TorException("can't handle answers of type " + relayData[0]);
        }
        // return payload
        if (relayData[0] == 0) {
            return new String(value, UTF8);
        } else {
            return new IpNetAddress(value);
        }
    }
}
