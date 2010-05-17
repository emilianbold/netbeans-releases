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


package org.netbeans.modules.visualweb.project.jsfloader;

import java.beans.PropertyVetoException;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;

import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;


/**
 * Utilities class for this package.
 *
 * @author  Peter Zavadsky
 */
final class Utils {


    private Utils() {
    }


    /** Finds corresponding 'backing' java fileobject for specified jsp file objecjt.
     * @return <code>FileObject</code> of corresponding java backing file or <code>null</code> if doesn't exist */
    public static FileObject findJavaForJsp(FileObject jsfFileObject) {
        if(jsfFileObject == null) {
            return null;
        }

        if(isTemplateFileObject(jsfFileObject)) {
            // If this is a template, then the backing file will be in the same dir.
            FileObject folder = jsfFileObject.getParent();
            return folder.getFileObject(jsfFileObject.getName(), "java"); // NOI18N
        } else {
            return JsfProjectUtils.getJavaForJsp(jsfFileObject);
        }
    }

    /** Finds corresponding jsp java file object for specified java 'backing' file object.
     * @return <code>FileObject</code> of corresponding java backing file or <code>null</code> if doesn't exist */
    public static FileObject findJspForJava(FileObject javaFileObject) {
        if(javaFileObject == null) {
            return null;
        }

        if(isTemplateFileObject(javaFileObject)) {
            // If this is a template, then the backing file will be in the same dir.
            FileObject folder = javaFileObject.getParent();
            FileObject jspFileObject = folder.getFileObject(javaFileObject.getName(), "jsp"); // NOI18N
            if(jspFileObject != null) {
                return jspFileObject;
            }
            return folder.getFileObject(javaFileObject.getName(), "jspf"); // NOI18N
        } else {
            return JsfProjectUtils.getJspForJava(javaFileObject);
        }
    }

    /** Finds corresponding jsp java folder object for specified jsp file object.
     * @return <code>FileObject</code> of corresponding java backing file or <code>null</code> if doesn't exist */
    public static FileObject findJavaFolderForJsp(FileObject jspFileObject) {
        if(jspFileObject == null) {
            return null;
        }

        if(isTemplateFileObject(jspFileObject)) {
            return jspFileObject.getParent();
        } else {
            return JsfProjectUtils.getJavaFolderForJsp(jspFileObject);
        }
    }

    /** Finds corresponding jsf jsp folder object for specified java 'backing' file object.
     * @return <code>FileObject</code> of corresponding java backing file or <code>null</code> if doesn't exist */
    public static FileObject findJspFolderForJava(FileObject javaFileObject) {
        if(javaFileObject == null) {
            return null;
        }

        if(isTemplateFileObject(javaFileObject)) {
            return javaFileObject.getParent();
        } else {
            return JsfProjectUtils.getJspFolderForJava(javaFileObject);
        }
    }

    public static boolean isTemplateFileObject(FileObject fo) {
        Object o = fo.getAttribute(DataObject.PROP_TEMPLATE);
        boolean hasTemplateAttribute;
        if(o instanceof Boolean) {
            hasTemplateAttribute = ((Boolean)o).booleanValue();
        } else {
            hasTemplateAttribute = false;
        }

        if(hasTemplateAttribute) {
            return true;
        }

        FileObject templatesFolder = FileUtil.getConfigFile("Templates"); // NOI18N
        if(templatesFolder != null) {
            return FileUtil.isParentOf(templatesFolder, fo);
        } else {
            return false;
        }
    }

