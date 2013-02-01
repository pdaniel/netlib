/*
 * @(#)AuthenticationHeader.java    1.9 05/12/01
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.silvertunnel.netlib.adapter.url.impl.net.http;

import java.util.HashMap;
import java.util.Iterator;

/**
 * This class is used to parse the information in WWW-Authenticate: and Proxy-Authenticate:
 * headers. It searches among multiple header lines and within each header line
 * for the best currently supported scheme. It can also return a HeaderParser
 * containing the challenge data for that particular scheme.
 *
 * Some examples:
 *
 * WWW-Authenticate: Basic realm="foo" Digest realm="bar" NTLM
 *  Note the realm parameter must be associated with the particular scheme.
 *
 * or
 * 
 * WWW-Authenticate: Basic realm="foo"
 * WWW-Authenticate: Digest realm="foo",qop="auth",nonce="thisisanunlikelynonce"
 * WWW-Authenticate: NTLM
 * 
 * or 
 *
 * WWW-Authenticate: Basic realm="foo"
 * WWW-Authenticate: NTLM ASKAJK9893289889QWQIOIONMNMN
 *
 * The last example shows how NTLM breaks the rules of rfc2617 for the structure of 
 * the authentication header. This is the reason why the raw header field is used for ntlm.
 *
 * At present, the class chooses schemes in following order :
 *     1. Negotiate (if supported) 
 *      2. Kerberos (if supported) 
 *      3. Digest 
 *      4. NTLM (if supported) 
 *      5. Basic
 * 
 * This choice can be modified by setting a system property:
 *
 *     -Dhttp.auth.preference="scheme"
 *
 * which in this case, specifies that "scheme" should be used as the auth scheme when offered
 * disregarding the default prioritisation. If scheme is not offered then the default priority
 * is used.
 *
 * Attention: when http.auth.preference is set as SPNEGO or Kerberos, it's actually "Negotiate 
 * with SPNEGO" or "Negotiate with Kerberos", which means the user will prefer the Negotiate 
 * scheme with GSS/SPNEGO or GSS/Kerberos mechanism.
 *
 * This also means that the real "Kerberos" scheme can never be set as a preference.
 */

public class AuthenticationHeader {
    
    MessageHeader rsp; // the response to be parsed
    HeaderParser preferred; 
    String preferredRaw;    // raw Strings
    String host = null; // the hostname for server, 
                        // used in checking the availability of Negotiate

    static String authPref=null;
    
    public String toString() {
        return "AuthenticationHeader: prefer " + preferredRaw;
    }

    static {
    authPref = (String) java.security.AccessController.doPrivileged(
        new java.security.PrivilegedAction() {
             public Object run() {
             return System.getProperty("http.auth.preference");
             }
        });
            
        // http.auth.preference can be set to SPNEGO or Kerberos.
        // In fact they means "Negotiate with SPNEGO" and "Negotiate with
        // Kerberos" separately, so here they are all translated into
        // Negotiate. Read NegotiateAuthentication.java to see how they
        // were used later.
            
    if (authPref != null) {
        authPref = authPref.toLowerCase();
            if(authPref.equals("spnego") || authPref.equals("kerberos")) {
                authPref = "negotiate";
            }
    }
    }

    String hdrname; // Name of the header to look for

    /**
     * parse a set of authentication headers and choose the preferred scheme
     * that we support
     */
    public AuthenticationHeader (String hdrname, MessageHeader response) {
    rsp = response;
    this.hdrname = hdrname;
    schemes = new HashMap<String,SchemeMapValue>();
    parse();
    }
 
    /**
     * parse a set of authentication headers and choose the preferred scheme
     * that we support for a given host
     */
    public AuthenticationHeader (String hdrname, MessageHeader response, String host) {
        this.host = host;
    rsp = response;
    this.hdrname = hdrname;
    schemes = new HashMap<String,SchemeMapValue>();
    parse();
    }

