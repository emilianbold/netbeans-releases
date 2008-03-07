/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.autoupdate.updateprovider;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import org.netbeans.spi.autoupdate.UpdateItem;
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
        }
        
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
        InputSource xmlInputSource = new InputSource (new BufferedInputStream (jf.getInputStream (entry)));
        return XMLUtil.parse (xmlInputSource, false, false, null /* logger */, org.netbeans.updater.XMLUtil.createAUResolver ());
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
