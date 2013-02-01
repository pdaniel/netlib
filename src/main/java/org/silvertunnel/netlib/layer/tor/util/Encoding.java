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
package org.silvertunnel.netlib.layer.tor.util;

import java.util.HashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * this class contains utility functions concerning encodings
 * 
 * @author Lexi Pimenidis
 * @author Andriy Panchenko
 * @author Michael Koellejan
 * @author Dipl.-Inf. (FH) Johann Nepomuk Loefflmann
 * @author hapke
 */
public class Encoding {
    private static final Logger log = Logger.getLogger(Encoding.class.getName());

    private static final String HEX_CHARS = "0123456789abcdef";
    private static String[] HEX_LOOKUP = { 
        "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0a", "0b", "0c", "0d", "0e", "0f",
        "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "1a", "1b", "1c", "1d", "1e", "1f", 
        "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "2a", "2b", "2c", "2d", "2e", "2f", 
        "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "3a", "3b", "3c", "3d", "3e", "3f", 
        "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "4a", "4b", "4c", "4d", "4e", "4f",
        "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "5a", "5b", "5c", "5d", "5e", "5f", 
        "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "6a", "6b", "6c", "6d", "6e", "6f", 
        "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "7a", "7b", "7c", "7d", "7e", "7f", 
        "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "8a", "8b", "8c", "8d", "8e", "8f", 
        "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "9a", "9b", "9c", "9d", "9e", "9f", 
        "a0", "a1", "a2", "a3", "a4", "a5", "a6", "a7", "a8", "a9", "aa", "ab", "ac", "ad", "ae", "af", 
        "b0", "b1", "b2", "b3", "b4", "b5", "b6", "b7", "b8", "b9", "ba", "bb", "bc", "bd", "be", "bf",
        "c0", "c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8", "c9", "ca", "cb", "cc", "cd", "ce", "cf", 
        "d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9", "da", "db", "dc", "dd", "de", "df", 
        "e0", "e1", "e2", "e3", "e4", "e5", "e6", "e7", "e8", "e9", "ea", "eb", "ec", "ed", "ee", "ef", 
        "f0", "f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9", "fa", "fb", "fc", "fd", "fe", "ff" 
    };

    private static final String BASE32_CHARS = "abcdefghijklmnopqrstuvwxyz234567";
    private static final int[] BASE32_LOOKUP = {
        0xFF,0xFF,0x1A,0x1B,0x1C,0x1D,0x1E,0x1F, // '0', '1', '2', '3', '4', '5', '6', '7'
        0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF, // '8', '9', ':', ';', '<', '=', '>', '?'
        0xFF,0x00,0x01,0x02,0x03,0x04,0x05,0x06, // '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G'
        0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E, // 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O'
        0x0F,0x10,0x11,0x12,0x13,0x14,0x15,0x16, // 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W'
        0x17,0x18,0x19,0xFF,0xFF,0xFF,0xFF,0xFF, // 'X', 'Y', 'Z', '[', '\', ']', '^', '_'
        0xFF,0x00,0x01,0x02,0x03,0x04,0x05,0x06, // '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g'
        0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E, // 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o'
        0x0F,0x10,0x11,0x12,0x13,0x14,0x15,0x16, // 'p', 'q', 'r', 's', 't', 'u', 'v', 'w'
        0x17,0x18,0x19,0xFF,0xFF,0xFF,0xFF,0xFF  // 'x', 'y', 'z', '{', '|', '}', '~', 'DEL'
    };

    private static final String BASE64_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    private static final Pattern HIDDENADDRESS_X_PATTERN = Parsing.compileRegexPattern("(.*?)\\.");
    private static final Pattern HIDDENADDRESS_Y_PATTERN = Parsing.compileRegexPattern("(.*?)\\.");

    
    /**
     * Converts a byte array to hex string
     */
    public static String toHexString(byte[] block, int columnWidth, int offset, int length) {
        byte[] temp = new byte[length];
        System.arraycopy(block, offset, temp, 0, length);
        return toHexString(temp, columnWidth);
    }

    /**
     * Converts a byte array to hex string
     */
    public static String toHexString(byte[] block, int columnWidth) {
        if (block==null) {
            return "null";
        }
        
        StringBuffer buf = new StringBuffer(4*(block.length+2));
        for (int i = 0; i < block.length; i++) {
            if (i > 0) {
                buf.append(":");
                if (i % (columnWidth / 3) == 0) {
                    buf.append("\n");
                }
            }
            buf.append(HEX_LOOKUP[block[i] & 0xff]);
        }
        return buf.toString();
    }

