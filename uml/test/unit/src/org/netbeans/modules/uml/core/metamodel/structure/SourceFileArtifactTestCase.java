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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import junit.textui.TestRunner;

import org.netbeans.modules.uml.core.eventframework.EventDispatchController;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventDispatchController;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;

/**
 *
 */
public class SourceFileArtifactTestCase extends AbstractUMLTestCase
{
    private IClass clazz;
    private ISourceFileArtifact artifact;
    private static final String SOURCE_PATH = new File("test/A/Xyz.java").getAbsolutePath();
    private static final String SOURCE_CODE = "public class Xyz {\n}\n";
    
    static
    {
        File f = new File(SOURCE_PATH);
        try
        {
            FileWriter fw = new FileWriter(f);
            PrintWriter pw = new PrintWriter(fw);
            pw.print(SOURCE_CODE);
            pw.close();
            fw.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public static boolean fireDirtyCalled = false;
    public static boolean firePreDirtyCalled = false;
    private IStructureEventDispatcher m_Dispatcher = null;
    private TestArtifactEventsListener m_artifactListener =
        new TestArtifactEventsListener();
    
    protected void setUp() throws Exception
    {
        super.setUp();
        clazz = factory.createClass(null);
        project.addOwnedElement(clazz);
        
        clazz.addSourceFile(SOURCE_PATH);
        artifact = (ISourceFileArtifact) clazz.getSourceFiles().get(0);
        
        
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
    
        /* (non-Javadoc)
         * @see junit.framework.TestCase#tearDown()
         */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        clazz.removeElement(artifact);
        m_Dispatcher.revokeArtifactSink(m_artifactListener);
    }
    
    public void testGetLanguage()
    {
        // TODO: Test once language manager is working.
    }
    
//	public void testGetName()
//	{
//		assertEquals(SOURCE_PATH, artifact.getName());
//	}
    
//	public void testGetShortName()
//	{
//		assertEquals("Xyz.java", artifact.getShortName());
//	}
    
//	public void testGetDrive()
//	{
//        String dir =
//            SOURCE_PATH.indexOf(":\\") == 1? SOURCE_PATH.substring(0, 2) : null;
//		assertEquals(dir, artifact.getDrive());
//	}
    
//	public void testGetDirectory()
//	{
//		assertEquals(new File(SOURCE_PATH).getParentFile(), new File(artifact.getDirectory()));
//	}
    
//	public void testGetFileName()
//	{
//		assertEquals(SOURCE_PATH,artifact.getFileName());
//	}
//	public void testCalculateCRC()
//	{
//		assertEquals(1868,artifact.calculateCRC());
//	}
    
//	public void testGetSourceCode()
//	{
//		assertEquals(SOURCE_CODE, artifact.getSourceCode());
//	}
    
    public void testEnsureWriteAccess()
    {
        assertTrue(artifact.ensureWriteAccess());
        assertTrue(fireDirtyCalled);
        assertTrue(firePreDirtyCalled);
    }
    
//    public void testGetBaseDirectory()
//    {
//        IPackage p = factory.createPackage(null);
//        p.setName("foo");
//        project.addOwnedElement(p);
//
//        IClass test = createClass("Q");
//        p.addOwnedElement(test);
//
//        File absFile = new File("test/A/foo", "Q.java").getAbsoluteFile();
//        absFile.getParentFile().mkdir();
//        String absPath = absFile.toString();
//        writeFile(absPath, "package foo; public class Q {}");
//
//        test.addSourceFile(absPath);
//
//        File basedir = new File(absPath).getParentFile().getParentFile();
//        // If the use-project-in-qualified-name pref is set, we'll need to
//        // step one level up, to strip the project directory as well.
//        if ("PSK_YES".equals(ProductRetriever.retrieveProduct()
//                        .getPreferenceManager()
//                        .getPreferenceValue("", "ProjectNamespace")))
//            basedir = basedir.getParentFile();
//
//        assertEquals(basedir.toString(),
//            ((ISourceFileArtifact) test.getSourceFiles().get(0))
//                .getBaseDirectory());
//    }
    
    public static void main(String[] args)
    {
        TestRunner.run(SourceFileArtifactTestCase.class);
    }
}

