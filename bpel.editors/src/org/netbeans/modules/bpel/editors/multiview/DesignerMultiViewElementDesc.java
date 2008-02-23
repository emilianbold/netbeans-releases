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
package org.netbeans.modules.bpel.editors.multiview;

import java.awt.Image;
import java.beans.BeanInfo;
import java.io.Serializable;

import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.modules.bpel.core.BPELDataObject;
import org.netbeans.modules.bpel.editors.api.BpelEditorConstants;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author ads
 */
public class DesignerMultiViewElementDesc implements MultiViewDescription,
    Serializable
{

    private static final long serialVersionUID = 1L;
    
    public static final String PREFERRED_ID = BpelEditorConstants.BPEL_DESIGNMV_PREFFERED_ID;
    
    private static final String LBL_DESIGN = "LBL_TAB_Design";

    // for deserialization
    private DesignerMultiViewElementDesc() {
    }
    
    public DesignerMultiViewElementDesc(BPELDataObject dataObject) {
        myDataObject = dataObject;
    }
    
    public MultiViewElement createElement() {
        return new DesignerMultiViewElement(myDataObject);
    }
    
    public String getDisplayName() {
        return NbBundle.getBundle(getClass()).getString(LBL_DESIGN);
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public Image getIcon() {
        return myDataObject.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
        
    }

    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }
    
    public String preferredID() {
        return PREFERRED_ID;
    }

    private BPELDataObject myDataObject;
}
