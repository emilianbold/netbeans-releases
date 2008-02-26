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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.Index;
import org.netbeans.modules.gsf.api.ParseEvent;
import org.netbeans.modules.gsf.api.ParseListener;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.netbeans.napi.gsfret.source.ClasspathInfo;
import org.netbeans.napi.gsfret.source.Source;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.Language;
import org.netbeans.modules.gsf.LanguageRegistry;
import org.netbeans.modules.gsf.spi.DefaultParserFile;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author tor
 */
class TestCompilationInfo extends CompilationInfo {
    private final String text;
    private Document doc;
    private Source source;
    private int caretOffset = -1;
    private RubyTestBase test;
    
    public TestCompilationInfo(RubyTestBase test, FileObject fileObject, BaseDocument doc, String text) throws IOException {
        super(fileObject);
        this.test = test;
        this.text = text;
        assert text != null;
        this.doc = doc;
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

    public Index getIndex(String mimeType) {
        ClasspathInfo cpi = source.getClasspathInfo();
        if (cpi != null) {
            return cpi.getClassIndex(mimeType);
        }
        
        return null;
    }

    @Override
    public Document getDocument() throws IOException {
        return this.doc;
    }
    
    private Map<String,ParserResult> embeddedResults = new HashMap<String,ParserResult>();
    
    @Override
    public Collection<? extends ParserResult> getEmbeddedResults(String mimeType) {
        ParserResult result = getEmbeddedResult(mimeType, 0);
        if (result != null) {
            return Collections.singletonList(result);
        } else {
            return Collections.emptyList();
        }
    }
    
    @Override
    public ParserResult getEmbeddedResult(String embeddedMimeType, int offset) {
        assert embeddedMimeType.equals(RubyMimeResolver.RUBY_MIME_TYPE);
        
        if (embeddedResults.size() == 0) {
            final List<Error> errors = new ArrayList<Error>();
            ParseListener listener =
                new ParseListener() {

                    public void started(ParseEvent e) {
                        errors.clear();
                    }

                    public void error(Error error) {
                        errors.add(error);
                    }

                    public void exception(Exception exception) {
                        Exceptions.printStackTrace(exception);
                    }

                    public void finished(ParseEvent e) {
                    }
                };
            
            List<ParserFile> sourceFiles = new ArrayList<ParserFile>(1);
            ParserFile file = new DefaultParserFile(getFileObject(), null, false);
            sourceFiles.add(file);
            
TranslatedSource translatedSource = null; // TODO            
            RubyParser.Context context = new RubyParser.Context(file, listener, text, caretOffset, translatedSource);
            RubyParser parser = new RubyParser();
            ParserResult parserResult = ((RubyParser)parser).parseBuffer(context, RubyParser.Sanitize.NONE);
            for (Error error : errors) {
                parserResult.addError(error);
            }
            embeddedResults.put(RubyMimeResolver.RUBY_MIME_TYPE, parserResult);
            parserResult.setInfo(this);
        }
        
        return embeddedResults.get(embeddedMimeType);
    }

    @Override
    public List<Error> getErrors() {
        // Force initialization
        getEmbeddedResult(RubyMimeResolver.RUBY_MIME_TYPE, 0);

        List<Error> errors = new ArrayList<Error>();
        for (ParserResult result : embeddedResults.values()) {
            errors.addAll(result.getDiagnostics());
        }

        return errors;
    }
//    
//    @Override
//    public void setParserResult(final ParserResult parserResult) {
//        parserResult = parserResult;
//        
//        embeddedResults.put(getFileObject().getMIMEType(), parserResult);
//    }
}
