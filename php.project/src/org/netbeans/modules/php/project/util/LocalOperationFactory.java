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
package org.netbeans.modules.php.project.util;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.api.util.Pair;
import org.netbeans.modules.php.project.ui.Utils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Radek Matous
 */
final class LocalOperationFactory extends FileOperationFactory {

    private static final Logger LOGGER = Logger.getLogger(LocalOperationFactory.class.getName());
    private static final boolean IS_WARNING_LOGGABLE = LOGGER.isLoggable(Level.WARNING);
    private static final boolean IS_FINE_LOGGABLE = LOGGER.isLoggable(Level.FINE);

    LocalOperationFactory(PhpProject project) {
        super(project);
    }

    private boolean isEnabledAndValidConfig() {
        boolean copySourcesEnabled = ProjectPropertiesSupport.isCopySourcesEnabled(project);
        if (!copySourcesEnabled) {
            return false;
        }

        if (getSources() == null) {
            if (IS_WARNING_LOGGABLE) {
                LOGGER.warning(String.format("Copy support disabled %s. Reason: %s", project.getName(), "source root is null"));
            }
            return false;
        }

        File targetRoot = getTargetRoot();
        if (targetRoot == null) {
            if (IS_WARNING_LOGGABLE) {
                LOGGER.warning(String.format("Copy support disabled %s. Reason: %s", project.getName(), "target root is null"));
            }
            return false;
        }

        File writableFolder = targetRoot;
        while (writableFolder != null && !writableFolder.exists()) {
            writableFolder = writableFolder.getParentFile();
        }

        boolean isWritable = writableFolder != null && Utils.isFolderWritable(writableFolder);
        if (!isWritable) {
            if (IS_WARNING_LOGGABLE) {
                LOGGER.warning(String.format("Copy support disabled %s. Reason: %s", project.getName(), "target root isn't writable"));
            }
            return false;
        }

        return true;
    }

