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
import org.netbeans.api.java.loaders.JavaDataSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.NbBundle;

/**
 * Data lodaer of JSF Java DataObjects, what means it loads java file,
 * only in case there is corresponding jsp file, and maintains its loose
 * reference, which is kind-of one part of virtual JSF DataObject (which
 * we are simulating).
 *
 * @author  Peter Zavadsky
 */
public class JsfJavaDataLoader extends MultiFileLoader {


    static final long serialVersionUID =-5809935261731217882L;
    
    public static final String JAVA_EXTENSION = "java"; // NOI18N
    
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
        // use String name instead of class.getName() for performance reasons
        super("org.netbeans.modules.visualweb.project.jsfloader.JsfJavaDataObject"); // NOI18N
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

        FileObject primaryFile = findPrimaryJavaFile(fo);
        if (primaryFile == null) {
            return null;
        }

        Object attrib = primaryFile.getAttribute(JsfJavaDataObject.JSF_ATTRIBUTE);
        if (attrib != null && attrib instanceof Boolean) {
            if (((Boolean)attrib).booleanValue() == true) 
                return primaryFile;
            else
                return null;
        }

        // XXX JsfProjectUtils.isJsfProjectFile() is very slow and should be revisited
        // since this affects all loaded java files in NetBeans
        boolean isTemplate = Utils.isTemplateFileObject(primaryFile);
        if (!isTemplate && !JsfProjectUtils.isJsfProjectFile(primaryFile)) {
            return null;
        }
        
        // It is most likely a Java file, however, we need to see if there is already a JsfJavaDataObject registered
        // for this file object.  There is a case where by in middle of refactoring, the JSP and the JAVA file are not
        // linked as they should be, but there is still a JsfJavaDataObject registered uner this file object
        // Duplicated in JsfJspDataObject
        // XXX This causes some performance issues and should probably be removed
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

        if (!isTemplate) {
            setJsfFileAttribute(primaryFile);
        }
        
        return primaryFile;
    }
    
    protected MultiDataObject createMultiObject(final FileObject primaryFile)
    throws DataObjectExistsException, IOException {
        return new JsfJavaDataObject(primaryFile, this);
    }
    
    @Override
    protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
        return JavaDataSupport.createJavaFileEntry(obj, primaryFile);        
    }

    @Override
    protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private FileObject findPrimaryJavaFile(FileObject fo) {
        if (fo.getExt().equals(JAVA_EXTENSION)) {
            return fo;
        }
        return null;
    }
    
    private void setJsfFileAttribute(FileObject fo) {
        try {
            fo.setAttribute(JsfJavaDataObject.JSF_ATTRIBUTE, Boolean.TRUE);
        }catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }
}

