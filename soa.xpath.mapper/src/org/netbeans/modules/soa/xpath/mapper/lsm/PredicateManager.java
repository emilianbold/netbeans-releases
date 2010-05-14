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

package org.netbeans.modules.soa.xpath.mapper.lsm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.xpath.mapper.tree.ArtificialTreeNode;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.soa.xpath.mapper.tree.DirectedList;
import org.netbeans.modules.soa.xpath.mapper.tree.PathConverter;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.XPathUtils;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.PredicatedSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.SchemaCompHolder;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;

/**
 * The class collects all predicate expressions which are in the edited 
 * XPath expression. Each predicate is declared in the XPath location step.
 * The location step is bound to the specific schema element or attribute.
 * The predicate manager keeps the location of predicate in term of
 * schema components' path.
 *
 * The main intention of the predicate manager is to provide showing of
 * predicates in the mapper source and destination trees.
 * 
 * @author nk160297
 */
public abstract class PredicateManager<V> implements PredicateCommands {

    /**
     * Modifies predicate inside of schema context by template predicate context.
     * @param sContext
     * @param template
     */
    public static void modifyPredicateInSContext(XPathSchemaContext sContext,
            PredicatedSchemaContext template,
            XPathPredicateExpression[] newExprArr) {
        //
        if (sContext != null) {
            PredicatedSchemaContext foundSContext =
                    findPredicateInSContext(sContext, template);
            if (foundSContext != null) {
                foundSContext.setPredicateExpressions(newExprArr);
            }
        }
    }

    /**
     * TODO: There are a dought that the method can be optimized.
     * It is necessary to create a DirectedList for the sContext and
     * compare the list backward.
     *
     * @param sContext
     * @param predSContext
     * @return
     */
    private static PredicatedSchemaContext findPredicateInSContext(
            XPathSchemaContext sContext, PredicatedSchemaContext template) {
        //
        assert sContext != null;
        assert template != null;
        //
        if (template.equalsChain(sContext)) {
            return (PredicatedSchemaContext)sContext;
        }
        //
        XPathSchemaContext parentSContext = sContext.getParentContext();
        if (parentSContext == null) {
            return null;
        } else {
            return findPredicateInSContext(parentSContext, template);
        }
    }

    // The cache of predicates.
    protected LinkedList<CachedPredicate> mPredicates;

    protected MapperSwingTreeModel mMapperTreeModel;
    protected Object mSynchSource;
    
    protected HashSet<PredicateModifiedListener<V>> mListenerSet =
            new HashSet<PredicateModifiedListener<V>>();
    
    protected PredicateManager(Object synchSource) {
        this(null, synchSource);
    }

    protected PredicateManager(MapperSwingTreeModel mapperTreeModel, Object synchSource) {
        //
        mMapperTreeModel = mapperTreeModel;
        mSynchSource = synchSource;
        mPredicates = new LinkedList<CachedPredicate>();
    }

    public MapperPredicate createMapperPredicate(PredicatedSchemaContext predSContext) {
        return new MapperPredicate(predSContext);
    }
    
    public void attachToTreeModel(MapperSwingTreeModel mapperTreeModel) {
        mMapperTreeModel = mapperTreeModel;
    }

    public List<MapperPredicate> getPredicates(
            TreeItem parentTreeItem, SchemaComponent sComp) {
        //    
        ArrayList<MapperPredicate> result = new ArrayList<MapperPredicate>();
        
        for (CachedPredicate cPred : mPredicates) {
            if (cPred.hasSameBase(sComp) && cPred.hasSameLocation(parentTreeItem)) {
                result.add(cPred.getPredicate());
            }
        }
        //
        return result;
    }
    
    public List<MapperPredicate> getPredicates(
            TreeItem parentTreeItem, XPathPseudoComp pseudo) {
        //    
        ArrayList<MapperPredicate> result = new ArrayList<MapperPredicate>();
        
        for (CachedPredicate cPred : mPredicates) {
            if (cPred.hasSameBase(pseudo) && cPred.hasSameLocation(parentTreeItem)) {
                result.add(cPred.getPredicate());
            }
        }
        //
        return result;
    }

    public boolean addPredicate(MapperPredicate newPred) {
        if (newPred == null) {
            return false;
        }
        //
        // skip first because of the path to the parent is required here.
        PathConverter pathConverter = mMapperTreeModel.getPathConverter();
        DirectedList<Object> parentPath = pathConverter.
                constructObjectLocationList(newPred.getSchemaContext(), true, true);
        //
        if (parentPath != null && !parentPath.isEmpty()) {
            return addPredicate(parentPath, newPred);
        }
        //
        return false;
    }

