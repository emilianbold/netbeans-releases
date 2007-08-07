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

package org.netbeans.modules.autoupdate.updateprovider;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.netbeans.modules.autoupdate.services.Utilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.openide.xml.XMLUtil;

/**
 *
 * @author Jiri Rechtacek
 */
public class AutoupdateCatalogParser {
    
    public static final String TAG_MODULE_UPDATES = "module_updates"; // NOI18N
    public static final String TAG_MODULE = "module"; // NOI18N
    public static final String TAG_MODULE_GROUP = "module_group"; // NOI18N
    public static final String ATTR_MODULE_GROUP_NAME = "name"; // NOI18N
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
    private static final String TIME_STAMP_ATTRIBUTE_NAME = "timestamp"; // NOI18N
    private static final String TIME_STAMP_FORMAT = "ss/mm/hh/dd/MM/yyyy"; // NOI18N
    
    private static final Logger ERR = Logger.getLogger ("org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogParser");
    private static String GZIP_EXTENSION = ".gz"; // NOI18N
    
    public static Map<String, UpdateItem> getUpdateItems (URL url, URL providerUrl) {
        Map<String, UpdateItem> items = new HashMap<String, UpdateItem> ();
        Map<String, String> licenses = getLicenses (url, providerUrl);
        Date d = getAutoupdateCatalogDate (url, providerUrl);
        String catalogDate = null;
        if (d != null) {
            catalogDate = Utilities.DATE_FORMAT.format (d);
        }
        
        try {
            
            List<SimpleItem> simpleItems = createSimpleItems (getDocument (url, providerUrl));
            for (SimpleItem simple : simpleItems) {
                if (simple instanceof SimpleItem.License) {
                    continue;
                }
                UpdateItem update = simple.toUpdateItem (licenses, catalogDate);
                items.put (simple.getId (), update);
            }
            
        } catch (IOException ex) {
            ERR.log (Level.INFO, ex.getMessage (), ex);
        } catch (SAXException ex) {
            ERR.log (Level.INFO, ex.getMessage (), ex);
        }
        
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
        }
        
