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

package org.netbeans.modules.php.project.ui.actions.support;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.options.PHPOptionsCategory;
import org.netbeans.modules.php.project.util.Pair;
import org.netbeans.modules.php.project.util.PhpUnit;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class ConfigActionTest extends ConfigAction {
    private static final String CWD = "."; // NOI18N

    @Override
    public void invokeActionForProject(PhpProject project) {
        invokeAction(project, null);
    }

    @Override
    public void invokeActionForContext(PhpProject project, Lookup context) {
        invokeAction(project, context);
    }

    @Override
    public boolean isActionEnabledForProject(PhpProject project) {
        return true;
    }

    @Override
    public boolean isActionEnabledForContext(PhpProject project, Lookup context) {
        return true;
    }

    private void invokeAction(PhpProject project, Lookup context) {
        List<Pair<FileObject, String>> pairs = getPairs(project, context);
        if (pairs == null) {
            return;
        }

        final PhpUnit phpUnit = CommandUtils.getPhpUnit(true);
        if (phpUnit == null) {
            return;
        }
        final ExecutionDescriptor executionDescriptor = new ExecutionDescriptor()
                .controllable(true)
                .frontWindow(true)
                .showProgress(true)
                .optionsPath(PHPOptionsCategory.PATH_IN_LAYER);

        try {
            for (Pair<FileObject, String> pair : pairs) {
                ExternalProcessBuilder externalProcessBuilder = new ExternalProcessBuilder(phpUnit.getPhpUnit());
                externalProcessBuilder = externalProcessBuilder.workingDirectory(FileUtil.toFile(pair.first));
                externalProcessBuilder = externalProcessBuilder.addArgument(pair.second);
                ExecutionService service = ExecutionService.newService(
                        externalProcessBuilder,
                        executionDescriptor,
                        NbBundle.getMessage(ConfigActionTest.class, "LBL_RunPhpUnitTests"));
                Future<Integer> result = service.run();
                result.get();
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            CommandUtils.processExecutionException(ex);
        }
    }

    // <working directory, script name>
    private List<Pair<FileObject, String>> getPairs(PhpProject project, Lookup context) {
        if (context == null) {
            return getProjectPair(project);
        }
        // XXX individual tests
        return null;
    }

    private List<Pair<FileObject, String>> getProjectPair(PhpProject project) {
        FileObject testDirectory = ProjectPropertiesSupport.getTestDirectory(project, true);
        if (testDirectory == null) {
            return null;
        }
        return Collections.singletonList(Pair.of(testDirectory, CWD));
    }
}
