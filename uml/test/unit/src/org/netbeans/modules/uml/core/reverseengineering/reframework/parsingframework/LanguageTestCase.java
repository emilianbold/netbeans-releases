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

import org.dom4j.Element;


/**
 * Test cases for Language.
 */
public class LanguageTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(LanguageTestCase.class);
    }

    private ILanguage l = product.getLanguageManager().getLanguage("Java");

    public void testGetCodeGenerationScripts()
    {
        assertTrue(l.getCodeGenerationScripts().size() > 0);
    }

	// String should not be considered as java data type #79093
//    public void testIsDataType()
//    {
//        assertTrue(l.isDataType("String"));
//    }

    public void testGetDataType()
    {
        assertEquals("int", l.getDataType("int").getName());
    }

    public void testGetExpansionVariables()
    {
        assertTrue(l.getExpansionVariables().size() > 0);
    }

    public void testIsFeatureSupported()
    {
        assertTrue(l.isFeatureSupported("Operation Reverse Engineering"));
    }

    public void testGetFormatDefinition()
    {
        assertEquals(
                ((Element) l.getFormatDefinition("NamedElement"))
                            .attributeValue("xmi.id"),
                ((Element) l.getFormatDefinition("Unknown")
                 .selectSingleNode("aDefinition")).attributeValue("pdref"));
    }

    public void testIsKeyword()
    {
        assertTrue(l.isKeyword("if"));
    }

//    public void testGetLibraryDefinition()
//    {
//        assertEquals(
//                new File(product.getLanguageManager().getConfigLocation(),
//                        "libraries/java13").toString(), 
//                l.getLibraryDefinition("JDK 1.3"));
//    }

    public void testGetLibraryNames()
    {
        assertEquals("JDK 1.6", l.getLibraryNames().get(0));
    }
    
    public void testIsPrimitive()
    {
        assertTrue(l.isPrimitive("int"));
    }
}
