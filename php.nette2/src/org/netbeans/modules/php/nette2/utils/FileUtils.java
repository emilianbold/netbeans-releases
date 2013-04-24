/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.nette2.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class FileUtils {
    private static final Logger LOGGER = Logger.getLogger(FileUtils.class.getName());

    private FileUtils() {
    }

    public static void copyDirectory(File sourceDirectory, File destinationDirectory) {
        assert sourceDirectory != null;
        assert destinationDirectory != null;
        if (sourceDirectory.isDirectory()) {
            if (!destinationDirectory.exists()) {
                destinationDirectory.mkdir();
            }
            String[] children = sourceDirectory.list();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    copyDirectory(new File(sourceDirectory, children[i]), new File(destinationDirectory, children[i]));
                }
            }
        } else {
            try {
                copyFile(sourceDirectory, destinationDirectory);
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
        }
    }

    public static void copyFile(File source, File destination) throws IOException {
        assert source != null;
        assert destination != null;
        InputStream fis = new FileInputStream(source);
        OutputStream fos = new FileOutputStream(destination);
        try {
            byte[] buf = new byte[1024];
            int len;
            while ((len = fis.read(buf)) > 0) {
                fos.write(buf, 0, len);
            }
        } finally {
            fis.close();
            fos.close();
        }
    }

    public static FileObject getFile(JTextComponent textComponent) {
        assert textComponent != null;
        return NbEditorUtilities.getFileObject(textComponent.getDocument());
    }

    public static void chmod777Recursively(FileObject fileObject) {
        assert fileObject != null;
        File file = FileUtil.toFile(fileObject);
        if (file != null) {
            chmod777(file);
            Enumeration<? extends FileObject> allNestedChildren = fileObject.getChildren(true);
            while (allNestedChildren.hasMoreElements()) {
                File child = FileUtil.toFile(allNestedChildren.nextElement());
                if (child != null) {
                    chmod777(child);
                }
            }
        }
    }

    private static void chmod777(File file) {
        assert file != null;
        file.setExecutable(true, false);
        file.setReadable(true, false);
        file.setWritable(true, false);
    }

}
