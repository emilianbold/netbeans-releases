/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.api.execution;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.netbeans.modules.dlight.api.impl.DLightSessionHandlerAccessor;
import org.netbeans.modules.dlight.api.impl.DLightSessionInternalReference;
import org.netbeans.modules.dlight.api.impl.DLightToolkitManager;
import org.netbeans.modules.dlight.api.tool.DLightConfiguration;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.openide.util.Lookup;

/**
 * Use this class to start D-Light'ing process.
 * <pre>
final NativeExecutableTarget target = new NativeExecutableTarget(new NativeExecutableTargetConfiguration(application, arguments, environment));
final DLightToolkitManagement dtm = DLightToolkitManagement.getInstance();
final Future&lt;DLightToolkitManagement.DLightSessionHandler&gt; sessionCreationTask = dtm.createSession(target, "Gizmo);
new Thread(new Runnable() {
public void run() {
try {
dtm.startSession(sessionCreationTask.get());
} catch (InterruptedException ex) {
Exceptions.printStackTrace(ex);
} catch (ExecutionException ex) {
Exceptions.printStackTrace(ex);
}
}
}).start();

</pre>
 */
public final class DLightToolkitManagement {

    private static final DLightToolkitManagement instance;
    private static final DLightToolkitManager toolkitManager;

    static {
        DLightSessionHandlerAccessor.setDefault(new DLightSessionHandlerAccessorImpl());
        Collection<? extends DLightToolkitManager> allManagers = Lookup.getDefault().lookupAll(DLightToolkitManager.class);

        // Pick the first one
        if (!allManagers.isEmpty()) {
            toolkitManager = allManagers.iterator().next();
        } else {
            toolkitManager = null;
        }

        instance = new DLightToolkitManagement();
    }

    private DLightToolkitManagement() {
        assert (toolkitManager != null);
    }

    /**
     * Singleton method to get instance to work with
     * @return instance
     */
    public static final DLightToolkitManagement getInstance() {
        return instance;
    }

    /**
     * Creates new session to start D-Light'ing of <code>target</code> using
     * <code>configurationName</code> as a D-Light Configuration name.
     * D-Light Configuration consist of tools to be used to d-light target.
     * It is defined in NetBeans files system.
     * <pre>
     * &lt;filesystem&gt;
    &lt;folder name="DLight"&gt;
    &lt;folder name="Configurations"&gt;
    &lt;folder name="MyFavoriteConfiguration"&gt;
    &lt;folder name="KnownToolsConfigurationProviders"&gt;
    &lt;file name="MyDLightToolConfigurationProvider.shadow"&gt;
    &lt;attr name="originalFile" stringvalue="DLight/ToolConfigurationProviders/MyDLightToolConfigurationProvider.instance"/&gt;
    &lt;/file&gt;
    &lt;file name="MemoryToolConfigurationProvider.shadow"&gt;
    &lt;attr name="originalFile" stringvalue="DLight/ToolConfigurationProviders/MemoryToolConfigurationProvider.instance"/&gt;
    &lt;/file&gt;
    &lt;file name="SyncToolConfigurationProvider.shadow"&gt;
    &lt;attr name="originalFile" stringvalue="DLight/ToolConfigurationProviders/SyncToolConfigurationProvider.instance"/&gt;
    &lt;/file&gt;
    &lt;/folder&gt;
    &lt;/folder&gt;
    &lt;/folder&gt;
    &lt;/folder&gt;
    &lt;/filesystem&gt;
    </pre>
     * Example of usage:
    <pre>
    final NativeExecutableTarget target = new NativeExecutableTarget(new NativeExecutableTargetConfiguration(application, arguments, environment));
    final DLightToolkitManagement dtm = DLightToolkitManagement.getInstance();
    final Future&lt;DLightToolkitManagement.DLightSessionHandler&gt; sessionCreationTask = dtm.createSession(target, "Gizmo);
    new Thread(new Runnable() {
    public void run() {
    try {
    dtm.startSession(sessionCreationTask.get());
    } catch (InterruptedException ex) {
    Exceptions.printStackTrace(ex);
    } catch (ExecutionException ex) {
    Exceptions.printStackTrace(ex);
    }
    }
    }).start();
    </pre>
     * @param target target to be d-lighted
     * @param configurationName configuration name to be used
     * @return session handler, this handler should be used to start {@link #startSession(org.netbeans.modules.dlight.api.execution.DLightToolkitManagement.DLightSessionHandler) }
     * or stop {@link #stopSession(org.netbeans.modules.dlight.api.execution.DLightToolkitManagement.DLightSessionHandler) } session.
     * @deprecated use #createSession(DLightSessionConfiguration)
     */
    @Deprecated
    public Future<DLightSessionHandler> createSession(
            final DLightTarget target,
            final String configurationName) {
        return createSession(target, configurationName, null);
    }

