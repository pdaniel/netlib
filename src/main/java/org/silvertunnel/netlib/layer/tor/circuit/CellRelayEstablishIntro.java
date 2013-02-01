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

import org.silvertunnel.netlib.layer.tor.directory.HiddenServiceProperties;
import org.silvertunnel.netlib.layer.tor.util.Encoding;
import org.silvertunnel.netlib.layer.tor.util.Encryption;

/**
 * this cell is used to establish introduction point
 * 
 * @author Lexi Pimenidis
 */
public class CellRelayEstablishIntro extends CellRelay {
    public CellRelayEstablishIntro(Circuit c, HiddenServiceProperties service) {
        super(c, RELAY_ESTABLISH_INTRO);
        // 'hash of session info'
        byte[] hsInput = new byte[20 + 9];
        System.arraycopy(c.getRouteNodes()[c.getRouteNodes().length - 1].getKh(), 0, hsInput, 0, 20);
        System.arraycopy("INTRODUCE".getBytes(), 0, hsInput, 20, 9);
        byte[] hs = Encryption.getDigest(hsInput);
        // concat all data
        byte[] pk = Encryption.getPKCS1EncodingFromRSAPublicKey(service.getPublicKey());
        byte[] kl = Encoding.intToNByteArray(pk.length, 2);
        byte[] input = new byte[pk.length + kl.length + hs.length];
        System.arraycopy(kl, 0, input, 0, 2);
        System.arraycopy(pk, 0, input, 2, pk.length);
        System.arraycopy(hs, 0, input, 2 + pk.length, hs.length);
        // signature
        byte[] signature = Encryption.signData(input, service.getPrivateKey());
        // copy to payload
        System.arraycopy(input, 0, data, 0, input.length);
        System.arraycopy(signature, 0, data, input.length, signature.length);
        setLength(input.length + signature.length);
    }
}