    /* we build up a map of scheme names mapped to SchemeMapValue objects */
    static class SchemeMapValue {
    SchemeMapValue (HeaderParser h, String r) {raw=r; parser=h;}
    String raw;    
    HeaderParser parser;
    }

    HashMap<String,SchemeMapValue> schemes; 

    /* Iterate through each header line, and then within each line.
     * If multiple entries exist for a particular scheme (unlikely)
     * then the last one will be used. The
     * preferred scheme that we support will be used.
     */
    private void parse () {
    Iterator<String> iter = rsp.multiValueIterator (hdrname);
    while (iter.hasNext()) {
        String raw = (String)iter.next();
        HeaderParser hp = new HeaderParser (raw);
        Iterator<Object> keys = hp.keys();
        int i, lastSchemeIndex;
        for (i=0, lastSchemeIndex = -1; keys.hasNext(); i++) {
        keys.next();
        if (hp.findValue(i) == null) { /* found a scheme name */
            if (lastSchemeIndex != -1) {
            HeaderParser hpn = hp.subsequence (lastSchemeIndex, i);
            String scheme = hpn.findKey(0);
            schemes.put (scheme, new SchemeMapValue (hpn, raw));
            }
            lastSchemeIndex = i;
        }
        }
        if (i > lastSchemeIndex) {
        HeaderParser hpn = hp.subsequence (lastSchemeIndex, i);
        String scheme = hpn.findKey(0);
        schemes.put (scheme, new SchemeMapValue (hpn, raw));
        }
    }

    /* choose the best of them, the order is
         * negotiate -> kerberos -> digest -> ntlm -> basic
         */
    SchemeMapValue v = null;
    if (authPref == null || (v=(SchemeMapValue)schemes.get (authPref)) == null) {
           
            if(v == null) {
                SchemeMapValue tmp = (SchemeMapValue)schemes.get("negotiate");
                if(tmp != null) {
                    if(host == null || !NegotiateAuthentication.isSupported(host, "Negotiate")) {
                        tmp = null;
                    }
                    v = tmp;
                }
            }

            if(v == null) {
                SchemeMapValue tmp = (SchemeMapValue)schemes.get("kerberos");
                if(tmp != null) {
                    // the Kerberos scheme is only observed in MS ISA Server. In
                    // fact i think it's a Kerberos-mechnism-only Negotiate.
                    // Since the Kerberos scheme is always accompanied with the
                    // Negotiate scheme, so it seems impossible to reach this
                    // line. Even if the user explicitly set http.auth.preference
                    // as Kerberos, it means Negotiate with Kerberos, and the code
                    // will still tried to use Negotiate at first.
                    //
                    // The only chance this line get executed is that the server
                    // only suggest the Kerberos scheme.
                    if(host == null || !NegotiateAuthentication.isSupported(host, "Kerberos")) {
                        tmp = null;
                    }
                    v = tmp;
                }
            }
            
            if(v == null) {
                if ((v=(SchemeMapValue)schemes.get ("digest")) == null) {
                    if (((v=(SchemeMapValue)schemes.get("ntlm"))==null)) {
                        v = (SchemeMapValue)schemes.get ("basic");
                    }
                }
        }
    }
    if (v != null) {
        preferred = v.parser;
        preferredRaw = v.raw;;
    } 
    }

    /**
     * return a header parser containing the preferred authentication scheme (only).
     * The preferred scheme is the strongest of the schemes proposed by the server.
     * The returned HeaderParser will contain the relevant parameters for that scheme
     */
    public HeaderParser headerParser() {
    return preferred;
    }

    /**
     * return the name of the preferred scheme
     */
    public String scheme() {
    if (preferred != null) {
        return preferred.findKey(0);
    } else {
        return null;
    }
    }

    /* return the raw header field for the preferred/chosen scheme */

    public String raw () {
    return preferredRaw;
    }

    /**
     * returns true is the header exists and contains a recognised scheme
     */
    public boolean isPresent () {
    return preferred != null;
    }
}
