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
     * Gets EXECUTION ClassPath for the given resource file accessible from the
     * given source file.
     */
    public static ClassPath getExecClassPath(FileObject srcFile, FileObject resFile) {
        ClassPath cp = ClassPath.getClassPath( srcFile, ClassPath.EXECUTE );
        String name = cp.getResourceName( resFile, '.', false );

        if (name == null) { // not found, try SOURCE class-path
            ClassPath cp1 = ClassPath.getClassPath( srcFile, ClassPath.SOURCE );
            return cp1;
        } else {
            return cp;
        }
    }
        
        

}
