/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.visualweb.project.jsf.api;


import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Utilities;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


public abstract class ProjectTemplate {
    private String beanPackage = null;

    public ProjectTemplate() {
    }

    public abstract void addLibrary(Project project) throws IOException;

    public abstract void create(Project project, String pageName) throws IOException;

    public abstract void instantiateFile(Project project, Node node, FileObject folder, String pageName) throws IOException;

    protected FileObject instantiateFileTemplate(FileObject folder, String name, String templateName, Map<String, String> parameters) throws IOException {
        FileObject FO = FileUtil.getConfigFile(templateName);
        if (FO == null) {
            IOException iox = new IOException("Can't find template FileObject for " + templateName); // NOI18N - internal error
            ErrorManager.getDefault().notify(ErrorManager.ERROR, iox);
            throw iox;
        }

        // Allow file been created under subdir of the root.
        int pindex = name.lastIndexOf('/');
        if (pindex != -1) {
            String path = name.substring(0, pindex);
            folder = FileUtil.createFolder(folder, path);
            name = name.substring(pindex+1);
        }

        DataFolder folderDataObj = (DataFolder)DataObject.find(folder);
        DataObject template = DataObject.find(FO);
        String ext = FileUtil.getExtension(name);
        if (ext.length() > 0)
            name = name.substring(0, name.lastIndexOf(ext) - 1);
        DataObject newDO = null;
        try {
            if (parameters != null) {
                newDO = template.createFromTemplate(folderDataObj, name, parameters);
            } else {
                newDO = template.createFromTemplate(folderDataObj, name);
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
        if (newDO == null)
            return null;
        else
            return newDO.getPrimaryFile();
    }


    protected String getAttr(Node node, String key) {
        String value = "";  // NOI18N
        NamedNodeMap attrs = node.getAttributes();
        if (attrs != null) {
            Node attr = attrs.getNamedItem(key);
            if (attr != null) {
                try {
                    value = attr.getNodeValue();
                } catch (Exception e) {
                    // default prevails
                }
            }
        }
        return value;
    }


    /**
     * Derive an identifier suitable for a java package name or context path
     * @param sourceName Original name from which to derive the name
     * @return An identifier suitable for a java package name or context path
     */
    public static String deriveSafeName(String sourceName) {
        StringBuffer dest   = new StringBuffer(sourceName.length());
        int sourceLen = sourceName.length();
        if (sourceLen > 0) {
            int pos = 0;
            while (pos < sourceLen) {
                if (Character.isJavaIdentifierStart(sourceName.charAt(pos))) {
                    dest.append(Character.toLowerCase(sourceName.charAt(pos)));
                    pos++;
                    break;
                }
                pos++;
            }

            for (int i = pos; i < sourceLen; i++) {
                if (Character.isJavaIdentifierPart(sourceName.charAt(i)))
                    dest.append(Character.toLowerCase(sourceName.charAt(i)));
            }
        }
        if (dest.length() == 0 || !Utilities.isJavaIdentifier(dest.toString()))
            return "untitled";  // NOI18N
        else
            return dest.toString();
    }
    
    public void setBeanPackage(String pkg) {
        beanPackage = pkg;
    }

    public String getBeanPackage() {
        return beanPackage;
    }

     /* 
     * Adding a project to the templates list means adding a node to the System filesystem
     * under Templates/Projects/MyTemplates. We use a boilerplate to clone most of the entries
     * for such a node. All we really need to supply is the path to the project that will
     * serve as a template.
     *
     */
    
    public static void addToTemplateChooser(Project project, String name) throws IOException {
        FileObject templateRootFolder = FileUtil.getConfigFile(JsfProjectConstants.USER_TEMPLATE_FS_ROOT);
        assert templateRootFolder != null;
        
        FileObject boilerplate = FileUtil.getConfigFile(JsfProjectConstants.USER_TEMPLATE_FS_BOILERPLATE);
        assert boilerplate != null;
        
        Enumeration attrs = boilerplate.getAttributes();
        
        URL templateUrl = null;
        try {
            templateUrl = FileUtil.toFile(project.getProjectDirectory()).toURL();
        } catch (Exception e) {
            throw new IOException("Unable to create template URLs");  // NOI18N
        }
                
        FileObject template = templateRootFolder.createData(name);
        
        // Copy over the attributes from the boilerplate template
        while (attrs.hasMoreElements()) {
            String key = (String)attrs.nextElement();
            template.setAttribute(key, boilerplate.getAttribute(key));
        }
        
        // Point to the user's project on disk
        template.setAttribute(JsfProjectConstants.USER_TEMPLATE_DIR_TAG, templateUrl);       
    }
    
}
