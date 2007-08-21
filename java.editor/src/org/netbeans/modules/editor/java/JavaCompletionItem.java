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

package org.netbeans.modules.editor.java;

import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.swing.ImageIcon;
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
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.xml.XMLUtil;

/**
 *
 * @author Dusan Balek
 */
public abstract class JavaCompletionItem implements CompletionItem {
    
    protected static int SMART_TYPE = 1000;
        
    public static final JavaCompletionItem createKeywordItem(String kwd, String postfix, int substitutionOffset, boolean smartType) {
        return new KeywordItem(kwd, 0, postfix, substitutionOffset, smartType);
    }
    
    public static final JavaCompletionItem createPackageItem(String pkgFQN, int substitutionOffset, boolean isDeprecated) {
        return new PackageItem(pkgFQN, substitutionOffset, isDeprecated);
    }

    public static final JavaCompletionItem createTypeItem(TypeElement elem, DeclaredType type, int substitutionOffset, boolean displayPkgName, boolean isDeprecated, boolean smartType) {
        switch (elem.getKind()) {
            case CLASS:
                return new ClassItem(elem, type, 0, substitutionOffset, displayPkgName, isDeprecated, smartType);
            case INTERFACE:
                return new InterfaceItem(elem, type, 0, substitutionOffset, displayPkgName, isDeprecated, smartType);
            case ENUM:
                return new EnumItem(elem, type, 0, substitutionOffset, displayPkgName, isDeprecated, smartType);
            case ANNOTATION_TYPE:
                return new AnnotationTypeItem(elem, type, 0, substitutionOffset, displayPkgName, isDeprecated, smartType);
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
                    return new ClassItem(elem, dt, dim, substitutionOffset, true, elements.isDeprecated(elem), true);
                case INTERFACE:
                    return new InterfaceItem(elem, dt, dim, substitutionOffset, true, elements.isDeprecated(elem), true);
                case ENUM:
                    return new EnumItem(elem, dt, dim, substitutionOffset, true, elements.isDeprecated(elem), true);
                case ANNOTATION_TYPE:
                    return new AnnotationTypeItem(elem, dt, dim, substitutionOffset, true, elements.isDeprecated(elem), true);
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
                return new ConstructorItem(elem, type, substitutionOffset, isDeprecated, smartType);
            default:
                throw new IllegalArgumentException("kind=" + elem.getKind());
        }
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
            switch (evt.getKeyChar()) {
                case ';':
                case ',':
                case '(':
                    Completion.get().hideDocumentation();
                    Completion.get().hideCompletion();
                case '.':
                    JTextComponent component = (JTextComponent)evt.getSource();
                    int caretOffset = component.getSelectionEnd();
                    substituteText(component, substitutionOffset, caretOffset - substitutionOffset, Character.toString(evt.getKeyChar()));
                    if (evt.getKeyChar() == '.')
                        Completion.get().showCompletion();
                    evt.consume();
                    break;
            }
        }
    }

    public boolean instantSubstitution(JTextComponent component) {
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

    protected void substituteText(JTextComponent c, int offset, int len, String toAdd) {
        BaseDocument doc = (BaseDocument)c.getDocument();
        String text = getInsertPrefix().toString();
        if (text != null) {
            int semiPos = toAdd != null && toAdd.endsWith(";") ? findPositionForSemicolon(c) : -2; //NOI18N
            if (semiPos > -2)
                toAdd = toAdd.length() > 1 ? toAdd.substring(0, toAdd.length() - 1) : null;
            if (toAdd != null && !toAdd.equals("\n")) {//NOI18N
                TokenSequence<JavaTokenId> sequence = SourceUtils.getJavaTokenSequence(TokenHierarchy.get(doc), offset + len);
                if (sequence == null || !sequence.moveNext() && !sequence.movePrevious()) {
                    text += toAdd;
                    toAdd = null;
                }
                boolean added = false;
                while(toAdd != null && toAdd.length() > 0) {
                    String tokenText = sequence.token().text().toString();
                    if (tokenText.startsWith(toAdd)) {
                        len = sequence.offset() - offset + toAdd.length();
                        text += toAdd;
                        toAdd = null;
                    } else if (toAdd.startsWith(tokenText)) {
                        sequence.moveNext();
                        len = sequence.offset() - offset;
                        text += toAdd.substring(0, tokenText.length());
                        toAdd = toAdd.substring(tokenText.length());
                        added = true;
                    } else if (sequence.token().id() == JavaTokenId.WHITESPACE && sequence.token().text().toString().indexOf('\n') < 0) {//NOI18N
                        if (!sequence.moveNext()) {
                            text += toAdd;
                            toAdd = null;
                        }
                    } else {
                        if (!added)
                            text += toAdd;
                        toAdd = null;
                    }
                }
            }
            // Update the text
            doc.atomicLock();
            try {
                String textToReplace = doc.getText(offset, len);
                if (text.equals(textToReplace)) {
                    if (semiPos > -1)
                        doc.insertString(semiPos, ";", null); //NOI18N
                    return;
                }                
                Position position = doc.createPosition(offset);
                Position semiPosition = semiPos > -1 ? doc.createPosition(semiPos) : null;
                doc.remove(offset, len);
                doc.insertString(position.getOffset(), text, null);
                if (semiPosition != null)
                    doc.insertString(semiPosition.getOffset(), ";", null);
            } catch (BadLocationException e) {
                // Can't update
            } finally {
                doc.atomicUnlock();
            }
        }
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
            if (icon == null) icon = new ImageIcon(org.openide.util.Utilities.loadImage(JAVA_KEYWORD));
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
        
        protected void substituteText(JTextComponent c, int offset, int len, String toAdd) {
            if (dim == 0) {
                super.substituteText(c, offset, len, toAdd != null ? toAdd : postfix);
                return;
            }
            BaseDocument doc = (BaseDocument)c.getDocument();
            final StringBuilder text = new StringBuilder();
            int semiPos = toAdd != null && toAdd.endsWith(";") ? findPositionForSemicolon(c) : -2; //NOI18N
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
            doc.atomicLock();
            try {
                Position semiPosition = semiPos > -1 ? doc.createPosition(semiPos) : null;
                if (len > 0)
                    doc.remove(offset, len);
                if (semiPosition != null)
                    doc.insertString(semiPosition.getOffset(), ";", null); //NOI18N
            } catch (BadLocationException e) {
                // Can't update
            } finally {
                doc.atomicUnlock();
            }
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
        
        private boolean isDeprecated;
        private String simpleName;
        private String sortText;
        private String leftText;

        private PackageItem(String pkgFQN, int substitutionOffset, boolean isDeprecated) {
            super(substitutionOffset);
            this.isDeprecated = isDeprecated;
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
            if (icon == null) icon = new ImageIcon(org.openide.util.Utilities.loadImage(PACKAGE));
            return icon;            
        }
        
        protected String getLeftHtmlText() {
            if (leftText == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(PACKAGE_COLOR);
                if (isDeprecated)
                    sb.append(STRIKE);
                sb.append(simpleName);
                if (isDeprecated)
                    sb.append(STRIKE_END);
                sb.append(COLOR_END);
                leftText = sb.toString();
            }
            return leftText;
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
        
        protected ElementHandle<TypeElement> elementHandle;
        private TypeMirrorHandle<DeclaredType> typeHandle;
        private int dim;
        private boolean isDeprecated;
        private boolean smartType;
        private String simpleName;
        private String typeName;
        private String enclName;
        private CharSequence sortText;
        private String leftText;
        
        private ClassItem(TypeElement elem, DeclaredType type, int dim, int substitutionOffset, boolean displayPkgName, boolean isDeprecated, boolean smartType) {
            super(substitutionOffset);
            TypeMirror elemType = elem.asType();
            if (elemType.getKind() == TypeKind.ERROR) {
                this.elementHandle = null;
                this.typeHandle = TypeMirrorHandle.create((DeclaredType)elemType);
            } else {
                this.elementHandle = ElementHandle.create(elem);
                this.typeHandle = TypeMirrorHandle.create(type);
            }
            this.dim = dim;
            this.isDeprecated = isDeprecated;
            this.smartType = smartType;
            this.simpleName = elem.getSimpleName().toString();
            this.typeName = Utilities.getTypeName(type, false).toString();
            if (displayPkgName) {
                this.enclName = Utilities.getElementName(elem.getEnclosingElement(), true).toString();
                this.sortText = new ClassSortText(this.simpleName, this.enclName);
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
            return JavaCompletionProvider.createDocTask(elementHandle);
        }

        protected ImageIcon getIcon(){
            if (icon == null) icon = new ImageIcon(org.openide.util.Utilities.loadImage(CLASS));
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
            if (enclName == null) {
                super.substituteText(c, offset, len, toAdd);
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
            final int finalLen = len;
            JavaSource js = JavaSource.forDocument(doc);
            try {
                js.runUserActionTask(new Task<CompilationController>() {

                    public void run(CompilationController controller) throws IOException {
                        controller.toPhase(Phase.RESOLVED);
                        DeclaredType type = typeHandle.resolve(controller);
                        TypeElement elem = elementHandle != null ? elementHandle.resolve(controller) : (TypeElement)type.asElement();
                        boolean asTemplate = false;
                        StringBuilder sb = new StringBuilder();
                        int cnt = 1;
                        sb.append("${PAR"); //NOI18N
                        sb.append(cnt++);
                        if (type.getKind() != TypeKind.ERROR && EnumSet.range(ElementKind.PACKAGE, ElementKind.INTERFACE).contains(elem.getEnclosingElement().getKind())) {
                            sb.append(" type=\""); //NOI18N
                            sb.append(elem.getQualifiedName());
                        } else {
                            sb.append(" default=\""); //NOI18N
                            sb.append(elem.getQualifiedName());
                        }
                        sb.append("\" editable=false}"); //NOI18N
                        Iterator<? extends TypeMirror> tas = type.getTypeArguments().iterator();
                        if (tas.hasNext()) {
                            sb.append('<'); //NOI18N
                            while (tas.hasNext()) {
                                TypeMirror ta = tas.next();
                                sb.append("${PAR"); //NOI18N
                                sb.append(cnt++);
                                if (ta.getKind() == TypeKind.TYPEVAR) {
                                    TypeVariable tv = (TypeVariable)ta;
                                    if (elem == tv.asElement().getEnclosingElement()) {
                                        sb.append(" type=\""); //NOI18N
                                        ta = tv.getUpperBound();
                                    } else {
                                        sb.append(" editable=false default=\""); //NOI18N
                                        asTemplate = true;
                                    }
                                    sb.append(Utilities.getTypeName(ta, true));
                                    sb.append("\"}"); //NOI18N
                                } else if (ta.getKind() == TypeKind.WILDCARD) {
                                    sb.append(" type=\""); //NOI18N
                                    TypeMirror bound = ((WildcardType)ta).getExtendsBound();
                                    if (bound == null)
                                        bound = ((WildcardType)ta).getSuperBound();
                                    sb.append(bound != null ? Utilities.getTypeName(bound, true) : "java.lang.Object"); //NOI18N
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
                                    sb.append("\" editable=false}"); //NOI18N
                                    asTemplate = true;
                                }
                                if (tas.hasNext())
                                    sb.append(", "); //NOI18N
                            }
                            sb.append('>'); //NOI18N
                        }
                        for(int i = 0; i < dim; i++) {
                            sb.append("[${PAR"); //NOI18N
                            sb.append(cnt++);
                            sb.append(" instanceof=\"int\" default=\"\"}]"); //NOI18N
                            asTemplate = true;
                        }
                        if (asTemplate) {
                            if (finalLen > 0) {
                                doc.atomicLock();
                                try {
                                    doc.remove(offset, finalLen);
                                } catch (BadLocationException e) {
                                    // Can't update
                                } finally {
                                    doc.atomicUnlock();
                                }
                            }
                            CodeTemplateManager ctm = CodeTemplateManager.get(doc);
                            if (ctm != null)
                                ctm.createTemporary(sb.append(text).toString()).insert(c);
                        } else {
                            // Update the text
                            doc.atomicLock();
                            try {
                                Position semiPosition = semiPos > -1 ? doc.createPosition(semiPos) : null;
                                TreePath tp = controller.getTreeUtilities().pathFor(offset);
                                text.insert(0, AutoImport.resolveImport(controller, tp, controller.getTypes().getDeclaredType(elem)));
                                String textToReplace = doc.getText(offset, finalLen);
                                if (textToReplace.contentEquals(text)) return;
                                doc.remove(offset, finalLen);
                                doc.insertString(offset, text.toString(), null);
                                if (semiPosition != null)
                                    doc.insertString(semiPosition.getOffset(), ";", null); //NOI18N
                            } catch (BadLocationException e) {
                                // Can't update
                            } finally {
                                doc.atomicUnlock();
                            }
                        }
                    }
                }, true);
            } catch (IOException ioe) {                
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
        
        private InterfaceItem(TypeElement elem, DeclaredType type, int dim, int substitutionOffset, boolean displayPkgName, boolean isDeprecated, boolean smartType) {
            super(elem, type, dim, substitutionOffset, displayPkgName, isDeprecated, smartType);
        }

        protected ImageIcon getIcon(){
            if (icon == null) icon = new ImageIcon(org.openide.util.Utilities.loadImage(INTERFACE));
            return icon;            
        }
        
        protected String getColor() {
            return INTERFACE_COLOR;
        }
    }

    static class EnumItem extends ClassItem {
        
        private static final String ENUM = "org/netbeans/modules/editor/resources/completion/enum.png"; // NOI18N
        private static ImageIcon icon;
        
        private EnumItem(TypeElement elem, DeclaredType type, int dim, int substitutionOffset, boolean displayPkgName, boolean isDeprecated, boolean smartType) {
        super(elem, type, dim, substitutionOffset, displayPkgName, isDeprecated, smartType);
        }

        protected ImageIcon getIcon(){
            if (icon == null) icon = new ImageIcon(org.openide.util.Utilities.loadImage(ENUM));
            return icon;            
        }
    }
    
    static class AnnotationTypeItem extends ClassItem {
        
        private static final String ANNOTATION = "org/netbeans/modules/editor/resources/completion/annotation_type.png"; // NOI18N
        private static ImageIcon icon;
        
        private AnnotationTypeItem(TypeElement elem, DeclaredType type, int dim, int substitutionOffset, boolean displayPkgName, boolean isDeprecated, boolean smartType) {
            super(elem, type, dim, substitutionOffset, displayPkgName, isDeprecated, smartType);
        }

        protected ImageIcon getIcon(){
            if (icon == null) icon = new ImageIcon(org.openide.util.Utilities.loadImage(ANNOTATION));
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
            if (icon == null) icon = new ImageIcon(org.openide.util.Utilities.loadImage(LOCAL_VARIABLE));
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
            ImageIcon newIcon = new ImageIcon(org.openide.util.Utilities.loadImage(iconPath));
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
            ImageIcon newIcon = new ImageIcon(org.openide.util.Utilities.loadImage(iconPath));
            icon[isStatic?1:0][level] = newIcon;
            return newIcon;            
        }
        
        protected void substituteText(final JTextComponent c, int offset, int len, String toAdd) {
            if (toAdd == null) {
                if (isPrimitive) {
                    try {
                        final String[] ret = new String[1];
                        JavaSource js = JavaSource.forDocument(c.getDocument());
                        js.runUserActionTask(new Task<CompilationController>() {

                            public void run(CompilationController controller) throws Exception {
                                controller.toPhase(JavaSource.Phase.PARSED);
                                TreePath tp = controller.getTreeUtilities().pathFor(c.getSelectionEnd());
                                Tree tree = tp.getLeaf();
                                if (tree.getKind() == Tree.Kind.IDENTIFIER || tree.getKind() == Tree.Kind.PRIMITIVE_TYPE)
                                    tp = tp.getParentPath();
                                if (tp.getLeaf().getKind() == Tree.Kind.MEMBER_SELECT ||
                                    (tp.getLeaf().getKind() == Tree.Kind.METHOD_INVOCATION && ((MethodInvocationTree)tp.getLeaf()).getMethodSelect() == tree))
                                    tp = tp.getParentPath();
                                if (tp.getLeaf().getKind() == Tree.Kind.EXPRESSION_STATEMENT || tp.getLeaf().getKind() == Tree.Kind.BLOCK)
                                    ret[0] = ";"; //NOI18N
                            }
                        }, true);
                        toAdd = ret[0];
                    } catch (IOException ex) {
                    }
                }
            }
            String add = inImport ? ";" : "()"; //NOI18N
            if (toAdd != null && !add.startsWith(toAdd))
                add += toAdd;
            if (inImport || params.isEmpty()) {
                super.substituteText(c, offset, len, add);
            } else {                
                BaseDocument doc = (BaseDocument)c.getDocument();
                String text = ""; //NOI18N
                int semiPos = add.endsWith(";") ? findPositionForSemicolon(c) : -2; //NOI18N
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
                doc.atomicLock();
                try {
                    Position semiPosition = semiPos > -1 ? doc.createPosition(semiPos) : null;
                    if (len > 0)
                        doc.remove(offset, len);
                    if (semiPosition != null)
                        doc.insertString(semiPosition.getOffset(), ";", null); //NOI18N
                } catch (BadLocationException e) {
                    // Can't update
                } finally {
                    doc.atomicUnlock();
                }
                CodeTemplateManager ctm = CodeTemplateManager.get(doc);
                if (ctm != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(getInsertPrefix());
                    sb.append("("); //NOI18N
                    if (text.length() > 1) {
                        for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                            ParamDesc paramDesc = it.next();
                            sb.append("${"); //NOI18N
                            sb.append(paramDesc.name);
                            sb.append(" named instanceof="); //NOI18N
                            sb.append(paramDesc.fullTypeName);
                            sb.append("}"); //NOI18N
                            if (it.hasNext())
                                sb.append(", "); //NOI18N
                        }
                        sb.append(")");//NOI18N
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
        
        private static ImageIcon implementBadge = new ImageIcon(org.openide.util.Utilities.loadImage(IMPL_BADGE_PATH));
        private static ImageIcon overrideBadge = new ImageIcon(org.openide.util.Utilities.loadImage(OVRD_BADGE_PATH));
        private static ImageIcon merged_icon[][] = new ImageIcon[2][4];
        
        private boolean implement;
        private String leftText;
        
        private OverrideMethodItem(ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean implement) {
            super(elem, type, substitutionOffset, false, false, false, false);
            this.implement = implement;
        }
        
        protected String getLeftHtmlText() {
            if (leftText == null)
                leftText = super.getLeftHtmlText() + (implement ? " - implement" : " - override");
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
            merged = new ImageIcon( org.openide.util.Utilities.mergeImages(
                superIcon.getImage(), 
                implement ? implementBadge.getImage() : overrideBadge.getImage(), 
                16 - 8, 
                16 - 8) );
            
            merged_icon[implement? 0 : 1][level] = merged;
            return merged;
        }

        
        protected void substituteText(final JTextComponent c, final int offset, final int len, String toAdd) {
            BaseDocument doc = (BaseDocument)c.getDocument();
            if (len > 0) {
                doc.atomicLock();
                try {
                    doc.remove(offset, len);
                } catch (BadLocationException e) {
                    // Can't update
                } finally {
                    doc.atomicUnlock();
                }
            }
            try {
                JavaSource js = JavaSource.forDocument(doc);
                js.runModificationTask(new Task<WorkingCopy>() {

                    public void run(WorkingCopy copy) throws IOException {
                        copy.toPhase(Phase.ELEMENTS_RESOLVED);
                        ExecutableElement ee = elementHandle.resolve(copy);                        
                        if (ee == null)
                            return;
                        TreePath tp = copy.getTreeUtilities().pathFor(offset);
                        if (tp.getLeaf().getKind() == Tree.Kind.CLASS) {
                            if (Utilities.isInMethod(tp))
                                copy.toPhase(Phase.RESOLVED);
                            int idx = 0;
                            for (Tree tree : ((ClassTree)tp.getLeaf()).getMembers()) {
                                if (copy.getTrees().getSourcePositions().getStartPosition(tp.getCompilationUnit(), tree) < offset)
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
                }).commit();
            } catch (IOException ex) {
                Logger.getLogger("global").log(Level.WARNING, null, ex);
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toString());
            sb.append(" - ");
            sb.append(implement ? "implement" : "override");
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
                this.name = "set" + Character.toUpperCase(simpleName.charAt(0)) + simpleName.substring(1, simpleName.length()); //NOI18N
            else
                this.name = (elem.asType().getKind() == TypeKind.BOOLEAN ? "is" : "get") + Character.toUpperCase(simpleName.charAt(0)) + simpleName.substring(1, simpleName.length()); //NOI18N
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
                lText.append(") - generate"); //NOI18N
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
                    superIcon = new ImageIcon(org.openide.util.Utilities.loadImage(METHOD_PUBLIC));
                if (setter) {
                    ImageIcon setterBadge = new ImageIcon(org.openide.util.Utilities.loadImage(SETTER_BADGE_PATH));
                    merged_icons[1] = new ImageIcon(org.openide.util.Utilities.mergeImages(superIcon.getImage(), 
                            setterBadge.getImage(), 16 - 8, 16 - 8));
                } else {
                    ImageIcon getterBadge = new ImageIcon(org.openide.util.Utilities.loadImage(GETTER_BADGE_PATH));
                    merged_icons[0] = new ImageIcon(org.openide.util.Utilities.mergeImages(superIcon.getImage(), 
                            getterBadge.getImage(), 16 - 8, 16 - 8));
                }
            }
            return merged_icons[setter ? 1 : 0];
        }
        
        @Override
        protected void substituteText(final JTextComponent c, final int offset, final int len, String toAdd) {
            BaseDocument doc = (BaseDocument)c.getDocument();
            if (len > 0) {
                doc.atomicLock();
                try {
                    doc.remove(offset, len);
                } catch (BadLocationException e) {
                    // Can't update
                } finally {
                    doc.atomicUnlock();
                }
            }
            try {
                JavaSource js = JavaSource.forDocument(doc);
                js.runModificationTask(new Task<WorkingCopy>() {

                    public void run(WorkingCopy copy) throws IOException {
                        copy.toPhase(Phase.ELEMENTS_RESOLVED);
                        VariableElement ve = elementHandle.resolve(copy);                        
                        if (ve == null)
                            return;
                        TreePath tp = copy.getTreeUtilities().pathFor(offset);
                        if (tp.getLeaf().getKind() == Tree.Kind.CLASS) {
                            if (Utilities.isInMethod(tp))
                                copy.toPhase(Phase.RESOLVED);
                            int idx = 0;
                            for (Tree tree : ((ClassTree)tp.getLeaf()).getMembers()) {
                                if (copy.getTrees().getSourcePositions().getStartPosition(tp.getCompilationUnit(), tree) < offset)
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
                }).commit();
            } catch (IOException ex) {
                Logger.getLogger("global").log(Level.WARNING, null, ex);
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
            sb.append(") - generate"); //NOI18N
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
        private String sortText;
        private String leftText;
        
        private ConstructorItem(ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, boolean smartType) {
            super(substitutionOffset);
            this.elementHandle = ElementHandle.create(elem);
            this.isDeprecated = isDeprecated;
            this.smartType = smartType;
            this.simpleName = elem.getEnclosingElement().getSimpleName().toString();
            this.modifiers = elem.getModifiers();
            this.params = new ArrayList<ParamDesc>();
            Iterator<? extends VariableElement> it = elem.getParameters().iterator();
            Iterator<? extends TypeMirror> tIt = type.getParameterTypes().iterator();
            while(it.hasNext() && tIt.hasNext()) {
                TypeMirror tm = tIt.next();
                this.params.add(new ParamDesc(tm.toString(), Utilities.getTypeName(tm, false, elem.isVarArgs() && !tIt.hasNext()).toString(), it.next().getSimpleName().toString()));
            }
            this.isAbstract = elem.getEnclosingElement().getModifiers().contains(Modifier.ABSTRACT);
        }
        
        public int getSortPriority() {
            return smartType ? 650 - SMART_TYPE : 650;
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
            ImageIcon newIcon = new ImageIcon(org.openide.util.Utilities.loadImage(iconPath));
            icon[level] = newIcon;
            return newIcon;            
        }
        
        protected void substituteText(JTextComponent c, int offset, int len, String toAdd) {
            offset += len;
            len = 0;
            int semiPos = toAdd != null && toAdd.endsWith(";") ? findPositionForSemicolon(c) : -2; //NOI18N
            if (semiPos > -2)
                toAdd = toAdd.length() > 1 ? toAdd.substring(0, toAdd.length() - 1) : null;
            BaseDocument doc = (BaseDocument)c.getDocument();
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
            if (toAdd != null && !add.startsWith(toAdd))
                add += toAdd;   
            String text = ""; //NOI18N
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
            doc.atomicLock();
            try {
                Position position = doc.createPosition(offset);
                Position semiPosition = semiPos > -1 ? doc.createPosition(semiPos) : null;
                doc.remove(offset, len);
                doc.insertString(position.getOffset(), text, null);
                if (semiPosition != null)
                    doc.insertString(semiPosition.getOffset(), ";", null); //NOI18N
            } catch (BadLocationException e) {
            } finally {
                doc.atomicUnlock();
            }
            Position position = null;
            if (isAbstract && text.length() > 3) {
                try {
                    JavaSource js = JavaSource.forDocument(doc);
                    final int off = offset + 4;
                    js.runModificationTask(new Task<WorkingCopy>() {

                        public void run(WorkingCopy copy) throws IOException {
                            copy.toPhase(JavaSource.Phase.RESOLVED);
                            TreePath path = copy.getTreeUtilities().pathFor(off);                            
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
                    }).commit();
                } catch (Exception ex) {
                }
            }            
            if (!params.isEmpty() && text.length() > 1) {
                CodeTemplateManager ctm = CodeTemplateManager.get(doc);
                if (ctm != null) {
                    StringBuilder sb = new StringBuilder();
                    for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                        ParamDesc paramDesc = it.next();
                        sb.append("${"); //NOI18N
                        sb.append(paramDesc.name);
                        sb.append(" named instanceof="); //NOI18N
                        sb.append(paramDesc.fullTypeName);
                        sb.append("}"); //NOI18N
                        if (it.hasNext())
                            sb.append(", "); //NOI18N
                    }
                    if (position != null)
                        offset = position.getOffset();
                    c.setCaretPosition(offset + 1);
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
            if (icon == null) icon = new ImageIcon(org.openide.util.Utilities.loadImage(CONSTRUCTOR));
            return icon;            
        }

        protected void substituteText(JTextComponent c, int offset, int len, String toAdd) {
            offset += len;
            len = 0;
            int semiPos = toAdd != null && toAdd.endsWith(";") ? findPositionForSemicolon(c) : -2; //NOI18N
            if (semiPos > -2)
                toAdd = toAdd.length() > 1 ? toAdd.substring(0, toAdd.length() - 1) : null;
            BaseDocument doc = (BaseDocument) c.getDocument();
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
            if (toAdd != null && !add.startsWith(toAdd))
                add += toAdd;
            String text = ""; //NOI18N
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
            doc.atomicLock();
            try {
                Position position = doc.createPosition(offset);
                Position semiPosition = semiPos > -1 ? doc.createPosition(semiPos) : null;
                doc.remove(offset, len);
                doc.insertString(position.getOffset(), text, null);
                if (semiPosition != null)
                    doc.insertString(position.getOffset(), ";", null); //NOI18N
            } catch (BadLocationException e) {
            } finally {
                doc.atomicUnlock();
            }
            if (isAbstract && text.length() > 3) {
                try {
                    JavaSource js = JavaSource.forDocument(c.getDocument());
                    final int off = c.getSelectionEnd() - text.length() + 4;
                    js.runModificationTask(new Task<WorkingCopy>() {

                        public void run(WorkingCopy copy) throws IOException {
                            copy.toPhase(JavaSource.Phase.RESOLVED);
                            TreePath path = copy.getTreeUtilities().pathFor(off);                            
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
                    }).commit();
                } catch (IOException ex) {
                }
            }
        }
        
        public String toString() {
            return simpleName + "()";
        }        
    }
    
    static class AnnotationItem extends AnnotationTypeItem {
        
        private AnnotationItem(TypeElement elem, DeclaredType type, int substitutionOffset, boolean isDeprecated, boolean smartType) {
            super(elem, type, 0, substitutionOffset, true, isDeprecated, smartType);
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
            JavaSource js = JavaSource.forDocument(doc);
            try {
                js.runUserActionTask(new Task<CompilationController>() {

                    public void run(CompilationController controller) throws IOException {
                        controller.toPhase(JavaSource.Phase.RESOLVED);
                        TypeElement elem = elementHandle.resolve(controller);
                        // Update the text
                        doc.atomicLock();
                        try {
                            Position semiPosition = semiPos > -1 ? doc.createPosition(semiPos) : null;
                            TreePath tp = controller.getTreeUtilities().pathFor(offset);
                            text.insert(0, "@" + AutoImport.resolveImport(controller, tp, controller.getTypes().getDeclaredType(elem))); //NOI18N
                            String textToReplace = doc.getText(offset, finalLen);
                            if (textToReplace.contentEquals(text)) return;
                            doc.remove(offset, finalLen);
                            doc.insertString(offset, text.toString(), null);
                            if (semiPosition != null)
                                doc.insertString(semiPosition.getOffset(), ";", null); //NOI18N
                        } catch (BadLocationException e) {
                            // Can't update
                        } finally {
                            doc.atomicUnlock();
                        }
                    }
                }, true);
            } catch (IOException ioe) {                
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
            return simpleName + "="; //NOI18N
        }
        
        public CompletionTask createDocumentationTask() {
            return JavaCompletionProvider.createDocTask(elementHandle);
        }

        protected ImageIcon getIcon(){
            if (icon == null) icon = new ImageIcon(org.openide.util.Utilities.loadImage(ATTRIBUTE));
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
            ImageIcon newIcon = new ImageIcon(org.openide.util.Utilities.loadImage(iconPath));
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
            JavaSource js = JavaSource.forDocument(doc);
            try {
                js.runUserActionTask(new Task<CompilationController>() {

                    public void run(CompilationController controller) throws IOException {
                        controller.toPhase(JavaSource.Phase.RESOLVED);
                        DeclaredType type = typeHandle.resolve(controller);
                        StringBuilder sb = new StringBuilder();
                        int cnt = 1;
                        sb.append("${PAR#"); //NOI18N
                        sb.append(cnt++);
                        sb.append(" type=\""); //NOI18N
                        sb.append(((TypeElement)type.asElement()).getQualifiedName());
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
                                    sb.append("\"}"); //NOI18N
                                } else if (ta.getKind() == TypeKind.WILDCARD) {
                                    sb.append(" type=\""); //NOI18N
                                    TypeMirror bound = ((WildcardType)ta).getExtendsBound();
                                    if (bound == null)
                                        bound = ((WildcardType)ta).getSuperBound();
                                    sb.append(bound != null ? Utilities.getTypeName(bound, true) : "java.lang.Object"); //NOI18N
                                    sb.append("\"}"); //NOI18N
                                } else if (ta.getKind() == TypeKind.ERROR) {
                                    sb.append(" default=\""); //NOI18N
                                    sb.append(((ErrorType)ta).asElement().getSimpleName());
                                    sb.append("\"}"); //NOI18N
                                } else {
                                    sb.append(" type=\""); //NOI18N
                                    sb.append(Utilities.getTypeName(ta, true));
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
                            sb.append("("); //NOI18N
                            for (Iterator<ParamDesc> it = params.iterator(); it.hasNext();) {
                                ParamDesc paramDesc = it.next();
                                sb.append("${"); //NOI18N
                                sb.append(paramDesc.name);
                                sb.append(" named instanceof="); //NOI18N
                                sb.append(paramDesc.fullTypeName);
                                sb.append("}"); //NOI18N
                                if (it.hasNext())
                                    sb.append(", "); //NOI18N
                            }
                            sb.append(")");//NOI18N
                        }
                        sb.append(text);
                        doc.atomicLock();
                        try {
                            Position semiPosition = semiPos > -1 ? doc.createPosition(semiPos) : null;
                            if (finalLen > 0)
                                doc.remove(offset, finalLen);
                            if (semiPosition != null)
                                doc.insertString(semiPosition.getOffset(), ";", null); //NOI18N
                        } catch (BadLocationException e) {
                            // Can't update
                        } finally {
                            doc.atomicUnlock();
                        }
                        CodeTemplateManager ctm = CodeTemplateManager.get(doc);
                        if (ctm != null) {
                            ctm.createTemporary(sb.toString()).insert(c);
                        }
                    }
                }, true);
            } catch (IOException ioe) {                
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
        private String simpleName;
        private List<ParamDesc> params;
        private String sortText;
        private String leftText;
        
        private InitializeAllConstructorItem(Iterable<? extends VariableElement> fields, TypeElement parent, int substitutionOffset) {
            super(substitutionOffset);
            this.fieldHandles = new ArrayList<ElementHandle<VariableElement>>();
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
                lText.append(')'); //NOI18N
                lText.append(" - generate"); //NOI18N
                leftText = lText.toString();
            }
            return leftText;
        }
        
        protected ImageIcon getIcon() {
            if (icon == null) 
                icon = new ImageIcon(org.openide.util.Utilities.loadImage(CONSTRUCTOR_PUBLIC));
            return icon;            
        }
        
        public CharSequence getInsertPrefix() {
            return simpleName;
        }        
        
        protected void substituteText(final JTextComponent c, final int offset, final int len, String toAdd) {
            BaseDocument doc = (BaseDocument)c.getDocument();
            if (len > 0) {
                doc.atomicLock();
                try {
                    doc.remove(offset, len);
                } catch (BadLocationException e) {
                    // Can't update
                } finally {
                    doc.atomicUnlock();
                }
            }
            try {
                JavaSource js = JavaSource.forDocument(c.getDocument());
                js.runModificationTask(new Task<WorkingCopy>() {

                    public void run(WorkingCopy copy) throws IOException {
                        copy.toPhase(JavaSource.Phase.PARSED);
                        TreePath tp = copy.getTreeUtilities().pathFor(offset);
                        if (tp.getLeaf().getKind() == Tree.Kind.CLASS) {
                            ArrayList<VariableElement> fieldElements = new ArrayList<VariableElement>();
                            for (ElementHandle<? extends Element> handle : fieldHandles)
                                fieldElements.add((VariableElement)handle.resolve(copy));
                            int idx = 0;
                            for (Tree tree : ((ClassTree)tp.getLeaf()).getMembers()) {
                                if (copy.getTrees().getSourcePositions().getStartPosition(tp.getCompilationUnit(), tree) < offset)
                                    idx++;
                                else
                                    break;
                            }
                            GeneratorUtils.generateConstructor(copy, tp, fieldElements, null, idx);
                        }
                    }
                }).commit();
            } catch (IOException ex) {
            }
        }

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
            sb.append(')'); //NOI18N
            sb.append(" - generate"); //NOI18N
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
            JavaSource js = JavaSource.forDocument(c.getDocument());
            js.runUserActionTask(new Task<CompilationController>() {

                public void run(CompilationController controller) throws Exception {
                    controller.toPhase(JavaSource.Phase.PARSED);
                    Tree t = null;
                    TreePath tp = controller.getTreeUtilities().pathFor(offset);
                    while (t == null && tp != null) {
                        switch(tp.getLeaf().getKind()) {
                            case EXPRESSION_STATEMENT:
                                ExpressionTree expr = ((ExpressionStatementTree)tp.getLeaf()).getExpression();
                                if (expr != null && expr.getKind() == Tree.Kind.ERRONEOUS)
                                    break;
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
                        TokenSequence<JavaTokenId> ts = findLastNonWhitespaceToken(controller, offset, endPos);
                        if (ts != null) {
                            ret[0] = ts.token().id() == JavaTokenId.SEMICOLON ? -1 : ts.offset() + ts.token().length();
                        }
                    } else {
                        TokenSequence<JavaTokenId> ts = controller.getTokenHierarchy().tokenSequence(JavaTokenId.language());
                        ts.move(offset);
                        if (ts.moveNext() &&  ts.token().id() == JavaTokenId.SEMICOLON)
                            ret[0] = -1;
                    }
                }
            }, true);
        } catch (IOException ex) {
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
    
    static class ClassSortText implements CharSequence {

        private String name;
        private String pkgName;
        private String text;
        
        public ClassSortText(String name, String pkgName) {
            this.name = name + "#"; //NOI18N
            this.pkgName = pkgName;
        }

        public int length() {
            return name.length() + pkgName.length() + 3;
        }

        public char charAt(int index) {
            return index < name.length() ? name.charAt(index) : getText().charAt(index);
        }

        public CharSequence subSequence(int start, int end) {
            return getText().subSequence(start, end);
        }
        
        private String getText() {
            if (text == null)
                text = name + Utilities.getImportanceLevel(pkgName) + "#" + pkgName; //NOI18N
            return text;
        }
    }
}
