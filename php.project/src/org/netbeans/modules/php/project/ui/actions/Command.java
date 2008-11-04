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
package org.netbeans.modules.php.project.ui.actions;

import org.netbeans.modules.php.project.ui.actions.support.Displayable;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import org.netbeans.modules.php.project.PhpActionProvider;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.Utils;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.web.client.tools.api.JSToNbJSLocationMapper;
import org.netbeans.modules.web.client.tools.api.LocationMappersFactory;
import org.netbeans.modules.web.client.tools.api.NbJSToJSLocationMapper;
import org.netbeans.modules.web.client.tools.api.WebClientToolsProjectUtils;
import org.netbeans.modules.web.client.tools.api.WebClientToolsSessionException;
import org.netbeans.modules.web.client.tools.api.WebClientToolsSessionStarterService;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * @author Radek Matous
 */
public abstract class Command {

    private final PhpProject project;

    public Command(PhpProject project) {
        this.project = project;
        assert project != null;
    }

    public abstract String getCommandId();

    public abstract void invokeAction(Lookup context) throws IllegalArgumentException;

    public abstract boolean isActionEnabled(Lookup context) throws IllegalArgumentException;

    public boolean asyncCallRequired() {
        return true;
    }

    public boolean saveRequired() {
        return true;
    }

    public final PhpProject getProject() {
        return project;
    }

    //Helper|Utility methods for subclasses
    protected final void showURL(final URL url) throws MalformedURLException {
        HtmlBrowser.URLDisplayer.getDefault().showURL(url);
    }

    protected final void showURLForProjectFile() throws MalformedURLException {
        HtmlBrowser.URLDisplayer.getDefault().showURL(urlForProjectFile(true));
    }

    protected final void showURLForDebugProjectFile() throws MalformedURLException {
        showURLForDebug(getURLForDebug(null, true));
    }

    protected final URL getURLForDebug(Lookup context, boolean useWebRoot) throws MalformedURLException {
        DebugInfo debugInfo = getDebugInfo();
        URL debugUrl;
        if (context != null) {
            debugUrl = debugInfo.debugServer ? urlForDebugContext(context, useWebRoot) : urlForContext(context, useWebRoot);
        } else {
            debugUrl = debugInfo.debugServer ? urlForDebugProjectFile(useWebRoot) : urlForProjectFile(useWebRoot);
        }
        return debugUrl;
    }

    private DebugInfo getDebugInfo() {
        boolean debugServer = WebClientToolsProjectUtils.getServerDebugProperty(project);
        boolean debugClient = WebClientToolsProjectUtils.getClientDebugProperty(project);

        if (!WebClientToolsSessionStarterService.isAvailable()) {
            debugServer = true;
            debugClient = false;
        }
        assert debugServer || debugClient;
        return new DebugInfo(debugClient, debugServer);
    }

