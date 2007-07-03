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
package org.netbeans.api.gsf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.api.gsf.Error;
import org.netbeans.api.gsf.Index;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.gsf.Parser;
import org.netbeans.api.gsf.ParserResult;
import org.netbeans.api.gsf.PositionManager;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.netbeans.api.gsf.annotations.CheckForNull;
import org.netbeans.api.gsf.annotations.NonNull;
import org.netbeans.api.gsf.annotations.Nullable;


/**
 * Assorted information about the Source.
 *
 * @todo Pass around a context object here that is managed by the client.
 *  This would let all the multiple clients of a particular compilation result
 *  share some work, such as computing the position stack for a caret offset,
 *  and so on. (Each client checks if it's initialized, and if not, perform
 *  work and store it in the context.)
 *
 * @author Petr Hrebejk, Tomas Zezula, Tor Norbye
 */
public abstract class CompilationInfo {
    //private Source.Phase phase = Source.Phase.MODIFIED;
    //private CompilationUnitTree compilationUnit;
    private ParserResult parserResult;
    private List<Error /*Diagnostic*/> errors;
    private PositionManager positions;
    private Parser parser;
    private FileObject fo;
    protected EmbeddingModel embeddingModel;

    public CompilationInfo(final FileObject fo) throws IOException {
        this.fo = fo;
        this.errors = new ArrayList<Error /*Diagnostic*/>();
    }

    /**
     * Returns the parsing result representing the parsed source file.
     */
    @CheckForNull
    public ParserResult getParserResult() {
        return this.parserResult;
    }

    /**
     * Returns the content of the file.
     *
     *
     * @return String the java source
     */
    public abstract String getText();

    
    /**
     * Returns the index associated with this file
     */
    public abstract Index getIndex();
    
    /**
     * Returns the errors in the file represented by the {@link Source}.
     *
     *
     * @return an list of {@link Error}
     */
    public List<Error /*Diagnostic*/> getDiagnostics() {
        ArrayList<Error /*Diagnostic*/> localErrors =
            new ArrayList<Error /*Diagnostic*/>(errors.size());

        for (Error /*Diagnostic*/ m : errors) {
            //            if (this.jfo == m.getSource())
            if (this.fo == m.getFile()) {
                localErrors.add(m);
            }
        }

        return localErrors;
    }
    
    public boolean hasErrors() {
        if (errors == null) {
            return false;
        }
        for (Error m : errors) {
            if (this.fo == m.getFile() && m.getSeverity() == Severity.ERROR) {
                return true;
            }
        }
        
        return false;
    }

    public FileObject getFileObject() {
        return fo;
    }

    @CheckForNull
    public PositionManager getPositionManager() {
        return positions;
    }

    public Document getDocument() throws IOException {
        if (this.fo == null) {
            return null;
        }

        DataObject od = DataObject.find(fo);
        EditorCookie ec = od.getCookie(EditorCookie.class);

        if (ec != null) {
            return ec.getDocument();
        } else {
            return null;
        }
    }

    public void setParserResult(@NonNull
    final ParserResult parserResult) {
        this.parserResult = parserResult;
    }

    public void addError(@NonNull
    Error message) {
        errors.add(message);
    }

    public void setPositionManager(PositionManager positions) {
        this.positions = positions;
    }

    public Parser getParser() {
        return parser;
    }

    public void setParser(Parser parser) {
        this.parser = parser;
    }

    public EmbeddingModel getEmbeddingModel() {
        return embeddingModel;
    }
}
