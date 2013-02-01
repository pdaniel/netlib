/*
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

import java.io.UnsupportedEncodingException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.silvertunnel.netlib.layer.tor.util.Encoding;
import org.silvertunnel.netlib.layer.tor.util.Encryption;
import org.silvertunnel.netlib.layer.tor.util.TorException;
import org.silvertunnel.netlib.layer.tor.util.Util;


/**
 * class that represents the Service Descriptor of a hidden service.
 * 
 * @see https://www.torproject.org/doc/design-paper/tor-design.html#sec:rendezvous
 * @see http://gitweb.torproject.org/tor.git?a=blob_plain;hb=HEAD;f=doc/spec/rend-spec.txt
 * 
 * @author Andriy
 * @author Lexi
 * @author hapke
 */
public class RendezvousServiceDescriptor {
    private static final Logger log = Logger.getLogger(RendezvousServiceDescriptor.class.getName());

    /** pattern of a RendezvousServiceDescriptor String */
    private static Pattern serviceDescriptorStringPattern;
    
    /** two days in milliseconds */
    private static final long MAX_SERVICE_DESCRIPTOR_AGE_IN_MS= 2L*24L*60L*60L*1000L;

    private static final String UTF8 = "UTF-8";

    /** descriptor-id */
    private byte[] descriptorId;
    /** version of this descriptor - usually 2 */
    private String version = "2";
    private RSAPublicKey permanentPublicKey;
    /** highest 80 bits of the hash of the permanentPublicKey in base32 */
    private String z;
    /** secret-id */
    private byte[] secretIdPart;
    private Date publicationTime;
    /** recognized and permitted version numbers for use in INTRODUCE cells (currently, we do not support version 3) */
    private Collection<String> protocolVersions = Arrays.asList("2");
    private Collection<SDIntroductionPoint> introductionPoints;
    
    // TODO: can we drop this?
    private String url;
    
    /** private key to sign an own service descriptor */
    private PrivateKey privateKey;
    
    private static final String DEFAULT_SERVICE_DESCRIPTOR_VERSION = "2";

    /**
     * Initialize pattern -
     * do it in a way that exceptions get logged.
     */
    static {
        try {
            serviceDescriptorStringPattern = Pattern.compile(
                    "^(rendezvous-service-descriptor ([a-z2-7]+)\n"+
                    "version (\\d+)\n"+
                    "permanent-key\n(-----BEGIN RSA PUBLIC KEY-----\n.*?-----END RSA PUBLIC KEY-----)\n"+
                    "secret-id-part ([a-z2-7]+)\n"+
                    "publication-time (\\S+ \\S+)\n"+
                    "protocol-versions (\\d+(?:,\\d+)?(?:,\\d+)?(?:,\\d+)?(?:,\\d+)?)\n"+
                    "introduction-points\n-----BEGIN MESSAGE-----\n(.*?)-----END MESSAGE-----\n"+
                    "signature\n)-----BEGIN SIGNATURE-----\n(.*?)-----END SIGNATURE-----",
                    Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE + Pattern.UNIX_LINES);
        } catch (Exception e) {
            log.log(Level.SEVERE, "could not initialze class RendezvousServiceDescriptor", e);
        }
    }
    
    public String toServiceDescriptorString() {
        // protocolVersionsStr: create comma separated String as e.g. "2,3,4"
        StringBuffer protocolVersionsStrBuf = new StringBuffer(10);
        boolean firstProtocolVersion = true;
        for (String protocolVersion : protocolVersions) {
            if (!firstProtocolVersion) {
                protocolVersionsStrBuf.append(",");
            }
            protocolVersionsStrBuf.append(protocolVersion);
            firstProtocolVersion = false;
        }
        String protocolVersionsStr = protocolVersionsStrBuf.toString();
        
        // introductionPointsBase64: create String
        String introductionPointsStr = SDIntroductionPoint.formatMultipleIntroductionPoints(introductionPoints)+"\n";
        byte[] introductionPointsBytes = null;
        try {
            introductionPointsBytes = introductionPointsStr.getBytes(UTF8);
        } catch (UnsupportedEncodingException e) {
        }
        final int BASE64_COLUMN_WITH = 64;
        String introductionPointsBase64 = Encoding.toBase64(introductionPointsBytes, BASE64_COLUMN_WITH);
        
        // build the complete result
        String dataToSignStr = 
            "rendezvous-service-descriptor "+Encoding.toBase32(descriptorId)+"\n"+
            "version "+version+"\n"+
            "permanent-key\n"+
            Encryption.getPEMStringFromRSAPublicKey(permanentPublicKey)+
            "secret-id-part "+Encoding.toBase32(secretIdPart)+"\n"+
            "publication-time "+Util.formatUtcTimestamp(publicationTime)+"\n"+
            "protocol-versions "+protocolVersionsStr+"\n"+
            "introduction-points\n"+
            "-----BEGIN MESSAGE-----\n"+
            introductionPointsBase64+
            "-----END MESSAGE-----\n"+
            "signature\n";
        
        // sign the signatureStr
        String signatureStr = "";
        if (privateKey!=null) {
            // yes we can sign this descriptor
            byte[] dataToSign = null;
            try {
                dataToSign = dataToSignStr.getBytes(Util.UTF8);
            } catch (UnsupportedEncodingException e) {
                log.log(Level.WARNING, "unexpected", e);
            } 
            byte[] signature = Encryption.signData(dataToSign, privateKey);
            signatureStr = Encoding.toBase64(signature, BASE64_COLUMN_WITH); 
        }
           
        // create full descriptor
        return
            dataToSignStr+
            "-----BEGIN SIGNATURE-----\n"+
            signatureStr+
            "-----END SIGNATURE-----\n";
    }

