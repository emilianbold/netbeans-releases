/*
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). You may not use this 
 * file except in compliance with the License.  You can obtain a copy of the
 *  License at http://www.netbeans.org/cddl.html
 *
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved
 *
 */
package org.netbeans.modules.edm.editor.dataobject;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import org.openide.ErrorManager;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.util.Utilities;

public class MashupDataNode extends DataNode {

    public static final int VALID = 1;
    public static final int ERROR = 0;
    public static final int WARNING = 2;
    private static final String IMAGE_ICON_BASE = "org/netbeans/modules/edm/editor/resources/mashup.png";
    static Image edmImg = Utilities.loadImage(IMAGE_ICON_BASE);
    static Image errorImg = Utilities.loadImage("org/netbeans/modules/edm/editor/resources/MashupError.png");
    static Image warningImg = Utilities.loadImage("org/netbeans/modules/edm/editor/resources/MashupWarning.png");
    private int state = VALID;

    public MashupDataNode(MashupDataObject obj) {
        super(obj, Children.LEAF);
        setIconBaseWithExtension(IMAGE_ICON_BASE);
    }

    public void setCollabState(int state) {
        this.state = state;
    }

    @Override
    public Image getIcon(int type) {
        Image img = edmImg;
        try {
            if (state == ERROR) {
                img = Utilities.mergeImages(edmImg, errorImg, 1, 0);
            } else if (state == WARNING) {
                img = Utilities.mergeImages(edmImg, warningImg, 1, 0);
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return img;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        fireIconChange();
    }
}
