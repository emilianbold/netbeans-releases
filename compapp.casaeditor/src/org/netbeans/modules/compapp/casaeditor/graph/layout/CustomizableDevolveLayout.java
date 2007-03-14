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

/*
* CustomizableDevolveLayout.java
*
* Created on November 28, 2006, 12:30 PM
*
* To change this template, choose Tools | Template Manager
* and open the template in the editor.
*/

package org.netbeans.modules.compapp.casaeditor.graph.layout;

import org.netbeans.api.visual.layout.SceneLayout;
import org.netbeans.api.visual.widget.Widget;

/**
 * A copy of the DevolveWidgetLayout class that allows access to
 * the animation flag.
 *
 * @author jsandusky
 */
public class CustomizableDevolveLayout extends SceneLayout {
    
    private Widget widget;
    private CustomizablePersistLayout devolveLayout;

    
    public CustomizableDevolveLayout (Widget widget, CustomizablePersistLayout devolveLayout) {
        super (widget.getScene ());
        assert devolveLayout != null;
        this.widget = widget;
        this.devolveLayout = devolveLayout;
    }

    
    public void setIsAdjustingForOverlapOnly(boolean isOverlapOnly) {
        this.devolveLayout.setIsAdjustingForOverlapOnly(isOverlapOnly);
    }
    
    public void setIsAnimating(boolean isAnimating) {
        this.devolveLayout.setIsAnimating(isAnimating);
    }
    
    public void setIsPersisting(boolean isPersisting) {
        this.devolveLayout.setIsPersisting(isPersisting);
    }
    
    protected void performLayout () {
        devolveLayout.layout(widget);
    }
}
