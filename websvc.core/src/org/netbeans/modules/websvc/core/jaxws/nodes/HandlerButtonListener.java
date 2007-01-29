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
package org.netbeans.modules.websvc.core.jaxws.nodes;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.common.source.GenerationUtils;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.core.webservices.ui.panels.MessageHandlerPanel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Handler;
import org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChain;
import org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChains;
import org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChainsProvider;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Roderico Cruz, Milan Kuchtiak
 */
public class HandlerButtonListener implements ActionListener{
    MessageHandlerPanel panel;
    HandlerChains handlerChains;
    HandlerChain chain;
    FileObject handlerFO;
    //JavaClass implBeanClass;
    FileObject implBeanClass;
    Service service;
    boolean isNew;
    
    /**
     * If there is no HandlerChain annotation, handlerChains and handlerFO
     * will both be null;
     */
    public HandlerButtonListener(MessageHandlerPanel panel,
            HandlerChains handlerChains, FileObject handlerFO,
            FileObject implBeanClass, Service service,
            boolean isNew){
        this.panel = panel;
        this.handlerChains = handlerChains;
        this.handlerFO = handlerFO;
        this.implBeanClass = implBeanClass;
        this.service = service;
        this.isNew = isNew;
    }
    public void actionPerformed(ActionEvent evt) {
        if(evt.getSource() == NotifyDescriptor.OK_OPTION) {
            if (isNew) {
                //add annotation
                String servicehandlerFileName = service.getName() + "_handler"; //NOI18N
                FileObject parent = implBeanClass.getParent();
                final String handlerFileName = FileUtil.findFreeFileName(parent, servicehandlerFileName, "xml");
                CancellableTask<WorkingCopy> modificationTask = new CancellableTask<WorkingCopy>() {
                    public void run(WorkingCopy workingCopy) throws IOException {
                        workingCopy.toPhase(Phase.RESOLVED);
                        GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                        if (genUtils!=null) {
                            TreeMaker make = workingCopy.getTreeMaker();
                            TypeElement chainElement = workingCopy.getElements().getTypeElement("javax.jws.HandlerChain"); //NOI18N
                            List<ExpressionTree> attrs = new ArrayList<ExpressionTree>();
                            AssignmentTree attr1 = make.Assignment(make.Identifier("name"), make.Literal(service.getName() + "_handlerChain"));

                            AssignmentTree attr2 = make.Assignment(make.Identifier("file"), make.Literal(handlerFileName + ".xml"));
                            attrs.add(attr1);attrs.add(attr2);
                            AnnotationTree chainAnnotation = make.Annotation(
                                    make.QualIdent(chainElement), 
                                    attrs
                            );
                            ClassTree javaClass = genUtils.getClassTree();
                            ClassTree modifiedClass = genUtils.addAnnotation(javaClass, chainAnnotation);
                            workingCopy.rewrite(javaClass, modifiedClass);
                        }
                    }
                    public void cancel() {}
                };
                JavaSource targetSource = JavaSource.forFileObject(implBeanClass);
                try {
                    targetSource.runModificationTask(modificationTask).commit();
                } catch(IOException exp) {
                        ErrorManager.getDefault().notify(exp);
                }
                
                handlerFO = parent.getFileObject(handlerFileName, "xml");
                if(handlerFO == null) {
                    //create handler file
                    try {
                        WSUtils.retrieveHandlerConfigFromResource(parent,handlerFileName + ".xml");
                        handlerFO = parent.getFileObject(handlerFileName, "xml");
                    }catch(Exception exp){
                        ErrorManager.getDefault().notify(exp);
                    }
                }
                //initialize handlerChains
                try{
                    handlerChains =
                            HandlerChainsProvider.getDefault().getHandlerChains(handlerFO);
                }catch(Exception e){
                    ErrorManager.getDefault().notify(e);
                    return; //TODO handle this
                }
            }
            DefaultListModel listModel = panel.getListModel();
            chain = handlerChains.getHandlerChains()[0];
            //add new handlers
            for(int i = 0; i < listModel.getSize(); i++){
                String className = (String)listModel.getElementAt(i);
                if(isNewHandler(className, chain)){
                    chain.addHandler(className, className);
                }
            }
            //remove handlers that have been deleted
            Handler[] handlers = chain.getHandlers();
            for(int j = 0; j < handlers.length; j++){
                Handler handler = handlers[j];
                String clsName = handler.getHandlerClass();
                if(!isInModel(clsName, listModel)){
                    chain.removeHandler(clsName);
                }
            }
            //if handler chain has no handlers, delete the annotation
            // and delete the handler xml file
            FileLock lock = null;
            OutputStream out = null;
            if(chain.getHandlers().length == 0) {
                
                CancellableTask<WorkingCopy> modificationTask = new CancellableTask<WorkingCopy>() {
                    public void run(WorkingCopy workingCopy) throws IOException {
                        workingCopy.toPhase(Phase.RESOLVED);
                        GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                        if (genUtils!=null) {
                            TreeMaker make = workingCopy.getTreeMaker();
                            AnnotationMirror chainAnnotation = JaxWsNode.getAnnotation(workingCopy, genUtils, "javax.jws.HandlerChain"); //NOI18N
                            if (chainAnnotation!=null) {
                                ClassTree classTree = genUtils.getClassTree();
                                AnnotationTree anotTree = (AnnotationTree)workingCopy.getTrees().getTree(genUtils.getTypeElement(),chainAnnotation);
                                ClassTree modifiedClass = make.Class(
                                    make.removeModifiersAnnotation(classTree.getModifiers(), anotTree),
                                    classTree.getSimpleName(),
                                    classTree.getTypeParameters(),
                                    classTree.getExtendsClause(),
                                    (List<ExpressionTree>)classTree.getImplementsClause(),
                                    classTree.getMembers());
                                workingCopy.rewrite(classTree, modifiedClass);
                            }
                        }
                    }
                    public void cancel() {}
                };
                JavaSource targetSource = JavaSource.forFileObject(implBeanClass);
                try {
                    targetSource.runModificationTask(modificationTask).commit();
                } catch(IOException exp) {
                        ErrorManager.getDefault().notify(exp);
                }
                
                //delete the handler xml file
                try{
                    lock = handlerFO.lock();
                    handlerFO.delete(lock);
                }catch(Exception e){
                    ErrorManager.getDefault().notify(e);
                } finally{
                    if(lock != null){
                        lock.releaseLock();
                    }
                }
            } else{
                try{
                    lock = handlerFO.lock();
                    out = handlerFO.getOutputStream(lock);
                    handlerChains.write(out);
                }catch(Exception e){
                    ErrorManager.getDefault().notify(e);
                }finally{
                    if(lock != null){
                        lock.releaseLock();
                    }
                    if (out != null){
                        try{
                            out.close();
                        } catch(IOException ioe){
                            ErrorManager.getDefault().notify(ioe);
                        }
                    }
                }
            }
            
        }
    }
    
    private boolean isInModel(String className, ListModel model) {
        for(int i = 0; i < model.getSize(); i++){
            String cls = (String)model.getElementAt(i);
            if(className.equals(cls)){
                return true;
            }
        }
        return false;
    }
    
    private boolean isNewHandler(String className, HandlerChain handlerChain){
        if(handlerChain != null){
            Handler[] handlers = handlerChain.getHandlers();
            for(int i = 0; i < handlers.length; i++){
                if(handlers[i].getHandlerClass().equals(className)){
                    return false;
                }
            }
        }
        return true;
    }
    
    private static String readResource(InputStream is) throws IOException {
        // read the config from resource first
        StringBuffer sb = new StringBuffer();
        String lineSep = System.getProperty("line.separator");//NOI18N
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append(lineSep);
            line = br.readLine();
        }
        br.close();
        return sb.toString();
    }
}
