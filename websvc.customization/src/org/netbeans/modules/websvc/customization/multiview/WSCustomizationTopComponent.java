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

package org.netbeans.modules.websvc.customization.multiview;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.Set;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

/**
 * @author  Rico Cruz
 */
public class WSCustomizationTopComponent extends TopComponent {

    static final long serialVersionUID=6021472310161712674L;
    private boolean initialized = false;
    private WSPanelFactory panelFactory = null;
    private Set<WSDLModel> models;
    private Node node;
    private JaxWsModel jmodel;
    private Definitions primaryDefinitions;
    
    public WSCustomizationTopComponent(Node node, Set<WSDLModel> models, 
            Definitions primaryDefinitions, JaxWsModel jmodel){
        setLayout(new BorderLayout());        
        initialized = false;
        this.node = node;
        this.models = models;
        this.primaryDefinitions = primaryDefinitions;
        this.jmodel = jmodel;
    }
    
    protected String preferredID(){
        return "CustomizationComponent";    //NOI18N
    }
    
  
    public Collection<SaveableSectionInnerPanel> getPanels(){
        return panelFactory.getPanels();
    }
    
    private void doInitialize() {
        initAccessibility();

        ToolBarDesignEditor tb = new ToolBarDesignEditor();
        panelFactory = new WSPanelFactory(tb, node, primaryDefinitions, jmodel);
        WSCustomizationView mview = new WSCustomizationView(panelFactory, models, primaryDefinitions); 
        tb.setContentView(mview);
        add(tb);
        setFocusable(true);
    }

    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    private void initAccessibility(){

    }
     
    public void addNotify() {
        if (!initialized) {
            initialized = true;
            doInitialize();
        }
        super.addNotify();
    }
    

    protected void componentShowing() {
        if (!initialized) {
            initialized = true;
            doInitialize();
        }
        super.componentShowing();
    }
    
}

