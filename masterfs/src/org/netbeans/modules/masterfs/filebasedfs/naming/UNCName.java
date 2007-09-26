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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.masterfs.filebasedfs.naming;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * @author Radek Matous
 */
public final class UNCName extends FolderName {
    UNCName(final FileNaming parent, final File f) {
        super(parent, f);
    }

    public final File getFile() {
        File file = super.getFile();
        file = (file instanceof UNCFile) ? file : new UNCFile(file);
        return file;
    }

    public final FileNaming getParent() {
        final FileNaming parent = super.getParent();
        return parent;
    }

    public static final class UNCFile extends File {
        private UNCFile(final File file) {
            super(file.getAbsolutePath());
        }

        public final boolean isDirectory() {
            return true;
        }

        public final File getParentFile() {
            return wrap(super.getParentFile());
        }

        public final File getAbsoluteFile() {
            return wrap(super.getAbsoluteFile());
        }

        public final File getCanonicalFile() throws IOException {
            return wrap(super.getCanonicalFile());
        }


        public final File[] listFiles() {
            return wrap(super.listFiles());
        }


        public final File[] listFiles(final FilenameFilter filter) {
            return wrap(super.listFiles(filter));
        }

        public final File[] listFiles(final FileFilter filter) {
            return wrap(super.listFiles(filter));
        }

        private File[] wrap(final File[] files) {
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    final File file = files[i];
                    files[i] = (file != null) ? new UNCFile(file) : null;
                    ;
                }
            }
            return files;
        }

        private File wrap(final File file) {
            return (file != null) ? new UNCFile(file) : null;
        }

        public final boolean exists() {
            return true;
        }

    }
}