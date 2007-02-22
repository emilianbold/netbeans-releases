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
/*
 * QueryUnusedGlobalsView.java
 *
 * Created on April 10, 2006, 5:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.refactoring.query.views;

import java.beans.PropertyChangeEvent;
import org.netbeans.modules.xml.nbprefuse.AnalysisViewer;
import org.netbeans.modules.xml.nbprefuse.View;
import org.netbeans.modules.xml.refactoring.ui.CancelSignal;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.refactoring.query.readers.QueryUnusedGlobalsReader;
import org.openide.nodes.Node;

/**
 *
 * @author Jeri Lockhart
 */
public class QueryUnusedGlobalsView implements View{
    private SchemaModel model;
    private Node root;
    private Boolean excludeGEs;
    
    /**
     * Creates a new instance of QueryUnusedGlobalsView
     */
    public QueryUnusedGlobalsView(final SchemaModel model, 
            final Boolean excludeGEs) {
        this.model = model;
        this.excludeGEs = excludeGEs;
    }
    
    
    
    public void usePacer(boolean use) {
    }
    
    
    /**
     *  Should the SchemaColumnView make the Column
     *  that the View is shown in as wide as possible?
     *  @return boolean true if View should be shown
     *    in a column as wide as the available horizontal space
     *    in the column view
     */
    public boolean getMaximizeWidth(){
        return false;
    }
    
    /**
     *  show a tree view of the global components
     *  that are defined in the current model and are
     *  not used in any of the schemas in the current project
     *
     *
     */
    public boolean showView(AnalysisViewer viewer) {
//        GraphUtilities.dumpNode(root);
        viewer.getPanel().setSize(250,200);
        viewer.setReshowOnResize(false);
        viewer.removeToolBar();
        viewer.addDisplayPanel(new ResultsPanel(root));
        return true;
    }
    
    
    /**
     *  Create a tree model with category nodes
     *  for each of the global types for which
     *  there are unused components.
     *  @param  cancelSignal createModels() will use this to check if the user
     *                       requested a cancel of the query
     *  @return  the root Node of a tree containing unused global components
     *           or null if the schema model does not contain any globals
     *           that aren't used
     *
     *
     */
    public Object[] createModels( ) {
        return null;
    }
    
    public Object[] createModels(CancelSignal cancelSignal) {
        QueryUnusedGlobalsReader reader =
                new QueryUnusedGlobalsReader();
        root = reader.findUnusedGlobals(cancelSignal, model,excludeGEs);
        
        return new Object[] {root};
        
    }
    
    /**
     * This method gets called when a bound property is changed.
     *
     * @param evt A PropertyChangeEvent object describing the event source
     *   	and the property that has changed.
     */
    public void propertyChange(PropertyChangeEvent evt) {
    }
    
   
}


