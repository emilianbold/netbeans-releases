/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.wsdlextensions.hl7.configeditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.wsdlextensions.hl7.HL7Address;
import org.netbeans.modules.wsdlextensions.hl7.HL7Binding;
import org.netbeans.modules.wsdlextensions.hl7.HL7CommunicationControl;
import org.netbeans.modules.wsdlextensions.hl7.HL7CommunicationControls;
import org.netbeans.modules.wsdlextensions.hl7.HL7Component;
import org.netbeans.modules.wsdlextensions.hl7.HL7Constants;
import org.netbeans.modules.wsdlextensions.hl7.HL7Message;
import org.netbeans.modules.wsdlextensions.hl7.HL7Operation;
import org.netbeans.modules.wsdlextensions.hl7.HL7ProtocolProperties;
import org.netbeans.modules.wsdlextensions.hl7.configeditor.CommunicationControlForm.Model;
import org.netbeans.modules.wsdlextensions.hl7.impl.HL7CommunicationControlImpl;
import org.netbeans.modules.wsdlextensions.hl7.impl.HL7CommunicationControlsImpl;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author Vishnuvardhan P.R
 */
public class CommunicationControlWsdlAdapter implements CommunicationControlForm.Model {

    private TimeToWaitControl timeToWaitControl;
    private NakReceivedControl nakReceivedControl;
    private MaxNoResponseControl maxNoResponseControl;
    private MaxNakReceivedControl maxNakReceivedControl;
    private MaxNakSentControl maxNakSentControl;
    private MaxCannedNakSentControl maxCannedNakSentControl;
    private MaxConnectRetriesControl maxConnectRetriesControl;
    private boolean hasFocus = true;
    private WSDLModel model;
    private HL7CommunicationControls focusedCommunicationControls;
    private List<HL7CommunicationControl> hl7CommCntrls = null;
    private String templateConstant;
    private boolean isInbound;

    /**
     * Create a WsdlConfigModelAdapter using the specified model.
     *
     * @param model Data model
     */
    public CommunicationControlWsdlAdapter(WSDLModel model, String templateConstant) {
        if (model == null) {
            throw new NullPointerException("model");
        }
        this.model = model;
        this.templateConstant = templateConstant;
        this.isInbound = this.templateConstant.equals(HL7Constants.TEMPLATE_IN);
    }

    /**
     * Select a HL7 binding set (port->binding->operation) in the underlying
     * model that will be the subject of reads and writes thru this adapter.
     *
     * @param component A HL7 extensibility element that exists in the model.
     */
    public void focus(WSDLComponent component) {
        synchronized (this) {
            //hasFocus = component != null && _focus(component);
            _focus(component);
        }
    }

