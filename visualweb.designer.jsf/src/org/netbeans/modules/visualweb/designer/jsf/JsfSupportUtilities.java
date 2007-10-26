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

package org.netbeans.modules.visualweb.designer.jsf;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import java.awt.Point;
import org.netbeans.modules.visualweb.api.designer.Designer;
import org.netbeans.modules.visualweb.api.designer.Designer.Box;
import org.netbeans.modules.visualweb.api.designer.cssengine.StyleData;
import org.netbeans.modules.visualweb.api.designtime.idebridge.DesigntimeIdeBridgeProvider;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;
import org.netbeans.modules.visualweb.designer.jsf.ui.JsfMultiViewElement;
import org.netbeans.modules.visualweb.insync.Util;
import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * Utilities class for the JSF support module.
 *
 * @author Peter Zavadsky
 */
public final class JsfSupportUtilities {
    
    /** Creates a new instance of JsfSupportUtilities */
    private JsfSupportUtilities() {
    }

    
    public static String[] getEditableProperties(DesignBean designBean) {
        return DomProviderServiceImpl.getEditablePropertyNames(designBean);
    }

    public static Designer findDesignerForDesignContext(DesignContext designContext) {
        Designer[] designers = JsfForm.findDesignersForDesignContext(designContext);
        return designers.length == 0 ? null : designers[0];
    }
    
    public static Designer findDesignerForJsfForm(JsfForm jsfForm) {
        Designer[] designers = JsfForm.findDesigners(jsfForm);
        return designers.length == 0 ? null : designers[0];
    }

    public static Element getComponentRootElementForDesignBean(DesignBean designBean) {
        if (designBean instanceof MarkupDesignBean) {
            return DomProviderImpl.getComponentRootElementForMarkupDesignBean((MarkupDesignBean)designBean);
        }
        return null;
    }
    
    public static JsfForm findJsfFormForDesignContext(DesignContext designContext) {
        return JsfForm.findJsfForm(designContext);
    }
    
    // XXX Also in designer/../DesignerUtils.
    /** Return true iff the string contains only whitespace */
    public static boolean onlyWhitespace(String s) {
//        if(DEBUG) {
//            debugLog(DesignerUtils.class.getName() + ".onlyWhitespace(String)");
//        }
        if(s == null) {
            return true;
        }
        int n = s.length();
        
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            
            /* See the "empty-cells" documentation in CSS2.1 for example:
             * it sounds like only SOME of the whitespace characters are
             * truly considered ignorable whitespace: \r, \n, \t, and space.
             * So do something more clever in some of these cases.
             */
            if (!Character.isWhitespace(c)) {
                return false;
            }
        }
        
        return true;
    }

    public static boolean isSpecialComponent(Element componentRootElement) {
        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        if (markupDesignBean == null) {
            return false;
        }
        return Util.isSpecialBean(markupDesignBean);
    }

    public static boolean isWebFormDataObject(DataObject dataObject) {
        return dataObject != null && isWebFormFileObject(dataObject.getPrimaryFile());
    }
    
    public static boolean isWebFormFileObject(FileObject fileObject) {
        return fileObject != null && FacesModel.getInstance(fileObject) != null;
    }
    
    public static void updateLocalStyleValuesForElement(Element e, StyleData[] setStyleData, StyleData[] removeStyleData) {
        Util.updateLocalStyleValuesForElement(e, setStyleData, removeStyleData);
    }

    public static boolean setStyleAttribute(Element componentRootElement, String attribute, int value) {
        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        if (markupDesignBean == null) {
            return false;
        }
        return Util.setDesignProperty(markupDesignBean, attribute, value);
    }
    
    public static Element getParentComponent(Element componentRootElement) {
        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        if (markupDesignBean == null) {
            return null;
        }

        DesignBean parent = markupDesignBean.getBeanParent();
        return parent instanceof MarkupDesignBean ? getComponentRootElementForMarkupDesignBean((MarkupDesignBean)parent) : null;
    }
    
    public /*private*/ static Element getComponentRootElementForMarkupDesignBean(MarkupDesignBean markupDesignBean) {
        return getComponentRootElementForDesignBean(markupDesignBean);
    }
    
    public static Element getComponentRootElementForElement(Element element) {
        return getComponentRootElementForDesignBean(MarkupUnit.getMarkupDesignBeanForElement(element));
    }
    
    public static Element getComponentRootElementFromNode(org.openide.nodes.Node node) {
        DesignBean bean = (DesignBean)node.getLookup().lookup(DesignBean.class);
        if (bean == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                new NullPointerException("No DesignBean for node=" + node)); // NOI18N
            return null;
        }

        return bean instanceof MarkupDesignBean ? getComponentRootElementForMarkupDesignBean((MarkupDesignBean)bean) : null;
    }
    
    public static Element findHtmlElementDescendant(DocumentFragment df) {
        return Util.findDescendant(HtmlTag.HTML.name, df);
    }

    public static Element findHtmlElementDescendant(Document doc) {
        if (doc == null) {
            return null;
        }
        return Util.findDescendant(HtmlTag.HTML.name, doc.getDocumentElement());
    }
    
    // XXX Copy also in designer/../GridHandler.
    /** Given absolute coordinates x,y in the viewport, compute
     * the CSS coordinates to assign to a box if it's parented by
     * the given parentBox such that the coordinates will result
     * in a box showing up at the absolute coordinates.
     * That was a really convoluted explanation, so to be specific:
     * If you have an absolutely positioned <div> at 100, 100,
     * and you drag a button into it such that it's its child,
     * and you drag it to screen coordinate 75, 150, then, in order
     * for the button to be rendered at 75, 150 and be a child of
     * the div its top/left coordinates must be -25, 50.
     */
    public static Point translateCoordinates(Box parentBox, int x, int y) {
        while (parentBox != null) {
//            if (parentBox.getBoxType().isPositioned()) {
            if (parentBox.isPositioned()) {
                x -= parentBox.getAbsoluteX();
                y -= parentBox.getAbsoluteY();

                return new Point(x, y);
            }

            if (parentBox.getPositionedBy() != null) {
                parentBox = parentBox.getPositionedBy();
            } else {
                parentBox = parentBox.getParent();
            }
        }

        return new Point(x, y);
    }

    public static void tcRepaint(Designer designer) {
        JsfMultiViewElement jsfMultiViewElement = JsfForm.findJsfMultiViewElementForDesigner(designer);
        if (jsfMultiViewElement == null) {
            return;
        }
        jsfMultiViewElement.getJsfTopComponent().repaint();
    }
    
    public static org.openide.nodes.Node getNodeRepresentation(Element componentRootElement) {
        MarkupDesignBean markupDesignBean = MarkupUnit.getMarkupDesignBeanForElement(componentRootElement);
        return DesigntimeIdeBridgeProvider.getDefault().getNodeRepresentation(markupDesignBean);
    }
    
}
