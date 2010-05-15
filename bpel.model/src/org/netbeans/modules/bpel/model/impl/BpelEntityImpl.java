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
package org.netbeans.modules.bpel.model.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.WeakHashMap;
import java.util.logging.Logger;
import javax.xml.namespace.QName;

import org.netbeans.modules.bpel.model.api.support.Utils;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.BpelReferenceable;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.xml.xpath.ext.schema.ExNamespaceContext;
import org.netbeans.modules.bpel.model.api.support.SimpleBpelModelVisitor;
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.netbeans.modules.bpel.model.api.references.SchemaReferenceBuilder;
import org.netbeans.modules.bpel.model.ext.ExtBpelAttribute;
import org.netbeans.modules.bpel.model.impl.events.BuildEvent;
import org.netbeans.modules.bpel.model.impl.events.CopyEvent;
import org.netbeans.modules.bpel.model.impl.events.CutEvent;
import org.netbeans.modules.bpel.model.impl.references.BpelReferenceBuilder;
import org.netbeans.modules.bpel.model.impl.references.WSDLReference;
import org.netbeans.modules.bpel.model.impl.references.WSDLReferenceBuilder;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * @author ads
 */
public abstract class BpelEntityImpl extends AbstractDocumentComponent<BpelEntity> implements
        BpelEntity {

    BpelEntityImpl(BpelModelImpl model, Element e) {
        super(model, e);
        myModel = model;
        myAccess = new AttributeAccess(this);
    }

    /**
     * This constructor is designed only for using in <code>builder</code>.
     * Here some inner events will be generated when new element is created via
     * builder.
     */
    BpelEntityImpl(BpelBuilderImpl builder, String tagName) {
        this(builder.getModel(),
                createNewComponent(builder.getModel(), tagName));
        writeLock();
        try {
            BuildEvent<? extends BpelEntity> event = preCreated(this);
            postEvent(event);
        } finally {
            writeUnlock();
        }
    }

    public BpelEntityImpl(BpelBuilderImpl builder, BpelElements elem) {
        this(builder.getModel(),
                createNewComponent(builder.getModel(), elem));
        writeLock();
        try {
            BuildEvent<? extends BpelEntity> event = preCreated(this);
            postEvent(event);
        } finally {
            writeUnlock();
        }
    }

    /**
     * This method should be implemented by any real class.
     * It returns list of possible attributes for entity.
     * It used in firing event about attribute change while synchronizing with source.
     */
    protected abstract Attribute[] getDomainAttributes();

    public BpelModelImpl getBpelModel() {
        return myModel;
    }

    @Override
    public BpelModelImpl getModel() {
        return (BpelModelImpl) super.getModel();
    }

    @Override
    public boolean canPaste(Component child) {
        // only container can contain children elements.
        // this method is redefined in BpelContainer.
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.BpelEntity#copy(java.util.HashMap)
     */
    public BpelEntity copy(HashMap<UniqueId, UniqueId> uniqueMap) {
        writeLock();
        try {
            checkDeleted();
            checkInTree();
            CopyEvent<? extends BpelEntity> event = preCopy(this);

            BpelEntity entity = copy(getParent());
            // we set our unique map as cookie in created entity.
            // after constructing we will remove it.
            entity.setCookie(IdMapKey.class, uniqueMap);
            event.setOutOfModelEntity(entity);

            postEvent(event);

            entity.removeCookie(IdMapKey.class);
            return entity;
        } finally {
            writeUnlock();
        }
    }

    @Override
    public final BpelContainerImpl getParent() {
        return (BpelContainerImpl) super.getParent();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.BpelEntity#cut()
     */
    public BpelEntity cut() {
        writeLock();
        try {
            checkDeleted();
            checkInTree();
            CutEvent<? extends BpelEntity> event = preCut(this);

            BpelEntity entity = copy(getParent());
            if (getParent() != null) {
                assert getParent() instanceof BpelContainer;
                ((BpelContainer) getParent()).remove(this);
            }

            event.setOutOfModelEntity(entity);
            postEvent(event);

            return entity;
        } finally {
            BpelChildEntitiesBuilder childBuilder = getBpelModel().getChildBuilder();
            childBuilder.setEffectiveParent(null);
            writeUnlock();
        }
    }

    @Override
    public BpelEntity copy(BpelEntity parent) {
        writeLock();
        try {
            BpelEntity entity = (BpelEntity) super.copy(parent);
            return entity;
        } finally {
            writeUnlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.BpelEntity#getUID()
     */
    public UniqueId getUID() {
        return myUid;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.BpelEntity#getNamespaceContext()
     */
    public ExNamespaceContext getNamespaceContext() {
        return new ExNamespaceContextImpl(this);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#accept(org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor)
     */
    public final void accept(SimpleBpelModelVisitor visitor) {
        accept((BpelModelVisitor) visitor);
        acceptChildren(visitor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.BpelEntity#getCookie(java.lang.Class)
     */
    public Object getCookie(Object key) {
        return myCookies.get(key);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.BpelEntity#setCookie(java.lang.Object)
     */
    public void setCookie(Object key, Object obj) {

        myCookies.put(key, obj);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel.api.BpelEntity#removeCookie(java.lang.Object)
     */
    public void removeCookie(Object obj) {

        myCookies.remove(obj);

    }

    //############################################################################
    //##
    //##    Methods for creation references to other model and inside BPEL model.  
    //##    Default implementation.
    //##
    //############################################################################
    public <T extends ReferenceableSchemaComponent> SchemaReference<T> 
            createSchemaReference(T target, Class<T> type) {
        readLock();
        try {
            return SchemaReferenceBuilder.getInstance().build(target, type, this);
        } finally {
            readUnlock();
        }
    }

    public <T extends BpelReferenceable> BpelReference<T> createReference(T target,
            Class<T> type) {
        readLock();
        try {
            return BpelReferenceBuilder.getInstance().build(target, type,
                    this);
        } finally {
            readUnlock();
        }
    }

    public <T extends ReferenceableWSDLComponent> WSDLReference<T> createWSDLReference(T target, Class<T> type) {
        readLock();
        try {
            return WSDLReferenceBuilder.getInstance().build(target, type,
                    this);
        } finally {
            readUnlock();
        }
    }
    
    public <T extends ReferenceableWSDLComponent> WSDLReference<T> 
            createWSDLReference(String refString, Class<T> type) 
    {
        readLock();
        try {
            return WSDLReferenceBuilder.getInstance().build(type, this, 
                    refString);
        } finally {
            readUnlock();
        }
    }

    @Override
    public String getAttribute(Attribute attr) {
        readLock();
        try {
            // Extension attribute requires namespace context to 
            // provide a prefix. Therefore the implied owner has to be assigned.
            if (attr instanceof ExtBpelAttribute) {
                ((ExtBpelAttribute)attr).setOwner(this);
            }
            
            /*
             * TODO : there is bug in XAM/XDM.
             * XML entities such as &gt;, &apos;, &quot; is not recognized.
             * The method below perform replacement in string pointed
             * entities to corresponding values.
             * Usage of this method possibly should be removed
             * when bug in XAM/XDM will be fixed.  
             */
            return Utils.hackXmlEntities(super.getAttribute(attr));
        } finally {
            readUnlock();
        }
    }

    @Override
    public void setAttribute(String propName, Attribute attr, Object value) {
        writeLock();
        try {
            super.setAttribute(propName, attr, value);
        } finally {
            writeUnlock();
        }
    }

    //############################################################################
    /**
     * @param visitor
     */
    protected void acceptChildren(SimpleBpelModelVisitor visitor) {
        List<BpelEntity> children = getChildren();
        for (BpelEntity entity : children) {
            entity.accept(visitor);
        }
    }

    protected Integer getIntegerAttribute(Attribute attr) {
        return getAttributeAccess().getIntegerAttribute(attr);
    }

    protected TBoolean getBooleanAttribute(Attribute attr) {
        return getAttributeAccess().getBooleanAttribute(attr);
    }

    protected <T extends BpelReferenceable> List<BpelReference<T>> getBpelReferenceList(Attribute attr, Class<T> type) {
        return getAttributeAccess().getBpelReferenceList(attr, type);
    }

    protected <T extends ReferenceableWSDLComponent> List<org.netbeans.modules.bpel.model.api.references.WSDLReference<T>> getWSDLReferenceList(Attribute attr, Class<T> type) {
        return getAttributeAccess().getWSDLReferenceList(attr, type);
    }

    protected <T extends ReferenceableSchemaComponent> List<SchemaReference<T>> getSchemaReferenceList(
            Attribute attr, Class<T> type) {
        return getAttributeAccess().getSchemaReferenceList(attr, type);
    }

    protected <T extends BpelReferenceable> void setBpelReferenceList(
            Attribute attr, Class<T> type, List<BpelReference<T>> list) {
        getAttributeAccess().setBpelReferenceList(attr, type, list);
    }

    protected <T extends ReferenceableWSDLComponent> void setWSDLReferenceList(
            Attribute attr, Class<T> type,
            List<org.netbeans.modules.bpel.model.api.references.WSDLReference<T>> list) {
        getAttributeAccess().setWSDLReferenceList(attr, type, list);
    }

    protected void setBpelAttribute(ExtBpelAttribute attr, String value)
            throws VetoException {
        getAttributeAccess().setBpelAttribute(attr, value);
    }

    protected void setBpelAttribute(ExtBpelAttribute attr, Enum value) {
        getAttributeAccess().setBpelAttribute(attr, value);
    }

    protected void setBpelAttribute(ExtBpelAttribute attr, QName qName)
            throws VetoException {
        getAttributeAccess().setBpelAttribute(attr, qName);
    }

    protected void setBpelAttribute(Attribute attr, String value)
            throws VetoException {
        getAttributeAccess().setBpelAttribute(attr, value);
    }

    protected void setBpelAttribute(Attribute attr, Enum value) {
        getAttributeAccess().setBpelAttribute(attr, value);
    }

    protected void setBpelAttribute(Attribute attr, QName qName)
            throws VetoException {
        getAttributeAccess().setBpelAttribute(attr, qName);
    }

    protected QName getQNameAttribute(Attribute attr) {
        return getAttributeAccess().getQNameAttribute(attr);
    }

    @Override
    protected void setText(String propName, String text) {
        writeLock();
        try {
            StringBuilder oldValue = new StringBuilder();
            ArrayList<Node> toRemove = new ArrayList<Node>();
            NodeList nodeList = getPeer().getChildNodes();

//            Element ref = null;
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node == null) {
                    continue;
                }
//                if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
//                    ref = (Element) node;
//                }
                toRemove.add(node);
                if (oldValue == null && node instanceof Text &&
                        node.getNodeType() != Node.COMMENT_NODE) {
                    oldValue.append(node.getNodeValue());
                }
            }

            getModel().getAccess().removeChildren(getPeer(), toRemove, this);
            if (text != null) {
                Text newNode = getModel().getDocument().createTextNode(text);
//                if (ref != null) {
//                    getModel().getAccess().insertBefore(getPeer(), newNode, ref,
//                            this);
//                } else {
                    getModel().getAccess().appendChild(getPeer(), newNode, this);
//                }
            }

            firePropertyChange(propName,
                    oldValue == null ? null : oldValue.toString(), text);
            fireValueChanged();
        } finally {
            writeUnlock();
        }
    }

    /**
     * This method is return corrected Xml content without XML
     * comments. See the problem appeared in getText() method.
     * 
     */
    protected String getCorrectedCDataContent() {
        String result = null;
        readLock();
        try {
            NodeList nodeList = getPeer().getChildNodes();

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.CDATA_SECTION_NODE) {
                    assert node instanceof CDATASection;
                    result = ((CDATASection)node).getNodeValue();
                    break;
                }
            }
            
        } finally {
            readUnlock();
        }
        return result;
    }

    protected void setCDataContent(String propName, String content) 
            throws VetoException, IOException 
    {
        writeLock();
        try {
            List<Node> toRemove = new ArrayList<Node>();
            Node oldValue = null;
            NodeList nodeList = getPeer().getChildNodes();

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node == null) {
                    continue;
                }
                toRemove.add(node);
                if (oldValue == null && node.getNodeType() == Node.CDATA_SECTION_NODE) {
                    oldValue = node;
                }
            }

            if (toRemove.size()>0) {
                for (Node rmNode : toRemove) {
                    getModel().getAccess().removeChild(getPeer(), rmNode, this);   
                }
            }
            CDATASection cdataContent = getModel().getDocument().createCDATASection(content);
            
            getModel().getAccess().appendChild(getPeer(), cdataContent, this);
            
            firePropertyChange(propName,
                    oldValue == null ? null : oldValue.toString(), content);
            fireValueChanged();
        } finally {
            writeUnlock();
        }
    }

    /**
     * This method is return corrected Xml content without XML
     * comments. See the problem appeared in getText() method.
     * 
     */
    protected String getCorrectedXmlContent() {
        readLock();
        try {
            return getXmlFragment();
        } finally {
            readUnlock();
        }
    }

    protected void setXmlContent(String propName, String xmlContent) 
            throws VetoException, IOException 
    {
        writeLock();
        try {
            List<Node> toRemove = new ArrayList<Node>();
            Node oldValue = null;
            NodeList nodeList = getPeer().getChildNodes();

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node == null) {
                    continue;
                }
                toRemove.add(node);
                if (oldValue == null && node.getNodeType() == Node.ELEMENT_NODE) {
                    oldValue = node;
                }
            }

            getModel().getAccess().removeChildren(getPeer(), toRemove, this);
            getModel().getAccess().setXmlFragment(this.getPeer(), xmlContent, this);
            
            firePropertyChange(propName,
                    oldValue == null ? null : oldValue.toString(), xmlContent);
            fireValueChanged();
        } finally {
            writeUnlock();
        }
    }

    protected void setText(String text) throws VetoException {
        getAttributeAccess().setText(text);
    }

    protected void removeAttribute(Attribute attr) {
        getAttributeAccess().removeAttribute(attr);
    }

    protected <T extends BpelReferenceable> BpelReference<T> getBpelReference(
            Attribute attr, Class<T> clazz) {
        return getAttributeAccess().getBpelReference(attr, clazz);
    }

    protected <T extends ReferenceableWSDLComponent> WSDLReference<T> getWSDLReference(Attribute attr, Class<T> clazz) {
        return getAttributeAccess().getWSDLReference(attr, clazz);
    }

    protected <T extends ReferenceableSchemaComponent> SchemaReference<T> getSchemaReference(Attribute attr, Class<T> clazz) {
        return getAttributeAccess().getSchemaReference(attr, clazz);
    }

    protected <T extends BpelReferenceable> void setBpelReference(
            Attribute attr, BpelReference<T> ref) {
        getAttributeAccess().setBpelReference(attr, ref);
    }

    protected <T extends ReferenceableWSDLComponent> void setWSDLReference(
            Attribute attr,
            org.netbeans.modules.bpel.model.api.references.WSDLReference<T> ref) {
        getAttributeAccess().setWSDLReference(attr, ref);
    }

    protected <T extends ReferenceableSchemaComponent> void setSchemaReference(
            Attribute attr, SchemaReference<T> ref) {
        getAttributeAccess().setSchemaReference(attr, ref);
    }

    protected void removeReference(BpelAttributes attr) {
        getAttributeAccess().removeAttribute(attr);
    }

    /**
     * This method is return corrected Text content without XML
     * comments. See the problem appeared in getText() method.
     * 
     */
    protected String getCorrectedText() {
        readLock();
        try {
            StringBuilder text = new StringBuilder();
            NodeList nodeList = getPeer().getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node instanceof Text && !(node instanceof Comment)) {
                    text.append(node.getNodeValue());
                }
            }
            /*
             * TODO : there is bug in XAM/XDM.
             * XML entities such as &gt;, &apos;, &quot; is not recognized.
             * The method below perform replacement in string pointed
             * entities to corresponding values.
             * Usage of this method possibly should be removed
             * when bug in XAM/XDM will be fixed.  
             * Fix for #84651
             */
            return Utils.hackXmlEntities(text.toString());
        } finally {
            readUnlock();
        }
    }

    /*
     * This method has some problems.
     * It return XML comments that are found inside current XML element.
     * This is consequence of XDM implementation. XDM consider each line in 
     * XML comment as Text Node.
     * Possibly it will be fixed. New method getCorrectedText() is created for
     * fixing this problem.
     * 
     *  (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent#getText()
     */
    @Override
    protected String getText() {
        readLock();
        try {
            /*
             * TODO : there is bug in XAM/XDM.
             * XML entities such as &gt;, &apos;, &quot; is not recognized.
             * The method below perform replacement in string pointed
             * entities to corresponding values.
             * Usage of this method possibly should be removed
             * when bug in XAM/XDM will be fixed.  
             */
            return Utils.hackXmlEntities(super.getText());
        } finally {
            readUnlock();
        }
    }

    @Override
    protected void populateChildren(List<BpelEntity> children) {
        // bpelentity is not container
    }

    /*@Override
    protected String getNamespaceURI()
    {
    return BUSINESS_PROCESS_NS_URI;
    }*/
    @Override
    protected Object getAttributeValueOf(Attribute attr, String stringValue) {
        return getAttributeAccess().getAttributeValueOf(attr, stringValue);
    }

    //#############################################################
    //##
    //##    Next methods are utility methods for model framework. 
    //##
    //#############################################################
    public final void readLock() {
        getBpelModel().readLock();
    }

    public final void readUnlock() {
        getBpelModel().readUnlock();
    }

    protected final void writeLock() {
        getBpelModel().writeLock();
    }

    protected final void writeUnlock() {
        getBpelModel().writeUnlock();
    }

    protected final void checkInTree() {
        if (!isInTree()) {
            throw new IllegalStateException("Trying to access entity that "// NOI18N
                    + "is not in the tree"); // NOI18N
        }
    }

    public final void checkDeleted() {
        if (getBpelModel().isInEventsFiring()) {
            // allow to access to tree structure while handling event. 
            return;
        }
        if (isDeleted()) {
            throw new IllegalStateException("Trying to access entity that "// NOI18N
                    + "already was deleted"); // NOI18N
        }
    }

    protected final String getEntityName() {
        return getPeer().getLocalName();
    }

    protected final void setInTree() {
        isInTree = true;
    }

    protected final void setInTreeRecursively() {
        setInTree();
        List<BpelEntityImpl> list = getChildren(BpelEntityImpl.class);
        for (BpelEntityImpl child : list) {
            child.setInTreeRecursively();
        }
    }

    /**
     * This method changes old reference <code>reference</code>
     * to new reference with referenceable object <code>subject</code>
     */
    @SuppressWarnings("unchecked")
    void updateReference( Reference reference, BpelReferenceable subject) {
        getAttributeAccess().updateReference(reference, subject);
    }

    void handleAttributeChange(Node oldAttr, Node newAttr) {
        getAttributeAccess().handleAttributeChange(oldAttr, newAttr);
    }

    void setUID(UniqueId id) {
        myUid = id;
    }

    Map<Object, Object> getCookies() {
        return myCookies;
    }

    void setCookies(Map<Object, Object> cookieSet) {
        myCookies = cookieSet;
    }

    public final boolean isInTree() {
        /*Element element = getPeer();
        assert element instanceof org.netbeans.modules.xml.xdm.nodes.Element;
        
        return ((org.netbeans.modules.xml.xdm.nodes.Element) element)
        .isInTree();*/
        return isInTree;
    }

    final boolean isDeleted() {
        return isDeleted;
    }

    final void setDeleted() {
        isDeleted = true;
    }

    class IdMapKey {
    };

    //#############################################################
    //##
    //##    These are methods for firing event in model.
    //##
    //#############################################################
    protected void postGlobalEvent(ChangeEvent event, boolean propogateToModel) {
        checkDeleted();

        try {
            if (isInTree()) {
                if (propogateToModel) {
                    getModel().fireChangeEvent(event);
                }
            } else {
                getModel().getBuilder().fireChangeEvent(event);
            }
        } finally {
            /* 
             * Real firing of event will be performed only after unlocking.
             * So this is the safe to perfrom dispatching events by inner listeners
             * becuase previous action just put event into the queue.
             * But dispatching by inner listeners will be perfromed right now.
             * So external listeners will be notified when inner already 
             * have ended its work. 
             */
            getModel().postInnerEventNotify(event);
        }
    }

    protected void postGlobalEvent(ChangeEvent event) {
        postGlobalEvent(event, true);
    }

    private <T extends BpelEntity> CutEvent<T> preCut(T entity) {
        BpelChildEntitiesBuilder childBuilder = getBpelModel().getChildBuilder();
        childBuilder.setEffectiveParent(getParent());
        CutEvent<T> event = new CutEvent<T>(entity);
        try {
            getModel().preInnerEventNotify(event);
        } catch (VetoException e) {
            assert false;
        }
        return event;
    }

    protected final AttributeAccess getAttributeAccess() {
        return myAccess;
    }

    private <T extends BpelEntity> CopyEvent<T> preCopy(T entity) {
        CopyEvent<T> event = new CopyEvent<T>(entity);
        try {
            getModel().preInnerEventNotify(event);
        } catch (VetoException e) {
            assert false;
        }
        return event;
    }

    protected <T extends BpelEntity> BuildEvent<T> preCreated(T entity) {
        BuildEvent<T> event = new BuildEvent<T>(entity, getEntityName());
        try {
            getModel().preInnerEventNotify(event);
        } catch (VetoException e) {
            assert false;
        }
        return event;
    }

    protected <T extends BpelEntity> void postEvent(ChangeEvent event) {
        getBpelModel().postInnerEventNotify(event);
    }

    private static Element createNewComponent(BpelModelImpl model,
            String tagName) {
        return model.getDocument().createElementNS(
                BpelEntity.BUSINESS_PROCESS_NS_URI, tagName);
    }

    private static Element createNewComponent(BpelModelImpl model,
            BpelElements elem) {
        if (elem.getNamespace() == null) {
            return createNewComponent(model, elem.getName());
        } else {
            return model.getDocument().createElementNS(
                    elem.getNamespace(), elem.getName());
        }
    }

    @Override
    public String toString() {
        if (this instanceof Named) {
            return Named.class.cast(this).getName();
        } else {
            return super.toString();
        }
    }

    private static final Logger LOGGER = Logger.getLogger(BpelEntityImpl.class.getName());
    private static final byte INIT_COOKIE_CAPACITY = 8; // we don't need big capaicty. This is degree of 2.  
    private Map<Object, Object> myCookies =
            Collections.synchronizedMap(new WeakHashMap<Object, Object>(INIT_COOKIE_CAPACITY));
    private UniqueId myUid;
    private boolean isDeleted;
    private BpelModelImpl myModel;
    private AttributeAccess myAccess;
    private boolean isInTree;
}
