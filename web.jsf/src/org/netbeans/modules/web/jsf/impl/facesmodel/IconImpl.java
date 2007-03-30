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

package org.netbeans.modules.web.jsf.impl.facesmodel;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.web.jsf.api.facesmodel.*;
import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl
 */
public class IconImpl extends JSFConfigComponentImpl implements Icon{
    protected static final List<String> SORTED_ELEMENTS = new ArrayList();
    {
        SORTED_ELEMENTS.add(JSFConfigQNames.SMALL_ICON.getLocalName());
        SORTED_ELEMENTS.add(JSFConfigQNames.LARGE_ICON.getLocalName());
    }
    
    public IconImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }
    
    public IconImpl(JSFConfigModelImpl model) {
        super(model, createElementNS(model, JSFConfigQNames.ICON));
    }

    protected List<String> getSortedListOfLocalNames(){
        return SORTED_ELEMENTS;
    }
    
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }

    public String getSmallIcon() {
        return getChildElementText(JSFConfigQNames.SMALL_ICON.getQName(getModel().getVersion()));
    }

    public void setSmallIcon(String smallIcon) {
        setChildElementText(SMALL_ICON, smallIcon, JSFConfigQNames.SMALL_ICON.getQName(getModel().getVersion()));
    }

    public String getLargeIcon() {
        return getChildElementText(JSFConfigQNames.LARGE_ICON.getQName(getModel().getVersion()));
    }

    public void setLargeIcon(String largeIcon) {
        setChildElementText(LARGE_ICON, largeIcon, JSFConfigQNames.LARGE_ICON.getQName(getModel().getVersion()));
    }

    public String getLang() {
        return getAttribute(FacesAttributes.LANG);
    }

    public void setLang(String lang) {
        setAttribute(LangAttribute.LANG_ATTRIBUTE, FacesAttributes.LANG, lang);
    }
}
