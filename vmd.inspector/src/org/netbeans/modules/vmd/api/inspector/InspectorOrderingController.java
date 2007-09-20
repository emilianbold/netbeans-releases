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

package org.netbeans.modules.vmd.api.inspector;

import java.util.Collection;
import java.util.List;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.TypeID;


/**
 *
 * @author Karol Harezlak
 */

/**
 * This class controls  order of the InspectorFolder in the branch of the Mobility
 * Visual Designer Navigator tree.
 */ 
public interface InspectorOrderingController {
    /**
     * Checks if given TypeId is supported by this ordering controller.
     * @param document current DesignDocument
     * @param typeID typeID to check
     * @return Boolen.TRUE when TypyID is supported, Boolean.FALSE when TypeID is
     * not supported
     */ 
    boolean isTypeIDSupported(DesignDocument document, TypeID typeID);
    
    /**
     * Orders and returns List of InspectorFolder for given component and collection of folders.
     * @param component DesignComponent
     * @return folders to order
     */ 
    List<InspectorFolder> getOrdered(DesignComponent component, Collection<InspectorFolder> folders);
    
    /**
     * Returns order Integer number for list of folders sorted by method getOrdered.
     * Higher Integer number means that sorted list of folders has higher priority when
     * it's necessary to order not only folders but also lists (List<InspectorFolder>) of sorted folders
     * in the same tree branch. For example if in the same tree branch there is two or more InspectorOrderingController
     * then ordered lists need to be ordered by Integer number returns by this method.
     * 
     * @return Integer number, when null then sorted list has lowest possible priority
     */ 
    Integer getOrder();

}
