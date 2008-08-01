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
package org.netbeans.modules.xml.schema.completion;

import java.util.List;
import junit.framework.*;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.schema.completion.util.CompletionContextImpl;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Samaresh
 */
public abstract class AbstractTestCase extends TestCase {
    
    protected String instanceResourcePath;
    protected FileObject instanceFileObject;
    protected BaseDocument instanceDocument;
    protected XMLSyntaxSupport support;
    
    public AbstractTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
    }
    
    protected void setupCompletion(String path, StringBuffer buffer) throws Exception {
        this.instanceResourcePath = path;
        this.instanceFileObject = Util.getResourceAsFileObject(path);
        this.instanceDocument = Util.getResourceAsDocument(path);
        this.support = ((XMLSyntaxSupport)instanceDocument.getSyntaxSupport());
        if(buffer != null) {
            instanceDocument.remove(0, instanceDocument.getLength());
            instanceDocument.insertString(0, buffer.toString(), null);
        }
        instanceDocument.putProperty(Language.class, XMLTokenId.language());        
    }
    
    /**
     * Queries and returns a list of completion items.
     * Each unit test is supposed to evaluate this result.
     */
    protected List<CompletionResultItem> query(int caretOffset) throws Exception {
        assert(instanceFileObject  != null && instanceDocument != null);
        CompletionQuery instance = new CompletionQuery(instanceFileObject);
        return instance.getCompletionItems(instanceDocument, caretOffset);
    }
    
    protected void assertResult(List<CompletionResultItem> result,
            String[] expectedResult) {
        if(result == null && expectedResult == null) {
            assert(true);
            return;
        }
        assert(result.size() == expectedResult.length);
        for(int i=0; i<expectedResult.length; i++) {
            boolean found = false;
            for(CompletionResultItem item : result) {
                String resultItem = item.getItemText();
                if(resultItem.equals(expectedResult[i])) {
                    found = true;
                    break;
                }
            }            
            assert(found);
        }
    }
    
    protected void assertResult(String[] result,
            String[] expectedResult) {
        if(result == null && expectedResult == null) {
            assert(true);
            return;
        }
        assert(result.length == expectedResult.length);
        for(int i=0; i<expectedResult.length; i++) {
            boolean found = false;
            for(String item : result) {
                if(item.equals(expectedResult[i])) {
                    found = true;
                    break;
                }
            }            
            assert(found);
        }
    }
    
    BaseDocument getDocument() {
        return instanceDocument;
    }
    
    XMLSyntaxSupport getXMLSyntaxSupport() {
        return support;
    }
    
    FileObject getFileObject() {
        return instanceFileObject;
    }
    
    CompletionContextImpl getContextAtOffset(int offset) {
        CompletionContextImpl context = new CompletionContextImpl(instanceFileObject, support, offset);
        context.initContext();
        return context;
    }
}
