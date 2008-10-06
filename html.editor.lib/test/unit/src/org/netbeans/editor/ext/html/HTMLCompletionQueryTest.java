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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.editor.ext.html;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.html.test.TestBase;
import org.netbeans.modules.html.editor.NbReaderProvider;
import org.netbeans.spi.editor.completion.CompletionItem;


/**Html completion test
 * This class extends TestBase class which provides access to the html editor module layer
 *
 * @author Marek Fukala
 */
public class HTMLCompletionQueryTest extends TestBase {
    
    public HTMLCompletionQueryTest() throws IOException, BadLocationException {
        super("htmlsyntaxsupporttest");
        NbReaderProvider.setupReaders(); //initialize DTD providers
    }

    public void setUp() {
    }
        
    public void tearDown() {
    }
    
    //test methods -----------
    public void testIndexHtml() throws IOException, BadLocationException {
        testCompletionResults(new File(getDataDir(), "input/HTMLCompletionQueryTest/index.html"));
    }
    
    // causing OutOfMemoryError
//    public void testNetbeansFrontPageHtml() throws IOException, BadLocationException {
//        testCompletionResults(new File(getDataDir(), "input/HTMLCompletionQueryTest/truncated_netbeans_front_page.html"));
//    }
    
    //helper methods ------------
    private void testCompletionResults(File inputFile) throws IOException, BadLocationException {
        String content = Utils.readFileContentToString(inputFile);
        BaseDocument doc = createDocument();
        doc.insertString(0,content,null);
        HTMLSyntaxSupport sup = new HTMLSyntaxSupport(doc);
        HTMLCompletionQuery query = new HTMLCompletionQuery();
        
        JEditorPane component = new JEditorPane();
        component.setDocument(doc);
        for(int i = 0; i < doc.getLength(); i++) {
            List<CompletionItem> result = query.query(component, i);
            if(result == null) {
                getRef().println(i+" => NO RESULT");
            } else {
                if(result == null) {
                    getRef().println(i + " => NO RESULT");
                } else {
                    StringBuffer sb = new StringBuffer();
                    sb.append('[');
                    Iterator itr = result.iterator();
                    while(itr.hasNext()) {
                        sb.append(itr.next());
                        if(itr.hasNext()) sb.append(',');
                    }
                    sb.append(']');
                    getRef().println(sb.toString());
                }
            }
            
        }
        
        compareReferenceFiles();
    }
    
}
