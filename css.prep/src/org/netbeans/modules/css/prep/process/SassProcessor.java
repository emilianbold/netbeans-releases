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
package org.netbeans.modules.css.prep.process;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.indexing.api.CssIndex;
import org.netbeans.modules.css.prep.CssPreprocessorType;
import org.netbeans.modules.css.prep.editor.CPUtils;
import org.netbeans.modules.css.prep.preferences.SassPreferences;
import org.netbeans.modules.css.prep.sass.SassCssPreprocessor;
import org.netbeans.modules.css.prep.sass.SassExecutable;
import org.netbeans.modules.css.prep.util.InvalidExternalExecutableException;
import org.netbeans.modules.css.prep.util.UiUtils;
import org.netbeans.modules.css.prep.util.Warnings;
import org.netbeans.modules.web.common.api.DependenciesGraph;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;

public final class SassProcessor extends BaseProcessor {

    private static final Logger LOGGER = Logger.getLogger(SassProcessor.class.getName());


    public SassProcessor(SassCssPreprocessor cssPreprocessor) {
        super(cssPreprocessor);
    }

    @Override
    protected boolean isEnabledInternal(Project project) {
        return SassPreferences.getInstance().isEnabled(project);
    }

    @Override
    protected boolean isSupportedFile(FileObject fileObject) {
        return CPUtils.SCSS_FILE_MIMETYPE.equals(FileUtil.getMIMEType(fileObject, CPUtils.SCSS_FILE_MIMETYPE));
    }

    @Override
    protected List<Pair<String, String>> getMappings(Project project) {
        return SassPreferences.getInstance().getMappings(project);
    }

    @Override
    protected void fileChanged(Project project, FileObject fileObject) {
        if (!isPartial(fileObject)) {
            super.fileChanged(project, fileObject);
            return;
        }
        // it is include
        if (project == null) {
            // we need project for dependencies
            LOGGER.log(Level.FINE, "Cannot compile 'import' file {0}, no project", fileObject);
            return;
        }
        try {
            DependenciesGraph dependenciesGraph = CssIndex.get(project).getDependencies(fileObject);
            for (FileObject referring : dependenciesGraph.getAllReferingFiles()) {
                if (isPartial(referring)) {
                    // ignore partials
                    continue;
                }
                assert isSupportedFile(referring) : "Sass file expected: " + referring;
                super.fileChanged(project, referring);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }

    @Override
    protected void compileInternal(Project project, File source, File target) {
        SassExecutable sass = getSass(project);
        if (sass == null) {
            return;
        }
        try {
            sass.compile(source, target);
        } catch (ExecutionException ex) {
            if (Warnings.showWarning(CssPreprocessorType.SASS)) {
                UiUtils.processExecutionException(ex);
            }
        }
    }

    @CheckForNull
    private SassExecutable getSass(Project project) {
        try {
            return SassExecutable.getDefault();
        } catch (InvalidExternalExecutableException ex) {
            cssPreprocessor.fireProcessingErrorOccured(project, ex.getLocalizedMessage());
        }
        return null;
    }

    private boolean isPartial(FileObject fileObject) {
        return fileObject.getName().startsWith("_"); // NOI18N
    }

}
