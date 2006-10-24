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
package org.netbeans.modules.refactoring.java.ui;

import java.beans.PropertyChangeListener;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.modules.refactoring.java.api.JavaWhereUsedQuery;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Matula, Jan Becicka
 */
public class WhereUsedQueryUI implements RefactoringUI {
    private final JavaWhereUsedQuery query;
    private final String name;
    private WhereUsedPanel panel;
    private final TreePathHandle element;
    private CompilationInfo info;

    public WhereUsedQueryUI(TreePathHandle jmiObject, CompilationInfo info) {
        this.query = new JavaWhereUsedQuery(jmiObject);
        this.element = jmiObject;
        name = UiUtils.getHeader(jmiObject.resolveElement(info), info, UiUtils.PrintPart.NAME);
        this.info = info;
        query.getContext().add(info);
    }

    public boolean isQuery() {
        return true;
    }

    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            panel = new WhereUsedPanel(name, element, parent, info);
        }
        return panel;
    }

    public org.netbeans.modules.refactoring.api.Problem setParameters() {
        Element element = this.element.resolveElement(info);
        query.setSearchInComments(panel.isSearchInComments());
        if (element instanceof ExecutableElement) {
            query.setSearchFromBaseClass(panel.isMethodFromBaseClass());
            query.setFindOverridingMethods(panel.isMethodOverriders());
            query.setFindUsages(panel.isMethodFindUsages());
            return query.checkParameters();
        } else if ((element.getKind() == ElementKind.INTERFACE) || (element.getKind() == ElementKind.CLASS)) {
            query.setFindSubclasses(panel.isClassSubTypes());
            query.setFindDirectSubclassesOnly(panel.isClassSubTypesDirectOnly());
            query.setFindUsages(panel.isClassFindUsages());
            return query.checkParameters();
        } else
            return null;
    }
    
    public org.netbeans.modules.refactoring.api.Problem checkParameters() {
       Element element = this.element.resolveElement(info);
       if (element instanceof ExecutableElement) {
            query.setSearchFromBaseClass(panel.isMethodFromBaseClass());
            query.setFindOverridingMethods(panel.isMethodOverriders());
            query.setFindUsages(panel.isMethodFindUsages());
            return query.fastCheckParameters();
        } else if ((element.getKind() == ElementKind.INTERFACE) || (element.getKind() == ElementKind.CLASS)) {
            query.setFindSubclasses(panel.isClassSubTypes());
            query.setFindDirectSubclassesOnly(panel.isClassSubTypesDirectOnly());
            query.setFindUsages(panel.isClassFindUsages());
            return query.fastCheckParameters();
        } else
            return null;
    }

    public org.netbeans.modules.refactoring.api.AbstractRefactoring getRefactoring() {
        return query;
    }

    public String getDescription() {
       Element element = this.element.resolveElement(info);
        if (panel!=null) {
            if ((element.getKind() == ElementKind.INTERFACE) || (element.getKind() == ElementKind.CLASS)) {
                if (!panel.isClassFindUsages())
                    if (!panel.isClassSubTypesDirectOnly()) {
                    return getString("DSC_WhereUsedFindAllSubTypes", name);
                    } else {
                    return getString("DSC_WhereUsedFindDirectSubTypes", name);
                    }
            } else {
                if (element instanceof ExecutableElement) {
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
