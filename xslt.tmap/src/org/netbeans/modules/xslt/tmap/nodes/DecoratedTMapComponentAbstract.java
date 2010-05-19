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

import java.awt.Image;
import org.netbeans.modules.xslt.tmap.model.api.Nameable;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public abstract class DecoratedTMapComponentAbstract<T extends TMapComponent> 
        implements DecoratedTMapComponent<T> 
{

    private T myOrig;
    private Object myAlternativeRef;
    
    public DecoratedTMapComponentAbstract(T orig) {
        this(orig, null);
    }

    public DecoratedTMapComponentAbstract(T orig, Object alternativeRef) {
        myOrig = orig;
        myAlternativeRef = alternativeRef;
    }

    public String getName() {
        T ref = getReference();
        String name = null;
        if (ref != null && ref instanceof Nameable) {
            name = ((Nameable)ref).getName();
        }
        
        if (name != null) {
            return name;
        }
        
        NodeType type = NodeType.getNodeType(myOrig);
        return type != null ? type.getDisplayName(): "";
    }

    public String getDisplayName() {
        return getName();
    }

    public String getHtmlDisplayName() {
        return getName();
    }

    public String getTooltip() {
        return "";
    }

    public Image getIcon() {
        NodeType type = NodeType.getNodeType(myOrig);
        return type != null ? type.getImage(): NodeType.UNKNOWN_TYPE.getImage();
    }

    public T getReference() {
        return myOrig;
    }

    public Object getAlternativeReference() {
        return myAlternativeRef;
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
                return origComponent.equals(objComponent);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        TMapComponent origComponent = getReference();
        return origComponent == null ? origComponent.hashCode() : super.hashCode();
    }
    
}
