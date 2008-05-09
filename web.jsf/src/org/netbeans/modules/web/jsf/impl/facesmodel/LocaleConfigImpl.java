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
    static { 
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
        setChildElementText(DEFAULT_LOCALE, locale.getLocale(), JSFConfigQNames.DEFAULT_LOCALE.getQName(getNamespaceURI()));
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
