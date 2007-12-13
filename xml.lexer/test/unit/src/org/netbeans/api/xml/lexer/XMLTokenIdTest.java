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
package org.netbeans.api.xml.lexer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.swing.text.Document;
import junit.framework.*;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.text.syntax.XMLKit;

/**
 *
 * @author Samaresh
 */
public class XMLTokenIdTest extends TestCase {
    
    public XMLTokenIdTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws Exception {
    }

    @Override
    public void tearDown() {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new XMLTokenIdTest("testTokens"));
        return suite;
    }
    
    public void testTokens() throws Exception {
        XMLTokenId[] expectedIds = {XMLTokenId.PI_START, XMLTokenId.PI_TARGET, XMLTokenId.WS, XMLTokenId.PI_CONTENT,
            XMLTokenId.PI_END, XMLTokenId.TEXT, XMLTokenId.TAG, XMLTokenId.WS, XMLTokenId.ARGUMENT,
            XMLTokenId.OPERATOR, XMLTokenId.VALUE, XMLTokenId.TAG, XMLTokenId.TEXT, XMLTokenId.TAG, XMLTokenId.WS, XMLTokenId.ARGUMENT,
            XMLTokenId.OPERATOR, XMLTokenId.VALUE, XMLTokenId.WS, XMLTokenId.ARGUMENT, XMLTokenId.OPERATOR, XMLTokenId.VALUE,
            XMLTokenId.WS, XMLTokenId.ARGUMENT, XMLTokenId.OPERATOR, XMLTokenId.VALUE, XMLTokenId.WS, XMLTokenId.ARGUMENT,
            XMLTokenId.OPERATOR, XMLTokenId.VALUE, XMLTokenId.WS, XMLTokenId.TAG, XMLTokenId.TEXT, XMLTokenId.TAG,
            XMLTokenId.TAG, XMLTokenId.TEXT, XMLTokenId.BLOCK_COMMENT, XMLTokenId.BLOCK_COMMENT, XMLTokenId.BLOCK_COMMENT,
            XMLTokenId.TEXT, XMLTokenId.TAG, XMLTokenId.TAG, XMLTokenId.TEXT, XMLTokenId.TAG, XMLTokenId.TAG, XMLTokenId.TEXT};
        
        javax.swing.text.Document document = getDocument("resources/test.xml");
        TokenHierarchy th = TokenHierarchy.get(document);
        TokenSequence ts = th.tokenSequence();
        assert(ts.tokenCount() == expectedIds.length);
        
        ts.moveStart();
        int index = 0;
        while(ts.moveNext()) {
            Token token = ts.offsetToken();
            //System.out.println("Class: " + token.getClass());
            //System.out.println("Id: " + token.id().name());
            //System.out.println("Text: [" + token.text()+"]");
            assert(token.id() == expectedIds[index]);
            index++;
        }
    }
    
    private javax.swing.text.Document getDocument(String path) throws Exception {
        javax.swing.text.Document doc = getResourceAsDocument(path);
        //must set the language inside unit tests
        Language language = XMLTokenId.language();
        doc.putProperty(Language.class, language); 
        return doc;
    }
                 
    public static Document getResourceAsDocument(String path) throws Exception {
        InputStream in = XMLTokenIdTest.class.getResourceAsStream(path);
        Document sd = new BaseDocument(XMLKit.class, false);
        BufferedReader br = new BufferedReader(new InputStreamReader(in,"UTF-8"));
        StringBuffer sbuf = new StringBuffer();
        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                sbuf.append(line);
                sbuf.append(System.getProperty("line.separator"));
            }
        } finally {
            br.close();
        }
        sd.insertString(0,sbuf.toString(),null);
        return sd;
    }
}