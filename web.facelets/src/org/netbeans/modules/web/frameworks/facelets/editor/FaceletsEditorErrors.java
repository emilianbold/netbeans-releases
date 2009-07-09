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

package org.netbeans.modules.web.frameworks.facelets.editor;

import org.netbeans.modules.editor.NbEditorDocument;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */

public class FaceletsEditorErrors {
    
    public static abstract class Error {
        
        protected String text;
        protected int line;
        
        public Error(int line, String text){
            this.text = text;
            this.line = line;
        }
        
        public int getLine(){
            return line;
        }
        
        public String getText(){
            return text;
        }
        
        public abstract FaceletsAnnotations.Annotation getErrorAnotation(NbEditorDocument document);
    }
    
    
    public static class ParseError extends Error {
        
        final private int column;
        
        /** Creates a new instance of EditorError */
        public ParseError(int line, int column, String text) {
            super(line, text);
            this.column = column;
        }
        
        
        
        public int getColumn() {
            return column;
        }
        
        public String toString(){
            return "ParserError [" +getLine()+ ", " +column+ "] " + getText(); //NOI18N
        }
        
        public FaceletsAnnotations.Annotation getErrorAnotation(NbEditorDocument document){
            return new FaceletsAnnotations.Parser(this, document);
        }
    }
    
    public static class EncodingError extends Error {
        
        final private String encoding;
        
        public EncodingError (String encoding){
            super(0, encoding);
            this.encoding = encoding;
        }
        
        public FaceletsAnnotations.Annotation getErrorAnotation(NbEditorDocument document) {
            return new FaceletsAnnotations.Encoding(this, document);
        }
    
        public String getEncoding(){
            return encoding;
        }
        
        public String getText(){
            return NbBundle.getMessage(FaceletsEditorErrors.class, "MSG_UnsupportedEncoding", encoding);
        }
    }
}
