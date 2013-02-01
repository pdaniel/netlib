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

import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.util.logging.Logger;

import org.silvertunnel.netlib.layer.tor.directory.RendezvousServiceDescriptorUtil;
import org.silvertunnel.netlib.layer.tor.util.Encryption;
import org.silvertunnel.netlib.layer.tor.util.RSAKeyPair;


/**
 * Address information of a hidden service
 * needed to create such service (server side).
 * 
 * Inclusive private key of the hidden service.
 * Without port number.
 * 
 * Used by TorHiddenServicePortPrivateNetAddressTorNetLayer.
 *  
 * @author hapke
 */
public class TorHiddenServicePrivateNetAddress {
    private static final Logger log = Logger.getLogger(TorHiddenServicePrivateNetAddress.class.getName());
    
    private RSAPublicKey publicKey;
    private RSAPrivateCrtKey privateKey;
    /** will be calculated once and then cached: publicKeyHash */
    private byte[] publicKeyHash;
    /** will be calculated once and then cached: publicOnionHostName */
    private String publicOnionHostname;

    
    /**
     * Create a new object.
     * 
     * @param publicKey
     * @param privateKey
     */
    public TorHiddenServicePrivateNetAddress(RSAPublicKey publicKey, RSAPrivateCrtKey privateKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;

        // pre-calculate some values
        publicKeyHash = Encryption.getDigest(Encryption.getPKCS1EncodingFromRSAPublicKey(publicKey));;

        String hiddenServicePermanentIdBase32 = RendezvousServiceDescriptorUtil.calculateZFromPublicKey(publicKey);
        publicOnionHostname = hiddenServicePermanentIdBase32 + ".onion";
        
    }
    
    /**
     * @return 20 bytes long hash value 
     */
    public byte[] getPublicKeyHash() {
        return publicKeyHash;
    }
    
    /**
     * 
     * @return host name of the hidden service, like "duskgytldkxiuqc6.onion",
     *         derived from the hidden service public key.
     */
    public String getPublicOnionHostname() {
        return publicOnionHostname;
    }
    
    /**
     * @return a unique id
     */
    protected String getId() {
        return "TorHiddenServicePrivateNetAddress(hostname="+getPublicOnionHostname()+")";
    }
    
    @Override
    public String toString() {
        return getId();
    }

    public String toStringDetails() {
        return "TorHiddenServicePrivateNetAddress(publicKey="+publicKey+", privateKey="+privateKey+")";
    }
    
    @Override
    public int hashCode() {
        return getId().hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj==null || !(obj instanceof TorHiddenServicePrivateNetAddress)) {
            return false;
        }
        
        TorHiddenServicePrivateNetAddress other = (TorHiddenServicePrivateNetAddress)obj;
        return getId().equals(other.getId());
    }

    public RSAKeyPair getKeyPair() {
        return new RSAKeyPair(publicKey, privateKey);
    }
    
    ///////////////////////////////////////////////////////
    // generated getters 
    ///////////////////////////////////////////////////////
    
    public RSAPrivateCrtKey getPrivateKey() {
        return privateKey;
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }
}