        return res;
    }
    
    public static String getNotification (URL url, URL providerUrl) {
        String notification = null;
        
        try {
            
            Document doc = getDocument (url, providerUrl);
            NodeList moduleUpdatesList = doc.getElementsByTagName (TAG_MODULE_UPDATES);
            assert moduleUpdatesList != null && moduleUpdatesList.getLength() == 1;
            NodeList moduleUpdatesChildren = moduleUpdatesList.item (0).getChildNodes ();
            for (int i = 0; i < moduleUpdatesChildren.getLength (); i++) {
                Node n = moduleUpdatesChildren.item (i);
                if (Node.ELEMENT_NODE != n.getNodeType()) {
                    continue;
                }
                assert n instanceof Element : n + " is instanceof Element";
                String tagName = ((Element) n).getTagName ();
                if (TAG_NOTIFICATION.equals (tagName)) {
                    NodeList innerList = n.getChildNodes ();
                    assert innerList != null && innerList.getLength() == 1 : "Notification " + n + " should contain only once data.";

                    String notificationText = innerList.item (0).getNodeValue ();
                    notification = "";
                    if (notificationText != null && notificationText.length () > 0) {
                        notification = notificationText;
                    }
                    String notificationUrl = SimpleItem.getAttribute (n, ATTR_NOTIFICATION_URL);
                    if (notificationUrl != null && notificationUrl.length () > 0) {
                        notification += (notification.length () > 0 ? "<br>" : "") +
                                "<a href=\"" + notificationUrl + "\">" + notificationUrl + "</a>"; // NOI18N
                    }
                    break;
                }
            }
        } catch (IOException ex) {
            ERR.log (Level.INFO, ex.getMessage (), ex);
        } catch (SAXException ex) {
            ERR.log (Level.INFO, ex.getMessage (), ex);
        }
        
        return notification;
    }
    
    static Document getDocument (URL url, URL providerUrl) throws IOException, SAXException {
        InputStream is = null;
        Document doc = null;
        try {
            is = getInputStream (url, isGZIP (providerUrl));
            doc = XMLUtil.parse (new InputSource (is), false, false, null /* logger */, org.netbeans.updater.XMLUtil.createAUResolver ());
        } finally {
            if (is != null) is.close ();
        }
        if (providerUrl != null) {
            try {
                doc.setDocumentURI (providerUrl.toURI ().toString ());
            } catch (URISyntaxException ex) {
                ERR.log (Level.INFO, ex.getMessage (), ex);
            }
        }
        return doc;
    }
    
    private static boolean isGZIP (URL url) {
        boolean res = false;
        if (url != null) {
            res = url.getPath ().toLowerCase ().endsWith (GZIP_EXTENSION);
        }
        ERR.log (Level.FINER, "Is GZIP " + url + " ? " + res);
        return res;
    }
    
    private static InputStream getInputStream (URL url, boolean isGZIP) {
        InputStream is = null;
        if (isGZIP) {
            try {
                is = new BufferedInputStream (new GZIPInputStream (url.openStream ()));
            } catch (IOException ioe) {
                ERR.log (Level.INFO, "IOException " + ioe.getMessage () + " while reading GZIP stream " + url, ioe);
            }
        }
        if (is == null) {
            try {
                is = new BufferedInputStream (url.openStream ());
            } catch (IOException ioe) {
                ERR.log (Level.INFO, "IOException " + ioe.getMessage () + " while reading stream " + url, ioe);
            }
        }
        return is;
    }
    
    private static Date getAutoupdateCatalogDate (URL url, URL providerUrl) {
        Date timeStamp = null;
        InputStream is = null;
        try {
            ERR.log (Level.FINER, "Inspect Autoupdate Catalog " + url);
            is = getInputStream (url, isGZIP (providerUrl));
            StringBuffer sb = new StringBuffer (1024);

            int c;
            while ((c = is.read ()) != -1 && sb.length () < 1024) {
                sb.append ((char) c);
            }
            ERR.log (Level.FINER, "Successfully checked " + url); // NOI18N

            String content = sb.toString ();
            ERR.log (Level.FINEST, "Read string " + sb); // NOI18N
            String time = null;
            int pos;
            if ((pos = content.indexOf (TIME_STAMP_ATTRIBUTE_NAME)) != -1) {
                content = content.substring (pos + TIME_STAMP_ATTRIBUTE_NAME.length () + 1 + 1);
                if ((pos = content.indexOf ('>')) != -1) {
                    time = content.substring (0, pos - 1);
                }
            }
            ERR.log (Level.FINEST, "Transposed time " + time); // NOI18N
            
            if (time == null) {
                ERR.log (Level.INFO, "No timestamp is presented in " + url); // NOI18N
            } else {
                DateFormat format = new SimpleDateFormat (TIME_STAMP_FORMAT);
                timeStamp = format.parse (time);
                ERR.log (Level.FINER, "Successfully read time " + timeStamp); // NOI18N
            }

        } catch (IOException ioe) {
            ERR.log (Level.FINE, null, ioe);
        } catch (ParseException ex) {
            ERR.log (Level.FINE, null, ex);
        } finally {
            if (is != null) {
                try {
                    is.close ();
                } catch (IOException ex) {
                    ERR.log (Level.FINE, null, ex);
                }
            }
        }
        return timeStamp;
    }
    
    static List<SimpleItem> createSimpleItems (Document xmlDocument) {
        NodeList moduleUpdatesList = xmlDocument.getElementsByTagName (TAG_MODULE_UPDATES);
        assert moduleUpdatesList != null && moduleUpdatesList.getLength() == 1;
        NodeList moduleUpdatesChildren = moduleUpdatesList.item (0).getChildNodes ();
        
        return parseUpdateItems (moduleUpdatesChildren, null);
    }
    
    private static List<SimpleItem> parseUpdateItems (NodeList children, String group) {
        List<SimpleItem> res = new ArrayList<SimpleItem> ();
        for (int i = 0; i < children.getLength (); i++) {
            Node n = children.item (i);
            if (Node.ELEMENT_NODE != n.getNodeType()) {
                continue;
            }
            assert n instanceof Element : n + " is instanceof Element";
            String tagName = ((Element) n).getTagName ();
            if (TAG_MODULE_GROUP.equals (tagName)) {
                group = SimpleItem.getAttribute (n, ATTR_MODULE_GROUP_NAME);
                res.addAll (parseUpdateItems (n.getChildNodes (), group));
            } else if (TAG_MODULE.equals (tagName)) {
                // split Module and Localization
                NodeList l10nElements = ((Element) n).getElementsByTagName (TAG_ELEMENT_L10N);
                NodeList manifestElements = ((Element) n).getElementsByTagName (TAG_MANIFEST);
                if (l10nElements != null && l10nElements.getLength () > 0) {
                    res.add (new SimpleItem.Localization (n, group));
                } else if (manifestElements != null && manifestElements.getLength () > 0) {
                    res.add (new SimpleItem.Module (n, group));
                } else {
                    assert false : "Unknown element " + n;
                }
            } else if (TAG_FEATURE.equals (tagName)) {
                res.add (new SimpleItem.Feature (n, group));
            } else if (TAG_LICENSE.equals (tagName)) {
                res.add (new SimpleItem.License (n));
            } else if (TAG_NOTIFICATION.equals (tagName)) {
                // don't read it now
            } else {
                assert false : "Unknown element tag " + tagName;
            }
        }
        return res;
        
    }
    
}
