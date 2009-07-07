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
package o.n.m.ruby.qaf;

import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NewRubyFileNameLocationStepOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author lukas
 */
public class RubyWizardsTest extends RubyTestCase {

    public RubyWizardsTest(String s) {
        super(s);
    }

    @Override
    protected String getProjectName() {
        return "RubyWizards"; //NOI18N
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(RubyWizardsTest.class).enableModules(".*").clusters(".*")); //NOI18N
    }

    /**
     * Test create new RHTML File
     */
    public void testRHTMLfile() {
        //RHTML File
        String label = Bundle.getStringTrimmed("org.netbeans.modules.ruby.rhtml.resources.Bundle", "Templates/Ruby/_view.rhtml");
        createNewRubyFile(getProject(), label);
        String name = "my_rhtml"; //NOI18N
        NewRubyFileNameLocationStepOperator op = new NewRubyFileNameLocationStepOperator();
        op.setObjectName(name);
        op.finish();
        EditorOperator eo = new EditorOperator(name + ".rhtml"); //NOI18N
        assertNotNull(eo);
        assertTrue(eo.getText().indexOf(name) > -1);
    }

    /**
     * Test create new ERB File
     */
    public void testERBfile() {
        //ERB File
        String label = Bundle.getStringTrimmed("org.netbeans.modules.ruby.rhtml.resources.Bundle", "Templates/Ruby/_view.erb");
        createNewRubyFile(getProject(), label);
        String name = "my_erb"; //NOI18N
        NewRubyFileNameLocationStepOperator op = new NewRubyFileNameLocationStepOperator();
        op.setObjectName(name);
        op.finish();
        EditorOperator eo = new EditorOperator(name + ".erb"); //NOI18N
        assertNotNull(eo);
        assertTrue(eo.getText().indexOf(name) > -1);
    }

    /**
     * Create new Ruby Class
     */
    public void testRubyClass() {
        //Ruby Class
        String label = Bundle.getStringTrimmed("org.netbeans.modules.ruby.rubyproject.ui.resources.Bundle", "Templates/Ruby/class.rb");
        createNewRubyFile(getProject(), label);
        String name = "my_ruby_class"; //NOI18N
        NewRubyFileNameLocationStepOperator op = new NewRubyFileNameLocationStepOperator();
        op.setObjectName(name);
        new JComboBoxOperator(op, new LocationCBFinder()).selectItem(0);
        op.finish();
        EditorOperator eo = new EditorOperator(name + ".rb"); //NOI18N
        assertNotNull(eo);
        assertTrue(eo.getText().indexOf("class MyRubyClass") > -1); //NOI18N
        assertTrue(eo.getText().indexOf("module") < 0); //NOI18N
        //in a custom module
        name = "my_mod_ruby_class"; //NOI18N
        String modName = "Mymod"; //NOI18N
        createNewRubyFile(getProject(), label);
        op = new NewRubyFileNameLocationStepOperator();
        op.setObjectName(name);
        new JTextFieldOperator(op, new ModuleTFFinder()).typeText(modName);
        new JComboBoxOperator(op, new LocationCBFinder()).selectItem(0);
        op.finish();
        eo = new EditorOperator(name + ".rb"); //NOI18N
        assertNotNull(eo);
        assertTrue(eo.getText().indexOf("class MyModRubyClass") > -1); //NOI18N
        assertTrue(eo.getText().indexOf("module " + modName) > -1); //NOI18N
    }

    /**
     * Test create new Ruby File
     */
    public void testRubyFile() {
        //Ruby File
        String label = Bundle.getStringTrimmed("org.netbeans.modules.ruby.rubyproject.ui.resources.Bundle", "Templates/Ruby/main.rb");
        createNewRubyFile(getProject(), label);
        String name = "my_ruby_file"; //NOI18N
        NewRubyFileNameLocationStepOperator op = new NewRubyFileNameLocationStepOperator();
        op.setObjectName(name);
        new JComboBoxOperator(op, new LocationCBFinder()).selectItem(0);
        op.finish();
        EditorOperator eo = new EditorOperator(name + ".rb"); //NOI18N
        assertNotNull(eo);
        assertTrue(eo.getText().indexOf("\"Hello World\"") > -1); //NOI18N
    }

    /**
     * Test create new Ruby Module
     */
    public void testRubyModule() {
        //Ruby Module
        String label = Bundle.getStringTrimmed("org.netbeans.modules.ruby.rubyproject.ui.resources.Bundle", "Templates/Ruby/module.rb");
        createNewRubyFile(getProject(), label);
        String name = "my_ruby_module"; //NOI18N
        NewRubyFileNameLocationStepOperator op = new NewRubyFileNameLocationStepOperator();
        op.setObjectName(name);
        new JComboBoxOperator(op, new LocationCBFinder()).selectItem(0);
        op.finish();
        EditorOperator eo = new EditorOperator(name + ".rb"); //NOI18N
        assertNotNull(eo);
        assertTrue(eo.getText().indexOf("module MyRubyModule") > -1); //NOI18N
        assertTrue(eo.getText().indexOf("class") < 0); //NOI18N
        //in a custom module
        name = "my_submodule"; //NOI18N
        String modName = "MyOrigMod"; //NOI18N
        createNewRubyFile(getProject(), label);
        op = new NewRubyFileNameLocationStepOperator();
        op.setObjectName(name);
        new JTextFieldOperator(op, new ModuleTFFinder()).typeText(modName);
        new JComboBoxOperator(op, new LocationCBFinder()).selectItem(0);
        op.finish();
        eo = new EditorOperator(name + ".rb"); //NOI18N
        assertNotNull(eo);
        int subIdx = eo.getText().indexOf("module MySubmodule"); //NOI18N
        int origIdx = eo.getText().indexOf("module " + modName); //NOI18N
        assertTrue(subIdx > -1);
        assertTrue(origIdx > -1);
        assertTrue(origIdx < subIdx);
    }

    /**
     * Test create new JavaScript File
     */
    public void testJSfile() {
        //JavaScript File
        String label = Bundle.getStringTrimmed("org.netbeans.modules.javascript.editing.Bundle", "Templates/Other/javascript.js");
        createNewRubyFile(getProject(), label);
        String name = "my_js"; //NOI18N
        NewRubyFileNameLocationStepOperator op = new NewRubyFileNameLocationStepOperator();
        op.setObjectName(name);
        op.finish();
        EditorOperator eo = new EditorOperator(name + ".js"); //NOI18N
        assertNotNull(eo);
    }

    /**
     * Test create new JSON File
     */
    public void testJSONfile() {
        //JSON File
        String label = Bundle.getStringTrimmed("org.netbeans.modules.javascript.editing.Bundle", "Templates/Other/json.json");
        createNewRubyFile(getProject(), label);
        String name = "my_json"; //NOI18N
        NewRubyFileNameLocationStepOperator op = new NewRubyFileNameLocationStepOperator();
        op.setObjectName(name);
        op.finish();
        EditorOperator eo = new EditorOperator(name + ".json"); //NOI18N
        assertNotNull(eo);
        assertTrue(eo.getText().indexOf("\"name\":") > -1); //NOI18N
    }

    /**
     * Test create new YAML File
     */
    public void testYAMLfile() {
        //YAML File
        String label = Bundle.getStringTrimmed("org.netbeans.modules.languages.yaml.Bundle", "Templates/Ruby/EmptyYAML.yml");
        createNewRubyFile(getProject(), label);
        String name = "my_yaml"; //NOI18N
        NewRubyFileNameLocationStepOperator op = new NewRubyFileNameLocationStepOperator();
        op.setObjectName(name);
        op.finish();
        EditorOperator eo = new EditorOperator(name + ".yml"); //NOI18N
        assertNotNull(eo);
    }

    /**
     * Test create new Ruby Unit Test
     */
    public void testRubyUTest() {
        //Ruby Unit Test
        String label = Bundle.getStringTrimmed("org.netbeans.modules.ruby.rubyproject.ui.resources.Bundle", "Templates/Ruby/test.rb");
        createNewRubyFile(getProject(), label);
        String name = "my_utest"; //NOI18N
        NewRubyFileNameLocationStepOperator op = new NewRubyFileNameLocationStepOperator();
        op.setObjectName(name);
        new JComboBoxOperator(op, new LocationCBFinder()).selectItem(1);
        op.finish();
        EditorOperator eo = new EditorOperator(name + ".rb"); //NOI18N
        assertNotNull(eo);
        //in a custom module
        createNewRubyFile(getProject(), label);
        name = "my_mod_utest"; //NOI18N
        String modName = "Mymod"; //NOI18N
        op = new NewRubyFileNameLocationStepOperator();
        op.setObjectName(name);
        new JComboBoxOperator(op, new LocationCBFinder()).selectItem(1);
        new JTextFieldOperator(op, new ModuleTFFinder()).typeText(modName);
        op.finish();
        eo = new EditorOperator(name + ".rb"); //NOI18N
        assertNotNull(eo);
    }

    /**
     * Test create new Ruby Unit Test Suite
     */
    public void testRubyUTestSuite() {
        //Ruby Unit Test Suite
        String label = Bundle.getStringTrimmed("org.netbeans.modules.ruby.rubyproject.ui.resources.Bundle", "Templates/Ruby/suite.rb");
        createNewRubyFile(getProject(), label);
        String name = "my_utest_suite"; //NOI18N
        NewRubyFileNameLocationStepOperator op = new NewRubyFileNameLocationStepOperator();
        op.setObjectName(name);
        new JComboBoxOperator(op, new LocationCBFinder()).selectItem(1);
        op.finish();
        EditorOperator eo = new EditorOperator(name + ".rb"); //NOI18N
        assertNotNull(eo);
        assertTrue(eo.getText().indexOf("require 'test/unit'") > -1); //NOI18N
    }

    /**
     * Test create new RSpec File
     */
    public void testRSpecfile() {
        //RSpec File
        String label = Bundle.getStringTrimmed("org.netbeans.modules.ruby.rubyproject.ui.resources.Bundle", "Templates/Ruby/rspec.rb");
        createNewRubyFile(getProject(), label);
        NewRubyFileNameLocationStepOperator op = new NewRubyFileNameLocationStepOperator();
        new JTextFieldOperator(op, new TestedClassTFFinder()).typeText("MyModRubyClass"); //NOI18N
        new JComboBoxOperator(op, new LocationCBFinder()).selectItem(2);
        op.finish();
        EditorOperator eo = new EditorOperator("my_mod_ruby_class_spec.rb"); //NOI18N
        assertNotNull(eo);
        assertTrue(eo.getText().indexOf("MyModRubyClass") > -1); //NOI18N
        createNewRubyFile(getProject(), label);
        op = new NewRubyFileNameLocationStepOperator();
        new JTextFieldOperator(op, new TestedClassTFFinder()).typeText("MyRubyClass"); //NOI18N
        op.finish();
        eo = new EditorOperator("my_ruby_class_spec.rb"); //NOI18N
        assertNotNull(eo);
        assertTrue(eo.getText().indexOf("MyRubyClass") > -1); //NOI18N
    }
}
