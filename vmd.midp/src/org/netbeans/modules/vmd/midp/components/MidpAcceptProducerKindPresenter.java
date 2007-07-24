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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vmd.midp.components;

import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.AcceptPresenter;
import org.netbeans.modules.vmd.api.model.common.AcceptSuggestion;
import org.netbeans.modules.vmd.midp.components.categories.ResourcesCategoryCD;
import org.netbeans.modules.vmd.midp.components.resources.FontCD;
import org.netbeans.modules.vmd.midp.components.resources.ImageCD;
import org.netbeans.modules.vmd.midp.components.resources.TickerCD;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Karol Harezlak
 */

public class MidpAcceptProducerKindPresenter extends AcceptPresenter {
    private static final String PROP_IMAGE = "image"; //NOI18N
    private static final String PROP_FONT = "font"; //NOI18N
    private static final String PROP_TICKER = "ticker"; //NOI18N
    
    public static AcceptPresenter createTickerAcceptPresenter() {
        return new MidpAcceptTrensferableKindPresenter().addType(TickerCD.TYPEID, PROP_TICKER);
    }
    
    public static AcceptPresenter createImageAcceptPresenter() {
        return new MidpAcceptTrensferableKindPresenter().addType(ImageCD.TYPEID, PROP_IMAGE);
    }
    
    public static AcceptPresenter createFontAcceptPresenter() {
        return new MidpAcceptTrensferableKindPresenter().addType(FontCD.TYPEID, PROP_FONT);
    }
    private Map<TypeID, String> typesMap;
    private TypeID currentType;
    
    public MidpAcceptProducerKindPresenter() {
        super(Kind.COMPONENT_PRODUCER);
    }
    
    public MidpAcceptProducerKindPresenter addType(TypeID type, String propertyName) {
        if (type == null || propertyName == null || propertyName.length() == 0)
            throw new IllegalArgumentException("Illegal argument type or properytName"); // NOI18N
        if (typesMap != null && typesMap.keySet().contains(type))
            Debug.warning("TypeID: " + type.toString() + " alredy exists in presenter: " + MidpAcceptProducerKindPresenter.class + " - component: " + getComponent()); // NOI18N
        if (typesMap == null)
            typesMap = new HashMap<TypeID, String>();
        typesMap.put(type, propertyName);
        return this;
    }
    
    public boolean isAcceptable (ComponentProducer producer, AcceptSuggestion suggestion) {
        DescriptorRegistry registry = getComponent().getDocument().getDescriptorRegistry();
        for (TypeID type : typesMap.keySet()) {
            if (registry.isInHierarchy(type, producer.getMainComponentTypeID ())) {
                currentType = type;
                return true;
            }
        }
        return false;
    }
    
    public final ComponentProducer.Result accept (ComponentProducer producer, AcceptSuggestion suggestion) {
        DesignDocument document = getComponent().getDocument();
        DesignComponent resource = producer.createComponent(document).getMainComponent();
        MidpDocumentSupport.getCategoryComponent(document, ResourcesCategoryCD.TYPEID).addComponent(resource);
        getComponent().writeProperty(typesMap.get(currentType), PropertyValue.createComponentReference(resource));
        return new ComponentProducer.Result(resource);
    }
    
}

