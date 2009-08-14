/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.compapp.projects.jbi.api;

import java.util.ArrayList;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;

import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

import java.util.Enumeration;
import java.util.List;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;


/**
 * DOCUMENT ME!
 *
 * @author
 * @version
 */
public class JbiInstalledExtensionInfo {
    /**
     * DOCUMENT ME!
     */
    public static final String EXT_FILE = "xsdFileName"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    public static final String EXT_TYPE = "extensionType"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    public static final String EXT_SUBTYPE = "extensionSubType"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    public static final String EXT_TARGET = "extensionTarget"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    public static final String EXT_PREFIX = "prefix"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    public static final String EXT_PROVIDER = "extensionClassProvider"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    public static final String EXT_NAMESPACE = "namespace"; // NOI18N
    /**
     * DOCUMENT ME!
     */
    public static final String EXT_ICON = "SystemFileSystem.icon"; // NOI18N
    /**
     * DOCUMENT ME!
     */
    public static final String ITEM_TYPE = "type"; // NOI18N
    /**
     * DOCUMENT ME!
     */
    public static final String ITEM_CODEGEN = "codegen"; // NOI18N
    /**
     * DOCUMENT ME!
     */
    public static final String ITEM_DEFAULT_VALUE = "defaultValue"; // NOI18N
    /**
     * DOCUMENT ME!
     */
    public static final String ITEM_DESC = "description"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    public static final String JBI_EXTENSIONS = "JbiExtensions"; // NOI18N
    
    private static final String CHOICE = "choice";
    private static final String DEFAULT_CHOICE = "default-choice";
    
    private static JbiInstalledExtensionInfo singleton = null;

    // a list of Jbi Extension Info known at design time
    private List<JbiExtensionInfo> extensionList = new ArrayList<JbiExtensionInfo>();

    // mapping Jbi Extension Info name to the component
    private Map<String, JbiExtensionInfo> extensionMap = new HashMap<String, JbiExtensionInfo>();

    private JbiInstalledExtensionInfo() {
    }

    /**
     * Factory method for the default component list object
     *
     * @return the default component list object
     */
    public static JbiInstalledExtensionInfo getInstalledExtensionInfo() {
        if (singleton == null) {
            FileSystem fileSystem = Repository.getDefault().getDefaultFileSystem();
            return getInstalledExtensionInfo(fileSystem);
        }

        return singleton;
    }
    
    static JbiInstalledExtensionInfo getInstalledExtensionInfo(FileSystem fileSystem) {
        if (singleton == null) {
            try {
                singleton = new JbiInstalledExtensionInfo();

                // load new container first
                FileObject fo = fileSystem.findResource(JBI_EXTENSIONS);
                loadJbiDefaultExtensionInfoFromFileObject(fo);

            } catch (Exception ex) {
                // failed... return withopt changing the selector content.
                ex.printStackTrace();
            }
        }

        return singleton;
    }

