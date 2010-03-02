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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.completion.cplusplus.ext;

import java.util.Iterator;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.Utilities;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.api.model.CsmClassForwardDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespaceAlias;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.deep.CsmLabel;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmIncludeResolver;
import org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider;
import org.netbeans.modules.cnd.completion.cplusplus.CsmCompletionUtils;
import org.netbeans.modules.cnd.completion.spi.dynhelp.CompletionDocumentationProvider;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.modelutil.CsmPaintComponent;
import org.netbeans.modules.cnd.modelutil.ParamStr;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.openide.util.Lookup;

/**
 *
 * @author  Vladimir Voskresensky
 * after JCResultItem
 */
public abstract class CsmResultItem implements CompletionItem {

    private static boolean enableInstantSubstitution = true;
    protected int selectionStartOffset = -1;
    protected int selectionEndOffset = -1;
    protected int substituteOffset = -1;
    CsmObject associatedObject;
    private static final Color KEYWORD_COLOR = Color.gray;
    private static final Color TYPE_COLOR = Color.black;
    private int priority;

    protected CsmResultItem(CsmObject associatedObject, int priority) {
        this.associatedObject = associatedObject;
        this.priority = priority;
    }

    public abstract String getItemText();

    public Object getAssociatedObject() {
        return associatedObject;
    }

    protected static Color getTypeColor(CsmType type) {
        return type.isBuiltInBased(false) ? KEYWORD_COLOR : TYPE_COLOR;
    }

    public void setSubstituteOffset(int substituteOffset) {
        this.substituteOffset = substituteOffset;
    }

    public boolean substituteCommonText(JTextComponent c, int offset, int len, int subLen) {
        // [PENDING] not enough info in parameters...
        // commonText
        // substituteExp
        return false;
    }

    public boolean substituteText(final JTextComponent c, final int offset, final int len, final boolean shift) {
        final BaseDocument doc = (BaseDocument) c.getDocument();
        final String text = getReplaceText();
        final boolean res[] = new boolean[]{true};
        if (text != null) {
            // Update the text
            doc.runAtomic(new Runnable() {

                public void run() {
                    try {
                        CharSequence textToReplace = DocumentUtilities.getText(doc, offset, len);
                        if (CharSequenceUtilities.textEquals(text, textToReplace)) {
                            res[0] = false;
                            return;
                        }

                        doc.remove(offset, len);
                        doc.insertString(offset, text, null);
                        if (selectionStartOffset >= 0) {
                            c.select(offset + selectionStartOffset,
                                    offset + selectionEndOffset);
                        }
                    } catch (BadLocationException e) {
                        // Can't update
                    }
                }
            });
        }

        return res[0];
    }

    public java.awt.Component getPaintComponent(javax.swing.JList list, boolean isSelected, boolean cellHasFocus) {
        Component ret;
        ret = getPaintComponent(isSelected);
        if (ret == null) {
            return null;
        }
        if (isSelected) {
            ret.setBackground(list.getSelectionBackground());
            ret.setForeground(list.getSelectionForeground());
        } else {
            ret.setBackground(list.getBackground());
            ret.setForeground(list.getForeground());
        }
        ret.getAccessibleContext().setAccessibleName(getItemText());
        ret.getAccessibleContext().setAccessibleDescription(getItemText());
        return ret;
    }

    protected abstract Component getPaintComponent(boolean isSelected);

