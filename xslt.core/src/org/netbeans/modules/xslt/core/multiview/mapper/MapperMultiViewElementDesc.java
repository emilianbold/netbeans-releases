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

package org.netbeans.modules.xslt.core.multiview.mapper;

import java.awt.Image;
import java.beans.BeanInfo;
import java.io.Serializable;

import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.modules.xslt.core.XSLTDataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 * 
 */
public class MapperMultiViewElementDesc implements MultiViewDescription,
    Serializable
{

    private static final long serialVersionUID = 1L;
    public static final String PREFERRED_ID = "xslt-mapper";
    private static final String LBL_MAPPER = "LBL_TAB_Mapper";
    private XSLTDataObject myDataObject;

    // for deserialization
    private MapperMultiViewElementDesc() {
        super();
    }
    
    public MapperMultiViewElementDesc(XSLTDataObject dataObject) {
        myDataObject = dataObject;
    }
    
    public MultiViewElement createElement() {
        return new MapperMultiViewElement(myDataObject);
    }
    
    public String getDisplayName() {
        return NbBundle.getBundle(getClass()).getString(LBL_MAPPER);
    }
    
        public HelpCtx getHelpCtx() {
        return new HelpCtx("xslt_editor_design_about"); //NOI18N
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
}
