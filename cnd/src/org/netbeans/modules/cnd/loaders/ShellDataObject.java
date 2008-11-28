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
package org.netbeans.modules.cnd.loaders;


import org.openide.filesystems.FileObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.DataObjectExistsException;
import org.openide.nodes.Node;
import org.openide.nodes.CookieSet;

import org.netbeans.modules.cnd.execution.ShellExecSupport;
import org.openide.text.DataEditorSupport;

/**
 *  Represents a Shell object in the Repository.
 */
public class ShellDataObject extends CndDataObject {

    /** Serial version number */
    static final long serialVersionUID = -5853234372530618782L;

    /** Constructor for this class */
    public ShellDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
    }

//    /*
//     * Return name with extension so renaming etc works
//     * But this breaks creating makefile with names xxx.mk from templates, so
//     * check if name starts with "__", then return name without the 'extension'
//     * (4899051)
//     */
//    public String getName() {
//	String ename = null;
//	ename = super.getName();
//	if (!ename.startsWith("__")) // NOI18N
//	    ename = getPrimaryFile().getNameExt();
//	return ename;
//    }
//
//    protected FileObject handleRename(String name) throws IOException {
//        FileLock lock = getPrimaryFile().lock();
//        int pos = name.lastIndexOf('.');
//
//        try {
//            if (pos <= 0){
//                // file without separator
//                getPrimaryFile().rename(lock, name, null);
//            } else {
//		getPrimaryFile().rename(lock, name.substring(0, pos), 
//                        name.substring(pos + 1, name.length()));
//            }
//        } finally {
//            lock.releaseLock ();
//        }
//        return getPrimaryFile ();
//    }
    /**
     *  The init method is called from CndDataObject's constructor.
     */
    @Override
    protected void init() {
        CookieSet cookies = getCookieSet();

        cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
        cookies.add(new ShellExecSupport(getPrimaryEntry()));
    }

    /** Create the delegate node */
    protected Node createNodeDelegate() {
        return new ShellDataNode(this);
    }
}
