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
package org.netbeans.modules.vmd.midp.components;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.ComponentProducer.Result;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.common.AbstractAcceptPresenter;
import org.netbeans.modules.vmd.api.model.common.DesignComponentDataFlavor;
import org.netbeans.modules.vmd.midp.components.resources.FontCD;
import org.netbeans.modules.vmd.midp.components.resources.ImageCD;
import org.netbeans.modules.vmd.midp.components.resources.TickerCD;
import org.openide.util.Exceptions;

/**
 *
 * @author Karol Harezlak
 */
public class MidpAcceptTrensferableKindPresenter extends AbstractAcceptPresenter {
    
    private static final String PROP_IMAGE = "image"; //NOI18N
    private static final String PROP_FONT = "font"; //NOI18N
    private static final String PROP_TICKER = "ticker"; //NOI18N
    
    public static AbstractAcceptPresenter createTickerAcceptPresenter() {
        return new MidpAcceptTrensferableKindPresenter().addType(TickerCD.TYPEID, PROP_TICKER);
    }
    
    public static AbstractAcceptPresenter createImageAcceptPresenter() {
        return new MidpAcceptTrensferableKindPresenter().addType(ImageCD.TYPEID, PROP_IMAGE);
    }
    
    public static AbstractAcceptPresenter createFontAcceptPresenter() {
        return new MidpAcceptTrensferableKindPresenter().addType(FontCD.TYPEID, PROP_FONT);
    }
    
    public static AbstractAcceptPresenter createImageItemFromImageAcceptPresenter() {
        return new MidpAcceptTrensferableKindPresenter().addType(ImageCD.TYPEID, PROP_IMAGE);
    }
    
    private Map<TypeID, String> typesMap;
    private DesignComponent component;
    private String propertyName;
    private DataFlavor dataFlavor;
    
    public MidpAcceptTrensferableKindPresenter() {
        super(AbstractAcceptPresenter.Kind.TRANSFERABLE);
        typesMap = new HashMap<TypeID, String>();
    }
    
    public MidpAcceptTrensferableKindPresenter addType(TypeID typeID, String propertyName) {
        if (propertyName == null || typeID == null)
            throw new IllegalArgumentException();
        if (typesMap.containsKey(typeID))
            throw new IllegalArgumentException("TypeId : " + typeID   +" is alredy registered in this presenter" ); //NOI18N
        
        typesMap.put(typeID, propertyName);
        return this;
    }

    public boolean isAcceptable(Transferable transferable) {
        if (typesMap.values().isEmpty())
            throw new IllegalArgumentException("No types to check. Use addNewType method to add types to check"); //NOI18N
        if (dataFlavor == null) {
            assert (getComponent() != null);
            dataFlavor = new DesignComponentDataFlavor(getComponent());
        }
        try {
            component = (DesignComponent) transferable.getTransferData(dataFlavor);
            if (typesMap.containsKey(component.getType())) {
                propertyName = typesMap.get(component.getType());
                return true;
            }
        } catch (UnsupportedFlavorException ex) {
            //Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }
    
    public Result accept(Transferable transferable) {
        getComponent().writeProperty(propertyName, PropertyValue.createComponentReference(this.component));
        propertyName = null;
        return new ComponentProducer.Result(this.component);
    }
}
