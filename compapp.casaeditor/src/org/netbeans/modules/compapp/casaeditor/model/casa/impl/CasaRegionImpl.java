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
package org.netbeans.modules.compapp.casaeditor.model.casa.impl;

import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentVisitor;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaQName;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaRegion;
import org.w3c.dom.Element;

/**
 *
 * @author jqian
 */
public class CasaRegionImpl extends CasaComponentImpl implements CasaRegion {

    public CasaRegionImpl(CasaModel model, Element element) {
        super(model, element);
    }
    
    public CasaRegionImpl(CasaModel model) {
        this(model, createElementNS(model, CasaQName.REGION)); 
    }

    public void accept(CasaComponentVisitor visitor) {
        visitor.visit(this);
    }

    public String getName() {
        return getAttribute(CasaAttribute.NAME);
    }

//    public void setName(String name) {
//        setAttribute(NAME_PROPERTY, CasaAttribute.NAME, name);
//    }

    public int getWidth() {
        return Integer.parseInt(getAttribute(CasaAttribute.WIDTH));
    }

    public void setWidth(int width) {
        setAttribute(WIDTH_PROPERTY, CasaAttribute.WIDTH, new Integer(width).toString());
    }
}
