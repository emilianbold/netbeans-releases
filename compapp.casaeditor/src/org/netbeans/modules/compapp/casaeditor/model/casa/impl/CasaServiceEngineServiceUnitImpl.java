/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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


import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConsumes;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentVisitor;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpointRef;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaProvides;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaQName;
import org.w3c.dom.Element;

/**
 *
 * @author jqian
 */
public class CasaServiceEngineServiceUnitImpl extends CasaServiceUnitImpl 
        implements CasaServiceEngineServiceUnit {
    
    public CasaServiceEngineServiceUnitImpl(CasaModel model, Element element) {
        super(model, element);
    }
    
    public CasaServiceEngineServiceUnitImpl(CasaModel model) {
        this(model, createElementNS(model, CasaQName.ENGINE_ENGINE_SERVICE_UNIT));
    }
    
    public void accept(CasaComponentVisitor visitor) {
        visitor.visit(this);
    }
//    
//    public CasaEndpoints getEndpoints() {
//        return getChild(CasaEndpoints.class);
//    }
//    
//    public void setEndpoints(CasaEndpoints endpoints) {
//        List<Class<? extends CasaComponent>> empty = Collections.emptyList();
//        setChild(CasaEndpoints.class, ENDPOINTS_PROPERTY, endpoints, empty);
//    }
    
    public void addConsumes(int index, CasaConsumes casaConsumes) {
        insertAtIndex(CONSUMES_PROPERTY, casaConsumes, index, CasaConsumes.class);
    }

    public void removeConsumes(CasaConsumes casaConsumes) {
        removeChild(CONSUMES_PROPERTY, casaConsumes);
    }

    public List<CasaConsumes> getConsumes() {
         return getChildren(CasaConsumes.class);
    }
           
    public void addProvides(int index, CasaProvides casaProvides) {
        insertAtIndex(PROVIDES_PROPERTY, casaProvides, index, CasaProvides.class);
    }

    public void removeProvides(CasaProvides casaProvides) {
        removeChild(PROVIDES_PROPERTY, casaProvides);
    }

    public List<CasaProvides> getProvides() {
         return getChildren(CasaProvides.class);
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

    public boolean isInternal() {
        return getAttribute(CasaAttribute.INTERNAL).equalsIgnoreCase(Constants.true_STRING);
    }

    public void setInternal(boolean internal) {
        setAttribute(INTERNAL_PROPERTY, CasaAttribute.INTERNAL, internal ? Constants.true_STRING : Constants.false_STRING);        
    }

    public boolean isDefined() {
        return getAttribute(CasaAttribute.DEFINED).equalsIgnoreCase(Constants.true_STRING);
    }

    public void setDefined(boolean defined) {
        setAttribute(DEFINED_PROPERTY, CasaAttribute.DEFINED, defined ? Constants.true_STRING : Constants.false_STRING);        
    }

    public boolean isUnknown() {
        return getAttribute(CasaAttribute.UNKNOWN).equalsIgnoreCase(Constants.true_STRING);
    }

    public void setUnknown(boolean unknown) {
        setAttribute(UNKNOWN_PROPERTY, CasaAttribute.UNKNOWN, unknown ? Constants.true_STRING : Constants.false_STRING);        
    }
        
    // Convenience methods
    public List<CasaEndpointRef> getEndpoints() {
        List<CasaEndpointRef> ret = new ArrayList<CasaEndpointRef>();
        ret.addAll(getConsumes());
        ret.addAll(getProvides());
        return ret;
    }
       
}