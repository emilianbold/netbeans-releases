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

package org.netbeans.modules.cnd.makeproject.api.runprofiles;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import org.netbeans.modules.cnd.api.utils.FileChooser;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationAuxObject;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.makeproject.runprofiles.RunProfileXMLCodec;
import org.netbeans.modules.cnd.makeproject.runprofiles.ui.EnvPanel;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoder;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Utilities;

public class RunProfile implements ConfigurationAuxObject {
    public static final String PROFILE_ID = "runprofile";
    
    /** Property name: runargs (args, cd, etc.) have changed */
    public static final String PROP_RUNARGS_CHANGED = "runargs-ch"; // NOI18N
    public static final String PROP_RUNDIR_CHANGED = "rundir-ch"; // NOI18N
    public static final String PROP_ENVVARS_CHANGED = "envvars-ch"; // NOI18N
    
    private PropertyChangeSupport pcs = null;
    
    private boolean needSave = false;
    
    // Auxiliary info objects (debugger, ...)
    private Vector auxObjects;
    
    // Where this profile is keept
    //private Profiles parent;
    // Clone
    private RunProfile cloneOf;
    // Default Profile. One and only one profile is the default.
    private boolean defaultProfile;
    // Arguments. Quoted flat representation.
    private String argsFlat;
    private boolean argsFlatValid = false;
    // Argumants. Array form.
    private String[] argsArray;
    private boolean argsArrayValid = false;
    // Run Directory. Relative or absolute.
    private String baseDir; // Alwasy set, always absolute
    private String runDir;  // relative (to baseDir) or absolute
    // Should start a build before executing/debugging.
    private boolean buildFirst;
    // Environment
    private Env environment;
    
    public RunProfile(String baseDir) {
        this.baseDir = baseDir;
        this.pcs = null;
        initialize();
    }
    
    public RunProfile(String baseDir, PropertyChangeSupport pcs) {
        this.baseDir = baseDir;
        this.pcs = pcs;
        initialize();
    }
    
    public void initialize() {
        //parent = null;
        environment = new Env();
        defaultProfile = false;
        argsFlat = ""; // NOI18N
        argsFlatValid = true;
        argsArrayValid = false;
        runDir = ".";
        buildFirst = true;
        clearChanged();
    }
    
    public boolean shared() {
        return false;
    }
    
    /**
     * Returns an unique id (String) used to retrive this object from the
     * pool of aux objects
     * OLD:
     * and for storing the object in xml form and
     * parsing the xml code to restore the object.
     */
    public String getId() {
        return PROFILE_ID;
    }
    
    // Set if this profile is a clone of another profile (not set for copy)
    public void setCloneOf(RunProfile profile) {
        this.cloneOf = profile;
    }
    
    public RunProfile getCloneOf() {
        return cloneOf;
    }
    
    // Default Profile ...
    public boolean isDefault() {
        return defaultProfile;
    }
    
    public void setDefault(boolean b) {
        defaultProfile = b;
    }
    
    // Args ...
    public void setArgs(String argsFlat) {
        String oldArgsFlat = getArgsFlat();
        this.argsFlat = argsFlat;
        argsFlatValid = true;
        argsArrayValid = false;
        if (pcs != null && !IpeUtils.sameString(oldArgsFlat, argsFlat))
            pcs.firePropertyChange(PROP_RUNARGS_CHANGED, oldArgsFlat, argsFlat);
        needSave = true;
    }
    
    public void setArgs(String[] argsArray) {
        String[] oldArgsArray = getArgsArray();
        this.argsArray = argsArray;
        argsFlatValid = false;
        argsArrayValid = true;
        if (pcs != null && !IpeUtils.sameStringArray(oldArgsArray, argsArray))
            pcs.firePropertyChange(PROP_RUNARGS_CHANGED, oldArgsArray, argsArray);
        needSave = true;
    }
    
