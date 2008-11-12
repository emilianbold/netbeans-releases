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

package org.netbeans.modules.cnd.discovery.wizard;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class FileChooser extends JFileChooser {
    private static File currectChooserFile = null;
    public FileChooser(String titleText, String buttonText, int mode, boolean multiSelection,
            FileFilter[] filters, String feed, boolean useParent) {
        super();
        setFileHidingEnabled(false);
        setFileSelectionMode(mode);
            setMultiSelectionEnabled(multiSelection);
        setDialogTitle(titleText); // NOI18N
        setApproveButtonText(buttonText); // NOI18N

        if (filters != null) {
            for (int i = 0; i < filters.length; i++) {
                addChoosableFileFilter(filters[i]);
            }
            setFileFilter(filters[0]);
        }

        String feedFilePath = feed;
        File feedFilePathFile = null;

        if (feedFilePath != null && feedFilePath.length() > 0) {
            feedFilePathFile = new File(feedFilePath);
            //try {
                //	feedFilePathFile = feedFilePathFile.getCanonicalFile();
            //}
            //catch (IOException e) {
            //}
        }

        if (feedFilePathFile != null && feedFilePathFile.exists()) {
            currectChooserFile = feedFilePathFile;
        }

        if (currectChooserFile == null && feedFilePathFile == null) {
            feedFilePathFile = new File(System.getProperty("user.home")); // NOI18N
        }
        
        if (currectChooserFile == null && feedFilePathFile.getParentFile().exists()) {
            currectChooserFile = feedFilePathFile.getParentFile();
            useParent = false;
        }


        // Set currect directory
        if (currectChooserFile != null) {
            if (useParent) {
                if (currectChooserFile != null && currectChooserFile.exists()) {
                    setSelectedFile(currectChooserFile);
                }
            }
            else {
                if (currectChooserFile != null && currectChooserFile.exists()) {
                    setCurrentDirectory(currectChooserFile);
                }
            }
        }
        else {
            String sd = System.getProperty("spro.pwd"); // NOI18N
            if (sd != null) {
                File sdFile = new File(sd);
                if (sdFile.exists()) {
                    setCurrentDirectory(sdFile);
                }
            }
        }
    }

    @Override
    public int showOpenDialog(Component parent) {
        int ret = super.showOpenDialog(parent);
        if (ret != CANCEL_OPTION) {
            if (getSelectedFile().exists()) {
                currectChooserFile = getSelectedFile();
            }
        }
        return ret;
    }

    public static File getCurrectChooserFile() {
    	return currectChooserFile;
    }
}
