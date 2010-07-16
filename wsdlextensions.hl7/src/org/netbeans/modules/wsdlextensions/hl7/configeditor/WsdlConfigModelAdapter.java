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

import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 *
 * @author Vishnuvardhan P.R
 */
public class WsdlConfigModelAdapter 
        implements HL7BindingsConfigurationEditorForm.Model {
    
    private WSDLModel model;
    private boolean hasFocus;
    private String location;
    private String softwareProductInfo;
    private String transportProtocol;
    private String use;
    private String encodingStyle;
    private String llpType;
    private String ackMode;
    private String startBlockCharacter;
    private String endBlockCharacter;
    private String endDataCharacter;
    private String processingId;
    private String versionId;
    private String softwareVendorOrganization;
    private String softwareVersionOrReleaseNo;
    private String softwareProductName;
    private String softwareBinaryId;
        /**
     * Create a WsdlConfigModelAdapter using the specified model.
     *
     * @param model Data model
     */
    public WsdlConfigModelAdapter(WSDLModel model) {
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

        // MQ extensibility element hierarchy:
        //
        // wsdl:binding
        //     mq:binding  <---
        //     wsdl:operation
        //         mq:operation <---
        //         wsdl:input
        //             mq:body <---
        // wsdl:service
        //     wsdl:port
        //         mq:address <---
        //
        // 1. Given a mq:address, resolve associated mq:binding, and parse.
        // 2. Given a mq:binding, resolve associated port, and parse.
        // 3. Given any other MQ extensibility element, resolve ancestor
        //    operation, binding, associated port, and parse.
     /*   if (MQAddress.class.isAssignableFrom(component.getClass())) {
            MQBinding binding = findMqBinding((MQAddress) component);
            MQOperation operation = findMqOperation(binding);
            hasFocus = _parse((MQAddress) component) && _parse(operation);
        } else if (MQBinding.class.isAssignableFrom(component.getClass())) {
            MQOperation operation = findMqOperation((MQBinding) component);
            MQAddress address = findMqAddress((MQBinding) component);
            hasFocus = _parse(address) && _parse(operation);
        } else if (MQBody.class.isAssignableFrom(component.getClass())) {
            MQOperation operation = findMqOperation((MQBody) component);
            MQBinding binding = findMqBinding((MQBody) component);
            MQAddress address = findMqAddress(binding);
            hasFocus = _parse(address) && _parse(operation);
        } else if (MQOperation.class.isAssignableFrom(component.getClass())) {
            MQBinding binding = findMqBinding((MQOperation) component);
            MQAddress address = findMqAddress(binding);
            hasFocus = _parse(address) && _parse((MQOperation) component);
        } else if (MQHeader.class.isAssignableFrom(component.getClass())) {
            MQOperation operation = findMqOperation((MQHeader) component);
            MQBinding binding = findMqBinding((MQHeader) component);
            MQAddress address = findMqAddress(binding);
            hasFocus = _parse(address) && _parse(operation);
        } else {
            // Non-MQ extensibility elements.
            hasFocus = false;
            if (Port.class.isAssignableFrom(component.getClass())) {
                List<MQAddress> addresses = component.getExtensibilityElements(MQAddress.class);
                if (!addresses.isEmpty()) {
                    MQAddress address = addresses.get(0);
                    MQBinding binding = findMqBinding((MQAddress) address);
                    MQOperation operation = findMqOperation(binding);
                    hasFocus = _parse(address) && _parse(operation);
                }
            }
        } */
        return hasFocus;
    }

    public synchronized String getLocation() {
        return Utils.safeString(location);
    }

    public void setLocation(String location) {
        //TODO
    }

    public synchronized String getTransportProtocol() {
        return Utils.safeString(transportProtocol);
    }

    public void setTransportProtocol(String transportProtocol) {
        
    }

    public synchronized String getUse() {
        return Utils.safeString(use);
    }

    public void setUse(String use) {
        //TODO
    }

    public synchronized String getEncodingStyle() {
        return Utils.safeString(encodingStyle);
    }

    public void setEncodingStyle(String encodingStyle) {
        //TODO
    }

    public synchronized String getAcknowledgementMode() {
        return Utils.safeString(ackMode);
    }

    public void setAcknowledgementMode(String ackMode) {
        //TODO
    }

    public synchronized String getLLPType() {
        return Utils.safeString(llpType);
    }

    public void setLLPType(String llpType) {
        //TODO
    }

    public synchronized String getStartBlockCharacter() {
        return Utils.safeString(startBlockCharacter);
    }

    public void setStartBlockCharacter(String startBlockChar) {
        //TODO
    }

    public synchronized String getEndBlockCharacter() {
        return Utils.safeString(endBlockCharacter);
    }

    public void setEndBlockCharacter(String endBlockChar) {
        //TODO
    }

    public synchronized String  getEndDataCharacter() {
        return Utils.safeString(endDataCharacter);
    }

    public void setEndDataCharacter(String endDataChar) {
        //TODO
    }

    public boolean isHLLPChecksumEnabled() {
        return false;
    }

    public void setHLLPChecksumEnabled(boolean enabled) {
        //TODO
    }

    public boolean isSequenceNoEnabled() {
        //TODO
        return false;
    }

    public void setSequenceNoEnabled(boolean enabled) {
        //TODO
    }

    public synchronized String getProcessingId() {
        return Utils.safeString(processingId);
    }

    public void setProcessingId(String processingId) {
        //TODO
    }

    public String getVersionId() {
        return Utils.safeString(versionId);
    }

    public void setVersionId(String versionId) {
        //TODO
    }

    public boolean isValidateMSH() {
        //TODO
        return false;
    }

    public void setValidateMSH(boolean validate) {
        //TODO
    }

    public boolean isSFTEnabled() {
        //TODO
        return false;
    }

    public void setSFTEnabled(boolean enabled) {
        //TODO
    }

    public synchronized String getSoftwareVendorOrganization() {
        return Utils.safeString(softwareVendorOrganization);
    }

    public void setSoftwareVendorOrganization(String softOrg) {
        //TODO
    }

    public synchronized String getSoftwareCertifiedVersionOrReleaseNo() {
        return Utils.safeString(softwareVersionOrReleaseNo);
    }

    public void setSoftwareCertifiedVersionOrReleaseNo(String versionOrReleaseNo) {
        //TODO
    }

    public synchronized String getSoftwareProductName() {
        return Utils.safeString(softwareProductName);
    }

    public void setSoftwareProductName(String name) {
        //TODO
    }

    public synchronized String getSoftwareBinaryId() {
        return Utils.safeString(softwareBinaryId);
    }

    public void setSoftwareBinaryId(String id) {
        //TODO
    }

    public String getSoftwareProductInformation() {
        return Utils.safeString(softwareProductInfo);
    }

    public void setSoftwareProductInformation(String info) {
        //TODO
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