    public String getArgsFlat() {
        if (!argsFlatValid) {
            argsFlat = "";
            for (int i = 0; i < argsArray.length; i++) {
                argsFlat += IpeUtils.quoteIfNecessary(argsArray[i]);
                if (i < (argsArray.length-1))
                    argsFlat += " ";
            }
            argsFlatValid = true;
        }
        return argsFlat;
    }
    
    public String[] getArgsArray() {
        if (!argsArrayValid) {
            argsArray = Utilities.parseParameters(argsFlat);
            argsArrayValid = true;
        }
        return argsArray;
    }
    
        /*
         * as array shifted one and executable as arg 0
         */
    public String[] getArgv(String ex) {
        String[] argsArrayShifted = new String[getArgsArray().length+1];
        argsArrayShifted[0] = ex;
        for (int i = 0; i < getArgsArray().length; i++)
            argsArrayShifted[i+1] = getArgsArray()[i];
        return argsArrayShifted;
    }
    
        /*
         * Gets base directory. Base directory is always set and is always absolute.
         * Base directory is what run directory is relative to, if it is relative.
         */
    public String getBaseDir() {
        return baseDir;
    }
    
        /*
         * Sets base directory. Base directory should  always be set and is always absolute.
         * Base directory is what run directory is relative to if it is relative.
         */
    public void setBaseDir(String baseDir) {
        assert baseDir != null && IpeUtils.isPathAbsolute( baseDir );
        this.baseDir = baseDir;
    }
    
        /*
         * Gets run directory.
         * Run Directory is either absolute or relative (to base directory).
         */
    public String getRunDir() {
        if (runDir == null || runDir.length() == 0)
            runDir = ".";
        return runDir;
    }
    
        /*
         * sets run directory.
         * Run Directory is either absolute or relative (to base directory).
         */
    public void setRunDir(String runDir) {
        if (runDir == null || runDir.length() == 0)
            runDir = ".";
        if (this.runDir == runDir)
            return;
        if (this.runDir != null && this.runDir.equals(runDir)) {
            return;
        }
        String oldRunDir = this.runDir;
        this.runDir = runDir;
        if (pcs != null)
            pcs.firePropertyChange(PROP_RUNDIR_CHANGED, null, this);
        needSave = true;
    }
    
    
        /*
         * Gets absolute run directory.
         */
    public String getRunDirectory() {
        String runDirectory;
        String runDirectoryCanonicalPath;
        if (getRunDir().length() == 0)
            setRunDir(".");
        if (IpeUtils.isPathAbsolute(getRunDir()))
            runDirectory = getRunDir();
        else
            runDirectory = getBaseDir() + "/" + getRunDir();
        
        // convert to canonical path
        File runDirectoryFile = new File(runDirectory);
        if (!runDirectoryFile.exists() || !runDirectoryFile.isDirectory()) {
            return runDirectory; // ??? FIXUP
        }
        try {
            runDirectoryCanonicalPath = runDirectoryFile.getCanonicalPath();
        } catch (IOException ioe) {
            runDirectoryCanonicalPath = runDirectory;
        }
        return runDirectoryCanonicalPath;
    }
    
        /*
         * Sets run directory.
         * If new run directory is relative, just set it.
         * If new run directory is absolute, convert to relative if already relative,
         * othervise just set it.
         */
    public void setRunDirectory(String newRunDir) {
        if (newRunDir == null || newRunDir.length() == 0) {
            newRunDir = ".";
        }
        setRunDir(IpeUtils.toAbsoluteOrRelativePath(getBaseDir(), newRunDir));
    }
    
    // Should Build ...
    public void setBuildFirst(boolean buildFirst) {
        this.buildFirst = buildFirst;
    }
    
    public boolean getBuildFirst() {
        return buildFirst;
    }
    
    // Environment
    public Env getEnvironment() {
        return environment;
    }
    
    public void setEnvironment(Env environment) {
        this.environment = environment;
        if (pcs != null)
            pcs.firePropertyChange(PROP_ENVVARS_CHANGED, null, this);
    }
    
    
    // Misc...
    
