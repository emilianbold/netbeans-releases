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

/*
 * EditWSAttributesCookieImpl.java
 *
 * Created on April 12, 2006, 10:13 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.core.wseditor.support;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.SwingUtilities;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.core.wseditor.spi.WSEditor;
import org.netbeans.modules.websvc.core.wseditor.spi.WSEditorProvider;
import org.netbeans.modules.websvc.core.wseditor.spi.WSEditorProviderRegistry;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;

/**
 *
 * @author rico
 */
public class EditWSAttributesCookieImpl implements EditWSAttributesCookie{
    
    /** Creates a new instance of EditWSAttributesCookieImpl */
    public EditWSAttributesCookieImpl(Node node, JaxWsModel jaxWsModel) {
        this.node = node;
        this.jaxWsModel = jaxWsModel;
    }
    
    public void openWSAttributesEditor(){
        tc = cachedTopComponents.get(node);
        if(tc == null){
            //populate the editor registry if needed
            populateWSEditorProviderRegistry();
            //get all providers
            providers =
                    WSEditorProviderRegistry.getDefault().getEditorProviders();
            tc = new EditWSAttributesTopComponent();
            cachedTopComponents.put(this, tc);
        }
        populatePanels();
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                tc.addTabs(editors, node, jaxWsModel );
                DialogDescriptor dialogDesc = new DialogDescriptor(tc, node.getName());
                dialogDesc.setHelpCtx(new HelpCtx(EditWSAttributesCookieImpl.class));
                Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDesc);
                dialog.setVisible(true);
                if(dialogDesc.getValue() == NotifyDescriptor.OK_OPTION){
                    for(WSEditor editor : editors){
                        editor.save(node, jaxWsModel);
                    }
                } else{
                    for(WSEditor editor: editors){
                        editor.cancel(node, jaxWsModel);
                    }
                }
            }
        });
    }
    
    
    
    class DialogWindowListener extends WindowAdapter{
        Set<WSEditor> editors;
        public DialogWindowListener(Set<WSEditor> editors){
            this.editors = editors;
        }
        public void windowClosing(WindowEvent e){
            for(WSEditor editor: editors){
                editor.cancel(node, jaxWsModel);
            }
        }
    }
    
    public Set getWSEditorProviders(){
        return providers;
    }
    
    private void populatePanels(){
        editors = new HashSet<WSEditor>();
        for(WSEditorProvider provider : providers){
            if(provider.enable(node)){
                //for each provider, create a WSAttributesEditor
                WSEditor editor = provider.createWSEditor();
                editors.add(editor);
            }
        }
    }
    
    private void populateWSEditorProviderRegistry(){
        WSEditorProviderRegistry registry = WSEditorProviderRegistry.getDefault();
        if(registry.getEditorProviders().isEmpty()){
            Lookup.Result results = Lookup.getDefault().
                    lookup(new Lookup.Template(WSEditorProvider.class));
            Collection<WSEditorProvider> services = results.allInstances();
            //System.out.println("###number of editors: " + services.size());
            for(WSEditorProvider provider : services){
                registry.register(provider);
            }
        }
    }
    
    private Set<WSEditorProvider> providers;
    private Set<WSEditor> editors;
    private static Map<EditWSAttributesCookie, EditWSAttributesTopComponent> cachedTopComponents
            = new WeakHashMap<EditWSAttributesCookie, EditWSAttributesTopComponent>();
    private EditWSAttributesTopComponent tc;
    private Node node;
    private JaxWsModel jaxWsModel;
    private DialogWindowListener windowListener;
}
