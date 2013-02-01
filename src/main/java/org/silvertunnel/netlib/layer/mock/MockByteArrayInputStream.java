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

import java.io.ByteArrayInputStream;

/**
 * Same as ByteArrayInputStream,
 * but wait (block) the specified time if the end is reached.
 * 
 * @author hapke
 */
public class MockByteArrayInputStream extends ByteArrayInputStream {
    private static final long ENDLESS_MS = Long.MAX_VALUE;
    private long waitAtTheEndMs;
    
    private Thread sleepingThread;
    
    /**
     * 
     * @param response
     * @param waitAtTheEndMs    if all data read: wait this time (milliseconds) before "end" is signaled;
     *                           0 = do not wait;
     *                          -1 = wait endless
     */
    public MockByteArrayInputStream(byte[] response, long waitAtTheEndMs) {
        super(response);
        this.waitAtTheEndMs = waitAtTheEndMs;
    }
    
    /**
     * Wait (sleep, block) the specified time.
     * 
     * Wait only once per object.
     */
    private void waitAtTheEnd() {
        if (waitAtTheEndMs==0) {
            // do not wait
            return;
        }
        
        try {
            sleepingThread = Thread.currentThread();
            Thread.sleep((waitAtTheEndMs<0) ? ENDLESS_MS : waitAtTheEndMs);
        } catch (InterruptedException e) {}
        sleepingThread = null;
        
        // wait only once
        waitAtTheEndMs = 0;
    }
    
    ///////////////////////////////////////////////////////
    // modified business methods
    ///////////////////////////////////////////////////////
    
    @Override
    public int read() {
        int result = super.read();
        if (result<0) {
            waitAtTheEnd();
        }
        return result;
    }
    
    @Override
    public int read(byte[] b, int off, int len) {
        int result = super.read(b, off, len);
        if (result<0) {
            waitAtTheEnd();
        }
        return result;
    }
    
    @Override
    public void close() {
        waitAtTheEndMs = 0;

        // let the data end
        super.skip(super.available());
        
        Thread sThread = sleepingThread;
        if (sThread!=null) {
            // finish sleeping
            sThread.interrupt();
        }
    }
}
