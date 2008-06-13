/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bpel.mapper.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.bpel.mapper.predicates.PredicateManager;
import org.netbeans.modules.bpel.mapper.cast.CastManager;
import org.netbeans.modules.bpel.mapper.cast.PseudoCompManager;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTreeModel;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.ext.editor.api.Cast;
import org.netbeans.modules.bpel.model.ext.editor.api.Casts;
import org.netbeans.modules.bpel.model.ext.editor.api.Editor;
import org.netbeans.modules.bpel.model.ext.editor.api.PseudoComp;
import org.netbeans.modules.bpel.model.ext.editor.api.PseudoComps;
import org.netbeans.modules.bpel.model.ext.editor.api.Source;

/**
 * Process special BPEL extensions like <editor>, <cast>, <pseudoComp>
 * 
 * @author nk160297
 */
public class EditorExtensionProcessor {
    
    private BpelMapperModel mModel;
    private BpelDesignContext mDContext;
    
    private CastManager mLeftCastManager;
    private PseudoCompManager mLeftPseudoCompManager;
    private PredicateManager mLeftPredicateManager;
    private CastManager mRightCastManager;
    private PseudoCompManager mRightPseudoCompManager;
    private PredicateManager mRightPredicateManager;
    
    public EditorExtensionProcessor(BpelMapperModel mm, BpelDesignContext context) {
        mModel = mm;
        mDContext = context;
    }

    public void processVariables() {
        //
        Set<VariableDeclaration> visVars = mDContext.getVisibilityScope().
                getVisibleVariables().getAllVisibleVariables();
        //
        for (VariableDeclaration varDecl : visVars) {
            BpelEditorExtensions exts = getExtList(varDecl);
            //
            registerCasts(exts.getFromCasts());
            registerCasts(exts.getToCasts());
            //
            registerPseudoComps(exts.getFromPseudoComps());
            registerPseudoComps(exts.getToPseudoComps());
        }
    }
    
    public void registerCasts(BpelEditorExtensions extList) {
        registerCasts(extList.getFromCasts());
        registerCasts(extList.getToCasts());
    }
    
    public boolean registerCasts(List<Cast> castList) {
        boolean anyRegistered = false;
        //
        for (Cast cast : castList) {
            Source source = cast.getSource();
            boolean useLeftTree = (source != Source.TO);
            CastManager castManager = getCastManager(useLeftTree);
            if (castManager != null) {
                if(castManager.addTypeCast(cast)) {
                    anyRegistered = true;
                }
            }
        }
        //
        return anyRegistered;
    }
    
    public void registerPseudoComps(BpelEditorExtensions extList) {
        registerPseudoComps(extList.getFromPseudoComps());
        registerPseudoComps(extList.getToPseudoComps());
    }
    
    public boolean registerPseudoComps(List<PseudoComp> pseudoCompList) {
        boolean anyRegistered = false;
        //
        for (PseudoComp pseudo : pseudoCompList) {
            Source source = pseudo.getSource();
            boolean useLeftTree = (source != Source.TO);
            PseudoCompManager pseudoManager = getPseudoCompManager(useLeftTree);
            if (pseudoManager != null) {
                if(pseudoManager.addPseudoComp(pseudo)) {
                    anyRegistered = true;
                }
            }
        }
        //
        return anyRegistered;
    }
    
    /**
     * Collects all BPEL editor extensions from the specified entity and 
     * returns them as one container object BpelEditorExtensions.
     * @param bpelEntity
     * @return
     */
    public BpelEditorExtensions getExtList(BpelEntity bpelEntity) {
        BpelEditorExtensions result = new BpelEditorExtensions();
        List<Editor> editorList = bpelEntity.getChildren(Editor.class);
        for (Editor editorExt : editorList) {
            List<Cast> castList = getCastList(editorExt);
            if (castList != null) {
                for (Cast cast : castList) {
                    if (cast.getSource() == Source.TO) {
                        result.getToCasts().add(cast);
                    } else {
                        result.getFromCasts().add(cast);
                    }
                }
            }
            List<PseudoComp> pseudoCompList = getPseudoCompList(editorExt);
            if (pseudoCompList != null) {
                for (PseudoComp pseudoComp : pseudoCompList) {
                    if (pseudoComp.getSource() == Source.TO) {
                        result.getToPseudoComps().add(pseudoComp);
                    } else {
                        result.getFromPseudoComps().add(pseudoComp);
                    }
                }
            }
        }
        return result;
    }
    
