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
package org.netbeans.modules.bpel.nodes;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bpel.design.model.DelegatingChangeEventListener;
import org.netbeans.modules.bpel.model.api.CompositeActivity;
import org.netbeans.modules.bpel.model.api.Documentation;
import org.netbeans.modules.bpel.nodes.actions.AddElseAction;
import org.netbeans.modules.bpel.nodes.actions.AddFromPaletteAction;
import org.netbeans.modules.bpel.nodes.actions.AddPartnerLinkAction;
import org.netbeans.modules.bpel.nodes.actions.GoToTypeSourceAction;
import org.netbeans.modules.bpel.nodes.actions.MoveDownBpelEntityAction;
import org.netbeans.modules.bpel.nodes.actions.MoveUpBpelEntityAction;
import org.netbeans.modules.bpel.nodes.actions.WrapAction;
import org.netbeans.modules.soa.ui.nodes.InstanceRef;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.ContentElement;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.events.ArrayUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.ChangeEventListenerAdapter;
import org.netbeans.modules.bpel.model.api.events.EntityInsertEvent;
import org.netbeans.modules.bpel.model.api.events.EntityRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.EntityUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyRemoveEvent;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.events.ChangeEventListener;
import org.netbeans.modules.soa.ui.nodes.NodeTypeHolder;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.netbeans.modules.bpel.nodes.actions.AddPropertyToWsdlAction;
import org.netbeans.modules.bpel.nodes.actions.DeleteBpelExtensibiltyWsdlRefAction;
import org.netbeans.modules.bpel.nodes.actions.DeletePropertyAction;
import org.netbeans.modules.bpel.nodes.actions.FindMexPeerAction;
import org.netbeans.modules.bpel.nodes.actions.GoToDiagrammAction;
import org.netbeans.modules.bpel.nodes.actions.GoToMessageExchangeContainerSourceAction;
import org.netbeans.modules.bpel.nodes.actions.MoveDownCopyAction;
import org.netbeans.modules.bpel.nodes.actions.MoveUpCopyAction;
import org.netbeans.modules.bpel.nodes.actions.ShowBpelMapperAction;
import org.netbeans.modules.bpel.nodes.actions.ToggleBreakpointAction;
import org.netbeans.modules.bpel.nodes.navigator.BpelNavigatorLookupHint;
import org.netbeans.modules.bpel.nodes.navigator.BpelNavigatorDecorationProvider;
import org.netbeans.modules.bpel.nodes.validation.ChangeValidationListener;
import org.netbeans.modules.bpel.nodes.validation.ValidationProxyListener;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.soa.ui.ExtendedLookup;
import org.netbeans.modules.soa.ui.nodes.NodesTreeParams;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.netbeans.modules.bpel.nodes.actions.AddCatchAction;
import org.netbeans.modules.bpel.nodes.actions.AddCatchAllAction;
import org.netbeans.modules.bpel.nodes.actions.AddCompensationHandlerAction;
import org.netbeans.modules.bpel.nodes.actions.AddElseIfAction;
import org.netbeans.modules.bpel.nodes.actions.AddCorrelationSetAction;
import org.netbeans.modules.bpel.nodes.actions.AddMessageExchangeAction;
import org.netbeans.modules.bpel.nodes.actions.AddOnAlarmAction;
import org.netbeans.modules.bpel.nodes.actions.AddOnEventAction;
import org.netbeans.modules.bpel.nodes.actions.AddPropertyAction;
import org.netbeans.modules.bpel.nodes.actions.AddPropertyAliasToWsdlAction;
import org.netbeans.modules.bpel.nodes.actions.AddSchemaImportAction;
import org.netbeans.modules.bpel.nodes.actions.AddTerminationHandlerAction;
import org.netbeans.modules.bpel.nodes.actions.BpelNodeAction;
import org.netbeans.modules.bpel.nodes.actions.BpelNodeNewType;
import org.netbeans.modules.bpel.nodes.actions.GoToSourceAction;
import org.netbeans.modules.bpel.nodes.actions.InsertElseIfAfterAction;
import org.netbeans.modules.bpel.nodes.actions.InsertElseIfBeforeAction;
import org.netbeans.modules.bpel.nodes.actions.MoveElseIfLeftAction;
import org.netbeans.modules.bpel.nodes.actions.MoveElseIfRightAction;
import org.netbeans.modules.bpel.nodes.actions.AddVariableAction;
import org.netbeans.modules.bpel.nodes.actions.AddWsdlImportAction;
import org.netbeans.modules.bpel.nodes.actions.DeleteAction;
import org.netbeans.modules.bpel.nodes.actions.GoToCorrelationSetContainerSourceAction;
import org.netbeans.modules.bpel.nodes.actions.GoToVariableContainerSourceAction;
import org.netbeans.modules.bpel.nodes.actions.OpenInEditorAction;
import org.netbeans.modules.bpel.nodes.actions.OpenPartnerLinkInEditor;
import org.netbeans.modules.bpel.nodes.actions.ShowPropertyEditorAction;
import org.netbeans.modules.bpel.nodes.dnd.BpelEntityPasteType;
import org.netbeans.modules.bpel.nodes.dnd.BpelNodeTransferable;
import org.netbeans.modules.bpel.nodes.dnd.Util;
import org.netbeans.modules.bpel.properties.PropertyType;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.modules.soa.ui.SoaUiUtil;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.netbeans.modules.xml.xam.ui.actions.GoToAction;
import org.openide.ErrorManager;
import org.openide.actions.NewAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ReorderAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.PasteType;

/**
 * This class represents the base class for BPEL related nodes.
 * <p>
 * The PropertyNodeFactory class is implied that the derived nodes has
 * at least one constructor with the specific parameters.
 *
 * @author nk160297
 */