    private boolean _focus(WSDLComponent component) {
        assert component != null;
        boolean hasFocus = false;

        // HL7 extensibility element hierarchy:
        //
        // wsdl:binding
        //     hl7:binding  <---
        //     wsdl:operation
        //         hl7:operation <---
        //         wsdl:input
        //             hl7:message <---
        // wsdl:service
        //     wsdl:port
        //         hl7:address <---
        //         hl7:protocolproperties <---
        //         hl7:communicationcontrols <---
        //
        // 1. Given a hl7:address, resolve associated hl7:binding, and parse.
        // 2. Given a hl7:binding, resolve associated port, and parse.
        // 3. Given any other hl7 extensibility element, resolve ancestor
        //    operation, binding, associated port, and parse.

        if (HL7Binding.class.isAssignableFrom(component.getClass())) {
            HL7CommunicationControls communicationControls = findHL7CommunicationControls((HL7Binding) component);
            hasFocus = _parse(communicationControls);
        } else if (HL7Operation.class.isAssignableFrom(component.getClass())) {
            HL7Binding binding = findHL7Binding((HL7Operation) component);
            HL7CommunicationControls communicationControls = findHL7CommunicationControls(binding);
            hasFocus = _parse(communicationControls);
        } else if (HL7Message.class.isAssignableFrom(component.getClass())) {
            HL7Binding binding = findHL7Binding((HL7Message) component);
            HL7CommunicationControls communicationControls = findHL7CommunicationControls(binding);
            hasFocus = _parse(communicationControls);
        } else if (HL7Address.class.isAssignableFrom(component.getClass())) {
            HL7Binding binding = findHL7Binding((HL7Address) component);
            HL7CommunicationControls communicationControls = findHL7CommunicationControls(binding);
            hasFocus = _parse(communicationControls);
        } else if (HL7ProtocolProperties.class.isAssignableFrom(component.getClass())) {
            HL7Binding binding = findHL7Binding((HL7ProtocolProperties) component);
            HL7CommunicationControls communicationControls = findHL7CommunicationControls(binding);
            hasFocus = _parse(communicationControls);
        } else {
            // Non-HL7 extensibility elements.
            hasFocus = false;
            if (Port.class.isAssignableFrom(component.getClass())) {
                List<HL7Address> addresses = component.getExtensibilityElements(HL7Address.class);
                if (!addresses.isEmpty()) {
                    HL7Address address = addresses.get(0);
                    HL7Binding binding = findHL7Binding((HL7Address) address);
                    HL7CommunicationControls communicationControls = findHL7CommunicationControls(binding);
                    hasFocus = _parse(communicationControls);
                }
            }
        }
        return hasFocus;
    }

    private HL7Binding findHL7Binding(HL7Address hl7Address) {
        assert hl7Address != null;
        HL7Binding hl7Binding = null;
        WSDLComponent parent = hl7Address.getParent();
        if (Port.class.isAssignableFrom(parent.getClass())) {
            Port port = (Port) parent;
            if (isReferencing(port.getBinding())) {
                Binding binding = port.getBinding().get();
                List<HL7Binding> bindings = binding.getExtensibilityElements(
                        HL7Binding.class);
                if (!bindings.isEmpty()) {
                    hl7Binding = bindings.get(0);
                }
            }
        }
        return hl7Binding;
    }

    private HL7Binding findHL7Binding(HL7ProtocolProperties hl7ProtocolProperties) {
        assert hl7ProtocolProperties != null;
        HL7Binding hl7Binding = null;
        WSDLComponent parent = hl7ProtocolProperties.getParent();
        if (Port.class.isAssignableFrom(parent.getClass())) {
            Port port = (Port) parent;
            if (isReferencing(port.getBinding())) {
                Binding binding = port.getBinding().get();
                List<HL7Binding> bindings = binding.getExtensibilityElements(
                        HL7Binding.class);
                if (!bindings.isEmpty()) {
                    hl7Binding = bindings.get(0);
                }
            }
        }
        return hl7Binding;


    }

    private HL7Binding findHL7Binding(HL7Component hl7Component) {
        assert hl7Component != null;
        HL7Binding hl7Binding = null;
        WSDLComponent element = hl7Component;
        if (HL7Address.class.isAssignableFrom(hl7Component.getClass())) {
            hl7Binding = findHL7Binding((HL7Address) hl7Component);
        } else {
            // Traverse up the model until we find the HL7 Binding.
            while (element != null && !Binding.class.isAssignableFrom(element.getClass())) {
                element = element.getParent();
            }
            if (element != null && Binding.class.isAssignableFrom(element.getClass())) {
                List<HL7Binding> hl7Bindings = element.getExtensibilityElements(HL7Binding.class);
                if (!hl7Bindings.isEmpty()) {
                    hl7Binding = hl7Bindings.get(0);
                }
            }
        }
        return hl7Binding;
    }

