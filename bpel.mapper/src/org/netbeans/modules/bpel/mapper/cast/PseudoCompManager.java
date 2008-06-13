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
import org.netbeans.modules.bpel.mapper.model.BpelMapperUtils;
import org.netbeans.modules.bpel.mapper.predicates.editor.PathConverter;
import org.netbeans.modules.bpel.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.bpel.mapper.tree.models.VariableTreeModel;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTreeModel;
import org.netbeans.modules.bpel.model.api.BPELElementsBuilder;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.ext.editor.api.Editor;
import org.netbeans.modules.bpel.model.ext.editor.api.PseudoComp;
import org.netbeans.modules.bpel.model.ext.editor.api.PseudoComps;
import org.netbeans.modules.bpel.model.ext.editor.api.Source;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;
import org.openide.ErrorManager;

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
public class PseudoCompManager {
    
    public static PseudoCompManager getPseudoCompManager(BpelMapperModel mm, boolean inLeftTree) {
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
        PseudoCompManager castManager = getPseudoCompManager(sourceModel);
        return castManager;
    }
    
    public static PseudoCompManager getPseudoCompManager(MapperTreeModel treeModel) {
        VariableTreeModel varTreeModel = MapperTreeModel.Utils.
                findExtensionModel(treeModel, VariableTreeModel.class);
        if (varTreeModel != null) {
            PseudoCompManager pseudoCompManager = varTreeModel.getPseudoCompManager();
            return pseudoCompManager;
        }
        //
        return null;
    }
    
    // The cache of Pseudo components.
    private LinkedList<CachedPseudoComp> mCachedPseudoCompList;
    
    private boolean mInLeftMapperTree;
    private Object mSynchSource;
    
    public PseudoCompManager(Object synchSource) {
        this(true, synchSource);
    }
    
    public PseudoCompManager(boolean inLeftMapperTree, Object synchSource) {
        mInLeftMapperTree = inLeftMapperTree;
        mSynchSource = synchSource;
        mCachedPseudoCompList = new LinkedList<CachedPseudoComp>();
    }
    
    //-------------------------------------------------------
    
    public List<XPathPseudoComp> getPseudoComp(
            Iterable<Object> paretnPathItrb) {
        //
        ArrayList<XPathPseudoComp> result = new ArrayList<XPathPseudoComp>();
        //
        for (CachedPseudoComp cPseudoComp : mCachedPseudoCompList) {
            if (cPseudoComp.hasSameLocation(paretnPathItrb, false)) {
                result.add(cPseudoComp.getPseudoComp());
            }
        }
        //
        return result;
    }
    
