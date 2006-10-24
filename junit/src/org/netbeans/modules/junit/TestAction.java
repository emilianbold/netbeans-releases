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

package org.netbeans.modules.junit;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.actions.CookieAction;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
//XXX: retouche
//import org.openide.cookies.SourceCookie;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.java.project.JavaProjectConstants;

/**
 * @author  or141057
 */
abstract class TestAction extends CookieAction { 
    
    /** Creates a new instance of TestAction */
    public TestAction() {
    }
    
    
    /* protected members */
    protected Class[] cookieClasses() {
        //XXX: retouche
        //original:
//        return new Class[] { DataFolder.class, SourceCookie.class };
        //to allow retouche compilation:
        return new Class[] { DataFolder.class };
    }

    protected int mode() {
        return MODE_ANY;    // allow creation of tests for multiple selected nodes (classes, packages)
    }

    public boolean asynchronous() {
        return true; // yes, this action should run asynchronously
        // would be better to rewrite it to synchronous (running in AWT thread),
        // just replanning test generation to RequestProcessor
    }    
    /** Perform special enablement check in addition to the normal one.
     * protected boolean enable (Node[] nodes) {
     * if (! super.enable (nodes)) return false;
     * if (...) ...;
     * }
     */
    protected boolean enable (Node[] nodes) {
        if (!super.enable(nodes)) return false;
        if (nodes.length == 0) return false;

        for (int i=0; i < nodes.length; i++) {
            if (!isEnabledOnNode(nodes[i])) return false;
        }
        return true;
    }

    
    /**
     * Returns true iff the node represents a source file or package.
     * @param node the Node to query
     * @return true or false
     */
    private static boolean isEnabledOnNode(Node node) {
        DataObject dob = (DataObject)node.getCookie(DataObject.class);
        FileObject fo = null;
        if (dob != null) fo = dob.getPrimaryFile();

        if (fo == null) return false;
        Project prj = FileOwnerQuery.getOwner(fo);
        if (prj == null) return false;
        if (getSourceGroup(fo, prj) != null) return true; 
        else return false;
    }
    

    private static SourceGroup getSourceGroup(FileObject file, Project prj) {
        Sources src = ProjectUtils.getSources(prj);
        SourceGroup[] srcgrps = src.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (int i = 0 ; i < srcgrps.length; i++) {
            FileObject rootFolder = srcgrps[i].getRootFolder();
            if ((file == rootFolder || FileUtil.isParentOf(rootFolder, file)) 
                && (srcgrps[i].contains(file))) 
                    return srcgrps[i];
        }
        return null;
    }    
    
    
}
