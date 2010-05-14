package org.netbeans.modules.wsdlextensions.jms.impl;

import org.netbeans.modules.wsdlextensions.jms.JMSComponent;
import org.netbeans.modules.wsdlextensions.jms.JMSJCAOptions;
import org.netbeans.modules.wsdlextensions.jms.JMSQName;
import org.w3c.dom.Element;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

public class JMSJCAOptionsImpl extends JMSComponentImpl implements JMSJCAOptions {

    public JMSJCAOptionsImpl(WSDLModel model, Element e) {
        super(model, e);
    }

    public JMSJCAOptionsImpl(WSDLModel model) {
        this(model, createPrefixedElement(JMSQName.JMSJCAOPTIONS.getQName(), model));
    }

    public void accept(JMSComponent.Visitor visitor) {
        visitor.visit(this);
    }
}
