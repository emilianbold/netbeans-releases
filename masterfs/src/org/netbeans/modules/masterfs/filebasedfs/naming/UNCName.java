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