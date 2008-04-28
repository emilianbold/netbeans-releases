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

import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.nodes.properties.PropertyType;
import org.netbeans.modules.xslt.tmap.nodes.properties.PropertyUtils;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TransformNode extends TMapComponentNode<DecoratedTransform> {

    public TransformNode(Transform ref, Lookup lookup) {
        this(ref, Children.LEAF, lookup);
    }

    public TransformNode(Transform ref, Children children, Lookup lookup) {
        super(new DecoratedTransform(ref), children, lookup);
    }

    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        if (getReference() == null) {
            // The related object has been removed!
            return sheet;
        }
        //
        Sheet.Set mainPropertySet =
                getPropertySet(sheet);
        //
        Node.Property prop;
        prop = PropertyUtils.registerProperty(this, mainPropertySet,
                PropertyType.NAME,
                "getName", "setName"); // NOI18N
        prop.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        prop = PropertyUtils.registerProperty(this, mainPropertySet,
                PropertyType.FILE,
                "getFile", "setFile"); // NOI18N
        prop.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        prop = PropertyUtils.registerProperty(this, mainPropertySet,
                PropertyType.SOURCE,
                "getSource", "setSource"); // NOI18N
        prop.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        prop = PropertyUtils.registerProperty(this, mainPropertySet,
                PropertyType.RESULT,
                "getResult", "setResult"); // NOI18N
        prop.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        return sheet;
    }
    
    

}

