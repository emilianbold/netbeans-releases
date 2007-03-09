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

package org.netbeans.modules.websvc.design.view;

import java.awt.Point;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Utilities;

/**
 *
 * @author Ajit Bhate
 */
public class DesignViewPopupProvider implements PopupMenuProvider{
    
    private Action[] actions;
    /** 
     * Creates a new instance of DesignPopupProvider 
     * @param actions actions represented by this.
     */
    public DesignViewPopupProvider(Action[] actions) {
        this.actions = actions;
    }
    
    public JPopupMenu getPopupMenu(Widget widget, Point point) {
        return Utilities.actionsToPopup(actions, 
                widget.getScene().getView().getComponentAt(point));
    }

}
