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

package org.netbeans.modules.xml.xam;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/**
 *
 * @author rico
 * @author Vidhya Narayanan
 * @author nn136682
 */
public abstract class AbstractComponent<C extends DocumentComponent<C>, M extends DocumentModel> implements DocumentComponent<C>, ModelAccess.NodeUpdater {
    private C parent;
    private List<C> children = null;
    private M model;
    private Element node;
    
    protected abstract void populateChildren(List<C> children);
    
    public AbstractComponent(M model, org.w3c.dom.Element e) {
        if (!(model instanceof AbstractModel)) {
            throw new IllegalArgumentException("Expecting instance of AbstractModel");
        }
        this.model = model;
        setRef(e);
    }
    
    public final void removePropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        model.removePropertyChangeListener(pcl);
    }
    
    public final void addPropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        model.addPropertyChangeListener(new DelegateListener(pcl));
    }
    
    public C getParent() {
        return parent;
    }
    
    void setParent(C component) {
        parent = component;
    }
    
    /**
     * @return the contained elements, this is the  model element
     * representations of the DOM children. The returned list is unmodifiable.
     */
    public List<C> getChildren() {
        _getChildren();
        return Collections.unmodifiableList(children);
    }
    
    private List<C> _getChildren() {
        if (children == null) {
            children = new ArrayList<C>();
            populateChildren(children);
            for (C child : children) {
                ((AbstractComponent)child).setParent(this);
            }
        }
        return children;
    }
    
    /**
     * @return the contained elements, this is the  model
     * element representations of the DOM children.
     *
     * @param type Interested children type to
     *	return.
     */
    public <T extends C>List<T> getChildren(Class<T> type) {
        List<T> result = new ArrayList<T>(_getChildren().size());
        for (C child : _getChildren()) {
            if (type.isAssignableFrom(child.getClass())) {
                result.add(type.cast(child));
            }
        }
        return Collections.unmodifiableList(result);
    }
    
    /**
     * @return the contained  elements, this is the  model
     * element representations of the DOM children.
     *
     * @param type Collection that accepts the interested types and filters
     *	the return list of Children.
     */
    public List<C> getChildren(Collection<Class<? extends C>> typeList) {
        List<C> comps = new ArrayList<C>();
        // createChildren is not necessary because this method delegates
        // to another getChildren which ensures initialization
        for(Class<? extends C> type : typeList) {
            comps.addAll(getChildren(type));
        }
        return Collections.unmodifiableList(comps);
    }
    
    public final M getModel() {
        return model;
    }
    
    private AbstractModel<C> getAbstractModel() {
        return (AbstractModel) model;
    }
    
    private void verifyWrite() {
        getAbstractModel().validateWrite();
    }
    
    private void firePropertyChange(String propName, Object oldValue, Object newValue) {
        PropertyChangeEvent event =
                new PropertyChangeEvent(this,propName,oldValue,newValue);
        getAbstractModel().firePropertyChangeEvent(event);
    }
    
    private void fireValueChanged() {
        getAbstractModel().fireComponentChangedEvent(new ComponentEvent(this,
                ComponentEvent.EventType.ATTRIBUTE));
    }
    
    private void fireChildRemoved() {
        getAbstractModel().fireComponentChangedEvent(new ComponentEvent(this,
                ComponentEvent.EventType.CHILD_REMOVED));
    }
    
    private void fireChildAdded() {
        getAbstractModel().fireComponentChangedEvent(new ComponentEvent(this,
                ComponentEvent.EventType.CHILD_ADDED));
    }
    
    /**
     * Stores the reference to the XDM node
     */
    private void setRef(Element n) {
        assert n != null : "n must not be null";
        node = n;
    }
    
    public Element getPeer() {
        return node;
    }
    
    /**
     * @return attribute value or null if the attribute is currently undefined
     */
    protected String getAttribute(Attribute attr) {
        return getPeer().getAttribute(attr.getName());
    }
    
    /**
     * Sets the component attribute String value and fire property change event
     * with the given property name.
     *
     * @param propertyName property name to be used in firing property change event.
     * @param attr attribute name
     * @value attribute value of type with an implemented toString method.
     */
    //TODO- revisit the access privilege of this
    public void setAttribute(String eventPropertyName, Attribute attr, Object value) {
        verifyWrite();
        Object old = null;
        String s = getAttribute(attr);
        if (s != null) {
            old = getAttributeValueOf(attr, s);
        }
        setAttribute(attr, value == null ? null : value.toString());
        firePropertyChange(eventPropertyName, old, value);
        fireValueChanged();
    }
    
    abstract protected Object getAttributeValueOf(Attribute attr, String stringValue);
    
    protected <T extends C> T getChild(Class<T> type) {
        List<T> result = getChildren(type);
        T value = null;
        if (!result.isEmpty()) {
            value = result.get(0);
        }
        return value;
    }
    
    /**
     * Adds a  element before all other children whose types are in the typeList Collection.
     */
    protected void addBefore(String propertyName, C component,
            Collection<Class<? extends C>> typeList){
        verifyWrite();
        addChild(propertyName, component, typeList, true);
        firePropertyChange(propertyName, null, component);
        fireChildAdded();
    }
    
    /**
     * Adds a  element after all other children whose types are in the typeList Collection.
     */
    protected void addAfter(String propertyName, C component,
            Collection<Class<? extends C>> typeList){
        verifyWrite();
        addChild(propertyName, component, typeList, false);
        firePropertyChange(propertyName, null, component);
        fireChildAdded();
    }
    
    /**
     * Adds the New Element in the XDM model.
     *
     * @param component The  element that needs to be set
     * @param typeList The collection list that contains the class names
     *		of  types of children
     * @param before boolean to indicate to add before/after the typelist
     */
    private void addChild(String propertyName, C component,
            Collection<Class<? extends C>> typeList, boolean before) {
        assert(component != null);
        
        if (typeList == null) {
            throw new IllegalArgumentException("typeList == null"); //NOI18N
        }
        Element newNode = AbstractComponent.class.cast(component).getPeer();
        assert newNode != null;
        
        List<? extends C> childnodes = getChildren();
        if (typeList.isEmpty() || childnodes.isEmpty()) {
            appendChild(component);
        } else {
            int lastIndex = before ? childnodes.size() : -1;
            for (Class<? extends C> type : typeList) {
                for (C child : childnodes) {
                    if (type.isAssignableFrom(child.getClass())) {
                        int i = childnodes.indexOf(child);
                        if (!before) {
                            if (i > lastIndex) lastIndex = i;
                        } else {
                            if (i < lastIndex) lastIndex = i;
                        }
                    }
                }
            }
            if (!before) {
                lastIndex++;
                for (int i=lastIndex ; i<childnodes.size() ; i++) {
                    if (childnodes.get(i).getClass().equals(component.getClass())) {
                        lastIndex++;
                    } else {
                        break;
                    }
                }
            }
            insertAtIndex(component, lastIndex);
        }
    }
    
    protected void appendChild(String propertyName, C child) {
        verifyWrite();
        if (child == null) {
            throw new IllegalArgumentException("child == null"); //NOI18N
        }
        appendChild(child);
        firePropertyChange(propertyName, null, child);
        fireChildAdded();
    }
    
    private void appendChild(C component) {
        fixupPrefix(component);
        //setPrefixRecursively(component);
        appendChild(component.getPeer());
        _addChild(component);
    }
    
    /**
     * Inserts a Component child at the specified index relative to
     * the provided type. This method is expected to be used only in
     * sequence.
     * @param propertyName to fire event on
     * @param component to insert
     * @param index relative to first instance of type, index = firstpos
     * @param type which index should be relative to
     */
    protected void insertAtIndex(String propertyName,
            C component, int index,
            Class<? extends C> type) {
        verifyWrite();
        int trueIndex = 0;
        for (C child: getChildren()) {
            if (type.isAssignableFrom(child.getClass())) {
                break;
            }
            trueIndex++;
        }
        index += trueIndex;
        insertAtIndex(component, index);
        firePropertyChange(propertyName, null, component);
        fireChildAdded();
    }
    
    private void insertAtIndex(C newComponent, int index) {
        if (index >= 0 && _getChildren().size() > 0 && index < _getChildren().size()) {
            fixupPrefix(newComponent);
            Node refChild = AbstractComponent.class.cast(_getChildren().get(index)).getPeer();
            insertBefore(newComponent.getPeer(), refChild);
            _addChild(index, newComponent);
        } else {
            appendChild(newComponent);
        }
    }
    
    protected void removeChild(String propertyName, C element) {
        verifyWrite();
        if (element == null) {
            throw new IllegalArgumentException("element == null"); //NOI18N
        }
        removeChild(element);
        firePropertyChange(propertyName, element, null);
        fireChildRemoved();
    }
    
    private void removeChild(C component) {
        removeChild(AbstractComponent.class.cast(component).getPeer());
        _removeChild(component);
    }
    
    private void _removeChild(C component) {
        _getChildren().remove(component);
        AbstractComponent.class.cast(component).setParent(null);
    }
    
    private void _addChild(C component) {
        _getChildren().add(component);
        AbstractComponent.class.cast(component).setParent(this);
    }
    
    private void _addChild(int index, C component) {
        _getChildren().add(index, component);
        AbstractComponent.class.cast(component).setParent(this);
        
    }
    
    /**
     * When a child element is set using this method:
     * (1) All children that are of the same or derived type as classType are removed.
     * (2) newEl is added as a child after any children that are of the same
     * type as any of the types listed in typeList
     * @param classType Class of the Component that is being added as a child
     * @param propertyName Property name used for firing events
     * @param newEl Component that is being added as a child
     * @param typeList Collection of java.lang.Class-es. newEl will be added as
     * a child after any children whose types belong to any listed in this. An
     * empty collection will append the child
     */
    protected void setChild(Class<? extends C> classType, String propertyName,
            C newEl, Collection<Class<? extends C>> typeList){
        setChildAfter(classType, propertyName, newEl, typeList);
    }
    
    protected void setChildAfter(Class<? extends C> classType, String propertyName,
            C newEl, Collection<Class<? extends C>> typeList){
        setChild(classType, propertyName, newEl, typeList, false);
    }
    protected void setChildBefore(Class<? extends C> classType, String propertyName,
            C newEl, Collection<Class<? extends C>> typeList){
        setChild(classType, propertyName, newEl, typeList, true);
    }
    protected void setChild(Class<? extends C> classType, String propertyName,
            C newEl, Collection<Class<? extends C>> typeList, boolean before){
        //remove all children of type classType
        verifyWrite();
        List<? extends C> childComponents = getChildren(classType);
        C old = childComponents.isEmpty() ? null : childComponents.get(childComponents.size()-1);
        for (C child : childComponents) {
            removeChild(child);
            fireChildRemoved();
        }
        if (newEl == null) {
            return;
        }
        addChild(propertyName, newEl, typeList, before);
        firePropertyChange(propertyName, old, newEl);
        fireChildAdded();
    }
    
    public String lookupNamespaceURI(String prefix){
        return getPeer().lookupNamespaceURI(prefix);
    }
    
    public String lookupPrefix(String namespace){
        return getPeer().lookupPrefix(namespace);
    }
    
    private class DelegateListener implements PropertyChangeListener {
        private final PropertyChangeListener delegate;
        
        public DelegateListener(PropertyChangeListener pcl) {
            delegate = pcl;
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() == AbstractComponent.this) {
                delegate.propertyChange(evt);
            }
        }
        
        public boolean equals(Object obj) {
            return delegate == obj;
        }
        
        public int hashCode() {
            return delegate.hashCode();
        }
    }
    
    /**
     * Set text value of the component.  This is for pure text-usage by documentation
     * components.  The children of peer node will be replaced with single
     * text node having given text.
     * @param propertyName name of property event to fire
     * @param text text value to set to.
     */
    protected void setText(String propertyName, String text) {
        verifyWrite();
        String oldVal = getText();
        getAccess().setText(getPeer(), text, this);
        firePropertyChange(propertyName, oldVal, text);
        fireValueChanged();
    }
    
    /**
     * Return text value of this component.  This is for text-usage by doucmentation
     * components.  Non-text children node are ignored.
     * @return aggregated text string of all child text nodes.
     */
    protected String getText() {
        StringBuilder text = new StringBuilder();
        org.w3c.dom.NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            org.w3c.dom.Node n = nl.item(i);
            if (n instanceof org.w3c.dom.Text) {
                text.append(n.getNodeValue());
            }
        }
        return text.toString();
    }
    
    public boolean referencesSameNode(Node n) {
        return getModel().areSameNodes(getPeer(), n);
    }
    
    public void updateReference(Node n) {
        if (n instanceof Element) {
            node = Element.class.cast(n);
        } else {
            throw new java.lang.IllegalArgumentException("Expect reference value of type Element"); //NOI18N
        }
    }
    
    public int findPosition() {
        return getAbstractModel().getAccess().findPosition(getPeer());
    }
    
    private void setAttribute(Attribute type, String newVal) {
        if (newVal == null) {
            removeAttribute(node, type.getName());
        } else {
            setAttribute(node, type.getName(), newVal);
        }
    }
    
    private void removeAttribute(Element element, String name) {
        getAccess().removeAttribute(element, name, this);
    }
    
    private void setAttribute(Element element, String name, String value) {
        getAccess().setAttribute(element, name, value, this);
    }
    
    private void insertBefore(Node newChild, Node refChild) {
        getAccess().insertBefore(node, newChild, refChild, this);
    }
    
    private void appendChild(Node newChild) {
        getAccess().appendChild(node, newChild, this);
    }
    
    private void removeChild(Node child) {
        getAccess().removeChild(node, child, this);
    }
    
    private ModelAccess getAccess() {
        _getChildren(); // ensure children are populated before mutating backing tree
        return getAbstractModel().getAccess();
    }
    
    /**
     * Shared utility for implementation to replace the current peer and
     * ensure the document tree also get update properly.
     */
    protected void updatePeer(String propertyName, org.w3c.dom.Element newPeer) {
        AbstractComponent aParent = (AbstractComponent)getParent();
        Element parentPeer = aParent.getPeer();
        Element oldPeer = getPeer();
        getAbstractModel().getAccess().replaceChild(parentPeer, getPeer(), newPeer, aParent);
        updateReference(newPeer);
        firePropertyChange(propertyName, oldPeer, newPeer);
        fireValueChanged();
    }
    
    private Attribute createPrefixAttribute(String prefix) {
        return new PrefixAttribute("xmlns:"+prefix);
    }
    
    /**
     * Declare prefix for given namespace (without any refactoring action).
     */
    public void addPrefix(String prefix, String namespace) {
        setAttribute(prefix, createPrefixAttribute(prefix), namespace);
    }
    
    /**
     * Remove declared prefix (without refactoring).
     */
    public void removePrefix(String prefix) {
        setAttribute(prefix, createPrefixAttribute(prefix), null);
    }
    
    /**
     * @return mapping from prefix to namespace.
     */
    public Map<String, String> getPrefixes() {
        Map<String,String> prefixes = new HashMap<String,String>();
        NamedNodeMap nodes = getPeer().getAttributes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            String name = n.getLocalName();
	    String prefix = n.getPrefix();
	    final String xmlns = "xmlns"; //NOI18N
	    if (xmlns.equals(name) || // default namespace
		xmlns.equals(prefix)) { // namespace prefix
		String ns = n.getNodeValue();
		prefixes.put(name, ns);
	    }
        }
        return prefixes;
    }
    
    public static class PrefixAttribute implements Attribute {
        private String prefix;
        public PrefixAttribute(String name) {
            prefix = name;
        }
        public Class getType() { return String.class; }
        public String getName() { return prefix; }
        public Class getMemberType() { return null; }
    }
    
    private void fixupPrefix(Component newComponent) {
        if (model.inSync()) return;
        
        AbstractComponent child = (AbstractComponent) newComponent;
        Element e = child.getPeer();
        String childNS = child.getNamespaceURI();
       
        if (childNS != null && childNS.equals(this.getNamespaceURI())) {
            getAccess().setPrefix(e, getPeer().getPrefix());
        } else if (childNS != null && childNS.equals(getPeer().lookupNamespaceURI(""))) {
            getAccess().setPrefix(e, null);
        } else {
            ensurePrefixDeclaredFor(e, childNS);
        }

        // recursively set children prefix if applicable
        child.setChildPrefixes(e.getPrefix());
    }
    
    private void ensurePrefixDeclaredFor(Element newComponentElement, String newComponentNS) {
        String existingPrefix = getPeer().lookupPrefix(newComponentNS);
        String prefix = newComponentElement.getPrefix();
        if (existingPrefix == null) {
            if (prefix != null) {
                ((AbstractComponent)getModel().getRootComponent()).addPrefix(prefix, newComponentNS);
            }
        } else {
            newComponentElement.setPrefix(existingPrefix);
        }
    }
    
    private void setChildPrefixes(String prefix) {
        List<C> childComponents = getChildren();
        for (C c : childComponents) {
            AbstractComponent ac = (AbstractComponent) c;
            fixupPrefix(ac);
        }
    }
    
    abstract protected String getNamespaceURI();

    public C findChildComponent(Element e) {
        for (C c : getChildren()) {
            if (c.referencesSameNode(e)) {
                return c;
            }
        }
        return null;
    }

    public C copy(C parent){
        Element newPeer = getAbstractModel().getAccess().duplicate(getPeer());
        DocumentModel<C> m = getModel();
        return m.createComponent(parent, newPeer);
    }
}

