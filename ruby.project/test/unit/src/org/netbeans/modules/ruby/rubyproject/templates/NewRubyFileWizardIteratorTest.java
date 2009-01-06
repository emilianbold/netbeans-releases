/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.ruby.rubyproject.templates;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.junit.Manager;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.project.ui.NewFileWizard;
import org.netbeans.modules.ruby.rubyproject.RubyProject;
import org.netbeans.modules.ruby.rubyproject.RubyProjectTestBase;
import org.netbeans.modules.ruby.rubyproject.templates.NewRubyFileWizardIterator.Type;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 * @author Tor Norbye
 */
public class NewRubyFileWizardIteratorTest extends RubyProjectTestBase {
    
    public NewRubyFileWizardIteratorTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(NewRubyFileWizardIteratorTest.class);
        return suite;
    }

    protected FileObject getProjectSourceFolder() {
        File dataFile = getDataSourceDir();
        assertNotNull(dataFile);
        FileObject data = FileUtil.toFileObject(dataFile);
        // Back up over test/unit/data
        FileObject projectFolder = data.getParent().getParent().getParent();
        
        return projectFolder;
    }
    
    protected void createTemplate(String newName, String templateName, Type type,
            Map<String,String> createProperties) throws Exception {
        //MockServices.setServices(GsfDataLoader.class);
        RubyProject p = createTestProject();

        NewRubyFileWizardIterator it = new NewRubyFileWizardIterator(type);
        NewFileWizard wiz = new NewFileWizard(p);
        it.initialize(wiz);
        FileObject libFile = p.getProjectDirectory().getFileObject("lib");
        assertNotNull(libFile);
        DataFolder lib = (DataFolder)DataObject.find(libFile);
        wiz.setTargetFolder(lib);
        wiz.setTargetName(newName);

        // Unfortunately lookup in the SFS isn't working from my tests...
        //String path = "Templates/Ruby/";
//        FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
//        FileObject templateFile = sfs.findResource(systemFile);
//        
//        if (templateFile == null) {
//            FileObject root = sfs.getRoot();
//            // XXX why doesn't layer registrations work from unit tests?
//            for (FileObject f : root.getChildren()) {
//                System.out.println("next = " + f.getNameExt());
//            }
//            templateFile = root.getFileObject(systemFile);
//        }
        // So use the source folder instead
        String path = "src/org/netbeans/modules/ruby/rubyproject/ui/resources";
        FileObject templateFile = getProjectSourceFolder().getFileObject(path + "/" + templateName);
        assertNotNull(templateFile);
        // Workaround since it's not coming from the layer
        FileObject dataFolder = FileUtil.toFileObject(getWorkDir());
        assertNotNull(dataFolder);
        FileObject copiedTemplate = dataFolder.getFileObject(templateFile.getName() + "." + templateFile.getExt());
        if (copiedTemplate == null) {
            copiedTemplate = FileUtil.copyFile(templateFile, dataFolder, templateFile.getName());
        }
        copiedTemplate.setAttribute("javax.script.ScriptEngine", "freemarker");
        assertNotNull(copiedTemplate);
        FileObject licenses = dataFolder.getParent().getFileObject("Licenses/license-default.txt");
        if (licenses == null) {
            FileObject licensesFO = FileUtil.createData(dataFolder.getParent(), "Licenses/license-default.txt");
            String license =
"<#if licenseFirst??>\n" +
"${licenseFirst}\n" +
"</#if>\n" +
"${licensePrefix}${name}.rb\n" +
"${licensePrefix}\n" +
 // Modified to remove ${date} and ${time} to make the test stable
"${licensePrefix}Created on <date and time removed to make test stable...>\n" +
"${licensePrefix}\n" +
"${licensePrefix}To change this template, choose Tools | Templates\n" +
"${licensePrefix}and open the template in the editor.\n" +
"<#if licenseLast??>\n" +
"${licenseLast}\n" +
"</#if>";
            OutputStream os = licensesFO.getOutputStream();
            Writer writer = new BufferedWriter(new OutputStreamWriter(os));
            writer.write(license);
            writer.close();
        }

        DataObject template = DataObject.find(copiedTemplate);
        assertNotNull(template);
        wiz.setTemplate(template);
        
        String expectedName = newName + "." + template.getPrimaryFile().getExt();
        FileObject existing = libFile.getFileObject(expectedName);
        if (existing != null) {
            existing.delete();
        }
        
        if (createProperties != null) {
            for (Map.Entry<String,String> entry : createProperties.entrySet()) {
                wiz.putProperty(entry.getKey(), entry.getValue());
            }
        }
        
        Set<FileObject> files = it.instantiate();
        assertTrue(files.size() == 1);
        FileObject created = files.iterator().next();
        assertEquals(created.getName(), newName);
        assertEquals(created.getExt(), template.getPrimaryFile().getExt());
        
        File golden = getGoldenFile();
        assertTrue("Golden file " + golden.getAbsolutePath() + " doesn't exist", 
                golden.exists());
        File createdF = FileUtil.toFile(created);
        File differences = new File(getWorkDir(), "template.diff");
        if (Manager.getSystemDiff().diff(createdF, golden, differences)) {
            fail("File \"" + createdF + "\" differs from \"" + golden + "\":\n" + RubyProjectTestBase.readFile(differences));
        }
    }

    public void testNewFile() throws Exception {
        Map<String,String> map = Collections.emptyMap();
        createTemplate("createdfile", "main.rb", Type.FILE, map);
    }

    public void testNewClass() throws Exception {
        Map<String,String> map = new HashMap<String,String>();
        map.put("class", "MyClass");
        map.put("module", "OutermostModule::OtherModule::InnerModule");
        map.put("extend", "ParentModule::ParentClass");
        createTemplate("createdclass", "class.rb", Type.CLASS, map);
    }

    public void testNewModule() throws Exception {
        Map<String,String> map = new HashMap<String,String>();
        map.put("module", "MyModule");
        map.put("outermodules", "OutermostModule::OtherModule::InnerModule");
        createTemplate("createdmodule", "module.rb", Type.MODULE, map);
    }

    public void testNewTest() throws Exception {
        Map<String,String> map = new HashMap<String,String>();
        map.put("class", "TestClass");
        map.put("classfile", "foo");
        map.put("module", "OutermostModule::OtherModule::InnerModule");
        map.put("extend", "Test::Unit::TestCase");
        createTemplate("createdtest", "test.rb", Type.TEST, map);
    }

    public void testNewSpec() throws Exception {
        Map<String,String> map = new HashMap<String,String>();
        map.put("classname", "FireFly");
        map.put("classfile", "fire_fly");
        map.put("file_to_require", "'fire_fly'");
        map.put("classfield", "fire_fly");
        createTemplate("createdspec", "rspec.rb", Type.SPEC, map);
    }
}
