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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.modules.autoupdate.services.Trampoline;
import org.netbeans.modules.autoupdate.services.UpdateLicenseImpl;
import org.netbeans.modules.autoupdate.services.Utilities;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateLicense;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Jiri Rechtacek
 */
public class AutoupdateInfoParser extends DefaultHandler {
    private static final String INFO_NAME = "info";
    private static final String INFO_EXT = ".xml";
    private static final String INFO_FILE = INFO_NAME + INFO_EXT;
    private static final String INFO_DIR = "Info";
    private static final String INFO_LOCALE = "locale";
    
    private final Map<String, UpdateItem> items;
    private final EntityResolver entityResolver;
    private final File nbmFile;
    private UpdateLicenseImpl currentUpdateLicenseImpl;
    
    private AutoupdateInfoParser (Map<String, UpdateItem> items, File nbmFile) {
        this.items = items;
        this.entityResolver = newEntityResolver();
        this.nbmFile = nbmFile;
    }

    private EntityResolver newEntityResolver () {
        try {
            Class.forName("org.netbeans.updater.XMLUtil");
        } catch (ClassNotFoundException e) {
            final File netbeansHomeFile = new File(System.getProperty("netbeans.home"));
            final File userdir = new File(System.getProperty("netbeans.user"));

            final String updaterPath = "modules/ext/updater.jar";
            final String newUpdaterPath = "update/new_updater/updater.jar";

            final File updaterPlatform = new File(netbeansHomeFile, updaterPath);
            final File updaterUserdir  = new File(userdir, updaterPath);

            final File newUpdaterPlatform = new File(netbeansHomeFile, newUpdaterPath);
            final File newUpdaterUserdir  = new File(userdir, newUpdaterPath);

            String message =
                    "    org.netbeans.updater.XMLUtil is not accessible\n" +
                    "    platform dir = " + netbeansHomeFile.getAbsolutePath() + "\n" +
                    "    userdir  dir = " + userdir.getAbsolutePath() + "\n" +
                    "    updater in platform exist = " + updaterPlatform.exists() + (updaterPlatform.exists() ? (", length = " + updaterPlatform.length() + " bytes") : "") + "\n" +
                    "    updater in userdir  exist = " + updaterUserdir.exists() + (updaterUserdir.exists() ? (", length = " + updaterUserdir.length() + " bytes") : "") + "\n" +
                    "    new updater in platform exist = " + newUpdaterPlatform.exists() + (newUpdaterPlatform.exists() ? (", length = " + newUpdaterPlatform.length() + " bytes") : "") + "\n" +
                    "    new updater in userdir  exist = " + newUpdaterUserdir.exists() + (newUpdaterUserdir.exists() ? (", length = " + newUpdaterUserdir.length() + " bytes") : "") + "\n";

            ERR.log(Level.WARNING, message);
        }
        return org.netbeans.updater.XMLUtil.createAUResolver ();
    }
    
    private static final Logger ERR = Logger.getLogger (AutoupdateInfoParser.class.getName ());
    
    private static enum ELEMENTS {
        module, description,
        module_notification, external_package, manifest, l10n, license
    }
    
    private static final String LICENSE_ATTR_NAME = "name";
    
    private static final String MODULE_ATTR_CODE_NAME_BASE = "codenamebase";
    private static final String MODULE_ATTR_HOMEPAGE = "homepage";
    private static final String MODULE_ATTR_DOWNLOAD_SIZE = "downloadsize";
    private static final String MODULE_ATTR_NEEDS_RESTART = "needsrestart";
    private static final String MODULE_ATTR_MODULE_AUTHOR = "moduleauthor";
    private static final String MODULE_ATTR_RELEASE_DATE = "releasedate";
    private static final String MODULE_ATTR_IS_GLOBAL = "global";
    private static final String MODULE_ATTR_TARGET_CLUSTER = "targetcluster";
    private static final String MODULE_ATTR_EAGER = "eager";
    private static final String MODULE_ATTR_AUTOLOAD = "autoload";
    private static final String MODULE_ATTR_LICENSE = "license";
    
    private static final String MANIFEST_ATTR_SPECIFICATION_VERSION = "OpenIDE-Module-Specification-Version";
    
