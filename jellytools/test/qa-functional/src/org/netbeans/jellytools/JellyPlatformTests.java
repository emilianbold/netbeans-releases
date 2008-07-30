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
import org.netbeans.jellytools.actions.ActionNoBlockTest;
import org.netbeans.jellytools.actions.ActionTest;
import org.netbeans.jellytools.actions.AddLocaleActionTest;
import org.netbeans.jellytools.actions.AttachWindowActionTest;
import org.netbeans.jellytools.actions.CopyActionTest;
import org.netbeans.jellytools.actions.CustomizeActionTest;
import org.netbeans.jellytools.actions.CutActionTest;
import org.netbeans.jellytools.actions.DeleteActionTest;
import org.netbeans.jellytools.actions.EditActionTest;
import org.netbeans.jellytools.actions.ExploreFromHereActionTest;
import org.netbeans.jellytools.actions.FindActionTest;
import org.netbeans.jellytools.actions.HelpActionTest;
import org.netbeans.jellytools.actions.MaximizeWindowActionTest;
import org.netbeans.jellytools.actions.OpenActionTest;
import org.netbeans.jellytools.actions.OutputWindowViewActionTest;
import org.netbeans.jellytools.actions.PasteActionTest;
import org.netbeans.jellytools.actions.PropertiesActionTest;
import org.netbeans.jellytools.actions.RenameActionTest;
import org.netbeans.jellytools.actions.ReplaceActionTest;
import org.netbeans.jellytools.actions.SaveActionTest;
import org.netbeans.jellytools.actions.SaveAllActionTest;
import org.netbeans.jellytools.actions.ShowDescriptionAreaActionTest;
import org.netbeans.jellytools.actions.SortByCategoryActionTest;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author shura
 */
public class JellyPlatformTests {
    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.
                createConfiguration(OutputOperatorTest.class).addTest(OutputOperatorTest.tests).
                addTest(OutputTabOperatorTest.class, OutputTabOperatorTest.tests).
                addTest(TopComponentOperatorTest.class, TopComponentOperatorTest.tests).
                addTest(ActionTest.class, ActionTest.tests).
                addTest(ActionNoBlockTest.class, ActionNoBlockTest.tests).
                addTest(AddLocaleActionTest.class, AddLocaleActionTest.tests).
                addTest(AttachWindowActionTest.class, AttachWindowActionTest.tests).
                addTest(CopyActionTest.class).
                addTest(CustomizeActionTest.class).
                addTest(CutActionTest.class).
                addTest(DeleteActionTest.class).
                addTest(EditActionTest.class).
                addTest(ExploreFromHereActionTest.class).
                addTest(FindActionTest.class).
                addTest(HelpActionTest.class).
                addTest(MaximizeWindowActionTest.class).
                addTest(OpenActionTest.class).
                addTest(OutputWindowViewActionTest.class).
                addTest(PasteActionTest.class).
                addTest(PropertiesActionTest.class).
                addTest(RenameActionTest.class).
                addTest(ReplaceActionTest.class).
                addTest(SaveActionTest.class, SaveActionTest.tests).
                addTest(SaveAllActionTest.class).
                addTest(ShowDescriptionAreaActionTest.class).
                addTest(SortByCategoryActionTest.class);
        return NbModuleSuite.create(conf.clusters(".*").enableModules(".*"));
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
