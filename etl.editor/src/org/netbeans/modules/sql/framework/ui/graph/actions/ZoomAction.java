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
package org.netbeans.modules.sql.framework.ui.graph.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import org.netbeans.modules.sql.framework.ui.zoom.ZoomComboBox;


/**
 * To change this template use Options | File Templates.
 * 
 * @author Ritesh Adval
 * @version $Revision$
 */
public class ZoomAction extends GraphAction {
    /**
     * called when this action is performed in the ui
     * 
     * @param ev event
     */
    public void actionPerformed(ActionEvent ev) {
        // no operation get component is actually used instead of action
    }

    public Component getComponent() {
        ZoomComboBox zoomCb = new ZoomComboBox();
        return zoomCb;
    }
}