    private static final String L10N_ATTR_LOCALE = "langcode";
    private static final String L10N_ATTR_BRANDING = "brandingcode";
    private static final String L10N_ATTR_MODULE_SPECIFICATION = "module_spec_version";
    private static final String L10N_ATTR_MODULE_MAJOR_VERSION = "module_major_version";
    private static final String L10N_ATTR_LOCALIZED_MODULE_NAME = "OpenIDE-Module-Name";
    private static final String L10N_ATTR_LOCALIZED_MODULE_DESCRIPTION = "OpenIDE-Module-Long-Description";
    
    public static Map<String, UpdateItem> getUpdateItems (File nbmFile) throws IOException, SAXException {
        Map<String, UpdateItem> items = new HashMap<String, UpdateItem> ();
        try {
            SAXParser saxParser = SAXParserFactory.newInstance ().newSAXParser ();
            saxParser.parse (getAutoupdateInfoInputStream (nbmFile), new AutoupdateInfoParser (items, nbmFile));
        } catch (SAXException ex) {
            ERR.log (Level.INFO, ex.getMessage (), ex);
        } catch (IOException ex) {
            ERR.log (Level.INFO, ex.getMessage (), ex);
        } catch (ParserConfigurationException ex) {
            ERR.log (Level.INFO, ex.getMessage (), ex);
        }
        return items;
    }
    
    private Stack<ModuleDescriptor> currentModule = new Stack<ModuleDescriptor> ();
    private Stack<String> currentLicense = new Stack<String> ();
    private List<String> lines = new ArrayList<String> ();

    @Override
    public void characters (char[] ch, int start, int length) throws SAXException {
        lines.add (new String(ch, start, length));
    }

    @Override
    public void endElement (String uri, String localName, String qName) throws SAXException {
        switch (ELEMENTS.valueOf (qName)) {
            case module :
                assert ! currentModule.empty () : "Premature end of module " + qName;
                currentModule.pop ();
                break;
            case l10n :
                break;
            case manifest :
                break;
            case description :
                ERR.info ("Not supported yet.");
                break;
            case module_notification :
                // write module notification
                ModuleDescriptor md = currentModule.peek ();
                assert md != null : "ModuleDescriptor found for " + nbmFile;
                StringBuffer buf = new StringBuffer ();
                for (String line : lines) {
                    buf.append (line);
                }
                md.appendNotification (buf.toString ());
                lines.clear ();
                break;
            case external_package :
                ERR.info ("Not supported yet.");
                break;
            case license :
                assert ! currentLicense.empty () : "Premature end of license " + qName;
                
                // find and fill UpdateLicenseImpl
                StringBuffer sb = new StringBuffer ();
                for (String line : lines) {
                    sb.append (line);
                }
                
                assert currentUpdateLicenseImpl != null : "UpdateLicenseImpl found for " + nbmFile;
                currentUpdateLicenseImpl.setAgreement (sb.toString ());
                
                currentLicense.pop ();
                lines.clear ();
                break;
            default:
                ERR.warning ("Unknown element " + qName);
        }
    }

    @Override
    public void endDocument () throws SAXException {
        ERR.fine ("End parsing " + nbmFile + " at " + System.currentTimeMillis ());
    }

    @Override
    public void startDocument () throws SAXException {
        ERR.fine ("Start parsing " + nbmFile + " at " + System.currentTimeMillis ());
    }

