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

package org.netbeans.modules.php.project.ui.actions.support;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.spi.XDebugStarter;
import org.netbeans.modules.php.project.ui.options.PHPOptionsCategory;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.netbeans.modules.php.project.util.PhpInterpreter;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Action implementation for SCRIPT configuration.
 * It means running and debugging scripts.
 * @author Tomas Mysik
 */
public class ConfigActionScript extends ConfigAction {

    @Override
    public boolean isRunProjectEnabled(PhpProject project) {
        return isRunProjectEnabled();
    }

    @Override
    public boolean isDebugProjectEnabled(PhpProject project) {
        return isDebugProjectEnabled();
    }

    @Override
    public boolean isRunFileEnabled(PhpProject project, Lookup context) {
        FileObject rootFolder = ProjectPropertiesSupport.getSourcesDirectory(project);
        FileObject file = CommandUtils.fileForContextOrSelectedNodes(context, rootFolder);
        return file != null && CommandUtils.isPhpFile(file);
    }

    @Override
    public boolean isDebugFileEnabled(PhpProject project, Lookup context) {
        if (XDebugStarterFactory.getInstance() == null) {
            return false;
        }
        return isRunFileEnabled(project, context);
    }

    @Override
    public void runProject(PhpProject project) {
        run(project, null);
    }

    @Override
    public void debugProject(PhpProject project) {
        debug(project, null);
    }

    @Override
    public void runFile(PhpProject project, Lookup context) {
        run(project, context);
    }

    @Override
    public void debugFile(PhpProject project, Lookup context) {
        debug(project, context);
    }

