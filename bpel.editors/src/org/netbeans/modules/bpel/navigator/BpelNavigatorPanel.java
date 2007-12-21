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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.bpel.core.BPELDataObject;
import org.openide.awt.UndoRedo;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.project.BpelproProject;
import org.netbeans.modules.bpel.project.ProjectCloseListener;
import org.netbeans.spi.navigator.NavigatorPanelWithUndo;

/**
 *
 * @author Vitaly Bychkov
 * @version 6 December 2005
 */
public class BpelNavigatorPanel implements NavigatorPanelWithUndo {
    
    private static String navPanelName = NbBundle.getMessage(BpelNavigatorPanel.class
            , "LBL_BPEL_LOGICAL_VIEW"); // NOI18N
    /** holds UI of this panel. */
    private JComponent myPanelUI;
    /** current context to work on. */
    private Lookup.Result<DataObject> myDObjLookupResult;
    
    private DataObject myDataObject;
    private PropertyChangeListener myDObjChangeListener = 
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (DataObject.PROP_VALID.equals(evt.getPropertyName()) 
                            && !myDataObject.isValid()) 
                    {
                        ((BpelNavigatorVisualPanel)getComponent()).emptyPanel();
                    }
                }
    };
    
    private Project myProject;
    private ProjectCloseListener myProjectCloseListener = 
            new ProjectCloseListener() {
                public void projectClosed() {
                    ((BpelNavigatorVisualPanel)getComponent()).emptyPanel();
                }
    };
    
    private final LookupListener selectionListener = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            if (myDObjLookupResult == null) {
                return;
            }
            SwingUtilities.invokeLater(
                    new Runnable() {
                        public void run() {
                            if (myDObjLookupResult != null) {
                                setNewContent(myDObjLookupResult.allInstances());
                            }
                        }
            });
        }
    };
    
    /** listener to bpelModel context changes. */
    private BpelModel myBpelModel;
    
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
            new Lookup.Template<DataObject>(DataObject.class));
        assert myDObjLookupResult !=null;
        Collection<? extends DataObject> datas = myDObjLookupResult.allInstances();
        if (datas == null || datas.size() != 1) {
        }
        DataObject curDataObject = datas == null || datas.size() != 1 ? null :
                datas.iterator().next();
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
                : dobjLookup.lookup(BpelModel.class);
    }
    
    private Lookup getLookup(DataObject dobj) {
        try {
            return dobj != null ? dobj.getLookup() : null;
        } catch (NullPointerException ex) {
            // temporary for 81507
            // TODO r 
        }
        return null;
    }
    
    /************* non - public part. ************/
    private void setNewContent(Collection<? extends DataObject> newData) {
        // todo m
        if (newData == null || newData.size() < 1) {
            return;
        }
        final DataObject curDataObject = newData.iterator().next();

        Lookup dobjLookup = getLookup(curDataObject);
        BpelModel bpelModel = getBpelModel(curDataObject);
        if (bpelModel != null && myBpelModel != bpelModel) {
            myBpelModel = bpelModel;

            if (myDataObject != null) {
                myDataObject.removePropertyChangeListener(myDObjChangeListener);
            }
            if (myProject instanceof BpelproProject) {
                ((BpelproProject)myProject).
                        removeProjectCloseListener(myProjectCloseListener);
            }

            myDataObject = curDataObject;
            myDataObject.addPropertyChangeListener(myDObjChangeListener);
            myProject = getProject(myDataObject);
            if (myProject instanceof BpelproProject) {
                ((BpelproProject)myProject).
                        addProjectCloseListener(myProjectCloseListener);
            }
            
            ((BpelNavigatorVisualPanel)getComponent())
                .navigate(dobjLookup, myBpelModel);
            
        }
    }
    
    private Project getProject(DataObject dObj) {
        if (dObj == null) {
            return null;
        }
    
        return FileOwnerQuery.getOwner(dObj.getPrimaryFile());
    }
    
    private DataObject getDataObject() {
        DataObject dObj = null;
        if (myDObjLookupResult != null 
                && myDObjLookupResult.allInstances() != null 
                && !myDObjLookupResult.allInstances().isEmpty()) 
        {
            dObj = (DataObject)  myDObjLookupResult.allInstances().iterator().next();
        }
        
        return dObj;
    }

    public UndoRedo getUndoRedo() {
        DataObject dObj = getDataObject();
        if (dObj instanceof BPELDataObject) {
            return ((BPELDataObject)dObj).getEditorSupport().getUndoManager();
        }
        
        return UndoRedo.NONE;
    }
}