    // CompletionItem implementation
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        Component renderComponent = getPaintComponent(false);
        renderComponent.setFont(defaultFont);
        int width = renderComponent.getPreferredSize().width;
        return width;
    }

    public void render(Graphics g, Font defaultFont, Color defaultColor,
            Color backgroundColor, int width, int height, boolean selected) {
        Component renderComponent = getPaintComponent(selected);
        renderComponent.setFont(defaultFont);
        renderComponent.setForeground(defaultColor);
        renderComponent.setBackground(backgroundColor);
        renderComponent.setBounds(0, 0, width, height);
        ((CsmPaintComponent) renderComponent).paintComponent(g);
    }

    protected static String getTypeName(CsmType type, boolean instantiateTypes) {
        CharSequence text;
        if (instantiateTypes) {
            text = CsmInstantiationProvider.getDefault().getInstantiatedText(type);
        } else {
            text = type.getText();
        }
        return text.toString();
    }

    /**
     * Used for testing only
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        Component comp = getPaintComponent(false);
        return comp != null ? comp.toString() : ""; //NOI18N
    }

    protected int convertCsmModifiers(CsmObject obj) {
        return CsmUtilities.getModifiers(obj);
    }
    public static final String COMPLETION_SUBSTITUTE_TEXT = "completion-substitute-text"; //NOI18N
    private String toAdd;

    public void processKeyEvent(KeyEvent evt) {
        if (evt.getID() == KeyEvent.KEY_TYPED) {
            Completion completion = Completion.get();
            switch (evt.getKeyChar()) {
                case ' ':
                    if (evt.getModifiers() == 0) {
                        completion.hideCompletion();
                        completion.hideDocumentation();
                    }
                    break;
                case ';':
                case ',':
                case '+':
                case '-':
                case '=':
                case '/':
                case '*':
                case '%':
                case ':':
                    completion.hideCompletion();
                    completion.hideDocumentation();
                    break;
                case '.':
                    if (defaultAction((JTextComponent) evt.getSource(), Character.toString(evt.getKeyChar()))) {
                        evt.consume();
                        break;
                    }
            }
        }
    }

    public CharSequence getSortText() {
        return getItemText();
    }

    public CharSequence getInsertPrefix() {
        return getItemText();
    }

    public CompletionTask createDocumentationTask() {
        CompletionDocumentationProvider p = Lookup.getDefault().lookup(CompletionDocumentationProvider.class);
        return p != null ? p.createDocumentationTask(this) : null;
    }

    public CompletionTask createToolTipTask() {
        return null;
    }

    public static void setEnableInstantSubstitution(boolean enable) {
        CsmResultItem.enableInstantSubstitution = enable;
    }

    public boolean instantSubstitution(JTextComponent c) {
        if (CsmResultItem.enableInstantSubstitution) {
            Completion completion = Completion.get();
            completion.hideCompletion();
            completion.hideDocumentation();
            defaultAction(c);
            return true;
        } else {
            return false;
        }
    }

    public void defaultAction(JTextComponent component) {
        Completion completion = Completion.get();
        completion.hideCompletion();
        completion.hideDocumentation();
        defaultAction(component, "");
    }

    boolean defaultAction(JTextComponent component, String addText) {
        int substOffset = substituteOffset;
        if (substOffset == -1) {
            substOffset = component.getCaret().getDot();
        }
        toAdd = addText;
        if (substituteText(component, substOffset, component.getCaret().getDot() - substOffset, false)) {
            if (CsmCompletionUtils.isAutoInsertIncludeDirectives()) {
                CsmIncludeResolver inclResolver = CsmIncludeResolver.getDefault();
                BaseDocument doc = (BaseDocument) component.getDocument();
                Object ob = getAssociatedObject();
                if (CsmKindUtilities.isCsmObject(ob)) {
                    CsmFile currentFile = CsmUtilities.getCsmFile(doc, false, false);
                    if (!inclResolver.isObjectVisible(currentFile, (CsmObject) ob)) {
                        String include = inclResolver.getIncludeDirective(currentFile, (CsmObject) ob);
                        if (include.length() != 0 && !isForwardDeclaration(component) && !isAlreadyIncluded(component, include)) {
                            insertInclude(component, currentFile, include, include.charAt(include.length() - 1) == '>');
                        }
                    }
                } else {
                    System.err.println("not yet handled object " + ob);
                }
            }
            return true;
        } else {
            return false;
        }

    }

    // Checks that include directive have not been already included
    // It needs in case if some files have not been parsed yet
    private boolean isAlreadyIncluded(JTextComponent component, String include) {
        TokenSequence<CppTokenId> ts;
        ts = CndLexerUtilities.getCppTokenSequence(component, 0, false, false);
        ts.moveStart();
        while (ts.moveNext()) {
            if (ts.token().id().equals(CppTokenId.PREPROCESSOR_DIRECTIVE)) {
                StringBuilder buf = new StringBuilder(ts.token().text());
                if (isIncludesEqual(include, buf.toString())) {
                    return true;
                }
            }
        }
        return false;
    }

    // Compares include directives dy file names
    private boolean isIncludesEqual(String inc1, String inc2) {
        inc1 = normalizeInclude(inc1);
        inc2 = normalizeInclude(inc2);
        return (inc1.equals(inc2));
    }

    // Normailizes include directive string
    private String normalizeInclude(String inc) {
        inc = inc.toLowerCase();
        inc = inc.replaceAll("[\\s\n]+", " "); // NOI18N
        inc = inc.replaceAll("[<>\"]", "\""); // NOI18N
        inc = inc.trim();
        return inc;
    }

    // Says is it forward declarartion or not
    private boolean isForwardDeclaration(JTextComponent component) {
        TokenSequence<CppTokenId> ts;
        ts = CndLexerUtilities.getCppTokenSequence(component, 0, false, false);
        ts.moveStart();
        if (!ts.moveNext()) {
            return false;
        }
        CppTokenId lastID = ts.token().id();
        while (ts.offset() < substituteOffset) {
            switch (ts.token().id()) {
                case BLOCK_COMMENT:
                case DOXYGEN_COMMENT:
                case NEW_LINE:
                case LINE_COMMENT:
                case DOXYGEN_LINE_COMMENT:
                case WHITESPACE:
                    // skip
                    break;
                default:
                    lastID = ts.token().id();
                    break;
            }
            if (!ts.moveNext()) {
                return false;
            }
        }
        switch (lastID) {
            case CLASS:
            case STRUCT:
            case UNION:
                return true;
        }
        return false;
    }

    // Inserts include derctive into document
    private void insertInclude(final JTextComponent component, final CsmFile currentFile, final String include, final boolean isSystem) {
        final BaseDocument doc = (BaseDocument) component.getDocument();
        CsmInclude lastInclude = null;
        boolean isLastIncludeTypeMatch = false;
        for (CsmInclude inc : currentFile.getIncludes()) {
            if (inc.isSystem() == isSystem) {
                lastInclude = inc;
                isLastIncludeTypeMatch = true;
            } else {
                if (lastInclude == null || (!isLastIncludeTypeMatch && !isSystem)) {
                    lastInclude = inc;
                }
            }
        }
        final CsmInclude lastInclude2 = lastInclude;
        final boolean isLastIncludeTypeMatch2 = isLastIncludeTypeMatch;
        doc.runAtomic(new Runnable() {

            public void run() {
                try {
                    if (lastInclude2 != null) {
                        if (isLastIncludeTypeMatch2) {
                            doc.insertString(lastInclude2.getEndOffset(), "\n" + include, null); // NOI18N
                        } else if (!isSystem) {
                            doc.insertString(lastInclude2.getEndOffset(), "\n\n" + include, null); // NOI18N
                        } else {
                            doc.insertString(lastInclude2.getStartOffset(), include + "\n\n", null); // NOI18N
                        }
                    } else {
                        CsmFileInfoQuery fiq = CsmFileInfoQuery.getDefault();
                        CsmOffsetable guardOffset = fiq.getGuardOffset(currentFile);
                        TokenSequence<CppTokenId> ts;
                        if (guardOffset != null) {
                            ts = CndLexerUtilities.getCppTokenSequence(component, guardOffset.getStartOffset(), false, false);
                        } else {
                            ts = CndLexerUtilities.getCppTokenSequence(component, 0, false, false);
                        }
                        if (ts != null) {
                            int offset = getIncludeOffsetFromTokenSequence(ts);
                            if (offset == 0 || guardOffset != null) {
                                doc.insertString(offset, "\n" + include + "\n\n", null); // NOI18N
                            } else {
                                doc.insertString(offset, "\n\n" + include + "\n", null); // NOI18N
                            }
                        }
                    }
                } catch (BadLocationException e) {
                    // Can't update
                }
            }
        });
    }

    // Finds place for include insertion in case if there is no other includes in document
    private int getIncludeOffsetFromTokenSequence(TokenSequence<CppTokenId> ts) {
        if (!ts.moveNext()) {
            return 0;
        }
        int offset = ts.offset();

        if (offset != 0) {
            if (ts.token().id().equals(CppTokenId.PREPROCESSOR_DIRECTIVE)) {
                if (!ts.moveNext()) {
                    return 0;
                }
                offset = ts.offset();
                if (ts.token().id().equals(CppTokenId.PREPROCESSOR_DIRECTIVE)) {
                    if (!ts.moveNext()) {
                        return 0;
                    }
                    offset = ts.offset();
                }
            }
        } else {
            while (ts.token().id().equals(CppTokenId.WHITESPACE) ||
                    ts.token().id().equals(CppTokenId.NEW_LINE)) {
                if (!ts.moveNext()) {
                    return offset;
                }
            }
            if (ts.token().id().equals(CppTokenId.BLOCK_COMMENT) ||
                    ts.token().id().equals(CppTokenId.DOXYGEN_COMMENT)) {
                if (!ts.moveNext()) {
                    return offset;
                }
                int firstCommentEndOffset = ts.offset();
                int newLineNumber = 0;
                while (ts.token().id().equals(CppTokenId.WHITESPACE) ||
                        ts.token().id().equals(CppTokenId.NEW_LINE)) {
                    if (ts.token().id().equals(CppTokenId.NEW_LINE)) {
                        newLineNumber++;
                    }
                    if (!ts.moveNext()) {
                        return offset;
                    }
                }
                if (ts.token().id().equals(CppTokenId.BLOCK_COMMENT) ||
                        ts.token().id().equals(CppTokenId.DOXYGEN_COMMENT)) {
                    return firstCommentEndOffset;
                } else {
                    if (newLineNumber > 1) {
                        return firstCommentEndOffset;
                    }
                }
            }
        }
        return offset;
    }

    protected String getReplaceText() {
        return getItemText();
    }

    public int getSortPriority() {
        return this.priority;
    }

    public static class FileLocalVariableResultItem extends VariableResultItem {

        public FileLocalVariableResultItem(CsmVariable fld, int priotity) {
            super(fld, priotity);
        }

        protected CsmPaintComponent.FieldPaintComponent createPaintComponent() {
            return new CsmPaintComponent.FileLocalVariablePaintComponent();
        }
    }

    public static class GlobalVariableResultItem extends VariableResultItem {

        public GlobalVariableResultItem(CsmVariable fld, int priotity) {
            super(fld, priotity);
        }

        protected CsmPaintComponent.FieldPaintComponent createPaintComponent() {
            return new CsmPaintComponent.GlobalVariablePaintComponent();
        }
    }

    public static class LocalVariableResultItem extends VariableResultItem {

        public LocalVariableResultItem(CsmVariable fld, int priotity) {
            super(fld, priotity);
        }

        protected CsmPaintComponent.FieldPaintComponent createPaintComponent() {
            return new CsmPaintComponent.LocalVariablePaintComponent();
        }
    }

    public static class FieldResultItem extends VariableResultItem {

        public FieldResultItem(CsmField fld, int priotity) {
            super(fld, priotity);
        }

        protected CsmPaintComponent.FieldPaintComponent createPaintComponent() {
            return new CsmPaintComponent.FieldPaintComponent();
        }
    }

    public static class MacroResultItem extends CsmResultItem {

        private CharSequence macName;
        private List<CharSequence> params;
        private static CsmPaintComponent.MacroPaintComponent macroPaintComp = null;

        public MacroResultItem(CsmMacro mac, int priotity) {
            super(mac, priotity);
            this.macName = mac.getName();
            this.params = mac.getParameters();
        }

        private String getName() {
            return macName.toString();
        }

        private List<CharSequence> getParams() {
            return params;
        }

        public String getItemText() {
            return getName();
        }

        protected CsmPaintComponent.MacroPaintComponent createPaintComponent() {
            return new CsmPaintComponent.MacroPaintComponent();
        }

        public Component getPaintComponent(boolean isSelected) {
            if (macroPaintComp == null) {
                macroPaintComp = createPaintComponent();
            }
            macroPaintComp.setName(getName());
            macroPaintComp.setParams(getParams());
            macroPaintComp.setSelected(isSelected);
            return macroPaintComp;
        }
    }

    public static class TemplateParameterResultItem extends CsmResultItem {

        private CharSequence parName;
        private static CsmPaintComponent.TemplateParameterPaintComponent parPaintComp = null;

        public TemplateParameterResultItem(CsmTemplateParameter par, int priotity) {
            super(par, priotity);
            this.parName = par.getName();
        }

        private String getName() {
            return parName.toString();
        }

        public String getItemText() {
            return getName();
        }

        protected CsmPaintComponent.TemplateParameterPaintComponent createPaintComponent() {
            return new CsmPaintComponent.TemplateParameterPaintComponent();
        }

        public Component getPaintComponent(boolean isSelected) {
            if (parPaintComp == null) {
                parPaintComp = createPaintComponent();
            }
            parPaintComp.setName(getName());
            parPaintComp.setSelected(isSelected);
            return parPaintComp;
        }
    }

    public static class LabelResultItem extends CsmResultItem {

        private CharSequence parName;
        private static CsmPaintComponent.LabelPaintComponent parPaintComp = null;

        public LabelResultItem(CsmLabel par, int priotity) {
            super(par, priotity);
            this.parName = par.getLabel();
        }

        private String getName() {
            return parName.toString();
        }

        public String getItemText() {
            return getName();
        }

        protected CsmPaintComponent.LabelPaintComponent createPaintComponent() {
            return new CsmPaintComponent.LabelPaintComponent();
        }

        public Component getPaintComponent(boolean isSelected) {
            if (parPaintComp == null) {
                parPaintComp = createPaintComponent();
            }
            parPaintComp.setName(getName());
            parPaintComp.setSelected(isSelected);
            return parPaintComp;
        }
    }

    public abstract static class VariableResultItem extends CsmResultItem {

        private CharSequence typeName;
        private Color typeColor;
        private CharSequence fldName;
        private int modifiers;
        private static CsmPaintComponent.FieldPaintComponent fieldComponent = null;
        private static CsmPaintComponent.FieldPaintComponent globVarComponent = null;
        private static CsmPaintComponent.FieldPaintComponent localVarComponent = null;
        private static CsmPaintComponent.FieldPaintComponent fileLocalVarComponent = null;

        public VariableResultItem(CsmVariable fld, int priotity) {
            super(fld, priotity);
            this.fldName = fld.getName();
            this.modifiers = convertCsmModifiers(fld);
            this.typeName = getTypeName(fld.getType(), false);
            this.typeColor = getTypeColor(fld.getType());
        }

        public String getItemText() {
            return fldName.toString();
        }

        abstract protected CsmPaintComponent.FieldPaintComponent createPaintComponent();

        public java.awt.Component getPaintComponent(boolean isSelected) {
            CsmPaintComponent.FieldPaintComponent comp = null;
            assert (CsmKindUtilities.isCsmObject(getAssociatedObject())) : "must be csm object"; //NOI18N
            CsmObject var = (CsmObject) getAssociatedObject();
            if (CsmKindUtilities.isField(var)) {
                if (fieldComponent == null) {
                    fieldComponent = createPaintComponent();
                }
                comp = fieldComponent;
            } else if (CsmKindUtilities.isGlobalVariable(var)) {
                if (globVarComponent == null) {
                    globVarComponent = createPaintComponent();
                }
                comp = globVarComponent;
            } else if (CsmKindUtilities.isFileLocalVariable(var)) {
                if (fileLocalVarComponent == null) {
                    fileLocalVarComponent = createPaintComponent();
                }
                comp = fileLocalVarComponent;
            } else {
                assert (CsmKindUtilities.isLocalVariable(var)) :
                        "support only global var, local var, file local var and class fields"; //NOI18N
                if (localVarComponent == null) {
                    localVarComponent = createPaintComponent();
                }
                comp = localVarComponent;
            }
            comp.setTypeName(typeName.toString());
            comp.setName(fldName.toString());
            comp.setTypeColor(typeColor);
            comp.setModifiers(modifiers);
            comp.setSelected(isSelected);

            return comp;
        }
    }

    public static class FileLocalFunctionResultItem extends MethodResultItem {

        public FileLocalFunctionResultItem(CsmFunction mtd, CsmCompletionExpression substituteExp, int priotity, boolean isDeclaration, boolean instantiateTypes) {
            super(mtd, substituteExp, priotity, isDeclaration, instantiateTypes);
        }

        @Override
        protected CsmPaintComponent.ConstructorPaintComponent createPaintComponent() {
            return new CsmPaintComponent.FileLocalFunctionPaintComponent();
        }
    }

    public static class GlobalFunctionResultItem extends MethodResultItem {

        public GlobalFunctionResultItem(CsmFunction mtd, CsmCompletionExpression substituteExp, int priotity, boolean isDeclaration, boolean instantiateTypes) {
            super(mtd, substituteExp, priotity, isDeclaration, instantiateTypes);
        }

        @Override
        protected CsmPaintComponent.ConstructorPaintComponent createPaintComponent() {
            return new CsmPaintComponent.GlobalFunctionPaintComponent();
        }

        // Checks is it function call or usage of function as a pointer
        // IZ 145380 : In code completion is missing command endl (without any parenthesis)
        @Override
        protected boolean isFunctionAsPointer(JTextComponent c, String funName) {
            if ((funName.startsWith("endl") || // NOI18N
                    funName.startsWith("ends") || // NOI18N
                    funName.startsWith("flush") || // NOI18N
                    funName.startsWith("getline") || // NOI18N
                    funName.startsWith("ws")) && // NOI18N
                    isAfterShiftOperator(c)) {
                return true;
            }
            return false;
        }

        private boolean isAfterShiftOperator(JTextComponent c) {
            TokenSequence<CppTokenId> ts;
            ts = CndLexerUtilities.getCppTokenSequence(c, 0, true, false);
            ts.moveStart();
            if (!ts.moveNext()) {
                return false;
            }
            boolean result = false;
            while (ts.offset() < substituteOffset) {
                CppTokenId id = ts.token().id();
                switch (id) {
                    case LTLT:
                    case GTGT:
                        result = true;
                        break;
                    default:
                        switch (id) {
                            case IDENTIFIER:
                            case SCOPE:
                            case BLOCK_COMMENT:
                            case DOXYGEN_COMMENT:
                            case NEW_LINE:
                            case LINE_COMMENT:
                            case DOXYGEN_LINE_COMMENT:
                            case WHITESPACE:
                                break;
                            default:
                                result = false;
                        }
                }
                if (!ts.moveNext()) {
                    return false;
                }
            }
            return result;
        }
    }

    public static class MethodResultItem extends ConstructorResultItem {

        private static CsmPaintComponent.MethodPaintComponent mtdComponent = null;
        private static CsmPaintComponent.MethodPaintComponent globFunComponent = null;
        private CharSequence typeName;
        private Color typeColor;

        public MethodResultItem(CsmFunction mtd, CsmCompletionExpression substituteExp, int priotity, boolean isDeclaration, boolean instantiateTypes) {
            super(mtd, substituteExp, priotity, isDeclaration, instantiateTypes);
            typeName = CsmResultItem.getTypeName(mtd.getReturnType(), instantiateTypes);
            typeColor = CsmResultItem.getTypeColor(mtd.getReturnType());
        }

        public String getTypeName() {
            return typeName.toString();
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        public Color getTypeColor() {
            return typeColor;
        }

        public void setTypeColor(Color typeColor) {
            this.typeColor = typeColor;
        }

        @Override
        protected CsmPaintComponent.ConstructorPaintComponent createPaintComponent() {
            return new CsmPaintComponent.MethodPaintComponent();
        }

        @Override
        public Component getPaintComponent(boolean isSelected) {
            CsmPaintComponent.MethodPaintComponent comp = null;
            assert (CsmKindUtilities.isCsmObject(getAssociatedObject())) : "must be csm object"; //NOI18N
            CsmObject mtd = (CsmObject) getAssociatedObject();
            if (CsmKindUtilities.isMethod(mtd)) {
                if (mtdComponent == null) {
                    mtdComponent = (CsmPaintComponent.MethodPaintComponent) createPaintComponent();
                }
                comp = mtdComponent;
            } else {
                assert (CsmKindUtilities.isGlobalFunction(mtd)) : "support only global fun and class methods"; //NOI18N
                if (globFunComponent == null) {
                    globFunComponent = (CsmPaintComponent.MethodPaintComponent) createPaintComponent();
                }
                comp = globFunComponent;
            }

            comp.setName(getName());
            comp.setModifiers(getModifiers());
            comp.setTypeName(getTypeName());
            comp.setTypeColor(getTypeColor());
            comp.setParams(getParams());
            comp.setExceptions(getExceptions());
            comp.setSelected(isSelected);
            return comp;
        }
    }

    public static class ConstructorResultItem extends CsmResultItem {

        private CsmFunction ctr;
        private CsmCompletionExpression substituteExp;
        private final boolean isDeclaration;
        private List<ParamStr> params = new ArrayList<ParamStr>();
        private List<?> excs = new ArrayList<Object>();
        private int modifiers;
        private static CsmPaintComponent.ConstructorPaintComponent ctrComponent = null;
        private int activeParameterIndex = -1;
        private int varArgIndex = -1;
        private final CharSequence mtdName;

        public ConstructorResultItem(CsmFunction ctr, CsmCompletionExpression substituteExp, int priority, boolean isDeclaration, boolean instantiateTypes) {
            super(ctr, priority);
            this.ctr = ctr;
            this.substituteExp = substituteExp;
            this.isDeclaration = isDeclaration;
            this.modifiers = convertCsmModifiers(ctr);
            if (CsmKindUtilities.isTemplate(ctr)) {
                mtdName = ((CsmTemplate) ctr).getDisplayName();
            } else {
                mtdName = ctr.getName();
            }
            int i = 0;
            for (Object prm : ctr.getParameters()) {
                if (prm == null) {
                    continue;
                }
                CsmType type = ((CsmParameter) prm).getType();
                if (type == null) {
                    // only var args parameters could have null types
                    assert (((CsmParameter) prm).isVarArgs());
                    params.add(new ParamStr("", "", ((CsmParameter) prm).getName().toString(), true, KEYWORD_COLOR)); //NOI18N
                    varArgIndex = i;
                } else {
                    String typeName = getTypeName(type, instantiateTypes);
                    params.add(new ParamStr(typeName, typeName, ((CsmParameter) prm).getName().toString(), false, TYPE_COLOR /*getTypeColor(type.getClassifier())*/));
                }
                i++;
            }
        // TODO
