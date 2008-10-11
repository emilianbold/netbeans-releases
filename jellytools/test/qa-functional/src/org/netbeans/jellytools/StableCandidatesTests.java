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

package org.netbeans.jellytools;

import junit.framework.Test;
import org.netbeans.jellytools.actions.ActionTest;
import org.netbeans.jellytools.actions.CopyActionTest;
import org.netbeans.jellytools.actions.CutActionTest;
import org.netbeans.jellytools.actions.DebugProjectActionTest;
import org.netbeans.jellytools.actions.EditActionTest;
import org.netbeans.jellytools.actions.NewFileActionTest;
import org.netbeans.jellytools.actions.OpenActionTest;
import org.netbeans.jellytools.actions.OutputWindowViewActionTest;
import org.netbeans.jellytools.actions.PropertiesActionTest;
import org.netbeans.jellytools.actions.ReplaceActionTest;
import org.netbeans.jellytools.actions.RuntimeViewActionTest;
import org.netbeans.jellytools.actions.ShowDescriptionAreaActionTest;
import org.netbeans.jellytools.actions.SortByCategoryActionTest;
import org.netbeans.jellytools.actions.SortByNameActionTest;
import org.netbeans.jellytools.modules.debugger.actions.DeleteAllBreakpointsActionTest;
import org.netbeans.jellytools.modules.javacvs.CheckoutWizardOperatorTest;
import org.netbeans.jellytools.modules.javacvs.ImportWizardOperatorTest;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author shura
 */
public class StableCandidatesTests {
    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.
                createConfiguration(CopyActionTest.class).
        addTest(ActionTest.class, ActionTest.tests).
        addTest(CheckoutWizardOperatorTest.class, CheckoutWizardOperatorTest.tests).
        addTest(CutActionTest.class, CutActionTest.tests).
        addTest(DebugProjectActionTest.class, DebugProjectActionTest.tests).
        addTest(DeleteAllBreakpointsActionTest.class).
        addTest(EditActionTest.class).
        addTest(EditorOperatorTest.class, EditorOperatorTest.tests).
        addTest(ImportWizardOperatorTest.class).
        addTest(NewFileActionTest.class, NewFileActionTest.tests).
        addTest(NewProjectWizardOperatorTest.class, NewProjectWizardOperatorTest.tests).
        addTest(OpenActionTest.class).
        addTest(OutputOperatorTest.class, OutputOperatorTest.tests).
        addTest(OutputWindowViewActionTest.class).
        addTest(PropertiesActionTest.class).
        addTest(ReplaceActionTest.class).
        addTest(RuntimeViewActionTest.class).
        addTest(ShowDescriptionAreaActionTest.class).
        addTest(SortByCategoryActionTest.class).
        addTest(SortByNameActionTest.class);
        return NbModuleSuite.create(conf.clusters(".*").enableModules(".*"));
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
