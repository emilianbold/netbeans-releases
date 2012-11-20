/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

/*
 * Utils.java
 *
 * Created on April 12, 2004, 6:53 PM
 */
package org.netbeans.modules.mobility.project.ui.wizard;

import java.io.File;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.openide.util.NbBundle;

/**
 *
 * @author  David Kaspar
 */
public class Utils {
    
    public static final String IS_LIBRARY = "is_library"; // NOI18N
    public static final String IS_EMBEDDED = "is_embedded"; // NOI18N

    private Utils()
    {
        //To avoid instantiation
    }
    
    public static String browseFolder(final JComponent parent, final String oldValue, final String title) {
        return browseFilter(parent, oldValue, title, JFileChooser.DIRECTORIES_ONLY, new FileFilter() {
            public boolean accept(File f) {
                return f.exists()  &&  f.canRead()  &&  f.isDirectory();
            }
            public String getDescription() {
                return NbBundle.getMessage(Utils.class,"LBL_Utils_FolderFilter"); // NOI18N
            }
        });
    }
    
    public static String browseFilter(final JComponent parent, final String oldValue, final String title, final int fileSelection, final FileFilter filter) {
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(fileSelection);
        chooser.setFileFilter(filter);
        if (oldValue != null)
            chooser.setSelectedFile(new File(oldValue));
        chooser.setDialogTitle(title);
        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }
    
    public static File findSubFile(final File[] files, final String fileName) {
        final String lowerCaseFileName = fileName.toLowerCase();
        File aprox = null;
        if (files != null) for (int a = 0; a < files.length; a ++) {
            final File file = files[a];
            final String name = file.getName();
            if (fileName.equals(name))
                return file;
            if (lowerCaseFileName.equals(name.toLowerCase()))
                aprox = file;
        }
        return aprox;
    }
    
    public static File findAnyFile(final File[] files, String ext) {
        ext = ext.toLowerCase();
        if (files != null) for (int a = 0; a < files.length; a ++) {
            final File file = files[a];
            final String name = file.getName();
            final int i = name.lastIndexOf('.');
            if (i >= 0) {
                if (ext.equals(name.substring(i + 1).toLowerCase()))
                    return file;
            }
        }
        return null;
    }
    
}
