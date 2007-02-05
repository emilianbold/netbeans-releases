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
import java.util.Map;
import java.util.TreeMap;
import org.netbeans.modules.vmd.api.inspector.InspectorFolder;

import org.netbeans.modules.vmd.api.inspector.InspectorOrderingController;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.TypeID;

/**
 *
 * @author Karol Harezlak 
 */
public class OrderingControllerByTypeID implements InspectorOrderingController {
    
    private Integer order;
    private List<TypeID> types;
    private Comparator<InspectorFolder> comparatorByName = new Comparator<InspectorFolder>() {
        public int compare(InspectorFolder fd1, InspectorFolder fd2) {
            return fd1.getDisplayName().compareTo(fd2.getDisplayName());
        }
    };
    private Comparator<TypeID> comparatorByType = new Comparator<TypeID>() {
        public int compare(TypeID t1, TypeID t2) {
            return t1.toString().compareTo(t2.toString());
        }
    };
    
    public OrderingControllerByTypeID(Integer order, TypeID... types) {
        if (types == null )
            throw new IllegalArgumentException("types argument cant be null"); //NOI18N
        
        this.order = order;
        this.types = Arrays.asList(types);
    }
    
    public List<InspectorFolder> getOrdered(DesignComponent component, Collection<InspectorFolder> folders) {
        Map<TypeID , List<InspectorFolder>> foldersMap = new TreeMap<TypeID, List<InspectorFolder>>(comparatorByType);
        List<InspectorFolder> sortedList = new ArrayList<InspectorFolder>();
        
        for (InspectorFolder f : folders) {
            if (foldersMap.get(f.getTypeID()) == null)
                foldersMap.put( f.getTypeID(), new ArrayList<InspectorFolder>(Arrays.asList(f)));
            else
                foldersMap.get(f.getTypeID()).add(f);
        }
        
        for (List<InspectorFolder> list : foldersMap.values()) {
            Collections.sort(list, comparatorByName);
            sortedList.addAll(list);
        }
        
        return sortedList;
    }
    
    public Integer getOrder() {
        return order;
    }

    public boolean isTypeIDSupported(TypeID typeID) {
        if (types.contains(typeID))
            return true;
        
        return false;
    }

}
