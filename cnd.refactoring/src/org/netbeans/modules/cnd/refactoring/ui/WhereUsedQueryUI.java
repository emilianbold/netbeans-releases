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
package org.netbeans.modules.cnd.refactoring.ui;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.model.CsmBuiltIn;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.refactoring.api.WhereUsedQueryConstants;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * WhereUsedQueryUI from the Java refactoring module, only moderately modified for Ruby
 * 
 * @author Martin Matula, Jan Becicka
 * @author Vladimir Voskresensky
 */
public class WhereUsedQueryUI implements RefactoringUI {
    private WhereUsedQuery query = null;
    private final String name;
    private WhereUsedPanel panel;
//    private final RubyElementCtx element;
    private final CsmObject element;
//    private ElementKind kind;
    private CsmDeclaration.Kind kind;
    private AbstractRefactoring delegate;

    public WhereUsedQueryUI(CsmObject csmObject/*RubyElementCtx jmiObject, CompilationInfo info*/) {
        this.query = new WhereUsedQuery(Lookups.singleton(csmObject));
//        this.query.getContext().add(RetoucheUtils.getClasspathInfoFor(jmiObject));
        this.element = getElement(csmObject);
        if (this.element != null && this.element != csmObject) {
            this.query.getContext().add(this.element);
        }
//        name = csmObject.getName();
//        kind = csmObject.getKind();
        name = getName(this.element);
        kind = getKind(this.element);
    }

    private CsmObject getElement(CsmObject csmObject) {
        if (csmObject instanceof CsmReference) {
            return ((CsmReference)csmObject).getReferencedObject();
        } else {
            return csmObject;
        }
    }
    
    private String getName(CsmObject csmObj) {
        assert csmObj != null;
        String objName;
        if (csmObj instanceof CsmReference) {
            objName = ((CsmReference)csmObj).getText();
        } else if (CsmKindUtilities.isNamedElement(csmObj)) {
            objName = ((CsmNamedElement)csmObj).getName();
        } else if (csmObj != null) {
            objName = "<UNNAMED ELEMENT>";
        } else {
            objName = "<UNRESOLVED ELEMENT>";
        }
        return objName;
    }
    
    private CsmDeclaration.Kind getKind(CsmObject obj) {
        return CsmDeclaration.Kind.CLASS;
    }
    
//    public WhereUsedQueryUI(CsmReference csmObject/*RubyElementCtx jmiObject*/, String name, AbstractRefactoring delegate) {
//        this.delegate = delegate;
//        this.element = csmObject;
//        this.name = name;
//    }
    
    public boolean isQuery() {
        return true;
    }

    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new WhereUsedPanel(name, element, parent);
        }
        return panel;
    }

    public org.netbeans.modules.refactoring.api.Problem setParameters() {
        query.putValue(WhereUsedQuery.SEARCH_IN_COMMENTS,panel.isSearchInComments());
        if (CsmKindUtilities.isFunction(element)/*kind == ElementKind.METHOD*/) {
            setForMethod();
            return query.checkParameters();
        } else if (CsmKindUtilities.isClass(element)/*kind == ElementKind.MODULE || kind == ElementKind.CLASS*/) {
            setForClass();
            return query.checkParameters();
        } else
            return null;
    }
    
    private void setForMethod() {
        if (panel.isMethodFromBaseClass()) {
            query.setRefactoringSource(Lookups.singleton(panel.getBaseMethod()));
        } else {
            query.setRefactoringSource(Lookups.singleton(element));
        }
        query.putValue(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS,panel.isMethodOverriders());
        query.putValue(WhereUsedQuery.FIND_REFERENCES,panel.isMethodFindUsages());
    }
    
    private void setForClass() {
        query.putValue(WhereUsedQueryConstants.FIND_SUBCLASSES,panel.isClassSubTypes());
        query.putValue(WhereUsedQueryConstants.FIND_DIRECT_SUBCLASSES,panel.isClassSubTypesDirectOnly());
        query.putValue(WhereUsedQuery.FIND_REFERENCES,panel.isClassFindUsages());
    }
    
    public org.netbeans.modules.refactoring.api.Problem checkParameters() {
        if (CsmKindUtilities.isFunction(element)/*kind == ElementKind.METHOD*/) {
            setForMethod();
            return query.fastCheckParameters();
        } else if (CsmKindUtilities.isClass(element)/*kind == ElementKind.MODULE || kind == ElementKind.CLASS*/) {
            setForClass();
            return query.fastCheckParameters();
        } else
            return null;
    }

    public org.netbeans.modules.refactoring.api.AbstractRefactoring getRefactoring() {
        return query!=null?query:delegate;
    }

    public String getDescription() {
        if (panel!=null) {
            if (CsmKindUtilities.isClass(element)/*kind == ElementKind.MODULE || kind == ElementKind.CLASS*/) {
                if (!panel.isClassFindUsages())
                    if (!panel.isClassSubTypesDirectOnly()) {
                        return getString("DSC_WhereUsedFindAllSubTypes", name);
                    } else {
                        return getString("DSC_WhereUsedFindDirectSubTypes", name);
                    }
            } else {
                if (CsmKindUtilities.isFunction(element)/*kind == ElementKind.METHOD*/) {
                    String description = null;
                    if (panel.isMethodFindUsages()) {
                        description = getString("DSC_FindUsages");
                    }
                    
                    if (panel.isMethodOverriders()) {
                        if (description != null) {
                            description += " " + getString("DSC_And") + " ";
                        } else {
                            description = "";
                        }
                        description += getString("DSC_WhereUsedMethodOverriders");
                    }
                    
                    description += " " + getString("DSC_WhereUsedOf", panel.getMethodDeclaringClass() + '.' + name); //NOI18N
                    return description;
                }
            }
        }
        return getString("DSC_WhereUsed", name);
    }
    
    private ResourceBundle bundle;
    private String getString(String key) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(WhereUsedQueryUI.class);
        }
        return bundle.getString(key);
    }
    
    private String getString(String key, String value) {
        return new MessageFormat(getString(key)).format (new Object[] {value});
    }


    public String getName() {
        return new MessageFormat(NbBundle.getMessage(WhereUsedPanel.class, "LBL_WhereUsed")).format (
                    new Object[] {name}
                );
    }
    
    public boolean hasParameters() {
        return true;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(WhereUsedQueryUI.class);
    }
}
