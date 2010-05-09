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
package org.netbeans.modules.cnd.gizmo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;
import org.netbeans.modules.cnd.gizmo.support.GizmoServiceInfo;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.remote.api.RemoteBinaryService;
import org.netbeans.modules.remote.api.RemoteBinaryService.RemoteBinaryID;
import org.netbeans.modules.cnd.dwarfdump.Offset2LineService;
import org.netbeans.modules.cnd.dwarfdump.Offset2LineService.AbstractFunctionToLine;
import org.netbeans.modules.cnd.dwarfdump.Offset2LineService.SourceLineInfo;
import org.netbeans.modules.dlight.management.remote.spi.PathMapper;
import org.netbeans.modules.dlight.management.remote.spi.PathMapperProvider;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcess.State;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Alexey Vladykin
 */
@ServiceProvider(service = SourceFileInfoProvider.class, position = 5000)
public class DwarfSourceInfoProvider implements SourceFileInfoProvider {
    private static final RequestProcessor RP = new RequestProcessor("ReadErrorStream", 2); // NOI18N
    private static final Logger logger = Logger.getLogger("org.netbeans.modules.cnd.gizmo.dwarf"); // NOI18N
    private WeakHashMap<String, Map<String, AbstractFunctionToLine>> cache;

    public DwarfSourceInfoProvider() {
        cache = new WeakHashMap<String, Map<String, AbstractFunctionToLine>>();
    }

    @Override
    public SourceFileInfo getSourceFileInfo(String functionQName, int lineNumber, long offset, Map<String, String> serviceInfo) {
        SourceFileInfo info = _fileName(functionQName, lineNumber, offset, serviceInfo);
        if (info != null) {
            PathMapperProvider provider = Lookup.getDefault().lookup(PathMapperProvider.class);
            if (provider != null) {
                String env = serviceInfo.get(ServiceInfoDataStorage.EXECUTION_ENV_KEY);
                if (env != null) {
                    PathMapper pathMapper = provider.getPathMapper(ExecutionEnvironmentFactory.fromUniqueID(env));
                    if (pathMapper != null) {
                        String remote = pathMapper.getLocalPath(info.getFileName());
                        if (remote != null) {
                            return new SourceFileInfo(remote, info.getLine(), 0);
                        }
                    }
                }
            }
        }
        return info;
    }

    private synchronized SourceFileInfo _fileName(String functionQName, int lineNumber, long offset, Map<String, String> serviceInfo) {
        if (serviceInfo == null){
            return null;
        }
        ExecutionEnvironment execEnv = ExecutionEnvironmentFactory.fromUniqueID(serviceInfo.get(ServiceInfoDataStorage.EXECUTION_ENV_KEY));
        String executable = null;

        if (execEnv.isLocal()) {
            executable = serviceInfo.get(GizmoServiceInfo.GIZMO_PROJECT_EXECUTABLE);
        } else {
            String remoteExecutable = serviceInfo.get(GizmoServiceInfo.GIZMO_REMOTE_EXECUTABLE);
            if (remoteExecutable != null) {
                if (cache.containsKey(remoteExecutable)){
                    Map<String, AbstractFunctionToLine> sourceInfoMap = cache.get(remoteExecutable);
                    if (sourceInfoMap != null) {
                        return findSourceInfo(sourceInfoMap, functionQName, lineNumber, offset);
                    }
                } else {
                    Map<String, AbstractFunctionToLine> sourceInfoMap = getOffsets(execEnv, remoteExecutable);
                    if (sourceInfoMap != null) {
                        cache.put(remoteExecutable, sourceInfoMap.isEmpty()?
                            Collections.<String, AbstractFunctionToLine>emptyMap() : sourceInfoMap);
                        return findSourceInfo(sourceInfoMap, functionQName, lineNumber, offset);
                    } else {
                        cache.put(remoteExecutable, null);
                    }
                }
            }
            String executableID = serviceInfo.get(GizmoServiceInfo.GIZMO_PROJECT_EXECUTABLE);
            RemoteBinaryID id = RemoteBinaryService.RemoteBinaryID.fromIDString(executableID);
            Future<Boolean> remoteSyncResult = RemoteBinaryService.getResult(id);

            if (remoteSyncResult != null && remoteSyncResult.isDone()) {
                try {
                    if (remoteSyncResult.get() == true) {
                        executable = RemoteBinaryService.getFileName(id);
                    }
                } catch (InterruptedException ex) {
                } catch (ExecutionException ex) {
                }
            }
        }

        if (executable != null) {
            return findSourceInfo(getSourceInfo(executable), functionQName, lineNumber, offset);
        }
        return null;
    }

