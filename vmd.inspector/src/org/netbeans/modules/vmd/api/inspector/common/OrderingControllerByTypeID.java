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
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.TypeID;

/**
 *
 * @author Karol Harezlak 
 */

/**
 * Implementation of InspectorOrderingController. This class sorts InspectorFolders in the Mobility Visual Designer Navigator
 * based on given TypeIDs. Folders are gruped and sorted based on the TypeID and display name inside of the same TypeID group.
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
    
     /**
     * Creates OrderingControllerByTypeID.
     * @param order Insteger number of the ordering controller, 
     * higher number means higher position in the tree branch
     * @param supportedTypeID array of TypeIDs accepted by this ordering controller,
     * this array is also used as a criterion in the InspectorFolders sorting 
     */
    public OrderingControllerByTypeID(Integer order, TypeID... types) {
        if (types == null )
            throw new IllegalArgumentException("types argument cant be null"); //NOI18N
        
        this.order = order;
        this.types = Arrays.asList(types);
    }
    /**
     * Returns List of sorted InspectorFolders based on the provided TypeIDs.
     * @param component current DeesignComponent
     * @param folders Collection of InspectorFolders to sort
     */ 
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

    public boolean isTypeIDSupported(DesignDocument document, TypeID typeID) {
        for (TypeID supportedTypeID : types) {
         if (document.getDescriptorRegistry().isInHierarchy(supportedTypeID, typeID))
            return true;
        }
        
        return false;
    }

}