    /**
     *
     * @param target
     * @param configurationName
     * @param sessionName
     * @return
     * @deprecated use #createSession(DLightSessionConfiguration)
     */
    @Deprecated
    public Future<DLightSessionHandler> createSession(
            final DLightTarget target,
            final String configurationName,
            final String sessionName) {
        final DLightSessionConfiguration sessionConfiguration = new DLightSessionConfiguration();
        sessionConfiguration.setDLightConfigurationName(configurationName);
        sessionConfiguration.setDLightTarget(target);
        sessionConfiguration.setSessionName(sessionName);
        return DLightExecutorService.submit(new Callable<DLightSessionHandler>() {

            @Override
            public DLightSessionHandler call() throws Exception {
                return toolkitManager.createSession(sessionConfiguration);
            }
        }, "DLight [" + configurationName + "] Session Creation for " + target); // NOI18N
    }

    /**
     *
     * @param target
     * @param configuration
     * @return
     * @deprecated use #createSession(DLightSessionConfiguration)
     */
    @Deprecated
    public Future<DLightSessionHandler> createSession(
            final DLightTarget target,
            final DLightConfiguration configuration) {
        return createSession(target, configuration, null);
    }

    /**
     *
     * @param target
     * @param configuration
     * @param sessionName
     * @return
     * @deprecated use #createSession(DLightSessionConfiguration)
     */
    @Deprecated
    public Future<DLightSessionHandler> createSession(
            final DLightTarget target,
            final DLightConfiguration configuration,
            final String sessionName) {
        final DLightSessionConfiguration sessionConfiguration = new DLightSessionConfiguration();
        sessionConfiguration.setDLightConfiguration(configuration);
        sessionConfiguration.setDLightTarget(target);
        sessionConfiguration.setSessionName(sessionName);
        return DLightExecutorService.submit(new Callable<DLightSessionHandler>() {

            @Override
            public DLightSessionHandler call() throws Exception {
                return toolkitManager.createSession(sessionConfiguration);
            }
        }, "DLight [" + configuration.getConfigurationName() + "] Session Creation for " + target); // NOI18N
    }

    public Future<DLightSessionHandler> createSession(final DLightSessionConfiguration sessionConfiguration) {
        return DLightExecutorService.submit(new Callable<DLightSessionHandler>() {

            @Override
            public DLightSessionHandler call() throws Exception {
                return toolkitManager.createSession(sessionConfiguration);
            }
        }, sessionConfiguration + ""); // NOI18N
    }

    /**
     * Stars session <code>sessionHandler<code>, the reference can be retrieved using {@link #createSession(org.netbeans.modules.dlight.api.execution.DLightTarget, java.lang.String) } method
     * @param sessionHandler session to be started
     */
    public void startSession(DLightSessionHandler sessionHandler) {
        toolkitManager.startSession(sessionHandler);
    }

    /**
     * Stop session <code>sessionHandler</code>, , the reference can be retrieved using {@link #createSession(org.netbeans.modules.dlight.api.execution.DLightTarget, java.lang.String) } method
     * @param sessionHandler session to be
     */
    public void stopSession(DLightSessionHandler sessionHandler) {
        toolkitManager.stopSession(sessionHandler);
    }

    private DLightSessionHandler create(DLightSessionInternalReference ref) {
        return new DLightSessionHandler(ref);
    }

    /**
     * Session handler, it can be retrieved using
     * {@link DLightToolkitManagement#createSession(org.netbeans.modules.dlight.api.execution.DLightTarget, java.lang.String) }
     * method and used to start and stop D-Light Session.
     */
    public static final class DLightSessionHandler {

        private DLightSessionInternalReference ref;

        private DLightSessionHandler(DLightSessionInternalReference ref) {
            this.ref = ref;
        }

        DLightSessionInternalReference getSessionReferenceImpl() {
            return ref;
        }
    }

    private static final class DLightSessionHandlerAccessorImpl extends DLightSessionHandlerAccessor {

        @Override
        public DLightSessionHandler create(DLightSessionInternalReference ref) {
            return DLightToolkitManagement.getInstance().create(ref);
        }

        @Override
        public DLightSessionInternalReference getSessionReferenceImpl(DLightSessionHandler handler) {
            return handler.getSessionReferenceImpl();
        }
    }        
}
