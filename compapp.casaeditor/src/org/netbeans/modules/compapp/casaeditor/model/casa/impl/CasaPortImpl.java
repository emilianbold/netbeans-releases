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
package org.netbeans.modules.compapp.casaeditor.model.casa.impl;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpointRef;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentVisitor;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConsumes;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaLink;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaProvides;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaQName;
import org.w3c.dom.Element;

/**
 *
 * @author jqian
 */
public class CasaPortImpl extends CasaComponentImpl implements CasaPort {
    
    public CasaPortImpl(CasaModel model, Element element) {
        super(model, element);
    }
    
    public CasaPortImpl(CasaModel model) {
        this(model, createElementNS(model, CasaQName.PORT));
    }    
         
    public void accept(CasaComponentVisitor visitor) {
        visitor.visit(this);
    }
    
    public int getX() {
        return Integer.parseInt(getAttribute(CasaAttribute.X));
    }
    
    public void setX(int x) {
        setAttribute(X_PROPERTY, CasaAttribute.X, new Integer(x).toString());
    }
    
    public int getY() {
        return Integer.parseInt(getAttribute(CasaAttribute.Y));
    }
    
    public void setY(int y) {
        setAttribute(Y_PROPERTY, CasaAttribute.Y, new Integer(y).toString());
    }    
    
    public String getState() {
        return getAttribute(CasaAttribute.STATE);
    }

    public void setState(String state) {
        setAttribute(STATE_PROPERTY, CasaAttribute.STATE, state);
    }
        
    public CasaLink getLink() {
        return getChild(CasaLink.class);
    }
    
    public void setLink(CasaLink link) {
        List<Class<? extends CasaComponent>> empty = Collections.emptyList();
        setChild(CasaLink.class, LINK_PROPERTY, link, empty);
    }
    
//
//    public String getBindingState() {
//        return getAttribute(CasaAttribute.BINDINGSTATE.getName());
//    }
//
//    public void setBindingState(String bindingState) {
//        setAttribute(CasaAttribute.BINDINGSTATE.getName(), bindingState);
//    }
    
//    public String getPortType() {
//        return getAttribute(CasaAttribute.PORTTYPE);
//    }
//    
//    public void setPortType(String portType) {
//        setAttribute(PORTTYPE_PROPERTY, CasaAttribute.PORTTYPE, portType);
//    }
    
    public String getBindingType() {
        return getAttribute(CasaAttribute.BINDINGTYPE);
    }
    
    public void setBindingType(String bindingType) {
        setAttribute(BINDINGTYPE_PROPERTY, CasaAttribute.BINDINGTYPE, bindingType);
    }
    
    public CasaConsumes getConsumes() {
        return getChild(CasaConsumes.class);
    }
    
    public void setConsumes(CasaConsumes casaConsumes) {
        List<Class<? extends CasaComponent>> empty = Collections.emptyList();
        setChild(CasaConsumes.class, CONSUMES_PROPERTY, casaConsumes, empty);
    }
    
    public CasaProvides getProvides() {
        return getChild(CasaProvides.class);
    }
    
    public void setProvides(CasaProvides casaProvides) {
        List<Class<? extends CasaComponent>> empty = Collections.emptyList();
        setChild(CasaProvides.class, PROVIDES_PROPERTY, casaProvides, empty);
    }
    
    // Convenience methods
    
    public String getEndpointName() {        
        CasaEndpointRef endpointRef = getConsumes();
        if (endpointRef == null) {
            endpointRef = getProvides();
        }
        return endpointRef.getEndpointName();
    }
}
