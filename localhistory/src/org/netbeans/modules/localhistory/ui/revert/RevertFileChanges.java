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
package org.netbeans.modules.localhistory.ui.revert;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import org.netbeans.modules.localhistory.store.StoreEntry;
import org.netbeans.modules.localhistory.ui.view.LocalHistoryFileView;
import org.netbeans.modules.localhistory.utils.Utils;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class RevertFileChanges extends RevertChanges implements PropertyChangeListener {
           
    void show(File[] roots) {
        LocalHistoryFileView view = new LocalHistoryFileView (roots);        
        view.getPanel().setPreferredSize(new Dimension(400, 250));
        if(show(view.getPanel())) {
            Node[] nodes = view.getExplorerManager().getSelectedNodes();
            revert(nodes); // XXX get selected nodes            
        }        
    }
    
    public static void revert(final Node[] nodes) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {                 
                Utils.revert(nodes);
            }
        });
    }        

    public void propertyChange(PropertyChangeEvent evt) {        
        setValid(isEnabled((Node[])evt.getNewValue()));        
    }   
    
    private boolean isEnabled(Node[] nodes) {
        if(nodes == null || nodes.length != 1) {
            return false;
        }        
        for(Node node : nodes) {
            StoreEntry se =  node.getLookup().lookup(StoreEntry.class);
            if(se == null) {
                return false;
            }            
        }
        return true; 
    }    
}
