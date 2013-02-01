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

package org.silvertunnel.netlib.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Utilities to handle input streams, output streams and byte arrays.
 * 
 * @author hapke
 */
public class ByteArrayUtil {
    private static final Logger log = Logger.getLogger(ByteArrayUtil.class.getName());

    private static final char SPECIAL_CHAR = '?';

    /**
     * Interpret the byte array as chars as far as possible.
     * 
     * @param b
     * @return
     */
    public static String showAsString(byte[] b) {
        StringBuffer result = new StringBuffer(b.length);
        for (int i=0; i<b.length; i++) {
            result.append(asChar(b[i]));
        }
        return result.toString();
    }
 
    /**
     * Interpret the byte array as chars as far as possible.
     * 
     * @param b
     * @return
     */
    public static String showAsStringDetails(byte[] b) {
        StringBuffer result = new StringBuffer(b.length);
        for (int i=0; i<b.length; i++) {
            result.append(asCharDetail(b[i]));
        }
        return result.toString();
    }
    
    /**
     * Get parameters as byte[].
     */
    public static byte[] getByteArray(int ... bytes) {
        byte[] result = new byte[bytes.length];
        for (int i=0; i<bytes.length; i++) {
            result[i] = (byte)bytes[i];
        }
        return result;
    }

    /**
     * This method creates a byte[] that can e.b. be used for testing with MockNetLayer.
     * 
     * The returned byte[] consist of
     * (prefix as UTF-8 bytes)+(middle sequence 0x00 0x01 ... with numOfBytesInTheMiddle bytes)+(suffix as UTF-8 bytes)
     * 
     * @param prefixStr                not null
     * @param numOfBytesInTheMiddle    >=0
     * @param suffixStr                not null
     * @return the described byte array; not null
     */
    public static byte[] getByteArray(String prefixStr, int numOfBytesInTheMiddle, String suffixStr) {
        try {
            // create the three parts
            byte[] prefix = prefixStr.getBytes("UTF-8");
            byte[] suffix = suffixStr.getBytes("UTF-8");
            byte[] middle = new byte[numOfBytesInTheMiddle];
            for (int i=0; i<middle.length; i++) {
                middle[i] = (byte)i;
            }
            
            // concat the parts
            byte[] result = new byte[prefix.length+middle.length+suffix.length];
            System.arraycopy(prefix, 0, result, 0, prefix.length);
            System.arraycopy(middle, 0, result, prefix.length, middle.length);
            System.arraycopy(suffix, 0, result, prefix.length+middle.length, suffix.length);
            return result;
            
        } catch (UnsupportedEncodingException e) {
            log.log(Level.SEVERE, "", e);
        }
        return new byte[0];
    }

    /**
     * @param b
     * @return b as printable char; or as ? if not printable
     */
    public static char asChar(byte b) {
        if (b<' ' || b>127) {
            return SPECIAL_CHAR;
        } else {
            return (char)b;
        }
    }

    /**
     * See also: BufferedLogger.log().
     * 
     * @param b
     * @return b as printable char; or as ?XX if not printable where XX is the hex code
     */
    public static String asCharDetail(byte b) {
        if (b<' ' || b>127) {
            // add hex value (always two digits)
            int i = b<0 ? 256+b : b;
            String hex = Integer.toHexString(i);
            if (hex.length()<2) {
                return SPECIAL_CHAR+"0"+hex;
            } else {
                return SPECIAL_CHAR+hex;
            }                
        } else {
            return Character.toString((char)b);
        }
    }
 
    /**
     * Read data from is until the buffer is full or the stream is closed.
     * 
     * @param maxResultSize
     * @param is
     * @return the bytes read (length<=maxResultSize).
     */
    public static byte[] readDataFromInputStream(int maxResultSize, InputStream is) throws IOException {
        byte[] tempResultBuffer = new byte[maxResultSize];
        
        int len = 0;
        do {
            if (len>=tempResultBuffer.length) {
                //log.info("result buffer is full");
                break;
            }
            int lastLen=is.read(tempResultBuffer, len, tempResultBuffer.length-len);
            if (lastLen<0) {
                //log.info("end of result stream");
                break;
            }
            len+=lastLen;
        } while (true);
    
        // copy to result buffer
        byte[] result = new byte[len];
        System.arraycopy(tempResultBuffer, 0, result, 0, len);
        
        return result;
    }

    public static byte[] concatByteArrays(byte[]... input) {
        // determine full length
        int len = 0;
        for (int i=0; i<input.length; i++) {
            len += input[i].length;
        }
        byte[] result = new byte[len];
        
        // copy single byte arrays
        int pos = 0;
        for (int i=0; i<input.length; i++) {
            System.arraycopy(input[i], 0, result, pos, input[i].length);
            pos += input[i].length;
        }
        
        return result;
    }
}
