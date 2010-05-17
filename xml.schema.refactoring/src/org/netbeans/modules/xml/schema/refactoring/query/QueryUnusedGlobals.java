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
 * QueryUnusedGlobals.java
 *
 * Created on April 10, 2006, 2:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.refactoring.query;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.xml.nbprefuse.AnalysisViewer;
import org.netbeans.modules.xml.refactoring.ui.CancelGraph;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.refactoring.query.views.QueryUnusedGlobalsCustomizerPanel;
import org.netbeans.modules.xml.schema.refactoring.query.views.QueryUnusedGlobalsView;
import org.netbeans.modules.xml.schema.refactoring.ui.QueryPanel;
import org.netbeans.modules.xml.schema.refactoring.ui.QueryPanelContainer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jeri Lockhart
 */
public class QueryUnusedGlobals implements Query {
    
//    private boolean isCancelRequested;
    private String shortName;
    private String displayName;
    private SchemaModel model;
//    private FUnCustomizer customizer;
    Boolean excludeGEs;
    private CustomizerResults results;
    private QueryUnusedGlobalsView view;
    
    /** Creates a new instance of QueryUnusedGlobals */
    public QueryUnusedGlobals(SchemaModel model) {
        this.model = model;
        this.shortName = NbBundle.getMessage(
                QueryUnusedGlobals.class, "LBL_QueryUnusedGlobal_Shortname");
        this.displayName = NbBundle.getMessage(
                QueryUnusedGlobals.class, "LBL_QueryUnusedGlobal_Display_Name");
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
     * Setter for property displayName.
     *
     * @param displayName New value of property displayName.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Getter for property shortName - used on customizer column button
     *
     * @return Value of property shortName.
     */
    public String getShortName() {
        return shortName;
    }
    
    
    
    /**
     * Implement RunQuery interface
     *  
     *
     */
    public void runQuery(final QueryPanel queryPanel, final AnalysisViewer analysisViewer) {
        showCustomizerDialog();
        if (results.wasCanceled()){
            return;
        }
        excludeGEs = getExcludeGEs();
        view =  new QueryUnusedGlobalsView(model, excludeGEs);
        final CancelGraph cancelSignal = new CancelGraph();
        RequestProcessor.getDefault().post(new Runnable(){
            public void run() {
                ProgressHandle ph = ProgressHandleFactory.createHandle(
                        NbBundle.getMessage(QueryUnusedGlobals.class,
                        "LBL_Finding_Unused_Global_Components"),
                        cancelSignal
                        );
                ph.start();
                ph.switchToIndeterminate();
                view.createModels(cancelSignal); // CancelSignal
                if (cancelSignal.isCancelRequested()){
                    ph.finish();
                    return;
                }
                // uncomment to test progress bar
//                try {
//                    Thread.currentThread().sleep(5000);
//                } catch (InterruptedException ex) {
//                    ex.printStackTrace();
//                }
                
//                if(Thread.currentThread().isInterrupted()){
//                    return;
//                }
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
                            
                            
                            
//                            queryPanel.requestFocus();
                            
                            if (view != null){
                                view.showView(analysisViewer);
                                analysisViewer.validate();
                                analysisViewer.repaint();
//                                analysisViewer.requestFocus();
                            }
                        }
                    }
                });
            }});
            
    }
    
    /**
     *
     *
     */
    public String toString() {
        return displayName;
    }
 
    public CustomizerResults showCustomizerDialog() {
        results = new CustomizerResults();
        QueryUnusedGlobalsCustomizerPanel panel = new QueryUnusedGlobalsCustomizerPanel();
        results.setPanel(panel);
        String title = NbBundle.getMessage(
                QueryUnusedGlobals.class, "LBL_QueryUnusedGlobal_Display_Name");
        DialogDescriptor descriptor = new DialogDescriptor(panel, title);
        JDialog dialog = (JDialog) DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setTitle(title);
        dialog.validate();
//        dialog.getAccessibleContext().setAccessibleName(rui.getName());
//        dialog.getAccessibleContext().setAccessibleDescription(ResourceBundle.getBundle("org/netbeans/modules/xml/refactoring/ui/j/spi/ui/Bundle").getString("ACSD_FindUsagesDialog"));
        
        
        if (DialogDisplayer.getDefault().notify(descriptor) != NotifyDescriptor.OK_OPTION) {
            results.setWasCanceled(true);
        }
        return results;
    }
    
//    public View getView() {
//        return view;
//    }
    
    /**
     * Get user customization option from customizer panel
     *
     */
    private boolean getExcludeGEs(){
        if (results != null){
            JPanel panel = results.getPanel();
            if (panel instanceof QueryUnusedGlobalsCustomizerPanel){
                QueryUnusedGlobalsCustomizerPanel cPnl = QueryUnusedGlobalsCustomizerPanel.class.cast(panel);
                return cPnl.getExcludeElements();
            }
        }
        return false;
    }

    public SchemaModel getModel() {
        return model;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////
    
    
    /**
     *
     *
     *
     */
    public class FUnCustomizer {
//            implements QueryCustomizer {
        
        protected CustomizerResults[] constraints;
        protected SchemaComponentReference[] references;
        protected Query query;
        protected QueryUnusedGlobalsCustomizerPanel panel;
        
        /** Creates a new instance of QueryCustomizerSupport */
        public FUnCustomizer(Query query) {
            super();
            this.query = query;
            initialize();
        }
        
        protected void initialize() {
            panel = new QueryUnusedGlobalsCustomizerPanel();
//            panel.addPropertyChangeListener(QueryUnusedGlobals.this);
            
        }
        
        public void setExcludeElements(boolean exclude){
            panel.setExcludeElements(exclude);
        }
        
        
        /**
         *  Implement QueryCustomizer
         *
         */
        
        
//        public void setQueryConstraints(final CustomizerResults[] queryConstraints) {
//            if (queryConstraints == null){
//                this.constraints = null;
//            } else {
//                this.constraints = new CustomizerResults[queryConstraints.length];
//                System.arraycopy(queryConstraints, 0, this.constraints, 0, queryConstraints.length);
//            }
//        }
//        
//        public void setSchemaComponentReferences(final SchemaComponentReference[] schemaComponentReferences) {
//            if (schemaComponentReferences == null){
//                this.references = null;
//            } else {
//                this.references = new SchemaComponentReference[schemaComponentReferences.length];
//                System.arraycopy(schemaComponentReferences, 0, this.references, 0, schemaComponentReferences.length);
//            }
//        }
//        
//        public SchemaComponentReference[] getSchemaComponentReferences() {
//            SchemaComponentReference[] refsCopy = new SchemaComponentReference[references.length];
//            System.arraycopy(references, 0, refsCopy, 0, references.length);
//            return refsCopy;
//        }
//        
//        public JPanel getQueryCustomizerPanel() {
//            return panel;
//        }
//        
//        public CustomizerResults[] getQueryConstraints() {
//            CustomizerResults[] constraintsCopy = new CustomizerResults[constraints.length];
//            System.arraycopy(constraints, 0, constraintsCopy, 0, constraints.length);
//            return constraintsCopy;
//        }
    }
}
