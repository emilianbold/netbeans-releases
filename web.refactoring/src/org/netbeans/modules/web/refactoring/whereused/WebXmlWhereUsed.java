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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.refactoring.whereused;

import java.text.MessageFormat;
import org.netbeans.modules.j2ee.dd.api.web.Filter;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.web.refactoring.WebXmlRefactoring;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Erno Mononen
 */
public class WebXmlWhereUsed extends WebXmlRefactoring{
    
    private final WhereUsedQuery whereUsedQuery;
    private final String clazzFqn;
    
    public WebXmlWhereUsed(FileObject webDD, WebApp webModel, String clazzFqn, WhereUsedQuery whereUsedQuery) {
        super(webDD, webModel);
        this.clazzFqn = clazzFqn;
        this.whereUsedQuery = whereUsedQuery;
    }
    
    
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        
        for (Servlet servlet : getServlets(clazzFqn)){
            refactoringElements.add(whereUsedQuery, new WhereUsedElement(webModel, webDD, "TXT_WebXmlServletWhereUsed", clazzFqn));//NO18N
        }
        
        for (Filter filter : getFilters(clazzFqn)){
            refactoringElements.add(whereUsedQuery, new WhereUsedElement(webModel, webDD, "TXT_WebXmlFilterWhereUsed", clazzFqn));//NO18N
        }
        
        for (Listener listener : getListeners(clazzFqn)){
            refactoringElements.add(whereUsedQuery, new WhereUsedElement(webModel, webDD, "TXT_WebXmlListenerWhereUsed", clazzFqn));//NO18N
        }
        
        return null;
    }
    
    private static class WhereUsedElement extends WebRefactoringElement{
        
        private final String clazz;
        private final String bundleKey;
        
        public WhereUsedElement(WebApp webApp, FileObject webDD, String bundleKey, String clazz) {
            super(webApp, webDD);
            this.bundleKey = bundleKey;
            this.clazz = clazz;
        }
        
        
        protected void doChange() {
            // do nothing
        }
        
        protected void undo() {
            // do nothing
        }
        
        public String getDisplayText() {
            Object[] args = new Object [] {clazz};
            return MessageFormat.format(NbBundle.getMessage(WebXmlWhereUsed.class, bundleKey), args);
        }
        
    }
}
