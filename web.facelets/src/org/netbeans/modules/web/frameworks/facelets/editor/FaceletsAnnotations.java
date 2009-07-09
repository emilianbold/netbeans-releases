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

import javax.swing.text.BadLocationException;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.NbEditorDocument;
import org.openide.text.Line;
import org.openide.text.NbDocument;


/**
 *
 * @author Petr Pisl
 */

public class FaceletsAnnotations {
    
    public static abstract class Annotation extends FaceletsAnnotationManager.LineSetAnnotation {
        
        public void attachToLineSet(Line.Set lines) {
            
            try {
                Line.Part part = createPart(lines);
                if (part != null)
                    attach(part);
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        }
        
        protected abstract Line.Part createPart(Line.Set lines) throws BadLocationException;
        
        public String getAnnotationType() {
            return "org-netbeans-modules-web-frameworks-facelets-editor-FaceletsErrorAnnotations"; //NOI18N
        }
    }
    
    public static class Parser extends Annotation {
        
        /** Document line where the bug is reported
         */
        private Line docline;
        
        /** The document, where the error is.
         */
        final private NbEditorDocument document;
        
        final private FaceletsEditorErrors.ParseError error;
        
        /** Creates a new instance of FaceletsParserAnnotation */
        public Parser(FaceletsEditorErrors.ParseError error, NbEditorDocument document) {
            this.document = document;
            this.error = error;
        }
        
        protected Line.Part createPart(Line.Set lines) throws BadLocationException {
            try {
                docline = lines.getCurrent(error.getLine()-1);
            } catch (IndexOutOfBoundsException ex) {
                // the document has been changed and the line is deleted
                return null;
            }
            String annTxt = docline.getText(); // text on the line
            if (annTxt == null) return null;
            
            int start,end;
            Line.Part part;
            
            end = NbDocument.findLineOffset(document, docline.getLineNumber()) + error.getColumn()-1;
            start = end;
            int column = error.getColumn();
            
            end = Utilities.getFirstNonWhiteBwd(document, end);
            start = Utilities.getPreviousWord(document, end);
            
            //ExtSyntaxSupport syntaxSupport = (ExtSyntaxSupport) document.getSyntaxSupport();
//            System.out.println("word on end |" + syntaxSupport.getTokenChain(end,end+1).getImage()+"|");
//            System.out.println("word on start |" + syntaxSupport.getTokenChain(start,start+1).getImage()+"|");
//            System.out.println("end lineoffset: " + Utilities.getLineOffset(document, end));
//            System.out.println("start lineoffset: " + Utilities.getLineOffset(document, start));
            if (Utilities.getLineOffset(document, end) != Utilities.getLineOffset(document, start)){
                start = end;
                end = Utilities.getRowLastNonWhite(document, end);
            }
            docline=lines.getCurrent(Utilities.getLineOffset(document, start));
            column = start - Utilities.getRowStart(document, start);
            
            int length = end-start+1;

            part=docline.createPart(column, length);
            return part;
        }
        
        public String getShortDescription() {
            return error.getText();
        }
    }
    
    public static class Encoding extends Annotation {
        
        /** Document line where the bug is reported
         */
        private Line docline;
        
        final private FaceletsEditorErrors.EncodingError error;
        final private NbEditorDocument document;
        
        /** Creates a new instance of FaceletsEncodingAnnotation */
        public Encoding(FaceletsEditorErrors.EncodingError error, NbEditorDocument document) {
            this.error = error;
            this.document = document;
        }
        
        protected Line.Part createPart(Line.Set lines) throws BadLocationException {
            Line.Part part = null;
            try {
                docline = lines.getCurrent(0);
            } catch (IndexOutOfBoundsException ex) {
                // the document has been changed and the line is deleted
                return null;
            }
            String annTxt = docline.getText(); // text on the line
            if (annTxt == null) return null;
            
            int offset = annTxt.indexOf(error.getEncoding());
            if (offset == -1) {
                offset = document.getText().toString().indexOf(error.getEncoding());
            }
            
            if (offset > -1){
                docline=lines.getCurrent(Utilities.getLineOffset(document, offset));
                int column = offset - Utilities.getRowStart(document, offset);
                
                int length = error.getEncoding().length();
                part=docline.createPart(column, length);
            }
            return part;
        }
        
        public String getShortDescription() {
            return error.getText();
        }
    }
}
