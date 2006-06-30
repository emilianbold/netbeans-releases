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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.i18n;

import org.openide.ErrorManager;
import org.openide.util.*;
import org.netbeans.api.project.Project;
import java.util.*;
import org.openide.nodes.Node;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.netbeans.api.project.FileOwnerQuery;

/**
 * Bundle access, ...
 *
 * @author  Petr Kuzel
 */
public class Util {

    public static String getString(String key) {
        return NbBundle.getMessage(Util.class, key);
    }
    
    public static char getChar(String key) {
        return getString(key).charAt(0);
    }
    
    /**
     * Write the exception into log.
     */
    public static void debug(Throwable t) {
        ErrorManager err = ErrorManager.getDefault();
        err.notify(err.INFORMATIONAL, t);
    }

    /**
     * Write annotated exception into log.
     */
    public static void debug(String annotation, Throwable t) {
        ErrorManager err = ErrorManager.getDefault();
        err.annotate(t, err.INFORMATIONAL, annotation, null, null, null);
        err.notify(err.INFORMATIONAL, t);
    }
    
    public static Project getProjectFor(DataObject dobj) {
      Project prj = null;
      FileObject fo = dobj.getPrimaryFile();
      return FileOwnerQuery.getOwner(fo);
    }

  public static Project getProjectFor(Node [] activatedNodes) {
    Project project = null;

    if (activatedNodes.length > 0) {
      DataObject dataObject = (DataObject)activatedNodes[0].getCookie(DataObject.class);
      if(dataObject != null && dataObject.getPrimaryFile() != null) 
	project = FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
    } 
    return project;
  }

    /**
     * Gets classpath that contains the given resource bundle. 
     * In addition to the bundle file, a source must be given that
     * will access the resource at run-time.
     */
    public static ClassPath getExecClassPath(FileObject srcFile, FileObject resFile) {
        // try EXECUTE class-path first
        ClassPath ecp = ClassPath.getClassPath( srcFile, ClassPath.EXECUTE );
        if ((ecp != null) && (ecp.getResourceName( resFile, '.',false) != null))
            return ecp;


        // if not directly on EXECUTE, might be on SOURCE
        ClassPath scp = ClassPath.getClassPath( srcFile, ClassPath.SOURCE);
        // try to find  the resource on source class path
        if ((scp != null) && (scp.getResourceName( resFile, '.',false) != null)) 
            return scp; 

        // now try resource owner
        ClassPath rcp = ClassPath.getClassPath( resFile, ClassPath.SOURCE);
        // try to find  the resource on source class path
        if ((rcp!=null) && (rcp.getResourceName( resFile, '.',false) != null))  
                return rcp; 
        

        return null;
    }
        
    /**
     * Tries to find the bundle either in sources or in execution
     * classpath.
     */
    public static FileObject getResource(FileObject srcFile, String bundleName) {
        // try to find it in sources of the same project
        ClassPath scp = ClassPath.getClassPath( srcFile, ClassPath.SOURCE);
        if (scp != null) {
            FileObject ret = scp.findResource(bundleName);
            if (ret != null) return ret;
        }

        // try to find in sources of execution classpath
        ClassPath ecp = ClassPath.getClassPath( srcFile, ClassPath.EXECUTE);
        Iterator it = ecp.entries().iterator();
        while (it.hasNext()) {
            ClassPath.Entry e = (ClassPath.Entry)it.next();
            SourceForBinaryQuery.Result r = SourceForBinaryQuery.findSourceRoots(e.getURL());
            FileObject[] sourceRoots = r.getRoots();
            for (int i=0; i < sourceRoots.length; i++) {
                // try to find the bundle under this source root
                ClassPath cp = ClassPath.getClassPath(sourceRoots[i], ClassPath.SOURCE);
                if (cp != null) {
                    FileObject ret = cp.findResource(bundleName);
                    if (ret != null)
                        return ret;
                }
            }
        }

        return null;
    }


    /**
     * Inverse to the previous method - finds name for the give
     * resource bundle. It is equivalent but more effective to use
     * this method instead of getExecClassPath(...).getResourceName(...) .
     */
    public static String getResourceName(FileObject srcFile, FileObject resFile, char separator, boolean bpar) {
        // try SOURCE class-path first
        ClassPath ecp = ClassPath.getClassPath( srcFile, ClassPath.EXECUTE );
        if (ecp!=null) {
            String ret = ecp.getResourceName( resFile, separator, bpar);
            if (ret != null) return ret;
        }

        ClassPath scp = ClassPath.getClassPath( srcFile, ClassPath.SOURCE );
        if (scp!= null) {
            String ret = scp.getResourceName( resFile, separator, bpar);
            if (ret!=null) return ret;
        }

        ClassPath rcp = ClassPath.getClassPath( resFile, ClassPath.SOURCE );
        if (rcp != null) {
            String ret = rcp.getResourceName( resFile, separator, bpar);
            if (ret!=null) return ret;
        }
        
        return null;
        
    }

    public static boolean isNbBundleAvailable(DataObject srcDataObject) {
        // is there a good way to recognize that NbBundle is available?
        // - execution CP may not work if everything is cleaned
        // - looking for NbBundle.java in sources of execution CP roots is expensive
        // - checking project impl. class name is ugly
        // - don't know how to check if there is "org.openide.util" module
        ClassPath classPath = ClassPath.getClassPath(srcDataObject.getPrimaryFile(), ClassPath.EXECUTE);
        if (classPath != null && classPath.findResource("org/openide/util/NbBundle.class") != null) // NOI18N
            return true;

        // hack: check project impl. class name
        Project p = FileOwnerQuery.getOwner(srcDataObject.getPrimaryFile());
        if (p != null && p.getClass().getName().startsWith("org.netbeans.modules.apisupport.") // NOI18N
                && p.getClass().getName().endsWith("Project")) // NOI18N
            return true;

        return false;
    }
}
