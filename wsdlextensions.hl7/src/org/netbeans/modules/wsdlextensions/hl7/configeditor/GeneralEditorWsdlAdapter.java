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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.wsdlextensions.hl7.HL7Address;
import org.netbeans.modules.wsdlextensions.hl7.HL7Binding;
import org.netbeans.modules.wsdlextensions.hl7.HL7Component;
import org.netbeans.modules.wsdlextensions.hl7.HL7Message;
import org.netbeans.modules.wsdlextensions.hl7.HL7Operation;
import org.netbeans.modules.wsdlextensions.hl7.HL7ProtocolProperties;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
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
public class GeneralEditorWsdlAdapter implements GeneralEditorForm.Model{

    
    private WSDLModel model;
    private boolean hasFocus = true;
    private String location;
    private String transportProtocol;
    private BinaryChoiceOption use = new BinaryChoiceOption("literal", "encoded");
    private String encodingStyle;
    private String llpType;
    private Byte startBlockCharacter;
    private Byte endBlockCharacter;
    private Byte endDataCharacter;
    private int mllpv2RetriesCountOnNak = 0;
    private long mllpv2RetryInterval = 0;
    private long mllpv2TimeToWaitForAckNak = 0 ;
    //private String part;
    
    private final BooleanOption hllpChecksumEnabled = new BooleanOption("HLLP_CHECKSUM_ENABLED");
	private final BooleanOption persistenceEnabled = new BooleanOption("PERSISTENCE_ENABLED");
    