    private HL7CommunicationControls findHL7CommunicationControls(HL7Binding hl7Binding) {
        assert hl7Binding != null;
        HL7CommunicationControls hl7CommunicationControls = null;
        Definitions definitions = model.getDefinitions();
        if (definitions != null) {
            WSDLComponent parent = hl7Binding.getParent();
            if (Binding.class.isAssignableFrom(parent.getClass())) {
                Binding targetBinding = (Binding) hl7Binding.getParent();
                search:
                for (Service service : definitions.getServices()) {
                    for (Port port : service.getPorts()) {
                        if (isReferencing(port.getBinding())) {
                            Binding binding = port.getBinding().get();
                            if (binding.equals(targetBinding)) {
                                List<HL7CommunicationControls> communicationControls =
                                        port.getExtensibilityElements(HL7CommunicationControls.class);
                                if (!communicationControls.isEmpty()) {
                                    hl7CommunicationControls = communicationControls.get(0);
                                    if (hl7CommunicationControls != null && hl7CommunicationControls.getHL7CommunicationControls() == null) {
                                        List<HL7CommunicationControl> ctrlsList = hl7CommunicationControls.getExtensibilityElements(HL7CommunicationControl.class);
                                        hl7CommunicationControls.setHL7CommunicationControls(ctrlsList);
                                    }
                                    break search;
                                } else {
                                    HL7CommunicationControls hl7CommControls = new HL7CommunicationControlsImpl(this.model);
                                    this.model.startTransaction();
                                    port.addExtensibilityElement(hl7CommControls);
                                    this.model.endTransaction();
                                    hl7CommunicationControls = hl7CommControls;
                                    break search;
                                }
                            }
                        }
                    }
                }
            }
        }
        return hl7CommunicationControls;

    }

    private boolean _parse(HL7CommunicationControls hl7communicationControls) {
        assert hl7communicationControls != null;

        synchronized (this) {
            if (this.maxCannedNakSentControl == null) {
                this.maxCannedNakSentControl = new Model.MaxCannedNakSentControl();
            }
            if (this.maxConnectRetriesControl == null) {
                this.maxConnectRetriesControl = new Model.MaxConnectRetriesControl();
            }
            if (this.maxNakReceivedControl == null) {
                this.maxNakReceivedControl = new Model.MaxNakReceivedControl();
            }
            if (this.maxNakSentControl == null) {
                this.maxNakSentControl = new Model.MaxNakSentControl();
            }
            if (this.maxNoResponseControl == null) {
                this.maxNoResponseControl = new Model.MaxNoResponseControl();
            }
            if (this.nakReceivedControl == null) {
                this.nakReceivedControl = new Model.NakReceivedControl();
            }
            if (this.timeToWaitControl == null) {
                this.timeToWaitControl = new Model.TimeToWaitControl();
            }

            List<HL7CommunicationControl> ctrlsList = hl7communicationControls.getHL7CommunicationControls();
            ctrlsList = checkAndCreateControls(ctrlsList, hl7communicationControls);
            if (ctrlsList != null) {
                for (HL7CommunicationControl control : ctrlsList) {
                    if (isInbound) {
                        if (control.getName().equals(HL7CommunicationControl.MAX_CANNED_NAK_SENT)) {
                            this.maxCannedNakSentControl.setEnabled(control.getEnabled());
                            this.maxCannedNakSentControl.setValue(control.getValue());
                            this.maxCannedNakSentControl.setRecourseAction(control.getRecourseAction());
                        } else if (control.getName().equals(HL7CommunicationControl.MAX_NAK_SENT)) {
                            this.maxNakSentControl.setEnabled(control.getEnabled());
                            this.maxNakSentControl.setValue(control.getValue());
                            this.maxNakSentControl.setRecourseAction(control.getRecourseAction());
                        }
                    } else {
                        if (control.getName().equals(HL7CommunicationControl.MAX_CONNECT_RETRIES)) {
                            this.maxConnectRetriesControl.setEnabled(control.getEnabled());
                            this.maxConnectRetriesControl.setValueAsString(control.getValueAsString());
                            this.maxConnectRetriesControl.setRecourseAction(control.getRecourseAction());
                        } else if (control.getName().equals(HL7CommunicationControl.MAX_NAK_RECEIVED)) {
                            this.maxNakReceivedControl.setEnabled(control.getEnabled());
                            this.maxNakReceivedControl.setValue(control.getValue());
                            this.maxNakReceivedControl.setRecourseAction(control.getRecourseAction());
                        } else if (control.getName().equals(HL7CommunicationControl.MAX_NO_RESPONSE)) {
                            this.maxNoResponseControl.setEnabled(control.getEnabled());
                            this.maxNoResponseControl.setValue(control.getValue());
                            this.maxNoResponseControl.setRecourseAction(control.getRecourseAction());
                        } else if (control.getName().equals(HL7CommunicationControl.NAK_RECEIVED)) {
                            this.nakReceivedControl.setEnabled(control.getEnabled());
                            this.nakReceivedControl.setValue(control.getValue());
                            this.nakReceivedControl.setRecourseAction(control.getRecourseAction());
                        } else if (control.getName().equals(HL7CommunicationControl.TIME_TO_WAIT_FOR_A_RESPONSE)) {
                            this.timeToWaitControl.setEnabled(control.getEnabled());
                            this.timeToWaitControl.setValue(control.getValue());
                            this.timeToWaitControl.setRecourseAction(control.getRecourseAction());
                        }
                    }
                }
            }
            this.hl7CommCntrls = ctrlsList;
        }
        this.focusedCommunicationControls = hl7communicationControls;
        return true;

    }

