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

import org.openide.filesystems.FileSystem;


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
    public static final String EXT_TARGET = "extensionTarget"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    public static final String EXT_PREFIX = "prefix"; // NOI18N

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
    public static final String ITEM_DESC = "description"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    public static final String JBI_EXTENSIONS = "JbiExtensions"; // NOI18N
    
    private static final String CHOICE = "choice";
    private static final String DEFAULT_CHOICE = "default-choice";
    private static final String DESCRIPTION = "description";
    
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
                    String file = ""; // NOI18N
                    String type = ""; // NOI18N
                    String target = ""; // NOI18N
                    String ns = ""; // NOI18N
                    String desc = ""; // NOI18N
                    URL icon = null;

                    FileObject compFO = extsDO.getPrimaryFile();
                    for (Enumeration<String> e = compFO.getAttributes(); e.hasMoreElements();) {
                        String attrName = e.nextElement();
                        Object attrObj = compFO.getAttribute(attrName);

                        if (attrName.equals(EXT_FILE)) {
                            file = (String) attrObj;
                        } else if (attrName.equals(EXT_TYPE)) {
                            type = (String) attrObj;
                        } else if (attrName.equals(EXT_TARGET)) {
                            target = (String) attrObj;
                        } else if (attrName.equals(EXT_NAMESPACE)) {
                            ns = (String) attrObj;
                        } else if (attrName.equals(EXT_ICON)) {
                            icon = (URL) attrObj;
                        }
                    }
                   
                    List[] children = processElement((DataFolder)extsDO);

                    @SuppressWarnings("unchecked")
                    JbiExtensionInfo extInfo = new JbiExtensionInfo(name, type, 
                            target, file, ns, desc, icon, children[0]);
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
            if (childFO.isFolder()) {
                JbiExtensionElement element;
                String choice = (String) childFO.getAttribute(CHOICE); 
                String description = (String) childFO.getAttribute(DESCRIPTION); 
                List[] grandChildren = processElement((DataFolder)child);  
                List<JbiExtensionElement> subElements = grandChildren[0];
                List<JbiExtensionAttribute> attributes = grandChildren[1];
                if (choice != null && choice.equalsIgnoreCase("true")) { // NOI18N
                    String defaultChoice = (String) childFO.getAttribute(DEFAULT_CHOICE); 
                    element = new JbiChoiceExtensionElement(
                            childName, subElements, attributes, description, 
                            defaultChoice);
                } else {
                    element = new JbiExtensionElement(
                            childName, subElements, attributes, description);
                }
                elements.add(element);                
            } else {
                String extType = (String) childFO.getAttribute(ITEM_TYPE);
                String extDesc = (String) childFO.getAttribute(ITEM_DESC);
                String extCodeGen = (String) childFO.getAttribute(ITEM_CODEGEN);
                JbiExtensionAttribute attr = new JbiExtensionAttribute(
                        childName, 
                        extType, 
                        extDesc,
                        !("false".equalsIgnoreCase(extCodeGen)));
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
}
