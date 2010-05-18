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
import org.netbeans.modules.uml.core.reverseengineering.parsers.javaparser.REJavaParser;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
/**
 * Test cases for LanguageManager.
 */
public class LanguageManagerTestCase extends AbstractUMLTestCase
{
   
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(LanguageManagerTestCase.class);
    }

    private ILanguageManager lm = product.getLanguageManager();

    public void testGetAttributeDefaultType()
    {
        assertEquals("int", 
                lm.getAttributeDefaultType(createClass("A")).getName());
    }

    public void testRetrieveContextForFile()
    {
        writeFile(null, null);
        assertEquals("org.netbeans.modules.uml.core.roundtripframework." +
                        "requestprocessors.javarpcomponent.JavaRequestProcessor", 
                        lm.retrieveContextForFile("Xyz.java", "RoundTrip"));
    }

    public void testRetrieveContextForLanguage()
    {
        assertEquals("org.netbeans.modules.uml.core.roundtripframework." +
                "requestprocessors.javarpcomponent.JavaRequestProcessor", 
                lm.retrieveContextForLanguage("Java", "RoundTrip"));
    }

    public void testGetDefaultForLanguage()
    {
        assertEquals(
                "null",
                lm.getDefaultForLanguage("Java", 
                                         "UnknownDataType Initialization"));
    }

    public void testGetDefaultLanguage()
    {
        assertEquals("Java", lm.getDefaultLanguage(createClass("Foo")).getName());
    }

    public void testGetDefaultSourceFileExtensionForLanguage()
    {
        assertEquals("java", lm.getDefaultSourceFileExtensionForLanguage("Java"));
    }

    public void testGetFileExtensionFilters()
    {
        ILanguageFilter fil = lm.getFileExtensionFilters("Java").get(0);
        assertEquals("Source Files", fil.getName());
        assertEquals("*.java", fil.getFilter());
    }

    public void testGetFileExtensionsForLanguage()
    {
        IStrings s = lm.getFileExtensionsForLanguage("Java");
        assertEquals(1, s.size());
        assertEquals("java", s.get(0));
    }

    public void testGetLanguageForFile()
    {
        assertEquals("Java", lm.getLanguageForFile("Xyz.java").getName());
    }

    public void testGetLanguage()
    {
        assertEquals("Java", lm.getLanguage("Java").getName());
    }

    public void testGetLanguagesWithCodeGenSupport()
    {
        ETList<ILanguage> langs = lm.getLanguagesWithCodeGenSupport();
        assertEquals(1, langs.size());
    }

    public void testGetOperationDefaultType()
    {
        assertEquals("void", 
                lm.getOperationDefaultType(createClass("Yak")).getName());
    }

    public void testGetParserForFile()
    {
        assertTrue(
                lm.getParserForFile("Xyz.java", "Default") 
                    instanceof REJavaParser);
        
    }

    public void testRetrieveParserForLanguage()
    {
        assertTrue(
                lm.retrieveParserForLanguage("Java", "Default") 
                    instanceof REJavaParser);
    }

    public void testGetSupportedLanguages2()
    {
        assertEquals(2, lm.getSupportedLanguages2().size());
    }

    public void testGetSupportedLanguagesAsString()
    {
        assertEquals("Java|UML", 
                lm.getSupportedLanguagesAsString());
    }

    public void testGetSupportedLanguages()
    {
        assertEquals(2, lm.getSupportedLanguages().size());
    }
}
