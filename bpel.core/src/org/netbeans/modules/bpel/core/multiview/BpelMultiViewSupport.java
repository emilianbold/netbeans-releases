/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.bpel.core.multiview;

import java.util.ArrayList;
import java.util.Collection;
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
    
    public static CloneableTopComponent createMultiView( final BPELDataObject 
            dataObject ) 
    {
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
            if (targetDO == (DataObject)topComponent.getLookup().lookup(
                    DataObject.class)) 
            {
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
                        BPELSourceMultiViewElementDesc.PREFERED_ID)) 
                {
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
        Result result = Lookup.getDefault().lookup(
                new Lookup.Template(BpelMultiviewProvider.class));
        return result.allInstances();
    }
    
    private final static BpelMultiViewSupport INSTANCE = new BpelMultiViewSupport();

}
