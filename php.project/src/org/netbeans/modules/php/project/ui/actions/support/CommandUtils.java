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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingUtilities;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.php.project.PhpActionProvider;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.actions.Command;
import org.netbeans.modules.php.project.ui.options.PHPOptionsCategory;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.modules.php.project.util.PhpUnit;
import org.netbeans.modules.web.client.tools.api.WebClientToolsProjectUtils;
import org.netbeans.modules.web.client.tools.api.WebClientToolsSessionStarterService;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * @author Radek Matous, Tomas Mysik
 */
public class CommandUtils {
    private static final String HTML_MIME_TYPE = "text/html"; // NOI18N

    private final PhpProject project;

    public CommandUtils(PhpProject project) {
        this.project = project;
    }

    /**
     * @return The file objects in the sources folder
     */
    public FileObject[] phpFilesForContext(Lookup context, boolean runAsScript) {
        FileObject dir = runAsScript ? ProjectPropertiesSupport.getSourcesDirectory(getProject()) :
            ProjectPropertiesSupport.getWebRootDirectory(getProject());
        return filter(filesForContext(context), dir);
    }

    public FileObject[] phpFilesForSelectedNodes(boolean runAsScript) {
        FileObject dir = runAsScript ? ProjectPropertiesSupport.getSourcesDirectory(getProject()) :
            ProjectPropertiesSupport.getWebRootDirectory(getProject());
        return filter(Arrays.asList(filesForSelectedNodes()), dir);
    }

    public String getRelativeSrcPath(FileObject fileObject) {
        return getRelativePhpPath(ProjectPropertiesSupport.getSourcesDirectory(getProject()), fileObject);
    }

    public String getRelativeWebRootPath(FileObject fileObject) {
        return getRelativePhpPath(ProjectPropertiesSupport.getWebRootDirectory(getProject()), fileObject);
    }

    private String getRelativePhpPath(FileObject folder, FileObject fileObject) {
        if (fileObject != null) {
            if (FileUtil.isParentOf(folder, fileObject)) {
                return FileUtil.getRelativePath(folder, fileObject);
            } else if (folder.equals(fileObject)) {
                return ""; //NOI18N
            }
        }
        return null;
    }

    private static boolean isUnderSourceRoot(FileObject sourceRoot, FileObject file) {
        return FileUtil.isParentOf(sourceRoot, file) && FileUtil.toFile(file) != null;
    }

    private static FileObject[] filter(Collection<? extends FileObject> files, FileObject dir) {
        Collection<FileObject> retval = new LinkedHashSet<FileObject>();
        for (FileObject file : files) {
            if (!isUnderSourceRoot(dir, file)) {
                return null;
            }
            retval.add(file);
        }
        return (!retval.isEmpty()) ? retval.toArray(new FileObject[retval.size()]) : null;
    }

    private PhpProject getProject() {
        return project;
    }

    public static boolean isPhpFile(FileObject file) {
        assert file != null;
        return PhpSourcePath.MIME_TYPE.equals(FileUtil.getMIMEType(file, PhpSourcePath.MIME_TYPE));
    }

    public static boolean isPhpOrHtmlFile(FileObject file) {
        assert file != null;
        String mimeType = FileUtil.getMIMEType(file, PhpSourcePath.MIME_TYPE, HTML_MIME_TYPE);
        return PhpSourcePath.MIME_TYPE.equals(mimeType) || HTML_MIME_TYPE.equals(mimeType);
    }

    /** Return <code>true</code> if user wants to restart the current debug session. */
    public static boolean warnNoMoreDebugSession() {
        String message = NbBundle.getMessage(CommandUtils.class, "MSG_NoMoreDebugSession");
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(message, NotifyDescriptor.OK_CANCEL_OPTION);
        return DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.OK_OPTION;
    }

