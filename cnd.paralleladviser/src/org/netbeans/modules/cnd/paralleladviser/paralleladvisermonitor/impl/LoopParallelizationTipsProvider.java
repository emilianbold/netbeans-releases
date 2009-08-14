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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.cnd.paralleladviser.paralleladviserview.Advice;
import org.netbeans.modules.cnd.paralleladviser.spi.ParallelAdviserTipsProvider;

/**
 * Service that provides tips for Parallel Adviser.
 *
 * @author Nick Krasilnikov
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.paralleladviser.spi.ParallelAdviserTipsProvider.class)
public class LoopParallelizationTipsProvider implements ParallelAdviserTipsProvider {

    private final static int REPRESENTATION_TYPE_SEPARATE_TIPS = 0;
    private final static int REPRESENTATION_TYPE_ALL_TIPS_IN_ONE = 1;
    private final static int REPRESENTATION_TYPE_SEPARATE_TIPS_AND_COMMON_ONE = 2;
    private final static int representationType = REPRESENTATION_TYPE_SEPARATE_TIPS_AND_COMMON_ONE;

    private final static List<LoopParallelizationAdvice> tips = new ArrayList<LoopParallelizationAdvice>();

    public static void addTip(LoopParallelizationAdvice tip) {
        for (LoopParallelizationAdvice advice : tips) {
            if(advice.getLoop().equals(tip.getLoop())) {
                tips.remove(advice);
                break;
            }
        }
        tips.add(tip);
    }

    public static void clearTips() {
        tips.clear();
    }

    public Collection<Advice> getTips() {
        if(representationType == REPRESENTATION_TYPE_SEPARATE_TIPS) {
            return new ArrayList<Advice>(tips);
        } else if (representationType == REPRESENTATION_TYPE_ALL_TIPS_IN_ONE) {
            ArrayList<Advice> arrayList = new ArrayList<Advice>();
            if(!tips.isEmpty()) {
                arrayList.add(new LoopsParallelizationAdvice(tips));
            }
            return arrayList;
        } else {
            ArrayList<Advice> arrayList = new ArrayList<Advice>(tips);
            if(!arrayList.isEmpty()) {
                arrayList.add(new LoopParallelizationCommonAdvice());
            }
            return arrayList;
        }
    }

}
