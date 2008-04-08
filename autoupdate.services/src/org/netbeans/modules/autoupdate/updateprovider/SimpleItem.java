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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.autoupdate.services.Trampoline;
import org.netbeans.modules.autoupdate.services.Utilities;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateLicense;
import org.openide.modules.ModuleInfo;
import org.openide.util.Exceptions;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Jiri Rechtacek
 */
public abstract class SimpleItem {
    
    protected Node declaratingNode;
    
    public static final String LICENSE = "license";
    
    static final Set<Node> declaratingNodes = new org.openide.util.WeakSet<Node> ();
    
    public SimpleItem (Node node) {
        declaratingNode = node;
        declaratingNodes.add (declaratingNode);
    }
    
    public abstract UpdateItem toUpdateItem (Map<String, String> licenses, String catalogDate);
    public UpdateItem toUpdateItem (Map<String, String> licenses, File nbm) {
        throw new UnsupportedOperationException();
    }
    public abstract String getId ();
        
    public static class Feature extends SimpleItem {
        public static final String CODE_NAME_BASE = "codename";
        public static final String SPECIFICATION_VERSION = "version";
        public static final String MODULE_DEPENDENCIES = "module-dependencies";
        public static final String DISPLAY_NAME = "displayname";
        public static final String DESCRIPTION = "description";
        
        private String codeName;
        private String version;
        private String category;
        
        public Feature (Node n, String category) {
            super (n);
            codeName = getAttribute (declaratingNode, CODE_NAME_BASE);
            version = getAttribute (declaratingNode, SPECIFICATION_VERSION);
            this.category = category;
        }
        
        public UpdateItem toUpdateItem(Map<String, String> licenses, String catalogDate) {
            assert declaratingNode != null : "declaratingNode must be declared";
            String dependencies = getAttribute (declaratingNode, MODULE_DEPENDENCIES);
            UpdateItem res = UpdateItem.createFeature (
                    getAttribute (declaratingNode, CODE_NAME_BASE),
                    getAttribute (declaratingNode, SPECIFICATION_VERSION),
                    readDependencies (dependencies, getAttribute (declaratingNode, CODE_NAME_BASE)),
                    getAttribute (declaratingNode, DISPLAY_NAME),
                    getAttribute (declaratingNode, DESCRIPTION),
                    category);
            
            // clean up declaratingNode
            declaratingNode = null;
            
            return res;
        }
        
        public String getId () {
            return codeName + '_' + version;
        }

    }
    
    public static class Module extends SimpleItem {
        public static final String CODE_NAME_BASE = "codenamebase";
        public static final String HOMEPAGE = "homepage";
        public static final String DISTRIBUTION = "distribution";
        public static final String DOWNLOAD_SIZE = "downloadsize";
        public static final String NEEDS_RESTART = "needsrestart";
        public static final String MODULE_AUTHOR = "moduleauthor";
        public static final String RELEASE_DATE = "releasedate";
        public static final String IS_GLOBAL = "global";
        public static final String TARGET_CLUSTER = "targetcluster";
        public static final String ATTR_SPECIFICATION_VERSION = "OpenIDE-Module-Specification-Version";
        public static final String IS_EAGER = "eager";
        public static final String IS_AUTOLOAD = "autoload";
        public static final String MODULE_NOTIFICATION = "module_notification";
        
        private String moduleCodeName;
        private String specVersion;
        private String category;
        
        public Module (Node n, String category) {
            super (n);
            this.category = category;
        }
        
        private static String getSpecificationVersion (Node n) {
            NodeList l = ((Element) n).getElementsByTagName (AutoupdateCatalogParser.TAG_MANIFEST);
            assert l != null && l.getLength() == 1 : "TAG_MANIFEST should contains one and only one list.";
            assert Node.ELEMENT_NODE == l.item (0).getNodeType ();

            String specVersion = null;
            NamedNodeMap attrList = l.item (0).getAttributes ();
            Manifest mf = new Manifest ();
            Attributes mfAttrs = mf.getMainAttributes ();

            for (int i = 0; i < attrList.getLength (); i++) {
                Attr attr = (Attr) attrList.item (i);
                mfAttrs.put (new Attributes.Name (attr.getName ()), attr.getValue ());
                if (ATTR_SPECIFICATION_VERSION.equals(attr.getName ())) {
                    specVersion = attr.getValue ();
                }
            }
            return specVersion;
        }
        
