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

package org.netbeans.modules.bpel.mapper.cast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;
import org.netbeans.modules.bpel.mapper.model.BpelMapperModel;
import org.netbeans.modules.bpel.mapper.predicates.editor.PathConverter;
import org.netbeans.modules.bpel.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.bpel.mapper.tree.models.VariableTreeModel;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTreeModel;
import org.netbeans.modules.bpel.mapper.tree.spi.RestartableIterator;
import org.netbeans.modules.bpel.model.api.BPELElementsBuilder;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.ext.editor.api.Cast;
import org.netbeans.modules.bpel.model.ext.editor.api.Casts;
import org.netbeans.modules.bpel.model.ext.editor.api.Editor;
import org.netbeans.modules.bpel.model.ext.editor.api.Source;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.openide.ErrorManager;

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
public class CastManager {
    
    public static CastManager getCastManager(BpelMapperModel mm, boolean inLeftTree) {
        BpelMapperModel mModel = (BpelMapperModel)mm;
        //
        MapperSwingTreeModel treeModel = null;
        if (inLeftTree) {
            treeModel = mModel.getLeftTreeModel();
        } else {
            treeModel = mModel.getRightTreeModel();
        }
        MapperTreeModel sourceModel = treeModel.getSourceModel();
        //
        // Calculate predicate location index
        // int predIndex = treeModel.getChildIndex(mTreePath.getParentPath(), mPred);
        //
        CastManager castManager = getCastManager(sourceModel);
        return castManager;
    }
    
    public static CastManager getCastManager(MapperTreeModel treeModel) {
        VariableTreeModel varTreeModel = MapperTreeModel.Utils.
                findExtensionModel(treeModel, VariableTreeModel.class);
        if (varTreeModel != null) {
            CastManager castManager = varTreeModel.getCastManager();
            return castManager;
        }
        //
        return null;
    }
    
    // The cache of type cast.
    private LinkedList<CachedCast> mCashedCastList;
    
    private boolean mInLeftMapperTree;
    private Object mSynchSource;
    
    public CastManager(Object synchSource) {
        this(true, synchSource);
    }
    
    public CastManager(boolean inLeftMapperTree, Object synchSource) {
        mInLeftMapperTree = inLeftMapperTree;
        mSynchSource = synchSource;
        mCashedCastList = new LinkedList<CachedCast>();
    }
    
    public List<AbstractTypeCast> getTypeCast(
            RestartableIterator<Object> parentPath, SchemaComponent sComp) {
        //    
        ArrayList<AbstractTypeCast> result = new ArrayList<AbstractTypeCast>();
        
        for (CachedCast cCast : mCashedCastList) {
            if (cCast.hasSameBase(sComp) && cCast.hasSameLocation(parentPath)) {
                result.add(cCast.getTypeCast());
            }
        }
        //
        return result;
    }
    
    public boolean addTypeCast(List<Object> parentPath, AbstractTypeCast cast) {
        for (CachedCast cCast : mCashedCastList) {
            if (cCast.hasSameLocation(parentPath) && cCast.hasSameCastTo(cast)) {
                // the same cast already in cache
                return false;
            }
        }
        //
        CachedCast cCast = new CachedCast(parentPath, cast);
        mCashedCastList.add(cCast);
        return true;
    }

    public boolean addTypeCast(RestartableIterator<Object> parentItr, 
            SyntheticTypeCast cast) {
        //
        List<Object> parentPath = 
                PathConverter.constructObjectLocationtList(parentItr);
        //
        if (parentPath != null) {
            if (addTypeCast(parentPath, cast)) {
                registerVariableTypeCast(cast);
            }
        }
        //
        return false;
    }
    
