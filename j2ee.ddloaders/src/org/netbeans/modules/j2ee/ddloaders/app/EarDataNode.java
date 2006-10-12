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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.app;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.openide.actions.OpenAction;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * A node to represent this ejb-jar.xml object.
 *
 * @author  Ludovic Champenois
 * @version 1.0
 */
public final class EarDataNode extends DataNode {
    
    private static final String DEPLOYMENT="deployment"; // NOI18N
 
    private EarDataObject dataObject;
   
    /** Name of property for spec version */
    public static final String PROPERTY_DOCUMENT_TYPE = "documentType"; // NOI18N
    
    /** Listener on dataobject */
    private PropertyChangeListener ddListener;

    private Action[] filteredActions;
    
    public EarDataNode (EarDataObject obj) {
        this (obj, Children.LEAF);
    }

    public EarDataNode (EarDataObject obj, Children ch) {
        super (obj, ch);
        dataObject=obj;
        setIconBaseWithExtension(dataObject.getIconBaseForValidDocument());
        initListeners();
    }

    /**
     * <strong>Note:</strong> Temporary fix for #77261. Remove when MultiView
     * editor is implemented for Enterprise Application deployment descriptor.
     */
    public Action[] getActions(boolean context) {
        if (filteredActions == null) {
            Action[] origActions = super.getActions(context);
            List/*<Action>*/ actions = new ArrayList/*<Action>*/();
            for (int i = 0; i < origActions.length; i++) {
                if (!(origActions[i] instanceof OpenAction)) {
                    actions.add(origActions[i]);
                }
            }
            filteredActions = (Action[]) actions.toArray(new Action[actions.size()]);
        }
        return filteredActions;
    }
    
    /** Initialize listening on adding/removing server so it is
     * possible to add/remove property sheets
     */
    private void initListeners(){
        ddListener = new PropertyChangeListener () {
            
            public void propertyChange (PropertyChangeEvent evt) {
                if (EarDataObject.PROP_DOCUMENT_DTD.equals (evt.getPropertyName ())) {
                    firePropertyChange (PROPERTY_DOCUMENT_TYPE, evt.getOldValue (), evt.getNewValue ());
                }
                if (DataObject.PROP_VALID.equals (evt.getPropertyName ())
                &&  Boolean.TRUE.equals (evt.getNewValue ())) {
                    removePropertyChangeListener (EarDataNode.this.ddListener);
                }
                if (EarDataObject.PROP_DOC_VALID.equals (evt.getPropertyName ())) {
                    if (Boolean.TRUE.equals (evt.getNewValue ()))
                        setIconBaseWithExtension(dataObject.getIconBaseForValidDocument());
                    else
                        setIconBaseWithExtension(dataObject.getIconBaseForInvalidDocument());
                }
                if (Node.PROP_PROPERTY_SETS.equals (evt.getPropertyName ())) {
                    firePropertySetsChange(null,null);
                }                
            }
            
        };
        getDataObject ().addPropertyChangeListener (ddListener);
    } 
    
    protected Sheet createSheet () {
        Sheet s = new Sheet ();
        Sheet.Set ss = new Sheet.Set ();
        ss.setName (DEPLOYMENT);
        ss.setDisplayName (NbBundle.getMessage (EarDataNode.class, "PROP_deploymentSet"));
        ss.setShortDescription (NbBundle.getMessage (EarDataNode.class, "HINT_deploymentSet"));  
        ss.setValue ("helpID", "TBD---Ludo ejbjar node");   // NOI18N
        
        Node.Property p = new PropertySupport.ReadOnly (
            PROPERTY_DOCUMENT_TYPE,
            String.class,
            NbBundle.getBundle(EarDataNode.class).getString("PROP_documentDTD"),
            NbBundle.getBundle(EarDataNode.class).getString("HINT_documentDTD")
        ) {
            public Object getValue () {
                return dataObject.getApplication().getVersion();
            }
        };
        ss.put (p);
        s.put (ss);
        
        return s;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx("TBD ejbjar file");//NOI18N
    }
    
}
