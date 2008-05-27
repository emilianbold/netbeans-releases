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
package org.netbeans.modules.css.editor.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.editor.ext.html.parser.AstNodeVisitor;
import org.netbeans.editor.ext.html.parser.SyntaxElement;
import org.netbeans.editor.ext.html.parser.SyntaxParser;
import org.netbeans.editor.ext.html.parser.SyntaxTree;

/**
 * @author Marek Fukala
 */
public class CssTest extends TestBase {

    private static final LanguagePath languagePath = LanguagePath.get(HTMLTokenId.language());

    public CssTest() throws IOException, BadLocationException {
        super("CssTest");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testDummy() throws BadLocationException, FileNotFoundException, IOException {
        
        File f = new File("/marek/propidx.html");
        
        assertTrue(f.exists());
        
        Reader r = new InputStreamReader(new FileInputStream(f));
        
        char[] buff = new char[100000];
        int read = r.read(buff);
        r.close();
        
        String source = new String(buff, 0, read);
        
        SyntaxParser parser = SyntaxParser.create(source);
        List<SyntaxElement> sels = parser.parseImmutableSource();
        AstNode node = SyntaxTree.makeTree(sels);
        
//        System.out.println(node.toString());
        
        final StringBuffer buf = new StringBuffer();
        
        final Map<String, Collection<String>> props = new HashMap<String, Collection<String>>();
        
        AstNodeVisitor v = new AstNodeVisitor() {

            public void visit(AstNode node) {
                if(node.type() == AstNode.NodeType.TAG) {
                    String name = node.name();
                    if("table".equalsIgnoreCase(name)) {
                        List<AstNode> children = node.children();
                        for(int i = 0; i < children.size(); i++) { 
                            AstNode child = children.get(i);
                            if(child.type() == AstNode.NodeType.UNMATCHED_TAG && "tr".equalsIgnoreCase(child.name())) {
                                
                                AstNode td = children.get(++i);
                                AstNode a = children.get(++i);
                                AstNode aa = a.children().get(0);
                                String t = aa.element().text();
                                
                                t = t.substring(9, t.indexOf('"', 9));
                                
                                int idx = t.indexOf('-');
                                
                                String propName = t.substring(idx + 1);
                                
                                buf.append(t + "=");
                                
                                System.out.println("Property " + propName + ": " + t);
                                
                                i++; //skip <td> for values
                                
                                Collection<String> vals = new ArrayList<String>();
                                //loop until <td>
                                AstNode val = children.get(++i);
                                while(!"td".equals(val.name())) {
                                    
                                    
                                    String v = val.element().text();
//                                    System.out.println(v);
                                    
                                    v = v.substring(9, v.indexOf('"', 9));
                                
                                    if(v.contains("value-")) {
                                        //value
                                        vals.add(v);
                                    } else {
                                        //property
                                        props.put(v, Collections.EMPTY_LIST);
                                    }
                                    
                                    
                                    int idx2 = v.indexOf('-');
                                
                                    String valName = v.substring(idx2 + 1);
                                    
                                    buf.append(v + ";");
                                    
                                    System.out.println("- " + valName + ": " + v);
                                
                                    val =  children.get(++i);
                                }
                                
                                props.put(t, vals);
                                
                                buf.deleteCharAt(buf.length() - 1);
                                buf.append('\n');
                                
                                
                            }
                        }
                        
                        
                    }
                }
            }
        };
        
        AstNodeUtils.visitChildren(node, v);
        
        StringBuffer buf2 = new StringBuffer();
        
        for(String name : props.keySet()) {
            Collection<String> values = props.get(name);
            buf2.append(name + "=");
            for(String val : values) {
                buf2.append(val + ";");
            }
            buf2.deleteCharAt(buf2.length() - 1);
            buf2.append('\n');
        }
        
        System.out.println(buf2);
    }
    
}
