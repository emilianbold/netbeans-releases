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
package org.netbeans.modules.cnd.paralleladviser.paralleladvisermonitor.impl;

import org.netbeans.modules.cnd.paralleladviser.paralleladviserview.*;
import java.net.URL;
import javax.swing.JComponent;
import org.netbeans.modules.cnd.paralleladviser.utils.ParallelAdviserAdviceUtils;

/**
 * Loop parallelization advice.
 *
 * @author Nick Krasilnikov
 */
public class LoopParallelizationCommonAdvice implements Advice {

    public LoopParallelizationCommonAdvice() {
    }

    public JComponent getComponent() {
        return ParallelAdviserAdviceUtils.createAdviceComponent(getHtml(), null);
    }

    public String getHtml() {
        URL iconUrl = LoopParallelizationCommonAdvice.class.getClassLoader().getResource("org/netbeans/modules/cnd/paralleladviser/paralleladviserview/resources/info.png"); // NOI18N
        String html = "<a href=\"http://en.wikipedia.org/wiki/Parallel_computing\">Parallel computing</a> is a form of computation in which many calculations are carried out simultaneously, " + // NOI18N
                "operating on the principle that large problems can often be divided into smaller ones, " + // NOI18N
                "which are then solved concurrently (\"in parallel\").<br>" + // NOI18N
                "There are several ways to make you program parallel. The easiest one is to use <a href=\"http://en.wikipedia.org/wiki/OpenMP\">OpenMP</a>."; // NOI18N
        return ParallelAdviserAdviceUtils.createAdviceHtml(iconUrl, "Parallel computing", // NOI18N
                html, 800); // NOI18N
    }
}
