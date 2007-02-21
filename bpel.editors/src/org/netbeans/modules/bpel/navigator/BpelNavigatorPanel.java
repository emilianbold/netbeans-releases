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


package org.netbeans.modules.bpel.navigator;

import java.util.Collection;
import javax.swing.JComponent;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.netbeans.modules.bpel.model.api.BpelModel;

/**
 *
 * @author Vitaly Bychkov
 * @version 6 December 2005
 */
public class BpelNavigatorPanel implements NavigatorPanel {
    
    private static String navPanelName = NbBundle.getMessage(BpelNavigatorPanel.class
            , "LBL_BPEL_LOGICAL_VIEW"); // NOI18N
    /** holds UI of this panel. */
    private JComponent myPanelUI;
    /** current context to work on. */
    private Lookup.Result myDObjLookupResult;
    
    private final LookupListener selectionListener = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            
            setNewContent(myDObjLookupResult.allInstances());
        }
    };
    
    /** listener to bpelModel context changes. */
    private BpelModel bpelModel;
    
    //  private final ChangeEventListener myBpelModelChangeEventListener = new BpelModelChangeEventListener();
    
    public BpelNavigatorPanel() {
//        myPanelUI = new BpelNavigatorVisualPanel();
    }
    
    public static String getUName() {
        return navPanelName;
    }
    
    public String getDisplayName() {
        return navPanelName;
    }
    
    public String getDisplayHint() {
        return NbBundle.getMessage(BpelNavigatorPanel.class
            , "LBL_BPEL_LOGICAL_VIEW_TOOLTIP"); // NOI18N
    }
    
    public JComponent getComponent() {
        if (myPanelUI == null) {
            myPanelUI = new BpelNavigatorVisualPanel();
        }
        return myPanelUI;
    }
    
    public void panelActivated(Lookup context) {
        myDObjLookupResult = context.lookup(
            new Lookup.Template(DataObject.class));
        assert myDObjLookupResult !=null;
        Collection datas = myDObjLookupResult.allInstances();
        if (datas == null || datas.size() != 1) {
        }
        DataObject curDataObject = datas == null || datas.size() != 1 ? null :
                (DataObject)datas.iterator().next();
//        if (curDataObject != null && getBpelModel(curDataObject) != null) {
            myDObjLookupResult.addLookupListener(selectionListener);
            selectionListener.resultChanged(null);
//        }
    }
    
    public void panelDeactivated() {
        if (myDObjLookupResult != null) {
            myDObjLookupResult.removeLookupListener(selectionListener);
            myDObjLookupResult = null;
        }
    }
    
    public Lookup getLookup() {
        // go with default activated Node strategy
        //Default mechanism chooses first Node from Utilities.actionsGlobalContext()
        //as activated Node for Navigator's TopComponent.
        return null;
    }
    
    private BpelModel getBpelModel(DataObject dobj) {
        Lookup dobjLookup = getLookup(dobj);
        return dobjLookup == null 
                ? null 
                : (BpelModel)dobjLookup.lookup(BpelModel.class);
    }
    
    private Lookup getLookup(DataObject dobj) {
        try {
        return dobj instanceof Lookup.Provider 
                ? ((Lookup.Provider)dobj).getLookup()
                : null;
        } catch (NullPointerException ex) {
            // temporary for 81507
            // TODO r 
        }
        return null;
    }
    
    /************* non - public part. ************/
    private void setNewContent(Collection newData) {
        
        if (newData == null || newData.size() != 1) {
            return;
        }
        
        final DataObject curDataObject = (DataObject) newData.iterator().next();

        Lookup dobjLookup = getLookup(curDataObject);
        BpelModel bpelModel = getBpelModel(curDataObject);
        
        if (bpelModel != null && this.bpelModel != bpelModel) {
            this.bpelModel = bpelModel;
            ((BpelNavigatorVisualPanel)getComponent())
            .navigate(dobjLookup, bpelModel);
            
        }
        
    }
}

