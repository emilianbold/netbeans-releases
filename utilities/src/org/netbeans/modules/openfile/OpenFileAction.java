/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.openfile;


import java.io.File;
import javax.swing.filechooser.FileFilter;
import javax.swing.JFileChooser;

import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/** 
 * Action which allows user open file from disk. It installed
 * in Menu | File | Open file... .
 *
 * @author Jesse Glick
 */
public class OpenFileAction extends CallableSystemAction {

    /** Generated serial version UID. */
    static final long serialVersionUID =-3424129228987962529L;
    
    /** Cache of last directory. */
    private static File currDir;

    
    /** Gets action name. ImMplements superclass abstract method. */
    public String getName() {
        return SettingsBeanInfo.getString ("LBL_openFile");
    }

    /** Gets action help context. Implements superclass abstract method. */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (OpenFileAction.class);
    }

    /** Gets action icon resource. Overrides superclass method. */
    protected String iconResource () {
        return "/org/netbeans/modules/openfile/openFile.gif"; // NOI18N
    }

    /** Actually perfoms action. Implements superclass abstract method. */
    public void performAction() {
        JFileChooser chooser = new JFileChooser();
        HelpCtx.setHelpIDString(chooser, getHelpCtx().getHelpID());
        
        FileFilter currentFilter = chooser.getFileFilter();
        
        chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
        
        chooser.setMultiSelectionEnabled(true);
        
        chooser.addChoosableFileFilter(new Filter(
            new String[] {OpenFile.JAVA_EXT},
            NbBundle.getBundle(getClass()).getString("TXT_JavaFilter")));
        chooser.addChoosableFileFilter(new Filter(
            new String[] {OpenFile.TXT_EXT}, 
            NbBundle.getBundle(getClass()).getString("TXT_TxtFilter")));
        
        chooser.setFileFilter(currentFilter);
        
        if(currDir != null) 
            chooser.setCurrentDirectory (currDir);
        
        File[] files = null;
        
        while(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            files = chooser.getSelectedFiles ();

            if (files.length == 0) {
                // In jdk.1.2 is in fact not supported multi selection -> bug.
                // Try to get the fisrt file and open.
                File selected = chooser.getSelectedFile();
                
                if(selected != null) {
                    files = new File[] {selected};
                } else {
                    // Selected file doesn't exist.
                    TopManager.getDefault().notify(new NotifyDescriptor.Message(
                        SettingsBeanInfo.getString("MSG_noFileSelected"),NotifyDescriptor.WARNING_MESSAGE));

                    continue;
                }
            }
            
            for(int i = 0; i < files.length; i++)
                OpenFile.open (files[i], false, null, 0, -1);
            
            break;
        }

        currDir = chooser.getCurrentDirectory ();
    }
    

    /** Filter for file chooser. */
    private static class Filter extends FileFilter {
        
        /** Extensions accepted by this filter. */
        private String[] extensions;
        
        /** Localized description of this filter. */
        private String description;
        
        
        /** Constructor. */
        public Filter(String[] extensions, String description) {
            this.extensions = extensions;
            this.description = description;
        }
        
        
        /** Accepts file or not. 
         * @return true if file is accepted by this filter. */
        public boolean accept(File file) {
            if(file.isDirectory())
                return true;
            
            for(int i = 0; i < extensions.length; i++) {
                if(file.getName().toUpperCase().endsWith(extensions[i]))
                    return true;
            }
            
            return false;
        }
        
        /** Gets filter description. Implements <code>FileFilter</code> interface method. */
        public String getDescription() {
            return description;
        }
    } // End of Filter class.

}
