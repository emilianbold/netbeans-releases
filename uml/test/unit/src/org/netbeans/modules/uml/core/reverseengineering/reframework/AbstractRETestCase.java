package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
/**
 */
public class AbstractRETestCase extends AbstractUMLTestCase
{
    protected Element element;
    
    protected Element createBaseElement(IParserData data, String name)
    {
        Document doc = XMLManip.getDOMDocument();
        element      = XMLManip.createElement(doc, name);
        
        if (data != null)
            data.setEventData(element);
        return element;
    }
    
    protected void attr(String name, String value)
    {
        element.addAttribute(name, value);
    }
    
    protected void addToken(String kind, String value)
    {
        Node n = element.selectSingleNode("TokenDescriptors");
        if (n == null)
            n = XMLManip.createElement(element, "TokenDescriptors");

        Element e = (Element) n;
        
        Element tdp  = XMLManip.createElement(e, "TDescriptor");
        tdp.addAttribute("value", value);
        tdp.addAttribute("type", kind);
    }
}