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
package org.netbeans.modules.php.dbgp.annotations;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.StartActionProviderImpl;
import org.netbeans.modules.php.dbgp.SessionId;
import org.netbeans.modules.php.dbgp.UnsufficientValueException;
import org.netbeans.modules.php.dbgp.breakpoints.Utils;
import org.netbeans.modules.php.dbgp.packets.EvalCommand;
import org.netbeans.modules.php.dbgp.packets.Property;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.DataEditorSupport;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.RequestProcessor;


/**
 * @author ads
 *
 */
public class ToolTipAnnotation extends Annotation 
    implements PropertyChangeListener
{

    private static final String RIGHT_BRACKET   = " ]";     // NOI18N
    
    private static final String COMMA           = ", ";     // NOI18N
    
    private static final String LEFT_BRACKET    = "[ ";     // NOI18N

    /* (non-Javadoc)
     * @see org.openide.text.Annotation#getAnnotationType()
     */
    @Override
    public String getAnnotationType()
    {
        return null; // Currently return null annotation type
    }

    /* (non-Javadoc)
     * @see org.openide.text.Annotation#getShortDescription()
     */
    @Override
    public String getShortDescription()
    {
        final Line.Part part = (Line.Part) getAttachedAnnotatable();
        if (part != null) {
            Runnable runnable = new Runnable() {

                public void run() {
                    evaluate(part);
                }
            };
            if ( SwingUtilities.isEventDispatchThread()){
                runnable.run();
            }
            else {
                SwingUtilities.invokeLater( runnable );
            }
        }
        
        return null;
    }
    
    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange( PropertyChangeEvent event ) {
        if ( event.getSource() instanceof EvalCommand ){
            Property value = (Property)event.getNewValue();
            firePropertyChange(PROP_SHORT_DESCRIPTION, null, 
                    getStringValue( value ) );
        }
    }

    private String getStringValue( Property value ) {
        if ( value == null ){
            return null;
        }
        String result = null;
        try {
            result = value.getStringValue();
        }
        catch (UnsufficientValueException e) {
            /*
             *  Result of eval command should contain all data because we are
             *  not able to retrieve value via property_value command.
             *  So this should never happened. Otherwise this is a bug in XDebug.   
             */
            return null;
        }
        if ( result != null ){
            return (result.trim().length()==0) ? null : result;
        }
        boolean notFirst = false;
        StringBuilder builder = new StringBuilder(LEFT_BRACKET);
        for( Property property : value.getChildren() ){
            if ( notFirst ){
                builder.append(COMMA);
            }
            builder.append( getStringValue( property) );
            notFirst = true;
        }
        builder.append(RIGHT_BRACKET);
        return builder.toString();
    }

    private void evaluate( Line.Part part ){
        Line line = part.getLine();
        if (line == null) {
            return;
        }
        DataObject dataObject = DataEditorSupport.findDataObject(line);
        final FileObject fileObject = dataObject.getPrimaryFile();
        if ( !isPhpDataObject( dataObject) ){
            return;
        }
        EditorCookie editorCookie = (EditorCookie)dataObject.
            getCookie(EditorCookie.class);
        StyledDocument document = editorCookie.getDocument();
        if (document == null) {return;}
        final int offset = NbDocument.findLineOffset(document, 
                part.getLine().getLineNumber()) + part.getColumn();
        JEditorPane ep = EditorContextDispatcher.getDefault().getCurrentEditor();
        String selectedText = getSelectedText( ep, offset);
        if ( selectedText != null ){
            sendEvalCommand( selectedText );
        } else {
            final String identifier = ep != null ? getIdentifier(document, ep, offset) : null;            
            if (identifier != null && isDollarMark(identifier.charAt(0))) {
                Runnable runnable = new Runnable(){
                    public void run() {
                        //TODO: should have been changed to PropertCommand
                        sendEvalCommand(identifier);
                    }
                };
                RequestProcessor.getDefault().post(runnable);
            }
        }
        //TODO: review, replace the code depending on lexer.model - part I
    }

    private static String getIdentifier(final StyledDocument doc, final JEditorPane ep, final int offset) {
        String t = null;
        if ((ep.getSelectionStart() <= offset) && (offset <= ep.getSelectionEnd())) {
            t = ep.getSelectedText();
        }
        if (t != null) { return t; }
        int line = NbDocument.findLineNumber(doc, offset);
        int col = NbDocument.findLineColumn(doc, offset);
        Element lineElem = NbDocument.findLineRootElement(doc).getElement(line);
        try {
            if (lineElem == null) { return null; }
            int lineStartOffset = lineElem.getStartOffset();
            int lineLen = lineElem.getEndOffset() - lineStartOffset;
            if (col + 1 >= lineLen) {
                // do not evaluate when mouse hover behind the end of line (112662)
                return null;
            }
            t = doc.getText(lineStartOffset, lineLen);
            return getExpressionToEvaluate(t, col);
        } catch (BadLocationException e) {
            return null;
        }
    }

    static String getExpressionToEvaluate(String text, int col) {
        int identStart = col;
        while (identStart > 0 && (isPHPIdentifier(text.charAt(identStart - 1)) || (text.charAt(identStart - 1) == '.'))) {
            identStart--;
        }
        int identEnd = col;
        while (identEnd < text.length() && Character.isJavaIdentifierPart(text.charAt(identEnd))) {
            identEnd++;
        }
        if (identStart == identEnd) {
            return null;
        }
        return text.substring(identStart, identEnd);
    }
    
    static boolean isPHPIdentifier(char ch) {
        return isDollarMark(ch) || Character.isJavaIdentifierPart(ch);
    }

    private static boolean isDollarMark(char ch) {
        return ch == '$';//NOI18N
    }        
    
    private boolean isPhpDataObject( DataObject dataObject ) {
        return Utils.isPhpFile( dataObject.getPrimaryFile() );
    }

    //TODO: review, replace the code depending on lexer.model - part II (methods computeVariable, getExpression)
    /*private void computeVariable( FileObject fObject, int offset ){
        PhpModel model = ModelAccess.getAccess().getModel( 
                ModelAccess.getModelOrigin( fObject ));
        if ( model == null ){
            return;
        }
        SourceElement element = model.findSourceElement(offset);
        if ( element == null ){
            return;
        }
        String expression = getExpression( element );
        if ( expression != null ) {
            sendEvalCommand( expression );
        }
    }
    
    private String getExpression( SourceElement element ) {
        if ( element == null ){
            return null;
        }
        if ( element instanceof Expression ){
            return element.getText();
        }
        else {
            return getExpression( element.getParent() );
        }
    }*/

    private void sendEvalCommand( String str ){
        DebugSession session = getSession();
        if ( session == null ){
            return;
        }
        EvalCommand command = new EvalCommand( session.getTransactionId() );
        command.setData( str );
        command.addPropertyChangeListener( this );
        session.sendCommandLater(command);
    }
    
    private String getSelectedText( JEditorPane pane , int offset ){
        if ((pane != null && pane.getSelectionStart() <= offset) && 
                (offset <= pane.getSelectionEnd()))
        {
            return pane.getSelectedText();
        }
        return null;
    }
    
    private DebugSession getSession() {
        DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager()
                .getCurrentEngine();
        if (currentEngine == null) {
            return null;
        }
        SessionId id = (SessionId) currentEngine.lookupFirst(null,
                SessionId.class);
        if (id == null) {
            return null;
        }
        DebugSession session = StartActionProviderImpl.getInstance()
                .getCurrentSession(id);
        return session; 
    }

}
