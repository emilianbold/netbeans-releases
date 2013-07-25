/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.php.editor.indent;

import javax.swing.text.BadLocationException;
import org.netbeans.modules.php.editor.PHPCodeCompletionTestBase;

/**
 * Test for PHPBracesMatcher
 *
 * @author Marek Slama
 */
public class PHPBracesMatcherTest extends PHPCodeCompletionTestBase {

    public PHPBracesMatcherTest(String testName) {
        super(testName);
    }

    /**
     * Test for BracesMatcher, first ^ gives current caret position,
     * second ^ gives matching caret position. Test is done in forward and backward direction.
     */
    private void match2(String original) throws BadLocationException {
        super.assertMatches2(wrapAsPhp(original));
    }

    private static String wrapAsPhp(String s) {
        // XXX: remove \n
        return "<?php\n" + s + "\n?>";
    }

    public void testFindMatching2() throws Exception {
        match2("x=^(true^)\ny=5");
    }

    public void testFindMatching3() throws Exception {
        match2("x=^(true || (false)^)\ny=5");
    }


    public void testIssue164495_01() throws Exception {
        match2("foreach ^($q['answers'] as $a^)\n{\n $tag=\"{value_$a[id]}\";\n}");
    }

    public void testIssue164495_02() throws Exception {
        match2("foreach ($q^['answers'^] as $a)\n{\n $tag=\"{value_$a[id]}\";\n}");
    }

    public void testIssue164495_03() throws Exception {
        match2("foreach ($q['answers'] as $a)\n^{\n $tag=\"{value_$a[id]}\";\n^}");
    }


    public void testIssue197709_01() throws Exception {
        match2("if (true) ^{\n"
                + "    echo \"Some string with braced ${variables[ $index ]} in it.\";\n"
                + "^}");
    }

    public void testIssue197709_02() throws Exception {
        match2("if (true) {\n"
                + "    echo \"Some string with braced ^${variables[ $index ]^} in it.\";\n"
                + "}");
    }

    public void testAlternativeSyntax_01() throws Exception {
        match2(
                "if ($i == 0) :\n"
                + "    if ($j == 0) :\n"
                + "    endif;\n"
                + "elseif ($i == 1)^:\n"
                + "    if ($j == 2):\n"
                + "        $l = 33;\n"
                + "    else:\n"
                + "        $l = 22;\n"
                + "    endif;\n"
                + "^endif;\n"
                + "\n");
    }

    public void testAlternativeSyntax_02() throws Exception {
        match2(
                "if ($i == 0) :\n"
                + "    if ($j == 0) ^:\n"
                + "    ^endif;\n"
                + "elseif ($i == 1):\n"
                + "    if ($j == 2):\n"
                + "        $l = 33;\n"
                + "    else:\n"
                + "        $l = 22;\n"
                + "    endif;\n"
                + "endif;\n");
    }

    public void testAlternativeSyntax_03() throws Exception {
        match2(   "for ($i = 0; $i < count($array); $i++) ^:\n"
                + "    for ($i = 0; $i < count($array); $i++) :\n"
                + "    endfor;\n"
                + "^endfor;\n");
    }

    public void testAlternativeSyntax_04() throws Exception {
        match2(   "for ($i = 0; $i < count($array); $i++) :\n"
                + "    for ($i = 0; $i < count($array); $i++) ^:\n"
                + "    ^endfor;\n"
                + "endfor;\n");
    }

    public void testAlternativeSyntax_05() throws Exception {
        match2(   "while (true)^:\n"
                + "    while(false):\n"
                + "        if ($a == 1):\n"
                + "\n            "
                + "        endif;\n"
                + "    endwhile;\n"
                + "^endwhile;\n");
    }

    public void testAlternativeSyntax_06() throws Exception {
        match2(   "while (true):\n"
                + "    while(false)^:\n"
                + "        if ($a == 1):\n"
                + "\n            "
                + "        endif;\n"
                + "    ^endwhile;\n"
                + "endwhile;\n");
    }

    public void testAlternativeSyntax_07() throws Exception {
        match2(   "switch ($i)^:\n"
                + "    case 22:\n"
                + "        $i = 44;\n"
                + "        break;\n"
                + "    case 33:\n"
                + "    case 44:\n"
                + "        $i = 55;\n"
                + "        break;\n"
                + "    default:\n"
                + "        $i = 66;\n"
                + "^endswitch;\n");
    }


}
