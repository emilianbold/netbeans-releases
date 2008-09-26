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

package org.netbeans.modules.xml.schema.ui.basic;

import java.awt.Image;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.ui.basic.SchemaSettings.ViewMode;
import org.netbeans.modules.xml.schema.ui.basic.search.AttributeNameSearchProvider;
import org.netbeans.modules.xml.schema.ui.basic.search.AttributeValueSearchProvider;
import org.netbeans.modules.xml.schema.ui.basic.search.ComponentNameSearchProvider;
import org.netbeans.modules.xml.schema.ui.basic.search.ComponentTypeSearchProvider;
import org.netbeans.modules.xml.schema.ui.basic.search.NameSpaceSearchProvider;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ui.category.AbstractCategory;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Displays the categorized version of the schema tree view.
 *
 * @author Nathan Fiedler
 */
public class SchemaTreeCategory extends AbstractCategory {
    /** Associated schema model. */
    private SchemaModel schemaModel;
    /** Our visual component. */
    private SchemaTreeView component;
    /** Our lookup. */
    private Lookup lookup;

    /**
     * Creates a new instance of SchemaCategory.
     *
     * @param  model   schema model to display.
     * @param  lookup  associated Lookup instance.
     */
    public SchemaTreeCategory(SchemaModel model, Lookup lookup) {
        this.schemaModel = model;
        Object[] searchers = new Object[] {
            new ComponentNameSearchProvider(model, this),
            new ComponentTypeSearchProvider(model, this),
            new AttributeNameSearchProvider(model, this),
            new AttributeValueSearchProvider(model, this),
            new NameSpaceSearchProvider(model, this),
// The XPath query is too slow; re-enable when model is faster.
//            new XPathSearchProvider(model, this),
        };
        this.lookup = new ProxyLookup(new Lookup[] {
            lookup,
            Lookups.fixed(searchers)
        });
    }

    public void componentHidden() {
    }

    public void componentShown() {
        initComponents();
        SchemaSettings.getDefault().setViewMode(ViewMode.TREE);
    }

    public java.awt.Component getComponent() {
        initComponents();
        return component;
    }

    public String getDescription() {
        return NbBundle.getMessage(SchemaTreeCategory.class,
                "HINT_SchemaCategory_Tree");
    }

    public Icon getIcon() {
        String url = NbBundle.getMessage(SchemaTreeCategory.class,
                "IMG_SchemaCategory_Tree");
        Image img = ImageUtilities.loadImage(url);
        return new ImageIcon(img);
    }

    public Lookup getLookup() {
        return lookup;
    }

    public String getTitle() {
        return NbBundle.getMessage(SchemaTreeCategory.class,
                "LBL_SchemaCategory_Tree");
    }

    /**
     * Construct our visual components, if they have not been already.
     */
    private void initComponents() {
        if (component == null) {
            component = new SchemaTreeView(schemaModel,
                    SchemaTreeView.ViewType.CATEGORIZED, getLookup());
        }
    }

    public void showComponent(Component comp) {
        if (comp instanceof SchemaComponent) {
            component.showComponent((SchemaComponent)comp);
        }
    }
}
