/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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