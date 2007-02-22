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

package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.awt.Dialog;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.xml.namespace.QName;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;

//import org.netbeans.modules.xml.refactoring.actions.FindUsagesAction;
//import org.netbeans.modules.xml.refactoring.actions.RefactorAction;
import org.netbeans.modules.xml.refactoring.ui.ReferenceableProvider;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.commands.CommonAttributePropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.commands.OtherAttributePropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.commands.XMLAttributePropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.cookies.RemoveWSDLElementCookie;
import org.netbeans.modules.xml.wsdl.ui.cookies.WSDLAttributeCookie;
import org.netbeans.modules.xml.wsdl.ui.cookies.WSDLElementCookie;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.UIUtilities;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.property.BaseAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.DocumentationNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.NewTypesFactory;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xml.xam.ui.ComponentPasteType;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.netbeans.modules.xml.xam.ui.actions.GoToAction;
import org.netbeans.modules.xml.xam.ui.cookies.CountChildrenCookie;
import org.netbeans.modules.xml.xam.ui.customizer.Customizer;
import org.netbeans.modules.xml.xam.ui.customizer.CustomizerProvider;
import org.netbeans.modules.xml.xam.ui.highlight.Highlight;
import org.netbeans.modules.xml.xam.ui.highlight.HighlightManager;
import org.netbeans.modules.xml.xam.ui.highlight.Highlighted;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.NewAction;
import org.openide.actions.PasteAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ReorderAction;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public abstract class WSDLElementNode extends AbstractNode
        implements ComponentListener, ReferenceableProvider, Highlighted,
        CountChildrenCookie, PropertyChangeListener {
    
    protected static final Logger mLogger = Logger.getLogger(WSDLElementNode.class.getName());
    
    private WSDLComponent mElement;
    /** Customizer soft reference; */
    private Reference<Customizer> customizerReference;
    private NewTypesFactory mNewTypesFactory;
    
    public static final String WSDL_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/";//NOI18N
    private InstanceContent mLookupContents;
    protected Sheet mSheet;
    private PropertyChangeListener weakModelListener;
    private ComponentListener weakComponentListener;
    /** Used for the highlighting API. */
    private Set<Component> referenceSet;
    /** Ordered list of highlights applied to this node. */
    private List<Highlight> highlights;
    
    /** cached so that during destroy all listeners can be cleaned up, nullified in destroy*/
    private WSDLModel model;
    
    private static final SystemAction[] ACTIONS = new SystemAction[] {
        SystemAction.get(CutAction.class),
        SystemAction.get(CopyAction.class),
        SystemAction.get(PasteAction.class),
        null,
        SystemAction.get(NewAction.class),
        SystemAction.get(DeleteAction.class),
        SystemAction.get(ReorderAction.class),
        null,
        SystemAction.get(GoToAction.class),
        //SystemAction.get(FindUsagesAction.class),
        (SystemAction)RefactoringActionsFactory.whereUsedAction(),
        null,
        (SystemAction)RefactoringActionsFactory.editorSubmenuAction(),
        null,
        SystemAction.get(PropertiesAction.class),
    };
    
    public WSDLElementNode(Children children, WSDLComponent element, NewTypesFactory newTypesFactory) {
        this(children, element);
        this.mNewTypesFactory = newTypesFactory;
        if (element != null) {
            model = element.getModel();
        }
    }

    public WSDLElementNode(Children children, WSDLComponent element) {
        this(children, element, new InstanceContent());
    }

    /**
     * Constructor hack to allow creating our own Lookup.
     *
     * @param  children  Node children.
     * @param  element   WSDL component.
     * @param  contents  Lookup contents.
     */
    private WSDLElementNode(Children children, WSDLComponent element,
            InstanceContent contents) {
        super(children, new AbstractLookup(contents));
        mElement = element;
        mLookupContents = contents;

        // Add various objects to the lookup.
        // Keep this node and its cookie implementation at the top of the
        // lookup, as they provide cookies needed elsewhere, and we want
        // this node to provide them, not the currently selected node.
        contents.add(this);
        contents.add(new WSDLElementCookie(mElement));
        // Include the data object in order for the Navigator to
        // show the structure of the current document.
        DataObject dobj = getDataObject();
        if (dobj != null) {
            contents.add(dobj);
        }
        contents.add(new SaveCookieDelegate());
        contents.add(new RemoveWSDLElementCookie(mElement));
        contents.add(element);
        Model model = element.getModel();
        weakModelListener = WeakListeners.propertyChange(this, model);
        model.addPropertyChangeListener(weakModelListener);
        weakComponentListener = (ComponentListener) WeakListeners.create(
                ComponentListener.class, this, model);
        model.addComponentListener(weakComponentListener);
        addNodeListener(new WSDLNodeListener(this));
        mSheet = new Sheet();
        // Let the node try to update its display name.
        updateDisplayName();
        
        referenceSet = Collections.singleton((Component) element);
        highlights = new LinkedList<Highlight>();
        HighlightManager.getDefault().addHighlighted(this);
    }
    
    public NewTypesFactory getNewTypesFactory() {
        if (mNewTypesFactory != null)
            return mNewTypesFactory;
        
        
        mNewTypesFactory = new NewTypesFactory() {
        
            public NewType[] getNewTypes(WSDLComponent component) {
                if (component.getDocumentation() == null) {
                    return new NewType[] {new DocumentationNewType(getWSDLComponent())};
                }
                return new NewType[] {};
            }
        
        };
        return mNewTypesFactory;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return ACTIONS;
    }
    
    @Override
    public void destroy() throws IOException {
        //get the stored model.
        if (model != null) {
            //remove the xml element listener when node is destroyed
            model.removePropertyChangeListener(weakModelListener);
            model.removeComponentListener(weakComponentListener);
            //remove reference for WSDLModel
            model = null;
        }
        
        WSDLModel wsdlModel = getWSDLComponent() != null ? getWSDLComponent().getModel() : null;
        if (wsdlModel != null) {
            //if we can get the model from wsdlcomponent, then delete the wsdlcomponent from model and appropriately select the node.
            WSDLComponent parent = getWSDLComponent().getParent();
            //try to select the parent.
            WSDLComponent nextSelection = parent;
            if (parent == null) {
                return;
            }

            if (parent.getChildren() != null) {
                int size = parent.getChildren().size();
                if (size > 0) {
                    int currentPos = parent.getChildren().indexOf(getWSDLComponent());
                    if (currentPos + 1 < size) {
                        nextSelection = parent.getChildren().get(currentPos + 1);
                    } else if (currentPos - 1 >= 0) {
                        nextSelection = parent.getChildren().get(currentPos - 1);
                    }
                }
                if (parent instanceof Definitions) {
                    //need to find a way to get the folder nodes selected
                    //for now select the root node.
                    if (!getWSDLComponent().getClass().isAssignableFrom(nextSelection.getClass())) {
                        nextSelection = parent;
                    }
                }
            }

            try {
                wsdlModel.startTransaction();
                wsdlModel.removeChildComponent(getWSDLComponent());
            } finally {
                wsdlModel.endTransaction();
            }
            ActionHelper.selectNode(nextSelection);
        }
        super.destroy();
    }

    @Override
    public boolean canCopy() {
        return true;
    }

    @Override
    public boolean canCut() {
        return isEditable();
    }

    @Override
    protected void createPasteTypes(Transferable transferable, List list) {
        // Make sure this node is still valid.
        if (mElement != null && mElement.getModel() != null && isEditable()) {
            PasteType type = ComponentPasteType.getPasteType(
                    mElement, transferable, null);
            if (type != null) {
                list.add(type);
            }
        }
    }

    @Override
    public PasteType getDropType(Transferable transferable, int action, int index) {
        // Make sure this node is still valid.
        if (mElement != null && mElement.getModel() != null && isEditable()) {
            PasteType type = ComponentPasteType.getDropType(
                    mElement, transferable, null, action, index);
            if (type != null) {
                return type;
            }
        }
        return null;
    }

    @Override
    public boolean canDestroy() {
        WSDLModel model = mElement.getModel();
        if (model != null && isSameAsMyWSDLElement(model.getDefinitions()) && !isEditable()) {
            return false;
        }
        return isEditable();
    }
    
    public WSDLComponent getWSDLComponent() {
        return this.mElement;
    }
    
    @Override
    public final NewType[] getNewTypes()
    {
        if (isEditable()) {
            return getNewTypesFactory().getNewTypes(getWSDLComponent());
        }
        return new NewType[] {};
    }

    /**
     * Subclasses wishing to provide a customizer must override this
     * method and supply a customizer provider, and override the
     * hasCustomizer() method to return true.
     *
     * @return  customizer provider.
     */
    protected CustomizerProvider getCustomizerProvider() {
        return null;
    }

    @Override
    public java.awt.Component getCustomizer() {
        if (!hasCustomizer() || !isEditable()) {
            return null;
        }
        Customizer customizer = customizerReference == null ? null :
            customizerReference.get();
        if (customizer == null) {
            CustomizerProvider cp = getCustomizerProvider();
            if (cp == null) {
                return null;
            }
            customizer = cp.getCustomizer();
            if (customizer == null || customizer.getComponent() == null) {
                return null;
            }
            customizerReference = new WeakReference<Customizer>(customizer);
        } else {
            customizer.reset();
        }
        DialogDescriptor descriptor = UIUtilities.getCustomizerDialog(
                customizer, getTypeDisplayName(), isEditable());
        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.getAccessibleContext().setAccessibleDescription(dlg.getTitle());
        return dlg;
    }

    /**
     * call this method before any method of XMLElementListener to check
     * if this is the same source.
     * @param node
     * @return
     */
    public boolean isSameAsMyWSDLElement(Component node) {
        if(node != null && node.equals(this.getWSDLComponent())) {
            return true;
        }
        
        return false;
    }

    public void childrenAdded(ComponentEvent evt) {
    }

    public void childrenDeleted(ComponentEvent evt) {
    }

    public void valueChanged(ComponentEvent evt) {
        if (!isSameAsMyWSDLElement((Component) evt.getSource())) {
            return;
        }
        updateDisplayName();
        refreshSheet();
        //fire a propertysets change so that property sheet can be refreshed
        firePropertySetsChange(new Node.PropertySet[] {}, getPropertySets());
    }

    public void propertyChange(PropertyChangeEvent event) {
        if (isValid() && event.getSource() == mElement) {
            try {
                updateDisplayName();
                String propName = event.getPropertyName();
                Sheet.Set propertySet = getSheet().get(Sheet.PROPERTIES);
                if (propertySet != null) {
                    if (propertySet.get(propName) != null) {
                        firePropertyChange(propName, event.getOldValue(),
                                event.getNewValue());
                    } else {
                        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL,
                                propName + " property is not defined in " +
                                getTypeDisplayName());
                    }
                }
            } catch (IllegalStateException ise) {
                // Component is not in the model.
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ise);
            } catch (NullPointerException npe) {
                // Does not reproduce reliably, but catch and log regardless.
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, npe);
            }
        }
    }

    /**
     * Determines if this node represents a component that is contained
     * in a valid (non-null) model.
     *
     * @return  true if model is valid, false otherwise.
     */
    protected boolean isValid() {
        return mElement.getModel() != null;
    }

    /**
     * Used by subclasses to update the display name as needed. The default
     * implementation updates the display name for named WSDL components.
     * Note, this method may be called from the constructor, so be sure to
     * avoid using member variables!
     */
    protected void updateDisplayName() {
        // Need a component connected to a model to work properly.
        if (isValid()) {
            // Automatically keep the name in sync for named components.
            if (mElement instanceof Named) {
                String name = ((Named) mElement).getName();
                // Prevent getting an NPE from ExplorerManager.
                super.setName(name == null ? "" : name);
                if (name == null || name.length() == 0) {
                    name = mElement.getPeer().getLocalName();
                }
                setDisplayName(name);
            }
        }
    }

    @Override
    protected final Sheet createSheet() {
        refreshSheet();
        return mSheet;
    }

    protected InstanceContent getLookupContents() {
        return mLookupContents;
    }

    private final void refreshSheet() {
        if (getWSDLComponent() != null && !getWSDLComponent().isInDocumentModel()) return; 
        refreshAttributesSheetSet();
        refreshOtherAttributesSheetSet();
        Cookie cookie = getCookie(WSDLAttributeCookie.class);
        if (hasOtherAttributesProperties()) {
            mLookupContents.add(new WSDLAttributeCookie("attribute", getWSDLComponent()));//NOI18N
        } else if (cookie != null) {
            mLookupContents.remove(cookie);
        }
    }
    
    private final boolean hasOtherAttributesProperties() {
        Map<QName,String> attributesMap = getWSDLComponent().getAttributeMap();
        if(attributesMap != null) {
            Iterator<Map.Entry<QName, String>> it = attributesMap.entrySet().iterator();
            while(it.hasNext()) {
                Entry<QName, String> entry = it.next();
                QName attrQName = entry.getKey();
                String ns = attrQName.getNamespaceURI();
                //if attribute are from non wsdl namespace
                //in that case we will have a namspace
                //for wsdl namspace attribute ns is empty string
                if(ns != null && !ns.trim().equals("") && !ns.equals(WSDL_NAMESPACE)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    
    
    protected void refreshAttributesSheetSet()  {
        Sheet.Set ss = createPropertiesSheetSet();

        try {
            AbstractDocumentComponent adc = (AbstractDocumentComponent) getWSDLComponent();
            QName elementQName = adc.getQName();
            String namespace = elementQName.getNamespaceURI();
            //here we go throw all the attributes which are specified so at least user cand
            //see them. wsdl spec element may not allow attributes apart from what is specified
            //in schema, but if a wsdl document have them we still should show and then
            //schema validation will catch the error
            Map<QName, String> attrMap = getWSDLComponent().getAttributeMap();
            for (QName attrQName : attrMap.keySet()) {
                String ns = attrQName.getNamespaceURI();
                
                Node.Property attrValueProperty = createAttributeProperty(attrQName);
                if(attrValueProperty != null) {
                    //if attribute are from non wsdl namespace
                    //in that case we will have a namspace
                    //for wsdl namspace attribute ns is empty string
                    if(ns == null || ns.trim().equals("") || ns.equals(namespace)) {
                        ss.put(attrValueProperty);
                    }
                }
            }
            
            List<Node.Property> properties = createAlwaysPresentAttributeProperty();
            if(properties != null) {
                Iterator<Node.Property> itP = properties.iterator();
                while(itP.hasNext()) {
                    Node.Property property = itP.next();
                    //if property is not present then add it
                    if(ss.get(property.getName()) == null) {
                        ss.put(property);
                    }
                }
            }
            
        } catch(Exception ex) {
            mLogger.log(Level.SEVERE, "failed to create property sheet for "+ getWSDLComponent(), ex);
        }
        
    }
    
    protected org.openide.nodes.Sheet.Set createOtherPropertiesSheetSet() {
        String otherAttributeSetName = NbBundle.getMessage(DefinitionsNode.class, "PROP_SHEET_CATEGORY_Other_Attributes");
        mSheet.remove(otherAttributeSetName);
        Sheet.Set otherAttributesSheetSet = new Sheet.Set();
        otherAttributesSheetSet.setName(otherAttributeSetName);
        otherAttributesSheetSet.setDisplayName(otherAttributeSetName);
        mSheet.put(otherAttributesSheetSet);
        return otherAttributesSheetSet;
    }

    protected Sheet.Set createPropertiesSheetSet() {
        mSheet.remove(Sheet.PROPERTIES);
        Sheet.Set ss = Sheet.createPropertiesSet();
        mSheet.put(ss);
        return ss;
    }

    //
    /**
     * Return a List of Node.Property which should be always present.
     * these attributes are always present and are not from extension namespace
     */
    protected List<Node.Property> createAlwaysPresentAttributeProperty() throws Exception {
        return Collections.emptyList();
    }
    
    protected Node.Property createAttributeProperty(QName attrQName) {
        Node.Property attrValueProperty = null;
        
        try {
            WSDLComponent component = getWSDLComponent();
            AbstractDocumentComponent adc = (AbstractDocumentComponent) component;
            QName elementQName = adc.getQName();
            String namespace = elementQName.getNamespaceURI();
            String ns = attrQName.getNamespaceURI();
            //if attribute are from non wsdl namespace
            //in that case we will have a namspace
            //for wsdl namspace attribute ns is empty string
            if(ns == null || ns.trim().equals("") || ns.equals(namespace)) {
                XMLAttributePropertyAdapter propertyAdapter = 
                    new XMLAttributePropertyAdapter(attrQName.getLocalPart(), component);
                attrValueProperty = getAttributeNodeProperty(attrQName.getLocalPart(), propertyAdapter);
                
            } else {
                OtherAttributePropertyAdapter propertyAdapter = 
                    new OtherAttributePropertyAdapter(attrQName, component);
                //attributes
                attrValueProperty = getOtherAttributeNodeProperty(attrQName, propertyAdapter);
                
            }
        } catch(Exception ex) {
            // mLogger.log(Level.SEVERE, "failed to create property sheet for "+ getWSDLComponent(), ex);
            ErrorManager.getDefault().notify(ex);
        }
        
        return attrValueProperty;
    }

    protected Node.Property getAttributeNodeProperty(String attrName, 
            XMLAttributePropertyAdapter propertyAdapter) throws NoSuchMethodException {
        Node.Property attrValueProperty = new BaseAttributeProperty(propertyAdapter, String.class, 
                CommonAttributePropertyAdapter.VALUE);
        attrValueProperty.setName(attrName);
        
        
        String desc = getAttributeShortDescription(attrName);
        if(desc != null && !desc.trim().equals("")) {
            attrValueProperty.setShortDescription(desc);
        } else {
            attrValueProperty.setShortDescription(attrName);
        }
        
        return attrValueProperty;
    }
    
    protected Node.Property getOtherAttributeNodeProperty(QName attrQName, 
            OtherAttributePropertyAdapter propertyAdapter) throws NoSuchMethodException {
        Node.Property attrValueProperty = new BaseAttributeProperty(propertyAdapter, String.class, CommonAttributePropertyAdapter.VALUE);
        attrValueProperty.setName(Utility.fromQNameToString(attrQName));
        String desc = getAttributeShortDescription(attrQName);
        if(desc != null && !(desc.trim().length() == 0)) {
            attrValueProperty.setShortDescription(desc);
        } else {
            attrValueProperty.setShortDescription(attrQName.toString());
        }
        
        return attrValueProperty;
    }

    
    protected String getAttributeShortDescription(QName attrQName) {
        return attrQName.toString();
    }
    
    protected String getAttributeShortDescription(String attrName) {
        return attrName;
    }
    
    protected void refreshOtherAttributesSheetSet() {
        Sheet.Set otherAttributesSheetSet = createOtherPropertiesSheetSet();
        addOtherAttributesProperties(otherAttributesSheetSet);
    }
     
        
    public static class QNameAttribute implements Attribute {
        private QName qName;
        QNameAttribute(QName q) {
            this.qName = q;
        }
        public String getName() {
            return Utility.fromQNameToString(qName);
        }
        public Class getType() { return String.class; }
        public Class getMemberType() { return String.class; }
    }
        
    private void addOtherAttributesProperties(Sheet.Set ss) {
        Map<QName,String> attributesMap = getWSDLComponent().getAttributeMap();
        if(attributesMap != null) {
            Iterator<Map.Entry<QName, String>> it = attributesMap.entrySet().iterator();
            while(it.hasNext()) {
                Entry<QName, String> entry = it.next();
                QName attrQName = entry.getKey();
                String ns = attrQName.getNamespaceURI();
                //if attribute are from non wsdl namespace
                //in that case we will have a namspace
                //for wsdl namspace attribute ns is empty string
                if(ns != null && !ns.trim().equals("") && !ns.equals(WSDL_NAMESPACE)) {
                    //String value = entry.getValue();
                    OtherAttributePropertyAdapter propertyAdapter = new OtherAttributePropertyAdapter(attrQName, 
                            getWSDLComponent());
                    //attributes
                    Node.Property attrValueProperty;
                    try {
                        attrValueProperty = getOtherAttributeNodeProperty(attrQName, propertyAdapter);
                        ss.put(attrValueProperty);
                    } catch (NoSuchMethodException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                }
            }
        }
    }
      
    /**
     * Determines if this node represents a component that is contained
     * is editable
     *
     * @return  true if component is editable, false otherwise.
     */
    
    protected boolean isEditable() {
        Model model = mElement.getModel();
        return model != null && XAMUtils.isWritable(model);
    }

    /**
     * 
     * 
     * @returns Referenceable to be used by Refactoring Find Usage, Safe Delete, 
     *          and Rename
     */
    public Referenceable getReferenceable() {
        return (mElement instanceof Referenceable)?Referenceable.class.cast(mElement):null;
    }
    
    public DataObject getDataObject() {
        return ActionHelper.getDataObject(mElement);
    }

    public Set<Component> getComponents() {
        return referenceSet;
    }

    public int getChildCount() {
        return getWSDLComponent().getChildren().size();
    }
    
    public void highlightAdded(Highlight hl) {
        highlights.add(hl);
        fireDisplayNameChange("TempName", getDisplayName());
    }

    public void highlightRemoved(Highlight hl) {
        highlights.remove(hl);
        fireDisplayNameChange("TempName", getDisplayName());
    }

    /**
     * Given a display name, add the appropriate HTML tags to highlight
     * the display name as dictated by any Highlights associated with
     * this node.
     *
     * @param  name  current display name.
     * @return  marked up display name.
     */
    protected String applyHighlights(String name) {
        int count = highlights.size();
        if (count > 0) {
            // Apply the last highlight that was added to our list.
            Highlight hl = highlights.get(count - 1);
            String type = hl.getType();
            String code = null;
            if (type.equals(Highlight.SEARCH_RESULT)) {
                code = "e68b2c";
            } else if (type.equals(Highlight.SEARCH_RESULT_PARENT)) {
                code = "ffc73c";
            }  else if (type.equals(Highlight.FIND_USAGES_RESULT_PARENT)) {
                code = "B5E682";    // was c7ff3c chartreuse
            }else if (type.equals(Highlight.FIND_USAGES_RESULT)) {
                code = "8be62c";    // darker green
            }
            name = "<strong><font color=\"#" + code + "\">" + name +
                    "</font></strong>";
        }
        return name;
    }

    @Override
    public String getDisplayName() {
        String instanceName = getDefaultDisplayName();
        if (getTypeDisplayName() == null) {
            return instanceName;
        }
        return instanceName.length()==0 ? instanceName : 
           instanceName + " " + "[" + getTypeDisplayName() + "]"; // NOI18N
    }
    
    public String getDefaultDisplayName() {
    String instanceName = super.getDisplayName();
        return instanceName == null || instanceName.length() == 0
        ? "" : instanceName; 
    }
    
    @Override
    public String getHtmlDisplayName() {
        String name = getDefaultDisplayName();
        // Need to escape any HTML meta-characters in the name.
        if(name!=null)
            name = name.replace("<", "&lt;").replace(">", "&gt;");
        return applyHighlights(name);
    }
    
    class SaveCookieDelegate implements SaveCookie {
        
        public void save() throws IOException {
            DataObject dobj = getDataObject();
            // May be null if component was removed from the model.
            if (dobj != null) {
                SaveCookie cookie = (SaveCookie) dobj.getCookie(SaveCookie.class);
                if (cookie != null) {
                    cookie.save();
                }
            }
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        //TODO:SKINI change this after documentation has been added for individual nodes
        //return new HelpCtx(getClass().getName());
        return new HelpCtx("org.netbeans.modules.xml.wsdlui.about");
    }
    
    
    /**
    *
    *
    */
   public abstract String getTypeDisplayName();

   
}
