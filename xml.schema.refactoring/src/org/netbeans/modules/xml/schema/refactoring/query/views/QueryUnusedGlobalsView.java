/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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


