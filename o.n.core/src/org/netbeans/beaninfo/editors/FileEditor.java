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

import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import java.awt.event.*;
import java.beans.*;

import javax.swing.JFileChooser;
import javax.swing.JButton;

import org.openide.explorer.propertysheet.*;
import org.openide.util.*;
import org.openide.TopManager;

/**
 * PropertyEditor for java.io.File.
 * @author  jtulach, dstrupl
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
    
    /** Openning mode.*/
    private int mode = JFileChooser.FILES_AND_DIRECTORIES;
    
    private boolean directories = true;
    private boolean files = true;
    private javax.swing.filechooser.FileFilter fileFilter = null;
    private java.io.File currentDirectory = null;

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
        if (filter instanceof java.io.FilenameFilter) {
            fileFilter = new DelegatingFilenameFilter((java.io.FilenameFilter)filter);
        }
        
        if (filter instanceof javax.swing.filechooser.FileFilter) {
            fileFilter = (javax.swing.filechooser.FileFilter)filter;
        }

        if (filter instanceof java.io.FileFilter) {
            fileFilter = new DelegatingFileFilter((java.io.FileFilter)filter);
        }

        Object curDir = env.getFeatureDescriptor().getValue(PROPERTY_CURRENT_DIR);
        if (curDir instanceof java.io.File) {
            currentDirectory = (File)curDir;
        }

        if (files) {
            mode = directories ? JFileChooser.FILES_AND_DIRECTORIES : 
                JFileChooser.FILES_ONLY;
        } else {
            mode = directories ? JFileChooser.DIRECTORIES_ONLY :
                JFileChooser.FILES_AND_DIRECTORIES; // both false, what now?
        }
    }
    
    /** Returns human readable form of the edited value.
     * @return string reprezentation
     */
    public java.lang.String getAsText() {
        File retValue = (File)getValue();
        try {
            return retValue.getCanonicalPath();
        } catch (IOException x) {
        }
        return null;
    }
    
    /** Parses the given string and should create a new instance of the
     * edited object.
     * @param str string reprezentation of the file (used as a parameter for java.io.File).
     * @throws IllegalArgumentException If the given string cannot be parsed
     */
    public void setAsText(java.lang.String str) throws java.lang.IllegalArgumentException {
        if (str == null) {
            throw new IllegalArgumentException("null"); // 
        }
        File f = new File(str);
        if (f != null)  {
            setValue(f);
        }
    }
    
    /** Custon editor.
     * @return Returns custom editor component.
     */
    public java.awt.Component getCustomEditor() {
        final JFileChooser chooser = createFileChooser ();

//      call by reflection this:  chooser.setControlButtonsAreShown(false);
//          - the method is not in JDK1.2
        try {
            Class clazz = chooser.getClass();
            Method m = clazz.getMethod("setControlButtonsAreShown", new Class [] { Boolean.TYPE } ); //NOI18N
            m.invoke(chooser, new Object[] { Boolean.FALSE });
        } catch (NoSuchMethodException x) {
            removeControlButtonsHack(chooser);
        } catch (InvocationTargetException x) {
            removeControlButtonsHack(chooser);
        } catch (IllegalAccessException x) {
            removeControlButtonsHack(chooser);
        }
        
        chooser.addPropertyChangeListener(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY,
        new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                File f = chooser.getSelectedFile ();
                if (f != null) {
                    if (!files && f.isFile ()) return;
                    if (!directories && f.isDirectory ()) return;
                }

                setValue(f);
            } 
        });
        
        HelpCtx.setHelpIDString (chooser, getHelpCtx ().getHelpID ());
        return chooser;
    }
    
    /** 
     */
    private JFileChooser createFileChooser () {
        final JFileChooser chooser = new JFileChooser();
        
        // [PENDING] should the value of currentDirectory override previous
        // opened directory?
        if ((currentDirectory != null) && (currentDirectory.isDirectory())) {
            chooser.setCurrentDirectory (currentDirectory);
        }
        
        File originalFile = (File)getValue ();
        chooser.setFileSelectionMode(mode);
        if (originalFile != null && originalFile.getParent () != null) {
            chooser.setCurrentDirectory (new File (originalFile.getParent ()));
        }
        if (originalFile != null) {
            chooser.setSelectedFile (originalFile);
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

        return chooser;
    }
    
    /** Implements java.beans.PropertyEditor method.
     * @return Returns true.
     */
    public boolean supportsCustomEditor() {
        // [PENDING] see org.openide.propertysheet.editors.FileEditor
        return true;
    }
    
    /** Should create a string insertable to the newly generated source code.
     * @return initialization string
     */
    public java.lang.String getJavaInitializationString() {
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
    
    /** This method is usefull only in JDK1.2. It removes the Ok, Cancel
     * buttons from the JFileChooser.
     */
    private static void removeControlButtonsHack(JFileChooser chooser) {
        List candidate = new ArrayList();
        candidate.add(chooser);
        while (!candidate.isEmpty()) {
            Object o = candidate.get(0);
            candidate.remove(0);
            if (o instanceof JButton) {
                JButton jb = (JButton)o;
                String text = jb.getText();
                if ((text != null) && (text.length() > 1)) {
                    // any button with text set to something reasonable will be removed
                    jb.getParent().remove(jb);
                }
            }
            if (o instanceof java.awt.Container) {
                java.awt.Container c = (java.awt.Container)o;
                Object []children = c.getComponents();
                for (int i = 0; i < children.length; i++) {
                    candidate.add(children[i]);
                }
            }
        }
    }
    
    private static String getString(String key) {
        return NbBundle.getBundle(FileEditor.class).getString(key);
    }
    
    /** 
     */
    private HelpCtx getHelpCtx () {
        return new HelpCtx (FileEditor.class);
    }
    
    /** Wraps java.io.FileFilter to javax.swing.filechooser.FileFilter. */
    private static class DelegatingFileFilter extends javax.swing.filechooser.FileFilter {
        private java.io.FileFilter filter;
        
        public DelegatingFileFilter(java.io.FileFilter f) {
            this.filter = f;
        }
        
        public boolean accept(java.io.File f) {
            return filter.accept(f);
        }
        
        public String getDescription() {
            // [PENDING] what should we return?
            return null;
        }
        
    }
    
    /** Wraps java.io.FilenameFilter to javax.swing.filechooser.FileFilter. */
    private static class DelegatingFilenameFilter extends javax.swing.filechooser.FileFilter {
        private java.io.FilenameFilter filter;
        
        public DelegatingFilenameFilter(java.io.FilenameFilter f) {
            this.filter = f;
        }
        /** Calls the filenameFilter's accept method with arguments
         * created from the original object f.
         */
        public boolean accept(java.io.File f) {
            return filter.accept(f.getParentFile(), f.getName());
        }
        
        public String getDescription() {
            // [PENDING] what should we return?
            return null;
        }
    }
}
