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
 *
 */

package org.netbeans.modules.vmd.screen.resource;

import org.netbeans.modules.vmd.api.screen.resource.ScreenResourceCategoryDescriptor;
import org.netbeans.modules.vmd.api.screen.resource.ScreenResourceItemPresenter;
import org.netbeans.modules.vmd.api.screen.resource.ScreenResourceCategoriesPresenter;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;

import java.util.*;

/**
 * @author David Kaspar
 */
public class ResourcePanelSupport {

    static Map<ScreenResourceCategoryDescriptor,ArrayList<ScreenResourceItemPresenter>> getCategoryDescriptors (DesignComponent editedScreen) {
        if (editedScreen == null)
            return Collections.emptyMap ();
        Collection<? extends ScreenResourceCategoriesPresenter> categoriesPresenters = editedScreen.getPresenters (ScreenResourceCategoriesPresenter.class);
        HashMap<ScreenResourceCategoryDescriptor,ArrayList<ScreenResourceItemPresenter>> categories = new HashMap<ScreenResourceCategoryDescriptor,ArrayList<ScreenResourceItemPresenter>> ();
        for (ScreenResourceCategoriesPresenter presenter : categoriesPresenters) {
            Collection<ScreenResourceCategoryDescriptor> list = presenter.getCategoryDescriptors ();
            if (list != null) {
                for (ScreenResourceCategoryDescriptor category : list)
                    categories.put (category, new ArrayList<ScreenResourceItemPresenter> ());
            }
        }
        return categories;
    }

    static void resolveResources (DesignDocument document, DesignComponent editedScreen, Map<ScreenResourceCategoryDescriptor, ArrayList<ScreenResourceItemPresenter>> categories) {
        if (editedScreen == null)
            return;
        Collection<ScreenResourceItemPresenter> resources = DocumentSupport.gatherAllPresentersOfClass (document, ScreenResourceItemPresenter.class);
        for (ScreenResourceItemPresenter resource : resources) {
            if (! resource.isActiveFor (editedScreen))
                continue;
            ScreenResourceCategoryDescriptor category = resource.getCategoryDescriptor ();
            ArrayList<ScreenResourceItemPresenter> list = categories.get (category);
            if (list == null)
                continue;
            list.add (resource);
        }
    }

    public static List<ScreenResourceCategoryDescriptor> getSortedCategories (Set<ScreenResourceCategoryDescriptor> set) {
        ArrayList<ScreenResourceCategoryDescriptor> list = new ArrayList<ScreenResourceCategoryDescriptor> (set);
        Collections.sort (list, new Comparator<ScreenResourceCategoryDescriptor>() {
            public int compare (ScreenResourceCategoryDescriptor o1, ScreenResourceCategoryDescriptor o2) {
                return o1.getOrder () - o2.getOrder ();
            }
        });
        return list;
    }

}
