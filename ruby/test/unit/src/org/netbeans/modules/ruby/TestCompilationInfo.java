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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import org.jruby.ast.Node;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.Error;
import org.netbeans.api.gsf.Index;
import org.netbeans.api.gsf.ParseEvent;
import org.netbeans.api.gsf.ParseListener;
import org.netbeans.api.gsf.ParserFile;
import org.netbeans.api.gsf.ParserResult;
import org.netbeans.api.gsf.SourceFileReader;
import org.netbeans.api.retouche.source.ClasspathInfo;
import org.netbeans.api.retouche.source.Source;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.gsf.DefaultParserFile;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 *
 * @author tor
 */
class TestCompilationInfo extends CompilationInfo {
    private final String text;
    private final Document doc;
    private Source source;
    private ParserResult result;
    
    public TestCompilationInfo(FileObject fileObject, BaseDocument doc, String text) throws IOException {
        super(fileObject);
        this.text = text;
        assert text != null;
        this.doc = doc;
        setParser(new RubyParser());
        if (fileObject != null) {
            source = Source.forFileObject(fileObject);
        }
    }

    public String getText() {
        return text;
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
            
            SourceFileReader reader =
                new SourceFileReader() {
                    public CharSequence read(ParserFile file) throws IOException {
                        return text;
                    }
                    public int getCaretOffset(ParserFile fileObject) {
                        return -1;
                    }
                };
            
            getParser().parseFiles(sourceFiles, listener, reader);
            
            r = result = resultHolder[0];
        }
        
        return r;
    }
}
