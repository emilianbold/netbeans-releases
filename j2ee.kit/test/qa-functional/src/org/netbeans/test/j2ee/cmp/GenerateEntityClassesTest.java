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

package org.netbeans.test.j2ee.cmp;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.dbschema.SchemaElement;
import org.netbeans.modules.dbschema.SchemaElementUtil;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.DBSchemaTableProvider;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.JavaPersistenceGenerator;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.PersistenceGenerator;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.ProgressPanel;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.SelectedTables;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableClosure;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.TableProvider;
import org.netbeans.test.j2ee.lib.J2eeProjectSupport;
import org.netbeans.test.j2ee.multiview.DDTestCase;
import org.openide.actions.SaveAllAction;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.RelatedCMPHelper;

/**
 *
 * @author jhorvath
 */
public class GenerateEntityClassesTest extends DDTestCase {
    public static File EJB_PROJECT_FILE;


    
    /** Creates a new instance of GenerateEntityClassesTest */
    public GenerateEntityClassesTest(String testName) {
        super(testName);
    }

    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.out.println("########  "+getName()+"  #######");
    }
    
    @Override
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(GenerateEntityClassesTest.class);
        conf = addServerTests(Server.GLASSFISH,conf,"testOpenProject");
        if (isRegistered(Server.GLASSFISH))
            conf = addServerTests(conf, "testGenerateBeans");
        conf = conf.enableModules(".*").clusters(".*");
        return NbModuleSuite.create(conf);
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run only selected test case
        TestRunner.run(suite());
    }
    
    /*
     * Method open project
     *
     */
    public void testOpenProject() throws Exception{
        EJB_PROJECT_FILE = new File(getDataDir(), "projects/TestGenerateEntity");
        project = (Project)J2eeProjectSupport.openProject(EJB_PROJECT_FILE);
        assertNotNull("Project is null.", project);
        Thread.sleep(1000);
       /* 
        EjbJarProject ejbJarProject = (EjbJarProject)project;
        ddFo = ejbJarProject.getAPIEjbJar().getDeploymentDescriptor();  // deployment descriptor
        assertNotNull("ejb-jar.xml FileObject is null.", ddFo);
        
        ejbJar = DDProvider.getDefault().getDDRoot(ddFo);
        
        ddObj = (EjbJarMultiViewDataObject)DataObject.find(ddFo); //MultiView Editor
        assertNotNull("MultiViewDO is null.",ddObj);
        
        EditCookie edit = (EditCookie)ddObj.getCookie(EditCookie.class);
        edit.edit();
        Thread.sleep(1000);
        Utils.waitForAWTDispatchThread();
        **/
    }
    
    public void testGenerateBeans() throws Exception{
        ProgressPanel progressPanel;
        Thread.sleep(4000);
        PersistenceGenerator generator = new JavaPersistenceGenerator();
        FileObject config=project.getProjectDirectory().getFileObject("/src/conf/");
        
        RelatedCMPHelper relatedCMPHelper=new RelatedCMPHelper(project, config, generator);
        FileObject schemaFo=project.getProjectDirectory().getFileObject("/src/conf/testSchema.dbschema");        
        SchemaElement se=SchemaElementUtil.forName(schemaFo);
        
        relatedCMPHelper.setTableSource(se, schemaFo);
        relatedCMPHelper.setPackageName("test");
        
        TableProvider tableProvider = new DBSchemaTableProvider(se, generator);    
        TableClosure tableClosure = new TableClosure(tableProvider);
        tableClosure.setClosureEnabled(false);
        tableClosure.addAllTables();
        Set selected = tableClosure.getSelectedTables();
        System.err.println("*** selected "+selected.size());

        Set available = tableClosure.getAvailableTables();
        System.err.println("*** available "+available.size());
        relatedCMPHelper.setTableClosure(tableClosure);
        
        SourceGroup sourceGroup = ProjectUtils.getSources(project).getSourceGroups("java")[0];
        relatedCMPHelper.setLocation(sourceGroup);
        SelectedTables selectedTables=new SelectedTables(generator, tableClosure, sourceGroup, "test");
        relatedCMPHelper.setSelectedTables(selectedTables);
        
        ProgressContributor contrib = AggregateProgressFactory.createProgressContributor("test");
        progressPanel = new ProgressPanel();
        contrib.start(5);
        relatedCMPHelper.buildBeans();
        generator.generateBeans(progressPanel, relatedCMPHelper, schemaFo, contrib);
        Set created = generator.createdObjects();
        System.err.println("*** created size: "+created.size());
            
        contrib.finish();
        Thread.sleep(4000);
        
        // save all created files
        SaveAllAction a = (SaveAllAction)SaveAllAction.get(SaveAllAction.class);
        a.performAction();
        
     
        org.netbeans.test.j2ee.lib.Utils utils = new org.netbeans.test.j2ee.lib.Utils(this);
        
        File dbBeansDir = new File(EJB_PROJECT_FILE, "src/java/test");
        String beanFiles[] = dbBeansDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".java");
            }
        });
        //if (beanFiles.length != 12)
        //    fail("12 generated db bean files are expected in "+dbBeansDir);
        utils.assertFiles(dbBeansDir, beanFiles, getName()+"_");
        
    }
    
    
}
