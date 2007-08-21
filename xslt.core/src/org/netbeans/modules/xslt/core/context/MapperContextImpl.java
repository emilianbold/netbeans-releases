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
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xslt.tmap.util.Util;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.mapper.model.MapperContext;
import org.netbeans.modules.xslt.mapper.model.MapperContextChangeListener;
import org.netbeans.modules.xslt.mapper.model.MapperContextChangeSupport;
import org.netbeans.modules.xslt.model.XslModel;
import org.netbeans.modules.xslt.project.spi.ProjectsFilesChangeHandler;
import org.netbeans.modules.xslt.project.spi.ProjectsFilesChangeListener;
import org.netbeans.modules.xslt.tmap.model.api.Operation;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.api.VariableReference;
import org.netbeans.modules.xslt.tmap.model.api.WSDLReference;
import org.netbeans.modules.xslt.tmap.model.impl.TMapComponents;
import org.netbeans.modules.xslt.tmap.model.validation.TransformmapValidator;
import org.netbeans.modules.xslt.tmap.model.validation.TransformmapValidatorImpl;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class MapperContextImpl implements MapperContext {

    private Transform myTransformContextComponent;
    private XslModel myXslModel;
    private AXIComponent mySourceComponent;
    private WSDLReference<Part> mySourcePart;
    private WSDLModel mySourceModel;

    private AXIComponent myTargetComponent;
    private WSDLReference<Part> myTargetPart;
    private WSDLModel myTargetModel;

    private MapperContextChangeSupport myChangeSupport = new MapperContextChangeSupport();
    private TMapModel myTMapModel;

    private PropertyChangeListener myContextChangeListener = new ContextPropertyChangeListener();
//    private ComponentListener myContextComponentListener =
//                                    new ComponentChangeListenerImpl();
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
        mySourcePart = getSourcePart(myTransformContextComponent);
        mySourceModel = getWsdlModel(mySourcePart);

        this.myTargetComponent = targetComponent;
        myTargetPart = getTargetPart(myTransformContextComponent);
        myTargetModel = getWsdlModel(myTargetPart);

        myTMapFo = org.netbeans.modules.xslt.core.util.Util.getFileObjectByModel(myTMapModel);
        myXslFo = myXslModel == null ? null : org.netbeans.modules.xslt.core.util.Util.getFileObjectByModel(myXslModel);
    }

    private void init(Transform transform, XslModel xslModel, AXIComponent sourceComponent, AXIComponent targetComponent) {
        init(transform == null ? null : transform.getModel(), transform, xslModel, sourceComponent, targetComponent);
    }

    private void init(TMapModel tMapModel, XslModel xslModel) {
        init(tMapModel, null, xslModel, null, null);
    }


    public void reinit(TMapModel tMapModel, Transform transform, XslModel xslModel, AXIComponent sourceComponent, AXIComponent targetComponent) {
        setXslModel(xslModel);
        setTransformContextComponent(tMapModel, transform);
        setSourceType(sourceComponent);
        setTargetType(targetComponent);
    }

    public void setTransformContextComponent(TMapModel tMapModel, Transform context) {
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

            myTMapFo = org.netbeans.modules.xslt.core.util.Util.getFileObjectByModel(myTMapModel);
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

    public void setXslModel(XslModel xslModel) {
        XslModel oldXslModel = myXslModel;
        this.myXslModel = xslModel;

        if ((myXslModel != null && !myXslModel.equals(oldXslModel)) || (oldXslModel != null && !oldXslModel.equals(myXslModel))) {
            if (myXslFo != null) {
                myXslFo.removeFileChangeListener(myFileChangeListener);
            }

            myXslFo = myXslModel == null ? null : org.netbeans.modules.xslt.core.util.Util.getFileObjectByModel(myXslModel);
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

    protected void setTargetType(AXIComponent axiComp) {
        AXIComponent oldValue = myTargetComponent;
        this.myTargetComponent = axiComp;

        if ((myTargetComponent != null && !myTargetComponent.equals(oldValue)) || (oldValue != null && !oldValue.equals(myTargetComponent))) {
            if (oldValue != null) {
                oldValue.removePropertyChangeListener(myContextChangeListener);
            }

            if (myTargetComponent != null) {
                myTargetComponent.addPropertyChangeListener(myContextChangeListener);
            }
            myChangeSupport.fireTargetTypeChanged(oldValue, myTargetComponent);
        }
    }

    public AXIComponent getSourceType() {
        return mySourceComponent;
    }

    protected void setSourceType(AXIComponent axiComp) {
        AXIComponent oldValue = mySourceComponent;
        this.mySourceComponent = axiComp;

        if ((mySourceComponent != null && !mySourceComponent.equals(oldValue)) || (oldValue != null && !oldValue.equals(mySourceComponent))) {
            if (oldValue != null) {
                oldValue.removePropertyChangeListener(myContextChangeListener);
            }

            if (mySourceComponent != null) {
                mySourceComponent.addPropertyChangeListener(myContextChangeListener);
            }
            myChangeSupport.fireSourceTypeChanged(oldValue, mySourceComponent);
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

        if (mySourceComponent != null) {
            mySourceComponent.addPropertyChangeListener(myContextChangeListener);
        }

        if (myTargetComponent != null) {
            myTargetComponent.addPropertyChangeListener(myContextChangeListener);
        }

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
    }

    private void reinitContext() {
        MapperContextFactory.getInstance().reinitMapperContext(this, myXslFo, Util.getProject(myTMapFo));
    }

    private class ContextPropertyChangeListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            String property = evt.getPropertyName();
            Object sourceObj = evt.getSource();
            Object oldValue = evt.getOldValue();
            Object newValue = evt.getNewValue();

            if (Model.STATE_PROPERTY.equals(property)) {
                if (oldValue instanceof Model.State && newValue instanceof Model.State) {
                    if (sourceObj instanceof TMapModel) {
                        myChangeSupport.fireTMapModelStateChanged((Model.State) evt.getOldValue(), (Model.State) evt.getNewValue());
                        if (Model.State.VALID.equals(newValue)) {
                            reinitContext();
                        } else {
                            setSourceType(null);
                            setTargetType(null);
                        }
                    }
                    if (sourceObj instanceof XslModel) {
                        myChangeSupport.fireXslModelStateChanged((Model.State) evt.getOldValue(), (Model.State) evt.getNewValue());
                    }
                }
            } else if (sourceObj instanceof TMapComponent) {
                reinitContext();
// TODO r | m
//                if (newValue == null && oldValue instanceof TMapComponent) {
//                    childrenRemoved(sourceObj, property, oldValue);
//                } else if (newValue == null
//                        && !(oldValue instanceof TMapComponent))
//                {
//                    attributeRemoved(sourceObj, property, oldValue);
//                } else if (oldValue == null
//                        && newValue instanceof TMapComponent)
//                {
//                    childrenAdded(sourceObj, property, oldValue);
//                } else if (oldValue == null
//                        && !(newValue instanceof TMapComponent))
//                {
//                    attributeAdded(sourceObj, property, oldValue);
//                } else if (oldValue instanceof String
//                        && newValue instanceof String)
//                {
//                    attributeChanged(sourceObj, property, oldValue, newValue);
//                }
//
//
//                System.out.println("sourceeeee: "+sourceObj+"; oldValue: "+
//                        evt.getOldValue()+";newValue: "+evt.getNewValue());
//                if (! (sourceObj instanceof TMapComponent)) {
//                    return;
//                }
                //            } else if (mySourceComponent != null
//                    && mySourceComponent.equals(sourceObj))
//            {
//                myChangeSupport.fireSourceTypeChanged(mySourceComponent, mySourceComponent);
//            } else if (myTargetComponent != null
//                    && myTargetComponent.equals(sourceObj))
//            {
//                myChangeSupport.fireTargetTypeChanged(myTargetComponent, myTargetComponent);
            } else if (sourceObj instanceof Part) {
                reinitContext();
            } else if (sourceObj instanceof WSDLComponent && (newValue instanceof Part || oldValue instanceof Part)) {
                reinitContext();
            } else {
//                System.out.println("unsupported  event occur: property : "+property
//                        +"; source: "+sourceObj+"; oldValue: "+oldValue
//                        +"; newValue: "+newValue);
            }

//            throw new UnsupportedOperationException("Not supported yet.");
        }

        private void attributeAdded(Object sourceObj, String property, Object oldValue) {
            if (myTransformContextComponent != null && myTransformContextComponent.equals(sourceObj)) {
                reinitContext();
            }
        }

        private void attributeChanged(Object sourceObj, String property, Object oldValue, Object newValue) {
//            if (myTransformContextComponent != null
//                    && myTransformContextComponent.equals(sourceObj))
//            {
//                if (Transform.FILE.equals(oldValue)) {
            reinitContext();
//                }
//            }
        }

        private void attributeRemoved(Object sourceObj, String property, Object oldValue) {
//            if (myTransformContextComponent != null
//                    && myTransformContextComponent.equals(sourceObj))
//            {
//                if (Transform.FILE.equals(oldValue)) {
            reinitContext();
//                }
//            }
        }

        private void childrenAdded(Object sourceObj, String property, Object oldValue) {
            if (myTransformContextComponent == null && oldValue instanceof Operation) {
                reinitContext();
            } else if (myTransformContextComponent != null && myTransformContextComponent.equals(oldValue) && TMapComponents.PARAM.equals(((TMapComponent) oldValue).getComponentType())) {
                // todo m add new param into source
                setSourceType(myTargetComponent);
//                reinitContext();
            } else {
                reinitContext();
            }
        }

        private void childrenRemoved(Object sourceObj, String property, Object oldValue) {
            if (myTransformContextComponent != null && oldValue instanceof Operation && oldValue.equals(myTransformContextComponent.getParent())) {
                reinitContext();
            } else if (myTransformContextComponent != null && myTransformContextComponent.equals(oldValue)) {
                // todo m deleted params
                setSourceType(myTargetComponent);
            } else {
                reinitContext();
            }
        }
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
            if (fo != null && myTMapFo == null && fo.equals(org.netbeans.modules.xslt.tmap.util.Util.getTMapFo(Util.getProject(fo)))) {
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
            result = validator.validate(Util.getTransformationDescriptor(Util.getProject(myXslFo)));

            if (result == null) {
                result = validator.validate(myTMapModel, myXslFo);
            }
        }
        return result;
    }
}
