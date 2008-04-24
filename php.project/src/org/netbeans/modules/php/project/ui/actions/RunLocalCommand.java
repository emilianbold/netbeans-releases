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
package org.netbeans.modules.php.project.ui.actions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.api.PhpOptions;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * @author Radek Matous
 */
public class RunLocalCommand extends Command implements Displayable {

    public static final String ID = "run.local";//NOI18N

    public RunLocalCommand(PhpProject project) {
        super(project);
    }

    @Override
    public final void invokeAction(final Lookup context) throws IllegalArgumentException {
        String command = getPhpInterpreter();
        FileObject scriptFo = fileForContext(context);
        File scriptFile = (scriptFo != null) ? FileUtil.toFile(scriptFo) : null;
        if (command == null || scriptFile == null) {
            //TODO mising error handling
            return;
        }

        //find out encoding
        Charset encoding = FileEncodingQuery.getDefaultEncoding();
        encoding = FileEncodingQuery.getEncoding(scriptFo);

        //prepare & start external process
        ProcessBuilder processBuilder = new ProcessBuilder(new String[]{command, scriptFile.getAbsolutePath()});
        initProcessBuilder(processBuilder);
        try {
            Process process = processBuilder.start();
            File outputTmpFile = processOutput(process, scriptFile, encoding);
            processError(process, scriptFile, encoding);
            if (process.waitFor() == 0/*OK*/) {
                PhpOptions options = PhpOptions.getInstance();
                if (options.isOpenResultInBrowser()) {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(outputTmpFile.toURL());
                }
                if (options.isOpenResultInEditor()) {
                    FileObject fo = FileUtil.toFileObject(outputTmpFile);
                    DataObject dobj = DataObject.find(fo);
                    EditorCookie ec = dobj.getCookie(EditorCookie.class);
                    ec.open();
                }
                if (options.isOpenResultInOutputWindow()) {
                    BufferedReader reader = reader(new FileInputStream(outputTmpFile), encoding);
                    BufferedWriter writer = outputTabWriter(scriptFile);
                    rewriteAndClose(reader, writer, null);
                }
            }
        } catch (IOException ex) {
            //TODO missing error handling
            Exceptions.printStackTrace(ex);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public boolean isActionEnabled(Lookup context) throws IllegalArgumentException {
        return fileForContext(context) != null;
    }

    @Override
    public String getCommandId() {
        return ID;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(RunCommand.class, "LBL_RunLocalCommand");//NOI18N

    }

    //designed to set env.variables for debugger to resuse this code
    private void initProcessBuilder(ProcessBuilder processBuilder) {
    }

    private void processError(Process process, File scriptFile, Charset encoding) throws IOException {
        BufferedReader errorReader = reader(process.getErrorStream(), encoding);
        BufferedWriter outputWriter = outputTabWriter(scriptFile);
        rewriteAndClose(errorReader, outputWriter, new StringConvertor() {

            public String convert(String text) {
                return NbBundle.getMessage(RunLocalCommand.class, "LBL_ExecErrorMsg", text);
            }
        });
    }

    private File processOutput(Process process, File scriptFile, Charset encoding) throws IOException {
        final File retval = tempFileForScript(scriptFile);
        BufferedReader reader = reader(process.getInputStream(), encoding);
        BufferedWriter fileWriter = writer(new FileOutputStream(retval), encoding);
        rewriteAndClose(reader, fileWriter, null);
        return retval;
    }

    private File tempFileForScript(File scriptFile) throws IOException {
        File retval = File.createTempFile(scriptFile.getName(), ".html");//NOI18N
        retval.deleteOnExit();
        return retval;
    }
}