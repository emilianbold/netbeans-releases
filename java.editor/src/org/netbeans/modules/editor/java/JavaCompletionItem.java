/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.editor.java;

import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;

/**
 *
 * @author Dusan Balek
 */
public abstract class JavaCompletionItem implements CompletionItem {
    
    protected static int SMART_TYPE = 1000;
    private static final String GENERATE_TEXT = NbBundle.getMessage(JavaCompletionItem.class, "generate_Lbl");
    private static final Logger LOGGER = Logger.getLogger(JavaCompletionItem.class.getName());

    public static final JavaCompletionItem createKeywordItem(String kwd, String postfix, int substitutionOffset, boolean smartType) {
        return new KeywordItem(kwd, 0, postfix, substitutionOffset, smartType);
    }
    
    public static final JavaCompletionItem createPackageItem(String pkgFQN, int substitutionOffset, boolean inPackageStatement) {
        return new PackageItem(pkgFQN, substitutionOffset, inPackageStatement);
    }

    public static final JavaCompletionItem createTypeItem(TypeElement elem, DeclaredType type, int substitutionOffset, boolean displayPkgName, boolean isDeprecated, boolean insideNew, boolean addTypeVars, boolean smartType) {
        switch (elem.getKind()) {
            case CLASS:
                return new ClassItem(elem, type, 0, substitutionOffset, displayPkgName, isDeprecated, insideNew, addTypeVars, smartType);
            case INTERFACE:
                return new InterfaceItem(elem, type, 0, substitutionOffset, displayPkgName, isDeprecated, insideNew, addTypeVars, smartType);
            case ENUM:
                return new EnumItem(elem, type, 0, substitutionOffset, displayPkgName, isDeprecated, insideNew, smartType);
            case ANNOTATION_TYPE:
                return new AnnotationTypeItem(elem, type, 0, substitutionOffset, displayPkgName, isDeprecated, insideNew, smartType);
            default:
                throw new IllegalArgumentException("kind=" + elem.getKind());
        }
    }
    
    public static final JavaCompletionItem createArrayItem(ArrayType type, int substitutionOffset, Elements elements) {
        int dim = 0;
        TypeMirror tm = type;
        while(tm.getKind() == TypeKind.ARRAY) {
            tm = ((ArrayType)tm).getComponentType();
            dim++;
        }
        if (tm.getKind().isPrimitive())
            return new KeywordItem(tm.toString(), dim, null, substitutionOffset, true);
        if (tm.getKind() == TypeKind.DECLARED || tm.getKind() == TypeKind.ERROR) {
            DeclaredType dt = (DeclaredType)tm;
            TypeElement elem = (TypeElement)dt.asElement();
            switch (elem.getKind()) {
                case CLASS:
                    return new ClassItem(elem, dt, dim, substitutionOffset, true, elements.isDeprecated(elem), false, false, true);
                case INTERFACE:
                    return new InterfaceItem(elem, dt, dim, substitutionOffset, true, elements.isDeprecated(elem), false, false, true);
                case ENUM:
                    return new EnumItem(elem, dt, dim, substitutionOffset, true, elements.isDeprecated(elem), false, true);
                case ANNOTATION_TYPE:
                    return new AnnotationTypeItem(elem, dt, dim, substitutionOffset, true, elements.isDeprecated(elem), false, true);
            }
        }
        throw new IllegalArgumentException("array element kind=" + tm.getKind());
    }
    
    public static final JavaCompletionItem createTypeParameterItem(TypeParameterElement elem, int substitutionOffset) {
        return new TypeParameterItem(elem, substitutionOffset);
    }

    public static final JavaCompletionItem createVariableItem(VariableElement elem, TypeMirror type, int substitutionOffset, boolean isInherited, boolean isDeprecated, boolean smartType) {
        switch (elem.getKind()) {
            case LOCAL_VARIABLE:
            case PARAMETER:
            case EXCEPTION_PARAMETER:
                return new VariableItem(type, elem.getSimpleName().toString(), substitutionOffset, smartType);
            case ENUM_CONSTANT:
            case FIELD:
                return new FieldItem(elem, type, substitutionOffset, isInherited, isDeprecated, smartType);
            default:
                throw new IllegalArgumentException("kind=" + elem.getKind());
        }
    }
    
    public static final JavaCompletionItem createVariableItem(String varName, int substitutionOffset, boolean smartType) {
        return new VariableItem(null, varName, substitutionOffset, smartType);
    }

    public static final JavaCompletionItem createExecutableItem(ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isInherited, boolean isDeprecated, boolean inImport, boolean smartType) {
        switch (elem.getKind()) {
            case METHOD:
                return new MethodItem(elem, type, substitutionOffset, isInherited, isDeprecated, inImport, smartType);
            case CONSTRUCTOR:
                return new ConstructorItem(elem, type, substitutionOffset, isDeprecated, smartType, null);
            default:
                throw new IllegalArgumentException("kind=" + elem.getKind());
        }
    }
    
    public static final JavaCompletionItem createThisOrSuperConstructorItem(ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, String name) {
        if (elem.getKind() == ElementKind.CONSTRUCTOR)
            return new ConstructorItem(elem, type, substitutionOffset, isDeprecated, false, name);
        throw new IllegalArgumentException("kind=" + elem.getKind());
    }

    public static final JavaCompletionItem createOverrideMethodItem(ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean implement) {
        switch (elem.getKind()) {
            case METHOD:
                return new OverrideMethodItem(elem, type, substitutionOffset, implement);
            default:
                throw new IllegalArgumentException("kind=" + elem.getKind());
        }
    }

    public static final JavaCompletionItem createGetterSetterMethodItem(VariableElement elem, TypeMirror type, int substitutionOffset, boolean setter) {
        switch (elem.getKind()) {
            case ENUM_CONSTANT:
            case FIELD:
                return new GetterSetterMethodItem(elem, type, substitutionOffset, setter);
            default:
                throw new IllegalArgumentException("kind=" + elem.getKind());
        }
    }

    public static final JavaCompletionItem createDefaultConstructorItem(TypeElement elem, int substitutionOffset, boolean smartType) {
        return new DefaultConstructorItem(elem, substitutionOffset, smartType);
    }
    
    public static final JavaCompletionItem createParametersItem(ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, int activeParamIndex, String name) {
        return new ParametersItem(elem, type, substitutionOffset, isDeprecated, activeParamIndex, name);
    }

    public static final JavaCompletionItem createAnnotationItem(TypeElement elem, DeclaredType type, int substitutionOffset, boolean isDeprecated) {
        return new AnnotationItem(elem, type, substitutionOffset, isDeprecated, true);
    }
    
    public static final JavaCompletionItem createAttributeItem(ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated) {
        return new AttributeItem(elem, type, substitutionOffset, isDeprecated);
    }
    
    public static final JavaCompletionItem createStaticMemberItem(DeclaredType type, Element memberElem, TypeMirror memberType, int substitutionOffset, boolean isDeprecated) {
        switch (memberElem.getKind()) {
            case METHOD:
            case ENUM_CONSTANT:
            case FIELD:
                return new StaticMemberItem(type, memberElem, memberType, substitutionOffset, isDeprecated);
            default:
                throw new IllegalArgumentException("kind=" + memberElem.getKind());
        }
    }

    public static final JavaCompletionItem createInitializeAllConstructorItem(Iterable<? extends VariableElement> fields, TypeElement parent, int substitutionOffset) {
        return new InitializeAllConstructorItem(fields, parent, substitutionOffset);
    }

    public static final String COLOR_END = "</font>"; //NOI18N
    public static final String STRIKE = "<s>"; //NOI18N
    public static final String STRIKE_END = "</s>"; //NOI18N
    public static final String BOLD = "<b>"; //NOI18N
    public static final String BOLD_END = "</b>"; //NOI18N

    protected int substitutionOffset;
    
    protected JavaCompletionItem(int substitutionOffset) {
        this.substitutionOffset = substitutionOffset;
    }
    
    public void defaultAction(JTextComponent component) {
        if (component != null) {
            Completion.get().hideDocumentation();
            Completion.get().hideCompletion();
            int caretOffset = component.getSelectionEnd();
            substituteText(component, substitutionOffset, caretOffset - substitutionOffset, null);
        }
    }

    public void processKeyEvent(KeyEvent evt) {
        if (evt.getID() == KeyEvent.KEY_TYPED) {
            if (Utilities.getJavaCompletionSelectors().indexOf(evt.getKeyChar()) >= 0) {
                if (evt.getKeyChar() == '(' && !(this instanceof AnnotationItem)
                        && !(this instanceof ConstructorItem)
                        && !(this instanceof DefaultConstructorItem)
                        && !(this instanceof MethodItem)
                        && !(this instanceof GetterSetterMethodItem)
                        && !(this instanceof InitializeAllConstructorItem)
                        && !(this instanceof OverrideMethodItem)
                        && !(this instanceof StaticMemberItem)) {
                    return;
                }
                Completion.get().hideDocumentation();
                if (Utilities.getJavaCompletionAutoPopupTriggers().indexOf(evt.getKeyChar()) < 0)
                    Completion.get().hideCompletion();
                JTextComponent component = (JTextComponent)evt.getSource();
                int caretOffset = component.getSelectionEnd();
                substituteText(component, substitutionOffset, caretOffset - substitutionOffset, Character.toString(evt.getKeyChar()));
                evt.consume();
            }
        }
    }

    public boolean instantSubstitution(JTextComponent component) {
        if (component != null) {
            try {
                int caretOffset = component.getSelectionEnd();
                if (caretOffset > substitutionOffset) {
                    String text = component.getDocument().getText(substitutionOffset, caretOffset - substitutionOffset);
                    if (!getInsertPrefix().toString().startsWith(text)) {
                        return false;
                    }
                }
            }
            catch (BadLocationException ble) {}
        }
        defaultAction(component);
        return true;
    }
    
    public CompletionTask createDocumentationTask() {
        return null;
    }
    
