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
package org.silvertunnel.netlib.layer.tor.common;

import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;


import com.maxmind.geoip.LookupService;

/**
 * Helper class to access LookupService.
 * 
 * @author hapke
 */
public class LookupServiceUtil {
    private static final Logger log = Logger.getLogger(LookupServiceUtil.class.getName());

    
    private static LookupService lookupService;
        
    static  {
        try {
            lookupService = new LookupService(LookupServiceUtil.class.getResourceAsStream(TorConfig.TOR_GEOIPCITY_PATH), TorConfig.TOR_GEOIPCITY_MAX_FILE_SIZE);
        } catch (Exception e) {
            log.log(Level.SEVERE, "LookupService could not be initialized", e);
        }
    }

    /**
     * Determine the country code of an IP address.
     * 
     * @param address
     * @return the countryCode; ?? if it could not be determined
     */
    public static String getCountryCodeOfIpAddress(InetAddress address) {
        String countryCode = null;
        if (lookupService!=null) {
            countryCode = lookupService.getCountry(address.getAddress()).getCode();
        }
        
        if (countryCode == null || countryCode.length()<1) {
            return "??";
        } else {
            return countryCode;
        }
    }
}
