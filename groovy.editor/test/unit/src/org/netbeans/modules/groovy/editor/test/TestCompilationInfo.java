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

package org.netbeans.modules.groovy.editor.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.Error;
import org.netbeans.api.gsf.Index;
import org.netbeans.api.gsf.ParseEvent;
import org.netbeans.api.gsf.ParseListener;
import org.netbeans.api.gsf.ParserFile;
import org.netbeans.api.gsf.ParserResult;
import org.netbeans.napi.gsfret.source.ClasspathInfo;
import org.netbeans.napi.gsfret.source.Source;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.parser.GroovyParser;
import org.netbeans.spi.gsf.DefaultParserFile;
import org.openide.filesystems.FileObject;

/**
 *
 * @author tor
 * @author Martin Adamek
 */
public class TestCompilationInfo extends CompilationInfo {
    
    private final String text;
    private Document doc;
    private Source source;
    private ParserResult result;
    private int caretOffset = -1;
    private GroovyTestBase test;
    
    public TestCompilationInfo(GroovyTestBase test, FileObject fileObject, BaseDocument doc, String text) throws IOException {
        super(fileObject);
        this.test = test;
        this.text = text;
        assert text != null;
        this.doc = doc;
        setParser(new GroovyParser());
        if (fileObject != null) {
            //source = Source.forFileObject(fileObject);
            ClasspathInfo cpInfo = ClasspathInfo.create(fileObject);
            source = Source.create(cpInfo, Collections.singletonList(fileObject));
        }
    }
    
    public void setCaretOffset(int caretOffset) {
        this.caretOffset = caretOffset;
    }

    public String getText() {
        return text;
    }
    
    public Source getSource() {
        return source;
    }

    public Index getIndex() {
        ClasspathInfo cpi = source.getClasspathInfo();
        if (cpi != null) {
            return cpi.getClassIndex();
        }
        
        return null;
    }

    @Override
    public Document getDocument() throws IOException {
        return this.doc;
    }
    
    @Override
    public ParserResult getParserResult() {
        ParserResult r = super.getParserResult();
        if (r == null) {
            r = result;
        }
        if (r == null) {
            final ParserResult[] resultHolder = new ParserResult[1];

            ParseListener listener =
                new ParseListener() {
                    public void started(ParseEvent e) {
                        //ParserTaskImpl.this.listener.started(e);
                    }
                    
                    public void error(Error e) {
                        //ParserTaskImpl.this.listener.error(e);
                        TestCompilationInfo.this.addError(e);
                    }
                    
                    public void exception(Exception e) {
                        //ParserTaskImpl.this.listener.exception(e);
                    }
                    
                    public void finished(ParseEvent e) {
                        // TODO - check state
                        if (e.getKind() == ParseEvent.Kind.PARSE) {
                            resultHolder[0] = e.getResult();
                        }
                        //ParserTaskImpl.this.listener.finished(e);
                    }
                };
            
            List<ParserFile> sourceFiles = new ArrayList<ParserFile>(1);
            ParserFile file = new DefaultParserFile(getFileObject(), null, false);
            sourceFiles.add(file);
            
            GroovyParser.Context context = new GroovyParser.Context(file, listener, text, caretOffset);
            GroovyParser parser = new GroovyParser();
            setPositionManager(parser.getPositionManager());
            ParserResult pr = parser.parseBuffer(context, GroovyParser.Sanitize.NONE);
            r = result = pr;
        }
        
        return r;
    }
}
