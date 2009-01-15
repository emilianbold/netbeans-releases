/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
import java.util.concurrent.Future;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.modules.php.project.util.PhpInterpreter;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.options.PHPOptionsCategory;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * @author Radek Matous, Tomas Mysik
 */
public class RunScript {
    protected final PhpProject project;

    public RunScript(PhpProject project) {
        assert project != null;
        this.project = project;
    }

    public void run() {
        run(null);
    }

    public void run(final Lookup context) {
        try {
            getCallable(context).call();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected final Callable<Cancellable> getCallable(final Lookup context)  {
        return new Callable<Cancellable>() {
            public Cancellable call() throws Exception {

                FileObject scriptFo = getStartFile(context);
                final File scriptFile = (scriptFo != null) ? FileUtil.toFile(scriptFo) : null;

                PhpInterpreter phpInterpreter = ProjectPropertiesSupport.getPhpInterpreter(project);
                if (!phpInterpreter.isValid() || scriptFile == null) {
                    return new Cancellable() {
                        public boolean cancel() {
                            return true;
                        }
                    };
                }

                ExecutionDescriptor descriptor = new ExecutionDescriptor()
                        .controllable(isControllable())
                        .frontWindow(PhpOptions.getInstance().isOpenResultInOutputWindow())
                        .inputVisible(true)
                        .showProgress(true)
                        .optionsPath(PHPOptionsCategory.PATH_IN_LAYER);
                InOutPostRedirector redirector = new InOutPostRedirector(scriptFile);
                descriptor = descriptor.outProcessorFactory(redirector);
                descriptor = descriptor.postExecution(redirector);
                final ExecutionService service = ExecutionService.newService(getProcessBuilder(phpInterpreter, scriptFile),
                        descriptor, getOutputTabTitle(phpInterpreter.getProgram(), scriptFile));
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

    protected boolean isControllable() {
        return true;
    }

    protected FileObject getStartFile(Lookup context) {
        FileObject sources = ProjectPropertiesSupport.getSourcesDirectory(project);
        FileObject startFile = null;
        if (context == null) {
            startFile = CommandUtils.fileForProject(project, sources);
        } else {
            startFile = CommandUtils.fileForContextOrSelectedNodes(context, sources);
        }
        return startFile;
    }

    protected ExternalProcessBuilder getProcessBuilder(PhpInterpreter phpInterpreter, File scriptFile) {
        ExternalProcessBuilder processBuilder = new ExternalProcessBuilder(phpInterpreter.getProgram());
        for (String param : phpInterpreter.getParameters()) {
            processBuilder = processBuilder.addArgument(param);
        }
        processBuilder = processBuilder.addArgument(scriptFile.getName());
        String argProperty = ProjectPropertiesSupport.getArguments(project);
        if (PhpProjectUtils.hasText(argProperty)) {
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

    protected String getOutputTabTitle(String command, File scriptFile) {
        return String.format("%s - %s", command, scriptFile.getName());
    }

    private static final class InOutPostRedirector implements InputProcessorFactory, Runnable {
        private final File tmpFile;
        private final Charset encoding;
        private BufferedWriter fileWriter;

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
