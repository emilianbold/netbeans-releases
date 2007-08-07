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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.JarFileSystem;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Jiri Rechtacek
 */
public class AutoupdateInfoParser {
    public static final String INFO_NAME = "info";
    public static final String INFO_EXT = ".xml";
    public static final String INFO_FILE = INFO_NAME + INFO_EXT;
    public static final String INFO_DIR = "Info";
    public static final String INFO_LOCALE = "locale";
    
    private static final Logger ERR = Logger.getLogger ("org.netbeans.modules.autoupdate.updateprovider.AutoupdateInfoParser");
    
    public static Map<String, UpdateItem> getUpdateItems (File nbmFile) throws IOException, SAXException {
        Map<String, UpdateItem> items = new HashMap<String, UpdateItem> ();
        Document doc = getAutoupdateInfo (nbmFile);
        SimpleItem simple = createSimpleItem (doc, false);
        assert ! (simple instanceof SimpleItem.License) : simple + " is not instanceof License.";
        UpdateItem update = simple.toUpdateItem (getLicenses (nbmFile), nbmFile);
        items.put (simple.getId (), update);
        
        return items;
    }
    
    public static Map<String, String> getLicenses (File nbmFile) {
        Map<String, String> res = new HashMap<String, String> ();
        try {
            Document doc = getAutoupdateInfo (nbmFile);
            SimpleItem simple = createSimpleItem (doc, true);
            if (simple != null) {
                assert simple instanceof SimpleItem.License : simple + " is instanceof License.";
                SimpleItem.License license = (SimpleItem.License) simple;
                res.put (license.getLicenseId (), license.getLicenseContent ());
            }
        } catch (IOException ex) {
            ERR.log (Level.INFO, ex.getMessage(), ex);
        } catch (SAXException ex) {
            ERR.log (Level.INFO, ex.getMessage(), ex);
        };
        
        return res;
    }
    
    // package-private only for unit testing purpose
    static SimpleItem createSimpleItem (Document xmlDocument, boolean onlyLicense) {
        assert xmlDocument.getDocumentElement () != null : xmlDocument + " contains DocumentElement.";
        //NodeList moduleUpdatesChildren = xmlDocument.getDocumentElement ().getChildNodes ();
        NodeList moduleUpdatesChildren = xmlDocument.getChildNodes ();
        
        return parseUpdateItem (moduleUpdatesChildren, onlyLicense);
    }
    
    // package-private only for unit testing purpose
    static Document getAutoupdateInfo (File nbmFile) throws IOException, SAXException {
        
        // find info.xml entry
        JarFile jf = new JarFile (nbmFile);
        String locale = Locale.getDefault ().toString ();
        ZipEntry entry = jf.getEntry (INFO_DIR + '/' + INFO_LOCALE + '/' + INFO_NAME + '_' + locale + INFO_EXT);
        if (entry == null) {
            entry = jf.getEntry (INFO_DIR + '/' + INFO_FILE);
        }
        if (entry == null) {
            throw new IllegalArgumentException ("info.xml found in file " + nbmFile);
        }
        
        // get xml document
        InputSource xmlInputSource = new InputSource (jf.getInputStream (entry));
        Document retval = XMLUtil.parse (xmlInputSource, false, false, null /* logger */, org.netbeans.updater.XMLUtil.createAUResolver ());
        if (retval != null) {
            jf.close();
            JarFileSystem jfs = new JarFileSystem();
            try {
                jfs.setJarFile(nbmFile);
                FileObject fo = jfs.findResource(entry.getName());
                URL u = (fo != null) ? URLMapper.findURL(fo, URLMapper.EXTERNAL) : null;
                if (u != null) {
                    retval.setDocumentURI(u.toURI().toString());
                }
            } catch(Exception iex) {
                Exceptions.printStackTrace(iex);
            } 
        }
        
        return retval;
    }
    
    private static SimpleItem parseUpdateItem (NodeList children, boolean onlyLicense) {
        SimpleItem res = null;
        for (int i = 0; i < children.getLength (); i++) {
            Node n = children.item (i);
            if (Node.ELEMENT_NODE != n.getNodeType()) {
                continue;
            }
            assert n instanceof Element : n + " is instanceof Element";
            String tagName = ((Element) n).getTagName ();
            if (AutoupdateCatalogParser.TAG_MODULE_GROUP.equals (tagName)) {
                assert false : AutoupdateCatalogParser.TAG_MODULE_GROUP + " is not allowed in Info.xml";
            } else if (AutoupdateCatalogParser.TAG_MODULE.equals (tagName)) {
                // split Module and Localization (and Licences only for info.xml)
                NodeList l10nElements = ((Element) n).getElementsByTagName (AutoupdateCatalogParser.TAG_ELEMENT_L10N);
                NodeList manifestElements = ((Element) n).getElementsByTagName (AutoupdateCatalogParser.TAG_MANIFEST);
                NodeList licenseElements = ((Element) n).getElementsByTagName (AutoupdateCatalogParser.TAG_LICENSE);
                if (l10nElements != null && l10nElements.getLength () == 1) {
                    if (! onlyLicense) {
                        res = new SimpleItem.Localization (n, null);
                    }
                } else if (manifestElements != null && manifestElements.getLength () == 1) {
                    if (! onlyLicense) {
                        res = new SimpleItem.Module (n, null);
                    }
                }
                if (licenseElements != null && licenseElements.getLength () == 1) {
                    if (onlyLicense) {
                        res = new SimpleItem.License (licenseElements.item(0));
                    }
                }
            } else if (AutoupdateCatalogParser.TAG_FEATURE.equals (tagName)) {
                assert false : AutoupdateCatalogParser.TAG_FEATURE + " is not allowed in Info.xml";
            } else if (AutoupdateCatalogParser.TAG_LICENSE.equals (tagName)) {
                if (onlyLicense) {
                    res = new SimpleItem.License (n);
                }
            } else {
                assert false : "Unknown element tag " + tagName;
            }
        }
        return res;
        
    }
    
}
