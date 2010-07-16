/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.mapper.lsm;

import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.worklist.editor.mapper.MapperTcContext;
import org.netbeans.modules.worklist.editor.mapper.WlmDesignContext;
import org.netbeans.modules.worklist.editor.mapper.model.WlmMapperModel;

/**
 *
 * TODO: It's a stub processor
 *
 * Process special BPEL extensions like <editor>, <cast>, <pseudoComp>
 *
 * TODO: Generalize the algorithm and move it to BPEL Model in order
 * the model and mapper can share a common algorithm!!!
 * TODO: It's necessary to introduce an XPathPredicate as a LocationStepModifier
 * at the level of XPath model to unify the algorithm.
 *
 * @author nk160297
 */
public class MapperLsmProcessor {
    
    private WlmMapperModel mModel;
    private WlmDesignContext mDContext;
    
    public MapperLsmProcessor(MapperTcContext mapperTcContext) {
        mModel = (WlmMapperModel)mapperTcContext.getMapper().getModel();
        mDContext = mapperTcContext.getDesignContextController().getContext();
        //
        assert mModel != null;
        assert mDContext != null;
    }

    public MapperLsmProcessor(WlmMapperModel mm, WlmDesignContext context) {
        mModel = mm;
        mDContext = context;
        //
        assert mModel != null;
        assert mDContext != null;
    }

    /**
     * Registers all LocationStepModifier extensions declared in variables. 
     */
    public void processVariables() {
    }
    
    // This method has the similar code in comparison with 
    // constructor of the XPathCastResolverImpl class
    public MapperLsmContainer collectsLsm(MapperLsmContainer extContainer,
            WLMComponent entity, WLMComponent varContextEntity,
            boolean registerFrom, boolean registerTo) {
        //
        return null;
    }

    //------------------------------------------------------------

    /**
     * Register all LSMs in corresponding managers.
     * @param collector
     *
     * TODO: rename to be more informative. For example,
     */
    public void registerAll(MapperLsmContainer collector) {
    }

    public void initManagers(boolean forLeftTree) {
    }

    //------------------------------------------------------------

    /**
     * It is an auxiliary container for building complex XPathCastResolver.
     * It collects all LocationStepModifiers in a form which acceptable
     * by XPath model.
     */
    public static class MapperLsmContainer {

        public MapperLsmContainer() {
        }

    }

    //------------------------------------------------------------

}
