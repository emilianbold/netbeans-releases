/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.project;

import java.io.IOException;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Skeleton of JFX Action Provider
 */
public class JFXActionProvider implements ActionProvider {

    private final Project prj;

    private static String[] ACTIONS =  new String[] {
        COMMAND_RUN
    };

    private JFXActionProvider(@NonNull final Project project) {
        this.prj = project;
    }

    @Override
    @NonNull
    public String[] getSupportedActions() {
        return ACTIONS;
    }

    @Override
    public void invokeAction(@NonNull String command, @NonNull Lookup context) throws IllegalArgumentException {
        if (ACTIONS[0].equals(command)) {
            FileObject buildFo = findBuildXml();
            assert buildFo != null && buildFo.isValid();
            try {
                final Properties p = new Properties();
                ActionUtils.runTarget(buildFo, new String[] {"run"}, p);    //NOI18N
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            throw new IllegalArgumentException(command);
        }
    }

    @Override
    public boolean isActionEnabled(@NonNull String command, @NonNull Lookup context) throws IllegalArgumentException {
        if (!JFXProjectUtils.isFXProject(prj)) {
            return false;
        }
        if (findBuildXml() == null) {
            return false;
        }
        return ACTIONS[0].equals(command);
    }

    @NonNull
    private static String getBuildXmlName (@NonNull final PropertyEvaluator evaluator) {
        String buildScriptPath = evaluator.getProperty("buildfile");    //NOI18N
        if (buildScriptPath == null) {
            buildScriptPath = GeneratedFilesHelper.BUILD_XML_PATH;
        }
        return buildScriptPath;
    }

    @CheckForNull
    private FileObject findBuildXml () {
        final J2SEPropertyEvaluator ep = prj.getLookup().lookup(J2SEPropertyEvaluator.class);
        assert ep != null;
        return prj.getProjectDirectory().getFileObject (getBuildXmlName(ep.evaluator()));
    }

    /**
     * position=90
     */
    public static class Registration implements LookupProvider {
        @Override
        @NonNull
        public Lookup createAdditionalLookup(@NonNull Lookup baseContext) {
            final Project project = baseContext.lookup(Project.class);
            final JFXActionProvider jfxActionProvider = new JFXActionProvider(project);
            return Lookups.fixed(jfxActionProvider);
        }

    }
}