//            CsmClass excepts[] = ctr.getExceptions();
//            for (int i=0; i<excepts.length; i++) {
//                CsmClass ex = (CsmClass) excepts[i];
//                excs.add(new ExcStr(ex.getName(), getTypeColor(ex)));
//            }

        }

        public CsmCompletionExpression getExpression() {
            return this.substituteExp;
        }

        int getActiveParameterIndex() {
            return activeParameterIndex;
        }

        /**
         * If set to value different than -1 it marks that
         * this component renders an outer enclosing constructor/method
         * and the given index is the index of the active parameter
         * which is being completed as an inner expression.
         */
        void setActiveParameterIndex(int activeParamIndex) {
            this.activeParameterIndex = activeParamIndex;
        }

        public int getModifiers() {
            return modifiers;
        }

        public String getName() {
            // TODO review the output
            return mtdName.toString();
        }

        public List<ParamStr> getParams() {
            return params;
        }

        public List<?> getExceptions() {
            return excs;
        }

        public int getCurrentParamIndex() {
            int idx = 0;
            if (substituteExp != null && substituteExp.getExpID() == CsmCompletionExpression.METHOD_OPEN) {
                idx = substituteExp.getParameterCount() - 1;
            }
            if (varArgIndex > -1 && varArgIndex < idx) {
                idx = varArgIndex;
            }
            return idx;
        }

        public List<String> createParamsList() {
            List<String> ret = new ArrayList<String>();
            for (Iterator<ParamStr> it = getParams().iterator(); it.hasNext();) {
                StringBuilder sb = new StringBuilder();
                ParamStr ps = it.next();
                sb.append(ps.getSimpleTypeName());
                if (ps.isVarArg()) {
                    sb.append("..."); // NOI18N
                } else {
                    String name = ps.getName();
                    if (name != null && name.length() > 0) {
                        sb.append(" "); // NOI18N
                        sb.append(name);
                    }
                }
                if (it.hasNext()) {
                    sb.append(", "); // NOI18N
                }
                ret.add(sb.toString());
            }
            return ret;
        }

        @Override
        public boolean substituteText(final JTextComponent c, final int offset, final int origLen, final boolean shift) {
            final boolean res[] = new boolean[]{true};
            final AtomicBoolean showTooltip = new AtomicBoolean();
            final BaseDocument doc = (BaseDocument) c.getDocument();
            final CsmResultItem thisItem = this;
            doc.runAtomic(new Runnable() {

                public void run() {
                    String text = null;
                    boolean addParams = true;
                    int len = origLen;

                    switch ((substituteExp != null) ? substituteExp.getExpID() : -1) {
                        case CsmCompletionExpression.METHOD:
                            // no subst
                            break;

                        case CsmCompletionExpression.METHOD_OPEN:
                            int parmsCnt = params.size();
                            if (parmsCnt == 0) {
                                if (getActiveParameterIndex() == -1) { // not showing active parm
                                    try {
                                        int fnwpos = Utilities.getFirstNonWhiteFwd(doc, offset + len);
                                        if (fnwpos > -1 && doc.getChars(fnwpos, 1)[0] == ')') { // NOI18N
                                            text = doc.getText(offset + len, fnwpos + 1 - offset - len);
                                            len = fnwpos + 1 - offset;
                                        }
                                    } catch (BadLocationException e) {
                                    }
                                    if (text == null) {
                                        text = ")";  // NOI18N
                                    }
                                }

                            } else { // one or more parameters
                                int activeParamIndex = getActiveParameterIndex();
                                if (activeParamIndex != -1) { // Active parameter being shown
                                    boolean substed = false;
                                    if (activeParamIndex < parmsCnt) {
                                        String paramName = (params.get(activeParamIndex)).getName();
                                        if (paramName != null) {
                                            try {
                                                // Fill in the parameter's name
                                                doc.insertString(c.getCaretPosition(), paramName, null);
                                                substed = true;
                                            } catch (BadLocationException e) {
                                                // Can't insert
                                            }
                                        }
                                    }
                                    res[0] = substed;
                                }
                                int ind = substituteExp.getParameterCount() - 1;
                                boolean addSpace = CodeStyle.getDefault(doc).spaceAfterComma();
                                try {
                                    if (addSpace && (ind == 0 || (offset > 0 && Character.isWhitespace(DocumentUtilities.getText(doc, offset - 1, 1).charAt(0))))) {
                                        addSpace = false;
                                    }
                                } catch (BadLocationException e) {
                                }

                                boolean isVarArg = parmsCnt > 0 ? (params.get(parmsCnt - 1)).isVarArg() : false;
                                if (ind < parmsCnt || isVarArg) {
                                    text = addSpace ? " " : ""; // NOI18N
                                }
                            }
                            break;

                        default:
                            text = getItemText();
                            boolean addSpace = CodeStyle.getDefault(doc).spaceBeforeMethodCallParen();//getFormatSpaceBeforeParenthesis();
                            boolean addClosingParen = false;

                            String mimeType = CsmCompletionUtils.getMimeType(doc);
                            if (mimeType != null) {
                                Preferences prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
                                addClosingParen = prefs.getBoolean(SimpleValueNames.COMPLETION_PAIR_CHARACTERS, false);
                            }

                            if (addParams) {
                                addParams = !isFunctionAsPointer(c, text);
                            }
                            if (addParams) {
                                String paramsText = null;
                                try {
                                    int fnwpos = Utilities.getFirstNonWhiteFwd(doc, offset + len);
                                    if (fnwpos > -1 && fnwpos <= Utilities.getRowEnd(doc, offset + len) && doc.getChars(fnwpos, 1)[0] == '(') { // NOI18N
                                        paramsText = doc.getText(offset + len, fnwpos + 1 - offset - len);
                                        if (addSpace && paramsText.length() < 2) {
                                            text += ' ';
                                        } // NOI18N
                                        len = fnwpos + 1 - offset;
                                        text += paramsText;
                                        thisItem.toAdd = null; // do not add '.', ',', ';'
                                    }
                                } catch (BadLocationException e) {
                                }
                                if (paramsText == null) {
                                    if (addSpace) {
                                        text += ' '; // NOI18N
                                    }
                                    text += '('; // NOI18N
                                    if (params.size() > 0) {
                                        selectionStartOffset = selectionEndOffset = text.length();
                                        showTooltip.set(true);
                                    }
                                    if (isDeclaration) {
                                        for (Object obj : createParamsList()) {
                                            text += obj;
                                        }
                                    }
                                    if (isDeclaration || addClosingParen) {
                                        text += ")";  // NOI18N
                                        if (isDeclaration && CsmKindUtilities.isMethod(ctr) && ((CsmMethod) ctr).isConst()) {
                                            // Fix for IZ#143117: Method autocompletion does not add 'const' keyword
                                            text += " const"; // NOI18N
                                        }
                                    }
                                } else {
                                    try {
                                        int fnwpos = Utilities.getFirstNonWhiteFwd(doc, offset + len);
                                        if (fnwpos > -1 && doc.getChars(fnwpos, 1)[0] == ')') { // NOI18N
                                            paramsText = doc.getText(offset + len, fnwpos + 1 - offset - len);
                                            len = fnwpos + 1 - offset;
                                            if (params.size() > 0) {
                                                selectionStartOffset = selectionEndOffset = text.length();
                                            }
                                            text += paramsText;
                                        }
                                    } catch (BadLocationException e) {
                                    }
                                }
                            }
                            break;
                    }

                    if (text != null) {
                        if (thisItem.toAdd != null && !thisItem.toAdd.equals("\n") && !"(".equals(thisItem.toAdd)) // NOI18N
                        {
                            text += thisItem.toAdd;
                        }
                        // Update the text
                        try {
                            CharSequence textToReplace = DocumentUtilities.getText(doc, offset, len);
                            if (CharSequenceUtilities.textEquals(text, textToReplace)) {
                                c.setCaretPosition(offset + len);
                                res[0] = false;
                            }
                            doc.remove(offset, len);
                            doc.insertString(offset, text, null);
                            if (isDeclaration) {
                                c.setCaretPosition(offset + text.length());
                            } else if (selectionStartOffset >= 0) {
                                c.select(offset + selectionStartOffset,
                                        offset + selectionEndOffset);
                            } else if ("(".equals(thisItem.toAdd)) { // NOI18N
                                int index = text.lastIndexOf(')');
                                if (index > -1) {
                                    c.setCaretPosition(offset + index);
                                }
                            }
                        } catch (BadLocationException e) {
                            // Can't update
                        }
                        res[0] = true;
                    } else {
                        res[0] = false;
                    }
                }
            });
            if (showTooltip.get()) {
                Completion completion = Completion.get();
                completion.hideCompletion();
                completion.hideDocumentation();
                completion.showToolTip();
            }
            return res[0];
        }

        // Checks is it function call or usage of function as a pointer
        protected boolean isFunctionAsPointer(JTextComponent c, String funName) {
            return false;
        }

        public String getItemText() {
            // TODO review the output
            return ctr.getName().toString();
        }

        protected CsmPaintComponent.ConstructorPaintComponent createPaintComponent() {
            return new CsmPaintComponent.ConstructorPaintComponent();
        }

        public Component getPaintComponent(boolean isSelected) {
            if (ctrComponent == null) {
                ctrComponent = createPaintComponent();
            }
            ctrComponent.setName(getItemText());
            ctrComponent.setModifiers(getModifiers());
            ctrComponent.setParams(getParams());
            ctrComponent.setExceptions(getExceptions());
            ctrComponent.setSelected(isSelected);
            return ctrComponent;
        }
    }

    public static class NamespaceAliasResultItem extends CsmResultItem {

        private CharSequence aliasName;
        private static CsmPaintComponent.NamespaceAliasPaintComponent aliasComponent;

        public NamespaceAliasResultItem(CsmNamespaceAlias alias, boolean displayFullNamespacePath, int priotity) {
            super(alias, priotity);
            this.aliasName = alias.getAlias();
        }

        public String getItemText() {
            return aliasName.toString();
        }

        protected CsmPaintComponent.NamespaceAliasPaintComponent createPaintComponent() {
            return new CsmPaintComponent.NamespaceAliasPaintComponent();
        }

        public Component getPaintComponent(boolean isSelected) {
            if (aliasComponent == null) {
                aliasComponent = createPaintComponent();
            }
            aliasComponent.setSelected(isSelected);
            aliasComponent.setAliasName(aliasName.toString());
            return aliasComponent;
        }
    }

    public static class NamespaceResultItem extends CsmResultItem {

        private boolean displayFullNamespacePath;
        private CsmNamespace pkg;
        private CharSequence pkgName;
        private static CsmPaintComponent.NamespacePaintComponent pkgComponent;

        public NamespaceResultItem(CsmNamespace pkg, boolean displayFullNamespacePath, int priotity) {
            super(pkg, priotity);
            this.pkg = pkg;
            this.displayFullNamespacePath = displayFullNamespacePath;
            this.pkgName = pkg.getName();
        }

        public String getItemText() {
            return displayFullNamespacePath ? pkg.getQualifiedName().toString() : pkg.getName().toString();
        }

        protected CsmPaintComponent.NamespacePaintComponent createPaintComponent() {
            return new CsmPaintComponent.NamespacePaintComponent();
        }

        public Component getPaintComponent(boolean isSelected) {
            if (pkgComponent == null) {
                pkgComponent = createPaintComponent();
            }
            pkgComponent.setSelected(isSelected);
            pkgComponent.setNamespaceName(pkgName.toString());
            pkgComponent.setDisplayFullNamespacePath(displayFullNamespacePath);
            return pkgComponent;
        }
    }

    public static class EnumResultItem extends CsmResultItem {

        private CsmEnum enm;
        private int classDisplayOffset;
        private boolean displayFQN;
        private static CsmPaintComponent.EnumPaintComponent enumComponent = null;

        public EnumResultItem(CsmEnum enm, boolean displayFQN, int priotity) {
            this(enm, 0, displayFQN, priotity);
        }

        public EnumResultItem(CsmEnum enm, int classDisplayOffset, boolean displayFQN, int priotity) {
            super(enm, priotity);
            this.enm = enm;
            this.classDisplayOffset = classDisplayOffset;
            this.displayFQN = displayFQN;
        }

        protected String getName() {
            return enm.getName().toString();
        }

        @Override
        protected String getReplaceText() {
            String text = getItemText();
            if (classDisplayOffset > 0 && classDisplayOffset < text.length()) { // Only the last name for inner classes
                text = text.substring(classDisplayOffset);
            }
            return text;
        }

        public String getItemText() {
            return displayFQN ? enm.getQualifiedName().toString() : enm.getName().toString();
        }

        protected CsmPaintComponent.EnumPaintComponent createPaintComponent() {
            return new CsmPaintComponent.EnumPaintComponent();
        }

        public Component getPaintComponent(boolean isSelected) {
            if (enumComponent == null) {
                enumComponent = createPaintComponent();
            }
            enumComponent.setSelected(isSelected);
            enumComponent.setFormatEnumName(getName());
            return enumComponent;
        }
    }

    public static class EnumeratorResultItem extends CsmResultItem {

        private CsmEnumerator enmtr;
        private int enumDisplayOffset;
        private boolean displayFQN;
        private static CsmPaintComponent.EnumeratorPaintComponent enumtrComponent = null;

        public EnumeratorResultItem(CsmEnumerator enmtr, boolean displayFQN, int priotity) {
            this(enmtr, 0, displayFQN, priotity);
        }

        public EnumeratorResultItem(CsmEnumerator enmtr, int enumDisplayOffset, boolean displayFQN, int priotity) {
            super(enmtr, priotity);
            this.enmtr = enmtr;
            this.enumDisplayOffset = enumDisplayOffset;
            this.displayFQN = displayFQN;
        }

        protected String getName() {
            return enmtr.getName().toString();
        }

        @Override
        protected String getReplaceText() {
            String text = getItemText();
            if (enumDisplayOffset > 0 && enumDisplayOffset < text.length()) { // Only the last name for inner classes
                text = text.substring(enumDisplayOffset);
            }
            return text;
        }

        public String getItemText() {
            // TODO: do we need name of enum?
            return (displayFQN ? enmtr.getEnumeration().getQualifiedName() + CsmCompletion.SCOPE : "") + enmtr.getName(); //NOI18N
        }

        protected CsmPaintComponent.EnumeratorPaintComponent createPaintComponent() {
            return new CsmPaintComponent.EnumeratorPaintComponent();
        }

        public Component getPaintComponent(boolean isSelected) {
            if (enumtrComponent == null) {
                enumtrComponent = createPaintComponent();
            }
            enumtrComponent.setSelected(isSelected);
            enumtrComponent.setFormatEnumeratorName(getName());
            return enumtrComponent;
        }
    }

    public static class ClassResultItem extends CsmResultItem {

        private CsmClass cls;
        private CsmDeclaration.Kind kind;
        private int classDisplayOffset;
        private boolean displayFQN;
        private static CsmPaintComponent.ClassPaintComponent clsComponent = null;
        private static CsmPaintComponent.StructPaintComponent structComponent = null;
        private static CsmPaintComponent.UnionPaintComponent unionComponent = null;

        public ClassResultItem(CsmClass cls, boolean displayFQN, int priotity) {
            this(cls, 0, displayFQN, priotity);
        }

        public ClassResultItem(CsmClass cls, int classDisplayOffset, boolean displayFQN, int priotity) {
            super(cls, priotity);
            this.cls = cls;
            this.kind = cls.getKind();
            this.classDisplayOffset = classDisplayOffset;
            this.displayFQN = displayFQN;
        }

        protected String getName() {
            return CsmKindUtilities.isTemplate(cls) ? ((CsmTemplate) cls).getDisplayName().toString() : cls.getName().toString();
        }

        @Override
        protected String getReplaceText() {
            String text = cls.getName().toString();
            if (classDisplayOffset > 0 && classDisplayOffset < text.length()) { // Only the last name for inner classes
                text = text.substring(classDisplayOffset);
            }
            return text;
        }

        public String getItemText() {
            return displayFQN ? cls.getQualifiedName().toString() : getName();
        }

        protected CsmPaintComponent.StructPaintComponent createStructPaintComponent() {
            return new CsmPaintComponent.StructPaintComponent();
        }

        protected CsmPaintComponent.UnionPaintComponent createUnionPaintComponent() {
            return new CsmPaintComponent.UnionPaintComponent();
        }

        protected CsmPaintComponent.ClassPaintComponent createClassPaintComponent() {
            return new CsmPaintComponent.ClassPaintComponent();
        }

        public Component getPaintComponent(boolean isSelected) {
            if (kind == CsmDeclaration.Kind.STRUCT) {
                if (structComponent == null) {
                    structComponent = createStructPaintComponent();
                }
                structComponent.setSelected(isSelected);
                structComponent.setFormatClassName(getName());
                return structComponent;
            } else if (kind == CsmDeclaration.Kind.UNION) {
                if (unionComponent == null) {
                    unionComponent = createUnionPaintComponent();
                }
                unionComponent.setSelected(isSelected);
                unionComponent.setFormatClassName(getName());
                return unionComponent;
            } else {
                assert (kind == CsmDeclaration.Kind.CLASS) : "must be class kind";
                if (clsComponent == null) {
                    clsComponent = createClassPaintComponent();
                }
                clsComponent.setSelected(isSelected);
                clsComponent.setFormatClassName(getName());
                return clsComponent;
            }
        }
    }

    public static class ForwardClassResultItem extends CsmResultItem {

        private CsmClassForwardDeclaration cls;
        private CsmDeclaration.Kind kind;
        private int classDisplayOffset;
        private boolean displayFQN;
        private static CsmPaintComponent.ClassPaintComponent clsComponent = null;
        private static CsmPaintComponent.StructPaintComponent structComponent = null;
        private static CsmPaintComponent.UnionPaintComponent unionComponent = null;

        public ForwardClassResultItem(CsmClassForwardDeclaration cls, boolean displayFQN, int priotity) {
            this(cls, 0, displayFQN, priotity);
        }

        public ForwardClassResultItem(CsmClassForwardDeclaration cls, int classDisplayOffset, boolean displayFQN, int priotity) {
            super(cls, priotity);
            this.cls = cls;
            CsmClass c = cls.getCsmClass();
            if (c != null) {
                this.kind = c.getKind();
            } else {
                this.kind = CsmDeclaration.Kind.STRUCT;
            }
            this.classDisplayOffset = classDisplayOffset;
            this.displayFQN = displayFQN;
        }

        protected String getName() {
            return cls.getName().toString();
        }

        @Override
        protected String getReplaceText() {
            String text = getItemText();
            if (classDisplayOffset > 0 && classDisplayOffset < text.length()) { // Only the last name for inner classes
                text = text.substring(classDisplayOffset);
            }
            return text;
        }

        public String getItemText() {
            return displayFQN ? cls.getQualifiedName().toString() : getName();
        }

        protected CsmPaintComponent.StructPaintComponent createStructPaintComponent() {
            return new CsmPaintComponent.StructPaintComponent();
        }

        protected CsmPaintComponent.UnionPaintComponent createUnionPaintComponent() {
            return new CsmPaintComponent.UnionPaintComponent();
        }

        protected CsmPaintComponent.ClassPaintComponent createClassPaintComponent() {
            return new CsmPaintComponent.ClassPaintComponent();
        }

        public Component getPaintComponent(boolean isSelected) {
            if (kind == CsmDeclaration.Kind.STRUCT) {
                if (structComponent == null) {
                    structComponent = createStructPaintComponent();
                }
                structComponent.setSelected(isSelected);
                structComponent.setFormatClassName(getName());
                return structComponent;
            } else if (kind == CsmDeclaration.Kind.UNION) {
                if (unionComponent == null) {
                    unionComponent = createUnionPaintComponent();
                }
                unionComponent.setSelected(isSelected);
                unionComponent.setFormatClassName(getName());
                return unionComponent;
            } else {
                assert (kind == CsmDeclaration.Kind.CLASS) : "must be class kind";
                if (clsComponent == null) {
                    clsComponent = createClassPaintComponent();
                }
                clsComponent.setSelected(isSelected);
                clsComponent.setFormatClassName(getName());
                return clsComponent;
            }
        }
    }

    public static class TypedefResultItem extends CsmResultItem {

        private CsmTypedef def;
        private int defDisplayOffset;
        private boolean displayFQN;
        private static CsmPaintComponent.TypedefPaintComponent defComponent = null;

        public TypedefResultItem(CsmTypedef def, boolean displayFQN, int priotity) {
            this(def, 0, displayFQN, priotity);
        }

        public TypedefResultItem(CsmTypedef def, int defDisplayOffset, boolean displayFQN, int priotity) {
            super(def, priotity);
            this.def = def;
            this.defDisplayOffset = defDisplayOffset;
            this.displayFQN = displayFQN;
        }

        protected String getName() {
            return def.getName().toString();
        }

        @Override
        protected String getReplaceText() {
            String text = getItemText();
            if (defDisplayOffset > 0 && defDisplayOffset < text.length()) { // Only the last name for inner classes
                text = text.substring(defDisplayOffset);
            }
            return text;
        }

        public String getItemText() {
            return displayFQN ? def.getQualifiedName().toString() : def.getName().toString();
        }

        protected CsmPaintComponent.TypedefPaintComponent createTypedefPaintComponent() {
            return new CsmPaintComponent.TypedefPaintComponent();
        }

        public Component getPaintComponent(boolean isSelected) {
            if (defComponent == null) {
                defComponent = createTypedefPaintComponent();
            }
            defComponent.setSelected(isSelected);
            defComponent.setFormatTypedefName(getName());
            return defComponent;
        }
    }

    public static class StringResultItem extends CsmResultItem {

        private String str;
        private static CsmPaintComponent.StringPaintComponent stringComponent = null;

        public StringResultItem(String str, int priotity) {
            super(null, priotity);
            this.str = str;
        }

        public String getItemText() {
            return str;
        }

        public Component getPaintComponent(boolean isSelected) {
            if (stringComponent == null) {
                stringComponent = createStringPaintComponent();
            }
            stringComponent.setSelected(isSelected);
            stringComponent.setString(str);
            return stringComponent;
        }

        @Override
        public Object getAssociatedObject() {
            return str;
        }

        protected CsmPaintComponent.StringPaintComponent createStringPaintComponent() {
            return new CsmPaintComponent.StringPaintComponent();
        }
    }
//    static class ParamStr {
//        private String type, simpleType, prm;
//        private Color typeColor;
//        public ParamStr(String type, String simpleType, String prm, Color typeColor) {
//            this.type = type;
//            this.simpleType = simpleType;
//            this.prm = prm;
//            this.typeColor = typeColor;
//        }
//        
//        public String getTypeName() {
//            return type;
//        }
//        
//        public String getSimpleTypeName() {
//            return simpleType;
//        }
//        
//        public String getName() {
//            return prm;
//        }
//        
//        public Color getTypeColor() {
//            return typeColor;
//        }
//    }
//    static class ExcStr {
//        private String name;
//        private Color typeColor;
//        public ExcStr(String name, Color typeColor) {
//            this.name = name;
//            this.typeColor = typeColor;
//        }
//        
//        public String getName() {
//            return name;
//        }
//        
//        public Color getTypeColor() {
//            return typeColor;
//        }
//    }
}