    public CompletionTask createToolTipTask() {
        return null;
    }
    
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(getLeftHtmlText(), getRightHtmlText(), g, defaultFont);
    }
    
    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(getIcon(), getLeftHtmlText(), getRightHtmlText(), g, defaultFont, defaultColor, width, height, selected);
    }

    protected ImageIcon getIcon() {
        return null;
    }
    
    protected String getLeftHtmlText() {
        return null;
    }
    
    protected String getRightHtmlText() {
        return null;
    }

    protected void substituteText (final JTextComponent c, final int offset, int len, String toAdd) {
        final BaseDocument doc = (BaseDocument)c.getDocument();
        CharSequence prefix = getInsertPrefix();
        if (prefix == null)
            return;
        final StringBuilder text = new StringBuilder(prefix);
        final int semiPos = toAdd != null && toAdd.endsWith(";") ? findPositionForSemicolon(c) : -2; //NOI18N
        if (semiPos > -2)
            toAdd = toAdd.length() > 1 ? toAdd.substring(0, toAdd.length() - 1) : null;
        if (toAdd != null && !toAdd.equals("\n")) {//NOI18N
            char ch;
            int i = 0;
            while(i < toAdd.length() && (ch = toAdd.charAt(i)) <= ' ' ) {
                text.append(ch);
                i++;
            }
            if (i > 0)
                toAdd = toAdd.substring(i);
            TokenSequence<JavaTokenId> sequence = SourceUtils.getJavaTokenSequence(TokenHierarchy.get(doc), offset + len);
            if (sequence == null || !sequence.moveNext() && !sequence.movePrevious()) {
                text.append(toAdd);
                toAdd = null;
            }
            boolean added = false;
            while(toAdd != null && toAdd.length() > 0) {
                String tokenText = sequence.token().text().toString();
                if (tokenText.startsWith(toAdd)) {
                    len = sequence.offset() - offset + toAdd.length();
                    text.append(toAdd);
                    toAdd = null;
                } else if (toAdd.startsWith(tokenText)) {
                    sequence.moveNext();
                    len = sequence.offset() - offset;
                    text.append(toAdd.substring(0, tokenText.length()));
                    toAdd = toAdd.substring(tokenText.length());
                    added = true;
                } else if (sequence.token().id() == JavaTokenId.WHITESPACE && sequence.token().text().toString().indexOf('\n') < 0) {//NOI18N
                    if (!sequence.moveNext()) {
                        text.append(toAdd);
                        toAdd = null;
                    }
                } else {
                    if (!added)
                        text.append(toAdd);
                    toAdd = null;
                }
            }
        }
        // Update the text
        final int length = len;
        doc.runAtomic (new Runnable () {
            public void run () {
                try {
                    String textToReplace = doc.getText(offset, length);
                    Position position = doc.createPosition(offset);
                    if (textToReplace.contentEquals(text)) {
                        if (semiPos > -1)
                            doc.insertString(semiPos, ";", null); //NOI18N
                        else
                            c.setCaretPosition(offset + length);
                        return;
                    }
                    Position semiPosition = semiPos > -1 ? doc.createPosition(semiPos) : null;
                    doc.remove(position.getOffset(), length);
                    doc.insertString(position.getOffset(), text.toString(), null);
                    if (semiPosition != null)
                        doc.insertString(semiPosition.getOffset(), ";", null);
                } catch (BadLocationException e) {
                    // Can't update
                }
            }
        });
    }
            
    static class KeywordItem extends JavaCompletionItem {
        
        private static final String JAVA_KEYWORD = "org/netbeans/modules/java/editor/resources/javakw_16.png"; //NOI18N
        private static final String KEYWORD_COLOR = "<font color=#000099>"; //NOI18N
        private static ImageIcon icon;
        
        private String kwd;
        private int dim;
        private String postfix;
        private boolean smartType;
        private String leftText;

        private KeywordItem(String kwd, int dim, String postfix, int substitutionOffset, boolean smartType) {
            super(substitutionOffset);
            this.kwd = kwd;
            this.dim = dim;
            this.postfix = postfix;
            this.smartType = smartType;
        }
        
        public int getSortPriority() {
            return smartType ? 600 - SMART_TYPE : 600;
        }
        
        public CharSequence getSortText() {
            return kwd;
        }
        
        public CharSequence getInsertPrefix() {
            return kwd;
        }
        
        protected ImageIcon getIcon(){
            if (icon == null) icon = ImageUtilities.loadImageIcon(JAVA_KEYWORD, false);
            return icon;            
        }
        
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(KEYWORD_COLOR);
                sb.append(BOLD);
                sb.append(kwd);
                for(int i = 0; i < dim; i++)
                    sb.append("[]"); //NOI18N
                sb.append(BOLD_END);
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
        }
        
        protected void substituteText (final JTextComponent c, final int offset, int len, String toAdd) {
            if (dim == 0) {
                super.substituteText(c, offset, len, toAdd != null ? toAdd : postfix);
                return;
            }
            final BaseDocument doc = (BaseDocument)c.getDocument();
            final StringBuilder text = new StringBuilder();
            final int semiPos = toAdd != null && toAdd.endsWith(";") ? findPositionForSemicolon(c) : -2; //NOI18N
            if (semiPos > -2)
                toAdd = toAdd.length() > 1 ? toAdd.substring(0, toAdd.length() - 1) : null;
            if (toAdd != null && !toAdd.equals("\n")) {//NOI18N
                TokenSequence<JavaTokenId> sequence = SourceUtils.getJavaTokenSequence(TokenHierarchy.get(doc), offset + len);
                if (sequence == null || !sequence.moveNext() && !sequence.movePrevious()) {
                    text.append(toAdd);
                    toAdd = null;
                }
                boolean added = false;
                while(toAdd != null && toAdd.length() > 0) {
                    String tokenText = sequence.token().text().toString();
                    if (tokenText.startsWith(toAdd)) {
                        len = sequence.offset() - offset + toAdd.length();
                        text.append(toAdd);
                        toAdd = null;
                    } else if (toAdd.startsWith(tokenText)) {
                        sequence.moveNext();
                        len = sequence.offset() - offset;
                        text.append(toAdd.substring(0, tokenText.length()));
                        toAdd = toAdd.substring(tokenText.length());
                        added = true;
                    } else if (sequence.token().id() == JavaTokenId.WHITESPACE && sequence.token().text().toString().indexOf('\n') < 0) {//NOI18N
                        if (!sequence.moveNext()) {
                            text.append(toAdd);
                            toAdd = null;
                        }
                    } else {
                        if (!added)
                            text.append(toAdd);
                        toAdd = null;
                    }
                }
            }
            StringBuilder sb = new StringBuilder();
            int cnt = 1;
            sb.append(kwd);
            for(int i = 0; i < dim; i++) {
                sb.append("[${PAR"); //NOI18N
                sb.append(cnt++);
                sb.append(" instanceof=\"int\" default=\"\"}]"); //NOI18N                
            }
            final int length = len;
            doc.runAtomic (new Runnable () {
                public void run () {
                    try {
                        Position semiPosition = semiPos > -1 ? doc.createPosition(semiPos) : null;
                        if (length > 0)
                            doc.remove(offset, length);
                        if (semiPosition != null)
                            doc.insertString(semiPosition.getOffset(), ";", null); //NOI18N
                    } catch (BadLocationException e) {
                        // Can't update
                    }
                }
            });
            CodeTemplateManager ctm = CodeTemplateManager.get(doc);
            if (ctm != null) {
                ctm.createTemporary(sb.append(text).toString()).insert(c);
            }
        }
    
        public String toString() {
            return kwd;
        }        
    }
    
    static class PackageItem extends JavaCompletionItem {
        
        private static final String PACKAGE = "org/netbeans/modules/java/editor/resources/package.gif"; // NOI18N
        private static final String PACKAGE_COLOR = "<font color=#005600>"; //NOI18N
        private static ImageIcon icon;
        
        private boolean inPackageStatement;
        private String simpleName;
        private String sortText;
        private String leftText;

        private PackageItem(String pkgFQN, int substitutionOffset, boolean inPackageStatement) {
            super(substitutionOffset);
            this.inPackageStatement = inPackageStatement;
            int idx = pkgFQN.lastIndexOf('.');
            this.simpleName = idx < 0 ? pkgFQN : pkgFQN.substring(idx + 1);
            this.sortText = this.simpleName + "#" + pkgFQN; //NOI18N
        }
        
        public int getSortPriority() {
            return 900;
        }
        
        public CharSequence getSortText() {
            return sortText;
        }
        
        public CharSequence getInsertPrefix() {
            return simpleName;
        }
        
        protected ImageIcon getIcon(){
            if (icon == null) icon = ImageUtilities.loadImageIcon(PACKAGE, false);
            return icon;            
        }
        
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(PACKAGE_COLOR);
                sb.append(simpleName);
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
        }

        public void defaultAction(JTextComponent component) {
            if (component != null) {
                Completion.get().hideDocumentation();
                if (inPackageStatement || Utilities.getJavaCompletionAutoPopupTriggers().indexOf('.') < 0)
                    Completion.get().hideCompletion();
                int caretOffset = component.getSelectionEnd();
                substituteText(component, substitutionOffset, caretOffset - substitutionOffset, null);
            }
        }

        protected void substituteText(JTextComponent c, int offset, int len, String toAdd) {
            super.substituteText(c, offset, len, inPackageStatement || toAdd != null ? toAdd : "."); //NOI18N
        }
        
        public String toString() {
            return simpleName;
        }        
    }

    static class ClassItem extends JavaCompletionItem {
        
        private static final String CLASS = "org/netbeans/modules/editor/resources/completion/class_16.png"; //NOI18N
        private static final String CLASS_COLOR = "<font color=#560000>"; //NOI18N
        private static final String PKG_COLOR = "<font color=#808080>"; //NOI18N
        private static ImageIcon icon;
        
        protected TypeMirrorHandle<DeclaredType> typeHandle;
        private int dim;
        private boolean isDeprecated;
        private boolean insideNew;
        private boolean addTypeVars;
        private boolean smartType;
        private String simpleName;
        private String typeName;
        private String enclName;
        private CharSequence sortText;
        private String leftText;
        
        private ClassItem(TypeElement elem, DeclaredType type, int dim, int substitutionOffset, boolean displayPkgName, boolean isDeprecated, boolean insideNew, boolean addTypeVars, boolean smartType) {
            super(substitutionOffset);
            this.typeHandle = TypeMirrorHandle.create(type);
            this.dim = dim;
            this.isDeprecated = isDeprecated;
            this.insideNew = insideNew;
            this.addTypeVars = addTypeVars;
            this.smartType = smartType;
            this.simpleName = elem.getSimpleName().toString();
            this.typeName = Utilities.getTypeName(type, false).toString();
            if (displayPkgName) {
                this.enclName = Utilities.getElementName(elem.getEnclosingElement(), true).toString();
                this.sortText = this.simpleName + Utilities.getImportanceLevel(this.enclName) + "#" + this.enclName; //NOI18N

            } else {
                this.enclName = null;
                this.sortText = this.simpleName;
            }
        }
        
        public int getSortPriority() {
            return smartType ? 800 - SMART_TYPE : 800;
        }
        
        public CharSequence getSortText() {
            return sortText;
        }
        
        public CharSequence getInsertPrefix() {
            return simpleName;
        }

        public boolean instantSubstitution(JTextComponent component) {
            return false;
        }
    
        public CompletionTask createDocumentationTask() {
            return typeHandle.getKind() == TypeKind.DECLARED ? JavaCompletionProvider.createDocTask(ElementHandle.from(typeHandle)) : null;
        }

        protected ImageIcon getIcon(){
            if (icon == null) icon = ImageUtilities.loadImageIcon(CLASS, false);
            return icon;            
        }

        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(getColor());
                if (isDeprecated)
                    sb.append(STRIKE);
                sb.append(escape(typeName));
                for(int i = 0; i < dim; i++)
                    sb.append("[]"); //NOI18N
                if (isDeprecated)
                    sb.append(STRIKE_END);
                if (enclName != null && enclName.length() > 0) {
                    sb.append(COLOR_END);
                    sb.append(PKG_COLOR);
                    sb.append(" ("); //NOI18N
                    sb.append(enclName);
                    sb.append(")"); //NOI18N
                }
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
        }
        
        protected String getColor() {
            return CLASS_COLOR;
        }

        protected void substituteText(final JTextComponent c, final int offset, int len, String toAdd) {
            final BaseDocument doc = (BaseDocument)c.getDocument();
            final StringBuilder text = new StringBuilder();
            final int semiPos = toAdd != null && toAdd.endsWith(";") ? findPositionForSemicolon(c) : -2; //NOI18N
            if (semiPos > -2)
                toAdd = toAdd.length() > 1 ? toAdd.substring(0, toAdd.length() - 1) : null;
            if (toAdd != null && !toAdd.equals("\n")) {//NOI18N
                TokenSequence<JavaTokenId> sequence = SourceUtils.getJavaTokenSequence(TokenHierarchy.get(doc), offset + len);
                if (sequence == null || !sequence.moveNext() && !sequence.movePrevious()) {
                    text.append(toAdd);
                    toAdd = null;
                }
                boolean added = false;
                while(toAdd != null && toAdd.length() > 0) {
                    String tokenText = sequence.token().text().toString();
                    if (tokenText.startsWith(toAdd)) {
                        len = sequence.offset() - offset + toAdd.length();
                        text.append(toAdd);
                        toAdd = null;
                    } else if (toAdd.startsWith(tokenText)) {
                        sequence.moveNext();
                        len = sequence.offset() - offset;
                        text.append(toAdd.substring(0, tokenText.length()));
                        toAdd = toAdd.substring(tokenText.length());
                        added = true;
                    } else if (sequence.token().id() == JavaTokenId.WHITESPACE && sequence.token().text().toString().indexOf('\n') < 0) {//NOI18N
                        if (!sequence.moveNext()) {
                            text.append(toAdd);
                            toAdd = null;
                        }
                    } else {
                        if (!added)
                            text.append(toAdd);
                        toAdd = null;
                    }
                }
            }
            final int finalLen = len;
            Source s = Source.create(doc);
            try {
                ParserManager.parse(Collections.singletonList(s), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        final CompilationController controller = CompilationController.get(resultIterator.getParserResult(offset));
                        controller.toPhase(Phase.RESOLVED);
                        final int embeddedOffset = controller.getSnapshot().getEmbeddedOffset(offset);
                        DeclaredType type = typeHandle.resolve(controller);
                        final TypeElement elem = type != null ? (TypeElement)type.asElement() : null;
                        boolean asTemplate = false;
                        StringBuilder sb = new StringBuilder();
                        if (elem != null) {
                            int cnt = 1;
                            sb.append("${PAR"); //NOI18N
                            sb.append(cnt++);
                            if ((type == null || type.getKind() != TypeKind.ERROR) &&
                                    EnumSet.range(ElementKind.PACKAGE, ElementKind.INTERFACE).contains(elem.getEnclosingElement().getKind())) {
                                sb.append(" type=\""); //NOI18N
                                sb.append(elem.getQualifiedName());
                                sb.append("\" default=\""); //NOI18N
                                sb.append(elem.getSimpleName());
                            } else {
                                sb.append(" default=\""); //NOI18N
                                sb.append(elem.getQualifiedName());
                            }
                            sb.append("\" editable=false}"); //NOI18N
                            Iterator<? extends TypeMirror> tas = type != null ? type.getTypeArguments().iterator() : null;
                            if (tas != null && tas.hasNext()) {
                                sb.append('<'); //NOI18N
                                if (!insideNew || controller.getSourceVersion().compareTo(SourceVersion.RELEASE_7) < 0) {
                                    while (tas.hasNext()) {
                                        TypeMirror ta = tas.next();
                                        sb.append("${PAR"); //NOI18N
                                        sb.append(cnt++);
                                        if (ta.getKind() == TypeKind.TYPEVAR) {
                                            TypeVariable tv = (TypeVariable)ta;
                                            if (elem == tv.asElement().getEnclosingElement()) {
                                                sb.append(" typeVar=\""); //NOI18N
                                                sb.append(tv.asElement().getSimpleName());
                                                sb.append("\" type=\""); //NOI18N
                                                ta = tv.getUpperBound();
                                                sb.append(Utilities.getTypeName(ta, true));
                                                sb.append("\" default=\""); //NOI18N
                                                sb.append(Utilities.getTypeName(ta, false));
                                                if (addTypeVars && SourceVersion.RELEASE_5.compareTo(controller.getSourceVersion()) <= 0)
                                                    asTemplate = true;
                                            } else {
                                                sb.append(" editable=false default=\""); //NOI18N
                                                sb.append(Utilities.getTypeName(ta, true));
                                                asTemplate = true;
                                            }
                                            sb.append("\"}"); //NOI18N
                                        } else if (ta.getKind() == TypeKind.WILDCARD) {
                                            sb.append(" type=\""); //NOI18N
                                            TypeMirror bound = ((WildcardType)ta).getExtendsBound();
                                            if (bound == null)
                                                bound = ((WildcardType)ta).getSuperBound();
                                            sb.append(bound != null ? Utilities.getTypeName(bound, true) : "Object"); //NOI18N
                                            sb.append("\" default=\""); //NOI18N
                                            sb.append(bound != null ? Utilities.getTypeName(bound, false) : "Object"); //NOI18N
                                            sb.append("\"}"); //NOI18N
                                            asTemplate = true;
                                        } else if (ta.getKind() == TypeKind.ERROR) {
                                            sb.append(" default=\""); //NOI18N
                                            sb.append(((ErrorType)ta).asElement().getSimpleName());
                                            sb.append("\"}"); //NOI18N
                                            asTemplate = true;
                                        } else {
                                            sb.append(" type=\""); //NOI18N
                                            sb.append(Utilities.getTypeName(ta, true));
                                            sb.append("\" default=\""); //NOI18N
                                            sb.append(Utilities.getTypeName(ta, false));
                                            sb.append("\" editable=false}"); //NOI18N
                                            asTemplate = true;
                                        }
                                        if (tas.hasNext())
                                            sb.append(", "); //NOI18N
                                    }
                                } else {
                                    asTemplate = true;
                                }
                                sb.append('>'); //NOI18N
                            }
                            for(int i = 0; i < dim; i++) {
                                sb.append("[${PAR"); //NOI18N
                                sb.append(cnt++);
                                sb.append(" instanceof=\"int\" default=\"\"}]"); //NOI18N
                                asTemplate = true;
                            }
                        }
                        if (asTemplate) {
                            if (insideNew)
                                sb.append("${cursor completionInvoke}"); //NOI18N
                            if (finalLen > 0) {
                                doc.runAtomic (new Runnable () {
                                    public void run () {
                                        try {
                                            doc.remove(offset, finalLen);
                                        } catch (BadLocationException e) {
                                            // Can't update
                                        }
                                    }
                                });
                            }
                            CodeTemplateManager ctm = CodeTemplateManager.get(doc);
                            if (ctm != null)
                                ctm.createTemporary(sb.append(text).toString()).insert(c);
                        } else {
                            // Update the text
                            doc.runAtomic (new Runnable () {
                                public void run () {
                                    try {
                                        Position semiPosition = semiPos > -1 && !insideNew ? doc.createPosition(semiPos) : null;
                                        TreePath tp = controller.getTreeUtilities().pathFor(embeddedOffset);
                                        CharSequence cs = enclName == null ? elem != null ? elem.getSimpleName() : simpleName : AutoImport.resolveImport(controller, tp, controller.getTypes().getDeclaredType(elem));
                                        if (!insideNew)
                                            cs = text.insert(0, cs);
                                        String textToReplace = doc.getText(offset, finalLen);
                                        Position pos = doc.createPosition(offset);
                                        if (textToReplace.contentEquals(cs)) return;
                                        doc.remove(offset, finalLen);
                                        doc.insertString(pos.getOffset(), cs.toString(), null);
                                        if (semiPosition != null)
                                            doc.insertString(semiPosition.getOffset(), ";", null); //NOI18N
                                    } catch (BadLocationException e) {
                                        // Can't update
                                    }
                                }
                            });
                            if (insideNew && type != null && type.getKind() == TypeKind.DECLARED) {
                                ExecutableElement ctor = null;
                                Trees trees = controller.getTrees();
                                Scope scope = controller.getTreeUtilities().scopeFor(embeddedOffset);
                                int val = 0; // no constructors seen yet
                                for (ExecutableElement ee : ElementFilter.constructorsIn(elem.getEnclosedElements())) {
                                    if (trees.isAccessible(scope, ee, type)) {
                                        if (ctor != null) {
                                            val = 2; // more than one accessible constructors seen
                                            break;
                                        }
                                        ctor = ee;
                                    }
                                    val = 1; // constructor seen
                                }
                                if (val != 1 || ctor != null) {
                                    final JavaCompletionItem item = val == 0 ? createDefaultConstructorItem(elem, offset, true) :
                                            val == 2 || Utilities.hasAccessibleInnerClassConstructor(elem, scope, trees) ? null :
                                            createExecutableItem(ctor, (ExecutableType)controller.getTypes().asMemberOf(type, ctor), offset, false, false, false, true);
                                    try {
                                        final Position offPosition = doc.createPosition(offset);
                                        SwingUtilities.invokeLater(new Runnable() {
                                            public void run() {
                                                if (item != null) {
                                                    item.substituteText(c, offPosition.getOffset(), c.getSelectionEnd() - offPosition.getOffset(), text.toString());
                                                } else {
                                                    //Temporary ugly solution
                                                    SwingUtilities.invokeLater(new Runnable() {
                                                        public void run() {
                                                            Completion.get().showCompletion();
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                    catch (BadLocationException e) {}
                                }
                            }
                        }
                    }
                });
            } catch (ParseException pe) {
            }
        }
        
        public String toString() {
            return simpleName;
        }        
    }
    
    static class InterfaceItem extends ClassItem {
        
        private static final String INTERFACE = "org/netbeans/modules/editor/resources/completion/interface.png"; // NOI18N
        private static final String INTERFACE_COLOR = "<font color=#404040>"; //NOI18N
        private static ImageIcon icon;
        
        private InterfaceItem(TypeElement elem, DeclaredType type, int dim, int substitutionOffset, boolean displayPkgName, boolean isDeprecated, boolean insideNew, boolean addTypeVars, boolean smartType) {
            super(elem, type, dim, substitutionOffset, displayPkgName, isDeprecated, insideNew, addTypeVars, smartType);
        }

        protected ImageIcon getIcon(){
            if (icon == null) icon = ImageUtilities.loadImageIcon(INTERFACE, false);
            return icon;            
        }
        
        protected String getColor() {
            return INTERFACE_COLOR;
        }
    }

    static class EnumItem extends ClassItem {
        
        private static final String ENUM = "org/netbeans/modules/editor/resources/completion/enum.png"; // NOI18N
        private static ImageIcon icon;
        
        private EnumItem(TypeElement elem, DeclaredType type, int dim, int substitutionOffset, boolean displayPkgName, boolean isDeprecated, boolean insideNew, boolean smartType) {
        super(elem, type, dim, substitutionOffset, displayPkgName, isDeprecated, insideNew, false, smartType);
        }

        protected ImageIcon getIcon(){
            if (icon == null) icon = ImageUtilities.loadImageIcon(ENUM, false);
            return icon;            
        }
    }
    
    static class AnnotationTypeItem extends ClassItem {
        
        private static final String ANNOTATION = "org/netbeans/modules/editor/resources/completion/annotation_type.png"; // NOI18N
        private static ImageIcon icon;
        
        private AnnotationTypeItem(TypeElement elem, DeclaredType type, int dim, int substitutionOffset, boolean displayPkgName, boolean isDeprecated, boolean insideNew, boolean smartType) {
            super(elem, type, dim, substitutionOffset, displayPkgName, isDeprecated, insideNew, false, smartType);
        }

        protected ImageIcon getIcon(){
            if (icon == null) icon = ImageUtilities.loadImageIcon(ANNOTATION, false);
            return icon;            
        }
    }
    
    static class TypeParameterItem extends JavaCompletionItem {
        
        private static final String TYPE_PARAMETER_COLOR = "<font color=#000000>"; //NOI18N

        private String simpleName;
        private String leftText;

        private TypeParameterItem(TypeParameterElement elem, int substitutionOffset) {
            super(substitutionOffset);
            this.simpleName = elem.getSimpleName().toString();
        }
        
        public int getSortPriority() {
            return 700;
        }
        
        public CharSequence getSortText() {
            return simpleName;
        }
        
        public CharSequence getInsertPrefix() {
            return simpleName;
        }
        
        protected String getLeftHtmlText() {
            if (leftText == null)
                leftText = TYPE_PARAMETER_COLOR + simpleName + COLOR_END;
            return leftText;
        }
        
        public String toString() {
            return simpleName;
        }        
    }

    static class VariableItem extends JavaCompletionItem {
        
        private static final String LOCAL_VARIABLE = "org/netbeans/modules/editor/resources/completion/localVariable.gif"; //NOI18N
        private static final String PARAMETER_COLOR = "<font color=#00007c>"; //NOI18N
        private static ImageIcon icon;

        private String varName;
        private boolean smartType;
        private String typeName;
        private String leftText;
        private String rightText;
        
        private VariableItem(TypeMirror type, String varName, int substitutionOffset, boolean smartType) {
            super(substitutionOffset);
            this.varName = varName;
            this.smartType = smartType;
            this.typeName = type != null ? Utilities.getTypeName(type, false).toString() : null;
        }
        
        public int getSortPriority() {
            return smartType ? 200 - SMART_TYPE : 200;
        }
        
        public CharSequence getSortText() {
            return varName;
        }
        
        public CharSequence getInsertPrefix() {
            return varName;
        }

        protected String getLeftHtmlText() {
            if (leftText == null)
                leftText = PARAMETER_COLOR + BOLD + varName + BOLD_END + COLOR_END;
            return leftText;
        }
        
        protected String getRightHtmlText() {
            if (rightText == null)
                rightText = escape(typeName);
            return rightText;
        }
        
        protected ImageIcon getIcon(){
            if (icon == null) icon = ImageUtilities.loadImageIcon(LOCAL_VARIABLE, false);
            return icon;            
        }

        public String toString() {
            return (typeName != null ? typeName + " " : "") + varName; //NOI18N
        }
   }

    static class FieldItem extends JavaCompletionItem {
        
        private static final String FIELD_PUBLIC = "org/netbeans/modules/editor/resources/completion/field_16.png"; //NOI18N
        private static final String FIELD_PROTECTED = "org/netbeans/modules/editor/resources/completion/field_protected_16.png"; //NOI18N
        private static final String FIELD_PACKAGE = "org/netbeans/modules/editor/resources/completion/field_package_private_16.png"; //NOI18N
        private static final String FIELD_PRIVATE = "org/netbeans/modules/editor/resources/completion/field_private_16.png"; //NOI18N        
        private static final String FIELD_ST_PUBLIC = "org/netbeans/modules/editor/resources/completion/field_static_16.png"; //NOI18N
        private static final String FIELD_ST_PROTECTED = "org/netbeans/modules/editor/resources/completion/field_static_protected_16.png"; //NOI18N
        private static final String FIELD_ST_PACKAGE = "org/netbeans/modules/editor/resources/completion/field_static_package_private_16.png"; //NOI18N
        private static final String FIELD_ST_PRIVATE = "org/netbeans/modules/editor/resources/completion/field_static_private_16.png"; //NOI18N
        private static final String FIELD_COLOR = "<font color=#008618>"; //NOI18N
        private static ImageIcon icon[][] = new ImageIcon[2][4];
        
        private ElementHandle<VariableElement> elementHandle;
        private boolean isInherited;
        private boolean isDeprecated;
        private boolean smartType;
        private String simpleName;
        private Set<Modifier> modifiers;
        private String typeName;
        private String leftText;
        private String rightText;
        
        private FieldItem(VariableElement elem, TypeMirror type, int substitutionOffset, boolean isInherited, boolean isDeprecated, boolean smartType) {
            super(substitutionOffset);
            this.elementHandle = ElementHandle.create(elem);
            this.isInherited = isInherited;
            this.isDeprecated = isDeprecated;
            this.smartType = smartType;
            this.simpleName = elem.getSimpleName().toString();
            this.modifiers = elem.getModifiers();
            this.typeName = Utilities.getTypeName(type, false).toString();
        }
        
        public int getSortPriority() {
            return smartType ? 300 - SMART_TYPE : 300;
        }
        
        public CharSequence getSortText() {
            return simpleName;
        }
        
        public CharSequence getInsertPrefix() {
            return simpleName;
        }

        public CompletionTask createDocumentationTask() {
            return JavaCompletionProvider.createDocTask(elementHandle);
        }

        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(FIELD_COLOR);
                if (!isInherited)
                    sb.append(BOLD);
                if (isDeprecated)
                    sb.append(STRIKE);
                sb.append(simpleName);
                if (isDeprecated)
                    sb.append(STRIKE_END);
                if (!isInherited)
                    sb.append(BOLD_END);
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
        }
        
        protected String getRightHtmlText() {
            if (rightText == null)
                rightText = escape(typeName);
            return rightText;
        }
        
        protected ImageIcon getIcon(){
            int level = getProtectionLevel(modifiers);
            boolean isStatic = modifiers.contains(Modifier.STATIC);
            ImageIcon cachedIcon = icon[isStatic?1:0][level];
            if (cachedIcon != null)
                return cachedIcon;            

            String iconPath = FIELD_PUBLIC;
            if (isStatic) {
                switch (level) {
                    case PRIVATE_LEVEL:
                        iconPath = FIELD_ST_PRIVATE;
                        break;

                    case PACKAGE_LEVEL:
                        iconPath = FIELD_ST_PACKAGE;
                        break;

                    case PROTECTED_LEVEL:
                        iconPath = FIELD_ST_PROTECTED;
                        break;

                    case PUBLIC_LEVEL:
                        iconPath = FIELD_ST_PUBLIC;
                        break;
                }
            }else{
                switch (level) {
                    case PRIVATE_LEVEL:
                        iconPath = FIELD_PRIVATE;
                        break;

                    case PACKAGE_LEVEL:
                        iconPath = FIELD_PACKAGE;
                        break;

                    case PROTECTED_LEVEL:
                        iconPath = FIELD_PROTECTED;
                        break;

                    case PUBLIC_LEVEL:
                        iconPath = FIELD_PUBLIC;
                        break;
                }
            }
            ImageIcon newIcon = ImageUtilities.loadImageIcon(iconPath, false);
            icon[isStatic?1:0][level] = newIcon;
            return newIcon;            
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for(Modifier mod : modifiers) {
               sb.append(mod.toString());
               sb.append(' ');
            }
            sb.append(typeName);
            sb.append(' ');
            sb.append(simpleName);
            return sb.toString();
        }
    }
    
    static class MethodItem extends JavaCompletionItem {
        
        private static final String METHOD_PUBLIC = "org/netbeans/modules/editor/resources/completion/method_16.png"; //NOI18N
        private static final String METHOD_PROTECTED = "org/netbeans/modules/editor/resources/completion/method_protected_16.png"; //NOI18N
        private static final String METHOD_PACKAGE = "org/netbeans/modules/editor/resources/completion/method_package_private_16.png"; //NOI18N
        private static final String METHOD_PRIVATE = "org/netbeans/modules/editor/resources/completion/method_private_16.png"; //NOI18N        
        private static final String METHOD_ST_PUBLIC = "org/netbeans/modules/editor/resources/completion/method_static_16.png"; //NOI18N
        private static final String METHOD_ST_PROTECTED = "org/netbeans/modules/editor/resources/completion/method_static_protected_16.png"; //NOI18N
        private static final String METHOD_ST_PRIVATE = "org/netbeans/modules/editor/resources/completion/method_static_private_16.png"; //NOI18N
        private static final String METHOD_ST_PACKAGE = "org/netbeans/modules/editor/resources/completion/method_static_package_private_16.png"; //NOI18N
        private static final String METHOD_COLOR = "<font color=#000000>"; //NOI18N
        private static final String PARAMETER_NAME_COLOR = "<font color=#a06001>"; //NOI18N
        private static ImageIcon icon[][] = new ImageIcon[2][4];

        protected ElementHandle<ExecutableElement> elementHandle;
        private boolean isInherited;
        private boolean isDeprecated;
        private boolean inImport;
        private boolean smartType;
        private String simpleName;
        protected Set<Modifier> modifiers;
        private List<ParamDesc> params;
        private String typeName;
        private boolean isPrimitive;
        private String sortText;
        private String leftText;
        private String rightText;
        
        private MethodItem(ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isInherited, boolean isDeprecated, boolean inImport, boolean smartType) {
            super(substitutionOffset);
            this.elementHandle = ElementHandle.create(elem);
            this.isInherited = isInherited;
            this.isDeprecated = isDeprecated;
            this.inImport = inImport;
            this.smartType = smartType;
            this.simpleName = elem.getSimpleName().toString();
            this.modifiers = elem.getModifiers();
            this.params = new ArrayList<ParamDesc>();
            Iterator<? extends VariableElement> it = elem.getParameters().iterator();
            Iterator<? extends TypeMirror> tIt = type.getParameterTypes().iterator();
            while(it.hasNext() && tIt.hasNext()) {
                TypeMirror tm = tIt.next();
                if (tm == null)
                    break;
                this.params.add(new ParamDesc(tm.toString(), Utilities.getTypeName(tm, false, elem.isVarArgs() && !tIt.hasNext()).toString(), it.next().getSimpleName().toString()));
            }
            TypeMirror retType = type.getReturnType();
            this.typeName = Utilities.getTypeName(retType, false).toString();
            this.isPrimitive = retType.getKind().isPrimitive() || retType.getKind() == TypeKind.VOID;
        }
        
        public int getSortPriority() {
            return smartType ? 500 - SMART_TYPE : 500;
        }
        
        public CharSequence getSortText() {
            if (sortText == null) {
                StringBuilder sortParams = new StringBuilder();
                sortParams.append('(');
                int cnt = 0;
                for(Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                    ParamDesc param = it.next();
                    sortParams.append(param.typeName);
                    if (it.hasNext()) {
                        sortParams.append(',');
                    }
                    cnt++;
                }
                sortParams.append(')');
                sortText = simpleName + "#" + ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString(); //NOI18N
            }
            return sortText;
        }
        
        public CharSequence getInsertPrefix() {
            return simpleName;
        }
        
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder lText = new StringBuilder();
                lText.append(METHOD_COLOR);
                if (!isInherited)
                    lText.append(BOLD);
                if (isDeprecated)
                    lText.append(STRIKE);
                lText.append(simpleName);
                if (isDeprecated)
                    lText.append(STRIKE_END);
                if (!isInherited)
                    lText.append(BOLD_END);
                lText.append(COLOR_END);
                lText.append('(');
                for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                    ParamDesc paramDesc = it.next();
                    lText.append(escape(paramDesc.typeName));
                    lText.append(' ');
                    lText.append(PARAMETER_NAME_COLOR);
                    lText.append(paramDesc.name);
                    lText.append(COLOR_END);
                    if (it.hasNext()) {
                        lText.append(", "); //NOI18N
                    }
                }
                lText.append(')');
                return lText.toString();
            }
            return leftText;
        }
        
        protected String getRightHtmlText() {
            if (rightText == null)
                rightText = escape(typeName);
            return rightText;
        }
        
        public CompletionTask createDocumentationTask() {
            return JavaCompletionProvider.createDocTask(elementHandle);
        }

        protected ImageIcon getIcon() {
            int level = getProtectionLevel(modifiers);
            boolean isStatic = modifiers.contains(Modifier.STATIC);
            ImageIcon cachedIcon = icon[isStatic?1:0][level];
            if (cachedIcon != null)
                return cachedIcon;
            
            String iconPath = METHOD_PUBLIC;            
            if (isStatic) {
                switch (level) {
                    case PRIVATE_LEVEL:
                        iconPath = METHOD_ST_PRIVATE;
                        break;

                    case PACKAGE_LEVEL:
                        iconPath = METHOD_ST_PACKAGE;
                        break;

                    case PROTECTED_LEVEL:
                        iconPath = METHOD_ST_PROTECTED;
                        break;

                    case PUBLIC_LEVEL:
                        iconPath = METHOD_ST_PUBLIC;
                        break;
                }
            }else{
                switch (level) {
                    case PRIVATE_LEVEL:
                        iconPath = METHOD_PRIVATE;
                        break;

                    case PACKAGE_LEVEL:
                        iconPath = METHOD_PACKAGE;
                        break;

                    case PROTECTED_LEVEL:
                        iconPath = METHOD_PROTECTED;
                        break;

                    case PUBLIC_LEVEL:
                        iconPath = METHOD_PUBLIC;
                        break;
                }
            }
            ImageIcon newIcon = ImageUtilities.loadImageIcon(iconPath, false);
            icon[isStatic?1:0][level] = newIcon;
            return newIcon;            
        }
        
        protected void substituteText(final JTextComponent c, final int offset, int len, String toAdd) {
            if (toAdd == null) {
                if (isPrimitive) {
                    try {
                        final String[] ret = new String[1];
                        Source s = Source.create(c.getDocument());
                        ParserManager.parse(Collections.singletonList(s), new UserTask() {
                            @Override
                            public void run(ResultIterator resultIterator) throws Exception {
                                final CompilationController controller = CompilationController.get(resultIterator.getParserResult(offset));
                                controller.toPhase(Phase.PARSED);
                                int embeddedOffset = controller.getSnapshot().getEmbeddedOffset(c.getSelectionEnd());
                                TreePath tp = controller.getTreeUtilities().pathFor(embeddedOffset);
                                Tree tree = tp.getLeaf();
                                if (tree.getKind() == Tree.Kind.IDENTIFIER || tree.getKind() == Tree.Kind.PRIMITIVE_TYPE) {
                                    tp = tp.getParentPath();
                                    if (tp.getLeaf().getKind() == Tree.Kind.VARIABLE && ((VariableTree)tp.getLeaf()).getType() == tree)
                                        ret[0] = ";"; //NOI18N
                                }
                                if (tp.getLeaf().getKind() == Tree.Kind.MEMBER_SELECT ||
                                    (tp.getLeaf().getKind() == Tree.Kind.METHOD_INVOCATION && ((MethodInvocationTree)tp.getLeaf()).getMethodSelect() == tree))
                                    tp = tp.getParentPath();
                                if (tp.getLeaf().getKind() == Tree.Kind.EXPRESSION_STATEMENT || tp.getLeaf().getKind() == Tree.Kind.BLOCK)
                                    ret[0] = ";"; //NOI18N
                            }
                        });
                        toAdd = ret[0];
                    } catch (ParseException ex) {
                    }
                }
            }
            if (inImport || params.isEmpty()) {
                String add = inImport ? ";" : CodeStyle.getDefault(c.getDocument()).spaceBeforeMethodCallParen() ? " ()" : "()"; //NOI18N
                if (toAdd != null && !add.startsWith(toAdd))
                    add += toAdd;
                super.substituteText(c, offset, len, add);
                if ("(".equals(toAdd)) //NOI18N
                    c.setCaretPosition(c.getCaretPosition() - 1);
            } else {
                String add = "()"; //NOI18N
                if (toAdd != null && !add.startsWith(toAdd))
                    add += toAdd;
                final BaseDocument doc = (BaseDocument)c.getDocument();
                String text = ""; //NOI18N
                final int semiPos = add.endsWith(";") ? findPositionForSemicolon(c) : -2; //NOI18N
                if (semiPos > -2)
                    add = add.length() > 1 ? add.substring(0, add.length() - 1) : null;
                TokenSequence<JavaTokenId> sequence = SourceUtils.getJavaTokenSequence(TokenHierarchy.get(doc), offset + len);
                if (sequence == null || !sequence.moveNext() && !sequence.movePrevious()) {
                    text += add;
                    add = null;
                }
                boolean added = false;
                while(add != null && add.length() > 0) {
                    String tokenText = sequence.token().text().toString();
                    if (tokenText.startsWith(add)) {
                        len = sequence.offset() - offset + add.length();
                        text += add;
                        add = null;
                    } else if (add.startsWith(tokenText)) {
                        sequence.moveNext();
                        len = sequence.offset() - offset;
                        text += add.substring(0, tokenText.length());
                        add = add.substring(tokenText.length());
                        added = true;
                    } else if (sequence.token().id() == JavaTokenId.WHITESPACE && sequence.token().text().toString().indexOf('\n') < 0) {//NOI18N
                        if (!sequence.moveNext()) {
                            text += add;
                            add = null;
                        }
                    } else {
                        if (!added)
                            text += add;
                        add = null;
                    }
                }
                final int length = len;
                doc.runAtomic (new Runnable () {
                    public void run () {
                    try {
                        Position pos = doc.createPosition(offset);
                        Position semiPosition = semiPos > -1 ? doc.createPosition(semiPos) : null;
                        if (length > 0)
                            doc.remove(pos.getOffset(), length);
                        doc.insertString(pos.getOffset(), getInsertPrefix().toString(), null);
                        if (semiPosition != null)
                            doc.insertString(semiPosition.getOffset(), ";", null); //NOI18N
                    } catch (BadLocationException e) {
                        // Can't update
                    }
                    }
                });
                CodeTemplateManager ctm = CodeTemplateManager.get(doc);
                if (ctm != null) {
                    StringBuilder sb = new StringBuilder();
                    boolean guessArgs = Utilities.guessMethodArguments();
                    if (CodeStyle.getDefault(doc).spaceBeforeMethodCallParen())
                    sb.append(' '); //NOI18N
                    sb.append('('); //NOI18N
                    if (text.length() > 1) {
                        for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                            ParamDesc paramDesc = it.next();
                            sb.append("${"); //NOI18N
                            sb.append(paramDesc.name);
                            if (guessArgs) {
                                sb.append(" named instanceof="); //NOI18N
                                sb.append(paramDesc.fullTypeName);
                            }
                            sb.append('}'); //NOI18N
                            if (it.hasNext())
                                sb.append(", "); //NOI18N
                        }
                        sb.append(')');//NOI18N
                        if (text.length() > 2)
                            sb.append(text.substring(2));
                    }
                    ctm.createTemporary(sb.toString()).insert(c);
                    Completion.get().showToolTip();
                }
            }
        }        

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Modifier mod : modifiers) {
                sb.append(mod.toString());
                sb.append(' ');
            }
            sb.append(typeName);
            sb.append(' ');
            sb.append(simpleName);
            sb.append('(');
            for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                ParamDesc paramDesc = it.next();
                sb.append(paramDesc.typeName);
                sb.append(' ');
                sb.append(paramDesc.name);
                if (it.hasNext()) {
                    sb.append(", "); //NOI18N
                }
            }
            sb.append(')');
            return sb.toString();
        }
    }    

    static class OverrideMethodItem extends MethodItem {
        
        private static final String IMPL_BADGE_PATH = "org/netbeans/modules/java/editor/resources/implement_badge.png";
        private static final String OVRD_BADGE_PATH = "org/netbeans/modules/java/editor/resources/override_badge.png";
        
        private static final String OVERRIDE_TEXT = NbBundle.getMessage(JavaCompletionItem.class, "override_Lbl");
        private static final String IMPLEMENT_TEXT = NbBundle.getMessage(JavaCompletionItem.class, "implement_Lbl");
        
        private static ImageIcon implementBadge = ImageUtilities.loadImageIcon(IMPL_BADGE_PATH, false);
        private static ImageIcon overrideBadge = ImageUtilities.loadImageIcon(OVRD_BADGE_PATH, false);
        private static ImageIcon merged_icon[][] = new ImageIcon[2][4];
        
        
        private boolean implement;
        private String leftText;
        
        private OverrideMethodItem(ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean implement) {
            super(elem, type, substitutionOffset, false, false, false, false);
            this.implement = implement;
        }
        
        protected String getLeftHtmlText() {
            if (leftText == null) {
                leftText = super.getLeftHtmlText() + " - ";
                leftText += (implement ? IMPLEMENT_TEXT : OVERRIDE_TEXT);
            }
            return leftText;
        }
        
        @Override
        protected ImageIcon getIcon() {
            int level = getProtectionLevel(modifiers);
            ImageIcon merged = merged_icon[implement? 0 : 1][level];
            if ( merged != null ) {
                return merged;
            }
            ImageIcon superIcon = super.getIcon();                        
            merged = new ImageIcon( ImageUtilities.mergeImages(
                superIcon.getImage(), 
                implement ? implementBadge.getImage() : overrideBadge.getImage(), 
                16 - 8, 
                16 - 8) );
            
            merged_icon[implement? 0 : 1][level] = merged;
            return merged;
        }

        
        protected void substituteText(final JTextComponent c, final int offset, final int len, String toAdd) {
            final BaseDocument doc = (BaseDocument)c.getDocument();
            final Position pos;
            try {
                pos = doc.createPosition(offset);
            } catch (BadLocationException e) {
                return; // Invalid offset -> do nothing
            }
            if (len > 0) {
                doc.runAtomic (new Runnable () {
                    public void run () {
                        try {
                            doc.remove(offset, len);
                        } catch (BadLocationException e) {
                            // Can't update
                        }
                    }
                });
            }
            try {
                ModificationResult mr = ModificationResult.runModificationTask(Collections.singletonList(Source.create(doc)), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        WorkingCopy copy = WorkingCopy.get(resultIterator.getParserResult());
                        copy.toPhase(Phase.ELEMENTS_RESOLVED);
                        ExecutableElement ee = elementHandle.resolve(copy);                        
                        if (ee == null)
                            return;
                        final int embeddedOffset = copy.getSnapshot().getEmbeddedOffset(pos.getOffset());
                        TreePath tp = copy.getTreeUtilities().pathFor(embeddedOffset);
                        if (tp.getLeaf().getKind() == Tree.Kind.CLASS) {
                            if (Utilities.inAnonymousOrLocalClass(tp))
                                copy.toPhase(Phase.RESOLVED);
                            int idx = 0;
                            for (Tree tree : ((ClassTree)tp.getLeaf()).getMembers()) {
                                if (copy.getTrees().getSourcePositions().getStartPosition(tp.getCompilationUnit(), tree) < embeddedOffset)
                                    idx++;
                                else
                                    break;
                            }
                            if (implement)
                                GeneratorUtils.generateAbstractMethodImplementation(copy, tp, ee, idx);
                            else
                                GeneratorUtils.generateMethodOverride(copy, tp, ee, idx);
                        }
                    }
                });
                GeneratorUtils.guardedCommit(c, mr);
            } catch (Exception ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toString());
            sb.append(" - ");
            sb.append(implement ? IMPLEMENT_TEXT : OVERRIDE_TEXT);
            return sb.toString();
        }

        public boolean instantSubstitution(JTextComponent component) {
            return false;//no instant substitution for override method item.
        }
    }

    static class GetterSetterMethodItem extends JavaCompletionItem {
        
        private static final String METHOD_PUBLIC = "org/netbeans/modules/editor/resources/completion/method_16.png"; //NOI18N
        private static final String GETTER_BADGE_PATH = "org/netbeans/modules/java/editor/resources/getter_badge.png"; //NOI18N
        private static final String SETTER_BADGE_PATH = "org/netbeans/modules/java/editor/resources/setter_badge.png"; //NOI18N
        private static final String METHOD_COLOR = "<font color=#000000>"; //NOI18N
        private static final String PARAMETER_NAME_COLOR = "<font color=#a06001>"; //NOI18N
        
        private static ImageIcon superIcon;
        private static ImageIcon[] merged_icons = new ImageIcon[2];
        
        protected ElementHandle<VariableElement> elementHandle;
        private boolean setter;
        private String simpleName;
        private String name;
        private String typeName;
        private String sortText;
        private String leftText;
        private String rightText;
        
        private GetterSetterMethodItem(VariableElement elem, TypeMirror type, int substitutionOffset, boolean setter) {
            super(substitutionOffset);
            this.elementHandle = ElementHandle.create(elem);            
            this.setter = setter;
            this.simpleName = elem.getSimpleName().toString();
            if (setter)
                this.name = "set" + GeneratorUtils.getCapitalizedName(simpleName); //NOI18N
            else
                this.name = (elem.asType().getKind() == TypeKind.BOOLEAN ? "is" : "get") + GeneratorUtils.getCapitalizedName(simpleName); //NOI18N
            this.typeName = Utilities.getTypeName(type, false).toString();
        }
        
        public int getSortPriority() {
            return 500;
        }
        
        public CharSequence getSortText() {
            if (sortText == null) {
                StringBuilder sortParams = new StringBuilder();
                sortParams.append('('); //NOI18N
                if (setter)
                    sortParams.append(typeName);
                sortParams.append(')'); //NOI18N
                sortText = name + "#" + (setter ? "01" : "00") + "#" + sortParams.toString(); //NOI18N
            }
            return sortText;
        }
        
        public CharSequence getInsertPrefix() {
            return name;
        }
        
        @Override
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder lText = new StringBuilder();
                lText.append(METHOD_COLOR);
                lText.append(BOLD);
                lText.append(name);
                lText.append(BOLD_END);
                lText.append(COLOR_END);
                lText.append('(');
                if (setter) {
                    lText.append(escape(typeName));
                    lText.append(' ');
                    lText.append(PARAMETER_NAME_COLOR);
                    lText.append(simpleName);
                    lText.append(COLOR_END);
                }
                lText.append(") - "); //NOI18N
                lText.append(GENERATE_TEXT);
                leftText = lText.toString();
            }
            return leftText;
        }
        
        @Override
        protected String getRightHtmlText() {
            if (rightText == null)
                rightText = setter ? "void" : escape(typeName);
            return rightText;
        }
        
        @Override
        protected ImageIcon getIcon() {
            if (merged_icons[setter ? 1 : 0] == null) {
                if (superIcon == null)
                    superIcon = ImageUtilities.loadImageIcon(METHOD_PUBLIC, false);
                if (setter) {
                    ImageIcon setterBadge = ImageUtilities.loadImageIcon(SETTER_BADGE_PATH, false);
                    merged_icons[1] = new ImageIcon(ImageUtilities.mergeImages(superIcon.getImage(),
                            setterBadge.getImage(), 16 - 8, 16 - 8));
                } else {
                    ImageIcon getterBadge = ImageUtilities.loadImageIcon(GETTER_BADGE_PATH, false);
                    merged_icons[0] = new ImageIcon(ImageUtilities.mergeImages(superIcon.getImage(),
                            getterBadge.getImage(), 16 - 8, 16 - 8));
                }
            }
            return merged_icons[setter ? 1 : 0];
        }
        
        @Override
        protected void substituteText(final JTextComponent c, final int offset, final int len, String toAdd) {
            final BaseDocument doc = (BaseDocument)c.getDocument();
            final Position pos;
            try {
                pos = doc.createPosition(offset);
            } catch (BadLocationException e) {
                return; // Invalid offset -> do nothing
            }
            if (len > 0) {
                doc.runAtomic (new Runnable () {
                    public void run () {
                    try {
                        doc.remove(offset, len);
                    } catch (BadLocationException e) {
                        // Can't update
                    }
                    }
                });
            }
            try {
                ModificationResult mr = ModificationResult.runModificationTask(Collections.singletonList(Source.create(doc)), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        WorkingCopy copy = WorkingCopy.get(resultIterator.getParserResult());
                        copy.toPhase(Phase.ELEMENTS_RESOLVED);
                        VariableElement ve = elementHandle.resolve(copy);                        
                        if (ve == null)
                            return;
                        final int embeddedOffset = copy.getSnapshot().getEmbeddedOffset(pos.getOffset());
                        TreePath tp = copy.getTreeUtilities().pathFor(embeddedOffset);
                        if (tp.getLeaf().getKind() == Tree.Kind.CLASS) {
                            if (Utilities.inAnonymousOrLocalClass(tp))
                                copy.toPhase(Phase.RESOLVED);
                            int idx = 0;
                            for (Tree tree : ((ClassTree)tp.getLeaf()).getMembers()) {
                                if (copy.getTrees().getSourcePositions().getStartPosition(tp.getCompilationUnit(), tree) < embeddedOffset)
                                    idx++;
                                else
                                    break;
                            }
                            TypeElement te = (TypeElement)copy.getTrees().getElement(tp);
                            if (te != null) {
                                GeneratorUtilities gu = GeneratorUtilities.get(copy);
                                MethodTree method = setter ? gu.createSetter(te, ve) : gu.createGetter(te, ve);
                                ClassTree decl = copy.getTreeMaker().insertClassMember((ClassTree)tp.getLeaf(), idx, method);
                                copy.rewrite(tp.getLeaf(), decl);
                            }
                        }
                    }
                });
                GeneratorUtils.guardedCommit(c, mr);
            } catch (Exception ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("public "); //NOI18N
            sb.append(setter ? "void" : typeName); //NOI18N
            sb.append(' ');
            sb.append(name);
            sb.append('(');
            if (setter) {
                sb.append(typeName);
                sb.append(' ');
                sb.append(simpleName);
            }
            sb.append(") - "); //NOI18N
            sb.append(GENERATE_TEXT);
            return sb.toString();
        }

        @Override
        public boolean instantSubstitution(JTextComponent component) {
            return false;//no instant substitution for override method item.
        }
    }

    static class ConstructorItem extends JavaCompletionItem {
        
        private static final String CONSTRUCTOR_PUBLIC = "org/netbeans/modules/editor/resources/completion/constructor_16.png"; //NOI18N
        private static final String CONSTRUCTOR_PROTECTED = "org/netbeans/modules/editor/resources/completion/constructor_protected_16.png"; //NOI18N
        private static final String CONSTRUCTOR_PACKAGE = "org/netbeans/modules/editor/resources/completion/constructor_package_private_16.png"; //NOI18N
        private static final String CONSTRUCTOR_PRIVATE = "org/netbeans/modules/editor/resources/completion/constructor_private_16.png"; //NOI18N
        private static final String CONSTRUCTOR_COLOR = "<font color=#b28b00>"; //NOI18N
        private static final String PARAMETER_NAME_COLOR = "<font color=#a06001>"; //NOI18N
        private static ImageIcon icon[] = new ImageIcon[4];

        private ElementHandle<ExecutableElement> elementHandle;
        private boolean isDeprecated;
        private boolean smartType;
        private String simpleName;
        protected Set<Modifier> modifiers;
        private List<ParamDesc> params;
        private boolean isAbstract;
        private boolean insertName;
        private String sortText;
        private String leftText;
        
        private ConstructorItem(ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, boolean smartType, String name) {
            super(substitutionOffset);
            this.elementHandle = ElementHandle.create(elem);
            this.isDeprecated = isDeprecated;
            this.smartType = smartType;
            this.simpleName = name != null ? name : elem.getEnclosingElement().getSimpleName().toString();
            this.insertName = name != null;
            this.modifiers = elem.getModifiers();
            this.params = new ArrayList<ParamDesc>();
            Iterator<? extends VariableElement> it = elem.getParameters().iterator();
            Iterator<? extends TypeMirror> tIt = type.getParameterTypes().iterator();
            while(it.hasNext() && tIt.hasNext()) {
                TypeMirror tm = tIt.next();
                if (tm == null)
                    break;
                this.params.add(new ParamDesc(tm.toString(), Utilities.getTypeName(tm, false, elem.isVarArgs() && !tIt.hasNext()).toString(), it.next().getSimpleName().toString()));
            }
            this.isAbstract = !insertName && elem.getEnclosingElement().getModifiers().contains(Modifier.ABSTRACT);
        }
        
        public int getSortPriority() {
            return insertName ? 550 : smartType ? 650 - SMART_TYPE : 650;
        }
        
        public CharSequence getSortText() {
            if (sortText == null) {
                StringBuilder sortParams = new StringBuilder();
                sortParams.append('(');
                int cnt = 0;
                for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                    ParamDesc paramDesc = it.next();
                    sortParams.append(paramDesc.typeName);
                    if (it.hasNext()) {
                        sortParams.append(',');
                    }
                    cnt++;
                }
                sortParams.append(')');
                sortText = simpleName + "#" + ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString(); //NOI18N
            }
            return sortText;
        }
        
        public CharSequence getInsertPrefix() {
            return simpleName;
        }        
        
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder lText = new StringBuilder();
                lText.append(CONSTRUCTOR_COLOR);
                lText.append(BOLD);
                if (isDeprecated)
                    lText.append(STRIKE);
                lText.append(simpleName);
                if (isDeprecated)
                    lText.append(STRIKE_END);
                lText.append(BOLD_END);
                lText.append(COLOR_END);
                lText.append('(');
                for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                    ParamDesc paramDesc = it.next();
                    lText.append(escape(paramDesc.typeName));
                    lText.append(' ');
                    lText.append(PARAMETER_NAME_COLOR);
                    lText.append(paramDesc.name);
                    lText.append(COLOR_END);
                    if (it.hasNext()) {
                        lText.append(", "); //NOI18N
                    }
                }
                lText.append(')');
                leftText = lText.toString();
            }
            return leftText;
        }
        
        public CompletionTask createDocumentationTask() {
            return JavaCompletionProvider.createDocTask(elementHandle);
        }

        protected ImageIcon getIcon() {
            int level = getProtectionLevel(modifiers);
            ImageIcon cachedIcon = icon[level];
            if (cachedIcon != null)
                return cachedIcon;
            
            String iconPath = CONSTRUCTOR_PUBLIC;            
            switch (level) {
                case PRIVATE_LEVEL:
                    iconPath = CONSTRUCTOR_PRIVATE;
                    break;

                case PACKAGE_LEVEL:
                    iconPath = CONSTRUCTOR_PACKAGE;
                    break;

                case PROTECTED_LEVEL:
                    iconPath = CONSTRUCTOR_PROTECTED;
                    break;

                case PUBLIC_LEVEL:
                    iconPath = CONSTRUCTOR_PUBLIC;
                    break;
            }
            ImageIcon newIcon = ImageUtilities.loadImageIcon(iconPath, false);
            icon[level] = newIcon;
            return newIcon;            
        }
        
        protected void substituteText(final JTextComponent c, int offset, int len, String toAdd) {
            if (!insertName) {
                offset += len;
                len = 0;
            }
            final boolean lpar = "(".equals(toAdd); //NOI18N
            final int semiPos = toAdd != null && toAdd.endsWith(";") ? findPositionForSemicolon(c) : -2; //NOI18N
            if (semiPos > -2)
                toAdd = toAdd.length() > 1 ? toAdd.substring(0, toAdd.length() - 1) : null;
            final BaseDocument doc = (BaseDocument)c.getDocument();
            TokenSequence<JavaTokenId> sequence = SourceUtils.getJavaTokenSequence(TokenHierarchy.get(doc), offset);
            if (sequence == null || !sequence.moveNext() && !sequence.movePrevious()) {
                sequence.movePrevious();
                if (sequence.token().id() == JavaTokenId.THIS || sequence.token().id() == JavaTokenId.SUPER) {
                    isAbstract = false;
                    if (toAdd == null)
                        toAdd = ";"; //NOI18N
                }
                sequence.moveNext();
            }
            String add = isAbstract ? "() {}" : "()"; //NOI18N
            if (toAdd != null && !add.startsWith(toAdd)) {
                add += toAdd;
            } else {
                toAdd = null;
            }
            String text = CodeStyle.getDefault(doc).spaceBeforeMethodCallParen() ? " " : ""; //NOI18N
            if (sequence == null) {
                text += add;
                add = null;
            }
            boolean added = false;
            while(add != null && add.length() > 0) {
                String tokenText = sequence.token().text().toString();
                if (tokenText.startsWith(add)) {
                    len = sequence.offset() - offset + add.length();
                    text += add;
                    add = null;
                } else if (add.startsWith(tokenText)) {
                    sequence.moveNext();
                    len = sequence.offset() - offset;
                    text += add.substring(0, tokenText.length());
                    add = add.substring(tokenText.length());
                    added = true;
                } else if (sequence.token().id() == JavaTokenId.WHITESPACE && sequence.token().text().toString().indexOf('\n') < 0) {//NOI18N
                    if (!sequence.moveNext()) {
                        text += add;
                        add = null;
                    }
                } else {
                    if (!added)
                        text += add;
                    add = null;
                }
            }
            final String text2 = text;
            final int length = len;
            final int offset2 = offset;
            final Position[] position = new Position [1];
            doc.runAtomic (new Runnable () {
                public void run () {
                    try {
                        Position semiPosition = semiPos > -1 ? doc.createPosition(semiPos) : null;
                        Position pos = doc.createPosition(offset2);
                        doc.remove(offset2, length);
                        if (insertName) {
                            doc.insertString(pos.getOffset(), simpleName + text2, null);
                            position [0] = doc.createPosition(pos.getOffset() - text2.length() + text2.indexOf('('));
                        } else {
                            doc.insertString(pos.getOffset(), text2, null);
                            position [0] = doc.createPosition(pos.getOffset() - text2.length() + text2.indexOf('('));
                        }
                        if (semiPosition != null)
                            doc.insertString(semiPosition.getOffset(), ";", null); //NOI18N
                        else if (!isAbstract && params.isEmpty() && lpar)
                            c.setCaretPosition(c.getCaretPosition() - 1);
                    } catch (BadLocationException e) {
                    }
                }
            });
            if (isAbstract && text.length() > 3) {
                try {
                    final int off = offset + text.indexOf('{') + 1;
                    ModificationResult mr = ModificationResult.runModificationTask(Collections.singletonList(Source.create(doc)), new UserTask() {
                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            WorkingCopy copy = WorkingCopy.get(resultIterator.getParserResult());
                            copy.toPhase(Phase.RESOLVED);
                            final int embeddedOffset = copy.getSnapshot().getEmbeddedOffset(off);
                            TreePath path = copy.getTreeUtilities().pathFor(embeddedOffset);
                            while (path.getLeaf() != path.getCompilationUnit()) {
                                Tree tree = path.getLeaf();
                                Tree parentTree = path.getParentPath().getLeaf();                                
                                if (parentTree.getKind() == Tree.Kind.NEW_CLASS && tree.getKind() == Tree.Kind.CLASS) {
                                    GeneratorUtils.generateAllAbstractMethodImplementations(copy, path);
                                    break;
                                }                                
                                path = path.getParentPath();
                            }
                        }
                    });
                    GeneratorUtils.guardedCommit(c, mr);
                } catch (Exception ex) {
                    LOGGER.log(Level.FINE, null, ex);
                }
            }
            if (!params.isEmpty() && text.trim().length() > 1) {
                CodeTemplateManager ctm = CodeTemplateManager.get(doc);
                if (ctm != null) {
                    if (position [0] != null)
                        offset = position [0].getOffset();
                    if (toAdd == null)
                        toAdd = ""; //NOI18N
                    if (text.startsWith("()" + toAdd)) //NOI18N
                        c.select(offset, offset + toAdd.length() + 2);
                    else if (text.startsWith("()")) //NOI18N
                        c.select(offset, offset + 2);
                    else
                        c.setCaretPosition(offset);
                    StringBuilder sb = new StringBuilder();
                    boolean guessArgs = Utilities.guessMethodArguments();
                    sb.append("("); //NOI18N
                    for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                        ParamDesc paramDesc = it.next();
                        sb.append("${"); //NOI18N
                        sb.append(paramDesc.name);
                        if (guessArgs) {
                            sb.append(" named instanceof="); //NOI18N
                            sb.append(paramDesc.fullTypeName);
                        }
                        sb.append("}"); //NOI18N
                        if (it.hasNext())
                            sb.append(", "); //NOI18N
                    }
                    sb.append(")"); //NOI18N
                    sb.append(toAdd);
                    ctm.createTemporary(sb.toString()).insert(c);
                    Completion.get().showToolTip();
                }
            }
        }        

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Modifier mod : modifiers) {
                sb.append(mod.toString());
                sb.append(' ');
            }
            sb.append(simpleName);
            sb.append('('); //NOI18N
            for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                ParamDesc paramDesc = it.next();
                sb.append(paramDesc.typeName);
                sb.append(' ');
                sb.append(paramDesc.name);
                if (it.hasNext()) {
                    sb.append(", "); //NOI18N
                }
            }
            sb.append(')');
            return sb.toString();
        }
    }
    
    static class DefaultConstructorItem extends JavaCompletionItem {
        
        private static final String CONSTRUCTOR = "org/netbeans/modules/java/editor/resources/new_constructor_16.png"; //NOI18N
        private static final String CONSTRUCTOR_COLOR = "<font color=#b28b00>"; //NOI18N
        private static ImageIcon icon;        
        
        private boolean smartType;
        private String simpleName;
        private boolean isAbstract;
        private String sortText;
        private String leftText;

        private DefaultConstructorItem(TypeElement elem, int substitutionOffset, boolean smartType) {
            super(substitutionOffset);
            this.smartType = smartType;
            this.simpleName = elem.getSimpleName().toString();
            this.isAbstract = elem.getModifiers().contains(Modifier.ABSTRACT);
        }

        public CharSequence getInsertPrefix() {
            return simpleName;
        }

        public int getSortPriority() {
            return smartType ? 650 - SMART_TYPE : 650;
        }

        public CharSequence getSortText() {
            if (sortText == null)
                sortText = simpleName + "#0#"; //NOI18N
            return sortText;
        }
        
        protected String getLeftHtmlText() {
            if (leftText == null)
                leftText = CONSTRUCTOR_COLOR + simpleName + "()" + COLOR_END; //NOI18N
            return leftText;
        }        

        protected ImageIcon getIcon() {
            if (icon == null) icon = ImageUtilities.loadImageIcon(CONSTRUCTOR, false);
            return icon;            
        }

        protected void substituteText(final JTextComponent c, final int offset, int len, String toAdd) {
            final boolean lpar = "(".equals(toAdd); //NOI18N
            final int[] offset2 = new int[] {offset + len};
            len = 0;
            final int semiPos = toAdd != null && toAdd.endsWith(";") ? findPositionForSemicolon(c) : -2; //NOI18N
            if (semiPos > -2)
                toAdd = toAdd.length() > 1 ? toAdd.substring(0, toAdd.length() - 1) : null;
            final BaseDocument doc = (BaseDocument) c.getDocument();
            TokenSequence<JavaTokenId> sequence = SourceUtils.getJavaTokenSequence(TokenHierarchy.get(doc), offset2 [0]);
            if (sequence == null || !sequence.moveNext() && !sequence.movePrevious()) {
                sequence.movePrevious();
                if (sequence.token().id() == JavaTokenId.THIS || sequence.token().id() == JavaTokenId.SUPER) {
                    isAbstract = false;
                    if (toAdd == null)
                        toAdd = ";"; //NOI18N
                }
                sequence.moveNext();
            }
            String add = isAbstract ? "() {}" : "()"; //NOI18N
            if (toAdd != null && !add.startsWith(toAdd))
                add += toAdd;
            String text = CodeStyle.getDefault(doc).spaceBeforeMethodCallParen() ? " " : ""; //NOI18N
            if (sequence == null) {
                text += add;
                add = null;
            }
            boolean added = false;
            while(add != null && add.length() > 0) {
                String tokenText = sequence.token().text().toString();
                if (tokenText.startsWith(add)) {
                    len = sequence.offset() - offset2 [0] + add.length();
                    text += add;
                    add = null;
                } else if (add.startsWith(tokenText)) {
                    sequence.moveNext();
                    len = sequence.offset() - offset2 [0];
                    text += add.substring(0, tokenText.length());
                    add = add.substring(tokenText.length());
                    added = true;
                } else if (sequence.token().id() == JavaTokenId.WHITESPACE && sequence.token().text().toString().indexOf('\n') < 0) {//NOI18N
                    if (!sequence.moveNext()) {
                        text += add;
                        add = null;
                    }
                } else {
                    if (!added)
                        text += add;
                    add = null;
                }
            }
            final Position[] position = new Position [1];
            final String text2 = text;
            final int length = len;
            doc.runAtomic (new Runnable () {
                public void run () {
                    try {
                        position [0] = doc.createPosition(offset2 [0]);
                        Position semiPosition = semiPos > -1 ? doc.createPosition(semiPos) : null;
                        doc.remove(offset2 [0], length);
                        offset2 [0] = position [0].getOffset();
                        doc.insertString(offset2 [0], text2, null);
                        position [0] = doc.createPosition(offset2 [0]);
                        if (semiPosition != null)
                            doc.insertString(semiPosition.getOffset(), ";", null); //NOI18N
                        else if (!isAbstract && lpar)
                            c.setCaretPosition(c.getCaretPosition() - 1);
                    } catch (BadLocationException e) {
                    }
                }
            });
            if (isAbstract && text.trim().length() > 3) {
                try {
                    if (position [0] != null)
                        offset2 [0] = position [0].getOffset();
                    final int off = offset2 [0] + text.indexOf('{') + 1;
                    ModificationResult mr = ModificationResult.runModificationTask(Collections.singletonList(Source.create(doc)), new UserTask() {
                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            WorkingCopy copy = WorkingCopy.get(resultIterator.getParserResult());
                            copy.toPhase(Phase.RESOLVED);
                            final int embeddedOffset = copy.getSnapshot().getEmbeddedOffset(off);
                            TreePath path = copy.getTreeUtilities().pathFor(embeddedOffset);
                            while (path.getLeaf() != path.getCompilationUnit()) {
                                Tree tree = path.getLeaf();
                                Tree parentTree = path.getParentPath().getLeaf();                                
                                if (parentTree.getKind() == Tree.Kind.NEW_CLASS && tree.getKind() == Tree.Kind.CLASS) {
                                    GeneratorUtils.generateAllAbstractMethodImplementations(copy, path);
                                    break;
                                }                                
                                path = path.getParentPath();
                            }
                        }
                    });
                    GeneratorUtils.guardedCommit(c, mr);
                } catch (Exception ex) {
                    LOGGER.log(Level.FINE, null, ex);
                }
            }
        }
        
        public String toString() {
            return simpleName + "()";
        }        
    }
    
    static class ParametersItem extends JavaCompletionItem {
        
        private static final String PARAMETERS_COLOR = "<font color=#808080>"; //NOI18N
        private static final String ACTIVE_PARAMETER_COLOR = "<font color=#000000>"; //NOI18N

        protected ElementHandle<ExecutableElement> elementHandle;
        private boolean isDeprecated;
        private int activeParamsIndex;
        private String simpleName;
        private ArrayList<ParamDesc> params;
        private String typeName;
        private String sortText;
        private String leftText;
        private String rightText;

        private ParametersItem(ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, int activeParamsIndex, String name) {
            super(substitutionOffset);
            this.elementHandle = ElementHandle.create(elem);
            this.isDeprecated = isDeprecated;
            this.activeParamsIndex = activeParamsIndex;
            this.simpleName = name != null ? name : elem.getKind() == ElementKind.CONSTRUCTOR ? elem.getEnclosingElement().getSimpleName().toString() : elem.getSimpleName().toString();
            this.params = new ArrayList<ParamDesc>();
            Iterator<? extends VariableElement> it = elem.getParameters().iterator();
            Iterator<? extends TypeMirror> tIt = type.getParameterTypes().iterator();
            while(it.hasNext() && tIt.hasNext()) {
                TypeMirror tm = tIt.next();
                if (tm == null)
                    break;
                this.params.add(new ParamDesc(tm.toString(), Utilities.getTypeName(tm, false, elem.isVarArgs() && !tIt.hasNext()).toString(), it.next().getSimpleName().toString()));
            }
            TypeMirror retType = type.getReturnType();
            this.typeName = Utilities.getTypeName(retType, false).toString();
        }

        public int getSortPriority() {
            return 100 - SMART_TYPE;
        }

        public CharSequence getSortText() {
            if (sortText == null) {
                StringBuilder sortParams = new StringBuilder();
                sortParams.append('(');
                int cnt = 0;
                for(Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                    ParamDesc param = it.next();
                    sortParams.append(param.typeName);
                    if (it.hasNext()) {
                        sortParams.append(',');
                    }
                    cnt++;
                }
                sortParams.append(')');
                sortText = "#" + ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString(); //NOI18N
            }
            return sortText;
        }

        public CharSequence getInsertPrefix() {
            return ""; //NOI18N
        }
        
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder lText = new StringBuilder();
                lText.append(PARAMETERS_COLOR);
                if (isDeprecated)
                    lText.append(STRIKE);
                lText.append(simpleName);
                if (isDeprecated)
                    lText.append(STRIKE_END);
                lText.append('(');
                for (int i = 0; i < params.size(); i++) {
                    ParamDesc paramDesc = params.get(i);
                    if (i == activeParamsIndex)
                        lText.append(COLOR_END).append(ACTIVE_PARAMETER_COLOR).append(BOLD);
                    lText.append(escape(paramDesc.typeName));
                    lText.append(' ');
                    lText.append(paramDesc.name);
                    if (i < params.size() - 1)
                        lText.append(", "); //NOI18N
                    else
                        lText.append(BOLD_END).append(COLOR_END).append(PARAMETERS_COLOR);                        
                }
                lText.append(')');
                lText.append(COLOR_END);
                return lText.toString();
            }
            return leftText;
        }
        
        protected String getRightHtmlText() {
            if (rightText == null)
                rightText = PARAMETERS_COLOR + escape(typeName) + COLOR_END;
            return rightText;
        }
        
        public CompletionTask createDocumentationTask() {
            return JavaCompletionProvider.createDocTask(elementHandle);
        }

        public boolean instantSubstitution(JTextComponent component) {
            return false;
        }
        
        protected void substituteText(final JTextComponent c, final int offset, int len, String toAdd) {
            String add = ")"; //NOI18N
            if (toAdd != null && !add.startsWith(toAdd))
                add += toAdd;
            if (params.isEmpty()) {
                super.substituteText(c, offset, len, add);
            } else {                
                final BaseDocument doc = (BaseDocument)c.getDocument();
                String text = ""; //NOI18N
                final int semiPos = add.endsWith(";") ? findPositionForSemicolon(c) : -2; //NOI18N
                if (semiPos > -2)
                    add = add.length() > 1 ? add.substring(0, add.length() - 1) : null;
                TokenSequence<JavaTokenId> sequence = SourceUtils.getJavaTokenSequence(TokenHierarchy.get(doc), offset + len);
                if (sequence == null || !sequence.moveNext() && !sequence.movePrevious()) {
                    text += add;
                    add = null;
                }
                boolean added = false;
                while(add != null && add.length() > 0) {
                    String tokenText = sequence.token().text().toString();
                    if (tokenText.startsWith(add)) {
                        len = sequence.offset() - offset + add.length();
                        text += add;
                        add = null;
                    } else if (add.startsWith(tokenText)) {
                        sequence.moveNext();
                        len = sequence.offset() - offset;
                        text += add.substring(0, tokenText.length());
                        add = add.substring(tokenText.length());
                        added = true;
                    } else if (sequence.token().id() == JavaTokenId.WHITESPACE && sequence.token().text().toString().indexOf('\n') < 0) {//NOI18N
                        if (!sequence.moveNext()) {
                            text += add;
                            add = null;
                        }
                    } else {
                        if (!added)
                            text += add;
                        add = null;
                    }
                }
                final int length = len;
                doc.runAtomic (new Runnable () {
                    public void run () {
                        try {
                            Position semiPosition = semiPos > -1 ? doc.createPosition(semiPos) : null;
                            if (length > 0)
                                doc.remove(offset, length);
                            if (semiPosition != null)
                                doc.insertString(semiPosition.getOffset(), ";", null); //NOI18N
                        } catch (BadLocationException e) {
                            // Can't update
                        }
                    }
                });
                CodeTemplateManager ctm = CodeTemplateManager.get(doc);
                if (ctm != null) {
                    StringBuilder sb = new StringBuilder();
                    boolean guessArgs = Utilities.guessMethodArguments();
                    for (int i = activeParamsIndex; i < params.size(); i++) {
                        ParamDesc paramDesc = params.get(i);
                        sb.append("${"); //NOI18N
                        sb.append(paramDesc.name);
                        if (guessArgs) {
                            sb.append(" named instanceof="); //NOI18N
                            sb.append(paramDesc.fullTypeName);
                        }
                        sb.append("}"); //NOI18N
                        if (i < params.size() - 1)
                            sb.append(", "); //NOI18N
                    }
                    if (text.length() > 0)
                        sb.append(text);
                    ctm.createTemporary(sb.toString()).insert(c);
                    Completion.get().showToolTip();
                }
            }
        }        

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(typeName);
            sb.append(' ');
            sb.append(simpleName);
            sb.append('(');
            for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                ParamDesc paramDesc = it.next();
                sb.append(paramDesc.typeName);
                sb.append(' ');
                sb.append(paramDesc.name);
                if (it.hasNext()) {
                    sb.append(", "); //NOI18N
                }
            }
            sb.append(") - parameters"); //NOI18N
            return sb.toString();
        }
    }
    
    static class AnnotationItem extends AnnotationTypeItem {
        
        private AnnotationItem(TypeElement elem, DeclaredType type, int substitutionOffset, boolean isDeprecated, boolean smartType) {
            super(elem, type, 0, substitutionOffset, true, isDeprecated, false, smartType);
        }

        public CharSequence getInsertPrefix() {
            return "@" + super.getInsertPrefix(); //NOI18N
        }

        protected void substituteText(final JTextComponent c, final int offset, int len, String toAdd) {
            final BaseDocument doc = (BaseDocument)c.getDocument();
            final StringBuilder text = new StringBuilder();
            final int semiPos = toAdd != null && toAdd.endsWith(";") ? findPositionForSemicolon(c) : -2; //NOI18N
            if (semiPos > -2)
                toAdd = toAdd.length() > 1 ? toAdd.substring(0, toAdd.length() - 1) : null;
            if (toAdd != null && !toAdd.equals("\n")) {//NOI18N
                TokenSequence<JavaTokenId> sequence = SourceUtils.getJavaTokenSequence(TokenHierarchy.get(doc), offset + len);
                if (sequence == null || !sequence.moveNext() && !sequence.movePrevious()) {
                    text.append(toAdd);
                    toAdd = null;
                }
                boolean added = false;
                while(toAdd != null && toAdd.length() > 0) {
                    String tokenText = sequence.token().text().toString();
                    if (tokenText.startsWith(toAdd)) {
                        len = sequence.offset() - offset + toAdd.length();
                        text.append(toAdd);
                        toAdd = null;
                    } else if (toAdd.startsWith(tokenText)) {
                        sequence.moveNext();
                        len = sequence.offset() - offset;
                        text.append(toAdd.substring(0, tokenText.length()));
                        toAdd = toAdd.substring(tokenText.length());
                        added = true;
                    } else if (sequence.token().id() == JavaTokenId.WHITESPACE && sequence.token().text().toString().indexOf('\n') < 0) {//NOI18N
                        if (!sequence.moveNext()) {
                            text.append(toAdd);
                            toAdd = null;
                        }
                    } else {
                        if (!added)
                            text.append(toAdd);
                        toAdd = null;
                    }
                }
            }
            final int finalLen = len;
            Source s = Source.create(doc);
            try {
                ParserManager.parse(Collections.singletonList(s), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        final CompilationController controller = CompilationController.get(resultIterator.getParserResult(offset));
                        controller.toPhase(Phase.RESOLVED);
                        final int embeddedOffset = controller.getSnapshot().getEmbeddedOffset(offset);
                        final DeclaredType type = typeHandle.resolve(controller);
                        // Update the text
                        doc.runAtomic (new Runnable () {
                            public void run () {
                                try {
                                    Position semiPosition = semiPos > -1 ? doc.createPosition(semiPos) : null;
                                    TreePath tp = controller.getTreeUtilities().pathFor(embeddedOffset);
                                    text.insert(0, "@" + AutoImport.resolveImport(controller, tp, type)); //NOI18N
                                    String textToReplace = doc.getText(offset, finalLen);
                                    if (textToReplace.contentEquals(text)) return;
                                    Position pos = doc.createPosition(offset);
                                    doc.remove(pos.getOffset(), finalLen);
                                    doc.insertString(pos.getOffset(), text.toString(), null);
                                    if (semiPosition != null)
                                        doc.insertString(semiPosition.getOffset(), ";", null); //NOI18N
                                } catch (BadLocationException e) {
                                    // Can't update
                                }
                            }
                        });
                    }
                });
            } catch (ParseException pe) {
            }
        }
    }
    
    static class AttributeItem extends JavaCompletionItem {
        
        private static final String ATTRIBUTE = "org/netbeans/modules/java/editor/resources/attribute_16.png"; // NOI18N
        private static final String ATTRIBUTE_COLOR = "<font color=#404040>"; //NOI18N
        private static ImageIcon icon;
        
        private ElementHandle<ExecutableElement> elementHandle;
        private boolean isDeprecated;
        private String simpleName;
        private String typeName;
        private String defaultValue;
        private String leftText;
        private String rightText;

        private AttributeItem(ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated) {
            super(substitutionOffset);
            this.elementHandle = ElementHandle.create(elem);
            this.isDeprecated = isDeprecated;
            this.simpleName = elem.getSimpleName().toString();
            this.typeName = Utilities.getTypeName(type.getReturnType(), false).toString();
            AnnotationValue value = elem.getDefaultValue();
            this.defaultValue = value != null ? value.toString() : null;
        }
        
        public int getSortPriority() {
            return 100;
        }
        
        public CharSequence getSortText() {
            return simpleName;
        }
        
        public CharSequence getInsertPrefix() {
            return simpleName;
        }

        public CompletionTask createDocumentationTask() {
            return JavaCompletionProvider.createDocTask(elementHandle);
        }

        protected ImageIcon getIcon(){
            if (icon == null) icon = ImageUtilities.loadImageIcon(ATTRIBUTE, false);
            return icon;            
        }
        
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(ATTRIBUTE_COLOR);
                if (defaultValue == null)
                    sb.append(BOLD);
                if (isDeprecated)
                    sb.append(STRIKE);
                sb.append(simpleName);
                if (isDeprecated)
                    sb.append(STRIKE_END);
                if (defaultValue == null) {
                    sb.append(BOLD_END);
                } else {
                    sb.append(" = "); //NOI18N
                    sb.append(defaultValue);
                }
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
        }
        
        protected String getRightHtmlText() {
            if (rightText == null)
                rightText = escape(typeName);
            return rightText;
        }
        
        protected void substituteText(JTextComponent c, int offset, int len, String toAdd) {
            super.substituteText(c, offset, len, toAdd != null ? toAdd : "="); //NOI18N
        }

        public String toString() {
            return simpleName;
        }        
    }

    static class StaticMemberItem extends JavaCompletionItem {
        
        private static final String FIELD_ST_PUBLIC = "org/netbeans/modules/editor/resources/completion/field_static_16.png"; //NOI18N
        private static final String FIELD_ST_PROTECTED = "org/netbeans/modules/editor/resources/completion/field_static_protected_16.png"; //NOI18N
        private static final String FIELD_ST_PACKAGE = "org/netbeans/modules/editor/resources/completion/field_static_package_private_16.png"; //NOI18N
        private static final String FIELD_COLOR = "<font color=#0000b2>"; //NOI18N
        private static final String METHOD_ST_PUBLIC = "org/netbeans/modules/editor/resources/completion/method_static_16.png"; //NOI18N
        private static final String METHOD_ST_PROTECTED = "org/netbeans/modules/editor/resources/completion/method_static_protected_16.png"; //NOI18N
        private static final String METHOD_ST_PACKAGE = "org/netbeans/modules/editor/resources/completion/method_static_package_private_16.png"; //NOI18N
        private static final String METHOD_COLOR = "<font color=#7c0000>"; //NOI18N
        private static final String PARAMETER_NAME_COLOR = "<font color=#b200b2>"; //NOI18N
        private static ImageIcon icon[][] = new ImageIcon[2][3];
        
        private TypeMirrorHandle<DeclaredType> typeHandle;
        private ElementHandle<Element> memberElementHandle;
        private boolean isDeprecated;
        private String typeName;
        private String memberName;
        private String memberTypeName;
        private Set<Modifier> modifiers;
        private List<ParamDesc> params;
        private String sortText;
        private String leftText;
        private String rightText;
        
        private StaticMemberItem(DeclaredType type, Element memberElem, TypeMirror memberType, int substitutionOffset, boolean isDeprecated) {
            super(substitutionOffset);
            this.typeHandle = TypeMirrorHandle.create(type);
            this.memberElementHandle = ElementHandle.create(memberElem);
            this.isDeprecated = isDeprecated;
            this.typeName = Utilities.getTypeName(type, false).toString();
            this.memberName = memberElem.getSimpleName().toString();
            this.memberTypeName = Utilities.getTypeName(memberElem.getKind().isField() ? memberType : ((ExecutableType)memberType).getReturnType(), false).toString();
            this.modifiers = memberElem.getModifiers();
            if (!memberElem.getKind().isField()) {
                this.params = new ArrayList<ParamDesc>();
                Iterator<? extends VariableElement> it = ((ExecutableElement)memberElem).getParameters().iterator();
                Iterator<? extends TypeMirror> tIt = ((ExecutableType)memberType).getParameterTypes().iterator();
                while(it.hasNext() && tIt.hasNext()) {
                    TypeMirror tm = tIt.next();
                    this.params.add(new ParamDesc(tm.toString(), Utilities.getTypeName(tm, false, ((ExecutableElement)memberElem).isVarArgs() && !tIt.hasNext()).toString(), it.next().getSimpleName().toString()));
                }
            }
        }
        
        public int getSortPriority() {
            return (params == null ? 700 : 750) - SMART_TYPE;
        }
        
        public CharSequence getSortText() {
            if (sortText == null) {
                if (params == null) {
                    sortText = memberName + "#" + typeName; //NOI18N
                } else {
                    StringBuilder sortParams = new StringBuilder();
                    sortParams.append('(');
                    int cnt = 0;
                    for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                        ParamDesc paramDesc = it.next();
                        sortParams.append(paramDesc.typeName);
                        if (it.hasNext()) {
                            sortParams.append(',');
                        }
                        cnt++;
                    }
                    sortParams.append(')');
                    sortText = memberName + "#" + ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString() + "#" + typeName; //NOI18N
                }
            }
            return sortText;
        }
        
        public CharSequence getInsertPrefix() {
            return typeName + "." + memberName; //NOI18N
        }

        public CompletionTask createDocumentationTask() {
            return JavaCompletionProvider.createDocTask(memberElementHandle);
        }

        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder lText = new StringBuilder();
                lText.append(memberElementHandle.getKind().isField() ? FIELD_COLOR : METHOD_COLOR);
                lText.append(escape(typeName));
                lText.append('.');
                if (isDeprecated)
                    lText.append(STRIKE);
                lText.append(memberName);
                if (isDeprecated)
                    lText.append(STRIKE_END);
                lText.append(COLOR_END);
                if (params != null) {
                    lText.append('(');
                    for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                        ParamDesc paramDesc = it.next();
                        lText.append(escape(paramDesc.typeName));
                        lText.append(' '); //NOI18N
                        lText.append(PARAMETER_NAME_COLOR);
                        lText.append(paramDesc.name);
                        lText.append(COLOR_END);
                        if (it.hasNext()) {
                            lText.append(", "); //NOI18N
                        }
                    }
                    lText.append(')');
                }
                leftText = lText.toString();
            }
            return leftText;
        }
        
        protected String getRightHtmlText() {
            if (rightText == null)
                rightText = escape(memberTypeName);
            return rightText;
        }
        
        protected ImageIcon getIcon(){
            int level = getProtectionLevel(modifiers);
            boolean isField = memberElementHandle.getKind().isField();
            ImageIcon cachedIcon = icon[isField ? 0 : 1][level - 1];
            if (cachedIcon != null)
                return cachedIcon;            

            String iconPath = null;
            if (isField) {
                switch (level) {
                    case PACKAGE_LEVEL:
                        iconPath = FIELD_ST_PACKAGE;
                        break;

                    case PROTECTED_LEVEL:
                        iconPath = FIELD_ST_PROTECTED;
                        break;

                    case PUBLIC_LEVEL:
                        iconPath = FIELD_ST_PUBLIC;
                        break;
                }
            }else{
                switch (level) {
                    case PACKAGE_LEVEL:
                        iconPath = METHOD_ST_PACKAGE;
                        break;

                    case PROTECTED_LEVEL:
                        iconPath = METHOD_ST_PROTECTED;
                        break;

                    case PUBLIC_LEVEL:
                        iconPath = METHOD_ST_PUBLIC;
                        break;
                }
            }
            if (iconPath == null)
                return null;
            ImageIcon newIcon = ImageUtilities.loadImageIcon(iconPath, false);
            icon[isField ? 0 : 1][level - 1] = newIcon;
            return newIcon;            
        }

        protected void substituteText(final JTextComponent c, final int offset, int len, String toAdd) {
            final BaseDocument doc = (BaseDocument)c.getDocument();
            final StringBuilder text = new StringBuilder();
            final int semiPos = toAdd != null && toAdd.endsWith(";") ? findPositionForSemicolon(c) : -2; //NOI18N
            if (semiPos > -2)
                toAdd = toAdd.length() > 1 ? toAdd.substring(0, toAdd.length() - 1) : null;
            if (toAdd != null && !toAdd.equals("\n")) { //NOI18N
                TokenSequence<JavaTokenId> sequence = SourceUtils.getJavaTokenSequence(TokenHierarchy.get(doc), offset + len);
                if (sequence == null || !sequence.moveNext() && !sequence.movePrevious()) {
                    text.append(toAdd);
                    toAdd = null;
                }
                boolean added = false;
                while(toAdd != null && toAdd.length() > 0) {
                    String tokenText = sequence.token().text().toString();
                    if (tokenText.startsWith(toAdd)) {
                        len = sequence.offset() - offset + toAdd.length();
                        text.append(toAdd);
                        toAdd = null;
                    } else if (toAdd.startsWith(tokenText)) {
                        sequence.moveNext();
                        len = sequence.offset() - offset;
                        text.append(toAdd.substring(0, tokenText.length()));
                        toAdd = toAdd.substring(tokenText.length());
                        added = true;
                    } else if (sequence.token().id() == JavaTokenId.WHITESPACE && sequence.token().text().toString().indexOf('\n') < 0) {//NOI18N
                        if (!sequence.moveNext()) {
                            text.append(toAdd);
                            toAdd = null;
                        }
                    } else {
                        if (!added)
                            text.append(toAdd);
                        toAdd = null;
                    }
                }
            }
            final int finalLen = len;
            Source s = Source.create(doc);
            try {
                ParserManager.parse(Collections.singletonList(s), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        final CompilationController controller = CompilationController.get(resultIterator.getParserResult(offset));
                        controller.toPhase(Phase.RESOLVED);
                        DeclaredType type = typeHandle.resolve(controller);
                        StringBuilder sb = new StringBuilder();
                        int cnt = 1;
                        sb.append("${PAR#"); //NOI18N
                        sb.append(cnt++);
                        sb.append(" type=\""); //NOI18N
                        sb.append(((TypeElement)type.asElement()).getQualifiedName());
                        sb.append("\" default=\""); //NOI18N
                        sb.append(((TypeElement)type.asElement()).getSimpleName());
                        sb.append("\" editable=false}"); //NOI18N
                        Iterator<? extends TypeMirror> tas = type.getTypeArguments().iterator();
                        if (tas.hasNext()) {
                            sb.append('<'); //NOI18N
                            while (tas.hasNext()) {
                                TypeMirror ta = tas.next();
                                sb.append("${PAR#"); //NOI18N
                                sb.append(cnt++);
                                if (ta.getKind() == TypeKind.TYPEVAR) {
                                    sb.append(" type=\""); //NOI18N
                                    ta = ((TypeVariable)ta).getUpperBound();
                                    sb.append(Utilities.getTypeName(ta, true));
                                    sb.append("\" default=\""); //NOI18N
                                    sb.append(Utilities.getTypeName(ta, false));
                                    sb.append("\"}"); //NOI18N
                                } else if (ta.getKind() == TypeKind.WILDCARD) {
                                    sb.append(" type=\""); //NOI18N
                                    TypeMirror bound = ((WildcardType)ta).getExtendsBound();
                                    if (bound == null)
                                        bound = ((WildcardType)ta).getSuperBound();
                                    sb.append(bound != null ? Utilities.getTypeName(bound, true) : "Object"); //NOI18N
                                    sb.append("\" default=\""); //NOI18N
                                    sb.append(bound != null ? Utilities.getTypeName(bound, false) : "Object"); //NOI18N
                                    sb.append("\"}"); //NOI18N
                                } else if (ta.getKind() == TypeKind.ERROR) {
                                    sb.append(" default=\""); //NOI18N
                                    sb.append(((ErrorType)ta).asElement().getSimpleName());
                                    sb.append("\"}"); //NOI18N
                                } else {
                                    sb.append(" type=\""); //NOI18N
                                    sb.append(Utilities.getTypeName(ta, true));
                                    sb.append("\" default=\""); //NOI18N
                                    sb.append(Utilities.getTypeName(ta, false));
                                    sb.append("\" editable=false}"); //NOI18N
                                }
                                if (tas.hasNext())
                                    sb.append(", "); //NOI18N
                            }
                            sb.append('>'); //NOI18N
                        }
                        sb.append('.'); //NOI18N
                        sb.append(memberName);
                        if (params != null) {
                            boolean guessArgs = Utilities.guessMethodArguments();
                            sb.append("("); //NOI18N
                            for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                                ParamDesc paramDesc = it.next();
                                sb.append("${"); //NOI18N
                                sb.append(paramDesc.name);
                                if (guessArgs) {
                                    sb.append(" named instanceof="); //NOI18N
                                    sb.append(paramDesc.fullTypeName);
                                }
                                sb.append("}"); //NOI18N
                                if (it.hasNext())
                                    sb.append(", "); //NOI18N
                            }
                            sb.append(")");//NOI18N
                        }
                        sb.append(text);
                        doc.runAtomic (new Runnable () {
                            public void run () {
                                try {
                                    Position semiPosition = semiPos > -1 ? doc.createPosition(semiPos) : null;
                                    if (finalLen > 0)
                                        doc.remove(offset, finalLen);
                                    if (semiPosition != null)
                                        doc.insertString(semiPosition.getOffset(), ";", null); //NOI18N
                                } catch (BadLocationException e) {
                                    // Can't update
                                }
                            }
                        });
                        CodeTemplateManager ctm = CodeTemplateManager.get(doc);
                        if (ctm != null) {
                            ctm.createTemporary(sb.toString()).insert(c);
                        }
                    }
                });
            } catch (ParseException pe) {
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for(Modifier mod : modifiers) {
               sb.append(mod.toString());
               sb.append(' '); // NOI18N
            }
            sb.append(memberTypeName);
            sb.append(' ');
            sb.append(typeName);
            sb.append('.');
            sb.append(memberName);
            if (params != null) {
                sb.append('('); //NOI18N
                for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                    ParamDesc paramDesc = it.next();
                    sb.append(paramDesc.typeName);
                    sb.append(' ');
                    sb.append(paramDesc.name);
                    if (it.hasNext()) {
                        sb.append(", "); //NOI18N
                    }
                }
                sb.append(')');
            }
            return sb.toString();
        }
    }
    
    static class InitializeAllConstructorItem extends JavaCompletionItem {
        
        private static final String CONSTRUCTOR_PUBLIC = "org/netbeans/modules/java/editor/resources/new_constructor_16.png"; //NOI18N
        private static final String CONSTRUCTOR_COLOR = "<font color=#b28b00>"; //NOI18N
        private static final String PARAMETER_NAME_COLOR = "<font color=#b200b2>"; //NOI18N
        private static ImageIcon icon;
        
        private List<ElementHandle<VariableElement>> fieldHandles;
        private ElementHandle<TypeElement> parentHandle;
        private String simpleName;
        private List<ParamDesc> params;
        private String sortText;
        private String leftText;
        
        private InitializeAllConstructorItem(Iterable<? extends VariableElement> fields, TypeElement parent, int substitutionOffset) {
            super(substitutionOffset);
            this.fieldHandles = new ArrayList<ElementHandle<VariableElement>>();
            this.parentHandle = ElementHandle.create(parent);
            this.params = new ArrayList<ParamDesc>();
            for (VariableElement ve : fields) {
                this.fieldHandles.add(ElementHandle.create(ve));
                this.params.add(new ParamDesc(null, Utilities.getTypeName(ve.asType(), false).toString(), ve.getSimpleName().toString()));
            }
            this.simpleName = parent.getSimpleName().toString();
        }
        
        public int getSortPriority() {
            return 400;
        }
        
        public CharSequence getSortText() {
            if (sortText == null) {
                StringBuilder sortParams = new StringBuilder();
                sortParams.append('('); //NOI18N
                int cnt = 0;
                for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                    ParamDesc paramDesc = it.next();
                    sortParams.append(paramDesc.typeName);
                    if (it.hasNext()) {
                        sortParams.append(','); //NOI18N
                    }
                    cnt++;
                }
                sortParams.append(')'); //NOI18N
                sortText = simpleName + "#" + ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString(); //NOI18N
            }
            return sortText;
        }
        
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder lText = new StringBuilder();
                lText.append(CONSTRUCTOR_COLOR);
                lText.append(simpleName);
                lText.append(COLOR_END);
                lText.append('('); //NOI18N
                for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                    ParamDesc paramDesc = it.next();
                    lText.append(escape(paramDesc.typeName));
                    lText.append(' '); //NOI18N
                    lText.append(PARAMETER_NAME_COLOR);
                    lText.append(paramDesc.name);
                    lText.append(COLOR_END);
                    if (it.hasNext()) {
                        lText.append(", "); //NOI18N
                    }
                }
                lText.append(") - "); //NOI18N
                lText.append(GENERATE_TEXT);
                leftText = lText.toString();
            }
            return leftText;
        }
        
        protected ImageIcon getIcon() {
            if (icon == null) 
                icon = ImageUtilities.loadImageIcon(CONSTRUCTOR_PUBLIC, false);
            return icon;            
        }
        
        public CharSequence getInsertPrefix() {
            return simpleName;
        }        
        
        protected void substituteText(final JTextComponent c, final int offset, final int len, String toAdd) {
            final BaseDocument doc = (BaseDocument)c.getDocument();
            if (len > 0) {
                doc.runAtomic (new Runnable () {
                    public void run () {
                        try {
                            doc.remove(offset, len);
                        } catch (BadLocationException e) {
                            // Can't update
                        }
                    }
                });
            }
            try {
                ModificationResult mr = ModificationResult.runModificationTask(Collections.singletonList(Source.create(doc)), new UserTask() {
                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        WorkingCopy copy = WorkingCopy.get(resultIterator.getParserResult());
                        copy.toPhase(Phase.ELEMENTS_RESOLVED);
                        final int embeddedOffset = copy.getSnapshot().getEmbeddedOffset(offset);
                        TreePath tp = copy.getTreeUtilities().pathFor(embeddedOffset);
                        if (tp.getLeaf().getKind() == Tree.Kind.CLASS) {
                            TypeElement parent = parentHandle.resolve(copy);
                            ArrayList<VariableElement> fieldElements = new ArrayList<VariableElement>();
                            for (ElementHandle<? extends Element> handle : fieldHandles)
                                fieldElements.add((VariableElement)handle.resolve(copy));
                            int idx = 0;
                            for (Tree tree : ((ClassTree)tp.getLeaf()).getMembers()) {
                                if (copy.getTrees().getSourcePositions().getStartPosition(tp.getCompilationUnit(), tree) < embeddedOffset)
                                    idx++;
                                else
                                    break;
                            }
                            
                            TreeMaker make = copy.getTreeMaker();
                            ClassTree clazz = (ClassTree) tp.getLeaf();
                            GeneratorUtilities gu = GeneratorUtilities.get(copy);
                            ClassTree decl = make.insertClassMember(clazz, idx, gu.createConstructor(parent, fieldElements, null)); //NOI18N
                            
                            copy.rewrite(clazz, decl);
                        }
                    }
                });
                GeneratorUtils.guardedCommit(c, mr);
            } catch (Exception ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("public "); //NOI18N
            sb.append(simpleName);
            sb.append('('); //NOI18N
            for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                ParamDesc paramDesc = it.next();
                sb.append(paramDesc.typeName);
                sb.append(' '); //NOI18N
                sb.append(paramDesc.name);
                if (it.hasNext()) {
                    sb.append(", "); //NOI18N
                }
            }
            sb.append(") - "); //NOI18N
            sb.append(GENERATE_TEXT);
            return sb.toString();
        }
        
        public boolean instantSubstitution(JTextComponent component) {
            return false; //no instant substitution for create constructor item
        }
    }

    private static final int PUBLIC_LEVEL = 3;
    private static final int PROTECTED_LEVEL = 2;
    private static final int PACKAGE_LEVEL = 1;
    private static final int PRIVATE_LEVEL = 0;
    
    private static int getProtectionLevel(Set<Modifier> modifiers) {
        if(modifiers.contains(Modifier.PUBLIC))
            return PUBLIC_LEVEL;
        if(modifiers.contains(Modifier.PROTECTED))
            return PROTECTED_LEVEL;
        if(modifiers.contains(Modifier.PRIVATE))
            return PRIVATE_LEVEL;
        return PACKAGE_LEVEL;
    }
    
    private static String escape(String s) {
        if (s != null) {
            try {
                return XMLUtil.toAttributeValue(s);
            } catch (Exception ex) {}
        }
        return s;
    }
    
    private static int findPositionForSemicolon(JTextComponent c) {
        final int[] ret = new int[] {-2};
        final int offset = c.getSelectionEnd();
        try {
            Source s = Source.create(c.getDocument());
            ParserManager.parse(Collections.singletonList(s), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    final CompilationController controller = CompilationController.get(resultIterator.getParserResult(offset));
                    controller.toPhase(Phase.PARSED);
                    final int embeddedOffset = controller.getSnapshot().getEmbeddedOffset(offset);
                    Tree t = null;
                    TreePath tp = controller.getTreeUtilities().pathFor(embeddedOffset);
                    while (t == null && tp != null) {
                        switch(tp.getLeaf().getKind()) {
                            case EXPRESSION_STATEMENT:
                            case IMPORT:                                
                                t = tp.getLeaf();
                                break;
                            case RETURN:
                                t = ((ReturnTree)tp.getLeaf()).getExpression();
                                break;
                            case THROW:
                                t = ((ThrowTree)tp.getLeaf()).getExpression();
                                break;
                        }
                        tp = tp.getParentPath();
                    }
                    if (t != null) {
                        SourcePositions sp = controller.getTrees().getSourcePositions();
                        int endPos = (int)sp.getEndPosition(tp.getCompilationUnit(), t);
                        TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(controller, embeddedOffset, endPos);
                        if (ts != null) {
                            ret[0] = ts.token().id() == JavaTokenId.SEMICOLON ? -1 : ts.offset() + ts.token().length();
                        }
                    } else {
                        TokenSequence<JavaTokenId> ts = controller.getTokenHierarchy().tokenSequence(JavaTokenId.language());
                        ts.move(embeddedOffset);
                        if (ts.moveNext() &&  ts.token().id() == JavaTokenId.SEMICOLON)
                            ret[0] = -1;
                    }
                }
            });
        } catch (ParseException ex) {
        }
        return ret[0];
    }
    
    private static TokenSequence<JavaTokenId> findLastNonWhitespaceToken(CompilationController controller, int startPos, int endPos) {
        TokenSequence<JavaTokenId> ts = controller.getTokenHierarchy().tokenSequence(JavaTokenId.language());
        ts.move(endPos);
        while(ts.movePrevious()) {
            int offset = ts.offset();
            if (offset < startPos)
                return null;
            switch (ts.token().id()) {
            case WHITESPACE:
            case LINE_COMMENT:
            case BLOCK_COMMENT:
            case JAVADOC_COMMENT:
                break;
            default:
                return ts;
            }
        }
        return null;
    }
    
    static class ParamDesc {
        private String fullTypeName;
        private String typeName;
        private String name;
    
        public ParamDesc(String fullTypeName, String typeName, String name) {
            this.fullTypeName = fullTypeName;
            this.typeName = typeName;
            this.name = name;
        }
    }
}
