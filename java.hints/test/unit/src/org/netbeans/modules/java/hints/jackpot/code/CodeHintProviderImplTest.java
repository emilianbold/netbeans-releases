/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jackpot.code;

import com.sun.source.tree.Tree.Kind;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.netbeans.modules.java.hints.jackpot.code.CodeHintProviderImpl.WorkerImpl;
import org.netbeans.modules.java.hints.jackpot.code.spi.Constraint;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerPattern;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerTreeKind;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import static org.junit.Assert.*;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescription;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescription.PatternDescription;
import org.netbeans.modules.java.hints.jackpot.spi.HintMetadata;
import org.netbeans.spi.editor.hints.ErrorDescription;

/**
 *
 * @author lahvac
 */
@Hint(id="hintPattern", category="general")
public class CodeHintProviderImplTest {

    public CodeHintProviderImplTest() {
    }

    @Test
    public void testComputeHints() throws Exception {
        Map<HintMetadata, ? extends Collection<? extends HintDescription>> hints = new CodeHintProviderImpl().computeHints();

        Set<String> golden = new HashSet<String>(Arrays.asList(
            "null:$1.toURL():public static org.netbeans.spi.editor.hints.ErrorDescription org.netbeans.modules.java.hints.jackpot.code.CodeHintProviderImplTest.hintPattern1(org.netbeans.modules.java.hints.jackpot.spi.HintContext)",
            "METHOD_INVOCATION:null:public static org.netbeans.spi.editor.hints.ErrorDescription org.netbeans.modules.java.hints.jackpot.code.CodeHintProviderImplTest.hintPattern2(org.netbeans.modules.java.hints.jackpot.spi.HintContext)"
        ));

        for (Collection<? extends HintDescription> hds : hints.values()) {
            for (HintDescription d : hds) {
                golden.remove(toString(d));
            }
        }

        assertTrue(golden.toString(), golden.isEmpty());
    }

    private static String toString(HintDescription hd) throws Exception {
        StringBuilder sb = new StringBuilder();

        sb.append(hd.getTriggerKind());
        sb.append(":");
        
        PatternDescription p = hd.getTriggerPattern();

        sb.append(p != null ? p.getPattern() : "null");
        //TODO: constraints
        sb.append(":");
        sb.append(((WorkerImpl) hd.getWorker()).getMethod().toGenericString());

        return sb.toString();
    }

    @TriggerPattern(value="$1.toURL()", constraints=@Constraint(variable="$1", type="java.io.File"))
    public static ErrorDescription hintPattern1(HintContext ctx) {
        return null;
    }

    @TriggerTreeKind(Kind.METHOD_INVOCATION)
    public static ErrorDescription hintPattern2(HintContext ctx) {
        return null;
    }

}