    @Override
    public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (ELEMENTS.valueOf (qName)) {
            case module :
                ModuleDescriptor md = new ModuleDescriptor (nbmFile);
                md.appendModuleAttributes (attributes);
                currentModule.push (md);
                break;
            case l10n :
                // construct l10n
                // XXX
                break;
            case manifest :
                
                // construct module
                ModuleDescriptor desc = currentModule.peek ();
                desc.appendManifest (attributes);
                UpdateItem m = desc.createUpdateItem ();
                
                // put license impl to map for future refilling
                UpdateItemImpl impl = Trampoline.SPI.impl (m);
                currentUpdateLicenseImpl = impl.getUpdateLicenseImpl ();
                
                // put module into UpdateItems
                items.put (desc.getId (), m);
                
                break;
            case description :
                ERR.info ("Not supported yet.");
                break;
            case module_notification :
                break;
            case external_package :
                ERR.info ("Not supported yet.");
                break;
            case license :
                assert lines.isEmpty (): "No other license is processing at start of new license.";
                currentLicense.push (attributes.getValue (LICENSE_ATTR_NAME));
                break;
            default:
                ERR.warning ("Unknown element " + qName);
        }
    }
    
    @Override
    public InputSource resolveEntity (String publicId, String systemId) throws IOException, SAXException {
        return entityResolver.resolveEntity (publicId, systemId);
    }
    
    private static class ModuleDescriptor {
        private String moduleCodeName;
        private String targetcluster;
        private String homepage;
        private String downloadSize;
        private String author;
        private String publishDate;
        private String notification;

        private Boolean needsRestart;
        private Boolean isGlobal;
        private Boolean isEager;
        private Boolean isAutoload;

        private String specVersion;
        private Manifest mf;

        private UpdateLicense lic;
        
        private final File nbmFile;
        
        public ModuleDescriptor (File nbmFile) {
            this.nbmFile = nbmFile;
        }
        
        public void appendModuleAttributes (Attributes module) {
            moduleCodeName = module.getValue (MODULE_ATTR_CODE_NAME_BASE);
            targetcluster = module.getValue (MODULE_ATTR_TARGET_CLUSTER);
            homepage = module.getValue (MODULE_ATTR_HOMEPAGE);
            downloadSize = module.getValue (MODULE_ATTR_DOWNLOAD_SIZE);
            author = module.getValue (MODULE_ATTR_MODULE_AUTHOR);
            publishDate = module.getValue (MODULE_ATTR_RELEASE_DATE);
            if (publishDate == null || publishDate.length () == 0) {
                publishDate = Utilities.formatDate (new Date (nbmFile.lastModified ()));
            }
            String needsrestart = module.getValue (MODULE_ATTR_NEEDS_RESTART);
            String global = module.getValue (MODULE_ATTR_IS_GLOBAL);
            String eager = module.getValue (MODULE_ATTR_EAGER);
            String autoload = module.getValue (MODULE_ATTR_AUTOLOAD);
                        
            needsRestart = needsrestart == null || needsrestart.trim ().length () == 0 ? null : Boolean.valueOf (needsrestart);
            isGlobal = global == null || global.trim ().length () == 0 ? null : Boolean.valueOf (global);
            isEager = Boolean.parseBoolean (eager);
            isAutoload = Boolean.parseBoolean (autoload);
                        
            String licName = module.getValue (MODULE_ATTR_LICENSE);
            lic = UpdateLicense.createUpdateLicense (licName, null);
        }
        
        public void appendManifest (Attributes manifest) {
            specVersion = manifest.getValue (MANIFEST_ATTR_SPECIFICATION_VERSION);
            mf = getManifest (manifest);
        }
        
        public void appendNotification (String notification) {
            this.notification = notification;
        }
        
        public String getId () {
            return moduleCodeName + '_' + specVersion; // NOI18N
        }
        
        public UpdateItem createUpdateItem () {
            URL distributionUrl = null;
            try {
                distributionUrl = nbmFile.toURI().toURL (); // nbm as a source
            } catch (MalformedURLException ex) {
                ERR.log (Level.INFO, null, ex);
            }
            UpdateItem res = UpdateItem.createModule (
                    moduleCodeName,
                    specVersion,
                    distributionUrl,
                    author,
                    downloadSize,
                    homepage,
                    publishDate,
                    null, // no group
                    mf,
                    isEager,
                    isAutoload,
                    needsRestart,
                    isGlobal,
                    targetcluster,
                    lic);
            
            // read module notification
            UpdateItemImpl impl = Trampoline.SPI.impl(res);
            ((ModuleItem) impl).setModuleNotification (notification);
            
            return res;
        }
    }
    
    private static Manifest getManifest (Attributes attrList) {
        Manifest mf = new Manifest ();
        java.util.jar.Attributes mfAttrs = mf.getMainAttributes ();

        for (int i = 0; i < attrList.getLength (); i++) {
            mfAttrs.put (new java.util.jar.Attributes.Name (attrList.getQName (i)), attrList.getValue (i));
        }
        return mf;
    }

    // package-private only for unit testing purpose
    static InputSource getAutoupdateInfoInputStream (File nbmFile) throws IOException, SAXException {
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

        return new InputSource (new BufferedInputStream (jf.getInputStream (entry)));
    }
    
}
