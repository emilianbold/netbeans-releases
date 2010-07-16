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
package org.netbeans.modules.cnd.utils.ui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.openide.util.NbPreferences;

public class FileChooser extends JFileChooser {

    private static File currectChooserFile = null;

    public FileChooser(String titleText, String buttonText, int mode, FileFilter[] filters, String feedFilePath, boolean useParent) {
        super();
        setFileHidingEnabled(false);
        setFileSelectionMode(mode);
        setDialogTitle(titleText);
        if (buttonText != null) {
            setApproveButtonText(buttonText);
        }
        if (filters != null) {
            for (int i = 0; i < filters.length; i++) {
                addChoosableFileFilter(filters[i]);
            }
            setFileFilter(filters[0]);
        }
        //System.out.println("A - currectChooserFile " + currectChooserFile);

        File feedFilePathFile = null;
        if (feedFilePath != null && feedFilePath.length() > 0) {
            feedFilePathFile = new File(feedFilePath);
            try {
                feedFilePathFile = feedFilePathFile.getCanonicalFile();
            } catch (IOException e) {
            }
        }

        if (feedFilePathFile != null && feedFilePathFile.exists()) {
            FileChooser.setCurrectChooserFile(feedFilePathFile);
        }

        if (FileChooser.getCurrectChooserFile() == null && feedFilePathFile == null) {
            feedFilePathFile = new File(getLastPath());
        }
        if (FileChooser.getCurrectChooserFile() == null && feedFilePathFile.getParentFile() != null && feedFilePathFile.getParentFile().exists()) {
            FileChooser.setCurrectChooserFile(feedFilePathFile.getParentFile());
            useParent = false;
        }


        // Set currect directory
        if (FileChooser.getCurrectChooserFile() != null) {
            if (useParent) {
                if (FileChooser.getCurrectChooserFile() != null && FileChooser.getCurrectChooserFile().exists()) {
                    setSelectedFile(FileChooser.getCurrectChooserFile());
                }
            } else {
                if (FileChooser.getCurrectChooserFile() != null && FileChooser.getCurrectChooserFile().exists()) {
                    setCurrentDirectory(FileChooser.getCurrectChooserFile());
                }
            }
        } else {
            String sd = System.getProperty("spro.pwd"); // NOI18N
            if (sd != null) {
                File sdFile = new File(sd);
                if (sdFile.exists()) {
                    setCurrentDirectory(sdFile);
                }
            }
        }
    }

    private String getLastPath(){
        String feed = System.getProperty("user.home"); // NOI18N
        if (feed == null) {
            feed = ""; // NOI18N
        }
        Preferences pref = NbPreferences.forModule(FileChooser.class);
        String res = pref.get("last-file", feed); // NOI18N
        File file = new File(res);
        if (!file.exists()) {
            if (!res.equals(feed)){
                res = feed;
            }
        }
        return res;
    }


    @Override
    public int showOpenDialog(Component parent) {
        int ret = super.showOpenDialog(parent);
        if (ret != CANCEL_OPTION) {
            if (getSelectedFile().exists()) {
                setCurrectChooserFile(getSelectedFile());
                Preferences pref = NbPreferences.forModule(FileChooser.class);
                pref.put("last-file", getCurrectChooserFile().getAbsolutePath()); // NOI18N
            }
        }
        return ret;
    }

    public static File getCurrectChooserFile() {
        return currectChooserFile;
    }

    private static void setCurrectChooserFile(File aCurrectChooserFile) {
        currectChooserFile = aCurrectChooserFile;
    }
}