    public boolean addPredicate(DirectedList<Object> parentPath, MapperPredicate pred) {
        for (CachedPredicate cPred : mPredicates) {
            if (cPred.hasSameLocation(parentPath) && cPred.hasSamePredicate(pred)) {
                // the same predicate already in cache
                return false;
            }
        }
        //
        CachedPredicate cPredicate = new CachedPredicate(parentPath, pred);
        mPredicates.add(cPredicate);
        return true;
    }

    public void addListener(PredicateModifiedListener newListener) {
        mListenerSet.add(newListener);
    }

    public void modifyPredicateEverywhere(V var,
            MapperPredicate oldPredicate,
            MapperPredicate newPredicate,
            XPathPredicateExpression[] newPredArr) {
        //
        // Update all related predicates
        for (CachedPredicate cPred : mPredicates) {
            if (cPred.getPredicate() == newPredicate) {
                // Skip self
                continue;
            }
            if (cPred.hasInPath(var) != null) {
                Object foundStep = cPred.hasInPath(oldPredicate);
                if (foundStep != null) {
                    assert foundStep instanceof MapperPredicate;
                    //
                    MapperPredicate relatedPredStep = (MapperPredicate)foundStep;
                    relatedPredStep.setPredicates(newPredArr);
                    //
                    PredicatedSchemaContext template = oldPredicate.getSchemaContext();
                    if (template != null) {
                        cPred.getPredicate().modifyPredicate(template, newPredArr);
                    }
                }
            }
        }
        //
        // Modify predicates in other places. 
        for (PredicateModifiedListener listener :  mListenerSet) {
            listener.predicateChanged(var, oldPredicate, newPredicate, newPredArr);
        }
        
    }
    
    /**
     * The predicate only removed from different Caches here - from local
     * predicates' cache and from other caches (CastManager, PseudoCompManager).
     * Phisical removing of the predicate from BPEL variable aria is performed
     * by the MapperLsmProcessor.deleteLsm() method.
     *
     * @param varDecl
     * @param oldPredicate
     */
    protected void deletePredicateInCaches(V var,
            MapperPredicate oldPredicate) {
        //
        // Delete all related predicates
        ListIterator<CachedPredicate> itr = mPredicates.listIterator(); 
        while (itr.hasNext()) {
            CachedPredicate cPred = itr.next();
            if (cPred.hasInPath(var) != null) {
                Object foundStep = cPred.hasInPath(oldPredicate);
                if (foundStep instanceof MapperPredicate) {
                    itr.remove();
                }
            }
        }
        //
        // Modify predicates in other places. 
        for (PredicateModifiedListener listener :  mListenerSet) {
            listener.predicateDeleted(var, oldPredicate);
        }
    }
    
    public void removePredicate(MapperPredicate predToDelete) {
        for (CachedPredicate cPred : mPredicates) {
            MapperPredicate pred = cPred.getPredicate();
            if (pred.equals(predToDelete)) {
                mPredicates.remove(cPred);
                return;
            }
        }
    }
    
    public static String toString(XPathPredicateExpression[] predicatesArr) {
        if (predicatesArr != null && predicatesArr.length != 0) {
            StringBuilder sb = new StringBuilder();
            for (XPathPredicateExpression predicate : predicatesArr) {
                sb.append(predicate.getExpressionString());
            }
            return sb.toString();
        } else {
            return "";
        }
    }
    
    /**
     * This class holds the predicate itself (PredicatedSchemaComp) + 
     * its location and the flag persistent. 
     * 
     * ATTENTION!
     * The location is the different notion relative to the XPathSchemaContext.
     * The schema context consists from SchemaComponent objects only. 
     * The location can contain a Variable, a Part, SchemaComponent and 
     * PredicatedSchemaComp objects. 
     */
    public static class CachedPredicate {
        // The list contains data objects from which a tree path consists of
        // It is implied that it can contain a set of SchemaComponents and 
        // PredicatedSchemaComp. And there is either a variable or 
        // a variable with a part at the end. 
        // The first element is the parent of the predicate!
        // The predicated schema component isn't in the list itself.
        // It it held in the separate attribute mPredSComp.
        private DirectedList<Object> mParentPath;

        // The Schema component which is the base for the predicate.
        private MapperPredicate mPred;
        
        // Persistense means that the instance should not be automatically
        // deleted from the cache if it is not used.
        // The predicates which are not persistent will be removed from
        // the cache automatically.
        private boolean isPersistent;

        public CachedPredicate(DirectedList<Object> parentPath, MapperPredicate pred) {
            mParentPath = ArtificialTreeNode.Utils.filter(parentPath);
            mPred = pred;
        }
        
        public boolean isPersistent() {
            return isPersistent;
        }
        