    /*private*/ static void loadJbiDefaultExtensionInfoFromFileObject(FileObject fo) {
        if (fo != null) {
            DataFolder df = DataFolder.findFolder(fo);
            for (DataObject extsDO : df.getChildren()) {
                if (extsDO instanceof DataFolder) {
                    String name = extsDO.getName();
                    String displayName = extsDO.getNodeDelegate().getDisplayName();
                    String desc = getLocalizedFileObjectAttribute(extsDO.getPrimaryFile(), ITEM_DESC);
                    String file = null; 
                    String type = null; 
                    String subType = null; 
                    String target = null; 
                    String ns = null; 
                    String provider = null; 
                    URL icon = null;

                    FileObject compFO = extsDO.getPrimaryFile();
                    for (Enumeration<String> e = compFO.getAttributes(); e.hasMoreElements();) {
                        String attrName = e.nextElement();
                        Object attrObj = compFO.getAttribute(attrName);

                        if (attrName.equals(EXT_FILE)) {
                            file = (String) attrObj;
                        } else if (attrName.equals(EXT_TYPE)) {
                            type = (String) attrObj;
                        } else if (attrName.equals(EXT_SUBTYPE)) {
                            subType = (String) attrObj;
                        } else if (attrName.equals(EXT_TARGET)) {
                            target = (String) attrObj;
                        } else if (attrName.equals(EXT_NAMESPACE)) {
                            ns = (String) attrObj;
                        } else if (attrName.equals(EXT_ICON)) {
                            icon = (URL) attrObj;
                        } else if (attrName.equals(EXT_PROVIDER)) {
                            provider = (String) attrObj;
                        }
                    }
                   
                    List[] children = processElement((DataFolder)extsDO);

                    @SuppressWarnings("unchecked")
                    JbiExtensionInfo extInfo = new JbiExtensionInfo(name, displayName, type, subType,
                            target, file, ns, desc, icon, provider, children[0]);
                    singleton.extensionList.add(extInfo);
                    singleton.extensionMap.put(name, extInfo);
                }
            }
        }
    }
    /**
     *
     */
    @SuppressWarnings("unchecked")
    private static List[] processElement(DataFolder ext) {
        List[] ret = new ArrayList[2];
        
        List<JbiExtensionElement> elements = new ArrayList<JbiExtensionElement>();
        List<JbiExtensionAttribute> attrs = new ArrayList<JbiExtensionAttribute>();
        
        for (DataObject child : ext.getChildren()) {
            FileObject childFO = child.getPrimaryFile();
            String childName = child.getName();
            String childDisplayName = child.getNodeDelegate().getDisplayName();
            String childDescription = getLocalizedFileObjectAttribute(childFO, ITEM_DESC); 
            if (childFO.isFolder()) {
                JbiExtensionElement element;
                String choice = (String) childFO.getAttribute(CHOICE); 
                List[] grandChildren = processElement((DataFolder)child);  
                List<JbiExtensionElement> subElements = grandChildren[0];
                List<JbiExtensionAttribute> attributes = grandChildren[1];
                if (choice != null && choice.equalsIgnoreCase("true")) { // NOI18N
                    String defaultChoice = (String) childFO.getAttribute(DEFAULT_CHOICE); 
                    element = new JbiChoiceExtensionElement(
                            childName, childDisplayName,
                            subElements, attributes, childDescription, 
                            defaultChoice);
                } else {
                    element = new JbiExtensionElement(
                            childName, childDisplayName,
                            subElements, attributes, childDescription);
                }
                elements.add(element);                
            } else {
                String childType = (String) childFO.getAttribute(ITEM_TYPE);
                String codeGen = (String) childFO.getAttribute(ITEM_CODEGEN);
                String defaultValue = (String) childFO.getAttribute(ITEM_DEFAULT_VALUE);
                JbiExtensionAttribute attr = new JbiExtensionAttribute(
                        childName, 
                        childDisplayName,
                        childType, 
                        childDescription,
                        !("false".equalsIgnoreCase(codeGen)),
                        defaultValue); // NOI18N
                attrs.add(attr);
            }
        }
        
        ret[0] = elements;
        ret[1] = attrs;
        
        return ret;
    }
    
    /**
     * Getter for the installed extension info list
     *
     * @return the default installed extension info list
     */
    public List<JbiExtensionInfo> getJbiExtensionList() {
        return extensionList;
    }

    /**
     * Getter for the specific extension info
     *
     * @param  id  extension identifier
     * @return the specific extension info
     */
    public JbiExtensionInfo getExtensionInfo(String id) {
        return extensionMap.get(id);
    }
        

    /** name of file attribute with localizing bundle */
    private static final String ATTR_BUNDLE = "SystemFileSystem.localizingBundle"; // NOI18N
    
    /** 
     * Gets the localized value of a file object attribute. 
     * 
     * The key for the file object attribute in the localized bundle is in
     * the format of ${fileOjectPath}@${attrName}. For example, 
     * JbiExtensions/RedeliveryExtension/redelivery/maxAttempts@description
     * 
     * @param fo    a file object
     * @param attrName  attribute name
     * @return the localized value of the file object attribute, or the 
     *          non-localized value if the localization doesn't exist.
     */
    private static String getLocalizedFileObjectAttribute(FileObject fo, 
            String attrName) {
        
        String ret = null;
                
        String bundleName = (String)fo.getAttribute(ATTR_BUNDLE); 
        
        // try the resource bundle first
        if (bundleName != null) {
            bundleName = org.openide.util.Utilities.translate(bundleName);
            ResourceBundle bundle = NbBundle.getBundle(bundleName);
            String key = fo.getPath() + "@" + attrName; // NOI18N
            try {
                ret = bundle.getString(key);
            } catch (MissingResourceException ex) {
                System.err.println("WARNING: Missing resource for " + key); // NOI18N
            }
        }      
        
        // fall back on the non-localized value
        if (ret == null) {
            ret = (String) fo.getAttribute(attrName);
            
            if (ret == null) {
                ret = ""; // NOI18N
            }
        }        
        
        return ret;
    }
}
