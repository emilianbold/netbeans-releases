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
package org.netbeans.modules.css.visual.filters;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import org.netbeans.modules.css.visual.RuleEditorPanel;
import org.netbeans.modules.css.visual.api.SortMode;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * This file is originally from Retouche, the Java Support infrastructure in
 * NetBeans. I have modified the file as little as possible to make merging
 * Retouche fixes back as simple as possible. <p> Creates filtering for the
 * ClassMemberPanel
 *
 * @author phrebejk
 */
@NbBundle.Messages({
    "filters.show.all.properties=Show All CSS Properties",
    "filters.show.all.properties.tooltip=Show All CSS Properties",
    "filters.show.categories=Show Property Categories",
    "filters.show.categories.tooltip=Show Property Categories"
})
public final class RuleEditorFilters {

    public static final String SHOW_ALL_PROPERTIES = "show.all.properties"; //NOI18N
    public static final String SHOW_CATEGORIES = "show.property.categories"; //NOI18N
    private RuleEditorPanel ruleEditorPanel;
    private FiltersManager filters;
    private FiltersSettings settings;

    public RuleEditorFilters(RuleEditorPanel ruleEditorPanel, FiltersSettings settings) {
        this.ruleEditorPanel = ruleEditorPanel;
        this.settings = settings;
    }

    public FiltersManager getInstance() {
        if (filters == null) {
            filters = createFilters();
        }
        return filters;
    }
    
    public FiltersSettings getSettings() {
        return settings;
    }

    public JComponent getComponent() {
        FiltersManager f = getInstance();
        return f.getComponent(createSortButtons());
    }

    void setSortMode(SortMode mode) {
        ruleEditorPanel.setSortMode(mode);

        //update the toggle bottons (they are switching)
        sortNaturalToggleButton.setSelected(mode == SortMode.NATURAL);
        sortAlphaToggleButton.setSelected(mode == SortMode.ALPHABETICAL);
    }

    SortMode getSortMode() {
        return ruleEditorPanel.getSortMode();
    }

    // Privare methods ---------------------------------------------------------
    /**
     * Creates filter descriptions and filters itself
     */
    private FiltersManager createFilters() {
        FiltersDescription desc = new FiltersDescription();

        if (settings.isShowAllPropertiesEnabled()) {
            desc.addFilter(SHOW_ALL_PROPERTIES,
                    Bundle.filters_show_all_properties(),
                    Bundle.filters_show_all_properties_tooltip(),
                    false,
                    new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/css/visual/resources/showAllProperties.png")), //NOI18N
                    null);
        }

        if (settings.isShowCategoriesEnabled()) {
            desc.addFilter(SHOW_CATEGORIES,
                    Bundle.filters_show_categories(),
                    Bundle.filters_show_categories_tooltip(),
                    true,
                    new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/css/visual/resources/showCategories.gif")), //NOI18N
                    null);
        }

        return FiltersDescription.createManager(desc);
    }
    private JToggleButton sortNaturalToggleButton;
    private JToggleButton sortAlphaToggleButton;

    private JToggleButton[] createSortButtons() {
        if (!settings.isSortingEnabled()) {
            return new JToggleButton[0];
        } else {
            JToggleButton[] res = new JToggleButton[2];
            if (null == sortNaturalToggleButton) {
                sortNaturalToggleButton = new JToggleButton(new SortActionSupport.NaturalSortAction(this));
                sortNaturalToggleButton.setToolTipText(sortNaturalToggleButton.getText());
                sortNaturalToggleButton.setText(null);
                sortNaturalToggleButton.setSelected(getSortMode() == SortMode.NATURAL);
                sortNaturalToggleButton.setFocusable(false);
            }
            res[0] = sortNaturalToggleButton;

            if (null == sortAlphaToggleButton) {
                sortAlphaToggleButton = new JToggleButton(new SortActionSupport.AlphabeticalSortAction(this));
                sortAlphaToggleButton.setToolTipText(sortAlphaToggleButton.getText());
                sortAlphaToggleButton.setText(null);
                sortAlphaToggleButton.setSelected(getSortMode() == SortMode.ALPHABETICAL);
                sortAlphaToggleButton.setFocusable(false);
            }
            res[1] = sortAlphaToggleButton;
            return res;
        }
    }
}