    /** Finds corresponding Jsf Jsp data object to the specified java 'backing' file object.
     * @param quietly TODO
     * @return <code>JsfJspDataObject</code> instance of <code>null</code> if it doesn't exist */
    public static JsfJspDataObject findCorrespondingJsfJspDataObject(FileObject jsfJavaFileObject, boolean quietly) {
        FileObject jspFile = Utils.findJspForJava(jsfJavaFileObject);
        if(jspFile == null) {
            if (!quietly)
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new IllegalStateException("Can't find corresponding jsp file to " + jsfJavaFileObject)); // NOI18N
            return null;
        } else {
            try {
                DataObject dob = DataObject.find(jspFile);
                if(dob instanceof JsfJspDataObject) {
                    return (JsfJspDataObject)dob;
                } else {
                    try {
                        dob.setValid(false);
                        dob = DataObject.find(jspFile);
                    } catch (PropertyVetoException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException("Corresponding jsp data object is not jsf " + dob)); // NOI18N
                    }
                    
                    if (!quietly)
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                new IllegalStateException("Corresponding jsp data object is not jsf " + dob)); // NOI18N
                    return null;
                }
            } catch(DataObjectNotFoundException dnfe) {
                if (!quietly)
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, dnfe);
                return null;
            }
        }
    }
    
    /** Finds corresponding Jsf Jsp editor support to the specified java 'backing' file object.
     * @param quietly TODO
     * @return <code>JsfJspEditorSupport</code> instance of <code>null</code> if it doesn't exist */
    public static JsfJspEditorSupport findCorrespondingJsfJspEditorSupport(FileObject jsfJavaFileObject, boolean quietly) {
        JsfJspDataObject jsfJspDataObject = findCorrespondingJsfJspDataObject(jsfJavaFileObject, false);
        if(jsfJspDataObject == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                new IllegalStateException("Can't find jsp data object to " + jsfJavaFileObject)); // NOI18N
            return null;
        } else {
            return (JsfJspEditorSupport)jsfJspDataObject.getCookie(JsfJspEditorSupport.class);
        }
    }

    /** Finds corresponding Jsf Java data object to the specified jsp file object.
     * @param quietly TODO
     * @return <code>JsfJspEditorSupport</code> instance of <code>null</code> if it doesn't exist */
    public static JsfJavaDataObject findCorrespondingJsfJavaDataObject(FileObject jsfJspFileObject, boolean quietly) {
        FileObject javaFile = Utils.findJavaForJsp(jsfJspFileObject);
        JsfJavaDataObject jsfJavaDataObject;
        if(javaFile == null) {
            if (!quietly)
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new IllegalStateException("Can't find corresponding java file to " + jsfJspFileObject)); // NOI18N
            return null;
        } else {
            try {
                DataObject dob = DataObject.find(javaFile);
                if(dob instanceof JsfJavaDataObject) {
                    return (JsfJavaDataObject)dob;
                } else {
                    try {
                        dob.setValid(false);
                        dob = DataObject.find(javaFile);
                    }catch (PropertyVetoException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                new IllegalStateException("Corresponding java data object is not jsf " + dob)); // NOI18N
                    }
                    
                    return (dob instanceof JsfJavaDataObject) ? (JsfJavaDataObject)dob : null;
                }
            } catch(DataObjectNotFoundException dnfe) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, dnfe);
                return null;
            }
        }
    }
    
    /** Finds corresponding Java 'backing' editor support to the specified jsf jsp file object.
     * @param quietly TODO
     * @return <code>JsfJavaEditorSupport</code> instance of <code>null</code> if it doesn't exist */
    public static JsfJavaEditorSupport findCorrespondingJsfJavaEditorSupport(FileObject jsfJspFileObject, boolean quietly) {
        JsfJavaDataObject jsfJavaDataObject = findCorrespondingJsfJavaDataObject(jsfJspFileObject, quietly);
        if(jsfJavaDataObject == null) {
            if (!quietly)
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                        new IllegalStateException("Can't find java data object to " + jsfJspFileObject)); // NOI18N
            return null;
        } else {
            return (JsfJavaEditorSupport)jsfJavaDataObject.getCookie(JsfJavaEditorSupport.class);
        }
    }

    /**
     * Return the logical bean name of a jsp.
     */
    public static final String getBeanNameForJsp(FileObject jspFile) {
        String beanName = JsfProjectUtils.getBasePathForJsp(jspFile);
        if (beanName == null)
            return null;
        if (beanName.length() == 0)
            return beanName;
        if (beanName.charAt(0) == '/')
            beanName = beanName.substring(1);
        beanName = beanName.replace('/', '$');
        return beanName;
    }   
}

