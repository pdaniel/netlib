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

package org.silvertunnel.netlib.layer.tor.directory;

import java.util.Arrays;

import org.silvertunnel.netlib.layer.tor.api.Fingerprint;
import org.silvertunnel.netlib.layer.tor.util.Parsing;


/**
 * A finger print (a HASH_LEN-byte of asn1 encoded public key)
 * of an identity key or signing key of a router or directory server.
 * 
 * An object is read only.
 * 
 * @author hapke
 */
public class FingerprintImpl implements Fingerprint, Cloneable {
    private byte[] bytes;
    /** cache of result of getHex() */
    private String hexCache;
    
    public FingerprintImpl(byte[] identityKey) {
        // check parameter
        if (identityKey==null) {
            throw new NullPointerException();
        }
        if (identityKey.length<4) {
            throw new IllegalArgumentException("invalid array length="+identityKey.length);
        }
        
        // save value
        this.bytes = identityKey;
    }
    
    public String getHex() {
        if (hexCache==null) {
            hexCache = Parsing.renderFingerprint(bytes, false);
        }
        return hexCache;
    }

    public String getHexWithSpaces() {
        return Parsing.renderFingerprint(bytes, true);
    }

    /**
     * @return a copy of the internally byte array
     */
    public byte[] getBytes() {
        byte[] result = new byte[bytes.length];
        System.arraycopy(bytes, 0, result, 0, bytes.length);
        return result;
    }
    
    @Override
    public String toString() {
        return "fingerprintHexWithSpaces="+getHexWithSpaces();
    }
    
    @Override
    public int hashCode() {
        return ((bytes[0]*256)+bytes[1]*256)+bytes[2];
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FingerprintImpl))  {
            return false;
        }
        FingerprintImpl o = (FingerprintImpl)obj;
        return Arrays.equals(this.bytes, o.bytes);
    }

    /**
     * @param other
     * @return      a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object. 
     */
    public int compareTo(Fingerprint other) {
        // performance optimization should be possible if necessary:
        return getHex().compareTo(other.getHex());
    }
    
    /**
     * Clone, but do not throw an exception.
     */
    public Fingerprint cloneReliable() throws RuntimeException {
        try {
            return (Fingerprint)clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