    protected final void showURLForDebug(URL debugUrl) throws MalformedURLException {
        assert debugUrl != null;
        if (getDebugInfo().debugClient) {
            try {
                launchJavaScriptDebugger(debugUrl);
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            HtmlBrowser.URLDisplayer.getDefault().showURL(debugUrl);
        }
    }

    protected final void launchJavaScriptDebugger(URL url) throws MalformedURLException, URISyntaxException {
            LocationMappersFactory mapperFactory = Lookup.getDefault().lookup(LocationMappersFactory.class);
            Lookup debuggerLookup = null;
            if (mapperFactory != null) {
                URI appContext = getBaseURL().toURI();
                FileObject[] srcRoots = Utils.getSourceObjects(getProject());

                JSToNbJSLocationMapper forwardMapper =
                        mapperFactory.getJSToNbJSLocationMapper(srcRoots, appContext, null);
                NbJSToJSLocationMapper reverseMapper =
                        mapperFactory.getNbJSToJSLocationMapper(srcRoots, appContext, null);
                debuggerLookup = Lookups.fixed(forwardMapper, reverseMapper, project);
            } else {
                debuggerLookup = Lookups.fixed(project);
            }

            URI clientUrl = url.toURI();

            HtmlBrowser.Factory browser = null;
            if (WebClientToolsProjectUtils.isInternetExplorer(project)) {
                browser = WebClientToolsProjectUtils.getInternetExplorerBrowser();
            } else {
                browser = WebClientToolsProjectUtils.getFirefoxBrowser();
            }

            if (browser == null) {
                HtmlBrowser.URLDisplayer.getDefault().showURL(url);
            } else {
                try {
                    WebClientToolsSessionStarterService.startSession(clientUrl, browser, debuggerLookup);
                } catch (WebClientToolsSessionException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

    }

    protected final URL getBaseURL() throws MalformedURLException {
        String baseURLPath = ProjectPropertiesSupport.getUrl(project);
        if (baseURLPath == null) {
            throw new MalformedURLException();
        }
        return new URL(baseURLPath);
    }

    protected final URL appendQuery(URL originalURL, String queryWithoutQMark) throws MalformedURLException {
        URI retval;
        try {
            retval = new URI(originalURL.getProtocol(), originalURL.getUserInfo(),
                    originalURL.getHost(), originalURL.getPort(), originalURL.getPath(),
                    queryWithoutQMark, originalURL.getRef());
            return retval.toURL();
        } catch (URISyntaxException ex) {
            MalformedURLException mex = new MalformedURLException(ex.getLocalizedMessage());
            mex.initCause(ex);
            throw mex;
        }
    }

    protected final URL urlForDebugProjectFile(boolean useWebRoot) throws MalformedURLException {
        return appendQuery(urlForProjectFile(useWebRoot), getDebugArguments());
    }

    protected final URL urlForDebugContext(Lookup context, boolean useWebRoot) throws MalformedURLException {
        return appendQuery(urlForContext(context, useWebRoot), getDebugArguments());
    }

    private String getDebugArguments() {
        String args = ProjectPropertiesSupport.getArguments(project);
        StringBuilder arguments = new StringBuilder();
        if (args != null && args.length() > 0) {
            arguments.append(args);
            arguments.append("&"); // NOI18N
        }
        arguments.append("XDEBUG_SESSION_START=" + PhpSourcePath.DEBUG_SESSION); // NOI18N
        return arguments.toString();
    }

    protected final URL urlForProjectFile(boolean useWebRoot) throws MalformedURLException {
        String relativePath = relativePathForProject(useWebRoot);
        if (relativePath == null) {
            //TODO makes sense just in case if listing is enabled | maybe user message
            relativePath = ""; //NOI18N
        }
        URL retval = new URL(getBaseURL(), relativePath);
        String arguments = ProjectPropertiesSupport.getArguments(project);
        return (arguments != null) ? appendQuery(retval, arguments) : retval;
    }

    protected final URL urlForContext(Lookup context, boolean useWebRoot) throws MalformedURLException {
        String relativePath = relativePathForContext(context, useWebRoot);
        if (relativePath == null) {
            throw new MalformedURLException();
        }
        URL retval = new URL(getBaseURL(), relativePath);
        String arguments = ProjectPropertiesSupport.getArguments(project);
        return (arguments != null) ? appendQuery(retval, arguments) : retval;
    }

    //or null
    protected final String relativePathForContext(Lookup context, boolean useWebRoot) {
        FileObject fileForContext = fileForContext(context);
        if (useWebRoot) {
            return getCommandUtils().getRelativeWebRootPath(fileForContext);
        }
        return getCommandUtils().getRelativeSrcPath(fileForContext);
    }

    //or null
    protected final String relativePathForProject(boolean useWebRoot) {
        FileObject fileForProject = fileForProject(useWebRoot);
        if (useWebRoot) {
            return getCommandUtils().getRelativeWebRootPath(fileForProject);
        }
        return getCommandUtils().getRelativeSrcPath(fileForProject);
    }

    //or null
    protected final FileObject fileForProject(boolean useWebRoot) {
        FileObject dir = useWebRoot ? ProjectPropertiesSupport.getWebRootDirectory(project) : ProjectPropertiesSupport.getSourcesDirectory(project);
        String indexFile = ProjectPropertiesSupport.getIndexFile(project);
        if (dir != null && indexFile != null) {
            return dir.getFileObject(indexFile);
        }
        return dir;
    }

    /** eventually show the customizer */
    protected boolean isRunConfigurationValid(boolean indexFileNeeded) {
        return ProjectPropertiesSupport.isActiveConfigValid(project, indexFileNeeded, true);
    }

    protected boolean isScriptSelected() {
        PhpProjectProperties.RunAsType runAs = ProjectPropertiesSupport.getRunAs(project);
        return PhpProjectProperties.RunAsType.SCRIPT.equals(runAs);
    }

    protected boolean isRemoteConfigSelected() {
        PhpProjectProperties.RunAsType runAs = ProjectPropertiesSupport.getRunAs(project);
        return PhpProjectProperties.RunAsType.REMOTE.equals(runAs);
    }

    protected boolean isPhpFileSelected(FileObject file) {
        if (file == null) {
            return false;
        }
        return CommandUtils.isPhpFile(file);
    }

    protected String getRemoteConfigurationName() {
        return ProjectPropertiesSupport.getRemoteConnection(project);
    }

    protected String getRemoteDirectory() {
        return ProjectPropertiesSupport.getRemoteDirectory(project);
    }

    //or null
    protected final FileObject fileForContext(Lookup context) {
        CommandUtils utils = getCommandUtils();
        boolean scriptSelected = isScriptSelected();
        FileObject[] files = utils.phpFilesForContext(context, scriptSelected);
        if (files == null || files.length == 0) {
            files = utils.phpFilesForSelectedNodes(scriptSelected);
        }
        return (files != null && files.length > 0) ? files[0] : null;
    }

    protected final Command getOtherCommand(String commandName) {
        PhpActionProvider provider = getProject().getLookup().lookup(PhpActionProvider.class);
        assert provider != null;
        return provider.getCommand(commandName);
    }


    private CommandUtils getCommandUtils() {
        CommandUtils utils = new CommandUtils(getProject());
        return utils;
    }

    private static OutputWriter getOutputWriter(String outTabTitle, boolean error, boolean clearOutput) {
        InputOutput io = IOProvider.getDefault().getIO(outTabTitle, false);
        io.select();
        if (clearOutput) {
            try {
                io.getOut().reset();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return error ? io.getErr() : io.getOut();
    }

    protected final BufferedReader reader(InputStream is, Charset encoding) {
        return new BufferedReader(new InputStreamReader(is, encoding));
    }

    protected final BufferedWriter outputTabWriter(File scriptFile, boolean error, boolean clearOutput) {
        String outputTitle = getOutputTabTitle(scriptFile);
        OutputWriter outputWriter = getOutputWriter(outputTitle, error, clearOutput);
        return new BufferedWriter(outputWriter);
    }

    protected final String getOutputTabTitle(File scriptFile) {
        return getOutputTabTitle(((Displayable) this).getDisplayName(), scriptFile);
    }

    protected String getOutputTabTitle(String command, File scriptFile) {
        assert this instanceof Displayable;
        return MessageFormat.format("{0} - {1}", command, scriptFile.getName());
    }

    protected static final BufferedWriter writer(OutputStream os, Charset encoding) {
        return new BufferedWriter(new OutputStreamWriter(os, encoding));
    }

    protected final void rewriteAndClose(Command.StringConvertor convertor,
            BufferedReader reader, BufferedWriter... writers) throws IOException {
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                line = (convertor != null) ? convertor.convert(line) : line;
                for (BufferedWriter writer : writers) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } finally {
            reader.close();
            for (BufferedWriter writer : writers) {
                writer.flush();
                writer.close();
            }
        }
    }

    public interface StringConvertor {
        String convert(String text);
    }

    protected void eventuallyUploadFiles() {
        eventuallyUploadFiles((FileObject[]) null);
    }

    protected void eventuallyUploadFiles(FileObject... preselectedFiles) {
        if (!isRemoteConfigSelected()) {
            return;
        }
        UploadCommand uploadCommand = (UploadCommand) getOtherCommand(UploadCommand.ID);
        if (!uploadCommand.isActionEnabled(null)) {
            return;
        }

        PhpProjectProperties.UploadFiles uploadFiles = ProjectPropertiesSupport.getRemoteUpload(getProject());
        assert uploadFiles != null;

        if (PhpProjectProperties.UploadFiles.ON_RUN.equals(uploadFiles)) {
            uploadCommand.uploadFiles(new FileObject[] {ProjectPropertiesSupport.getSourcesDirectory(getProject())}, preselectedFiles);
        }
    }

    private static class DebugInfo {
        final boolean debugClient;
        final boolean debugServer;

        public DebugInfo(boolean debugClient, boolean debugServer) {
            this.debugClient = debugClient;
            this.debugServer = debugServer;
        }
    }
}
