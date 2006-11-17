/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 * 
 * $Id$
 */
package org.netbeans.installer.utils.xml.visitors;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 *
 * @author Danila_Dugurov
 */
public class DomVisitor {
   
   public void visit(Node node) {
      final Class clazz = node.getClass();
      if (Element.class.isAssignableFrom(clazz)) {
         visit((Element) node);
      } else if (Document.class.isAssignableFrom(clazz)) {
         visit((Document) node);
      } else if (Text.class.isAssignableFrom(clazz)) {
         visit((Text) node);
      } else {
         throw new RuntimeException("Unhandled node class type " + clazz.getName());
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
