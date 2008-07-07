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

package org.netbeans.modules.bpel.mapper.predicates;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.netbeans.modules.bpel.mapper.model.PathConverter;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.StepNodeTestType;
import org.netbeans.modules.xml.xpath.ext.StepNodeTypeTest;

/**
 * The class collects all special location steps expressions, which are 
 * in the edited XPath expression. The special location step is a LocationStep 
 * with StepNodeTest of type StepNodeTypeTest. The manager keeps the location 
 * of the step in term of schema components' path.
 *
 * The main intention of the manager is to provide showing of the special steps
 * the mapper source and destination trees.
 * 
 * @author nk160297
 */
public class SpecialStepManager {
    
    // The cache of predicates.
    private LinkedList<CachedStep> mSteps;
    
    public SpecialStepManager() {
        mSteps = new LinkedList<CachedStep>();
    }
    
    public List<LocationStep> getSteps(Iterable<Object> parentPathb) {
        //    
        ArrayList<LocationStep> result = new ArrayList<LocationStep>();
        
        for (CachedStep cStep : mSteps) {
            if (cStep.hasSameLocation(parentPathb)) {
                result.add(cStep.getStep());
            }
        }
        //
        return result;
    }
    
    public boolean addStep(List<Object> parentPath, LocationStep newStep) {
        for (CachedStep cStep : mSteps) {
            if (cStep.hasSameLocation(parentPath) && 
                    cStep.getStep().equals(newStep)) {
                // the same predicate already in cache
                return false;
            }
        }
        //
        CachedStep cPredicate = new CachedStep(parentPath, newStep);
        mSteps.add(cPredicate);
        return true;
    }

    public boolean addStep(TreeItem parentTreeItem, LocationStep step) {
        //
        List<Object> parentPath = 
                PathConverter.constructObjectLocationtList(
                parentTreeItem, true, false);
        //
        if (parentPath != null) {
            return addStep(parentPath, step);
        }
        //
        return false;
    }
    
    public void removeStep(TreeItem parentTreeItem, LocationStep stepToDelete) {
        List<Object> parentPath = 
                PathConverter.constructObjectLocationtList(
                parentTreeItem, true, false);
        //
        if (parentPath != null) {
            for (CachedStep cStep : mSteps) {
                if (cStep.getStep().equals(stepToDelete) && 
                        cStep.hasSameLocation(parentPath)) {
                    mSteps.remove(cStep);
                    break;
                }
            }
        }
    }
    
    /**
     * This class holds the special step itself (LocationStep) + 
     * its location and the flag persistent. 
     */
    public static class CachedStep {
        // The list contains data objects from which a tree path consists of
        // It is implied that it can contain a set of SchemaComponents and 
        // PredicatedSchemaComp. And there is either a variable or 
        // a variable with a part at the end. 
        // The first element is the parent of the predicate!
        // The predicated schema component isn't in the list itself.
        // It it held in the separate attribute mPredSComp.
        private List<Object> mParentPath;

        // The special location step.
        private LocationStep mStep;
        
        // Persistense means that the instance should not be automatically
        // deleted from the cache if it is not used.
        // The predicates which are not persistent will be removed from
        // the cache automatically.
        private boolean isPersistent;

        public CachedStep(List<Object> parentPath, LocationStep step) {
            mParentPath = parentPath;
            mStep = step;
            //
            assert step.getNodeTest() instanceof StepNodeTypeTest;
        }
        
        public boolean isPersistent() {
            return isPersistent;
        }
        
        public void setPersistent(boolean newValue) {
            isPersistent = newValue;
        }
        
        public LocationStep getStep() {
            return mStep;
        }
        
        public StepNodeTestType getNodeType() {
            StepNodeTypeTest sntt = (StepNodeTypeTest)mStep.getNodeTest();
            return sntt.getNodeType();
        }
        
        /**
         * Returns the list of data objects which indicate the location of 
         * the predicate in the tree. The first element of the list points 
         * to the parent element of the predicate.
         */
        public List getParentPath() {
            return mParentPath;
        }
        
        public boolean hasSameLocation(Iterable parentPathItrb) {
            return hasSameLocationImpl(parentPathItrb.iterator());
        }
        
        private boolean hasSameLocationImpl(Iterator externalItr) {
            //
            Iterator internalItr = mParentPath.iterator();
            boolean theSame = true;
            while (externalItr.hasNext() && internalItr.hasNext()) {
                Object externalPathStep = externalItr.next();
                Object internalPathStep = internalItr.next();
                if (!externalPathStep.equals(internalPathStep)) {
                    theSame = false;
                    break;
                }
            }
            //
            if (theSame && internalItr.hasNext()) {
                // internal location path longer then required. 
                // It isn't allowed. It can be shorter only. 
                return false;
            }
            //
            return theSame;
        }
        
        @Override
        public String toString() {
            String nodeType = getNodeType().toString();
            String endText = nodeType + " persistent=" + isPersistent; // NOI18N
            //
            String parentPath = locationToString();
            if (parentPath == null || parentPath.length() == 0) {
                return endText;
            } else {
                return parentPath + " " + endText;
            }
        }
        
        private String locationToString() {
            StringBuilder sb = new StringBuilder();
            ListIterator itr = mParentPath.listIterator(mParentPath.size());
            boolean isFirst = true;
            while (itr.hasPrevious()) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    sb.append("/");
                }
                //
                Object stepObj = itr.previous();
                sb.append(stepObj.toString());
            }
            return sb.toString();
        }
        
        @Override
        public boolean equals(Object obj2) {
            if (!(obj2 instanceof CachedStep)) {
                return false;
            }
            //
            // Compare the StepNodeTestType
            CachedStep step2 = (CachedStep)obj2;
            LocationStep lStep2 = ((CachedStep)step2).getStep();
            if (!lStep2.equals(mStep)) {
                return false;
            }
            //
            List path2 = step2.getParentPath();
            if (path2.size() != mParentPath.size()) {
                // Pathes have diferrent length
                return false;
            }
            //
            Iterator itr = mParentPath.listIterator();
            Iterator itr2 = path2.listIterator();
            //
            while (itr.hasNext()) { // Pathes have the same length!
                Object dataObj = itr.next();
                Object dataObj2 = itr2.next();
                if (!(dataObj.equals(dataObj2))) {
                    return false;
                }
            }
            //
            return true;
        }
    }
    
}
  
