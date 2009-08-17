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

/*
 * Output.java
 *
 * Created on March 25, 2005, 2:22 PM
 */

package org.netbeans.modules.compapp.catd.n2m;

import org.netbeans.modules.compapp.catd.util.Util;
import java.io.*;

/**
 *
 * @author blu
 */
public class Output {
    private String mName;
    private File mActual;
    private File mExpected;

    /** Creates a new instance of Output */
    public Output(String name, File actual, File expected) {
        mName = name;
        mActual = actual;
        mExpected = expected;
    }

    public String getName() {
        return mName;
    }

    public String getExpected() {
        String ret = Util.getFileContent(mExpected);
        return ret;
    }

    public String getExpectedWithoutCRNL() {
        String ret = Util.getFileContentWithoutCRNL(mExpected);
        return ret;
    }

    public String getActual() {
        String ret = Util.getFileContent(mActual);
        return ret;
    }

    public String getActualWithoutCRNL() {
        String ret = Util.getFileContentWithoutCRNL(mActual);
        return ret;
    }

    public void removeActual() {
        if (mActual != null && mActual.exists()) {
            mActual.delete();
        }
    }

}
