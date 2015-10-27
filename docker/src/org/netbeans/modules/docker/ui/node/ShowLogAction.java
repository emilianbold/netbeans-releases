/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.ui.node;

import java.io.InputStream;
import java.nio.charset.Charset;
import org.netbeans.api.extexecution.base.input.InputProcessors;
import org.netbeans.api.extexecution.base.input.InputReaderTask;
import org.netbeans.api.extexecution.base.input.InputReaders;
import org.netbeans.modules.docker.DockerContainer;
import org.netbeans.modules.docker.DockerUtils;
import org.netbeans.modules.docker.remote.DockerException;
import org.netbeans.modules.docker.remote.DockerRemote;
import org.netbeans.modules.docker.ui.DockerOutputTask;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Petr Hejl
 */
public class ShowLogAction extends AbstractContainerAction {

    @NbBundle.Messages("LBL_ShowLogAction=Show Log")
    public ShowLogAction() {
        super(Bundle.LBL_ShowLogAction(), null);
    }

    @NbBundle.Messages({
        "# {0} - container id",
        "MSG_ShowingLog=Showing log for container {0}"
    })
    @Override
    protected String getProgressMessage(DockerContainer container) {
        return Bundle.MSG_ShowingLog(DockerUtils.getShortId(container));
    }

    @Override
    protected void performAction(DockerContainer container) throws DockerException {
        DockerRemote facade = new DockerRemote(container.getInstance());
        DockerRemote.LogResult r = facade.logs(container);
        InputOutput io = IOProvider.getDefault().getIO("Test", true);
        io.select();
        RequestProcessor.getDefault().post(new DockerOutputTask(io, r));
    }

}
