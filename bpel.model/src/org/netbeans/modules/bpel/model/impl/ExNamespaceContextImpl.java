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
package org.netbeans.modules.bpel.model.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.netbeans.modules.xml.xpath.ext.schema.ExNamespaceContext;
import org.netbeans.modules.xml.xpath.ext.schema.InvalidNamespaceException;
import org.netbeans.modules.bpel.model.api.support.Utils;

/**
 * @author ads
 */
public class ExNamespaceContextImpl implements ExNamespaceContext {

    static final String DEFAULT_NS_PREFIX = "ns";      // NOI18N

    public ExNamespaceContextImpl(BpelEntityImpl element) {
        myElement = element;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
     */
    public String getNamespaceURI(String prefix) {
        myElement.readLock();
        try {
            return myElement.lookupNamespaceURI(prefix);
        } finally {
            myElement.readUnlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
     */
    public String getPrefix(String uri) {
        myElement.readLock();
        try {
            String str = myElement.getPeer().lookupPrefix(uri);
            if (str != null && str.length() == 0) {
                // it seems this is some buf in XDM when "" prefix is returned .
                return null;
            }
            return str;
        } finally {
            myElement.readUnlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
     */
    public Iterator getPrefixes(String uri) {
        myElement.readLock();
        try {
            assert uri != null;

            List<String> list = new ArrayList<String>();

            BpelEntityImpl entity = myElement;
            while (entity != null) {
                fillPrefixes(entity, uri, list);
                entity = (BpelEntityImpl) entity.getParent();
            }

            return list.iterator();
        } finally {
            myElement.readUnlock();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.support.ExNamespaceContext#getPrefixes()
     */
    public Iterator<String> getPrefixes() {
        myElement.readLock();
        try {
            List<String> list = new ArrayList<String>();

            BpelEntityImpl entity = myElement;
            while (entity != null) {
                fillPrefixes(entity, null, list);
                entity = (BpelEntityImpl) entity.getParent();
            }

            return list.iterator();
        } finally {
            myElement.readUnlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.support.ExNamespaceContext#addNamespace(java.lang.String)
     */
    public String addNamespace(String uri) throws InvalidNamespaceException {
//System.out.println();
//System.out.println("URI: " + uri);
//System.out.println();
        try {
            new URI(uri);
        } catch (URISyntaxException e) {
            InvalidNamespaceException exc = new InvalidNamespaceException(e.getMessage());
            throw exc;
        }
        myElement.writeLock();
        try {
            if (getPrefix(uri) != null) {
                return getPrefix(uri);
            }
            //
            boolean useDefaultPrefix = false;
            String prefix = PreferredNsPrefixes.getPreferredPrefix(uri);
            if (prefix == null) {
                prefix = DEFAULT_NS_PREFIX;
                useDefaultPrefix = true;
            }
            //
            int i = getMaximumSuffix(prefix);
            if (i == -1) {
                if (useDefaultPrefix) {
                    prefix = prefix + "0"; // NOI18N
                } else {
                    if (isPrefixRegistered(prefix)) {
                        prefix = prefix + "1"; // NOI18N
                    }
                }
            } else {
                i++;
                prefix = prefix + i;
            }
            //
            BpelEntityImpl entity = getRoot();
            entity.addPrefix(prefix, uri);
            return prefix;
        } finally {
            myElement.writeUnlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.support.ExNamespaceContext#addNamespace(java.lang.String,
     *      java.lang.String)
     */
    public void addNamespace(String prefix, String uri)
            throws InvalidNamespaceException {
        assert prefix != null && prefix.length() != 0;
        //
        String registeredUri = getNamespaceURI(prefix);
        if (registeredUri != null && registeredUri.length() > 0 &&
                !registeredUri.equals(uri)) {
            throw new InvalidNamespaceException(
                    "Element's scope already have the prefix \""// NOI18N
                    + prefix + "\" and it declared "// NOI18N
                    + "with different namespace uri"); // NOI18N
        }
        //
        try {
            new URI(uri);
        } catch (URISyntaxException e) {
            InvalidNamespaceException exc = new InvalidNamespaceException(e.getMessage());
            throw exc;
        }
        //
        myElement.writeLock();
        try {
            if (!Utils.checkNCName(prefix)) {
                throw new InvalidNamespaceException("Prefix : '" + prefix +// NOI18N
                        "' is not acceptable as prefix fot namespace."); // NOI18N
            }
            getRoot().addPrefix(prefix, uri);
        } finally {
            myElement.writeUnlock();
        }
    }

    private void fillPrefixes(BpelEntityImpl entity, String uri,
            List<String> list) {
        Map<String, String> map = entity.getPrefixes();
        for (Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (uri == null) {
                list.add(key);
            }
            if ((value != null) && (value.equals(uri))) {
                list.add(key);
            }

        }
    }

    /**
     * Determines if the specified prefix has already reqistered in the context.
     * @param prefix
     * @return
     */
    private boolean isPrefixRegistered(String prefix) {
        assert prefix != null;
        String uri = getNamespaceURI(prefix);
        return (uri != null && uri.length() > 0);
    }

    /**
     * Takes all prefixes of the special form and calculate the maximum 
     * integer value for set of prefixes. 
     * The special form is the following: prepositionXXX. 
     * The prefixes HAVE TO be started with the text, which is specified by 
     * the parameter prefPreposition and CAN contain a positive integer at the end. 
     * Other prefixes are ignored. 
     * The ending integer values are compared and the corresponding maximum
     * value is returned. Otherwize the -1 is returned and it means that 
     * there isn't any registered prefixes of the described form. 
     * 
     * @param pref
     * @return
     */
    private int getMaximumSuffix(String prefPreposition) {
        assert prefPreposition != null;
        //
        Iterator<String> iterator = getPrefixes();
        int maxPrefixIndex = -1;
        while (iterator.hasNext()) {
            String registeredPrefix = iterator.next();
            if (registeredPrefix.startsWith(prefPreposition)) {
                String end = registeredPrefix.substring(prefPreposition.length());
                try {
                    int ind = Integer.parseInt(end);
                    if (ind > maxPrefixIndex) {
                        maxPrefixIndex = ind;
                    }
                } catch (NumberFormatException e) {
                    // we don't care about it.
                }
            }

        }
        return maxPrefixIndex;
    }

    private BpelEntityImpl getRoot() {
        BpelEntityImpl entity = myElement;
        while (entity.getParent() != null) {
            entity = (BpelEntityImpl) entity.getParent();
        }
        return entity;
    }
    private BpelEntityImpl myElement;

}
