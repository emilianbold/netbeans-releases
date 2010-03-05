/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.filesystems.FileLock;



/** Superclass for Elf objects in the Repository.
 *
 */
public class ExeElfObject extends ExeObject {

    /** Serial version number */
    static final long serialVersionUID = 77972208704093735L;

    public ExeElfObject(FileObject pf, ExeLoader loader)
	throws DataObjectExistsException {
	super(pf, loader);
    }
  
    @Override
    protected Node createNodeDelegate() {
	return new ExeNode(this);
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
            if (pos <= 0){
                // file without separator
                getPrimaryFile().rename(lock, name, null);
            } else {
		getPrimaryFile().rename(lock, name.substring(0, pos), 
                        name.substring(pos + 1, name.length()));
            }
        } finally {
            lock.releaseLock ();
        }
        return getPrimaryFile ();
    }

    @Override
    protected DataObject handleCopy(DataFolder df) throws IOException {
	// let super do the job and then set the execution flag on the copy
	DataObject dao = super.handleCopy(df);
	setExecutionFlags(dao.getPrimaryFile());
	return dao;
    }

    @Override
    protected FileObject handleMove(DataFolder df) throws IOException {
	// let super do the job and then set the execution flag on the copy
	FileObject fob = super.handleMove(df);
	setExecutionFlags(fob);
	return fob;
    }

    private void setExecutionFlags(FileObject fob) throws IOException {
	if (fob != null) {
            Runtime.getRuntime().exec("/bin/chmod +x " + FileUtil.toFile(fob).getPath()); // NOI18N
        }
    }
}

