/*
 * silvertunnel.org Netlib - Java library to easily access anonymity networks
 * Copyright (c) 2009-2013 silvertunnel.org
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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.silvertunnel.netlib.api.util.TcpipNetAddress;
import org.silvertunnel.netlib.layer.tor.api.Fingerprint;
import org.silvertunnel.netlib.layer.tor.common.TorConfig;
import org.silvertunnel.netlib.layer.tor.util.Encoding;

/**
 * This class manages the hard-coded Tor authorities.
 * 
 * @author hapke
 */
public class AuthorityServers {
    private static final Logger log = Logger.getLogger(AuthorityServers.class.getName());

    /**
     * it follows the list of official authorized authorityKeyCertificate identity keys/fingerprints
     *
     * This list is derived from the list of the original C implementation under
     *    https://gitweb.torproject.org/tor.git/blob/HEAD:/src/or/config.c
     *     http://gitweb.torproject.org/tor.git?a=blob;f=src/or/config.c;hb=HEAD
     *    https://git.torproject.org/checkout/tor/master/src/or/config.c (former URL)
     * and based of the following code fragment.
     * 
     * Only the identity keys/fingerprints are used here. The rest is ignored.
     *
            Add the default directory authorities directly into the trusted dir list,
                but only add them insofar as they share bits with <b>type</b>.
            static void
            add_default_trusted_dir_authorities(authority_type_t type)
            {
              int i;
              const char *dirservers[] = {
                "moria1 orport=9101 no-v2 "
                  "v3ident=D586D18309DED4CD6D57C18FDB97EFA96D330566 "
                  "128.31.0.39:9131 9695 DFC3 5FFE B861 329B 9F1A B04C 4639 7020 CE31",
                "tor26 v1 orport=443 v3ident=14C131DFC5C6F93646BE72FA1401C02A8DF2E8B4 "
                  "86.59.21.38:80 847B 1F85 0344 D787 6491 A548 92F9 0493 4E4E B85D",
                "dizum orport=443 v3ident=E8A9C45EDE6D711294FADF8E7951F4DE6CA56B58 "
                  "194.109.206.212:80 7EA6 EAD6 FD83 083C 538F 4403 8BBF A077 587D D755",
                "Tonga orport=443 bridge no-v2 82.94.251.203:80 "
                  "4A0C CD2D DC79 9508 3D73 F5D6 6710 0C8A 5831 F16D",
                "turtles orport=9090 no-v2 "
                  "v3ident=27B6B5996C426270A5C95488AA5BCEB6BCC86956 "
                  "76.73.17.194:9030 F397 038A DC51 3361 35E7 B80B D99C A384 4360 292B",
                "gabelmoo orport=443 no-v2 "
                  "v3ident=ED03BB616EB2F60BEC80151114BB25CEF515B226 "
                  "212.112.245.170:80 F204 4413 DAC2 E02E 3D6B CF47 35A1 9BCA 1DE9 7281",
                "dannenberg orport=443 no-v2 "
                  "v3ident=585769C78764D58426B8B52B6651A5A71137189A "
                  "193.23.244.244:80 7BE6 83E6 5D48 1413 21C5 ED92 F075 C553 64AC 7123",
                "urras orport=80 no-v2 v3ident=80550987E1D626E3EBA5E5E75A458DE0626D088C "
                  "208.83.223.34:443 0AD3 FA88 4D18 F89E EA2D 89C0 1937 9E0E 7FD9 4417",
                "maatuska orport=80 no-v2 "
                  "v3ident=49015F787433103580E3B66A1707A00E60F2D15B "
                  "171.25.193.9:443 BD6A 8292 55CB 08E6 6FBE 7D37 4836 3586 E46B 3810",
                "Faravahar orport=443 no-v2 "
                  "v3ident=EFCBE720AB3A82B99F9E953CD5BF50F7EEFC7B97 "
                  "154.35.32.5:80 CF6D 0AAF B385 BE71 B8E1 11FC 5CFF 4B47 9237 33BC",
                NULL
              };
              for (i=0; dirservers[i]; i++) {
                if (parse_dir_server_line(dirservers[i], type, 0)<0) {
                  log_err(LD_BUG, "Couldn't parse internal dirserver line %s",
                          dirservers[i]);
                }
              }
            }
          */
    
