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


package org.netbeans.beaninfo.editors;


import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import javax.swing.JFileChooser;

import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/**
 * PropertyEditor for <code>java.io.File</code>.
 *
 * @author  Jaroslav Tulach, David Strupl
 */
public class FileEditor extends PropertyEditorSupport implements ExPropertyEditor {
    
    /** Env passed in the attachEnv method.*/
    private PropertyEnv env;
    
    /** Name of the property obtained from the feature descriptor.*/
    private static final String PROPERTY_SHOW_DIRECTORIES = "directories"; //NOI18N
    
    /** Name of the property obtained from the feature descriptor.*/
    private static final String PROPERTY_SHOW_FILES = "files"; //NOI18N
    
    /** Name of the property obtained from the feature descriptor.*/
    private static final String PROPERTY_FILTER = "filter"; //NOI18N
    
    /** Name of the property obtained from the feature descriptor.*/
    private static final String PROPERTY_CURRENT_DIR = "currentDir"; //NOI18N

    /** Name of the property obtained from the feature descriptor. */
    private static final String PROPERTY_BASE_DIR = "baseDir"; // NOI18N
    
    /** Openning mode.*/
    private int mode = JFileChooser.FILES_AND_DIRECTORIES;
    
    /** Flag indicating whether to choose directories. Default value is <code>true</code>. */
    private boolean directories = true;
    /** Flag indicating whether to choose files. Default value is <code>true</code>. */
    private boolean files = true;
    /** Filter for files to show. */
    private javax.swing.filechooser.FileFilter fileFilter;
    /** Current firectory. */
    private File currentDirectory;
    /** Base directory to which to show relative path, if is set. */
    private File baseDirectory;

    /** Caches last used directory. */
    private static File lastCurrentDir;
    
    /** File chooser. */
    private JFileChooser chooser;
    /** Property change listener. */
    private PropertyChangeListener pListener;
    
    /**
     * This method is called by the IDE to pass
     * the environment to the property editor.
     * @param env Environment passed by the ide.
     */
    public void attachEnv(PropertyEnv env) {
        this.env = env;
        
        Object dirs = env.getFeatureDescriptor().getValue(PROPERTY_SHOW_DIRECTORIES);
        if (dirs instanceof Boolean) {
            directories = ((Boolean)dirs).booleanValue();
        }
        Object fil = env.getFeatureDescriptor().getValue(PROPERTY_SHOW_FILES);
        if (fil instanceof Boolean) {
            files = ((Boolean)fil).booleanValue();
        }
        
        Object filter = env.getFeatureDescriptor().getValue(PROPERTY_FILTER);
        if (filter instanceof FilenameFilter) {
            fileFilter = new DelegatingFilenameFilter((FilenameFilter)filter);
        }
        
        if (filter instanceof javax.swing.filechooser.FileFilter) {
            fileFilter = (javax.swing.filechooser.FileFilter)filter;
        }

        if (filter instanceof java.io.FileFilter) {
            fileFilter = new DelegatingFileFilter((java.io.FileFilter)filter);
        }

        Object curDir = env.getFeatureDescriptor().getValue(PROPERTY_CURRENT_DIR);
        if (curDir instanceof File) {
            currentDirectory = (File)curDir;
        }

        Object baseDir = env.getFeatureDescriptor().getValue(PROPERTY_BASE_DIR);
        if(baseDir instanceof File) {
            baseDirectory = (File)baseDir;
            // As baseDir accept only directories in their absolute form.
            if(!baseDirectory.isDirectory() || !baseDirectory.isAbsolute()) {
                baseDirectory = null;
            }
        }
        
        if (files) {
            mode = directories ? JFileChooser.FILES_AND_DIRECTORIES : 
                JFileChooser.FILES_ONLY;
        } else {
            mode = directories ? JFileChooser.DIRECTORIES_ONLY :
                JFileChooser.FILES_AND_DIRECTORIES; // both false, what now?
        }
    }

    /** Sets value. */
    public void setValue(Object value) {
        super.setValue(value);
        if ((value instanceof File) && (chooser != null)) {
            lastCurrentDir = ((File)value).getParentFile();
        }
    }
    
