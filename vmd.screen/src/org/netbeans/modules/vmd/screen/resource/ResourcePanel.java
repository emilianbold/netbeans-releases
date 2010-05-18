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
package org.netbeans.modules.vmd.screen.resource;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.screen.resource.ScreenResourceCategoryDescriptor;
import org.netbeans.modules.vmd.api.screen.resource.ScreenResourceItemPresenter;
import org.netbeans.modules.vmd.screen.ScreenAccessController;
import org.netbeans.modules.vmd.screen.MainPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author David Kaspar
 */
public class ResourcePanel extends JPanel {

    private ScreenAccessController controller;
    private JPanel fillPanel;

    public ResourcePanel (ScreenAccessController controller) {
        this.controller = controller;
        setBackground (MainPanel.BACKGROUND_COLOR);
        setLayout (new GridBagLayout ());
        fillPanel = new JPanel ();
        fillPanel.setBackground (MainPanel.BACKGROUND_COLOR);
        fillPanel.setPreferredSize (new Dimension (250, 0));
    }
    
    // called in AWT and document transation
    public void reload () {
        DesignComponent editedScreen = controller.getEditedScreen ();
        Map<ScreenResourceCategoryDescriptor, ArrayList<ScreenResourceItemPresenter>> categories = ResourcePanelSupport.getCategoryDescriptors (editedScreen);
        ResourcePanelSupport.resolveResources (controller.getDocument (), editedScreen, categories);
        List<ScreenResourceCategoryDescriptor> sortedCategories = ResourcePanelSupport.getSortedCategories (categories.keySet ());

        removeAll ();
        int y = 0;
        for (ScreenResourceCategoryDescriptor category : sortedCategories) {
            ResourceCategoryPanel categoryPanel = new ResourceCategoryPanel (category); // TODO - cache ResourceCategoryPanel instances
            categoryPanel.reload (categories.get (category));
            add (categoryPanel, new GridBagConstraints (0, y ++, 1, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets (0, 0, 20, 0), 0, 0));
        }
        add (fillPanel, new GridBagConstraints (0, y, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets (0, 0, 0, 0), 0, 0));
    }

}
