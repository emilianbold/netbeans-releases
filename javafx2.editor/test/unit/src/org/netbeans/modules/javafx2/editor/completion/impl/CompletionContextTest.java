/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.editor.completion.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestSuite;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.junit.Manager;
import org.netbeans.modules.javafx2.editor.FXMLCompletionTestBase;
import org.netbeans.spi.editor.completion.CompletionProvider;

/**
 *
 * @author sdedic
 */
public class CompletionContextTest extends FXMLCompletionTestBase {
    
    private static final String MARKER = "\\|([A-Z_]+?),([a-zA-Z]+?)\\|"; // NOI18N
    
    private String text;
    private int offset;
    private String state;
    private CompletionContext.Type stateVal;

    public CompletionContextTest(String testName) {
        super(testName);
    }
    
    public CompletionContextTest(String testName, String text, String expectedState, int offset) {
        super(testName);
        this.state = expectedState;
        this.offset = offset;
        this.text = text;
    }
    
    private void prepareData(String tokenName) throws Exception  {
        File dataDir = getDataDir();
        File f = new File(dataDir, CompletionContextTest.class.getPackage().getName().replaceAll("\\.", "/") + "/Simple.fxml");
        InputStream stream = new FileInputStream(f);
        InputStreamReader rd = new InputStreamReader(stream, "UTF-8");
        
        StringBuffer sb = new StringBuffer();
        CharBuffer cb = CharBuffer.allocate(10000);
        
        while (rd.read(cb) != -1) {
            cb.flip();
            sb.append(cb.toString());
            cb.rewind();
        }
        rd.close();
        
        String text = sb.toString();
        
        // strip all occurrences of markers:
        String pristine = text.replaceAll(MARKER, "");
        
        Pattern p = Pattern.compile(MARKER);
        Matcher m = p.matcher(text);
        TestSuite ts = new TestSuite(CompletionContextTest.class.getName());
        
        while (m.find()) {
            String val = m.group(1);
            String name = m.group(2);
            
            if (!name.equals(tokenName)) {
                continue;
            }
            
            int offset = m.start();
            String cleanBefore = text.substring(0, offset).replaceAll(MARKER, "");
            
            this.text = pristine;
            this.state = val;
            this.stateVal = CompletionContext.Type.valueOf(val);
            this.offset = cleanBefore.length();
            return;
        }
        throw new IllegalArgumentException("Token " + tokenName + " not found in the template");
    }
    
    private void runNamedTest(String name) throws Exception {
        prepareData(name);
        Language l = Language.find("text/fxml+xml");
        TokenHierarchy th = TokenHierarchy.create(text, l);
        CompletionContext ctx = new CompletionContext(th, offset, CompletionProvider.COMPLETION_QUERY_TYPE);
        
        CompletionContext.Type type = CompletionContext.Type.valueOf(state);
        
        assertEquals(type, ctx.getType());
    }

    protected void xrunTest() throws Throwable {
        Language l = Language.find("text/fxml+xml");
        TokenHierarchy th = TokenHierarchy.create(text, l);
        CompletionContext ctx = new CompletionContext(th, offset, CompletionProvider.COMPLETION_QUERY_TYPE);
        
        CompletionContext.Type type = CompletionContext.Type.valueOf(state);
        
        assertEquals(type, ctx.getType());
    }
    
    // processing instructions
    public void testStartOfInstruction() throws Exception {
        runNamedTest("startOfInstruction");
    }

    
    public void testDataInTheMiddle() throws Exception {
        runNamedTest("dataInTheMiddle");
    }

    public void testDataSuffix() throws Exception {
        runNamedTest("dataSuffix");
    }

    public void testInstructionEnd() throws Exception {
        runNamedTest("instructionEnd");
    }

    public void testAfterInstruction() throws Exception {
        runNamedTest("afterInstruction");
    }

    public void testBeginOfTargetName() throws Exception {
        runNamedTest("beginOfTargetName");
    }

    public void testWitespaceAfterInstruction() throws Exception {
        runNamedTest("whitespaceAfterInstruction");
    }
    
    // elements

    public void testExistingClassNameStart() throws Exception {
        runNamedTest("existingClassNameStart");
    }

    public void testExistingPropertyElementStart() throws Exception {
        runNamedTest("existingPropertyElementStart");
    }
    
    public void testPropertyElementEnd() throws Exception {
        runNamedTest("propertyElementEnd");
    }
    
    public void testWhitespaceAfterTag() throws Exception {
        runNamedTest("whitespaceAfterTag");
    }
    
    public void testEndOfTagName() throws Exception {
        runNamedTest("endOfTagName");
    }
    
    public void testendOfPropertyTagName() throws Exception {
        runNamedTest("endOfPropertyTagName");
    }
    
    public void testWhittespaceAfterPropertyTagName() throws Exception {
        runNamedTest("whitespaceAfterPropertyTagName");
    }
    
    // attributes
    public void testPropertyValueEquals() throws Exception {
        runNamedTest("propertyValueEquals");
    }

    public void testExistingPropertyStart() throws Exception {
        runNamedTest("existingPropertyStart");
    }

    public void testPropertyValueStart() throws Exception {
        runNamedTest("existingClassNameStart");
    }

    public void testPropertyValueMiddle() throws Exception {
        runNamedTest("propertyValueMiddle");
    }
    
    public void testWhitespaceAfterAttribute() throws Exception {
        runNamedTest("whitespaceAfterAttribute");
    }
    
    // attribute values

    public void testVariableStart() throws Exception {
        runNamedTest("variableStart");
    }

    public void testVariableMiddle() throws Exception {
        runNamedTest("variableMiddle");
    }

    public void testBundleReference() throws Exception {
        runNamedTest("bundleReference");
    }
    
    public void testBinding() throws Exception {
        runNamedTest("binding");
    }
    
    public void testResourceReference() throws Exception {
        runNamedTest("resourceReference");
    }
}
