/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.builds;

import java.io.IOException;
import java.io.File;
import java.text.MessageFormat;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.ResourceBundle;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.netbeans.modules.cnd.execution41.org.openide.loaders.ExecutionSupport;
import org.netbeans.modules.cnd.settings.MakeSettings;
import java.beans.PropertyEditorSupport;
import java.beans.PropertyEditor;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;


/**
 *  A support class for helping execution of a Makefile.
 */
public class MakeExecSupport extends ExecutionSupport {
    // the property sheet where properties are shown
    private Sheet.Set sheetSet;
    
    public final static String PROP_BUILD_DIRECTORY = "buildDirectory"; // NOI18N
    public final static String PROP_MAKE_COMMAND = "makeCommand"; // NOI18N
    public final static String PROP_MAKE_OPTIONS = "makeOptions"; // NOI18N
    public final static String PROP_MAKE_TARGETS = "makeTargets"; // NOI18N
    
    // The list of our properties
    private PropertySupport buildDirectoryProperty = null;
    private PropertySupport makeCommandProperty = null;
    private PropertySupport makeOptionsProperty = null;
    private PropertySupport makeTargetsProperty = null;
    
    /** Store a File of the Build directory */
    private File buildDir;
    
    private static ResourceBundle bundle = NbBundle.getBundle(MakeExecSupport.class);
    
    /** Constructor */
    public MakeExecSupport(MultiDataObject.Entry entry) {
        super(entry);
    }
    
    
    public FileObject getFileObject() {
        return getEntry().getFile();
    }
    
    /**
     *  Helper method that creates default properties for a Makefile entry.
     *
     *  @param set sheet set to add properties to
     */
    public void createProperties() {
        if (buildDirectoryProperty == null) {
            buildDirectoryProperty = createBuildDirectoryProperty();
            makeCommandProperty = createMakeCommandProperty();
            makeOptionsProperty = createMakeOptionsProperty();
            makeTargetsProperty = createMakeTargetsProperty();
        }
    }
    
    /**
     *  Helper method that adds propertiesd to property sheet
     *
     *  @param set sheet set to add properties to
     */
    public void addProperties(Sheet.Set set) {
        createProperties();
        
        this.sheetSet = set;
        
        //super.addProperties(set);
        set.put(buildDirectoryProperty);
        //set.put(executorProperty);
        set.put(makeCommandProperty);
        set.put(makeOptionsProperty);
        set.put(makeTargetsProperty);
        
    }
    
    
    /**
     *  Create the build directory property.
     *
     *  @return The build directory property
     */
    private PropertySupport createBuildDirectoryProperty() {
        
        return new PropertySupport.ReadWrite(PROP_BUILD_DIRECTORY, String.class,
                getString("PROP_BUILD_DIRECTORY"), // NOI18N
                getString("HINT_BUILD_DIRECTORY")) { // NOI18N
            
            public Object getValue() {
                return getBuildDirectory();
            }
            public void setValue(Object val) {
                setBuildDirectory((String) val);
            }
            public boolean supportsDefaultValue() {
                return true;
            }
            public void restoreDefaultValue() {
                setValue(null);
            }
            public boolean canWrite() {
                return getEntry().getFile().getParent().canWrite();
            }
        };
    }
    
    /**
     *  Get the the build directory, the directory to invoke make from.
     *
     *  @return the build directory
     */
    public String getBuildDirectory() {
        String dir = (String) getEntry().getFile().getAttribute(PROP_BUILD_DIRECTORY);
        
        if (dir == null) {
            dir = MakeSettings.getDefault().getDefaultBuildDirectory();
            setBuildDirectory(dir);
        }
        
        return dir;
    }
    