    /** Decides if a reference isn't broken. */
    private boolean isReferencing(NamedComponentReference obj) {
        return obj != null && obj.get() != null;
    }

    private void _updateHL7CommunicationControls() {
        assert focusedCommunicationControls != null;

        Iterator<HL7CommunicationControl> it = hl7CommCntrls.iterator();
        while (it.hasNext()) {
            HL7CommunicationControl control = it.next();
            if (isInbound) {
                if (control.getName().equals(HL7CommunicationControl.MAX_CANNED_NAK_SENT)) {
                    control.setEnabled(this.maxCannedNakSentControl.isEnabled());
                    control.setValue(Long.valueOf(this.maxCannedNakSentControl.getValue()));
                    control.setRecourseAction(this.maxCannedNakSentControl.getRecourseAction());
                } else if (control.getName().equals(HL7CommunicationControl.MAX_NAK_SENT)) {
                    control.setEnabled(this.maxNakSentControl.isEnabled());
                    control.setValue(Long.valueOf(this.maxNakSentControl.getValue()));
                    control.setRecourseAction(this.maxNakSentControl.getRecourseAction());
                }
            } else {
                if (control.getName().equals(HL7CommunicationControl.MAX_CONNECT_RETRIES)) {
                    control.setEnabled(this.maxConnectRetriesControl.isEnabled());
                    control.setValueAsString(this.maxConnectRetriesControl.getValueAsString());
                    control.setRecourseAction(this.maxConnectRetriesControl.getRecourseAction());
                } else if (control.getName().equals(HL7CommunicationControl.MAX_NAK_RECEIVED)) {
                    control.setEnabled(this.maxNakReceivedControl.isEnabled());
                    control.setValue(Long.valueOf(this.maxNakReceivedControl.getValue()));
                    control.setRecourseAction(this.maxNakReceivedControl.getRecourseAction());
                } else if (control.getName().equals(HL7CommunicationControl.MAX_NO_RESPONSE)) {
                    control.setEnabled(this.maxNoResponseControl.isEnabled());
                    control.setValue(Long.valueOf(this.maxNoResponseControl.getValue()));
                    control.setRecourseAction(this.maxNoResponseControl.getRecourseAction());
                } else if (control.getName().equals(HL7CommunicationControl.NAK_RECEIVED)) {
                    control.setEnabled(this.nakReceivedControl.isEnabled());
                    control.setValue(Long.valueOf(this.nakReceivedControl.getValue()));
                    control.setRecourseAction(this.nakReceivedControl.getRecourseAction());
                } else if (control.getName().equals(HL7CommunicationControl.TIME_TO_WAIT_FOR_A_RESPONSE)) {
                    control.setEnabled(this.timeToWaitControl.isEnabled());
                    control.setValue(Long.valueOf(this.timeToWaitControl.getValue()));
                    control.setRecourseAction(this.timeToWaitControl.getRecourseAction());
                }
            }
        }

        focusedCommunicationControls.setHL7CommunicationControls(hl7CommCntrls);

    }

