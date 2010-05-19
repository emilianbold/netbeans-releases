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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.loaders;

import java.io.IOException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataObject;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 * {@link OpenCookie} implementation that launches external program
 * with DataObject's primary file.
 *
 * @author Alexey Vladykin
 */
/*package*/ final class ExternalProgramOpenCookie implements OpenCookie {

    private final DataObject dao;
    private final String program;
    private final String failmsg;

    public ExternalProgramOpenCookie(DataObject dao, String program, String failmsg) {
        Parameters.notNull("dao", dao);
        Parameters.notNull("program", program);
        this.dao = dao;
        this.program = program;
        this.failmsg = failmsg;
    }

    @Override
    public void open() {
        boolean success = false;
        ProcessBuilder pb = new ProcessBuilder(program, dao.getPrimaryFile().getPath());
        try {
            pb.start();
            success = true;
        } catch (IOException ex) {
        }

        if (!success && Utilities.isMac()) {
            // On Mac the built-in "open" command can launch installed
            // applications without having them in PATH. This fixes
            // bug #178742 - NetBeans can't launch Qt Designer
            pb = new ProcessBuilder("open", "-a", program, dao.getPrimaryFile().getPath()); // NOI18N
            try {
                // "open" exits immediately, it does not wait until
                // launched application finishes, so waitFor() can be safely used
                int exitCode = pb.start().waitFor();
                success = exitCode == 0;
            } catch (IOException ex) {
            } catch (InterruptedException ex) {
            }
        }

        if (!success && failmsg != null) {
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(failmsg));
        }
    }
}
