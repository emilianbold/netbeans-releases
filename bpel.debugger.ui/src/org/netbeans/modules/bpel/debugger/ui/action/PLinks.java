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

package org.netbeans.modules.bpel.debugger.ui.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.netbeans.modules.bpel.debugger.ui.plinks.PLinksNodeModel;
import org.netbeans.modules.bpel.debugger.ui.plinks.PLinksTopComponent;
import org.openide.util.NbBundle;
import org.openide.util.ImageUtilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Kirill Sorokin
 */
public class PLinks extends AbstractAction {
    
    public PLinks() {
      // When changed, update also mf-layer.xml, where are the properties duplicated because of Actions.alwaysEnabled()
      putValue(NAME, NbBundle.getMessage(
              PLinks.class, "LBL_PLinksView")); // NOI18N
      putValue(SMALL_ICON, new ImageIcon(ImageUtilities.loadImage(
              PLinksNodeModel.PARTNER_LINK_ICON + ".gif"))); // NOI18N
    }
    
    public void actionPerformed(ActionEvent e) {
        final TopComponent view = WindowManager.getDefault().findTopComponent(
            PLinksTopComponent.VIEW_NAME);
        
        if (view == null) {
            throw new IllegalArgumentException(PLinksTopComponent.VIEW_NAME);
        }
        
        view.open();
        view.requestActive();
    }
    
    private static final long serialVersionUID = 1L; 
}
