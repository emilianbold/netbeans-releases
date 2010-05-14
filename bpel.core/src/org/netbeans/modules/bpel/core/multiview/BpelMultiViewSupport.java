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

package org.netbeans.modules.bpel.core.multiview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.bpel.core.BPELDataEditorSupport;
import org.netbeans.modules.bpel.core.BPELDataObject;
import org.netbeans.modules.bpel.core.multiview.spi.BpelMultiviewProvider;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;


/**
 * @author ads
 *
 */
public final class BpelMultiViewSupport {

    // class should not be inst-ed
    private BpelMultiViewSupport() {
        super();
    }
    
    public static CloneableTopComponent createMultiView( final BPELDataObject dataObject) {
        Collection coll = getMultiviewProviders();
        MultiViewDescription views[] = new MultiViewDescription[coll.size()+1];
        
        
        // Put the source element first so that client code can find its
        // CloneableEditorSupport.Pane implementation.
        views[0] = new BPELSourceMultiViewElementDesc( dataObject ); 
        int i=0;
        for ( Object provider : coll ) {
            views[++i] = ((BpelMultiviewProvider)provider).
                createMultiview(dataObject);
        }
        
        CloneableTopComponent multiview =
            MultiViewFactory.createCloneableMultiView(
            views,
            views[0],
            new BPELDataEditorSupport.CloseHandler( dataObject ));
        String name = dataObject.getNodeDelegate().getDisplayName();
        multiview.setDisplayName(name);
        multiview.setName(name);
        return multiview;
    }
    
    public static final BpelMultiViewSupport getInstance() {
        return INSTANCE;
    }
    
    public void requestViewOpen(BPELDataEditorSupport support) {
        
        List<TopComponent> associatedTCs = new ArrayList<TopComponent>();
        DataObject targetDO = support.getDataObject();
        TopComponent activeTC = TopComponent.getRegistry().getActivated();
        if (targetDO ==  (DataObject) activeTC.getLookup().lookup(DataObject.class)) {
            associatedTCs.add(activeTC);
        }
        Set openTCs = TopComponent.getRegistry().getOpened();
        for (Object tc : openTCs) {
            TopComponent topComponent = (TopComponent) tc;
            if (targetDO == (DataObject)topComponent.getLookup().lookup(DataObject.class)) {
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
                if ( !mvp.preferredID().equals(BPELSourceMultiViewElementDesc.PREFERED_ID)) {
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
    
    private static Collection getMultiviewProviders() {
        Result<BpelMultiviewProvider> result = Lookup.getDefault().lookup(
                new Lookup.Template(BpelMultiviewProvider.class));
        Collection<? extends BpelMultiviewProvider> coll 
                = result.allInstances();
        assert coll != null;
        List<? extends BpelMultiviewProvider>  bpelMvProvs 
                = new ArrayList<BpelMultiviewProvider>(coll);

        Collections.sort(bpelMvProvs, new Comparator<BpelMultiviewProvider>() {

            public int compare(BpelMultiviewProvider o1, BpelMultiviewProvider o2) {
                int o1Weight = o1 == null ? -1 : o1.getSortWeight();
                int o2Weight = o2 == null ? -1 : o2.getSortWeight();
                
                return o1Weight < o2Weight ? -1 : o1Weight == o2Weight ? 0 : 1;
            }

        });
        return bpelMvProvs;
    }
    
    private final static BpelMultiViewSupport INSTANCE = new BpelMultiViewSupport();
}
