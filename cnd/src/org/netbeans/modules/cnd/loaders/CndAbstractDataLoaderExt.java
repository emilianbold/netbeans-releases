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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.loaders;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import org.netbeans.modules.cnd.editor.filecreation.CndHandlableExtensions;
import org.netbeans.modules.cnd.editor.filecreation.ExtensionsSettings;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;

/**
 *
 * @author Sergey Grinev
 */
public abstract class CndAbstractDataLoaderExt extends CndAbstractDataLoader 
        implements CndHandlableExtensions {

    protected CndAbstractDataLoaderExt(String representationClassName) {
        super(representationClassName);
    }

    @Override
    public ExtensionList getExtensions() {
        return ExtensionsSettings.getInstance(this).getExtensionList();
    }

    @Override
    public void setExtensions(ExtensionList ext) {
        ExtensionsSettings.getInstance(this).setExtensionList(ext);
    }

    protected static ExtensionList arrayToExtensionList(String[] ar) {
        ExtensionList l = new ExtensionList();
        for (int i = 0; i < ar.length; i++) {
            l.addExtension(ar[i]);
        }
        return l;
    }

    public String getSettingsName() {
        return getRepresentationClassName();
    }

    @Override
    protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
        return new CndFormatExt(obj, primaryFile);
    }

    private static class CndFormatExt extends CndFormat {

        public CndFormatExt(MultiDataObject obj, FileObject primaryFile) {
            super(obj, primaryFile);
        }

        @Override
        public FileObject createFromTemplate(FileObject f, String name) throws IOException {
            // we don't want extension to be taken from template filename
            String ext = FileUtil.getExtension(name);
            assert ext.length() > 0;
            name = name.substring(0, name.length() - ext.length() - 1);

            FileObject fo = f.createData(name, ext);

            java.text.Format frm = createFormat(f, name, ext);

            BufferedReader r = new BufferedReader(new InputStreamReader(getFile().getInputStream()));
            try {
                FileLock lock = fo.lock();
                try {
                    BufferedWriter w = new BufferedWriter(new OutputStreamWriter(fo.getOutputStream(lock)));

                    try {
                        String current;
                        while ((current = r.readLine()) != null) {
                            w.write(frm.format(current));
                            // Cf. #7061.
                            w.newLine();
                        }
                    } finally {
                        w.close();
                    }
                } finally {
                    lock.releaseLock();
                }
            } finally {
                r.close();
            }

            // copy attributes
            FileUtil.copyAttributes(getFile(), fo);

            // unmark template state
            setTemplate(fo, false);

            return fo;
        }

        private static boolean setTemplate(FileObject fo, boolean newTempl) throws IOException {
            boolean oldTempl = false;

            Object o = fo.getAttribute(DataObject.PROP_TEMPLATE);
            if ((o instanceof Boolean) && ((Boolean) o).booleanValue()) {
                oldTempl = true;
            }
            if (oldTempl == newTempl) {
                return false;
            }

            fo.setAttribute(DataObject.PROP_TEMPLATE, (newTempl ? Boolean.TRUE : null));

            return true;
        }
    }
}
