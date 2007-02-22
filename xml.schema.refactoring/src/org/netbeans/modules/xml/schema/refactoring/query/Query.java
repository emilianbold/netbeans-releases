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
 * Query.java
 *
 * Created on December 15, 2005, 11:47 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.refactoring.query;

import org.netbeans.modules.xml.nbprefuse.AnalysisViewer;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.refactoring.ui.QueryPanel;


/**
 *
 * @author Jeri Lockhart
 */
public interface Query {


    /**
     * Setter for property displayName.
     * @param displayName New value of property displayName.
     */
    public void setDisplayName(String displayName);


    /**
     * Getter for property queryCustomizer.
     * @return Value of property queryCustomizer.
     */
//    public QueryCustomizer createQueryCustomizer() ;

    /**
     * Getter for property shortName - used on customizer column button
     * @return Value of property shortName.
     */
    public String getShortName();

    /**
     * Setter for property shortName - used on customizer column button
     * @param shortName New value of property shortName.
     */
    public void setShortName(String shortName);
    
//    public CustomizerResults showCustomizerDialog();
    
    public void runQuery(final QueryPanel queryPanel, 
            final AnalysisViewer analysisViewer);
    
    /**
     * @returns schema model on which this query is run against.
     */
    public SchemaModel getModel();
    
//    public View getView();

//    public void showResults(final AnalysisViewer viewer);
}
