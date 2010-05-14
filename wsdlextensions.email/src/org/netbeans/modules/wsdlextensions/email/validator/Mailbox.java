/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.wsdlextensions.email.validator;

import java.util.Stack;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.AddressException;

/**
 * The Mailbox class represents a "mailbox" token defined by RFC 822.
 * The grammar for this is defined below:
 * <p>
 * <blockquote>
 *    mailbox     =  addr-spec                    ; simple address
 *                /  phrase route-addr            ; name & addr-spec
 *
 *    route-addr  =  "<" [route] addr-spec ">"
 *
 *    route       =  1#("@" domain) ":"           ; path-relative
 *
 *    addr-spec   =  local-part "@" domain        ; global address
 *
 *    local-part  =  word *("." word)             ; uninterpreted
 *                                                ; case-preserved
 *
 *    domain      =  sub-domain *("." sub-domain)
 *
 *    sub-domain  =  domain-ref / domain-literal
 *
 *    domain-ref  =  atom                         ; symbolic reference
 * </blockquote>
 * <p>
 * In addition to following the aforementioned grammar, this class provides
 * facilities to strip out comments and remove white spaces.  Comments are
 * nested, parenthetical statements that may show up in an address.  Moreover,
 * although spaces, special characters, and control characters are not allowed
 * in "atom"s, instead of throwing exceptions, this class simply removes them.
 * This behavior is in line with example A.1.4 in Appendix A.1 of RFC 822.
 * <p>
 * Furthermore, this class doesn NOT deal with URL encoded addresses.  Users of
 * this class must properly pass in unencoded "mailbox" tokens.
 *
 * @author       Alexander Fung
 * @version      
 *
 */
public class Mailbox {

    private String mLocalPart;
    private String mDomain;
    private String mPhrase = "";
    private String[] mRouteTokens;

    public Mailbox() {
    }

    public Mailbox(String mailbox) throws AddressException {
        this();
        unmarshal(mailbox);
    }

    public String getAddressSpec() {
        return mLocalPart + "@" + mDomain;
    }

    public String getNormalizedAddressSpec() {
        return normalize(stripComments(getAddressSpec()));
    }

    public String getLocalPart() {
        return mLocalPart;
    }

    public String getNormalizedLocalPart() {
        return normalize(stripComments(mLocalPart));
    }

    public String getDomain() {
        return mDomain;
    }

    public String getNormalizedDomain() {
        return normalize(stripComments(mDomain));
    }

    public String getPhrase() {
        return mPhrase;
    }

    public String getRoute() {
        StringBuilder routeBuilder = new StringBuilder();
        if (mRouteTokens != null) {
            for (int ii = 0; ii < mRouteTokens.length; ii++) {
                routeBuilder.append(mRouteTokens[ii]).append(",");
            }
            //remove last comma
            routeBuilder.deleteCharAt(routeBuilder.length() - 1);
            //replace by :
            routeBuilder.append(":");
        }
        return routeBuilder.toString();
    }

    public void unmarshal(String mailbox) throws AddressException {

        if (mailbox.indexOf('<') != -1) {
            parseFullAddressSpecification(mailbox);
        } else {
            parseAddressSpecification(mailbox);
        }

        //Validataing the normalized email address
        InternetAddress emailAddress = new InternetAddress(getNormalizedAddressSpec());
    }

    public String marshal() {

        if (mPhrase.equals("")) {
            return getAddressSpec();
        } else {
            String route = getRoute();
            if (route.equals("")) {
                return mPhrase + " <" + getAddressSpec() + ">";
            } else {
                return mPhrase + " <" + getRoute() + " " + getAddressSpec() + ">";
            }
        }
    }

    protected void parseFullAddressSpecification(String fullAddrSpec) {

        int bracket = fullAddrSpec.indexOf("<");
        if (bracket == -1) {
            //throw new InvalidMailboxException();
        }

        mPhrase = fullAddrSpec.substring(0, bracket).trim();
        parseRouteAddress(fullAddrSpec.substring(bracket,
                fullAddrSpec.length()));

    }

    protected void parseRouteAddress(String routeAddr) {

        if (!routeAddr.startsWith("<") || !routeAddr.endsWith(">")) {
            //throw new InvalidMailboxException();
        }

        String newRouteAddr = routeAddr.substring(1, routeAddr.length() - 1);
        int colon = newRouteAddr.indexOf(':');
        if (colon == -1) {
            parseAddressSpecification(newRouteAddr);
        } else {
            parseRoute(newRouteAddr.substring(0, colon));
            parseAddressSpecification(newRouteAddr.substring(colon + 2,
                    newRouteAddr.length()));
        }
    }

    protected void parseRoute(String route) {
        mRouteTokens = route.split(",");
    }

    protected void parseAddressSpecification(String addrSpec) {

        int atSign = addrSpec.indexOf('@');
        if (atSign != -1) {
            mLocalPart = addrSpec.substring(0, atSign);
            mDomain = addrSpec.substring(atSign + 1, addrSpec.length());
        } else {
            //throw new InvalidMailboxException();
        }
    }

    /**
     * All atoms are any characters except specials, space, and control
     * characters.  For now, just remove space 
     *
     * @param        
     * @return       
     * @exception    
     * @see          
     */
    protected String normalize(String token) {
        if (token.startsWith("\"") && token.endsWith("\"")) {
            return token;
        }

        return token.replace(" ", "");
    }

    protected String stripComments(String token) {
        if (token.startsWith("\"") && token.endsWith("\"")) {
            return token;
        }

        String tmp = token;
        boolean hasParens = (tmp.indexOf('(') != -1);
        while (hasParens) {
            Stack<Integer> parens = new Stack<Integer>();
            char[] chars = tmp.toCharArray();
            hasParens = false;
            for (int ii = 0; ii < chars.length; ii++) {
                if (chars[ii] == '(') {
                    parens.push(Integer.valueOf(ii));
                }
                if (chars[ii] == ')') {
                    // Found a match
                    int start = parens.pop().intValue();

                    // Remove the string
                    tmp = tmp.replace(tmp.substring(start, ii + 1), "");

                    // Reinitialize everything and start over
                    parens = new Stack<Integer>();
                    chars = tmp.toCharArray();
                    hasParens = true;
                    break;
                }
            }
        }

        return tmp;
    }
}
