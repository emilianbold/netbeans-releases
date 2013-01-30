/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.ui.actions.support;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.php.api.util.Pair;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.spi.XDebugStarter;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;

/**
 * @author Radek Matous, Tomas Mysik
 */
@Deprecated
public class DebugScript  extends RunScript {
    private final Provider provider;

    public DebugScript(Provider provider) {
        super(provider);
        this.provider = provider;
    }

    @Override
    public void run() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                //temporary; after narrowing deps. will be changed
                Callable<Cancellable> callable = getCallable();
                XDebugStarter dbgStarter =  XDebugStarterFactory.getInstance();
                assert dbgStarter != null;
                if (dbgStarter.isAlreadyRunning()) {
                    if (CommandUtils.warnNoMoreDebugSession()) {
                        dbgStarter.stop();
                        run();
                    }
                } else {
                    PhpProject phpProject = PhpProjectUtils.getPhpProject(provider.getStartFile());
                    // #198753 - debug file without project
                    String encoding = phpProject != null ? ProjectPropertiesSupport.getEncoding(phpProject) : FileEncodingQuery.getDefaultEncoding().name();
                    XDebugStarter.Properties props = XDebugStarter.Properties.create(
                            provider.getStartFile(),
                            true,
                            // #209682 - "run as script" always from project files
                            Collections.<Pair<String, String>>emptyList(),
                            provider.getDebugProxy(),
                            encoding);
                    dbgStarter.start(provider.getProject(), callable, props);
                }
            }
        });
    }

    @Override
    protected boolean isControllable() {
        return false;
    }

    @Override
    protected String getOutputTabTitle() {
        return String.format("%s %s", super.getOutputTabTitle(), NbBundle.getMessage(DebugScript.class, "MSG_Suffix_Debug"));
    }

    @Override
    protected ExternalProcessBuilder getProcessBuilder() {
        return super.getProcessBuilder()
                .addEnvironmentVariable("XDEBUG_CONFIG", "idekey=" + PhpOptions.getInstance().getDebuggerSessionId()); // NOI18N
    }

    public interface Provider extends RunScript.Provider {
        Project getProject();
        FileObject getStartFile();
        List<Pair<String, String>> getDebugPathMapping();
        Pair<String, Integer> getDebugProxy();
    }
}
