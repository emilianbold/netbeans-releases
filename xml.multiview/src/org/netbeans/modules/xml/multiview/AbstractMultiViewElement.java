/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.xml.multiview;

import java.util.Arrays;
import javax.swing.Action;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.util.NbBundle;

import java.io.Serializable;
import org.openide.actions.FileSystemAction;
import org.openide.util.actions.SystemAction;

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
        Action[] actions = callback.createDefaultActions();
        SystemAction fsAction = SystemAction.get(FileSystemAction.class);
        if (!Arrays.asList(actions).contains(fsAction)) {
            Action[] newActions = new Action[actions.length+1];
            System.arraycopy(actions, 0, newActions, 0, actions.length);
            newActions[actions.length] = fsAction;
            actions = newActions;
        }
        return actions;
    }

    public void componentOpened() {
    }

    public void componentClosed() {
    }

    public org.openide.awt.UndoRedo getUndoRedo() {
        return dObj ==null ? null : dObj.getEditorSupport().getUndoRedo0();
    }
}
