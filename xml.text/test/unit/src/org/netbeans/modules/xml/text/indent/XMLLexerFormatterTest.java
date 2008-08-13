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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.xml.text.indent;

import org.netbeans.modules.xml.text.AbstractTestCase;
import junit.framework.*;
import org.netbeans.editor.BaseDocument;

/**
 * Formatting related tests based on new formatter. See XMLLexerFormatter.
 * 
 * @author Samaresh (samaresh.panda@sun.com)
 */
public class XMLLexerFormatterTest extends AbstractTestCase {
    
    public XMLLexerFormatterTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new XMLLexerFormatterTest("testFormat"));
        suite.addTest(new XMLLexerFormatterTest("testFormatSubsection"));
        suite.addTest(new XMLLexerFormatterTest("testFormatForTab"));
       // suite.addTest(new XMLLexerFormatterTest("testFormatTime"));
        return suite;
    }
    
    /**
     * Formats an input document and then compares the formatted doc
     * with a document that represents expected outcome.
     */
    public void testFormat() throws Exception {
        BaseDocument inputDoc = getDocument("indent/input.xml");
        //format the inputDoc
        XMLLexerFormatter formatter = new XMLLexerFormatter(null);
        BaseDocument formattedDoc = formatter.doReformat(inputDoc, 0, inputDoc.getLength());
        System.out.println(formattedDoc.getText(0, formattedDoc.getLength()));
        BaseDocument outputDoc = getDocument("indent/output.xml");        
        assert(compare(formattedDoc, outputDoc));
    }
    
    public void testFormatSubsection() throws Exception {
        BaseDocument inputDoc = getDocument("indent/input_sub.xml");
        //format a subsection of the inputDoc
        XMLLexerFormatter formatter = new XMLLexerFormatter(null);
        BaseDocument formattedDoc = formatter.doReformat(inputDoc, 72, 97);
        System.out.println(formattedDoc.getText(0, formattedDoc.getLength()));
        BaseDocument outputDoc = getDocument("indent/output_sub.xml");        
        assert(compare(formattedDoc, outputDoc));
    }
    
    //for bug 139160
    public void testFormatForTab() throws Exception {
        BaseDocument inputDoc = getDocument("indent/input2.xsd");
        //format the inputDoc
        XMLLexerFormatter formatter = new XMLLexerFormatter(null);
        BaseDocument formattedDoc = formatter.doReformat(inputDoc, 0, inputDoc.getLength());
        System.out.println(formattedDoc.getText(0, formattedDoc.getLength()));
        BaseDocument outputDoc = getDocument("indent/output2.xsd");        
        assert(compare(formattedDoc, outputDoc));
    }
    
      
    public void testFormatTime() throws Exception {
        BaseDocument inputDoc = getDocument("indent/1998stats.xml");
        //format the inputDoc
        XMLLexerFormatter formatter = new XMLLexerFormatter(null);
        long t1 = System.currentTimeMillis();
        BaseDocument formattedDoc = formatter.doReformat(inputDoc, 0, inputDoc.getLength());
        long t2 = System.currentTimeMillis();
        long timeElapsed = t2-t1;
        System.out.println("Time take to format(ms):: " +timeElapsed);
        
    }
}