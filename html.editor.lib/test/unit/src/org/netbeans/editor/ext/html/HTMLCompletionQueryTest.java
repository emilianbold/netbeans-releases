/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.editor.ext.html;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.CompletionQuery;
import org.netbeans.editor.ext.html.HTMLSyntax;
import org.netbeans.editor.ext.html.dtd.DTD;
import org.netbeans.editor.ext.html.test.TestBase;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.html.HTMLKit;
import org.netbeans.modules.editor.html.NbReaderProvider;

import org.openide.ErrorManager;

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
    
    public void testNetbeansFrontPageHtml() throws IOException, BadLocationException {
        testCompletionResults(new File(getDataDir(), "input/HTMLCompletionQueryTest/truncated_netbeans_front_page.html"));
    }
    
    //helper methods ------------
    private void testCompletionResults(File inputFile) throws IOException, BadLocationException {
        String content = Utils.readFileContentToString(inputFile);
        BaseDocument doc = createDocument();
        doc.insertString(0,content,null);
        HTMLSyntaxSupport sup = new HTMLSyntaxSupport(doc);
        HTMLCompletionQuery query = new HTMLCompletionQuery();
        
        JEditorPane component = new JEditorPane();
        for(int i = 0; i < doc.getLength(); i++) {
            CompletionQuery.Result result = query.query(component, HTMLKit.class, doc, i, sup);
            if(result == null) {
                getRef().println(i+" => NO RESULT");
            } else {
                List data = result.getData();
                if(data == null) {
                    getRef().println(i + " => NO RESULT");
                } else {
                    StringBuffer sb = new StringBuffer();
                    sb.append('[');
                    Iterator itr = data.iterator();
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
