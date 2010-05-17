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

package org.netbeans.core.spi.multiview;

import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.text.Document;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;

/**
 * @author  mkleint
 */
abstract class MultiViewCloneableEditor extends CloneableEditor  implements MultiViewElement {

    private static final long serialVersionUID =-3126744316644172415L;

    private transient MultiViewElementCallback multiViewObserver;
    private transient JToolBar bar;
    
    /** Creates a new instance of MultiViewClonableEditor */
    public MultiViewCloneableEditor() {
        this(null);
    }
    
    public MultiViewCloneableEditor(CloneableEditorSupport support) {
        super(support);
    }
    
    public JComponent getToolbarRepresentation() {
        Document doc = getEditorPane().getDocument();
        if (doc instanceof NbDocument.CustomToolbar) {
            if (bar == null) {
                bar = ((NbDocument.CustomToolbar)doc).createToolbar(getEditorPane());
            }
            return bar;
        }
        return null;
    }
    
    public javax.swing.JComponent getVisualRepresentation() {
        return this;
    }
    
    public final void setMultiViewCallback(MultiViewElementCallback callback) {
        multiViewObserver = callback;
    }
    
    protected final MultiViewElementCallback getElementObserver() {
        return multiViewObserver;
    }
    
    public void componentActivated() {
        super.componentActivated();
    }
    
    public void componentClosed() {
        super.componentClosed();
    }
    
    public void componentDeactivated() {
        super.componentDeactivated();
    }
    
    public void componentHidden() {
        super.componentHidden();
    }
    
    public void componentOpened() {
        super.componentOpened();
    }
    
    public void componentShowing() {
        if (multiViewObserver != null) {
            updateName();
        }
        super.componentShowing();
    }
    
    public javax.swing.Action[] getActions() {
        return super.getActions();
    }
    
    public org.openide.util.Lookup getLookup() {
        return super.getLookup();
    }
    
    public String preferredID() {
        return super.preferredID();
    }
    
    
    public void requestVisible() {
        if (multiViewObserver != null) {
            multiViewObserver.requestVisible();
        } else {
            super.requestVisible();
        }
    }
    
    public void requestActive() {
        if (multiViewObserver != null) {
            multiViewObserver.requestActive();
        } else {
            super.requestActive();
        }
    }
    
    
    public void updateName() {
        super.updateName();
        if (multiViewObserver != null) {
            multiViewObserver.updateTitle(getDisplayName());
        }
    }
    
    public void open() {
        if (multiViewObserver != null) {
            multiViewObserver.requestVisible();
        } else {
            super.open();
        }
        
    }
    
    public CloseOperationState canCloseElement() {
        throw new IllegalStateException("Not implemented yet.");
//        return CloseOperationState.STATE_OK;
    }
    
}
