/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.debugger.gdb.profiles;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import org.netbeans.modules.cnd.actions.BuildToolsAction;

import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.utils.Path;
import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;

import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationAuxObject;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoder;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.makeproject.api.configurations.CompilerSet2Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.settings.CppSettings;
import org.netbeans.modules.cnd.ui.options.LocalToolsPanelModel;
import org.netbeans.modules.cnd.ui.options.ToolsPanelModel;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

public class GdbProfile implements ConfigurationAuxObject {

    public static final String GDB_PROFILE_ID = "gdbdebugger"; // NOI18N
    
    public static final String PROP_GDB_COMMAND = "gdb_command"; // NOI18N
    public static final String PROP_ARRAY_REPEAT_THRESHOLD = "array_repeat_threshold"; // NOI18N

    private PropertyChangeSupport pcs = null;

    private boolean needSave = false;
    
    private String gdb_command;


    /**
     * Constructor
     * Don't call this directly. It will get called when creating
     * ...cnd.execution.profiles.Profile().
     */
    public GdbProfile() {
        initialize();
    }

    protected GdbProfile(PropertyChangeSupport pcs) {
	this.pcs = pcs;
        initialize();
    }
    
    public void initialize() {
        if (gdb_command == null) {
            if (GdbDebugger.isUnitTest()) {
                gdb_command = "gdb"; // NOI18N
            } else {
                gdb_command = CppSettings.getDefault().getGdbName();
            }
        }
    }

    public boolean shared() {
	return false;
    }
    
    /**
     * Returns a unique id (String) used to retrive this object from the
     * pool of aux objects.
     */
    public String getId() {
        return GDB_PROFILE_ID;
    }
    
    public String getGdbCommand() {
        return gdb_command;
    }
    
    public void setGdbCommand(String gdb_command) {
        int l = gdb_command.length();
        boolean b = this.gdb_command.equals(gdb_command);
        
        if (gdb_command.length() > 0 && !this.gdb_command.equals(gdb_command)) {
            if (pcs != null) {
                pcs.firePropertyChange(PROP_GDB_COMMAND, this.gdb_command, gdb_command);
            }
            this.gdb_command = gdb_command;
        }
    }
    
