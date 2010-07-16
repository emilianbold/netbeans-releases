/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import java.util.List;
import org.netbeans.modules.wsdlextensions.hl7.HL7Address;
import org.netbeans.modules.wsdlextensions.hl7.HL7Binding;
import org.netbeans.modules.wsdlextensions.hl7.HL7Component;
import org.netbeans.modules.wsdlextensions.hl7.HL7Message;
import org.netbeans.modules.wsdlextensions.hl7.HL7Operation;
import org.netbeans.modules.wsdlextensions.hl7.HL7ProtocolProperties;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
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
public class V2EditorWsdlAdapter implements V2EditorForm.Model{

    
    private WSDLModel model;
    private boolean hasFocus;
    private String softwareProductInfo;
    private String ackMode;
    private String processingId;
    private String versionId;
    private String softwareVendorOrganization;
    private String softwareVersionOrReleaseNo;
    private String softwareProductName;
    private String softwareBinaryId;
    
    private final BooleanOption sequenceNoEnabled = new BooleanOption("SEQUENCE_NO_ENABLED");
    private final BooleanOption sftEnabled = new BooleanOption("SFT_ENABLED");
    private final BooleanOption validateMSH = new BooleanOption("VALIDATE_MSH");
    private final BooleanOption journallingEnabled = new BooleanOption("JOURNALLING_ENABLED");
    private final BooleanOption persistenceEnabled = new BooleanOption("PERSISTENCE_ENABLED");
    
    private HL7Address focusedAddress;
    private HL7ProtocolProperties focusedProtocolProperties;
    private HL7Message focusedHL7Message;
    private String sendingApplication;
    private String softwareInstallDate;
    private String encodingCharacters;
    private String sendingFacility;
    private Byte fieldSeparator;
    
    
        /**
     * Create a WsdlConfigModelAdapter using the specified model.
     *
     * @param model Data model
     */
    public V2EditorWsdlAdapter(WSDLModel model) {
        if (model == null) {
            throw new NullPointerException("model");
        }
        this.model = model;
    }

