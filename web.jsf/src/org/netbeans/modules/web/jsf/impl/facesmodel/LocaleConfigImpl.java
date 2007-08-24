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
import org.netbeans.modules.web.jsf.api.facesmodel.DefaultLocale;
import org.netbeans.modules.web.jsf.api.facesmodel.LocaleConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.SupportedLocale;
import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl
 */

public class LocaleConfigImpl extends JSFConfigComponentImpl implements LocaleConfig {
    
    protected static final List<String> SORTED_ELEMENTS = new ArrayList<String>();
    { 
        SORTED_ELEMENTS.add(JSFConfigQNames.DEFAULT_LOCALE.getLocalName());
        SORTED_ELEMENTS.add(JSFConfigQNames.SUPPORTED_LOCALE.getLocalName());
    }

    public LocaleConfigImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }
    
    public LocaleConfigImpl(JSFConfigModelImpl model) {
        super(model, createElementNS(model, JSFConfigQNames.LOCALE_CONFIG));
    }
    
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }

    public DefaultLocale getDefaultLocale() {
        return getChild(DefaultLocale.class);
    }

    public void setDefaultLocale(DefaultLocale locale) {
        setChildElementText(DEFAULT_LOCALE, locale.getLocale(), JSFConfigQNames.DEFAULT_LOCALE.getQName(getModel().getVersion()));
    }

    public List<SupportedLocale> getSupportedLocales() {
        return getChildren(SupportedLocale.class);
    }

    public void addSupportedLocales(SupportedLocale locale) {
        appendChild(SUPPORTED_LOCALE, locale);
    }

    public void addSupportedLocales(int index, SupportedLocale locale) {
        insertAtIndex(SUPPORTED_LOCALE, locale, index, SupportedLocale.class);
    }

    

}
