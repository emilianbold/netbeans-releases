/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.i18n;

import org.openide.ErrorManager;
import org.openide.util.*;
import org.openide.loaders.DataFilter;
import org.netbeans.api.project.Project;
import java.util.*;
import org.openide.nodes.Node;
import org.netbeans.spi.project.ui.support.LogicalViews;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.nodes.FilterNode;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.openide.loaders.DataObject;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;

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
     * Gets classpath that contains the given resource bundel. 
     * In addition to the bundle file, a source must be given that
     * will access the resource in the run-time.
     */
    public static ClassPath getExecClassPath(FileObject srcFile, FileObject resFile) {
        // try EXECUTE class-path first
        ClassPath ecp = ClassPath.getClassPath( srcFile, ClassPath.EXECUTE );
        if (ecp.getResourceName( resFile, '.',false) != null)
            return ecp;


        // if not directly on EXECUTE, might be on SOURCE
        ClassPath scp = ClassPath.getClassPath( srcFile, ClassPath.SOURCE);
        // try to find  the resource on source class path
        if (scp.getResourceName( resFile, '.',false) != null) 
            return scp; 

        // now try resource owner
        ClassPath rcp = ClassPath.getClassPath( resFile, ClassPath.SOURCE);
        if (rcp!=null) {
            // try to find  the resource on source class path
            if (rcp.getResourceName( resFile, '.',false) != null) 
                return rcp; 
        }

        return null;
    }
        
    /**
     * Tries to find the bundle either in sources or in execution
     * classpath.
     */
    public static FileObject getResource(FileObject srcFile, String bundleName) {
        // try to find it in sources
        ClassPath scp = ClassPath.getClassPath( srcFile, ClassPath.SOURCE);
        FileObject ret = scp.findResource(bundleName);
        if (ret != null) return ret;

        // or on the execution class-path
        ClassPath ecp = ClassPath.getClassPath( srcFile, ClassPath.EXECUTE);
        ret = scp.findResource(bundleName);
        if (ret != null) return ret;

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
        String ret = ecp.getResourceName( resFile, separator, bpar);
        if (ret != null) return ret;

        ClassPath scp = ClassPath.getClassPath( srcFile, ClassPath.SOURCE );
        ret = scp.getResourceName( resFile, separator, bpar);
        if (ret!=null) return ret;

        ClassPath rcp = ClassPath.getClassPath( resFile, ClassPath.SOURCE );
        if (rcp != null) {
            ret = rcp.getResourceName( resFile, separator, bpar);
            if (ret!=null) return ret;
        }
        
        return null;
        
    }

}
