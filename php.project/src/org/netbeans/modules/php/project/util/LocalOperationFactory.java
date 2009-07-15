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
    private final PhpProject project;

    LocalOperationFactory(PhpProject project) {
        if (project == null) {
            throw new IllegalArgumentException("project can't be null");
        }
        this.project = project;
    }

    private boolean isEnabledAndValidConfig() {
        assert project != null;
        boolean copySourcesEnabled = ProjectPropertiesSupport.isCopySourcesEnabled(project);
        if (!copySourcesEnabled) return false;

        FileObject sourceRoot = ProjectPropertiesSupport.getSourcesDirectory(project);
        if (sourceRoot == null) {
            if (IS_WARNING_LOGGABLE) {
                LOGGER.warning(String.format("Copy support disabled %s. Reason: %s", project.getName(), "source root is null"));//NOI18N
            }
            return false;
        }

        File targetRoot = getTargetRoot(project);
        if (targetRoot == null) {
            if (IS_WARNING_LOGGABLE) {
                LOGGER.warning(String.format("Copy support disabled %s. Reason: %s", project.getName(), "target root is null"));//NOI18N
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
                LOGGER.warning(String.format("Copy support disabled %s. Reason: %s", project.getName(), "target root isn't writable"));//NOI18N
            }
            return false;
        }
        
        return true;
    }
    
    @Override
    Callable<Boolean> createCopyHandler(final FileObject source) {
        Callable<Boolean> retval = (isEnabledAndValidConfig()) ? new Callable<Boolean>() {
            public Boolean call() throws Exception {
                FileObject sourcesDirectory = ProjectPropertiesSupport.getSourcesDirectory(project);
                Pair<FileObject, File> cfgPair = Pair.of(sourcesDirectory, getTargetRoot(project));
                boolean sourceFileValid = isPairValid(cfgPair) && isSourceFileValid(cfgPair.first, source);
                final File target = sourceFileValid ? getTarget(cfgPair, source) : null;
                return (target != null) ? doCopy(source, target) : false;
            }
        } : null;
        if (IS_FINE_LOGGABLE) {
            String format = retval != null ? "Copying file \"%s\" from project \"%s\" is scheduled." ://NOI18N
                "!Copying file \"%s\" from project \"%s\" isn't scheduled.";//NOI18N
            LOGGER.fine(String.format(format, FileUtil.getFileDisplayName(source), project.getName()));
        }
        return retval;
    }

    @Override
    Callable<Boolean> createDeleteHandler(final FileObject source) {
        Callable<Boolean> retval = (isEnabledAndValidConfig()) ? new Callable<Boolean>() {

            public Boolean call() throws Exception {
                FileObject sourcesDirectory = ProjectPropertiesSupport.getSourcesDirectory(project);
                Pair<FileObject, File> cfgPair = Pair.of(sourcesDirectory, getTargetRoot(project));
                boolean sourceFileValid = isPairValid(cfgPair) && isSourceFileValid(cfgPair.first, source);
                final File target = sourceFileValid ? getTarget(cfgPair, source) : null;
                return (target != null) ? doDelete(target) : false;
            }
        } : null;
        if (IS_FINE_LOGGABLE) {
            String format = retval != null ? "Deleting file \"%s\" from project \"%s\" is scheduled." ://NOI18N
                "!Deleting file \"%s\" from project \"%s\" isn't scheduled.";//NOI18N
            LOGGER.fine(String.format(format, FileUtil.getFileDisplayName(source), project.getName()));
        }
        return retval;
    }

    @Override
    Callable<Boolean> createInitHandler(final FileObject source) {
        Callable<Boolean> retval = (isEnabledAndValidConfig()) ? new Callable<Boolean>() {
            public Boolean call() throws Exception {
                FileObject sourcesDirectory = ProjectPropertiesSupport.getSourcesDirectory(project);
                Pair<FileObject, File> cfgPair = Pair.of(sourcesDirectory, getTargetRoot(project));
                boolean sourceFileValid = isPairValid(cfgPair) && isSourceFileValid(cfgPair.first, source);
                if (sourceFileValid) {
                    File target = getTarget(cfgPair, source);
                    if (target != null && !target.exists()) {
                        FileUtil.createFolder(target);
                    }
                    if (target != null && target.exists() && target.list().length == 0) {
                        Enumeration<? extends FileObject> children = source.getChildren(true);
                        while (children.hasMoreElements()) {
                            FileObject chld = children.nextElement();
                            target = getTarget(cfgPair, chld);
                            if (target != null) {
                                if (!doCopy(chld, target)) {
                                    return false;
                                }
                            }
                        }
                        return true;
                    }
                }
                return false;
            }
        } : null;
        if (IS_FINE_LOGGABLE) {
            String format = retval != null ? "Initialization of folder \"%s\" from project \"%s\" is scheduled." ://NOI18N
                "!Initialization of folder \"%s\" from project \"%s\" isn't scheduled.";//NOI18N
            LOGGER.fine(String.format(format, FileUtil.getFileDisplayName(source), project.getName()));
        }
        return retval;
    }

    @Override
    Callable<Boolean> createRenameHandler(final FileObject source, final String oldName) {
        Callable<Boolean> retval = (isEnabledAndValidConfig()) ? new Callable<Boolean>() {

            public Boolean call() throws Exception {
                FileObject sourcesDirectory = ProjectPropertiesSupport.getSourcesDirectory(project);
                Pair<FileObject, File> cfgPair = Pair.of(sourcesDirectory, getTargetRoot(project));
                boolean sourceFileValid = isPairValid(cfgPair) && isSourceFileValid(cfgPair.first, source);
                File target = sourceFileValid && isPairValid(cfgPair) ? getTarget(cfgPair, source) : null;
                if (target != null) {
                    if (source.isFolder()) {
                        FileObject[] children = source.getChildren();
                        for (FileObject child : children) {
                            final File childTarget = sourceFileValid && isPairValid(cfgPair) ? getTarget(cfgPair, child) : null;
                            if (childTarget != null) {
                                if (!doCopy(child, childTarget)) {
                                    return false;
                                }
                            }
                        }
                    } else {
                        if (target != null) {
                            if (!doCopy(source, target)) {
                                return false;
                            }
                        }
                    }
                    target = target.getParentFile();
                    if (target != null) {
                        target = new File(target, oldName);
                        return (target.exists()) ? doDelete(target) : false;
                    }
                }
                return false;
            }
        } : null;
        if (IS_FINE_LOGGABLE) {
            String format = retval != null ? "Renaming file \"%s\" from project \"%s\" is scheduled." ://NOI18N
                "!Renaming file \"%s\" from project \"%s\" isn't scheduled.";//NOI18N
            LOGGER.fine(String.format(format, FileUtil.getFileDisplayName(source), project.getName()));
        }
        return retval;
    }

    private static File getTargetRoot(PhpProject project) {
        return ProjectPropertiesSupport.getCopySourcesTarget(project);
    }

    private static File getTarget(Pair<FileObject, File> cfgPair, FileObject source) {
        FileObject sourceRoot = cfgPair.first;
        File targetRoot = cfgPair.second;
        File target = null;
        if (sourceRoot != null && targetRoot != null) {
            String relativePath = FileUtil.getRelativePath(sourceRoot, source);
            if (relativePath != null) {
                assert targetRoot != null;
                target = FileUtil.normalizeFile(new File(targetRoot, relativePath));
            }
        }
        return target;
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
        return false;
    }

    private static boolean isPairValid(Pair<FileObject, File> pair) {
        return pair != null && pair.first != null && pair.second != null;
    }

    @Override
    void invalidate() {
        // ignored
    }
}
