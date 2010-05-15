/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.compapp.projects.jbi.anttasks;

import java.io.File;
import java.io.FileFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.w3c.dom.Document;
import org.netbeans.modules.xml.xam.Component;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
/**
 * This is a temporary code which is copied from 
 *http://xml.netbeans.org/source/browse/xml/xam/src/org/netbeans/modules/xml/xam/dom/ReadOnlyAccess.java
 *because this code did not make through beta2. This class will not be required since this 
 *functionality will be provided by ReadOnlyAccess after beta2
 * @author radval
 */
public class ModelUtil {
    
    /** Creates a new instance of ModelUtil */
    public ModelUtil() {
    }
    
     private static javax.swing.text.Element findLineRootElement(AbstractDocument doc) {
        //checkDocParameter(doc);

        javax.swing.text.Element e = doc.getParagraphElement(0).getParentElement();

        if (e == null) {
            // try default root (should work for text/plain)
            e = doc.getDefaultRootElement();
        }

        return e;
    }
 
    /**
     *  Get line number from Component.
     */
    public static int getLineNumber(Component component) {
        AbstractDocument doc = getAbstractDocument(component);
        int position = findPosition((AbstractDocumentModel) component.getModel(), ((AbstractDocumentComponent) component).getPeer());
        return findLineNumber(doc, position) + 1;
    }
    
    
    /**
     *  Get column number from Component.
     */
    public static int getColumnNumber(Component component) {
        AbstractDocument doc = getAbstractDocument(component);
        int position = findPosition((AbstractDocumentModel) component.getModel(), ((AbstractDocumentComponent) component).getPeer());
        return findColumnNumber(doc, position);
    }
    
    
    private static int findLineNumber(AbstractDocument doc, int argInt) {
        javax.swing.text.Element paragraphsParent = findLineRootElement(doc);
        int retInt = paragraphsParent.getElementIndex(argInt); // argInt is offset
        return retInt;
    }
    
    private static int findColumnNumber(AbstractDocument doc, int argInt) {
        javax.swing.text.Element paragraphsParent = findLineRootElement(doc);
        int indx = paragraphsParent.getElementIndex(argInt); // argInt is offset
        int retInt = argInt - paragraphsParent.getElement(indx).getStartOffset();
        return retInt;
    }
    
     /**
     *  Get styled document from Component.
     */
    private static AbstractDocument getAbstractDocument(Component component) {
        int position = 0;
        AbstractDocument doc;
        
        doc = (AbstractDocument) component.getModel().
                getModelSource().getLookup().lookup(AbstractDocument.class);
        
        return doc;
    }
    
     private static int findPosition(AbstractDocumentModel model, Node node) {
        Element root = ((DocumentComponent) model.getRootComponent()).getPeer();
        javax.swing.text.Document doc = model.getBaseDocument();
        try {
            String buf = doc.getText(0, doc.getLength());
            if (node instanceof Element) {
                return findPosition((Element)node, buf, root, getRootElementPosition(buf, root));
            }
        } catch(BadLocationException e) {
            // just return -1
        }
        return -1;
    }
    
      private static int getRootElementPosition(String buf, Element root) {
        NodeList children = root.getOwnerDocument().getChildNodes();
        int pos = 0;
        for (int i=0; i<children.getLength(); i++) {
            Node n = children.item(i);
            if (n != root) {
                String s = n.getNodeValue();
                if (s != null) {
                    pos += s.length();
                }
            } else {
                break;
            }
        }
        pos = buf.indexOf(root.getTagName(), pos);
        assert pos > 0 : "Root element position should be greater than 0";
        return pos;
    }
    
    private static int findPosition(Element target, String buf, Element base, Integer fromPos) {
        if (target == base) {
            return fromPos;
        }
        
        NodeList children = base.getChildNodes();
        for (int i=0; i<children.getLength(); i++) {
            Node node = children.item(i);
            // condition is we always have argument fromPos be the index of the
            // previous element plus sum length of any text/comment/attribute
            if (! (node instanceof Element)) {
                String s = node.getNodeValue();
                if (s == null) {
                    s = node.getTextContent();
                }
                if (s != null) {
                    fromPos += s.length();
                }
                //TODO handle leading or trailing whitespaces for Attr
                continue;
            }
            Element current = (Element)children.item(i);
            String tag = "<" + current.getTagName(); //TODO use pattern to deal with space in-between
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
    
}
