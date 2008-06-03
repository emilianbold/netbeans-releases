/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.test.refactoring;

import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.modules.test.refactoring.actions.RenamePopupAction;
import org.netbeans.modules.test.refactoring.operators.FindUsagesResultOperator;
import org.netbeans.modules.test.refactoring.operators.RenameOperator;

/**
 *
 * @author Jiri Prox Jiri.Prox@SUN.Com
 */
public class RenameTest extends ModifyingRefactoring {

    public RenameTest(String name) {
        super(name);
    }

    public void testRenameClass() {       
        String fileName = "Rename";        
        openSourceFile("renameClass", fileName);        
        EditorOperator editor = new EditorOperator(fileName);
        editor.setCaretPosition(3, 17);
        new RenamePopupAction().perform(editor);
        new EventTool().waitNoEvent(1000);
        RenameOperator ro = new  RenameOperator();
        new EventTool().waitNoEvent(1000);
        ro.getNewName().typeText("Renamed");
        ro.getPreview().push();        
        new EventTool().waitNoEvent(1000);                
        FindUsagesResultOperator result = FindUsagesResultOperator.getPreview();
        result.test(result.getSource(), 0, 0);
        
        JButtonOperator jbo = new JButtonOperator(result.getRefresh());
        jbo.pushNoBlock();
        new EventTool().waitNoEvent(1000);                
        //result.test(result.getJToolbar(), 0, 0);
        

    }
    
    public static void main(String[] args) {
        TestRunner.run(RenameTest.class);
        
    }

}
