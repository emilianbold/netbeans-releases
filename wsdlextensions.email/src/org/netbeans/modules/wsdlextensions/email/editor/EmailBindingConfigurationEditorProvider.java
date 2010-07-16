package org.netbeans.modules.wsdlextensions.email.editor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.netbeans.modules.wsdlextensions.email.EmailComponent;
import org.netbeans.modules.wsdlextensions.email.imap.IMAPComponent;
import org.netbeans.modules.wsdlextensions.email.imap.IMAPQName;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.wsdlextensions.email.pop3.POP3Binding;
import org.netbeans.modules.wsdlextensions.email.pop3.POP3Component;
import org.netbeans.modules.wsdlextensions.email.pop3.POP3QName;
import org.netbeans.modules.wsdlextensions.email.smtp.SMTPComponent;
import org.netbeans.modules.wsdlextensions.email.smtp.SMTPQName;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorComponent;
import org.netbeans.modules.xml.wsdl.bindingsupport.spi.ExtensibilityElementConfigurationEditorProvider;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

public class EmailBindingConfigurationEditorProvider extends ExtensibilityElementConfigurationEditorProvider {

    // Editor cache
    private final Map<WSDLModel, OutboundBindingConfigurationEditorComponent> mOutboundModelEditors =
            new HashMap<WSDLModel, OutboundBindingConfigurationEditorComponent>();
    private final Map<WSDLModel, InboundBindingConfigurationEditorComponent> mInboundModelEditors =
            new HashMap<WSDLModel, InboundBindingConfigurationEditorComponent>();
    // Record of latest time every model was accessed. Used to
    // remove cached editors that have not been used after a while.
    private final Map<WSDLModel, Date> mModelLastAccessTimes =
            new HashMap<WSDLModel, Date>();
    // Cached editors not accessed for this much time or longer, are removed
    // from the cache during each pruning pass.
    private final long PRUNE_AGE = 1800000; // milliseconds; 30 minutes
    // Editor cache
    private String mTemplateType = "";
    private String mLinkDirection = null;
    private WSDLComponent wsdlComponent;
    private InboundBindingConfigurationEditorComponent inboundEditorComponent;
    private OutboundBindingConfigurationEditorComponent outboundEditorComponent;

    @Override
    /**
     * Return the namespace for which this plugin provides components.
     *
     * @return String namespace corresponding to BC's schema file.
     */
    public String getNamespace() {
        return "http://schemas.sun.com/jbi/wsdl-extensions/email/";
    }

    @Override
    /**
     * Provides component at current context using qname and/or wsdlcomponent.
     * Return an appropriate EditorComponent corresponding to the qname and/or
     * wsdlcomponent.
     *
     * @param qname QName of the element in the wsdl
     * @param component WSDLComponent in the wsdl.
     *
     * @return An ExtensibilityElementConfigurationEditorComponent populated
     *         with the information contained in the component's WSDL model.
     */
    public ExtensibilityElementConfigurationEditorComponent getComponent(QName qname, WSDLComponent component) {

        // based on the direction of the link, we need to determine if it is
        // one way inbound or outbound so we can return the
        // right visual component
        if (mLinkDirection != null) {
            if (mLinkDirection.equals(
                    ExtensibilityElementConfigurationEditorComponent.BC_TO_BP_DIRECTION)) {
                // from direction of link, it is an inbound
                Definitions defs = component.getModel().getDefinitions();
                if (defs != null) {
                    Binding binding = defs.getBindings().iterator().next();
                    List<POP3Binding> bindings = binding.getExtensibilityElements(POP3Binding.class);
                    if (!bindings.isEmpty()) {
                        mTemplateType = "POP3";
                    } else {
                        mTemplateType = "IMAP";
                    }
                }
            } else if (mLinkDirection.equals(
                    ExtensibilityElementConfigurationEditorComponent.BP_TO_BC_DIRECTION)) {
                // from direction of link, it is an outbound
                mTemplateType = "SMTP";
            }
        }
        if (cleanupDefaultTemplateIfAny(wsdlComponent)) {
            createSkeleton(wsdlComponent, mTemplateType);
        }
        if (mTemplateType.equalsIgnoreCase("SMTP")) {
            outboundEditorComponent = findOutboundEditor(qname, component);
            outboundEditorComponent.reparse(component);
            return outboundEditorComponent;
        } else {
            inboundEditorComponent = findInboundEditor(qname, component, mTemplateType);
            inboundEditorComponent.reparse(component);
            return inboundEditorComponent;
        }
    }