    /** Returns human readable form of the edited value.
     * @return string reprezentation
     */
    public String getAsText() {
        File retValue = (File)getValue();
        if (retValue == null) {
            return ""; // NOI18N
        }

        if(baseDirectory != null) {
            String relPath = getRelativePath(baseDirectory, retValue);
            if(relPath != null) {
                return relPath;
            }
        }
        
        try {
            return retValue.getCanonicalPath();
        } catch (IOException ioe) {
            // Should not happen.
        }
        
        return null;
    }
    
    /** Parses the given string and should create a new instance of the
     * edited object.
     * @param str string reprezentation of the file (used as a parameter for File).
     * @throws IllegalArgumentException If the given string cannot be parsed
     */
    public void setAsText(String str) throws IllegalArgumentException {
        if (str == null) {
            throw new IllegalArgumentException("null"); // NOI18N
        }

        File f = null;
        
        if(baseDirectory != null) {
            f = new File(str);
            
            if(!f.isAbsolute()) {
                f = new File(baseDirectory, str);
            }
        } else {
            f = new File(str);
        }
        
        if(f != null)  {
            setValue(f);
        }
    }
    
    /** Custon editor.
     * @return Returns custom editor component.
     */
    public Component getCustomEditor() {
        final JFileChooser ch = createFileChooser ();
        return ch;
    }
    
    /** Creates file chooser. */
    private JFileChooser createFileChooser () {
        if (chooser == null) {
            chooser = new JFileChooser();
        }
        
        File originalFile = (File)getValue ();
        chooser.setFileSelectionMode(mode);
        if ((originalFile != null) && (originalFile.isAbsolute())){
            if (originalFile.getParent () != null) {
                chooser.setCurrentDirectory (new File (originalFile.getParent ()));
            }
            chooser.setSelectedFile (originalFile);
        } else {
            if (lastCurrentDir != null) {
                chooser.setCurrentDirectory(lastCurrentDir);
            }
        }
        
        if ((currentDirectory != null) && (currentDirectory.isDirectory())) {
            chooser.setCurrentDirectory (currentDirectory);
        }
        
        chooser.setApproveButtonText (getString ("CTL_ApproveSelect"));
        chooser.setApproveButtonToolTipText (getString ("CTL_ApproveSelectToolTip"));
        if (fileFilter != null) {
            chooser.setFileFilter(fileFilter);
        }
        
        switch (mode) {
        case JFileChooser.FILES_AND_DIRECTORIES:
            chooser.setDialogTitle (getString ("CTL_DialogTitleFilesAndDirs"));
            break;
        case JFileChooser.FILES_ONLY:
            chooser.setDialogTitle (getString ("CTL_DialogTitleFiles"));
            break;
        case JFileChooser.DIRECTORIES_ONLY:
            chooser.setDialogTitle (getString ("CTL_DialogTitleDirs"));
            break;
        }

        chooser.setControlButtonsAreShown(false);

        chooser.removePropertyChangeListener(getPListener());
        
        
        chooser.addPropertyChangeListener(
            JFileChooser.SELECTED_FILE_CHANGED_PROPERTY,
            getPListener()
        );
        
        HelpCtx.setHelpIDString (chooser, getHelpCtx ().getHelpID ());

        return chooser;
    }
    
    /** Lazy initialization of PListener. */
    private PropertyChangeListener getPListener() {
        if (pListener == null) {
            pListener = new PListener();
        }
        return pListener;
    }

    /** Implements PropertyEditor method.
     * @return Returns true.
     */
    public boolean supportsCustomEditor() {
        // [PENDING] see org.openide.propertysheet.editors.FileEditor
        return true;
    }
    
    /** Should create a string insertable to the newly generated source code.
     * @return initialization string
     */
    public String getJavaInitializationString() {
        File value = (File) getValue ();
        if (value == null) {
            return "null"; // NOI18N
        } else {
            // [PENDING] not a full escape of filenames, but enough to at least
            // handle normal Windows backslashes
            return "new java.io.File (\"" + // NOI18N
                   Utilities.replaceString (value.getAbsolutePath (), "\\", "\\\\") // NOI18N
                   + "\")"; // NOI18N
        }
    }

