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

import java.util.List;
import org.netbeans.modules.web.jsf.api.facesmodel.Application;
import org.netbeans.modules.web.jsf.api.facesmodel.LocaleConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.ViewHandler;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.w3c.dom.Element;


/**
 *
 * @author Petr Pisl
 */
public class ApplicationImpl extends JSFConfigComponentImpl implements Application {

    /** Creates a new instance of CondverterImpl */
    public ApplicationImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }
    
    public ApplicationImpl(JSFConfigModelImpl model) {
        this(model, createElementNS(model, JSFConfigQNames.APPLICATION));
    }
    
    public List<ViewHandler> getViewHandlers() {
        return getChildren(ViewHandler.class);
    }

    public void addViewHandler(ViewHandler handler) {
        appendChild(VIEW_HANDLER, handler);
    }

    public void addViewHandler(int index, ViewHandler handler) {
        insertAtIndex(VIEW_HANDLER, handler, index, ViewHandler.class);
    }

    public void removeViewHandler(ViewHandler handler) {
        removeChild(VIEW_HANDLER, handler);
    }

    public List<LocaleConfig> getLocaleConfig() {
        return getChildren(LocaleConfig.class);
    }

    public void addLocaleConfig(LocaleConfig locale) {
        appendChild(LOCALE_CONFIG, locale);
    }

    public void addLocaleConfig(int index, LocaleConfig locale) {
        insertAtIndex(LOCALE_CONFIG, locale, index, LocaleConfig.class);
    }

    public void removeLocaleConfig(LocaleConfig locale) {
        removeChild(LOCALE_CONFIG, locale);
    }

    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }
}
