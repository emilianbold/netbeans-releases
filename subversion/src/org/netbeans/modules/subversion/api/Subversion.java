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

package org.netbeans.modules.subversion.api;

import java.io.File;
import java.net.MalformedURLException;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.ui.browser.Browser;
import org.netbeans.modules.subversion.ui.checkout.CheckoutAction;
import org.netbeans.modules.subversion.ui.repository.RepositoryConnection;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Marian Petras
 */
public class Subversion {

    private static final String RELATIVE_PATH_ROOT = "/";               //NOI18N

    /**
     * Displays a dialog for selection of one or more Subversion repository
     * folders.
     *
     * @param  dialogTitle  title of the dialog
     * @param  repositoryUrl  URL of the Subversion repository to browse
     * @return  relative paths of the selected folders,
     *          or {@code null} if the user cancelled the selection
     * @throws  java.net.MalformedURLException  if the given URL is invalid
     */
    public static String[] selectRepositoryFolders(String dialogTitle,
                                                   String repositoryUrl)
                throws MalformedURLException {
        return selectRepositoryFolders(dialogTitle, repositoryUrl, null, null);
    }

    /**
     * Displays a dialog for selection of one or more Subversion repository
     * folders.
     * 
     * @param  dialogTitle  title of the dialog
     * @param  repositoryUrl  URL of the Subversion repository to browse
     * @param  username  username for access to the given repository
     * @param  password  password for access to the given repository
     * @return  relative paths of the selected folders,
     *          or {@code null} if the user cancelled the selection
     * @throws  java.net.MalformedURLException  if the given URL is invalid
     */
    public static String[] selectRepositoryFolders(String dialogTitle,
                                                   String repositoryUrl,
                                                   String username,
                                                   String password)
                throws MalformedURLException {

        RepositoryConnection conn = new RepositoryConnection(repositoryUrl);
        SVNUrl svnUrl = conn.getSvnUrl();
        SVNRevision svnRevision = conn.getSvnRevision();

        RepositoryFile repositoryFile = new RepositoryFile(svnUrl, svnRevision);
        Browser browser = new Browser(dialogTitle,
                                      Browser.BROWSER_SHOW_FILES | Browser.BROWSER_FOLDERS_SELECTION_ONLY,
                                      repositoryFile,
                                      null,     //files to select
                                      (username != null) ? username : "", //NOI18N
                                      ((username != null) && (password != null)) ? password : "", //NOI18N
                                      null,     //node actions
                                      Browser.BROWSER_HELP_ID_CHECKOUT);    //PENDING - is this help ID correct?

        RepositoryFile[] selectedFiles = browser.getRepositoryFiles();
        if ((selectedFiles == null) || (selectedFiles.length == 0)) {
            return null;
        }


        String[] relativePaths = makeRelativePaths(repositoryFile, selectedFiles);
        return relativePaths;
    }

    private static String[] makeRelativePaths(RepositoryFile repositoryFile,
                                              RepositoryFile[] selectedFiles) {
        String[] result = new String[selectedFiles.length];

        String[] repoPathSegments = repositoryFile.getPathSegments();

        for (int i = 0; i < selectedFiles.length; i++) {
            RepositoryFile selectedFile = selectedFiles[i];
            result[i] = makeRelativePath(repoPathSegments, selectedFile.getPathSegments());
        }
        return result;
    }

    private static String makeRelativePath(String[] repoPathSegments,
                                           String[] selectedPathSegments) {
        assert isPrefixOf(repoPathSegments, selectedPathSegments);
        int delta = selectedPathSegments.length - repoPathSegments.length;

        if (delta == 0) {
            return "/";     //root of the repository selected           //NOI18N
        }

        if (delta == 1) {
            return selectedPathSegments[selectedPathSegments.length - 1];
        }

        StringBuilder buf = new StringBuilder(120);
        int startIndex = repoPathSegments.length;
        int endIndex = selectedPathSegments.length;
        buf.append(selectedPathSegments[startIndex++]);
        for (int i = startIndex; i < endIndex; i++) {
            buf.append('/');
            buf.append(selectedPathSegments[i]);
        }
        return buf.toString();
    }

    private static boolean isPrefixOf(String[] prefix, String[] path) {
        if (prefix.length > path.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (!path[i].equals(prefix[i])) {
                return false;
            }
        }
        return true;
    }


