/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.project.ui.NewFileWizard;
import org.netbeans.modules.ruby.rubyproject.RubyProject;
import org.netbeans.modules.ruby.rubyproject.RubyProjectTestBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 * @autho!r Tor Norbye
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
    
    protected void createTemplate(String newName, String templateName, int type,
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
            FileObject projectXml = FileUtil.createData(dataFolder.getParent(), "Licenses/license-default.txt");
            String license =
"${licenseFirst}\n" +
"${licensePrefix}${name}.rb\n" +
"${licensePrefix}\n" +
 // Modified to remove ${date} and ${time} to make the test stable
"${licensePrefix}Created on <date and time removed to make test stable...>\n" +
"${licensePrefix}\n" +
"${licensePrefix}To change this template, choose Tools | Templates\n" +
"${licensePrefix}and open the template in the editor.\n" +
"${licenseLast}\n";
            OutputStream os = projectXml.getOutputStream();
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
        
        @SuppressWarnings("unchecked")
        Set<FileObject> files = it.instantiate();
        assertTrue(files.size() == 1);
        FileObject created = files.iterator().next();
        assertEquals(created.getName(), newName);
        assertEquals(created.getExt(), template.getPrimaryFile().getExt());
        
        File golden = getGoldenFile();
        assertTrue("Golden file " + golden.getAbsolutePath() + " doesn't exist", 
                golden.exists());
        assertFile(FileUtil.toFile(created), golden);
    }

    public void testNewFile() throws Exception {
        Map<String,String> map = Collections.emptyMap();
        createTemplate("createdfile", "main.rb", NewRubyFileWizardIterator.TYPE_FILE, map);
    }

    public void testNewClass() throws Exception {
        Map<String,String> map = new HashMap<String,String>();
        map.put("class", "MyClass");
        map.put("module", "OutermostModule::OtherModule::InnerModule");
        map.put("extend", "ParentModule::ParentClass");
        createTemplate("createdclass", "class.rb", NewRubyFileWizardIterator.TYPE_CLASS, map);
    }

    public void testNewModule() throws Exception {
        Map<String,String> map = new HashMap<String,String>();
        map.put("module", "MyModule");
        map.put("outermodules", "OutermostModule::OtherModule::InnerModule");
        createTemplate("createdmodule", "module.rb", NewRubyFileWizardIterator.TYPE_MODULE, map);
    }

    public void testNewTest() throws Exception {
        Map<String,String> map = new HashMap<String,String>();
        map.put("class", "TestClass");
        map.put("module", "OutermostModule::OtherModule::InnerModule");
        map.put("extend", "Test::Unit::TestCase");
        createTemplate("createdtest", "test.rb", NewRubyFileWizardIterator.TYPE_TEST, map);
    }
}
