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


package org.netbeans.modules.visualweb.project.jsfloader;


import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import java.io.IOException;
import org.openide.ErrorManager;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.util.NbBundle;



/**
 * Data lodaer of JSF Jsp DataObjects.
 *
 * @author Peter Zavadsky
 */
public class JsfJspDataLoader extends UniFileLoader {


    static final long serialVersionUID =-5809935261731217882L;
    static final ThreadLocal<Boolean> jspTemplateCreation = new ThreadLocal<Boolean>();
    
    public JsfJspDataLoader() {
        // Use String representation instead of JsfJspDataObject.class.getName() for classloading performance
        super("org.netbeans.modules.visualweb.project.jsfloader.JsfJspDataObject"); // NOI18N
    }

    protected void initialize() {
        super.initialize();
        getExtensions().addExtension ("jsp"); // NOI18N
        getExtensions().addExtension ("jspf"); // NOI18N

    }

    protected FileObject findPrimaryFile(FileObject fo) {
        if (fo.isFolder()) {
            return null;
        }

        FileObject primaryFile = super.findPrimaryFile(fo);
        if (primaryFile == null) {
            return null;
        }

        Object attrib = primaryFile.getAttribute(JsfJspDataObject.JSF_ATTRIBUTE);
        if (attrib != null && attrib instanceof Boolean) {
            if (((Boolean)attrib).booleanValue() == true) {
                return primaryFile;
            }else {
                return null;
            }
        }
        
        // XXX JsfProjectUtils.isJsfProjectFile() is very slow and should be revisited
        // since this affects all loaded jsp files in NetBeans
        boolean isTemplate = Utils.isTemplateFileObject(primaryFile);
        if (!isTemplate && !JsfProjectUtils.isJsfProjectFile(primaryFile)) {
            return null;
        }

        // XXX #146796 The lines below are redundant. If the fileobject is already assigned to data object, than this method is not called.
//        // It is most likely a JSP file, however, we need to see if there is already a JsfJspDataObject registered
//        // for this file object.  There is a case where by in middle of refactoring, the JSP and the JAVA file are not
//        // linked as they should be, but there is still a JsfJspDataObject registered uner this file object
//        DataObject dataObject = JsfJavaDataLoader.DataObjectPoolFind(fo);
//        if (dataObject instanceof JsfJspDataObject) {
//            return fo;
//        }
        
        // Handle DataObject during template instantiation specially since the
        // corresponding java file may not have been generated yet
        if (jspTemplateCreation.get() == Boolean.TRUE) {
            return primaryFile;
        }
        
        // Now do the normal stuff
        FileObject javaFile = Utils.findJavaForJsp(primaryFile);
        if(javaFile == null) {
            // If there is no java backing file, then this is not a JSF jsp data object.
            return null;
        }
        // It has to be linked vice versa too.
        if(primaryFile != Utils.findJspForJava(javaFile)) {
            return null;
        }

        if (!isTemplate)
            setJsfFileAttribute(primaryFile);
        
        return primaryFile;
    }

    protected MultiDataObject createMultiObject(final FileObject primaryFile)
    throws DataObjectExistsException, IOException {
        return new JsfJspDataObject(primaryFile, this);
    }

    protected String defaultDisplayName() {
        return NbBundle.getMessage(JsfJspDataLoader.class, "PROP_JsfJspDataLoader_Name");
    }

    protected String actionsContext () {
        return "Loaders/text/x-jsp/visual/Actions/"; // NOI18N
    }

    private void setJsfFileAttribute(FileObject fo) {
        try {
            fo.setAttribute(JsfJspDataObject.JSF_ATTRIBUTE, Boolean.TRUE);
        }catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }
}