    /**
     * Saves this profile *and* all other profiles of the same parent to disk
     */
    public void saveToDisk() {
            /*
            if (parent != null) {
                parent.saveToDisk();
            }
             */
    }
    
    /**
     *  Adds property change listener.
     *  @param l new listener.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (pcs != null)
            pcs.addPropertyChangeListener(l);
    }
    
    /**
     *  Removes property change listener.
     *  @param l removed listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (pcs != null)
            pcs.removePropertyChangeListener(l);
    }
    
    //
    // XML codec support
    // This stuff ends up in <projectdir>/nbproject/private/profiles.xml
    //
    
    public XMLDecoder getXMLDecoder() {
        return new RunProfileXMLCodec(this);
    }
    
    public XMLEncoder getXMLEncoder() {
        return new RunProfileXMLCodec(this);
    }
    
    /**
     * Responsible for saving the object in xml format.
     * It should save the object in the following format using the id
     * string from getId():
     * <id-string>
     *     <...
     *     <...
     * </id-string>
     */
    
        /* OLD
        public void writeElement(PrintWriter pw, int indent, Object object) {
            RunProfileHelper.writeProfileBlock(pw, indent, this);
        }
         */
    
    /**
     * Responsible for parsing the xml code created from above and
     * for restoring the state of the object (but not the object itself).
     * Refer to the Sax parser documentation for details.
     */
    
        /* OLD
        public void startElement(String namespaceURI, String localName, String element, Attributes atts) {
            RunProfileHelper.startElement(this, element, atts);
        }
         */
    
        /* OLD
        public void endElement(String uri, String localName, String qName, String currentText) {
            RunProfileHelper.endElement(this, qName, currentText);
        }
         */
    
    // interface ProfileAuxObject
    public boolean hasChanged() {
        return needSave;
    }
    
    // interface ProfileAuxObject
    public void clearChanged() {
        needSave = false;
    }
    
    public void assign(ConfigurationAuxObject profileAuxObject) {
        if (!(profileAuxObject instanceof RunProfile)) {
            // FIXUP: exception ????
            System.err.print("Profile - assign: Profile object type expected - got " + profileAuxObject); // NOI18N
            return;
        }
        RunProfile p = (RunProfile)profileAuxObject;
        setDefault(p.isDefault());
        setArgs(p.getArgsFlat());
        setBaseDir(p.getBaseDir());
        setRunDir(p.getRunDir());
        //setRawRunDirectory(p.getRawRunDirectory());
        setBuildFirst(p.getBuildFirst());
        setEnvironment(p.getEnvironment());
    }
    
    public RunProfile cloneProfile() {
        return (RunProfile)clone();
    }
    
    /**
     * Clones the profile.
     * All fields are cloned except for 'parent'.
     */
    public Object clone() {
        RunProfile p = new RunProfile(getBaseDir());
        //p.setParent(getParent());
        p.setCloneOf(this);
        p.setDefault(isDefault());
        p.setArgs(getArgsFlat());
        p.setRunDir(getRunDir());
        //p.setRawRunDirectory(getRawRunDirectory());
        p.setBuildFirst(getBuildFirst());
        p.setEnvironment(getEnvironment().cloneEnv());
        return p;
    }
    
    public Sheet getSheet() {
        return createSheet();
    }
    
    private Sheet createSheet() {
        Sheet sheet = new Sheet();
        
        Sheet.Set set = new Sheet.Set();
        set.setName("Action");
        set.setDisplayName("Action");
        set.setShortDescription("Action");
        set.put(new ArgumentsNodeProp());
        set.put(new RunDirectoryNodeProp());
        set.put(new EnvNodeProp());
        set.put(new BuildFirstNodeProp());
        sheet.put(set);
        
        return sheet;
    }
    
    private class ArgumentsNodeProp extends PropertySupport {
        public ArgumentsNodeProp() {
            super("Arguments", String.class, "Arguments", "Arguments", true, true);
        }
        
