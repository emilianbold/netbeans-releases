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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.cookies.LineCookie;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;

import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

public final class ValidationUtil {
    
    private ValidationUtil() {}
    
    public static List<ResultItem> filterBpelResultItems(List<ResultItem> validationResults) {
        List<ResultItem> bpelResultItems = new ArrayList<ResultItem>();
        
        for(ResultItem resultItem: validationResults) {
            Component component = resultItem.getComponents();

            if(component instanceof BpelEntity) {
                ResultItem bpelResultItem = 
                    new ResultItem(resultItem.getValidator(),
                        resultItem.getType(), component, 
                        resultItem.getDescription());
                bpelResultItems.add(bpelResultItem);
            }
        }
        return bpelResultItems;
    }
    
    public static boolean equals(ResultItem item1, ResultItem item2){
        if (item1 == item2){
            return true;
        }
        if(!item1.getDescription().equals(item2.getDescription())) {
            return false;
        }
        
        if(!item1.getType().equals(item2.getType())) {
            return false;
        }
        
        Component components1 = item1.getComponents();
        Component components2 = item2.getComponents();
        
        if(components1 != components2) {
            return false;
        }
        return true;
    }

    private static boolean contains(List<ResultItem> list, ResultItem resultItem) {
        assert list!=null;
        for (ResultItem item: list) {
            if (equals(item, resultItem)){
                return true;
            }
        }
        return false;
    }

    public static Line getLine(ResultItem item) {
      int number;
      Component component = item.getComponents();

      if (component != null) {
        number = -1;

        if (component instanceof DocumentComponent) {
          number = getLineNumber((DocumentComponent) item.getComponents());
        } 
      }
      else {
        number = item.getLineNumber() - 1;
      }
//System.out.println("  number: " + number);

      if (number < 1) {
        return null;
      }
      FileObject file = getFileObjectByModel(component == null ? item.getModel() : component.getModel());

      if (file == null) {
        return null;
      }
      LineCookie cookie = null;

      try {
        DataObject data = DataObject.find(file);
        cookie = (LineCookie) data.getCookie(LineCookie.class);
      }
      catch (DataObjectNotFoundException e) {
        e.printStackTrace();
      }
      if (cookie == null) {
        return null;
      }
      Line line = cookie.getLineSet().getCurrent(number);

      if (line == null) {
        return null;
      }
      return cookie.getLineSet().getCurrent(number);
    }
    
    public static Line.Part getLinePart(ResultItem item) {
      Line line = getLine(item);

      if (line == null) {
        return null;
      }
      int column = getColumn(item.getComponents());

      if (column == -1) {
        column = 0;
      }
      int length = line.getText().length() - column;

      return line.createPart(column, length);
    }
    
    private static int getColumn(Component component) {
      AbstractDocument doc = getAbstractDocument(component);
      int position = findPosition((AbstractDocumentModel) component.getModel(), ((AbstractDocumentComponent) component).getPeer());
      return findColumn(doc, position);
    }
    
    private static AbstractDocument getAbstractDocument(Component component) {
      return (AbstractDocument) component.getModel().getModelSource().getLookup().lookup(AbstractDocument.class);
    }

    private static int findColumn(AbstractDocument doc, int argInt) {
      javax.swing.text.Element paragraphsParent = findLineRootElement(doc);
      int indx = paragraphsParent.getElementIndex(argInt);
      return argInt - paragraphsParent.getElement(indx).getStartOffset();
    }

    private static javax.swing.text.Element findLineRootElement(AbstractDocument doc) {
      javax.swing.text.Element element = doc.getParagraphElement(0).getParentElement();

      if (element == null) {
        element = doc.getDefaultRootElement();
      }
      return element;
    }

    private static int findPosition(AbstractDocumentModel model, Node node) {
      Element root = ((DocumentComponent) model.getRootComponent()).getPeer();
      javax.swing.text.Document doc = model.getBaseDocument();

      try {
        String buf = doc.getText(0, doc.getLength());
      
        if (node instanceof Element) {
          return findPosition((Element) node, buf, root, getRootElementPosition(buf, root));
        }
      }
      catch (BadLocationException e) {}

      return -1;
    }

    private static int getRootElementPosition(String buf, Element root) {
      NodeList children = root.getOwnerDocument().getChildNodes();
      int pos = 0;

      for (int i = 0; i < children.getLength(); i++) {
        Node n = children.item(i);

        if (n != root) {
          String s = n.getNodeValue();
        
          if (s != null) {
            pos += s.length();
          }
        }
        else {
          break;
        }
      }
      return buf.indexOf(root.getTagName(), pos);
    }

    private static int findPosition(Element target, String buf, Element base, Integer fromPos) {
      if (target == base) {
        return fromPos;
      }
      NodeList children = base.getChildNodes();

      for (int i = 0; i < children.getLength(); i++) {
        Node node = children.item(i);

        if ( !(node instanceof Element)) {
          String s = node.getNodeValue();

          if (s == null) {
            s = node.getTextContent();
          }
          if (s != null) {
            fromPos += s.length();
          }
          continue;
        }
        Element current = (Element) children.item(i);
        String tag = "<" + current.getTagName();
        fromPos = buf.indexOf(tag, fromPos);

        if (current == target) {
          return fromPos;
        }
        int found = findPosition(target, buf, current, fromPos);

        if (found > -1) {
          return found;
        }
      }
      return -1;
    }

    private static int getLineNumber(DocumentComponent entity) {
      if (entity == null) {
        return -1;
      }
      Model model = entity.getModel();

      if (model == null) {
        return -1;
      }
      ModelSource source = model.getModelSource();

      if (source == null) {
        return -1;
      }
      Lookup lookup = source.getLookup();

      if (lookup == null) {
        return -1;
      }
      StyledDocument document = (StyledDocument) lookup.lookup(StyledDocument.class);

      if (document == null) {
        return -1;
      }
      return NbDocument.findLineNumber(document, entity.findPosition());
    }

    private static FileObject getFileObjectByModel(Model model) {
      if (model == null) {
        return null;
      }
      ModelSource src = model.getModelSource();

      if (src == null) {
       return null;
      }
      Lookup lookup = src.getLookup();

      if (lookup == null) {
        return null;
      }
      return lookup.lookup(FileObject.class);
    }
}
