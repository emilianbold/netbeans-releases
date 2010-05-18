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
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.eventframework.EventDispatchController;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventDispatchController;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
/**
 *
 */
public class ArtifactTestCase extends AbstractUMLTestCase
{
    private IArtifact arti = null;
    private TestArtifactEventsListener m_artifactListener =
        new TestArtifactEventsListener();
    private IStructureEventDispatcher m_Dispatcher = null;
    
    public static boolean callingPreModified = false;
    public static boolean callingModified = false;
    private IDeployment depAdded = factory.createDeployment(null);
    
    public ArtifactTestCase()
    {
        super();
    }
    
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(ArtifactTestCase.class);
    }
    
    protected void setUp() throws Exception
    {
        arti = factory.createArtifact(null);
        project.addElement(arti);
        
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
        m_Dispatcher.registerForArtifactEvents(m_artifactListener);
        ret.setController(cont);
    }
    
    protected void tearDown() throws Exception
    {
        super.tearDown();
        m_Dispatcher.revokeArtifactSink(m_artifactListener);
    }
    
    public void testAddDeployment()
    {
        assertNotNull(arti);
        
        project.addElement(depAdded);
        arti.addDeployment(depAdded);
        
        ETList<IDeployment> deps = arti.getDeployments();
        assertNotNull(deps);
        
        IDeployment depGot = null;
        if (deps != null)
        {
            for (int i=0;i<deps.size();i++)
            {
                depGot = deps.get(i);
            }
        }
        assertEquals(depAdded.getXMIID(), depGot.getXMIID());
    }
    
    public void testSetContent()
    {
        assertNotNull(arti);
        IDeploymentSpecification spec = factory.createDeploymentSpecification(null);
        arti.setContent(spec);
        project.addElement(spec);
        IDeploymentSpecification spec1 = arti.getContent();
        assertEquals(spec.getXMIID(),spec1.getXMIID());
    }
    
    public void testAddImplementedElement()
    {
        //NamedElement creation
    }
    
//	public void testSetFileName()
//	{
//		String str = "test/A/NewFile.java";
//        String absPath = new File(str).getAbsolutePath();
//        writeFile(absPath, "public class NewFile{}");
//        arti.setFileName(absPath);
//		String newStr = arti.getFileName();
//		assertTrue(ArtifactTestCase.callingPreModified);
//		assertNotNull(newStr);
//	    assertEquals(new File(str).getAbsolutePath(),newStr);
//	}
    
    public void testGetBaseDir()
    {
        Artifact fac = (Artifact)arti;
        String sourceFile = new File("c", "D.java").getAbsolutePath();
        String qualifiedName = "c::D";
        String baseD = fac.getBaseDir(sourceFile,qualifiedName);
        assertEquals(new File(sourceFile).getParentFile().getParent(), baseD);
    }
    
    public void testRemoveDeployment()
    {
        arti.removeDeployment(depAdded);
        ETList<IDeployment> deps = arti.getDeployments();
        assertTrue(deps == null || deps.size() == 0);
    }
}