    public String getGdbPath(MakeConfiguration conf, boolean canAskUser) {
        if (getClass().getName().contains("gd" + "b2")) { // NOI18N - Debugging code only...
            return "/usr/local/bin/gd" + "b2"; // NOI18N - DEBUG tool for debugger co-existance development
        }
        CompilerSet2Configuration csconf = conf.getCompilerSet();
        CompilerSet cs;
        String csname;
        
        if (csconf.isValid()) {
            csname = csconf.getOption();
            cs = CompilerSetManager.getDefault(conf.getDevelopmentHost().getExecutionEnvironment()).getCompilerSet(csname);
        } else {
            csname = csconf.getOldName();
            cs = CompilerSet.getCompilerSet(conf.getDevelopmentHost().getExecutionEnvironment(), csname, conf.getPlatformInfo().getPlatform());
            CompilerSetManager.getDefault(conf.getDevelopmentHost().getExecutionEnvironment()).add(cs);
            csconf.setValid();
        }
        Tool debuggerTool = cs.getTool(Tool.DebuggerTool);
        ExecutionEnvironment execEnv = null;
        if (debuggerTool != null) {
            String gdbPath = debuggerTool.getPath();
            execEnv = conf.getDevelopmentHost().getExecutionEnvironment();
            if (execEnv.isLocal()) {
                File gdbFile = new File(gdbPath);
                if (gdbFile.exists() && !gdbFile.isDirectory()) {
                    return gdbPath;
                }
                
                // Try from user's PATH (if user specified just debugger name (gdb) in tools setup)
                String fromUsersPath = Path.findCommand(gdbPath);
                if (fromUsersPath != null) {
                    return fromUsersPath;
                }
            } else {
                // Remote gdb...
                if (ServerList.isValidExecutable(execEnv, gdbPath)) {
                    return gdbPath;
                }
            }
        }
        if (canAskUser) {
            // No debugger in cs and non-absolute name in project. So post a Build Tools window and
            // force the user to add a directory with gdb or cancel
            ToolsPanelModel model = new LocalToolsPanelModel();
//            model.setGdbName(name);
//            model.setGdbEnabled(true);
            model.setCRequired(false);
            model.setCppRequired(false);
            model.setFortranRequired(false);
            model.setMakeRequired(false);
            model.setDebuggerRequired(true);
            model.setShowRequiredBuildTools(false);
            model.setShowRequiredDebugTools(true);
            model.setCompilerSetName(null); // means don't change
            model.setSelectedCompilerSetName(csname);
            model.setSelectedDevelopmentHost(execEnv);
            model.setEnableDevelopmentHostChange(false);
            BuildToolsAction bt = SystemAction.get(BuildToolsAction.class);
            bt.setTitle(NbBundle.getMessage(GdbProfile.class, "LBL_ResolveMissingGdb_Title")); // NOI18N
            if (bt.initBuildTools(model, new ArrayList<String>())) {
//                if (!name.equals(model.getGdbName())) {
//                    setGdbCommand(model.getGdbName());
//                }
                conf.getCompilerSet().setValue(model.getSelectedCompilerSetName());
                cs = CompilerSetManager.getDefault(conf.getDevelopmentHost().getExecutionEnvironment()).getCompilerSet(model.getSelectedCompilerSetName());
                return cs.getTool(Tool.DebuggerTool).getPath();
            }
        }
        return null;
    }
    
//    /**
//     * Find the path to gdb. Start with the name/path stored in the project. If that doesn't resolve,
//     * bring up a Build Tools window. If that doesn't resolve then return null (at which point the
//     * debug action will be terminated).
//     *
//     * @param ev What we need to get the GdbProfile
//     * @return Either an absolute path to gdb or null
//     */
//    public String getGdbPath(String name, String dir) {
//        File file;
//        
//        if (name.charAt(0) == '.') {
//            file = new File(dir, name);
//            if (file.exists()) {
//                return file.getAbsolutePath();
//            }
//        } else if ((Utilities.isUnix() && name.charAt(0) == '/') ||
//                (Utilities.isWindows() && name.charAt(1) == ':')) {
//            file = new File(name);
//            if (file.exists()) {
//                return file.getAbsolutePath();
//            }
//        } else {
//            StringTokenizer tok = new StringTokenizer(Path.getPathAsString(), File.pathSeparator);
//            while (tok.hasMoreTokens()) {
//                String d = tok.nextToken();
//                file = new File(d, name);
//                if (file.exists()) {
//                    return file.getAbsolutePath();
//                }
//                if (Utilities.isWindows()) {
//                    file = new File(d, name + ".exe"); // NOI18N
//                    if (file.exists()) {
//                        return file.getAbsolutePath();
//                    }
//                }
//            }
//        }
//        
//        // No gdb in $PATH and non-absolute name in project. So post a Build Tools window and
//        // force the user to add a directory with gdb or cancel
//        ToolsPanelModel model = new LocalToolsPanelModel();
////        model.setGdbName(name);
//        model.setGdbRequired(true);
//        model.setGdbEnabled(true);
//        model.setCRequired(false);
//        model.setCppRequired(false);
//        model.setFortranRequired(false);
//        BuildToolsAction bt = (BuildToolsAction) SystemAction.get(BuildToolsAction.class);
//        bt.setTitle(NbBundle.getMessage(GdbProfile.class, "LBL_ResolveMissingGdb_Title")); // NOI18N
//        if (bt.initBuildTools(model, new ArrayList())) {
////            if (!name.equals(model.getGdbName())) {
////                setGdbCommand(model.getGdbName());
////            }
//            return model.getGdbPath();
//        } else {
//            return null;
//        }
//    }
    
