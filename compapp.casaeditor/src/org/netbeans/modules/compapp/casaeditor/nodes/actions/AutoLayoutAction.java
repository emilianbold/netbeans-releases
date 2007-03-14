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

package org.netbeans.modules.compapp.casaeditor.nodes.actions;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author jsandusky
 */
public class AutoLayoutAction extends AbstractAction {
    
    private static final Image ICON_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/nodes/resources/AutoLayout.png");
    
    private WeakReference mReference;
    
    
    public AutoLayoutAction(CasaModelGraphScene scene) {
        super(
                NbBundle.getMessage(AutoLayoutAction.class, "TXT_AutoLayout"), 
                new ImageIcon(ICON_IMAGE));
        mReference = new WeakReference(scene);
    }
    
    
    public void actionPerformed(ActionEvent e) {
        CasaModelGraphScene scene = (CasaModelGraphScene) mReference.get();
        if (scene != null) {
            scene.autoLayout(true, true);
        }
    }
}
