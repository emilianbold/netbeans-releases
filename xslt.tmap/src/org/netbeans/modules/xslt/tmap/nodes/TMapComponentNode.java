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

package org.netbeans.modules.xslt.tmap.nodes;

import java.awt.Image;
import org.netbeans.modules.soa.ui.nodes.InstanceRef;
import org.netbeans.modules.soa.ui.nodes.NodeTypeHolder;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TMapComponentNode<T extends DecoratedTMapComponent> extends AbstractNode 
        implements InstanceRef<T>, NodeTypeHolder<NodeType> 
{

    private T myDecoratedComponent; 
    private NodeType myNodeType;
    private Synchronizer synchronizer = new Synchronizer();
    private Object NAME_LOCK = new Object();
    private String cachedName;
    private String cachedShortDescription;
    private String cachedHtmlDisplayName;

    public TMapComponentNode(T ref, Lookup lookup) {
        this(ref, Children.LEAF, lookup);
    }
    
    public TMapComponentNode(T ref, Children children, Lookup lookup) {
        super(children);
        setReference(ref);
        myNodeType = NodeType.getNodeType(ref.getOriginal());
        assert myNodeType != null;
        
        TMapModel model = ref.getOriginal().getModel();

        assert model != null: "Can't create Node for orphaned TMapComponent"; // NOI18N
        model.addComponentListener(synchronizer);
        
        // set nodeDescription property which is shown in property sheet help region
        setValue("nodeDescription", "");
    }

    public Object getAlternativeReference() {
        return null;
    }

    public NodeType getNodeType() {
        return myNodeType;
    }
    
    private void setReference(T ref) {
        myDecoratedComponent = ref;
    }

    /**
     * The reference to an object which the node represents.
     */
    public T getReference() {
        return myDecoratedComponent;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        
        //
        if (obj instanceof TMapComponentNode) {
            Object thisOrig = this.getReference().getOriginal();
            Object objOrig = ((TMapComponentNode) obj).getReference();
            
            return this.getNodeType().equals(((TMapComponentNode) obj).getNodeType())
                    && thisOrig != null
                    && thisOrig.equals(objOrig);
        }
        //
        return false;
    }

    @Override
    public boolean canCopy() {
        return false;
    }
    
    @Override
    public boolean canCut() {
        return false;
    }

    @Override
    public boolean canDestroy() {
        return false;
    }

    @Override
    public Image getIcon(int type) {
        T ref = getReference();
        return ref == null ? null : ref.getIcon();
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    protected String getNameImpl(){
        T ref = getReference();
        String name = null;
        return ref != null ? ref.getName() : "";
    }
    
    @Override
    public String getName() {
        synchronized(NAME_LOCK) {
            if (cachedName == null){
                cachedName = getNameImpl();
            }
            return cachedName;
        }
    }
    
    @Override
    public String getDisplayName() {
        String instanceName = getName();
        return ((instanceName == null || instanceName.length() == 0)
        ? "" : instanceName + " ") +
                "[" + getNodeType().getDisplayName() + "]"; // NOI18N
    }
    
    @Override
    public String getHtmlDisplayName() {
        synchronized (NAME_LOCK) {
            if (cachedHtmlDisplayName == null) {
                cachedHtmlDisplayName = getImplHtmlDisplayName();
            }
        }
        
        return cachedHtmlDisplayName;
    }
    
    protected String getImplHtmlDisplayName() {
        T ref = getReference();
        String name = null;
        return ref != null ? ref.getHtmlDisplayName() : "";
    }
    
    protected String getImplShortDescription() {
        T ref = getReference();
        String name = null;
        return ref != null ? ref.getTooltip() : "";
    }
    
    @Override
    public String getShortDescription() {
        synchronized (NAME_LOCK) {
            if (cachedShortDescription == null) {
                cachedShortDescription = getImplShortDescription();
            }
        }
        return cachedShortDescription;
    }
    
    public void updateName(){
        
        //name will be reloaded from model during next getName call
        synchronized(NAME_LOCK){
            cachedName = null;
        }
        
        fireNameChange(null, getName());
        synchronized(NAME_LOCK){
            cachedShortDescription = null;
        }
        
        synchronized(NAME_LOCK){
            cachedHtmlDisplayName = null;
        }
        fireShortDescriptionChange(null,getShortDescription());
        fireDisplayNameChange(null, getDisplayName());
    }
    
//    protected void updateProperty(PropertyType propertyType) {
//        updateProperty(propertyType.toString());
//    }
//    
//    protected void updateProperty(String propertyName) {
//        Property prop = PropertyUtils.lookForPropertyByName(this, propertyName);
//        Object newValue = null;
//        try {
//            newValue = prop.getValue();
//        } catch (Exception ex) {
//            // do nothing here
//        }
//        firePropertyChange(propertyName, UNKNOWN_OLD_VALUE, newValue);
//    }
    
    public void updatePropertyChange(String propertyName,
            Object oldValue,Object newValue) {
        firePropertyChange(propertyName, oldValue, newValue);
        synchronized(NAME_LOCK){
            cachedShortDescription = null;
        }
        
        synchronized(NAME_LOCK){
            cachedHtmlDisplayName = null;
        }
        fireDisplayNameChange(null, getDisplayName());
        fireShortDescriptionChange(null,getShortDescription());
    }
    
    public void updateShortDescription() {
        synchronized(NAME_LOCK){
            cachedShortDescription = null;
        }
        
        fireShortDescriptionChange(null,getShortDescription());
    }

//    /**
//     * Iterates over all registered properties and update them.
//     */
//    public void updateAllProperties() {
//        PropertySet[] psArr = getSheet().toArray();
//        for (PropertySet ps : psArr) {
//            Property[] propArr = ps.getProperties();
//            for (Property prop : propArr) {
//                String propName = prop.getName();
//                try {
//                    Object newPropValue = prop.getValue();
//                    firePropertyChange(propName, UNKNOWN_OLD_VALUE, newPropValue);
//                } catch (Exception ex) {
//                    ErrorManager.getDefault().notify(ex);
//                }
//            }
//        }
//    }
//    
//    public void updateAttributeProperty(String attributeName) {
//        Property prop = PropertyUtils.lookForPropertyByBoundedAttribute(
//                this, attributeName);
//        if (prop != null) {
//            String propName = prop.getName();
//            try {
//                Object newPropValue = prop.getValue();
//                firePropertyChange(propName, UNKNOWN_OLD_VALUE, newPropValue);
//            } catch (Exception ex) {
//                ErrorManager.getDefault().notify(ex);
//            }
//        }
//        
//    }
//    
//    public void updateElementProperty(Class elementClass) {
//        Property prop = PropertyUtils.lookForPropertyByBoundedElement(
//                this, elementClass);
//        if (prop != null) {
//            String propName = prop.getName();
//            try {
//                Object newPropValue = prop.getValue();
//                firePropertyChange(propName, UNKNOWN_OLD_VALUE, newPropValue);
//            } catch (Exception ex) {
//                ErrorManager.getDefault().notify(ex);
//            }
//        }
//    }
    
    private class Synchronizer implements ComponentListener {
        public Synchronizer() {
        }

        public void valueChanged(ComponentEvent evt) {
//            System.out.println("value changed: "+evt.getSource());
        }

        public void childrenAdded(ComponentEvent evt) {
//            System.out.println("value changed: "+evt.getSource());
            reloadChildren();
        }

        public void childrenDeleted(ComponentEvent evt) {
//            System.out.println("value changed: "+evt.getSource());
            reloadChildren();
        }

        private void reloadChildren() {
            
            Children children = getChildren();
            
            if (children instanceof ReloadableChildren) {
                ((ReloadableChildren)children).reload();
            } else if (TMapComponentNode.this instanceof ReloadableChildren) {
                ((ReloadableChildren) TMapComponentNode.this).reload();
            }
        }
        
//////        public void notifyEntityRemoved(EntityRemoveEvent event) {
//////            BpelEntity entity = event.getOutOfModelEntity();
//////            //
//////            T ref = getReference();
//////            if (ref == null) {
//////                //
//////                // the referenced element already removed
//////                //
//////                BpelModel bpelModel = entity.getBpelModel();
//////                unsubscribedFromAndDestroy(bpelModel);
//////            } else {
//////                if (BpelNode.this.isEventRequreUpdate(event)) {
//////                    reloadChildren();
//////                }
//////                
//////                if (event.getOutOfModelEntity() instanceof Documentation 
//////                        && ref.equals(event.getParent())) 
//////                {
//////                    updateShortDescription();
//////                }
//////            }
//////            //
//////            // Perform update processing of complex ptoperties
//////            updateComplexProperties(event);
//////        }
//////        
//////        public void notifyPropertyRemoved(PropertyRemoveEvent event) {
//////            BpelEntity entity = event.getParent();
//////            
//////            //
////////////            if (BpelNode.this.isEventRequreUpdate(event)) {
////////////                String attributeName = event.getName();
////////////                updateAttributeProperty(attributeName);
////////////                updateName();
////////////                //
////////////                // Check if the property has the List type
////////////                Object value = event.getOldValue();
////////////                if (value != null && value instanceof List) {
////////////                    reloadChildren();
////////////                }
////////////            }
////////////            //
////////////            // Check that the property is the content of an entity
////////////            // which owned by the node's entity.
////////////            if (ContentElement.CONTENT_PROPERTY.equals(event.getName())) {
////////////                BpelEntity parentEntity = event.getParent();
////////////                //
////////////                T curEntity = getReference();
////////////                if (curEntity != null && parentEntity != null &&
////////////                        parentEntity.getParent() == curEntity) {
////////////                    updateElementProperty(parentEntity.getClass());
////////////                }
////////////            }
////////////
//////            //
//////            T ref = getReference();
//////            if (ref == null) {
//////                //
//////                // the referenced element already removed
//////                //
//////                BpelModel bpelModel = entity.getBpelModel();
//////                unsubscribedFromAndDestroy(bpelModel);
//////            } else {
//////                if (BpelNode.this.isEventRequreUpdate(event)) {
//////                    reloadChildren();
//////                }
//////            }
//////            //
//////            // Perform update processing of complex ptoperties
//////            updateComplexProperties(event);
//////        }
//////        
//////        public void notifyPropertyUpdated(PropertyUpdateEvent event) {
//////            if (BpelNode.this.isEventRequreUpdate(event)) {
//////                String attributeName = event.getName();
//////                updateAttributeProperty(attributeName);
//////                updateName();
//////                //
//////                // Check if the property has the List type
//////                Object value = event.getNewValue();
//////                if (value == null) {
//////                    value = event.getOldValue();
//////                }
//////                if (value != null && value instanceof List) {
//////                    reloadChildren();
//////                }
//////            }
//////            //
//////            // Check that the property is the content of an entity
//////            // which owned by the node's entity.
//////            if (ContentElement.CONTENT_PROPERTY.equals(event.getName())) {
//////                BpelEntity parentEntity = event.getParent();
//////                //
//////                T curEntity = getReference();
//////                if (curEntity != null && parentEntity != null &&
//////                        parentEntity.getParent() == curEntity) {
//////                    updateElementProperty(parentEntity.getClass());
//////                    if (parentEntity instanceof Documentation) {
//////                        updateShortDescription();
//////                    }
//////                }
//////            }
//////            //
//////            // Perform update processing of complex ptoperties
//////            updateComplexProperties(event);
//////        }
//////        
//////        public void notifyArrayUpdated(ArrayUpdateEvent event) {
//////            if (BpelNode.this.isEventRequreUpdate(event)) {
//////                reloadChildren();
//////            }
//////            //
//////            // Perform update processing of complex ptoperties
//////            updateComplexProperties(event);
//////        }
//////        
//////        public void notifyEntityUpdated(EntityUpdateEvent event) {
//////            if (BpelNode.this.isEventRequreUpdate(event)) {
//////                BpelEntity entity = event.getNewValue();
//////                if (entity == null) {
//////                    entity = event.getOldValue();
//////                }
//////                if (entity != null) {
//////                    updateElementProperty(entity.getClass());
//////                }
//////                //
//////                reloadChildren();
//////            }
//////            
//////            //
//////            // Perform update processing of complex ptoperties
//////            updateComplexProperties(event);
//////        }
//////        
//////        public void notifyEntityInserted(EntityInsertEvent event) {
//////            if (BpelNode.this.isEventRequreUpdate(event)) {
//////                BpelEntity entity = event.getValue();
//////                if (entity != null) {
//////                    updateElementProperty(entity.getClass());
//////                }
//////                
//////                if (entity instanceof Documentation) {
//////                    updateShortDescription();
//////                } else {
//////                    //
//////                    reloadChildren();
//////                }
//////            }
//////            //
//////            // Perform update processing of complex ptoperties
//////            updateComplexProperties(event);
//////        }
        
    }

}