         /**
     * Converts a byte array to hex string
     */
    public static String toHexStringNoColon(byte[] block) {
        StringBuffer buf = new StringBuffer(4*(block.length+2));
        for (int i = 0; i < block.length; i++) {
            buf.append(HEX_LOOKUP[block[i] & 0xff]);
        }
        return buf.toString();
    }


    public static String toHexString(byte[] block) {
        return toHexString(block, block.length * 3 + 1);
    }

    /**
     * Convert int to the array of bytes
     * 
     * @param myInt
     *            integer to convert
     * @param n
     *            size of the byte array
     * @return byte array of size n
     * 
     */
    public static byte[] intToNByteArray(int myInt, int n) {
        byte[] myBytes = new byte[n];
    
        for (int i = 0; i < n; ++i) {
            myBytes[i] = (byte) ((myInt >> ((n - i - 1) * 8)) & 0xff);
        }
        return myBytes;
    }

    /**
     * wrapper to convert int to the array of 2 bytes
     * 
     * @param myInt
     *            integer to convert
     * @return byte array of size two
     */
    public static byte[] intTo2ByteArray(int myInt) {
        return intToNByteArray(myInt, 2);
    }

    /**
     * Convert the byte array to an int starting from the given offset.
     * 
     * @param b
     *            byte array
     * @param offset
     *            array offset
     * @param length
     *            number of bytes to convert
     * @return integer
     */
    public static int byteArrayToInt(byte[] b, int offset, int length) {
        int value = 0;
        int numbersToConvert = b.length - offset;
    
        int n = Math.min(length, 4); // 4 bytes is max int size (2^32)
        n = Math.min(n, numbersToConvert); // make sure we are not out of array
                                            // bounds
    
        // if (numbersToConvert > 4)
        // offset = b.length - 4; // warning: offset has been changed
        // in order to convert LSB
    
        for (int i = 0; i < n; i++) {
            int shift = (n - 1 - i) * 8;
            value += (b[i + offset] & 0xff) << shift;
        }
        return value;
    }

    /**
     * Convert the byte array to an int
     * 
     * @param b
     *            byte array
     * @return the integer
     * 
     */
    public static int byteArrayToInt(byte[] b) {
        return byteArrayToInt(b, 0, b.length);
    }

    /**
     * converts a notation like 192.168.3.101 into a binary format
     * 
     * @param s
     *            a string containing the dotted notation
     * @return the binary format
     */
    public static long dottedNotationToBinary(String s) {
        long temp = 0;
    
        Pattern p = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)");
        Matcher m = p.matcher(s);
        if (m.find()) {
            for (int i = 1; i <= 4; ++i) {
                temp = temp << 8;
                temp = temp | Integer.parseInt(m.group(i));
            }
        }
    