    /**
     * Constructor for creating a service descriptor of the newest support version.
     */
    public RendezvousServiceDescriptor(String hiddenServicePermanentIdBase32, int replica, Date now, RSAPublicKey publicKey, RSAPrivateKey privateKey,
            Collection<SDIntroductionPoint> givenIntroPoints)
            throws TorException {
        this(DEFAULT_SERVICE_DESCRIPTOR_VERSION, hiddenServicePermanentIdBase32, replica, now, publicKey, privateKey, givenIntroPoints);
    }
    /**
     * Constructor for creating a service descriptor
     */
    public RendezvousServiceDescriptor(String version, String hiddenServicePermanentIdBase32, int replica, Date publicationTime, RSAPublicKey publicKey,
            RSAPrivateKey privateKey, Collection<SDIntroductionPoint> givenIntroPoints)
            throws TorException {
        
         if (!DEFAULT_SERVICE_DESCRIPTOR_VERSION.equals(version)) {
            // FIXME: service descriptors of version != 0 are not supported, yet
            throw new TorException("not implemented: service descriptors of version != "+DEFAULT_SERVICE_DESCRIPTOR_VERSION+" are not supported, yet");
        }
        this.version = version;
        RendezvousServiceDescriptorKeyValues calculatedValues = RendezvousServiceDescriptorUtil.getRendezvousDescriptorId(hiddenServicePermanentIdBase32, replica, publicationTime);
        this.descriptorId = calculatedValues.getDescriptorId();
        
        this.publicationTime = publicationTime;
        this.permanentPublicKey = publicKey;
        this.privateKey = privateKey;
        updateURL();
        
        // store introduction-points
        introductionPoints = givenIntroPoints;

        // calculate current time-period
        //TODO: delete?: byte[] rendezvousDescriptorServiceId = Encoding.parseBase32(hiddenServicePermanentIdBase32);
        // get secret-id-part = h(time-period + descriptorCookie + replica)
        this.secretIdPart = calculatedValues.getSecretIdPart();


        /* TODO remove?
        byte[] temp = new byte[introductionPoints.size() * 100];
        int tempFill = 0;
        Iterator<SDIntroductionPoint> i = introductionPoints.iterator();
        while (i.hasNext()) {
            byte[] s = i.next().toString().getBytes();
            System.arraycopy(s, 0, temp, tempFill, s.length);
            tempFill += s.length + 1;
        }
        this.bytesIntroductionPoints = new byte[tempFill];
        System.arraycopy(temp, 0, bytesIntroductionPoints, 0, tempFill);
        */
    }

