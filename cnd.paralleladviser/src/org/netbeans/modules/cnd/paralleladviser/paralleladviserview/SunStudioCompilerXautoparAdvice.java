/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.paralleladviser.paralleladviserview;

import java.net.URL;
import javax.swing.JComponent;
import org.netbeans.modules.cnd.paralleladviser.utils.ParallelAdviserAdviceUtils;

/**
 * Sun Studio Compiler advice.
 *
 * @author Nick Krasilnikov
 */
public class SunStudioCompilerXautoparAdvice implements Advice {

    public JComponent getComponent() {

        URL iconUrl = SunStudioCompilerXautoparAdvice.class.getClassLoader().getResource("org/netbeans/modules/cnd/paralleladviser/paralleladviserview/resources/info.png"); // NOI18N

        return ParallelAdviserAdviceUtils.createAdviceComponent(iconUrl, "Automatic parallelization with Sun Studio compilers", // NOI18N
                "<b>-xautopar</b> turns on automatic parallelization for multiple processors. " + // NOI18N
                "Does dependence analysis (analyze loops for inter-iteration data dependence) and loop restructuring. " + // NOI18N
                "If optimization is not at-xO3 or higher, optimization is raised to-xO3 and a warning is emitted." + // NOI18N
                "<br>" + // NOI18N
                "<br>" + // NOI18N
                "To achieve faster execution, this option requires a multiple processor system. " + // NOI18N
                "On a single-processor system, the resulting binary usually runs slower." + // NOI18N
                "<br>" + // NOI18N
                "<br>" + // NOI18N
                "If you use <b>-xautopar</b> and compile and link in one step, " + // NOI18N
                "then linking automatically includes the microtasking library and the threads-safe C runtime library. " + // NOI18N
                "If you use <b>-xautopar</b> and compile and link in separate steps, then you must also link with <b>-xautopar</b>." + // NOI18N
                "<br>" + // NOI18N
                "<br>" + // NOI18N
                "<a href=\"http://developers.sun.com/sunstudio/downloads/\">Download Sun Studio</a>.", null); // NOI18N
    }
}