        private static Manifest getManifest (Node n) {
            NodeList l = ((Element) n).getElementsByTagName (AutoupdateCatalogParser.TAG_MANIFEST);
            assert l != null && l.getLength() == 1 : "TAG_MANIFEST should contains one and only one list.";
            assert Node.ELEMENT_NODE == l.item (0).getNodeType ();

            NamedNodeMap attrList = l.item (0).getAttributes ();
            Manifest mf = new Manifest ();
            Attributes mfAttrs = mf.getMainAttributes ();

            for (int i = 0; i < attrList.getLength (); i++) {
                Attr attr = (Attr) attrList.item (i);
                mfAttrs.put (new Attributes.Name (attr.getName ()), attr.getValue ());
            }
            return mf;
        }
        
        private static String getModuleNotfication (Node n) {
            NodeList l = ((Element) n).getElementsByTagName (MODULE_NOTIFICATION);
            if (l == null || l.getLength () == 0) {
                return null;
            }
            assert l != null && l.getLength() == 1 : "MODULE_NOTIFICATION should contains one and only one list.";
            assert Node.ELEMENT_NODE == l.item (0).getNodeType ();
            
            return l.item (0).getChildNodes().item (0).getNodeValue ();
        }
        
        private static String getModuleCodeName (Node n) {
            return getAttribute (n, CODE_NAME_BASE);
        }
        
        public UpdateItem toUpdateItem (Map<String, String> licenses, String catalogDate) {
            return toUpdateItem(licenses, null, catalogDate);
        }
        
        @Override
        public UpdateItem toUpdateItem(Map<String, String> licenses, File nbm) {
            String fileLastModified = Utilities.formatDate(new Date (nbm.lastModified ()));
            return toUpdateItem(licenses, nbm, fileLastModified);
        }

        private URL getDistribution(File nbm/*or null*/) {
            URL retval = null;            
            try {
                if (nbm != null) {
                    retval = nbm.toURI().toURL();
                } else {
                    String distribution = getAttribute(declaratingNode, DISTRIBUTION);
                    if (distribution != null && distribution.length() > 0) {
                        URI distributionURI = new URI(distribution);
                        if (!distributionURI.isAbsolute()) {
                            distributionURI = new URI(declaratingNode.getBaseURI()).resolve(distributionURI);
                        } 
                        retval = distributionURI.toURL();
                    }
                }                
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            } catch (URISyntaxException urisy) {
                Exceptions.printStackTrace(urisy);
            }            
            return retval;
        }
        
        private UpdateItem toUpdateItem(Map<String, String> licenses, File nbm, String catalogDate) {
            assert declaratingNode != null : "declaratingNode must be declared";
            URL distributionURL = null;                        
            moduleCodeName = getModuleCodeName (declaratingNode);
            distributionURL = getDistribution(nbm);
            String needsrestart = getAttribute (declaratingNode, NEEDS_RESTART);
            String global = getAttribute (declaratingNode, IS_GLOBAL);
            String targetcluster = getAttribute (declaratingNode, TARGET_CLUSTER);
            String homepage = getAttribute (declaratingNode, HOMEPAGE);
            String downloadSize = (nbm != null && nbm.exists()) ? String.valueOf(nbm.length()) :  getAttribute (declaratingNode, DOWNLOAD_SIZE);
            String author = getAttribute (declaratingNode, MODULE_AUTHOR);
            String publishDate = getAttribute (declaratingNode, RELEASE_DATE);
            String eager = getAttribute (declaratingNode, IS_EAGER);
            String autoload = getAttribute (declaratingNode, IS_AUTOLOAD);
            if (publishDate == null || publishDate.length () == 0) {
                publishDate = catalogDate;
            }
            
            
            Boolean needsRestart = needsrestart == null || needsrestart.trim ().length () == 0 ? null : Boolean.valueOf (needsrestart);
            Boolean isGlobal = global == null || global.trim ().length () == 0 ? null : Boolean.valueOf (global);
            Boolean isEager = Boolean.parseBoolean (eager);
            Boolean isAutoload = Boolean.parseBoolean (autoload);
            
            specVersion = getSpecificationVersion (declaratingNode);
            Manifest mf = getManifest (declaratingNode);
            
            String licName = getAttribute (declaratingNode, LICENSE);
            String licContent = licenses.get (licName);
            UpdateLicense lic = UpdateLicense.createUpdateLicense (licName, licContent);
            
            UpdateItem res = UpdateItem.createModule (
                    moduleCodeName,
                    specVersion,
                    distributionURL,
                    author,
                    downloadSize,
                    homepage,
                    publishDate,
                    category,
                    mf,
                    isEager,
                    isAutoload,
                    needsRestart,
                    isGlobal,
                    targetcluster,
                    lic);
            
            // read module notification
            UpdateItemImpl impl = Trampoline.SPI.impl(res);
            ((ModuleItem) impl).setModuleNotification (getModuleNotfication (declaratingNode));
            
            // clean up declaratingNode
            declaratingNode = null;
            
            return res;
        }
        
