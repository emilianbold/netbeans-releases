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

package org.netbeans.modules.xml.xam.dom;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xam.EmbeddableRoot;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.ModelSource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 *
 * @author rico
 * @author Vidhya Narayanan
 * @author Chris Webster
 * @author Nam Nguyen
 */
public abstract class AbstractDocumentComponent<C extends DocumentComponent<C>> 
        extends AbstractComponent<C> implements DocumentComponent<C>, DocumentModelAccess.NodeUpdater {
    private Element node;
    
    protected abstract void populateChildren(List<C> children);
    
    public AbstractDocumentComponent(AbstractDocumentModel model, org.w3c.dom.Element e) {
        super(model);
        setRef(e);
    }
    
    /**
     * Stores the reference to the DOM node
     */
    private void setRef(Element n) {
        assert n != null : "n must not be null";
        node = n;
    }
    
    public synchronized Element getPeer() {
        return node;
    }
    
    /**
     * @return attribute value or null if the attribute is currently undefined
     */
    public String getAttribute(Attribute attr) {
        return normalizeUndefinedAttributeValue(
	    getPeer().getAttribute(attr.getName()));
    }
    
    /**
     * Sets the component attribute String value and fire property change event
     * with the given property name.
     *
     * @param eventPropertyName property name to be used in firing property change event.
     * @param attr attribute name
     * @value attribute value
     */
    public void setAttribute(String eventPropertyName, Attribute attr, Object value) {
        verifyWrite();
        Object old = null;
        String s = getAttribute(attr);
        if (s != null) {
            try {
                old = getAttributeValueOf(attr, s);
            } catch(IllegalArgumentException ex) {
                //ignored, equivalent to having no old value
            }
        }
        setAttributeQuietly(attr, value);
        firePropertyChange(eventPropertyName, old, value);
        fireValueChanged();
    }
    
    abstract protected Object getAttributeValueOf(Attribute attr, String stringValue);
    
    /**
     * Returns string value of the attribute from different namespace.
     * If given QName has prefix, it will be ignored.
     * @param attr non-null QName represents the attribute name.
     * @return attribute value
     */
    public String getAnyAttribute(QName attr) {
        assert attr != null;
        String name = attr.getLocalPart();
        String namespace = attr.getNamespaceURI();
        String prefix = namespace == null ? null : lookupPrefix(namespace);
        String attrName = prefix == null ? name : prefix + ":" + name; //NOI18N
        return normalizeUndefinedAttributeValue(
	    getPeer().getAttribute(attrName));
    }
    
    /**
     * use the normalized value method from ModelAccess if available, otherwise
     * just return the attribute value. The normalized access will not be
     * available if the component has been deleted from the model. 
     */
    private String normalizeUndefinedAttributeValue(String value) {
	String normalizedValue = value;
	if (getModel() != null) {
	    normalizedValue =
		getAccess().normalizeUndefinedAttributeValue(value);
	}
	return normalizedValue;
    }
    
    /**
     * Set string value of the attribute identified by given QName.
     * This will fire property change event using attribute local name.
     * @param attr non-null QName represents the attribute name.
     * @param value string value for the attribute.
     */
    public void setAnyAttribute(QName attr, String value) {
        setQNameAttribute(attr.getLocalPart(), attr, value);
    } 
    
    protected void setQNameAttribute(String propertyName, QName attr, String value) {
        assert attr != null;

        verifyWrite();
        
        String name = getPrefixedName(attr, (value != null));
        String old = getAnyAttribute(attr);
        if (value == null) {
            removeAttribute(getPeer(), name);
        } else {
            setAttribute(getPeer(), name, value);
        }
        
        firePropertyChange(propertyName, old, value);
        fireValueChanged();
    }
    
    protected String getPrefixedName(QName q, boolean declarePrefix) {
        String name = q.getLocalPart();
        String namespace = q.getNamespaceURI();
        String prefix = q.getPrefix();
        return getPrefixedName(namespace, name, prefix, declarePrefix);
    }
    

    protected String getPrefixedName(String namespace, String localName) {
        return getPrefixedName(namespace, localName, null, false);
    }
    
    protected String getPrefixedName(String namespace, String name, String prefix, boolean declarePrefix) {
        if (namespace == null || namespace.length() == 0) {
            declarePrefix = false;
        }
        String existingPrefix = lookupPrefix(namespace);
        AbstractDocumentComponent root = (AbstractDocumentComponent)getModel().getRootComponent();
        if (existingPrefix == null) {
            //might have not been added to xmd tree, so lookup at tree root
            existingPrefix = root.lookupPrefix(namespace);
        }
        //recheck to see if this prefix is overridden
        if (existingPrefix != null) {
            String localNS = lookupNamespaceURI(existingPrefix);
            if (localNS != null && ! localNS.equals(namespace)) {
                existingPrefix = null;
            }
        }
            
        if (existingPrefix != null) {
            prefix = existingPrefix;
        } else if (declarePrefix) {
            if (prefix == null) {
                prefix = "ns"; //NOI18N
            }
            if (prefix.length() > 0) {
                prefix = ensureUnique(prefix, namespace);
            }
            root.addPrefix(prefix, namespace);
        }
        
        if (prefix != null && prefix.length() > 0) {
            name = prefix + ":" + name; //NOI18N
        }
        
        return name;
    }
    
    /**
     * Returns a unique prefix for the given namespace by appending number from 1 to 100.
     */
    protected String ensureUnique(String prefix, String namespace) {
        assert namespace != null;
        int count = 0;
        String prefixN = prefix;
        String existing = lookupNamespaceURI(prefixN);
        while (existing != null && count < 100 && ! existing.equals(namespace)) {
            ++count;
            prefixN = prefix+count;
            existing = lookupNamespaceURI(prefix+count);
        }
        if (count >= 100) {
            Logger.getLogger(getClass().getName()).log(
                Level.FINE, "Failed to generate unique prefix for "+namespace); //NOI18N
        }
        return prefixN;
    }
    
    protected void setAttributeQuietly(Attribute type, Object newVal) {
        if (newVal == null) {
            removeAttribute(node, type.getName());
        } else {
            String stringValue = newVal.toString();
            if (newVal instanceof NamedComponentReference) {
                NamedComponentReference ref = (NamedComponentReference) newVal;
                QName q = ref.getQName();
                stringValue = getPrefixedName(q.getNamespaceURI(), q.getLocalPart(), null, true);
                if (getEffectiveParent() == null) {
                    Logger.getLogger(getClass().getName()).log(Level.FINE,
                            "Referencing while not in tree yet could result in unwanted prefix declaration"); //NOI18N
                }
                ((AbstractNamedComponentReference) ref).refresh();
            }
            setAttribute(node, type.getName(), stringValue);
        }
    }
    
    protected void removeAttributeQuietly(Element element, String name) {
        getAccess().removeAttribute(element, name, this);
    }
    
    protected void appendChildQuietly(C component, List<C> children) {
        fixupPrefix(component);
        getAccess().appendChild(getPeer(), component.getPeer(), this);
        children.add(component);
    }
    
    protected void insertAtIndexQuietly(C newComponent, List<C> children, int index) {
        if (index >= 0 && children.size() > 0 && index < children.size()) {
            fixupPrefix(newComponent);
            Node refChild = children.get(index).getPeer();
            insertBefore(newComponent.getPeer(), refChild);
            children.add(index, newComponent);
        } else {
            appendChildQuietly(newComponent, children);
        }
    }
    
    protected void removeChildQuietly(C component, List<C> children) {
        removeChild(component.getPeer());
        children.remove(component);
    }
    
    protected String getNamespaceURI() {
        String ns = getPeer().getNamespaceURI();
        if (ns == null && getEffectiveParent() != null) {
            String prefix = getPeer().getPrefix();
            ns = lookupNamespaceURI(prefix);
        } 
        return ns;
    }

    /**
     * Returns namespace for the given prefix. If optimized is specified, 
     * lookup will use component tree hierarchy instead of the underlying DOM tree hiearchy.
     */
    public String lookupNamespaceURI(String prefix, boolean optimized) {
        if (optimized) {
            String namespace = getPrefixes().get(prefix == null ? "" : prefix);
            if (namespace == null && getEffectiveParent() != null) {
                namespace = getEffectiveParent().lookupNamespaceURI(prefix, true);
            }
            return namespace;
        } else {
            return lookupNamespaceURI(prefix);
        }
    }
    
    public String lookupNamespaceURI(String prefix) {
        String ns = getPeer().lookupNamespaceURI(prefix);
        if (ns == null && getEffectiveParent() != null) {
            ns = getEffectiveParent().lookupNamespaceURI(prefix);
        } 
        return ns;
    }
    
    public String lookupPrefix(String namespace){
        String prefix = getPeer().lookupPrefix(namespace);
        if (prefix == null && getEffectiveParent() != null) {
            prefix = getEffectiveParent().lookupPrefix(namespace);
        } 
        return prefix;
    }
    
    /**
     * Get the XML fragment text that make up the children the component peer node.  
     */
    protected String getXmlFragment() {
        return getAccess().getXmlFragment(getPeer());
    }
    
    /**
     * Set text as XML fragment children the component peer node.  
     * The children of peer node will be replaced with nodes resulted from
     * parsing given text.  This method will not fire child component added
     * or removed events.  This method should only be exposed on leaf component.
     *
     * @param propertyName name of property event to fire
     * @param text text value to set to.
     * @exception IOException if text is not well-formed.
     */
    protected synchronized void setXmlFragment(String propertyName, String text) throws IOException {
        verifyWrite();
        String oldVal = getText();
        getAccess().setXmlFragment(getPeer(), text, this);
        firePropertyChange(propertyName, oldVal, text);
        fireValueChanged();
    }
    
    /**
     * Set text value of the component.  This is for pure text-usage by documentation
     * components.  The children of peer node will be replaced with single
     * text node having given text.
     * @param propertyName name of property event to fire
     * @param text text value to set to.
     */
    protected synchronized void setText(String propertyName, String text) {
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
        return getText(getPeer());
    }

    public static String getText(Element e) {
        StringBuilder text = new StringBuilder();
        org.w3c.dom.NodeList nl = e.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            org.w3c.dom.Node n = nl.item(i);
            if (n instanceof org.w3c.dom.Text) {
                text.append(n.getNodeValue());
            }
        }
        return text.toString();
    }
    
    public AbstractDocumentModel getModel() {
        return (AbstractDocumentModel) super.getModel();
    }
    
    public boolean referencesSameNode(Node n) {
        return getModel().areSameNodes(getPeer(), n);
    }
    
    public synchronized void updateReference(Element element) {
        node = element;
    }

    /**
     * Update all parents with fresh nodes.
     * This implemenation assume optimal case where the length of pathToRootNode
     * is same as or longer than path to root component.  The update will skip 
     * uncorrelated nodes in pathToRootNode.  Individual will need to override
     * this implementation if it has special needs.
     */
    public synchronized <N extends Node> void updateReference(List<N> pathToRoot) {
        AbstractDocumentComponent current = this;
        assert pathToRoot != null && pathToRoot.size() > 0;
        for (int i=0; i<pathToRoot.size(); i++) {
            assert pathToRoot.get(i) instanceof Element;
            Element e = (Element) pathToRoot.get(i);
            if (current.referencesSameNode(e)) {
                current.updateReference(e, pathToRoot);
                if (current.getEffectiveParent() != null) {
                    current = (AbstractDocumentComponent) current.getEffectiveParent();
                } else {
                    break;
                }
            } else if (i == pathToRoot.size()-1) {
                throw new IllegalArgumentException("Expect new reference node has same Id as current"); //NOI18N
            }
        }
    }
    
    /**
     * Updates peer node with given peer and the path for context of the update.
     * The default behavior just call #updateReference(Element peer).
     * Subclass with special need for auxiliary update needs to override.
     *
     * @param peer the peer node to update with
     * @param updatingPath full path for context of the update
     */
    protected <N extends Node> void updateReference(Element peer, List<N> updatingPath) {
        updateReference(peer);
    }
    
    private DocumentModelAccess getAccess() {
        getChildren(); //make sure children populated before potential mutation
        return (DocumentModelAccess) getModel().getAccess();
    }
    
    public int findPosition() {
        if (getModel() == null) {
            return 0;
        }
        return getAccess().findPosition(getPeer());
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
    
    private void removeChild(Node child) {
        getAccess().removeChild(node, child, this);
    }
    
    /**
     * Shared utility for implementation to replace the current peer and
     * ensure the document tree also get update properly.
     */
    protected void updatePeer(String propertyName, org.w3c.dom.Element newPeer) {
        AbstractDocumentComponent aParent = getEffectiveParent();
        Element parentPeer = aParent.getPeer();
        Element oldPeer = getPeer();
        getAccess().replaceChild(parentPeer, getPeer(), newPeer, aParent);
        updateReference(newPeer);
        firePropertyChange(propertyName, oldPeer, newPeer);
        fireValueChanged();
    }
    
    protected Attribute createPrefixAttribute(String prefix) {
        assert prefix != null;
        if (prefix.length() == 0) {
            return new PrefixAttribute(XMLConstants.XMLNS_ATTRIBUTE);
        } else {
            return new PrefixAttribute("xmlns:"+prefix); //NOI18N
        }
    }
    
    /**
     * Declare prefix for given namespace (without any refactoring action).
     */
    public void addPrefix(String prefix, String namespace) {
        assert prefix != null;
        String event = (prefix.length() == 0) ? XMLConstants.XMLNS_ATTRIBUTE : "xmlns:"+prefix; //NOI18N
        setAttribute(event, createPrefixAttribute(prefix), namespace);
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
	    final String xmlns = XMLConstants.XMLNS_ATTRIBUTE; //NOI18N
	    if (xmlns.equals(name) || // default namespace
		xmlns.equals(prefix)) { // namespace prefix
		String ns = n.getNodeValue();
		prefixes.put(name, ns);
	    }
        }
        String defaultNamespace = prefixes.remove(XMLConstants.XMLNS_ATTRIBUTE);
        if (defaultNamespace != null) {
            prefixes.put(XMLConstants.DEFAULT_NS_PREFIX, defaultNamespace);
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
    
    /**
     * Ensure (recursively) children prefix is same as parent if same namespace.
     * To be used when adding newcomponent subtree to this component.
     */
    private void fixupPrefix(C newComponent) {
        if (getModel().inSync()) return;
        
        AbstractDocumentComponent child = (AbstractDocumentComponent) newComponent;
        Element e = child.getPeer();
        String childNS = child.getNamespaceURI();
       
        if (childNS != null && childNS.equals(getNamespaceURI())) {
            e.setPrefix(getPeer().getPrefix());
        } else if (childNS != null && childNS.equals(lookupNamespaceURI(""))) {
            e.setPrefix(null);
        } else {
            ensurePrefixDeclaredFor(e, childNS);
        }

        for (C c : newComponent.getChildren()) {
            fixupPrefix(c);
        }
    }

    private void ensurePrefixDeclaredFor(Element newComponentElement, String newComponentNS) {
        String existingPrefix = lookupPrefix(newComponentNS);
        String prefix = newComponentElement.getPrefix();
        if (existingPrefix == null) {
            if (prefix == null) {
                prefix = "ns"; //NOI18N
            }
            ensureUnique(prefix, newComponentNS);
            ((AbstractDocumentComponent)getModel().getRootComponent()).addPrefix(prefix, newComponentNS);
        } else {
            newComponentElement.setPrefix(existingPrefix);
        }
    }
    
    protected void ensureValueNamespaceDeclared(String newNamespace, String oldNamespace, 
            String preferredPrefix) {
        if (newNamespace == null) return;
        String prefix = null;
        if (oldNamespace != null) {
            prefix = lookupPrefix(oldNamespace);
        }
        
        if (prefix == null) {
            // see if i can use 'tns'
            String tnsURI = lookupNamespaceURI(preferredPrefix);
            if (tnsURI == null) {
                prefix = preferredPrefix;
            } else {
                prefix = ensureUnique(preferredPrefix, newNamespace);
            }
            addPrefix(prefix, newNamespace);
        } else { // redefined existing prefix
            removePrefix(prefix);
            addPrefix(prefix, newNamespace);
        }
    }
    
    public C findChildComponent(Element e) {
        for (C c : getChildren()) {
            if (c.referencesSameNode(e)) {
                return c;
            }
        }
        return null;
    }

    public C findChildComponentByIdentity(Element e) {
        ElementIdentity ei = getModel().getAccess().getElementIdentity();
        Document doc = getModel().getDocument();
        for (C c : getChildren()) {
            if (ei.compareElement(c.getPeer(), e, doc, doc)) {
                return c;
            }
        }
        return null;
    }
    
    public DocumentComponent copy(C parent) {
        if (getModel() == null) {
            throw new IllegalStateException("Cannot copy component already removed from model");
        }
         Element newPeer = getAccess().duplicate(getPeer());
        DocumentModel<C> m = parent == null ? getModel() : (DocumentModel) parent.getModel();
        return m.createComponent(parent, newPeer);
    }
    
    protected void verifyWrite() {
        if (isInDocumentModel()) {
            getModel().validateWrite();
        }
    }
    
    protected void firePropertyChange(String propName, Object oldValue, Object newValue) {
        if (isInDocumentModel()) {
            super.firePropertyChange(propName, oldValue, newValue);
        }
    }
    
    protected void fireValueChanged() {
        if (isInDocumentModel()) {
            super.fireValueChanged();
        }
    }
    
    protected void fireChildRemoved() {
        if (isInDocumentModel()) {
            super.fireChildRemoved();
        }
    }
    
    protected void fireChildAdded() {
        if (isInDocumentModel()) {
            super.fireChildAdded();
        }
    }
    
    /**
     * Returns true if the component is part of the document model.
     */
    public boolean isInDocumentModel() {
        if (getModel() == null) return false;
        AbstractDocumentComponent root = (AbstractDocumentComponent) getModel().getRootComponent();
        if (root == null) return false;
        if (root == this) return true;
        AbstractDocumentComponent myRoot = (AbstractDocumentComponent) getEffectiveParent();
        if (myRoot == null) return false; // no parent
        while (myRoot != null && myRoot.getEffectiveParent() != null) {
            if (myRoot instanceof EmbeddableRoot) {
                root = (AbstractDocumentComponent) myRoot.getEffectiveParent().getModel().getRootComponent();
            }
            myRoot = (AbstractDocumentComponent) myRoot.getEffectiveParent();
        }
        return root == myRoot;
    }
    
    public int findAttributePosition(String attributeName) {
        org.w3c.dom.Attr a = getPeer().getAttributeNode(attributeName);
        if (a != null) {
            return getAccess().findPosition(a);
        } else {
            return -1;
        }
    }
    
    /**
     * Returns QName of the component.
     */
    public QName getQName() {
        return getQName(getPeer());
    }
    
    public static QName getQName(Node n) {
        String namespace = n.getNamespaceURI();
        String localName = n.getLocalName();
        String prefix = n.getPrefix();
        assert(localName != null);
        if (namespace == null && prefix == null) {
            return new QName(localName);
        } else if (namespace != null && prefix == null) {
            return new QName(namespace, localName);
        } else {
            return new QName(namespace, localName, prefix);
        }
    }
    
    private ModelSource resolveModelSource(String location, 
	ModelSource currentSource, CatalogModel currentCatalog) {
	ModelSource ms = null;
	try {
	    if (location != null) {
		ms = currentCatalog.getModelSource(getURI(location), 
		    currentSource);
	    } 
	} catch (CatalogModelException nse) {
	    // unable to resolve location
	    Logger l = Logger.getLogger(AbstractDocumentComponent.class.getName());
	    l.log(Level.FINE, nse.getMessage());
	}
	return ms;
    }
    
    /**
     * Resolves reference to external models using location hint. 
     * @param hint on location of where external model reference could reside.
     * @return the model source of the referenced model if found.
     * @throws CatalogModelException if the model cannot be located or the 
     * hint is not well-formed URI
     */
    protected ModelSource resolveModel(String hint) throws CatalogModelException {
        return _resolveModel(hint, null);
    }
    
    private ModelSource _resolveModel(String hint, String backup) throws CatalogModelException {
	CatalogModel nr = (CatalogModel) 
	    getModel().getModelSource().getLookup().lookup(CatalogModel.class);

	// try hint
	ModelSource ms = resolveModelSource(hint, getModel().getModelSource(), 
	    nr);
        
	// hint didn't work now try backup
	if (ms == null) {
	    ms = resolveModelSource(backup, getModel().getModelSource(),
		nr);
	}
	
	// unable to resolve
	if (ms == null) {
            String msg = "Cannot resolve file using hint = " + hint + //NOI18N
			 " backup = " + backup; //NOI18N
            throw new CatalogModelException(msg);
        }
	
	return ms;
    }
    
    private static URI getURI(String s) throws CatalogModelException {
	try {
	    return new URI(s);
	} catch (URISyntaxException ex) {
	    throw new CatalogModelException(ex);
	}
    }
    
    public Map<QName,String> getAttributeMap() {
        return getModel().getAccess().getAttributeMap(getPeer());
    }
    
    /**
     * @returns the index of given element relative to other domain elements in 
     * the peer node children; return -1 if not domain element or not found.
     */
    protected int findDomainIndex(Element e) {
        if (! getModel().isDomainElement(e)) {
            return -1;
        }
        int domainInsertIndex = 0;
        NodeList nl = getPeer().getChildNodes();
        for (int i=0; i<nl.getLength(); i++) {
            if (nl.item(i) == e) {
                return domainInsertIndex;
            }
            if (getModel().isDomainElement(nl.item(i))) {
                domainInsertIndex++;
            }
        }
        return -1;
    }
    
    protected AbstractDocumentComponent getEffectiveParent() {
        AbstractDocumentComponent p = (AbstractDocumentComponent) getParent();
        if (p == null && this instanceof EmbeddableRoot) {
            p = (AbstractDocumentComponent) ((EmbeddableRoot)this).getForeignParent();
        }
        return p;
    }


    protected Element getChildElement(QName qname) {
        NodeList nl = getPeer().getElementsByTagName(qname.getLocalPart());
        Element ret = null;
        if (nl != null) {
            for (int i=0; i<nl.getLength(); i++) {
                if (qname.equals(getQName(nl.item(i)))) {
                    ret = (Element) nl.item(i);
                    break;
                }
            }
        }
        return ret;
    }
    
    protected String getChildElementText(QName qname) {
        Element ret = getChildElement(qname);
        return ret == null ? null : getText(ret);
    }
    
    protected void setChildElementText(String propertyName, String text, QName qname) {
        verifyWrite();
        Element childElement = getChildElement(qname);
        String oldVal = childElement == null ? null : getText(childElement);
        if (childElement == null) {
            childElement = getPeer().getOwnerDocument().createElementNS(qname.getNamespaceURI(), qname.getLocalPart());
            getModel().getAccess().appendChild(getPeer(), childElement, this);
        }
        getModel().getAccess().setText(childElement, text, this);
        firePropertyChange(propertyName, oldVal, text);
        fireValueChanged();
    }

    /**
     * Returns leading text for the child component of the given index.
     * @param index 0-based position of the child
     * @return value of the leading text node, or null the indexed component peer does not have leading text nodes.
     */
    protected String getLeadingText(int index) {
        return getText(index, true);
    }
    
    /**
     * Set leading text for the child component which position is the given index.
     * @param index 0-based position of the child
     * @param text value of the leading text node, or null to remove the leading text node.
     */
    protected void setLeadingText(String propName, String text, int index) {
        setText(propName, text, index, true);
    }
    
    /**
     * Returns trailing text for the child component of the given index.
     * @param index 0-based position of the child
     * @return value of the leading text node, or null the indexed component peer does not have trailing text nodes.
     */
    protected String getTrailingText(int index, boolean leading) {
        return getText(index, false);
    }
    
    /**
     * Set trailing text for the child component which position is the given index.
     * @param index 0-based position of the child
     * @param text value of the trailing text node, or null to remove the trailing text node.
     */
    protected void setTrailingText(String propName, String text, int index) {
        setText(propName, text, index, false);
    }
    
    private String getText(int index, boolean leading) {
        C child = getChildren().get(index);
        int domIndex = getAccess().getElementIndexOf(getPeer(), child.getPeer());
        domIndex = leading ? domIndex-1 : domIndex+1;
        NodeList nl = getPeer().getChildNodes();
        if (domIndex > -1 && domIndex < nl.getLength()) {
            Node n = nl.item(domIndex);
            if (n instanceof Text) {
                return ((Text)n).getNodeValue();
            }
        }
        return null;
    }
    
    private void setText(String propertyName, String text, int index, boolean leading) {
        verifyWrite();

        C child = getChildren().get(index);
        int domIndex = getAccess().getElementIndexOf(getPeer(), child.getPeer());
        domIndex = leading ? domIndex-1 : domIndex+1;
        NodeList nl = getPeer().getChildNodes();
        String oldValue = null;
        Text oldNode = null;
        Node refChild = getPeer();
        if (domIndex > -1 && domIndex < nl.getLength()) {
            Node n = nl.item(domIndex);
            if (n instanceof Text) {
                oldValue = ((Text)n).getNodeValue();
                oldNode = (Text) n;
            } else if (! leading) {
                refChild = nl.item(domIndex);
            }
        }

        Text newNode = text == null ? null : getModel().getDocument().createTextNode(text);
        if (newNode == null && oldValue == null) return;
        if (newNode == null) {
            getModel().getAccess().removeChild(getPeer(), oldNode, this);
        } else if (oldValue == null) {
            getModel().getAccess().insertBefore(getPeer(), newNode, refChild, this);
        } else {
            getModel().getAccess().replaceChild(getPeer(), oldNode, newNode, this);
        }
        
        firePropertyChange(propertyName, oldValue, text);
        fireValueChanged();
    }
}

