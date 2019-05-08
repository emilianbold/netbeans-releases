/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.cnd.makeproject.uiapi;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Lookup;

/**
 *
 */
public final class ConfirmSupport {
    
    public interface MimeExtensions {
        boolean isC();
        boolean isCpp();
        boolean isHeader();
    }

    public interface MimeExtension {
        boolean addNewExtension();
    }
    
    public interface SelectExecutable {
        String getExecutable();
    }

    public interface AutoConfirm {
    }

    public interface ConfirmPlatformMismatch {
    }

    public interface ConfirmVersion {
    }

    public interface BatchConfigurationSelector {
        String getCommand();
        Configuration[] getSelectedConfs();
    }
    
    public interface ConfirmMimeExtensionsFactory {
        MimeExtensions create(Set<String> unknownC, Set<String> unknownCpp, Set<String> unknownH);
        MimeExtension create(Set<String> usedExtension, String mime);
    }

    public interface SelectExecutableFactory {
        SelectExecutable create(ProjectActionEvent pae);
    }

    public interface AutoConfirmFactory {
        AutoConfirm create(String dialogTitle, String message, String autoConfirmMessage);
    }

    public interface ConfirmPlatformMismatchFactory {
        ConfirmPlatformMismatch create(String dialogTitle, String message);
        ConfirmPlatformMismatch createAndWait(String message, String autoConfirmMessage);
    }
    
    public interface ConfirmVersionFactory {
        ConfirmVersion create(String dialogTitle, String message, String autoConfirmMessage, Runnable onConfirm);
        ConfirmVersion createAndWait(String dialogTitle, String message, String autoConfirmMessage);
    }

    public interface ForbidBuildAnalyzerFactory {
        void show(Project project);
    }

    public interface ResolveRfsLibraryFactory {
        void show(ExecutionEnvironment env);
    }
    
    public interface BatchConfigurationSelectorFactory {
        BatchConfigurationSelector create(MakeProject project, Configuration[] confs);
    }

    public interface NotifyCantConnectFactory {
        void showErrorLater(String dialogTitle, String message);
    }
    
    public interface ConfirmCreateConnectionFactory {
        AutoConfirm createConnection(String dialogTitle, String message, String autoConfirmMessage);
    }

    public interface ResolveBuildToolsFactory {
        boolean resolveTools(String title, MakeConfigurationDescriptor pd, MakeConfiguration conf, ExecutionEnvironment env, String csname, CompilerSet cs, boolean cRequired, boolean cppRequired, boolean fRequired, boolean asRequired, ArrayList<String> errs);
    }
    
    private static final Default DEFAULT = new Default();

    private ConfirmSupport() {
    }

    public static ConfirmMimeExtensionsFactory getDefaultConfirmMimeExtensionsFactory() {
        ConfirmMimeExtensionsFactory defaultFactory = Lookup.getDefault().lookup(ConfirmMimeExtensionsFactory.class);
        return defaultFactory == null ? DEFAULT : defaultFactory;
    }

    public static SelectExecutableFactory getDefaultSelectExecutableFactory() {
        SelectExecutableFactory defaultFactory = Lookup.getDefault().lookup(SelectExecutableFactory.class);
        return defaultFactory == null ? DEFAULT : defaultFactory;
    }

    public static AutoConfirmFactory getAutoConfirmFactory() {
        AutoConfirmFactory defaultFactory = Lookup.getDefault().lookup(AutoConfirmFactory.class);
        return defaultFactory == null ? DEFAULT : defaultFactory;
    }

    public static ConfirmPlatformMismatchFactory getConfirmPlatformMismatchFactory() {
        ConfirmPlatformMismatchFactory defaultFactory = Lookup.getDefault().lookup(ConfirmPlatformMismatchFactory.class);
        return defaultFactory == null ? DEFAULT : defaultFactory;
    }

    public static ConfirmVersionFactory getConfirmVersionFactory() {
        ConfirmVersionFactory defaultFactory = Lookup.getDefault().lookup(ConfirmVersionFactory.class);
        return defaultFactory == null ? DEFAULT : defaultFactory;
    }

    public static ForbidBuildAnalyzerFactory getForbidBuildAnalyzerFactory() {
        ForbidBuildAnalyzerFactory defaultFactory = Lookup.getDefault().lookup(ForbidBuildAnalyzerFactory.class);
        return defaultFactory == null ? DEFAULT : defaultFactory;
    }

    public static ResolveRfsLibraryFactory getResolveRfsLibraryFactory() {
        ResolveRfsLibraryFactory defaultFactory = Lookup.getDefault().lookup(ResolveRfsLibraryFactory.class);
        return defaultFactory == null ? DEFAULT : defaultFactory;
    }

    public static BatchConfigurationSelectorFactory getBatchConfigurationSelectorFactory() {
        BatchConfigurationSelectorFactory defaultFactory = Lookup.getDefault().lookup(BatchConfigurationSelectorFactory.class);
        return defaultFactory == null ? DEFAULT : defaultFactory;
    }

    public static NotifyCantConnectFactory getNotifyCantConnectFactory() {
        NotifyCantConnectFactory defaultFactory = Lookup.getDefault().lookup(NotifyCantConnectFactory.class);
        return defaultFactory == null ? DEFAULT : defaultFactory;
    }

