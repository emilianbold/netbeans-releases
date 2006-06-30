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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.multiview;

import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.util.NbBundle;

import java.io.Serializable;

/**
 * @author pfiala
 */
public abstract class AbstractMultiViewElement implements MultiViewElement, Serializable {
    static final long serialVersionUID = -1161218720923844459L;

    protected XmlMultiViewDataObject dObj;
    protected transient MultiViewElementCallback callback;

    protected AbstractMultiViewElement() {
    }

    protected AbstractMultiViewElement(XmlMultiViewDataObject dObj) {
        this.dObj = dObj;
    }

    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
        if (dObj!=null) {
            XmlMultiViewEditorSupport support = dObj.getEditorSupport();
            if (support!=null) {
                support.setMVTC(callback.getTopComponent());
                support.updateDisplayName();
            }
        }
    }

    public CloseOperationState canCloseElement() {
        if (dObj == null) {
            return CloseOperationState.STATE_OK;
        } else if (!dObj.canClose()) {
            return MultiViewFactory.createUnsafeCloseState(NbBundle.getMessage(AbstractMultiViewElement.class,
                    "LBL_DataObjectModified"), null, null);
        } else {
            return CloseOperationState.STATE_OK;
        }
    }

    public javax.swing.Action[] getActions() {
        return callback.createDefaultActions();
    }

    public void componentOpened() {
    }

    public void componentClosed() {
    }

    public org.openide.awt.UndoRedo getUndoRedo() {
        return dObj ==null ? null : dObj.getEditorSupport().getUndoRedo0();
    }
}
