package org.netbeans.installer.utils.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Danila_Dugurov
 */

public interface DomExternalizable {
   
   void readXML(Element element);
   
   Element writeXML(Document document);          
}