    private void run(PhpProject project, Lookup context) {
        try {
            getCallable(project, context, true, Collections.<String, String>emptyMap(), null).call();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void debug(PhpProject project, Lookup context) {
        //temporary; after narrowing deps. will be changed
        Callable<Cancellable> callable = getCallable(project, context, false, getDebugEnvironmentVariables(),
                NbBundle.getMessage(ConfigActionScript.class, "MSG_Suffix_Debug"));
        XDebugStarter dbgStarter =  XDebugStarterFactory.getInstance();
        if (dbgStarter != null) {
            if (dbgStarter.isAlreadyRunning()) {
                if (CommandUtils.warnNoMoreDebugSession()) {
                    dbgStarter.stop();
                    debug(project, context);
                }
            } else {
                dbgStarter.start(project, callable, getStartFile(project, context), true);
            }
        }
    }

    private final Callable<Cancellable> getCallable(final PhpProject project, final Lookup context,
            final boolean isControllable, final Map<String, String> environmentVariables, final String displayNameSuffix)  {
        return new Callable<Cancellable>() {
            public Cancellable call() throws Exception {

                FileObject scriptFo = getStartFile(project, context);
                final File scriptFile = (scriptFo != null) ? FileUtil.toFile(scriptFo) : null;

                PhpInterpreter phpInterpreter = ProjectPropertiesSupport.getPhpInterpreter(project);
                if (!phpInterpreter.isValid() || scriptFile == null) {
                    return new Cancellable() {
                        public boolean cancel() {
                            return true;
                        }
                    };
                }

                ExternalProcessBuilder processBuilder = getBuilder(project, phpInterpreter, scriptFile);
                for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
                    processBuilder = processBuilder.addEnvironmentVariable(entry.getKey(), entry.getValue());
                }

                StringBuilder displayName = new StringBuilder();
                displayName.append(getOutputTabTitle(phpInterpreter.getInterpreter(), scriptFile));
                if (displayNameSuffix != null) {
                    displayName.append(" "); // NOI18N
                    displayName.append(displayNameSuffix);
                }

                ExecutionDescriptor descriptor = new ExecutionDescriptor()
                        .controllable(isControllable)
                        .frontWindow(PhpOptions.getInstance().isOpenResultInOutputWindow())
                        .inputVisible(true)
                        .showProgress(true)
                        .optionsPath(PHPOptionsCategory.PATH_IN_LAYER);
                InOutPostRedirector redirector = new InOutPostRedirector(scriptFile);
                descriptor = descriptor.outProcessorFactory(redirector);
                descriptor = descriptor.postExecution(redirector);
                final ExecutionService service = ExecutionService.newService(processBuilder,
                        descriptor, displayName.toString());
                final Future<Integer> result = service.run();
                // #155251, #155741
//                try {
//                    result.get();
//                } catch (ExecutionException exc) {
//                    CommandUtils.processExecutionException(exc);
//                }
                return new Cancellable() {
                    public boolean cancel() {
                        return result.cancel(true);
                    }
                };
            }
        };
    }

    private FileObject getStartFile(PhpProject project, Lookup context) {
        FileObject sources = ProjectPropertiesSupport.getSourcesDirectory(project);
        FileObject startFile = null;
        if (context == null) {
            startFile = CommandUtils.fileForProject(project, sources);
        } else {
            startFile = CommandUtils.fileForContextOrSelectedNodes(context, sources);
        }
        return startFile;
    }

    private ExternalProcessBuilder getBuilder(PhpProject project, PhpInterpreter phpInterpreter, File scriptFile) {
        ExternalProcessBuilder processBuilder = new ExternalProcessBuilder(phpInterpreter.getInterpreter());
        for (String param : phpInterpreter.getParameters()) {
            processBuilder = processBuilder.addArgument(param);
        }
        processBuilder = processBuilder.addArgument(scriptFile.getName());
        String argProperty = ProjectPropertiesSupport.getArguments(project);
        if (argProperty != null && argProperty.length() > 0) {
            for (String argument : Arrays.asList(argProperty.split(" "))) { // NOI18N
                processBuilder = processBuilder.addArgument(argument);
            }
        }
        processBuilder = processBuilder.workingDirectory(scriptFile.getParentFile());
        return processBuilder;
    }

    private static File tempFileForScript(File scriptFile) throws IOException {
        File retval = File.createTempFile(scriptFile.getName(), ".html"); //NOI18N
        retval.deleteOnExit();
        return retval;
    }

    private String getOutputTabTitle(String command, File scriptFile) {
        return String.format("%s - %s", command, scriptFile.getName());
    }

    private Map<String, String> getDebugEnvironmentVariables() {
        return Collections.<String, String>singletonMap("XDEBUG_CONFIG", "idekey=" + PhpOptions.getInstance().getDebuggerSessionId()); // NOI18N
    }

    private static final class InOutPostRedirector implements InputProcessorFactory, Runnable {
        private BufferedWriter fileWriter;
        private final File tmpFile;
        private Charset encoding;

        public InOutPostRedirector(File scriptFile) throws IOException {
            this.tmpFile = FileUtil.normalizeFile(tempFileForScript(scriptFile));
            this.encoding = FileEncodingQuery.getEncoding(FileUtil.toFileObject(scriptFile));
        }

        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return InputProcessors.proxy(defaultProcessor,
                new InputProcessor() {

                    public void processInput(char[] chars) throws IOException {
                        getFileWriter().write(chars);
                    }

                    public void reset() throws IOException {
                    }

                    public void close() throws IOException {
                        getFileWriter().flush();
                        getFileWriter().close();
                    }

                });
        }

        public void run() {
            try {
                PhpOptions options = PhpOptions.getInstance();
                if (options.isOpenResultInBrowser()) {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(tmpFile.toURI().toURL());
                }
                if (options.isOpenResultInEditor()) {
                    FileObject fo = FileUtil.toFileObject(tmpFile);
                    DataObject dobj = DataObject.find(fo);
                    EditorCookie ec = dobj.getCookie(EditorCookie.class);
                    ec.open();
                }
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (CancellationException ex) {
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                setFileWriter(null);
            }
        }

        /**
         * @return the fileWriter
         */
        public synchronized BufferedWriter getFileWriter() throws FileNotFoundException {
            if (fileWriter == null) {
                fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile), encoding));
            }
            return fileWriter;
        }

        /**
         * @param fileWriter the fileWriter to set
         */
        public synchronized void setFileWriter(BufferedWriter fileWriter) {
            this.fileWriter = fileWriter;
        }
    }
}
