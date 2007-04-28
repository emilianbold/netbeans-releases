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

package org.netbeans.modules.autoupdate.updateprovider;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.openide.xml.XMLUtil;
import org.xml.sax.EntityResolver;

/**
 *
 * @author Jiri Rechtacek
 */
public class AutoupdateCatalogParser {
    
    public static final String TAG_MODULE_UPDATES = "module_updates"; // NOI18N
    public static final String TAG_MODULE = "module"; // NOI18N
    public static final String TAG_MODULE_GROUP = "module_group"; // NOI18N
    public static final String TAG_FEATURE = "feature"; // NOI18N
    public static final String TAG_LICENSE = "license"; // NOI18N
    public static final String TAG_NOTIFICATION = "notification"; // NOI18N
    public static final String ATTR_NOTIFICATION_URL = "url"; // NOI18N
    public static final String TAG_ERROR = "error"; // NOI18N
    public static final String TAG_AUTH_ERROR = "auth_error"; // NOI18N
    public static final String TAG_OTHER_ERROR = "other_error"; // NOI18N
    public static final String ATTR_MESSAGE_ERROR = "message"; // NOI18N
    public static final String TAG_ELEMENT_L10N = "l10n"; // NOI18N
    public static final String TAG_MANIFEST = "manifest"; // NOI18N
    
    private static final Logger ERR = Logger.getLogger ("org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogParser");
    
    public static Map<String, UpdateItem> getUpdateItems (URL url, URL providerUrl) {
        Map<String, UpdateItem> items = new HashMap<String, UpdateItem> ();
        Map<String, String> licenses = getLicenses (url, providerUrl);
        
        try {
            
            List<SimpleItem> simpleItems = createSimpleItems (getDocument (url, providerUrl));
            for (SimpleItem simple : simpleItems) {
                if (simple instanceof SimpleItem.License) {
                    continue;
                }
                UpdateItem update = simple.toUpdateItem (licenses);
                items.put (simple.getId (), update);
            }
            
        } catch (IOException ex) {
            ERR.log (Level.INFO, ex.getMessage (), ex);
        } catch (SAXException ex) {
            ERR.log (Level.INFO, ex.getMessage (), ex);
        };
        
        return items;
    }
    
    // package-private only for unit testing purpose
    static Map<String, String> getLicenses (URL url, URL providerUrl) {
        Map<String, String> res = new HashMap<String, String> ();
        try {
            List<SimpleItem> items = createSimpleItems (getDocument (url, providerUrl));
            for (SimpleItem item : items) {
                if (item instanceof SimpleItem.License) {
                    SimpleItem.License license = (SimpleItem.License) item;
                    res.put (license.getLicenseId (), license.getLicenseContent ());
                }
            }
        } catch (IOException ex) {
            ERR.log (Level.INFO, ex.getMessage (), ex);
        } catch (SAXException ex) {
            ERR.log (Level.INFO, ex.getMessage (), ex);
        };
        
        return res;
    }
    
    static Document getDocument (URL url, URL providerUrl) throws IOException, SAXException {
        Document doc = XMLUtil.parse (new InputSource (url.toString ()), false, false, null /* logger */, createAUResolver ());
        if (providerUrl != null) {
            try {
                doc.setDocumentURI (providerUrl.toURI ().toString ());
            } catch (URISyntaxException ex) {
                ERR.log (Level.INFO, ex.getMessage (), ex);
            }
        }
        return doc;
    }
    
    static List<SimpleItem> createSimpleItems (Document xmlDocument) {
        NodeList moduleUpdatesList = xmlDocument.getElementsByTagName (TAG_MODULE_UPDATES);
        assert moduleUpdatesList != null && moduleUpdatesList.getLength() == 1;
        NodeList moduleUpdatesChildren = moduleUpdatesList.item (0).getChildNodes ();
        
        return parseUpdateItems (moduleUpdatesChildren);
    }
    
