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

import java.io.IOException;

import org.netbeans.modules.cnd.execution41.org.openide.cookies.ExecCookie;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.nodes.Node;
import org.openide.nodes.CookieSet;
import org.openide.filesystems.FileLock;

import org.netbeans.modules.cnd.execution.BinaryExecSupport;

/** Superclass for Elf objects in the Repository.
 *
 */
public class CoreElfObject extends ExeObject {

    /** Serial version number */
    static final long serialVersionUID = 4165108744340374591L;

    public CoreElfObject(FileObject pf, ExeLoader loader)
            throws DataObjectExistsException {
        super(pf, loader);
    }

    @Override
    protected void init() {
        CookieSet cookies = getCookieSet();

        // Actually, we don't want Execute, we only want Start!
        // See below; we override getCookie to disable execution.
        cookies.add(new BinaryExecSupport(getPrimaryEntry()));
    }

    @Override
    protected Node createNodeDelegate() {
        return new CoreElfNode(this);
    }

    /** Implement parent's getCookie, except disable execution.
    <p>
    ExecSupport includes both execution and debugging. We don't want
    that.  From http://www.netbeans.org/www-nbdev/msg07823.html: A
    workaround would be to override getCookie on your DataObject to
    check for DebuggerCookie.class, return null if so, else return
    super.getCookie.
     */
    @Override
    public Node.Cookie getCookie(Class c) {
        if (c.isAssignableFrom(ExecCookie.class)) {
            return null;
        } else {
            return super.getCookie(c);
        }
    }

    /*
     * Return name with extension so renaming etc works
     */
    @Override
    public String getName() {
        String ename = getPrimaryFile().getNameExt();
        return ename;
    }

    /**
     *  Renames all entries and changes their files to new ones.
     *  We only override this to prevent you from changing the template
     *  name to something invalid (like an empty name)
     */
    @Override
    protected FileObject handleRename(String name) throws IOException {
        FileLock lock = getPrimaryFile().lock();
        int pos = name.lastIndexOf('.');

        try {
            if (pos <= 0) {
                // file without separator
                getPrimaryFile().rename(lock, name, null);
            } else {
                getPrimaryFile().rename(lock, name.substring(0, pos),
                        name.substring(pos + 1, name.length()));
            }
        } finally {
            lock.releaseLock();
        }
        return getPrimaryFile();
    }
}

