package org.netbeans.installer.utils.xml.visitors;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 *
 * @author Danila_Dugurov
 */
public class DomVisitor {
   
   private static final Logger LOG = Logger.getLogger("org.util.visitors.DomVisitor");
   
   public void visit(Node node) {
      final Class clazz = node.getClass();
      if (Element.class.isAssignableFrom(clazz)) {
         visit((Element) node);
      } else if (Document.class.isAssignableFrom(clazz)) {
         visit((Document) node);
      } else if (Text.class.isAssignableFrom(clazz)) {
         visit((Text) node);
      } else {
         LOG.logp(Level.SEVERE, "DomVisiter", "visit(Node node)",
                 "Unhandled node class type " + clazz.getName());
         throw new RuntimeException("unhandled node");
      }
   }
   
   public void visit(Document document) {
      visit(document.getDocumentElement());
   }
   
   public void visit(Element element) {
   }
   
   public void visit(Text text) {
   }
}
