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

package org.netbeans.modules.php.project;

import java.beans.PropertyChangeListener;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.RunAsType;
import org.netbeans.modules.php.project.util.PhpInterpreter;
import java.io.File;
import org.netbeans.modules.php.project.api.PhpLanguageOptions;
import org.netbeans.modules.php.project.ui.customizer.CompositePanelProviderImpl;
import org.netbeans.modules.php.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.ui.customizer.RunAsValidator;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Helper class for getting <b>all</b> the properties of a PHP project.
 * <p>
 * <b>This class is the preferred way to get PHP project properties.</b>
 * </p>
 * <p>
 * Method {@link #isActiveConfigValid(org.netbeans.modules.php.project.PhpProject, boolean) isActiveConfigValid()}
 * could be called before getting any Run Configuration property. It's possible to show the project properties
 * dialog if the configuration is invalid.
 * @author Tomas Mysik
 */
public final class ProjectPropertiesSupport {

    private ProjectPropertiesSupport() {
    }

    /**
     * <b>This method should not be used, use other methods in this class.</b>
     * <p>
     * Use this method only if you don't want to show customizer automatically
     * or if you understand what you are doing ;)
     * @see #addWeakPropertyEvaluatorListener(org.netbeans.modules.php.project.PhpProject, java.beans.PropertyChangeListener)
     */
    public static PropertyEvaluator getPropertyEvaluator(PhpProject project) {
        return project.getEvaluator();
    }

    public static void addWeakPropertyEvaluatorListener(PhpProject project, PropertyChangeListener listener) {
        project.addWeakPropertyEvaluatorListener(listener);
    }

    public static FileObject getProjectDirectory(PhpProject project) {
        return project.getProjectDirectory();
    }

    public static FileObject getSourcesDirectory(PhpProject project) {
        return project.getSourcesDirectory();
    }

    public static FileObject getWebRootDirectory(PhpProject project) {
        return getSourceSubdirectory(project, project.getEvaluator().getProperty(PhpProjectProperties.WEB_ROOT));
    }

    public static FileObject getSourceSubdirectory(PhpProject project, String subdirectoryPath) {
        FileObject subdirectory = project.getSourcesDirectory();
        if (subdirectoryPath != null && subdirectoryPath.trim().length() > 0 && !subdirectoryPath.equals(".")) { // NOI18N
            subdirectory = subdirectory.getFileObject(subdirectoryPath);
        }
        assert subdirectory != null : "Subdirectory " + subdirectoryPath + " must be found";
        return subdirectory;
    }

    public static PhpInterpreter getPhpInterpreter(PhpProject project) {
        String interpreter = project.getEvaluator().getProperty(PhpProjectProperties.INTERPRETER);
        if (interpreter != null && interpreter.length() > 0) {
            return new PhpInterpreter(interpreter);
        }
        return new PhpInterpreter(PhpOptions.getInstance().getPhpInterpreter());
    }

    public static boolean isCopySourcesEnabled(PhpProject project) {
        return getBoolean(project, PhpProjectProperties.COPY_SRC_FILES, false);
    }

    /**
     * @return file or <code>null</code>.
     */
    public static File getCopySourcesTarget(PhpProject project) {
        String targetString = project.getEvaluator().getProperty(PhpProjectProperties.COPY_SRC_TARGET);
        if (targetString != null && targetString.trim().length() > 0) {
            return FileUtil.normalizeFile(new File(targetString));
        }
        return null;
    }

    public static String getEncoding(PhpProject project) {
        return project.getEvaluator().getProperty(PhpProjectProperties.SOURCE_ENCODING);
    }

    public static boolean areShortTagsEnabled(PhpProject project) {
        return getBoolean(project, PhpProjectProperties.SHORT_TAGS, PhpLanguageOptions.SHORT_TAGS_ENABLED);
    }

    public static boolean areAspTagsEnabled(PhpProject project) {
        return getBoolean(project, PhpProjectProperties.ASP_TAGS, PhpLanguageOptions.ASP_TAGS_ENABLED);
    }

    /** validates the active config and return <code>true</code> if it's OK */
    public static boolean isActiveConfigValid(PhpProject project, boolean showCustomizer) {
        boolean valid = validateActiveConfig(project);
        if (!valid && showCustomizer) {
            project.getLookup().lookup(CustomizerProviderImpl.class).showCustomizer(CompositePanelProviderImpl.RUN);
        }
        return valid;
    }

    private static boolean validateActiveConfig(PhpProject project) {
        RunAsType runAs = getRunAs(project);
        if (runAs == null) {
            return false;
        }
        boolean valid = true;
        String indexFile = getIndexFile(project);
        switch (runAs) {
            case LOCAL:
                if (RunAsValidator.validateWebFields(getUrl(project), FileUtil.toFile(getWebRootDirectory(project)),
                        indexFile, getArguments(project)) != null) {
                    valid = false;
                } else if (indexFile == null) {
                    valid = false;
                }
                break;
            case REMOTE:
                if (RunAsValidator.validateWebFields(getUrl(project), FileUtil.toFile(getWebRootDirectory(project)),
                        getIndexFile(project), getArguments(project)) != null) {
                    valid = false;
                } else if (indexFile == null) {
                    valid = false;
                } else if (getRemoteConnection(project) == null) {
                    // XXX non-existing configuration is not handled (hardly can be)
                    valid = false;
                } else if (RunAsValidator.validateUploadDirectory(getRemoteDirectory(project), true) != null) {
                    valid = false;
                }
                break;
            case SCRIPT:
                if (RunAsValidator.validateScriptFields(getPhpInterpreter(project).getInterpreter(),
                        FileUtil.toFile(getSourcesDirectory(project)), getIndexFile(project), getArguments(project)) != null) {
                    valid = false;
                } else if (indexFile == null) {
                    valid = false;
                }
                break;
            default:
                assert false : "Invalid run configuration type: " + runAs;
        }
        return valid;
    }

    /**
     * @return run as type or <code>null</code>.
     */
    public static PhpProjectProperties.RunAsType getRunAs(PhpProject project) {
        PhpProjectProperties.RunAsType runAsType = null;
        String runAs = project.getEvaluator().getProperty(PhpProjectProperties.RUN_AS);
        if (runAs == null) {
            return null;
        }
        try {
            runAsType = PhpProjectProperties.RunAsType.valueOf(runAs);
        } catch (IllegalArgumentException iae) {
            // ignored
        }
        return runAsType;
    }

    /**
     * @return url or <code>null</code>.
     */
    public static String getUrl(PhpProject project) {
        return project.getEvaluator().getProperty(PhpProjectProperties.URL);
    }

    /**
     * @return index file or <code>null</code>.
     */
    public static String getIndexFile(PhpProject project) {
        return project.getEvaluator().getProperty(PhpProjectProperties.INDEX_FILE);
    }

    /**
     * @return arguments or <code>null</code>.
     */
    public static String getArguments(PhpProject project) {
        return project.getEvaluator().getProperty(PhpProjectProperties.ARGS);
    }

    /**
     * @return remote connection (configuration) name or <code>null</code>.
     */
    public static String getRemoteConnection(PhpProject project) {
        return project.getEvaluator().getProperty(PhpProjectProperties.REMOTE_CONNECTION);
    }

    /**
     * @return remote (upload) directory or <code>null</code>.
     */
    public static String getRemoteDirectory(PhpProject project) {
        return project.getEvaluator().getProperty(PhpProjectProperties.REMOTE_DIRECTORY);
    }

    /**
     * @return remote upload or <code>null</code>.
     */
    public static PhpProjectProperties.UploadFiles getRemoteUpload(PhpProject project) {
        PhpProjectProperties.UploadFiles uploadFiles = null;
        String remoteUpload = project.getEvaluator().getProperty(PhpProjectProperties.REMOTE_UPLOAD);
        assert remoteUpload != null;
        try {
            uploadFiles = PhpProjectProperties.UploadFiles.valueOf(remoteUpload);
        } catch (IllegalArgumentException iae) {
            // ignored
        }
        return uploadFiles;
    }

    private static boolean getBoolean(PhpProject project, String property, boolean defaultValue) {
        String boolValue = project.getEvaluator().getProperty(property);
        if (boolValue != null && boolValue.trim().length() > 0) {
            return Boolean.parseBoolean(boolValue);
        }
        return defaultValue;
    }
}
