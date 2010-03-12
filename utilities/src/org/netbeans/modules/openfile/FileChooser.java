/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2007 Sun
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

package org.netbeans.modules.openfile;

import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author  Jesse Glick
 * @author  Marian Petras
 */
class FileChooser extends JFileChooser {

    /** Creates a new instance of FileChooser */
    FileChooser() {
        setFileSelectionMode(JFileChooser.FILES_ONLY);
        setMultiSelectionEnabled(true);
        
        /* initialize file filters */
        FileFilter currentFilter = getFileFilter();
        addChoosableFileFilter(new Filter(
            new String[] {DefaultOpenFileImpl.JAVA_EXT},
            NbBundle.getMessage(FileChooser.class, "TXT_JavaFilter"))); //NOI18N
        addChoosableFileFilter(new Filter(
            new String[] {DefaultOpenFileImpl.TXT_EXT}, 
            NbBundle.getMessage(FileChooser.class, "TXT_TxtFilter")));  //NOI18N
        setFileFilter(currentFilter);
    }
    
    @Override
    public void approveSelection() {
        final File[] selectedFiles = getSelectedFiles();

        /* check the files: */
        List<String> errorMsgs = null;
        for (int i = 0; i < selectedFiles.length; i++) {
            String msgPatternRef = null;
            File file = selectedFiles[i];

            if (!file.exists()) {
                msgPatternRef = "MSG_FileDoesNotExist";                 //NOI18N
            } else if (file.isDirectory()) {
                msgPatternRef = "MSG_FileIsADirectory";                 //NOI18N
            } else if (!file.isFile()) {
                msgPatternRef = "MSG_FileIsNotPlainFile";               //NOI18N
            }
            if (msgPatternRef == null) {
                continue;
            }

            if (errorMsgs == null) {
                errorMsgs = new ArrayList<String>(selectedFiles.length - i);
            }
            errorMsgs.add(NbBundle.getMessage(FileChooser.class,
                                              msgPatternRef, file.getName()));
        }
        if (errorMsgs == null) {
            super.approveSelection();
        } else {
            JPanel panel = new JPanel(new GridLayout(errorMsgs.size(), 0,
                                                     0, 2));        //gaps
            for (String errMsg : errorMsgs) {
                panel.add(new JLabel(errMsg));
            }
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(
                            panel, NotifyDescriptor.WARNING_MESSAGE));
        }
    }

    /** File chooser filter that filters files by their names' suffixes. */
    private static class Filter extends FileFilter {
        
        /** suffixes accepted by this filter */
        private String[] extensions;
        
        /** localized description of this filter */
        private String description;
        
        
        /**
         * Creates a new filter that accepts files having specified suffixes.
         * The filter is case-insensitive.
         * <p>
         * The filter does not use file <em>extensions</em> but it just
         * tests whether the file name ends with the specified string.
         * So it is recommended to pass a file name extension including the
         * preceding dot rather than just the extension.
         *
         * @param  extensions  list of accepted suffixes
         * @param  description  name of the filter
         */
        public Filter(String[] extensions, String description) {
            
            this.extensions = new String[extensions.length];
            for (int i = 0; i < extensions.length; i++) {
                this.extensions[i] = extensions[i].toUpperCase();
            }
            this.description = description;
        }
        
        
        /**
         * @return  <code>true</code> if the file's name ends with one of the
         *          strings specified by the constructor or if the file
         *          is a directory, <code>false</code> otherwise
         */
        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            for (int i = 0; i < extensions.length; i++) {
                if (file.getName().toUpperCase().endsWith(extensions[i])) {
                    return true;
                }
            }
            
            return false;
        }
        
        /** */
        @Override
        public String getDescription() {
            return description;
        }
    } // End of Filter class.

}
