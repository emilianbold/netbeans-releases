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

import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;
import org.netbeans.modules.cnd.gizmo.support.GizmoServiceInfo;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Future;
import java.util.logging.Level;
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
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Alexey Vladykin
 */
@ServiceProvider(service = SourceFileInfoProvider.class, position = 5000)
public class DwarfSourceInfoProvider implements SourceFileInfoProvider {
    private static final boolean TRACE = false;
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

    private SourceFileInfo _fileName(String functionQName, int lineNumber, long offset, Map<String, String> serviceInfo) {
        if (serviceInfo == null){
            return null;
        }

        ExecutionEnvironment execEnv = ExecutionEnvironmentFactory.fromUniqueID(serviceInfo.get(ServiceInfoDataStorage.EXECUTION_ENV_KEY));

        String executable = null;

        if (execEnv.isLocal()) {
            executable = serviceInfo.get(GizmoServiceInfo.GIZMO_PROJECT_EXECUTABLE);
        } else {
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
            Map<String, AbstractFunctionToLine> sourceInfoMap = getSourceInfo(executable);
            if (TRACE) {
                System.err.println("Search for:"+functionQName+"+"+offset); // NOI18N
            }
            AbstractFunctionToLine fl = sourceInfoMap.get(functionQName);
            if (fl != null) {
                SourceLineInfo sourceInfo = fl.getLine((int)offset);
                if (TRACE) {
                    System.err.println("Found:"+fl); // NOI18N
                    System.err.println("Line:"+sourceInfo); // NOI18N
                }
                if (lineNumber > 0 && sourceInfo != null) {
                    return new SourceFileInfo(sourceInfo.getFileName(), lineNumber, 0);
                }
                return new SourceFileInfo(sourceInfo.getFileName(), sourceInfo.getLine(), 0);
            }
        }
        return null;
    }

    private synchronized Map<String, AbstractFunctionToLine> getSourceInfo(String executable) {
        Map<String, AbstractFunctionToLine> sourceInfoMap = cache.get(executable);
        if (sourceInfoMap == null) {
            try {
                sourceInfoMap = Offset2LineService.getOffset2Line(executable);
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
