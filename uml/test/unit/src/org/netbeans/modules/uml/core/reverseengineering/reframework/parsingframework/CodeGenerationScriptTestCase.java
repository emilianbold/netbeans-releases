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


package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.ModuleUnitTestSuiteBuilder;
import java.io.File;
import java.io.IOException;

import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;

/**
 * Test cases for CodeGenerationScript.
 */
public class CodeGenerationScriptTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(CodeGenerationScriptTestCase.class);
    }

    private ICodeGenerationScript cgs;
    private IClassifier           c;
    private IAttribute            attr;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        cgs = new CodeGenerationScript();
        
        c = createClass("NY");
        assertNotNull(c.getNode());
        assertNotNull(c.getNode().getDocument());
        assertNotNull( attr = c.createAttribute("int", "harlem") );
        c.addAttribute(attr);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        attr.delete();
        c.delete();
        super.tearDown();
    }

    public void testExecute()
    {
        cgs.setLanguage(c.getLanguages().get(0));
        // String location = ProductHelper.getConfigManager()
        //    .getDefaultConfigLocation();
        
        String location = ModuleUnitTestSuiteBuilder.tempDotUmlDirName
            + File.separator + "config" + File.separator; // NOI18N

		if ((location != null) && (location.length() > 0))
		{
		  String addinFile = "";
		  File file = new File(location);
		  File parent = file.getParentFile();
		  if (parent != null)
		  {
                try
                {
                    addinFile = parent.getCanonicalPath() + File.separator
                            + "scripts";
                } 
                catch (IOException e)
                {
				e.printStackTrace();
			}
		  }

            String gtPath = new File(addinFile + "/java/java_attribute.gt")
                    .getAbsolutePath();
            cgs.setFile(gtPath);
		  cgs.setName("java_attribute.gt");
		}
 
        assertEquals("private int harlem;", cgs.execute(attr));
    }
}
