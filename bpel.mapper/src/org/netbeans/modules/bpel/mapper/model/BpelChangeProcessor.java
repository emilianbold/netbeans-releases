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
package org.netbeans.modules.bpel.mapper.model;

import java.util.Collection;
import java.util.concurrent.Callable;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.openide.ErrorManager;

/**
 * Controlls a BPEL model's updating process after a change at the BPEL mapper. 
 * 
 * @author nk160297
 */
public class BpelChangeProcessor implements GraphChangeProcessor {

    private BpelModelUpdater mBpelModelUpdater;
    
    public BpelChangeProcessor(BpelModelUpdater bpelModelUpdater) {
        assert bpelModelUpdater != null;
        mBpelModelUpdater = bpelModelUpdater;
    }
    
    public void processChanges(final TreePath graphTreePath) {
        try {
            BpelModel bpelModel = getBpelModel();
            if (bpelModel == null) {
                return;
            }
            bpelModel.invoke(new Callable<Object>() {
                public Object call() throws Exception {
                    mBpelModelUpdater.updateOnChanges(graphTreePath);
                    return null;
                }
            }, this);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    public void processChanges(final Collection<TreePath> graphTreePathList) {
        try {
            BpelModel bpelModel = getBpelModel();
            if (bpelModel == null) {
                return;
            }
            bpelModel.invoke(new Callable<Object>() {
                public Object call() throws Exception {
                    for (TreePath graphTreePath : graphTreePathList) {
                        mBpelModelUpdater.updateOnChanges(graphTreePath);
                    }
                    return null;
                }
            }, this);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    private BpelModel getBpelModel() {
        return mBpelModelUpdater.getDesignContext().getBpelModel();
    }
}
