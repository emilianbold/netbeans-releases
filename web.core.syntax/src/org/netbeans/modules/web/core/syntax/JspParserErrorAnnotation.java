/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * JspParserErrorAnnotation.java
 *
 * Created on October 28, 2004, 4:35 PM
 */

package org.netbeans.modules.web.core.syntax;

/**
 *
 * @author Petr Pisl
 */


import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.ext.html.HTMLTokenContext;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.web.core.syntax.spi.ErrorAnnotation;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.text.Line.Set;
import org.openide.text.NbDocument;

public class JspParserErrorAnnotation extends ErrorAnnotation.LineSetAnnotation {
    
    /** Document line where the bug is reported
     */
    private Line docline;   
    /** Line and column, where the bug is reported
     *
     */
    private final int line,column;
    /** The description of the error.
     */
    private final String error;
    /** The document, where the error is. 
     */
    private NbEditorDocument document;
    /** Creates a new instance of JspParserErrorAnnotation */
    public JspParserErrorAnnotation(int line, int column, String error, NbEditorDocument document) {
        this.line = line;
        this.column = column;
        this.error = error;
        this.document = document;
    }
    
    public String getShortDescription() {
        // Localize this with NbBundle:
        return error;
    }
    
    public int getLine(){
        return line;
    }
    
    public int getColumn(){
        return column;
    }
    
    public String getError(){
        return error;
    }
    
    public String getAnnotationType() {
        return "org-netbeans-modules-web-core-syntax-JspParserErrorAnnotation"; //NOI18N
    }
    
    public void attachToLineSet(Set lines) {
        char string[];
        int start,end;
        Line.Part part;
        
        try {
            docline=lines.getCurrent(line-1);
        } catch (IndexOutOfBoundsException ex) {
            // the document has been changed and the line is deleted
            return;
        }
        
        String annTxt = docline.getText(); // text on the line
        if (annTxt == null) return; // document is already closed
        
        ExtSyntaxSupport support = (ExtSyntaxSupport)document.getSyntaxSupport();
        int offset = NbDocument.findLineOffset(document, docline.getLineNumber()) + column+1;  // offset, where the bug is reported
        TokenItem token = null;
        start = 0;  // column, where the underlining starts on the line, where the bug should be attached. default first column
        string = annTxt.toCharArray();
        end = string.length - 1; // length of the underlining
        
        // when the error is reported outside the page, underline the first line
        if (offset < 1){
            textOnLine(docline);
            return;
        }
        
        try{
            token = ((JspSyntaxSupport)support).getTokenChain(offset-1, offset);  // get token on the reported offset

            if (token == null){                 // if no token returned, unerline the whole line
                textOnLine(docline);
                return;
            }
                
            start = NbDocument.findLineColumn(document, token.getOffset());
            offset = token.getOffset();

            TokenContextPath contextPath = token.getTokenContextPath();
            // Find the start and the end of the appropriate tag or EL 
            if (contextPath.contains(JspTagTokenContext.contextPath)
                || contextPath.contains(HTMLTokenContext.contextPath)){
                // the error is in the tag or directive
                // find the start of the tag, directive
                while (token != null && !(token.getTokenID().getNumericID() == JspTagTokenContext.SYMBOL_ID
                        && token.getImage().charAt(0) == '<')) {
                    token = token.getPrevious();    
                    if (token != null){
                        start = NbDocument.findLineColumn(document, token.getOffset());
                        offset = token.getOffset();
                    }
                }
                
                // find the end of the tag or directive
                while (token != null &&  ( token.getTokenID().getNumericID() != JspTagTokenContext.SYMBOL_ID
                        || token.getImage().charAt(token.getImage().trim().length()-1) != '>')
                        && token.getTokenID().getNumericID() != JspTagTokenContext.EOL_ID){
                    token = token.getNext();
                }
            }
            else{
                // The error is in EL
                // find the start of the EL
                while (token != null && (token.getTokenID().getNumericID() != ELTokenContext.EL_DELIM_ID
                        || token.getImage().charAt(0) != '$')) {
                    token = token.getPrevious();    
                    if (token != null){
                        start = NbDocument.findLineColumn(document, token.getOffset());
                        offset = token.getOffset();
                    }
                }
                // find the end of the EL
                while (token != null &&  ( token.getTokenID().getNumericID() != ELTokenContext.EL_DELIM_ID
                        || token.getImage().charAt(token.getImage().trim().length()-1) != '}')
                        && token.getTokenID().getNumericID() != ELTokenContext.EOL_ID){
                    token = token.getNext();
                }
                        
            }
            if (token != null)
                end = token.getOffset() + token.getImage().trim().length() - offset;
            else {
                while (end >= 0 && end > start && string[end] != ' ') {
                    end--;
                }         
            }
        }
        catch (javax.swing.text.BadLocationException e){
            e.printStackTrace(System.out);
            return;
        }
        catch (java.lang.AssertionError e){
            e.printStackTrace(System.out);
            return;
        }
        part=docline.createPart(start, end);//token.getImage().length());
        attach(part);
    }
    
    private void textOnLine(Line docline){
        int start = 0;  // column, where the underlining starts on the line, where the bug should be attached. default first column
        char string[] = docline.getText().toCharArray();
        int end = string.length - 1; // length of the underlining
        Line.Part part;
        
        while (start<=end && string[start]<=' ') {
            start++;
        }
        while (start<=end && string[end]<=' ') {
            end--;
        }
        if (start<=end)
            part=docline.createPart(start,end-start+1);
        else
            part=docline.createPart(0,string.length);
        attach(part);
        return;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof JspParserErrorAnnotation) {
            JspParserErrorAnnotation ann=(JspParserErrorAnnotation)obj;
            
            if (this==obj)
                return true;
            if (line!=ann.getLine())
                return false;
            if (column!=ann.getColumn())
                return false;
            if (!error.equals(ann.getError()))
                return false;
            /*if (getState()==STATE_DETACHED || ann.getState()==STATE_DETACHED)
                return false;*/
            return true;
        }
        return false;
    }
}
