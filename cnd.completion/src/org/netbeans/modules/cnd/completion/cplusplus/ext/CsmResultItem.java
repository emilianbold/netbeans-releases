/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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


package org.netbeans.modules.cnd.completion.cplusplus.ext;

import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.CompletionQuery;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.modules.cnd.modelutil.CsmPaintComponent;
import org.netbeans.modules.cnd.modelutil.ParamStr;
import org.netbeans.modules.cnd.editor.cplusplus.CCSettingsNames;

/**
 *
 * @author  Vladimir Voskresensky
 * after JCResultItem
 */

public abstract class CsmResultItem
        implements CompletionQuery.ResultItem, CompletionQuery.ResultItemAssociatedObject {
    
    CsmObject associatedObject;
    private static final Color KEYWORD_COLOR = Color.gray;
    private static final Color TYPE_COLOR = Color.black;
    
    
    public CsmResultItem(CsmObject associatedObject){
        this.associatedObject = associatedObject;
    }
    
    
    public abstract String getItemText();
    
    protected abstract Component getPaintComponent(boolean isSelected);
    
    /**
     * Used for testing only
     * @return a string representation of the object.
     */
    public String toString() {
        Component comp = getPaintComponent(false);
        return comp != null ? comp.toString() : ""; //NOI18N
    }    
    
    public Object getAssociatedObject(){
        return associatedObject;
    }
    
    protected String getReplaceText() {
        return getItemText();
    }
    
    protected int convertCsmModifiers(CsmObject obj) {
        return CsmUtilities.getModifiers(obj);
    }
    
    public boolean substituteCommonText(JTextComponent c, int offset, int len, int subLen) {
        // [PENDING] not enough info in parameters...
        // commonText
        // substituteExp
        return false;
    }
    
    public boolean substituteText(JTextComponent c, int offset, int len, boolean shift) {
        BaseDocument doc = (BaseDocument)c.getDocument();
        String text = getReplaceText();
        int selectionStartOffset = -1;
        int selectionEndOffset = -1;
        
        if (text != null) {
            // Update the text
            doc.atomicLock();
            try {
                String textToReplace = doc.getText(offset, len);
                if (text.equals(textToReplace)) return false;
                
                doc.remove(offset, len);
                doc.insertString(offset, text, null);
                if (selectionStartOffset >= 0) {
                    c.select(offset + selectionStartOffset,
                            offset + selectionEndOffset);
                }
            } catch (BadLocationException e) {
                // Can't update
            } finally {
                doc.atomicUnlock();
            }
        }
        
        return true;
    }
    
    public java.awt.Component getPaintComponent(javax.swing.JList list, boolean isSelected, boolean cellHasFocus) {
        Component ret;
        ret = getPaintComponent(isSelected);
        if (ret==null) return null;
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
    
    protected static Color getTypeColor(CsmClassifier cls) {
        return (CsmKindUtilities.isBuiltIn(cls)) ? KEYWORD_COLOR : TYPE_COLOR;
    }
    
    protected static String getTypeName(CsmType typ) {
//        return typ.format(false);
        return typ.getText();
    }
    
    public static class FileLocalVariableResultItem extends VariableResultItem {
        
        public FileLocalVariableResultItem(CsmVariable fld){
            super(fld);
        }
        
        protected CsmPaintComponent.FieldPaintComponent createPaintComponent(){
            return new CsmPaintComponent.FileLocalVariablePaintComponent();
        }
    }   
    
    public static class GlobalVariableResultItem extends VariableResultItem {
        
        public GlobalVariableResultItem(CsmVariable fld){
            super(fld);
        }
        
        protected CsmPaintComponent.FieldPaintComponent createPaintComponent(){
            return new CsmPaintComponent.GlobalVariablePaintComponent();
        }
    }    
    
    public static class LocalVariableResultItem extends VariableResultItem {
        
        public LocalVariableResultItem(CsmVariable fld){
            super(fld);
        }
        
        protected CsmPaintComponent.FieldPaintComponent createPaintComponent(){
            return new CsmPaintComponent.LocalVariablePaintComponent();
        }
    }  
    
    public static class FieldResultItem extends VariableResultItem {
        
        public FieldResultItem(CsmField fld) {
            super(fld);
        }
        
        protected CsmPaintComponent.FieldPaintComponent createPaintComponent(){
            return new CsmPaintComponent.FieldPaintComponent();
        }
    }
    
    public static class MacroResultItem extends CsmResultItem {
        private String macName;
        private List params;
        public MacroResultItem(CsmMacro mac) {
            super(mac);
            this.macName = mac.getName();
            this.params = mac.getParameters();
        }

        private String getName(){
            return macName;
        }
        
        private List getParams(){
            return params;
        }

        public String getItemText() {
            return getName();
        }
        
        protected CsmPaintComponent.MacroPaintComponent createPaintComponent(){
            return new CsmPaintComponent.MacroPaintComponent();
        }

        public Component getPaintComponent(boolean isSelected) {
            CsmPaintComponent.MacroPaintComponent comp = createPaintComponent();
            CsmMacro mac = (CsmMacro)getAssociatedObject();
            comp.setName(getName());
            comp.setParams(getParams());
            comp.setSelected(isSelected);
            return comp;
        }
    }
    
    public abstract static class VariableResultItem extends CsmResultItem {
        
        private String typeName;
        private Color typeColor;
        private String fldName;
        private int modifiers;
        private boolean isDeprecated;
        
        private static CsmPaintComponent.FieldPaintComponent fieldComponent = null;
        private static CsmPaintComponent.FieldPaintComponent globVarComponent = null;
        private static CsmPaintComponent.FieldPaintComponent localVarComponent = null;
        private static CsmPaintComponent.FieldPaintComponent fileLocalVarComponent = null;
        
        public VariableResultItem(CsmVariable fld) {
            super(fld);
            this.fldName = fld.getName();
            this.modifiers = convertCsmModifiers(fld);
            this.typeName = getTypeName(fld.getType());
            this.typeColor = getTypeColor(fld.getType().getClassifier());
        }
        
        public String getItemText() {
            return fldName;
        }
        
        abstract protected CsmPaintComponent.FieldPaintComponent createPaintComponent();
        
        public java.awt.Component getPaintComponent(boolean isSelected) {
            CsmPaintComponent.FieldPaintComponent comp = null;
            assert (CsmKindUtilities.isCsmObject(getAssociatedObject())) : "must be csm object"; //NOI18N
            CsmObject var = (CsmObject)getAssociatedObject();
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
            comp.setTypeName(typeName);
            comp.setName(fldName);
            comp.setTypeColor(typeColor);
            comp.setModifiers(modifiers);
            comp.setSelected(isSelected);
            
            return comp;
        }
    }
    
    public static class GlobalFunctionResultItem extends MethodResultItem {
        public GlobalFunctionResultItem(CsmFunction mtd, CsmCompletionExpression substituteExp) {
            super(mtd, substituteExp);
        }
        
        protected CsmPaintComponent.ConstructorPaintComponent createPaintComponent(){
            return new CsmPaintComponent.GlobalFunctionPaintComponent();
        }
    }
    
    public static class MethodResultItem extends ConstructorResultItem {
        
        private static CsmPaintComponent.MethodPaintComponent mtdComponent = null;
        private static CsmPaintComponent.MethodPaintComponent globFunComponent = null;
        private String typeName;
        private Color typeColor;
        private String mtdName;
        
        
        public MethodResultItem(CsmFunction mtd, CsmCompletionExpression substituteExp){
            super(mtd, substituteExp);
            typeName = CsmResultItem.getTypeName(mtd.getReturnType());
            mtdName = mtd.getName();
            typeColor = CsmResultItem.getTypeColor(mtd.getReturnType().getClassifier());
        }
        
        public String getName(){
            return mtdName;
        }
        
        
        public String getItemText() {
            return getName();
        }
        
        public String getTypeName() {
            return typeName;
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
        
        protected CsmPaintComponent.ConstructorPaintComponent createPaintComponent(){
            return new CsmPaintComponent.MethodPaintComponent();
        }
        
        public Component getPaintComponent(boolean isSelected) {
            CsmPaintComponent.MethodPaintComponent comp = null;
            assert (CsmKindUtilities.isCsmObject(getAssociatedObject())) : "must be csm object"; //NOI18N
            CsmObject mtd = (CsmObject)getAssociatedObject();
            if (CsmKindUtilities.isMethod(mtd)) {
                if (mtdComponent == null) {
                    mtdComponent = (CsmPaintComponent.MethodPaintComponent)createPaintComponent();
                }
                comp = mtdComponent;
            } else {
                assert (CsmKindUtilities.isGlobalFunction(mtd)) : "support only global fun and class methods"; //NOI18N
                if (globFunComponent == null) {
                    globFunComponent = (CsmPaintComponent.MethodPaintComponent)createPaintComponent();
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
    
    public static class ConstructorResultItem extends CsmResultItem{
        
        private CsmFunction ctr;
        private CsmCompletionExpression substituteExp;
        private List params = new ArrayList();
        private List excs = new ArrayList();
        private int modifiers;
        private static CsmPaintComponent.ConstructorPaintComponent ctrComponent = null;
        
        public ConstructorResultItem(CsmFunction ctr, CsmCompletionExpression substituteExp){
            super(ctr);
            this.ctr = ctr;
            this.substituteExp = substituteExp;
            this.modifiers = convertCsmModifiers(ctr);
            CsmParameter[] prms = (CsmParameter[]) ctr.getParameters().toArray(new CsmParameter[0]);
            for (int i=0; i<prms.length; i++) {
                CsmParameter prm = (CsmParameter) prms[i];
                CsmType type = prm.getType();
                if (type == null) {
                    // only var args parameters could have null types
                    assert (prm.isVarArgs());
                    params.add(new ParamStr("", "" , prm.getName(), KEYWORD_COLOR)); //NOI18N
                } else {
                    // XXX may be need full name as the first param
                    // FIXUP: too expensive to call getClassifier here!
                    String strFullName = type.getText();// type.getClassifier().getName();
                    params.add(new ParamStr(strFullName, type.getText() , prm.getName(), TYPE_COLOR /*getTypeColor(type.getClassifier())*/));
                }
            }
            // TODO
//            CsmClass excepts[] = ctr.getExceptions();
//            for (int i=0; i<excepts.length; i++) {
//                CsmClass ex = (CsmClass) excepts[i];
//                excs.add(new ExcStr(ex.getName(), getTypeColor(ex)));
//            }
            
        }
        
        public int getModifiers(){
            return modifiers;
        }
        
        public String getName(){
            // TODO review the output
            return ctr.getName();
        }
        
        public List getParams(){
            return params;
        }
        
        public List getExceptions(){
            return excs;
        }
        
        public boolean substituteText(JTextComponent c, int offset, int len, boolean shift) {
            
            String text = null ;
            BaseDocument doc = (BaseDocument)c.getDocument();
            
            int selectionStartOffset = -1;
            int selectionEndOffset = -1;
            
            switch ((substituteExp != null) ? substituteExp.getExpID() : -1) {
                case CsmCompletionExpression.METHOD:
                    // no subst
                    break;
                    
                case CsmCompletionExpression.METHOD_OPEN:
                    CsmParameter[] parms = (CsmParameter[]) ctr.getParameters().toArray(new CsmParameter[0]);
                    if (parms.length == 0) {
                        try {
                            int fnwpos = Utilities.getFirstNonWhiteFwd(doc, offset + len);
                            if (fnwpos > -1 && doc.getChars(fnwpos, 1)[0] == ')') { // NOI18N
                                text = doc.getText(offset + len, fnwpos + 1 - offset - len);
                                len = fnwpos + 1 - offset;
                            }
                        } catch (BadLocationException e) {
                        }
                        if (text == null)
                            text = ")"; // NOI18N
                    } else { // one or more parameters
                        int ind = substituteExp.getParameterCount() - 1;
                        boolean addSpace = false;
                        boolean addClosingParen = false;
                        Formatter f = doc.getFormatter();
                        if (f instanceof ExtFormatter) {
                            // XXX CPP settings
                            Object o = ((ExtFormatter)f).getSettingValue(CCSettingsNames.FORMAT_SPACE_AFTER_COMMA);
                            if ((o instanceof Boolean) && ((Boolean)o).booleanValue()) {
                                addSpace = true;
                            }
                            // XXX CPP settings
                            o = ((ExtFormatter)f).getSettingValue(CCSettingsNames.PAIR_CHARACTERS_COMPLETION);
                            if ((o instanceof Boolean) && ((Boolean)o).booleanValue()) {
                                addClosingParen = true;
                            }
                        }
                        
                        try {
                            if (addSpace && (ind == 0 || (offset > 0
                                    && Character.isWhitespace(doc.getText(offset - 1, 1).charAt(0))))
                                    ) {
                                addSpace = false;
                            }
                        } catch (BadLocationException e) {
                        }
                        
                        if (ind < parms.length) {
                            text = addSpace ? " " : ""; // NOI18N
                            selectionStartOffset = text.length();
                            text += parms[ind].getName();
                            selectionEndOffset = text.length();
                            if (addClosingParen && ind == parms.length - 1) {
                                String paramsText = null;
                                try {
                                    int fnwpos = Utilities.getFirstNonWhiteFwd(doc, offset + len);
                                    if (fnwpos > -1 && doc.getChars(fnwpos, 1)[0] == ')') { // NOI18N
                                        paramsText = doc.getText(offset + len, fnwpos + 1 - offset - len);
                                        text += paramsText;
                                        len = fnwpos + 1 - offset;
                                    }
                                } catch (BadLocationException e) {
                                }
                                if (paramsText == null)
                                    text += ')'; // NOI18N
                            }
                        }
                    }
                    break;
                    
                default:
                    text = getItemText();
                    boolean addSpace = false;
                    boolean addClosingParen = false;
                    Formatter f = doc.getFormatter();
                    if (f instanceof ExtFormatter) {
                        Object o = ((ExtFormatter)f).getSettingValue(CCSettingsNames.FORMAT_SPACE_BEFORE_PARENTHESIS);
                        if ((o instanceof Boolean) && ((Boolean)o).booleanValue()) {
                            addSpace = true;
                        }
                        o = ((ExtFormatter)f).getSettingValue(CCSettingsNames.PAIR_CHARACTERS_COMPLETION);
                        if ((o instanceof Boolean) && ((Boolean)o).booleanValue()) {
                            addClosingParen = true;
                        }
                    }
                    
                    String paramsText = null;
                    try {
                        int fnwpos = Utilities.getFirstNonWhiteFwd(doc, offset + len);
                        if (fnwpos > -1 && doc.getChars(fnwpos, 1)[0] == '(') { // NOI18N
                            paramsText = doc.getText(offset + len, fnwpos + 1 - offset - len);
                            if (addSpace && paramsText.length() < 2)
                                text += ' '; // NOI18N
                            len = fnwpos + 1 - offset;
                            text += paramsText;
                        }
                    } catch (BadLocationException e) {
                    }
                    parms = (CsmParameter[]) ctr.getParameters().toArray(new CsmParameter[0]);
                    if (paramsText == null) {
                        if (addSpace) {
                            text += ' ';
                        }
                        text += '(';
                        
                        if (parms.length > 0) {
                            selectionStartOffset = text.length();
                            text += parms[0].getName();
                            selectionEndOffset = text.length();
                        }
                        if (parms.length == 0 || (addClosingParen && parms.length == 1)) {
                            text += ")"; // NOI18N
                        }
                    } else {
                        try {
                            int fnwpos = Utilities.getFirstNonWhiteFwd(doc, offset + len);
                            if (fnwpos > -1 && doc.getChars(fnwpos, 1)[0] == ')') { // NOI18N
                                paramsText = doc.getText(offset + len, fnwpos + 1 - offset - len);
                                len = fnwpos + 1 - offset;
                                if (parms.length > 0) {
                                    selectionStartOffset = text.length();
                                    text += parms[0].getName();
                                    selectionEndOffset = text.length();
                                }
                                text += paramsText;
                            }
                        } catch (BadLocationException e) {
                        }
                    }
                    break;
            }
            
            
            if (text != null) {
                // Update the text
                doc.atomicLock();
                try {
                    String textToReplace = doc.getText(offset, len);
                    if (text.equals(textToReplace)) {
                        c.setCaretPosition(offset + len);
                        return false;
                    }
                    doc.remove(offset, len);
                    doc.insertString(offset, text, null);
                    if (selectionStartOffset >= 0) {
                        c.select(offset + selectionStartOffset,
                                offset + selectionEndOffset);
                    }
                    return true;
                } catch (BadLocationException e) {
                    // Can't update
                } finally {
                    doc.atomicUnlock();
                }
            }
            
            return false;
        }
        
        public String getItemText() {
            // TODO review the output
            return ctr.getName();
        }
        
        protected CsmPaintComponent.ConstructorPaintComponent createPaintComponent(){
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
    
    public static class NamespaceResultItem extends CsmResultItem{
        
        private boolean displayFullNamespacePath;
        private CsmNamespace pkg;
        private String pkgName;
        private static CsmPaintComponent.NamespacePaintComponent pkgComponent;
        
        public NamespaceResultItem(CsmNamespace pkg, boolean displayFullNamespacePath){
            super(pkg);
            this.pkg = pkg;
            this.displayFullNamespacePath = displayFullNamespacePath;
            this.pkgName = pkg.getName();
            
        }
        
        
        public String getItemText() {
            return displayFullNamespacePath ? pkg.getQualifiedName() : pkg.getName();
        }
        
        protected CsmPaintComponent.NamespacePaintComponent createPaintComponent(){
            return new CsmPaintComponent.NamespacePaintComponent();
        }
        
        public Component getPaintComponent(boolean isSelected) {
            if (pkgComponent == null) {
                pkgComponent = createPaintComponent();
            }
            pkgComponent.setSelected(isSelected);
            pkgComponent.setNamespaceName(pkgName);
            pkgComponent.setDisplayFullNamespacePath(displayFullNamespacePath);
            return pkgComponent;
        }
        
    }
    
    public static class EnumResultItem extends CsmResultItem{
        
        private CsmEnum enm;
        private boolean isInterface;
        private int classDisplayOffset;
        private boolean isDeprecated;
        private boolean displayFQN;
        
        private static CsmPaintComponent.EnumPaintComponent enumComponent = null;
        
        public EnumResultItem(CsmEnum enm, boolean displayFQN){
            this(enm, 0, displayFQN);
        }
        
        public EnumResultItem(CsmEnum enm, int classDisplayOffset, boolean displayFQN){
            super(enm);
            this.enm = enm;
            this.classDisplayOffset = classDisplayOffset;
            this.displayFQN = displayFQN;
        }
        
        
        protected String getName(){
            return enm.getName();
        }
        
        protected String getReplaceText(){
            String text = getItemText();
            if (classDisplayOffset > 0
                    && classDisplayOffset < text.length()
                    ) { // Only the last name for inner classes
                text = text.substring(classDisplayOffset);
            }
            return text;
        }
        
        public String getItemText() {
            return displayFQN ? enm.getQualifiedName() : enm.getName();
        }
        
        protected CsmPaintComponent.EnumPaintComponent createPaintComponent(){
            return new CsmPaintComponent.EnumPaintComponent();
        }
        
        public Component getPaintComponent(boolean isSelected) {
            if (enumComponent == null){
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
        private boolean isDeprecated;
        private boolean displayFQN;
        
        private static CsmPaintComponent.EnumeratorPaintComponent enumtrComponent = null;
        
        public EnumeratorResultItem(CsmEnumerator enmtr, boolean displayFQN){
            this(enmtr, 0, displayFQN);
        }
        
        public EnumeratorResultItem(CsmEnumerator enmtr, int enumDisplayOffset, boolean displayFQN){
            super(enmtr);
            this.enmtr = enmtr;
            this.enumDisplayOffset = enumDisplayOffset;
            this.displayFQN = displayFQN;
        }
        
        
        protected String getName(){
            return enmtr.getName();
        }
        
        protected String getReplaceText(){
            String text = getItemText();
            if (enumDisplayOffset > 0
                    && enumDisplayOffset < text.length()
                    ) { // Only the last name for inner classes
                text = text.substring(enumDisplayOffset);
            }
            return text;
        }
        
        public String getItemText() {
            // TODO: do we need name of enum?
            return (displayFQN ? enmtr.getEnumeration().getQualifiedName() + CsmCompletion.SCOPE : "") + enmtr.getName(); //NOI18N
        }
        
        protected CsmPaintComponent.EnumeratorPaintComponent createPaintComponent(){
            return new CsmPaintComponent.EnumeratorPaintComponent();
        }
        
        public Component getPaintComponent(boolean isSelected) {
            if (enumtrComponent == null){
                enumtrComponent = createPaintComponent();
            }
            enumtrComponent.setSelected(isSelected);
            enumtrComponent.setFormatEnumeratorName(getName());
            return enumtrComponent;
        }
        
    }    
    
    public static class ClassResultItem extends CsmResultItem{
        
        private CsmClass cls;
        private CsmDeclaration.Kind kind;
        private boolean isInterface;
        private int classDisplayOffset;
        private boolean isDeprecated;
        private boolean displayFQN;
        
        private static CsmPaintComponent.ClassPaintComponent clsComponent = null;
        private static CsmPaintComponent.StructPaintComponent structComponent = null;
        private static CsmPaintComponent.UnionPaintComponent unionComponent = null;
        
        public ClassResultItem(CsmClass cls, boolean displayFQN){
            this(cls, 0, displayFQN);
        }
        
        public ClassResultItem(CsmClass cls, int classDisplayOffset, boolean displayFQN){
            super(cls);
            this.cls = cls;
            this.kind = cls.getKind();
            this.classDisplayOffset = classDisplayOffset;
            this.displayFQN = displayFQN;
        }
        
        
        protected String getName(){
            return cls.getName();
        }
        
        protected String getReplaceText(){
            String text = getItemText();
            if (classDisplayOffset > 0
                    && classDisplayOffset < text.length()
                    ) { // Only the last name for inner classes
                text = text.substring(classDisplayOffset);
            }
            return text;
        }
        
        public String getItemText() {
            return displayFQN ? cls.getQualifiedName() : cls.getName();
        }
        
        
        protected CsmPaintComponent.StructPaintComponent createStructPaintComponent(){
            return new CsmPaintComponent.StructPaintComponent();
        }
        
        protected CsmPaintComponent.UnionPaintComponent createUnionPaintComponent(){
            return new CsmPaintComponent.UnionPaintComponent();
        }
        
        protected CsmPaintComponent.ClassPaintComponent createClassPaintComponent(){
            return new CsmPaintComponent.ClassPaintComponent();
        }
        
        public Component getPaintComponent(boolean isSelected) {
            if (kind == CsmDeclaration.Kind.STRUCT){
                if (structComponent == null){
                    structComponent = createStructPaintComponent();
                }
                structComponent.setSelected(isSelected);
                structComponent.setFormatClassName(getName());
                return structComponent;
            }else if (kind == CsmDeclaration.Kind.UNION) {
                if (unionComponent == null){
                    unionComponent = createUnionPaintComponent();
                }
                unionComponent.setSelected(isSelected);
                unionComponent.setFormatClassName(getName());
                return unionComponent;
            } else {
                assert (kind == CsmDeclaration.Kind.CLASS) : "must be class kind";
                if (clsComponent == null){
                    clsComponent = createClassPaintComponent();
                }
                clsComponent.setSelected(isSelected);
                clsComponent.setFormatClassName(getName());
                return clsComponent;
            }
        }
        
    }
    
    
    public static class TypedefResultItem extends CsmResultItem{
        
        private CsmTypedef def;
        private int defDisplayOffset;
        private boolean isDeprecated;
        private boolean displayFQN;
        
        private static CsmPaintComponent.TypedefPaintComponent defComponent = null;
        
        public TypedefResultItem(CsmTypedef def, boolean displayFQN){
            this(def, 0, displayFQN);
        }
        
        public TypedefResultItem(CsmTypedef def, int defDisplayOffset, boolean displayFQN){
            super(def);
            this.def = def;
            this.defDisplayOffset = defDisplayOffset;
            this.displayFQN = displayFQN;
        }
        
        
        protected String getName(){
            return def.getName();
        }
        
        protected String getReplaceText(){
            String text = getItemText();
            if (defDisplayOffset > 0
                    && defDisplayOffset < text.length()
                    ) { // Only the last name for inner classes
                text = text.substring(defDisplayOffset);
            }
            return text;
        }
        
        public String getItemText() {
            return displayFQN ? def.getQualifiedName() : def.getName();
        }
        
        
        protected CsmPaintComponent.TypedefPaintComponent createTypedefPaintComponent(){
            return new CsmPaintComponent.TypedefPaintComponent();
        }
        
        public Component getPaintComponent(boolean isSelected) {
            if (defComponent == null){
                defComponent = createTypedefPaintComponent();
            }
            defComponent.setSelected(isSelected);
            defComponent.setFormatTypedefName(getName());
            return defComponent;
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