    public static void processExecutionException(ExecutionException exc) {
        final Throwable cause = exc.getCause();
        assert cause != null;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Exception(
                        cause, NbBundle.getMessage(CommandUtils.class, "MSG_ExceptionDuringRunScript", cause.getLocalizedMessage())));
                OptionsDisplayer.getDefault().open(PHPOptionsCategory.PATH_IN_LAYER);
            }
        });
    }

    /**
     * Get a {@link PhpUnit} instance (path from IDE options used).
     * @param showCustomizer if <code>true</code>, IDE options dialog is shown if the path of PHP Unit is not valid.
     * @return a {@link PhpUnit} instance or <code>null</code> if the path of PHP Unit is not valid.
     */
    public static PhpUnit getPhpUnit(boolean showCustomizer) {
        final String phpUnitPath = PhpOptions.getInstance().getPhpUnit();
        if (Utils.validatePhpUnit(phpUnitPath) != null) {
            OptionsDisplayer.getDefault().open(PHPOptionsCategory.PATH_IN_LAYER);
            return null;
        }
        return new PhpUnit(phpUnitPath);
    }

    /**
     * Get <b>valid</b> {@link FileObject}s for given nodes.
     * @param nodes nodes to get {@link FileObject}s from.
     * @return list of <b>valid</b> {@link FileObject}s, never <code>null</code>.
     */
    public static List<FileObject> getFileObjects(final Node[] nodes) {
        if (nodes.length == 0) {
            return Collections.<FileObject>emptyList();
        }

        final List<FileObject> files = new ArrayList<FileObject>();
        for (Node node : nodes) {
            FileObject fo = getFileObject(node);
            assert fo != null : "A valid file object not found for node: " + node;
            files.add(fo);
        }
        return files;
    }

    /**
     * Get a <b>valid</b> {@link FileObject} for given node.
     * @param node node to get {@link FileObject}s from.
     * @return a <b>valid</b> {@link FileObject}, <code>null</code> otherwise.
     */
    public static FileObject getFileObject(Node node) {
        assert node != null;

        FileObject fileObj = node.getLookup().lookup(FileObject.class);
        if (fileObj != null && fileObj.isValid()) {
            return fileObj;
        }
        DataObject dataObj = node.getCookie(DataObject.class);
        if (dataObj == null) {
            return null;
        }
        fileObj = dataObj.getPrimaryFile();
        if ((fileObj == null) || !fileObj.isValid()) {
            return null;
        }
        return fileObj;
    }

    /**
     * Return <code>true</code> if {@link FileObject} is underneath project sources directory
     * or sources directory itself.
     * @param project project to get sources directory from.
     * @param fileObj {@link FileObject} to check.
     * @return <code>true</code> if {@link FileObject} is underneath project sources directory
     *         or sources directory itself.
     */
    public static boolean isUnderSources(PhpProject project, FileObject fileObj) {
        assert project != null;
        assert fileObj != null;
        FileObject sources = ProjectPropertiesSupport.getSourcesDirectory(project);
        return sources.equals(fileObj) || FileUtil.isParentOf(sources, fileObj);
    }

    /**
     * Return <code>true</code> if {@link FileObject} is underneath project tests directory
     * or tests directory itself.
     * @param project project to get tests directory from.
     * @param fileObj {@link FileObject} to check.
     * @return <code>true</code> if {@link FileObject} is underneath project tests directory
     *         or tests directory itself.
     */
    public static boolean isUnderTests(PhpProject project, FileObject fileObj, boolean showFileChooser) {
        assert project != null;
        assert fileObj != null;
        FileObject tests = ProjectPropertiesSupport.getTestDirectory(project, showFileChooser);
        return tests != null && (tests.equals(fileObj) || FileUtil.isParentOf(tests, fileObj));
    }

    public static Collection<? extends FileObject> filesForContext(Lookup context) {
        assert context != null;
        return context.lookupAll(FileObject.class);
    }

    public static FileObject[] filesForContext(Lookup context, FileObject baseDirectory) {
        return filter(filesForContext(context), baseDirectory);
    }

    public static FileObject[] filesForSelectedNodes() {
        Node[] nodes = getSelectedNodes();
        if (nodes == null) {
            return new FileObject[0];
        }
        List<FileObject> fileObjects = getFileObjects(nodes);
        return fileObjects.toArray(new FileObject[fileObjects.size()]);
    }

    public static FileObject[] filesForSelectedNodes(FileObject baseDirectory) {
        return filter(Arrays.asList(filesForSelectedNodes()), baseDirectory);
    }

    public static FileObject fileForContextOrSelectedNodes(Lookup context, FileObject baseDirectory) {
        assert baseDirectory != null;
        assert baseDirectory.isFolder() : "Folder must be given: " + baseDirectory;

        FileObject[] files = filesForContext(context, baseDirectory);
        if (files == null || files.length == 0) {
            files = filesForSelectedNodes(baseDirectory);
        }
        return (files != null && files.length > 0) ? files[0] : null;
    }

    public static URL urlForProject(PhpProject project) throws MalformedURLException {
        FileObject webRoot = ProjectPropertiesSupport.getWebRootDirectory(project);
        FileObject indexFile = fileForProject(project, webRoot);
        return urlForFile(project, webRoot, indexFile);
    }

    public static URL urlForDebugProject(PhpProject project) throws MalformedURLException {
        DebugInfo debugInfo = getDebugInfo(project);
        URL debugUrl = urlForProject(project);
        if (debugInfo.debugServer) {
            debugUrl = appendQuery(debugUrl, getDebugArguments(project));
        }
        return debugUrl;
    }

    public static URL urlForContext(PhpProject project, Lookup context) throws MalformedURLException {
        FileObject webRoot = ProjectPropertiesSupport.getWebRootDirectory(project);
        FileObject selectedFile = fileForContextOrSelectedNodes(context, webRoot);
        return urlForFile(project, webRoot, selectedFile);
    }

    public static URL urlForDebugContext(PhpProject project, Lookup context) throws MalformedURLException {
        DebugInfo debugInfo = getDebugInfo(project);
        URL debugUrl = urlForContext(project, context);
        if (debugInfo.debugServer) {
            debugUrl = appendQuery(debugUrl, getDebugArguments(project));
        }
        return debugUrl;
    }

    /**
     * @return FileObject or <code>null</code>.
     */
    public static FileObject fileForProject(PhpProject project, FileObject baseDirectory) {
        assert baseDirectory != null;
        assert baseDirectory.isFolder() : "Folder must be given: " + baseDirectory;

        String indexFile = ProjectPropertiesSupport.getIndexFile(project);
        if (indexFile != null) {
            return baseDirectory.getFileObject(indexFile);
        }
        return baseDirectory;
    }

    public static DebugInfo getDebugInfo(PhpProject project) {
        boolean debugServer = WebClientToolsProjectUtils.getServerDebugProperty(project);
        boolean debugClient = WebClientToolsProjectUtils.getClientDebugProperty(project);

        if (!WebClientToolsSessionStarterService.isAvailable()) {
            debugServer = true;
            debugClient = false;
        }
        assert debugServer || debugClient;
        return new DebugInfo(debugClient, debugServer);
    }

    public static URL getBaseURL(PhpProject project) throws MalformedURLException {
        String baseURLPath = ProjectPropertiesSupport.getUrl(project);
        if (baseURLPath == null) {
            throw new MalformedURLException();
        }
        return new URL(baseURLPath);
    }

    public static Command getCommand(PhpProject project, String commandName) {
        PhpActionProvider provider = project.getLookup().lookup(PhpActionProvider.class);
        assert provider != null;
        return provider.getCommand(commandName);
    }

    private static Node[] getSelectedNodes() {
        return TopComponent.getRegistry().getCurrentNodes();
    }

    private static URL urlForFile(PhpProject project, FileObject webRoot, FileObject file) throws MalformedURLException {
        String relativePath = FileUtil.getRelativePath(webRoot, file);
        assert relativePath != null : String.format("WebRoot %s must be parent of file %s", webRoot, file);
        URL retval = new URL(getBaseURL(project), relativePath);
        String arguments = ProjectPropertiesSupport.getArguments(project);
        return (arguments != null) ? appendQuery(retval, arguments) : retval;
    }
    private static URL appendQuery(URL originalURL, String queryWithoutQMark) throws MalformedURLException {
        URI retval;
        try {
            retval = new URI(originalURL.getProtocol(), originalURL.getUserInfo(),
                    originalURL.getHost(), originalURL.getPort(), originalURL.getPath(),
                    queryWithoutQMark, originalURL.getRef());
        } catch (URISyntaxException ex) {
            MalformedURLException mex = new MalformedURLException(ex.getLocalizedMessage());
            mex.initCause(ex);
            throw mex;
        }
        return retval.toURL();
    }

    private static String getDebugArguments(PhpProject project) {
        String args = ProjectPropertiesSupport.getArguments(project);
        StringBuilder arguments = new StringBuilder();
        if (PhpProjectUtils.hasText(args)) {
            arguments.append(args);
            arguments.append("&"); // NOI18N
        }
        arguments.append("XDEBUG_SESSION_START=" + PhpOptions.getInstance().getDebuggerSessionId()); // NOI18N
        return arguments.toString();
    }


    public final static class DebugInfo {
        final boolean debugClient;
        final boolean debugServer;

        public DebugInfo(boolean debugClient, boolean debugServer) {
            this.debugClient = debugClient;
            this.debugServer = debugServer;
        }
    }
}
