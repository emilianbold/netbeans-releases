/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.discovery.projectimport;

import static java.util.logging.Logger.getLogger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.actions.CMakeAction;
import org.netbeans.modules.cnd.actions.MakeAction;
import org.netbeans.modules.cnd.actions.QMakeAction;
import org.netbeans.modules.cnd.actions.ShellRunAction;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.netbeans.modules.cnd.builds.ImportUtils;
import org.netbeans.modules.cnd.execution.ShellExecSupport;
import org.netbeans.modules.cnd.execution.ExecutionSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.CompilerSet2Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;
import org.openide.windows.InputOutput;

/**
 *
 * @author Alexander Simon
 */
public class ReconfigureProject {
    private static boolean TRACE = Boolean.getBoolean("cnd.discovery.trace.projectimport"); // NOI18N
    private static final Logger logger = getLogger("org.netbeans.modules.cnd.discovery.projectimport.ImportProject"); // NOI18N
    private static final RequestProcessor RP = new RequestProcessor(ReconfigureProject.class.getName(), 1);
    private final Project makeProject;
    private final ConfigurationDescriptorProvider pdp;
    private final boolean isSunCompiler;
    private CompilerSet compilerSet;
    private final int platform;
    private DataObject configure;
    private DataObject cmake;
    private DataObject qmake;
    private DataObject make;
    private String cFlags;
    private String cxxFlags;
    private String linkerFlags;
    private AtomicBoolean canceled = new AtomicBoolean(false);
    private Future<Integer> lastTask;
    private Set<ExecutionListener> listeners = new WeakSet<ExecutionListener>();
    private InputOutput tab;

    public ReconfigureProject(Project makeProject){
        if (TRACE) {
            logger.setLevel(Level.ALL);
        }
        this.makeProject = makeProject;
        pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        assert pdp != null && pdp.gotDescriptor();
        MakeConfiguration configuration = pdp.getConfigurationDescriptor().getActiveConfiguration();
        assert configuration != null && configuration.getConfigurationType().getValue() ==  MakeConfiguration.TYPE_MAKEFILE;
        CompilerSet2Configuration set = configuration.getCompilerSet();
        compilerSet = set.getCompilerSet();
        assert compilerSet != null;
        isSunCompiler = compilerSet.getCompilerFlavor().isSunStudioCompiler();
        Folder important = pdp.getConfigurationDescriptor().getExternalFileItems();
        for(Item item : important.getAllItemsAsArray()){
            DataObject dao = item.getDataObject();
            if (dao != null) {
                String mime = dao.getPrimaryFile().getMIMEType();
                if (MIMENames.SHELL_MIME_TYPE.equals(mime)){
                    if ("configure".equals(dao.getPrimaryFile().getName())){ // NOI18N
                        configure = dao;
                    }
                } else if (MIMENames.CMAKE_MIME_TYPE.equals(mime)){
                    cmake = dao;
                } else if (MIMENames.QTPROJECT_MIME_TYPE.equals(mime)){
                    qmake = dao;
                } else if (MIMENames.MAKEFILE_MIME_TYPE.equals(mime)){
                    if (dao.getPrimaryFile().hasExt("mk")) { // NOI18N
                        if (make == null) {
                            make = dao;
                        }
                    } else {
                        make = dao;
                    }
                }
            }
        }
        platform = configuration.getDevelopmentHost().getBuildPlatform();
    }

    public void addExecutionListener(ExecutionListener listener){
        listeners.add(listener);
    }

    public void removeExecutionListener(ExecutionListener listener){
        listeners.remove(listener);
    }

    private String escapeFlags(String flags) {
        if ((flags.indexOf(' ') > 0 || flags.indexOf('=') > 0)&& !flags.startsWith("\"")) { // NOI18N
            flags = "\""+flags+"\""; // NOI18N
        }
        return flags;
    }

    public void reconfigure(final String cFlags, final String cxxFlags, final String linkerFlags, final InputOutput io){
        if (SwingUtilities.isEventDispatchThread()){
            RP.post(new Runnable() {
                @Override
                public void run() {
                    reconfigure(cFlags, cxxFlags, linkerFlags, getRestOptions(), true, io);
                }
            });
        } else {
            reconfigure(cFlags, cxxFlags, linkerFlags, getRestOptions(), true, io);
        }
    }

