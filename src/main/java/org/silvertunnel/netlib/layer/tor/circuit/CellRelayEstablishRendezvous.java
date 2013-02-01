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
package org.silvertunnel.netlib.layer.tor.circuit;

import org.silvertunnel.netlib.layer.tor.util.TorException;

/**
 * this cell is used to establish rendezvous point
 * 
 * @author Andriy Panchenko
 */
public class CellRelayEstablishRendezvous extends CellRelay {
    public CellRelayEstablishRendezvous(Circuit c, byte[] cookie) throws TorException {
        super(c, RELAY_ESTABLISH_RENDEZVOUS);
        // check whether the cookie size suits into data and is at least 20 bytes
        if (cookie.length<20) {
            throw new TorException("CellRelayEstablishRendezvous: rendevouz-cookie is too small");
        }
        if (cookie.length>data.length) {
            throw new TorException("CellRelayEstablishRendezvous: rendevouz-cookie is too large");
        }

        // copy cookie
        System.arraycopy(cookie, 0, data, 0, cookie.length);
        setLength(cookie.length);
    }
}
