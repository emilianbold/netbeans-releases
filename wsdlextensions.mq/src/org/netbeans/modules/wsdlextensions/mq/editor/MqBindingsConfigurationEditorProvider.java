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
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.wsdlextensions.mq.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;

import org.netbeans.modules.wsdlextensions.mq.MQOperation;
import org.netbeans.modules.wsdlextensions.mq.MQQName;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorProvider;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 * MQ bindings editing support in the NetBeans Composite Application
 * Service Assembly (CASA) editor.
 *
 * @author Noel.Ang@sun.com
 * @see MqBindingsConfigurationEditor
 */
public final class MqBindingsConfigurationEditorProvider
        extends ExtensibilityElementConfigurationEditorProvider {

    // Editor caches
    private final Map<WSDLModel, MqBindingsConfigurationEditor> mModelEditors =
            new HashMap<WSDLModel, MqBindingsConfigurationEditor>();

    private final Map<Operation, MqBindingsConfigurationEditor> mModelOpEditors =
            new HashMap<Operation, MqBindingsConfigurationEditor>();

    private volatile WSDLComponent wsdlComponent;
    private volatile String direction;
    
    /**
     * Return the namespace for which this plugin provides components.
     *
     * @return String namespace corresponding to BC's schema file.
     */
    public String getNamespace() {
        return MQQName.MQ_NS_URI;
    }

    /**
     * Provides component at current context using qname and/or wsdlcomponent.
     * Return an appropriate EditorComponent corresponding to the qname and/or
     * wsdlcomponent.
     *
     * @param qname QName of the element in the wsdl; optional.
     * @param component WSDLComponent in the wsdl.
     *
     * @return An ExtensibilityElementConfigurationEditorComponent populated
     *         with the information contained in the component's WSDL model.
     * @throws NullPointerException if component is null.
     */
    public synchronized ExtensibilityElementConfigurationEditorComponent getComponent(
            QName qname,
            WSDLComponent component
    ) {
        if (component == null) {
            throw new NullPointerException("component");
        }
        return getComponent(component, null);
    }

    /**
     * The next two methods are interlinked. It allows for binding configuration
     * per operation. This is the entry point from casa editor, the skeleton
     * template is loaded from the template.xml at this point. It is recommended
     * that the port and the link direction is cached and reused when the
     * getComponent(Operation operation) is called.
     *
     * @param wsdlComponent WSDL document component that is the focus of the
     * editing.
     * @param linkDirection The association denoted in the CASA editor that
     * describes this component's relationship to whatever object it is
     * connected.
     *
     * @throws NullPointerException if either of the arguments are null.
     * @see {ExtensibilityElementConfigurationEditorComponent#BC_TO_BP_DIRECTION}
     * @see {ExtensibilityElementConfigurationEditorComponent#BP_TO_BC_DIRECTION}
     */
    @Override
    public synchronized void initOperationBasedEditingSupport(WSDLComponent wsdlComponent,
                                                              String linkDirection
    ) {
        if (wsdlComponent == null) {
            throw new NullPointerException("component");
        }
        if (linkDirection == null) {
            throw new NullPointerException("linkDirection");
        }
        if (!linkDirection.equals(
                ExtensibilityElementConfigurationEditorComponent.BC_TO_BP_DIRECTION
        ) && linkDirection.equals(
                ExtensibilityElementConfigurationEditorComponent.BP_TO_BC_DIRECTION
        )) {
            throw new IllegalArgumentException("linkDirection: " + linkDirection
            );
        }
        this.wsdlComponent = wsdlComponent;
        direction = linkDirection;
    }

    /**
     * Return the component for the operation. This can be called multiple
     * times, so it is recommended to cache it.
     *
     * @param operation The operation in the current WSDL document context that
     * the produced editor is focused on. The WSDL context is provided by the
     * most recent, preceeding call to {@link #initOperationBasedEditingSupport}.
     *
     * @return An ExtensibilityElementConfigurationEditorComponent to modify the
     *         indicated operation, or null if the operation cannot be resolved
     *         (such as when no WSDL context was previously provided).
     * @throws NullPointerException if operation is null.
     */
    @Override
    public synchronized ExtensibilityElementConfigurationEditorComponent getOperationBasedEditorComponent(
            Operation operation
    ) {
        if (operation == null) {
            throw new NullPointerException("operation");
        }
        return getComponent(wsdlComponent, operation);
    }

    /**
     * Called when OK is pressed in the dialog, commit all the panels related to
     * each operation in the operation list.
     *
     * @param operations List of operations.
     *
     * @return true if successfully committed
     */
    @Override
    public synchronized boolean commitOperationBasedEditor(ArrayList<Operation> operations) {
        boolean allCommitsSuccessful = true;

        for (Operation operation : operations) {
            ExtensibilityElementConfigurationEditorComponent editor =
                    mModelOpEditors.get(operation);
            if (editor == null) {
                // operation I never edited; something's wrong.
                allCommitsSuccessful = false;
                break;
            }
            if (!editor.commit()) {
                allCommitsSuccessful = false;
                break;
            }
        }
        return allCommitsSuccessful;
    }

    /**
     * Called when dialog is cancelled/closed, rollback all the panels related
     * to each operation in the operation list. Can be used to cleanup.
     *
     * @param operations List of operations.
     */
    @Override
    public synchronized void rollbackOperationBasedEditor(ArrayList<Operation> operations) {
        for (Operation operation : operations) {
            ExtensibilityElementConfigurationEditorComponent editor =
                    mModelOpEditors.get(operation);
            if (editor != null) {
                editor.rollback();
            }
        }
    }

    /**
     * If configuration is supported on the extensibility element, return true,
     * else false.
     *
     * @param qname qname of the extensibility element
     *
     * @return boolean
     */
    @Override
    public boolean isConfigurationSupported(QName qname) {
        boolean isSupported;
        if (qname == null || !MQQName.MQ_NS_URI.equals(qname.getNamespaceURI())) {
            isSupported = super.isConfigurationSupported(qname);
        } else {
            isSupported = MQQName.ADDRESS.getQName().equals(qname)
                    || MQQName.BINDING.getQName().equals(qname)
                    || MQQName.OPERATION.getQName().equals(qname)
                    || MQQName.BODY.getQName().equals(qname)
                    || MQQName.HEADER.getQName().equals(qname)
                    || MQQName.FAULT.getQName().equals(qname)
                    || MQQName.REDELIVERY.getQName().equals(qname);
        }
        return isSupported;
    }

    private ExtensibilityElementConfigurationEditorComponent getComponent(
            WSDLComponent component,
            Operation operation
    ) {
        MqBindingsConfigurationEditor editor = null;

        if (component != null && (component instanceof Port)) {
            editor = findEditor((Port) component, operation);

            // Important Thing To Remember:
            // We cannot assume this editor is the only entity working on the model.
            //
            // The model may have been modified externally between calls to
            // this method; e.g., user modifies the model using this editor, then
            // edits it manually (in source view), then brings the model up in this
            // editor again.
            //
            // The model must be reparsed EVERY TIME. Even if we're returning
            // a cached editor.
            editor.reparse();

            if (operation != null) {
                editor.focus(operation);
            }
        }

        return editor;
    }

    /**
     * Find (or create) an editor component for this supplied WSDL component.
     * Editors are cached because calls to {@link #getComponent} can happen for
     * ANY WSDLComponent in the SAME model.
     *
     * @param port The WSDL port for whose binding the requested editor is
     * intended.
     * @param operation Optional operation within the specified binding that is
     * to be the editor's object of regard. When this parameter is null, the
     * default editor focus is evaluated in the following order:
     * <p>
     * <ol>
     * <li>The specified binding's first MQ-bound operation; or</li>
     * <li>The model's first MQ-bound binding's first MQ-bound operation; or</li>
     * <li>null</li>
     * </ol></p>
     *
     * @return An ExtensibilityElementConfigurationEditor.
     */
    private MqBindingsConfigurationEditor findEditor(Port port,
                                                     Operation operation
    ) {
        assert port != null;

        MqBindingsConfigurationEditor editor;

        synchronized (mModelEditors) {
        synchronized (mModelOpEditors) {
                // Find/create an editor.
                WSDLModel model = port.getModel();
                if (operation != null) {
                    editor = mModelOpEditors.get(operation);
                } else {
                    editor = mModelEditors.get(model);
                }
                if (editor == null) {
                    // No cache hit - create a new one
                    editor = new MqBindingsConfigurationEditor(model,
                            bindingMode(port, operation)
                    );
                }

                // Update cache
                if (operation != null) {
                    mModelOpEditors.put(operation, editor);
                } else {
                    mModelEditors.put(model, editor);
                }
        }
        }
        
        editor.focus(port);
        return editor;
    }

    private MqBindingsConfigurationEditorForm.BindingMode bindingMode(
            Port port,
            Operation operation
    ) {
        assert port != null;
        
        MqBindingsConfigurationEditorForm.BindingMode mode =
                MqBindingsConfigurationEditorForm.BindingMode.ONEWAYGET;

        if (direction != null) {
            if (direction.equals(
                    ExtensibilityElementConfigurationEditorComponent.BC_TO_BP_DIRECTION
            )) {
                return MqBindingsConfigurationEditorForm.BindingMode.ONEWAYGET;
            } else if (direction.equals(
                    ExtensibilityElementConfigurationEditorComponent.BP_TO_BC_DIRECTION
            )) {
                return isSolicitedRead(port, operation)
                        ? MqBindingsConfigurationEditorForm.BindingMode.TWOWAYGET
                        : MqBindingsConfigurationEditorForm.BindingMode.ONEWAYPUT;
            }
        }

        return mode;
    }

    private boolean isSolicitedRead(Port port, Operation operation) {
        
        assert port != null;
        
        boolean solicited = false;
        
        String opName = (operation != null ? operation.getName() : null);
        Binding parentBinding = port.getBinding().get();
            
        if (parentBinding != null) {
            Collection<BindingOperation> bindingOps =
                    parentBinding.getBindingOperations();
            
            if (bindingOps != null) {
                for (BindingOperation bindingOp : bindingOps) {
                    
                    Collection<MQOperation> mqOps =
                            bindingOp.getExtensibilityElements(MQOperation.class);
                    
                    boolean match = (opName != null
                            ? bindingOp.getName().equals(opName)
                            : mqOps != null && !mqOps.isEmpty());
                    
                    if (match) {
                        if ((mqOps != null) && (!mqOps.isEmpty())) {
                            BindingOutput bindingOutput = bindingOp.getBindingOutput();
                            
                            if (bindingOutput != null) {
                                Output output = bindingOutput.getOutput().get();
                                solicited = (output != null);
                                if (solicited) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }                
        }
        return solicited;
    }
}
