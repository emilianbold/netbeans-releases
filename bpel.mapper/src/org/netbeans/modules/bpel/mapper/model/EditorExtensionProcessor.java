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
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTreeModel;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.ext.editor.api.Cast;
import org.netbeans.modules.bpel.model.ext.editor.api.Casts;
import org.netbeans.modules.bpel.model.ext.editor.api.Editor;
import org.netbeans.modules.bpel.model.ext.editor.api.Source;

/**
 * Process special BPEL extensions like <editor>, <cast>
 * 
 * @author nk160297
 */
public class EditorExtensionProcessor {
    
    private BpelMapperModel mModel;
    private BpelDesignContext mDContext;
    
    private CastManager mLeftCastManager;
    private PredicateManager mLeftPredicateManager;
    private CastManager mRightCastManager;
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
            BpelEntityCasts castList = getCastList(varDecl);
            registerCasts(castList.getFromCasts());
            registerCasts(castList.getToCasts());
        }
    }
    
    public void registerCasts(BpelEntityCasts castList) {
        registerCasts(castList.getFromCasts());
        registerCasts(castList.getToCasts());
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
    
    public BpelEntityCasts getCastList(BpelEntity bpelEntity) {
        BpelEntityCasts result = new BpelEntityCasts();
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
            mLeftPredicateManager = PredicateManager.getPredicateManager(sourceModel);
        } else {
            mRightCastManager = CastManager.getCastManager(sourceModel);
            mRightPredicateManager = PredicateManager.getPredicateManager(sourceModel);
        }
    }
    
    public class BpelEntityCasts {
        
        private ArrayList<Cast> mFromCasts;
        private ArrayList<Cast> mToCasts;
        
        public BpelEntityCasts() {
            mFromCasts = new ArrayList<Cast>();
            mToCasts = new ArrayList<Cast>();
        }
        
        public List<Cast> getFromCasts() {
            return mFromCasts;
        }
        
        public List<Cast> getToCasts() {
            return mToCasts;
        }
        
        @Override
        public String toString() {
            return "From: " + mFromCasts + " To: " + mToCasts;
        }
        
    }
}
