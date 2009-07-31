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

package org.netbeans.modules.php.project.ui.customizer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.connections.TransferFile;
import org.netbeans.modules.php.project.ui.Utils;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Helper class for validating {@link RunAsLocalWeb}, {@link RunAsRemoteWeb} and {@link RunAsScript}.
 * @author Tomas Mysik
 */
public final class RunAsValidator {

    private RunAsValidator() {
    }

    /**
     * Validate given parameters and return an error message or <code>null</code> if everything is OK.
     * @param url URL to validate, must end with "/" (slash), can be <code>null</code>.
     * @param webRoot parent directory of the indexFile.
     * @param indexFile file name or even relative file path (probably to sources) to validate, can be <code>null</code>.
     * @param arguments arguments to validate, can be <code>null</code>.
     * @return an error message or <code>null</code> if everything is OK.
     */
    public static String validateWebFields(String url, File webRoot, String indexFile, String arguments) {
        String err = null;
        if (!Utils.isValidUrl(url)) {
            err = NbBundle.getMessage(RunAsValidator.class, "MSG_InvalidUrl");
        } else if (!url.endsWith("/")) { // NOI18N
            err = NbBundle.getMessage(RunAsValidator.class, "MSG_UrlNotTrailingSlash");
        } else {
            err = validateIndexFile(webRoot, indexFile, arguments);
        }
        return err;
    }

    /**
     * Validate given parameters and return an error message or <code>null</code> if everything is OK.
     * @param phpInterpreter PHP interpreter path to validate.
     * @param projectDirectory parent directory of the indexFile.
     * @param indexFile file name or even relative file path (probably to sources) to validate, can be <code>null</code>.
     * @param arguments arguments to validate, can be <code>null</code>.
     * @return an error message or <code>null</code> if everything is OK.
     */
    public static String validateScriptFields(String phpInterpreter, File projectDirectory, String indexFile, String arguments) {
        String err = Utils.validatePhpInterpreter(phpInterpreter);
        if (err != null) {
            return err;
        }
        return validateIndexFile(projectDirectory, indexFile, arguments);
    }

    private static final String INVALID_SEPARATOR = "\\";
    public static String validateUploadDirectory(String uploadDirectory, boolean allowEmpty) {
        if (allowEmpty && (uploadDirectory == null || uploadDirectory.trim().length() == 0)) {
            return null;
        }

        if (uploadDirectory == null || uploadDirectory.trim().length() == 0) {
            return NbBundle.getMessage(RunAsValidator.class, "MSG_MissingUploadDirectory");
        } else if (!uploadDirectory.startsWith(TransferFile.SEPARATOR)) {
            return NbBundle.getMessage(RunAsValidator.class, "MSG_InvalidUploadDirectoryStart", TransferFile.SEPARATOR);
        } else if (uploadDirectory.length() > 1
                && uploadDirectory.endsWith(TransferFile.SEPARATOR)) {
            return NbBundle.getMessage(RunAsValidator.class, "MSG_InvalidUploadDirectoryEnd", TransferFile.SEPARATOR);
        } else if (uploadDirectory.contains(INVALID_SEPARATOR)) {
            return NbBundle.getMessage(RunAsValidator.class, "MSG_InvalidUploadDirectoryContent", INVALID_SEPARATOR);
        }
        return null;
    }

    /**
     * Validate given parameters and return an error message or <code>null</code> if everything is OK.
     * @param parentDirectory parent directory of the indexFile.
     * @param indexFile file name or even relative file path (to webRoot) to validate, can be <code>null</code>.
     *                  <b>File separator can be only "/". If it is <code>null</code> then no error message is returned.</b>
     * @param arguments arguments to validate, can be <code>null</code>.
     * @return an error message or <code>null</code> if everything is OK.
     */
    public static String validateIndexFile(File parentDirectory, String indexFile, String arguments) {
        assert parentDirectory != null;
        if (indexFile != null) {
            if (!StringUtils.hasText(indexFile)) {
                return NbBundle.getMessage(RunAsValidator.class, "MSG_NoIndexFile");
            }
            indexFile = indexFile.trim();
            boolean error = false;
            if (indexFile.startsWith("/") // NOI18N
                    || indexFile.startsWith("\\")) { // NOI18N
                error = true;
            } else if (Utilities.isWindows() && indexFile.contains(File.separator)) {
                error = true;
            } else {
                File index = new File(parentDirectory, indexFile.replace('/', File.separatorChar)); // NOI18N
                if (!index.isFile()
                        || !index.equals(FileUtil.normalizeFile(index))) {
                    error = true;
                }
            }
            if (error) {
                return NbBundle.getMessage(RunAsValidator.class, "MSG_IndexFileInvalid");
            }
        }
        //XXX validation for arguments?
        return null;
    }

    /**
     * Create and return valid URL, throws InvalidUrlException if a valid URL cannot be created.
     * @param baseURL base URL.
     * @param indexFile index file.
     * @param args arguments.
     * @return valid URL.
     * @throws InvalidUrlException if a valid URL cannot be created.
     */
    public static String composeUrlHint(String baseURL, String indexFile, String args) throws InvalidUrlException {
        URL retval = null;
        try {
            if (baseURL != null && baseURL.trim().length() > 0) {
                retval = new URL(baseURL);
            }
            if (retval != null && indexFile != null && indexFile.trim().length() > 0) {
                retval = new URL(retval, indexFile);
            }
            if (retval != null && args != null && args.trim().length() > 0) {
                retval = new URI(retval.getProtocol(), retval.getUserInfo(), retval.getHost(), retval.getPort(),
                        retval.getPath(), args, retval.getRef()).toURL();
            }
        } catch (MalformedURLException ex) {
            throw new InvalidUrlException(NbBundle.getMessage(RunAsValidator.class, "MSG_InvalidUrl"));
        } catch (URISyntaxException ex) {
            throw new InvalidUrlException(NbBundle.getMessage(RunAsValidator.class, "MSG_InvalidUrl"));
        }
        return (retval != null) ? retval.toExternalForm() : ""; // NOI18N
    }

    public static final class InvalidUrlException extends Exception {
        private static final long serialVersionUID = 1234514014505423742L;

        public InvalidUrlException(String message) {
            super(message);
        }
    }
}