        return temp;
    }

    /**
     * converts netmask into int - number of significant bits
     * 
     * @param netmask
     *            netmask
     * 
     * @return number of significant bits
     */
    public static int netmaskToInt(long netmask) {
        int result = 0;
        while ((netmask & 0xffffffffL) != 0) {
            netmask = netmask << 1;
            result++;
        }
        return result;
    }

    /**
     * converts our binary format back into dotted-decimal notation
     * 
     * @param ip
     *            binary encoded ip-address.
     */
    public static String binaryToDottedNotation(long ip) {
        StringBuffer dottedNotation = new StringBuffer();
    
        dottedNotation.append(((ip & 0xff000000) >> 24) + ".");
        dottedNotation.append(((ip & 0x00ff0000) >> 16) + ".");
        dottedNotation.append(((ip & 0x0000ff00) >> 8) + ".");
        dottedNotation.append(((ip & 0x000000ff) >> 0));
    
        return dottedNotation.toString();
    }

    /** creates an base64-string out of a byte[] */
    public static String toBase64(byte[] data) {
        return new String(org.bouncycastle.util.encoders.Base64.encode(data));
    }

    /**
     * parses a base64-String. <br>
     * <b>Q</b>: Why doesn't provide Java us with such a functionality? <br>
     * <b>A</b>: Because it sucks. <br>
     * <b>A</b>: RTFM e.g.
     * 
     * @see sun.misc.BASE64Decoder <br>
     * 
     * @param s
     *            a string that contains a base64 encoded array
     * @return the decoded array
     */
    public static byte[] parseBase64(String s) {
        byte[] b = new byte[s.length() + 5]; // size is a upper approximation
                                                // of expected length needed
        int fill = 0;
        char[] c = s.toCharArray();
    
        // main loop
        int temp = 0; // stores the reconstructed bitfield
        int tempFill = 0;
        for (int i = 0; i < c.length; ++i) { // loop over the complete string
            int v = BASE64_CHARS.indexOf(c[i]);
            if (v >= 0) { // base64 char found
                temp = temp << 6;
                temp = temp | v;
                ++tempFill;
                if (tempFill >= 4) { // filled up 24bit
                    System.arraycopy(Encoding.intToNByteArray(temp, 3), 0, b,
                            fill, 3);
                    fill += 3;
                    tempFill = 0;
                    temp = 0;
                }
            } else {
                // the string to be parsed does obviously contain other
                // characters, too.
            }
        }

        if (tempFill > 0) { // end of string, 24 bit buffer still filled?
            if (tempFill == 1) {
                temp = temp << 18;
            }
            if (tempFill == 2) {
                temp = temp << 12;
            }
            if (tempFill == 3) {
                temp = temp << 6;
            }
            System.arraycopy(Encoding.intToNByteArray(temp, 3), 0, b, fill, 3);
    
            if (tempFill == 1) {
                fill += 2;
            }
            if (tempFill == 2) {
                fill += 2;
            }
            if (tempFill == 3) {
                fill += 2;
            }
        }
    
        // copy from temp array to return-array
        byte[] ret = new byte[fill];
        for (int i = 0; i < fill; ++i) {
            ret[i] = b[i];
        }
        return ret;
    }

    /**
     * parses a hex-string into a byte-array.<br>
     * <b>Q</b>: Why doesn't provide Java us with such a functionality? <br>
     * <b>A</b>: Because it sucks.
     * 
     * @param s
     *            a string that contains a hex-encoded array
     * @return the decoded array
     */
    public static byte[] parseHex(String s) {
        byte[] b = new byte[s.length()]; // size is a worst-case
                                            // approximation of needed length
        int p = 0;
        s = s.toLowerCase();
        char[] c = s.toCharArray();
    
        // loop over complete string
        boolean first = true;
        for (int i = 0; i < c.length; ++i) {
            byte temp = (byte) HEX_CHARS.indexOf(c[i]);
            if (temp >= 0) {
                b[p] = (byte) (b[p] | temp);
                first = !first;
                if (first) { // put together two nibbles
                    ++p;
                    if (p >= b.length)
                        return b;
                } else {
                    b[p] = (byte) (b[p] << 4);
                }
            }
        }
    
        // copy to return array
        byte[] ret = new byte[p];
        for (int i = 0; i < p; ++i) {
            ret[i] = b[i];
        }
        return ret;
    }

    public static String toBase64(final byte[] bytes, int columnWidth) {
        String rawResult = toBase64(bytes);
        
        // format columns
        StringBuffer result = new StringBuffer(1+(rawResult.length()+columnWidth)/columnWidth);
        for (int i=0; i<rawResult.length(); i+=columnWidth) {
            String line = rawResult.substring(i, Math.min(rawResult.length(), i+columnWidth));
            result.append(line);
            result.append("\n");
        }
        
        return result.toString();
    }

    static public String toBase32(final byte[] bytes)
        {
            int i =0, index = 0, digit = 0;
            int currByte, nextByte;

            // begin fix
            // added by jonelo@jonelo.de, Feb 13, 2005
            // according to RFC 3548, the encoding must also contain paddings in some cases
            int add=0;
            switch (bytes.length) {
                case 1: add=6; break;
                case 2: add=4; break;
                case 3: add=3; break;
                case 4: add=1; break;
            }
            // end fix

            StringBuffer base32 = new StringBuffer(((bytes.length+7)*8/5)+add);

            while(i < bytes.length)
            {
                currByte = (bytes[i]>=0) ? bytes[i] : (bytes[i]+256); // unsign

                /* Is the current digit going to span a byte boundary? */
                if (index > 3)
                {
                    if ((i+1)<bytes.length)
                        nextByte = (bytes[i+1]>=0) ? bytes[i+1] : (bytes[i+1]+256);
                    else
                        nextByte = 0;

                    digit = currByte & (0xFF >> index);
                    index = (index + 5) % 8;
                    digit <<= index;
                    digit |= nextByte >> (8 - index);
                    i++;
                }
                else
                {
                    digit = (currByte >> (8 - (index + 5))) & 0x1F;
                    index = (index + 5) % 8;
                    if (index == 0)
                        i++;
                }
                base32.append(BASE32_CHARS.charAt(digit));
            }

            // begin fix
            // added by jonelo@jonelo.de, Feb 13, 2005
            // according to RFC 3548, the encoding must also contain paddings in some cases
            switch (bytes.length) {
                case 1: base32.append("======"); break;
                case 2: base32.append("===="); break;
                case 3: base32.append("==="); break;
                case 4: base32.append("="); break;
            }
            // end fix

            return base32.toString();
        }

        static public byte[] parseBase32(final String base32)
        {
            int    i, index, lookup, offset, digit;
            byte[] bytes = new byte[base32.length()*5/8];

            for(i = 0, index = 0, offset = 0; i < base32.length(); i++)
            {
                lookup = base32.charAt(i) - '0';

                /* Skip chars outside the lookup table */
                if ( lookup < 0 || lookup >= BASE32_LOOKUP.length)
                    continue;

                digit = BASE32_LOOKUP[lookup];

                /* If this digit is not in the table, ignore it */
                if (digit == 0xFF)
                    continue;

                if (index <= 3)
                {
                    index = (index + 5) % 8;
                    if (index == 0)
                    {
                       bytes[offset] |= digit;
                       offset++;
                       if(offset>=bytes.length) break;
                    }
                    else
                       bytes[offset] |= digit << (8 - index);
                }
                else
                {
                    index = (index + 5) % 8;
                    bytes[offset] |= (digit >>> index);
                    offset++;

                    if(offset>=bytes.length) break;
                    bytes[offset] |= digit << (8 - index);
                }
            }
            return bytes;
        }

    /**
     * returns a string with a hex-representation of the provided long
     */
    public static String toHex(long n) {
        int[] octet = new int[4];
        octet[0] = (int) ((n >> 24) & 0xff);
        octet[1] = (int) ((n >> 16) & 0xff);
        octet[2] = (int) ((n >> 8) & 0xff);
        octet[3] = (int) ((n) & 0xff);
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < 4; ++i) {
            buf.append(HEX_CHARS.substring(octet[i] >> 4, (octet[i] >> 4) + 1));
            buf.append(HEX_CHARS.substring(octet[i] & 0xf, (octet[i] & 0xf) + 1));
            buf.append(" ");
        }
        return buf.toString();
    }

    /**
     * compares two arrays.
     * 
     * @return true, if the two arrays are equal
     */
    public static boolean arraysEqual(byte[] one, byte[] two) {
        if ((one == null) && (two == null)) {
            return true;
        }
        if ((one != null) && (two == null)) {
            // log.fine("first array contains data, second doesn't");
            return false;
        }
        if ((one == null) && (two != null)) {
            // log.fine("seconds array contains data, first doesn't");
            return false;
        }
        if (one.length != two.length) {
            // log.fine("Different size: "+one.length+" !=
            // "+two.length);
            return false;
        }
        for (int i = 0; i < one.length; ++i) {
            if (one[i] != two[i]) {
                // log.fine("byte "+i+" of "+one.length+" differs:
                // "+one[i]+" != "+two[i]);
                return false;
            }
        }
        return true;
    }

    /**
     * makes hashmap with x,y,z parts of the hidden service address
     * 
     * @param hostname
     *            hostname of the hidden service
     * @return hashmap with keys "x","y","z" and their corresponding values
     */
    public static HashMap<String,String> parseHiddenAddress(String hostname) {
        String x, y, z;
        HashMap<String,String> result = new HashMap<String,String>(3);
    
        z = hostname;
        z = z.replaceFirst(".onion", "");
    
        x = Parsing.parseStringByRE(z, HIDDENADDRESS_X_PATTERN, "");
        z = z.replaceFirst(x + "\\.", "");
    
        y = Parsing.parseStringByRE(z, HIDDENADDRESS_Y_PATTERN, "");
        z = z.replaceFirst(y + "\\.", "");
    
        if (y == "") {
            y = x;
            x = "";
        }
    
        result.put("x", x);
        result.put("y", y);
        result.put("z", z);
    
        return result;
    }

    /**
     * takes a string and returns it urlencoded
     */
    public static String UrlEncode(String input) {
        StringBuffer output = new StringBuffer();
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; ++i) {
            if (Character.isDigit(c[i]) || Character.isLetter(c[i])) {
                output.append(c[i]);
            } else {
                output.append("%" + HEX_LOOKUP[c[i] & 0xff]);
            }
        }
        return output.toString();
    }
}
