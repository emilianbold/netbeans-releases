/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Map;
import java.text.DateFormat;
import org.netbeans.modules.cnd.editor.filecreation.CCFSrcFileIterator;
import org.openide.filesystems.FileObject;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.FileEntry;
import org.openide.loaders.UniFileLoader;
import org.openide.modules.InstalledFileLocator;
import org.netbeans.modules.cnd.settings.CppSettings;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 * DataLoader for recognising C/C++/Fortran (C-C-F) source files.
 *
 * It also defines an innerclass, CndFormat, whose derived classes are
 * used to format template files (e.g. substitute values for parameters such as
 * __FILENAME__, __NAME__, __DATE__, __TIME__, __USER__, __GUARD_NAME etc.).
 */
public abstract class CndAbstractDataLoader extends UniFileLoader {

    /** Serial version number */
    static final long serialVersionUID = 6801389470714975682L;

    protected CndAbstractDataLoader(String representationClassName) {
        super(representationClassName);
    }

    protected final void createExtentions(String[] extensions) {
        ExtensionList extensionList = new ExtensionList();
        for (int i = 0; i < extensions.length; i++) {
            extensionList.addExtension(extensions[i]);
        }
        setExtensions(extensionList);
    }

    protected abstract String getMimeType();

    @Override
    protected void initialize() {
        super.initialize();
//        getExtensions().addMimeType(getMimeType());
    }

    @Override
    protected String actionsContext() {
        return "Loaders/text/x-cnd-sourcefile/Actions/"; // NOI18N
    }

    @Override
    protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
        // Entry for the important file: by default, is preserved
        // during all operations.
        return new CndFormat(obj, primaryFile);
    }

// Inner class: Substitute important template parameters...
    /*package*/static class CndFormat extends FileEntry.Format {

        public CndFormat(MultiDataObject obj, FileObject primaryFile) {
            super(obj, primaryFile);
        }

        @Override
        public FileObject createFromTemplate(FileObject f, String name) throws IOException {
            FileObject fo;
            if (CCFSrcFileIterator.NEW_EXTENSION) {
                fo = createFromTemplate2(f, name);
            } else {
                fo = super.createFromTemplate(f, name);
            }
                  
            if (fo.getAttribute(TemplateExtensionUtils.NAME_ATTRIBUTE) != null) {
                fo.setAttribute(TemplateExtensionUtils.NAME_ATTRIBUTE, null);
            }
            return fo;
        }

        public FileObject createFromTemplate2 (FileObject f, String name) throws IOException {
            // we don't want extension to be taken from template filename
            String ext = FileUtil.getExtension(name);
            assert ext.length() > 0;
            name = name.substring(0, name.length() - ext.length() - 1);

            FileObject fo = f.createData (name, ext);

            java.text.Format frm = createFormat (f, name, ext);

            BufferedReader r = new BufferedReader (new InputStreamReader (getFile ().getInputStream ()));
            try {
                FileLock lock = fo.lock ();
                try {
                    BufferedWriter w = new BufferedWriter (new OutputStreamWriter (fo.getOutputStream (lock)));

                    try {
                        String current;
                        while ((current = r.readLine ()) != null) {
                            w.write (frm.format (current));
                            // Cf. #7061.
                            w.newLine ();
                        }
                    } finally {
                        w.close ();
                    }
                } finally {
                    lock.releaseLock ();
                }
            } finally {
                r.close ();
            }

            // copy attributes
            FileUtil.copyAttributes (getFile (), fo);

            // unmark template state
            setTemplate (fo, false);

            return fo;
        }
        
        private static boolean setTemplate (FileObject fo, boolean newTempl) throws IOException {
            boolean oldTempl = false;

            Object o = fo.getAttribute(DataObject.PROP_TEMPLATE);
            if ((o instanceof Boolean) && ((Boolean)o).booleanValue())
                oldTempl = true;
            if (oldTempl == newTempl)
                return false;

            fo.setAttribute(DataObject.PROP_TEMPLATE, (newTempl ? Boolean.TRUE : null));

            return true;
        }

        protected java.text.Format createFormat(FileObject target, String name, String ext) {

            Map map = (CppSettings.findObject(CppSettings.class, true)).getReplaceableStringsProps();

            String packageName = target.getPath().replace('/', '_');
            // add an underscore to the package name if it is not an empty string
            if (!packageName.equals("")) { // NOI18N                
                packageName = packageName + "_"; // NOI18N
            }
            map.put("PACKAGE_AND_NAME", packageName + name); // NOI18N
            map.put("NAME", name); // NOI18N
            map.put("EXTENSION", ext); // NOI18N
            String guardName = name.replace('-', '_').replace('.', '_'); // NOI18N
            map.put("GUARD_NAME", guardName.toUpperCase()); // NOI18N
            /*
            This is a ugly hack but I don't have a choice. That's because
            NetBeans will not pass me the name the user typed in as the
            "root" name; instead I get the substituted name for each
            template file. In other words, suppose I use the parameter
            __NAME__ in my template source files. The name passed to
            createFormat is then the *filename* instead of just the
            Name: field the user had entered. e.g. if I'm instantiating the
            following files:
            __sample___foo.cc
            __sample___bar.cc
            Then for the first file, __NAME__ becomes <myname>_foo and in
            the second file, __NAME__ becomes <myname>_bar. But I really
            need the Name itself, so that I can for example have have
            #include "<myname>_externs.h"
            in the templates!
             */

            int crop = name.lastIndexOf('_');
            if (crop != -1) {
                name = name.substring(0, crop);
            }
	    map.put("CROPPEDNAME", name);  // NOI18N
	    map.put("DATE", DateFormat.getDateInstance	// NOI18N
		     (DateFormat.LONG).format(new Date()));
	    map.put("TIME", DateFormat.getTimeInstance	// NOI18N
		     (DateFormat.SHORT).format(new Date()));
            //	    map.put("USER", System.getProperty("user.name"));	// NOI18N
            String nbHome = null; //System.getProperty("netbeans.home");
            File file = InstalledFileLocator.getDefault().locate("lib", null, false); // NOI18N
            if (file != null) {
                nbHome = file.getParent();
            }
            if (nbHome == null) {
                nbHome = "";
            }
            map.put("NBDIR", nbHome); // NOI18N
            map.put("QUOTES", "\""); // NOI18N
            org.openide.util.MapFormat format = new org.openide.util.MapFormat(map);

            // Use "%<%" and "%>%" instead of "__" (which most other templates
            // use) since "__" is used for many C++ tokens and we don't want
            // any conflicts with valid code. For example, __FILE__ is a valid
            // construct in Sun C++ files and the compiler will replace the
            // current file name during compilation.
            format.setLeftBrace("%<%"); // NOI18N
            format.setRightBrace("%>%"); // NOI18N
            return format;
        }
    }
}