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
/*
 * ImportExportFileChooser.java
 *
 * Created on September 1, 2004, 2:37 PM
 */

package org.netbeans.modules.visualweb.ejb.ui;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.openide.util.NbBundle;

/**
 * A file chooser for choosing a file to import from or export to
 *
 * @author  cao
 */
public class ImportExportFileChooser {

    public static String defaultFilePath = System.getProperty("user.home") + File.separator + "exported_ejb_datasources.jar";

    private JFileChooser fileChooser = org.netbeans.modules.visualweb.extension.openide.awt.JFileChooser_RAVE.getJFileChooser();

    private Component parent;

    public ImportExportFileChooser( Component parent )
    {
        this.parent = parent;

        // Set current dir or default dir
        File curDir = null;
        File curSelection = new File( defaultFilePath );
        if (curSelection.exists()){
            if (curSelection.isDirectory()) {
                curDir = curSelection;
            } else {
                curDir = curSelection.getParentFile();
            }
        }

        if (curDir == null)
            curDir = new File(System.getProperty("user.home")); //NOI18N

        if (curSelection != null && curSelection.exists())
            fileChooser.setSelectedFile(curSelection);

        fileChooser.setCurrentDirectory(curDir);
        fileChooser.addChoosableFileFilter(new JarFilter());
    }

    public static void setCurrentFilePath( String path )
    {
        defaultFilePath = path;
    }

    // Returns the selected file
    public String getExportFile()
    {
        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION)
        {
            String selectedFile = fileChooser.getSelectedFile().getAbsolutePath();

            return selectedFile;
        }
        else
            return null;
    }

    // Returns the selected file
    public String getImportFile()
    {
        if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION)
        {
            String selectedFile = fileChooser.getSelectedFile().getAbsolutePath();

            return selectedFile;
        }
        else
            return null;
    }

     public class JarFilter extends FileFilter {

        //Accept all directories and all ".jar" files.
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String extension = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');

            if (i > 0 &&  i < s.length() - 1) {
                extension = s.substring(i+1).toLowerCase();
            }

            if (extension != null) {
                if (extension.equals("jar")) { //NOI18N
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        }

        //The description of this filter
        public String getDescription() {
            return NbBundle.getMessage(ExportEjbDataSourcesPanel.class, "JAR_FILE_FILTER_DESCRIPTION");
        }
    }
}
