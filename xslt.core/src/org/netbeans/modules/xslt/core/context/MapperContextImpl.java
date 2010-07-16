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
package org.netbeans.modules.xslt.core.context;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventObject;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xslt.tmap.util.Util;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.model.XslModel;
import org.netbeans.modules.xslt.project.spi.ProjectsFilesChangeHandler;
import org.netbeans.modules.xslt.project.spi.ProjectsFilesChangeListener;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.api.VariableReference;
import org.netbeans.modules.xslt.tmap.model.api.WSDLReference;
import org.netbeans.modules.xslt.tmap.model.validation.TransformmapValidator;
import org.netbeans.modules.xslt.tmap.model.validation.TransformmapValidatorImpl;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.soa.ui.SoaUtil;

/**
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class MapperContextImpl implements MapperContext {

    private static final int CONTEXT_CHANGE_TASK_DELAY = 500;
    private transient RequestProcessor.Task myPreviousChangeTask;

    private Transform myTransformContextComponent;
    private XslModel myXslModel;
    private AXIComponent mySourceComponent;
    private AXIModel mySourceAxiModel;
    
    private WSDLReference<Part> mySourcePart;
    private WSDLModel mySourceModel;

    private AXIComponent myTargetComponent;
    private AXIModel myTargetAxiModel;
    
    private WSDLReference<Part> myTargetPart;
    private WSDLModel myTargetModel;

    private MapperContextChangeSupport myChangeSupport = new MapperContextChangeSupport();
    private TMapModel myTMapModel;

    private ContextPropertyChangeListener myContextChangeListener = 
            new ContextPropertyChangeListener();
    private FileChangeListener myFileChangeListener = new FileChangeListenerImpl();

    private ProjectsFilesChangeHandler myProjectsFilesChangeHandler;
    private ProjectsFilesChangeListener myProjectsFilesChangeListener = new ProjectsFilesChangeListenerImpl();

    private FileObject myTMapFo;
    private FileObject myXslFo;

    public MapperContextImpl(XslModel xslModel, TMapModel model) {
        init(model, xslModel);
        addContextChangeListeners();
    }

    // TODO m
    public MapperContextImpl(Transform transform, XslModel xslModel, AXIComponent sourceComponent, AXIComponent targetComponent) {
        init(transform, xslModel, sourceComponent, targetComponent);
        addContextChangeListeners();
    }

    private void init(TMapModel tMapModel, Transform transform, XslModel xslModel, AXIComponent sourceComponent, AXIComponent targetComponent) {
        this.myXslModel = xslModel;
        this.myTransformContextComponent = transform;
        this.myTMapModel = tMapModel;


        this.mySourceComponent = sourceComponent;
        mySourceAxiModel = mySourceComponent != null 
                ? mySourceComponent.getModel() : null;
//        this.mySourceSchemaModel = sourceModel != null 
//                ? sourceModel.getSchemaModel() : null;

        //        this.mySourceAxiDocument = sourceModel != null 
//                ? sourceModel.getRoot() : null;

        mySourcePart = getSourcePart(myTransformContextComponent);
        mySourceModel = getWsdlModel(mySourcePart);

        this.myTargetComponent = targetComponent;
        myTargetAxiModel = myTargetComponent != null 
                ? myTargetComponent.getModel() : null;
//        this.myTargetSchemaModel = targetModel != null 
//                ? targetModel.getSchemaModel() : null;
//        this.myTargetAxiDocument = targetModel != null 
//                ? targetModel.getRoot() : null;
                
        myTargetPart = getTargetPart(myTransformContextComponent);
        myTargetModel = getWsdlModel(myTargetPart);

        myTMapFo = SoaUtil.getFileObjectByModel(myTMapModel);
        myXslFo = myXslModel == null ? null : SoaUtil.getFileObjectByModel(myXslModel);
    }

    private void init(Transform transform, XslModel xslModel, AXIComponent sourceComponent, AXIComponent targetComponent) {
        init(transform == null ? null : transform.getModel(), transform, xslModel, sourceComponent, targetComponent);
    }

    private void init(TMapModel tMapModel, XslModel xslModel) {
        init(tMapModel, null, xslModel, null, null);
    }


    public void reinit(TMapModel tMapModel, 
            Transform transform, 
            XslModel xslModel, 
            AXIComponent sourceComponent, 
            AXIComponent targetComponent,
            EventObject evt) 
    {
//        XslModel oldXslModel = myXslModel;
//        Transform oldTransform = myTransformContextComponent;
//        TMapModel oldTMapModel = myTMapModel;
//        AXIComponent oldSourceComponent = mySourceComponent;
//        AXIComponent oldTargetComponent = myTargetComponent;
        
        setXslModel(xslModel, false);
        setTransformContextComponent(tMapModel, transform, false);
        setSourceType(sourceComponent, false);
        setTargetType(targetComponent, false);
        
//        fireChanges(oldXslModel, oldTransform, oldTMapModel, 
//                oldSourceComponent, oldTargetComponent);
        fireChanges(evt);
    }

    private void fireChanges(EventObject evt) {
        myChangeSupport.fireMapperContextChanged(null, evt);
    }
    
//    private void fireChanges(XslModel oldXslModel, 
//            Transform oldTransform,
//            TMapModel oldTMapModel,
//            AXIComponent oldSourceComponent,
//            AXIComponent oldTargetComponent) 
//    {
//        
////        myChangeSupport.fireMapperContextChanged(oldSourceComponent, mySourceComponent);
////        System.out.println("mappercontext changed ...");
//        
//        if (myXslModel == null || !myXslModel.equals(oldXslModel) ) {
//            myChangeSupport.fireMapperContextChanged(oldXslModel, myXslModel);
//            return;
//        }
//        
//        if (myTransformContextComponent == null || !myTransformContextComponent.equals(oldTransform) ) {
//            myChangeSupport.fireMapperContextChanged(oldTransform, myTransformContextComponent);
//            return;
//        }
//
//        if (myTMapModel == null || !myTMapModel.equals(oldTMapModel) ) {
//            myChangeSupport.fireMapperContextChanged(oldTMapModel, myTMapModel);
//            return;
//        }
//        
//        if (mySourceComponent == null || !mySourceComponent.equals(oldSourceComponent) ) {
//            myChangeSupport.fireMapperContextChanged(oldSourceComponent, mySourceComponent);
//            return;
//        }
//
//        if (myTargetComponent == null || !myTargetComponent.equals(oldTargetComponent) ) {
//            myChangeSupport.fireMapperContextChanged(oldTargetComponent, myTargetComponent);
//            return;
//        }
//    }
    
    private void setTransformContextComponent(TMapModel tMapModel, Transform context, boolean fireChanges) {
        TMapModel oldTMapModel = myTMapModel;
        myTMapModel = tMapModel;
        if ((myTMapModel != null && !myTMapModel.equals(oldTMapModel)) || (oldTMapModel != null && !oldTMapModel.equals(myTMapModel))) {
            if (oldTMapModel != null) {
                myTMapModel.removePropertyChangeListener(myContextChangeListener);
            }

            if (myTMapModel != null) {
                myTMapModel.addPropertyChangeListener(myContextChangeListener);
            }
        }

        Transform oldTransform = myTransformContextComponent;
        this.myTransformContextComponent = context;

        if ((myTransformContextComponent != null && !myTransformContextComponent.equals(oldTransform)) || (oldTransform != null && !oldTransform.equals(myTransformContextComponent))) {
            if (myTMapFo != null) {
                myTMapFo.removeFileChangeListener(myFileChangeListener);
            }

            myTMapFo = SoaUtil.getFileObjectByModel(myTMapModel);
            if (myTMapFo != null) {
                myTMapFo.addFileChangeListener(myFileChangeListener);
            }
        }

        setSourcePart(getSourcePart(myTransformContextComponent));
        setTargetPart(getTargetPart(myTransformContextComponent));
    }

    public TMapModel getTMapModel() {
        return myTMapModel;
    }

    private void setXslModel(XslModel xslModel, boolean fireChanges) {
        XslModel oldXslModel = myXslModel;
        this.myXslModel = xslModel;

        if ((myXslModel != null && !myXslModel.equals(oldXslModel)) || (oldXslModel != null && !oldXslModel.equals(myXslModel))) {
            if (myXslFo != null) {
                myXslFo.removeFileChangeListener(myFileChangeListener);
            }

            myXslFo = myXslModel == null ? null : SoaUtil.getFileObjectByModel(myXslModel);
            if (myXslFo != null) {
                myXslFo.addFileChangeListener(myFileChangeListener);
            }
        }
    }

    public XslModel getXSLModel() {
        return myXslModel;
    }

    public AXIComponent getTargetType() {
        return myTargetComponent;
    }

    private void setTargetType(AXIComponent axiComp, boolean fireChanges) {
        AXIComponent oldValue = myTargetComponent;
//        SchemaModel oldModel = myTargetSchemaModel;
        AXIModel oldAxiModel = myTargetAxiModel;
//        AXIDocument oldDocument = mySourceAxiDocument;
        this.myTargetComponent = axiComp;


        this.myTargetAxiModel = myTargetComponent != null 
                ? myTargetComponent.getModel() : null;
//        this.myTargetSchemaModel = myTargetAxiModel != null 
//                ? myTargetAxiModel.getSchemaModel() : null;
//        myTargetAxiDocument = targetModel != null ? targetModel.getRoot() : null;

        
//        if ((myTargetSchemaModel != null && !myTargetSchemaModel.equals(oldModel)) || (oldModel != null && !oldModel.equals(myTargetSchemaModel))) {
//            if (oldModel != null) {
//                oldModel.removePropertyChangeListener(myContextChangeListener);
//            }
//
//            if (myTargetSchemaModel != null) {
//                myTargetSchemaModel.addPropertyChangeListener(myContextChangeListener);
//            }
////            myChangeSupport.fireMapperContextChanged(oldModel, myTargetSchemaModel);
//        }
        
        
        if ((myTargetAxiModel != null && !myTargetAxiModel.equals(oldAxiModel)) 
                || (oldAxiModel != null && !oldAxiModel.equals(myTargetAxiModel)) 
                || (oldAxiModel == null && myTargetAxiModel == null)) 
        {
            if (oldAxiModel != null) {
                oldAxiModel.removePropertyChangeListener(myContextChangeListener);
                oldAxiModel.removeComponentListener(myContextChangeListener);
            }

            if (myTargetAxiModel != null) {
                myTargetAxiModel.addPropertyChangeListener(myContextChangeListener);
                myTargetAxiModel.addComponentListener(myContextChangeListener);
            }

            if (fireChanges) {
                myChangeSupport.fireMapperContextChanged(oldAxiModel, myTargetAxiModel);
            }
        }

        if ((myTargetComponent != null && !myTargetComponent.equals(oldValue)) || (oldValue != null && !oldValue.equals(myTargetComponent))) {
//            if (oldValue != null) {
//                oldValue.removePropertyChangeListener(myContextChangeListener);
//            }
//
//            if (myTargetComponent != null) {
//                myTargetComponent.addPropertyChangeListener(myContextChangeListener);
//            }
            if (fireChanges) {
                myChangeSupport.fireTargetTypeChanged(oldValue, myTargetComponent);
            }
        }
    }

    public AXIComponent getSourceType() {
        return mySourceComponent;
    }

    private void setSourceType(AXIComponent axiComp, boolean fireChanges) {
        AXIComponent oldValue = mySourceComponent;
//        SchemaModel oldModel = mySourceSchemaModel;
        AXIModel oldAxiModel = mySourceAxiModel;
        
        this.mySourceComponent = axiComp;

        mySourceAxiModel = mySourceComponent != null 
                ? mySourceComponent.getModel() : null;
//        this.mySourceSchemaModel = mySourceAxiModel != null 
//                ? mySourceAxiModel.getSchemaModel() : null;

//        if ((mySourceSchemaModel != null && !mySourceSchemaModel.equals(oldModel)) || (oldModel != null && !oldModel.equals(mySourceSchemaModel))) {
//            if (oldModel != null) {
//                oldModel.removePropertyChangeListener(myContextChangeListener);
//            }
//
//            if (mySourceSchemaModel != null) {
//                mySourceSchemaModel.addPropertyChangeListener(myContextChangeListener);
//            }
////            myChangeSupport.fireMapperContextChanged(oldModel, mySourceSchemaModel);
//        }

        
        if ((mySourceAxiModel != null && !mySourceAxiModel.equals(oldAxiModel)) 
                || (oldAxiModel != null && !oldAxiModel.equals(mySourceAxiModel)) 
                || (oldAxiModel == null && mySourceAxiModel == null)) {
            if (oldAxiModel != null) {
                oldAxiModel.removePropertyChangeListener(myContextChangeListener);
                oldAxiModel.removeComponentListener(myContextChangeListener);
            }

            if (mySourceAxiModel != null) {
                mySourceAxiModel.addPropertyChangeListener(myContextChangeListener);
                mySourceAxiModel.addComponentListener(myContextChangeListener);
            }

            if (fireChanges) {
                myChangeSupport.fireMapperContextChanged(oldAxiModel, mySourceAxiModel);
            }
        }
        
        
        if ((mySourceComponent != null && !mySourceComponent.equals(oldValue)) || (oldValue != null && !oldValue.equals(mySourceComponent)) || (oldValue == null && mySourceComponent == null)) {
//            if (oldValue != null) {
//                oldValue.removePropertyChangeListener(myContextChangeListener);
//            }
//
//            if (mySourceComponent != null) {
//                mySourceComponent.addPropertyChangeListener(myContextChangeListener);
//            }

            if (fireChanges) {
                myChangeSupport.fireSourceTypeChanged(oldValue, mySourceComponent);
            }
        }
    }

    public void addMapperContextChangeListener(MapperContextChangeListener listener) {
        myChangeSupport.addPropertyChangeListener(listener);
    }

    public void removeMapperContextChangeListener(MapperContextChangeListener listener) {
        myChangeSupport.removePropertyChangeListener(listener);
    }

    private void setSourcePart(WSDLReference<Part> sourcePart) {
        mySourcePart = sourcePart;
        WSDLModel oldSourceModel = mySourceModel;
        mySourceModel = getWsdlModel(mySourcePart);


        if ((mySourceModel != null && !mySourceModel.equals(oldSourceModel)) || (oldSourceModel != null && !oldSourceModel.equals(mySourcePart))) {
            if (oldSourceModel != null) {
                oldSourceModel.removePropertyChangeListener(myContextChangeListener);
            }

            if (mySourceModel != null) {
                mySourceModel.addPropertyChangeListener(myContextChangeListener);
            }
        }
    }

    private WSDLReference<Part> getSourcePart(Transform transform) {
        VariableReference varRef = transform == null ? null : transform.getSource();
        return varRef == null ? null : varRef.getPart();
    }

    private void setTargetPart(WSDLReference<Part> targetPart) {
        myTargetPart = targetPart;
        WSDLModel oldTargetModel = myTargetModel;
        myTargetModel = getWsdlModel(myTargetPart);


        if ((myTargetModel != null && !myTargetModel.equals(oldTargetModel)) || (oldTargetModel != null && !oldTargetModel.equals(myTargetPart))) {
            if (oldTargetModel != null) {
                oldTargetModel.removePropertyChangeListener(myContextChangeListener);
            }

            if (myTargetModel != null) {
                myTargetModel.addPropertyChangeListener(myContextChangeListener);
            }
        }
    }

    private WSDLReference<Part> getTargetPart(Transform transform) {
        VariableReference varRef = transform == null ? null : transform.getResult();
        return varRef == null ? null : varRef.getPart();
    }

    private WSDLModel getWsdlModel(WSDLReference<Part> partRef) {
        WSDLModel model = null;
        if (partRef != null) {
            Part resultPart = partRef.get();
            model = resultPart == null ? null : resultPart.getModel();
        }
        return model;
    }

    private void addContextChangeListeners() {
        if (myTMapModel != null) {
            myTMapModel.addPropertyChangeListener(myContextChangeListener);
//            myTMapModel.addComponentListener(myContextComponentListener);
        }
        if (myTMapFo != null) {
            myTMapFo.addFileChangeListener(myFileChangeListener);
        }

        if (myXslFo != null) {
            myXslFo.addFileChangeListener(myFileChangeListener);

            Project xsltProject = FileOwnerQuery.getOwner(myXslFo);
            myProjectsFilesChangeHandler = xsltProject.getLookup().lookup(ProjectsFilesChangeHandler.class);
            if (myProjectsFilesChangeHandler != null) {
                myProjectsFilesChangeHandler.addProjectsFilesChangeListener(myProjectsFilesChangeListener);
            }
        }

//        if (mySourceComponent != null) {
////            mySourceComponent.addPropertyChangeListener(myContextChangeListener);
//            mySoaxiModel = mySourceComponent.getModel();
//        }
        if (mySourceAxiModel != null) {
            mySourceAxiModel.addPropertyChangeListener(myContextChangeListener);
            mySourceAxiModel.addComponentListener(myContextChangeListener);
        }
//        if (mySourceSchemaModel != null) {
//            mySourceSchemaModel.addPropertyChangeListener(myContextChangeListener);
//        }
        
//        if (myTargetComponent != null) {
////            myTargetComponent.addPropertyChangeListener(myContextChangeListener);
//            AXIModel axiModel = myTargetComponent.getModel();
//            if (axiModel != null) {
//                myTargetAxiDocument = axiModel.getRoot();
//                myTargetAxiDocument.addPropertyChangeListener(myContextChangeListener);
//            }
//        }
        if (myTargetAxiModel != null) {
            myTargetAxiModel.addPropertyChangeListener(myContextChangeListener);
            myTargetAxiModel.addComponentListener(myContextChangeListener);
        }
//        if (myTargetSchemaModel != null) {
//            myTargetSchemaModel.addPropertyChangeListener(myContextChangeListener);
//        }

        if (mySourceModel != null) {
            mySourceModel.addPropertyChangeListener(myContextChangeListener);
        }

        if (myTargetModel != null) {
            myTargetModel.addPropertyChangeListener(myContextChangeListener);
        }

        
    }

    public void removeContextChangeListeners() {
        if (myTMapModel != null) {
            myTMapModel.removePropertyChangeListener(myContextChangeListener);
//            myTMapModel.removeComponentListener(myContextComponentListener);
        }
        if (myTMapFo != null) {
            myTMapFo.removeFileChangeListener(myFileChangeListener);
        }

        if (myXslFo != null) {
            myXslFo.removeFileChangeListener(myFileChangeListener);
        }

        if (myProjectsFilesChangeHandler != null) {
            myProjectsFilesChangeHandler.removeProjectsFilesChangeListener(myProjectsFilesChangeListener);
        }
        
//        if (mySourceComponent != null ) {
//            mySourceComponent.removePropertyChangeListener(myContextChangeListener);
//            AXIModel axiModel = mySourceComponent.getModel();
//            if (axiModel != null) {
//                mySourceAxiDocument = axiModel.getRoot();
//                mySourceAxiDocument.addPropertyChangeListener(myContextChangeListener);
//            }
//        }
        if (mySourceAxiModel != null) {
            mySourceAxiModel.removePropertyChangeListener(myContextChangeListener);
            mySourceAxiModel.removeComponentListener(myContextChangeListener);
        }
//        if (mySourceSchemaModel != null) {
//            mySourceSchemaModel.removePropertyChangeListener(myContextChangeListener);
//        }
        
        
//        if (myTargetComponent != null ) {
//            myTargetComponent.removePropertyChangeListener(myContextChangeListener);
//            AXIModel axiModel = myTargetComponent.getModel();
//            if (axiModel != null) {
//                myTargetAxiDocument = axiModel.getRoot();
//                myTargetAxiDocument.addPropertyChangeListener(myContextChangeListener);
//            }
//        }
        if (myTargetAxiModel != null) {
            myTargetAxiModel.removePropertyChangeListener(myContextChangeListener);
            myTargetAxiModel.removeComponentListener(myContextChangeListener);
        }
//        if (myTargetSchemaModel != null) {
//            myTargetSchemaModel.removePropertyChangeListener(myContextChangeListener);
//        }
    }

    private void reinitContext() {
        reinitContext(null);
    }
    
    private void reinitContext(final EventObject evt) {
        if (myPreviousChangeTask != null) {
            myPreviousChangeTask.cancel();
        }
        if (myPreviousChangeTask != null && !myPreviousChangeTask.isFinished()) {
            myPreviousChangeTask.waitFinished();
            myPreviousChangeTask = null;
        }

        myPreviousChangeTask = RequestProcessor.getDefault().post(
                new Runnable() {
            public void run() {
//                setActivatedNodes(curEditorPane.getCaret().getDot());
                MapperContextFactory.getInstance().reinitMapperContext(
                        MapperContextImpl.this, myXslFo, SoaUtil.getProject(myTMapFo),
                        evt);
            }
        }, CONTEXT_CHANGE_TASK_DELAY);
        
//        MapperContextFactory.getInstance().reinitMapperContext(this, myXslFo, Util.getProject(myTMapFo));
    }

    private class ContextPropertyChangeListener 
            implements PropertyChangeListener, ComponentListener        
    {

        public void propertyChange(PropertyChangeEvent evt) {
//System.out.println("ContextPropertyChangeListener: source: "+evt.getSource()+"; propName: "+evt.getPropertyName());
               reinitContext(evt);
        }

        public void valueChanged(ComponentEvent arg0) {
            reinitContext(arg0);
        }

        public void childrenAdded(ComponentEvent arg0) {
            reinitContext(arg0);        }

        public void childrenDeleted(ComponentEvent arg0) {
            reinitContext(arg0);
        }

//        private void attributeAdded(Object sourceObj, String property, Object oldValue) {
//System.out.println("attributeAdded: ContextPropertyChangeListener: source: "+sourceObj+"; propName: "+property);
//              reinitContext();
//            
//        }
//
//        private void attributeChanged(Object sourceObj, String property, Object oldValue, Object newValue) {
//System.out.println("attributeChanged: ContextPropertyChangeListener: source: "+sourceObj+"; propName: "+property);
//              reinitContext();
//
//        }
//
//        private void attributeRemoved(Object sourceObj, String property, Object oldValue) {
//System.out.println("attributeRemoved: ContextPropertyChangeListener: source: "+sourceObj+"; propName: "+property);
//            reinitContext();
//        }
//
//        private void childrenAdded(Object sourceObj, String property, Object oldValue) {
//System.out.println("childrenAdded: ContextPropertyChangeListener: source: "+sourceObj+"; propName: "+property);
//                reinitContext();
//        }
//
//        private void childrenRemoved(Object sourceObj, String property, Object oldValue) {
//System.out.println("childrenRemoved: ContextPropertyChangeListener: source: "+sourceObj+"; propName: "+property);
//                reinitContext();
//        }
    }

//    private class ComponentChangeListenerImpl implements ComponentListener {
//
//        public void valueChanged(ComponentEvent evt) {
//            Object changedValue = evt.getSource();
//            System.out.println("value changed: "+changedValue);
//            if (changedValue instanceof Part) {
//                reinitContext();
//            }
//
////            if (changedValue instanceof TMapComponent
////                    && ((TMapComponent)changedValue).) {
////            }
//
////            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public void childrenAdded(ComponentEvent evt) {
//            Object childrenAdded = evt.getSource();
//            System.out.println("children added: "+childrenAdded);
//            if ((mySourcePart == null || myTargetPart == null)
//                    && childrenAdded instanceof Part )
//            {
//                reinitContext();
//            }
//
////            if (myTransformContextComponent == null && childrenAdded instanceof Operation) {
////                reinitContext();
////            } else if (myTransformContextComponent != null
////                    && myTransformContextComponent.equals(childrenAdded)
////                    && TMapComponents.PARAM.equals(((TMapComponent)childrenAdded).getComponentType()))
////            {
////                // todo m add new param into source
////                setSourceType(myTargetComponent);
//////                reinitContext();
////            }
//////            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public void childrenDeleted(ComponentEvent evt) {
//            Object childrenDeleted = evt.getSource();
//            System.out.println("children deleted: "+childrenDeleted);
////            if (myTransformContextComponent != null
////                    && childrenDeleted instanceof Operation
////                    && childrenDeleted.equals(myTransformContextComponent.getParent()))
////            {
////                reinitContext();
////            } else if (myTransformContextComponent != null
////                    && myTransformContextComponent.equals(childrenDeleted))
////            {
////                // todo m deleted params
////                setSourceType(myTargetComponent);
////            }
////            throw new UnsupportedOperationException("Not supported yet.");
//        }
//    }

    private class FileChangeListenerImpl implements FileChangeListener {

        public void fileFolderCreated(FileEvent fe) {
//            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void fileDataCreated(FileEvent fe) {
            FileObject fo = fe.getFile();
            if (fo != null && myTMapFo == null && fo.equals(org.netbeans.modules.xslt.tmap.util.Util.getTMapFo(SoaUtil.getProject(fo)))) {
                reinitContext();
                myTMapFo.addFileChangeListener(myFileChangeListener);
                if (myTMapModel != null) {
                    myTMapModel.addPropertyChangeListener(myContextChangeListener);
                }
            }
        }

        public void fileChanged(FileEvent fe) {
        }

        public void fileDeleted(FileEvent fe) {
            FileObject fo = fe.getFile();
            if (fo != null) {
                fo.removeFileChangeListener(myFileChangeListener);
            }
        }

        public void fileRenamed(FileRenameEvent fe) {
            FileObject fo = fe.getFile();

            // TODO m
            if (fo.equals(myXslFo)) {
                reinitContext();
            }

            if (fo.equals(myTMapFo)) {
                reinitContext();
            }
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
    }

    private class ProjectsFilesChangeListenerImpl implements ProjectsFilesChangeListener {

        public void fileAdded(FileObject fo) {
            if (mySourceComponent == null || myTargetComponent == null) {
                reinitContext();
            }
        }

        public void fileRenamed(FileRenameEvent fe) {
            reinitContext();
        }

        public void fileDeleted(FileObject fo) {
            reinitContext();
        }
    }

    public String getValidationMessage() {
        String result = null;
        TransformmapValidator validator = TransformmapValidatorImpl.getInstance();

        if (myXslFo != null) {
            result = validator.validate(Util.getTransformationDescriptor(SoaUtil.getProject(myXslFo)));

            if (result == null) {
                result = validator.validate(myTMapModel, myXslFo);
            }
            if (result == null) {
                AXIComponent typeIn = getSourceType();
                result = validator.validate(typeIn, "source"); // NOI18N

            }
            if (result == null) {
                AXIComponent typeOut = getTargetType();
                result = validator.validate(typeOut, "target"); // NOI18N
            }
        }
        return result;
    }
}
