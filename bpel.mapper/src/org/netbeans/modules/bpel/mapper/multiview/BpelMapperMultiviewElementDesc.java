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
package org.netbeans.modules.bpel.mapper.multiview;

import java.awt.Image;
import java.io.Serializable;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.bpel.core.BPELDataObject;
import org.netbeans.modules.bpel.editors.api.BpelEditorConstants;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class BpelMapperMultiviewElementDesc 
        implements MultiViewDescription, Serializable 
{

    private static final long serialVersionUID = 1L;   
    
    /** unique ID of <code>TopComponent</code> (singleton) */
    public static final String GROUP_ID = "bpel_mapper_tcgroup";  //NOI18N

    private BPELDataObject myDataObject;

    public BpelMapperMultiviewElementDesc(BPELDataObject dObj) {
        myDataObject = dObj;
    }
    
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(BpelMapperMultiviewElementDesc.class,
                "LBL_BpelMapperMultiview_DisplayName"); // NOI18N
    }

    public Image getIcon() {
        return Utilities.loadImage(
                "org/netbeans/modules/bpel/mapper/resources/mapper.png",//NOI18N
                true);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }

    public String preferredID() {
        return BpelEditorConstants.BPEL_MAPPERMV_PREFFERED_ID;
    }

    public MultiViewElement createElement() {
        return new BpelMapperMultiviewElement(myDataObject);
//        return MultiViewFactory.BLANK_ELEMENT;
    }

}
