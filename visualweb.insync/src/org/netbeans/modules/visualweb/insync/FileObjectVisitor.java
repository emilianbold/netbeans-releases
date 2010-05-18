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
