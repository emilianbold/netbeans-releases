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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.websvc.editor.hints;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.awt.EventQueue;
import javax.lang.model.element.TypeElement;

import org.openide.filesystems.FileObject;
import org.openide.util.WeakListeners;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;

import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;

import org.netbeans.modules.websvc.editor.hints.common.ProblemContext;
import org.netbeans.modules.websvc.editor.hints.common.RulesEngine;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 * @author Ajit.Bhate@Sun.COM
 */
public class WebServicesHintsProvider {
    
    private final static Object singleInstanceLock = new Object();
    private static WebServicesHintsProvider runningInstance = null;
    
    private FileObject file;
    private boolean cancelled = false;
    private ProblemContext context = null;
    private List<ErrorDescription> problemsFound = new ArrayList<ErrorDescription>();
    private Object cancellationLock = new Object();
    private WSDLModel wsdlModel;
    private Service service;
    private ComponentListener changeListener;
    
    public WebServicesHintsProvider(FileObject file) {
        this.file = file;
    }
    
    public void cancel() {
        cancelled = true;
        
        synchronized(cancellationLock){
            if (context != null){
                context.setCancelled(true);
            }
        }
    }
    
    public void run(final CompilationInfo info) throws Exception{
        if (runningInstance != null){
            runningInstance.cancel();
        }
        
        synchronized(singleInstanceLock){
            runningInstance = this;
            // the 'cancelled' flag must be reset as the instance of WebServicesHintsProvider is reused
            cancelled = false;
            problemsFound.clear();
            for (Tree tree : info.getCompilationUnit().getTypeDecls()){
                if (isCancelled()){
                    break;
                }
                
                if (tree.getKind() == Tree.Kind.CLASS){
                    TreePath path = info.getTrees().getPath(info.getCompilationUnit(), tree);
                    TypeElement javaClass = (TypeElement) info.getTrees().getElement(path);
                    
                    initServiceMetadata(javaClass);
                    createProblemContext(info, javaClass);
                    
                    RulesEngine rulesEngine = new WebServicesRulesEngine();
                    javaClass.accept(rulesEngine, context);
                    problemsFound.addAll(rulesEngine.getProblemsFound());

                    synchronized(cancellationLock){
                        context = null;
                    }
                }
            }
            
            //TODO: should we really reset the errors if the task is cancelled?
            HintsController.setErrors(file, "WebService Verification", problemsFound); //NOI18N
            runningInstance = null;
        }
    }
    
    private void createProblemContext(CompilationInfo info,
            TypeElement javaClass){
        context = new ProblemContext();
        context.setJavaClass(javaClass);
        context.setFileObject(file);
        context.setCompilationInfo(info);
        if(service!=null) context.addUserObject(service);
        if(wsdlModel!=null) context.addUserObject(wsdlModel);
    }
    
    private void initServiceMetadata(TypeElement javaClass) {
        if (service == null) {
            Project owner = FileOwnerQuery.getOwner(file);
            if(owner!=null) {
                JaxWsModel jaxwsModel = owner.getLookup().lookup(JaxWsModel.class);
                if (jaxwsModel != null) {
                    service = jaxwsModel.findServiceByImplementationClass(javaClass.getQualifiedName().toString());
                }
            }
        }
        if (service != null && service.getLocalWsdlFile()!=null) {
            JAXWSSupport jaxwsSupport = JAXWSSupport.getJAXWSSupport(file);
            if(jaxwsSupport!=null) {
                FileObject wsdlFo = jaxwsSupport.getLocalWsdlFolderForService(service.getName(), false).getFileObject(service.getLocalWsdlFile());
                WSDLModel tmpModel = WSDLModelFactory.getDefault().getModel(
                        Utilities.getModelSource(wsdlFo, true));
                if(tmpModel!=wsdlModel) {
                    if(wsdlModel!=null) {
                        if(changeListener!=null) {
                            wsdlModel.removeComponentListener(changeListener);
                            changeListener = null;
                        }
                    }
                    wsdlModel = tmpModel;
                    if(wsdlModel!=null) {
                        if(changeListener==null) 
                            changeListener = WeakListeners.create(ComponentListener.class,
                                    new WsdlModelListener(file), wsdlModel);
                        wsdlModel.addComponentListener(changeListener);
                    }
                }
            }
        }
    }
    
    public boolean isCancelled(){
        return cancelled;
    }
    
    public List<? extends ErrorDescription> getProblemsFound(){
        return problemsFound;
    }
    
    public static class ProblemFinderCompInfo extends WebServicesHintsProvider implements CancellableTask<CompilationInfo>{
        public ProblemFinderCompInfo(FileObject file){
            super(file);
        }
    }
    
    public static class ProblemFinderCompControl extends WebServicesHintsProvider implements CancellableTask<CompilationController>{
        public ProblemFinderCompControl(FileObject file){
            super(file);
        }
        
        public void run(CompilationController controller) throws Exception {
            controller.toPhase(JavaSource.Phase.RESOLVED);
            super.run(controller);
        }
    }

    private abstract class RescanTrigger{
        private FileObject file;
        
        RescanTrigger(FileObject file){
            this.file = file;
        }
        
        void rescan(){
            final JavaSource javaSrc = JavaSource.forFileObject(file);
            
            if (javaSrc != null){
                try{
                    if(EventQueue.isDispatchThread()) {
                        RequestProcessor.getDefault().post(new Runnable() {
                            public void run() {
                                try{
                                    javaSrc.runUserActionTask(new ProblemFinderCompControl(file), true);
                                } catch (IOException e){
                                }
                            }
                        });
                    } else {
                        javaSrc.runUserActionTask(new ProblemFinderCompControl(file), true);
                    }
                } catch (IOException e){
                }
            }
        }
    }
    
    private class WsdlModelListener extends RescanTrigger implements ComponentListener {
        WsdlModelListener(FileObject file){
            super(file);
        }
        public void valueChanged(ComponentEvent evt) {
            if(!WebServicesHintsProvider.this.isCancelled()) {
                rescan();
            }
        }
        public void childrenAdded(ComponentEvent evt) {
            if(!WebServicesHintsProvider.this.isCancelled()) {
                rescan();
            }
        }
        public void childrenDeleted(ComponentEvent evt) {
            if(!WebServicesHintsProvider.this.isCancelled()) {
                rescan();
            }
        }
    }
}
