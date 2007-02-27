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

import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import java.lang.reflect.Method;
import org.netbeans.modules.visualweb.api.designer.HtmlDomProvider;
import org.netbeans.modules.visualweb.api.designer.markup.MarkupService;
import org.netbeans.modules.visualweb.insync.faces.Entities;
import org.netbeans.modules.visualweb.insync.live.DesignBeanNode;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

/**
 * Impl of <code>HtmlDomProvider.InlineEditorSupport</code>
 *
 * @author Peter Zavadsky
 * @author Tor Norby (old original code)
 */
class InlineEditorSupportImpl implements HtmlDomProvider.InlineEditorSupport {

    private final HtmlDomProviderImpl htmlDomProviderImpl;
    private final MarkupDesignBean markupDesignBean;
    private final DesignProperty   designProperty;
    
    /** Creates a new instance of InlineEditorSupportImpl */
    public InlineEditorSupportImpl(HtmlDomProviderImpl htmlDomProviderImpl, MarkupDesignBean markupDesignBean, DesignProperty designProperty) {
        this.htmlDomProviderImpl = htmlDomProviderImpl;
        this.markupDesignBean = markupDesignBean;
        this.designProperty = designProperty;
    }


//    public static HtmlDomProvider.InlineEditorSupport createDummyInlineEditorSupport() {
//        return new DummyInlineEditorSupport();
//    }
//    
//    private static class DummyInlineEditorSupport implements HtmlDomProvider.InlineEditorSupport {
//    } // End of DummyInlineEditorSupport.

    public boolean isEditingAllowed() {
        return HtmlDomProviderServiceImpl.isEditingAllowed(designProperty);
    }

    public String getValueSource() {
        return designProperty.getValueSource();
    }

    public void unset() {
        designProperty.unset();
    }

    public void setValue(String value) {
        designProperty.setValue(value);
    }

    public String getName() {
        return designProperty.getPropertyDescriptor().getName();
    }

    // XXX AttributeInlineEditor only.
    public String getSpecialInitValue() {
        return HtmlDomProviderServiceImpl.getSpecialInitValue(designProperty);
    }

    public String getValue() {
        // String assumption should be checked in beandescriptor search for TEXT_NODE_PROPERTY,
        // especially if we publish this property. Or we could at least specify that the
        // property MUST be a String.
        return (String)designProperty.getValue();
    }

    public String getDisplayName() {
        return designProperty.getPropertyDescriptor().getDisplayName();
    }

    public Method getWriteMethod() {
        return designProperty.getPropertyDescriptor().getWriteMethod();
    }

    public Element getRenderedElement() {
        Element sourceElement = markupDesignBean.getElement();
        return MarkupService.getRenderedElementForElement(sourceElement);
    }

    public DocumentFragment createSourceFragment() {
        return htmlDomProviderImpl.createSourceFragment(markupDesignBean);
    }

    public String expandHtmlEntities(String value, boolean warn) {
        return Entities.expandHtmlEntities(value, warn, markupDesignBean.getElement());
    }

    public boolean isEscaped() {
        return HtmlDomProviderServiceImpl.isEscapedDesignBean(markupDesignBean);
    }
}
