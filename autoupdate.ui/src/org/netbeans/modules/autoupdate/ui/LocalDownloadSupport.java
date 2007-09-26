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

package org.netbeans.modules.autoupdate.ui;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jiri Rechtacek, Radek Matous
 */
public class LocalDownloadSupport {
    private static final FileFilter NBM_FILE_FILTER = new NbmFileFilter();
    private Map<String, File> units2file = new HashMap<String, File> ();
    private static String LOCAL_DOWNLOAD_DIRECTORY_KEY = "local-download-directory"; // NOI18N
    private static String LOCAL_DOWNLOAD_FILES = "local-download-files"; // NOI18N    
    private FileList fileList = new FileList();
    private List<UpdateUnit> updateUnits;
    private Logger err = Logger.getLogger (LocalDownloadSupport.class.getName ());

    public LocalDownloadSupport() {}
    
    public boolean selectNbmFiles () {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.addChoosableFileFilter(NBM_FILE_FILTER);
        chooser.setFileFilter(NBM_FILE_FILTER);
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileHidingEnabled(false);
        chooser.setDialogTitle(getBundle("CTL_FileChooser_Title"));
        
        File dir = getDefaultDir();
        if ( dir != null && dir.exists()) {
            chooser.setCurrentDirectory(dir);
        }
        
        Component parent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            synchronized(LocalDownloadSupport.class) {
                updateUnits = null;
            }
            getPreferences ().put(LOCAL_DOWNLOAD_DIRECTORY_KEY, chooser.getCurrentDirectory ().getAbsolutePath ()); // NOI18N 
            fileList.addFiles(chooser.getSelectedFiles());
            return true;
        }
        return false;
    }
        
    public List<UpdateUnit> getUpdateUnits () {
        boolean isNull = true;
        synchronized(LocalDownloadSupport.class) {
            isNull = (updateUnits == null);
        }
        
        if (isNull) {
            synchronized(LocalDownloadSupport.class) {
                updateUnits = new ArrayList<UpdateUnit>();
            }
            Set<File> files = fileList.getSelectedFiles();
            File [] nbms = files.toArray(new File[files.size()]);        
            UpdateUnitProviderFactory factory = UpdateUnitProviderFactory.getDefault();
            for (File file : nbms) {
                UpdateUnitProvider provider = factory.create(file.getName(), new File[] {file});
                List<UpdateUnit> units = Collections.emptyList ();
                try {
                    units = provider.getUpdateUnits (UpdateManager.TYPE.MODULE);
                } catch (RuntimeException re) {
                    err.log (Level.WARNING, re.getMessage (), re);
                    DialogDisplayer.getDefault().notifyLater (new NotifyDescriptor.Exception (re, getBundle ("LocalDownloadSupport_BrokenNBM_Exception", file.getName ())));
                    fileList.removeFile (file);
                }
                synchronized(LocalDownloadSupport.class) {                
                    updateUnits.addAll(units);
                }
                for (UpdateUnit updateUnit : units) {
                    units2file.put(updateUnit.getCodeName(), file);
                }
            }
        }
        return updateUnits;
    }
    
    public List<UpdateUnit> remove(UpdateUnit unit) {
        File f = units2file.remove(unit.getCodeName());
        if (f != null) {
            fileList.removeFile(f);
            synchronized(LocalDownloadSupport.class) {
                updateUnits = null;
            }            
        }
        return getUpdateUnits ();
    }
    
    private static class NbmFileFilter extends FileFilter {
        public boolean accept (File f ) {
            return f.isDirectory() || f.getName ().toLowerCase ().endsWith (".nbm"); // NOI18N
        }

        public String getDescription () {
            return getBundle ("CTL_FileFilterDescription");
        }
    }
    
    private static File getDefaultDir () {
        File retval = new File(getPreferences ().get (LOCAL_DOWNLOAD_DIRECTORY_KEY, System.getProperty ("netbeans.user")));// NOI18N
        return (retval.exists()) ? retval : new File(System.getProperty ("netbeans.user")); // NOI18N
    }

    public static String getBundle (String key, String... params) {
        return NbBundle.getMessage (LocalDownloadSupport.class, key, params);
    }
    
    private static Preferences getPreferences() {
        return NbPreferences.forModule(LocalDownloadSupport.class);
    }
    
    private static class FileList {
        private Set<File> selectedFiles = null;        
        
        Set<File> getSelectedFiles() {
            if (selectedFiles == null) {
                selectedFiles = new LinkedHashSet<File> ();
                addFiles(loadPresistentState());
            }            
            return selectedFiles;
        }

        void addFiles(File[] files) {
            addFiles(Arrays.asList(files));
        }
        
        void addFiles(Collection<File> files) {
            getSelectedFiles().addAll(files);
            selectedFiles = stripNoNBMs (stripNotExistingFiles(getSelectedFiles ()));
            makePersistent(selectedFiles);
        }

        void removeFile(File file) {
            removeFiles(Collections.singleton(file));
        }
        
        void removeFiles(Collection<File> files) {
            getSelectedFiles().removeAll(files);
            selectedFiles = stripNoNBMs (stripNotExistingFiles(getSelectedFiles ()));
            makePersistent(selectedFiles);
        }
        
        
        private static Set<File> loadPresistentState() {
            Set<File> retval = new HashSet<File>();
            String files = getPreferences().get(LOCAL_DOWNLOAD_FILES, null); // NOI18N
            if (files != null) {
                String[] fileArray = files.split(",");                
                for (String file : fileArray) {
                    retval.add(new File(file));
                }                
            }
            return retval;            
        }
        
        private static void makePersistent(Set<File> files) {
            StringBuilder sb = null;
            if (!files.isEmpty()) {
                for (File file : files) {
                    if (sb == null) {
                        sb = new StringBuilder(file.getAbsolutePath());
                    } else {
                        sb.append(',').append(file.getAbsolutePath());
                    }
                }
            } 
            if (sb == null) {
                getPreferences().remove(LOCAL_DOWNLOAD_FILES);
            } else {
                getPreferences().put(LOCAL_DOWNLOAD_FILES, sb.toString());
            }
        }
        
        private static Set<File> stripNotExistingFiles(Set<File> files) {
            Set<File> retval = new HashSet<File>();
            for (File file : files) {
                if (file.exists()) {
                    retval.add(file);
                }
            }
            return retval;
        }
        
        private static Set<File> stripNoNBMs (Set<File> files) {
            Set<File> retval = new HashSet<File> ();
            for (File file : files) {
                if (NBM_FILE_FILTER.accept (file)) {
                    retval.add(file);
                }
            }
            return retval;
        }                
    }        
}