        public Object getValue() {
            return getArgsFlat();
        }
        
        public void setValue(Object v) {
            setArgs((String)v);
        }
    }
    
    private class RunDirectoryNodeProp extends PropertySupport {
        public RunDirectoryNodeProp() {
            super("Run Directory", String.class, "Run Directory", "Run Directory", true, true);
        }
        
        public Object getValue() {
            return getRunDir();
        }
        
        public void setValue(Object v) {
            String path = IpeUtils.toAbsoluteOrRelativePath(getBaseDir(), (String)v);
            path = FilePathAdaptor.mapToRemote(path);
            path = FilePathAdaptor.normalize(path);
            setRunDir(path);
        }
        
        public PropertyEditor getPropertyEditor() {
            String seed;
            if (IpeUtils.isPathAbsolute(getRunDir()))
                seed = getRunDir();
            else
                seed = getBaseDir() + File.separatorChar + getRunDir();
            return new DirEditor(seed);
        }
    }
    
    private class DirEditor extends PropertyEditorSupport implements ExPropertyEditor {
        private PropertyEnv propenv;
        private String seed;
        
        public DirEditor(String seed) {
            this.seed = seed;
        }
        
        public void setAsText(String text) {
            setRunDir(text);
        }
        
        public String getAsText() {
            return getRunDir();
        }
        
        public Object getValue() {
            return getRunDir();
        }
        
        public void setValue(Object v) {
            setRunDir((String)v);
        }
        
        public boolean supportsCustomEditor() {
            return true;
        }
        
        public java.awt.Component getCustomEditor() {
            return new DirPanel(seed, this, propenv);
        }
        
        public void attachEnv(PropertyEnv propenv) {
            this.propenv = propenv;
        }
    }
    
    class DirPanel extends FileChooser implements PropertyChangeListener {
        PropertyEditorSupport editor;
        
        public DirPanel(String seed, PropertyEditorSupport editor, PropertyEnv propenv) {
            super(
		java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("Run_Directory"),
		java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("SelectLabel"),
		FileChooser.DIRECTORIES_ONLY,
		null,
		seed,
		true
		);
            setControlButtonsAreShown(false);
            
            this.editor = editor;
            
            propenv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            propenv.addPropertyChangeListener(this);
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID) {
                File file = getSelectedFile();
                editor.setValue(file.getPath());
            }
        }
    }
    
    private class BuildFirstNodeProp extends PropertySupport {
        public BuildFirstNodeProp() {
            super("Build First", Boolean.class, "Build First", "Build First", true, true);
        }
        
        public Object getValue() {
            return new Boolean(getBuildFirst());
        }
        
        public void setValue(Object v) {
            setBuildFirst(((Boolean)v).booleanValue());
        }
    }
    
    private class EnvNodeProp extends PropertySupport {
        public EnvNodeProp() {
            super("Environment", Env.class, "Environment", "Environment", true, true);
        }
        
        public Object getValue() {
            return getEnvironment();
        }
        
        public void setValue(Object v) {
            getEnvironment().assign((Env)v);
        }
        
        public PropertyEditor getPropertyEditor() {
            return new EnvEditor(getEnvironment().cloneEnv());
        }
        
        public Object getValue(String attributeName) {
            if (attributeName.equals("canEditAsText"))
                return Boolean.FALSE;
            return super.getValue(attributeName);
        }
    }
    
    private class EnvEditor extends PropertyEditorSupport implements ExPropertyEditor {
        private Env env;
        private PropertyEnv propenv;
        
        public EnvEditor(Env env) {
            this.env = env;
        }
        
        public void setAsText(String text) {
        }
        
        public String getAsText() {
            return env.toString();
        }
        
        public java.awt.Component getCustomEditor() {
            return new EnvPanel(env, this, propenv);
        }
        
        public boolean supportsCustomEditor() {
            return true;
        }
        
        public void attachEnv(PropertyEnv propenv) {
            this.propenv = propenv;
        }
    }
}
