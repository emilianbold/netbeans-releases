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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.xpath.mapper.tree.ArtificialTreeNode;
import org.netbeans.modules.soa.xpath.mapper.tree.DirectedList;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.soa.xpath.mapper.tree.PathConverter;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.PredicatedSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;

/**
 * The class collects all Pseudo Components, which are used in the edited 
 * XPath expression. Each PseudoComp is declared in the special BPEL extension element.
 * The PseudoComp manager keeps the location of type PseudoComp in term of
 * schema components' path.
 *
 * The main intention of the PseudoComp manager is to provide showing of
 * PseudoComp objects in the mapper source and destination trees.
 * 
 * @author nk160297
 */
public abstract class PseudoCompManager<V>
        implements PseudoCompCommands, PredicateModifiedListener<V> {
    
    // The cache of Pseudo components.
    private LinkedList<CachedPseudoComp> mCachedPseudoCompList;
    
    protected MapperSwingTreeModel mMapperTreeModel;
    
    public PseudoCompManager() {
        this(null);
    }

    public PseudoCompManager(MapperSwingTreeModel mapperTreeModel) {
        mMapperTreeModel = mapperTreeModel;
        mCachedPseudoCompList = new LinkedList<CachedPseudoComp>();
    }
    
    //-------------------------------------------------------

    public MapperPseudoComp createMapperPseudoComp(XPathPseudoComp pseudoComp) {
        return new MapperPseudoComp(pseudoComp);
    }

    public void attachToTreeModel(MapperSwingTreeModel mapperTreeModel) {
        mMapperTreeModel = mapperTreeModel;
    }

    public List<XPathPseudoComp> getPseudoComp(TreeItem parentTreeItem) {
        //
        ArrayList<XPathPseudoComp> result = new ArrayList<XPathPseudoComp>();
        //
        for (CachedPseudoComp cPseudoComp : mCachedPseudoCompList) {
            if (cPseudoComp.hasSameLocation(parentTreeItem, false)) {
                result.add(cPseudoComp.getPseudoComp());
            }
        }
        //
        return result;
    }
    
    public XPathPseudoComp getPseudoComp(TreeItem treeItem, 
            boolean skipPathFirst, String soughtName, String soughtNamespace, 
            boolean isAttribute) {
        //
        for (CachedPseudoComp cPseudoComp : mCachedPseudoCompList) {
            if (cPseudoComp.hasSameLocation(treeItem, true)) {
                XPathPseudoComp pseudo = cPseudoComp.getPseudoComp();
                if (pseudo.isAttribute() == isAttribute && 
                        pseudo.getName().equals(soughtName) && 
                        pseudo.getNamespace().equals(soughtNamespace)) {
                    return pseudo;
                }
            }
        }
        //
        return null;
    }
    
    public boolean addPseudoComp(XPathPseudoComp pseudoComp) {
        if (pseudoComp != null) {
            XPathSchemaContext sContext = pseudoComp.getSchemaContext();
            if (sContext != null) {
                PathConverter pathConverter = mMapperTreeModel.getPathConverter();
                DirectedList<Object> parentPath = pathConverter.
                        constructObjectLocationList(sContext, true, false);
                //
                if (parentPath != null && !parentPath.isEmpty()) {
                    // Convert the XPathPseudoComp to MapperPseudoComp if necessary
                    if (!(pseudoComp instanceof MapperPseudoComp)) {
                        pseudoComp = createMapperPseudoComp(pseudoComp);
                    }
                    return addPseudoCompImpl(
                            parentPath, (MapperPseudoComp)pseudoComp);
                }
            }
        }
        //
        return false;
    }
    
    protected boolean addPseudoCompImpl(DirectedList<Object> parentCompPath,
            MapperPseudoComp pseudo) {
        //
        for (CachedPseudoComp cPseudoComp : mCachedPseudoCompList) {
            if (cPseudoComp.hasSameLocation(parentCompPath, false) && 
                    cPseudoComp.hasSamePseudoComp(pseudo)) {
                // the same pseudo already in cache
                return false;
            }
        }
        //
        CachedPseudoComp cPseudo = new CachedPseudoComp(parentCompPath, pseudo);
        mCachedPseudoCompList.add(cPseudo);
        return true;
    }

    /**
     * Removes the specified PseudoComp from the cache.
     * @param pseudoToDelete
     */
    public void removePseudoComp(XPathPseudoComp pseudoToDelete) {
        ListIterator<CachedPseudoComp> itr = mCachedPseudoCompList.listIterator();
        while (itr.hasNext()) {
            CachedPseudoComp cPseudo = itr.next();
            XPathPseudoComp pseudo = cPseudo.getPseudoComp();
            if (pseudo.equals(pseudoToDelete)) {
                itr.remove();
                return;
            }
        }
    }
    
    public void predicateChanged(V var,
            MapperPredicate oldPredicate,
            MapperPredicate newPredicate,
            XPathPredicateExpression[] newPredArr) {
        //
        // Collect all related predicates
        for (CachedPseudoComp cPseudoComp : mCachedPseudoCompList) {
            if (cPseudoComp.hasInPath(var) != null) {
                Object foundStep = cPseudoComp.hasInPath(oldPredicate);
                if (foundStep != null) {
                    assert foundStep instanceof MapperPredicate;
                    //
                    MapperPredicate relatedPredStep = (MapperPredicate)foundStep;
                    relatedPredStep.setPredicates(newPredArr);
                    //
                    PredicatedSchemaContext template = oldPredicate.getSchemaContext();
                    if (template != null) {
                        cPseudoComp.getPseudoComp().modifyPredicate(template, newPredArr);
                    }
                }
            }
        }
    }
    

    public void predicateDeleted(V var,
            MapperPredicate deletedPredicate) {
        //
        // Delete all related predicates inside of variable declaration
        // Only CashedCast can have predicates. It doesn't necessary 
        // to process the list of CachedVariableCast objects.
        ListIterator<CachedPseudoComp> itr = mCachedPseudoCompList.listIterator(); 
        while (itr.hasNext()) {
            CachedPseudoComp cPseudoComp = itr.next();
            if (cPseudoComp.hasInPath(var) != null) {
                Object foundStep = cPseudoComp.hasInPath(deletedPredicate);
                if (foundStep instanceof MapperPredicate) {
                    itr.remove();
                }
            }
        }
    }
    
    @Override
    public String toString() {
        return " inLeftTree:" + mMapperTreeModel.isLeftMapperTree() +
                "  PseudoCompCount: " + mCachedPseudoCompList.size() + 
                "  ||  " + super.toString(); 
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
    public static class CachedPseudoComp {
        // Points to the parent of the cached Pseudo Schema Component. 
        // The list can contain any data objects from which a tree path consists of
        // It can be a SchemaComponent, PredicatedSchemaComp, special steps, 
        // type casts or another pseudo schema component. 
        // And there is either a variable or a variable with a part at the end. 
        // The first element is the parent of the Pseudo component!
        // The Pseudo component isn't in the list itself.
        // It it held in the separate attribute mPseudoComp.
        private DirectedList<Object> mParentPath;

        // The type cast.
        private MapperPseudoComp mPseudoComp;
        
        public CachedPseudoComp(DirectedList<Object> parentPath, MapperPseudoComp pseudo) {
            mParentPath = ArtificialTreeNode.Utils.filter(parentPath);
            mPseudoComp = pseudo;
        }
        
        public MapperPseudoComp getPseudoComp() {
            return mPseudoComp;
        }
        
        /**
         * Returns the list of data objects which point to the parent 
         * of the pseudo component.
         */
        public DirectedList<Object> getParentPath() {
            return mParentPath;
        }
        
        public boolean hasSamePseudoComp(XPathPseudoComp pseudo) {
            return MapperPseudoComp.equalTypeName(getPseudoComp(), pseudo);
        }
        
//        public boolean hasSameCastedCompLocation(RestartableIterator castedCompItr) {
//            castedCompItr.restart();
//            return hasSameCastedCompPathImpl(castedCompItr);
//        }
        
        private boolean hasSameLocation(Iterable<Object> parentPathItrb, 
                boolean skipPathFirst) {
            //
            Iterator internalItr = mParentPath.iterator();
            Iterator externalItr = parentPathItrb.iterator();
            //
            if (skipPathFirst && externalItr.hasNext()) {
                externalItr.next();
            }
            //
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
            if (theSame && internalItr.hasNext())  {
                // internal location path longer then required. 
                // It isn't allowed. It can be shorter only. 
                return false;
            }
            //
            return theSame;
        }
        
        @Override
        public String toString() {
            String endText = mPseudoComp.toString(); // NOI18N
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
        public boolean equals(Object obj2) {
            if (!(obj2 instanceof CachedPseudoComp)) {
                return false;
            }
            //
            CachedPseudoComp pseudo2 = (CachedPseudoComp)obj2;
            if (!pseudo2.getPseudoComp().equals(mPseudoComp)) {
                return false;
            }
            //
            DirectedList path2 = pseudo2.getParentPath();
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
  
