/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * A FileChooser that can remember the last directory that was
 * used for reading/writing a file. The last directory is shown as
 * the default.
 * To make it remember, call update after the constructor has returned. e.eg.
 * s = new StickyFileChooser (...);
 * s.rememberDirectory();
 *
 */
package org.netbeans.modules.collab.ui;

import com.sun.collablet.CollabException;

import org.openide.util.*;

import java.awt.*;

import java.io.*;

import java.util.*;

import javax.swing.*;

import org.netbeans.modules.collab.*;


class StickyFileChooserFilter extends javax.swing.filechooser.FileFilter {
    private HashSet extensions = null;
    private String fileType = null;

    StickyFileChooserFilter(String fileExtensions, String fileType) {
        if (fileExtensions != null) {
            extensions = new HashSet();

            for (StringTokenizer st = new StringTokenizer(fileExtensions, ";"); st.hasMoreTokens();) {
                String ext = st.nextToken();

                while (ext.startsWith("*"))
                    ext = ext.substring(1);

                if (!ext.startsWith(".")) {
                    ext = "." + ext;
                }

                extensions.add(ext);
            }
        }

        this.fileType = fileType;
    }

    public boolean accept(File f) {
        if ((extensions == null) || (extensions.size() == 0)) {
            return true;
        }

        if (f.isDirectory()) {
            return true;
        }

        int i = f.getName().lastIndexOf(".");

        if (i > 0) {
            String ext = f.getName().substring(i);

            return extensions.contains(ext);
        } else {
            return false;
        }
    }

    public String getDescription() {
        return fileType;
    }
}


/*
 *
 */
public class StickyFileChooser {
    //   private static com.iplanet.im.util.SafeResourceBundle _bundle = new com.iplanet.im.util.SafeResourceBundle("com.iplanet.im.swing.swing");
    private static File currentDirectory = null;

    /**
     * pops a save-file dialog and saves the file.
     * @param parent parent component, if any
     * @param filename name of file to choose from
     * @return The saved File.
     */
    static public File chooseSaveFile(Component parent, String defaultFileName, String fileExtension, String fileType) {
        int rc = JFileChooser.ERROR_OPTION;

        String ext;

        if ((fileExtension != null) && !fileExtension.startsWith(".")) {
            ext = "." + fileExtension;
        } else {
            ext = fileExtension;
        }

        JFileChooser fc = new JFileChooser(currentDirectory);

        if (fileExtension != null) {
            StickyFileChooserFilter filter = new StickyFileChooserFilter(fileExtension, fileType);
            fc.setFileFilter(filter);
        }

        if (defaultFileName != null) {
            fc.setSelectedFile(new File(currentDirectory, defaultFileName));
        }

        try {
            rc = fc.showSaveDialog(parent);
        } catch (Exception e) {
        }

        switch (rc) {
        case JFileChooser.APPROVE_OPTION:

            File f = fc.getSelectedFile();

            // check for overwrite
            if ((parent != null) && f.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(
                        parent, NbBundle.getMessage(StickyFileChooser.class, "FileDialog_fileExist_question"),
                        NbBundle.getMessage(StickyFileChooser.class, "FileDialog_fileExist_title"),
                        JOptionPane.YES_NO_OPTION
                    );

                if (overwrite == JOptionPane.NO_OPTION) {
                    f = null;
                }
            }

            currentDirectory = fc.getCurrentDirectory();

            // fix the filename extension if necessary
            if (ext != null) {
                if (!f.getName().endsWith(ext)) {
                    f = new File(f.getParentFile(), f.getName() + ext);
                }
            }

            return f;

        case JFileChooser.ERROR_OPTION:
        case JFileChooser.CANCEL_OPTION:default:
            return null;
        }
    }

    /**
     * pops a custom choose-file dialog
     * @param parent parent component, if any
     * @param fileExtensions file name extension (.gif, .txt)
     * @param fileType descriptive file type
     * @param dialogTitle dialog title to use, optional
     * @param approveButtonLabel approve button label to use, optional
     * @param approveButtonMnemonic dialog title to use, optional
     * @return The chosen File.
     */
    static public File chooseFile(
        Component parent, String fileExtensions, String fileType, String dialogTitle, String approveButtonLabel,
        String approveButtonMnemonic
    ) throws CollabException {
        int rc = JFileChooser.ERROR_OPTION;

        JFileChooser fc = new JFileChooser(currentDirectory);

        if (fileExtensions != null) {
            StickyFileChooserFilter filter = new StickyFileChooserFilter(fileExtensions, fileType);
            fc.setFileFilter(filter);
        }

        //try {
        if (approveButtonLabel != null) {
            if (dialogTitle != null) {
                fc.setDialogTitle(dialogTitle);
            }

            if (approveButtonLabel != null) {
                fc.setApproveButtonText(approveButtonLabel);
            }

            if (approveButtonMnemonic != null) {
                fc.setApproveButtonMnemonic(approveButtonMnemonic.charAt(0));
            }

            rc = fc.showDialog(parent, approveButtonLabel);
        } else {
            rc = fc.showOpenDialog(parent);
        }

        //  } catch(Exception e) {
        //}
        switch (rc) {
        case JFileChooser.APPROVE_OPTION:
            currentDirectory = fc.getCurrentDirectory();

            return fc.getSelectedFile();

        case JFileChooser.ERROR_OPTION:
            throw new CollabException("");

        case JFileChooser.CANCEL_OPTION:default:
            return null;
        }
    }

    /**
     * pops a save-file dialog and saves the file.
     * @param parent parent component, if any
     * @param fileExtensions accepted file name extension list (.gif;.txt)
     * @param fileType descriptive file type
     * @return The opened File.
     */
    static public File chooseOpenFile(Component parent, String fileExtensions, String fileType)
    throws CollabException {
        return chooseFile(parent, fileExtensions, fileType, null, null, null);
    }
}
