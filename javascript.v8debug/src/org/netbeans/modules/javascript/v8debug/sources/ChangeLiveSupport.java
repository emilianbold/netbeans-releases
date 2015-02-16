/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.v8debug.sources;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.lib.v8debug.PropertyBoolean;
import org.netbeans.lib.v8debug.V8Arguments;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Request;
import org.netbeans.lib.v8debug.V8Response;
import org.netbeans.lib.v8debug.V8Script;
import org.netbeans.lib.v8debug.commands.ChangeLive;
import org.netbeans.lib.v8debug.commands.ChangeLive.ChangeLog.BreakpointUpdate;
import org.netbeans.lib.v8debug.commands.ChangeLive.ChangeLog.BreakpointUpdate.Position;
import org.netbeans.lib.v8debug.commands.Source;
import org.netbeans.modules.javascript.v8debug.ScriptsHandler;
import org.netbeans.modules.javascript.v8debug.V8Debugger;
import org.netbeans.modules.javascript.v8debug.V8DebuggerEngineProvider;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * Support for live application of saved files to the V8.
 * 
 * @author Martin Entlicher
 */
@LazyActionsManagerListener.Registration(path=V8DebuggerEngineProvider.ENGINE_NAME)
public final class ChangeLiveSupport extends LazyActionsManagerListener {
    
    private static final Logger LOG = Logger.getLogger(ChangeLiveSupport.class.getName());
    
    private static final String PREP_REGEX = "^(\\(function.*\\(.*\\).*\\{ ).*";
    private static final Pattern PREP_PATTERN = Pattern.compile(PREP_REGEX, Pattern.MULTILINE | Pattern.DOTALL);
    
    //private static final String PREP_TEXT = "(function (exports, require, module, __filename, __dirname) { ";
    private static final String APP_TEXT = "})();";
    
    private final V8Debugger dbg;
    private final ActionsProvider stepIntoActionsProvider;
    private final FileChangeListener sourceChangeListener;
    private final File[] sourceChangeRoots;
    private final RequestProcessor rp = new RequestProcessor(ChangeLiveSupport.class);
    
