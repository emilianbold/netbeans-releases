/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo.editors;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import java.io.File;
import java.io.FilenameFilter;
import javax.swing.JFileChooser;

import org.openide.ErrorManager;
import org.openide.TopManager;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * PropertyEditor for <code>java.io.File</code>.
 *
 * @author Jaroslav Tulach, David Strupl, Peter Zavadsky, Jesse Glick
 */
public class FileEditor extends PropertyEditorSupport implements ExPropertyEditor, PropertyChangeListener {
    
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
    
    /** Cached chooser.
     * If you don't cache it, MountIterator in core flickers and behaves weirdly,
     * because apparently PropertyPanel will call getCustomEditor repeatedly and
     * refresh the display each time.
     */
    private JFileChooser chooser;
    
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
        } // XXX else if != null, warn
        Object fil = env.getFeatureDescriptor().getValue(PROPERTY_SHOW_FILES);
        if (fil instanceof Boolean) {
            files = ((Boolean)fil).booleanValue();
        } // XXX else if != null, warn
        
        Object filter = env.getFeatureDescriptor().getValue(PROPERTY_FILTER);
        if (filter instanceof FilenameFilter) {
            fileFilter = new DelegatingFilenameFilter((FilenameFilter)filter);
        } else if (filter instanceof javax.swing.filechooser.FileFilter) {
            fileFilter = (javax.swing.filechooser.FileFilter)filter;
        } else if (filter instanceof java.io.FileFilter) {
            fileFilter = new DelegatingFileFilter((java.io.FileFilter)filter);
        } // XXX else if != null, warn

        Object curDir = env.getFeatureDescriptor().getValue(PROPERTY_CURRENT_DIR);
        if (curDir instanceof File) {
            currentDirectory = (File)curDir;
            if(! currentDirectory.isDirectory()) {
                TopManager.getDefault().getErrorManager().log(ErrorManager.WARNING, "java.io.File will not accept currentDir=" + baseDirectory); // NOI18N
                currentDirectory = null;
            }
        } // XXX else if != null, warn

        Object baseDir = env.getFeatureDescriptor().getValue(PROPERTY_BASE_DIR);
        if(baseDir instanceof File) {
            baseDirectory = (File)baseDir;
            // As baseDir accept only directories in their absolute form.
            if(!baseDirectory.isDirectory() || !baseDirectory.isAbsolute()) {
                TopManager.getDefault().getErrorManager().log(ErrorManager.WARNING, "java.io.File will not accept baseDir=" + baseDirectory); // NOI18N
                baseDirectory = null;
            }
        } // XXX else if != null, warn
        
        if (files) {
            mode = directories ? JFileChooser.FILES_AND_DIRECTORIES : 
                JFileChooser.FILES_ONLY;
        } else {
            mode = directories ? JFileChooser.DIRECTORIES_ONLY :
                JFileChooser.FILES_AND_DIRECTORIES; // both false, what now? XXX warn
        }
    }

    /** Returns human readable form of the edited value.
     * @return string reprezentation
     */
    public String getAsText() {
        File file = (File)getValue();
        if (file == null) {
            return ""; // NOI18N
        }
        String path = file.getPath();
        // Dot is more friendly to people though Java itself would prefer blank:
        if (path.equals("")) path = "."; // NOI18N
        return path;
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
        if (str.equals("")) { // NOI18N
            setValue(null);
            return;
        }
        // See getAsText.
        if (str.equals(".")) str = ""; // NOI18N
        setValue(new File(str));
    }

    /** Custon editor.
     * @return Returns custom editor component.
     */
    public Component getCustomEditor() {
        boolean nue = false;
        if (chooser == null) {
            nue = true;
            chooser = new JFileChooser();
        }
        
        File originalFile = (File)getValue ();
        if (originalFile != null && ! originalFile.isAbsolute() && baseDirectory != null) {
            originalFile = new File(baseDirectory, originalFile.getPath());
        }
        if (currentDirectory != null) {
            chooser.setCurrentDirectory (currentDirectory);
        } else if (originalFile != null && originalFile.getParentFile() != null) {
            chooser.setCurrentDirectory (originalFile.getParentFile());
            chooser.setSelectedFile (originalFile);
        } else if (lastCurrentDir != null) {
            chooser.setCurrentDirectory(lastCurrentDir);
        }
        
        chooser.setFileSelectionMode(mode);
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

        if (nue) {
            chooser.addPropertyChangeListener(
                JFileChooser.SELECTED_FILE_CHANGED_PROPERTY,
                this
            );
        }
        
        HelpCtx.setHelpIDString (chooser, getHelpCtx ().getHelpID ());

        return chooser;
    }
    
    /** Implements PropertyEditor method.
     * @return Returns true.
     */
    public boolean supportsCustomEditor() {
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
            if (baseDirectory != null && !value.isAbsolute()) {
                return "new java.io.File(" // NOI18N
                    + stringify(baseDirectory.getPath())
                    + ", " // NOI18N
                    + stringify(value.getPath())
                    + ")"; // NOI18N
            } else {
                return "new java.io.File(" // NOI18N
                    + stringify(value.getAbsolutePath())
                    + ")"; // NOI18N
            }
        }
    }
    private static String stringify(String in) {
        StringBuffer buf = new StringBuffer(in.length() * 2 + 2);
        buf.append('"'); // NOI18N
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if (c == '\\' || c == '"') { // NOI18N
                buf.append('\\'); // NOI18N
            }
            buf.append(c);
        }
        buf.append('"'); // NOI18N
        return buf.toString();
    }

    /** Gets help context. */
    private HelpCtx getHelpCtx () {
        return new HelpCtx (FileEditor.class);
    }
    
    /** Gets localized string. Helper method. */
    private static String getString(String key) {
        return NbBundle.getBundle(FileEditor.class).getString(key);
    }
    
    /** Gets relative path of file to specified directory only for case the file
     * is in directory tree.
     * @param baseDir base directory
     * @param file file which relative path to <code>baseDir</code> is needed
     * @return relative path or <code>null</code> can't be resolved 
     * or if the <code>file</code> is not under <code>baseDir</code> tree */
    private static String getChildRelativePath(File baseDir, File file) {
        // Handle hypothetical weird situations where file is in baseDir
        // but the prefixes do not match. E.g.:
        // file=\foo\bar.txt (assumed to be on C:) baseDir=c:\foo
        if (file.equals(baseDir)) {
            // The empty pathname, not ".", is correct here I think...
            // Try making new File(new File("/tmp", x)) for x in {".", ""}
            return ""; // NOI18N
        }
        StringBuffer buf = new StringBuffer(file.getPath().length());
        buf.append(file.getName());
        for (File parent = file.getParentFile(); parent != null; parent = parent.getParentFile()) {
            if (parent.equals(baseDir)) {
                return buf.toString();
            }
            buf.insert(0, File.separatorChar);
            buf.insert(0, parent.getName());
        }
        return null;
    }
    
    /** Property change listaner attached to the JFileChooser chooser. */
    public void propertyChange(PropertyChangeEvent e) {
        JFileChooser chooser = (JFileChooser)e.getSource();
        File f = (File)chooser.getSelectedFile();
        if (f == null) {
            return;
        }

        if (!files && f.isFile ()) return;
        if (!directories && f.isDirectory ()) return;

        if (baseDirectory != null) {
            String rel = getChildRelativePath(baseDirectory, f);
            if (rel != null) {
                f = new File(rel);
            }
        }
        setValue(f);
        
        lastCurrentDir = chooser.getCurrentDirectory();
    }
    
    
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
