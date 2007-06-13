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
package org.netbeans.modules.web.refactoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.web.Filter;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * A base class for web.xml refactorings.
 *
 * @author Erno Mononen
 */
public abstract class WebXmlRefactoring implements WebRefactoring{
    
    protected final WebApp webModel;
    protected final FileObject webDD;
    
    protected WebXmlRefactoring(FileObject webDD, WebApp webModel) {
        this.webDD = webDD;
        this.webModel = webModel;
    }
    
    public Problem preCheck() {
        if (webModel.getStatus() == WebApp.STATE_INVALID_UNPARSABLE){
            return new Problem(false, NbBundle.getMessage(WebXmlRefactoring.class, "TXT_WebXmlInvalidProblem"));
        }
        return null;
        
    }
    
    protected List<Servlet> getServlets(String clazz){
        List<Servlet> result = new ArrayList<Servlet>();
        for(Servlet servlet : webModel.getServlet())
            if (servlet.getServletClass().equals(clazz)){
                result.add(servlet);
            }
        return result;
    }
    
    protected List<Filter> getFilters(String clazz){
        List<Filter> result = new ArrayList<Filter>();
        for (Filter filter : webModel.getFilter()){
            if (filter.getFilterClass().equals(clazz)){
                result.add(filter);
            }
        }
        return result;
    }
    
    protected List<Listener> getListeners(String clazz){
        List<Listener> result = new ArrayList<Listener>();
        for (Listener listener : webModel.getListener()){
            if (listener.getListenerClass().equals(clazz)){
                result.add(listener);
            }
        }
        return result;
    }
    
    protected List<EjbRef> getEjbRefs(String clazz, boolean remote){
        List<EjbRef> result = new ArrayList<EjbRef>();
        for (EjbRef ejbRef : webModel.getEjbRef()){
            if (remote && clazz.equals(ejbRef.getRemote())){
                result.add(ejbRef);
            } else if (clazz.equals(ejbRef.getHome())){
                result.add(ejbRef);
            }
        }
        return result;
    }
    
    protected List<EjbLocalRef> getEjbLocalRefs(String clazz, boolean localHome){
        List<EjbLocalRef> result = new ArrayList<EjbLocalRef>();
        for (EjbLocalRef ejbLocalRef : webModel.getEjbLocalRef()){
            if (localHome && clazz.equals(ejbLocalRef.getLocalHome())){
                result.add(ejbLocalRef);
            } else if (clazz.equals(ejbLocalRef.getLocal())){
                result.add(ejbLocalRef);
            }
        }
        return result;
    }
    
    private boolean packageEquals(String pkg, String fqn){
        int lastDot = fqn.lastIndexOf(".");
        if (lastDot <= 0){
            return false;
        }
        return pkg.equals(fqn.substring(lastDot));
    }
    
    protected abstract static class WebRefactoringElement extends SimpleRefactoringElementImplementation{
        
        protected final WebApp webApp;
        protected final FileObject webDD;
        
        public WebRefactoringElement(WebApp webApp, FileObject webDD) {
            this.webApp = webApp;
            this.webDD = webDD;
        }
        
        public void performChange() {
            doChange();
            writeDD();
        }
        
        private void writeDD(){
            try{
                webApp.write(webDD);
            }catch(IOException ioe){
                Exceptions.printStackTrace(ioe);
            }
        }
        
        protected abstract void doChange();
        
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
        
        public FileObject getParentFile() {
            return webDD;
        }
        
        public PositionBounds getPosition() {
            try {
                //XXX: does not work correctly when a class is specified more than once in web.xml
                return new PositionBoundsResolver(DataObject.find(webDD),getName()).getPositionBounds();
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }
        
        public String getText() {
            return getDisplayText();
        }
        
        @Override
        public void undoChange() {
            undo();
            writeDD();
        }
        
        protected abstract String getName();
        
        protected abstract void undo();
    }
    
}