    public void reconfigure(String cFlags, String cxxFlags, String linkerFlags, String otherOptions, boolean waitFinished, final InputOutput io){
        tab = io;
        if (waitFinished) {
            final AtomicInteger res = new AtomicInteger();
            final AtomicBoolean finished = new AtomicBoolean(false);
            ExecutionListener listener = new ExecutionListener() {
                @Override
                public void executionStarted(int pid) {
                }
                @Override
                public void executionFinished(int rc) {
                    res.set(rc);
                    finished.set(true);
                    synchronized(finished) {
                        finished.notifyAll();
                    }
                }
            };
            addExecutionListener(listener);
            _reconfigure(cFlags, cxxFlags, linkerFlags, otherOptions);
            synchronized(finished) {
                while(true) {
                    try {
                        finished.wait();
                        if (finished.get()) {
                            return;
                        }
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        } else {
            _reconfigure(cFlags, cxxFlags, linkerFlags, otherOptions);
        }
    }

    private void _reconfigure(String cFlags, String cxxFlags, String linkerFlags, String otherOptions){
        cFlags = escapeFlags(cFlags);
        cxxFlags = escapeFlags(cxxFlags);
        linkerFlags = escapeFlags(linkerFlags);
        this.cFlags = cFlags;
        this.cxxFlags = cxxFlags;
        this.linkerFlags = linkerFlags;
        if (cmake != null && make != null) {
            String arguments = getConfigureArguments(cmake.getPrimaryFile().getPath(), otherOptions, cFlags, cxxFlags, linkerFlags, isSunCompiler());
            ExecutionSupport ses = cmake.getNodeDelegate().getCookie(ExecutionSupport.class);
            if (ses != null) {
                try {
                    List<String> vars = ImportUtils.parseEnvironment(arguments);
                    for (String s : ImportUtils.quoteList(vars)) {
                        int i = arguments.indexOf(s);
                        if (i >= 0){
                            arguments = arguments.substring(0, i) + arguments.substring(i + s.length());
                        }
                    }
                    ses.setArguments(new String[]{arguments});
                    ses.setEnvironmentVariables(vars.toArray(new String[vars.size()]));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            ExecutionListener listener = new ExecutionListener() {
                @Override
                public void executionStarted(int pid) {
                    for(ExecutionListener listener : listeners){
                        listener.executionStarted(pid);
                    }
                }
                @Override
                public void executionFinished(int rc) {
                    if (rc == 0) {
                        postClean(false);
                    } else {
                        for(ExecutionListener listener : listeners){
                            listener.executionFinished(rc);
                        }
                    }
                }
            };
            if (TRACE) {
                logger.log(Level.INFO, "#{0} {1}", new Object[]{cmake.getPrimaryFile().getPath(), arguments}); // NOI18N
            }
            if (canceled.get()) {
                listener.executionFinished(-1);
            } else {
                lastTask = CMakeAction.performAction(cmake.getNodeDelegate(), listener, null, makeProject, tab);
            }
        } else if (qmake != null && make != null){
            String arguments = getConfigureArguments(qmake.getPrimaryFile().getPath(), otherOptions, cFlags, cxxFlags, linkerFlags, isSunCompiler());
            ExecutionSupport ses = qmake.getNodeDelegate().getCookie(ExecutionSupport.class);
            if (ses != null) {
                try {
                    ses.setArguments(new String[]{arguments});
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            ExecutionListener listener = new ExecutionListener() {
                @Override
                public void executionStarted(int pid) {
                    for(ExecutionListener listener : listeners){
                        listener.executionStarted(pid);
                    }
                }
                @Override
                public void executionFinished(int rc) {
                    if (rc == 0) {
                        postClean(false);
                    } else {
                        for(ExecutionListener listener : listeners){
                            listener.executionFinished(rc);
                        }
                    }
                }
            };
            if (TRACE) {
                logger.log(Level.INFO, "#{0} {1}", new Object[]{qmake.getPrimaryFile().getPath(), arguments}); // NOI18N
            }
            if (canceled.get()) {
                listener.executionFinished(-1);
            } else {
                lastTask = QMakeAction.performAction(qmake.getNodeDelegate(), listener, null, makeProject, tab);
            }
        } else if (configure != null && make != null) {
            String arguments = getConfigureArguments(configure.getPrimaryFile().getPath(), otherOptions, cFlags, cxxFlags, linkerFlags, isSunCompiler());
            ShellExecSupport ses = configure.getNodeDelegate().getCookie(ShellExecSupport.class);
            if (ses != null) {
                try {
                    ses.setArguments(new String[]{arguments});
                    List<String> vars = ImportUtils.parseEnvironment(arguments);
                    ses.setEnvironmentVariables(vars.toArray(new String[vars.size()]));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            ExecutionListener listener = new ExecutionListener() {
                @Override
                public void executionStarted(int pid) {
                    for(ExecutionListener listener : listeners){
                        listener.executionStarted(pid);
                    }
                }
                @Override
                public void executionFinished(int rc) {
                    if (rc == 0) {
                        postClean(false);
                    } else {
                        for(ExecutionListener listener : listeners){
                            listener.executionFinished(rc);
                        }
                    }
                }
            };
            if (TRACE) {
                logger.log(Level.INFO, "#{0} {1}", new Object[]{configure.getPrimaryFile().getPath(), arguments}); // NOI18N
            }
            if (canceled.get()) {
                listener.executionFinished(-1);
            } else {
                lastTask = ShellRunAction.performAction(configure.getNodeDelegate(), listener, null, makeProject, tab);
            }
        } else if (make != null) {
            postClean(true);
        } else {
            assert false;
        }
    }

    private void postClean(final boolean notifyStart) {
        ExecutionListener listener = new ExecutionListener() {
            @Override
            public void executionStarted(int pid) {
                if (notifyStart) {
                    for(ExecutionListener listener : listeners){
                        listener.executionStarted(pid);
                    }
                }
            }
            @Override
            public void executionFinished(int rc) {
                postMake();
            }
        };
        if (TRACE) {
            logger.log(Level.INFO, "#make -f {0} clean", make.getPrimaryFile().getPath()); // NOI18N
        }
        if (canceled.get()) {
            listener.executionFinished(-1);
        } else {
            lastTask = MakeAction.execute(make.getNodeDelegate(), "clean", listener, null, makeProject, null, tab); // NOI18N
        }
    }

    private void postMake(){
        String arguments = getConfigureArguments(make.getPrimaryFile().getPath(), null, cFlags, cxxFlags, linkerFlags, isSunCompiler());
        if (TRACE) {
            logger.log(Level.INFO, "#make -f {0}", make.getPrimaryFile().getPath()); // NOI18N
        }
        Node node = make.getNodeDelegate();
        ExecutionSupport ses = node.getCookie(ExecutionSupport.class);
        List<String> vars = ImportUtils.parseEnvironment(arguments);
        if (ses != null) {
            try {
                ses.setEnvironmentVariables(vars.toArray(new String[vars.size()]));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        ExecutionListener listener = new ExecutionListener() {
            @Override
            public void executionStarted(int pid) {
            }
            @Override
            public void executionFinished(int rc) {
                for(ExecutionListener listener : listeners){
                    listener.executionFinished(rc);
                }
            }
        };
        if (canceled.get()) {
            listener.executionFinished(-1);
        } else {
            lastTask = MakeAction.execute(node, "", listener, null, makeProject,vars, tab); // NOI18N
        }
    }

    public void cancel() {
        canceled.set(true);
        Future<Integer> task = lastTask;
        if (task != null) {
            task.cancel(true);
        }
    }

    private String getConfigureArguments(String configure, String otherOptions, String cCompilerFlags, String cppCompilerFlags, String ldFlags, boolean isSunCompiler) {
        StringBuilder buf = new StringBuilder();
        if (otherOptions != null && otherOptions.length() > 0){
            buf.append(otherOptions);
            buf.append(' ');
        }
        if (configure.endsWith("CMakeLists.txt")){ // NOI18N
            buf.append(" -G \"Unix Makefiles\""); // NOI18N
            buf.append(" -DCMAKE_BUILD_TYPE=Debug"); // NOI18N
            buf.append(" -DCMAKE_C_COMPILER=").append(getCCompilerName()); // NOI18N
            buf.append(" -DCMAKE_CXX_COMPILER=").append(getCppCompilerName()); // NOI18N
            buf.append(" -DCMAKE_C_FLAGS_DEBUG=").append(cCompilerFlags); // NOI18N
            buf.append(" -DCMAKE_CXX_FLAGS_DEBUG=").append(cppCompilerFlags); // NOI18N
            buf.append(" -DCMAKE_EXE_LINKER_FLAGS_DEBUG=").append(ldFlags); // NOI18N
        } else if (configure.endsWith(".pro")){ // NOI18N
            if (isSunCompiler && (platform == PlatformTypes.PLATFORM_SOLARIS_INTEL || platform == PlatformTypes.PLATFORM_SOLARIS_SPARC)) {
                buf.append(" -spec solaris-cc"); // NOI18N
            }
            if (platform == PlatformTypes.PLATFORM_MACOSX) {
                buf.append(" -spec macx-g++"); // NOI18N
            }
            buf.append(" QMAKE_CC=").append(getCCompilerName()); // NOI18N
            buf.append(" QMAKE_CXX=").append(getCppCompilerName()); // NOI18N
            buf.append(" QMAKE_CFLAGS=").append(cCompilerFlags); // NOI18N
            buf.append(" QMAKE_CXXFLAGS=").append(cppCompilerFlags); // NOI18N
            buf.append(" QMAKE_LDFLAGS=").append(ldFlags); // NOI18N
        } else {
            buf.append(" CC=").append(getCCompilerName()); // NOI18N
            buf.append(" CXX=").append(getCppCompilerName()); // NOI18N
            buf.append(" CFLAGS=").append(cCompilerFlags); // NOI18N
            buf.append(" CXXFLAGS=").append(cppCompilerFlags); // NOI18N
            buf.append(" LDFLAGS=").append(ldFlags); // NOI18N
        }
        return buf.toString();
    }

    public CompilerOptions getLastCompilerOptions(){
        String lastFlags = getLastFlags();
        if (lastFlags == null) {
            return null;
        }
        DataObject dao = getImportant();
        if (dao == null) {
            return null;
        }
        String mime = dao.getPrimaryFile().getMIMEType();
        CompilerOptions options = new CompilerOptions();
        if (MIMENames.SHELL_MIME_TYPE.equals(mime)){
            options.CFlags = getFlags(lastFlags, "CFLAGS="); // NOI18N
            options.CppFlags = getFlags(lastFlags, "CXXFLAGS="); // NOI18N
            options.CCompiler = getFlags(lastFlags, "CC="); // NOI18N
            options.CppCompiler = getFlags(lastFlags, "CXX="); // NOI18N
            options.LinkerFlags = getFlags(lastFlags, "LDFLAGS="); // NOI18N
        } else if (MIMENames.CMAKE_MIME_TYPE.equals(mime)){
            options.CFlags = getFlags(lastFlags, "-DCMAKE_C_FLAGS_DEBUG="); // NOI18N
            options.CppFlags = getFlags(lastFlags, "-DCMAKE_CXX_FLAGS_DEBUG="); // NOI18N
            options.CCompiler = getFlags(lastFlags, "-DCMAKE_C_COMPILER="); // NOI18N
            options.CppCompiler = getFlags(lastFlags, "-DCMAKE_CXX_COMPILER="); // NOI18N
            options.LinkerFlags = getFlags(lastFlags, "-DCMAKE_EXE_LINKER_FLAGS_DEBUG="); // NOI18N
        } else if (MIMENames.QTPROJECT_MIME_TYPE.equals(mime)){
            options.CFlags = getFlags(lastFlags, "QMAKE_CFLAGS="); // NOI18N
            options.CppFlags = getFlags(lastFlags, "QMAKE_CXXFLAGS="); // NOI18N
            options.CCompiler = getFlags(lastFlags, "QMAKE_CC="); // NOI18N
            options.CppCompiler = getFlags(lastFlags, "QMAKE_CXX="); // NOI18N
            options.LinkerFlags = getFlags(lastFlags, "QMAKE_LDFLAGS="); // NOI18N
        } else if (MIMENames.MAKEFILE_MIME_TYPE.equals(mime)){
            options.CFlags = getFlags(lastFlags, "CFLAGS="); // NOI18N
            options.CppFlags = getFlags(lastFlags, "CXXFLAGS="); // NOI18N
            options.CCompiler = getFlags(lastFlags, "CC="); // NOI18N
            options.CppCompiler = getFlags(lastFlags, "CXX="); // NOI18N
            options.LinkerFlags = getFlags(lastFlags, "LDFLAGS="); // NOI18N
        }
        return options;
    }

    public String getRestOptions() {
        String lastFlags = getLastFlags();
        if (lastFlags == null) {
            return ""; // NOI18N
        }
        DataObject dao = getImportant();
        if (dao == null) {
            return ""; // NOI18N
        }
        String mime = dao.getPrimaryFile().getMIMEType();
        if (MIMENames.SHELL_MIME_TYPE.equals(mime)){
            lastFlags = removeFlag(lastFlags, "CFLAGS=", false); // NOI18N
            lastFlags = removeFlag(lastFlags, "CXXFLAGS=", false); // NOI18N
            lastFlags = removeFlag(lastFlags, "CC=", false); // NOI18N
            lastFlags = removeFlag(lastFlags, "CXX=", false); // NOI18N
            lastFlags = removeFlag(lastFlags, "LDFLAGS=", false); // NOI18N
        } else if (MIMENames.CMAKE_MIME_TYPE.equals(mime)){
            lastFlags = removeFlag(lastFlags, "-DCMAKE_BUILD_TYPE=", false); // NOI18N
            lastFlags = removeFlag(lastFlags, "-DCMAKE_C_FLAGS_DEBUG=", false); // NOI18N
            lastFlags = removeFlag(lastFlags, "-DCMAKE_CXX_FLAGS_DEBUG=", false); // NOI18N
            lastFlags = removeFlag(lastFlags, "-DCMAKE_C_COMPILER=", false); // NOI18N
            lastFlags = removeFlag(lastFlags, "-DCMAKE_CXX_COMPILER=", false); // NOI18N
            lastFlags = removeFlag(lastFlags, "-DCMAKE_EXE_LINKER_FLAGS_DEBUG=", false); // NOI18N
            lastFlags = removeFlag(lastFlags, "-G", false); // NOI18N
        } else if (MIMENames.QTPROJECT_MIME_TYPE.equals(mime)){
            lastFlags = removeFlag(lastFlags, "QMAKE_CFLAGS=", false); // NOI18N
            lastFlags = removeFlag(lastFlags, "QMAKE_CXXFLAGS=", false); // NOI18N
            lastFlags = removeFlag(lastFlags, "QMAKE_CC=", false); // NOI18N
            lastFlags = removeFlag(lastFlags, "QMAKE_CXX=", false); // NOI18N
            lastFlags = removeFlag(lastFlags, "QMAKE_LDFLAGS=", false); // NOI18N
            lastFlags = removeFlag(lastFlags, "-spec solaris-cc", true); // NOI18N
            lastFlags = removeFlag(lastFlags, "-spec macx-g++", true); // NOI18N
        } else if (MIMENames.MAKEFILE_MIME_TYPE.equals(mime)){
            return ""; // NOI18N
        }
        return lastFlags.trim();
    }

    private String removeFlag(String flags, String key, boolean noValue) {
        int i = flags.indexOf(key);
        if (i >= 0) {
            if (key.charAt(key.length()-1)=='=') { // NOI18N
                String rest = flags.substring(i+key.length());
                if (rest.startsWith("\"")){ // NOI18N
                    int j = rest.indexOf('"',1); // NOI18N
                    if (j > 0) {
                        return flags.substring(0,i)+rest.substring(j+1);
                    }
                } else {
                    int j = rest.indexOf(' ',1); // NOI18N
                    if (j > 0) {
                        return flags.substring(0,i)+rest.substring(j+1);
                    } else {
                        return flags.substring(0,i);
                    }
                }
            } else {
                String rest = flags.substring(i+key.length());
                if (rest.startsWith(" ")){ // NOI18N
                    if (noValue) {
                        return flags.substring(0,i)+rest;
                    }
                    rest = rest.substring(1);
                    if (rest.startsWith("\"")){ // NOI18N
                        int j = rest.indexOf('"',1); // NOI18N
                        if (j > 0) {
                            return flags.substring(0,i)+rest.substring(j+1);
                        }
                    } else {
                        int j = rest.indexOf(' ',1); // NOI18N
                        if (j > 0) {
                            return flags.substring(0,i)+rest.substring(j+1);
                        } else {
                            return flags.substring(0,i);
                        }
                    }
                } else if (rest.length()==0) {
                    return flags.substring(0,i);
                }
            }
        }
        return flags.trim();
    }

    private String getFlags(String flags, String key){
        int i = flags.indexOf(key);
        if (i >= 0) {
            if (key.charAt(key.length()-1)=='=') { // NOI18N
                String rest = flags.substring(i+key.length());
                if (rest.startsWith("\"")){ // NOI18N
                    int j = rest.indexOf('"',1); // NOI18N
                    if (j > 0) {
                        return rest.substring(1,j);
                    }
                } else {
                    int j = rest.indexOf(' ',1); // NOI18N
                    if (j > 0) {
                        return rest.substring(0,j);
                    } else {
                        return rest;
                    }
                }
            } else {
                String rest = flags.substring(i+key.length());
                if (rest.startsWith(" ")){ // NOI18N
                    rest = rest.substring(1);
                    if (rest.startsWith("\"")){ // NOI18N
                        int j = rest.indexOf('"',1); // NOI18N
                        if (j > 0) {
                            return rest.substring(1,j);
                        }
                    } else {
                        int j = rest.indexOf(' ',1); // NOI18N
                        if (j > 0) {
                            return rest.substring(0,j);
                        } else {
                            return rest;
                        }
                    }
                } else if (rest.length()==0) {
                    return ""; // NOI18N
                }
            }
        }
        return null;
    }

    /**
     * @return the isSunCompiler
     */
    public boolean isSunCompiler() {
        return isSunCompiler;
    }

    /**
     * @return compiler set
     */
    public CompilerSet getCompilerSet() {
        return compilerSet;
    }

    private String getCCompilerName(){
        String path = getToolPath(PredefinedToolKind.CCompiler);
        if (path == null) {
            if (isSunCompiler()) {
                return "cc"; // NOI18N
            } else {
                return "gcc"; // NOI18N
            }
        }
        return path;
    }

    private String getCppCompilerName(){
        String path = getToolPath(PredefinedToolKind.CCCompiler);
        if (path == null) {
            if (isSunCompiler()) {
                return "CC"; // NOI18N
            } else {
                return "g++"; // NOI18N
            }
        }
        return path;
    }

    private String getToolPath(PredefinedToolKind tool){
        Tool compiler = compilerSet.findTool(tool);
        if (compiler == null) {
            return null;
        }
        return escapeFlags(compiler.getPath());
    }

    public DataObject getImportant(){
        if (cmake != null && make != null) {
            return cmake;
        } else if (qmake != null && make != null){
            return qmake;
        } else if (configure != null && make != null) {
            return configure;
        } else if (make != null) {
            return make;
        }
        return null;
    }

    public String getLastFlags(){
        if (cmake != null && make != null) {
            ExecutionSupport ses = cmake.getNodeDelegate().getCookie(ExecutionSupport.class);
            if (ses != null) {
                String[] args = ses.getArguments();
                if (args != null && args.length > 0) {
                    return args[0];
                }
            }
        } else if (qmake != null && make != null){
            ExecutionSupport ses = qmake.getNodeDelegate().getCookie(ExecutionSupport.class);
            if (ses != null) {
                String[] args = ses.getArguments();
                if (args != null && args.length > 0) {
                    return args[0];
                }
            }
        } else if (configure != null && make != null) {
            ShellExecSupport ses = configure.getNodeDelegate().getCookie(ShellExecSupport.class);
            if (ses != null) {
                String[] args = ses.getArguments();
                if (args != null && args.length > 0) {
                    return args[0];
                }
            }
        } else if (make != null) {
            ExecutionSupport ses = make.getNodeDelegate().getCookie(ExecutionSupport.class);
            if (ses != null) {
                String[] args = ses.getEnvironmentVariables();
                if (args != null && args.length > 0) {
                    List<String> list = new ArrayList<String>();
                    for (int i = 0; i < args.length; i++) {
                        list.add(args[i]);
                    }
                    list = ImportUtils.quoteList(list);
                    StringBuilder b = new StringBuilder();
                    for (String s : list) {
                        b.append(s).append(' '); // NOI18N
                    }
                    return b.toString();
                }
            }
        }
        return null;
    }

    public static final class CompilerOptions {
        public String CFlags;
        public String CppFlags;
        public String CCompiler;
        public String CppCompiler;
        public String LinkerFlags;
    }
}
