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

package org.netbeans.modules.vmd.api.screen.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;

/**
 *
 * @author Karol Harezlak
 */
public abstract class ScreenResourceOrderingController {
    
    private static ArrayOrderingController arrayOrdering;
    private static DefaultOrderingController defaultOrdering;
    
    public static ScreenResourceOrderingController getArrayOrdering(final String propertyName) {
        if (arrayOrdering == null)
            arrayOrdering = new ArrayOrderingController(propertyName);
        
        return arrayOrdering;
    }
    
    public static ScreenResourceOrderingController getDefaultOrdering() {
        if (defaultOrdering == null)
            defaultOrdering = new DefaultOrderingController();
        
        return defaultOrdering;
    }
    
    public abstract List<ScreenResourceItemPresenter> getOrdered(DesignComponent component, Collection<ScreenResourceItemPresenter> items);
    
    private static class ArrayOrderingController extends ScreenResourceOrderingController {
        
        private String propertyName;
        
        private ArrayOrderingController(String propertyName) {
            this.propertyName = propertyName;
        }
        
        public List<ScreenResourceItemPresenter> getOrdered(DesignComponent component, Collection<ScreenResourceItemPresenter> items) {
            List<ScreenResourceItemPresenter> list = new ArrayList<ScreenResourceItemPresenter>(items.size());
            //Fix for issue Issue 149281
            if (component == null || component.readProperty(propertyName) == null || component.readProperty(propertyName).getArray() == null) {
                return list;
            }
            //End of the Fix for issue Issue 149281
            List<PropertyValue> array = new ArrayList<PropertyValue>(component.readProperty(propertyName).getArray());
            for (PropertyValue value : array) {
                DesignComponent commandEventSource = value.getComponent();
                for (ScreenResourceItemPresenter descriptor : items)
                    if (descriptor.getRelatedComponent() == commandEventSource) {
                        list.add(descriptor);
                        break;
                    }
            }
            
            return list;
        }
    }
    
    private static class DefaultOrderingController extends ScreenResourceOrderingController {
        public List<ScreenResourceItemPresenter> getOrdered(DesignComponent component, Collection<ScreenResourceItemPresenter> items) {
            List<ScreenResourceItemPresenter> orderedList = new ArrayList<ScreenResourceItemPresenter>(items);
            Collections.sort(orderedList, new Comparator<ScreenResourceItemPresenter>() {
                public int compare(ScreenResourceItemPresenter item1, ScreenResourceItemPresenter item2) {
                    String name1 = InfoPresenter.getDisplayName(item1.getRelatedComponent());
                    String name2 = InfoPresenter.getDisplayName(item2.getRelatedComponent());
                    if (name1 == null || name2 == null)
                        throw new NullPointerException();
                    return name1.compareTo(name2);
                }
            });
            
            return orderedList;
        }
    }
}
