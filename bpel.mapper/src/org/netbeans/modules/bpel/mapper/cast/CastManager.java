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
import org.netbeans.modules.bpel.mapper.model.PathConverter;
import org.netbeans.modules.bpel.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.bpel.mapper.tree.models.VariableTreeModel;
import org.netbeans.modules.soa.ui.tree.impl.IterableExpander;
import org.netbeans.modules.bpel.model.api.AbstractVariableDeclaration;
import org.netbeans.modules.bpel.model.api.BPELElementsBuilder;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.support.XPathBpelVariable;
import org.netbeans.modules.bpel.model.ext.editor.api.Cast;
import org.netbeans.modules.bpel.model.ext.editor.api.Casts;
import org.netbeans.modules.bpel.model.ext.editor.api.Editor;
import org.netbeans.modules.bpel.model.ext.editor.api.PseudoComp;
import org.netbeans.modules.bpel.model.ext.editor.api.Source;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Part;
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
        SoaTreeModel sourceModel = treeModel.getSourceModel();
        //
        // Calculate predicate location index
        // int predIndex = treeModel.getChildIndex(mTreePath.getParentPath(), mPred);
        //
        CastManager castManager = getCastManager(sourceModel);
        return castManager;
    }
    
    public static CastManager getCastManager(SoaTreeModel treeModel) {
        VariableTreeModel varTreeModel = SoaTreeModel.MyUtils.
                findExtensionModel(treeModel, VariableTreeModel.class);
        if (varTreeModel != null) {
            CastManager castManager = varTreeModel.getCastManager();
            return castManager;
        }
        //
        return null;
    }
    
    // The cache of type cast.
    private LinkedList<CachedCast> mCachedCastList;
    
    // The cache of casted variables.
    private LinkedList<CachedVariableCast> mCachedCastedVarList;
    
    private boolean mInLeftMapperTree;
    private Object mSynchSource;
    
    public CastManager(Object synchSource) {
        this(true, synchSource);
    }
    
    public CastManager(boolean inLeftMapperTree, Object synchSource) {
        mInLeftMapperTree = inLeftMapperTree;
        mSynchSource = synchSource;
        mCachedCastList = new LinkedList<CachedCast>();
        mCachedCastedVarList = new LinkedList<CachedVariableCast>();
    }
    
    //-------------------------------------------------------
    
    public List<AbstractTypeCast> getTypeCast(
            TreeItem paretnTreeItem, SchemaComponent castedComp) {
        //
        // Convert the iterator, which points to the paretn element to the 
        // iterator, which points to the casted component.
        IterableExpander<Object> baseCompItr = new IterableExpander<Object>(
                paretnTreeItem, castedComp); 
        //
        ArrayList<AbstractTypeCast> result = new ArrayList<AbstractTypeCast>();
        //
        for (CachedCast cCast : mCachedCastList) {
            if (cCast.hasSameCastedCompLocation(baseCompItr)) {
                result.add(cCast.getTypeCast());
            }
        }
        //
        return result;
    }
    
    public boolean addTypeCast(Cast cast) {
        TypeCast typeCast = TypeCast.convert(cast);
        if (typeCast != null) {
            List<Object> castedCompPath = 
                    PathConverter.constructObjectLocationtList(
                    typeCast.getPathExpression());
            //
            if (castedCompPath != null) {
                return addTypeCastImpl(castedCompPath, typeCast);
            }
        }
        //
        return false;
    }
    
    private boolean addTypeCastImpl(List<Object> castedCompPath, AbstractTypeCast cast) {
        //
        XPathBpelVariable xPathVar = PathConverter.constructXPathBpelVariable(castedCompPath);
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

    public boolean addTypeCast(TreeItem castedTreeItem, SyntheticTypeCast cast) {
        //
        List<Object> castedCompPath = PathConverter.constructObjectLocationtList(
                castedTreeItem, true, false);
        //
        if (castedCompPath != null) {
            if (addTypeCastImpl(castedCompPath, cast)) {
                registerVariableTypeCast(cast);
                return true;
            }
        }
        //
        return false;
    }
    
    //-------------------------------------------------------
    
    /**
     * Returns casted variables based on the specified variable. 
     * @param parentPath
     * @param var
     * @return
     */
    public List<AbstractTypeCast> getCastedVariables(
            AbstractVariableDeclaration var, Part part) {
        //
        ArrayList<AbstractTypeCast> result = new ArrayList<AbstractTypeCast>();
        if (mCachedCastedVarList != null && !mCachedCastedVarList.isEmpty()) {
            XPathBpelVariable xPathVar = new XPathBpelVariable(var, part);
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
    
    public boolean addCastedVariable(AbstractVariableDeclaration var, Part part, 
            AbstractTypeCast cast) {
        //
        XPathBpelVariable xPathVar = new XPathBpelVariable(var, part);
        return addCastedVariableImpl(xPathVar, cast);
    }
        
    private boolean addCastedVariableImpl(XPathBpelVariable xPathVar, 
            AbstractTypeCast cast) {
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

    private Casts getCastsEntity(BPELElementsBuilder builder, 
            ExtensibleElements destination, boolean create) {
        //
        Editor editor = null;
        List<Editor> editorList = destination.getChildren(Editor.class);
        if (editorList != null && !editorList.isEmpty()) {
            editor = editorList.get(0);
        }
        //
        if (editor == null) {
            if (!create) {
                return null;
            }
            Editor newEditor = builder.createExtensionEntity(Editor.class);
            editor = newEditor;
            destination.addExtensionEntity(Editor.class, editor);
        }
        //
        Casts casts = editor.getCasts();
        if (casts == null) {
            if (!create) {
                return null;
            }
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
        Casts casts = getCastsEntity(builder, destination, true);
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
            casts.addCast(newCast);
            newTypeCast.populateCast(newCast, casts, mInLeftMapperTree);
        }
    }
    
    public void registerTypeCasts(ExtensibleElements destination, 
            Collection<AbstractTypeCast> newTypeCasts) {
        BpelModel bpelModel = destination.getBpelModel();
        BPELElementsBuilder builder = bpelModel.getBuilder();
        //
        Casts casts = getCastsEntity(builder, destination, true);
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
                casts.addCast(newCast);
                typeCast.populateCast(newCast, casts, mInLeftMapperTree);
            }
        }
    }
    
    public void deleteTypeCast(final AbstractTypeCast castToUnreg) {
        final AbstractVariableDeclaration var = castToUnreg.getBaseVariable();
        if (var != null) {
            //
            // TODO: check if the deleted cast is used somewhere and ask 
            // user's confirmation if so. 
            //
            try {
                BpelModel bpelModel = ((BpelEntity)var).getBpelModel();
                bpelModel.invoke(new Callable<Object>() {
                    public Object call() throws Exception {
                        unregisterTypeCast((ExtensibleElements)var, castToUnreg);
                        clearEmptyEditorEntity((ExtensibleElements)var);
                        removeTypeCast(castToUnreg);
                        return null;
                    }
                }, mSynchSource);
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }
    
    /**
     * Deletes the Cast eitity from the BPEL sources at the specified 
     * destination.
     * 
     * @param destination
     * @param castToUnreg
     */
    public void unregisterTypeCast(ExtensibleElements destination, 
            AbstractTypeCast castToUnreg) {
        //
        BpelModel bpelModel = destination.getBpelModel();
        BPELElementsBuilder builder = bpelModel.getBuilder();
        //
        Casts casts = getCastsEntity(builder, destination, false);
        //
        if (casts == null) {
            return; // Nothing to unregister
        }
        //
        Cast[] castArr = casts.getCasts();
        for (int index = 0; index < castArr.length; index++) {
            Cast cast = castArr[index];
            //
            if (mInLeftMapperTree && cast.getSource() == Source.TO ||
                    !mInLeftMapperTree && cast.getSource() != Source.TO) {
                // Skip casts with oposite source
                continue;
            }
            //
            TypeCast tCast = TypeCast.convert(cast);
            if (tCast != null && castToUnreg.equals(tCast)) {
                // The required cast found!
                casts.removeCast(index);
            }
        }
        //
    }
    
    /**
     * Clear empty Editor extensions from the destination.
     * @param destination
     */
    public void clearEmptyEditorEntity(ExtensibleElements destination) {
        List<Editor> editorExt = destination.getChildren(Editor.class);
        for (Editor editor : editorExt) {
            if (clearEmptyContainer(editor)) {
                destination.remove(editor);
            }
        }
    }
    
    /**
     * Recursively deletes empty containers. 
     * The method has to be called inside of transaction.
     * @param container
     * @return true if all children were deleted.
     */
    private boolean clearEmptyContainer(BpelContainer container) {
        List<BpelEntity> children = container.getChildren();
        int initialSize = children.size();
        int deletedChildCount = 0;
        for (BpelEntity child : children) {
            if (child instanceof Cast || child instanceof PseudoComp) {
                // The Cast and PseudoComp are leaf objects in the 
                // Editor extension hierarchy extension tree. 
                continue;
            }
            if (child instanceof BpelContainer) {
                if (clearEmptyContainer((BpelContainer)child)) {
                    container.remove(child);
                    deletedChildCount++;
                }
            }
        }
        //
        return deletedChildCount == initialSize;
    }
    
    /**
     * Removes the specified cast from the cache.
     * @param castToDelete
     */
    public void removeTypeCast(AbstractTypeCast castToDelete) {
        ListIterator<CachedVariableCast> varItr = mCachedCastedVarList.listIterator();
        while (varItr.hasNext()) {
            CachedVariableCast cVarCast = varItr.next();
            AbstractTypeCast cast = cVarCast.getTypeCast();
            if (cast.equals(castToDelete)) {
                varItr.remove();
                return;
            }
        }
        //
        ListIterator<CachedCast> typeItr = mCachedCastList.listIterator();
        while (typeItr.hasNext()) {
            CachedCast cCast = typeItr.next();
            AbstractTypeCast cast = cCast.getTypeCast();
            if (cast.equals(castToDelete)) {
                typeItr.remove();
                return;
            }
        }
    }

    @Override
    public String toString() {
        return " inLeftTree:" + mInLeftMapperTree + 
                "  TypeCastCount: " + mCachedCastList.size() + 
                "  ||  " + super.toString(); 
    }
    
    public static class CachedVariableCast {
        // The type cast.
        private XPathBpelVariable mVariable;
        
        // The type cast.
        private AbstractTypeCast mTypeCast;
        
        public CachedVariableCast(XPathBpelVariable variable, 
                AbstractTypeCast typeCast) {
            //
            assert variable != null && typeCast != null;
            //
            mVariable = variable;
            mTypeCast = typeCast;
        }

        public XPathBpelVariable getCastedVariableDecl() {
            return mVariable;
        }
        
        public AbstractTypeCast getTypeCast() {
            return mTypeCast;
        }
        
        public boolean hasSameValues(XPathBpelVariable variable, 
                AbstractTypeCast typeCast) {
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
        private List<Object> mCastedCompPath;

        // The type cast.
        private AbstractTypeCast mTypeCast;
        
        public CachedCast(List<Object> castedCompPath, AbstractTypeCast cast) {
            mCastedCompPath = castedCompPath;
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
         * the casted component to whith the type cast is applied.
         * The casted and casting components are always sibling in the tree.
         * So the second element of the list is the parent of the cast.
         */
        public List<Object> getCastedCompPath() {
            return mCastedCompPath;
        }
        
        public boolean hasSameBase(SchemaComponent baseSchemaComp) {
            return getTypeCast().getSComponent().equals(baseSchemaComp);
        }
        
        public boolean hasSameCastTo(AbstractTypeCast cast) {
            GlobalType type = getTypeCast().getType();
            return type.equals(cast.getType());
        }
        
        public boolean hasSameCastedCompLocation(Iterable castedCompItrb) {
            return hasSameCastedCompPathImpl(castedCompItrb.iterator());
        }
        
        public boolean hasSameCastedCompPath(List<Object> castedCompPath) {
            Iterator externalItr = castedCompPath.iterator();
            return hasSameCastedCompPathImpl(externalItr);
        }
        
        private boolean hasSameCastedCompPathImpl(Iterator externalItr) {
            //
            Iterator internalItr = mCastedCompPath.iterator();
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
            ListIterator itr = mCastedCompPath.listIterator(mCastedCompPath.size());
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
            List path2 = castpred2.getCastedCompPath();
            if (path2.size() != mCastedCompPath.size()) {
                // Pathes have diferrent length
                return false;
            }
            //
            Iterator itr = mCastedCompPath.listIterator();
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
  
