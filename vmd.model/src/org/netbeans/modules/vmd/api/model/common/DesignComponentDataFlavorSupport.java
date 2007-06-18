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
package org.netbeans.modules.vmd.api.model.common;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import org.netbeans.modules.vmd.api.model.DesignComponent;

/**
 *
 * @author Karol Harezlak
 */
public final class DesignComponentDataFlavorSupport {

    private static String HUMAN_READABLE_NAME = "Design Component flavor"; //NOI18N
    public static DataFlavor DESIGN_COMPONENT_DATA_FLAVOR = new DataFlavor(DesignComponent.class, HUMAN_READABLE_NAME);
    
    public static DesignComponent getTransferableDesignComponent(Transferable transferable) {
        if (!transferable.isDataFlavorSupported(DesignComponentDataFlavorSupport.DESIGN_COMPONENT_DATA_FLAVOR))
            return null;
        DesignComponent componentTrans = null;
        try {
            componentTrans = (DesignComponent) transferable.getTransferData(DesignComponentDataFlavorSupport.DESIGN_COMPONENT_DATA_FLAVOR);
        } catch (UnsupportedFlavorException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return componentTrans;
    }
}