    private HL7Address focusedAddress;
    private HL7ProtocolProperties focusedProtocolProperties;
    private List<HL7Message> focusedHL7Messages;
    
    
        /**
     * Create a WsdlConfigModelAdapter using the specified model.
     *
     * @param model Data model
     */
    public GeneralEditorWsdlAdapter(WSDLModel model) {
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
        //
        // 1. Given a hl7:address, resolve associated hl7:binding, and parse.
        // 2. Given a hl7:binding, resolve associated port, and parse.
        // 3. Given any other hl7 extensibility element, resolve ancestor
        //    operation, binding, associated port, and parse.
        
        if (HL7Binding.class.isAssignableFrom(component.getClass())) {
            HL7Address address = findHL7Address((HL7Binding) component);
            HL7ProtocolProperties protocolProperties = findHL7ProtocolProperties((HL7Binding) component);
            List<HL7Message> messages = findHL7Messages((HL7Binding)component);
            hasFocus = _parse(address) && _parse(messages) && _parse(protocolProperties);
        }else if (HL7Operation.class.isAssignableFrom(component.getClass())) {
            HL7Binding binding = findHL7Binding((HL7Operation) component);
            HL7Address address = findHL7Address(binding);
            HL7ProtocolProperties protocolProperties = findHL7ProtocolProperties(binding);
            List<HL7Message> messages = findHL7Messages(binding);
            hasFocus = _parse(address) && _parse(protocolProperties) && _parse(messages);
        } else if (HL7Message.class.isAssignableFrom(component.getClass())) {
            HL7Binding binding = findHL7Binding((HL7Message) component);
            HL7Address address = findHL7Address(binding);
            HL7ProtocolProperties protocolProperties = findHL7ProtocolProperties(binding);

            List<HL7Message> hl7Msgs = new ArrayList<HL7Message>();
            hl7Msgs.add((HL7Message)component);

            hasFocus = _parse(address) && _parse(protocolProperties) && _parse(hl7Msgs);
        } else if (HL7Address.class.isAssignableFrom(component.getClass())) {
            HL7Binding binding = findHL7Binding((HL7Address) component);
            List<HL7Message> messages = findHL7Messages(binding);
            HL7ProtocolProperties protocolProperties = findHL7ProtocolProperties(binding);
            hasFocus = _parse((HL7Address)component) && _parse(protocolProperties) && _parse(messages);
        }   else if (HL7ProtocolProperties.class.isAssignableFrom(component.getClass())) {
            HL7Binding binding = findHL7Binding((HL7ProtocolProperties) component);
            HL7Address address = findHL7Address(binding);
            List<HL7Message> messages = findHL7Messages(binding);
            hasFocus = _parse(address) && _parse((HL7ProtocolProperties)component) && _parse(messages);
        } else {
            // Non-HL7 extensibility elements.
            hasFocus = false;
            if (Port.class.isAssignableFrom(component.getClass())) {
                List<HL7Address> addresses = component.getExtensibilityElements(HL7Address.class);
                if (!addresses.isEmpty()) {
                    HL7Address address = addresses.get(0);
                    HL7Binding binding = findHL7Binding((HL7Address) address);
                    List<HL7Message> messages = findHL7Messages(binding);
                    HL7ProtocolProperties protocolProperties = findHL7ProtocolProperties(binding);
                    hasFocus = _parse(address) && _parse(messages) && _parse(protocolProperties);
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

    private List<HL7Message> findHL7Messages(HL7Binding hl7Binding) {
        assert hl7Binding != null;

        List<HL7Message> ret = new ArrayList<HL7Message>();
        WSDLComponent parent = hl7Binding.getParent();
        if (Binding.class.isAssignableFrom(parent.getClass())) {
            Binding binding = (Binding) parent;
            for (BindingOperation bindingOp : binding.getBindingOperations()) {
                BindingInput bindingInput = bindingOp.getBindingInput();
                if (bindingInput != null) {
                    ret.addAll(bindingInput.getExtensibilityElements(HL7Message.class));
                }
                BindingOutput bindingOutput = bindingOp.getBindingOutput();
                if (bindingOutput != null) {
                    ret.addAll(bindingOutput.getExtensibilityElements(HL7Message.class));
                }
            }
        }
        return ret;

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
                _updateHL7Messages();
            }
        }
    }
    private void _updateAddress() {
        assert focusedAddress != null;
        focusedAddress.setHL7ServerLocationURL(this.location);
        focusedAddress.setTransportProtocolName(this.transportProtocol);
    }
    
    private void _updateHL7Messages() {
        assert focusedHL7Messages != null && focusedHL7Messages.size() > 0;
        for (HL7Message focusedHL7Message : focusedHL7Messages) {
            focusedHL7Message.setEncodingStyle(this.encodingStyle);
            focusedHL7Message.setUse(this.use.choice());
            //focusedHL7Message.setPart(this.part);
        }
    }

    private void _updateProtocolProperties() {
        assert focusedProtocolProperties != null;
        focusedProtocolProperties.setEndBlockChar(this.endBlockCharacter);
        focusedProtocolProperties.setEndDataChar(this.endDataCharacter);
        focusedProtocolProperties.setHLLPChkSumEnabled(this.hllpChecksumEnabled.value());
        focusedProtocolProperties.setPersistenceEnabled(this.persistenceEnabled.value());
        focusedProtocolProperties.setLLPType(this.llpType);
        focusedProtocolProperties.setMLLPV2RetriesCountOnNak(this.mllpv2RetriesCountOnNak);
        focusedProtocolProperties.setMLLPV2RetryInterval(this.mllpv2RetryInterval);
        focusedProtocolProperties.setMLLPV2TimeToWaitForAckNak(this.mllpv2TimeToWaitForAckNak);
        focusedProtocolProperties.setStartBlockChar(this.startBlockCharacter);
    }
    
    private boolean _parse(HL7Address hl7Address){
        assert hl7Address != null;
        String location = Utils.safeString(hl7Address.getHL7ServerLocationURL());
        String transportProtocol = Utils.safeString(hl7Address.getTransportProtocolName());
        synchronized(this){
            this.location = location;
            this.transportProtocol = transportProtocol;
        }
        this.focusedAddress = hl7Address;
        return true;
    }
    
    private boolean _parse(List<HL7Message> hl7Messages){
        assert hl7Messages != null && hl7Messages.size() > 0;

        HL7Message hl7Message = hl7Messages.get(0);
        String use = Utils.safeString(hl7Message.getUse());
        String encodingStyle = Utils.safeString(hl7Message.getEncodingStyle());
        //String part = Utils.safeString(hl7Message.getPart());
        synchronized(this){
            this.use.accept(use);
            this.encodingStyle = encodingStyle;
            //this.part = part;
        }
        this.focusedHL7Messages = hl7Messages;
        return true;
    }
    
    private boolean _parse(HL7ProtocolProperties hl7ProtocolProperties){
        assert hl7ProtocolProperties != null;
        String llpType = Utils.safeString(hl7ProtocolProperties.getLLPType());
        Byte startBlockChar =hl7ProtocolProperties.getStartBlockChar();
        Byte endBlockChar = hl7ProtocolProperties.getEndBlockChar();
        Byte endDataChar = hl7ProtocolProperties.getEndDataChar();
        boolean hllpChecksumEnabled = hl7ProtocolProperties.getHLLPChkSumEnabled();
        boolean persistenceEnabled = hl7ProtocolProperties.getPersistenceEnabled();
        int mllpv2Retries = hl7ProtocolProperties.getMLLPV2RetriesCountOnNak();
        long mllv2RetryInterval = hl7ProtocolProperties.getMLLPV2RetryInterval();
        long mllpv2DurationForAckNak = hl7ProtocolProperties.getMLLPV2TimeToWaitForAckNak();
        
        synchronized(this){
            this.llpType = llpType;
            this.startBlockCharacter = startBlockChar;
            this.endBlockCharacter = endBlockChar;
            this.endDataCharacter = endDataChar;
            this.hllpChecksumEnabled.accept(hllpChecksumEnabled);
            this.persistenceEnabled.accept(persistenceEnabled);
            this.mllpv2RetriesCountOnNak = mllpv2Retries;
            this.mllpv2RetryInterval = mllv2RetryInterval;
            this.mllpv2TimeToWaitForAckNak = mllpv2DurationForAckNak;
        }
        this.focusedProtocolProperties = hl7ProtocolProperties;
        return true;
        
    }

    public synchronized String getLocation() {
        return Utils.safeString(location);
    }

    public void setLocation(String value) {
        value = Utils.safeString(value);
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            location = value;
            updateModel();
        }

    }

    public synchronized String getTransportProtocol() {
        return Utils.safeString(transportProtocol);
    }

    public void setTransportProtocol(String value) {
        value = Utils.safeString(value);
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            transportProtocol = value;
            updateModel();
        }

    }