    private static final String[] rawData = {
        "moria1 orport=9101 no-v2 "+
          "v3ident=D586D18309DED4CD6D57C18FDB97EFA96D330566 "+
          "128.31.0.39:9131 9695 DFC3 5FFE B861 329B 9F1A B04C 4639 7020 CE31",
        "tor26 v1 orport=443 v3ident=14C131DFC5C6F93646BE72FA1401C02A8DF2E8B4 "+
          "86.59.21.38:80 847B 1F85 0344 D787 6491 A548 92F9 0493 4E4E B85D",
        "dizum orport=443 v3ident=E8A9C45EDE6D711294FADF8E7951F4DE6CA56B58 "+
          "194.109.206.212:80 7EA6 EAD6 FD83 083C 538F 4403 8BBF A077 587D D755",
        "Tonga orport=443 bridge no-v2 82.94.251.203:80 "+
          "4A0C CD2D DC79 9508 3D73 F5D6 6710 0C8A 5831 F16D",
        "turtles orport=9090 no-v2 "+
          "v3ident=27B6B5996C426270A5C95488AA5BCEB6BCC86956 "+
          "76.73.17.194:9030 F397 038A DC51 3361 35E7 B80B D99C A384 4360 292B",
        "gabelmoo orport=443 no-v2 "+
          "v3ident=ED03BB616EB2F60BEC80151114BB25CEF515B226 "+
          "212.112.245.170:80 F204 4413 DAC2 E02E 3D6B CF47 35A1 9BCA 1DE9 7281",
        "dannenberg orport=443 no-v2 "+
          "v3ident=585769C78764D58426B8B52B6651A5A71137189A "+
          "193.23.244.244:80 7BE6 83E6 5D48 1413 21C5 ED92 F075 C553 64AC 7123",
        "urras orport=80 no-v2 v3ident=80550987E1D626E3EBA5E5E75A458DE0626D088C "+
          "208.83.223.34:443 0AD3 FA88 4D18 F89E EA2D 89C0 1937 9E0E 7FD9 4417",
        "maatuska orport=80 no-v2 "+
          "v3ident=49015F787433103580E3B66A1707A00E60F2D15B "+
          "171.25.193.9:443 BD6A 8292 55CB 08E6 6FBE 7D37 4836 3586 E46B 3810",
        "Faravahar orport=443 no-v2 "+
          "v3ident=EFCBE720AB3A82B99F9E953CD5BF50F7EEFC7B97 "+
          "154.35.32.5:80 CF6D 0AAF B385 BE71 B8E1 11FC 5CFF 4B47 9237 33BC",
    };
    
    private static Collection<RouterImpl> parsedAuthorityRouters;
    
    /** pattern of rawData */
    private static Pattern pattern;

    /**
     * Initialize in a way that exceptions get logged.
     */
    static {
        try {
            pattern = Pattern.compile(
                    "^(\\w+) .*?orport=(\\d+) .*?(?:v3ident=(\\w+) .*?)?([0-9\\.]+):(\\d+) ((\\w{4} ){9}(\\w{4}))",
                    Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE + Pattern.UNIX_LINES);
        } catch (Exception e) {
            log.log(Level.SEVERE, "could not initialze class AuthorityKeyCertificate", e);
        }
    }
    
    /**
     * @return the list of (hard-coded) authority servers 
     */
    public static Collection<RouterImpl> getAuthorityRouters() {
        if (parsedAuthorityRouters==null) {
            // initial parsing
            parsedAuthorityRouters = parseAuthorityRouters();
        }
        
        return parsedAuthorityRouters;
    }
    
    /**
     * Parse the hard-coded data.
     * 
     * @return the list of (hard-coded) authority servers
     */
    private static Collection<RouterImpl> parseAuthorityRouters() {
        TorConfig torConfig = new TorConfig(false);
        
        Collection<RouterImpl> result = new ArrayList<RouterImpl>();
        // try to parse the separate authority server entries
        for (String singleRawData : rawData) {
            // one server
            try {
                Matcher m = pattern.matcher(singleRawData);
                if (m.find()) {
                    // extract single values
                    String nickname = m.group(1);
                    int orPort = Integer.parseInt(m.group(2));
                    String v3IdentStr = m.group(3);
                    Fingerprint v3Ident = (v3IdentStr==null) ? null : new FingerprintImpl(Encoding.parseHex(v3IdentStr));
                    TcpipNetAddress netAddress = new TcpipNetAddress(m.group(4)+":0");
                    int dirPort = Integer.parseInt(m.group(5));
                    String fingerprintStr = m.group(6);
                    Fingerprint fingerprint = new FingerprintImpl(Encoding.parseHex(fingerprintStr));

                    // create and collect object
                    RouterImpl router = new RouterImpl(torConfig, nickname, InetAddress.getByAddress(netAddress.getIpaddress()), orPort, dirPort, v3Ident, fingerprint);
                    result.add(router);
                } else {
                    log.warning("Did not match to pattern: \""+singleRawData+"\"");
                }
            } catch (Exception e) {
                log.log(Level.INFO, "problem while parsing data, skip: "+singleRawData, e);
            }
        }
        
        return result;
    }
    
    /**
     * @return the list of (hard-coded) "IP:DIRPORT" of the authority servers
     */
    public static Collection<String> getAuthorityIpAndPorts() {
        Collection<String> result = new ArrayList<String>();

        // convert data
        Collection<RouterImpl> authorityRouters = getAuthorityRouters();
        for (RouterImpl router : authorityRouters) {
            String ipAndPort = router.getAddress().getHostAddress()+":"+router.getDirPort();
            result.add(ipAndPort);
        }
        
        return result;
    }

    /**
     * @return the hard-coded list of authority server idents/fingerprints
     */
    static Collection<Fingerprint> getAuthorityDirIdentityKeyDigests() {
        Collection<Fingerprint> result = new ArrayList<Fingerprint>();
 
        // convert data
        Collection<RouterImpl> authorityRouters = getAuthorityRouters();
        for (RouterImpl router : authorityRouters) {
            Fingerprint v3Ident = router.getV3Ident();
            if (v3Ident!=null) {
                result.add(router.getV3Ident());
            }
        }
        
        return result;
    }
}
