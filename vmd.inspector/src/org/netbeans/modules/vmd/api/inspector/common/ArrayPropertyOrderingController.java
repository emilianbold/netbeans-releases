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

package org.netbeans.modules.vmd.api.inspector.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.vmd.api.inspector.InspectorFolder;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingController;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;

/**
 *
 * @author Karol Harezlak
 */
/**
 * Implementation of InspectorOrderingController. This class sorts InspectorFolders in the Mobility Visual Designer Navigator
 * according of their position in the property array. It means when this presenter is
 * attached to the DesignComponent then DesignComponents from indicated array property of this
 * DesignComponent will be displayed in Mobility Visual Designer Navigator in the  same
 * order like in array.
 */
public class ArrayPropertyOrderingController implements InspectorOrderingController {

    private String propertyName;
    private Integer order;
    private TypeID supportedTypeID;

    /**
     * Creates ArrayPropertyOrderingController.
     * @parma propertyName property name of DesignComponent array property
     * @param order Insteger number of the ordering controller, higher number means higher position in the tree branch
     * @param supportedTypeID TypeID accepted by this ordering controller, all TypeIDs which inherits from
     * given TypeID are also accepted by this ordering controller
     */
    public ArrayPropertyOrderingController(String propertyName, Integer order, TypeID supportedTypeID) {
        this.propertyName = propertyName;
        this.order = order;
        this.supportedTypeID = supportedTypeID;
    }

    /**
     * Returns ordered List of InspectorFolders.
     * @param component current DesignComponent
     * @parma folders InspectorFolders to sort
     * @raturn ordered List of InspectorFolders
     */
    public List<InspectorFolder> getOrdered(DesignComponent component, Collection<InspectorFolder> folders) {
        List<InspectorFolder> orderedList = new ArrayList<InspectorFolder>(folders.size());
        if (component == null) {
            return new ArrayList<InspectorFolder>(folders);
        }
        List<PropertyValue> array = component.readProperty(propertyName).getArray();
        if (array == null) {
            return new ArrayList<InspectorFolder>(folders);
        }
        for (PropertyValue value : array) {
            DesignComponent itemsComponent = value.getComponent();
            for (InspectorFolder f : folders) {
                if (f.getComponentID().equals(itemsComponent.getComponentID())) {
                    orderedList.add(f);
                    break;
                }
            }
        }

        return orderedList;
    }

    /**
     * Returns order Integer number for list of folders sorted by method getOrdered.
     * Higher Integer number means that sorted list of folders has higher priority when
     * it's necessary to order not only folders but also lists (List<InspectorFolder>) of sorted folders
     * in the same tree branch. For example if in the same tree branch there is two or more InspectorOrderingController
     * then ordered lists need to be ordered by Integer number returns by this method.
     *
     * @return Integer number, when null then sorted list has lowest possible priority
     */
    public Integer getOrder() {
        return order;
    }

    /**
     * Checks if given TypeId is supported by this ordering controller.
     * @param document current DesignDocument
     * @param typeID typeID to check
     * @return Boolen.TRUE when TypyID is supported, Boolean.FALSE when TypeID is
     * not supported
     */ 
    public boolean isTypeIDSupported(DesignDocument document, TypeID typeID) {
        if (document.getDescriptorRegistry().isInHierarchy(supportedTypeID, typeID)) {
            return true;
        }
        return false;
    }
}
