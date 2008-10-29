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
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.extexecution.api.ExecutionDescriptor;
import org.netbeans.modules.extexecution.api.ExecutionDescriptor.InputProcessorFactory;
import org.netbeans.modules.extexecution.api.ExecutionService;
import org.netbeans.modules.extexecution.api.ExternalProcessBuilder;
import org.netbeans.modules.extexecution.api.input.InputProcessor;
import org.netbeans.modules.php.project.util.PhpInterpreter;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.actions.Command;
import org.netbeans.modules.php.project.ui.options.PHPOptionsCategory;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
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
 * @author Radek Matous
 */
public class RunScript extends Command implements Displayable {
    public static final String ID = "run.local"; // NOI18N

    public RunScript(PhpProject project) {
        super(project);
    }

    @Override
    public void invokeAction(final Lookup context) throws IllegalArgumentException {
        try {
            getCallable(context).call();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public final Callable<Cancellable> getCallable(final Lookup context)  {
        return new Callable<Cancellable>() {
            public Cancellable call() throws Exception {
                PhpInterpreter phpInterpreter = ProjectPropertiesSupport.getPhpInterpreter(getProject());
                final FileObject scriptFo = (context == null) ? fileForProject(false) : fileForContext(context);
                final File scriptFile = (scriptFo != null) ? FileUtil.toFile(scriptFo) : null;
                if (!phpInterpreter.isValid() || scriptFile == null) {
                    return new Cancellable() {
                        public boolean cancel() {
                            return true;
                        }
                    };
                }
                ExecutionDescriptor descriptor = new ExecutionDescriptor().controllable(isControllable()).frontWindow(true).inputVisible(false).showProgress(true).optionsPath(PHPOptionsCategory.PATH_IN_LAYER);
                InOutPostRedirector redirector = new InOutPostRedirector(scriptFile);
                descriptor = descriptor.outProcessorFactory(redirector);
                descriptor = descriptor.postExecution(redirector);
                final ExecutionService service = ExecutionService.newService(getBuilder(phpInterpreter, scriptFile),
                        descriptor, getOutputTabTitle(phpInterpreter.getInterpreter(), scriptFile));
                final Future<Integer> result = service.run();
                return new Cancellable() {
                    public boolean cancel() {
                        return result.cancel(true);
                    }
                };
            }
        };
    }

    private ExternalProcessBuilder getBuilder(PhpInterpreter phpInterpreter, File scriptFile) {
        ExternalProcessBuilder processBuilder = new ExternalProcessBuilder(phpInterpreter.getInterpreter());
        for (String param : phpInterpreter.getParameters()) {
            processBuilder = processBuilder.addArgument(param);
        }
        processBuilder = processBuilder.addArgument(scriptFile.getName());
        String argProperty = ProjectPropertiesSupport.getArguments(getProject());
        if (argProperty != null && argProperty.length() > 0) {
            for (String argument : Arrays.asList(argProperty.split(" "))) { // NOI18N
                processBuilder = processBuilder.addArgument(argument);
            }
        }
        processBuilder = processBuilder.workingDirectory(scriptFile.getParentFile());
        processBuilder = initProcessBuilder(processBuilder);
        return processBuilder;
    }

    @Override
    public boolean isActionEnabled(Lookup context) throws IllegalArgumentException {
        return ((context == null) ? fileForProject(false) : fileForContext(context)) != null;
    }

    @Override
    public String getCommandId() {
        return ID;
    }

    protected boolean isControllable() {
        return true;
    }

    //designed to set env.variables for debugger to resuse this code
    protected  ExternalProcessBuilder initProcessBuilder(ExternalProcessBuilder processBuilder) {
        return processBuilder;
    }

    private static File tempFileForScript(File scriptFile) throws IOException {
        File retval = File.createTempFile(scriptFile.getName(), ".html"); //NOI18N
        retval.deleteOnExit();
        return retval;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(DebugScript.class, "LBL_RunScript");
    }

    private static class InOutPostRedirector implements InputProcessorFactory, Runnable {
        private BufferedWriter fileWriter;
        private final File tmpFile;
        private Charset encoding;

        public InOutPostRedirector(File scriptFile) throws IOException {
            this.tmpFile = FileUtil.normalizeFile(tempFileForScript(scriptFile));
            this.encoding = FileEncodingQuery.getEncoding(FileUtil.toFileObject(scriptFile));
        }

        public InputProcessor newInputProcessor() {
            return new InputProcessor() {

                public void processInput(char[] chars) throws IOException {
                    getFileWriter().write(chars);
                }

                public void reset() throws IOException {
                }

                public void close() throws IOException {
                    getFileWriter().flush();
                    getFileWriter().close();
                }

            };
        }

        public void run() {
            try {
                PhpOptions options = PhpOptions.getInstance();
                if (options.isOpenResultInBrowser()) {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(tmpFile.toURL());
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
                fileWriter = writer(new FileOutputStream(tmpFile), encoding);
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
