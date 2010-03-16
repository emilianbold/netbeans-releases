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
package org.netbeans.modules.cnd.loaders;

import java.io.IOException;
import java.text.MessageFormat;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataObject;

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
        if (dao == null) {
            throw new NullPointerException("dao can't be null"); // NOI18N
        }
        if (program == null) {
            throw new NullPointerException("program can't be null"); // NOI18N
        }
        this.dao = dao;
        this.program = program;
        this.failmsg = failmsg;
    }

    @Override
    public void open() {
        ProcessBuilder pb = new ProcessBuilder(program, dao.getPrimaryFile().getPath());
        try {
            pb.start();
        } catch (IOException ex) {
            if (failmsg != null) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        MessageFormat.format(failmsg, program)));
            }
        }
    }
}