    public void registerVariableTypeCast(final SyntheticTypeCast typeCast) {
        final Variable var = typeCast.getBaseVariable();
        if (var != null) {
            //
            try {
                BpelModel bpelModel = var.getBpelModel();
                bpelModel.invoke(new Callable<Object>() {
                    public Object call() throws Exception {
                        registerTypeCast(var, typeCast);
                        return null;
                    }
                }, mSynchSource);
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }

    
    private Casts registerCastsEntity(BPELElementsBuilder builder, 
            ExtensibleElements destination) {
        //
        Editor editor = null;
        List<Editor> editorList = destination.getChildren(Editor.class);
        if (editorList != null && !editorList.isEmpty()) {
            editor = editorList.get(0);
        }
        //
        if (editor == null) {
            Editor newEditor = builder.createExtensionEntity(Editor.class);
            editor = newEditor;
            destination.addExtensionEntity(Editor.class, editor);
        }
        //
        Casts casts = editor.getCasts();
        if (casts == null) {
            Casts newCasts = builder.createExtensionEntity(Casts.class);
            casts = newCasts;
            editor.setCasts(casts);
        }
        //
        return casts;
    }
    
    public void registerTypeCast(ExtensibleElements destination, 
            AbstractTypeCast newTypeCast) {
        BpelModel bpelModel = destination.getBpelModel();
        BPELElementsBuilder builder = bpelModel.getBuilder();
        //
        Casts casts = registerCastsEntity(builder, destination);
        //
        Cast[] castArr = casts.getCasts();
        //
        boolean isEqualFound = false;
        for (Cast cast : castArr) {
            //
            if (cast.getSource() == Source.TO && mInLeftMapperTree || 
                    cast.getSource() != Source.TO && !mInLeftMapperTree) {
                // Skip casts with oposit source
                continue;
            } 
            //
            TypeCast varTypeCast = TypeCast.convert(cast);
            if (varTypeCast != null) {
                if (varTypeCast.equals(newTypeCast)) {
                    isEqualFound = true;
                    break;
                }
            }
        }
        //
        if (!isEqualFound) {
            Cast newCast = builder.createExtensionEntity(Cast.class);
            newTypeCast.populateCast(newCast, casts, mInLeftMapperTree);
            casts.addCast(newCast);
        }
    }
    
    public void registerTypeCasts(ExtensibleElements destination, 
            Collection<AbstractTypeCast> newTypeCasts) {
        BpelModel bpelModel = destination.getBpelModel();
        BPELElementsBuilder builder = bpelModel.getBuilder();
        //
        Casts casts = registerCastsEntity(builder, destination);
        //
        Cast[] castArr = casts.getCasts();
        //
        for (AbstractTypeCast typeCast : newTypeCasts) {
            boolean isEqualFound = false;
            for (Cast cast : castArr) {
                //
                if (cast.getSource() == Source.TO && mInLeftMapperTree || 
                        cast.getSource() != Source.TO && !mInLeftMapperTree) {
                    // Skip casts with oposit source
                    continue;
                } 
                //
                TypeCast varTypeCast = TypeCast.convert(cast);
                if (varTypeCast != null) {
                    if (varTypeCast.equals(typeCast)) {
                        isEqualFound = true;
                        break;
                    }
                }
            }
            //
            if (!isEqualFound) {
                Cast newCast = builder.createExtensionEntity(Cast.class);
                typeCast.populateCast(newCast, casts, mInLeftMapperTree);
                casts.addCast(newCast);
            }
        }
    }
    
    public boolean addTypeCast(Cast cast) {
        TypeCast typeCast = TypeCast.convert(cast);
        if (typeCast != null) {
            List<Object> parentPath = 
                    PathConverter.constructObjectLocationtList(
                    typeCast.getXPathExpression());
            //
            // Remove first component of the path
            int size = parentPath.size();
            assert size > 0;
            parentPath.remove(0);
            //
            return addTypeCast(parentPath, typeCast);
        }
        return false;
    }
    
    public void removeTypeCast(AbstractTypeCast castToDelete) {
        for (CachedCast cCast : mCashedCastList) {
            AbstractTypeCast cast = cCast.getTypeCast();
            if (cast.equals(castToDelete)) {
                mCashedCastList.remove(cast);
                break;
            }
        }
    }
    
    @Override
    public String toString() {
        return " inLeftTree:" + mInLeftMapperTree + 
                "  TypeCastCount: " + mCashedCastList.size() + 
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
    public static class CachedCast {
        // The list contains data objects from which a tree path consists of
        // It is implied that it can contain a set of SchemaComponents and 
        // PredicatedSchemaComp, special steps or another type casts. 
        // And there is either a variable or a variable with a part at the end. 
        // The first element is the parent of the type cast!
        // The type cast component isn't in the list itself.
        // It it held in the separate attribute mTypeCast.
        private List<Object> mParentPath;

        // The type cast.
        private AbstractTypeCast mTypeCast;
        
        public CachedCast(List<Object> parentPath, AbstractTypeCast cast) {
            mParentPath = parentPath;
            mTypeCast = cast;
        }
        
        public SchemaComponent getBaseType() {
            return mTypeCast.getSComponent();
        }
        
        public AbstractTypeCast getTypeCast() {
            return mTypeCast;
        }
        
        /**
         * Returns the list of data objects which indicate the location of 
         * the type cast in the tree. The first element of the list points 
         * to the parent element of the type cast.
         */
        public List getParentPath() {
            return mParentPath;
        }
        
        public boolean hasSameBase(SchemaComponent baseSchemaComp) {
            return getTypeCast().getSComponent().equals(baseSchemaComp);
        }
        
        public boolean hasSameCastTo(AbstractTypeCast cast) {
            GlobalType gType = getTypeCast().getCastTo();
            return gType.equals(cast.getCastTo());
        }
        
        public boolean hasSameLocation(RestartableIterator parentPathItr) {
            parentPathItr.restart();
            return hasSameLocationImpl(parentPathItr);
        }
        
        public boolean hasSameLocation(List<Object> parentPath) {
            Iterator externalItr = parentPath.iterator();
            return hasSameLocationImpl(externalItr);
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
            if (!(obj2 instanceof CachedCast)) {
                return false;
            }
            //
            CachedCast castpred2 = (CachedCast)obj2;
            if (!castpred2.getTypeCast().equals(mTypeCast)) {
                return false;
            }
            //
            List path2 = castpred2.getParentPath();
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
  
