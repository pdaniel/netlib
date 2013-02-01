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

package org.silvertunnel.netlib.api.service;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Create log output while starting silvertunnel.org Netlib.
 * 
 * @author hapke
 */
public class NetlibVersion {
    private static final Logger log = Logger.getLogger(NetlibVersion.class.getName());
    private static final Logger logNetlib = Logger.getLogger("org.silvertunnel.netlib");
    
    private static NetlibVersion instance;
    
    private static final String VERSION_PROPERTIES = "/org/silvertunnel/netlib/version.properties";

    private String netlibVersionInfo = "unknown";
    
    private static NetlibVersion info = getInstance();

    /**
     * Get an instance.
     * 
     * During the first call additional initialization can happen.
     * 
     * @param initLayersInFirstCall    will be ignored
     * @return an instance of NetlibStartInfo
     */
    public static NetlibVersion getInstance() {
        if (instance==null) {
            // init!?
            synchronized(NetlibVersion.class) {
                if (instance==null) {
                    // init!
                    instance = new NetlibVersion();
                }
            }
        }
        
        return instance;
    }
    
    /**
     * Initialization.
     * 
     * Called only once per JVM.
     */
    private NetlibVersion() {
        // load version info properties
        try {
            InputStream in = getClass().getResourceAsStream(VERSION_PROPERTIES);
            Properties props = new Properties();
            props.load(in);
            netlibVersionInfo =  props.getProperty("netlib.version.info");
        } catch (Exception e) {
            log.log(Level.SEVERE, "error while initializing NetlibStartInfo", e);
        }
        
        // log version info
        logNetlib.info("Welcome to silvertunnel.org Netlib (version "+netlibVersionInfo+")");
    }
    
    /**
     * @return    name of the version of this silvertunnel.org Netlib 
     */
    public String getNetlibVersionInfo() {
    	return netlibVersionInfo;
    }
}
