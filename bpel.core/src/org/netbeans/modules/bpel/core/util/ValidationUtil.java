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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.core.util;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

public final class ValidationUtil {
    
    private ValidationUtil() {}
    
    public static List<ResultItem> filterBpelResultItems( 
            List<ResultItem> validationResults) 
    {
        List<ResultItem> bpelResultItems = new ArrayList<ResultItem>();
        
        // Go through all result items.
        for(ResultItem resultItem: validationResults) {
            // For each result item go through all components for
            // which it applies.
            Component component = resultItem.getComponents();
            // Only gather bpel components.
            if(component instanceof BpelEntity) {
                ResultItem bpelResultItem = 
                    new ResultItem(resultItem.getValidator(),
                        resultItem.getType(), component, 
                        resultItem.getDescription());
                bpelResultItems.add(bpelResultItem);
            }
        }
        return bpelResultItems;
    }
    
    public static boolean equals(ResultItem item1, ResultItem item2){
        if (item1 == item2){
            return true;
        }
        if(!item1.getDescription().equals(item2.getDescription())) {
            return false;
        }
        
        if(!item1.getType().equals(item2.getType())) {
            return false;
        }
        
        Component components1 = item1.getComponents();
        Component components2 = item2.getComponents();
        
        if(components1 != components2) {
            return false;
        }
        return true;
    }
    
    public static List<ResultItem> getAddedResultItems(List<ResultItem> oldList, 
            List<ResultItem> newList) 
    {
        List<ResultItem> addedResultItems = new ArrayList<ResultItem>();
        
        if(oldList == null)
            return newList;
        
        if(newList == null) // No items could have been added.
            return addedResultItems;
        
        for(ResultItem item: newList) {
            if(!contains(oldList, item)) {
                addedResultItems.add(item);
            }
        }
        return addedResultItems;
    }
    
    public static List<ResultItem> getRemovedResultItems(List<ResultItem> oldList,
            List<ResultItem> newList) 
    {
        List<ResultItem> removedResultItems = new ArrayList<ResultItem>();
        
        if(newList == null) {
            return oldList;
        }
        if(oldList == null) {  // No items could have been removed.
            return removedResultItems;
        }
        
        for(ResultItem item: oldList) {
            if(!contains(newList, item)) {
                removedResultItems.add(item);
            }
        }
        return removedResultItems;
    }
    
    private static boolean contains(List<ResultItem> list, ResultItem resultItem) {
        assert list!=null;
        for (ResultItem item: list) {
            if (equals(item, resultItem)){
                return true;
            }
        }
        return false;
    }
}
