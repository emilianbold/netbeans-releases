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


package org.netbeans.modules.visualweb.project.jsfloader;


import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.openide.ErrorManager;

import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.FileEntry;
import org.openide.loaders.UniFileLoader;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.util.MapFormat;
import org.openide.util.NbBundle;

import org.netbeans.modules.visualweb.api.insync.InSyncService;


/**
 * Data lodaer of JSF Jsp DataObjects.
 *
 * @author Peter Zavadsky
 */
public class JsfJspDataLoader extends UniFileLoader {


    static final long serialVersionUID =-5809935261731217882L;
    static final ThreadLocal jspTemplateCreation = new ThreadLocal();
    
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

        // It is most likely a JSP file, however, we need to see if there is already a JsfJspDataObject registered
        // for this file object.  There is a case where by in middle of refactoring, the JSP and the JAVA file are not
        // linked as they should be, but there is still a JsfJspDataObject registered uner this file object
        DataObject dataObject = JsfJavaDataLoader.DataObjectPoolFind(fo);
        if (dataObject instanceof JsfJspDataObject) {
            return fo;
        }
        
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

