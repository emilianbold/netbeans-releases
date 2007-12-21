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
package org.netbeans.modules.xslt.tmap.model.impl;

import java.util.List;
import org.netbeans.modules.xslt.tmap.model.api.Service;
import org.netbeans.modules.xslt.tmap.model.api.TMapVisitor;
import org.netbeans.modules.xslt.tmap.model.api.TransformMap;
import org.w3c.dom.Element;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TransformMapImpl extends TMapComponentContainerImpl 
        implements TransformMap 
{
    
    public TransformMapImpl(TMapModelImpl model) {
        this(model, createNewElement(TMapComponents.TRANSFORM_MAP, model));
    }
    
    public TransformMapImpl(TMapModelImpl model, Element element) {
        super(model, element);
    }

    public List<Service> getServices() {
        return getChildren(Service.class);
    }

    public void removeService(Service service) {
        removeChild(TYPE.getTagName(), service);
    }

    public void addService(Service service) {
        addAfter(TYPE.getTagName(), service, TYPE.getChildTypes());
    }

    public int getSizeOfServices() {
        List<Service> services = getServices();
        return services == null ? 0 : services.size();
    }

    public void accept(TMapVisitor visitor) {
        visitor.visit(this);
    }
    
    public Class<TransformMap> getComponentType() {
        return TransformMap.class;
    }
}
