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


package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.reverseengineering.reframework.FileSystemClassLocator;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass;
import org.netbeans.modules.uml.core.reverseengineering.reframework.LanguageLibrary;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IErrorEvent;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * Test cases for REClassLoader.
 */
public class REClassLoaderTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(REClassLoaderTestCase.class);
    }
    
    private IREClassLoader recl;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        recl = new REClassLoader();
        
        FileSystemClassLocator fsl = new FileSystemClassLocator();
        fsl.addBaseDirectory(".");
        
        recl.setClassLocator(fsl);
        writeFile("Xyz.java", "public class Xyz { } class Other { }");
    }
    
    public void testLoadClass()
    {
        IREClass c = recl.loadClass("Xyz");
        assertEquals("Xyz", c.getName());
    }
    
    public void testLoadClassFromFile()
    {
        IREClass c = recl.loadClassFromFile("Xyz.java", "Xyz");
        assertEquals("Xyz", c.getName());
    }
    
    public void testLoadClassesFromFile()
    {
        ETList<IREClass> c = recl.loadClassesFromFile("Xyz.java");
        assertEquals(2, c.size());
        assertEquals("Xyz", c.get(0).getName());
        assertEquals("Other", c.get(1).getName());
    }
    
    public void testGetErrorInFile()
    {
        writeFile("Xyz.java", "public class Xyz { } A class Other { }");
        recl.loadFile("Xyz.java");
        ETList<IErrorEvent> errors = recl.getErrorInFile("Xyz.java");
        assertTrue(errors.size() > 0);
    }
    
    public void testLoadFile()
    {
        writeFile("Xyz.java", "public class Xyz { } class Other { } " +
            "class Third { }");
        recl.loadFile("Xyz.java");
        assertEquals(3, recl.getLoadedClasses().size());
        ETList<IErrorEvent> errors = recl.getErrorInFile("Xyz.java");
        assertTrue(errors.size() == 0);
    }
    
    public void testAddLibrary()
    {
        LanguageLibrary ll = new LanguageLibrary();
        ll.setIndex("../config/Libraries/Java16.index");
        ll.setLookupFile("../config/Libraries/Java16.etd");
        
        recl.addLibrary(ll);
        
        IREClass c = recl.loadClass("java::lang::String");
        assertNotNull(c);
        assertEquals("String", c.getName());
    }
    
    public void testGetLoadedClasses()
    {
        // Tested by testLoadFile
    }
}
