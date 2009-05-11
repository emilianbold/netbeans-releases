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
import org.netbeans.jellytools.actions.AttachWindowActionTest;
import org.netbeans.jellytools.actions.CleanJavaProjectActionTest;
import org.netbeans.jellytools.actions.CustomizeActionTest;
import org.netbeans.jellytools.actions.EditActionTest;
import org.netbeans.jellytools.actions.ExploreFromHereActionTest;
import org.netbeans.jellytools.actions.FindInFilesActionTest;
import org.netbeans.jellytools.actions.MaximizeWindowActionTest;
import org.netbeans.jellytools.actions.OpenActionTest;
import org.netbeans.jellytools.actions.PaletteViewActionTest;
import org.netbeans.jellytools.actions.ProjectViewActionTest;
import org.netbeans.jellytools.actions.RenameActionTest;
import org.netbeans.jellytools.actions.SaveAsTemplateActionTest;
import org.netbeans.jellytools.modules.db.actions.DbActionsTest;
import org.netbeans.jellytools.modules.db.nodes.DatabasesNodeTest;
import org.netbeans.jellytools.modules.debugger.BreakpointsWindowOperatorTest;
import org.netbeans.jellytools.modules.debugger.actions.BreakpointsWindowActionTest;
import org.netbeans.jellytools.modules.form.FormEditorOperatorTest;
import org.netbeans.jellytools.modules.form.properties.editors.MethodPickerOperatorTest;
import org.netbeans.jellytools.modules.form.properties.editors.ParametersPickerOperatorTest;
import org.netbeans.jellytools.modules.form.properties.editors.PropertyPickerOperatorTest;
import org.netbeans.jellytools.modules.javacvs.VersioningOperatorTest;
import org.netbeans.jellytools.nodes.HTMLNodeTest;
import org.netbeans.jellytools.nodes.ImageNodeTest;
import org.netbeans.jellytools.nodes.URLNodeTest;
import org.netbeans.jellytools.nodes.UnrecognizedNodeTest;
import org.netbeans.jellytools.properties.PropertyTest;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author shura
 */
public class StableTests {
    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.
                createConfiguration(EditActionTest.class).
        addTest(AttachWindowActionTest.class, AttachWindowActionTest.tests).
        addTest(BreakpointsWindowActionTest.class).
        addTest(BreakpointsWindowOperatorTest.class).
        addTest(CleanJavaProjectActionTest.class).
        addTest(CustomizeActionTest.class).
        addTest(DatabasesNodeTest.class).
        addTest(DbActionsTest.class).
        addTest(EditorWindowOperatorTest.class, EditorWindowOperatorTest.tests).
        addTest(ExploreFromHereActionTest.class, ExploreFromHereActionTest.tests).
        addTest(FilesTabOperatorTest.class, FilesTabOperatorTest.tests).
        addTest(FindInFilesActionTest.class, FindInFilesActionTest.tests).
        addTest(FindInFilesOperatorTest.class).
        addTest(FormEditorOperatorTest.class, FormEditorOperatorTest.tests).
        addTest(HTMLNodeTest.class, HTMLNodeTest.tests).
        addTest(ImageNodeTest.class, ImageNodeTest.tests).
        addTest(MaximizeWindowActionTest.class).
        addTest(MethodPickerOperatorTest.class, MethodPickerOperatorTest.tests).
        addTest(OpenActionTest.class).
        addTest(NewJavaFileNameLocationStepOperatorTest.class, NewJavaFileNameLocationStepOperatorTest.tests).
        addTest(PaletteViewActionTest.class).
        addTest(ParametersPickerOperatorTest.class, ParametersPickerOperatorTest.tests).
        addTest(ProjectsTabOperatorTest.class, ProjectsTabOperatorTest.tests).
        addTest(ProjectViewActionTest.class).
        addTest(PropertyPickerOperatorTest.class, PropertyPickerOperatorTest.tests).
        addTest(PropertyTest.class, PropertyTest.tests).
        addTest(RenameActionTest.class).
        addTest(RuntimeTabOperatorTest.class).
        addTest(SaveAsTemplateActionTest.class).
        addTest(SaveAsTemplateOperatorTest.class, SaveAsTemplateOperatorTest.tests).
        addTest(SearchResultsOperatorTest.class, SearchResultsOperatorTest.tests).
        addTest(UnrecognizedNodeTest.class, UnrecognizedNodeTest.tests).
        addTest(URLNodeTest.class).
        addTest(VersioningOperatorTest.class, VersioningOperatorTest.tests);
        return NbModuleSuite.create(conf.clusters(".*").enableModules(".*"));
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
