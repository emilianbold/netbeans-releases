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

package org.netbeans.test.mobility.tools;

//<editor-fold desc="imports">
import org.netbeans.jellytools.Bundle;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
//</editor-fold>

/**
 *
 * @author joshis
 */
public class toolsActionsTests extends JellyTestCase {
    
    public static final String PROJECT_NAME_MIDP = "FileWizardTestProject_MIDP";
    public static final String WIZARD_BUNDLE = "org.netbeans.modules.mobility.project.ui.wizard.Bundle";
    public static final String PROJECT_MIDP = Bundle.getStringTrimmed(WIZARD_BUNDLE, "Templates/Project/J2ME/MobileApplication");
    public static final String CATEGORY_MIDP = Bundle.getStringTrimmed(WIZARD_BUNDLE, "Templates/MIDP");
    
    public toolsActionsTests(String s) {
        super(s);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new toolsActionsTests("mobilityDeploymentInvokeTest"));
        suite.addTest(new toolsActionsTests("keystoresTest"));
        return suite;
    }
    
    public void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    public void mobilityDeploymentInvokeTest()  {
        new ActionNoBlock("Tools|Mobility Deployment", null).perform();
        new NbDialogOperator("Mobility Deployment").btClose().push();
    }
    
    public void keystoresTest()  {
        new ActionNoBlock("Tools|Keystores", null).perform();
        new NbDialogOperator("Keystores Manager").btClose().push();
    }
    
}



