package org.netbeans.installer.utils.xml.visitors;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Danila_Dugurov
 */

public class RecursiveDomVisitor extends DomVisitor {
   
   public void visit(Element element) {
      final NodeList children = element.getChildNodes();
      for (int i = 0; i < children.getLength(); i++) {
         visit(children.item(i));
      }
   }
}
