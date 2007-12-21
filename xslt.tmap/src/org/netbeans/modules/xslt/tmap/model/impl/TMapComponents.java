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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.xslt.tmap.model.api.Service;
import org.netbeans.modules.xslt.tmap.model.api.Operation;
import org.netbeans.modules.xslt.tmap.model.api.Invoke;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.api.Param;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public enum TMapComponents {
    TRANSFORM_MAP("transformmap", ChildrenTypes.TRANSFORM_MAP_CHILDREN), // NOI18N
    SERVICE("service", ChildrenTypes.SERVICE_CHILDREN), // NOI18N
    OPERATION("operation", ChildrenTypes.OPERATION_CHILDREN), // NOI18N
    INVOKE("invoke", ChildrenTypes.INVOKE_CHILDREN),// NOI18N
    TRANSFORM("transform", ChildrenTypes.TRANSFORM_CHILDREN),// NOI18N
    PARAM("param", ChildrenTypes.PARAM_CHILDREN);// NOI18N

    private String myTagName;
    private ChildrenTypes myChildrenTypes;
            
    private TMapComponents(String tagName, ChildrenTypes childrenTypes) {
        myTagName = tagName;
        myChildrenTypes = childrenTypes;
    }

    public String getTagName() {
        return myTagName;
    }
   
    public Collection<Class<? extends TMapComponent>> getChildTypes() {
        return myChildrenTypes.getTypes();
    }
            
    public static enum ChildrenTypes {
        TRANSFORM_MAP_CHILDREN(createTransformMap()),
        SERVICE_CHILDREN(createService()),
        OPERATION_CHILDREN(createOperation()),
        INVOKE_CHILDREN(createInvoke()),
        TRANSFORM_CHILDREN(createTransform()),
        PARAM_CHILDREN(createParam());
        
        private Collection<Class<? extends TMapComponent>> myTypes;
        
        private ChildrenTypes(Collection<Class<? extends TMapComponent>> types) {
            myTypes = types;
        }
        
        public Collection<Class<? extends TMapComponent>> getTypes() {
            return myTypes;
        }
        
        private static Collection<Class<? extends TMapComponent>> createTransformMap() {
            Collection<Class<? extends TMapComponent>> children  = new ArrayList<Class<? extends TMapComponent>>(1);
            children.add(Service.class);
            return children;
        }
        
        private static Collection<Class<? extends TMapComponent>> createService() {
            Collection<Class<? extends TMapComponent>> children  = new ArrayList<Class<? extends TMapComponent>>(1);
            children.add(Operation.class);
            return children;
        }

        private static Collection<Class<? extends TMapComponent>> createOperation() {
            Collection<Class<? extends TMapComponent>> children  = new ArrayList<Class<? extends TMapComponent>>(2);
            children.add(Invoke.class);
            children.add(Transform.class);
            return children;
        }

        private static Collection<Class<? extends TMapComponent>> createInvoke() {
            Collection<Class<? extends TMapComponent>> children  = Collections.emptyList();
            return children;
        }

        private static Collection<Class<? extends TMapComponent>> createTransform() {
            Collection<Class<? extends TMapComponent>> children  = new ArrayList<Class<? extends TMapComponent>>(1);
            children.add(Param.class);
            return children;
        }

        private static Collection<Class<? extends TMapComponent>> createParam() {
            Collection<Class<? extends TMapComponent>> children  = Collections.emptyList();
            return children;
        }
    }
    
}
