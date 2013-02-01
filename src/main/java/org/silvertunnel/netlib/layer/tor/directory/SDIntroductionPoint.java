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

import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.silvertunnel.netlib.api.util.TcpipNetAddress;
import org.silvertunnel.netlib.layer.tor.api.Fingerprint;
import org.silvertunnel.netlib.layer.tor.util.Encoding;
import org.silvertunnel.netlib.layer.tor.util.Encryption;


/**
 * An object of this class represents a single introduction point of a rendezvous service descriptor.
 * 
 * @see https://www.torproject.org/doc/design-paper/tor-design.html#sec:rendezvous
 * @see http://gitweb.torproject.org/tor.git?a=blob_plain;hb=HEAD;f=doc/spec/rend-spec.txt
 * 
 * @author hapke
 */
public class SDIntroductionPoint {
    private static final Logger log = Logger.getLogger(SDIntroductionPoint.class.getName());

    /** pattern of one introduction-point */
    private static Pattern patternSingle;

    /** part of the service descriptor entry: identifier=base32 fingerprint of the router */
    private String identifier;
    /** part of the service descriptor entry */
    private TcpipNetAddress ipAddressAndOnionPort;
    /** part of the service descriptor entry */
    private RSAPublicKey onionPublicKey;
    /** part of the service descriptor entry */
    private RSAPublicKey servicePublicKey;

    /**
     * Initialize in a way that exceptions get logged.
     */
    static {
        try {
            patternSingle = Pattern.compile(
                    "introduction-point ([a-z2-7]+)\n"+
                    "ip-address (\\d+\\.\\d+\\.\\d+\\.\\d+)\n"+
                    "onion-port (\\d+)\n"+
                    "onion-key\n(-----BEGIN RSA PUBLIC KEY-----\n.*?-----END RSA PUBLIC KEY-----)\n"+
                    "service-key\n(-----BEGIN RSA PUBLIC KEY-----\n.*?-----END RSA PUBLIC KEY-----)",
                    Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE + Pattern.UNIX_LINES);
        } catch (Exception e) {
            log.log(Level.SEVERE, "could not initialze class "+SDIntroductionPoint.class.getName(), e);
        }
    }
    
    /**
     * Parse all introduction points of a service descriptor.
     * 
     * @param introductionPointsStr    introduction points as (decrypted) String
     * @return the parsed
     */
    public static Collection<SDIntroductionPoint> parseMultipleIntroductionPoints(String introductionPointsStr) {
        Collection<SDIntroductionPoint> result = new ArrayList<SDIntroductionPoint>();
        
        Matcher m = patternSingle.matcher(introductionPointsStr);
        for (int i=0; m.find(); i++) {
            // parse a single introduction point
            try {
                SDIntroductionPoint ip = new SDIntroductionPoint(m);
                result.add(ip);

            } catch (Exception e) {
                log.fine("invalid introduction-point i="+i+" skipped");
            }
        }
        
        return result;
    }

    /**
     * Format all introduction points to be a part of a service descriptor.
     * 
     * @param introductionPointsStr    introduction points as (decrypted) String
     * @return the parsed
     */
    public static String formatMultipleIntroductionPoints(Collection<SDIntroductionPoint> introPoints) {
        StringBuffer result = new StringBuffer();
        for (SDIntroductionPoint introPoint : introPoints) {
            result.append(introPoint.toIntroductionPoint());
        }
        return result.toString();
    }

    
    public Fingerprint getIdentifierAsFingerprint() {
        return new FingerprintImpl(Encoding.parseBase32(identifier));
    }

    /**
     * Parse a single introduction point.
     * 
     * @param m    a matcher of patternSingle that found an occurrence 
     */
    private SDIntroductionPoint(Matcher m) {
        identifier = m.group(1);
        
        // IP address and onion port
        String ipAddress = m.group(2);
        int onionPort = Integer.parseInt(m.group(3));
        ipAddressAndOnionPort = new TcpipNetAddress(ipAddress+":"+onionPort);
        
        // parse public keys
        String onionKeyStr = m.group(4);
        onionPublicKey = Encryption.extractPublicRSAKey(onionKeyStr);
        String serviceKeyStr = m.group(5);
        servicePublicKey = Encryption.extractPublicRSAKey(serviceKeyStr);
    }
 
    /**
     * Create a single introduction point.
     * 
     * @param identifier               part of the service descriptor entry: identifier=base32 fingerprint of the router
     * @param ipAddressAndOnionPort    part of the service descriptor entry
     * @param onionPublicKey           part of the service descriptor entry
     * @param servicePublicKey         part of the service descriptor entry
     */
    public SDIntroductionPoint(String identifier, TcpipNetAddress ipAddressAndOnionPort, RSAPublicKey onionPublicKey, RSAPublicKey servicePublicKey) {
        this.identifier = identifier;
        this.ipAddressAndOnionPort = ipAddressAndOnionPort;
        this.onionPublicKey = onionPublicKey;
        this.servicePublicKey = servicePublicKey;
    }
     
    @Override
    public String toString() {
        return identifier+"-"+ipAddressAndOnionPort;
    }

    /**
     * Format this introduction point to be a part of a service descriptor.
     * 
     * @return    introductionPointsStr
     */
    public String toIntroductionPoint() {
        return  "introduction-point "+identifier+"\n"+
                "ip-address "+ipAddressAndOnionPort.getIpaddressAsString()+"\n"+
                "onion-port "+ipAddressAndOnionPort.getPort()+"\n"+
                "onion-key\n"+
                Encryption.getPEMStringFromRSAPublicKey(onionPublicKey)+
                "service-key\n"+
                Encryption.getPEMStringFromRSAPublicKey(servicePublicKey);
    }
    
    ///////////////////////////////////////////////////////
    // getters and setters
    ///////////////////////////////////////////////////////

    public String getIdentifier() {
        return identifier;
    }


    public TcpipNetAddress getIpAddressAndOnionPort() {
        return ipAddressAndOnionPort;
    }


    public RSAPublicKey getOnionPublicKey() {
        return onionPublicKey;
    }


    public RSAPublicKey getServicePublicKey() {
        return servicePublicKey;
    }    
}
