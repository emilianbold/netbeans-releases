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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.languages;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.ErrorManager;
import org.openide.util.Lookup;

import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;


public class LibrarySupport {

    
    public static LibrarySupport create (String resourceName) {
        return new LibrarySupport (resourceName);
    }
    
    private String resourceName;
    
    private LibrarySupport (String resourceName) {
        this.resourceName = resourceName;
    }
    
    
    private Map<String,List> keys = new HashMap ();
    
    public List<String> getItems (String context) {
        List k = (List) keys.get (context);
        if (k == null) {
            Map m = getItems ().get (context);
            if (m == null) return null;
            k = new ArrayList (m.keySet ());
            Collections.sort (k);
            k = Collections.unmodifiableList (k);
            keys.put (context, k);
        }
        return k;
    }
    
    public String getProperty (String context, String item, String propertyName) {
        Map m = getItems ().get (context);
        if (m == null) return null;
        m = (Map) m.get (item);
        if (m == null) return null;
        return (String) m.get (propertyName);
    }
    
    
    // generics support methods ................................................
    
    private Map<String,Map> items;
    
    private Map<String,Map> getItems () {
        if (items == null)
            try {
                XMLReader reader = XMLUtil.createXMLReader ();
                Handler handler = new Handler ();
                reader.setEntityResolver (handler);
                reader.setContentHandler (handler);
                ClassLoader loader = (ClassLoader) Lookup.getDefault ().
                    lookup (ClassLoader.class);
                InputStream is = loader.getResourceAsStream (resourceName);
                try {
                    reader.parse (new InputSource (is));
                } finally {
                    is.close ();
                }
                items = handler.result;
            } catch (Exception ex) {
                ErrorManager.getDefault ().notify (ex);
                items = Collections.EMPTY_MAP;
            }
        return items;
    }
    
    static class Handler extends DefaultHandler {
        
        Map<String,Map> result = new HashMap ();
        
        public void startElement (
            String uri, 
            String localName,
            String name, 
            Attributes attributes
        ) throws SAXException {
            try {
                if (name.equals ("node")) {
                    String contexts = attributes.getValue ("context");
                    String key = attributes.getValue ("key");
                    Map properties = null;
                    if (attributes.getLength () > 2) {
                        properties = new HashMap ();
                        int i, k = attributes.getLength ();
                        for (i = 0; i < k; i++) {
                            String propertyName = attributes.getQName (i);
                            if ("context".equals (propertyName)) continue;
                            if ("key".equals (propertyName)) continue;
                            properties.put (propertyName, attributes.getValue (i));
                        }
                    }
                    while (true) {
                        int i = contexts.indexOf (',');
                        String context = i >= 0 ? 
                            contexts.substring (0, i).trim () : contexts;
                        Map c = (Map) result.get (context);
                        if (c == null) {
                            c = new HashMap ();
                            result.put (context, c);
                        }
                        if (c.containsKey (key))
                            throw new IllegalArgumentException ("Key " + context + "-" + key + " already exists!");
                        c.put (key, properties);
                        if (i < 0) break;
                        contexts = contexts.substring (i + 1);
                    }
                } 
            } catch (Exception ex) {
                ErrorManager.getDefault ().notify (ex);
            }
        }
        
        public InputSource resolveEntity (String pubid, String sysid) {
            return new InputSource (
                new java.io.ByteArrayInputStream (new byte [0])
            );
        }
    }
}
