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

import org.netbeans.modules.vmd.api.screen.resource.ScreenResourceCategoryDescriptor;
import org.netbeans.modules.vmd.api.screen.resource.ScreenResourceItemPresenter;
import org.netbeans.modules.vmd.api.screen.resource.ScreenResourceCategoriesPresenter;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;

import java.util.*;
import org.netbeans.modules.vmd.api.screen.resource.ScreenResourceOrderingController;

/**
 * @author David Kaspar
 */
public class ResourcePanelSupport {
    
    static Map<ScreenResourceCategoryDescriptor,ArrayList<ScreenResourceItemPresenter>> getCategoryDescriptors(DesignComponent editedScreen) {
        if (editedScreen == null)
            return Collections.emptyMap();
        Collection<? extends ScreenResourceCategoriesPresenter> categoriesPresenters = editedScreen.getPresenters(ScreenResourceCategoriesPresenter.class);
        HashMap<ScreenResourceCategoryDescriptor,ArrayList<ScreenResourceItemPresenter>> categories = new HashMap<ScreenResourceCategoryDescriptor,ArrayList<ScreenResourceItemPresenter>> ();
        for (ScreenResourceCategoriesPresenter presenter : categoriesPresenters) {
            Collection<ScreenResourceCategoryDescriptor> list = presenter.getCategoryDescriptors();
            if (list != null) {
                for (ScreenResourceCategoryDescriptor category : list)
                    categories.put(category, new ArrayList<ScreenResourceItemPresenter> ());
            }
        }
        return categories;
    }
    
    static void resolveResources(DesignDocument document, DesignComponent editedScreen, Map<ScreenResourceCategoryDescriptor, ArrayList<ScreenResourceItemPresenter>> categories) {
        if (editedScreen == null)
            return;
        Collection<ScreenResourceItemPresenter> resources = DocumentSupport.gatherAllPresentersOfClass(document, ScreenResourceItemPresenter.class);
        for (ScreenResourceItemPresenter resource : resources) {
            if (! resource.isActiveFor(editedScreen))
                continue;
            ScreenResourceCategoryDescriptor category = resource.getCategoryDescriptor();
            ArrayList<ScreenResourceItemPresenter> list = categories.get(category);
            if (list == null)
                continue;
            list.add(resource);
        }
        //Sorting
        for (ScreenResourceCategoryDescriptor category : categories.keySet()) {
            for (ScreenResourceOrderingController oc  : category.getOrderingControllers()) {
                List<ScreenResourceItemPresenter> orderedList =  oc.getOrdered(editedScreen, categories.get(category));
                if (orderedList == null)
                    continue;
                categories.get(category).removeAll(orderedList);
                categories.get(category).addAll(orderedList);
            }
        }
    }
    
    public static List<ScreenResourceCategoryDescriptor> getSortedCategories(Set<ScreenResourceCategoryDescriptor> set) {
        ArrayList<ScreenResourceCategoryDescriptor> list = new ArrayList<ScreenResourceCategoryDescriptor> (set);
        Collections.sort(list, new Comparator<ScreenResourceCategoryDescriptor>() {
            public int compare(ScreenResourceCategoryDescriptor o1, ScreenResourceCategoryDescriptor o2) {
                return o1.getOrder() - o2.getOrder();
            }
        });
        return list;
    }
    
}