    private ArrayList<HL7CommunicationControl> checkAndCreateControls(
            List<HL7CommunicationControl> hl7CommCntrls, HL7CommunicationControls controls) {

        if (hl7CommCntrls == null) {
            hl7CommCntrls = new ArrayList<HL7CommunicationControl>();
        }
        boolean transactionStarted = false;
        HashMap<String, HL7CommunicationControl> controlMap = new HashMap<String, HL7CommunicationControl>();

        for (HL7CommunicationControl control : hl7CommCntrls) {
            controlMap.put(control.getName(), control);
        }

        HL7CommunicationControl control = controlMap.get(HL7CommunicationControl.MAX_NAK_SENT);
        if (control == null &&
                (this.templateConstant.equals(HL7Constants.TEMPLATE_IN))) {
            control = new HL7CommunicationControlImpl(this.model);
            control.setName(HL7CommunicationControl.MAX_NAK_SENT);
            control.setEnabled(false);
            control.setValue(0L);
            control.setRecourseAction("");
            if (!this.model.isIntransaction()) {
                this.model.startTransaction();
                transactionStarted = true;
            }
            controls.addExtensibilityElement(control);
            if (transactionStarted) {
                this.model.endTransaction();
                transactionStarted = false;
            }
            controlMap.put(control.getName(), control);
        }

        control = controlMap.get(HL7CommunicationControl.MAX_CANNED_NAK_SENT);
        if (control == null &&
                (this.templateConstant.equals(HL7Constants.TEMPLATE_IN))) {
            control = new HL7CommunicationControlImpl(this.model);
            control.setName(HL7CommunicationControl.MAX_CANNED_NAK_SENT);
            control.setEnabled(false);
            control.setValue(0L);
            control.setRecourseAction("");
            if (!this.model.isIntransaction()) {
                this.model.startTransaction();
                transactionStarted = true;
            }
            controls.addExtensibilityElement(control);
            if (transactionStarted) {
                this.model.endTransaction();
                transactionStarted = false;
            }
            controlMap.put(control.getName(), control);
        }

        control = controlMap.get(HL7CommunicationControl.MAX_CONNECT_RETRIES);
        if (control == null &&
                (this.templateConstant.equals(HL7Constants.TEMPLATE_OUT))) {
            control = new HL7CommunicationControlImpl(this.model);
            control.setName(HL7CommunicationControl.MAX_CONNECT_RETRIES);
            control.setEnabled(false);
            control.setValue(0L);
            control.setValueAsString("");
            control.setRecourseAction("");
            if (!this.model.isIntransaction()) {
                this.model.startTransaction();
                transactionStarted = true;
            }
            controls.addExtensibilityElement(control);
            if (transactionStarted) {
                this.model.endTransaction();
                transactionStarted = false;
            }
            controlMap.put(control.getName(), control);
        }

        control = controlMap.get(HL7CommunicationControl.MAX_NAK_RECEIVED);
        if (control == null &&
                (this.templateConstant.equals(HL7Constants.TEMPLATE_OUT))) {
            control = new HL7CommunicationControlImpl(this.model);
            control.setName(HL7CommunicationControl.MAX_NAK_RECEIVED);
            control.setEnabled(false);
            control.setValue(0L);
            control.setRecourseAction("");
            if (!this.model.isIntransaction()) {
                this.model.startTransaction();
                transactionStarted = true;
            }
            controls.addExtensibilityElement(control);
            if (transactionStarted) {
                this.model.endTransaction();
                transactionStarted = false;
            }
            controlMap.put(control.getName(), control);
        }


        control = controlMap.get(HL7CommunicationControl.MAX_NO_RESPONSE);
        if (control == null &&
                (this.templateConstant.equals(HL7Constants.TEMPLATE_OUT))) {
            control = new HL7CommunicationControlImpl(this.model);
            control.setName(HL7CommunicationControl.MAX_NO_RESPONSE);
            control.setEnabled(false);
            control.setValue(0L);
            control.setRecourseAction("");
            if (!this.model.isIntransaction()) {
                this.model.startTransaction();
                transactionStarted = true;
            }
            controls.addExtensibilityElement(control);
            if (transactionStarted) {
                this.model.endTransaction();
                transactionStarted = false;
            }
            controlMap.put(control.getName(), control);
        }

        control = controlMap.get(HL7CommunicationControl.NAK_RECEIVED);
        if (control == null &&
                (this.templateConstant.equals(HL7Constants.TEMPLATE_OUT))) {
            control = new HL7CommunicationControlImpl(this.model);
            control.setName(HL7CommunicationControl.NAK_RECEIVED);
            control.setEnabled(false);
            control.setValue(0L);
            control.setRecourseAction("");
            if (!this.model.isIntransaction()) {
                this.model.startTransaction();
                transactionStarted = true;
            }
            controls.addExtensibilityElement(control);
            if (transactionStarted) {
                this.model.endTransaction();
                transactionStarted = false;
            }
            controlMap.put(control.getName(), control);
        }

        control = controlMap.get(HL7CommunicationControl.TIME_TO_WAIT_FOR_A_RESPONSE);
        if (control == null &&
                (this.templateConstant.equals(HL7Constants.TEMPLATE_OUT))) {
            control = new HL7CommunicationControlImpl(this.model);
            control.setName(HL7CommunicationControl.TIME_TO_WAIT_FOR_A_RESPONSE);
            control.setEnabled(false);
            control.setValue(0L);
            control.setRecourseAction("");
            if (!this.model.isIntransaction()) {
                this.model.startTransaction();
                transactionStarted = true;
            }
            controls.addExtensibilityElement(control);
            if (transactionStarted) {
                this.model.endTransaction();
                transactionStarted = false;
            }
            controlMap.put(control.getName(), control);
        }

        return new ArrayList<HL7CommunicationControl>(controlMap.values());
    }

