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

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.bpel.mapper.predicates.PredicateManager;
import org.netbeans.modules.bpel.mapper.cast.CastManager;
import org.netbeans.modules.bpel.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.bpel.mapper.tree.search.AllVariablesFinder;
import org.netbeans.modules.bpel.mapper.tree.search.TreeFinderProcessor;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTreeModel;
import org.netbeans.modules.bpel.mapper.tree.spi.TreeItemFinder;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Variable;
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
    
    private CastManager mLeftCastManager;
    private PredicateManager mLeftPredicateManager;
    private CastManager mRightCastManager;
    private PredicateManager mRightPredicateManager;
    
    public EditorExtensionProcessor(BpelMapperModel mm) {
        mModel = mm;
    }

    public void processVariables() {
        //
        // Only left tree model is used because the left and the right trees 
        // have the same variables. Variables are the same because of the 
        // same visibility context.
        // 
        TreeFinderProcessor findProc = 
                new TreeFinderProcessor(mModel.getLeftTreeModel());
        AllVariablesFinder finder = new AllVariablesFinder();
        List<TreeItemFinder> finderList = 
                Collections.singletonList((TreeItemFinder)finder);
        List<Object> foundObjList = findProc.findAllDataObjects(finderList);
        //
        for (Object obj : foundObjList) {
            if (obj instanceof Variable) {
                processChildrenEditorExt((Variable)obj);
            }
        }
    }
    
    public void processChildrenEditorExt(BpelEntity bpelEntity) {
        List<Editor> editorList = bpelEntity.getChildren(Editor.class);
        for (Editor editorExt : editorList) {
            process(editorExt);
        }
    }
    
    public void process(Editor editorExt) {
        Casts casts = editorExt.getCasts();
        if (casts != null) {
            Cast[] castArr = casts.getCasts();
            if (castArr.length > 0) {
                for (Cast cast : castArr) {
                    Source source = cast.getSource();
                    boolean useLeftTree = (source != Source.TO);
                    CastManager castManager = getCastManager(useLeftTree);
                    if (castManager != null) {
                        castManager.addTypeCast(cast);
                    }
                }
            }
        }
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
    
    
}