    public int getArrayRepeatThreshold() {
        return CppSettings.getDefault().getArrayRepeatThreshold(); 
    }
    
    public void setArrayRepeatThreshold(int arrayRepeatThreshold) {
        CppSettings.getDefault().setArrayRepeatThreshold(arrayRepeatThreshold);
    }
    
    /**
     *  Adds property change listener.
     *  @param l new listener.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (pcs != null) {
            pcs.addPropertyChangeListener(l);
        }
    }
    
    /**
     *  Removes property change listener.
     *  @param l removed listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (pcs != null) {
            pcs.removePropertyChangeListener(l);
        }
    }

    //
    // XML codec support
    // This stuff ends up in <projectdir>/nbproject/private/profiles.xml
    // 

    public XMLDecoder getXMLDecoder() {
	return new GdbProfileXMLCodec(this);
    }

    public XMLEncoder getXMLEncoder() {
	return new GdbProfileXMLCodec(this);
    }

    // interface ProfileAuxObject
    public boolean hasChanged() {
	return needSave;
    }

    // interface ProfileAuxObject
    public void clearChanged() {
	needSave = false;
    }


    // interface ProfileAuxObject
    /**
     * Assign all values from a profileAuxObject to this object (reverse
     * of clone)
     */
    public void assign(ConfigurationAuxObject profileAuxObject) {
	assert profileAuxObject instanceof GdbProfile;

	GdbProfile that = (GdbProfile) profileAuxObject;

	this.setGdbCommand(that.getGdbCommand());
    }

    

    // interface ProfileAuxObject
    /**
     * Clone itself to an identical (deep) copy.
     */
    @Override
    public GdbProfile clone(Configuration conf) {
	GdbProfile p = new GdbProfile();

	p.setGdbCommand(getGdbCommand());

	return p;
    }

    public Sheet getSheet() {
	Sheet sheet = new Sheet();
	Sheet.Set set = new Sheet.Set();
	set.setName("General"); // NOI18N
	set.setDisplayName(NbBundle.getMessage(GdbProfile.class, "LBL_GENERAL")); // NOI18N
	set.setShortDescription(NbBundle.getMessage(GdbProfile.class, "HINT_GENERAL")); // NOI18N
	set.put(new GdbCommandNodeProp());
        set.put(new ArrayRepeatThresholdNodeProp());
	sheet.put(set);
	return sheet;
    }
    
    private class GdbCommandNodeProp extends PropertySupport<String> {
        public GdbCommandNodeProp() {
            super(PROP_GDB_COMMAND, String.class,
                    NbBundle.getMessage(GdbProfile.class, "LBL_GDB_COMMAND"), // NOI18N
                    NbBundle.getMessage(GdbProfile.class, "HINT_GDB_COMMAND"), // NOI18N
                    true, false);
        }
        
        public String getValue() {
            return getGdbCommand();
        }
        
        public void setValue(String v) {
            // TODO: shouldn't we check for null here?
            setGdbCommand(v);
        }
    }
    
    private class ArrayRepeatThresholdNodeProp extends PropertySupport<Integer> {
        public ArrayRepeatThresholdNodeProp() {
            super(PROP_ARRAY_REPEAT_THRESHOLD, Integer.class,
                    NbBundle.getMessage(GdbProfile.class, "LBL_ArrayRepeatThreshold"), // NOI18N
                    NbBundle.getMessage(GdbProfile.class, "HINT_ArrayRepeatThreshold"), // NOI18N
                    true, true);
        }
        
        public Integer getValue() {
            return getArrayRepeatThreshold();
        }
        
        public void setValue(Integer v) {
            // TODO: why do we need to check it here?
            if (v != null) {
                setArrayRepeatThreshold(v);
            }
        }
    }
}
