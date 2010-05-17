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


package org.netbeans.modules.uml.core.metamodel.structure;

import java.io.File;
import java.io.IOException;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.eventframework.EventDispatchController;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventDispatchController;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 *
 */
public class ProjectTestCase extends AbstractUMLTestCase
{
    //project is available from the super class.
    private static final String FILE_NAME = new File("AAAAA").getAbsolutePath();
    // Initialize the variable with relative path
    private static final String NEW_FILE_NAME = new File("test\\News\\News.etd").getAbsolutePath();
    
    private TestProjectEventsListener m_projectListener =
        new TestProjectEventsListener();
    private IStructureEventDispatcher m_Dispatcher = null;
    
    public static boolean callingModeModified = false;
    public static boolean callingPreModeModified = false;
    
    public ProjectTestCase()
    {
        super();
    }
    
    protected void setUp()
    {
        EventDispatchRetriever ret = EventDispatchRetriever.instance();
        IEventDispatchController cont = ret.getController();
        if (cont == null)
        {
            cont = new EventDispatchController();
        }
        m_Dispatcher =  (IStructureEventDispatcher)
        cont.retrieveDispatcher(EventDispatchNameKeeper.EDT_STRUCTURE_KIND);
        if (m_Dispatcher == null)
        {
            m_Dispatcher =  new StructureEventDispatcher();
            cont.addDispatcher(EventDispatchNameKeeper.EDT_STRUCTURE_KIND,
                m_Dispatcher);
        }
        m_Dispatcher.registerForProjectEvents(m_projectListener);
        ret.setController(cont);
    }
    
    protected void tearDown()
    {
        m_Dispatcher.revokeProjectSink(m_projectListener);
    }
    
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(ProjectTestCase.class);
    }
    
    public void testSave()
    {
        project.setDirty(true);
        project.save(FILE_NAME,true);
        
        File file = new File(project.getFileName());
        assertTrue(file.exists());
        //assertEquals("A.etd",file.getName());
    }
    
    public void testGetBaseDirectory()
    {
        try
        {
            assertEquals(new File("test", "A").getAbsoluteFile(),
                new File(project.getBaseDirectory()).getCanonicalFile());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail();
        }
    }
    
	public void testSetFileName() {
		String tstr = project.getFileName();
		project.setFileName(NEW_FILE_NAME);
		assertEquals(NEW_FILE_NAME, project.getFileName());
		project.setFileName(tstr);
   }
    
    public void testGetDefaultLanguage()
    {
        String defLang = project.getDefaultLanguage();
        ILanguage lang = project.getDefaultLanguage2();
        assertEquals(defLang, lang.getName());
        assertEquals("Java", defLang);
    }
    
    public void testSetDirty()
    {
        project.setDirty(false);
        project.setChildrenDirty(false);
        assertFalse(project.isDirty());
    }
    
    public void testGetWSProject()
    {
        IWSProject wsProj = project.getWSProject();
        assertNotNull(wsProj);
        assertNotNull(project.getName());
        assertEquals(project.getName(), wsProj.getName());
    }
    
    //TODO: need to be completed
    public void testGetReferencedLibraries()
    {
        ETList<String> libs = project.getReferencedLibraries();
    }
    
    
    public void testSetMode()
    {
//		project.setMode("PSK_IMPLEMENTATION");
        project.setMode("Implementation");
        assertNotNull(project.getMode());
//		assertEquals("PSK_IMPLEMENTATION",project.getMode());
        assertEquals("Implementation",project.getMode());
        assertTrue(ProjectTestCase.callingModeModified);
        assertTrue(ProjectTestCase.callingPreModeModified);
    }
}


