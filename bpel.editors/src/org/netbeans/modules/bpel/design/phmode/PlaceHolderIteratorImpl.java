/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.bpel.design.phmode;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.selection.EntitySelectionModel;
import org.netbeans.modules.bpel.design.selection.PlaceHolder;
import org.netbeans.modules.bpel.design.selection.PlaceHolderManager;

/**
 *
 * @author Vitaly Bychkov
 */
public class PlaceHolderIteratorImpl implements PlaceHolderIterator {

    
    private PlaceHolderManager phManager;
    private PlaceHolderSelectionModel selectionModel;
    
    public PlaceHolderIteratorImpl(PlaceHolderManager phManager,
            PlaceHolderSelectionModel selectionModel) {
        assert phManager != null && selectionModel != null;
        
        this.phManager = phManager;
        this.selectionModel = selectionModel;
    }
    
    public PlaceHolder next() {
        PlaceHolder nextPh = getNextPlaceHolder(selectionModel.getSelectedPlaceHolder());

        return nextPh;
    }
    
    public PlaceHolder previous() {
        PlaceHolder ph = selectionModel.getSelectedPlaceHolder();
        
        return getPreviousPlaceHolder(ph);
    }

    // TODO m
    private PlaceHolder getNextPlaceHolder(PlaceHolder ph) {
        PlaceHolder nextPh = null;
        List<PlaceHolder> phs = phManager.getPlaceHolders();
        if (phs != null && phs.size() > 0) {
            if (ph == null) {
                nextPh = phs.get(0);
            } else {
                int phIndex = phs.indexOf(ph);
                int nextPhIndex = phIndex < phs.size() -1 ? phIndex+1 : 0;
                nextPh = phs.get(nextPhIndex);
            }
        }
        return nextPh;
    }

    // TODO m
    private PlaceHolder getPreviousPlaceHolder(PlaceHolder ph) {
        PlaceHolder prevPh = null;
        List<PlaceHolder> phs = phManager.getPlaceHolders();
        if (phs != null && phs.size() > 0) {
            if (ph == null) {
                prevPh = phs.get(phs.size()-1);
            } else {
                int phIndex = phs.indexOf(ph);
                int prevPhIndex = phIndex >0 ? phIndex-1 : phs.size()-1;
                prevPh = phs.get(prevPhIndex);
            }
        }
        return prevPh;
    }
    
    

}
