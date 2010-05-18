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

package org.netbeans.modules.xslt.tmap.navigator;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JComponent;
import org.netbeans.modules.xslt.tmap.TMapDataObject;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.netbeans.spi.navigator.NavigatorPanelWithUndo;
import org.openide.awt.UndoRedo;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;


/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TMapLogicalNavigatorPanel implements NavigatorPanel/*WithUndo*/ {

    private AtomicReference<UndoRedo.Manager> myUndoRedoRef = 
            new AtomicReference<UndoRedo.Manager>();
    
    /** holds UI of this panel. */
    private JComponent myComponent;
    /** current context to work on. */
    private Lookup.Result<DataObject> myContext;
    
    private TMapModel myModel;
    
    private static String NAV_PANEL_NAME = NbBundle.
            getMessage(TMapLogicalNavigatorPanel.class, "LBL_TMAP_LOGICAL_VIEW"); // NOI18N
    
    public static String getUName() {
        return NAV_PANEL_NAME;
    }
    
    private final LookupListener mySelectionListener = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            setNewContent();
        }
    };
    
    public TMapLogicalNavigatorPanel() {
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return NAV_PANEL_NAME;
    }

    /** {@inheritDoc} */
    public String getDisplayHint() {
        return NbBundle.getMessage(TMapLogicalNavigatorPanel.class, 
                "LBL_TMAP_LOGICAL_VIEW_TOOLTIP"); // NOI18N
    }

    /** {@inheritDoc} */
    public JComponent getComponent() {
        if (myComponent == null) {
            myComponent = new TMapLogicalPanel();
        }
        return myComponent;
    }

    /** {@inheritDoc} */
    public void panelActivated(Lookup context) {
        myContext = context.lookup(
            new Lookup.Template(DataObject.class));
        assert myContext !=null;
        DataObject curDataObject = getTMapDataObject();
        myContext.addLookupListener(mySelectionListener);
        mySelectionListener.resultChanged(null);
    }

    /** {@inheritDoc} */
    public void panelDeactivated() {
        if (myContext  != null) {
            myContext.removeLookupListener(mySelectionListener);
            myContext = null;
        }
    }

    /** {@inheritDoc} */
    public Lookup getLookup() {
        return null;
    }
    
    /** {@inheritDoc} */
//    public UndoRedo getUndoRedo() {
//        if (myUndoRedoRef.get() == null) {
//            myUndoRedoRef.compareAndSet(null, createUndoRedo());
//        }
//        return myUndoRedoRef.get();
//    }
//
//    private UndoRedo.Manager createUndoRedo() {
//        UndoRedo.Manager undoRedo = null;
//        TMapDataObject dObj = getTMapDataObject();
//        if (dObj != null) {
//             undoRedo = dObj.getEditorSupport().getUndoManager();
//        }
//
//        return undoRedo;
//    }

    private TMapDataObject getTMapDataObject() {
        TMapDataObject dObj = null;
        Collection<? extends DataObject> dObjs = myContext.allInstances();
        if (dObjs != null && dObjs.size() == 1) {
            DataObject tmpDObj = dObjs.iterator().next();
            if (tmpDObj.getClass() == TMapDataObject.class) {
                dObj = (TMapDataObject)tmpDObj;
            }
        }
        
        return dObj;
    }
    
    private TMapModel getModel(DataObject dOBj) {
        assert dOBj != null;
        Lookup lookup = dOBj.getLookup();
        return lookup != null ? lookup.lookup(TMapModel.class) : null;
    }
    
    private void setNewContent() {
        final DataObject curDataObject = getTMapDataObject();

        TMapModel tMapModel = null;
        if (curDataObject != null) {
            tMapModel = getModel(curDataObject);
        }
        
        if (tMapModel != null && myModel != tMapModel) {
            myModel = tMapModel;
            ((TMapLogicalPanel)getComponent()).
                    navigate(curDataObject.getLookup(), myModel);
            
        }
        
    }

}