    /** Gets help context. */
    private HelpCtx getHelpCtx () {
        return new HelpCtx (FileEditor.class);
    }
    
    /** Gets localized string. Helper method. */
    private static String getString(String key) {
        return NbBundle.getBundle(FileEditor.class).getString(key);
    }
    
    /** Gets relative path of file to specified directory.
     * @param baseDir base directory
     * @param file file which relative path to <code>baseDir</code> is needed
     * @return rtelative path or <code>null</code> can't be resolved */
    private static String getRelativePath(File baseDir, File file) {
        if(baseDir.equals(file)) {
            return "."; // NOI18N
        }
        
        String basePath = getNormalizedAbsolutePath(baseDir);
        String filePath = getNormalizedAbsolutePath(file);
        
        File parent = baseDir;
        
        int level = 0;
    
        String relPath;
        
        while(true) {
            relPath = extractChildRelativePath(basePath, filePath);
            
            if(relPath != null) {
                break;
            }
            
            parent = parent.getParentFile();

            if(parent == null) {
                return null;
            }
            
            basePath = parent.getAbsolutePath();
            level++;
        }

        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < level; i++) {
            buffer.append(".."); // NOI18N
            if(i < (level - 1)) {
                buffer.append(File.separatorChar);
            }
        }
        
        return buffer.append(relPath).toString();
    }

    /** Gets 'normalized' absolute class path, i.e. path 
     * which doesn't end with '.' and ends with separator char if it is directory.  */
    private static String getNormalizedAbsolutePath(File file) {
        String path = file.getAbsolutePath();
        
        if(path.endsWith(".")) { // NOI18N
            path = path.substring(0, path.length() - 1);
        }
        
        if(file.isDirectory() && !path.endsWith(new String(new char[] {File.separatorChar}))) {
            path += File.separatorChar;
        }
        
        return path;
    }
    
    /** Extracts childs relative path to the path of directory which contains it.
     * @param basePath absolute path to the base directory
     * @param childPath absolute path to the child file/directory
     * @return child's relative path or <code>null</code> if the child path
     * is not in the base directory tree */
    private static String extractChildRelativePath(String basePath, String childPath) {
        if(childPath.startsWith(basePath)) {
            return childPath.substring(basePath.length());
        }
        
        return null;
    }

    
    
    /** Property change listaner attached to the JFileChooser chooser. */
    private class PListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            File f = chooser.getSelectedFile ();
            if (f == null) {
                return;
            }
            
            if (f != null) {
                if (!files && f.isFile ()) return;
                if (!directories && f.isDirectory ()) return;
            }
            Object oldVal = getValue();
            
            if ((oldVal == null) && (f == null)) return;
            
            if ((oldVal == null) || (f == null)) {
                setValue(f);
                return;
            }
            if (!f.equals(oldVal)) {
                setValue(f);
            }
        } 
    } // End of class PListener.
    
    
    /** Wraps java.io.FileFilter to javax.swing.filechooser.FileFilter. */
    private static class DelegatingFileFilter extends javax.swing.filechooser.FileFilter {
        private java.io.FileFilter filter;
        
        public DelegatingFileFilter(java.io.FileFilter f) {
            this.filter = f;
        }
        
        public boolean accept(File f) {
            return filter.accept(f);
        }
        
        public String getDescription() {
            // [PENDING] what should we return?
            return null;
        }
        
    } // End of class DelegatingFileFilter.
    
    
    /** Wraps FilenameFilter to javax.swing.filechooser.FileFilter. */
    private static class DelegatingFilenameFilter extends javax.swing.filechooser.FileFilter {
        private FilenameFilter filter;
        
        public DelegatingFilenameFilter(FilenameFilter f) {
            this.filter = f;
        }
        /** Calls the filenameFilter's accept method with arguments
         * created from the original object f.
         */
        public boolean accept(File f) {
            return filter.accept(f.getParentFile(), f.getName());
        }
        
        public String getDescription() {
            // [PENDING] what should we return?
            return null;
        }
    } // End of class DelegatingFilenameFilter.
    
}