    public TimeToWaitControl getTimeToWaitControl() {
        return this.timeToWaitControl;
    }

    public void setTimeToWaitControl(TimeToWaitControl value) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            this.timeToWaitControl = value;
            _updateHL7CommunicationControls();
        }
    }

    public NakReceivedControl getNakReceivedControl() {
        return this.nakReceivedControl;
    }

    public void setNakReceivedControl(NakReceivedControl value) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }

            this.nakReceivedControl = value;
            _updateHL7CommunicationControls();
        }
    }

    public MaxNoResponseControl getMaxNoResponseControl() {
        return this.maxNoResponseControl;
    }

    public void setMaxNoResponseControl(MaxNoResponseControl value) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }

            this.maxNoResponseControl = value;
            _updateHL7CommunicationControls();
        }
    }

    public MaxNakReceivedControl getMaxNakReceivedControl() {
        return this.maxNakReceivedControl;
    }

    public void setMaxNakReceivedControl(MaxNakReceivedControl value) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }

            this.maxNakReceivedControl = value;
            _updateHL7CommunicationControls();
        }
    }

    public MaxNakSentControl getMaxNakSentControl() {
        return this.maxNakSentControl;
    }

    public void setMaxNakSentControl(MaxNakSentControl value) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }

            this.maxNakSentControl = value;
            _updateHL7CommunicationControls();
        }
    }

    public MaxCannedNakSentControl getMaxCannedNakSentControl() {
        return this.maxCannedNakSentControl;
    }

    public void setMaxCannedNakSentControl(MaxCannedNakSentControl value) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }

            this.maxCannedNakSentControl = value;
            _updateHL7CommunicationControls();
        }
    }

    public MaxConnectRetriesControl getMaxConnectRetriesControl() {
        return this.maxConnectRetriesControl;
    }

    public void setMaxConnectRetriesControl(MaxConnectRetriesControl value) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }

            this.maxConnectRetriesControl = value;
            _updateHL7CommunicationControls();
        }
    }
}
