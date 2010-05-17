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


package org.netbeans.modules.uml.core.workspacemanagement;

import java.io.File;

import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;

import junit.framework.TestCase;
/**
 *
 * @author Trey Spiva
 */
public class WorkspaceDispatcherTestCase extends TestCase
{
    private WorkspaceEventDispatcher m_Dispatcher        = new WorkspaceEventDispatcher();
    private TestWorkspaceListener    m_WorkspaceListener = new TestWorkspaceListener();
    
    /**
     * Constructor for WorkspaceDispatcherTestCase.
     * @param arg0
     */
    public WorkspaceDispatcherTestCase(String arg0)
    {
        super(arg0);
    }
    
    public void testWorkspaceCreate()
    {
        WorkspaceManager manager = new WorkspaceManager();
        manager.setEventDispatcher(m_Dispatcher);
        
        File dir = new File("c:\\temp\\workspacetest\\testworkspace");
        deleteFolder(dir);
        
        createWorkspaceTest(manager, "c:\\temp\\workspacetest\\testworkspace\\TestWorkspace.etw", "TestWorkspace", true);
        createWorkspaceTest(manager, "c:\\temp\\workspacetest\\testworkspace\\TestWorkspace.etw", "", true);
        
        dir.mkdirs();
        createWorkspaceTest(manager, "c:\\temp\\workspacetest\\testworkspace\\TestWorkspace.etw", "TestWorkspace", false);
        
    }
    
    public void testWorkspaceOpen()
    {
        WorkspaceManager manager = new WorkspaceManager();
        manager.setEventDispatcher(m_Dispatcher);
        
        try
        {
            IWorkspace space = manager.openWorkspace("c:\\temp\\workspacetest\\testworkspace\\TestWorkspace.etw");
            manager.closeWorkspace(space, "c:\\temp\\workspacetest\\testworkspace\\TestOpenWorkspace.etw", true);
            
        }
        catch (InvalidArguments e)
        {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
        catch (WorkspaceManagementException e)
        {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
    }
    
    /**
     * @param string
     * @param string2
     * @param b
     */
    protected void createWorkspaceTest(WorkspaceManager manager,
        String wsFilename,
        String wsName,
        boolean throwExpected)
    {
        
        try
        {
            manager.createWorkspace(wsFilename, wsName);
            
            if(throwExpected == true)
            {
                fail("An exception was expected to be thrown.");
            }
        }
        catch (WorkspaceManagementException e2)
        {
            if(throwExpected == false)
            {
                e2.printStackTrace();
                fail(e2.getLocalizedMessage());
            }
        }
        catch (InvalidArguments e)
        {
            if(throwExpected == false)
            {
                e.printStackTrace();
                fail(e.getLocalizedMessage());
            }
        }
        
    }
    
    /**
     * @param dir
     */
    protected void deleteFolder(File dir)
    {
        if(dir.isDirectory() == true)
        {
            File[] contents = dir.listFiles();
            for(int index = 0; index < contents.length; index++)
            {
                if(contents[index].isDirectory() == true)
                {
                    deleteFolder(dir);
                    contents[index] = null;
                }
                else
                {
                    contents[index].delete();
                    contents[index] = null;
                }
            }
            
            dir.delete();
        }
    }
    
   /*
    * @see TestCase#setUp()
    */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        m_Dispatcher.registerForWorkspaceEvents(m_WorkspaceListener);
    }
    
   /*
    * @see TestCase#tearDown()
    */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        m_Dispatcher.revokeWorkspaceSink(m_WorkspaceListener);
    }
    
}