    public ChangeLiveSupport(ContextProvider lookupProvider) {
        this.dbg = lookupProvider.lookupFirst(null, V8Debugger.class);
        this.sourceChangeListener = new SourceChangeListener();
        this.sourceChangeRoots = dbg.getScriptsHandler().getLocalRoots();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("new ChangeLiveSupport(), sourceChangeRoots = "+Arrays.toString(sourceChangeRoots));
        }
        if (sourceChangeRoots.length == 0) {
            FileUtil.addFileChangeListener(sourceChangeListener);
        } else {
            for (File root : sourceChangeRoots) {
                FileUtil.addRecursiveListener(sourceChangeListener, root);
            }
        }
        ActionsProvider stepIntoAP = null;
        List<? extends ActionsProvider> actionProviders = lookupProvider.lookup(null, ActionsProvider.class);
        for (ActionsProvider ap : actionProviders) {
            if (ap.getActions().contains(ActionsManager.ACTION_STEP_INTO)) {
                stepIntoAP = ap;
            }
        }
        this.stepIntoActionsProvider = stepIntoAP;
    }
    
    private void applyModifiedFiles(List<FileObject> modifiedFiles) {
        ScriptsHandler sh = dbg.getScriptsHandler();
        Collection<V8Script> scripts = sh.getScripts();
        final AtomicBoolean doStepInto = new AtomicBoolean(false);
        final Phaser phaser = new Phaser(1);
        LOG.log(Level.FINE, "applyModifiedFiles({0})", modifiedFiles);
        for (FileObject fo : modifiedFiles) {
            if (!sh.containsLocalFile(fo)) {
                continue;
            }
            String path = fo.getPath();
            String serverPath;
            try {
                serverPath = sh.getServerPath(path);
            } catch (ScriptsHandler.OutOfScope ex) {
                continue;
            }
            V8Script script = null;
            for (V8Script s : scripts) {
                if (serverPath.equals(s.getName())) {
                    script = s;
                    break;
                }
            }
            if (script == null) {
                // Not a loaded script
                continue;
            }
            String origScriptSource = script.getSource();
            if (origScriptSource == null) {
                origScriptSource = script.getSourceStart();
            }
            String prependedText = null;
            Matcher matcher = PREP_PATTERN.matcher(origScriptSource);
            if (matcher.matches()) {
                int gc = matcher.groupCount();
                if (gc > 0) {
                    prependedText = matcher.group(1);
                }
            }
            String fileSource;
            try {
                fileSource = fo.asText();
            } catch (IOException ioex) {
                // Can not update scripts that can not be read.
                continue;
            }
            LOG.fine("Identified changed script "+script.getName());
            if (prependedText != null) {
                if (!fileSource.startsWith(prependedText)) {
                    // It's not there already
                    fileSource = prependedText + fileSource + APP_TEXT;
                    LOG.log(Level.FINE,"Header text added: ''{0}"+"'', appended: ''"+APP_TEXT+"''", prependedText);
                }
            }
            V8Arguments changeLiveArgs = new ChangeLive.Arguments(
                    script.getId(),
                    fileSource,
                    Boolean.FALSE
                    );
            phaser.register();
            LOG.log(Level.FINE, "Running ChangeLive command for script {0}", script.getName());
            V8Request sendCLRequest = dbg.sendCommandRequest(V8Command.Changelive, changeLiveArgs, new V8Debugger.CommandResponseCallback() {
                @Override
                public void notifyResponse(V8Request request, V8Response response) {
                    try {
                        if (response != null) {
                            ChangeLive.ResponseBody clrb = (ChangeLive.ResponseBody) response.getBody();
                            if (clrb != null) {
                                ChangeLive.ChangeLog changeLog = clrb.getChangeLog();
                                if (changeLog != null) {
                                    updateBreakpoints(changeLog.getBreakpointsUpdate());
                                }
                                PropertyBoolean doStepIn = clrb.getStepInRecommended();
                                ChangeLive.Result result = clrb.getResult();
                                if (result != null) {
                                    if (!doStepIn.hasValue()) {
                                        doStepIn = result.getStackUpdateNeedsStepIn();
                                    }
                                }
                                if (doStepIn.getValue()) {
                                    doStepInto.set(true);
                                }
                            }
                        }
                    } finally {
                        LOG.fine("A ChangeLive command finished.");
                        phaser.arriveAndDeregister();
                    }
                }
            });
            if (sendCLRequest == null) {
                phaser.arriveAndDeregister();
            }
        }
        phaser.arriveAndAwaitAdvance();
        boolean doStepIn = doStepInto.get();
        LOG.log(Level.FINE, "ALl ChangeLive commands processed. Will step into = {0}", doStepIn && stepIntoActionsProvider != null);
        if (doStepIn && stepIntoActionsProvider != null) {
            final CountDownLatch cdl = new CountDownLatch(1);
            stepIntoActionsProvider.postAction(ActionsManager.ACTION_STEP_INTO, new Runnable() {
                @Override
                public void run() {
                    cdl.countDown();
                }
            });
            try {
                cdl.await();
            } catch (InterruptedException ex) {}
        }
    }

    private void updateBreakpoints(BreakpointUpdate[] breakpointsUpdate) {
        for (BreakpointUpdate bu : breakpointsUpdate) {
            long bpId = bu.getId();
            BreakpointUpdate.Type type = bu.getType();
            LOG.fine("updateBreakpoint id = "+bpId+", type = "+type);
            switch (type) {
                case CopiedToOld:
                    
                    break;
                case PositionChanged:
                    Position oldPos = bu.getOldPositions();
                    Position newPos = bu.getNewPositions();
                    dbg.getBreakpointsHandler().positionChanged(bpId,
                            newPos.getLine(), newPos.getColumn());
                    break;
            }
        }
    }
    
    @Override
    protected void destroy() {
        if (sourceChangeRoots.length == 0) {
            FileUtil.removeFileChangeListener(sourceChangeListener);
        } else {
            for (File root : sourceChangeRoots) {
                FileUtil.removeRecursiveListener(sourceChangeListener, root);
            }
        }
    }

    @Override
    public String[] getProperties() {
        return new String[] {};
    }

    private final class SourceChangeListener implements FileChangeListener {
        
        private FileChangeDelivery fileChangeDelivery = new FileChangeDelivery();

        @Override
        public void fileFolderCreated(FileEvent fe) {
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
        }

        @Override
        public void fileChanged(FileEvent fe) {
            fileChangeDelivery.add(fe.getFile());
            fe.runWhenDeliveryOver(fileChangeDelivery);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
        
    }
    
    private final class FileChangeDelivery implements Runnable {
        
        private final List<FileObject> changedFiles = new LinkedList<>();

        private void add(FileObject file) {
            synchronized (changedFiles) {
                changedFiles.add(file);
            }
        }
        
        @Override
        public void run() {
            final List<FileObject> modifiedFiles;
            synchronized (changedFiles) {
                modifiedFiles = new ArrayList<>(changedFiles);
                changedFiles.clear();
            }
            rp.post(new Runnable() {
                @Override
                public void run() {
                    applyModifiedFiles(modifiedFiles);
                }
            });
        }

    }
}