    public static ConfirmCreateConnectionFactory getConfirmCreateConnectionFactory() {
        ConfirmCreateConnectionFactory defaultFactory = Lookup.getDefault().lookup(ConfirmCreateConnectionFactory.class);
        return defaultFactory == null ? DEFAULT : defaultFactory;
    }

    public static ResolveBuildToolsFactory getResolveBuildToolsFactory() {
        ResolveBuildToolsFactory defaultFactory = Lookup.getDefault().lookup(ResolveBuildToolsFactory.class);
        return defaultFactory == null ? DEFAULT : defaultFactory;
    }

    private static final class Default implements ConfirmMimeExtensionsFactory, SelectExecutableFactory, AutoConfirmFactory,
            ConfirmPlatformMismatchFactory, ConfirmVersionFactory, ForbidBuildAnalyzerFactory, ResolveRfsLibraryFactory,
            BatchConfigurationSelectorFactory, NotifyCantConnectFactory, ConfirmCreateConnectionFactory, ResolveBuildToolsFactory {

        @Override
        public MimeExtensions create(Set<String> unknownC, Set<String> unknownCpp, Set<String> unknownH) {
            return new ConfirmExtensionsUiImpl(unknownC, unknownCpp, unknownH);
        }

        @Override
        public MimeExtension create(Set<String> usedExtension, String mime) {
            return new ConfirmExtensionUiImpl();
        }

        @Override
        public SelectExecutable create(ProjectActionEvent pae) {
            return null;
        }

        @Override
        public AutoConfirm create(String dialogTitle, String message, String autoConfirmMessage) {
            return new AutoConfirmImpl(dialogTitle, message, autoConfirmMessage);
        }

        @Override
        public ConfirmPlatformMismatch create(String dialogTitle, String message) {
            return new ConfirmPlatformMismatchImpl(dialogTitle, message);
        }

        @Override
        public ConfirmPlatformMismatch createAndWait(String message, String autoConfirmMessage) {
            return new ConfirmPlatformMismatchImpl(message, autoConfirmMessage);
        }

        @Override
        public ConfirmVersion create(String dialogTitle, String message, String autoConfirmMessage, Runnable onConfirm) {
            return new ConfirmVersionImpl(dialogTitle, message, autoConfirmMessage, onConfirm);
        }
        
        @Override
        public ConfirmVersion createAndWait(String dialogTitle, String message, String autoConfirmMessage) {
            return new ConfirmVersionImpl(dialogTitle, message, autoConfirmMessage);
        }

        @Override
        public void show(Project project) {
        }

        @Override
        public void show(ExecutionEnvironment env) {
        }

        @Override
        public BatchConfigurationSelector create(MakeProject project, Configuration[] confs) {
            return null;
        }

        @Override
        public void showErrorLater(String dialogTitle, String message) {
            new Exception(message).printStackTrace(System.err);
        }

        @Override
        public AutoConfirm createConnection(String dialogTitle, String message, String autoConfirmMessage) {
            new Exception(autoConfirmMessage).printStackTrace(System.err);
            return new AutoConfirm() {
            };
        }

        @Override
        public boolean resolveTools(String title, MakeConfigurationDescriptor pd, MakeConfiguration conf, ExecutionEnvironment env, String csname, CompilerSet cs, boolean cRequired, boolean cppRequired, boolean fRequired, boolean asRequired, ArrayList<String> errs) {
            return false;
        }

        private static final class ConfirmExtensionsUiImpl implements MimeExtensions {
            private final AtomicBoolean cCheck;
            private final AtomicBoolean cppCheck;
            private final AtomicBoolean headerCheck;

            public ConfirmExtensionsUiImpl(Set<String> unknownC, Set<String> unknownCpp, Set<String> unknownH) {
                cCheck = new AtomicBoolean(!unknownC.isEmpty());
                cppCheck = new AtomicBoolean(!unknownCpp.isEmpty());
                headerCheck = new AtomicBoolean(!unknownH.isEmpty());
            }

            @Override
            public boolean isC(){
                return cCheck.get();
            }

            @Override
            public boolean isCpp(){
                return cppCheck.get();
            }

            @Override
            public boolean isHeader(){
                return headerCheck.get();
            }
        }
        
        private static final class ConfirmExtensionUiImpl implements MimeExtension {

            @Override
            public boolean addNewExtension() {
                return true;
            }
        }
        
        private static final class AutoConfirmImpl implements AutoConfirm {
            private AutoConfirmImpl(String dialogTitle, String message, String autoConfirmMessage) {
                System.err.print(message);
                System.err.println(autoConfirmMessage);
            }
        }
        
        private static final class ConfirmPlatformMismatchImpl implements ConfirmPlatformMismatch {
            private ConfirmPlatformMismatchImpl(String dialogTitle, String message) {
                new Exception(message).printStackTrace(System.err);
            }
        }
        
        private static final class ConfirmVersionImpl implements ConfirmVersion {
            private ConfirmVersionImpl(String dialogTitle, String message, String autoConfirmMessage, Runnable onConfirm) {
                System.err.print(message);
                System.err.println(autoConfirmMessage);
                onConfirm.run();
            }
            private ConfirmVersionImpl(String dialogTitle, String message, String autoConfirmMessage) {
                System.err.print(message);
                System.err.println(autoConfirmMessage);
            }
        }
    }    
}