    /**
     *  Set the build directory
     *
     *  @param target the build directory
     */
    public void setBuildDirectory(String dir) {
        try {
            getEntry().getFile().setAttribute(PROP_BUILD_DIRECTORY, dir);
        } catch (IOException ex) {
            String msg = MessageFormat.format(
                    getString("MSG_CANT_SET_BUILD_DIRECTORY"), // NOI18N
                    new Object[] { FileUtil.toFile(getEntry().getFile()).getPath() });
            
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                ex.printStackTrace();
            }
        }
    }
    
    
    
    
    /**
     *  Create the make command property.
     *
     *  @return The make command property
     */
    private PropertySupport createMakeCommandProperty() {
        
        return new PropertySupport.ReadWrite(PROP_MAKE_COMMAND, String.class,
                getString("PROP_MAKE_COMMAND"), getString("HINT_MAKE_COMMAND")) { // NOI18N
            
            public Object getValue() {
                return getMakeCommand();
            }
            public void setValue(Object val) {
                setMakeCommand((String) val);
            }
            public boolean supportsDefaultValue() {
                return true;
            }
            public void restoreDefaultValue() {
                setValue(null);
            }
            public boolean canWrite() {
                return getEntry().getFile().getParent().canWrite();
            }
        };
    }
    
    /**
     *  Get the the make command to invoke.
     *
     *  @return the make command
     */
    public String getMakeCommand() {
        String make = (String) getEntry().getFile().getAttribute(PROP_MAKE_COMMAND);
        
        if (make == null || make.equals("")) {
            make = MakeSettings.getDefault().getDefaultMakeCommand();
            setMakeCommand(make);
        }
        
        return make;
    }
    
    /**
     *  Set the make command
     *
     *  @param make the make command
     */
    public void setMakeCommand(String make) {
        try {
            getEntry().getFile().setAttribute(PROP_MAKE_COMMAND, make);
        } catch (IOException ex) {
            String msg = MessageFormat.format(
                    getString("MSG_CANT_SET_MAKE_COMMAND"), // NOI18N
                    new Object[] { FileUtil.toFile(getEntry().getFile()).getPath() });
            
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                ex.printStackTrace();
            }
        }
    }
    
    
    /**
     *  Create the make options property.
     *
     *  @return The make options property
     */
    private PropertySupport createMakeOptionsProperty() {
        
        return new PropertySupport.ReadWrite(PROP_MAKE_OPTIONS, String.class,
                getString("PROP_MAKE_OPTIONS"), getString("HINT_MAKE_OPTIONS")) { // NOI18N
            
            public Object getValue() {
                return getMakeOptions(false);
            }
            public void setValue(Object val) {
                setMakeOptions((String) val);
            }
            public boolean supportsDefaultValue() {
                return true;
            }
            public void restoreDefaultValue() {
                setValue(null);
            }
            public boolean canWrite() {
                return getEntry().getFile().getParent().canWrite();
            }
        };
    }
    
    
    /**
     *  Get the the command line options to invoke make with. These may
     *  be flags recognised by make or variable definitions. They should
     *  bot be targets, although nothing prevents this misuse.
     *
     *  @return the options
     */
    public String getMakeOptions() {
        return getMakeOptions(false);
    }
    
    
    /**
     *  Get the the command line options to invoke make with. These may
     *  be flags recognised by make or variable definitions. They should
     *  bot be targets, although nothing prevents this misuse.
     *
     *  @return the options
     */
    public String getMakeOptions(boolean useCustomizer) {
        StringBuffer options = new StringBuffer(256);
        String savedOptions = (String) getEntry().getFile().getAttribute(PROP_MAKE_OPTIONS);
        
        if (savedOptions == null) {
            savedOptions = ""; // NOI18N
            setMakeOptions(savedOptions);
        }
        
        options.append(savedOptions);
        
        return options.toString();
    }
    
    /**
     *  Set the make options
     *
     *  @param options The make options
     */
    public void setMakeOptions(String options) {
	FileObject fo = getEntry().getFile();
        try {
            fo.setAttribute(PROP_MAKE_OPTIONS, options);
        } catch (IOException ex) {
            String msg = MessageFormat.format(
                    getString("MSG_CANT_SET_MAKE_OPTIONS"), // NOI18N
                    new Object[] { FileUtil.toFile(fo).getPath() });
            
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                ex.printStackTrace();
            }
        }
    }
    
    /**
     *  Create the make target property.
     *
     *  @return The make target property
     */
    private PropertySupport createMakeTargetsProperty() {
        
        return new PropertySupport.ReadWrite(PROP_MAKE_TARGETS, String.class,
                getString("PROP_MAKE_TARGETS"), getString("HINT_MAKE_TARGETS")) { // NOI18N
            
            public Object getValue() {
                return getMakeTargets();
            }
            public void setValue(Object val) {
                setMakeTargets((String) val);
            }
            public boolean supportsDefaultValue() {
                return false;
            }
            public void restoreDefaultValue() {
                setValue(null);
            }
            public boolean canWrite() {
                return getEntry().getFile().getParent().canWrite();
            }
            public PropertyEditor getPropertyEditor() {
                return new TargetsPropertyEditor(this);
            }
        };
    }
    
    /**
     *  Get the (space separated) list of make targets. The target can be an empty string but
     *  not a null.
     *
     *  @return the target list
     */
    public String getMakeTargets() {
        String target = (String) getEntry().getFile().getAttribute(PROP_MAKE_TARGETS);
        
        if (target == null) {
            target = ""; // NOI18N
            setMakeTargets(target);
        }
        
        return target;
    }
    
    private String[] tokenizeTargets(String targets) {
        StringTokenizer st = new StringTokenizer(targets, ";:,"); // NOI18N
        Vector v = new Vector();
        while (st.hasMoreTokens()) {
            int n = 0;
            String t = st.nextToken();
            // strip leading spaces...
            while (n < t.length() && Character.isWhitespace(t.charAt(n))) {
                n++;
                if (n >= t.length())
                    break;
            }
            if (n < t.length()) {
                if (n > 0)
                    v.add(t.substring(n));
                else
                    v.add(t);
            }
        }
        String[] ret = new String[v.size()];
        for (int i = 0; i < v.size(); i++) {
            ret[i] = (String)v.elementAt(i);
        }
        return ret;
    }
    
    /**
     *  Get the list of make targets as an array of Strings.
     *
     *  @return the array of targets
     */
    public String[] getMakeTargetsArray() {
        return tokenizeTargets(getMakeTargets());
    }
    
    /**
     *  Set the make target list
     *
     *  @param targetlist the (space separated) make target list
     */
    public void setMakeTargets(String targetlist) {
	FileObject fo = getEntry().getFile();
        try {
            fo.setAttribute(PROP_MAKE_TARGETS, targetlist);
            // This is a hack! How to refresh the property sheet so it shows the changed value????
            // FIXUP !!!!
            if (sheetSet != null) {
                sheetSet.remove(PROP_MAKE_TARGETS);
                sheetSet.put(makeTargetsProperty);
            }
            // End hack
        } catch (IOException ex) {
            String msg = MessageFormat.format(getString("MSG_CANT_SET_MAKE_TARGETS"), // NOI18N
                    new Object[] { FileUtil.toFile(fo).getPath() });
            
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                ex.printStackTrace();
            }
        }
    }
    
    /**
     *  Add a space separated list of targts to the target list
     *
     *  @param newtarget the (space separated) target list
     */
    public void addMakeTargets(String newtargets) {
        String targets = (String) getEntry().getFile().getAttribute(PROP_MAKE_TARGETS);
        
        if (newtargets == null || newtargets.length() == 0)
            return;
        
        if (targets == null)
            targets = "";   // NOI18N
        
        if (targets.length() == 0)
            targets = newtargets;
        else
            targets = targets + ", " + newtargets;   // NOI18N
        setMakeTargets(targets);
    }
    
    class TargetsPropertyEditor extends PropertyEditorSupport implements ExPropertyEditor {
        private PropertySupport prop = null;
        private PropertyEnv env;
        
        TargetsPropertyEditor(PropertySupport prop) {
            this.prop = prop;
        }
        
        public java.awt.Component getCustomEditor() {
            String val = null;
            try {
                val = (String)prop.getValue();
            } catch (Exception e) {}
            
            return new TargetEditor(tokenizeTargets(val), this, env);
        }
        
        public boolean supportsCustomEditor() {
            return true;
        }
        
        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }
    }
    
    
    private static String getString(String prop) {
        return bundle.getString(prop);
    }
}
