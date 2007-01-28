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

package org.netbeans.modules.visualweb.project.jsf.api;

import org.netbeans.modules.visualweb.project.jsf.*;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import org.netbeans.api.project.Project;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Utilities;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public abstract class ProjectTemplate {
    private String beanPackage = null;

    public ProjectTemplate() {
    }

    public abstract void addLibrary(Project project) throws IOException;

    public abstract void create(Project project, String j2eeLevel) throws IOException;

    public abstract void instantiateFile(Project project, Node node, FileObject folder, String j2eeLevel) throws IOException;

    protected FileObject instantiateFileTemplate(FileObject folder, String name, String templateName) throws IOException {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        FileObject FO = fs.findResource(templateName);
        if (FO == null) {
            IOException iox = new IOException("Can't find template FileObject for " + templateName); // NOI18N - internal error
            ErrorManager.getDefault().notify(ErrorManager.ERROR, iox);
            throw iox;
        }
        DataFolder folderDataObj = (DataFolder)DataObject.find(folder);
        DataObject template = DataObject.find(FO);
        String ext = FileUtil.getExtension(name);
        if (ext.length() > 0)
            name = name.substring(0, name.lastIndexOf(ext) - 1);
        DataObject newDO = null;
        try {
            newDO = template.createFromTemplate(folderDataObj, name);
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
        FileSystem dfs = Repository.getDefault().getDefaultFileSystem();
        FileObject templateRootFolder = dfs.findResource(JsfProjectConstants.USER_TEMPLATE_FS_ROOT);
        assert templateRootFolder != null;
        
        FileObject boilerplate = dfs.findResource(JsfProjectConstants.USER_TEMPLATE_FS_BOILERPLATE);
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
