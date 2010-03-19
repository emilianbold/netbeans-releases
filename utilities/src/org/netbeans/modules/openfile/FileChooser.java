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
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

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
        addChoosableFileFilters();
        setFileFilter(currentFilter);
    }

    /**
     * Adds filters to the list of user choosable file filters.
     *
     * @see javax.swing.JFileChooser
     */
    public void addChoosableFileFilters() {
        for (OpenFileDialogFilter f :
                    Lookup.getDefault().lookupAll(OpenFileDialogFilter.class)) {
            addChoosableFileFilter(f);
        }
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


    @ServiceProvider(service=org.netbeans.modules.openfile.OpenFileDialogFilter.class)
    public static class JavaFilesFilter extends OpenFileDialogFilter {

        @Override
        public String getDescriptionString() {
            return NbBundle.getMessage(getClass(), "OFDFD_Java"); // NOI18N
        }

        @Override
        public String[] getSuffixes() {
            return new String[] {".java"};
        }

    }

    @ServiceProvider(service=OpenFileDialogFilter.class)
    public static class TxtFileFilter
            extends OpenFileDialogFilter.ExtensionFilter {

        @Override
        public FileNameExtensionFilter getFilter() {
            return new FileNameExtensionFilter(
                            NbBundle.getMessage(getClass(), "OFDFD_Txt"),
                            "txt"); // NOI18N
        }

    } // End of TxtFileFilter

}
