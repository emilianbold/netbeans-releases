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