    private static List<SimpleItem> parseUpdateItems (NodeList children) {
        List<SimpleItem> res = new ArrayList<SimpleItem> ();
        for (int i = 0; i < children.getLength (); i++) {
            Node n = children.item (i);
            if (Node.ELEMENT_NODE != n.getNodeType()) {
                continue;
            }
            assert n instanceof Element : n + " is instanceof Element";
            String tagName = ((Element) n).getTagName ();
            if (TAG_MODULE_GROUP.equals (tagName)) {
                res.addAll (parseUpdateItems (n.getChildNodes ()));
            } else if (TAG_MODULE.equals (tagName)) {
                // split Module and Localization
                NodeList l10nElements = ((Element) n).getElementsByTagName (TAG_ELEMENT_L10N);
                NodeList manifestElements = ((Element) n).getElementsByTagName (TAG_MANIFEST);
                if (l10nElements != null && l10nElements.getLength () > 0) {
                    res.add (new SimpleItem.Localization (n));
                } else if (manifestElements != null && manifestElements.getLength () > 0) {
                    res.add (new SimpleItem.Module (n));
                } else {
                    assert false : "Unknown element " + n;
                }
            } else if (TAG_FEATURE.equals (tagName)) {
                res.add (new SimpleItem.Feature (n));
            } else if (TAG_LICENSE.equals (tagName)) {
                res.add (new SimpleItem.License (n));
            } else {
                assert false : "Unknown element tag " + tagName;
            }
        }
        return res;
        
    }
    
    /** Entity resolver that knows about AU DTDs, so no network is needed.
     * @author Jesse Glick
     */
    public static EntityResolver createAUResolver() {
        return new EntityResolver() {
            public InputSource resolveEntity(String publicID, String systemID) throws IOException, SAXException {
                if ("-//NetBeans//DTD Autoupdate Catalog 1.0//EN".equals(publicID)) { // NOI18N
                    return new InputSource(AutoupdateCatalogParser.class.getResource ("dtds/autoupdate-catalog-1_0.dtd").toString());
                } else if ("-//NetBeans//DTD Autoupdate Module Info 1.0//EN".equals(publicID)) { // NOI18N
                    return new InputSource(AutoupdateCatalogParser.class.getResource ("dtds/autoupdate-info-1_0.dtd").toString());
                } else if ("-//NetBeans//DTD Autoupdate Catalog 2.0//EN".equals(publicID)) { // NOI18N
                    return new InputSource(AutoupdateCatalogParser.class.getResource ("dtds/autoupdate-catalog-2_0.dtd").toString());
                } else if ("-//NetBeans//DTD Autoupdate Module Info 2.0//EN".equals(publicID)) { // NOI18N
                    return new InputSource(AutoupdateCatalogParser.class.getResource ("dtds/autoupdate-info-2_0.dtd").toString());
                } else if ("-//NetBeans//DTD Autoupdate Catalog 2.2//EN".equals(publicID)) { // NOI18N
                    return new InputSource(AutoupdateCatalogParser.class.getResource ("dtds/autoupdate-catalog-2_2.dtd").toString());
                } else if ("-//NetBeans//DTD Autoupdate Module Info 2.2//EN".equals(publicID)) { // NOI18N
                    return new InputSource(AutoupdateCatalogParser.class.getResource ("dtds/autoupdate-info-2_2.dtd").toString());
                } else if ("-//NetBeans//DTD Autoupdate Catalog 2.3//EN".equals(publicID)) { // NOI18N
                    return new InputSource(AutoupdateCatalogParser.class.getResource ("dtds/autoupdate-catalog-2_3.dtd").toString());
                } else if ("-//NetBeans//DTD Autoupdate Module Info 2.3//EN".equals(publicID)) { // NOI18N
                    return new InputSource(AutoupdateCatalogParser.class.getResource ("dtds/autoupdate-info-2_3.dtd").toString());
                } else if ("-//NetBeans//DTD Autoupdate Catalog 2.4//EN".equals(publicID)) { // NOI18N
                    return new InputSource(AutoupdateCatalogParser.class.getResource ("dtds/autoupdate-catalog-2_4.dtd").toString());
                } else if ("-//NetBeans//DTD Autoupdate Module Info 2.4//EN".equals(publicID)) { // NOI18N
                    return new InputSource(AutoupdateCatalogParser.class.getResource ("dtds/autoupdate-info-2_4.dtd").toString());
                } else if ("-//NetBeans//DTD Autoupdate Catalog 3.0//EN".equals(publicID)) { // NOI18N
                    return new InputSource(AutoupdateCatalogParser.class.getResource ("dtds/autoupdate-catalog-3_0.dtd").toString());
                } else if ("-//NetBeans//DTD Autoupdate Module Info 3.0//EN".equals(publicID)) { // NOI18N
                    return new InputSource(AutoupdateCatalogParser.class.getResource ("dtds/autoupdate-info-3_0.dtd").toString());
                } else {
                    return null;
                }
            }
        };
    }

}