    /**
     * Find (or create) an editor component for this supplied WSDL component.
     * This method is more complicated than I would like, because it caches
     * editors, and prunes its cache at every opportunity for old editors.
     * Editors are cached because calls to {@link #getComponent} can happen for
     * ANY WSDLComponent in the SAME model.  Creating a new editor component for
     * the entire model EACH TIME a node in that model is visited is crazy
     * expensive. The caching will alleviate some allocation stress, but since
     * every model must be reparsed anyway no matter what, it does not mitigate
     * the computational cost.
     *
     * @param qname Name of component
     * @param component The component itself
     *
     * @return An ExtensibilityElementConfigurationEditor.
     */
    private InboundBindingConfigurationEditorComponent findInboundEditor(QName qname,
            WSDLComponent component, String templateType) {

        assert component != null;
        assert component.getModel() != null;

        WSDLModel model;
        InboundBindingConfigurationEditorComponent editor;

        synchronized (mInboundModelEditors) {
            // Find/create an editor.
            model = component.getModel();
            editor = mInboundModelEditors.get(model);
            if (editor == null) {
                // No cache hit - create a new one
                editor = new InboundBindingConfigurationEditorComponent(qname, model, templateType, false);
            }

            // Update cache
            mInboundModelEditors.put(model, editor);
            mModelLastAccessTimes.put(model, new Date());
        }

        return editor;
    }

    /**
     * Find (or create) an editor component for this supplied WSDL component.
     * This method is more complicated than I would like, because it caches
     * editors, and prunes its cache at every opportunity for old editors.
     * Editors are cached because calls to {@link #getComponent} can happen for
     * ANY WSDLComponent in the SAME model.  Creating a new editor component for
     * the entire model EACH TIME a node in that model is visited is crazy
     * expensive. The caching will alleviate some allocation stress, but since
     * every model must be reparsed anyway no matter what, it does not mitigate
     * the computational cost.
     *
     * @param qname Name of component
     * @param component The component itself
     *
     * @return An ExtensibilityElementConfigurationEditor.
     */
    private OutboundBindingConfigurationEditorComponent findOutboundEditor(QName qname,
            WSDLComponent component) {

        assert component != null;
        assert component.getModel() != null;

        WSDLModel model;
        OutboundBindingConfigurationEditorComponent editor;

        synchronized (mOutboundModelEditors) {
            // Find/create an editor.
            model = component.getModel();
            editor = mOutboundModelEditors.get(model);
            if (editor == null) {
                // No cache hit - create a new one
                editor = new OutboundBindingConfigurationEditorComponent(qname, model, false);
            }

            // Update cache
            mOutboundModelEditors.put(model, editor);
            mModelLastAccessTimes.put(model, new Date());
        }

        return editor;
    }

    /**
     * The next two methods are interlinked. It allows for binding configuration per operation.
     * This is the entry point from casa editor, the skeleton template is loaded from the template.xml at this point.
     * It is recommended that the port and the link direction is cached and reused when the getComponent(Operation operation) is called.
     * 
     * @param component
     * @param linkDirection
     * @return
     */
    @Override
    public void initOperationBasedEditingSupport(WSDLComponent component, String linkDirection) {
        mLinkDirection = linkDirection;
        wsdlComponent = component;
    }

    /**
     * Called when OK is pressed in the dialog, commit all the panels related to each operation in the operation list.
     * @param operationList
     * @return true if successfully committed
     */
    @Override
    public boolean commitOperationBasedEditor(ArrayList<Operation> operationList) {
        if (mTemplateType.equalsIgnoreCase("SMTP")) {
            cleanup();
            return outboundEditorComponent.commit();
        } else {
            cleanup();
            return inboundEditorComponent.commit();
        }

    }

    /**
     * Return the component for the operation. This can be called multiple times, so it is recommended to cache it.
     * 
     * @param operation
     * @return
     */
    @Override
    public ExtensibilityElementConfigurationEditorComponent getOperationBasedEditorComponent(Operation operation) {
        ExtensibilityElementConfigurationEditorComponent component = getComponent(null, wsdlComponent);
        return component;
    }

