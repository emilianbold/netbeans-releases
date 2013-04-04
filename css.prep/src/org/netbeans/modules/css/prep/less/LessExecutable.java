/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.prep.less;

import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.modules.css.prep.options.CssPrepOptions;
import org.netbeans.modules.css.prep.util.ExternalExecutable;
import org.netbeans.modules.css.prep.util.ExternalExecutableValidator;
import org.netbeans.modules.css.prep.util.InvalidExternalExecutableException;
import org.netbeans.modules.css.prep.util.UiUtils;
import org.netbeans.modules.css.prep.util.Warnings;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;

/**
 * Class representing <tt>lessc</tt> command line tool.
 */
public final class LessExecutable {

    private static final Logger LOGGER = Logger.getLogger(LessExecutable.class.getName());

    public static final String EXECUTABLE_NAME = "lessc"; // NOI18N

    private final String sassPath;


    private LessExecutable(String sassPath) {
        this.sassPath = sassPath;
    }

    /**
     * Get the default, <b>valid only</b> Less executable.
     * @return the default, <b>valid only</b> Less executable.
     * @throws InvalidExternalExecutableException if Less executable is not valid.
     */
    public static LessExecutable getDefault() throws InvalidExternalExecutableException {
        String path = CssPrepOptions.getInstance().getLessPath();
        String error = validate(path);
        if (error != null) {
            throw new InvalidExternalExecutableException(error);
        }
        return new LessExecutable(path);
    }

    @NbBundle.Messages("Less.executable.label=LESS")
    public static String validate(String path) {
        return ExternalExecutableValidator.validateCommand(path, Bundle.Less_executable_label());
    }

    @NbBundle.Messages("Less.compile=LESS (compile)")
    @CheckForNull
    public void compile(FileObject fileObject) {
        assert !EventQueue.isDispatchThread();
        assert fileObject.isValid() : "Invalid file given: " + fileObject;
        try {
            File lessFile = FileUtil.toFile(fileObject);
            final File cssFile = getCssFile(lessFile, fileObject.getName());
            getExecutable(Bundle.Less_compile())
                    .additionalParameters(getParameters(lessFile, cssFile))
                    .runAndWait(getDescriptor(new Runnable() {
                @Override
                public void run() {
                    FileUtil.refreshFor(cssFile.getParentFile());
                }
            }), "Compiling less files..."); // NOI18N
        } catch (CancellationException ex) {
            // cancelled
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
            if (Warnings.showLessWarning()) {
                UiUtils.processExecutionException(ex);
            }
        }
    }

    private ExternalExecutable getExecutable(String title) {
        return new ExternalExecutable(sassPath)
                .displayName(title);
    }

    private ExecutionDescriptor getDescriptor(Runnable postTask) {
        return new ExecutionDescriptor()
                .inputOutput(IOProvider.getDefault().getIO(Bundle.Less_compile(), false))
                .inputVisible(false)
                .frontWindow(false)
                .frontWindowOnError(true)
                .noReset(true)
                .showProgress(true)
                .postExecution(postTask);
    }

    private List<String> getParameters(File inputFile, File outputFile) {
        List<String> params = new ArrayList<String>();
        // input
        params.add(inputFile.getAbsolutePath());
        // output
        params.add(outputFile.getAbsolutePath());
        return params;
    }

    private File getCssFile(File lessFile, String name) {
        return new File(lessFile.getParent(), name + ".css"); // NOI18N
    }

}