    public XPathPseudoComp getPseudoComp(Iterable<Object> pathItrb, 
            boolean skipPathFirst, String soughtName, String soughtNamespace, 
            boolean isAttribute) {
        //
        for (CachedPseudoComp cPseudoComp : mCachedPseudoCompList) {
            if (cPseudoComp.hasSameLocation(pathItrb, true)) {
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
    
    public boolean addPseudoComp(PseudoComp pseudo) {
        MapperPseudoComp pseudoComp = MapperPseudoComp.convert(pseudo);
        if (pseudoComp != null) {
            List<Object> parentPath = 
                    PathConverter.constructObjectLocationtList(
                    pseudoComp.getParentPathExpression());
            //
            if (parentPath != null) {
                return addPseudoCompImpl(parentPath, pseudoComp);
            }
        }
        //
        return false;
    }
    
    private boolean addPseudoCompImpl(List<Object> parentCompPath, XPathPseudoComp pseudo) {
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

    public boolean addPseudoComp(Iterable<Object> anyLocationItrb, 
            SyntheticPseudoComp pseudo) throws Exception {
        //
        List<Object> parentCompPath = 
                PathConverter.constructObjectLocationtList(anyLocationItrb, true, true);
        //
        if (parentCompPath != null) {
            if (addPseudoCompImpl(parentCompPath, pseudo)) {
                registerVariablePseudoComp(pseudo);
                return true;
            }
        }
        //
        return false;
    }
    
    private PseudoComps getPseudoCompsEntity(BPELElementsBuilder builder, 
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
        PseudoComps pseudoComps = editor.getPseudoComps();
        if (pseudoComps == null) {
            if (!create) {
                return null;
            }
            PseudoComps newPseudoComps = builder.createExtensionEntity(PseudoComps.class);
            pseudoComps = newPseudoComps;
            editor.setPseudoComps(pseudoComps);
        }
        //
        return pseudoComps;
    }
    
    public void registerPseudoComp(ExtensibleElements destination, 
            XPathPseudoComp newPseudoComp) throws ExtRegistrationException {
        //
        BpelModel bpelModel = destination.getBpelModel();
        BPELElementsBuilder builder = bpelModel.getBuilder();
        //
        PseudoComps pseudoComps = getPseudoCompsEntity(builder, destination, true);
        //
        PseudoComp[] pseudoCompArr = pseudoComps.getPseudoComps();
        //
        boolean isEqualFound = false;
        for (PseudoComp pseudo : pseudoCompArr) {
            //
            if (pseudo.getSource() == Source.TO && mInLeftMapperTree || 
                    pseudo.getSource() != Source.TO && !mInLeftMapperTree) {
                // Skip PseudoComps with oposit source
                continue;
            } 
            //
            MapperPseudoComp varPseudoComp = MapperPseudoComp.convert(pseudo);
            if (varPseudoComp != null) {
                if (varPseudoComp.equals(newPseudoComp)) {
                    isEqualFound = true;
                    break;
                }
            }
        }
        //
        if (!isEqualFound) {
            PseudoComp newPseudo = builder.createExtensionEntity(PseudoComp.class);
            pseudoComps.addPseudoComp(newPseudo);
            try {
                AbstractPseudoComp.populatePseudoComp(
                        newPseudoComp, newPseudo, pseudoComps, mInLeftMapperTree);
            } catch (ExtRegistrationException ex) {
                // 
                // Delete the last PseudoComponent
                PseudoComp[] allPCArr = pseudoComps.getPseudoComps();
                if (allPCArr.length != 0) {
                    pseudoComps.removePseudoComp(allPCArr.length - 1);
                }
                // rethrow the exception
                throw ex;
            }
        }
    }
    
    public void registerPseudoComp(ExtensibleElements destination, 
            Collection<XPathPseudoComp> newPseudoComps) {
        //
        BpelModel bpelModel = destination.getBpelModel();
        BPELElementsBuilder builder = bpelModel.getBuilder();
        //
        PseudoComps pseudoComps = getPseudoCompsEntity(builder, destination, true);
        //
        PseudoComp[] pseudoCompArr = pseudoComps.getPseudoComps();
        //
        for (XPathPseudoComp pseudoComp : newPseudoComps) {
            boolean isEqualFound = false;
            for (PseudoComp pseudo : pseudoCompArr) {
                //
                if (pseudo.getSource() == Source.TO && mInLeftMapperTree || 
                        pseudo.getSource() != Source.TO && !mInLeftMapperTree) {
                    // Skip casts with oposit source
                    continue;
                } 
                //
                MapperPseudoComp varPseudoComp = MapperPseudoComp.convert(pseudo);
                if (varPseudoComp != null) {
                    if (varPseudoComp.equals(pseudoComp)) {
                        isEqualFound = true;
                        break;
                    }
                }
            }
            //
            if (!isEqualFound) {
                PseudoComp newPseudoComp = builder.createExtensionEntity(PseudoComp.class);
                pseudoComps.addPseudoComp(newPseudoComp);
                try {
                    AbstractPseudoComp.populatePseudoComp(
                        pseudoComp, newPseudoComp, pseudoComps, mInLeftMapperTree);
                } catch (ExtRegistrationException ex) {
                    // 
                    // Delete the last PseudoComponent
                    PseudoComp[] allPCArr = pseudoComps.getPseudoComps();
                    if (allPCArr.length != 0) {
                        pseudoComps.removePseudoComp(allPCArr.length - 1);
                    }
                    
                    ex.notifyAbout();
                }
            }
        }
    }
    
    public void deletePseudoComp(Iterable<Object> locationItrb, 
            final XPathPseudoComp pseudoToUnreg) {
        //
        final VariableDeclaration var = BpelMapperUtils.getBaseVariable(locationItrb);
        if (var != null) {
            //
            // TODO: check if the deleted cast is used somewhere and ask 
            // user's confirmation if so. 
            //
            try {
                BpelModel bpelModel = ((BpelEntity)var).getBpelModel();
                bpelModel.invoke(new Callable<Object>() {
                    public Object call() throws Exception {
                        unregisterPseudoComp((ExtensibleElements)var, pseudoToUnreg);
                        clearEmptyEditorEntity((ExtensibleElements)var);
                        removePseudoComp(pseudoToUnreg);
                        return null;
                    }
                }, mSynchSource);
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }
    
    /**
     * Deletes the PseudoComp eitity from the BPEL sources at the specified 
     * destination.
     * 
     * @param destination
     * @param pseudoToUnreg
     */
    public void unregisterPseudoComp(ExtensibleElements destination, 
            XPathPseudoComp pseudoToUnreg) {
        //
        BpelModel bpelModel = destination.getBpelModel();
        BPELElementsBuilder builder = bpelModel.getBuilder();
        //
        PseudoComps pseudoComps = getPseudoCompsEntity(builder, destination, false);
        //
        if (pseudoComps == null) {
            return; // Nothing to unregister
        }
        //
        PseudoComp[] pseudoCompArr = pseudoComps.getPseudoComps();
        for (int index = 0; index < pseudoCompArr.length; index++) {
            PseudoComp pseudo = pseudoCompArr[index];
            //
            if (mInLeftMapperTree && pseudo.getSource() == Source.TO ||
                    !mInLeftMapperTree && pseudo.getSource() != Source.TO) {
                // Skip PseudoComps with oposite source
                continue;
            }
            //
            MapperPseudoComp mPseudo = MapperPseudoComp.convert(pseudo);
            if (mPseudo != null && pseudoToUnreg.equals(mPseudo)) {
                // The required PseudoComp found!
                pseudoComps.removePseudoComp(index);
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
            if (child instanceof PseudoComp) {
                // it's strange, but the PseudoComp extends the BpelContainer
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

    
    public void registerVariablePseudoComp(final SyntheticPseudoComp pseudo) 
            throws Exception {
        final VariableDeclaration var = pseudo.getBaseVariable();
        assert var instanceof ExtensibleElements;
        if (var != null) {
            //
            BpelModel bpelModel = ((BpelEntity)var).getBpelModel();
            bpelModel.invoke(new Callable<Object>() {
                public Object call() throws Exception {
                    try {
                        registerPseudoComp((ExtensibleElements)var, pseudo);
                    } finally {
                        clearEmptyEditorEntity((ExtensibleElements)var);
                    }
                    return null;
                }
            }, mSynchSource);
        }
    }
    
    @Override
    public String toString() {
        return " inLeftTree:" + mInLeftMapperTree + 
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
        private List<Object> mParentPath;

        // The type cast.
        private XPathPseudoComp mPseudoComp;
        
        public CachedPseudoComp(List<Object> parentPath, XPathPseudoComp pseudo) {
            mParentPath = parentPath;
            mPseudoComp = pseudo;
        }
        
        public XPathPseudoComp getPseudoComp() {
            return mPseudoComp;
        }
        
        /**
         * Returns the list of data objects which point to the parent 
         * of the pseudo component.
         */
        public List<Object> getParentPath() {
            return mParentPath;
        }
        
        public boolean hasSamePseudoComp(XPathPseudoComp pseudo) {
            return AbstractPseudoComp.equalTypeName(getPseudoComp(), pseudo);
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
                Object externalPathStep = externalItr.next();
                Object internalPathStep = internalItr.next();
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
            if (!(obj2 instanceof CachedPseudoComp)) {
                return false;
            }
            //
            CachedPseudoComp pseudo2 = (CachedPseudoComp)obj2;
            if (!pseudo2.getPseudoComp().equals(mPseudoComp)) {
                return false;
            }
            //
            List path2 = pseudo2.getParentPath();
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
  
