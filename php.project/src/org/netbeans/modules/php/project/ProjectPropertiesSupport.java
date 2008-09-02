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

import org.netbeans.modules.php.project.util.PhpInterpreter;
import java.io.File;
import java.nio.charset.Charset;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Helper class for getting <b>all</b> the properties of a PHP project.
 * <p>
 * <b>This class should be the only way to get PHP project properties.</b>
 * @author Tomas Mysik
 */
public final class ProjectPropertiesSupport {

    private ProjectPropertiesSupport() {
    }

    public static FileObject getProjectDirectory(PhpProject project) {
        return project.getProjectDirectory();
    }

    public static FileObject getSourcesDirectory(PhpProject project) {
        return project.getSourcesDirectory();
    }

    public static FileObject getWebRootDirectory(PhpProject project) {
        String webRootPath = project.getEvaluator().getProperty(PhpProjectProperties.WEB_ROOT);
        FileObject webRoot = project.getSourcesDirectory();
        if (webRootPath != null && webRootPath.trim().length() > 0 && !webRootPath.equals(".")) { // NOI18N
            webRoot = project.getSourcesDirectory().getFileObject(webRootPath);
        }
        return webRoot;
    }

    public static PhpInterpreter getPhpInterpreter(PhpProject project) {
        String interpreter = project.getEvaluator().getProperty(PhpProjectProperties.INTERPRETER);
        if (interpreter != null && interpreter.length() > 0) {
            return new PhpInterpreter(interpreter);
        }
        return new PhpInterpreter(PhpOptions.getInstance().getPhpInterpreter());
    }

    public static boolean isCopySourcesEnabled(PhpProject project) {
        boolean retval = false;
        String copySrcFiles = project.getEvaluator().getProperty(PhpProjectProperties.COPY_SRC_FILES);
        if (copySrcFiles != null && copySrcFiles.trim().length() > 0) {
            retval = Boolean.parseBoolean(copySrcFiles);
        }
        return retval;
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

    public static Charset getEncoding(PhpProject project) {
        throw new UnsupportedOperationException();
    }

    public static Charset getUrl(PhpProject project) {
        throw new UnsupportedOperationException();
    }

    public static Charset getIndexFile(PhpProject project) {
        throw new UnsupportedOperationException();
    }

    public static Charset getIncludePath(PhpProject project) {
        throw new UnsupportedOperationException();
    }

    public static Charset getArguments(PhpProject project) {
        throw new UnsupportedOperationException();
    }

    public static Charset getRunAs(PhpProject project) {
        throw new UnsupportedOperationException();
    }

    public static Charset getRemoteConnection(PhpProject project) {
        throw new UnsupportedOperationException();
    }

    public static Charset getRemoteDirectory(PhpProject project) {
        throw new UnsupportedOperationException();
    }

    public static Charset getRemoteUpload(PhpProject project) {
        throw new UnsupportedOperationException();
    }
}
