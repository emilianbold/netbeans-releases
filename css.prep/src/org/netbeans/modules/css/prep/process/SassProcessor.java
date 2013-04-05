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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.indexing.api.CssIndex;
import org.netbeans.modules.css.prep.preferences.SassPreferences;
import org.netbeans.modules.css.prep.sass.SassExecutable;
import org.netbeans.modules.css.prep.util.InvalidExternalExecutableException;
import org.netbeans.modules.css.prep.util.UiUtils;
import org.netbeans.modules.css.prep.util.Warnings;
import org.netbeans.modules.web.common.api.DependenciesGraph;
import org.openide.filesystems.FileObject;

/**
 * Process file/folder changes.
 */
public final class SassProcessor {

    private static final Logger LOGGER = Logger.getLogger(SassProcessor.class.getName());

    private static final String SCSS_EXTENSION = "scss"; // NOI18N
    private static final String SASS_EXTENSION = "sass"; // NOI18N
    private static final String CSS_EXTENSION = "css"; // NOI18N


    public void process(Project project, FileObject fileObject) {
        if (fileObject.isData()) {
            processFile(project, fileObject, true);
        } else {
            assert fileObject.isFolder() : "Folder expected: " + fileObject;
            if (!isEnabled(project)) {
                // not enabled in this project
                return;
            }
            processFolder(project, fileObject);
        }
    }

    private void processFolder(Project project, FileObject fileObject) {
        assert fileObject.isFolder() : "Folder expected: " + fileObject;
        for (FileObject child : fileObject.getChildren()) {
            if (child.isData()) {
                processFile(project, child, false);
            } else {
                processFolder(project, child);
            }
        }
    }

    private void processFile(Project project, FileObject fileObject, boolean checkEnabled) {
        assert fileObject.isData() : "File expected: " + fileObject;
        if (!isSassFile(fileObject)) {
            // not sass file
            return;
        }
        if (checkEnabled
                && !isEnabled(project)) {
            // not enabled in this project
            return;
        }
        if (fileObject.isValid()) {
            fileChanged(project, fileObject);
        } else {
            // deleted file
            fileDeleted(project, fileObject);
        }
    }

    private boolean isEnabled(Project project) {
        if (project == null) {
            return true;
        }
        return SassPreferences.isEnabled(project);
    }

    private boolean isSassFile(FileObject fileObject) {
        // XXX mime type?
        String extension = fileObject.getExt().toLowerCase();
        return SASS_EXTENSION.equals(extension)
                || SCSS_EXTENSION.equals(extension);
    }

    private void fileChanged(Project project, FileObject fileObject) {
        if (!fileObject.getName().startsWith("_")) { // NOI18N
            compileFile(fileObject);
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
                if (fileObject.equals(referring)) {
                    // file itself, ignore it
                    continue;
                }
                assert isSassFile(referring) : "Sass file expected: " + referring;
                compileFile(referring);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }

    private void compileFile(FileObject fileObject) {
        SassExecutable sass = getSass();
        if (sass == null) {
            return;
        }
        sass.compile(fileObject);
    }

    private void fileDeleted(Project project, FileObject fileObject) {
        FileObject cssFile = fileObject.getParent().getFileObject(fileObject.getName(), CSS_EXTENSION);
        if (cssFile != null
                && cssFile.isValid()) {
            try {
                cssFile.delete();
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, "Cannot delete file", ex);
            }
        }
    }

    @CheckForNull
    private SassExecutable getSass() {
        try {
            return SassExecutable.getDefault();
        } catch (InvalidExternalExecutableException ex) {
            if (Warnings.showSassWarning()) {
                UiUtils.invalidScriptProvided(ex.getLocalizedMessage());
            }
        }
        return null;
    }

}
