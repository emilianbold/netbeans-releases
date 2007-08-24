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
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.LocaleConfig;
import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl
 */

public class DefaultLocaleImpl extends JSFConfigComponentImpl implements DefaultLocale {
    
    public DefaultLocaleImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }
    
    public DefaultLocaleImpl(JSFConfigModelImpl model) {
        super(model, createElementNS(model, JSFConfigQNames.DEFAULT_LOCALE));
    }
    
    public void accept(JSFConfigVisitor visitor) {
    }

    public String getLocale() {
        return getText().trim();    
    }

    public void setLocale(String locale) {
        setText(LocaleConfig.DEFAULT_LOCALE, locale);
    }

    
}
