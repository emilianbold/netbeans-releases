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
