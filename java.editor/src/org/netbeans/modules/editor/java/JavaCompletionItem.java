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
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.xml.XMLUtil;

/**
 *
 * @author Dusan Balek
 */
public abstract class JavaCompletionItem implements CompletionItem {
        
    public static final JavaCompletionItem createKeywordItem(String kwd, String postfix, int substitutionOffset) {
        return new KeywordItem(kwd, 0, postfix, substitutionOffset);
    }
    
    public static final JavaCompletionItem createPackageItem(String pkgFQN, int substitutionOffset, boolean isDeprecated) {
        return new PackageItem(pkgFQN, substitutionOffset, isDeprecated);
    }

    public static final JavaCompletionItem createTypeItem(TypeElement elem, DeclaredType type, int substitutionOffset, boolean displayPkgName, boolean isDeprecated) {
        switch (elem.getKind()) {
            case CLASS:
                return new ClassItem(elem, type, 0, substitutionOffset, displayPkgName, isDeprecated);
            case INTERFACE:
                return new InterfaceItem(elem, type, 0, substitutionOffset, displayPkgName, isDeprecated);
            case ENUM:
                return new EnumItem(elem, type, 0, substitutionOffset, displayPkgName, isDeprecated);
            case ANNOTATION_TYPE:
                return new AnnotationItem(elem, type, 0, substitutionOffset, displayPkgName, isDeprecated);
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
            return new KeywordItem(tm.toString(), dim, null, substitutionOffset);
        if (tm.getKind() == TypeKind.DECLARED) {
            DeclaredType dt = (DeclaredType)tm;
            TypeElement elem = (TypeElement)dt.asElement();
            switch (elem.getKind()) {
                case CLASS:
                    return new ClassItem(elem, dt, dim, substitutionOffset, true, elements.isDeprecated(elem));
                case INTERFACE:
                    return new InterfaceItem(elem, dt, dim, substitutionOffset, true, elements.isDeprecated(elem));
                case ENUM:
                    return new EnumItem(elem, dt, dim, substitutionOffset, true, elements.isDeprecated(elem));
                case ANNOTATION_TYPE:
                    return new AnnotationItem(elem, dt, dim, substitutionOffset, true, elements.isDeprecated(elem));
            }
        }
        throw new IllegalArgumentException("array element kind=" + tm.getKind());
    }
    
    public static final JavaCompletionItem createTypeParameterItem(TypeParameterElement elem, int substitutionOffset) {
        return new TypeParameterItem(elem, substitutionOffset);
    }

    public static final JavaCompletionItem createVariableItem(VariableElement elem, TypeMirror type, int substitutionOffset, boolean isInherited, boolean isDeprecated) {
        switch (elem.getKind()) {
            case LOCAL_VARIABLE:
            case PARAMETER:
            case EXCEPTION_PARAMETER:
                return new VariableItem(type, elem.getSimpleName(), substitutionOffset);
            case ENUM_CONSTANT:
            case FIELD:
                return new FieldItem(elem, type, substitutionOffset, isInherited, isDeprecated);
            default:
                throw new IllegalArgumentException("kind=" + elem.getKind());
        }
    }
    
    public static final JavaCompletionItem createVariableItem(CharSequence varName, int substitutionOffset) {
        return new VariableItem(null, varName, substitutionOffset);
    }

    public static final JavaCompletionItem createExecutableItem(ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isInherited, boolean isDeprecated, boolean inImport) {
        switch (elem.getKind()) {
            case METHOD:
                return new MethodItem(elem, type, substitutionOffset, isInherited, isDeprecated, inImport);
            case CONSTRUCTOR:
                return new ConstructorItem(elem, type, substitutionOffset, isDeprecated);
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

    public static final JavaCompletionItem createDefaultConstructorItem(TypeElement elem, int substitutionOffset) {
        return new DefaultConstructorItem(elem, substitutionOffset);
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

    public static final JavaCompletionItem createInitializeAllConstructorItem(Iterable<VariableElement> fields, TypeElement parent, int substitutionOffset) {
        return new InitializeAllConstructorItem(fields, parent, substitutionOffset);
    }

    public static final String COLOR_END = "</font>"; //NOI18N
    public static final String STRIKE = "<s>"; //NOI18N
    public static final String STRIKE_END = "</s>"; //NOI18N
    public static final String BOLD = "<b>"; //NOI18N
    public static final String BOLD_END = "</b>"; //NOI18N

    int substitutionOffset;
    
    JavaCompletionItem(int substitutionOffset) {
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
            if (toAdd != null && !toAdd.equals("\n")) {//NOI18N
                TokenSequence<JavaTokenId> sequence = Utilities.getJavaTokenSequence(c, offset + len);
                if (sequence == null) {
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
                if (text.equals(textToReplace)) return;
                
                doc.remove(offset, len);
                doc.insertString(offset, text, null);
            } catch (BadLocationException e) {
                // Can't update
            } finally {
                doc.atomicUnlock();
            }
        }
    }
            
    private static class KeywordItem extends JavaCompletionItem {
        
        private static final String JAVA_KEYWORD = "org/netbeans/modules/java/editor/resources/javakw.gif"; //NOI18N
        private static final String KEYWORD_COLOR = "<font color=#000099>"; //NOI18N
        private static ImageIcon icon;
        
        private String kwd;
        private int dim;
        private String postfix;

        private KeywordItem(String kwd, int dim, String postfix, int substitutionOffset) {
            super(substitutionOffset);
            this.kwd = kwd;
            this.dim = dim;
            this.postfix = postfix;
        }
        
        public int getSortPriority() {
            return 600;
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
            StringBuilder sb = new StringBuilder();
            sb.append(KEYWORD_COLOR);
            sb.append(BOLD);
            sb.append(kwd);
            for(int i = 0; i < dim; i++)
                sb.append("[]"); //NOI18N
            sb.append(BOLD_END);
            sb.append(COLOR_END);
            return sb.toString();
        }
        
        protected void substituteText(JTextComponent c, int offset, int len, String toAdd) {
            if (dim == 0) {
                super.substituteText(c, offset, len, toAdd != null ? toAdd : postfix);
                return;
            }
            BaseDocument doc = (BaseDocument)c.getDocument();
            final StringBuilder text = new StringBuilder();
            if (toAdd != null && !toAdd.equals("\n")) {//NOI18N
                TokenSequence<JavaTokenId> sequence = Utilities.getJavaTokenSequence(c, offset + len);
                if (sequence == null) {
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
            CodeTemplateManager ctm = CodeTemplateManager.get(doc);
            if (ctm != null) {
                ctm.createTemporary(sb.append(text).toString()).insert(c);
            }
        }
    
        public String toString() {
            return kwd;
        }        
    }
    
    private static class PackageItem extends JavaCompletionItem {
        
        private static final String PACKAGE = "org/netbeans/modules/java/editor/resources/package.gif"; // NOI18N
        private static final String PACKAGE_COLOR = "<font color=#005600>"; //NOI18N
        private static ImageIcon icon;
        
        private String simpleName;
        private String qualifiedName;
        private boolean isDeprecated;

        private PackageItem(String pkgFQN, int substitutionOffset, boolean isDeprecated) {
            super(substitutionOffset);
            this.qualifiedName = pkgFQN;
            int idx = pkgFQN.lastIndexOf('.');
            this.simpleName = idx < 0 ? pkgFQN : pkgFQN.substring(idx + 1);
            this.isDeprecated = isDeprecated;
        }
        
        public int getSortPriority() {
            return 900;
        }
        
        public CharSequence getSortText() {
            return simpleName + "#" + qualifiedName; //NOI18N
        }
        
        public CharSequence getInsertPrefix() {
            return simpleName;
        }
        
        protected ImageIcon getIcon(){
            if (icon == null) icon = new ImageIcon(org.openide.util.Utilities.loadImage(PACKAGE));
            return icon;            
        }
        
        protected String getLeftHtmlText() {
            StringBuilder sb = new StringBuilder();
            sb.append(PACKAGE_COLOR);
            if (isDeprecated)
                sb.append(STRIKE);
            sb.append(simpleName);
            if (isDeprecated)
                sb.append(STRIKE_END);
            sb.append(COLOR_END);
            return sb.toString();
        }
        
        public String toString() {
            return simpleName;
        }        
    }

    private static class ClassItem extends JavaCompletionItem {
        
        private static final String CLASS = "org/netbeans/modules/editor/resources/completion/class_16.png"; //NOI18N
        private static final String CLASS_COLOR = "<font color=#560000>"; //NOI18N
        private static final String PKG_COLOR = "<font color=#808080>"; //NOI18N
        private static ImageIcon icon;
        
        private TypeElement elem;
        private DeclaredType type;
        private int dim;
        private boolean displayPkgName;
        private boolean isDeprecated;
        
        private ClassItem(TypeElement elem, DeclaredType type, int dim, int substitutionOffset, boolean displayPkgName, boolean isDeprecated) {
            super(substitutionOffset);
            this.elem = elem;
            this.type = type;
            this.dim = dim;
            this.displayPkgName = displayPkgName;
            this.isDeprecated = isDeprecated;
        }
        
        public int getSortPriority() {
            return 800;
        }
        
        public CharSequence getSortText() {
            return elem.getSimpleName() + "#" + elem.getQualifiedName(); //NOI18N
        }
        
        public CharSequence getInsertPrefix() {
            return elem.getSimpleName();
        }

        public CompletionTask createDocumentationTask() {
            return JavaCompletionProvider.createDocTask(ElementHandle.create(elem));
        }

        protected ImageIcon getIcon(){
            if (icon == null) icon = new ImageIcon(org.openide.util.Utilities.loadImage(CLASS));
            return icon;            
        }

        protected String getLeftHtmlText() {
            StringBuilder sb = new StringBuilder();
            sb.append(getColor());
            if (isDeprecated)
                sb.append(STRIKE);
            sb.append(escape(Utilities.getTypeName(type, false).toString()));
            for(int i = 0; i < dim; i++)
                sb.append("[]"); //NOI18N
            if (isDeprecated)
                sb.append(STRIKE_END);
            CharSequence enclName = displayPkgName ? Utilities.getElementName(elem.getEnclosingElement(), true) : null;
            if (enclName != null && enclName.length() > 0) {
                sb.append(COLOR_END);
                sb.append(PKG_COLOR);
                sb.append(" ("); //NOI18N
                sb.append(enclName);
                sb.append(")"); //NOI18N
            }
            sb.append(COLOR_END);
            return sb.toString();
        }
        
        protected String getColor() {
            return CLASS_COLOR;
        }

        protected void substituteText(final JTextComponent c, int offset, int len, String toAdd) {
            if (!displayPkgName) {
                super.substituteText(c, offset, len, toAdd);
                return;
            }
            BaseDocument doc = (BaseDocument)c.getDocument();
            final StringBuilder text = new StringBuilder();
            if (toAdd != null && !toAdd.equals("\n")) {//NOI18N
                TokenSequence<JavaTokenId> sequence = Utilities.getJavaTokenSequence(c, offset + len);
                if (sequence == null) {
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
            boolean asTemplate = false;
            StringBuilder sb = new StringBuilder();
            int cnt = 1;
            sb.append("${PAR"); //NOI18N
            sb.append(cnt++);
            sb.append(" type=\""); //NOI18N
            sb.append(elem.getQualifiedName());
            sb.append("\" editable=false}"); //NOI18N
            Iterator<? extends TypeMirror> tas = type.getTypeArguments().iterator();
            if (tas.hasNext()) {
                sb.append('<'); //NOI18N
                while (tas.hasNext()) {
                    TypeMirror ta = tas.next();
                    sb.append("${PAR"); //NOI18N
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
                        asTemplate = true;
                    } else if (ta.getKind() == TypeKind.ERROR) {
                        sb.append(" default=\"\"}"); //NOI18N
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
                CodeTemplateManager ctm = CodeTemplateManager.get(doc);
                if (ctm != null) {
                    ctm.createTemporary(sb.append(text).toString()).insert(c);
                }
            } else {
                Position position = null;
                try {
                    position = doc.createPosition(offset);
                    JavaSource js = JavaSource.forDocument(doc);
                    js.runModificationTask(new CancellableTask<WorkingCopy>() {
                        public void cancel() {
                        }
                        public void run(WorkingCopy copy) throws IOException {
                            copy.toPhase(JavaSource.Phase.RESOLVED);
                            TreePath tp = copy.getTreeUtilities().pathFor(substitutionOffset);
                            text.insert(0, AutoImport.resolveImport(copy, tp, copy.getTypes().getDeclaredType(elem)));
                        }
                    }).commit();
                } catch (Exception ex) {
                }
                // Update the text
                doc.atomicLock();
                try {
                    if (position != null)
                        offset = position.getOffset();
                    String textToReplace = doc.getText(offset, len);
                    if (textToReplace.contentEquals(text)) return;
                    
                    doc.remove(offset, len);
                    doc.insertString(offset, text.toString(), null);
                } catch (BadLocationException e) {
                    // Can't update
                } finally {
                    doc.atomicUnlock();
                }
            }
        }
        
        public String toString() {
            return elem.getSimpleName().toString();
        }        
    }
    
    private static class InterfaceItem extends ClassItem {
        
        private static final String INTERFACE = "org/netbeans/modules/editor/resources/completion/interface.png"; // NOI18N
        private static final String INTERFACE_COLOR = "<font color=#404040>"; //NOI18N
        private static ImageIcon icon;
        
        private InterfaceItem(TypeElement elem, DeclaredType type, int dim, int substitutionOffset, boolean displayPkgName, boolean isDeprecated) {
            super(elem, type, dim, substitutionOffset, displayPkgName, isDeprecated);
        }

        protected ImageIcon getIcon(){
            if (icon == null) icon = new ImageIcon(org.openide.util.Utilities.loadImage(INTERFACE));
            return icon;            
        }
        
        protected String getColor() {
            return INTERFACE_COLOR;
        }
    }

    private static class EnumItem extends ClassItem {
        
        private static final String ENUM = "org/netbeans/modules/editor/resources/completion/enum.png"; // NOI18N
        private static ImageIcon icon;
        
        private EnumItem(TypeElement elem, DeclaredType type, int dim, int substitutionOffset, boolean displayPkgName, boolean isDeprecated) {
        super(elem, type, dim, substitutionOffset, displayPkgName, isDeprecated);
        }

        protected ImageIcon getIcon(){
            if (icon == null) icon = new ImageIcon(org.openide.util.Utilities.loadImage(ENUM));
            return icon;            
        }
    }
    
    private static class AnnotationItem extends ClassItem {
        
        private static final String ANNOTATION = "org/netbeans/modules/editor/resources/completion/annotation_type.png"; // NOI18N
        private static ImageIcon icon;
        
        private AnnotationItem(TypeElement elem, DeclaredType type, int dim, int substitutionOffset, boolean displayPkgName, boolean isDeprecated) {
            super(elem, type, dim, substitutionOffset, displayPkgName, isDeprecated);
        }

        protected ImageIcon getIcon(){
            if (icon == null) icon = new ImageIcon(org.openide.util.Utilities.loadImage(ANNOTATION));
            return icon;            
        }
    }
    
    private static class TypeParameterItem extends JavaCompletionItem {
        
        private static final String TYPE_PARAMETER = "org/netbeans/modules/java/editor/resources/javakw.gif"; //NOI18N
        private static final String TYPE_PARAMETER_COLOR = "<font color=#000000>"; //NOI18N
        private static ImageIcon icon;

        private TypeParameterElement elem;

        private TypeParameterItem(TypeParameterElement elem, int substitutionOffset) {
            super(substitutionOffset);
            this.elem = elem;
        }
        
        public int getSortPriority() {
            return 700;
        }
        
        public CharSequence getSortText() {
            return elem.getSimpleName();
        }
        
        public CharSequence getInsertPrefix() {
            return elem.getSimpleName();
        }
        
        protected ImageIcon getIcon(){
            if (icon == null) icon = new ImageIcon(org.openide.util.Utilities.loadImage(TYPE_PARAMETER));
            return icon;            
        }
        
        protected String getLeftHtmlText() {
            return TYPE_PARAMETER_COLOR + elem.getSimpleName() + COLOR_END;
        }
        
        protected void substituteText(JTextComponent c, int offset, int len, String toAdd) {
            super.substituteText(c, offset, len, toAdd);
        }
    
        public String toString() {
            return elem.getSimpleName().toString();
        }        
    }

    private static class VariableItem extends JavaCompletionItem {
        
        private static final String LOCAL_VARIABLE = "org/netbeans/modules/editor/resources/completion/localVariable.gif"; //NOI18N
        private static final String PARAMETER_COLOR = "<font color=#00007c>"; //NOI18N
        private static ImageIcon icon;

        private TypeMirror type;
        private CharSequence varName;
        
        private VariableItem(TypeMirror type, CharSequence varName, int substitutionOffset) {
            super(substitutionOffset);
            this.type = type;
            this.varName = varName;
        }
        
        public int getSortPriority() {
            return 200;
        }
        
        public CharSequence getSortText() {
            return varName;
        }
        
        public CharSequence getInsertPrefix() {
            return varName;
        }

        protected String getLeftHtmlText() {
             return PARAMETER_COLOR + BOLD + varName + BOLD_END + COLOR_END;
        }
        
        protected String getRightHtmlText() {
            return escape(Utilities.getTypeName(type, false).toString());
        }
        
        protected ImageIcon getIcon(){
            if (icon == null) icon = new ImageIcon(org.openide.util.Utilities.loadImage(LOCAL_VARIABLE));
            return icon;            
        }

        public String toString() {
            return (type != null ? Utilities.getTypeName(type, false) + " " : "") + varName; //NOI18N
        }
   }

    private static class FieldItem extends JavaCompletionItem {
        
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
        
        private VariableElement elem;
        private TypeMirror type;
        private boolean isInherited;
        private boolean isDeprecated;
        
        private FieldItem(VariableElement elem, TypeMirror type, int substitutionOffset, boolean isInherited, boolean isDeprecated) {
            super(substitutionOffset);
            this.elem = elem;
            this.type = type;
            this.isInherited = isInherited;
            this.isDeprecated = isDeprecated;
        }
        
        public int getSortPriority() {
            return 300;
        }
        
        public CharSequence getSortText() {
            return elem.getSimpleName();
        }
        
        public CharSequence getInsertPrefix() {
            return elem.getSimpleName();
        }

        public CompletionTask createDocumentationTask() {
            return JavaCompletionProvider.createDocTask(ElementHandle.create(elem));
        }

        protected String getLeftHtmlText() {
            StringBuilder sb = new StringBuilder();
            sb.append(FIELD_COLOR);
            if (!isInherited)
                sb.append(BOLD);
            if (isDeprecated)
                sb.append(STRIKE);
            sb.append(elem.getSimpleName());
            if (isDeprecated)
                sb.append(STRIKE_END);
            if (!isInherited)
                sb.append(BOLD_END);
            sb.append(COLOR_END);
            return sb.toString();
        }
        
        protected String getRightHtmlText() {
            return escape(Utilities.getTypeName(type, false).toString());
        }
        
        protected ImageIcon getIcon(){
            Set<Modifier> modifiers = elem.getModifiers();
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
            for(Modifier mod : elem.getModifiers()) {
               sb.append(mod.toString());
               sb.append(' ');
            }
            sb.append(Utilities.getTypeName(type, false));
            sb.append(' ');
            sb.append(elem.getSimpleName());
            return sb.toString();
        }
    }
    
    private static class MethodItem extends JavaCompletionItem {
        
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

        private ExecutableElement elem;
        private ExecutableType type;
        private boolean isInherited;
        private boolean isDeprecated;
        private boolean inImport;
        
        private MethodItem(ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isInherited, boolean isDeprecated, boolean inImport) {
            super(substitutionOffset);
            this.elem = elem;
            this.type = type;
            this.isInherited = isInherited;
            this.isDeprecated = isDeprecated;
            this.inImport = inImport;
        }
        
        public int getSortPriority() {
            return 500;
        }
        
        public CharSequence getSortText() {
            StringBuilder sortParams = new StringBuilder();
            sortParams.append('(');
            int cnt = 0;
            for(Iterator<? extends TypeMirror> it = type.getParameterTypes().iterator(); it.hasNext();) {
                sortParams.append(Utilities.getTypeName(it.next(), false, elem.isVarArgs() && !it.hasNext()));
                if (it.hasNext()) {
                    sortParams.append(',');
                }
                cnt++;
            }
            sortParams.append(')');
            return elem.getSimpleName() + "#" + ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString(); //NOI18N
        }
        
        protected String getLeftHtmlText() {
            StringBuilder lText = new StringBuilder();
            lText.append(METHOD_COLOR);
            if (!isInherited)
                lText.append(BOLD);
            if (isDeprecated)
                lText.append(STRIKE);
            lText.append(elem.getSimpleName());
            if (isDeprecated)
                lText.append(STRIKE_END);
            if (!isInherited)
                lText.append(BOLD_END);
            lText.append(COLOR_END);
            lText.append('(');
            Iterator<? extends VariableElement> it = elem.getParameters().iterator();
            Iterator<? extends TypeMirror> tIt = type.getParameterTypes().iterator();
            while(it.hasNext() && tIt.hasNext()) {
                lText.append(escape(Utilities.getTypeName(tIt.next(), false, elem.isVarArgs() && !tIt.hasNext()).toString()));
                lText.append(' ');
                lText.append(PARAMETER_NAME_COLOR);
                lText.append(it.next().getSimpleName());
                lText.append(COLOR_END);
                if (it.hasNext()) {
                    lText.append(", "); //NOI18N
                }
            }
            lText.append(')');
            return lText.toString();
        }
        
        public CharSequence getInsertPrefix() {
            return elem.getSimpleName();
        }
        
        protected String getRightHtmlText() {
            return escape(Utilities.getTypeName(type.getReturnType(), false).toString());
        }
        
        public CompletionTask createDocumentationTask() {
            return JavaCompletionProvider.createDocTask(ElementHandle.create(elem));
        }

        protected ImageIcon getIcon() {
            Set<Modifier> modifiers = elem.getModifiers();
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
                TypeMirror retType = type.getReturnType();
                if (retType.getKind().isPrimitive() || retType.getKind() == TypeKind.VOID) {
                    try {
                        final String[] ret = new String[1];
                        JavaSource js = JavaSource.forDocument(c.getDocument());
                        js.runUserActionTask(new CancellableTask<CompilationController>() {
                            public void cancel() {
                            }
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
            List<? extends VariableElement> params = elem.getParameters();
            List<? extends TypeMirror> paramTypes = type.getParameterTypes();
            String add = inImport ? ";" : "()"; //NOI18N
            if (toAdd != null && !add.startsWith(toAdd))
                add += toAdd;
            if (inImport || params.isEmpty()) {
                super.substituteText(c, offset, len, add);
            } else {                
                BaseDocument doc = (BaseDocument)c.getDocument();
                String text = ""; //NOI18N
                TokenSequence<JavaTokenId> sequence = Utilities.getJavaTokenSequence(c, offset + len);
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
                CodeTemplateManager ctm = CodeTemplateManager.get(doc);
                if (ctm != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(getInsertPrefix());
                    sb.append("("); //NOI18N
                    if (text.length() > 1) {
                        Iterator<? extends VariableElement> it = params.iterator();
                        Iterator<? extends TypeMirror> tIt = paramTypes.iterator();
                        while (it.hasNext() && tIt.hasNext()) {
                            sb.append("${"); //NOI18N
                            sb.append(it.next().getSimpleName());
                            sb.append(" named instanceof="); //NOI18N
                            sb.append(tIt.next().toString());
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
            for (Modifier mod : elem.getModifiers()) {
                sb.append(mod.toString());
                sb.append(' ');
            }
            sb.append(Utilities.getTypeName(type.getReturnType(), false));
            sb.append(' ');
            sb.append(elem.getSimpleName());
            sb.append('(');
            Iterator<? extends VariableElement> it = elem.getParameters().iterator();
            Iterator<? extends TypeMirror> tIt = type.getParameterTypes().iterator();
            while(it.hasNext() && tIt.hasNext()) {
                sb.append(Utilities.getTypeName(tIt.next(), false, elem.isVarArgs() && !tIt.hasNext()));
                sb.append(' ');
                sb.append(it.next().getSimpleName());
                if (it.hasNext()) {
                    sb.append(", "); //NOI18N
                }
            }
            sb.append(')');
            return sb.toString();
        }
   }
    

    private static class OverrideMethodItem extends MethodItem {
        
        private ExecutableElement elem;
        private ElementHandle<ExecutableElement> elemHandle;
        private boolean implement;
        
        private OverrideMethodItem(ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean implement) {
            super(elem, type, substitutionOffset, false, false, false);
            this.elem = elem;
            this.elemHandle = ElementHandle.create(elem);
            this.implement = implement;
        }
        
        public int getSortPriority() {
            return 500;
        }
        
        protected String getLeftHtmlText() {
            String result = super.getLeftHtmlText();

            return result + (implement ? " - implement" : " - override");
        }
        
        public void defaultAction(JTextComponent component) {
            Completion.get().hideDocumentation();
            Completion.get().hideCompletion();
            substituteText(component, substitutionOffset, component.getSelectionEnd() - substitutionOffset);
        }
        
        public CompletionTask createDocumentationTask() {
            return JavaCompletionProvider.createDocTask(ElementHandle.create(elem));
        }

        protected void substituteText(final JTextComponent c, final int offset, final int len) {
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
                js.runModificationTask(new CancellableTask<WorkingCopy>() {
                    public void cancel() {
                    }
                    public void run(WorkingCopy copy) throws IOException {
                        copy.toPhase(Phase.ELEMENTS_RESOLVED);
                        ExecutableElement ee = elemHandle.resolve(copy);
                        
                        if (ee == null) {
                            return;
                        }
                        
                        TreePath tp = copy.getTreeUtilities().pathFor(offset);
                        Tree t = tp.getLeaf();
                        if (t.getKind() == Tree.Kind.CLASS) {
                            if (Utilities.isInMethod(tp)) {
                                copy.toPhase(Phase.RESOLVED);
                            }
                        
                            Tree lastBefore = null;
                            for (Tree tree : ((ClassTree) t).getMembers()) {
                                if (copy.getTrees().getSourcePositions().getStartPosition(tp.getCompilationUnit(), tree) < offset) {
                                    lastBefore = tree;
                                } else {
                                    break;
                                }
                            }
                            OverrideImplementMethodGenerator.generateOverrideImplement(copy, ee, tp, lastBefore, (ClassTree) t);
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

    private static class ConstructorItem extends JavaCompletionItem {
        
        private static final String CONSTRUCTOR_PUBLIC = "org/netbeans/modules/editor/resources/completion/constructor_16.png"; //NOI18N
        private static final String CONSTRUCTOR_PROTECTED = "org/netbeans/modules/editor/resources/completion/constructor_protected_16.png"; //NOI18N
        private static final String CONSTRUCTOR_PACKAGE = "org/netbeans/modules/editor/resources/completion/constructor_package_private_16.png"; //NOI18N
        private static final String CONSTRUCTOR_PRIVATE = "org/netbeans/modules/editor/resources/completion/constructor_private_16.png"; //NOI18N
        private static final String CONSTRUCTOR_COLOR = "<font color=#b28b00>"; //NOI18N
        private static final String PARAMETER_NAME_COLOR = "<font color=#a06001>"; //NOI18N
        private static ImageIcon icon[] = new ImageIcon[4];

        private ExecutableElement elem;
        private ExecutableType type;
        private boolean isDeprecated;
        
        private ConstructorItem(ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated) {
            super(substitutionOffset);
            this.elem = elem;
            this.type = type;
        }
        
        public int getSortPriority() {
            return 400;
        }
        
        public CharSequence getSortText() {
            StringBuilder sortParams = new StringBuilder();
            sortParams.append('(');
            int cnt = 0;
            for(Iterator<? extends TypeMirror> it = type.getParameterTypes().iterator(); it.hasNext();) {
                sortParams.append(Utilities.getTypeName(it.next(), false, elem.isVarArgs() && !it.hasNext()));
                if (it.hasNext()) {
                    sortParams.append(',');
                }
                cnt++;
            }
            sortParams.append(')');
            return elem.getEnclosingElement().getSimpleName() + "#" + ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString(); //NOI18N
        }
        
        protected String getLeftHtmlText() {
            StringBuilder lText = new StringBuilder();
            lText.append(CONSTRUCTOR_COLOR);
            lText.append(BOLD);
            if (isDeprecated)
                lText.append(STRIKE);
            lText.append(elem.getEnclosingElement().getSimpleName());
            if (isDeprecated)
                lText.append(STRIKE_END);
            lText.append(BOLD_END);
            lText.append(COLOR_END);
            lText.append('(');
            Iterator<? extends VariableElement> it = elem.getParameters().iterator();
            Iterator<? extends TypeMirror> tIt = type.getParameterTypes().iterator();
            while(it.hasNext() && tIt.hasNext()) {
                lText.append(escape(Utilities.getTypeName(tIt.next(), false, elem.isVarArgs() && !tIt.hasNext()).toString()));
                lText.append(' ');
                lText.append(PARAMETER_NAME_COLOR);
                lText.append(it.next().getSimpleName());
                lText.append(COLOR_END);
                if (it.hasNext()) {
                    lText.append(", "); //NOI18N
                }
            }
            lText.append(')');
            return lText.toString();
        }
        
        protected ImageIcon getIcon() {
            int level = getProtectionLevel(elem.getModifiers());
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
        
        public CharSequence getInsertPrefix() {
            return elem.getEnclosingElement().getSimpleName();
        }        
        
        public CompletionTask createDocumentationTask() {
            return JavaCompletionProvider.createDocTask(ElementHandle.create(elem));
        }

        protected void substituteText(JTextComponent c, int offset, int len, String toAdd) {
            offset += len;
            len = 0;
            BaseDocument doc = (BaseDocument)c.getDocument();
            boolean isAbstract = elem.getEnclosingElement().getModifiers().contains(Modifier.ABSTRACT);
            String add = isAbstract ? "() {}" : "()"; //NOI18N
            if (toAdd != null && !add.startsWith(toAdd))
                add += toAdd;            
            String text = ""; //NOI18N
            TokenSequence<JavaTokenId> sequence = Utilities.getJavaTokenSequence(c, offset);
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
                doc.remove(offset, len);
                doc.insertString(offset, text, null);
            } catch (BadLocationException e) {
            } finally {
                doc.atomicUnlock();
            }
            Position position = null;
            if (isAbstract && text.length() > 3) {
                try {
                    position = doc.createPosition(offset);
                    JavaSource js = JavaSource.forDocument(doc);
                    final int off = offset + 4;
                    js.runModificationTask(new CancellableTask<WorkingCopy>() {
                        public void cancel() {                            
                        }
                        public void run(WorkingCopy copy) throws IOException {
                            copy.toPhase(JavaSource.Phase.RESOLVED);
                            TreePath path = copy.getTreeUtilities().pathFor(off);
                            
                            while (path.getLeaf() != path.getCompilationUnit()) {
                                Tree lastTree = path.getLeaf();
                                Tree tree = path.getParentPath().getLeaf();
                                
                                if (tree.getKind() == Tree.Kind.NEW_CLASS && lastTree.getKind() == Tree.Kind.CLASS) {
                                    AbstractMethodImplGenerator.implementAllAbstractMethods(copy, path, (ClassTree) lastTree);
                                    break;
                                }
                                
                                path = path.getParentPath();
                            }
                        }
                    }).commit();
                } catch (Exception ex) {
                }
            }
            List<? extends VariableElement> params = elem.getParameters();
            if (!params.isEmpty() && text.length() > 1) {
                List<? extends TypeMirror> paramTypes = type.getParameterTypes();
                CodeTemplateManager ctm = CodeTemplateManager.get(doc);
                if (ctm != null) {
                    StringBuilder sb = new StringBuilder();
                    Iterator<? extends VariableElement> it = params.iterator();
                    Iterator<? extends TypeMirror> tIt = paramTypes.iterator();
                    while (it.hasNext() && tIt.hasNext()) {
                        sb.append("${"); //NOI18N
                        sb.append(it.next().getSimpleName());
                        sb.append(" named instanceof="); //NOI18N
                        sb.append(tIt.next().toString());
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
            for (Modifier mod : elem.getModifiers()) {
                sb.append(mod.toString());
                sb.append(' ');
            }
            sb.append(elem.getEnclosingElement().getSimpleName());
            sb.append('('); //NOI18N
            Iterator<? extends VariableElement> it = elem.getParameters().iterator();
            Iterator<? extends TypeMirror> tIt = type.getParameterTypes().iterator();
            while(it.hasNext() && tIt.hasNext()) {
                sb.append(Utilities.getTypeName(tIt.next(), false, elem.isVarArgs() && !tIt.hasNext()));
                sb.append(' ');
                sb.append(it.next().getSimpleName());
                if (it.hasNext()) {
                    sb.append(", "); //NOI18N
                }
            }
            sb.append(')');
            return sb.toString();
        }
    }
    
    private static class DefaultConstructorItem extends JavaCompletionItem {
        
        private static final String CONSTRUCTOR = "org/netbeans/modules/editor/resources/completion/constructor_16.png"; //NOI18N
        private static final String CONSTRUCTOR_COLOR = "<font color=#b28b00>"; //NOI18N
        private static ImageIcon icon;        
        
        private TypeElement elem;

        private DefaultConstructorItem(TypeElement elem, int substitutionOffset) {
            super(substitutionOffset);
            this.elem = elem;
        }

        public CharSequence getInsertPrefix() {
            return elem.getSimpleName();
        }

        protected ImageIcon getIcon() {
            if (icon == null) icon = new ImageIcon(org.openide.util.Utilities.loadImage(CONSTRUCTOR));
            return icon;            
        }

        public int getSortPriority() {
            return 400;
        }

        public CharSequence getSortText() {
            return elem.getSimpleName() + "#0#"; //NOI18N
        }
        
        protected String getLeftHtmlText() {
            return CONSTRUCTOR_COLOR + elem.getSimpleName() + "()" + COLOR_END; //NOI18N
        }        

        protected void substituteText(JTextComponent c, int offset, int len, String toAdd) {
            offset += len;
            len = 0;
            boolean isAbstract = elem.getModifiers().contains(Modifier.ABSTRACT);
            String add = isAbstract ? "() {}" : "()"; //NOI18N
            if (toAdd != null && !add.startsWith(toAdd))
                add += toAdd;
            BaseDocument doc = (BaseDocument)c.getDocument();
            String text = ""; //NOI18N
            TokenSequence<JavaTokenId> sequence = Utilities.getJavaTokenSequence(c, offset);
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
                doc.remove(offset, len);
                doc.insertString(offset, text, null);
            } catch (BadLocationException e) {
            } finally {
                doc.atomicUnlock();
            }
            if (isAbstract && text.length() > 3) {
                try {
                    JavaSource js = JavaSource.forDocument(c.getDocument());
                    final int off = c.getSelectionEnd() - text.length() + 4;
                    js.runModificationTask(new CancellableTask<WorkingCopy>() {
                        public void cancel() {                            
                        }
                        public void run(WorkingCopy copy) throws IOException {
                            copy.toPhase(JavaSource.Phase.RESOLVED);
                            TreePath path = copy.getTreeUtilities().pathFor(off);
                            
                            while (path.getLeaf() != path.getCompilationUnit()) {
                                Tree lastTree = path.getLeaf();
                                Tree tree = path.getParentPath().getLeaf();
                                
                                if (tree.getKind() == Tree.Kind.NEW_CLASS && lastTree.getKind() == Tree.Kind.CLASS) {
                                    AbstractMethodImplGenerator.implementAllAbstractMethods(copy, path, (ClassTree) lastTree);
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
            return elem.getSimpleName() + "()";
        }        
    }
    
    private static class AttributeItem extends JavaCompletionItem {
        
        private static final String ATTRIBUTE = "org/netbeans/modules/java/editor/resources/attribute_16.png"; // NOI18N
        private static final String ATTRIBUTE_COLOR = "<font color=#404040>"; //NOI18N
        private static ImageIcon icon;
        
        private ExecutableElement elem;
        private ExecutableType type;
        private boolean isDeprecated;

        private AttributeItem(ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated) {
            super(substitutionOffset);
            this.elem = elem;
            this.type = type;
            this.isDeprecated = isDeprecated;
        }
        
        public int getSortPriority() {
            return 100;
        }
        
        public CharSequence getSortText() {
            return elem.getSimpleName();
        }
        
        public CharSequence getInsertPrefix() {
            return elem.getSimpleName() + "="; //NOI18N
        }
        
        public CompletionTask createDocumentationTask() {
            return JavaCompletionProvider.createDocTask(ElementHandle.create(elem));
        }

        protected ImageIcon getIcon(){
            if (icon == null) icon = new ImageIcon(org.openide.util.Utilities.loadImage(ATTRIBUTE));
            return icon;            
        }
        
        protected String getLeftHtmlText() {
            StringBuilder sb = new StringBuilder();
            sb.append(ATTRIBUTE_COLOR);
            AnnotationValue value = elem.getDefaultValue();
            if (value != null)
                sb.append(BOLD);
            if (isDeprecated)
                sb.append(STRIKE);
            sb.append(elem.getSimpleName());
            if (isDeprecated)
                sb.append(STRIKE_END);
            if (value != null) {                
                sb.append(BOLD_END);
                sb.append(" = "); //NOI18N
                sb.append(value);
            }
            sb.append(COLOR_END);
            return sb.toString();
        }
        
        protected String getRightHtmlText() {
            return escape(Utilities.getTypeName(type.getReturnType(), false).toString());
        }
        
        public String toString() {
            return elem.getSimpleName().toString();
        }        
    }

    private static class StaticMemberItem extends JavaCompletionItem {
        
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
        
        private DeclaredType type;
        private Element memberElem;
        private TypeMirror memberType;
        private boolean isDeprecated;
        
        private StaticMemberItem(DeclaredType type, Element memberElem, TypeMirror memberType, int substitutionOffset, boolean isDeprecated) {
            super(substitutionOffset);
            this.type = type;
            this.memberElem = memberElem;
            this.memberType = memberType;
            this.isDeprecated = isDeprecated;
        }
        
        public int getSortPriority() {
            return memberElem.getKind().isField() ? 700 : 750;
        }
        
        public CharSequence getSortText() {
            if (memberElem.getKind().isField())
                return memberElem.getSimpleName();
            StringBuilder sortParams = new StringBuilder();
            sortParams.append('(');
            int cnt = 0;
            for(Iterator<? extends TypeMirror> it = ((ExecutableType)memberType).getParameterTypes().iterator(); it.hasNext();) {
                sortParams.append(Utilities.getTypeName(it.next(), false, ((ExecutableElement)memberElem).isVarArgs() && !it.hasNext()));
                if (it.hasNext()) {
                    sortParams.append(',');
                }
                cnt++;
            }
            sortParams.append(')');
            return memberElem.getSimpleName() + "#" + ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString(); //NOI18N
        }
        
        public CharSequence getInsertPrefix() {
            return memberElem.getSimpleName();
        }

        public CompletionTask createDocumentationTask() {
            return JavaCompletionProvider.createDocTask(ElementHandle.create(memberElem));
        }

        protected String getLeftHtmlText() {
            StringBuilder lText = new StringBuilder();
            lText.append(memberElem.getKind().isField() ? FIELD_COLOR : METHOD_COLOR);
            lText.append(escape(Utilities.getTypeName(type, false).toString()));
            lText.append('.');
            if (isDeprecated)
                lText.append(STRIKE);
            lText.append(memberElem.getSimpleName());
            if (isDeprecated)
                lText.append(STRIKE_END);
            lText.append(COLOR_END);
            if (!memberElem.getKind().isField()) {
                lText.append('(');
                Iterator<? extends VariableElement> it = ((ExecutableElement)memberElem).getParameters().iterator();
                Iterator<? extends TypeMirror> tIt = ((ExecutableType)memberType).getParameterTypes().iterator();
                while(it.hasNext() && tIt.hasNext()) {
                    lText.append(escape(Utilities.getTypeName(tIt.next(), false, ((ExecutableElement)memberElem).isVarArgs() && !tIt.hasNext()).toString()));
                    lText.append(' '); //NOI18N
                    lText.append(PARAMETER_NAME_COLOR);
                    lText.append(it.next().getSimpleName());
                    lText.append(COLOR_END);
                    if (it.hasNext()) {
                        lText.append(", "); //NOI18N
                    }
                }
                lText.append(')');
            }
            return lText.toString();
        }
        
        protected String getRightHtmlText() {
            return escape(Utilities.getTypeName(memberElem.getKind().isField() ? memberType : ((ExecutableType)memberType).getReturnType(), false).toString());
        }
        
        protected ImageIcon getIcon(){
            int level = getProtectionLevel(memberElem.getModifiers());
            boolean isField = memberElem.getKind().isField();
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

        protected void substituteText(JTextComponent c, int offset, int len, String toAdd) {
            BaseDocument doc = (BaseDocument)c.getDocument();
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
                        sb.append(" default=\"\"}"); //NOI18N
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
            sb.append(memberElem.getSimpleName());
            if (!memberElem.getKind().isField()) {
                sb.append("("); //NOI18N
                Iterator<? extends VariableElement> it = ((ExecutableElement)memberElem).getParameters().iterator();
                Iterator<? extends TypeMirror> tIt = ((ExecutableType)memberType).getParameterTypes().iterator();
                while (it.hasNext() && tIt.hasNext()) {
                    sb.append("${"); //NOI18N
                    sb.append(it.next().getSimpleName());
                    sb.append(" named instanceof="); //NOI18N
                    sb.append(tIt.next().toString());
                    sb.append("}"); //NOI18N
                    if (it.hasNext())
                        sb.append(", "); //NOI18N
                }
                sb.append(")");//NOI18N
            }
            if (toAdd != null && !toAdd.equals("\n")) { //NOI18N
                TokenSequence<JavaTokenId> sequence = Utilities.getJavaTokenSequence(c, offset + len);
                if (sequence == null) {
                    sb.append(toAdd);
                    toAdd = null;
                }
                boolean added = false;
                while(toAdd != null && toAdd.length() > 0) {
                    String tokenText = sequence.token().text().toString();
                    if (tokenText.startsWith(toAdd)) {
                        len = sequence.offset() - offset + toAdd.length();
                        sb.append(toAdd);
                        toAdd = null;
                    } else if (toAdd.startsWith(tokenText)) {
                        sequence.moveNext();
                        len = sequence.offset() - offset;
                        sb.append(toAdd.substring(0, tokenText.length()));
                        toAdd = toAdd.substring(tokenText.length());
                        added = true;
                    } else if (sequence.token().id() == JavaTokenId.WHITESPACE && sequence.token().text().toString().indexOf('\n') < 0) {//NOI18N
                        if (!sequence.moveNext()) {
                            sb.append(toAdd);
                            toAdd = null;
                        }
                    } else {
                        if (!added)
                            sb.append(toAdd);
                        toAdd = null;
                    }
                }
            }
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
            CodeTemplateManager ctm = CodeTemplateManager.get(doc);
            if (ctm != null) {
                ctm.createTemporary(sb.toString()).insert(c);
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for(Modifier mod : memberElem.getModifiers()) {
               sb.append(mod.toString());
               sb.append(' '); // NOI18N
            }
            sb.append(Utilities.getTypeName(memberElem.getKind().isField() ? memberType : ((ExecutableType)memberType).getReturnType(), false));
            sb.append(' ');
            sb.append(Utilities.getTypeName(type, false));
            sb.append('.');
            sb.append(memberElem.getSimpleName());
            if (!memberElem.getKind().isField()) {
                sb.append('('); //NOI18N
                Iterator<? extends VariableElement> it = ((ExecutableElement)memberElem).getParameters().iterator();
                Iterator<? extends TypeMirror> tIt = ((ExecutableType)memberType).getParameterTypes().iterator();
                while(it.hasNext() && tIt.hasNext()) {
                    sb.append(Utilities.getTypeName(tIt.next(), false, ((ExecutableElement)memberElem).isVarArgs() && !tIt.hasNext()));
                    sb.append(' ');
                    sb.append(it.next().getSimpleName());
                    if (it.hasNext()) {
                        sb.append(", "); //NOI18N
                    }
                }
                sb.append(')');
            }
            return sb.toString();
        }
    }
    
    private static class InitializeAllConstructorItem extends JavaCompletionItem {
        
        private static final String CONSTRUCTOR_PUBLIC = "org/netbeans/modules/editor/resources/completion/constructor_16.png"; //NOI18N
        private static final String CONSTRUCTOR_PROTECTED = "org/netbeans/modules/editor/resources/completion/constructor_protected_16.png"; //NOI18N
        private static final String CONSTRUCTOR_PACKAGE = "org/netbeans/modules/editor/resources/completion/constructor_package_private_16.png"; //NOI18N
        private static final String CONSTRUCTOR_PRIVATE = "org/netbeans/modules/editor/resources/completion/constructor_private_16.png"; //NOI18N
        private static final String CONSTRUCTOR_COLOR = "<font color=#b28b00>"; //NOI18N
        private static final String PARAMETER_NAME_COLOR = "<font color=#b200b2>"; //NOI18N
        private static ImageIcon icon[] = new ImageIcon[4];

        private Iterable<VariableElement> fields;
        private TypeElement parent;
        
        private InitializeAllConstructorItem(Iterable<VariableElement> fields, TypeElement parent, int substitutionOffset) {
            super(substitutionOffset);
            this.fields = fields;
            this.parent = parent;
        }
        
        public int getSortPriority() {
            return 400;
        }
        
        public CharSequence getSortText() {
            StringBuilder sortParams = new StringBuilder();
            sortParams.append('('); //NOI18N
            int cnt = 0;
            for(Iterator<? extends VariableElement> it = fields.iterator(); it.hasNext();) {
                sortParams.append(it.next().asType().toString());
                if (it.hasNext()) {
                    sortParams.append(','); //NOI18N
                }
                cnt++;
            }
            sortParams.append(')'); //NOI18N
            return parent.getSimpleName() + "#" + ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString(); //NOI18N
        }
        
        protected String getLeftHtmlText() {
            StringBuilder lText = new StringBuilder();
            lText.append(CONSTRUCTOR_COLOR);
            lText.append("<b>" + parent.getSimpleName() + "</b>"); //NOI18N
            lText.append(COLOR_END);
            lText.append('('); //NOI18N
            Iterator<? extends VariableElement> it = fields.iterator();
            while(it.hasNext()) {
                VariableElement ve = it.next();
                lText.append(escape(Utilities.getTypeName(ve.asType(), false).toString()));
                lText.append(' '); //NOI18N
                lText.append(PARAMETER_NAME_COLOR);
                lText.append(ve.getSimpleName());
                lText.append(COLOR_END);
                if (it.hasNext()) {
                    lText.append(", "); //NOI18N
                }
            }
            lText.append(')'); //NOI18N
            lText.append(" - generate"); //NOI18N
            return lText.toString();
        }
        
        protected ImageIcon getIcon() {
            int level = PUBLIC_LEVEL;
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
        
        public CharSequence getInsertPrefix() {
            return parent.getSimpleName();
        }        
        
        public CompletionTask createDocumentationTask() {
            return null;//JavaCompletionProvider.createDocTask(elem);
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
                js.runModificationTask(new CancellableTask<WorkingCopy>() {
                    public void cancel() {
                    }
                    public void run(WorkingCopy copy) throws IOException {
                        copy.toPhase(JavaSource.Phase.PARSED);
                        TreePath tp = copy.getTreeUtilities().pathFor(offset);
                        Tree t = tp.getLeaf();
                        if (t.getKind() == Tree.Kind.CLASS) {
                            Tree lastBefore = null;
                            for (Tree tree : ((ClassTree) t).getMembers()) {
                                if (copy.getTrees().getSourcePositions().getStartPosition(tp.getCompilationUnit(), tree) < offset) {
                                    lastBefore = tree;
                                } else {
                                    break;
                                }
                            }
                            CreateConstructor.createConstructor(copy, fields, parent, tp, lastBefore, (ClassTree) t);
                        }
                    }
                }).commit();
            } catch (IOException ex) {
            }
        }

        private static class CreateConstructor {
            
            public static void createConstructor(WorkingCopy wc, Iterable<VariableElement> fields, TypeElement parent, TreePath path, Tree putBefore, ClassTree node) {
                assert path.getLeaf() == node;
                TreeMaker make = wc.getTreeMaker();
                TypeElement te = (TypeElement)wc.getTrees().getElement(path);
                if (te != null) {
                    List<Tree> members = new ArrayList<Tree>(node.getMembers());
                    int pos = putBefore != null ? members.indexOf(putBefore) + 1: members.size();
                    
                    Set<Modifier> mods = EnumSet.of(Modifier.PUBLIC);
                    
                    //create body:
                    List<StatementTree> statements = new ArrayList();
                    List<VariableTree> arguments = new ArrayList();

                    for (VariableElement ve : fields) {
                        AssignmentTree a = make.Assignment(make.MemberSelect(make.Identifier("this"), ve.getSimpleName()), make.Identifier(ve.getSimpleName()));

                        statements.add(make.ExpressionStatement(a));
                        arguments.add(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), ve.getSimpleName(), make.Type(ve.asType()), null));
                    }
                    
                    BlockTree body = make.Block(statements, false);

                    ClassTree decl = make.insertClassMember(node, pos, make.Method(make.Modifiers(mods), "<init>", null, Collections.<TypeParameterTree> emptyList(), arguments, Collections.<ExpressionTree>emptyList(), body, null));
                    wc.rewrite(node, decl);
                }
            }            
        }
        
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Modifier mod : EnumSet.of(Modifier.PUBLIC)) {
                sb.append(mod.toString());
                sb.append(' '); //NOI18N
            }
            sb.append(parent.getSimpleName());
            sb.append('('); //NOI18N
            Iterator<? extends VariableElement> it = fields.iterator();
            while(it.hasNext()) {
                VariableElement ve = it.next();
                sb.append(Utilities.getTypeName(ve.asType(), false));
                sb.append(' '); //NOI18N
                sb.append(ve.getSimpleName());
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
}
