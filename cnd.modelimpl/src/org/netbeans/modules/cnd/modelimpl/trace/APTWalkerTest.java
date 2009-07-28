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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.modelimpl.trace;

import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.support.APTAbstractWalker;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileBufferFile;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;

/**
 * simple test implementation of walker
 * @author Vladimir Voskresensky
 */
public class APTWalkerTest extends APTAbstractWalker {

    public APTWalkerTest(APTFile apt, APTPreprocHandler ppHandler) {
        super(apt, ppHandler, null);
    }

    private long resolvingTime = 0;
    private long lastTime = 0;
    public long getIncludeResolvingTime() {
        return resolvingTime;
    }

    @Override
    protected void onInclude(APT apt) {
        lastTime = System.currentTimeMillis();
        super.onInclude(apt);
    }

    @Override
    protected void onIncludeNext(APT apt) {
        lastTime = System.currentTimeMillis();
        super.onIncludeNext(apt);
    }

    protected boolean include(ResolvedPath resolvedPath, APTInclude aptInclude, APTMacroMap.State postIncludeState) {
        resolvingTime += System.currentTimeMillis() - lastTime;
        if (resolvedPath != null && getIncludeHandler().pushInclude(resolvedPath.getPath(), aptInclude, resolvedPath.getIndex())) {
            APTFile apt;
            boolean res = false;
            try {
                apt = APTDriver.getInstance().findAPTLight(new FileBufferFile(resolvedPath.getPath()));
                APTWalkerTest walker = new APTWalkerTest(apt, getPreprocHandler());
                walker.visit();
                resolvingTime += walker.resolvingTime;
                res = true;               
            } catch (IOException ex) {
		DiagnosticExceptoins.register(ex);
                APTUtils.LOG.log(Level.SEVERE, "error on include " + resolvedPath, ex);// NOI18N
            } finally {
                getIncludeHandler().popInclude(); 
            }
            return postIncludeState == null;
        } else {
            return false;
        }
    }

    @Override
    protected boolean hasIncludeActionSideEffects() {
        return true;
    }
}
