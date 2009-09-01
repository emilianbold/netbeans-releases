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

package org.netbeans.modules.cnd.actions;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.execution.ExecutionListener;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.execution.ShellExecSupport;
import org.netbeans.modules.cnd.loaders.ShellDataObject;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.NativeProcessChangeEvent;
import org.openide.LifecycleManager;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * Base class for Make Actions ...
 */
public class ShellRunAction extends AbstractExecutorRunAction {
    private static final boolean TRACE = false;

    public String getName() {
        return getString("BTN_Run"); // NOI18N
    }

    @Override
    protected boolean accept(DataObject object) {
        return object instanceof ShellDataObject;
    }

    protected void performAction(Node[] activatedNodes) {
        // Save everything first
        LifecycleManager.getDefault().saveAll();

        for (int i = 0; i < activatedNodes.length; i++) {
            performAction(activatedNodes[i]);
        }
    }


    public static void performAction(Node node) {
        performAction(node, null, null, null);
    }

    public static void performAction(Node node, final ExecutionListener listener, final Writer outputListener, Project project) {
        ShellExecSupport bes = node.getCookie(ShellExecSupport.class);
        if (bes == null) {
            return;
        }
        //Save file
        SaveCookie save = node.getLookup().lookup(SaveCookie.class);
        if (save != null) {
            try {
                save.save();
            } catch (IOException ex) {
            }
        }
        DataObject dataObject = node.getCookie(DataObject.class);
        FileObject fileObject = dataObject.getPrimaryFile();
        
        File shellFile = FileUtil.toFile(fileObject);
        // Build directory
        String bdir = bes.getRunDirectory();
        File buildDir = getAbsoluteBuildDir(bdir, shellFile);
        // Tab Name
        String tabName = getString("RUN_LABEL", node.getName()); // NOI18N
        
        String[] shellCommandAndArgs = bes.getShellCommandAndArgs(fileObject); // from inside shell file or properties
        String shellCommand = shellCommandAndArgs[0];
        String shellFilePath = IpeUtils.toRelativePath(buildDir.getPath(), shellFile.getPath()); // Absolute path to shell file
        if (shellFilePath.equals(shellFile.getName())) {
            shellFilePath = "."+File.separatorChar+shellFilePath; //NOI18N
        }
        String[] args = bes.getArguments(); // from properties

        ExecutionEnvironment execEnv = getExecutionEnvironment(fileObject, project);
        // Windows: The command is usually of the from "/bin/sh", but this
        // doesn't work here, so extract the 'sh' part and use that instead. 
        // FIXUP: This is not entirely correct though.
        if (PlatformInfo.getDefault(execEnv).isWindows() && shellCommand.length() > 0) {
            int i = shellCommand.lastIndexOf("/"); // UNIX PATH // NOI18N
            if (i >= 0) {
                shellCommand = shellCommand.substring(i+1);
            }
        }
        
        StringBuilder argsFlat = new StringBuilder();
        if (shellCommandAndArgs[0].length() > 0) {
            for (int i = 1; i < shellCommandAndArgs.length; i++) {
                argsFlat.append(" "); // NOI18N
                argsFlat.append(shellCommandAndArgs[i]);
            }
        }
        argsFlat.append(shellFilePath);
        for (int i = 0; i < args.length; i++) {
            argsFlat.append(" "); // NOI18N
            argsFlat.append(args[i]);
        }

        String[] additionalEnvironment = getAdditionalEnvirounment(node);
        String[] env = prepareEnv(execEnv);
        if (additionalEnvironment != null && additionalEnvironment.length>0){
            String[] tmp = new String[env.length + additionalEnvironment.length];
            for(int i=0; i < env.length; i++){
                tmp[i] = env[i];
            }
            for(int i=0; i < additionalEnvironment.length; i++){
                tmp[env.length + i] = additionalEnvironment[i];
            }
            env = tmp;
        }
        Map<String, String> envMap = new HashMap<String, String>();
        for(String s: env) {
            int i = s.indexOf('='); // NOI18N
            if (i>0) {
                String key = s.substring(0, i);
                String value = s.substring(i+1);
                envMap.put(key, value);
            }
        }
       
        if (TRACE) {
            System.err.println("Run "+shellCommand); // NOI18N
            System.err.println("\tin folder   "+buildDir.getPath()); // NOI18N
            System.err.println("\targuments   "+argsFlat); // NOI18N
            System.err.println("\tenvironment "); // NOI18N
            for(String v : env) {
                System.err.println("\t\t"+v); // NOI18N
            }
        }
        InputOutput _tab = IOProvider.getDefault().getIO(tabName, false); // This will (sometimes!) find an existing one.
        _tab.closeInputOutput(); // Close it...
        final InputOutput tab = IOProvider.getDefault().getIO(tabName, true); // Create a new ...
        try {
            tab.getOut().reset();
        } catch (IOException ioe) {
        }
        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv)
        .setWorkingDirectory(buildDir.getPath())
        .setCommandLine(quoteExecutable(shellCommand)+" "+argsFlat.toString()) // NOI18N
        .unbufferOutput(false)
        .addEnvironmentVariables(envMap)
        .addNativeProcessListener(new ChangeListener() {
           private long startTimeMillis;
           public void stateChanged(ChangeEvent e) {
                if (!(e instanceof NativeProcessChangeEvent)) {
                    return;
                }
                NativeProcessChangeEvent event = (NativeProcessChangeEvent) e;
                NativeProcess process = (NativeProcess) event.getSource();
                switch (event.state) {
                    case INITIAL:
                        break;
                    case STARTING:
                        startTimeMillis = System.currentTimeMillis();
                        if (listener != null) {
                            listener.executionStarted(event.pid);
                        }
                        break;
                    case RUNNING:
                        break;
                    case CANCELLED:
                    {
                        if (listener != null) {
                            listener.executionFinished(process.exitValue());
                        }
                        String message = getString("Output.RunTerminated", formatTime(System.currentTimeMillis() - startTimeMillis)); // NOI18N
                        tab.getOut().println();
                        tab.getOut().println(message);
                        tab.getOut().flush();
                        break;
                    }
                    case ERROR:
                    {
                        if (listener != null) {
                            listener.executionFinished(-1);
                        }
                        String message = getString("Output.RunFailedToStart"); // NOI18N
                        tab.getOut().println();
                        tab.getOut().println(message);
                        tab.getOut().flush();
                        break;
                    }
                    case FINISHED:
                    {
                        if (listener != null) {
                            listener.executionFinished(process.exitValue());
                        }
                        String message;
                        if (process.exitValue() != 0) {
                            message = getString("Output.RunFailed", ""+process.exitValue(), formatTime(System.currentTimeMillis() - startTimeMillis)); // NOI18N
                        } else {
                            message = getString("Output.RunSuccessful", formatTime(System.currentTimeMillis() - startTimeMillis)); // NOI18N
                        }
                        tab.getOut().println();
                        tab.getOut().println(message);
                        tab.getOut().flush();
                        break;
                    }
                }
            }
        });
        npb.redirectError();

        ExecutionDescriptor descr = new ExecutionDescriptor()
        .controllable(true)
        .frontWindow(true)
        .inputVisible(true)
        .inputOutput(tab)
        .outLineBased(true)
        .showProgress(true)
        .outConvertorFactory(new ExecutionDescriptor.LineConvertorFactory() {
            public LineConvertor newLineConvertor() {
                return new LineConvertor() {
                    @Override
                    public List<ConvertedLine> convert(String line) {
                        if (outputListener != null) {
                            try {
                                outputListener.write(line);
                                outputListener.write("\n"); // NOI18N
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                        return null;
                    }
                };
            }
        });
        // Execute the shellfile
        final ExecutionService es = ExecutionService.newService(npb, descr, "Run"); // NOI18N
        Future<Integer> result = es.run();
    }
    
}
