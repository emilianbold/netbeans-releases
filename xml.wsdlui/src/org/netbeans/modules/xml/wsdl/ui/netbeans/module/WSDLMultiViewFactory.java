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

package org.netbeans.modules.xml.wsdl.ui.netbeans.module;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jeri Lockhart
 */
public class WSDLMultiViewFactory {
    /**
     * Creates a new instance of WSDLMultiViewFactory
     */
    public WSDLMultiViewFactory() {
    }
    
    public static CloneableTopComponent createMultiView(WSDLDataObject wsdlDataObject) {
        MultiViewDescription views[] = new MultiViewDescription[3];
        
        views[0] = getWSDLSourceMultiviewDesc(wsdlDataObject);
        views[1] = getWSDLTreeViewMultiViewDesc(wsdlDataObject);
        views[2] = getWSDLDesignMultiviewDesc(wsdlDataObject);
        
        CloneableTopComponent multiview =
                MultiViewFactory.createCloneableMultiView(
                views,
                views[0],
                new WSDLEditorSupport.CloseHandler(wsdlDataObject));
        
        //IZ 84440 - show file name with extension
        String name = wsdlDataObject.getNodeDelegate().getDisplayName();
        multiview.setDisplayName(name);
        multiview.setName(name);
        
        
        return multiview;
    }
    
    
    private static MultiViewDescription getWSDLTreeViewMultiViewDesc(WSDLDataObject wsdlDataObject) {
        return new WSDLTreeViewMultiViewDesc(wsdlDataObject);
    }
    
    private static MultiViewDescription getWSDLSourceMultiviewDesc(WSDLDataObject wsdlDataObject) {
        return new WSDLSourceMultiviewDesc(wsdlDataObject);
    }
    
    private static MultiViewDescription getWSDLDesignMultiviewDesc(WSDLDataObject wsdlDataObject) {
        return new WSDLDesignMultiViewDesc(wsdlDataObject);
    }
    
    /**
     * Shows the desired multiview element. Must be called after the editor
     * has been opened (i.e. WSDLEditorSupport.open()) so the TopComponent
     * will be the active one in the registry.
     *
     * @param  id      identifier of the multiview element.
     */
    public static void requestMultiviewActive(String id) {
        TopComponent activeTC = TopComponent.getRegistry().getActivated();
        MultiViewHandler handler = MultiViews.findMultiViewHandler(activeTC);
        if (handler != null) {
            MultiViewPerspective[] perspectives = handler.getPerspectives();
            for (MultiViewPerspective perspective : perspectives) {
                if (perspective.preferredID().equals(id)) {
                    handler.requestActive(perspective);
                }
            }
        }
    }
    
    public static Boolean groupVisible = Boolean.FALSE; 
    
    public static void updateGroupVisibility(String preferredId) {
        WindowManager wm = WindowManager.getDefault();
        TopComponentGroup group = wm.findTopComponentGroup("wsdl_ui"); // NOI18N
        if (group == null) {
            return; // group not found (should not happen)
        }
        //
        boolean isDesignViewSelected = false;
        for (Mode mode : wm.getModes()) {
            TopComponent selected = mode.getSelectedTopComponent();
            if (selected != null) {
                MultiViewHandler mvh = MultiViews.findMultiViewHandler(selected);
                if (mvh != null) {
                    MultiViewPerspective mvp = mvh.getSelectedPerspective();
                    if (mvp != null) {
                        String id = mvp.preferredID();
                        if (preferredId.equals(id)) {
                            isDesignViewSelected = true;
                            break;
                        }
                    }
                }
            }
        }
        synchronized (groupVisible) {
        	if (isDesignViewSelected && !groupVisible) {
        		group.open();
        		groupVisible = Boolean.TRUE;
        	} else if (!isDesignViewSelected && groupVisible){
        		group.close();
        		groupVisible = Boolean.FALSE;
        	}
        }
        

    }
}
