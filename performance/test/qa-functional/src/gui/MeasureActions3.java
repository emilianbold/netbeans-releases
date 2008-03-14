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

package gui;


import org.netbeans.junit.NbTestSuite;
import gui.action.*;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author  mmirilovic@netbeans.org
 */
public class MeasureActions3  {

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();

        suite.addTest(new TypingInEditor("testJavaEditor", "Type a character in Java Editor"));
        suite.addTest(new TypingInEditor("testTxtEditor", "Type a character in Txt Editor"));

        suite.addTest(new PageUpPageDownInEditor("testPageUp", "Press Page Up in the editor"));
        suite.addTest(new PageUpPageDownInEditor("testPageDown", "Press Page Down in the editor"));
        
        suite.addTest(new JavaCompletionInEditor("measureTime", "Invoke Code Completion dialog in Editor"));
        
      
        suite.addTest(new CloseEditor("testClosing20kBJavaFile", "Close Java file (20kB)"));
        suite.addTest(new CloseEditor("testClosing20kBFormFile", "Close Form file (20kB)"));
        
        
        suite.addTest(new CloseEditorTab("measureTime", "Close on tab from Editor window"));
        
        suite.addTest(new CloseEditorModified("measureTime", "Close modified Java file"));
        
        suite.addTest(new SaveModifiedFile("measureTime", "Save modified Java file"));
        
        suite.addTest(new SelectCategoriesInNewFile("testSelectGUIForms","Select GUI Forms in New File"));
        suite.addTest(new SelectCategoriesInNewFile("testSelectXML","Select XML in New File"));
        suite.addTest(new SelectCategoriesInNewFile("testSelectOther","Select Other in New File"));
        
        suite.addTest(new AddToFavorites("testAddJavaFile", "Add to Favorites Java file"));
        
        
        return suite;
    }
    
}
