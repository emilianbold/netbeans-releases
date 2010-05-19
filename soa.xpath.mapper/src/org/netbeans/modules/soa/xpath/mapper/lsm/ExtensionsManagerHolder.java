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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.xpath.mapper.lsm;

import org.netbeans.modules.soa.xpath.mapper.specstep.SpecialStepManager;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperSwingTreeModel;

/**
 * Holds different extension managers.
 *
 * @author Nikita Krjukov
 */
public interface ExtensionsManagerHolder {

    CastManager getCastManager();
    PseudoCompManager getPseudoCompManager();
    PredicateManager getPredicateManager();
    SpecialStepManager getSpecialStepManager();

    void setCastManager(CastManager cm);
    void setPseudoCompManager(PseudoCompManager pcm);
    void setPredicateManager(PredicateManager prm);
    void setSpecialStepManager(SpecialStepManager ssm);

    void attachToTree(MapperSwingTreeModel treeModel);

    public interface Provider {
        ExtensionsManagerHolder getExtManagerHolder();
    }

    public static class Default implements ExtensionsManagerHolder {

        private CastManager mCm;
        private PseudoCompManager mPcm;
        private PredicateManager mPrm;
        private SpecialStepManager mSsm;

        public CastManager getCastManager() {
            return mCm;
        }

        public PseudoCompManager getPseudoCompManager() {
            return mPcm;
        }

        public PredicateManager getPredicateManager() {
            return mPrm;
        }

        public SpecialStepManager getSpecialStepManager() {
            return mSsm;
        }

        public void setCastManager(CastManager cm) {
            this.mCm = cm;
        }

        public void setPseudoCompManager(PseudoCompManager pcm) {
            this.mPcm = pcm;
        }

        public void setPredicateManager(PredicateManager prm) {
            this.mPrm = prm;
        }

        public void setSpecialStepManager(SpecialStepManager ssm) {
            this.mSsm = ssm;
        }

        public void attachToTree(MapperSwingTreeModel treeModel) {
            if (mCm != null) {
                mCm.attachToTreeModel(treeModel);
            }
            if (mPcm != null) {
                mPcm.attachToTreeModel(treeModel);
            }
            if (mPrm != null) {
                mPrm.attachToTreeModel(treeModel);
            }
            if (mSsm != null) {
                mSsm.attachToTreeModel(treeModel);
            }
        }

    }

    /**
     * Immutable extension managers holder.
     */
    public static class ReadOnly implements ExtensionsManagerHolder {

        private ExtensionsManagerHolder mWrapped;

        public ReadOnly(ExtensionsManagerHolder wrapped) {
            mWrapped = wrapped;
        }

        public CastManager getCastManager() {
            return mWrapped.getCastManager();
        }

        public PseudoCompManager getPseudoCompManager() {
            return mWrapped.getPseudoCompManager();
        }

        public PredicateManager getPredicateManager() {
            return mWrapped.getPredicateManager();
        }

        public SpecialStepManager getSpecialStepManager() {
            return mWrapped.getSpecialStepManager();
        }

        public void setCastManager(CastManager cm) {
        }

        public void setPseudoCompManager(PseudoCompManager pcm) {
        }

        public void setPredicateManager(PredicateManager prm) {
        }

        public void setSpecialStepManager(SpecialStepManager ssm) {
        }

        public void attachToTree(MapperSwingTreeModel treeModel) {
        }

    }

}


