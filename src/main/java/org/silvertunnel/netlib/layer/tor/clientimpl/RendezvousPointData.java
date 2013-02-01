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
package org.silvertunnel.netlib.layer.tor.clientimpl;

import org.silvertunnel.netlib.layer.tor.circuit.Circuit;
import org.silvertunnel.netlib.layer.tor.directory.RouterImpl;

/**
 * Simple bean to hold data of a rendezvous point.
 * 
 * @author hapke
 */
class RendezvousPointData {
    private byte[] rendezvousCookie = new byte[20];
    private RouterImpl rendezvousPointRouter = null;
    private Circuit myRendezvousCirc = null;
    
    public RendezvousPointData(byte[] rendezvousCookie, RouterImpl rendezvousPointRouter, Circuit myRendezvousCirc) {
        this.rendezvousCookie = rendezvousCookie;
        this.rendezvousPointRouter = rendezvousPointRouter;
        this.myRendezvousCirc = myRendezvousCirc;
    }

    ///////////////////////////////////////////////////////
    // generated getters
    ///////////////////////////////////////////////////////
    

    public byte[] getRendezvousCookie() {
        return rendezvousCookie;
    }

    public RouterImpl getRendezvousPointRouter() {
        return rendezvousPointRouter;
    }

    public Circuit getMyRendezvousCirc() {
        return myRendezvousCirc;
    }
}
