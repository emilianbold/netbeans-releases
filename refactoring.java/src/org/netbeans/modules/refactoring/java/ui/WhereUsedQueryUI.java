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

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.api.WhereUsedQueryConstants;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Martin Matula, Jan Becicka
 */
public class WhereUsedQueryUI implements RefactoringUI {
    private WhereUsedQuery query = null;
    private final String name;
    private WhereUsedPanel panel;
    private final TreePathHandle element;
    private ElementKind kind;
    private AbstractRefactoring delegate;

    public WhereUsedQueryUI(TreePathHandle handle, CompilationInfo info) {
        this.query = new WhereUsedQuery(Lookups.singleton(handle));
        this.query.getContext().add(info.getClasspathInfo());
        this.element = handle;
        Element el = handle.resolveElement(info);
        name = UiUtils.getHeader(el, info, UiUtils.PrintPart.NAME);
        kind = el.getKind();
    }
    
    public WhereUsedQueryUI(TreePathHandle jmiObject, String name, AbstractRefactoring delegate) {
        this.delegate = delegate;
        //this.query = new JavaWhereUsedQuery(jmiObject);
        //this.query.getContext().add(info.getClasspathInfo());
        this.element = jmiObject;
        //Element el = jmiObject.resolveElement(info);
        //name = UiUtils.getHeader(el, info, UiUtils.PrintPart.NAME);
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
        if (panel.getScope()==WhereUsedPanel.Scope.ALL) {
            if (kind==ElementKind.METHOD && panel.isMethodFromBaseClass()) {
                query.getContext().add(RetoucheUtils.getClasspathInfoFor(panel.getBaseMethod()));
            } else {
                query.getContext().add(RetoucheUtils.getClasspathInfoFor(element));
            }
        } else {
            ClasspathInfo info = query.getContext().lookup(ClasspathInfo.class);
            Project p = FileOwnerQuery.getOwner(element.getFileObject());
            Sources sources = ProjectUtils.getSources(p);
            Set<FileObject> roots = new HashSet();
            for (SourceGroup sg:sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                roots.add(sg.getRootFolder());
            }
            ClassPath rcp = ClassPathSupport.createClassPath(roots.toArray(new FileObject[roots.size()]));
            info = ClasspathInfo.create(info.getClassPath(ClasspathInfo.PathKind.BOOT), info.getClassPath(ClasspathInfo.PathKind.COMPILE), rcp);
            query.getContext().add(info);
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
        if (panel.isMethodFromBaseClass()) {
            query.setRefactoringSource(Lookups.singleton(panel.getBaseMethod()));
        } else {
            query.setRefactoringSource(Lookups.singleton(element));
        }
        query.putValue(WhereUsedQueryConstants.FIND_OVERRIDING_METHODS,panel.isMethodOverriders());
        query.putValue(query.FIND_REFERENCES,panel.isMethodFindUsages());
    }
    
    private void setForClass() {
        query.putValue(WhereUsedQueryConstants.FIND_SUBCLASSES,panel.isClassSubTypes());
        query.putValue(WhereUsedQueryConstants.FIND_DIRECT_SUBCLASSES,panel.isClassSubTypesDirectOnly());
        query.putValue(query.FIND_REFERENCES,panel.isClassFindUsages());
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