    /**
     * Called when dialog is cancelled/closed, rollback all the panels related to each operation in the operation list.
     * Can be used to cleanup.
     * @param operationList
     */
    @Override
    public void rollbackOperationBasedEditor(ArrayList<Operation> operationList) {
        if (mTemplateType.equalsIgnoreCase("SMTP")) {
            outboundEditorComponent.rollback();
        } else {
            inboundEditorComponent.rollback();
        }
        cleanup();
    }

    private void cleanup() {
        mLinkDirection = null;
        wsdlComponent = null;
        mTemplateType = "";
    }

    private boolean cleanupDefaultTemplateIfAny(Port port) {
        Binding binding = port.getBinding().get();
        boolean changed = false;
        if (cleanupDefaultTemplateIfAny(port, getClassTypeForTemplate(mTemplateType))) {
            changed = true;
        }
        if (cleanupDefaultTemplateIfAny(binding)) {
            changed = true;
        }
        return changed;
    }

    private boolean cleanupDefaultTemplateIfAny(Binding binding) {
        boolean changed = false;
        if (cleanupDefaultTemplateIfAny(binding, getClassTypeForTemplate(mTemplateType))) {
            changed = true;
        }
        for (BindingOperation operation : binding.getBindingOperations()) {
            if (cleanupDefaultTemplateIfAny(operation)) {
                changed = true;
            }
        }
        return changed;
    }

    private boolean cleanupDefaultTemplateIfAny(BindingOperation operation) {
        boolean changed = false;
        if (cleanupDefaultTemplateIfAny(operation, getClassTypeForTemplate(mTemplateType))) {
            changed = true;
        }
        Class<? extends EmailComponent> c = getClassTypeForTemplate(mTemplateType);
        BindingInput input = operation.getBindingInput();
        if (input != null && cleanupDefaultTemplateIfAny(input, c)) {
            changed = true;
        }
        BindingOutput output = operation.getBindingOutput();
        if (output != null && cleanupDefaultTemplateIfAny(output, c)) {
            changed = true;
        }
        for (BindingFault fault : operation.getBindingFaults()) {
            if (cleanupDefaultTemplateIfAny(fault, c)) {
                changed = true;
            }
        }
        return changed;
    }

    private boolean cleanupDefaultTemplateIfAny(WSDLComponent wsdlComponent) {
        WSDLModel model = wsdlComponent.getModel();
        boolean inTransaction = false;
        if (!(inTransaction = model.isIntransaction())) {
            model.startTransaction();
        }
        boolean changed = false;
        try {
            Class<? extends EmailComponent> c = getClassTypeForTemplate(mTemplateType);
            if (wsdlComponent instanceof Port) {
                changed = cleanupDefaultTemplateIfAny((Port) wsdlComponent);
            } else if (wsdlComponent instanceof Binding) {
                changed = cleanupDefaultTemplateIfAny((Binding) wsdlComponent);
            } else if (wsdlComponent instanceof BindingOperation) {
                changed = cleanupDefaultTemplateIfAny((BindingOperation) wsdlComponent);
            } else if (wsdlComponent instanceof BindingInput) {
                changed = cleanupDefaultTemplateIfAny((BindingInput) wsdlComponent, c);
            } else if (wsdlComponent instanceof BindingOutput) {
                changed = cleanupDefaultTemplateIfAny((BindingOutput) wsdlComponent, c);
            } else if (wsdlComponent instanceof BindingFault) {
                changed = cleanupDefaultTemplateIfAny((BindingFault) wsdlComponent, c);
            }

        } finally {
            if (!inTransaction && model.isIntransaction()) {
                if (changed) {
                    model.endTransaction();
                } else {
                    model.rollbackTransaction();
                }
            }
        }
        return changed;

    }

    private boolean cleanupDefaultTemplateIfAny(WSDLComponent wsdlComponent, Class<? extends EmailComponent> c) {
        boolean changed = false;
        List<ExtensibilityElement> emailExtElements = wsdlComponent.getExtensibilityElements();
        for (ExtensibilityElement ee : emailExtElements) {
            if (ee instanceof EmailComponent && !c.isInstance(ee)) {
                wsdlComponent.removeExtensibilityElement(ee);
                changed = true;
            }
        }
        return changed;
    }

    private Class<? extends EmailComponent> getClassTypeForTemplate(String templateType) {
        if (templateType.equalsIgnoreCase("SMTP")) {
            return SMTPComponent.class;
        } else if (templateType.equalsIgnoreCase("IMAP")) {
            return IMAPComponent.class;
        } else if (templateType.equalsIgnoreCase("POP3")) {
            return POP3Component.class;
        }
        return null;
    }

