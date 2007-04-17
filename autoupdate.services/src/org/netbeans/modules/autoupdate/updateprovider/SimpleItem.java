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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateLicense;
import org.openide.filesystems.FileUtil;
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
    
    public SimpleItem (Node node) {
        declaratingNode = node;
    }
    
    public abstract UpdateItem toUpdateItem (Map<String, String> licenses);
    public abstract String getId ();
        
    public static class Feature extends SimpleItem {
        public static final String CODE_NAME_BASE = "name";
        public static final String SPECIFICATION_VERSION = "version";
        public static final String MODULE_DEPENDENCIES = "module-dependencies";
        public static final String DISPLAY_NAME = "displayname";
        public static final String DESCRIPTION = "description";
        
        private String codeName;
        private String version;
        
        public Feature (Node n) {
            super (n);
            codeName = getAttribute (declaratingNode, CODE_NAME_BASE);
            version = getAttribute (declaratingNode, SPECIFICATION_VERSION);
        }
        
        public UpdateItem toUpdateItem(Map<String, String> licenses) {
            assert declaratingNode != null : "declaratingNode must be declared";
            String dependencies = getAttribute (declaratingNode, MODULE_DEPENDENCIES);
            UpdateItem res = UpdateItem.createFeature (
                    getAttribute (declaratingNode, CODE_NAME_BASE),
                    getAttribute (declaratingNode, SPECIFICATION_VERSION),
                    readDependencies (dependencies),
                    getAttribute (declaratingNode, DESCRIPTION),
                    getAttribute (declaratingNode, DISPLAY_NAME));
            
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
        public static final String LICENSE = "license";
        public static final String DOWNLOAD_SIZE = "downloadsize";
        public static final String NEEDS_RESTART = "needsrestart";
        public static final String MODULE_AUTHOR = "moduleauthor";
        public static final String RELEASE_DATE = "releasedate";
        public static final String IS_GLOBAL = "global";
        public static final String TARGET_CLUSTER = "targetcluster";
        public static final String ATTR_SPECIFICATION_VERSION = "OpenIDE-Module-Specification-Version";
        
        private String moduleCodeName;
        private String specVersion;
        
        public Module (Node n) {
            super (n);
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
        
        private static String getModuleCodeName (Node n) {
            return getAttribute (n, CODE_NAME_BASE);
        }
        
        public UpdateItem toUpdateItem (Map<String, String> licenses) {
            assert declaratingNode != null : "declaratingNode must be declared";
            
            moduleCodeName = getModuleCodeName (declaratingNode);
            String distribution = getAttribute (declaratingNode, DISTRIBUTION);
            if (declaratingNode.getBaseURI() != null || distribution == null || distribution.length() == 0) {
                String baseURI = declaratingNode.getBaseURI();
                if (baseURI != null && baseURI.length() > 0) {
                    try {
                        URL uu = URI.create(baseURI).toURL();
                        if (uu != null /*&&  FileUtil.isArchiveFile(uu)*/) {
                            uu = FileUtil.getArchiveFile(uu);
                            if (uu != null && "file".equals(uu.getProtocol())) {
                                distribution = uu.toExternalForm();
                            }
                        }
                    } catch(MalformedURLException mex) {
                        Exceptions.printStackTrace(mex);
                    }
                }
            }             
            if (distribution != null && distribution.length () > 0) {
                URI distributionURI = null;
                URI baseURI = null;
                try {
                    distributionURI = new URI (distribution);
                    baseURI = new URI (declaratingNode.getBaseURI ());
                } catch (URISyntaxException urisy) {
                    Exceptions.printStackTrace (urisy);
                }
                if (! distributionURI.isAbsolute ()) {
                    distributionURI = baseURI.resolve (distributionURI);
                    distribution = distributionURI.toString ();
                }
            } 
            String needsrestart = getAttribute (declaratingNode, NEEDS_RESTART);
            String global = getAttribute (declaratingNode, IS_GLOBAL);
            String targetcluster = getAttribute (declaratingNode, TARGET_CLUSTER);
            String homepage = getAttribute (declaratingNode, HOMEPAGE);
            String downloadSize = getAttribute (declaratingNode, DOWNLOAD_SIZE);
            String author = getAttribute (declaratingNode, MODULE_AUTHOR);
            
            URL distributionURL = null;
            if (distribution != null && distribution.length() > 0) {
                try {
                    distributionURL = new URL (distribution);
                } catch (MalformedURLException mue) {
                    assert false : mue;
                }
            }
            
            Boolean needsRestart = needsrestart == null || needsrestart.trim ().length () == 0 ? null : Boolean.valueOf (needsrestart);
            Boolean isGlobal = global == null || global.trim ().length () == 0 ? null : Boolean.valueOf (global);
            
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
                    mf,
                    needsRestart,
                    isGlobal,
                    targetcluster,
                    lic);
            
            // clean up declaratingNode
            declaratingNode = null;
            
            return res;
        }

        public String getId() {
            return moduleCodeName + '_' + specVersion;
        }

    }
    
    public static class Localization extends SimpleItem {
        public static final String CODE_NAME_BASE = "codenamebase";
        public static final String DISTRIBUTION = "distribution";
        public static final String LICENSE = "license";
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

        public Localization (Node n) {
            super (n);
        }
        
        public UpdateItem toUpdateItem(Map<String, String> licenses) {
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
            assert innerList != null && innerList.getLength() == 1 : "Lincese " + getAttribute (declaratingNode, LICENSE_ID) + " should contain only once data.";

            /* for more text in Lincese:
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
    
        public UpdateItem toUpdateItem(Map<String, String> licenses) {
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
    
    private static Set<String> readDependencies (String input) {
        Set<String> res = new HashSet<String> ();
        StringTokenizer tokenizer = new StringTokenizer (input.trim (), ",");
        
        while (tokenizer.hasMoreTokens ()) {
            res.add (tokenizer.nextToken ());
        }
        
        return res;
    }

}
