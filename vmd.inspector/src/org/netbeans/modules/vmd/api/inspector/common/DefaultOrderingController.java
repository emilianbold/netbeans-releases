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

package org.netbeans.modules.vmd.api.inspector.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.modules.vmd.api.inspector.InspectorFolder;
import org.netbeans.modules.vmd.api.inspector.InspectorOrderingController;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.TypeID;

/**
 *
 * @author Karol Harezlak
 */

/**
 * Implementation of InspectorOrderingController. This class sorts InspectorFolders in the Mobility Visual Designer Navigator
 * according of their natural order. Parameter used to sort InspectorFolders is display name.
 */
public class DefaultOrderingController implements InspectorOrderingController {

    private static final Comparator<InspectorFolder> DEFAULT_STRING_COMPARATOR = new Comparator<InspectorFolder>() {

        public int compare(InspectorFolder fd1, InspectorFolder fd2) {
            return fd1.getDisplayName().compareTo(fd2.getDisplayName());
        }
    };
    private Integer order;
    private List<TypeID> types;

    /**
     * Creates DefaultOrderingController.
     * @param order Insteger number of the ordering controller, higher number means higher position in the tree branch
     * @param supportedTypeID array of TypeIDs accepted by this ordering controller
     */
    public DefaultOrderingController(Integer order, TypeID... types) {
        if (types == null) {
            throw new IllegalArgumentException("types argument cant be null"); //NOI18N
        }
        this.order = order;
        this.types = Arrays.asList(types);
    }

    /**
     * Returns list of sorted InspectorFolders in the natural order based on the InspectorFolder display name.
     * @param component current DesignComponent
     * @parma folders InspectorFolders to sort
     * @raturn ordered List of InspectorFolders
     */
    public List<InspectorFolder> getOrdered(DesignComponent component, Collection<InspectorFolder> folders) {
        List<InspectorFolder> sortedList = new ArrayList<InspectorFolder>(folders);
        Collections.sort(sortedList, DEFAULT_STRING_COMPARATOR);
        return sortedList;
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
        if (types.contains(typeID)) {
            return true;
        }
        return false;
    }
}