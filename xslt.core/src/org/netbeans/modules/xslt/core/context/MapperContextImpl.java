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
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xslt.core.util.Util;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.mapper.model.MapperContext;
import org.netbeans.modules.xslt.mapper.model.MapperContextChangeListener;
import org.netbeans.modules.xslt.mapper.model.MapperContextChangeSupport;
import org.netbeans.modules.xslt.model.XslModel;
import org.netbeans.modules.xslt.tmap.model.api.Operation;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.impl.TMapComponents;
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
    private AXIComponent myTargetComponent;
    private MapperContextChangeSupport myChangeSupport;
    private TMapModel myTMapModel;
    
    private PropertyChangeListener myContextChangeListener = 
                                    new ContextPropertyChangeListener();
//    private ComponentListener myContextComponentListener = 
//                                    new ComponentChangeListenerImpl();
    private FileChangeListener myFileChangeListener = 
                                    new FileChangeListenerImpl();

    private FileObject myTMapFo;
    private FileObject myXslFo;

    public MapperContextImpl(XslModel xslModel, TMapModel model) {
//        assert model != null;
        this.myXslModel = xslModel;
        this.myTMapModel = model;
        this.myChangeSupport = new MapperContextChangeSupport();
        
        myTMapFo = Util.getFileObjectByModel(myTMapModel);
        myXslFo = myXslModel == null 
                ? null : Util.getFileObjectByModel(myXslModel);
        
        addContextChangeListeners();
    }

    // TODO m
    public MapperContextImpl(Transform transform, XslModel xslModel, AXIComponent sourceComponent, AXIComponent targetComponent) {
        this.myXslModel = xslModel;
        this.myTMapModel = transform == null ? null : transform.getModel();
        
        this.myTransformContextComponent = transform;
        this.mySourceComponent = sourceComponent;
        this.myTargetComponent = targetComponent;
        this.myChangeSupport = new MapperContextChangeSupport();

//        assert myTMapModel != null;
        myTMapFo = Util.getFileObjectByModel(myTMapModel);
        myXslFo = myXslModel == null 
                ? null : Util.getFileObjectByModel(myXslModel);

        addContextChangeListeners();
        
//        assocTransformDesc.getModel().addPropertyChangeListener(this);
    }

