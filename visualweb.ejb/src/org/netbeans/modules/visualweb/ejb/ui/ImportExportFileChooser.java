/*
 * ImportExportFileChooser.java
 *
 * Created on September 1, 2004, 2:37 PM
 */

package org.netbeans.modules.visualweb.ejb.ui;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.openide.util.NbBundle;

/**
 * A file chooser for choosing a file to import from or export to
 *
 * @author  cao
 */
public class ImportExportFileChooser {

    public static String defaultFilePath = System.getProperty("user.home") + File.separator + "exported_ejb_datasources.jar";

    private JFileChooser fileChooser = org.netbeans.modules.visualweb.extension.openide.awt.JFileChooser_RAVE.getJFileChooser();

    private Component parent;

    public ImportExportFileChooser( Component parent )
    {
        this.parent = parent;

        // Set current dir or default dir
        File curDir = null;
        File curSelection = new File( defaultFilePath );
        if (curSelection.exists()){
            if (curSelection.isDirectory()) {
                curDir = curSelection;
            } else {
                curDir = curSelection.getParentFile();
            }
        }

        if (curDir == null)
            curDir = new File(System.getProperty("user.home")); //NOI18N

        if (curSelection != null && curSelection.exists())
            fileChooser.setSelectedFile(curSelection);

        fileChooser.setCurrentDirectory(curDir);
        fileChooser.addChoosableFileFilter(new JarFilter());
    }

    public static void setCurrentFilePath( String path )
    {
        defaultFilePath = path;
    }

    // Returns the selected file
    public String getExportFile()
    {
        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION)
        {
            String selectedFile = fileChooser.getSelectedFile().getAbsolutePath();

            return selectedFile;
        }
        else
            return null;
    }

    // Returns the selected file
    public String getImportFile()
    {
        if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION)
        {
            String selectedFile = fileChooser.getSelectedFile().getAbsolutePath();

            return selectedFile;
        }
        else
            return null;
    }

     public class JarFilter extends FileFilter {

        //Accept all directories and all ".jar" files.
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String extension = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');

            if (i > 0 &&  i < s.length() - 1) {
                extension = s.substring(i+1).toLowerCase();
            }

            if (extension != null) {
                if (extension.equals("jar")) { //NOI18N
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        }

        //The description of this filter
        public String getDescription() {
            return NbBundle.getMessage(ExportEjbDataSourcesPanel.class, "JAR_FILE_FILTER_DESCRIPTION");
        }
    }
}