    public synchronized String getUse() {
        return use.choice();
    }

    public void setUse(String value) {
        value = Utils.safeString(value);
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            this.use.accept(value);
            updateModel();
        }
    }

    public synchronized String getEncodingStyle() {
        return Utils.safeString(encodingStyle);
    }

    public void setEncodingStyle(String value) {
        value = Utils.safeString(value);
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            encodingStyle = value;
            updateModel();
        }
    }


    public synchronized String getLLPType() {
        return Utils.safeString(llpType);
    }

    public void setLLPType(String value) {
        value = Utils.safeString(value);
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            llpType = value;
            updateModel();
        }
    }

    public synchronized Byte getStartBlockCharacter() {
        return startBlockCharacter;
    }

    public void setStartBlockCharacter(Byte value) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            startBlockCharacter = value;
            updateModel();
        }
    }

    public synchronized Byte getEndBlockCharacter() {
        return endBlockCharacter;
    }

    public void setEndBlockCharacter(Byte value) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            endBlockCharacter = value;
            updateModel();
        }
    }

    public synchronized Byte  getEndDataCharacter() {
        return endDataCharacter;
    }

    public void setEndDataCharacter(Byte value) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            endDataCharacter = value;
            updateModel();
        }
    }

    public boolean isHLLPChecksumEnabled() {
        return hllpChecksumEnabled.value();
    }

    public boolean isPersistenceEnabled() {
        return persistenceEnabled.value();
    }
    public void setHLLPChecksumEnabled(boolean enabled) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            this.hllpChecksumEnabled.accept(enabled);
            updateModel();
        }
    }
	
	public void setPersistenceEnabled(boolean enabled) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            this.persistenceEnabled.accept(enabled);
            updateModel();
        }
    }

    public int getMllpv2RetriesCountOnNak() {
        return this.mllpv2RetriesCountOnNak;
    }

    public void setMllpv2RetriesCountOnNak(int count) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }

            this.mllpv2RetriesCountOnNak = count;
            updateModel();
        }
                
    }

    public long getMllpv2RetryInterval() {
        return this.mllpv2RetryInterval;
    }

    public void setMllpv2RetryInterval(long interval) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
        
            this.mllpv2RetryInterval = interval;
            this.updateModel();
        }
    }

    public long getMllpv2TimeToWaitForAckNak() {
        return this.mllpv2TimeToWaitForAckNak;
    }

    public void setMllpv2TimeToWaitForAckNak(long duration) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
        
            this.mllpv2TimeToWaitForAckNak = duration;
            this.updateModel();
        }
    }

    /*public String getPart() {
        return this.part;
    }

    public void setPart(String part) {
        synchronized (this) {
            if (!hasFocus) {
                throw new IllegalStateException(
                        "Binding targeted for modification is unspecified.");
            }
            this.part = part;
            updateModel();
        }
    }*/
    
    
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
