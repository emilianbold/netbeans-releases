/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.refactoring.java.ui;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.swing.event.ChangeListener;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.ui.ElementHeaders;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.api.WhereUsedQueryConstants;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Martin Matula, Jan Becicka, Ralph Ruijs
 */
public class WhereUsedQueryUI implements RefactoringUI, Openable {
    private WhereUsedQuery query = null;
    private final String name;
    private WhereUsedPanel panel;
    private final TreePathHandle element;
    private ElementHandle elementHandle;
    private ElementKind kind;
    private AbstractRefactoring delegate;

    public WhereUsedQueryUI(TreePathHandle handle, CompilationInfo info) {
        this.query = new WhereUsedQuery(Lookups.singleton(handle));
        // ClasspathInfo needs to be in context until all other modules change there
        // implementation to use scopes #199779. This is used by at least JPA refactoring and
        // API support.
        this.query.getContext().add(RetoucheUtils.getClasspathInfoFor(handle));
        this.element = handle;
        Element el = handle.resolveElement(info);
        if (!(el.getKind() == ElementKind.LOCAL_VARIABLE || el.getKind() == ElementKind.PARAMETER))
            elementHandle = ElementHandle.create(el);
        if (el!=null) {
            name = ElementHeaders.getHeader(el, info, ElementHeaders.NAME);
            kind = el.getKind();
        } else {
            name = ""; //NOI18N
            kind = ElementKind.OTHER;
        }
    }
    
    public WhereUsedQueryUI(TreePathHandle jmiObject, String name, AbstractRefactoring delegate) {
        this.delegate = delegate;
        //this.query = new JavaWhereUsedQuery(jmiObject);
        //this.query.getContext().add(info.getClasspathInfo());
        this.element = jmiObject;
        //Element el = jmiObject.resolveElement(info);
        //name = ElementHeaders.getHeader(el, info, ElementHeaders.NAME);
        //kind = el.getKind();
        this.name = name;
    }
    

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
        query.putValue(query.SEARCH_IN_COMMENTS,panel.isSearchInComments());
//        query.putValue(query., name);
        Scope customScope = panel.getCustomScope();
        if(customScope != null) {
            query.getContext().add(customScope);
        }
        if (kind == ElementKind.METHOD) {
            setForMethod();
            return query.checkParameters();
        } else if (kind.isClass() || kind.isInterface()) {
            setForClass();
            return query.checkParameters();
        } else
            return null;
    }
    
    private void setForMethod() {
        query.setRefactoringSource(Lookups.singleton(element));
        query.putValue(WhereUsedQueryConstants.SEARCH_FROM_BASECLASS,panel.isMethodFromBaseClass());
        query.putValue(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS,panel.isMethodOverriders());
        query.putValue(WhereUsedQuery.FIND_REFERENCES,panel.isMethodFindUsages());
    }
    
    private void setForClass() {
        query.putValue(WhereUsedQueryConstants.FIND_SUBCLASSES,panel.isClassSubTypes());
        query.putValue(WhereUsedQueryConstants.FIND_DIRECT_SUBCLASSES,panel.isClassSubTypesDirectOnly());
        query.putValue(WhereUsedQuery.FIND_REFERENCES,panel.isClassFindUsages());
    }
    
    public org.netbeans.modules.refactoring.api.Problem checkParameters() {
        if (kind == ElementKind.METHOD) {
            setForMethod();
            return query.fastCheckParameters();
        } else if (kind.isClass() || kind.isInterface()) {
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
            if ((kind == ElementKind.INTERFACE) || (kind == ElementKind.CLASS)) {
                if (!panel.isClassFindUsages())
                    if (!panel.isClassSubTypesDirectOnly()) {
                    return getString("DSC_WhereUsedFindAllSubTypes", name);
                    } else {
                    return getString("DSC_WhereUsedFindDirectSubTypes", name);
                    }
            } else {
                if (kind == ElementKind.METHOD) {
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
        return new MessageFormat(NbBundle.getMessage(WhereUsedPanel.class, "LBL_UsagesOf")).format (
                    new Object[] {name}
                );
    }
    
    public boolean hasParameters() {
        return true;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(WhereUsedQueryUI.class);
    }

    @Override
    public void open() {
        if (elementHandle!=null) {
            ElementOpen.open(element.getFileObject(), elementHandle);
        }
    }
    
}
