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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.execution.ExecutionListener;
import org.netbeans.modules.cnd.api.execution.NativeExecutor;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.execution.ShellExecSupport;
import org.netbeans.modules.cnd.loaders.ShellDataObject;
import org.openide.LifecycleManager;
import org.openide.cookies.SaveCookie;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Cancellable;

/**
 * Base class for Make Actions ...
 */
public class ShellRunAction extends AbstractExecutorRunAction {

    public String getName() {
        return getString("BTN_Run");
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
        File buildDir;
        if (bdir.length() == 0 || bdir.equals(".")) { // NOI18N
            buildDir = shellFile.getParentFile();
        } else if (IpeUtils.isPathAbsolute(bdir)) {
            buildDir = new File(bdir);
        } else {
            buildDir = new File(shellFile.getParentFile(), bdir);
        }
        try {
            buildDir = buildDir.getCanonicalFile();
        }
        catch (IOException ioe) {
            // FIXUP
        }
        // Tab Name
        String tabName = getString("RUN_LABEL", node.getName());
        
	String[] shellCommandAndArgs = bes.getShellCommandAndArgs(fileObject); // from inside shell file or properties
        String shellCommand = shellCommandAndArgs[0];
        String shellFilePath = IpeUtils.toRelativePath(buildDir.getPath(), shellFile.getPath()); // Absolute path to shell file
	String[] args = bes.getArguments(); // from properties

        String developmentHost = getDevelopmentHost(fileObject);
        // Windows: The command is usually of the from "/bin/sh", but this
        // doesn't work here, so extract the 'sh' part and use that instead. 
        // FIXUP: This is not entirely correct though.
        if (PlatformInfo.getDefault(developmentHost).isWindows() && shellCommand.length() > 0) {
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
       
        // Execute the shellfile

        NativeExecutor nativeExecutor = new NativeExecutor(
            developmentHost,
            buildDir.getPath(),
            shellCommand,
            argsFlat.toString(),
            prepareEnv(developmentHost),
            tabName,
            "Run", // NOI18N
            false,
            true,
            false);

        new ShellExecuter(nativeExecutor).execute();
    }
    
    private static class ShellExecuter implements ExecutionListener {
        private final NativeExecutor nativeExecutor;
        private final ProgressHandle progressHandle;
        private ExecutorTask executorTask = null;

        public ShellExecuter(NativeExecutor nativeExecutor) {
            this.nativeExecutor = nativeExecutor;
            nativeExecutor.addExecutionListener(this);
            this.progressHandle = createPogressHandle(new StopAction(this), nativeExecutor);
        }
        
        public void execute() {
            try {
                executorTask = nativeExecutor.execute();
            } catch (IOException ioe) {
            } 
        }
        
        public void executionFinished(int rc) {
            progressHandle.finish();
        }

        public void executionStarted() {
            progressHandle.start();
        }

        public ExecutorTask getExecutorTask() {
            return executorTask;
        }
    }
    
    private static final class StopAction extends AbstractAction {
        private final ShellExecuter shellExecutor;

        public StopAction(ShellExecuter shellExecutor) {
            this.shellExecutor = shellExecutor;
        }

//        @Override
//        public Object getValue(String key) {
//            if (key.equals(Action.SMALL_ICON)) {
//                return new ImageIcon(DefaultProjectActionHandler.class.getResource("/org/netbeans/modules/cnd/makeproject/ui/resources/stop.png"));
//            } else if (key.equals(Action.SHORT_DESCRIPTION)) {
//                return getString("TargetExecutor.StopAction.stop");
//            } else {
//                return super.getValue(key);
//            }
//        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled()) {
                return;
            }
            setEnabled(false);
            if (shellExecutor.getExecutorTask() != null) {
                shellExecutor.getExecutorTask().stop();
            }
        }
    }
    
    private static ProgressHandle createPogressHandle(final AbstractAction sa, final NativeExecutor nativeExecutor) {
        ProgressHandle handle = ProgressHandleFactory.createHandle(nativeExecutor.getTabeName(), new Cancellable() {
            public boolean cancel() {
                sa.actionPerformed(null);
                return true;
            }
        }, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                nativeExecutor.getTab().select();
            }
        });
        handle.setInitialDelay(0);
        return handle;
    }
        
    @Override
    protected boolean asynchronous() {
        return false;
    }
}
