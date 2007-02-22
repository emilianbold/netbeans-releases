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
 * View.java
 *
 * Created on October 26, 2005, 12:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.nbprefuse;

import java.beans.PropertyChangeListener;

/**
 *
 * @author Jeri Lockhart
 */
public interface View  extends PropertyChangeListener {

    /**
     * Create the models used for the graph, tree or explorer
     *  such as a prefuse Graph, a TreeModel, or a netbeans root Node
     */
    public Object[] createModels();
    
    
    /**
     *  Show the View in the analysis viewer
     *
     */
    public boolean showView(AnalysisViewer viewer);
    
    /**
     * SlowInSlowOut Pacer for initial animation of graph
     * 
     */
    public void usePacer(boolean use);
    
    /**
     *  Should the SchemaColumnView make the Column
     *  that the View is shown in as wide as possible?
     *  @return boolean true if View should be shown
     *    in a column as wide as the available horizontal space
     *    in the column view
     */
    public boolean getMaximizeWidth();
}