////    public MapperContextImpl(Operation operation, XslModel xslModel, AXIComponent sourceComponent, AXIComponent targetComponent) {
////        this.myTransformContextComponent = operation;
////        this.myXslModel = xslModel;
////        this.mySourceComponent = sourceComponent;
////        this.myTargetComponent = targetComponent;
//////        this.changeSupport = new MapperContextChangeSupport();
//////        
//////        assocTransformDesc.getModel().addPropertyChangeListener(this);
////    }

    public TMapModel getTMapModel() {
        return myTMapModel;
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
        myChangeSupport.fireTargetTypeChanged(oldValue, myTargetComponent);
    }

    public AXIComponent getSourceType() {
        return mySourceComponent;
    }
    protected void setSourceType(AXIComponent axiComp) {
        AXIComponent oldValue = mySourceComponent;
        this.mySourceComponent = axiComp;
        myChangeSupport.fireSourceTypeChanged(oldValue, mySourceComponent);
    }

    public void addMapperContextChangeListener(MapperContextChangeListener listener) {
        myChangeSupport.addPropertyChangeListener(listener);
    }

    public void removeMapperContextChangeListener(MapperContextChangeListener listener) {
        myChangeSupport.removePropertyChangeListener(listener);
    }

    private void addContextChangeListeners() {
        if (myTMapFo != null) {
            myTMapModel.addPropertyChangeListener(myContextChangeListener);
//            myTMapModel.addComponentListener(myContextComponentListener);
        }
        if (myTMapFo != null) {
            myTMapFo.addFileChangeListener(myFileChangeListener);
        }
        
        if (myXslFo != null) {
            myXslFo.addFileChangeListener(myFileChangeListener);
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
        
    }

    private void reinitContext() {
        MapperContextFactory.getInstance().reinitMapperContext(this, 
                myXslFo, Util.getProject(myTMapFo));
    }
    
    private class ContextPropertyChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            String property = evt.getPropertyName();
            Object sourceObj = evt.getSource();
            Object oldValue = evt.getOldValue();
            Object newValue = evt.getNewValue();
            
            if (Model.STATE_PROPERTY.equals(property)) {
                if (oldValue instanceof Model.State 
                        && newValue instanceof Model.State) 
                {
                    if (sourceObj instanceof TMapModel) {
                        myChangeSupport.fireTMapModelStateChanged((Model.State)evt.getOldValue(),
                                (Model.State)evt.getNewValue());
                        if (Model.State.VALID.equals(newValue)) {
                            reinitContext();
                        } else {
                            setSourceType(null);
                            setTargetType(null);
                        }
                    } 
                    if (sourceObj instanceof XslModel) {
                        myChangeSupport.fireXslModelStateChanged((Model.State)evt.getOldValue(),
                                (Model.State)evt.getNewValue());
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
            }
            
//            throw new UnsupportedOperationException("Not supported yet.");
        }

        private void attributeAdded(Object sourceObj, String property, Object oldValue) {
            if (myTransformContextComponent != null 
                    && myTransformContextComponent.equals(sourceObj)) 
            {
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
            } else if (myTransformContextComponent != null 
                    && myTransformContextComponent.equals(oldValue)
                    && TMapComponents.PARAM.equals(((TMapComponent)oldValue).getComponentType()))
            {
                // todo m add new param into source
                setSourceType(myTargetComponent);
//                reinitContext();
            } else {
                reinitContext();
            }
        }

        private void childrenRemoved(Object sourceObj, String property, Object oldValue) {
            if (myTransformContextComponent != null 
                    && oldValue instanceof Operation
                    && oldValue.equals(myTransformContextComponent.getParent())) 
            {
                reinitContext();
            } else if (myTransformContextComponent != null 
                    && myTransformContextComponent.equals(oldValue)) 
            {
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
//            if (myTransformContextComponent == null && childrenAdded instanceof Operation) {
//                reinitContext();
//            } else if (myTransformContextComponent != null 
//                    && myTransformContextComponent.equals(childrenAdded)
//                    && TMapComponents.PARAM.equals(((TMapComponent)childrenAdded).getComponentType()))
//            {
//                // todo m add new param into source
//                setSourceType(myTargetComponent);
////                reinitContext();
//            }
////            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        public void childrenDeleted(ComponentEvent evt) {
//            Object childrenDeleted = evt.getSource();
//            System.out.println("children deleted: "+childrenDeleted);
//            if (myTransformContextComponent != null 
//                    && childrenDeleted instanceof Operation
//                    && childrenDeleted.equals(myTransformContextComponent.getParent())) 
//            {
//                reinitContext();
//            } else if (myTransformContextComponent != null 
//                    && myTransformContextComponent.equals(childrenDeleted)) 
//            {
//                // todo m deleted params
//                setSourceType(myTargetComponent);                
//            }
////            throw new UnsupportedOperationException("Not supported yet.");
//        }
//    }
//    
    private class FileChangeListenerImpl implements FileChangeListener {

        public void fileFolderCreated(FileEvent fe) {
//            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void fileDataCreated(FileEvent fe) {
            FileObject fo = fe.getFile();
            if (fo != null 
                    && myTMapFo == null 
                    && fo.equals(org.netbeans.modules.xslt.tmap.util.Util.getTMapFo(Util.getProject(fo)))) 
            {
                reinitContext();
            }
//            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void fileChanged(FileEvent fe) {
//         throw new UnsupportedOperationException("Not supported yet.");
        }

        public void fileDeleted(FileEvent fe) {
            FileObject fo = fe.getFile();
//            throw new UnsupportedOperationException("Not supported yet.");
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
//            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

}
