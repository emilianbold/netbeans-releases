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

package org.netbeans.modules.java.navigation;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.ElementJavadoc;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.openide.filesystems.FileObject;

/**
 * This task is called every time the caret position changes in a Java editor.
 * <p>
 * The task finds the TreePath of the Tree under the caret, converts it to
 * an Element and then shows the declartion of the element in Declaration window
 * and javadoc in the Javadoc window.
 *
 * @author Sandip V. Chitale (Sandip.Chitale@Sun.Com)
 */
public class CaretListeningTask implements CancellableTask<CompilationInfo> {
    
    private CaretListeningFactory caretListeningFactory;
    private FileObject fileObject;
    private boolean canceled;
    
    private static ElementHandle<Element> lastEh;
    private static ElementHandle<Element> lastEhForNavigator;
    
    private static final Set<JavaTokenId> TOKENS_TO_SKIP = EnumSet.of(JavaTokenId.WHITESPACE, 
                                  JavaTokenId.BLOCK_COMMENT, 
                                  JavaTokenId.LINE_COMMENT, 
                                  JavaTokenId.JAVADOC_COMMENT);
    
    
    CaretListeningTask(CaretListeningFactory whichElementJavaSourceTaskFactory,FileObject fileObject) {
        this.caretListeningFactory = whichElementJavaSourceTaskFactory;
        this.fileObject = fileObject;
    }
    
    static void resetLastEH() {
        lastEh = null;
    }
    
    public void run(CompilationInfo compilationInfo) {
        // System.out.println("running " + fileObject);
        resume();
        
        boolean navigatorShouldUpdate = ClassMemberPanel.getInstance() != null; // XXX set by navigator visible
        boolean javadocShouldUpdate = JavadocTopComponent.shouldUpdate();
        boolean declarationShouldUpdate = DeclarationTopComponent.shouldUpdate();
        
        if ( isCancelled() || ( !navigatorShouldUpdate && !javadocShouldUpdate && !declarationShouldUpdate ) ) {
            return;
        }
                        
        int lastPosition = CaretListeningFactory.getLastPosition(fileObject);
        
        TokenHierarchy tokens = compilationInfo.getTokenHierarchy();
        TokenSequence ts = tokens.tokenSequence();
        boolean inJavadoc = false;
        int offset = ts.move(lastPosition);
        if (ts.moveNext() && ts.token() != null ) {
            
            Token token = ts.token();
            TokenId tid = token.id();
            if ( tid == JavaTokenId.JAVADOC_COMMENT ) {
                inJavadoc = true;                
            }
            
            if ( shouldGoBack( token.toString(), offset < 0 ? 0 : offset ) ) {
                if ( ts.movePrevious() ) {
                    token = ts.token();
                    tid = token.id();
                }
            }
            
            if ( TOKENS_TO_SKIP.contains(tid) ) {
                skipTokens(ts, TOKENS_TO_SKIP);                
            }
            lastPosition = ts.offset();
        }
                
        if (ts.token() != null && ts.token().length() > 1) {
            // it is magic for TreeUtilities.pathFor to proper tree
            ++lastPosition;
        }
                
        // Find the TreePath for the caret position
        TreePath tp =
                compilationInfo.getTreeUtilities().pathFor(lastPosition);        
        // if cancelled, return
        if (isCancelled()) {
            return;
        }
        
        // Update the navigator
        if ( navigatorShouldUpdate ) {
            updateNavigatorSelection(compilationInfo, tp); 
        }
        
        // Get Element
        Element element = compilationInfo.getTrees().getElement(tp);
                       
        // if cancelled or no element, return
        if (isCancelled() ) {
            return;
        }
    
        if ( element == null || inJavadoc ) {
            element = outerElement(compilationInfo, tp);
        }
        
        // if is canceled or no element
        if (isCancelled() || element == null) {            
            return;
        }
        
        // Don't update when element is the same
        if ( lastEh != null && lastEh.signatureEquals(element) && !inJavadoc ) {
            // System.out.println("  stoped because os same eh");
            return;
        }
        else {
            switch (element.getKind()) {
            case PACKAGE:
            case CLASS:
            case INTERFACE:
            case ENUM:
            case ANNOTATION_TYPE:
            case METHOD:
            case CONSTRUCTOR:
            case INSTANCE_INIT:
            case STATIC_INIT:
            case FIELD:
            case ENUM_CONSTANT:
                lastEh = ElementHandle.create(element);
                // Different element clear data
                setDeclaration(""); // NOI18N
                setJavadoc(null); // NOI18N
                break;
            case PARAMETER:
                element = element.getEnclosingElement(); // Take the enclosing method
                lastEh = ElementHandle.create(element);
                setDeclaration(""); // NOI18N
                setJavadoc(null); // NOI18N
                break;
            case LOCAL_VARIABLE:
                lastEh = null; // ElementHandle not supported 
                setDeclaration(Utils.format(element)); // NOI18N
                setJavadoc(null); // NOI18N
                return;
            default:
                // clear
                setDeclaration(""); // NOI18N
                setJavadoc(null); // NOI18N
                return;
            }
        }
            
        
        // Compute and set javadoc
        if ( javadocShouldUpdate ) {
            // System.out.println("Updating JD");
            computeAndSetJavadoc(compilationInfo, element);
        }
        
        if ( isCancelled() ) {
            return;
        }
        
        // Compute and set declaration
        if ( declarationShouldUpdate ) {
            // System.out.println("Updating DECL");
            computeAndSetDeclaration(compilationInfo, element);
        }
        
    }
    
