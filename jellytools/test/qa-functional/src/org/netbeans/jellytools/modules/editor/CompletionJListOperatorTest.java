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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.jellytools.modules.editor;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.Operator.StringComparator;
import org.netbeans.junit.NbTest;

/**
 * Tests if CompletionJListOperator works properly when the completion list
 * is invoked by typing ".", pressing Ctrl+Space and by 
 * calling  CompletionJListOperator.showCompletion().
 *
 * @author Vojtech.Sigler@Sun.COM
 */
public class CompletionJListOperatorTest extends JellyTestCase {


    private static String[] tests = new String[]
    {
        "testCompletionDot", "testCompletionInvoke", "testCompletionCtrlSpace"
    };

    private static EditorOperator eo;

    public CompletionJListOperatorTest(String isTestName)
    {
        super(isTestName);
    }

    protected void setUp() throws IOException
    {
        System.out.println("### "+getName()+" ###");
        openDataProjects("SampleProject");
        Node node = new Node(new SourcePackagesNode("SampleProject"), "sample1|SampleClass1.java"); // NOI18N
        new OpenAction().perform(node);
        eo = new EditorOperator("SampleClass1.java");   // NOI18N
        eo.setCaretPositionToEndOfLine(55);
        eo.insert("System.out");
    }

    protected void tearDown()
    {
        eo.closeDiscardAll();
    }


    public static NbTest suite()
    {
        return (NbTest) createModuleTest(CompletionJListOperatorTest.class,
        tests);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void testCompletionDot() throws Exception
    {        
        eo.typeKey('.');

        CompletionJListOperator lrComplOp = new CompletionJListOperator();
        lrComplOp.getCompletionItems();
                
        lrComplOp.clickOnItem("println()", new StringComparator() {
            public boolean equals(String caption, String match)
            {
                return caption.contains(match);
            }
        }, 2); //doubleclick

        assertTrue("The line does not contain the clicked item from CompletionList!",
                eo.contains("System.out.println()"));
        
    }

    public void testCompletionInvoke() throws Exception
    {
        eo.insert(".");

        CompletionJListOperator lrComplOp = CompletionJListOperator.showCompletion();
        lrComplOp.getCompletionItems();        

        lrComplOp.clickOnItem("println()", new StringComparator() {
            public boolean equals(String caption, String match)
            {
                return caption.contains(match);
            }
        }, 2); //doubleclick

        assertTrue("The line does not contain the clicked item from CompletionList!",
                eo.contains("System.out.println()"));
    }

    public void testCompletionCtrlSpace() throws Exception
    {
        eo.insert(".");

        eo.txtEditorPane().typeKey(KeyEvent.VK_SPACE, ' ', InputEvent.CTRL_DOWN_MASK); //press ctrl+space

        CompletionJListOperator lrComplOp = new CompletionJListOperator();
        lrComplOp.getCompletionItems();

        lrComplOp.clickOnItem("println()", new StringComparator() {
            public boolean equals(String caption, String match)
            {
                return caption.contains(match);
            }
        }, 2); //doubleclick

        assertTrue("The line does not contain the clicked item from CompletionList!",
                eo.contains("System.out.println()"));
    }
}
