/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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



/*
 * Created on Sep 22, 2003
 *
 */
package org.netbeans.modules.uml.core;
import org.netbeans.modules.uml.core.coreapplication.CoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
/**
 * @author aztec
 *
 */
public class ApplicationTestCase extends AbstractUMLTestCase
{
    IApplication app = null;
    IWorkspace ws = null;
    /**
     *  Constructor
     */
    public ApplicationTestCase()
    {
        super();
    }
    
    protected void setUp()
    {
        ICoreProduct prod = CoreProductManager.instance().getCoreProduct();
        if (prod != null)
        {
            //app = prod.getApplication();
            app = new Application();
        }
    }
    
    public void testCreateWorkspace()
    {
        ws = app.createWorkspace("d:\\temp\\test\\SampleWS","MyWS");
        assertEquals("MyWS",ws.getName());
        assertTrue(ws.isOpen());
        
        app.closeWorkspace(ws,"d:\\temp\\test\\SampleWS",false);
        assertFalse(ws.isOpen());
    }
    
    public void testGetQueryManager()
    {
        //Getting query manager.
        assertNotNull(app.getQueryManager());
    }
    
    public void testProjectLifeCycle()
    {
        //Create Project
        String projFileName = "d:\\temp\\test\\TestProject.etd";
        String projName = "TestProject";
        IProject proj = app.createProject();
        proj.setFileName(projFileName);
        proj.setName(projName);
        proj.save(projFileName,true);
        
        //Open Project
        IProject proj1 = app.openProject(projFileName);
        assertNotNull(proj1);
        assertEquals(projName,proj1.getName());
        
        //Get Project [ 3 kinds of methods]
        IProject proj2 = app.getProjectByName("TestProject");
        assertEquals(projName,proj2.getName());
        
        proj2 = app.getProjectByFileName(projFileName);
        assertEquals(projFileName,proj2.getFileName());
        
//		IWorkspace ws1 = ProductRetriever.retrieveProduct().getCurrentWorkspace();
        IWorkspace ws1 = app.openWorkspace("d:\\temp\\test\\SampleWS");
        proj2 = app.getProjectByName(ws1,projName);
        assertEquals(projName,proj2.getName());
        
        //Destroy application. The destroy call will call "closeAllProjects()" also.
        app.destroy();
        assertEquals(0,app.getNumOpenedProjects());
    }
    
    
    public void testSample()
    {
/*		Application application = (Application)app;
                IWorkspace ws = application.createWorkspace("d:\\temp\\test\\SampleWS","MyWS");
 
                IProject proj = application.createProject();
                proj.setFileName("d:\\temp\\test\\Loveyou2.etd");
                proj.setName("Loveyou2");
                proj.save("d:\\temp\\test\\Loveyou2.etd",true);
 
                ArrayList<IProject> projs = application.m_Projects;
                for (int i=0;i<projs.size();i++)
                {
                        ETSystem.out.println("proj name "+i+" = "+projs.get(i).getFileName());
                }
//		ETSystem.out.println("Num of prjs "+projs);
                IProject proj1 = application.openProject("d:\\temp\\test\\Loveyou2.etd");
                ETSystem.out.println("Opened proj "+proj1);
//		ETSystem.out.println("num opened projects after "+application.getNumOpenedProjects());
 */	}
    
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ApplicationTestCase.class);
    }
}



