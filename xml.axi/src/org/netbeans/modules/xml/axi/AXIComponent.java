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
package org.netbeans.modules.xml.axi;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import org.netbeans.modules.xml.axi.Compositor.CompositorType;
import org.netbeans.modules.xml.axi.impl.AXIDocumentImpl;
import org.netbeans.modules.xml.axi.impl.AXIModelBuilder;
import org.netbeans.modules.xml.axi.impl.AXIModelImpl;
import org.netbeans.modules.xml.axi.impl.Util;
import org.netbeans.modules.xml.axi.visitor.AXIVisitor;
import org.netbeans.modules.xml.axi.visitor.DeepAXITreeVisitor;
import org.netbeans.modules.xml.schema.model.Documentation;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xam.AbstractComponent;
import org.openide.util.WeakListeners;

/**
 * Base component in the AXI model tree.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class AXIComponent extends AbstractComponent<AXIComponent>
        implements Cloneable, PropertyChangeListener {
    
    /**
     * Represents the type of this component.
     * Can be one of local, shared, proxy or reference.
     */
    public static enum ComponentType {
        LOCAL,
        SHARED,
        PROXY,
        REFERENCE
    }
    
    /**
     * Creates a new instance of AXIComponent.
     */
    public AXIComponent(AXIModel model) {
        super(model);
    }
    
    /**
     * Creates a new instance of AXIComponent.
     */
    public AXIComponent(AXIModel model, SchemaComponent schemaComponent) {
        super(model);
        setPeer(schemaComponent);
    }
    
    /**
     * Creates a proxy component for the specified global or shared component.
     */
    public AXIComponent(AXIModel model, AXIComponent sharedComponent) {
        super(model);
        setSharedComponent(sharedComponent);
    }

    /**
     * Allow an AXIVisitor to visit this component.
     */
    public abstract void accept(AXIVisitor visitor);
    
    
    /**
     * Returns this component's absolute index in the parent's children list.
     * Returns -1 if child or parent are not in model or the child is
     * not found in the parent's children list.
     */
    public int getIndex() {
        return getIndex(true);
    }
    
    /**
     * Returns this component's index (relative or absolute) in the parent's children list.
     * Returns -1 if child or parent are not in model or the child is
     * not found in the parent's children list.
     *
     * @param absolute - true, relative (to its type in parent) - false
     */
    public int getIndex(boolean absolute) {
        AXIComponent parent = getParent();
        if(parent == null || !isInModel())
            return -1;
        List<AXIComponent> childs = Collections.emptyList();
        if(absolute)
            childs = parent.getChildren();
        else
            childs = parent.getChildren((Class<AXIComponent>)this.getClass());
        for(int i=0; i<childs.size(); i++) {
            if(childs.get(i) == this) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * In AXI model a proxy acts on behalf of a sharable or global component.
     * However, there can be multiple levels of indirection to the original global
     * component.
     *
     * If this is a proxy, returns the shared global component, else returns
     * itself.
     */
    public AXIComponent getOriginal() {
        if(getComponentType() == ComponentType.REFERENCE)
            return this;

        if(getComponentType() == ComponentType.PROXY)
            return getSharedComponent().getOriginal();
        
        return this;
    }        
    
    /**
     * Returns the shared component, if any, null otherwise.
     */
    public AXIComponent getSharedComponent() {
        return sharedComponent;
    }
    
    /**
     * Returns true for a global component, false otherwise.
     */
    public boolean isGlobal() {
        return (getParent() instanceof AXIDocument);
    }    
    
    /**
     * Sets the shared component.
     */
    protected void setSharedComponent(AXIComponent sharedComponent) {
        this.sharedComponent = sharedComponent;
        if(sharedComponent == null) {
            return;
        }
        AXIModelImpl thisModel = (AXIModelImpl)getModel();
        if(thisModel == sharedComponent.getModel()) {
            sharedComponent.addListener(this);
            return;
        }
        
        //keep listening to the other model
        thisModel.listenToReferencedModel(sharedComponent.getModel());
    }
    
    /**
     * Add a listener to the shared component.
     */
    public void addListener(AXIComponent proxy) {
        if(getModel() != proxy.getModel())
            return;
        
        if(pcs == null)
            pcs = new PropertyChangeSupport(this);
        
        PropertyChangeListener l = getWeakListener(proxy, false);
        if(l != null)
            pcs.addPropertyChangeListener(l);
    }
    
    /**
     * Remove listener from the shared component.
     */
    public void removeListener(AXIComponent proxy) {
        if(pcs == null)
            return;
        
        pcs.removePropertyChangeListener(getWeakListener(proxy, true));
    }
    
    private void removeAllListeners() {
        if(pcs == null)
            return;
        
        for(AXIComponent listener: getRefSet()) {
            removeListener(listener);
        }
    }
    
    /**
     * Returns the list of components that are listening to this component.
     */
    public List<AXIComponent> getRefSet() {
        if(pcs == null || listenerMap == null)
            return null;
        Set<AXIComponent> keySet = listenerMap.keySet();
        return Collections.unmodifiableList(
                Arrays.asList(keySet.toArray(new AXIComponent[keySet.size()])));
    }
        
    private PropertyChangeListener getWeakListener(AXIComponent proxy, boolean remove) {
        if(listenerMap == null) {
            listenerMap = new WeakHashMap<AXIComponent, PropertyChangeListener>();
        }
        if(remove)
            return listenerMap.remove(proxy);
        
        if(proxy.getComponentType() != ComponentType.PROXY) {
            Set<AXIComponent> keySet = listenerMap.keySet();
            for(AXIComponent key : keySet) {
                if(key.getPeer() == proxy.getPeer())
                    return null;
            }
        }
        
        PropertyChangeListener listener = listenerMap.get(proxy);
        if(listener == null) {
            listener = (PropertyChangeListener)WeakListeners.
                    create(PropertyChangeListener.class, proxy, this);
            listenerMap.put(proxy, listener);
            return listener;
        }
        
        //if exists, return null.
        return null;
    }
    
    /**
     * Returns documentation for the schema component, if any.
     */
    public String getDocumentation() {
        if(getPeer() == null ||
           getPeer().getAnnotation() == null ||
           getPeer().getAnnotation().getDocumentationElements() == null) {
            return null;
        }
        
        StringBuilder buffer = new StringBuilder();
        for(Documentation doc : getPeer().getAnnotation().getDocumentationElements()) {
            buffer.append(doc.getContent());
        }
        
        return buffer.toString();
    }
    
    /**
     * Tells whether this component is mutable or not, w.r.t. the specified model.
     * Returns true if this component does not belong to the given model,
     * false otherwise.
     */
    public boolean isReadOnly() {
        if(!isInModel())
            return false;
        
        return (getModel().isReadOnly() || getModel() != getOriginal().getModel());
    }
    
    /**
     * Tells if this component supports cardinality or not.
     * Returns false for all global elements and types, true otherwise.
     */
    public boolean supportsCardinality() {
        return (getParent() instanceof AXIDocument) ? false: true;
    }
    
    /**
     * Returns true, if the children have been initialized, false otherwise.
     */
    public boolean canVisitChildren() {
        return super.isChildrenInitialized();
    }
    
    /**
     * Returns true, for proxies and references, false otherwise.
     */
    public boolean isShared() {
        ComponentType type = getComponentType();
        if(type == ComponentType.PROXY || type == ComponentType.REFERENCE)
            return true;
        
        return false;
    }
    
    /**
     * Returns the type of this component,
     * may be local, shared, proxy or reference.
     * @see ComponentType.
     */
    public ComponentType getComponentType() {
        if(getParent() instanceof AXIDocument)
            return ComponentType.SHARED;
        
        return ComponentType.LOCAL;
    }
    
    /**
     * Returns the content model, this AXI component belongs to.
     * Returns null for a local component.
     */
    public ContentModel getContentModel() {
        if(getComponentType() == ComponentType.PROXY) {
            return getOriginal().getContentModel();
        }
                
        if(this instanceof ContentModel)
            return (ContentModel)this;
                    
        AXIComponent parent = getParent();
        if(parent == null ||
           parent instanceof AXIDocument ||
           parent instanceof Element)
            return null;
        
        return parent.getContentModel();
    }    
            
    /**
     * Returns the namespace, this component belongs to.
     */
    public String getTargetNamespace() {
        if(getComponentType() == ComponentType.PROXY) {
            return getOriginal().getTargetNamespace();
        }
            
        SchemaComponent peer = getPeer();
        return peer.getModel().getEffectiveNamespace(peer);
    }
        
    /**
     * Returns the strongly typed model,
     * else the caller will have to cast.
     */
    public AXIModel getModel() {        
        return (AXIModel)super.getModel();
    }
    
    /**
     * Returns the parent element for this component.
     */
    public Element getParentElement() {
        return Util.findParentElement(this);
    }
    
    /**
     * Returns all the child elements for this component.
     */
    public List<AbstractElement> getChildElements() {
        if(getSharedComponent() != null) {
            return getSharedComponent().getChildElements();
        }            
        
        List<AbstractElement> childrenElements = new ArrayList<AbstractElement>();
        populateChildElements(childrenElements, this);
        return Collections.unmodifiableList(childrenElements);
    }
    
    private void populateChildElements(List<AbstractElement> childrenElements,
            AXIComponent component) {
        for(AXIComponent child : component.getChildren()) {
            if( child instanceof ContentModel )
                continue;
            
            if( child instanceof AbstractElement ) {
                childrenElements.add((AbstractElement)child);
            } else {
                populateChildElements(childrenElements, child);
            }
        }
    }
    
    /**
     * Returns the corresponding SchemaComponent.
     * 0th should always be the absolute peer.
     */
    public final SchemaComponent getPeer() {
        if(getComponentType() == ComponentType.REFERENCE) {
            return peer;
        }
        
        if(getSharedComponent() != null) {
            return getSharedComponent().getPeer();
        }
        return peer;
    }
    
    /**
     * Sets the peer and resets the schema component listener.
     */
    public final void setPeer(SchemaComponent peer) {
        if(getComponentType() == ComponentType.REFERENCE) {
            this.peer = peer;
            return;
        }
        
        if(getSharedComponent() != null) {
            getSharedComponent().setPeer(peer);
            return;
        }
        
        this.peer = peer;
    }
        
    ///////////////////////////////////////////////////////////////////
    //////////Implements AbstractComponent's abstract methods//////////
    ///////////////////////////////////////////////////////////////////
    protected void appendChildQuietly(AXIComponent newComponent, List<AXIComponent> children) {
        if(getComponentType() == ComponentType.PROXY) {
            getOriginal().appendChildQuietly(newComponent, children);
            return;
        }
        
        children.add(newComponent);
    }
    
    protected void insertAtIndexQuietly(AXIComponent newComponent, List<AXIComponent> children, int index) {        
        if(getComponentType() == ComponentType.PROXY) {
            getOriginal().insertAtIndexQuietly(newComponent, children, index);
            return;
        }
        children.add(index, newComponent);
    }
    
    protected void removeChildQuietly(AXIComponent component, List<AXIComponent> children) {
        if(getComponentType() == ComponentType.PROXY) {
            getOriginal().removeChildQuietly(component, children);
            return;
        }
        children.remove(component);
    }
                
    /**
     * Visits each child schema component and creates
     * corresponding axi component, adds them to the parent
     * axi component.
     */
    public void populateChildren(List<AXIComponent> children) {        
        if(getSharedComponent() != null) {
            Util.addProxyChildren(this, getSharedComponent(), children);
            return;
        }
        
        //Safeguard: this can be removed if DesignPatternTest goes through
        if(getPeer() == null)
            return;
        
        AXIModelBuilder builder = new AXIModelBuilder(this);
        builder.populateChildren(getPeer(), true, children);
    }
        
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
    public AXIComponent copy(AXIComponent parent) {
        AXIComponentFactory f = parent.getModel().getComponentFactory();
        return f.copy(this);
    }
        
    /**
     * Sets the compositor.
     */
    protected void setCompositor(Compositor oldCompositor,
            CompositorType newType) {
        //find the existing compositor's position
        int index = oldCompositor.getIndex();
        if(index == -1)
            return;
        
        //must deep visit so that we can deep copy.
        DeepAXITreeVisitor visitor = new DeepAXITreeVisitor();
        oldCompositor.accept(visitor);
        
        Compositor newCompositor = (Compositor)oldCompositor.copy(this);
        newCompositor.setCompositorType(newType);
        
        //remove the existing compositor, insert the new one
        removeChild(Compositor.PROP_COMPOSITOR, oldCompositor);
        
        insertAtIndex(Compositor.PROP_COMPOSITOR, newCompositor, index);
        copyTree(oldCompositor, newCompositor);
    }
    
    private void copyTree(AXIComponent original, AXIComponent copy) {
        List<AXIComponent> children = new ArrayList<AXIComponent>();
        for(AXIComponent child: original.getChildren()) {
            AXIComponent childCopy = child.copy(copy);
            copy.appendChild(childCopy);
            copyTree(child, childCopy);
        }
    }
    
    /**
     * Checks if a component is part of an AXI model.
     * Returns true if it has a valid parent and model, false otherwise.
     */
    protected boolean isInModel() {        
        //for AXIDocument, check if the model is valid
        if(this instanceof AXIDocument)
            return getModel() != null;
        
	//for everything else, both parent and model should be valid
        return ( (getParent() != null) && (getModel() != null) );
    }
    
    /**
     * Must be called from all set methods.
     */
    protected void firePropertyChangeEvent(String property, Object oldVal, Object newVal) {
	if (!isInModel())
            return;
        
        fireValueChanged();
        if(property != null)
            firePropertyChange(property, oldVal, newVal);        
        if(pcs != null)
            pcs.firePropertyChange(property, oldVal, newVal);
    }
    
    /**
     * Overwritten so that it can fire events to the proxies.
     */
    protected void appendChild(String property, AXIComponent child) {
        if(getModel() != child.getModel())
            return;
        
        super.appendChild(property, child);
        if(pcs != null)
            pcs.firePropertyChange(PROP_CHILD_ADDED, null, child);        
        
        if(this instanceof AXIDocumentImpl)
            ((AXIDocumentImpl)this).addToCache(child);
    }
    
    /**
     * Overwritten so that it can fire events to the proxies.
     */
    public void insertAtIndex(String property, AXIComponent child, int index) {
        if(getModel() != child.getModel())
            return;
        
        super.insertAtIndex(property, child, index);
        if(pcs != null)
            pcs.firePropertyChange(PROP_CHILD_ADDED, null, child);
        
        if(this instanceof AXIDocumentImpl)
            ((AXIDocumentImpl)this).addToCache(child);
    }
    
    /**
     * Overwritten so that it can fire events to the proxies.
     */
    public void removeChild(String property, AXIComponent child) {
        if(getModel() != child.getModel())
            return;
        
        super.removeChild(property, child);
        if(pcs != null) {
            //fire event so that proxy children get deleted from their parents
            pcs.firePropertyChange(PROP_CHILD_REMOVED, child, null);            
            //finally, remove all listeners from the shared child
            child.removeAllListeners();
        }
        if(this instanceof AXIDocumentImpl)
            ((AXIDocumentImpl)this).removeFromCache(child);
    }
    
    /**
     * Convenient method to append a child. If the parent is a proxy,
     * delegates to the shared component.
     */
    public final void appendChild(AXIComponent child) {
        if(getComponentType() == ComponentType.PROXY && !getModel().inSync()) {
            getOriginal().appendChild(child);
            return;
        }
        
        appendChild(Util.getProperty(child), child);
    }
    
    /**
     * Convenient method to insert a child at a specified index.
     * If the parent is a proxy, delegates to the shared component.
     */
    public final void addChildAtIndex(AXIComponent child, int index) {
        if(getComponentType() == ComponentType.PROXY && !getModel().inSync()) {
            getOriginal().addChildAtIndex(child, index);
            return;
        }
        
        insertAtIndex(Util.getProperty(child), child, index);
    }
        
    /**
     * Convenient method to remove a child.
     * If the parent is a proxy, delegates to the shared component.
     */
    public final void removeChild(AXIComponent child) {
        if(child.getComponentType() == ComponentType.REFERENCE) {
            removeChild(Util.getProperty(child), child);
            return;            
        }
        
        //proxy child delete from UI: delete original child
        if(child.getComponentType() == ComponentType.PROXY &&
           !getModel().inSync()) {
            AXIComponent oChild = child.getOriginal();
            oChild.getParent().removeChild(oChild);
            return;
        }
        
        removeChild(Util.getProperty(child), child);
    }
    
    /**
     * Removes all children one by one. This is a special case where
     * removal is not delegated to the shared parent.
     */
    public void removeAllChildren() {
        List<AXIComponent> removedChildren = new ArrayList<AXIComponent>();
        for(AXIComponent child : getChildren()) {
                removedChildren.add(child);
        }
        for(AXIComponent child : removedChildren) {
            removeChild(Util.getProperty(child), child);
        }        
    }
    
    /////////////////////////////////////////////////////////////////////////////
    ////////// Following methods are applicable for proxies only ////////////////
    /////////////////////////////////////////////////////////////////////////////
    /**
     * The proxy component receives an event notification.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        AXIComponent source = (AXIComponent)evt.getSource();
        String property = evt.getPropertyName();
        if(!isInModel()) {
            //Ideally it shouldn't come here. Remove this as listener
            //and make shared as null, so that it'll be GCed.
            source.removeListener(this);
            //setSharedComponent(null);
            return;
        }
        //if(evt.getOldValue() == null && evt.getNewValue() != null) {
        if(PROP_CHILD_ADDED.equals(property)) {            
            onChildAdded(evt);
            return;
        }
        //if(evt.getOldValue() != null && evt.getNewValue() == null) {
        if(PROP_CHILD_REMOVED.equals(property)) {
            onChildDeleted(evt);
            return;
        }
                
        firePropertyChangeEvent(evt.getPropertyName(),
                evt.getOldValue(), evt.getNewValue());
    }

    private void onChildAdded(PropertyChangeEvent evt) {
        if(!isChildrenInitialized())
            return;
        AXIComponent parent = (AXIComponent)evt.getSource();
        AXIComponent child = (AXIComponent)evt.getNewValue();
        int index = -1;
        for(int i=0; i<parent.getChildren().size(); i++) {
            if(parent.getChildren().get(i) == child) {
                index = i;
                break;
            }
        }
        if(index == -1)
            return;
        
        AXIComponentFactory factory = getModel().getComponentFactory();
        AXIComponent proxy = factory.createProxy(child);
        insertAtIndex(Util.getProperty(child), proxy, index);
    }
    
    private void onChildDeleted(PropertyChangeEvent evt) {
        AXIComponent parent = (AXIComponent)evt.getSource();
        AXIComponent child = (AXIComponent)evt.getOldValue();
        if(child instanceof ContentModel) {
            onContentModelDeleted((ContentModel)child);
            return;
        }
        AXIComponent deletedChild = null;
        for(AXIComponent c : getChildren()) {
            if(c.getSharedComponent() == child) {
                deletedChild = c;
                break;
            }
        }
        if(deletedChild == null)
            return;
        
        removeChild(Util.getProperty(deletedChild), deletedChild);
    }
    
    /**
     * Removing a content model is special. For example, if element shipTo and billTo
     * are of type USAddress, and USAddress gets deleted, then delete the proxy children
     * of shipTo and billTo and finally delete USAddress.
     */
    private void onContentModelDeleted(ContentModel contentModel) {
        List<AXIComponent> removeList = new ArrayList<AXIComponent>();
        for(AXIComponent child : getChildren()) {
            if(child.getContentModel() == contentModel) {
                removeList.add(child);
            }
        }
        for(AXIComponent child: removeList) {
            child.getParent().removeChild(Util.getProperty(child), child);
        }
    }
    
    /////////////////////////////////////////////////////////////////////
    ////////////////////////// member variables /////////////////////////
    /////////////////////////////////////////////////////////////////////
    /**
     * Peer schema component.
     */
    private SchemaComponent peer;

    /**
     * Reference to the shared object, if this is a proxy.
     */
    protected AXIComponent sharedComponent;    
    private PropertyChangeSupport pcs;
    private WeakHashMap<AXIComponent, PropertyChangeListener> listenerMap;
    
    private static final String PROP_CHILD_ADDED     = "child_added";
    private static final String PROP_CHILD_REMOVED   = "child_removed";
}