public abstract class BpelNode<T>
        extends AbstractNode
        implements InstanceRef<T>, NodeTypeHolder<NodeType>
{
    
    // constants used in childs
    public static final String VARIABLE_EQ = " variable="; // NOI18N
    public static final String MESSAGE_EXCHANGE_EQ = " messageExchange="; // NOI18N
    public static final String PARTNER_LINK_EQ = " partnerLink="; // NOI18N
    public static final String OPERATION_EQ = " operation="; // NOI18N
    public static final String CREATE_INSTANCE_EQ = " createInstance="; // NOI18N
    public static final String FROM_VARIABLE_EQ = " fromVariable="; // NOI18N
    public static final String TO_VARIABLE_EQ = " toVariable="; // NOI18N
    public static final String PART_EQ = " part="; // NOI18N
    public static final String TYPE_EQ = " type="; // NOI18N
    public static final String QUERY_EQ = " query="; // NOI18N
    public static final String ELEMENT_EQ = " element="; // NOI18N
    public static final String FOR_PROPERTY_EQ = " forProperty="; // NOI18N
    public static final String EMPTY_STRING = "";
    public static final String MESSAGE_TYPE_EQ = " messageType=";
    public static final int MAX_CONTENT_NAME_LENGTH = 50;
    public static final String DOTS_SIGN = "...";
    public static final String DELIMITER = "/"; // NOI18N
    public static final String EQUAL_SIGN = "="; // NOI18N
    public static final String EXP_LABEL = "(exp)"; // NOI18N
    public static final String QUERY_LABEL = "(query)"; // NOI18N
    public static final String ENDPOINT_REFERENCE= "endpointReference"; // NOI18N
    public static final String WHITE_SPACE = " "; // NOI18N
    
    
    
    public static final Object UNKNOWN_OLD_VALUE = new Object();
    //
    private static final Map<ActionType, Action> ACTION_TYPE_MAP
            = new HashMap<ActionType, Action>();
    
    private DecorationProvider decorationProvider; 
    
    static {
        ACTION_TYPE_MAP.put(ActionType.REMOVE,SystemAction.get(DeleteAction.class));
        ACTION_TYPE_MAP.put(ActionType.DELETE_BPEL_EXT_FROM_WSDL
                ,SystemAction.get(DeleteBpelExtensibiltyWsdlRefAction.class));
        ACTION_TYPE_MAP.put(ActionType.DELETE_PROPERTY_ACTION
                ,SystemAction.get(DeletePropertyAction.class));
        ACTION_TYPE_MAP.put(ActionType.SHOW_POPERTY_EDITOR
                ,SystemAction.get(ShowPropertyEditorAction.class));
        ACTION_TYPE_MAP.put(ActionType.ADD_CATCH
                , SystemAction.get(AddCatchAction.class));
        ACTION_TYPE_MAP.put(ActionType.ADD_CATCH_ALL
                , SystemAction.get(AddCatchAllAction.class));
        ACTION_TYPE_MAP.put(ActionType.ADD_COMPENSATION_HANDLER
                , SystemAction.get(AddCompensationHandlerAction.class));
        ACTION_TYPE_MAP.put(ActionType.ADD_TERMINATION_HANDLER
                , SystemAction.get(AddTerminationHandlerAction.class));
        ACTION_TYPE_MAP.put(ActionType.ADD_ON_EVENT
                , SystemAction.get(AddOnEventAction.class));
        ACTION_TYPE_MAP.put(ActionType.ADD_ON_ALARM
                , SystemAction.get(AddOnAlarmAction.class));
        ACTION_TYPE_MAP.put(ActionType.ADD_ELSE
                , SystemAction.get(AddElseAction.class));
        ACTION_TYPE_MAP.put(ActionType.ADD_ELSE_IF
                , SystemAction.get(AddElseIfAction.class));
        ACTION_TYPE_MAP.put(ActionType.INSERT_ELSE_IF_AFTER
                , SystemAction.get(InsertElseIfAfterAction.class));
        ACTION_TYPE_MAP.put(ActionType.INSERT_ELSE_IF_BEFORE
                , SystemAction.get(InsertElseIfBeforeAction.class));
        ACTION_TYPE_MAP.put(ActionType.MOVE_ELSE_IF_LEFT
                , SystemAction.get(MoveElseIfLeftAction.class));
        ACTION_TYPE_MAP.put(ActionType.MOVE_ELSE_IF_RIGHT
                , SystemAction.get(MoveElseIfRightAction.class));
        ACTION_TYPE_MAP.put(ActionType.ADD_VARIABLE
                , SystemAction.get(AddVariableAction.class));
        ACTION_TYPE_MAP.put(ActionType.ADD_CORRELATION_SET
                , SystemAction.get(AddCorrelationSetAction.class));
        ACTION_TYPE_MAP.put(ActionType.ADD_PROPERTY
                , SystemAction.get(AddPropertyAction.class));
        ACTION_TYPE_MAP.put(ActionType.OPEN_IN_EDITOR
                , SystemAction.get(OpenInEditorAction.class));
        ACTION_TYPE_MAP.put(ActionType.OPEN_PL_IN_EDITOR
                , SystemAction.get(OpenPartnerLinkInEditor.class));
        ACTION_TYPE_MAP.put(ActionType.GO_TO_SOURCE
                , SystemAction.get(GoToSourceAction.class));
        ACTION_TYPE_MAP.put(ActionType.GO_TO_WSDL_SOURCE
                , SystemAction.get(GoToAction.class));
        ACTION_TYPE_MAP.put(ActionType.GO_TO_CORRSETCONTAINER_SOURCE
                , SystemAction.get(GoToCorrelationSetContainerSourceAction.class));
        ACTION_TYPE_MAP.put(ActionType.GO_TO_VARCONTAINER_SOURCE
                , SystemAction.get(GoToVariableContainerSourceAction.class));
        ACTION_TYPE_MAP.put(ActionType.GO_TO_MSG_EX_CONTAINER_SOURCE
                , SystemAction.get(GoToMessageExchangeContainerSourceAction.class));
        ACTION_TYPE_MAP.put(ActionType.GO_TO_TYPE_SOURCE
                , SystemAction.get(GoToTypeSourceAction.class));
        ACTION_TYPE_MAP.put(ActionType.ADD_MESSAGE_EXCHANGE
                , SystemAction.get(AddMessageExchangeAction.class));
        ACTION_TYPE_MAP.put(ActionType.ADD_NEWTYPES
                , SystemAction.get(NewAction.class));
        ACTION_TYPE_MAP.put(ActionType.MOVE_UP
                , SystemAction.get(MoveUpBpelEntityAction.class));
        ACTION_TYPE_MAP.put(ActionType.MOVE_DOWN
                , SystemAction.get(MoveDownBpelEntityAction.class));
        ACTION_TYPE_MAP.put(ActionType.MOVE_COPY_UP
                , SystemAction.get(MoveUpCopyAction.class));
        ACTION_TYPE_MAP.put(ActionType.MOVE_COPY_DOWN
                , SystemAction.get(MoveDownCopyAction.class));
        ACTION_TYPE_MAP.put(ActionType.PROPERTIES
                , SystemAction.get(PropertiesAction.class));
        ACTION_TYPE_MAP.put(ActionType.TOGGLE_BREAKPOINT
                , SystemAction.get(ToggleBreakpointAction.class));
        ACTION_TYPE_MAP.put(ActionType.CYCLE_MEX
                , SystemAction.get(FindMexPeerAction.class));
        ACTION_TYPE_MAP.put(ActionType.ADD_PROPERTY_TO_WSDL
                , SystemAction.get(AddPropertyToWsdlAction.class));
        ACTION_TYPE_MAP.put(ActionType.ADD_PROPERTY_ALIAS_TO_WSDL
                , SystemAction.get(AddPropertyAliasToWsdlAction.class));
        ACTION_TYPE_MAP.put(ActionType.ADD_PARTNER_LINK,
                  SystemAction.get(AddPartnerLinkAction.class));
        ACTION_TYPE_MAP.put(ActionType.SHOW_BPEL_MAPPER
                , SystemAction.get(ShowBpelMapperAction.class));
        ACTION_TYPE_MAP.put(ActionType.FIND_USAGES,
                  RefactoringActionsFactory.whereUsedAction());
//                  SystemAction.get(FindUsagesAction.class));
        ACTION_TYPE_MAP.put(ActionType.WRAP,
                  SystemAction.get(WrapAction.class));
        ACTION_TYPE_MAP.put(ActionType.ADD_FROM_PALETTE,
                  SystemAction.get(AddFromPaletteAction.class));
        ACTION_TYPE_MAP.put(ActionType.GO_TO_DIAGRAMM,
                  SystemAction.get(GoToDiagrammAction.class));
        ACTION_TYPE_MAP.put(ActionType.CHANGE_ORDER_ACTION,
                  SystemAction.get(ReorderAction.class));
        ACTION_TYPE_MAP.put(ActionType.ADD_WSDL_IMPORT,
                  SystemAction.get(AddWsdlImportAction.class));
        ACTION_TYPE_MAP.put(ActionType.ADD_SCHEMA_IMPORT,
                  SystemAction.get(AddSchemaImportAction.class));
    }
    
    private Object reference;
    private Synchronizer synchronizer = new Synchronizer();
    private String cachedName;
    private String cachedShortDescription;
    private String cachedHtmlDisplayName;
    private Object NAME_LOCK = new Object();
//    private BPELValidationListener validationListener;
    private ChangeValidationListener validationListener;
    private boolean isErrorBadged;
    private boolean isWarningBadged;
//    private boolean isDebuggerBadged;
//    private AnnotationType debuggerBadgeType;
    
    public BpelNode(T ref, Lookup lookup) {
        this(ref, Children.LEAF, lookup);
    }
    
    public BpelNode(T ref, Children children, Lookup lookup) {
        super(children
                ,new ExtendedLookup(lookup,BpelNavigatorLookupHint.getInstance())
                );
        setReference(ref);
        
        if (ref instanceof BpelEntity){
            BpelModel model = ((BpelEntity) ref).getBpelModel();
            
            assert model != null: "Can't create Node for orphaned Bpel Entity"; // NOI18N
           /// model.addEntityChangeListener(synchronizer);
            synchronizer.subscribe(model);
        }
        validationListener = new ValidationChangesListener();
        attachValidationController(validationListener);
        
        // TODO m
        // set nodeDescription property which is shown in property sheet help region
        setValue("nodeDescription", "");
        //set default tooltip provider if it is no in lookup 
        decorationProvider = (DecorationProvider)lookup.lookup(DecorationProvider.class);
        if (decorationProvider == null) {
            setDecorationProvider();
        }
    }
    
    public void setDecorationProvider(DecorationProvider decorationProvider) {
        this.decorationProvider = decorationProvider;
    }

    /**
     * Set default tooltip provider
     */
    protected void setDecorationProvider() {
        this.decorationProvider = new BpelNavigatorDecorationProvider(getNodeType(), getReference());
    }

    public DecorationProvider getDecorationProvider() {
        return decorationProvider;
    }

    protected boolean isValidationAnnotatedEntity(Component component) {
        Object reference = BpelNode.this instanceof ContainerBpelNode
                ? ((ContainerBpelNode)BpelNode.this).getContainerReference()
                : getReference();
        if (reference == null) {
            return false;
        }
        return reference.equals(component);
    }
    
    protected Validator.ResultType getValidationStatus(
            ValidationProxyListener vpl) 
    {
        assert vpl != null;
        Object ref = (this instanceof ContainerBpelNode)?
                    ((ContainerBpelNode) this).getContainerReference() :
                    getReference();
        Validator.ResultType resultType = vpl
                .getValidationStatusForElement(ref);

        return resultType;
    }

    /**
     * true means that validation status from child elements should be accounted
     */ 
    protected boolean isComplexValidationStatus() {
        return false;
    }
// TODO r | m    
//    private Validator.ResultType getValidationStatus(
//            ValidationProxyListener vpl, List<BpelEntity> children) 
//    {
//        assert vpl != null;
//        if (children == null || children.size() == 0) {
//            return null;
//        }
//        Validator.ResultType resultType = null;
//        
//        List<Validator.ResultType> resultTypes = 
//                new ArrayList<Validator.ResultType>();
//        Validator.ResultType tmpResult = null;
//        for (BpelEntity entity : children) {
//            tmpResult = vpl
//                .getValidationStatusForElement(entity);
//            if (tmpResult != null) {
//                resultTypes.add(tmpResult);
//            }
//        }
//
//        resultType = ValidationProxyListener.getPriorytestType(resultTypes);
//        
//        if (resultType == null) {
//            for (BpelEntity entity : children) {
//                if (entity instanceof BpelContainer) {
//                    resultType = getValidationStatus(vpl, entity.getChildren());
//                }
//                if (resultType != null) {
//                    break;
//                }
//            }
//        }
//        
//        return resultType;
//    }
//        
    /**
     *  Attach listener to validation changes.
     */
    private void attachValidationController(ChangeValidationListener listener) {
//        final ValidationProxyListener validationProxyListener =
//                ValidationProxyListener.getInstance(getLookup());
//        if (validationProxyListener == null) {
//            return;
//        }
        
        final ValidationProxyListener vpl = getValidationProxyListener();
        if (vpl == null) {
            return;
        }
        
        vpl.addChangeValidationListener(listener);
        Validator.ResultType resultType = getValidationStatus(vpl);
        
        if (resultType == null) {
            isErrorBadged = false;
            isWarningBadged = false;
            return;
        }
        switch (resultType) {
            case ERROR :
                isErrorBadged = true;
                isWarningBadged = false;
                break;
            case WARNING :
                isErrorBadged = false;
                isWarningBadged = true;
                break;
            default :
                isErrorBadged = false;
                isWarningBadged = false;
        }
        
//        isDebuggerBadged = vpl.getAnnotationStatusForElement(((this instanceof ContainerBpelNode)?
//                    ((ContainerBpelNode) this).getContainerReference() :
//                    getReference()));
        
    }
    
    private AnnotationType getDebuggerAnnotationType() {
        ValidationProxyListener vpl = getValidationProxyListener();
        String[] types = null;
        if (vpl != null ) {
            types = vpl.getAnnotationTypes(getReference());
        }
        
        if (types == null || types.length < 1) {
            return null;
        }
        
        AnnotationType tmpDebuggerBadgeType = null;
        boolean isCurrentPosition = false;
        for (int i = 0; i < types.length; i++) {
            if (AnnotationType.CURRENT_POSITION.
                    equals(AnnotationType.getAnnotationType(types[i]))) {
                isCurrentPosition = true;
            } else if (AnnotationType.BREAKPOINT.
                    equals(AnnotationType.getAnnotationType(types[i]))) {
                tmpDebuggerBadgeType = AnnotationType.BREAKPOINT;
            } else if (tmpDebuggerBadgeType == null
                    && AnnotationType.DISABLED_BREAKPOINT.
                    equals(AnnotationType.getAnnotationType(types[i]))) {
                tmpDebuggerBadgeType = AnnotationType.DISABLED_BREAKPOINT;
            }
        }
        
        if (isCurrentPosition && tmpDebuggerBadgeType == null) {
            tmpDebuggerBadgeType = AnnotationType.CURRENT_POSITION;
        } else if (isCurrentPosition && tmpDebuggerBadgeType != null) {
            switch (tmpDebuggerBadgeType) {
                case BREAKPOINT :
                    tmpDebuggerBadgeType = AnnotationType.CURRENT_BREAKPOINT;
                    break;
                case DISABLED_BREAKPOINT :
                    tmpDebuggerBadgeType = AnnotationType.CURRENT_DISABLED_BREAKPOINT;
                    break;
            }
        }
        return tmpDebuggerBadgeType;
    }
    
    
    /**
     *  Dettach listener to validation changes.
     */
    private void detachValidationController(ChangeValidationListener listener) {
        ValidationProxyListener vpl = getValidationProxyListener();
        if (vpl != null) {
            vpl.removeChangeValidationListener(listener);
        }
    }
    
    private ValidationProxyListener getValidationProxyListener() {
        Lookup lookup = getLookup();
        if (lookup == null) {
            return null;
        }
        return (ValidationProxyListener)lookup.lookup(ValidationProxyListener.class);
    }
    
    /**
     * The reference to an object which the node represents.
     */
    public T getReference() {
        T ref = null;
        if (reference instanceof BpelSafeReference) {
            ref = (T)((BpelSafeReference)reference).getBpelObject();
        } else {
            ref = (T)reference;
        }
        
        if (ref instanceof BpelEntity) {
            warnIfBpelModelUnLocked((BpelEntity)ref);
        }
        
        return ref;
    }
    
    public Object getAlternativeReference() {
        return null;
    }
    
    private void warnIfBpelModelUnLocked(BpelEntity entity) {
        assert entity != null;
//        if (entity == null) {
//            return;
//        }
        
        BpelModel model = entity.getBpelModel();
        if (model == null) {
            return;
        }
        
        if (! model.isIntransaction()) {
//            ErrorManager.getDefault()
//                .notify(new IllegalStateException("The bpel model didn't lock"));
//            System.out.println("The bpel model didn't lock");
        }
    }
    
    /**
     * Should be overriden for complex nodes
     * @return true if event require update of the current node
     */
    protected boolean isEventRequreUpdate(ChangeEvent event) {
        T curEntity = getReference();
        return (event != null
                && curEntity != null
                && event.getParent() == curEntity) || isRequireSpecialUpdate(event, curEntity);
    }
    
    protected boolean isRequireSpecialUpdate(ChangeEvent event, Object currentEntity) {
        BpelEntity sourceEntity = null;
        if (event.getClass() == EntityRemoveEvent.class) {
            sourceEntity = ((EntityRemoveEvent)event).getOldValue();
        } else if (event.getClass() == EntityUpdateEvent.class) {
//            sourceEntity = ((EntityUpdateEvent)event).getOldValue();
            sourceEntity = ((EntityUpdateEvent)event).getNewValue();
        } else if (event.getClass() == EntityInsertEvent.class) {
            sourceEntity = ((EntityInsertEvent)event).getValue();
        }
        
        if (sourceEntity == null
                || sourceEntity.getElementType() != Scope.class) {
            return false;
        }
        
        BpelEntity scopeParent = event.getParent();
        
        if (!(scopeParent instanceof BaseScope)) {
            scopeParent = org.netbeans.modules.bpel.nodes.navigator.Util.getUpClosestBaseScope(scopeParent);
        }
        
        if (scopeParent == null) {
            return false;
        }
        
        if (scopeParent.getElementType() == Process.class) {
            return getNodeType().equals(NodeType.VARIABLE_CONTAINER)
            || getNodeType().equals(NodeType.CORRELATION_SET_CONTAINER);
        }
        
        if (scopeParent == currentEntity) {
            return true;
        }
        
        return false;
    }
    
    /**
     * This method is called each time when an event is come.
     * It is intended to be overridden only by the subclasses which
     * require update name for unusual events.
     */ 
    private void updateComplexNames(ChangeEvent event) {
        if (isRequireNameUpdate(event)) {
            updateName();
        }
    }
    
    protected boolean isRequireNameUpdate(ChangeEvent event) {
        if (event instanceof PropertyUpdateEvent) {
            return true;
        }
        return false;
    }

    
    /**
     * This method is called each time when an event is come.
     * It is intended to be overridden only by the subclasses which
     * require specific processing to update their complex properties.
     * It generally intended to update the calculated properties.
     *
     * Don't update all properties here because most of them
     * can be processed automatically with the help of other mechanism.
     * See updateElementProperty and updateAttributeProperty methods.
     */
    protected void updateComplexProperties(ChangeEvent event) {
        // DO NOTHING HERE
    }
    
    private Image getWarningBadge() {
        return NodeBadges.WARNING.IMAGE;
    }
    
    private synchronized Image getErrorBadge() {
        return NodeBadges.ERROR.IMAGE;
    }
    
    private synchronized Image getBreakpointBadge() {
        return NodeBadges.BREAKPOINT.IMAGE;
    }
    
    private synchronized Image getCurrentBreakpointBadge() {
        return NodeBadges.CURRENT_BREAKPOINT.IMAGE;
    }
    
    private synchronized Image getDisabledBreakpointBadge() {
        return NodeBadges.DISABLED_BREAKPOINT.IMAGE;
    }
    
    private synchronized Image getCurrentDisabledBreakpointBadge() {
        return NodeBadges.CURRENT_DISABLED_BREAKPOINT.IMAGE;
    }
    
    private synchronized Image getCurrentPositionBadge() {
        return NodeBadges.CURRENT_POSITION.IMAGE;
    }
    
    private Image getDebuggerBadgedImage(Image nodeImage ) {
        return SoaUiUtil.getBadgedIcon(
                nodeImage,getBreakpointBadge(), 9, 9);
    }
    
    private Image getAnnotationIcon(Image nodeImage, AnnotationType type) {
        switch (type) {
            case BREAKPOINT :
                return SoaUiUtil.getBadgedIcon(
                        nodeImage,getBreakpointBadge(), 9, 9);
            case CURRENT_BREAKPOINT :
                return SoaUiUtil.getBadgedIcon(
                        nodeImage,getCurrentBreakpointBadge(), 9, 9);
            case DISABLED_BREAKPOINT :
                return SoaUiUtil.getBadgedIcon(
                        nodeImage,getDisabledBreakpointBadge(), 9, 9);
            case CURRENT_DISABLED_BREAKPOINT :
                return SoaUiUtil.getBadgedIcon(
                        nodeImage,getCurrentDisabledBreakpointBadge(), 9, 9);
            case CURRENT_POSITION :
                return SoaUiUtil.getBadgedIcon(
                        nodeImage,getCurrentPositionBadge(), 9, 9);
        }
        
        return nodeImage;
    }
    
    public Image getIcon(int type) {
        Image vrgnImage = getNodeType().getImage();
        AnnotationType annotationDebugType = getDebuggerAnnotationType();
//        System.out.println("getted Debug annotationType: "+annotationDebugType);
        if (annotationDebugType != null) {
            vrgnImage = getAnnotationIcon(vrgnImage, annotationDebugType);
        }
        
        vrgnImage = vrgnImage == null ? getNodeType().getImage() : vrgnImage;
        
        if (isErrorBadged) {
            return getErrorBadge() != null
                    ? SoaUiUtil.getBadgedIcon(
                    vrgnImage/*getNodeType().getImage()*/,getErrorBadge())
                    : vrgnImage;//getNodeType().getImage();
        } else if (isWarningBadged) {
            return getWarningBadge() != null
                    ? SoaUiUtil.getBadgedIcon(
                    vrgnImage/*getNodeType().getImage()*/,getWarningBadge())
                    : vrgnImage;//getNodeType().getImage();
        }
        
        
        return vrgnImage;//getNodeType().getImage();
    }
    
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
    protected void setReference(T newRef){
        if (newRef instanceof BpelEntity) {
            this.reference = new BpelSafeReference((BpelEntity)newRef);
        } else {
            this.reference = newRef;
        }
    }
    
    public abstract NodeType getNodeType();
    
    protected String getNameImpl(){
        Object ref = getReference();
        String name = null;
        if (ref != null) {
            if (ref instanceof Named) {
                name = ((Named)ref).getName();
            }
        }
        return (name != null) ? name : "";
    }
    
    public String getName() {
        /**
         * HACK to avoid deadlocks in navigator which uses separate thread to
         * deal with selection. Changing selection enters Children.MUTEX and
         * tries to get access to BPEL model lock via BpelEntity.getName();
         *
         * BTW, this caching may also improve the perfomance,
         * ... and cause a lot of bugs
         *
         * Name cache is cleared in updateName method, which is called when
         * ANY attribute of BPEL entity changes.
         **/
        
        synchronized(NAME_LOCK) {
            if (cachedName == null){
                cachedName = getNameImpl();
            }
            
            return cachedName;
        }
    }
    
    public String getDisplayName() {
        String instanceName = getName();
        return ((instanceName == null || instanceName.length() == 0)
        ? "" : instanceName + " ") +
                "[" + getNodeType().getDisplayName() + "]"; // NOI18N
    }
    
    public String getHtmlDisplayName() {
        synchronized (NAME_LOCK) {
            if (cachedHtmlDisplayName == null) {
                cachedHtmlDisplayName = getImplHtmlDisplayName();
            }
        }
        
        return cachedHtmlDisplayName;
    }
    
    protected String getImplHtmlDisplayName() {
        String htmlDisplayName = getName();
        htmlDisplayName = htmlDisplayName != null && htmlDisplayName.length() > 0 ?
            htmlDisplayName : getNodeType().getDisplayName();
        
        return org.netbeans.modules.bpel.editors.api.utils.Util.getCorrectedHtmlRenderedString(htmlDisplayName);
    }
    
    protected String getImplShortDescription() {
//        String instanceName = getName();
//        return NbBundle.getMessage(BpelNode.class,
//                "LBL_SHORT_TOOLTIP_HTML_TEMPLATE", // NOI18N
//                getNodeType().getDisplayName(), 
//                instanceName); // NOI18N
//        return getDisplayName();
       
        return decorationProvider == null 
                ? EMPTY_STRING 
                : decorationProvider.getTooltip(getNodeType(),getReference());
    }
    
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
    
    protected void updateProperty(PropertyType propertyType) {
        updateProperty(propertyType.toString());
    }
    
    protected void updateProperty(String propertyName) {
        Property prop = PropertyUtils.lookForPropertyByName(this, propertyName);
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
    
    public void updateShortDescription() {
        synchronized(NAME_LOCK){
            cachedShortDescription = null;
        }
        
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
                    ErrorManager.getDefault().notify(ex);
                }
            }
        }
    }
    
    public void updateAttributeProperty(String attributeName) {
        Property prop = PropertyUtils.lookForPropertyByBoundedAttribute(
                this, attributeName);
        if (prop != null) {
            String propName = prop.getName();
            try {
                Object newPropValue = prop.getValue();
                firePropertyChange(propName, UNKNOWN_OLD_VALUE, newPropValue);
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        
    }
    
    public void updateElementProperty(Class elementClass) {
        Property prop = PropertyUtils.lookForPropertyByBoundedElement(
                this, elementClass);
        if (prop != null) {
            String propName = prop.getName();
            try {
                Object newPropValue = prop.getValue();
                firePropertyChange(propName, UNKNOWN_OLD_VALUE, newPropValue);
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }
    
    
    public boolean isModelReadOnly() {
        Object ref = getReference();
        if (ref == null) return true;
        if (!(ref instanceof DocumentComponent)) return true;
        Model model = ((DocumentComponent) ref).getModel();
        if (model == null) return true;
        return !XAMUtils.isWritable(model);
    }
    
    public boolean canDestroy() {
        DeleteAction action = getActualDeleteAction()/*SystemAction.get(DeleteAction.class)*/;
        return action != null && action.enable(new Node[] {this});
    }
    
    public void destroy() throws IOException {
        
        T ref = getReference();
        
//        DeleteAction action = (DeleteAction)SystemAction.get(DeleteAction.class);
        DeleteAction action = getActualDeleteAction();
        if (action != null) {
            action.performAction(new Node[] {this});
        }
        
        if (ref instanceof BpelEntity){
            BpelModel model = ((BpelEntity) ref).getBpelModel();
            model.removeEntityChangeListener(synchronizer);
        }
        
        if (validationListener != null) {
            detachValidationController(validationListener);
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
    
    private void unsubscribedFromAndDestroy(BpelModel bpelModel) {
        bpelModel.removeEntityChangeListener(synchronizer);
        //
        if (validationListener != null) {
            detachValidationController(validationListener);
        }
        //
        try {
            super.destroy();
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    protected boolean isChildrenAllowed() {
        boolean isChildrenAllowed = true; // allowed by default
        NodesTreeParams treeParams =
                (NodesTreeParams)getLookup().lookup(NodesTreeParams.class);
        if (treeParams != null) {
            Class<? extends Node>[] typeArr = treeParams.getLeaftNodeClasses();
            if (typeArr != null) {
                for (Class<? extends Node> leafNodeType : typeArr) {
                    if (leafNodeType.equals(this.getClass())) {
                        isChildrenAllowed = false;
                        break;
                    }
                }
            }
        }
        return isChildrenAllowed;
    }
    
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        
        //
        if (obj instanceof BpelNode) {
            return this.getNodeType().equals(((BpelNode) obj).getNodeType()) &&
                    this.reference.equals(((BpelNode) obj).reference);
        }
        //
        return false;
    }
    
    public Action getPreferredAction() {
        Action preferedAction = getDefaultPreferedAction();
        return preferedAction != null ? preferedAction : super.getPreferredAction();
    }

    /**
     * If there is no PreffredeActionProvider in lookup then getPreffredAction
     * invokes this method.
     */
    protected Action getDefaultPreferedAction() {
        Action[] actions = getActions(true);
        if (actions == null || actions.length < 1) {
            return null;
        }
        
        for (Action elem : actions) {
            if (elem instanceof ShowBpelMapperAction) {
                return elem;
            }
            
            if (elem instanceof ShowPropertyEditorAction) {
                return elem;
            }
        }
        
        return null;
    }
    
    private class Synchronizer extends ChangeEventListenerAdapter{
        public Synchronizer() {
        }
        
        
        private ChangeEventListener listener;
        
        private void subscribe(BpelModel model) {
            listener = new DelegatingChangeEventListener(this);
            model.addEntityChangeListener(listener);
            
        }

        
        
        private void reloadChildren() {
            
            Children children = getChildren();
            if (children instanceof ReloadableChildren) {
                ((ReloadableChildren)children).reload();
            } else if (BpelNode.this instanceof ReloadableChildren) {
                ((ReloadableChildren) BpelNode.this).reload();
            }
        }
        
        public void notifyEntityRemoved(EntityRemoveEvent event) {
            BpelEntity entity = event.getOutOfModelEntity();
            //
            T ref = getReference();
            if (ref == null) {
                //
                // the referenced element already removed
                //
                BpelModel bpelModel = entity.getBpelModel();
                unsubscribedFromAndDestroy(bpelModel);
            } else {
                if (BpelNode.this.isEventRequreUpdate(event)) {
                    reloadChildren();
                }
                
                if (event.getOutOfModelEntity() instanceof Documentation 
                        && ref.equals(event.getParent())) 
                {
                    updateShortDescription();
                }
            }
            //
            // Perform update processing of complex ptoperties
            updateComplexProperties(event);
            updateComplexNames(event);
        }
        
        public void notifyPropertyRemoved(PropertyRemoveEvent event) {
            BpelEntity entity = event.getParent();
            

            T ref = getReference();
            if (ref == null) {
                //
                // the referenced element already removed
                //
                BpelModel bpelModel = entity.getBpelModel();
                unsubscribedFromAndDestroy(bpelModel);
            } else {
                if (BpelNode.this.isEventRequreUpdate(event)) {
                    reloadChildren();
                }
            }
            //
            // Perform update processing of complex ptoperties
            updateComplexProperties(event);
            updateComplexNames(event);
        }
        
        public void notifyPropertyUpdated(PropertyUpdateEvent event) {
            if (BpelNode.this.isEventRequreUpdate(event)) {
                String attributeName = event.getName();
                updateAttributeProperty(attributeName);
                updateName();
                //
                // Check if the property has the List type
                Object value = event.getNewValue();
                if (value == null) {
                    value = event.getOldValue();
                }
                if (value != null && value instanceof List) {
                    reloadChildren();
                }
            }
            //
            // Check that the property is the content of an entity
            // which owned by the node's entity.
            if (ContentElement.CONTENT_PROPERTY.equals(event.getName())) {
                BpelEntity parentEntity = event.getParent();
                //
                T curEntity = getReference();
                if (curEntity != null && parentEntity != null &&
                        parentEntity.getParent() == curEntity) {
                    updateElementProperty(parentEntity.getClass());
                    if (parentEntity instanceof Documentation) {
                        updateShortDescription();
                    }
                }
            }
            //
            // Perform update processing of complex ptoperties
            updateComplexProperties(event);
        }
        
        public void notifyArrayUpdated(ArrayUpdateEvent event) {
            if (BpelNode.this.isEventRequreUpdate(event)) {
                reloadChildren();
            }
            //
            // Perform update processing of complex ptoperties
            updateComplexProperties(event);
            updateComplexNames(event);
        }
        
        public void notifyEntityUpdated(EntityUpdateEvent event) {
            if (BpelNode.this.isEventRequreUpdate(event)) {
                BpelEntity entity = event.getNewValue();
                if (entity == null) {
                    entity = event.getOldValue();
                }
                if (entity != null) {
                    updateElementProperty(entity.getClass());
                }
                //
                reloadChildren();
            }
            
            //
            // Perform update processing of complex ptoperties
            updateComplexProperties(event);
            updateComplexNames(event);
        }
        
        public void notifyEntityInserted(EntityInsertEvent event) {
            if (BpelNode.this.isEventRequreUpdate(event)) {
                BpelEntity entity = event.getValue();
                if (entity != null) {
                    updateElementProperty(entity.getClass());
                }
                
                if (entity instanceof Documentation) {
                    updateShortDescription();
                } else {
                    //
                    reloadChildren();
                }
            }
            //
            // Perform update processing of complex ptoperties
            updateComplexProperties(event);
            updateComplexNames(event);
        }

        
    }
    
    private class ValidationChangesListener implements ChangeValidationListener {
        // TODO r
//        public void validationUpdated(Validator.ResultItem updatedItem) {
//            for(Component component: updatedItem.getComponents()) {
//                Object reference = BpelNode.this instanceof ContainerBpelNode
//                        ? ((ContainerBpelNode)BpelNode.this).getContainerReference()
//                        : getReference();
//                if (reference == null) {
//                    return;
//                }
//                if (reference.equals(component)) {
//                    switch (updatedItem.getType()) {
//                        case ERROR :
//                            isErrorBadged = true;
//                            isWarningBadged = false;
//                            fireIconChange();
//                            fireOpenedIconChange();
//                            break;
//                        case WARNING :
//                            isErrorBadged = false;
//                            isWarningBadged = true;
//                            fireIconChange();
//                            fireOpenedIconChange();
//                            break;
//                        default :
//                            isErrorBadged = false;
//                            isWarningBadged = false;
//                            fireIconChange();
//                            fireOpenedIconChange();
//                    }
//                }
//            }
//        }
//
//        public void validationRemoved(Validator.ResultItem updatedItem) {
//            for(Component component: updatedItem.getComponents()) {
//                Object reference = getReference();
//                if (reference == null) {
//                    return;
//                }
//                if (reference.equals(component)) {
//                    isErrorBadged = false;
//                    isWarningBadged = false;
//                    fireIconChange();
//                    fireOpenedIconChange();
//                }
//            }
//        }
        public void validationUpdated(Component component, Validator.ResultType resultType) {
            if (isValidationAnnotatedEntity(component)) {

                if (isComplexValidationStatus()) {
                    resultType = getValidationStatus(getValidationProxyListener());
                }
                updateValidationIcons(resultType);
            }
        }
        
        private void updateValidationIcons(Validator.ResultType resultType) {
            if (resultType == null) {
                isErrorBadged = false;
                isWarningBadged = false;
                fireIconChange();
                fireOpenedIconChange();
                return;
            }
            
            switch (resultType) {
                case ERROR :
                    isErrorBadged = true;
                    isWarningBadged = false;
                    fireIconChange();
                    fireOpenedIconChange();
                    break;
                case WARNING :
                    isErrorBadged = false;
                    isWarningBadged = true;
                    fireIconChange();
                    fireOpenedIconChange();
                    break;
                default :
                    isErrorBadged = false;
                    isWarningBadged = false;
                    fireIconChange();
                    fireOpenedIconChange();
            }
        }
        
        public void validationRemoved(Component component) {
            if (isValidationAnnotatedEntity(component)) {
                Validator.ResultType resultType = null;
                if (isComplexValidationStatus()) {
                    resultType = getValidationStatus(getValidationProxyListener());
                } 
                updateValidationIcons(resultType);
            }
        }
        
        public void annotationAdded(UniqueId entity, String type) {
            if (!isMyAnnotattionEvent(entity)) {
                return;
            }
//            System.out.println("annotationAdded: type "+type);
            fireIconChange();
            fireOpenedIconChange();
        }
        
        public void annotationRemoved(UniqueId entity, String type) {
            if (!isMyAnnotattionEvent(entity)) {
                return;
            }
//            System.out.println("annotationRemoved: type "+type);
            fireIconChange();
            fireOpenedIconChange();
        }
        
        private boolean isMyAnnotattionEvent(UniqueId entity) {
            Object ref = getReference();
            if (ref instanceof BpelEntity && ((BpelEntity)ref).getUID().equals(entity)) {
                return true;
            }
            return false;
        }
    }
    
//    private class BpelSafeReference<T> {
//
//        private UniqueId myId;
//        private BpelModel model;
//
//        public BpelSafeReference(T bpelEntity) {
//
//            myId = ((BpelEntity)bpelEntity).getUID();
//            model = ((BpelEntity)bpelEntity).getBpelModel();
//        }
//
//        public UniqueId getId() {
//            return myId;
//        }
//
//        public T getBpelObject() {
//            BpelEntity bpelEntity = model.getEntity(myId);
//            return (T)bpelEntity;
//        }
//    }
    
    
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
    
//    protected Node.Property createProperty(PropertyType propType, ) {
//    }
    
    public Action[] getActions(boolean b) {
        Action[] actions = null;
        Object ref = getReference();
        if (ref instanceof BpelEntity) {
            BpelModel model = ((BpelEntity)ref).getBpelModel();
            if (model != null && BpelModel.State.VALID.equals(model.getState())) 
            {
                actions = createActionsArray();
            }
        } 
        
        return actions == null ? super.getActions(b) : actions;
    }
    
    protected ActionType[] getActionsArray() {
        List<ActionType> actionList = new ArrayList<ActionType>();
        T ref = getReference(); 
        
        if (ref instanceof Activity) {
            actionList.add(ActionType.ADD_FROM_PALETTE);
            if (!(ref instanceof Process)) {
                actionList.add(ActionType.WRAP);
            }
            actionList.add(ActionType.SEPARATOR);
        }
        if (ref instanceof CompositeActivity) {
            actionList.add(ActionType.CHANGE_ORDER_ACTION);
            actionList.add(ActionType.SEPARATOR);
        }
        
        actionList.add(ActionType.GO_TO_SOURCE);
        if (ref instanceof Activity) {
            actionList.add(ActionType.GO_TO_DIAGRAMM);
        }
        if (ref instanceof Activity) {
            actionList.add(ActionType.SEPARATOR);
            actionList.add(ActionType.MOVE_UP);
            actionList.add(ActionType.MOVE_DOWN);
            actionList.add(ActionType.SEPARATOR);
            actionList.add(ActionType.TOGGLE_BREAKPOINT);
        }
        actionList.add(ActionType.SEPARATOR);
        actionList.add(ActionType.REMOVE);
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
    
    protected Action[] createActionsArray() {
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
    
    
    public boolean canCopy() {
        return true;
    }
    
    
    public boolean canCut() {
        return true;
    }
    
    
    public Transferable clipboardCopy() throws IOException {
        try {
            return new BpelNodeTransferable(this);
        } catch (ClassNotFoundException e) {
            Exception ioe = new IOException();
            throw (IOException) ErrorManager.getDefault().annotate(ioe,e);
        }
    }
    
    
    public Transferable clipboardCut() throws IOException {
        try {
            return new BpelNodeTransferable(this);
        } catch (ClassNotFoundException e) {
            Exception ioe = new IOException();
            throw (IOException) ErrorManager.getDefault().annotate(ioe,e);
        }
    }
    
    protected void createPasteTypes(Transferable t, List/*<PasteType>*/ list) {
        List<BpelEntityPasteType> supportedPTs = getBpelPasteTypes(t);
        if (supportedPTs != null && supportedPTs.size() > 0) {
            list.addAll(supportedPTs);
        }
    }
    
    public final List<BpelEntityPasteType> getBpelPasteTypes(Transferable t) {
        List<BpelEntityPasteType> pasteTypes = new ArrayList<BpelEntityPasteType>();
        BpelNode transfBpelNode = Util.getTransferableBpelNode(t);
        if (transfBpelNode != null
                && transfBpelNode != this
                && isDropNodeInstanceSupported(transfBpelNode)) {
            List<BpelEntityPasteType> supportedPTs = createSupportedPasteTypes(transfBpelNode);
            if ( supportedPTs != null && supportedPTs.size() > 0) {
                pasteTypes.addAll(supportedPTs);
            }
        }
//        System.out.println("isPasteSupported: "+(pasteTypes.size() > 0) ); // NOI18N
        return pasteTypes;
    }
    
// default implementation
// other nodes should override it
// all checking rules should be implemented inside isDropNodeInstanceSupported
    /*
     * @return list of the supported PasteTypes
     */
    public List<BpelEntityPasteType> createSupportedPasteTypes(BpelNode childNode) {
        return null;
    }
    
// default checking rule - can't be overriden
// and could be used as the first checking rule if neccessary
    public final boolean isDropNodeSupported(BpelNode childNode) {
        if (childNode == null) {
            return false;
        }
        
        if (childNode.getReference() instanceof BpelEntity
                && getReference() instanceof BpelEntity) {
            BpelEntity childRefObj = (BpelEntity)childNode.getReference();
            BpelEntity parentRefObj = (BpelEntity)getReference();
            if (parentRefObj.getUID().
                    equals(childRefObj.getUID())
                    || ( childRefObj.getParent() != null
                    && parentRefObj.getUID().equals(
                    childRefObj.getParent().getUID())
                    )
                    ) {
                return false;
            } else {
                // check is drop node is parent node for this
                BpelEntity thisParentRefObj = parentRefObj == null ? null
                        : parentRefObj.getParent();
                while(thisParentRefObj != null) {
                    if (thisParentRefObj.getUID().
                            equals(childRefObj.getUID())) {
                        return false;
                    }
                    thisParentRefObj = thisParentRefObj.getParent();
                }
                
                return true;
            }
        }
        
        return false;
    }
    
    protected boolean isDropNodeInstanceSupported(BpelNode childNode) {
        if (!isDropNodeSupported(childNode)) {
            return false;
        }
        // here should be additional checks
        return false;
    }

//    public Transferable drag() throws IOException {
//        returnclipboardCopy();
//    }
//    
  /*
   * @param transferable transferable object
   * @param action the drag'n'drop action
   * @param i0 index between children the drop occured at or -1 if not specified
   *
   */
    public PasteType getDropType(Transferable transferable, int action, int i0) {
        List<BpelEntityPasteType> pasteTypes = getBpelPasteTypes(transferable);
        if (pasteTypes == null || pasteTypes.size() == 0) {
            return null;
        }
        
        for (BpelEntityPasteType elem : pasteTypes) {
            if (elem.isSupportedDnDOperations(action) && elem.isSupportedChildIndex(i0)) {
                elem.setDndAction(action);
                elem.setPlaceIndex(i0);
                return elem;
            }
        }
        return null;
    }
    
    /**
     * @return the new types that can be created in this node
     */
    public NewType[] getNewTypes() {
        List<BpelNodeAction> actions = getAddActions();
        if (actions == null || actions.size() < 1) {
            return super.getNewTypes();
        }
        NewType[] newTypes = new NewType[actions.size()];
        for (int i = 0; i < newTypes.length; i++) {
            newTypes[i] = new BpelNodeNewType(actions.get(i), this);
        }
        return newTypes;
    }
    
    public List<BpelNodeAction> getAddActions() {
        ActionType[] actions = getAddActionArray();
        if (actions == null || actions.length == 0) {
            return null;
        }
        List<BpelNodeAction> resultAddActions = new ArrayList<BpelNodeAction>();
        for (ActionType elem : actions) {
            if (elem != null) {
                Action action = createAction(elem);
                if (action != null
                        && action instanceof BpelNodeAction
                        && ((BpelNodeAction)action).enable(new Node[] {this})) {
                    resultAddActions.add((BpelNodeAction)action);
                }
            }
        }
        
        return resultAddActions;
    }
    
    public String getHelpId() {
        return null;
    }
    
    public HelpCtx getHelpCtx() {
        return getHelpId() == null ? null : new HelpCtx(getHelpId());
    }
    
    public java.awt.Component getCustomizer() {
        return getCustomizer(CustomNodeEditor.EditingMode.EDIT_INSTANCE);
    }
    
    public java.awt.Component getCustomizer(CustomNodeEditor.EditingMode editingMode) {
        return null;
    }
    
    public static class NameComparator implements Comparator<Node> {
        public int compare(Node o1, Node o2) {
            if (o1 == null || o2 == null) {
                if (o1 == null && o2 == null) {
                    return 0;
                } else if (o1 == null) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                return o1.getName().compareTo(o2.getName());
            }
        }
    }
    
    public static class DisplayNameComparator implements Comparator<Node> {
        public int compare(Node o1, Node o2) {
            if (o1 == null || o2 == null) {
                if (o1 == null && o2 == null) {
                    return 0;
                } else if (o1 == null) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        }
    }
    
    public static class NodeTypeComparator extends BpelNode.DisplayNameComparator {
        
        private Class[] classesSortOrder;
        
        public NodeTypeComparator(final Class... classesSortOrder) {
            this.classesSortOrder = classesSortOrder;
        }
        
        public int compare(BpelNode o1, BpelNode o2) {
            if (o1 == null || o2 == null) {
                return super.compare(o1, o2);
            } else {
                Class c1 = o1.getClass();
                Class c2 = o2.getClass();
                if (c1.equals(c2)) {
                    return super.compare(o1, o2);
                } else {
                    return getSortOrderIndex(c1) - getSortOrderIndex(c2);
                }
            }
        }
        
        private int getSortOrderIndex(Class classObj) {
            for (int index = 0; index < classesSortOrder.length; index++) {
                if (classesSortOrder[index] == classObj) {
                    return index;
                }
            }
            //
            return classesSortOrder.length;
        }
    }
    
    public static final class NodeBadges {
        public static final class WARNING {
            private static final String WARNING_BADGE =
                    "org/netbeans/modules/bpel/editors/resources/badge_warning.png"; //NOI18N
            public static final Image IMAGE =
                    Utilities.loadImage(WARNING_BADGE);
        }
        
        public static final class ERROR {
            private static final String ERROR_BADGE =
                    "org/netbeans/modules/bpel/editors/resources/badge_error.png"; //NOI18N
            public static final Image IMAGE =
                    Utilities.loadImage(ERROR_BADGE);
        }
        
        
        public static final class BREAKPOINT {
            private static final String BREAKPOINT_BADGE =
                    "org/netbeans/modules/bpel/editors/resources/badge_breakpoint.png"; //NOI18N
            public static final Image IMAGE =
                    Utilities.loadImage(BREAKPOINT_BADGE);
        }
        
        public static final class CURRENT_POSITION {
            private static final String CURRENT_POSITION_BADGE =
                    "org/netbeans/modules/bpel/editors/resources/badge_current_position.png"; //NOI18N
            public static final Image IMAGE =
                    Utilities.loadImage(CURRENT_POSITION_BADGE);
        }
        
        public static final class CURRENT_DISABLED_BREAKPOINT {
            private static final String CURRENT_DISABLED_BREAKPOINT_BADGE =
                    "org/netbeans/modules/bpel/editors/resources/badge_current_disabled_breakpoint.png"; //NOI18N
            public static final Image IMAGE =
                    Utilities.loadImage(CURRENT_DISABLED_BREAKPOINT_BADGE);
        }
        
        public static final class DISABLED_BREAKPOINT {
            private static final String DISABLED_BREAKPOINT_BADGE =
                    "org/netbeans/modules/bpel/editors/resources/badge_disabled_breakpoint.png"; //NOI18N
            public static final Image IMAGE =
                    Utilities.loadImage(DISABLED_BREAKPOINT_BADGE);
        }
        
        public static final class CURRENT_BREAKPOINT {
            private static final String CURRENT_BREAKPOINT_BADGE =
                    "org/netbeans/modules/bpel/editors/resources/badge_current_breakpoint.png"; //NOI18N
            public static final Image IMAGE =
                    Utilities.loadImage(CURRENT_BREAKPOINT_BADGE);
        }
    }
}
