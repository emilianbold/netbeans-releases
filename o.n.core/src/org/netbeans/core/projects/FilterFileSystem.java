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
 * The Original Software is Forte for Java, Community Edition. The Initial
 * Developer of the Original Software is Sun Microsystems, Inc. Portions
 * Copyright 1997-2006 Sun Microsystems, Inc. All Rights Reserved.
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

package org.netbeans.core.projects;

import org.openide.filesystems.*;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Set;
import org.openide.util.Exceptions;

/**
 *
 * @author  Petr Jiricka, Vita Stejskal
 * @version 1.0
 */
public final class FilterFileSystem extends MultiFileSystem {
	
    private final FileObject root;
    private final FileSystem del;

    public FilterFileSystem (FileObject root) throws FileStateInvalidException {
        super (new FileSystem [] { root.getFileSystem () });
        this.root = root;
        this.del = root.getFileSystem ();
        setSystemName();
        setPropagateMasks (true);
    }

    @SuppressWarnings("deprecation")
    private void setSystemName() {
        try {
            setSystemName(del.getSystemName() + " : " + root.getPath()); //NOI18N
        } catch (PropertyVetoException e) {
            // ther shouldn't be any listener vetoing setSystemName
            Exceptions.printStackTrace(e);
        }
    }

    public final FileObject getRootFileObject () {
        return root;
    }

    protected @Override FileObject findResourceOn(FileSystem fs, String res) {
        return fs.findResource (root.getPath() + "/" + res); //NOI18N
    }

    protected @Override Set<? extends FileSystem> createLocksOn(String name) throws IOException {
        String nn = root.getPath() + "/" + name;
        org.netbeans.core.startup.layers.LocalFileSystemEx.potentialLock (name, nn);
        return super.createLocksOn (name);
    }
}
