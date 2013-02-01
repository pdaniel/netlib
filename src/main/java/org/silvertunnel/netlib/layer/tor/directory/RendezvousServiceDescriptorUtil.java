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
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;

import org.silvertunnel.netlib.layer.tor.util.Encoding;
import org.silvertunnel.netlib.layer.tor.util.Encryption;
import org.silvertunnel.netlib.util.ByteArrayUtil;

/**
 * Simple helper methods to use RendezvousServiceDescriptor.
 * 
 * @author hapke
 */
public class RendezvousServiceDescriptorUtil {
    private static final Logger log = Logger.getLogger(RendezvousServiceDescriptorUtil.class.getName());

    /** one day in seconds */
    private static final int TIMEPERIOD_V2_DESC_VALIDITY_SECONDS = 24*60*60;


    /**
     * See http://gitweb.torproject.org/tor.git?a=blob_plain;hb=HEAD;f=doc/spec/rend-spec.txt - chapter 1.2
     * 
     * @param hiddenServicePermanentIdBase32   base32 encoded z (length 16 chars), also known as permanent id,
     *                                         usually left part of the .onion domain
     * @param now                              current time
     * @return base32 encoded descriptorId of z (length 32 chars) and more; not null
     */
    public static RendezvousServiceDescriptorKeyValues getRendezvousDescriptorId(String hiddenServicePermanentIdBase32, int replica, Date now) {
        RendezvousServiceDescriptorKeyValues result = new RendezvousServiceDescriptorKeyValues();
        
        // shared secret between hidden service and its client: currently not used
        byte[] descriptorCookie = null;
        
        // calculate current time-period
        byte[] hiddenServicePermanentId = Encoding.parseBase32(hiddenServicePermanentIdBase32);
        result.setTimePeriod(RendezvousServiceDescriptorUtil.getRendezvousTimePeriod(hiddenServicePermanentId, now));
        
        // calculate secret-id-part = h(time-period + descriptorCookie + replica)
        result.setSecretIdPart(RendezvousServiceDescriptorUtil.getRendezvousSecretIdPart(result.getTimePeriod(), descriptorCookie, replica));
        
        // calculate descriptor ID
        byte[] unhashedDescriptorId = ByteArrayUtil.concatByteArrays(hiddenServicePermanentId, result.getSecretIdPart());
        if (hiddenServicePermanentId.length!=10) {
            log.warning("wrong length of hiddenServicePermanentId="+Arrays.toString(hiddenServicePermanentId));
        }
        result.setDescriptorId(Encryption.getDigest(unhashedDescriptorId));
        
        return result;
    }

    /**
     * See http://gitweb.torproject.org/tor.git?a=blob_plain;hb=HEAD;f=doc/spec/rend-spec.txt - chapter 1.2
     * 
     * @param hiddenServicePermanentIdBase32   base32 encoded z (length 16 chars), also known as permanent id,
     *                                         usually left part of the .onion domain
     * @param now                              current time
     * @return base32 encoded descriptorId of z (length 32 chars)
     */
    public static String getRendezvousDescriptorIdBase32(String hiddenServicePermanentIdBase32, int replica, Date now) {
        return Encoding.toBase32(getRendezvousDescriptorId(hiddenServicePermanentIdBase32, replica, now).getDescriptorId());
    }

    /**
     * See http://gitweb.torproject.org/tor.git?a=blob_plain;hb=HEAD;f=doc/spec/rend-spec.txt - chapter 1.2
     * time-period = (current-time + permanent-id-highest-byte * 86400 / 256) / 86400
     * 
     * @param hiddenServicePermanentId    also known as permanent id
     * @param now
     * @return
     */
    public static int getRendezvousTimePeriod(byte[] hiddenServicePermanentId, Date now) {
        int nowInSeconds = (int)(now.getTime()/1000L);
        int serviceIdHighestByte = (256+hiddenServicePermanentId[0])%256;
        int result = (nowInSeconds + (serviceIdHighestByte * RendezvousServiceDescriptorUtil.TIMEPERIOD_V2_DESC_VALIDITY_SECONDS / 256)) / RendezvousServiceDescriptorUtil.TIMEPERIOD_V2_DESC_VALIDITY_SECONDS;
        return result;
    }

    /**
     * See http://gitweb.torproject.org/tor.git?a=blob_plain;hb=HEAD;f=doc/spec/rend-spec.txt - chapter 1.2
     * 
     * @param timePeriod
     * @param descriptorCookieBytes    can be null
     * @param replica
     * @return h(timePeriod + escriptorCookie + replica)
     */
    public static byte[] getRendezvousSecretIdPart(int timePeriod, byte[] descriptorCookieBytes, int replica) {
        // convert input to byte arrays
        final int BYTES4 = 4;
        byte[] timePeriodBytes = Encoding.intToNByteArray(timePeriod, BYTES4);
        if (descriptorCookieBytes==null) {
            descriptorCookieBytes = new byte[0];
        }
        final int BYTES1 = 1;
        byte[] replicaBytes = Encoding.intToNByteArray(replica, BYTES1);
        
        // calculate digest
        byte[] allBytes = ByteArrayUtil.concatByteArrays(timePeriodBytes, descriptorCookieBytes, replicaBytes);
        byte[] result = Encryption.getDigest(allBytes);
        return result;
    }

    /**
     * Calculate z of domain z.onion as specified in
     * See http://gitweb.torproject.org/tor.git?a=blob_plain;hb=HEAD;f=doc/spec/rend-spec.txt - chapter "1.5. Alice receives a z.onion address"
     *
     * @return z    hiddenServicePermanentIdBase32
     */
    public static String calculateZFromPublicKey(RSAPublicKey publicKey) {
        byte[] publicKeyHash = Encryption.getDigest(Encryption.getPKCS1EncodingFromRSAPublicKey(publicKey));
        byte[] zBytes = new byte[10];
        System.arraycopy(publicKeyHash, 0, zBytes, 0, 10);
        String z = Encoding.toBase32(zBytes);
        return z;
    }
}
