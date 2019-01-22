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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.vmd.midp.components;

import java.awt.datatransfer.Transferable;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.ComponentProducer.Result;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.common.AcceptPresenter;
import org.netbeans.modules.vmd.api.model.common.DesignComponentDataFlavorSupport;
import org.netbeans.modules.vmd.api.model.common.AcceptSuggestion;


/**
 *
 * 
 */
public class MidpAcceptTrensferableKindPresenter extends AcceptPresenter {
    
    protected Map<TypeID, String> typesMap;
    
    public MidpAcceptTrensferableKindPresenter() {
        super(AcceptPresenter.Kind.TRANSFERABLE);
        typesMap = new HashMap<TypeID, String>();
    }
    
    public MidpAcceptTrensferableKindPresenter addType(TypeID typeID, String propertyName) {
        if (propertyName == null || typeID == null)
            throw new IllegalArgumentException();
        if (typesMap.containsKey(typeID))
            throw new IllegalArgumentException("TypeId : " + typeID   + " is alredy registered in this presenter" ); // NOI18N
        
        typesMap.put(typeID, propertyName);
        return this;
    }
    
    @Override
    public boolean isAcceptable(Transferable transferable, AcceptSuggestion suggestion) {
        if (getComponent().getDocument().getSelectedComponents().size() > 1) 
            return false;
        if (typesMap.values().isEmpty())
            throw new IllegalArgumentException("No types to check. Use addNewType method to add types to check"); // NOI18N
        
        if (!transferable.isDataFlavorSupported(DesignComponentDataFlavorSupport.DESIGN_COMPONENT_DATA_FLAVOR))
            return false;
        DesignComponent  component = DesignComponentDataFlavorSupport.getTransferableDesignComponent(transferable);
        if (component == null)
            return false;
        if (typesMap.containsKey(component.getType()) && typesMap.get(component.getType()) != null)
            return true;
        return false;
//        return typesMap.get(component.getType()) != null;
    }
    
    @Override
    public Result accept(Transferable transferable, AcceptSuggestion suggestion) {
        DesignComponent component = DesignComponentDataFlavorSupport.getTransferableDesignComponent(transferable);
        String propertyName = typesMap.get(component.getType());
        if (propertyName == null)
            throw new IllegalStateException();
        getComponent().writeProperty(propertyName, PropertyValue.createComponentReference(component));
        return new ComponentProducer.Result(component);
    }
}
