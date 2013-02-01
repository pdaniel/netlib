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

package org.silvertunnel.netlib.nameservice.cache;

import java.net.UnknownHostException;
import java.util.logging.Logger;

import org.silvertunnel.netlib.api.NetAddress;
import org.silvertunnel.netlib.api.NetAddressNameService;

/**
 * Cache name service requests/responses
 * and forward the requests to the lower name service if necessary.
 * 
 * @author hapke
 */
public class CachingNetAddressNameService implements NetAddressNameService {
    private static Logger log = Logger.getLogger(CachingNetAddressNameService.class.getName());

    private static final int DEFAULT_MAX_ELEMENTS_IN_CACHE = 1000;
    private static final boolean DEFAULT_IS_NAME_CASE_SENSITIVE = false;
    private static final int DEFAULT_CACHE_TTL_SECONDS = 60;
    private static final int DEFAULT_CACHE_NEGATIVE_TTL_SECONDS = 60;
    
    
    /** service to retrieve non-cached entries */
    private NetAddressNameService lowerNetAddressNameService;

    private boolean isNameCaseSensitive;
    
    /** cache positive hits */
    private Cache<String,NetAddress[]> name2AddressesMappingPositive;
    /** cache negative hits; value is always Boolean.TRUE */
    private Cache<String,Boolean>      name2AddressesMappingNegative;
    
    /** cache positive hits */
    private Cache<NetAddress,String[]> address2NamesMappingPositive;
    /** cache negative hits; value is always Boolean.TRUE */
    private Cache<NetAddress,Boolean>  address2NamesMappingNegative;
    
    /**
     * Initialize this name service.
     * 
     * Use default values for maxElementsInCache, cacheTtlSeconds and cacheNegativeTtlSeconds.
     * 
     * @param lowerNetAddressNameService    service to retrieve non-cached entries 
     */
    public CachingNetAddressNameService(NetAddressNameService lowerNetAddressNameService) {
        this(lowerNetAddressNameService, 
                DEFAULT_MAX_ELEMENTS_IN_CACHE, DEFAULT_IS_NAME_CASE_SENSITIVE, 
                DEFAULT_CACHE_TTL_SECONDS, DEFAULT_CACHE_NEGATIVE_TTL_SECONDS);
    }
    
    /**
     * Initialize this name service.
     * 
     * Read more about the JDK time-to-live caching parameters
     * (we use a similar semantic here but the values are specified as constructor parameters):
     *   http://download.oracle.com/javase/1.4.2/docs/guide/net/properties.html
     *   http://www.rgagnon.com/javadetails/java-0445.html
     *
     * @param lowerNetAddressNameService        service to retrieve non-cached entries 
     * @param maxElementsInCache                >0
     * @param boolean isNameCaseSensitive       should be false for DNS caching
     * @param cacheTtlSeconds                   cache time-to-live in seconds = caching period of resolved queries;
     *                                          -1=cache forever if possible; 0=do not cache at all
     * @param cacheNegativeTtlSeconds           negative cache time-to-live in seconds = caching period of non-resolved queries;
     *                                          -1=cache forever if possible; 0=do not cache at all
     */
    public CachingNetAddressNameService(NetAddressNameService lowerNetAddressNameService,
            int maxElementsInCache, boolean isNameCaseSensitive, int cacheTtlSeconds, int cacheNegativeTtlSeconds) {
        this.lowerNetAddressNameService = lowerNetAddressNameService;
        this.isNameCaseSensitive = isNameCaseSensitive;
        
        // initialize internal caches
        name2AddressesMappingPositive = new Cache<String,NetAddress[]>(maxElementsInCache, cacheTtlSeconds);
        name2AddressesMappingNegative = new Cache<String,Boolean>(maxElementsInCache, cacheNegativeTtlSeconds);
        address2NamesMappingPositive = new Cache<NetAddress,String[]>(maxElementsInCache, cacheTtlSeconds);
        address2NamesMappingNegative = new Cache<NetAddress,Boolean>(maxElementsInCache, cacheNegativeTtlSeconds);
    }

    
    /** @see NetAddressNameService#getAddresses */
    public NetAddress[] getAddressesByName(String name) throws UnknownHostException {
        // check parameter
        if (name==null) {
            throw new UnknownHostException("name=null");
        }

        // normalize parameter
        if (!isNameCaseSensitive) {
            name = name.toLowerCase();
        }
        
        // look for the result in the cache
        NetAddress[] result = name2AddressesMappingPositive.get(name);
        if (result!=null) {
            // positive result found in cache
            return result;
        }
        Boolean negativeResult = name2AddressesMappingNegative.get(name);
        if (Boolean.TRUE==negativeResult) {
            // negative result found in cache
            throw new UnknownHostException("name=\""+name+"\" could be resolved in cache as negative result");
        }

        // forward to lower NetAddressNameService
        try {
            result = lowerNetAddressNameService.getAddressesByName(name);
            if (result!=null) {
                // cache positive result
                name2AddressesMappingPositive.put(name, result);
            }
            return result;
        } catch (UnknownHostException e) {
            // cache negative result
            name2AddressesMappingNegative.put(name, Boolean.TRUE);
            throw e;
        }
    }

    /** @see NetAddressNameService#getNames */
    public String[] getNamesByAddress(NetAddress address) throws UnknownHostException {
        // check parameter
        if (address==null) {
            throw new UnknownHostException("address=null");
        }

        // look for the result in the cache
        String[] result = address2NamesMappingPositive.get(address);
        if (result!=null) {
            // positive result found in cache
            return result;
        }
        Boolean negativeResult = address2NamesMappingNegative.get(address);
        if (Boolean.TRUE==negativeResult) {
            // negative result found in cache
            throw new UnknownHostException("address=\""+address+"\" could be resolved in cache as negative result");
        }

        // forward to lower NetAddressNameService
        try {
            result = lowerNetAddressNameService.getNamesByAddress(address);
            if (result!=null) {
                // cache positive result
                address2NamesMappingPositive.put(address, result);
            }
            return result;
        } catch (UnknownHostException e) {
            // cache negative result
            address2NamesMappingNegative.put(address, Boolean.TRUE);
            throw e;
        }
    }
}
