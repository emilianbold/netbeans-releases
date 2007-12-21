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
