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

package org.netbeans.modules.xslt.tmap.nodes;

import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TransformMap;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class DecoratedImportsContainer extends DecoratedTMapComponentAbstract<TransformMap>{

    private static final String IMPORTS = NbBundle.getMessage(TMapComponentNode.class, 
                "LBL_Imports"); // 
    
    public DecoratedImportsContainer(TransformMap orig) {
        super(orig);
    }

    @Override
    public String getName() {
        return IMPORTS;
    }

    @Override
    public String getTooltip() {
        return NbBundle.getMessage(TMapComponentNode.class, 
                "LBL_ImportsTooltip");
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        
        if (obj instanceof DecoratedTMapComponent) {
            Object objComponent = ((DecoratedTMapComponent)obj).getReference();
            TMapComponent origComponent = getReference();
            if (origComponent != null ) {
                return origComponent.equals(objComponent) && getClass().equals(obj.getClass());
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        TMapComponent origComponent = getReference();
        return origComponent == null ? origComponent.hashCode()*getClass().hashCode() : super.hashCode();
    }
    
}
