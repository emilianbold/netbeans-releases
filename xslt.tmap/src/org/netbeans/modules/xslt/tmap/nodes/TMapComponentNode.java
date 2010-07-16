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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.xslt.tmap.model.api.events.VetoException;
import org.netbeans.modules.xslt.tmap.nodes.actions.AddOperationAction;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.netbeans.modules.soa.ui.nodes.InstanceRef;
import org.netbeans.modules.soa.ui.nodes.NodeTypeHolder;
import org.netbeans.modules.soa.ui.nodes.synchronizer.ModelSynchronizer;
import org.netbeans.modules.soa.ui.nodes.synchronizer.SynchronisationListener;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.netbeans.modules.xslt.tmap.model.api.Nameable;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.nodes.actions.ActionType;
import org.netbeans.modules.xslt.tmap.nodes.actions.AddInvokeAction;
import org.netbeans.modules.xslt.tmap.nodes.actions.AddParamAction;
import org.netbeans.modules.xslt.tmap.nodes.actions.AddServiceAction;
import org.netbeans.modules.xslt.tmap.nodes.actions.AddTransformAction;
import org.netbeans.modules.xslt.tmap.nodes.actions.AddWsdlImportAction;
import org.netbeans.modules.xslt.tmap.nodes.actions.DeleteAction;
import org.netbeans.modules.xslt.tmap.nodes.actions.GoToAction;
import org.netbeans.modules.xslt.tmap.nodes.actions.GoToSourceAction;
import org.netbeans.modules.xslt.tmap.nodes.actions.GoToTreeAction;
import org.netbeans.modules.xslt.tmap.nodes.actions.OpenInEditorAction;
import org.netbeans.modules.xslt.tmap.nodes.actions.PropertiesAction;
import org.netbeans.modules.xslt.tmap.nodes.actions.TMapAbstractNodeAction;
import org.netbeans.modules.xslt.tmap.nodes.actions.TMapNodeNewType;
import org.netbeans.modules.xslt.tmap.nodes.properties.Constants;
import org.netbeans.modules.xslt.tmap.nodes.properties.PropertyType;
import org.netbeans.modules.xslt.tmap.nodes.properties.PropertyUtils;
import org.openide.actions.NewAction;
import org.openide.actions.PasteAction;
//import org.openide.actions.PropertiesAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public abstract class TMapComponentNode<T extends DecoratedTMapComponent<? extends TMapComponent>> extends AbstractNode 
        implements InstanceRef<T>, NodeTypeHolder<NodeType>, SynchronisationListener 
{
    
    public static final String EMPTY_STRING = ""; // NOI18N
    public static final String WHITE_SPACE = " "; // NOI18N

    public static final String MAIN_SET = "Main"; // NOI18N
    
    private static final Logger LOGGER = Logger.getLogger(TMapComponentNode.class.getName()); 
    public static final Object UNKNOWN_OLD_VALUE = new Object();
    
    private T myDecoratedComponent; 
    private ModelSynchronizer synchronizer;
    private final Object NAME_LOCK = new Object();
    private String cachedName;
    private String cachedShortDescription;
    private String cachedHtmlDisplayName;

    private static final Map<ActionType, Action> ACTION_TYPE_MAP
            = new HashMap<ActionType, Action>();
    
    static {
        ACTION_TYPE_MAP.put(ActionType.REMOVE
                ,SystemAction.get(DeleteAction.class));
        ACTION_TYPE_MAP.put(ActionType.COPY
                , SystemAction.get(CopyAction.class));
        ACTION_TYPE_MAP.put(ActionType.CUT
                ,SystemAction.get(CutAction.class));
        ACTION_TYPE_MAP.put(ActionType.PASTE
                ,SystemAction.get(PasteAction.class));
//        ACTION_TYPE_MAP.put(ActionType.PROPERTIES
//                , SystemAction.get(PropertiesAction.class));
        ACTION_TYPE_MAP.put(ActionType.PROPERTIES
                , SystemAction.get(PropertiesAction.class));
        ACTION_TYPE_MAP.put(ActionType.ADD_NEWTYPES
                , SystemAction.get(NewAction.class));
        ACTION_TYPE_MAP.put(ActionType.ADD_WSDL_IMPORT
                , SystemAction.get(AddWsdlImportAction.class));
        ACTION_TYPE_MAP.put(ActionType.ADD_SERVICE
                , SystemAction.get(AddServiceAction.class));
        ACTION_TYPE_MAP.put(ActionType.ADD_OPERATION
                , SystemAction.get(AddOperationAction.class));
        ACTION_TYPE_MAP.put(ActionType.ADD_TRANSFORM
                , SystemAction.get(AddTransformAction.class));
        ACTION_TYPE_MAP.put(ActionType.ADD_INVOKE
                , SystemAction.get(AddInvokeAction.class));
        ACTION_TYPE_MAP.put(ActionType.ADD_PARAM
                , SystemAction.get(AddParamAction.class));
        ACTION_TYPE_MAP.put(ActionType.GO_TO
                , SystemAction.get(GoToAction.class));
        ACTION_TYPE_MAP.put(ActionType.GO_TO_SOURCE
                , SystemAction.get(GoToSourceAction.class));
        ACTION_TYPE_MAP.put(ActionType.GO_TO_TREE
                , SystemAction.get(GoToTreeAction.class));
        ACTION_TYPE_MAP.put(ActionType.OPEN_IN_EDITOR
                , SystemAction.get(OpenInEditorAction.class));
        

    }

    public TMapComponentNode(T ref, Lookup lookup) {
        this(ref, Children.LEAF, lookup);
    }
    
    public TMapComponentNode(T ref, Children children, Lookup lookup) {
        super(children);
        assert ref != null;
        setReference(ref);
        
        assert ref.getReference() != null;
        TMapModel model = ref.getReference().getModel();

        assert model != null: "Can't create Node for orphaned TMapComponent"; // NOI18N
        synchronizer = new ModelSynchronizer(this);
        synchronizer.subscribe(model);
        
        // set nodeDescription property which is shown in property sheet help region
        setValue("nodeDescription", "");
    }

    public Object getAlternativeReference() {
        return myDecoratedComponent != null ? myDecoratedComponent.getAlternativeReference() : null;
    }

    public abstract NodeType getNodeType();
    
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
    public java.awt.Component getCustomizer() {
        return getCustomizer(CustomNodeEditor.EditingMode.EDIT_INSTANCE);
    }
    
    public java.awt.Component getCustomizer(CustomNodeEditor.EditingMode editingMode) {
        return null;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        
        //
        if (obj instanceof TMapComponentNode) {
            Object thisOrig = this.getReference().getReference();
            Object objOrig = ((TMapComponentNode) obj).getReference();
            
            Object thisAlter = this.getAlternativeReference();
            Object objAlter = this.getAlternativeReference();
            
            return this.getNodeType().equals(((TMapComponentNode) obj).getNodeType())
                    && thisOrig != null
                    && thisOrig.equals(objOrig)
                    && ((thisAlter != null && thisAlter.equals(objAlter)) || thisAlter == null);
        }
        //
        return false;
    }

    @Override
    public int hashCode() {
        T ref = this.getReference();
        Object thisOrig = ref != null ? ref.getReference() : ref;
        Object nodeType = getNodeType();
        Object thisAlter = this.getAlternativeReference();
        return nodeType == null ? 0 : nodeType.hashCode() 
                + (thisOrig == null ? 0 : thisOrig.hashCode())
                + (thisAlter == null ? 0 : thisAlter.hashCode())
                + (ref == null ? 0 : ref.hashCode());
    }

    @Override
    public void setName(String s) {
        if (!canRename()) {
            return;
        }

        TMapComponent ref =  getReference().getReference();
        if (ref instanceof Nameable) {
            boolean isOwnTransact = false;
            try {
                if (!ref.getModel().isIntransaction()) {
                    ref.getModel().startTransaction();
                    isOwnTransact = true;
                }
                ((Nameable) ref).setName(s);

            } catch (VetoException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            } finally {
                if (isOwnTransact && ref.getModel().isIntransaction()) {
                    ref.getModel().endTransaction();
                }
            }
            super.setName(s);
            updateName();
        }
    }

    @Override
    public boolean canRename() {
        return isEditable() && getReference().getReference() instanceof Nameable;
    }

    @Override
    public boolean canCopy() {
        return true;
    }
    
    @Override
    public boolean canCut() {
        return true;
    }

    @Override
    public boolean canDestroy() {
        DeleteAction action = getActualDeleteAction()/*SystemAction.get(DeleteAction.class)*/;
        return action != null && action.enable(new Node[] {this});
    }
    
    @Override
    public void destroy() throws IOException {
        
        TMapComponent ref = getReference().getReference();
        
        DeleteAction action = getActualDeleteAction();
        if (action != null) {
            action.performAction(new Node[] {this});
        }
        
        if (ref != null){
            synchronizer.unsubscribe();
        }
        super.destroy();
    }
    
    private DeleteAction getActualDeleteAction() {
        Action[] actions = getActions(true);
        for (Action elem : actions) {
            if (elem instanceof DeleteAction) {
                return (DeleteAction)elem;
            }
        }
        return null;
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
    
    public void updateShortDescription() {
        synchronized(NAME_LOCK){
            cachedShortDescription = null;
        }
        
        fireShortDescriptionChange(null,getShortDescription());
    }

    /**
     * Looks for the Properties Set by the Group enum.
     * If the group isn't
     */
    protected Sheet.Set getPropertySet(
            Sheet sheet, Constants.PropertiesGroups group) {
        Sheet.Set propSet = sheet.get(group.getDisplayName());
        if (propSet == null) {
            propSet = new Sheet.Set();
            propSet.setName(group.getDisplayName());
            sheet.put(propSet);
        }
        //
        return propSet;
    }

    @Override
    public Action[] getActions(boolean context) {
        TMapComponent ref = getComponentRef();
        TMapModel model = ref == null ? null : ref.getModel();
        Action[] actions = null;
        if (model !=null 
                && TMapModel.State.VALID.equals(model.getState())) 
        {
            actions = createActionsArray(context);
        }
        return  actions == null ? super.getActions(context) : actions;
    }

    protected ActionType[] getActionsArray() {
        List<ActionType> actionList = new ArrayList<ActionType>();
        T ref = getReference(); 
        
//        if (ref instanceof Activity) {
//            actionList.add(ActionType.ADD_FROM_PALETTE);
//            if (!(ref instanceof Process)) {
//                actionList.add(ActionType.WRAP);
//            }
//            actionList.add(ActionType.SEPARATOR);
//        }
//        if (ref instanceof CompositeActivity) {
//            actionList.add(ActionType.CHANGE_ORDER_ACTION);
//            actionList.add(ActionType.SEPARATOR);
//        }
//        
//        actionList.add(ActionType.GO_TO_SOURCE);
//        if (ref instanceof Activity) {
//            actionList.add(ActionType.GO_TO_DIAGRAMM);
//        }
//        if (ref instanceof Activity) {
//            actionList.add(ActionType.SEPARATOR);
//            actionList.add(ActionType.MOVE_UP);
//            actionList.add(ActionType.MOVE_DOWN);
//            actionList.add(ActionType.SEPARATOR);
//            actionList.add(ActionType.TOGGLE_BREAKPOINT);
//        }
//        actionList.add(ActionType.SEPARATOR);
        actionList.add(ActionType.ADD_NEWTYPES);
        actionList.add(ActionType.SEPARATOR);
        actionList.add(ActionType.REMOVE);
//        actionList.add(ActionType.SEPARATOR);
//        actionList.add(ActionType.COPY);
//        actionList.add(ActionType.CUT);
//        actionList.add(ActionType.PASTE);
//        actionList.add(ActionType.SEPARATOR);
        PropertySet[] propertySets = getPropertySets();
        if (propertySets != null && propertySets.length > 0) {
            actionList.add(ActionType.SEPARATOR);
            actionList.add(ActionType.PROPERTIES);
        }
        return actionList.toArray(new ActionType[actionList.size()]);
    }
    
    /**
     * @return actions type which used to create new elements
     * used in #getNewTypes
     */
    public ActionType[] getAddActionArray() {
        return new ActionType[0];
    }
    
    /**
     * @return the new types that can be created in this node
     */
    @Override
    public NewType[] getNewTypes() {
        List<TMapAbstractNodeAction> actions = getAddActions();
        if (actions == null || actions.size() < 1) {
            return super.getNewTypes();
        }
        NewType[] newTypes = new NewType[actions.size()];
        for (int i = 0; i < newTypes.length; i++) {
            newTypes[i] = new TMapNodeNewType(actions.get(i), this);
        }
        return newTypes;
    }
    
    public List<TMapAbstractNodeAction> getAddActions() {
        ActionType[] actions = getAddActionArray();
        if (actions == null || actions.length == 0) {
            return null;
        }
        List<TMapAbstractNodeAction> resultAddActions = new ArrayList<TMapAbstractNodeAction>();
        for (ActionType elem : actions) {
            if (elem != null) {
                Action action = createAction(elem);
                if (action != null
                        && action instanceof TMapAbstractNodeAction
                        && ((TMapAbstractNodeAction)action).enable(new Node[] {this})) {
                    resultAddActions.add((TMapAbstractNodeAction)action);
                }
            }
        }
        
        return resultAddActions;
    }
    
    protected Action[] createActionsArray(boolean context) {
        ActionType[] actions = getActionsArray();
        if (actions == null) {
            return null;
        }
        
        Action[] actionsArray = new Action[actions.length];
        for (int i = 0; i < actions.length; i++) {
            actionsArray[i] = createAction(actions[i]);
        }
        return actionsArray;
    }
    
    public Action createAction(ActionType actionType) {
        Action action = null;
        return ACTION_TYPE_MAP.get(actionType);
    }
    
    
    public TMapComponent getComponentRef() {
        T ref = getReference();
        return ref.getReference();
    }

//    @Override
//    public Transferable clipboardCopy() throws IOException {
//        return super.clipboardCopy();
//    }
//
//    @Override
//    public Transferable clipboardCut() throws IOException {
//        return super.clipboardCut();
//    }
    
    protected void updateProperty(PropertyType propertyType) {
        updateProperty(propertyType.toString());
    }
    
    protected void updateProperty(String propertyName) {
        Property prop = PropertyUtils.getInstance().lookForPropertyByName(this, propertyName);
        Object newValue = null;
        try {
            newValue = prop.getValue();
        } catch (Exception ex) {
            // do nothing here
        }
        firePropertyChange(propertyName, UNKNOWN_OLD_VALUE, newValue);
    }
    
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
    
    /**
     * Iterates over all registered properties and update them.
     */
    public void updateAllProperties() {
        PropertySet[] psArr = getSheet().toArray();
        for (PropertySet ps : psArr) {
            Property[] propArr = ps.getProperties();
            for (Property prop : propArr) {
                String propName = prop.getName();
                try {
                    Object newPropValue = prop.getValue();
                    firePropertyChange(propName, UNKNOWN_OLD_VALUE, newPropValue);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public void updateAttributeProperty(String attributeName) {
        Property prop = PropertyUtils.getInstance().lookForPropertyByBoundedAttribute(
                this, attributeName);
        if (prop != null) {
            String propName = prop.getName();
            try {
                Object newPropValue = prop.getValue();
                firePropertyChange(propName, UNKNOWN_OLD_VALUE, newPropValue);
            } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    public void updateElementProperty(Class elementClass) {
        Property prop = PropertyUtils.getInstance().lookForPropertyByBoundedElement(
                this, elementClass);
        if (prop != null) {
            String propName = prop.getName();
            try {
                Object newPropValue = prop.getValue();
                firePropertyChange(propName, UNKNOWN_OLD_VALUE, newPropValue);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    
//    private class Synchronizer implements ComponentListener, PropertyChangeListener {
//        public Synchronizer() {
//        }
//        
//        private void subscribe(TMapModel model) {
//            assert model != null;
//            model.addPropertyChangeListener(this);
//            model.addComponentListener(this);
//        }
//
//        private void unsubscribe(TMapModel model) {
//            assert model != null;
//            model.removeComponentListener(this);
//            model.removePropertyChangeListener(this);
//        }
//        
//        public void valueChanged(ComponentEvent evt) {
//            System.out.println("value changed: "+evt.getSource());
//            reloadChildren();
//        }
//
//        public void childrenAdded(ComponentEvent evt) {
//            System.out.println("children added: "+evt.getSource());
//            reloadChildren();
//        }
//
//        public void childrenDeleted(ComponentEvent evt) {
//            System.out.println("children deleted: "+evt.getSource());
//            reloadChildren();
//        }
//
//        public void propertyChange(PropertyChangeEvent evt) {
//            System.out.println("property change event:  "+evt.getPropertyName());
//            System.out.print(" oldValue: "+evt.getOldValue());
//            System.out.print(" newValue: "+evt.getNewValue());
//        }
//        
//        private void reloadChildren() {
//            
//            Children children = getChildren();
//            
//            if (children instanceof ReloadableChildren) {
//                ((ReloadableChildren)children).reload();
//            } else if (TMapComponentNode.this instanceof ReloadableChildren) {
//                ((ReloadableChildren) TMapComponentNode.this).reload();
//            }
//        }
//
////////        public void notifyEntityRemoved(EntityRemoveEvent event) {
////////            BpelEntity entity = event.getOutOfModelEntity();
////////            //
////////            T ref = getReference();
////////            if (ref == null) {
////////                //
////////                // the referenced element already removed
////////                //
////////                BpelModel bpelModel = entity.getBpelModel();
////////                unsubscribedFromAndDestroy(bpelModel);
////////            } else {
////////                if (BpelNode.this.isEventRequreUpdate(event)) {
////////                    reloadChildren();
////////                }
////////                
////////                if (event.getOutOfModelEntity() instanceof Documentation 
////////                        && ref.equals(event.getParent())) 
////////                {
////////                    updateShortDescription();
////////                }
////////            }
////////            //
////////            // Perform update processing of complex ptoperties
////////            updateComplexProperties(event);
////////        }
////////        
////////        public void notifyPropertyRemoved(PropertyRemoveEvent event) {
////////            BpelEntity entity = event.getParent();
////////            
////////            //
//////////////            if (BpelNode.this.isEventRequreUpdate(event)) {
//////////////                String attributeName = event.getName();
//////////////                updateAttributeProperty(attributeName);
//////////////                updateName();
//////////////                //
//////////////                // Check if the property has the List type
//////////////                Object value = event.getOldValue();
//////////////                if (value != null && value instanceof List) {
//////////////                    reloadChildren();
//////////////                }
//////////////            }
//////////////            //
//////////////            // Check that the property is the content of an entity
//////////////            // which owned by the node's entity.
//////////////            if (ContentElement.CONTENT_PROPERTY.equals(event.getName())) {
//////////////                BpelEntity parentEntity = event.getParent();
//////////////                //
//////////////                T curEntity = getReference();
//////////////                if (curEntity != null && parentEntity != null &&
//////////////                        parentEntity.getParent() == curEntity) {
//////////////                    updateElementProperty(parentEntity.getClass());
//////////////                }
//////////////            }
//////////////
////////            //
////////            T ref = getReference();
////////            if (ref == null) {
////////                //
////////                // the referenced element already removed
////////                //
////////                BpelModel bpelModel = entity.getBpelModel();
////////                unsubscribedFromAndDestroy(bpelModel);
////////            } else {
////////                if (BpelNode.this.isEventRequreUpdate(event)) {
////////                    reloadChildren();
////////                }
////////            }
////////            //
////////            // Perform update processing of complex ptoperties
////////            updateComplexProperties(event);
////////        }
////////        
////////        public void notifyPropertyUpdated(PropertyUpdateEvent event) {
////////            if (BpelNode.this.isEventRequreUpdate(event)) {
////////                String attributeName = event.getName();
////////                updateAttributeProperty(attributeName);
////////                updateName();
////////                //
////////                // Check if the property has the List type
////////                Object value = event.getNewValue();
////////                if (value == null) {
////////                    value = event.getOldValue();
////////                }
////////                if (value != null && value instanceof List) {
////////                    reloadChildren();
////////                }
////////            }
////////            //
////////            // Check that the property is the content of an entity
////////            // which owned by the node's entity.
////////            if (ContentElement.CONTENT_PROPERTY.equals(event.getName())) {
////////                BpelEntity parentEntity = event.getParent();
////////                //
////////                T curEntity = getReference();
////////                if (curEntity != null && parentEntity != null &&
////////                        parentEntity.getParent() == curEntity) {
////////                    updateElementProperty(parentEntity.getClass());
////////                    if (parentEntity instanceof Documentation) {
////////                        updateShortDescription();
////////                    }
////////                }
////////            }
////////            //
////////            // Perform update processing of complex ptoperties
////////            updateComplexProperties(event);
////////        }
////////        
////////        public void notifyArrayUpdated(ArrayUpdateEvent event) {
////////            if (BpelNode.this.isEventRequreUpdate(event)) {
////////                reloadChildren();
////////            }
////////            //
////////            // Perform update processing of complex ptoperties
////////            updateComplexProperties(event);
////////        }
////////        
////////        public void notifyEntityUpdated(EntityUpdateEvent event) {
////////            if (BpelNode.this.isEventRequreUpdate(event)) {
////////                BpelEntity entity = event.getNewValue();
////////                if (entity == null) {
////////                    entity = event.getOldValue();
////////                }
////////                if (entity != null) {
////////                    updateElementProperty(entity.getClass());
////////                }
////////                //
////////                reloadChildren();
////////            }
////////            
////////            //
////////            // Perform update processing of complex ptoperties
////////            updateComplexProperties(event);
////////        }
////////        
////////        public void notifyEntityInserted(EntityInsertEvent event) {
////////            if (BpelNode.this.isEventRequreUpdate(event)) {
////////                BpelEntity entity = event.getValue();
////////                if (entity != null) {
////////                    updateElementProperty(entity.getClass());
////////                }
////////                
////////                if (entity instanceof Documentation) {
////////                    updateShortDescription();
////////                } else {
////////                    //
////////                    reloadChildren();
////////                }
////////            }
////////            //
////////            // Perform update processing of complex ptoperties
////////            updateComplexProperties(event);
////////        }
//        
//    }

    /**
     * Determines if this node represents a component that is contained
     * is editable
     *
     * @return  true if component is editable, false otherwise.
     */
    public boolean isEditable() {
        TMapComponent component = getComponentRef();
        Model model = component == null ? null : component.getModel();
        return model != null && XAMUtils.isWritable(model);
    }

    public void componentUpdated(Component component) {
        if (component.equals(getReference().getReference())) {
            updateName();
            updateShortDescription();
            updateAllProperties();
        }
    }

    public void childrenUpdated(Component component) {
        if (component.equals(getReference().getReference())) {
            reloadChildren();
        }
    }

    private void reloadChildren() {

        Children children = getChildren();

        if (children instanceof ReloadableChildren) {
            ((ReloadableChildren)children).reload();
        } else if (TMapComponentNode.this instanceof ReloadableChildren) {
            ((ReloadableChildren) TMapComponentNode.this).reload();
        }
    }

}
