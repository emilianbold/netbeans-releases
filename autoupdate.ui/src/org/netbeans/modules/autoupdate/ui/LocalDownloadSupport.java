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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.autoupdate.ui;

import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jiri Rechtacek
 */
public class LocalDownloadSupport {
    private static final FileFilter NBM_FILE_FILTER = new NbmFileFilter();
    private Set<File> selectedFiles = null;
    private Map<UpdateUnit, File> units2file = new HashMap<UpdateUnit, File> ();
    private static String LOCAL_DOWNLOAD_DIRECTORY_KEY = "local-download-directory"; // NOI18N
    private static String LOCAL_DOWNLOAD_FILES = "local-download-files"; // NOI18N    
    
    public LocalDownloadSupport() { 
        loadSelectedFiles();
    }
    public File [] selectNbmFiles () {
        JFileChooser chooser = new JFileChooser ();
        chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
        chooser.addChoosableFileFilter (NBM_FILE_FILTER);
        chooser.setFileFilter (NBM_FILE_FILTER);
        chooser.setMultiSelectionEnabled (true);
        chooser.setFileHidingEnabled (false);
        chooser.setDialogTitle (getBundle ("CTL_FileChooser_Title"));
        
        String filePath = getPreferences ().get (LOCAL_DOWNLOAD_DIRECTORY_KEY, null);
        File dir = null;
        if (filePath != null) {
            dir = new File (filePath);
            if (! dir.exists ()) {
                dir = getDefaultDir ();
            }
        }

        if ( dir != null) {
            chooser.setCurrentDirectory (dir);
        }

        Window focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager ().getActiveWindow ();

        if (chooser.showOpenDialog (KeyboardFocusManager.getCurrentKeyboardFocusManager ().getActiveWindow ()) 
                == JFileChooser.APPROVE_OPTION) {
            
            File [] newFiles = chooser.getSelectedFiles ();
            
            getSelectedFiles ().addAll (Arrays.asList (newFiles));
            storeSelectedFiles();
        }
            
        getPreferences ().put(LOCAL_DOWNLOAD_DIRECTORY_KEY, chooser.getCurrentDirectory ().getAbsolutePath ()); // NOI18N
        
        /*// #46353, recover focus after close the JFileChooser
        if (focusOwner != null) {
            focusOwner.toFront ();
        }*/
            
            
        return getSelectedFiles ().toArray (new File [0]);
        }
        
    public List<UpdateUnit> getUpdateUnits () {                
        List<UpdateUnit> retval = new ArrayList<UpdateUnit>();
        Set<File> files = getSelectedFiles();
        if (files != null && files.size() > 0) {
            File [] nbms = new File[files.size()];        
            files.toArray(nbms);
            for (File file : nbms) {
                String fileName = file.getName ();
                List<UpdateUnit> temp = UpdateUnitProviderFactory.getDefault ().
                        create (fileName, new File[] {file}).getUpdateUnits (UpdateManager.TYPE.MODULE);
                retval.addAll(temp);
                for (UpdateUnit updateUnit : temp) {
                    units2file.put(updateUnit, file);
                }
            }
        } else {
            retval = Collections.emptyList();
        }        
        return retval;
    }
    
    public List<UpdateUnit> remove(UpdateUnit unit) {
        File f = units2file.remove(unit);
        if (f != null && getSelectedFiles() != null) {
            getSelectedFiles().remove(f);
            storeSelectedFiles();
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
        return new File (System.getProperty ("netbeans.user")); // NOI18N
    }

    public static String getBundle (String key) {
        return NbBundle.getMessage (LocalDownloadSupport.class, key);
    }
    
    private Set<File> getSelectedFiles () {
        if (selectedFiles == null) {
            selectedFiles = new HashSet<File> ();            
        }
        return selectedFiles;
    }
    
    private void storeSelectedFiles() {
        StringBuffer sb = null;
        for (File file : getSelectedFiles()) {
            if (!file.exists()) continue;
            if (sb == null) {
                sb = new StringBuffer();
            } else {
                sb.append(",");//NOI18N
            }
            sb.append(file.getAbsolutePath());
        }
        if (sb != null) {
            getPreferences().put(LOCAL_DOWNLOAD_FILES, sb.toString()); // NOI18N
        }
    }
    
    private void loadSelectedFiles() {
        String files = getPreferences().get(LOCAL_DOWNLOAD_FILES, null); // NOI18N
        if (files != null) {
            String[] fileArray = files.split(",");
            if (fileArray != null && fileArray.length > 0) {
                for (String file : fileArray) {
                    File tmp = new File(file);
                    if (tmp.exists()) {
                        getSelectedFiles().add(tmp);
                    }
                }
            }
        }
        storeSelectedFiles();
    }
    
    private Preferences getPreferences () {
        return NbPreferences.forModule (LocalDownloadSupport.class);
    }
    
}
