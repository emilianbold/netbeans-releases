/*
 * PartnerLinkTypeValidatorSchemaFactory.java
 *
 * Created on August 15, 2006, 6:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation.schema;

import java.io.InputStream;

import org.netbeans.modules.xml.wsdl.validator.spi.ValidatorSchemaFactory;

/**
 *
 * @author skini
 */
public class BPELPropertiesValidatorSchemaFactory extends ValidatorSchemaFactory{
    static final String bpwsXSDUrl = "/org/netbeans/modules/xml/wsdl/model/extensions/bpel/validation/schema/resources/wsbpel_msgprop-2005-12-22.xsd";
    
    @Override
    public String getNamespaceURI() {
        return "http://schemas.xmlsoap.org/ws/2004/03/business-process/";
    }
    
    @Override
    public InputStream getSchemaInputStream() {
        return BPELPropertiesValidatorSchemaFactory.class.getResourceAsStream(bpwsXSDUrl);
    }
}