    public List<Cast> getCastList(Editor editorExt) {
        Casts casts = editorExt.getCasts();
        if (casts != null) {
            Cast[] castArr = casts.getCasts();
            if (castArr.length > 0) {
                return Arrays.asList(castArr);
            }
        }
        //
        return null;
    }
    
    public List<PseudoComp> getPseudoCompList(Editor editorExt) {
        PseudoComps pseudoComps = editorExt.getPseudoComps();
        if (pseudoComps != null) {
            PseudoComp[] pseudoCompArr = pseudoComps.getPseudoComps();
            if (pseudoCompArr.length > 0) {
                return Arrays.asList(pseudoCompArr);
            }
        }
        //
        return null;
    }
    
    public CastManager getCastManager(boolean forLeftTree) {
        if (forLeftTree) {
            if (mLeftCastManager == null) {
                initManagers(forLeftTree);
            }
            return mLeftCastManager;
        } else {
            if (mRightCastManager == null) {
                initManagers(forLeftTree);
            }
            return mRightCastManager;
        }
    }
    
    public PseudoCompManager getPseudoCompManager(boolean forLeftTree) {
        if (forLeftTree) {
            if (mLeftPseudoCompManager == null) {
                initManagers(forLeftTree);
            }
            return mLeftPseudoCompManager;
        } else {
            if (mRightPseudoCompManager == null) {
                initManagers(forLeftTree);
            }
            return mRightPseudoCompManager;
        }
    }
    
    public PredicateManager getPredicateManager(boolean forLeftTree) {
        if (forLeftTree) {
            if (mLeftPredicateManager == null) {
                initManagers(forLeftTree);
            }
            return mLeftPredicateManager;
        } else {
            if (mRightPredicateManager == null) {
                initManagers(forLeftTree);
            }
            return mRightPredicateManager;
        }
    }
    
    public void initManagers(boolean forLeftTree) {
        MapperSwingTreeModel treeModel = null;
        if (forLeftTree) {
            treeModel = mModel.getLeftTreeModel();
        } else {
            treeModel = mModel.getRightTreeModel();
        }
        //
        MapperTreeModel sourceModel = treeModel.getSourceModel();
        //
        if (forLeftTree) {
            mLeftCastManager = CastManager.getCastManager(sourceModel);
            mLeftPseudoCompManager = PseudoCompManager.getPseudoCompManager(sourceModel);
            mLeftPredicateManager = PredicateManager.getPredicateManager(sourceModel);
        } else {
            mRightCastManager = CastManager.getCastManager(sourceModel);
            mRightPseudoCompManager = PseudoCompManager.getPseudoCompManager(sourceModel);
            mRightPredicateManager = PredicateManager.getPredicateManager(sourceModel);
        }
    }
    
    /**
     * The temporary object for loading all BPEL Editor extensions.
     */
    public class BpelEditorExtensions {
        
        private ArrayList<Cast> mFromCasts;
        private ArrayList<Cast> mToCasts;
        private ArrayList<PseudoComp> mFromPseudoComps;
        private ArrayList<PseudoComp> mToPseudoComps;
        
        public BpelEditorExtensions() {
            mFromCasts = new ArrayList<Cast>();
            mToCasts = new ArrayList<Cast>();
            mFromPseudoComps = new ArrayList<PseudoComp>();
            mToPseudoComps = new ArrayList<PseudoComp>();
        }
        
        public List<Cast> getFromCasts() {
            return mFromCasts;
        }
        
        public List<Cast> getToCasts() {
            return mToCasts;
        }
        
        public List<PseudoComp> getFromPseudoComps() {
            return mFromPseudoComps;
        }
        
        public List<PseudoComp> getToPseudoComps() {
            return mToPseudoComps;
        }
        
        @Override
        public String toString() {
            return "FromCast: " + mFromCasts + 
                    " FromPseudoComp: " + mFromPseudoComps + 
                    " ToCast: " + mToCasts + 
                    " ToPseudoComp: " + mToPseudoComps;
        }
        
    }
}
