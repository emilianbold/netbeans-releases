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
package org.netbeans.modules.visualweb.insync;

import java.util.Enumeration;

import org.openide.filesystems.FileObject;
import org.netbeans.modules.visualweb.extension.openide.util.Trace;

/**
 * A general purpose visitor class for use in traversing a directory structure.
 * @author eat
 */
public abstract class FileObjectVisitor {

    /**
     * Subclass hook method that gets called for every file object in a file object.
     */
    protected abstract void visitImpl(FileObject file);

    /**
     * Visit a specific leaf node, gathering some info and logging where we are before calling the
     * actual protected worker method.
     */
    public void visit(FileObject file) {
        if (file == null) {  // in case the project item exists, but the physical file does not
            assert Trace.trace("insync.model", "PV.visit file:null");
            return;
        }

        if (Trace.ON) {
            String mime = file.getMIMEType();
            assert Trace.trace("insync.model", "PV.visit file:" + file + " mime:" + mime);
        }

        visitImpl(file);
    }

    /**
     * Travers the directory tree or any of its folders and visit the nodes within.
     */
    public void traverse(FileObject root) {
        Enumeration enumeration = root.getChildren(true);
        while (enumeration.hasMoreElements()) {
            FileObject item = (FileObject)enumeration.nextElement();
            if (item.getAttribute("NBIssue81746Workaround") == Boolean.TRUE) { // NOI18N
                try {
                    item.setAttribute("NBIssue81746Workaround", null); // NOI18N
                } catch (java.io.IOException ex) {
                    ex.printStackTrace();
                }
            }
            if (item.isData())
                visit(item);
        }

    }
}
