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

package org.netbeans.modules.vmd.midp.analyzer;

import org.netbeans.modules.vmd.api.analyzer.Analyzer;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.vmd.midp.components.categories.ResourcesCategoryCD;
import org.openide.util.NbBundle;

/**
 *
 * @author Anton Chechel
 */
public class ResourcesAnalyzer implements Analyzer {
    
    public ResourcesAnalyzer() {
    }

    public String getProjectType () {
        return MidpDocumentSupport.PROJECT_TYPE_MIDP;
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(ResourcesAnalyzer.class, "ResourcesAnalyzer.displayName"); // NOI18N
    }
    
    public String getToolTip() {
        return NbBundle.getMessage(ResourcesAnalyzer.class, "ResourcesAnalyzer.toolTip"); // NOI18N
    }
    
    public Image getIcon() {
        return null; // TODO
    }
    
    public JComponent createVisualRepresentation() {
        return new ResourcesAnalyzerPanel();
    }
    
    public void update(final JComponent visualRepresentation, final DesignDocument document) {
        if (visualRepresentation == null  ||  document == null) {
            return;
        }
        
        final ResourcesAnalyzerPanel analyzerPanel = (ResourcesAnalyzerPanel) visualRepresentation;
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                document.getTransactionManager().readAccess(new Runnable() {
                    public void run() {
                        java.util.List<DesignComponent> resources = new ArrayList<DesignComponent>();
                        DesignComponent rootComponent = document.getRootComponent ();
                        if (rootComponent != null) {
                            for (DesignComponent component : rootComponent.getComponents()) {
                                if (ResourcesCategoryCD.TYPEID.equals(component.getType())) {
                                    resources.addAll(component.getComponents());
                                }
                            }
                            filterResources (rootComponent, resources);
                        }
                        
                        analyzerPanel.setUnusedResources(document, resources);
                    }
                });
            }
        });
    }
    
    private void filterResources(DesignComponent component,Collection<DesignComponent> resources) {
        for (DesignComponent child : component.getComponents()) {
            filterResources(child, resources);
        }
        
        Collection<DesignComponent> references = new ArrayList<DesignComponent>();
        for (PropertyDescriptor pd : component.getComponentDescriptor().getPropertyDescriptors()) {
            Debug.collectAllComponentReferences(component.readProperty(pd.getName()), references);
            resources.removeAll(references);
            references.clear();
        }
    }
    
}
