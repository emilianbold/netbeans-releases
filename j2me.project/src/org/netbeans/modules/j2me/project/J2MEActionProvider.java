/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2me.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2me.project.ui.ManageMIDlets;
import org.netbeans.modules.j2me.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl;
import org.netbeans.modules.java.api.common.project.BaseActionProvider;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import static org.netbeans.spi.project.ActionProvider.COMMAND_BUILD;
import static org.netbeans.spi.project.ActionProvider.COMMAND_CLEAN;
import static org.netbeans.spi.project.ActionProvider.COMMAND_COMPILE_SINGLE;
import static org.netbeans.spi.project.ActionProvider.COMMAND_COPY;
import static org.netbeans.spi.project.ActionProvider.COMMAND_DEBUG;
import static org.netbeans.spi.project.ActionProvider.COMMAND_DELETE;
import static org.netbeans.spi.project.ActionProvider.COMMAND_MOVE;
import static org.netbeans.spi.project.ActionProvider.COMMAND_PROFILE;
import static org.netbeans.spi.project.ActionProvider.COMMAND_REBUILD;
import static org.netbeans.spi.project.ActionProvider.COMMAND_RENAME;
import static org.netbeans.spi.project.ActionProvider.COMMAND_RUN;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Zezula
 */
final class J2MEActionProvider extends BaseActionProvider {

    private static final Logger LOG = Logger.getLogger(J2MEActionProvider.class.getName());
    private static final String MIDLET = "javax.microedition.midlet.MIDlet";    //NOI18N

    private static final String[] supportedActions = {
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        COMMAND_COMPILE_SINGLE,
        COMMAND_RUN,
        COMMAND_DEBUG,
        COMMAND_PROFILE,
        JavaProjectConstants.COMMAND_JAVADOC,
        JavaProjectConstants.COMMAND_DEBUG_FIX,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME,
    };

    private static final String[] platformSensitiveActions = {
        COMMAND_BUILD,
        COMMAND_REBUILD,
        COMMAND_COMPILE_SINGLE,
        COMMAND_RUN,
        COMMAND_DEBUG,
        COMMAND_PROFILE,
        JavaProjectConstants.COMMAND_JAVADOC,
        JavaProjectConstants.COMMAND_DEBUG_FIX,
    };

