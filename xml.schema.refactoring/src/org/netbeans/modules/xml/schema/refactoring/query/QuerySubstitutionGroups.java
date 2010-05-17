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
 * QuerySubstitutionGroups.java
 *
 * Created on January 16, 2006, 4:43 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.refactoring.query;

import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.xml.nbprefuse.AnalysisViewer;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.refactoring.query.views.QuerySubstitutionGroupsView;
import org.netbeans.modules.xml.schema.refactoring.ui.QueryPanel;
import org.netbeans.modules.xml.schema.refactoring.ui.QueryPanelContainer;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jeri Lockhart
 */
public class QuerySubstitutionGroups  implements Query {
    protected String displayName;
    private String shortName;
    private SchemaModel model;
    
    /** Creates a new instance of QuerySubstitutionGroups */
    public QuerySubstitutionGroups(final SchemaModel model) {
        super();
        this.model = model;
        shortName = NbBundle.getMessage(QuerySubstitutionGroups.class,
                "LBL_Query_SubstitutionGroups_ShortName");
        initialize();
    }
    
    private void initialize() {
        displayName = NbBundle.getMessage(QuerySubstitutionGroups.class,
                "LBL_QuerySubstitutionGroupsCustomizerPanel_desc");
        
    }

    
    public String toString() {
        return displayName;
    }
   
    /**
     * Setter for property displayName.
     *
     * @param displayName New value of property displayName.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    
    
    /**
     * Setter for property shortName - used on customizer column button
     *
     * @param shortName New value of property shortName.
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
    
    /**
     * Getter for property shortName - used on customizer column button
     *
     * @return Value of property shortName.
     */
    public String getShortName() {
        return shortName;
    }
    
    
    public void runQuery(final QueryPanel queryPanel, final AnalysisViewer analysisViewer) {
        final QuerySubstitutionGroupsView view =  new QuerySubstitutionGroupsView(
                    model);
            
            
            RequestProcessor.getDefault().post(new Runnable(){
                public void run() {
                    ProgressHandle ph = ProgressHandleFactory.createHandle(
                            NbBundle.getMessage(QuerySubstitutionGroups.class,
                            "LBL_Finding_Substitution_Groups"));
                    ph.start();
                    ph.switchToIndeterminate();
                    view.createModels();
                    
                    if(Thread.currentThread().isInterrupted()){
                        return;
                    }
                    ph.finish();
                    SwingUtilities.invokeLater(new Runnable(){
                    public void run() {
                        
                        if (view != null){
                            if (!queryPanel.getIsVisible()) {
                                // dock it into output window area and display
                                QueryPanelContainer cont =
                                        QueryPanelContainer.getUsagesComponent();
                                if (cont == null){
                                    ErrorManager.getDefault().log(
                                            ErrorManager.ERROR,
                                            "XML Schema Query Failed to open QueryPanelContainer. The problem could be that the XML settings and wstcref files in userdir Windows2Local are obsolete.  Try removing xml-schema-query.* and restart the IDE.");
                                    return;
                                }
                                cont.open();
                                cont.requestActive();
                                cont.addPanel(queryPanel);
                                queryPanel.setIsVisible(true);
                            }
                            
                            queryPanel.requestFocus();
                            
                            if (view != null){
                                view.showView(analysisViewer);
                                analysisViewer.validate();
                                analysisViewer.repaint();
                            }
                            
                        }
                    }
                });
                }});
                
         
    }

    public SchemaModel getModel() {
        return model;
    }
    
    
 
}
