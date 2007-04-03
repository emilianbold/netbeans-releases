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

package org.netbeans.modules.visualweb.designer.jsf;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import org.netbeans.modules.visualweb.api.designer.Designer;
import org.netbeans.modules.visualweb.api.designer.cssengine.StyleData;
import org.netbeans.modules.visualweb.designer.html.HtmlTag;
import org.netbeans.modules.visualweb.insync.Util;
import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
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
        return HtmlDomProviderServiceImpl.getEditablePropertyNames(designBean);
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
            return HtmlDomProviderImpl.getComponentRootElementForMarkupDesignBean((MarkupDesignBean)designBean);
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

}
