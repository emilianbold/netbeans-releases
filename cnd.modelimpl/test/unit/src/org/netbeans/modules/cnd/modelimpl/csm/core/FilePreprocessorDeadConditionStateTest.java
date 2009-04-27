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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.util.Arrays;
import org.netbeans.modules.cnd.test.BaseTestCase;

/**
 *
 * @author Vladimir Voskresensky
 */
public class FilePreprocessorDeadConditionStateTest extends BaseTestCase {

    public FilePreprocessorDeadConditionStateTest() {
        super("dead blocks test");
    }

    public void testDeadBlocksComparision() throws Exception {
        FilePreprocessorDeadConditionState state1 = new FilePreprocessorDeadConditionState("state1");
        state1.addBlockImpl(10, 20);
        state1.addBlockImpl(30, 60);
        state1.addBlockImpl(70, 80);
        state1.trimSize();

        FilePreprocessorDeadConditionState state2 = new FilePreprocessorDeadConditionState("state2");
        state2.addBlockImpl(10, 20);
        state2.addBlockImpl(70, 80);
        state2.trimSize();

        FilePreprocessorDeadConditionState biggest = new FilePreprocessorDeadConditionState("biggest");
        biggest.addBlockImpl(5, 90);
        biggest.trimSize();

        FilePreprocessorDeadConditionState state4 = new FilePreprocessorDeadConditionState("state4");
        state4.addBlockImpl(40, 50);
        state4.trimSize();

        FilePreprocessorDeadConditionState state5 = new FilePreprocessorDeadConditionState("state5");
        state5.addBlockImpl(10, 20);
        state5.addBlockImpl(40, 50);
        state5.trimSize();

        FilePreprocessorDeadConditionState state6 = new FilePreprocessorDeadConditionState("state6");
        state6.addBlockImpl(30, 40);
        state6.addBlockImpl(50, 60);
        state6.trimSize();

        FilePreprocessorDeadConditionState state7 = new FilePreprocessorDeadConditionState("state7");
        state7.addBlockImpl(50, 60);
        state7.addBlockImpl(70, 80);
        state7.trimSize();

        FilePreprocessorDeadConditionState empty = new FilePreprocessorDeadConditionState("emtpy");
        empty.trimSize();

        assertTrue("state1:"+state1 + " must replace " + biggest, state1.canReplaceOther(biggest));
        assertFalse("state1:"+state1 + " is not replaceable by " + biggest, biggest.canReplaceOther(state1));

        assertTrue("state2:"+state2 + " must replace " + state1, state2.canReplaceOther(state1));
        assertFalse("state2:"+state2 + " is not replaceable by " + state1, state1.canReplaceOther(state2));

        assertTrue("state4:"+state4 + " must replace " + state1, state4.canReplaceOther(state1));
        assertFalse("state4:"+state4 + " is not replaceable by " + state1, state1.canReplaceOther(state4));

        assertTrue("state4:"+state4 + " must replace " + biggest, state4.canReplaceOther(biggest));
        assertFalse("state4:"+state4 + " is not replaceable by " + biggest, biggest.canReplaceOther(state4));

        assertTrue("emtpy:"+empty + " must replace " + state1, empty.canReplaceOther(state1));
        assertFalse("emtpy:"+empty + " is not replaceable by " + state1, state1.canReplaceOther(empty));

        assertTrue("emtpy:"+empty + " must replace " + state2, empty.canReplaceOther(state2));
        assertFalse("emtpy:"+empty + " is not replaceable by " + state2, state2.canReplaceOther(empty));

        assertTrue("emtpy:"+empty + " must replace " + biggest, empty.canReplaceOther(biggest));
        assertFalse("emtpy:"+empty + " is not replaceable by " + biggest, biggest.canReplaceOther(empty));
        
        assertTrue("emtpy:"+empty + " must replace " + state4, empty.canReplaceOther(state4));
        assertFalse("emtpy:"+empty + " is not replaceable by " + state4, state4.canReplaceOther(empty));

        assertFalse("state4:"+state4 + " is not comaprable with " + state2, state4.canReplaceOther(state2));
        assertFalse("state2:"+state2 + " is not comaprable with " + state4, state2.canReplaceOther(state4));

        assertFalse("state5:" + state5 + " is not comaprable with " + state2, state5.canReplaceOther(state2));
        assertFalse("state2:" + state2 + " is not comaprable with " + state5, state2.canReplaceOther(state5));

        assertFalse("state6:" + state6 + " is not comaprable with " + state2, state6.canReplaceOther(state2));
        assertFalse("state2:" + state2 + " is not comaprable with " + state6, state2.canReplaceOther(state6));

        assertTrue("state4:" + state4 + " must replace " + state5, state4.canReplaceOther(state5));
        assertFalse("state4:" + state4 + " is not replaceable by " + state5, state5.canReplaceOther(state4));

        assertTrue("["+10+"-"+20+"] is in active block of " + state4, state4.isInActiveBlock(10, 20));

        assertFalse("state7:" + state7 + " is not comaprable with " + state2, state7.canReplaceOther(state2));
        assertFalse("state2:" + state2 + " is not comaprable with " + state7, state2.canReplaceOther(state7));
        assertFalse("state7:" + state7 + " is not comaprable with " + state6, state7.canReplaceOther(state6));
        assertFalse("state6:" + state6 + " is not comaprable with " + state7, state6.canReplaceOther(state7));
        assertTrue("state7: " + state7 + " can be replaced by composition of " + state2 + " " + state6, state7.canBeReplacedByComposition(Arrays.asList(state2, state6)));
    }
}
