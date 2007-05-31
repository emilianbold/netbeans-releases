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
package org.netbeans.modules.web.refactoring.rename;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.j2ee.dd.api.web.Filter;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Handles renaming of classes specified in web.xml.
 *
 * @author Erno Mononen
 */
public class WebXmlRename implements RefactoringPlugin{
    
    private final WebApp webModel;
    private final String oldFqn;
    private final RenameRefactoring rename;
    private final FileObject webDD;
    
    public WebXmlRename(String oldFqn, RenameRefactoring rename, WebApp webModel, FileObject webDD) {
        this.oldFqn = oldFqn;
        this.rename = rename;
        this.webModel = webModel;
        this.webDD = webDD;
    }
    
    public Problem preCheck() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
    public Problem checkParameters() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Problem fastCheckParameters() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void cancelRequest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        
        String newName = renameClass(oldFqn, rename.getNewName());
        for (Servlet servlet : getServlets(oldFqn)){
            refactoringElements.add(rename, new ServletRenameElement(oldFqn, newName, webModel, webDD, servlet));
        }
        
        for (Listener listener : getListeners(oldFqn)){
            refactoringElements.add(rename, new ListenerRenameElement(oldFqn, newName, webModel, webDD, listener));
        }
        
        for (Filter filter : getFilters(oldFqn)){
            refactoringElements.add(rename, new FilterRenameElement(oldFqn, newName, webModel, webDD, filter));
        }
        
        
        return null;
        
    }
    
    public static String renameClass(String originalFullyQualifiedName, String newName){
        int lastDot = originalFullyQualifiedName.lastIndexOf('.');
        return (lastDot <= 0) ? newName : originalFullyQualifiedName.substring(0, lastDot + 1) + newName;
    }
    
    private List<Servlet> getServlets(String clazz){
        List<Servlet> result = new ArrayList<Servlet>();
        for(Servlet servlet : webModel.getServlet())
            if (servlet.getServletClass().equals(clazz)){
                result.add(servlet);
            }
        return result;
    }
    
    private List<Filter> getFilters(String clazz){
        List<Filter> result = new ArrayList<Filter>();
        for (Filter filter : webModel.getFilter()){
            if (filter.getFilterClass().equals(clazz)){
                result.add(filter);
            }
        }
        return result;
    }
    
    private List<Listener> getListeners(String clazz){
        List<Listener> result = new ArrayList<Listener>();
        for (Listener listener : webModel.getListener()){
            if (listener.getListenerClass().equals(clazz)){
                result.add(listener);
            }
        }
        return result;
    }
    
    
    
    private abstract static class WebRenameElement extends SimpleRefactoringElementImplementation{
        
        protected WebApp webApp;
        protected FileObject webDD;
        protected String oldName;
        protected String newName;
        protected RenameRefactoring rename;
        
        public WebRenameElement(String newName, String oldName, WebApp webApp, FileObject webDD) {
            this.newName = newName;
            this.oldName = oldName;
            this.webApp = webApp;
            this.webDD = webDD;
        }
        
        public void performChange() {
            doChange();
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
            return null;
        }
        
        public String getText() {
            return getDisplayText();
        }
        
        @Override
        public void undoChange() {
            undo();
            try{
                webApp.write(webDD);
            }catch(IOException ioe){
                Exceptions.printStackTrace(ioe);
            }
        }
        
        protected abstract void undo();
    }
    
    private static class ServletRenameElement extends WebRenameElement{
        
        private Servlet servlet;
        public ServletRenameElement(String newName, String oldName, WebApp webApp, FileObject webDD, Servlet servlet) {
            super(newName, oldName, webApp, webDD);
            this.servlet = servlet;
        }
        
        protected void doChange() {
            servlet.setServletClass(newName);
        }
        
        public String getDisplayText() {
            Object[] args = new Object [] {getParentFile().getNameExt(), oldName, newName};
            return MessageFormat.format(NbBundle.getMessage(WebXmlRename.class, "TXT_WebXmlServletRename"), args);
        }
        
        protected void undo() {
            servlet.setServletClass(oldName);
        }
        
    }
    
    private static class FilterRenameElement extends WebRenameElement{
        
        private Filter filter;
        public FilterRenameElement(String newName, String oldName, WebApp webApp, FileObject webDD, Filter filter) {
            super(newName, oldName, webApp, webDD);
            this.filter = filter;
        }
        
        protected void doChange() {
            filter.setFilterClass(newName);
        }
        
        public String getDisplayText() {
            Object[] args = new Object [] {getParentFile().getNameExt(), oldName, newName};
            return MessageFormat.format(NbBundle.getMessage(WebXmlRename.class, "TXT_WebXmlFilterRename"), args);
        }
        
        protected void undo() {
            filter.setFilterClass(oldName);
        }
        
    }
    
    private static class ListenerRenameElement extends WebRenameElement{
        
        private Listener listener;
        
        public ListenerRenameElement(String newName, String oldName, WebApp webApp, FileObject webDD, Listener listener) {
            super(newName, oldName, webApp, webDD);
            this.listener = listener;
        }
        
        protected void doChange() {
            listener.setListenerClass(newName);
        }
        
        public String getDisplayText() {
            Object[] args = new Object [] {getParentFile().getNameExt(), oldName, newName};
            return MessageFormat.format(NbBundle.getMessage(WebXmlRename.class, "TXT_WebXmlListenerRename"), args);
        }
        
        protected void undo() {
            listener.setListenerClass(oldName);
        }
        
    }
    
}