    /**
     * Constructor for parsing a service descriptor
     * 
     * @param serviceDescriptorStr
     * @param currentDate         is used to check whether the service descriptor is still valid
     */
    protected RendezvousServiceDescriptor(String serviceDescriptorStr, Date currentDate) throws TorException {
        this(serviceDescriptorStr, currentDate, true);
    }
    /**
     * Constructor for parsing a service descriptor
     * 
     * @param serviceDescriptorStr
     * @param currentDate         is used to check whether the service descriptor is still valid
     * @param checkSignature      true=check signature; false(only for testing)=ignore signature
     */
    protected RendezvousServiceDescriptor(String serviceDescriptorStr, Date currentDate, boolean checkSignature) throws TorException {
        try {
            // parse the authorityKeyCertificateStr
            Matcher m = serviceDescriptorStringPattern.matcher(serviceDescriptorStr);
            m.find();
    
            // read several fields
            String descriptorIdBase32 = m.group(2);
            descriptorId = Encoding.parseBase32(descriptorIdBase32);
    
            version = m.group(3);
    
            // read and check public key
            String permanentKeyStr = m.group(4);
            permanentPublicKey = Encryption.extractPublicRSAKey(permanentKeyStr);
            z = RendezvousServiceDescriptorUtil.calculateZFromPublicKey(permanentPublicKey);
            
            String secretIdPartBase32 = m.group(5);
            secretIdPart = Encoding.parseBase32(secretIdPartBase32);
    
            // parse and check publication time
            publicationTime = Util.parseUtcTimestamp(m.group(6));
            if (!isPublicationTimeValid(currentDate)) {
                throw new TorException("invalid publication-time="+publicationTime);
            }
            
            // parse: a comma-separated list of recognized and permitted version numbers for use in INTRODUCE cells
            String protocolVersionsStr = m.group(7);
            protocolVersions = Arrays.asList(protocolVersionsStr.split(","));
            
            // read and parse introduction-points
            String introductionPointsBase64 = m.group(8);
            byte[] introductionPointsBytes = Encoding.parseBase64(introductionPointsBase64);
            String introductionPointsStr = new String(introductionPointsBytes, UTF8);
            introductionPoints = SDIntroductionPoint.parseMultipleIntroductionPoints(introductionPointsStr);
            if (log.isLoggable(Level.FINE)) {
                log.fine("ips = " + introductionPoints);
            }
            
            // read and check signature
            String signatureStr = m.group(9);
            byte[] signature = Encoding.parseBase64(signatureStr);
            String signedDataStr = m.group(1);
            byte[] signedData = null;
            try {
                signedData = signedDataStr.getBytes(Util.UTF8);
            } catch (UnsupportedEncodingException e) {
                log.log(Level.WARNING, "unexpected", e);
            }
            if (checkSignature && !Encryption.verifySignature(signature, permanentPublicKey, signedData)) {
                throw new TorException("dirKeyCertification check failed");
            }
 
        } catch (TorException e) {
            // just pass it
            throw e;
        } catch (Exception e) {
            // convert the exception
            log.log(Level.INFO, "long log", e);
            throw new TorException("could not parse service descriptor:"+e);
        }
    }
    
    /**
     * needs to be called, in case of service descriptor is self-generated and
     * shall be called with toByteArray()
     */
    void updateSignature() throws TorException {
        throw new UnsupportedOperationException("not yet implemented");
        /* TODO
        signature = Encryption.signData(toByteArray(false), privateKey);
        */
    }

    /**
     * for sending the descriptor
     */
    byte[] toByteArray() {
        try {
            return toServiceDescriptorString().getBytes(UTF8);
        } catch (UnsupportedEncodingException e) {
            log.log(Level.WARNING, "may not occur", e);
            return null;
        }
    }
 
    
    private void updateURL() {
        try {
            // create hash of public key
            byte[] hash = Encryption.getDigest(Encryption.getPKCS1EncodingFromRSAPublicKey(permanentPublicKey));
            // take top 80-bits and convert to biginteger
            byte[] h1 = new byte[10];
            System.arraycopy(hash, 0, h1, 0, 10);
            // return encoding
            this.url = Encoding.toBase32(h1)+".onion";

        } catch (Exception e) {
            log.severe("ServiceDescriptor.updateURL(): " + e.getMessage());
            e.printStackTrace();
            this.url = null;
        }
    }

    /**
     * checks whether the timestamp is no older than 24h
     * 
     * @param currentDate
     */
    public boolean isPublicationTimeValid(Date currentDate) {
        if (publicationTime==null) {
            return false;
        }
        if (publicationTime.after(currentDate) || (currentDate.getTime()-publicationTime.getTime()>MAX_SERVICE_DESCRIPTOR_AGE_IN_MS)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return
            "RendezvousServiceDescriptor=(descriptorIdBase32="+Encoding.toBase32(descriptorId)+
            ",publicationTime="+publicationTime+
            ",introductionPoints="+introductionPoints+")";
    }
    
    ///////////////////////////////////////////////////////
    // getters and setters
    ///////////////////////////////////////////////////////
    
    /**
     * returns the z-part of the url
     */
    public String getURL() {
        return url;
    }

    public RSAPublicKey getPermamentPublicKey() {
        return permanentPublicKey;
    }

    public byte[] getDescriptorId() {
        return descriptorId;
    }

    public String getVersion() {
        return version;
    }

    public RSAPublicKey getPermanentPublicKey() {
        return permanentPublicKey;
    }

    public String getZ() {
        return z;
    }

    public byte[] getSecretIdPart() {
        return secretIdPart;
    }

    public Date getPublicationTime() {
        return publicationTime;
    }

    public Collection<String> getProtocolVersions() {
        return protocolVersions;
    }

    public Collection<SDIntroductionPoint> getIntroductionPoints() {
        return introductionPoints;
    }
}
