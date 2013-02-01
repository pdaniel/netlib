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

package org.silvertunnel.netlib.layer.mock;

import org.silvertunnel.netlib.api.NetLayer;
import org.silvertunnel.netlib.api.NetLayerFactory;
import org.silvertunnel.netlib.api.NetLayerIDs;

/**
 * Factory used to manage the default instance of the
 * TcpipNetLayer.
 * This factory will be instantiated via default constructor.
 * 
 * Needed only by convenience-class NetFactory.
 * 
 * @author hapke
 */
public class NopNetLayerFactory implements NetLayerFactory {
    private NetLayer netLayer;
    
    /**
     * @see NetLayerFactory#getNetLayerById(String)
     * 
     * @param netLayerId
     * @return the requested NetLayer if found; null if not found;
     *         it is not guaranteed that the type is TcpipNetLayer
     */
    public synchronized NetLayer getNetLayerById(String netLayerId) {
        if (NetLayerIDs.NOP.equals(netLayerId)) {
            if (netLayer==null) {
                // create a new netLayer instance
                netLayer = new NopNetLayer();
            }
            return netLayer;
        }
        
        // unsupported netLayerId
        return null;
    }
}
