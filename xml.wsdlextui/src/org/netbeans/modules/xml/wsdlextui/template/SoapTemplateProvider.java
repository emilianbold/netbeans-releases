package org.netbeans.modules.xml.wsdlextui.template;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;

import org.netbeans.modules.xml.wsdl.ui.spi.ExtensibilityElementTemplateProvider;
import org.netbeans.modules.xml.wsdl.ui.spi.ValidationInfo;
import org.netbeans.modules.xml.xam.Reference;
import org.openide.util.NbBundle;

public class SoapTemplateProvider extends ExtensibilityElementTemplateProvider {
    
    static final String soapTemplateUrl = "/org/netbeans/modules/xml/wsdlextui/template/template.xml";

    
    public InputStream getTemplateInputStream() {
        return SoapTemplateProvider.class.getResourceAsStream(soapTemplateUrl);
    }

    
    public String getLocalizedMessage(String str, Object[] objects) {
        return NbBundle.getMessage(SoapTemplateProvider.class, str, objects);
    }
    
    /**
     * Do any post processing on Binding and its child elements based on
     * the information available in corresponding PortType. This will be called
     * when binding is about to be added to definition. Note this binding is not yet added to definition.
     * @param wsdlTargetNamespace targetNamespace of wsdl where this binding will be added.
     * @param binding Binding for portType
     */
    public void postProcess(String  wsdlTargetNamespace, Binding binding) {
        SoapBindingPostProcessor processor = new SoapBindingPostProcessor();
        processor.postProcess(wsdlTargetNamespace, binding);
    }
    
    public void postProcess(String wsdlTargetNamespace, Port port) {
        SoapBindingPostProcessor processor = new SoapBindingPostProcessor();
        processor.postProcess(wsdlTargetNamespace, port);
    }
    
    /**
     * validate Binding and its child elements based on
     * the information available in corresponding PortType. This will be called
     * when binding user goes from portType configuration wizard to binding
     * configuration or when user changes subtype of binding in binding configuration.
     * Note this binding is not yet added to definition.
     * @param binding Binding for portType
     */
    public List<ValidationInfo> validate(Binding binding) {
        SoapBindingValidator validator = new SoapBindingValidator();
        return validator.validate(binding);
    }

    public List<ValidationInfo> validate(Port port) {
        return null;
    }
    

    
    

}
