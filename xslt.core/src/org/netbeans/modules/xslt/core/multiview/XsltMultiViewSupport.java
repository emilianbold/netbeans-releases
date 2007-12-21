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

package org.netbeans.modules.xslt.core.multiview;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.xslt.core.XSLTDataEditorSupport;
import org.netbeans.modules.xslt.core.XSLTDataObject;
import org.netbeans.modules.xslt.core.multiview.source.XSLTSourceMultiViewElementDesc;
import org.netbeans.modules.xslt.core.multiview.mapper.MapperMultiViewElementDesc;
import org.openide.loaders.DataObject;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;


/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class XsltMultiViewSupport {
    private final static XsltMultiViewSupport INSTANCE = new XsltMultiViewSupport();
    
    private XsltMultiViewSupport() {
    }
    
    public static final XsltMultiViewSupport getInstance() {
        return INSTANCE;
    }
    
    public static CloneableTopComponent createMultiView( final XSLTDataObject
            dataObject ) 
    {
        MultiViewDescription views[] =  {
            new XSLTSourceMultiViewElementDesc( dataObject ),
            new MapperMultiViewElementDesc( dataObject)
        };

        CloneableTopComponent multiview =
                MultiViewFactory.createCloneableMultiView(
                views,
                // source view invokes cloneableEditorSupport initialization (add listeners ...)
                views[0],
                new XSLTDataEditorSupport.CloseHandler( dataObject ));
        String name = dataObject.getNodeDelegate().getDisplayName();
        multiview.setDisplayName(name);
        multiview.setName(name);
        return multiview;
    }
    
    public void requestViewOpen(XSLTDataEditorSupport support) {
        
        List<TopComponent> associatedTCs = new ArrayList<TopComponent>();
        DataObject targetDO = support.getDataObject();
        TopComponent activeTC = TopComponent.getRegistry().getActivated();
        if (targetDO ==  (DataObject) activeTC.getLookup().lookup(DataObject.class)) {
            associatedTCs.add(activeTC);
        }
        Set openTCs = TopComponent.getRegistry().getOpened();
        for (Object tc : openTCs) {
            TopComponent topComponent = (TopComponent) tc;
            if (targetDO == (DataObject)topComponent.getLookup().lookup(
                    DataObject.class)) {
                associatedTCs.add(topComponent);
            }
        }
        
        // Use the first TC in the list that has the desired perspective
        boolean found = false;
        for (TopComponent targetTC: associatedTCs){
            MultiViewHandler handler =
                    MultiViews.findMultiViewHandler(targetTC);
            if ( handler==null) {
                continue;
            }
            MultiViewPerspective[] p = handler.getPerspectives();
            for (MultiViewPerspective mvp : p) {
                if ( !mvp.preferredID().equals(
                        XSLTSourceMultiViewElementDesc.PREFERED_ID)) {
                    handler.requestActive(mvp);
                    found = true;
                    break;
                }
            }
            if (found){
                break;
            }
        }
        
    }
}