    private void createSkeleton(WSDLComponent wsdlComponent, String mTemplateType) {
        if (wsdlComponent instanceof Port) {
            Port port = (Port) wsdlComponent;
            WSDLModel model = wsdlComponent.getModel();
            QName qname = null;
            if (mTemplateType.equalsIgnoreCase("SMTP")) {
                qname = SMTPQName.ADDRESS.getQName();
            } else if (mTemplateType.equalsIgnoreCase("IMAP")) {
                qname = IMAPQName.ADDRESS.getQName();
            } else if (mTemplateType.equalsIgnoreCase("POP3")) {
                qname = POP3QName.ADDRESS.getQName();
            }
            boolean isInTransaction = model.isIntransaction();
            if (!isInTransaction) {
                model.startTransaction();
            }
            try {
                wsdlComponent.addExtensibilityElement((ExtensibilityElement) model.getFactory().create(wsdlComponent, qname));
                Binding binding = port.getBinding().get();
                createSkeleton(binding, mTemplateType);
            } finally {
                if (!isInTransaction && model.isIntransaction()) {
                    model.endTransaction();
                }
            }
        } else if (wsdlComponent instanceof Binding) {
            Binding binding = (Binding) wsdlComponent;
            WSDLModel model = wsdlComponent.getModel();
            QName qname = null;
            if (mTemplateType.equalsIgnoreCase("SMTP")) {
                qname = SMTPQName.BINDING.getQName();
            } else if (mTemplateType.equalsIgnoreCase("IMAP")) {
                qname = IMAPQName.BINDING.getQName();
            } else if (mTemplateType.equalsIgnoreCase("POP3")) {
                qname = POP3QName.BINDING.getQName();
            }
            boolean isInTransaction = model.isIntransaction();
            if (!isInTransaction) {
                model.startTransaction();
            }
            try {
                wsdlComponent.addExtensibilityElement((ExtensibilityElement) model.getFactory().create(wsdlComponent, qname));
                for (BindingOperation operation : binding.getBindingOperations()) {
                    createSkeleton(operation, mTemplateType);
                }
            } finally {
                if (!isInTransaction && model.isIntransaction()) {
                    model.endTransaction();
                }
            }
        } else if (wsdlComponent instanceof BindingOperation) {
            BindingOperation bindingOperation = (BindingOperation) wsdlComponent;
            WSDLModel model = wsdlComponent.getModel();
            QName qname = null;
            if (mTemplateType.equalsIgnoreCase("SMTP")) {
                qname = SMTPQName.OPERATION.getQName();
            } else if (mTemplateType.equalsIgnoreCase("IMAP")) {
                qname = IMAPQName.OPERATION.getQName();
            } else if (mTemplateType.equalsIgnoreCase("POP3")) {
                qname = POP3QName.OPERATION.getQName();
            }
            boolean isInTransaction = model.isIntransaction();
            if (!isInTransaction) {
                model.startTransaction();
            }
            try {
                wsdlComponent.addExtensibilityElement((ExtensibilityElement) model.getFactory().create(wsdlComponent, qname));
                createSkeleton(bindingOperation.getBindingInput(), mTemplateType);
            } finally {
                if (!isInTransaction && model.isIntransaction()) {
                    model.endTransaction();
                }
            }
        } else if (wsdlComponent instanceof BindingInput) {
            WSDLModel model = wsdlComponent.getModel();
            QName qname = null;
            if (mTemplateType.equalsIgnoreCase("SMTP")) {
                qname = SMTPQName.INPUT.getQName();
            } else if (mTemplateType.equalsIgnoreCase("IMAP")) {
                qname = IMAPQName.INPUT.getQName();
            } else if (mTemplateType.equalsIgnoreCase("POP3")) {
                qname = POP3QName.INPUT.getQName();
            }
            boolean isInTransaction = model.isIntransaction();
            if (!isInTransaction) {
                model.startTransaction();
            }
            try {
                wsdlComponent.addExtensibilityElement((ExtensibilityElement) model.getFactory().create(wsdlComponent, qname));
            } finally {
                if (!isInTransaction && model.isIntransaction()) {
                    model.endTransaction();
                }
            }
        }
    }
}
