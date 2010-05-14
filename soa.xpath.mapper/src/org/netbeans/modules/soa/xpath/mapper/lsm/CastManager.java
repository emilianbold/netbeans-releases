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
import org.netbeans.modules.soa.ui.tree.impl.IterableExpander;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.xpath.mapper.tree.ArtificialTreeNode;
import org.netbeans.modules.soa.xpath.mapper.tree.DirectedList;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.soa.xpath.mapper.tree.PathConverter;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.CastSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.PredicatedSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCast;
import org.netbeans.modules.xml.xpath.ext.spi.XPathVariable;

/**
 * The class collects all type casts which are used in the edited 
 * XPath expression. Each type cast is declared in the special BPEL extension element.
 * The cast manager keeps the location of type cast in term of
 * schema components' path.
 *
 * The main intention of the cast manager is to provide showing of
 * type casts in the mapper source and destination trees.
 * 
 * @author nk160297
 */
public abstract class CastManager<V>
        implements CastCommands, PredicateModifiedListener<V> {
    
    // The cache of type cast.
    private LinkedList<CachedCast> mCachedCastList;
    
    // The cache of casted variables.
    protected LinkedList<CachedVariableCast> mCachedCastedVarList;

    protected MapperSwingTreeModel mMapperTreeModel;
    
    protected CastManager() {
        this(null);
    }

    protected CastManager(MapperSwingTreeModel mapperTreeModel) {
        mMapperTreeModel = mapperTreeModel;
        mCachedCastList = new LinkedList<CachedCast>();
        mCachedCastedVarList = new LinkedList<CachedVariableCast>();
    }
    
    //-------------------------------------------------------

    public MapperTypeCast createMapperTypeCast(XPathCast cast) {
        return new MapperTypeCast(cast);
    }

    public void attachToTreeModel(MapperSwingTreeModel mapperTreeModel) {
        mMapperTreeModel = mapperTreeModel;
    }

    public List<MapperTypeCast> getTypeCast(
            TreeItem paretnTreeItem, SchemaComponent castedComp) {
        //
        // Convert the iterator, which points to the paretn element to the 
        // iterator, which points to the casted component.
        IterableExpander<Object> baseCompItr = new IterableExpander<Object>(
                paretnTreeItem, castedComp); 
        //
        ArrayList<MapperTypeCast> result = new ArrayList<MapperTypeCast>();
        //
        for (CachedCast cCast : mCachedCastList) {
            if (cCast.hasSameCastedCompLocation(baseCompItr)) {
                result.add(cCast.getTypeCast());
            }
        }
        //
        return result;
    }
    
    public boolean addTypeCast(XPathCast typeCast) {
        if (typeCast != null) {
            XPathSchemaContext sContext = typeCast.getSchemaContext();
            assert sContext instanceof CastSchemaContext;
            if (sContext != null) {
                CastSchemaContext castSContext = (CastSchemaContext)sContext;
                //
                PathConverter pathConverter = mMapperTreeModel.getPathConverter();
                DirectedList<Object> castedCompPath =
                        pathConverter.constructObjectLocationList(
                        castSContext.getBaseContext(), true, false);
                //
                if (castedCompPath != null && !castedCompPath.isEmpty()) {
                    // Convert the XPathCast to MapperTypeCast if necessary
                    if (!(typeCast instanceof MapperTypeCast)) {
                        typeCast = createMapperTypeCast(typeCast);
                    }
                    return addTypeCastImpl(castedCompPath, (MapperTypeCast)typeCast);
                }
            }
        }
        //
        return false;
    }
    
    public boolean addTypeCastImpl(DirectedList<Object> castedCompPath,
            MapperTypeCast cast) {
        //
        PathConverter pathConverter = mMapperTreeModel.getPathConverter();
        XPathVariable xPathVar = pathConverter.constructXPathVariable(castedCompPath);
        if (xPathVar != null) {
            return addCastedVariableImpl(xPathVar, cast);
        } else {
            //
            for (CachedCast cCast : mCachedCastList) {
                if (cCast.hasSameCastedCompPath(castedCompPath) && cCast.hasSameCastTo(cast)) {
                    // the same cast already in cache
                    return false;
                }
            }
            //
            CachedCast cCast = new CachedCast(castedCompPath, cast);
            mCachedCastList.add(cCast);
            return true;
        }
    }

    //-------------------------------------------------------
    
    /**
     * Returns casted variables based on the specified variable. 
     * @param parentPath
     * @param var
     * @return
     */
    public List<MapperTypeCast> getCastedVariables(XPathVariable xPathVar) {
        //
        ArrayList<MapperTypeCast> result = new ArrayList<MapperTypeCast>();
        if (mCachedCastedVarList != null && !mCachedCastedVarList.isEmpty()) {
            //
            for (CachedVariableCast cVCast : mCachedCastedVarList) {
                if (cVCast.getCastedVariableDecl().equals(xPathVar)) {
                    result.add(cVCast.getTypeCast());
                }
            }
        }
        //
        return result;
    }
    
    private boolean addCastedVariableImpl(XPathVariable xPathVar,
            MapperTypeCast cast) {
        //
        for (CachedVariableCast cVCast : mCachedCastedVarList) {
            if (cVCast.hasSameValues(xPathVar, cast) ) {
                // the same cast already in cache
                return false;
            }
        }
        //
        CachedVariableCast cVCast = new CachedVariableCast(xPathVar, cast);
        mCachedCastedVarList.add(cVCast);
        return true;
    }

    //-------------------------------------------------------
    
    /**
     * Removes the specified cast from the cache.
     * @param castToDelete
     */
    public void removeTypeCast(MapperTypeCast castToDelete) {
        ListIterator<CachedVariableCast> varItr = mCachedCastedVarList.listIterator();
        while (varItr.hasNext()) {
            CachedVariableCast cVarCast = varItr.next();
            MapperTypeCast cast = cVarCast.getTypeCast();
            if (cast.equals(castToDelete)) {
                varItr.remove();
                return;
            }
        }
        //
        ListIterator<CachedCast> typeItr = mCachedCastList.listIterator();
        while (typeItr.hasNext()) {
            CachedCast cCast = typeItr.next();
            MapperTypeCast cast = cCast.getTypeCast();
            if (cast.equals(castToDelete)) {
                typeItr.remove();
                return;
            }
        }
    }
    
    public void predicateChanged(V var,
            MapperPredicate oldPredicate,
            MapperPredicate newPredicate,
            XPathPredicateExpression[] newPredArr) {
        //
        // Process all related predicates inside of variable declaration
        // Only CashedCast can have predicates. It doesn't necessary 
        // to process the list of CachedVariableCast objects.
        for (CachedCast cCast : mCachedCastList) {
            if (cCast.hasInPath(var) != null) {
                Object foundStep = cCast.hasInPath(oldPredicate);
                if (foundStep != null) {
                    assert foundStep instanceof MapperPredicate;
                    //
                    MapperPredicate relatedPredStep = (MapperPredicate)foundStep;
                    relatedPredStep.setPredicates(newPredArr);
                    //
                    PredicatedSchemaContext template = oldPredicate.getSchemaContext();
                    if (template != null) {
                        cCast.getTypeCast().modifyPredicate(template, newPredArr);
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
        ListIterator<CachedCast> itr = mCachedCastList.listIterator(); 
        while (itr.hasNext()) {
            CachedCast cCast = itr.next();
            if (cCast.hasInPath(var) != null) {
                Object foundStep = cCast.hasInPath(deletedPredicate);
                if (foundStep instanceof MapperPredicate) {
                    itr.remove();
                }
            }
        }
    }
    
    @Override
    public String toString() {
        return " inLeftTree:" + mMapperTreeModel.isLeftMapperTree() +
                "  TypeCastCount: " + mCachedCastList.size() + 
                "  ||  " + super.toString(); 
    }
    
    public static class CachedVariableCast {
        // The type cast.
        private XPathVariable mVariable;
        
        // The type cast.
        private MapperTypeCast mTypeCast;
        
        public CachedVariableCast(XPathVariable variable,
                MapperTypeCast typeCast) {
            //
            assert variable != null && typeCast != null;
            //
            mVariable = variable;
            mTypeCast = typeCast;
        }

        public XPathVariable getCastedVariableDecl() {
            return mVariable;
        }
        
        public MapperTypeCast getTypeCast() {
            return mTypeCast;
        }
        
        public boolean hasSameValues(XPathVariable variable,
                MapperTypeCast typeCast) {
            return mVariable.equals(variable) && mTypeCast.equals(typeCast);
        }
        
        @Override
        public  boolean equals(Object obj) {
            if (obj instanceof CachedVariableCast) {
                CachedVariableCast other = (CachedVariableCast)obj;
                return mVariable.equals(other.getCastedVariableDecl()) && 
                        mTypeCast.equals(other.getTypeCast());
            }
            //
            return false;
        }
    
        @Override
        public String toString() {
            return "Var: " + mVariable.toString() + 
                    " Cast: " + mTypeCast.toString(); // NOI18N
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
    public static class CachedCast {
        // Points to the Casted Schema Component. 
        // The list contains data objects from which a tree path consists of
        // It is implied that it can contain a set of SchemaComponents and 
        // PredicatedSchemaComp, special steps or another type casts. 
        // And there is either a variable or a variable with a part at the end. 
        // The first element is the casted component! The second is the parent 
        // of the casted and casting components (they are sibling).
        // The type cast component isn't in the list itself.
        // It it held in the separate attribute mTypeCast.
        private DirectedList<Object> mCastedCompPath;

        // The type cast.
        private MapperTypeCast mTypeCast;
        
        public CachedCast(DirectedList<Object> castedCompPath, MapperTypeCast cast) {
            mCastedCompPath = ArtificialTreeNode.Utils.filter(castedCompPath);
            mTypeCast = cast;
        }
        
        public SchemaComponent getBaseType() {
            return mTypeCast.getSComponent();
        }
        
        public MapperTypeCast getTypeCast() {
            return mTypeCast;
        }
        
        /**
         * Returns the list of data objects which indicate the location of 
         * the casted component to whith the type cast is applied.
         * The casted and casting components are always sibling in the tree.
         * So the second element of the list is the parent of the cast.
         */
        public DirectedList<Object> getCastedCompPath() {
            return mCastedCompPath;
        }
        
        public boolean hasSameBase(SchemaComponent baseSchemaComp) {
            return getTypeCast().getSComponent().equals(baseSchemaComp);
        }
        
        public boolean hasSameCastTo(MapperTypeCast cast) {
            GlobalType type = getTypeCast().getType();
            return type.equals(cast.getType());
        }
        
        public boolean hasSameCastedCompLocation(Iterable castedCompItrb) {
            return hasSameCastedCompPathImpl(castedCompItrb.iterator());
        }
        
        public boolean hasSameCastedCompPath(DirectedList<Object> castedCompPath) {
            Iterator externalItr = castedCompPath.iterator();
            return hasSameCastedCompPathImpl(externalItr);
        }
        
        private boolean hasSameCastedCompPathImpl(Iterator externalItr) {
            //
            Iterator internalItr = mCastedCompPath.iterator();
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
                ListIterator itr = mCastedCompPath.backwardIterator();
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
            String endText = mTypeCast.toString(); // NOI18N
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
            ListIterator itr = mCastedCompPath.backwardIterator();
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
            if (!(obj2 instanceof CachedCast)) {
                return false;
            }
            //
            CachedCast castpred2 = (CachedCast)obj2;
            if (!castpred2.getTypeCast().equals(mTypeCast)) {
                return false;
            }
            //
            DirectedList path2 = castpred2.getCastedCompPath();
            if (path2.size() != mCastedCompPath.size()) {
                // Pathes have different length
                return false;
            }
            //
            Iterator itr = mCastedCompPath.forwardIterator();
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
  
