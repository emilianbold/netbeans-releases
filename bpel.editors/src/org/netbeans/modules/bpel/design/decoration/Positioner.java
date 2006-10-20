/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.bpel.design.decoration;

import java.awt.Component;
import java.util.Collection;
import org.netbeans.modules.bpel.design.geom.FRectangle;
import org.netbeans.modules.bpel.design.model.elements.BorderElement;
import org.netbeans.modules.bpel.design.model.patterns.CompositePattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;

/**
 *
 * @author Alexey
 */
public abstract class Positioner {
    public abstract void position(Pattern pattern, Collection<Component> components,
            float zoom);

    public static FRectangle getPatternBounds(Pattern pattern) {
        FRectangle result = null;
        if (pattern instanceof CompositePattern) {
            BorderElement border = ((CompositePattern) pattern).getBorder();
            if (border != null) {
                result = border.getBounds();
            }
        } else {
            result = pattern.getFirstElement().getBounds();
        }
        
        if (result == null) {
            result = pattern.getBounds();
        }
        
        return result;
    }
}
