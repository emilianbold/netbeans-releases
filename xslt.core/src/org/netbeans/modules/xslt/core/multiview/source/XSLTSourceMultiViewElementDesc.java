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

package org.netbeans.modules.xslt.core.multiview.source;

import java.awt.Image;
import java.beans.BeanInfo;
import java.io.Serializable;

import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.xslt.core.XSLTDataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;


/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class XSLTSourceMultiViewElementDesc
        implements MultiViewDescription, Serializable 
{

    private static final long serialVersionUID = 1L;
    public static final String PREFERED_ID = "xsltsource";    //NOI18N
    private static final String DISPLAY_NAME = 
        "LBL_SourceMultiview_DisplayName";                      //NOI18N
    private XSLTDataObject myDataObject;
    
    // need for serialization
    private XSLTSourceMultiViewElementDesc() {
        super();
    }
        
    public XSLTSourceMultiViewElementDesc( XSLTDataObject dataObject ) {
                super();
        myDataObject = dataObject;
    }
    
    public MultiViewElement createElement() {
        if ( myDataObject.getEditorSupport()!=null ) {
            return new XSLTSourceMultiViewElement( myDataObject );
        }
        return MultiViewFactory.BLANK_ELEMENT;
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), DISPLAY_NAME ); 
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx("xslt_editor_source"); //NOI18N
    }
    
    public Image getIcon() {
        return getDataObject().getNodeDelegate().getIcon(
                                BeanInfo.ICON_COLOR_16x16);
    }
    
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }
    
    public String preferredID() {
        return PREFERED_ID;
    }
        
    private XSLTDataObject getDataObject() {
        return myDataObject;
    }    
}