    private static final Map<String,String[]> commands;
    static {
        Map<String,String[]> tmp = new HashMap<>();
        tmp.put(COMMAND_CLEAN, new String[] {"clean"}); // NOI18N
        tmp.put(COMMAND_COMPILE_SINGLE, new String[] {"compile-single"}); // NOI18N
        tmp.put(COMMAND_RUN, new String[] {"run"}); // NOI18N
        tmp.put(COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
        tmp.put(COMMAND_PROFILE, new String[] {"profile"}); // NOI18N
        tmp.put(JavaProjectConstants.COMMAND_JAVADOC, new String[] {"javadoc"}); // NOI18N
        tmp.put(JavaProjectConstants.COMMAND_DEBUG_FIX, new String[] {"debug-fix"}); // NOI18N
        commands = Collections.unmodifiableMap(tmp);
    }

    private static final Set<String> bkgScanSensitiveActions = Collections.unmodifiableSet(
        new HashSet<String>(Arrays.asList(
        COMMAND_RUN,
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG,
        COMMAND_DEBUG_SINGLE
    )));

    private static final Set<String>  needJavaModelActions = Collections.unmodifiableSet(
        new HashSet<String>(Arrays.asList(
        JavaProjectConstants.COMMAND_DEBUG_FIX
    )));

    private final J2MEProject project;

    J2MEActionProvider(
        @NonNull final J2MEProject project,
        @NonNull final UpdateHelper updateHelper,
        @NonNull final SourceRoots src,
        @NonNull final SourceRoots test) {
        super(
            project,
            updateHelper,
            project.evaluator(),
            src,
            test,
            updateHelper.getAntProjectHelper(),
            new CallbackImpl(project.getClassPathProvider(), project));
        this.project = project;
    }

    @Override
    protected String[] getPlatformSensitiveActions() {
        return platformSensitiveActions;
    }

    @Override
    protected String[] getActionsDisabledForQuickRun() {
        return supportedActions;
    }

    @Override
    public Map<String, String[]> getCommands() {
        return commands;
    }

    @Override
    protected Set<String> getScanSensitiveActions() {
        return bkgScanSensitiveActions;
    }

    @Override
    protected Set<String> getJavaModelActions() {
        return needJavaModelActions;
    }

    @Override
    protected boolean isCompileOnSaveEnabled() {
        return false;
    }

    @Override
    public String[] getSupportedActions() {
        return supportedActions;
    }

    @Override
    @CheckForNull
    protected JavaPlatform getProjectPlatform() {
        return CommonProjectUtils.getActivePlatform(
            project.evaluator().getProperty(ProjectProperties.PLATFORM_ACTIVE),
            J2MEProjectProperties.PLATFORM_TYPE_J2ME);
    }

    @Override
    protected String getProjectMainClass(boolean verify) {
        final String rawMidlets = project.evaluator().getProperty(J2MEProjectProperties.MANIFEST_MIDLETS);
        if (rawMidlets == null || rawMidlets.isEmpty()) {
            return null;
        }
        final List<String> midlets = new ArrayList<String>();
        final StringTokenizer tk = new StringTokenizer(rawMidlets,"\n");  //NOI18N
        while (tk.hasMoreTokens()) {
            String line = tk.nextToken().trim();
            String[] lineParts = line.split("\\s*,\\s*");
            if (lineParts.length == 3) {
                midlets.add(lineParts[2]);
            }            
        }

        if (!verify) {
            return midlets.isEmpty() ?
                null :
                midlets.iterator().next();
        }
        final FileObject[] sourcesRoots = project.getSourceRoots().getRoots();
        ClassPath bootPath;
        ClassPath sysPath;
        ClassPath srcPath;
        if (sourcesRoots.length > 0) {
            LOG.log(
                Level.FINE,
                "Searching main class {0} using source root {1}",   //NOI18N
                new Object[] {
                    rawMidlets,
                    FileUtil.getFileDisplayName(sourcesRoots[0])
                });
            bootPath = ClassPath.getClassPath (sourcesRoots[0], ClassPath.BOOT);            
            if (bootPath == null) {
                bootPath = project.getClassPathProvider().getProjectSourcesClassPath(ClassPath.BOOT);
            }
            sysPath = ClassPath.getClassPath(sourcesRoots[0], ClassPath.EXECUTE);
            if (sysPath == null) {
                sysPath = project.getClassPathProvider().getProjectSourcesClassPath(ClassPath.EXECUTE);
            }
            srcPath = ClassPath.getClassPath(sourcesRoots[0], ClassPath.SOURCE);
        } else {
            LOG.log(
                Level.FINE,
                "Searching main class {0} without source root",   //NOI18N
                rawMidlets);
            bootPath = project.getClassPathProvider().getProjectSourcesClassPath(ClassPath.BOOT);
            sysPath = project.getClassPathProvider().getProjectSourcesClassPath(ClassPath.EXECUTE);
            srcPath = project.getClassPathProvider().getProjectSourcesClassPath(ClassPath.SOURCE);   //Empty ClassPath
        }
        LOG.log(
            Level.FINE,
            "Classpaths used to resolve main class boot: {0}, exec: {1}, src: {2}",   //NOI18N
            new Object[]{
                bootPath,
                sysPath,
                srcPath
            });
        final JavaSource js = JavaSource.create(ClasspathInfo.create(bootPath, sysPath, srcPath));
        if (js == null) {
            return rawMidlets;
        }
        try {
            final String[] res = new String[1];
            js.runUserActionTask(new Task<CompilationController>() {
                @Override
                public void run(@NonNull final CompilationController cc) throws Exception {
                    final Elements e = cc.getElements();
                    final Types t = cc.getTypes();
                    final TypeElement midlet = e.getTypeElement(MIDLET);
                    if (midlet != null) {
                        for (String fqn : midlets) {
                            final TypeElement main = e.getTypeElement(fqn);
                            if (main != null) {
                                if (t.isSubtype(main.asType(), midlet.asType())) {
                                   res[0] = fqn;
                                   break;
                                }
                            }
                        }
                    }
                }
            }, true);
            LOG.log(
                Level.FINE,
                "Main class {0} valid: {1}",   //NOI18N
                new Object[] {
                    rawMidlets,
                    res[0]});   //NOI18N
            return res[0];
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return rawMidlets;
        }        
    }

    @Override
    protected boolean showMainClassSelector() {
        final ManageMIDlets mm = new ManageMIDlets(project);
        DialogDescriptor dd = new DialogDescriptor(
            mm,
            NbBundle.getMessage(
                J2MEActionProvider.class,
                "TXT_RunProject",
                ProjectUtils.getInformation(project).getDisplayName()),
            true,
            DialogDescriptor.OK_CANCEL_OPTION,
            DialogDescriptor.OK_OPTION,
            null);
        if (DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION) {
            return mm.store();
        }
        return false;
    }

    public static final class CallbackImpl implements Callback3 {

        private final ClassPathProviderImpl cp;
        private final J2MEProject project;

        public CallbackImpl(ClassPathProviderImpl cp, @NonNull final J2MEProject project) {
            this.cp = cp;
            this.project = project;
        }

        @Override
        public ClassPath getProjectSourcesClassPath(String type) {
            return cp.getProjectSourcesClassPath(type);
        }

        @Override
        public ClassPath findClassPath(FileObject file, String type) {
            return cp.findClassPath(file, type);
        }

        @Override
        public Map<String, String> createAdditionalProperties(String command, Lookup context) {
            final Map<String, String> result = new HashMap<>();
            if (command.equals(COMMAND_RUN)) {
                PropertyEvaluator pe = project.evaluator();
                final String runMethod = pe.getProperty(J2MEProjectProperties.PROP_RUN_METHOD);
                if (runMethod != null && runMethod.equals("OTA")) { //NOI18N
                    String url = J2MEProjectUtils.getJadURL(project.getHelper());
                    if (url != null) {
                        result.put("dist.jad.url", url); //NOI18N
                    }
                }
            }
            return result;
        }

        @Override
        public Set<String> createConcealedProperties(String command, Lookup context) {
            return Collections.emptySet();
        }

        @Override
        public void antTargetInvocationStarted(String command, Lookup context) {
        }

        @Override
        public void antTargetInvocationFinished(String command, Lookup context, int result) {
        }

        @Override
        public void antTargetInvocationFailed(String command, Lookup context) {
        }
    }
}