    /**
     * Select a HL7 binding set (port->binding->operation) in the underlying
     * model that will be the subject of reads and writes thru this adapter.
     *
     * @param component A HL7 extensibility element that exists in the model.
     */
    public void focus(WSDLComponent component) {
        synchronized (this) {
            hasFocus = component != null && _focus(component);
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
        //
        // 1. Given a hl7:address, resolve associated hl7:binding, and parse.
        // 2. Given a hl7:binding, resolve associated port, and parse.
        // 3. Given any other hl7 extensibility element, resolve ancestor
        //    operation, binding, associated port, and parse.
        
         if (HL7Binding.class.isAssignableFrom(component.getClass())) {
            HL7Address address = findHL7Address((HL7Binding) component);
            HL7ProtocolProperties protocolProperties = findHL7ProtocolProperties((HL7Binding) component);
            HL7Message message = findHL7Message((HL7Binding)component);
            hasFocus = _parse(address) && _parse(message) && _parse(protocolProperties);
        }else if (HL7Operation.class.isAssignableFrom(component.getClass())) {
            HL7Binding binding = findHL7Binding((HL7Operation) component);
            HL7Address address = findHL7Address(binding);
            HL7ProtocolProperties protocolProperties = findHL7ProtocolProperties(binding);
            HL7Message message = findHL7Message(binding);
            hasFocus = _parse(address) && _parse(protocolProperties) && _parse(message);
        } else if (HL7Message.class.isAssignableFrom(component.getClass())) {
            HL7Binding binding = findHL7Binding((HL7Message) component);
            HL7Address address = findHL7Address(binding);
            HL7ProtocolProperties protocolProperties = findHL7ProtocolProperties(binding);
            hasFocus = _parse(address) && _parse(protocolProperties) && _parse((HL7Message)component);
        } else if (HL7Address.class.isAssignableFrom(component.getClass())) {
            HL7Binding binding = findHL7Binding((HL7Address) component);
            HL7Message message = findHL7Message(binding);
            HL7ProtocolProperties protocolProperties = findHL7ProtocolProperties(binding);
            hasFocus = _parse((HL7Address)component) && _parse(protocolProperties) && _parse(message);
        }   else if (HL7ProtocolProperties.class.isAssignableFrom(component.getClass())) {
            HL7Binding binding = findHL7Binding((HL7ProtocolProperties) component);
            HL7Address address = findHL7Address(binding);
            HL7Message message = findHL7Message(binding);
            hasFocus = _parse(address) && _parse((HL7ProtocolProperties)component) && _parse(message);
        } else {
            // Non-HL7 extensibility elements.
            hasFocus = false;
            if (Port.class.isAssignableFrom(component.getClass())) {
                List<HL7Address> addresses = component.getExtensibilityElements(HL7Address.class);
                if (!addresses.isEmpty()) {
                    HL7Address address = addresses.get(0);
                    HL7Binding binding = findHL7Binding((HL7Address) address);
                    HL7Message message = findHL7Message(binding);
                    HL7ProtocolProperties protocolProperties = findHL7ProtocolProperties(binding);
                    hasFocus = _parse(address) && _parse(message) && _parse(protocolProperties);
                }
            }
        } 
        return hasFocus;
    }

    private HL7Address findHL7Address(HL7Binding hl7Binding) {
        assert hl7Binding != null;
        HL7Address hl7Address = null;
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
                                List<HL7Address> addresses =
                                        port.getExtensibilityElements(HL7Address.class);
                                if (!addresses.isEmpty()) {
                                    hl7Address = addresses.get(0);
                                    break search;
                                }
                            }
                        }
                    }
                }
            }
        }
        return hl7Address;
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
            while (element != null && !Binding.class
                    .isAssignableFrom(element.getClass())) {
                element = element.getParent();
            }
            if (element != null && Binding.class
                    .isAssignableFrom(element.getClass())) {
                List<HL7Binding> hl7Bindings = element.getExtensibilityElements(HL7Binding.class);
                if (!hl7Bindings.isEmpty()) {
                    hl7Binding = hl7Bindings.get(0);
                }
            }
        }
        return hl7Binding;
    }

    private HL7Message findHL7Message(HL7Binding hl7Binding) {
        assert hl7Binding != null;
        HL7Message hl7Message = null;
        WSDLComponent parent = hl7Binding.getParent();
        if (Binding.class.isAssignableFrom(parent.getClass())) {
            Binding binding = (Binding) parent;
            if(binding.getBindingOperations().size() > 0){
                BindingOperation operation = binding.getBindingOperations().iterator().next();
                List<HL7Message> hl7Messages = operation.getBindingInput().getExtensibilityElements(HL7Message.class);
                if(!hl7Messages.isEmpty()){
                    hl7Message = hl7Messages.get(0);
                }
            }
        }
        return hl7Message;

    }

    private HL7ProtocolProperties findHL7ProtocolProperties(HL7Binding hl7Binding) {
        assert hl7Binding != null;
        HL7ProtocolProperties hl7ProtocolProperties = null;
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
                                List<HL7ProtocolProperties> protocolProperties =
                                        port.getExtensibilityElements(HL7ProtocolProperties.class);
                                if (!protocolProperties.isEmpty()) {
                                    hl7ProtocolProperties = protocolProperties.get(0);
                                    break search;
                                }
                            }
                        }
                    }
                }
            }
        }
        return hl7ProtocolProperties;
    }

    /** Decides if a reference isn't broken. */
    private boolean isReferencing(NamedComponentReference obj) {
        return obj != null && obj.get() != null;
    }
    
    private void updateModel() {
        synchronized (this) {
            if (hasFocus) {
                _updateAddress();
                _updateProtocolProperties();
                _updateHL7Message();
            }
        }
    }
    private void _updateAddress() {
        assert focusedAddress != null;
        //no properties in HL7Address to be updated
    }
    
    private void _updateHL7Message() {
        assert focusedHL7Message != null;
        
    }

    private void _updateProtocolProperties() {
        assert focusedProtocolProperties != null;
        focusedProtocolProperties.setAckMode(this.ackMode);
        focusedProtocolProperties.setProcessingID(this.processingId);
        focusedProtocolProperties.setSFTEnabled(this.sftEnabled.value());
        focusedProtocolProperties.setSeqNumEnabled(this.sequenceNoEnabled.value());
        focusedProtocolProperties.setSoftwareBinaryID(this.softwareBinaryId);
        focusedProtocolProperties.setSoftwareCertifiedVersionOrReleaseNumber(this.softwareVersionOrReleaseNo);
        focusedProtocolProperties.setSoftwareProductInformation(this.softwareProductInfo);
        focusedProtocolProperties.setSoftwareProductName(this.softwareProductName);
        focusedProtocolProperties.setSoftwareVendorOrganization(this.softwareVendorOrganization);
        focusedProtocolProperties.setValidateMSHEnabled(this.validateMSH.value());
        focusedProtocolProperties.setVersionID(this.versionId);
        focusedProtocolProperties.setEncodingCharacters(this.encodingCharacters);
        focusedProtocolProperties.setFieldSeparator(this.fieldSeparator);
        focusedProtocolProperties.setSendingApplication(this.sendingApplication);
        focusedProtocolProperties.setSendingFacility(this.sendingFacility);
        focusedProtocolProperties.setSoftwareInstallDate(this.softwareInstallDate);
        focusedProtocolProperties.setJournallingEnabled(this.journallingEnabled.value());
        focusedProtocolProperties.setPersistenceEnabled(this.persistenceEnabled.value());
    }
    
    private boolean _parse(HL7Address hl7Address){
        assert hl7Address != null;
        focusedAddress = hl7Address;
        return true;
    }
    
    private boolean _parse(HL7Message hl7Message){
        assert hl7Message != null;
        focusedHL7Message = hl7Message;
        return true;
    }
    
    private boolean _parse(HL7ProtocolProperties hl7ProtocolProperties){
        assert hl7ProtocolProperties != null;

        String ackMode = Utils.safeString(hl7ProtocolProperties.getAckMode());
        String processingId = Utils.safeString(hl7ProtocolProperties.getProcessingID());
        String versionId = Utils.safeString(hl7ProtocolProperties.getVersionID());
        boolean validateMSH = hl7ProtocolProperties.getValidateMSHEnabled();
        boolean sftEnabled = hl7ProtocolProperties.getSFTEnabled();
        String softwareVendorOrg = Utils.safeString(hl7ProtocolProperties.getSoftwareVendorOrganization());
        String softwareVersion = Utils.safeString(hl7ProtocolProperties.getSoftwareCertifiedVersionOrReleaseNumber());
        String softwareProductName = Utils.safeString(hl7ProtocolProperties.getSoftwareProductName());
        String softwareProductInfo = Utils.safeString(hl7ProtocolProperties.getSoftwareProductInformation());
        String softwareBinaryId = Utils.safeString(hl7ProtocolProperties.getSoftwareBinaryID());
        boolean seqNoEnabled = hl7ProtocolProperties.getSeqNumEnabled();
        String sendingApplication = Utils.safeString(hl7ProtocolProperties.getSendingApplication());
        String sendingFacility = Utils.safeString(hl7ProtocolProperties.getSendingFacility());
        String softwareInstallDate = Utils.safeString(hl7ProtocolProperties.getSoftwareInstallDate());
        String encodingCharacters = Utils.safeString(hl7ProtocolProperties.getEncodingCharacters());
        Byte fieldSeparator = hl7ProtocolProperties.getFieldSeparator();
        boolean journallingEnabled = hl7ProtocolProperties.getJournallingEnabled();
        boolean persistenceEnabled = hl7ProtocolProperties.getPersistenceEnabled();
        
        synchronized(this){
            this.ackMode = ackMode;
            this.processingId = processingId;
            this.versionId = versionId;
            this.validateMSH.accept(validateMSH);
            this.sftEnabled.accept(sftEnabled);
            this.softwareVendorOrganization = softwareVendorOrg;
            this.softwareVersionOrReleaseNo = softwareVersion;
            this.softwareProductName = softwareProductName;
            this.softwareProductInfo = softwareProductInfo;
            this.softwareBinaryId = softwareBinaryId;
            this.sequenceNoEnabled.accept(seqNoEnabled);
            this.sendingApplication = sendingApplication;
            this.sendingFacility = sendingFacility;
            this.softwareInstallDate = softwareInstallDate;
            this.encodingCharacters = encodingCharacters;
            this.fieldSeparator = fieldSeparator;
            this.journallingEnabled.accept(journallingEnabled);
            this.persistenceEnabled.accept(persistenceEnabled);

        }
        this.focusedProtocolProperties = hl7ProtocolProperties;
        return true;
        
    }

    public synchronized String getAcknowledgementMode() {
        return Utils.safeString(ackMode);
    }

    public void setAcknowledgementMode(String value) {
        value = Utils.safeString(value);
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            ackMode = value;
            updateModel();
        }
    }


    public boolean isSequenceNoEnabled() {
        return sequenceNoEnabled.value();
    }

    public void setSequenceNoEnabled(boolean enabled) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            this.sequenceNoEnabled.accept(enabled);
            updateModel();
        }
    }

    public synchronized String getProcessingId() {
        return Utils.safeString(processingId);
    }

    public void setProcessingId(String value) {
        value = Utils.safeString(value);
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            this.processingId = value;
            updateModel();
        }
    }

    public String getVersionId() {
        return Utils.safeString(versionId);
    }

    public void setVersionId(String value) {
        value = Utils.safeString(value);
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            this.versionId = value;
            updateModel();
        }
    }

    public boolean isValidateMSH() {
        return validateMSH.value();
    }

    public void setValidateMSH(boolean validate) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            this.validateMSH.accept(validate);
            updateModel();
        }
    }

    public boolean isSFTEnabled() {
        return sftEnabled.value();
    }

    public void setSFTEnabled(boolean value) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            this.sftEnabled.accept(value);
            updateModel();
        }
    }

    public synchronized String getSoftwareVendorOrganization() {
        return Utils.safeString(softwareVendorOrganization);
    }

    public void setSoftwareVendorOrganization(String value) {
        value = Utils.safeString(value);
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            softwareVendorOrganization = value;
            updateModel();
        }

    }

    public synchronized String getSoftwareCertifiedVersionOrReleaseNo() {
        return Utils.safeString(softwareVersionOrReleaseNo);
    }

    public void setSoftwareCertifiedVersionOrReleaseNo(String value) {
        value = Utils.safeString(value);
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            softwareVersionOrReleaseNo = value;
            updateModel();
        }
    }

    public synchronized String getSoftwareProductName() {
        return Utils.safeString(softwareProductName);
    }

    public void setSoftwareProductName(String value) {
        value = Utils.safeString(value);
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            softwareProductName = value;
            updateModel();
        }
    }

    public synchronized String getSoftwareBinaryId() {
        return Utils.safeString(softwareBinaryId);
    }

    public void setSoftwareBinaryId(String value) {
        value = Utils.safeString(value);
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            softwareBinaryId = value;
            updateModel();
        }
    }

    public String getSoftwareProductInformation() {
        return Utils.safeString(softwareProductInfo);
    }

    public void setSoftwareProductInformation(String value) {
        value = Utils.safeString(value);
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            softwareProductInfo = value;
            updateModel();
        }
    }
    public Byte getFieldSeparator() {
        return this.fieldSeparator;
    }

    public void setFieldSeparator(Byte fieldSeparator) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            this.fieldSeparator = fieldSeparator;
            updateModel();
        }

    }

    public String getSendingApplication() {
        return this.sendingApplication;
    }

    public void setSendingApplication(String sendingApplication) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            this.sendingApplication = sendingApplication;
            updateModel();
        }
    }

    public String getSendingFacility() {
        return this.sendingFacility;
    }

    public void setSendingFacility(String sendingFacility) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            this.sendingFacility = sendingFacility;
            updateModel();
        }

    }

    public String getSoftwareInstallDate() {
        return this.softwareInstallDate;
    }

    public void setSoftwareInstallDate(String softwareInstallDate) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            this.softwareInstallDate = softwareInstallDate;
            updateModel();
        }

    }

    public String getEncodingCharacters() {
        return this.encodingCharacters;
    }

    public void setEncodingCharacters(String encodingCharacters) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            this.encodingCharacters = encodingCharacters;
            updateModel();
        }
    }

    public boolean isJournallingEnabled() {
        return journallingEnabled.value();
    }

    public void setJournallingEnabled(boolean value) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            this.journallingEnabled.accept(value);
            updateModel();
        }
    }
    public boolean isPersistenceEnabled() {
        return persistenceEnabled.value();
    }

    public void setPersistenceEnabled(boolean value) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            this.persistenceEnabled.accept(value);
            updateModel();
        }
    }
    
    
    abstract class Option<T> {
        public abstract String name();

        public abstract boolean accept(Object value);

        public abstract T value();
    }

    /** An Option that is either true or false. */
    class BooleanOption extends Option<Boolean> {
        private boolean value;
        private final String name;

        BooleanOption(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }

        public synchronized boolean accept(Object value) {
            if (!(value instanceof String)) {
                this.value = Boolean.valueOf(String.valueOf(value));
            } else {
                this.value = value.toString().equalsIgnoreCase(name());
            }
            return this.value;
        }

        public synchronized Boolean value() {
            return value;
        }
    }

    /** An Option that is one thing, or another thing. */
    class BinaryChoiceOption extends BooleanOption {
        private final String alternateValue;

        BinaryChoiceOption(String name, String alternateValue) {
            super(name);
            this.alternateValue = alternateValue;
        }

        public String choice() {
            return (value() ? name() : alternateValue);
        }
    }

}