    private SourceFileInfo findSourceInfo(Map<String, AbstractFunctionToLine> sourceInfoMap, String functionQName, int lineNumber, long offset ) {
        logger.log(Level.FINE, "Search for:{0}+{1}", new Object[]{functionQName, offset}); // NOI18N
        AbstractFunctionToLine fl = sourceInfoMap.get(functionQName);
        if (fl != null) {
            SourceLineInfo sourceInfo = fl.getLine((int) offset);
            logger.log(Level.FINE, "Found:{0}", fl); // NOI18N
            logger.log(Level.FINE, "Line:{0}", sourceInfo); // NOI18N
            if (lineNumber > 0 && sourceInfo != null) {
                return new SourceFileInfo(sourceInfo.getFileName(), lineNumber, 0);
            }
            return new SourceFileInfo(sourceInfo.getFileName(), sourceInfo.getLine(), 0);
        }
        return null;
    }

    private Map<String, AbstractFunctionToLine> getOffsets(ExecutionEnvironment execEnv, String executable) {
        NativeProcess process = null;
        Task errorTask = null;
        try {
            process = RemoteJarServiceProvider.getJavaProcess(Offset2LineService.class, execEnv, new String[]{executable});
            if (process.getState() != State.ERROR){
                final NativeProcess startedProcess = process;
                final List<String> errors = new ArrayList<String>();
                errorTask = RP.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            errors.addAll(ProcessUtils.readProcessError(startedProcess));
                        } catch (Throwable ex) {
                        }
                    }
                });

                BufferedReader out = new BufferedReader(new InputStreamReader(process.getInputStream()));
                Map<String, AbstractFunctionToLine> res = Offset2LineService.getOffset2Line(out);

                int rc = process.waitFor();
                logger.log(Level.FINE, "Return code {0}", rc); // NOI18N
                boolean hasException = false;
                for(String error : errors) {
                    if (error.indexOf("Exception") >= 0) { // NOI18N
                        hasException = true;
                    }
                    logger.log(Level.INFO, error); // NOI18N
                }
                if (rc == 0 && !hasException) {
                    logger.log(Level.FINE, "Loaded lines info for {0} functions from executable file {1}", new Object[]{res.size(), executable}); // NOI18N
                    return res;
                }
            }
        } catch (IOException ex) {
            logger.log(Level.INFO, ex.getMessage(), ex);
        } catch (InterruptedException ex) {
        } catch (Throwable ex) {
            logger.log(Level.INFO, ex.getMessage(), ex);
        } finally {
            if (errorTask != null){
                errorTask.cancel();
            }
            if (process != null) {
                process.destroy();
            }
        }
        return null;
    }

    private synchronized Map<String, AbstractFunctionToLine> getSourceInfo(String executable) {
        Map<String, AbstractFunctionToLine> sourceInfoMap = cache.get(executable);
        if (sourceInfoMap == null) {
            try {
                sourceInfoMap = Offset2LineService.getOffset2Line(executable);
                logger.log(Level.FINE, "Loaded lines info for {0} functions from executable file {1}", new Object[]{sourceInfoMap.size(), executable}); // NOI18N
            } catch (FileNotFoundException ex) {
                DLightLogger.instance.log(Level.SEVERE, ex.getMessage(), ex);
            } catch (IOException ex) {
                DLightLogger.instance.log(Level.INFO, ex.getMessage());
            } catch (Throwable ex) {
                DLightLogger.instance.log(Level.INFO, ex.getMessage(), ex);
            }
            cache.put(executable, sourceInfoMap.isEmpty()?
                Collections.<String, AbstractFunctionToLine>emptyMap() : sourceInfoMap);
        }
        return sourceInfoMap;
    }
}