    @Override
    Callable<Boolean> createInitHandler(final FileObject source) {
        LOGGER.log(Level.FINE, "Creating INIT handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                LOGGER.log(Level.FINE, "Running INIT handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
                File target = getTarget(source);
                if (target == null) {
                    LOGGER.log(Level.FINE, "Ignored for {0} (no target)", getPath(source));
                    return null;
                }

                if (!target.exists()) {
                    FileUtil.createFolder(target);
                    if (!target.isDirectory()) {
                        LOGGER.log(Level.FINE, "Failed for {0}, cannot create directory {1}", new Object[] {getPath(source), target});
                        return false;
                    }
                    LOGGER.log(Level.FINE, "Directory {0} created", target);
                }
                String[] list = target.list();
                if (list != null && target.list().length == 0) {
                    Enumeration<? extends FileObject> children = source.getChildren(true);
                    while (children.hasMoreElements()) {
                        FileObject chld = children.nextElement();
                        target = getTarget(chld, false);
                        if (target == null) {
                            LOGGER.log(Level.FINE, "Ignored for {0} (no target)", getPath(chld));
                            continue;
                        }
                        if (!doCopy(chld, target)) {
                            return false;
                        }
                    }
                }
                return true;
            }
        };
    }

    @Override
    Callable<Boolean> createCopyHandler(final FileObject source) {
        LOGGER.log(Level.FINE, "Creating COPY handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                LOGGER.log(Level.FINE, "Running COPY handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
                File target = getTarget(source);
                if (target == null) {
                    LOGGER.log(Level.FINE, "Ignored for {0} (no target)", getPath(source));
                    return null;
                }
                return doCopy(source, target);
            }
        };
    }

    @Override
    Callable<Boolean> createRenameHandler(final FileObject source, final String oldName) {
        LOGGER.log(Level.FINE, "Creating RENAME handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                LOGGER.log(Level.FINE, "Running RENAME handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
                File target = getTarget(source);
                if (target == null) {
                    LOGGER.log(Level.FINE, "Ignored for {0} (no target)", getPath(source));
                    return null;
                }

                if (source.isFolder()) {
                    Enumeration<? extends FileObject> children = source.getChildren(true);
                    while (children.hasMoreElements()) {
                        FileObject child = children.nextElement();
                        final File childTarget = getTarget(child, false);
                        if (childTarget != null
                                && !doCopy(child, childTarget)) {
                            return false;
                        }
                    }
                } else {
                    if (!doCopy(source, target)) {
                        return false;
                    }
                }
                // delete the old file/directory
                File parent = target.getParentFile();
                if (parent != null) {
                    File oldTarget = new File(parent, oldName);
                    return doDelete(oldTarget);
                }
                return true;
            }
        };
    }

    @Override
    Callable<Boolean> createDeleteHandler(final FileObject source) {
        LOGGER.log(Level.FINE, "Creating DELETE handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                LOGGER.log(Level.FINE, "Running DELETE handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
                File target = getTarget(source);
                if (target == null) {
                    LOGGER.log(Level.FINE, "Ignored for {0} (no target)", getPath(source));
                    return null;
                }
                return doDelete(target);
            }
        };
    }

    private File getTargetRoot() {
        return ProjectPropertiesSupport.getCopySourcesTarget(project);
    }

    private Pair<FileObject, File> getConfigPair() {
        return Pair.of(getSources(), getTargetRoot());
    }

    private File getTarget(FileObject source) {
        return getTarget(source, true);
    }

    private File getTarget(FileObject source, boolean deepCheck) {
        LOGGER.log(Level.FINE, "Getting target for {0} (project {1}, deep check: {2})", new Object[] {getPath(source), project.getName(), deepCheck});
        FileObject sources = getSources();
        Pair<FileObject, File> cfgPair = getConfigPair();

        if (deepCheck) {
            if (!isEnabledAndValidConfig()) {
                LOGGER.fine("\t-> null (invalid config)");
                return null;
            }
            if (!isPairValid(cfgPair)) {
                LOGGER.fine("\t-> null (invalid config pair)");
                return null;
            }
        }
        if (!isSourceFileValid(sources, source)) {
            LOGGER.fine("\t-> null (invalid source)");
            return null;
        }

        FileObject sourceRoot = cfgPair.first;
        File targetRoot = cfgPair.second;
        assert sourceRoot != null;
        assert targetRoot != null;

        String relativePath = FileUtil.getRelativePath(sourceRoot, source);
        assert relativePath != null : String.format("Relative path be found because isSourceFileValid() was already called for %s", getPath(source));
        LOGGER.fine("\t-> found");
        return FileUtil.normalizeFile(new File(targetRoot, relativePath));
    }

    private static boolean doCopy(FileObject source, File target) throws IOException {
        File targetParent = target.getParentFile();
        if (FileOperationFactory.isNbProjectMetadata(source)) return true;
        if (source.isData()) {
            doDelete(target);
            FileObject parent = FileUtil.createFolder(targetParent);
            FileUtil.copyFile(source, parent, source.getName(), source.getExt());
        } else {
            String[] childs = target.list();
            if (childs == null || childs.length == 0) {
                doDelete(target);
            }
            FileUtil.createFolder(target);
        }
        if (IS_FINE_LOGGABLE) {
            LOGGER.fine((target.exists() ? "file copied: " : "!file not copied: ") + target.getAbsolutePath());//NOI18N
        } else if ((IS_WARNING_LOGGABLE && !target.exists())) {
            LOGGER.warning("!file not copied: " + target.getAbsolutePath());//NOI18N
        }
        return target.exists();
    }

    private static boolean doDelete(File target) throws IOException {
        if (target.exists()) {
            FileObject targetFo = FileUtil.toFileObject(target);
            if (targetFo != null && targetFo.isValid()) {
                targetFo.delete();
            } else {
                target.delete();
            }
            if (IS_FINE_LOGGABLE) {
                LOGGER.fine((!target.exists() ? "file deleted: " : "!file not deleted: ") + target.getAbsolutePath());//NOI18N
            } else if ((IS_WARNING_LOGGABLE && target.exists())) {
                LOGGER.warning("!file not deleted: " + target.getAbsolutePath());//NOI18N
            }

            return !target.exists();
        }
        return true;
    }

    private static boolean isPairValid(Pair<FileObject, File> pair) {
        return pair != null && pair.first != null && pair.second != null;
    }

    @Override
    void invalidate() {
        // ignored
    }
}
