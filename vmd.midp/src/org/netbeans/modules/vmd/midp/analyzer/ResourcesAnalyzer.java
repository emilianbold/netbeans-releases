/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.vmd.midp.analyzer;

import org.netbeans.modules.vmd.api.analyzer.Analyzer;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.commands.CommandCD;
import org.netbeans.modules.vmd.midp.components.categories.ResourcesCategoryCD;
import org.netbeans.modules.vmd.midp.components.categories.CommandsCategoryCD;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Anton Chechel
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.vmd.api.analyzer.Analyzer.class)
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
                            DesignComponent commandsCategory = MidpDocumentSupport.getCategoryComponent (document, CommandsCategoryCD.TYPEID);
                            if (commandsCategory != null)
                                for (DesignComponent command : commandsCategory.getComponents ())
                                    if (MidpTypes.getBoolean (command.readProperty (CommandCD.PROP_ORDINARY)))
                                        resources.add (command);

                            DesignComponent resourcesCategory = MidpDocumentSupport.getCategoryComponent (document, ResourcesCategoryCD.TYPEID);
                            if (resourcesCategory != null)
                                resources.addAll(resourcesCategory.getComponents());

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
