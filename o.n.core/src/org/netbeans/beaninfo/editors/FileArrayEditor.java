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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo.editors;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import java.io.File;
import java.io.FilenameFilter;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

import org.openide.nodes.Node;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * PropertyEditor for <code>[java.io.File</code>.
 *
 * @author David Strupl
 */
public class FileArrayEditor extends PropertyEditorSupport implements ExPropertyEditor, PropertyChangeListener {
    
    /** Openning mode.*/
    private int mode = JFileChooser.FILES_AND_DIRECTORIES;
    
    /** Flag indicating whether to choose directories. Default value is <code>true</code>. */
    private boolean directories = true;
    /** Flag indicating whether to choose files. Default value is <code>true</code>. */
    private boolean files = true;
    /** Flag indicating whether to hide files marked as hidden. Default value is <code>false</code>. */
    private boolean fileHiding = false;
    /** Filter for files to show. */
    private javax.swing.filechooser.FileFilter fileFilter;
    /** Current firectory. */
    private File currentDirectory;
    /** Base directory to which to show relative path, if is set. */
    private File baseDirectory;

    /** Cached chooser.
     * If you don't cache it, MountIterator in core flickers and behaves weirdly,
     * because apparently PropertyPanel will call getCustomEditor repeatedly and
     * refresh the display each time.
     * XXX MountIterator is dead so is this still necessary? -jglick
     */
    private JFileChooser chooser;
    
    /** whether the value can be edited -- default to true */
    private boolean editable = true;

    /**
     * This method is called by the IDE to pass
     * the environment to the property editor.
     * @param env Environment passed by the ide.
     */
    public void attachEnv(PropertyEnv env) {
        // clearing to defaults
        directories = true;
        files = true;
        fileFilter = null;
        fileHiding = false;

        Object dirs = env.getFeatureDescriptor().getValue(FileEditor.PROPERTY_SHOW_DIRECTORIES);
        if (dirs instanceof Boolean) {
            directories = ((Boolean)dirs).booleanValue();
        } // XXX else if != null, warn
        Object fil = env.getFeatureDescriptor().getValue(FileEditor.PROPERTY_SHOW_FILES);
        if (fil instanceof Boolean) {
            files = ((Boolean)fil).booleanValue();
        } // XXX else if != null, warn
        Object filter = env.getFeatureDescriptor().getValue(FileEditor.PROPERTY_FILTER);
        if (filter instanceof FilenameFilter) {
            fileFilter = new FileEditor.DelegatingFilenameFilter((FilenameFilter)filter);
        } else if (filter instanceof javax.swing.filechooser.FileFilter) {
            fileFilter = (javax.swing.filechooser.FileFilter)filter;
        } else if (filter instanceof java.io.FileFilter) {
            fileFilter = new FileEditor.DelegatingFileFilter((java.io.FileFilter)filter);
        } // XXX else if != null, warn

        Object curDir = env.getFeatureDescriptor().getValue(FileEditor.PROPERTY_CURRENT_DIR);
        if (curDir instanceof File) {
            currentDirectory = (File)curDir;
            if(! currentDirectory.isDirectory()) {
                Logger.getAnonymousLogger().warning("java.io.File will not accept currentDir=" + baseDirectory); // NOI18N
                currentDirectory = null;
            }
        } // XXX else if != null, warn

        Object baseDir = env.getFeatureDescriptor().getValue(FileEditor.PROPERTY_BASE_DIR);
        if(baseDir instanceof File) {
            baseDirectory = (File)baseDir;
            // As baseDir accept only directories in their absolute form.
            if(!baseDirectory.isDirectory() || !baseDirectory.isAbsolute()) {
                Logger.getAnonymousLogger().warning("java.io.File will not accept baseDir=" + baseDirectory); // NOI18N
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
        
        Object fileHide = env.getFeatureDescriptor().getValue(FileEditor.PROPERTY_FILE_HIDING);
        if (fileHide instanceof Boolean) {
            fileHiding = ((Boolean)fileHide).booleanValue();
        }
        
        if (env.getFeatureDescriptor() instanceof Node.Property){
            Node.Property prop = (Node.Property)env.getFeatureDescriptor();
            editable = prop.canWrite();
        }
    }

    /** Returns human readable form of the edited value.
     * @return string reprezentation
     */
    public String getAsText() {
        File[] file = (File[])getValue();
        if (file == null) {
            return ""; // NOI18N
        }
        String path = "[";
        for (int i = 0; i < file.length; i++) {
            path += file[i].getPath();
        }
        // Dot is more friendly to people though Java itself would prefer blank:
        if (path.equals("[")) path = "[."; // NOI18N
        return path + "]"; // NOI18N
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
        
        // [PENDING] Add tokenizer here !!!
    }

    /** Custon editor.
     * @return Returns custom editor component.
     */
    public Component getCustomEditor() {
        if (!editable) {
            return new StringCustomEditor(getAsText(), false);
        }
        if (chooser == null) {
            chooser = FileEditor.createHackedFileChooser();
            chooser.setMultiSelectionEnabled(true);
        
            Object vv = getValue ();
            File originalFile = null;
            
            if (vv instanceof File[]) {
                File[] ofile = (File[]) vv;
                if (ofile.length > 0) {
                    originalFile = ofile[0];
                    if (originalFile != null && ! originalFile.isAbsolute() && baseDirectory != null) {
                        originalFile = new File(baseDirectory, originalFile.getPath());
                    }
                }
            }
            if (currentDirectory != null) {
                chooser.setCurrentDirectory (currentDirectory);
            } else if (originalFile != null && originalFile.getParentFile() != null) {
                chooser.setCurrentDirectory (originalFile.getParentFile());
                chooser.setSelectedFile (originalFile);
            } else if (FileEditor.lastCurrentDir != null) {
                chooser.setCurrentDirectory(FileEditor.lastCurrentDir);
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
            chooser.setFileHidingEnabled(fileHiding);

            chooser.setControlButtonsAreShown(false);

            chooser.addPropertyChangeListener(this);
            
            HelpCtx.setHelpIDString (chooser, getHelpCtx ().getHelpID ());
        }
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
        File[] value = (File[]) getValue ();
        if (value == null) {
            return "null"; // NOI18N
        } else {
            // [PENDING] not a full escape of filenames, but enough to at least
            // handle normal Windows backslashes
            String retVal = "new java.io.File[] { "; // NOI18N
            for (int i = 0; i < value.length; i++) {
                if (baseDirectory != null && !value[i].isAbsolute()) {
                    retVal += "new java.io.File(" // NOI18N
                        + FileEditor.stringify(baseDirectory.getPath())
                        + ", " // NOI18N
                        + FileEditor.stringify(value[i].getPath())
                        + "), "; // NOI18N
                } else {
                    retVal += "new java.io.File(" // NOI18N
                        + FileEditor.stringify(value[i].getAbsolutePath())
                        + "), "; // NOI18N
                }
            }
            return retVal+" }";
        }
    }
    
    /** Gets help context. */
    private HelpCtx getHelpCtx () {
        return new HelpCtx (FileEditor.class);
    }
    
    /** Gets localized string. Helper method. */
    private static String getString(String key) {
        return NbBundle.getBundle(FileArrayEditor.class).getString(key);
    }
    
    /** Property change listaner attached to the JFileChooser chooser. */
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getSource() instanceof JFileChooser) {
            JFileChooser jfc = (JFileChooser) e.getSource();
            if (mode == jfc.DIRECTORIES_ONLY && jfc.DIRECTORY_CHANGED_PROPERTY.equals(e.getPropertyName())) {
                if (jfc.getSelectedFile() == null) {
                    File dir = jfc.getCurrentDirectory();
                    if (dir != null) {
                        setValue (new File[] {new File(dir.getAbsolutePath())});
                        return;
                    }
                }
            }
        }
        
        if (( ! JFileChooser.SELECTED_FILES_CHANGED_PROPERTY.equals(e.getPropertyName())) && 
            ( ! JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(e.getPropertyName()))) {
                return;
        }
        if (! (e.getSource() instanceof JFileChooser)) {
            return;
        }
        JFileChooser chooser = (JFileChooser)e.getSource();
        File[] f = (File[])chooser.getSelectedFiles();
        if (f == null) {
            return;
        }
        
        if ((f.length == 0) && (chooser.getSelectedFile() != null)) {
            f = new File[] { chooser.getSelectedFile() };
        }
        
        for (int i = 0; i < f.length; i++) {
            if (!files && f[i].isFile ()) return;
            if (!directories && f[i].isDirectory ()) return;
        }

        if (baseDirectory != null) {
            for (int i = 0; i < f.length; i++) {
                String rel = FileEditor.getChildRelativePath(baseDirectory, f[i]);
                if (rel != null) {
                    f[i] = new File(rel);
                }
            }
        }
        
        File[] nf = new File[f.length];
        for (int i = 0; i < f.length; i++) {
            // the next line is
            // workaround for JDK bug 4533419
            // it should be returned back to f[i] after the
            // mentioned bug is fixed in JDK.
            nf[i] = new File(f[i].getAbsolutePath());
        }
        setValue(nf);
        
        FileEditor.lastCurrentDir = chooser.getCurrentDirectory();
    }
}