    private void setDeclaration(final String declaration) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DeclarationTopComponent declarationTopComponent = DeclarationTopComponent.findInstance();
                if (declarationTopComponent != null && declarationTopComponent.isOpened()) {
                    declarationTopComponent.setDeclaration(declaration);
                }
            }
        });
    }
    
    private void setJavadoc(final ElementJavadoc javadoc) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JavadocTopComponent javadocTopComponent = JavadocTopComponent.findInstance();
                if (javadocTopComponent != null && javadocTopComponent.isOpened()) {
                    javadocTopComponent.setJavadoc(javadoc);
                }
            }
        });
    }
    
    /**
     * After this method is called the task if running should exit the run
     * method immediately.
     */
    public final synchronized void cancel() {
        canceled = true;
    }
    
    protected final synchronized boolean isCancelled() {
        return canceled;
    }
    
    protected final synchronized void resume() {
        canceled = false;
    }
    
   
    private void computeAndSetJavadoc(CompilationInfo compilationInfo, Element element) {
        
        if (isCancelled()) {
            return;
        }
        setJavadoc(ElementJavadoc.create(compilationInfo, element));
    }
    
    private void computeAndSetDeclaration(CompilationInfo compilationInfo, Element element ) {
            
        if ( element.getKind() == ElementKind.PACKAGE ) { 
            setDeclaration("package " + element.toString() + ";");
            return;
        }
            
        if ( isCancelled() ) {
            return;
        }
        
        Tree tree = compilationInfo.getTrees().getTree(element);

        if ( isCancelled()) {
            return;
        }

        if ( tree != null ) {
            String declaration = unicodeToUtf(tree.toString());
            if (element.getKind() ==  ElementKind.CONSTRUCTOR) {
                String constructorName = element.getEnclosingElement().getSimpleName().toString();
                declaration = declaration.replaceAll(Pattern.quote("<init>"), Matcher.quoteReplacement(constructorName));
            } else if (element.getKind() ==  ElementKind.METHOD) {
                if (declaration != null) {
                    ExecutableElement executableElement = (ExecutableElement) element;
                    AnnotationValue annotationValue = executableElement.getDefaultValue();
                    if (annotationValue != null) {
                        int lastSemicolon = declaration.lastIndexOf(";"); // NOI18N
                        if (lastSemicolon == -1) {
							declaration += " default " + String.valueOf(annotationValue) + ";"; // NOI18N
                        } else {
                            declaration = declaration.substring(0, lastSemicolon) +
							    " default " + String.valueOf(annotationValue) +  // NOI18N
							    declaration.substring(lastSemicolon);
                        }
                    }
                }
            } else if ( element.getKind() == ElementKind.FIELD ) {
                declaration = declaration + ";"; // NOI18N 
            }
            setDeclaration(declaration);
            return;
        }
    }
    
    private String unicodeToUtf( String s ) {
        
        char buf[] = new char[s.length()];
        s.getChars(0, s.length(), buf, 0);
        
        int j = 0;
        for( int i = 0; i < buf.length; i++ ) {
            
            if (buf[i] == '\\' ) {                
                i++;
                char ch = buf[i];
                if (ch == 'u') {
                    do {
                        i++; 
                        ch = buf[i];
                    } 
                    while (ch == 'u');
                    
                    int limit = i + 3;
                    if (limit < buf.length) {
                        int d = digit(16, buf[i]);
                        int code = d;
                        while (i < limit && d >= 0) {
                            i++; 
                            ch = buf[i];
                            d = digit(16, ch);
                            code = (code << 4) + d;
                        }
                        if (d >= 0) {
                            ch = (char)code;
                            buf[j] = ch;
                            j++;
                            //unicodeConversionBp = bp;
                            // return;
                        }
                    }
                    // lexError(bp, "illegal.unicode.esc");
                } else {
                    i--;
                    j++;
                    // buf = '\\';
                }
            }
            else {
                buf[j] = buf[i];
                j++;
            }
	}
        
        return new String( buf, 0, j);
    }
    
     private int digit(int base, char ch) {
	char c = ch;
	int result = Character.digit(c, base);
	if (result >= 0 && c > 0x7f) {
	    // lexError(pos+1, "illegal.nonascii.digit");
	    ch = "0123456789abcdef".charAt(result);
	}
	return result;
    }
    
    private void updateNavigatorSelection(CompilationInfo ci, TreePath tp) {
        
        // Try to find the declaration we are in
        Element e = outerElement(ci, tp);
                
        if ( e != null ) {
            final ElementHandle<Element> eh = ElementHandle.create(e);
            
            if ( lastEhForNavigator != null && eh.signatureEquals(lastEhForNavigator)) {
                return;
            }
            
            lastEhForNavigator = eh;
            
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    final ClassMemberPanel cmp = ClassMemberPanel.getInstance();
                    if (cmp != null) {
                        cmp.selectElement(eh);
                    }                    
                }                
            });
        }
        
    }
       
    private static Element outerElement( CompilationInfo ci, TreePath tp ) {
        
        Element e = null;
        
        while (tp != null) {
            
            switch( tp.getLeaf().getKind()) {
                case METHOD:
                case CLASS:
                case COMPILATION_UNIT:
                    e = ci.getTrees().getElement(tp);                    
                    break;
                case VARIABLE:
                    e = ci.getTrees().getElement(tp);
                    if (e != null && !e.getKind().isField()) {
                        e = null;
                    }
                    break;                
            }                        
            if ( e != null ) {
                break;
            }
            tp = tp.getParentPath();
        }
    
        return e;
    }
    
    
    private void skipTokens( TokenSequence ts, Set<JavaTokenId> typesToSkip ) {
                  
        while(ts.moveNext()) {
            if ( !typesToSkip.contains(ts.token().id()) ) {
                return;
            }
        }
        
        return;
    }
    
    private boolean shouldGoBack( String s, int offset ) {
        
        int nlBefore = 0;
        int nlAfter = 0;
        
        for( int i = 0; i < s.length(); i++ ) {
            if ( s.charAt(i) == '\n' ) { // NOI18N
                if ( i < offset ) {
                    nlBefore ++; 
                }
                else { 
                    nlAfter++; 
                }
                
                if ( nlAfter > nlBefore ) {
                    return true;
                }                
            }
        }
        
        if ( nlBefore < nlAfter ) {
            return false;
        }
        
        return offset < (s.length() - offset);
        
    }
    
    
    
}
