/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.openide.filesystems;

import java.lang.ref.WeakReference;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class RecursiveListener extends WeakReference<FileObject>
implements FileChangeListener {
    private final FileChangeListener fcl;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RecursiveListener other = (RecursiveListener) obj;
        if (this.fcl != other.fcl && (this.fcl == null || !this.fcl.equals(other.fcl))) {
            return false;
        }
        final FileObject otherFo = other.get();
        final FileObject thisFo = this.get();
        if (thisFo != otherFo && (thisFo == null || !thisFo.equals(otherFo))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final FileObject thisFo = this.get();
        int hash = 3;
        hash = 37 * hash + (this.fcl != null ? this.fcl.hashCode() : 0);
        hash = 13 * hash + (thisFo != null ? thisFo.hashCode() : 0);
        return hash;
    }

    public RecursiveListener(FileObject source, FileChangeListener fcl) {
        super(source);
        this.fcl = fcl;
    }

    public void fileRenamed(FileRenameEvent fe) {
        FileObject thisFo = this.get();
        if (thisFo != null && FileUtil.isParentOf(thisFo, fe.getFile())) {
            fcl.fileRenamed(fe);
        }
    }

    public void fileFolderCreated(FileEvent fe) {
        FileObject thisFo = this.get();
        if (thisFo != null && FileUtil.isParentOf(thisFo, fe.getFile())) {
            fcl.fileFolderCreated(fe);
        }
    }

    public void fileDeleted(FileEvent fe) {
        FileObject thisFo = this.get();
        if (thisFo != null && FileUtil.isParentOf(thisFo, fe.getFile())) {
            fcl.fileDeleted(fe);
        }
    }

    public void fileDataCreated(FileEvent fe) {
        FileObject thisFo = this.get();
        if (thisFo != null && FileUtil.isParentOf(thisFo, fe.getFile())) {
            fcl.fileDataCreated(fe);
        }
    }

    public void fileChanged(FileEvent fe) {
        FileObject thisFo = this.get();
        if (thisFo != null && FileUtil.isParentOf(thisFo, fe.getFile())) {
            fcl.fileChanged(fe);
        }
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
        FileObject thisFo = this.get();
        if (thisFo != null && FileUtil.isParentOf(thisFo, fe.getFile())) {
            fcl.fileAttributeChanged(fe);
        }
    }
}