    /**
     * Checks out a given folder from a given Subversion repository.
     * @param  repositoryUrl  URL of the Subversion repository
     * @param  relativePaths  relative paths denoting folder the folder in the
     *                       repository that is to be checked-out; to specify
     *                       that the whole repository folder should be
     *                       checked-out, use {@code &quot;/&quot;}
     * @param  localFolder  local folder to store the checked-out files to
     * @param  scanForNewProjects scans the created working copy for netbenas projects
     *                            and presents a dialog to open them eventually
     * @return  {@code true} if the checkout was successful,
     *          {@code false} otherwise
     */
    public static boolean checkoutRepositoryFolder(String repositoryUrl,
                                                   String[] relativePaths,
                                                   File localFolder,
                                                   boolean scanForNewProjects)
            throws MalformedURLException {
        return checkoutRepositoryFolder(repositoryUrl,
                                        relativePaths,
                                        localFolder,
                                        null,
                                        null,
                                        scanForNewProjects);
    }

    /**
     * Checks out a given folder from a given Subversion repository.
     * @param  repositoryUrl  URL of the Subversion repository
     * @param  relativePaths  relative paths denoting folder the folder in the
     *                       repository that is to be checked-out; to specify
     *                       that the whole repository folder should be
     *                       checked-out, use {@code &quot;/&quot;}
     * @param  localFolder  local folder to store the checked-out files to
     * @param  username  username for access to the given repository
     * @param  password  password for access to the given repository
     * @param  scanForNewProjects scans the created working copy for netbenas projects
     *                            and presents a dialog to open them eventually
     * @return  {@code true} if the checkout was successful,
     *          {@code false} otherwise
     */
    public static boolean checkoutRepositoryFolder(String repositoryUrl,
                                                   String[] repoRelativePaths,
                                                   File localFolder,
                                                   String username,
                                                   String password,
                                                   boolean scanForNewProjects)
            throws MalformedURLException {

        org.netbeans.modules.subversion.Subversion subversion
                = org.netbeans.modules.subversion.Subversion.getInstance();
        if(!subversion.checkClientAvailable()) {
            return false;
        }

        RepositoryConnection conn = new RepositoryConnection(repositoryUrl);

        SVNUrl svnUrl = conn.getSvnUrl();
        SVNRevision svnRevision = conn.getSvnRevision();

        SvnClient client;
        try {
            client = (username != null)
                               ? subversion.getClient(svnUrl,
                                                      username,
                                                      (password != null) ? password
                                                                         : "") //NOI18N
                               : subversion.getClient(svnUrl);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, false, true);
            return false;
        }

        RepositoryFile[] repositoryFiles = new RepositoryFile[repoRelativePaths.length];
        for (int i = 0; i < repoRelativePaths.length; i++) {
            String repoRelativePath = repoRelativePaths[i];

            repoRelativePath = polishRelativePath(repoRelativePath);
            SVNUrl repoSvnUrl = isRootRelativePath(repoRelativePath)
                                     ? svnUrl
                                     : svnUrl.appendPath(repoRelativePath);

            repositoryFiles[i] = new RepositoryFile(repoSvnUrl, repoRelativePath, svnRevision);
        }
        CheckoutAction.performCheckout(
                svnUrl,
                client,
                repositoryFiles,
                localFolder,
                false,             // do not checkout at working dir level
                scanForNewProjects).waitFinished();

        return true;
    }

    private static String polishRelativePath(String path) {
        if (path.length() == 0) {
            throw new IllegalArgumentException("empty path");           //NOI18N
        }
        path = removeDuplicateSlashes(path);
        if (path.equals("/")) {                                         //NOI18N
            return RELATIVE_PATH_ROOT;
        }
        if (path.charAt(0) == '/') {
            path = path.substring(1);
        }
        if (path.charAt(path.length() - 1) == '/') {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    private static boolean isRootRelativePath(String relativePath) {
        return relativePath.equals(RELATIVE_PATH_ROOT);
    }

    private static String removeDuplicateSlashes(String str) {
        int len = str.length();

        StringBuilder buf = null;
        boolean lastWasSlash = false;
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (c == '/') {
                if (lastWasSlash) {
                    if (buf == null) {
                        buf = new StringBuilder(len);
                        buf.append(str, 0, i);  //up to the first slash in a row
                    }
                    continue;
                }
                lastWasSlash = true;
            } else {
                lastWasSlash = false;
            }
            if (buf != null) {
                buf.append(c);
            }
        }
        return (buf != null) ? buf.toString() : str;
    }

}
