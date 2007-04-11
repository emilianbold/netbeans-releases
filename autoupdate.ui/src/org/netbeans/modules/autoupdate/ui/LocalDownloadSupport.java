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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek
 */
public class LocalDownloadSupport {
    private static File defaultDir = null;
    private static final FileFilter NBM_FILE_FILTER = new NbmFileFilter();
    
    public static File [] selectNbmFiles () {
        JFileChooser chooser = new JFileChooser ();
        chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
        chooser.addChoosableFileFilter (NBM_FILE_FILTER);
        chooser.setFileFilter (NBM_FILE_FILTER);
        chooser.setMultiSelectionEnabled (true);
        chooser.setFileHidingEnabled (false);
        chooser.setDialogTitle (getBundle ("CTL_FileChooser_Title"));
        
        if (defaultDir == null)
            defaultDir = getDefaultDir ();

        if ( defaultDir != null) {
            chooser.setCurrentDirectory (defaultDir);
        }

        Window focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager ().getActiveWindow ();

        if (chooser.showOpenDialog (KeyboardFocusManager.getCurrentKeyboardFocusManager ().getActiveWindow ()) 
                /*, getBundle ("CTL_FileChooser_Approve_Button")*/
                != JFileChooser.APPROVE_OPTION) {
            
            return null;
        }

        defaultDir = chooser.getCurrentDirectory ();
        
        // #46353, recover focus after close the JFileChooser
        if (focusOwner != null) {
            focusOwner.toFront ();
        }

        return chooser.getSelectedFiles ();
    }
    
    public static List<UpdateUnit> getUpdateUnits () {
        File [] nbms = selectNbmFiles ();        
        List<UpdateUnit> retval = null;
        if (nbms != null) {
            retval = UpdateUnitProviderFactory.getDefault ().create ("local-downloaded", nbms).getUpdateUnits ();
        } else {
            retval = Collections.emptyList();
        }        
        return retval;
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
        return new File (System.getProperty ("netbeans.user"));
    }

    public static String getBundle (String key) {
        return NbBundle.getMessage (LocalDownloadSupport.class, key);
    }
    
}
