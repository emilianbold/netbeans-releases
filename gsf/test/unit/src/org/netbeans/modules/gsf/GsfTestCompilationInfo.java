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
package org.netbeans.modules.gsf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.Index;
import org.netbeans.modules.gsf.api.ParseEvent;
import org.netbeans.modules.gsf.api.ParseListener;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.napi.gsfret.source.ClasspathInfo;
import org.netbeans.napi.gsfret.source.Source;
import org.netbeans.editor.BaseDocument;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author tor
 */
public abstract class GsfTestCompilationInfo extends CompilationInfo {
    protected final String text;
    protected Document doc;
    protected Source source;
    protected int caretOffset = -1;
    protected Map<String,ParserResult> embeddedResults = new HashMap<String,ParserResult>();
    
    public GsfTestCompilationInfo(FileObject fileObject, BaseDocument doc, String text) throws IOException {
        super(fileObject);
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
    
    @Override
    public Collection<? extends ParserResult> getEmbeddedResults(String mimeType) {
        ParserResult result = getEmbeddedResult(mimeType, 0);
        if (result != null) {
            return Collections.singletonList(result);
        } else {
            return Collections.emptyList();
        }
    }

    protected abstract String getPreferredMimeType();
    
    @Override
    public abstract ParserResult getEmbeddedResult(String embeddedMimeType, int offset);

    @Override
    public List<Error> getErrors() {
        // Force initialization
        getEmbeddedResult(getPreferredMimeType(), 0);

        List<Error> errors = new ArrayList<Error>();
        for (ParserResult result : embeddedResults.values()) {
            errors.addAll(result.getDiagnostics());
        }

        return errors;
    }

    public static class GsfTestParseListener implements ParseListener {
        final List<Error> errors = new ArrayList<Error>();

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
        
        public List<Error> getErrors() {
            return errors;
        }
    }
    
}
