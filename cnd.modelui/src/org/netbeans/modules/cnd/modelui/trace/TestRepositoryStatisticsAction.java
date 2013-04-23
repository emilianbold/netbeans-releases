/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelui.trace;

import java.util.Collection;
import java.util.Date;
import javax.swing.JOptionPane;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.repository.support.RepositoryStatistics;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * @author Vladimir Kvashin
 */

public class TestRepositoryStatisticsAction extends TestProjectActionBase {

    @Override
    public String getName() {
        return NbBundle.getMessage(TestRepositoryStatisticsAction.class, "CTL_TestRepositoryStatistics"); //NOI18N
    }


    @Override
    protected void performAction(Collection<CsmProject> csmProjects) {

        NotifyDescriptor nd = new DialogDescriptor.Confirmation(
                NbBundle.getMessage(TestRepositoryStatisticsAction.class, "CTL_TRS_Message"),
                NbBundle.getMessage(TestRepositoryStatisticsAction.class, "CTL_TRS_Title"),
                NotifyDescriptor.YES_NO_CANCEL_OPTION);

        Object ret = DialogDisplayer.getDefault().notify(nd);
        if (ret.equals(JOptionPane.CANCEL_OPTION)) {
            return;
        }
        InputOutput io = IOProvider.getDefault().getIO("", false);
        io.select();
        OutputWriter out = io.getOut();
        RepositoryStatistics.report(out, new Date().toString());
        if (ret.equals(JOptionPane.YES_OPTION)) {
            RepositoryStatistics.clear();
        }
    }
}