        public String getId() {
            return moduleCodeName + '_' + specVersion;
        }

    }
    
    public static class InstalledModule extends SimpleItem {
        private ModuleInfo info;
        
        public InstalledModule (ModuleInfo moduleInfo) {
            super (null);
            if (moduleInfo == null) {
                throw new IllegalArgumentException ("ModuleInfo must found while constructing InstalledModule.");
            }
            this.info = moduleInfo;
        }

        public UpdateItem toUpdateItem (Map<String, String> licenses, String installTime) {
            UpdateItemImpl impl = new InstalledModuleItem (
                    info.getCodeNameBase (),
                    info.getSpecificationVersion () == null ? null : info.getSpecificationVersion ().toString (),
                    info,
                    null, // XXX author
                    null, // installed cluster
                    installTime
                    
                    );
            
            return Utilities.createUpdateItem (impl);
        }

        public String getId () {
            return info.getCodeName () + '_' + info.getSpecificationVersion ();
        }

    }
    
    public static class Localization extends SimpleItem {
        public static final String CODE_NAME_BASE = "codenamebase";
        public static final String DISTRIBUTION = "distribution";
        public static final String DOWNLOAD_SIZE = "downloadsize";
        public static final String NEEDS_RESTART = "needsrestart";
        public static final String MODULE_AUTHOR = "moduleauthor";
        public static final String RELEASE_DATE = "releasedate";
        public static final String IS_GLOBAL = "global";
        public static final String TARGET_CLUSTER = "targetcluster";
        
        public static final String LOCALE = "langcode";
        public static final String BRANDING = "brandingcode";
        public static final String MODULE_SPECIFICATION = "module_spec_version";
        public static final String MODULE_MAJOR_VERSION = "module_major_version";
        public static final String LOCALIZED_MODULE_NAME = "OpenIDE-Module-Name";
        public static final String LOCALIZED_MODULE_DESCRIPTION = "OpenIDE-Module-Long-Description";
        
        private String specificationVersion;
        private String moduleCodeName;
        private String locale;
        private String branding;
        private String category;

        public Localization (Node n, String category) {
            super (n);
            this.category = category;
        }
        