        public void setPersistent(boolean newValue) {
            isPersistent = newValue;
        }
        
        public SchemaCompHolder getBaseType() {
            return mPred.getSCompHolder(true);
        }
        
        public MapperPredicate getPredicate() {
            return mPred;
        }
        
        /**
         * Returns the list of data objects which indicate the location of 
         * the predicate in the tree. The first element of the list points 
         * to the parent element of the predicate.
         */
        public DirectedList getParentPath() {
            return mParentPath;
        }

        /**
         * Compares the specified schema component with the predicate's base
         * component. The matryoshka's core is used as a base of comparison.
         *
         * @see XPathSchemaContext.Utilities#getMatryoshkaCore(WrappingSchemaContext)
         * @param baseSchemaComp
         * @return
         */
        public boolean hasSameBase(SchemaComponent baseSchemaComp) {
            SchemaCompHolder sCompHolder = getPredicate().getSCompHolder(true);
            assert sCompHolder != null;
            if (sCompHolder.isPseudoComp()) {
                return false;
            } else {
                return sCompHolder.getSchemaComponent().equals(baseSchemaComp);
            }
        }
        
        /**
         * Compares the specified pseudo component with the predicate's base 
         * component. The matryoshka's core is used as a base of comparison.
         *
         * @see XPathSchemaContext.Utilities#getMatryoshkaCore(WrappingSchemaContext)
         * @param baseSchemaComp
         * @return
         */
        public boolean hasSameBase(XPathPseudoComp pseudo) {
            SchemaCompHolder sCompHolder = getPredicate().getSCompHolder(true);
            if (sCompHolder.isPseudoComp()) {
                return sCompHolder.getHeldComponent().equals(pseudo);
            } else {
                return false;
            }
        }
        
        public boolean hasSameBase(SchemaCompHolder baseSchemaCompHolder) {
            // TODO: matryoshka?
            return getPredicate().getSCompHolder(true).equals(baseSchemaCompHolder);
        }
        
        /**
         * Check if the cached predicate has the same schema component 
         * and the same predicates.
         */
        public boolean hasSameParams(SchemaCompHolder schemaCompHolder,
                XPathPredicateExpression[] predArr) {
            MapperPredicate pComp = getPredicate();
            // TODO: matryoshka?
            return pComp.getSCompHolder(false).equals(schemaCompHolder) &&
                    XPathUtils.samePredicatesArr(pComp.getPredicates(), predArr);
        }
        
        public boolean hasSamePredicate(MapperPredicate pred) {
            MapperPredicate pComp = getPredicate();
            return pComp.equals(pred);
        }
        
        public boolean hasSameLocation(Iterable parentPathItrb) {
            return hasSameLocationImpl(parentPathItrb.iterator());
        }
        
        private boolean hasSameLocationImpl(Iterator externalItr) {
            //
            Iterator internalItr = mParentPath.iterator();
            boolean theSame = true;
            while (externalItr.hasNext() && internalItr.hasNext()) {
                Object internalPathStep = internalItr.next();
                Object externalPathStep = externalItr.next();
                if (externalPathStep instanceof ArtificialTreeNode) {
                    if (externalItr.hasNext()) {
                        // Skip artificial tree node
                        externalPathStep = externalItr.next();
                    } else {
                        break;
                    }
                }
                //
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

        /**
         * Checks if the path contains the specified step
         * Returns the found step or null
         * @param soughtStep
         * @return
         */
        public Object hasInPath(Object soughtStep) {
            if (soughtStep != null) {
                // Look throuhg the list in back direction!
                ListIterator itr = mParentPath.backwardIterator();
                while(itr.hasNext()) {
                    Object step = itr.next();
                    if (soughtStep.equals(step)) {
                        return step;
                    }
                }
            }
            return null;
        }
        
        @Override
        public String toString() {
            String endText = mPred.toString() + " persistent=" + isPersistent; // NOI18N
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
            ListIterator itr = mParentPath.backwardIterator();
            boolean isFirst = true;
            while (itr.hasNext()) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    sb.append("/");
                }
                //
                Object stepObj = itr.next();
                sb.append(stepObj.toString());
            }
            return sb.toString();
        }
        
        @Override
        public boolean equals(Object obj2) {
            if (!(obj2 instanceof CachedPredicate)) {
                return false;
            }
            //
            CachedPredicate pred2 = (CachedPredicate)obj2;
            if (!pred2.getPredicate().equals(mPred)) {
                return false;
            }
            //
            DirectedList path2 = pred2.getParentPath();
            if (path2.size() != mParentPath.size()) {
                // Pathes have different length
                return false;
            }
            //
            Iterator itr = mParentPath.forwardIterator();
            Iterator itr2 = path2.forwardIterator();
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
  
