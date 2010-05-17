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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.xml.nbprefuse.AnalysisConstants;
import org.netbeans.modules.xml.nbprefuse.AnalysisViewer;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.refactoring.query.views.ComplexTypeDerivationsView;
import org.netbeans.modules.xml.schema.refactoring.query.views.WhereUsedExplorer;
import org.netbeans.modules.xml.schema.refactoring.query.views.WhereUsedExplorer.QueryCustomizerNode;
import org.netbeans.modules.xml.schema.refactoring.ui.QueryPanel;
import org.netbeans.modules.xml.schema.refactoring.ui.QueryPanelContainer;
import org.netbeans.modules.xml.schema.refactoring.ui.QueryUtilities;
import org.netbeans.modules.xml.xam.Named;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jeri Lockhart
 */
public class QueryComplexTypeDerivations  implements Query {
    protected String displayName;
    private WhereUsedExplorer customizer;
    private SchemaModel model;
    private String shortName;
    private CustomizerResults results;
    
    /** Creates a new instance of QuerySubstitutionGroups */
    public QueryComplexTypeDerivations(final SchemaModel model) {
        super();
        this.model = model;
        setDisplayName(NbBundle.getMessage(QueryComplexTypeDerivations.class,
                "LBL_QueryComplexTypeDerivations"));
        shortName = NbBundle.getMessage(QueryComplexTypeDerivations.class,
                "LBL_QueryComplexTypeDerivations_ShortName");
    }
    
    
    /**
     * Setter for property displayName.
     *
     * @param displayName New value of property displayName.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    
    public String toString() {
        return displayName;
    }
    
    
    
    public SchemaModel getSchemaModel() {
        return model;
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
        showCustomizerDialog();
        if (results != null && results.wasCanceled()){
            return;
        }
        SchemaComponent selectedComponent = customizer.getSelectedSchemaComponent();
        GlobalComplexType baseCT = null;
        if (selectedComponent instanceof GlobalComplexType){
            baseCT = (GlobalComplexType) selectedComponent;
        }
        else {
            return;
        }
        final ComplexTypeDerivationsView view =  new
                ComplexTypeDerivationsView( baseCT);
        
        RequestProcessor.getDefault().post(new Runnable(){
            public void run() {
                ProgressHandle ph = ProgressHandleFactory.createHandle(
                        NbBundle.getMessage(QueryComplexTypeDerivations.class,
                        "LBL_Finding_Derivations"));
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
    
    
    public CustomizerResults showCustomizerDialog() {
        results = new CustomizerResults();
        customizer = new CTExplorer(model);
        results.setPanel(customizer);
        String title = NbBundle.getMessage(
                QueryUnusedGlobals.class, "LBL_QueryComplexTypeDerivations_ShortName");
        DialogDescriptor descriptor = new DialogDescriptor(customizer, title);
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

    public SchemaModel getModel() {
        return model;
    }
    
    
    public class CTExplorer extends WhereUsedExplorer {
        
        static final private long serialVersionUID = 1L;
        
        public CTExplorer(SchemaModel model){
            super( model);
//            setNodePreferredAction(new ShowDerivationsAction());
        }
        
        
        
        @Override
        protected Node createTree() {
            // the previewsMap is retrieved later by the view, when the
            //   query in invoked  -  Map<GlobalComplexType,Preview>
            count = 0;
            Schema schema = model.getSchema();
            AbstractNode root = QueryUtilities.createCategoryNode(AnalysisConstants.GlobalTypes.BASE_COMPLEX_TYPES);
            String icon_str = ICON_BASE + COMPLEX_TYPE_IMAGE;
            root.setIconBaseWithExtension(icon_str);
            
            ArrayList<QueryCustomizerNode> baseCTs = new ArrayList<QueryCustomizerNode>();
            // get all the global complex types (gct)
            for(GlobalComplexType g : schema.getComplexTypes()) {
                createCustomizerNode(baseCTs, g, icon_str, false);                
            }
            // Update Status Area
            int count = baseCTs.size();
            
            if (count == 1) {
                StatusDisplayer.getDefault().setStatusText(
                        NbBundle.getMessage(QueryComplexTypeDerivations.class,
                        "LBL_1_ComplexType_with_Derivations_Found")
                        );
            } else {
                
                StatusDisplayer.getDefault().setStatusText(
                        MessageFormat.format(NbBundle.getMessage(QueryComplexTypeDerivations.class,
                        "LBL_ComplexTypes_with_Derivations_Found"),
                        new Object[] {count
                }
                ));
            }
            root.getChildren().add((QueryCustomizerNode[]) baseCTs.toArray(new QueryCustomizerNode[baseCTs.size()]));
            return root;
        }
        
        
        /**
         *
         * don't create duplicates
         */
        @Override
                protected void createCustomizerNode(List<QueryCustomizerNode> items, Named c, String icon_base_with_ext, boolean primitive){
            QueryCustomizerNode n  = null;
            if (primitive){
                n  = new QueryCustomizerNode(Children.LEAF, c, primitive, model);
            } else {
                n  = new QueryCustomizerNode(Children.LEAF, c, primitive);
            }
            
            n.setIconBaseWithExtension(icon_base_with_ext);
            n.setName(c.getName());
            for (QueryCustomizerNode nn: items){
                if (nn.getSchemaComponent() == c){
                    return;
                }
            }
            items.add(n);
            if (!primitive){
                count++;
            }
        }
        
        
    }
    
     
    
}