        public UpdateItem toUpdateItem(Map<String, String> licenses, String catalogDate) {
            assert declaratingNode != null : "declaratingNode must be declared";
            
            moduleCodeName = getAttribute (declaratingNode, CODE_NAME_BASE);
            String distribution = getAttribute (declaratingNode, DISTRIBUTION);
            String needsrestart = getAttribute (declaratingNode, NEEDS_RESTART);
            String global = getAttribute (declaratingNode, IS_GLOBAL);
            String targetcluster = getAttribute (declaratingNode, TARGET_CLUSTER);
            
            URL distributionURL = null;
            try {
                distributionURL = new URL (distribution);
            } catch (MalformedURLException mue) {
                assert false : mue;
            }
            
            Boolean needsRestart = needsrestart == null || needsrestart.trim ().length () == 0 ? null : Boolean.valueOf (needsrestart);
            Boolean isGlobal = global == null || global.trim ().length () == 0 ? null : Boolean.valueOf (global);
            
            NodeList l = ((Element) declaratingNode).getElementsByTagName (AutoupdateCatalogParser.TAG_ELEMENT_L10N);
            assert l != null && l.getLength() == 1 : "TAG_MANIFEST in module " + moduleCodeName + " should contains one and only one list, but was " + l.getLength ();
            assert Node.ELEMENT_NODE == l.item (0).getNodeType ();
            
            Node n = l.item (0);

            locale = getAttribute (n, LOCALE);
            branding = getAttribute (n, BRANDING);
            
            String localizationCodeName = moduleCodeName + '_' + locale + '_' + branding;
            specificationVersion = "1.0"; // XXX issue 90185
            
            String licName = getAttribute (declaratingNode, LICENSE);
            String licContent = licenses.get (licName);
            UpdateLicense lic = UpdateLicense.createUpdateLicense (licName, licContent);
            
            UpdateItem res = UpdateItem.createLocalization (
                    localizationCodeName,
                    specificationVersion,
                    getAttribute (declaratingNode, MODULE_SPECIFICATION),
                    locale != null && locale.length () > 0 ? new Locale (locale) : null,
                    branding != null && branding.length() > 0 ? branding : null,
                    getAttribute (declaratingNode, LOCALIZED_MODULE_NAME),
                    getAttribute (declaratingNode, LOCALIZED_MODULE_DESCRIPTION),
                    category,
                    distributionURL,
                    needsRestart,
                    isGlobal,
                    targetcluster,
                    lic);
            
            // clean up declaratingNode
            declaratingNode = null;
            
            return res;
        }
        
        public String getId() {
            return moduleCodeName + '_' + specificationVersion + '_'+ locale + '_' + branding;
        }

    }
    
    public static class License extends SimpleItem {
        public static final String LICENSE_ID = "name";
        
        private String licenceId;
        private String licenseContent;
        
        public License (Node n) {
            super (n);
            assert declaratingNode != null : "declaratingNode must be declared";
            licenceId = getAttribute (declaratingNode, LICENSE_ID);
            NodeList innerList = declaratingNode.getChildNodes ();
            /* XXX: due to #111578 was commented out:
            assert innerList != null && innerList.getLength() == 1 :
                "License " + getAttribute (declaratingNode, LICENSE_ID) + " should contain only once data.";
            */
            if (innerList == null) {
                Logger.getLogger (SimpleItem.class.getName ()).log (Level.WARNING,
                        "License " + getAttribute (declaratingNode, LICENSE_ID) +
                        " doesn't contain any data.");
            }
            if (innerList.getLength() != 1) {
                Logger.getLogger (SimpleItem.class.getName ()).log (Level.WARNING,
                        "License " + getAttribute (declaratingNode, LICENSE_ID) +
                        " contains more(" + innerList.getLength () + "x) instances of license content. " +
                        "Reopen the issue 111578.");
            }

            /* for more text in License:
             for (int j = 0; j < innerList.getLength (); j++ ) {
                short type = innerList.item( j ).getNodeType();
                if (type == Node.CDATA_SECTION_NODE )  {
                    sb.append (innerList.item (j).getNodeValue ());
                }
            }
            return sb.toString();*/
            
            licenseContent = innerList.item (0).getNodeValue ();
            
            // clean up declaratingNode
            declaratingNode = null;
        }
        
        public String getLicenseId () {
            assert licenceId != null : "licenceId cannot be null";
            return licenceId;
        }
    
        public String getLicenseContent () {
            assert licenseContent != null : "licenseContent for " + licenceId + " cannot be null";
            return licenseContent;
        }
    
        public UpdateItem toUpdateItem(Map<String, String> licenses, String catalogDate) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getId() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
}

    // helper methods
    
    static String getAttribute (Node n, String attribute) {
        Node attr = n.getAttributes ().getNamedItem (attribute);
        return attr == null ? null : attr.getNodeValue ();
    }
    
    private static Set<String> readDependencies (String input, String who) {
        Set<String> res = new HashSet<String> ();
        //assert input != null : "Each feature needs own modules, but " + who + " has " + input; XXX
        if (input == null) {
            Logger.getLogger (SimpleItem.class.getName ()).log (Level.INFO, "Each feature needs own modules, but " + who + " has " + input);
        }
        if (input == null) {
            return res;
        }
        StringTokenizer tokenizer = new StringTokenizer (input.trim (), ",");
        
        while (tokenizer.hasMoreTokens ()) {
            res.add (tokenizer.nextToken ());
        }
        
        return res;
    }

}
