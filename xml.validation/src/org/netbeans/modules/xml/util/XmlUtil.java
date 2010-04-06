/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
package org.netbeans.modules.xml.util;

import java.io.File;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.netbeans.modules.xml.xam.Named;
import static org.netbeans.modules.xml.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.04.14
 */
public final class XmlUtil {

    private XmlUtil() {}

    public static String getComponentName(Object component) {
        if (component == null) {
            return null;
        }
        String name = getGoodName(component);

        if (name != null) {
            return name;
        }
        name = component.getClass().getName();
        int k = name.lastIndexOf(".");

        if (k == -1) {
            return name;
        }
        return name.substring(k + 1);
    }

    public static String getGoodName(Object component) {
        if (component instanceof Named) {
            return ((Named) component).getName();
        }
        return null;
    }

    public static Line.Part getLinePart(ResultItem item) {
        Line line = getLine(item);

        if (line == null) {
            return null;
        }
        int column = getColumnNumber(item.getComponents());

        if (column == -1) {
            column = 0;
        }
        int length = line.getText().length() - column;

        return line.createPart(column, length);
    }

    public static Line getLine(ResultItem item) {
        int number;
        Component component = item.getComponents();

        if (component == null) {
            number = item.getLineNumber() - 1;
        } else {
            number = getLineNumber(item.getComponents());
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
        } catch (DataObjectNotFoundException e) {
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

    public static String getValidationError(File file, ResultItem result) {
//System.err.println();
//System.err.println("!!!!!!!: " + result.getModel() + " " + result.getComponents());
        StringBuilder builder = new StringBuilder();
        builder.append(getLocation(getFile(result, file), result.getComponents()));
        builder.append("\n" + result.getType().name() + ": " + result.getDescription()); // NOI18N
        return builder.toString();
    }

    private static File getFile(ResultItem result, File origin) {
        Model model = result.getModel();

        if (model == null) {
            return origin;
        }
        FileObject fileObject = getFileObjectByModel(model);

        if (fileObject == null) {
            return origin;
        }
        File file = FileUtil.toFile(fileObject);

        if (file == null) {
            return origin;
        }
        return file;
    }

    public static String getLocation(File file, Component component) {
        StringBuilder builder = new StringBuilder();

        if (file == null) {
            return builder.toString();
        }
        int line = getLineNumber(component) + 1;
        int column = getColumnNumber(component);

        if (file != null) {
            builder.append(file.getPath().replace("\\", "/")); // NOI18N;

            if (line != -1) {
                builder.append(":"); // NOI18N
                builder.append(line);
            }
            if (column != -1) {
                builder.append(": "); // NOI18N
                builder.append(column);
            }
        }
        return builder.toString();
    }

    private static int getColumnNumber(Component component) {
        AbstractDocument doc = getAbstractDocument(component);

        if (doc == null) {
            return -1;
        }
        int position = findPosition((AbstractDocumentModel) component.getModel(), ((AbstractDocumentComponent) component).getPeer());
        return findColumn(doc, position);
    }

    private static AbstractDocument getAbstractDocument(Component component) {
        if (component == null) {
            return null;
        }
        Model model = component.getModel();

        if (model == null) {
            return null;
        }
        ModelSource source = model.getModelSource();

        if (source == null) {
            return null;
        }
        return (AbstractDocument) source.getLookup().lookup(AbstractDocument.class);
    }

    private static int getLineNumber(Component component) {
        if (!(component instanceof DocumentComponent)) {
            return -1;
        }
        DocumentComponent entity = (DocumentComponent) component;

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

    private static int findColumn(AbstractDocument doc, int argInt) {
        javax.swing.text.Element paragraphsParent = findLineRootElement(doc);
        int indx = paragraphsParent.getElementIndex(argInt);
        return argInt - paragraphsParent.getElement(indx).getStartOffset();
    }

    private static int findPosition(AbstractDocumentModel model, Node node) {
        Element root = ((DocumentComponent) model.getRootComponent()).getPeer();
        javax.swing.text.Document doc = model.getBaseDocument();

        try {
            String buf = doc.getText(0, doc.getLength());

            if (node instanceof Element) {
                return findPosition((Element) node, buf, root, getRootElementPosition(buf, root));
            }
        } catch (BadLocationException e) {
            return -1;
        }
        return -1;
    }

    private static javax.swing.text.Element findLineRootElement(AbstractDocument doc) {
        javax.swing.text.Element element = doc.getParagraphElement(0).getParentElement();

        if (element == null) {
            element = doc.getDefaultRootElement();
        }
        return element;
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
            } else {
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

            if (!(node instanceof Element)) {
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

    public static FileObject getFileObjectByModel(Model model) {
      if (model == null) {
        return null;
      }
      ModelSource source = model.getModelSource();

      if (source == null) {
       return null;
      }
      Lookup lookup = source.getLookup();

      if (lookup == null) {
        return null;
      }
      return lookup.lookup(FileObject.class);
    }

    public static final String FOUND_VALIDATION_ERRORS = "Found validation error(s)."; // NOI18N
}
