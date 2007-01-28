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
import java.io.IOException;
import java.lang.reflect.Method;

import org.netbeans.modules.java.JavaDataLoader;

import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.util.NbBundle;

/**
 * Data lodaer of JSF Java DataObjects, what means it loads java file,
 * only in case there is corresponding jsp file, and maintains its loose
 * reference, which is kind-of one part of virtual JSF DataObject (which
 * we are simulating).
 *
 * @author  Peter Zavadsky
 */
public class JsfJavaDataLoader extends JavaDataLoader {


    static final long serialVersionUID =-5809935261731217882L;

    protected static Object dataObjectPool;
    protected static Method dataObjectPoolFindMethod;

    static {
        // EAT: All this to make sure I dont make a change to NB to make DataObjectPool public
        try {
            Class clazz = Class.forName("org.openide.loaders.DataObjectPool");
            Method getPoolMethod = clazz.getDeclaredMethod("getPOOL", new Class[0]);
            getPoolMethod.setAccessible(true);
            dataObjectPool = getPoolMethod.invoke(null, new Object[0]);
            dataObjectPoolFindMethod = clazz.getDeclaredMethod("find", new Class[] {FileObject.class});
            dataObjectPoolFindMethod.setAccessible(true);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.ERROR, e);
        }
    }

    public static DataObject DataObjectPoolFind(FileObject fileObject) {
        // EAT: All this to make sure I dont make a change to NB to make DataObjectPool public
        try {
            Object result = dataObjectPoolFindMethod.invoke(dataObjectPool, new Object[] {fileObject});
            return (DataObject) result;
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.ERROR, e);
            return null;
        }
    }

    public JsfJavaDataLoader() {
        super(JsfJavaDataObject.class.getName());
    }


    protected String defaultDisplayName() {
        return NbBundle.getMessage(JsfJavaDataLoader.class, "PROP_JsfJavaDataLoader_Name");
    }

    protected String actionsContext() {
        return "Loaders/text/x-java/Actions/"; // NOI18N
    }

    protected ThreadLocal findPrimaryFileThreadLocal = new ThreadLocal();

    protected FileObject findPrimaryFile(FileObject fo) {
        if (fo.isFolder()) {
            return null;
        }

        FileObject primaryFile = super.findPrimaryFile(fo);
        if (primaryFile == null) {
            return null;
        }

        // Needs to check whether the file belongs to the Creator JSF project,
        // The file in NB project has to fall back to NB loaders.
        // XXX Workaround for NB issue #78424
        if (!JsfProjectUtils.isJsfProjectFile(primaryFile) && (primaryFile.getAttribute("template") != Boolean.TRUE)) {
            return null;
        }

        // It is most likely a Java file, however, we need to see if there is already a JsfJavaDataObject registered
        // for this file object.  There is a case where by in middle of refactoring, the JSP and the JAVA file are not
        // linked as they should be, but there is still a JsfJavaDataObject registered uner this file object
        // Duplicated in JsfJspDataObject
        DataObject dataObject = DataObjectPoolFind(fo);
        if (dataObject instanceof JsfJavaDataObject) {
            return fo;
        }
        // We now know look for the jsp file
        FileObject jspFile = Utils.findJspForJava(primaryFile);
        if(jspFile == null) {
            // If there is no backing file, then this is not a JSF data object.
            return null;
        }
        // It has to be linked vice versa too.
        if(primaryFile != Utils.findJavaForJsp(jspFile)) {
            return null;
        }

        return primaryFile;
    }
    
    protected MultiDataObject createMultiObject(final FileObject primaryFile)
    throws DataObjectExistsException, IOException {
        return new JsfJavaDataObject(primaryFile, this);
    }
    
}

