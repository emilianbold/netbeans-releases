/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence.editor.cache;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.text.Document;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.text.CloneableEditor;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 */
public class CacheConfigSourceEditor extends CloneableEditor implements MultiViewElement, Runnable {

    private JComponent toolbar;
    private MultiViewElementCallback callback;

    public CacheConfigSourceEditor(CacheConfigEditorSupport support) {
        super(support);
    }

    public CacheConfigSourceEditor() {
    }

    public JComponent getVisualRepresentation() {
        return this;
    }

    public JComponent getToolbarRepresentation() {
        if (toolbar == null) {
            JEditorPane jEditorPane = this.pane;
            if (jEditorPane != null) {
                Document doc = jEditorPane.getDocument();
                if (doc instanceof NbDocument.CustomToolbar) {
                    toolbar = ((NbDocument.CustomToolbar) doc).createToolbar(jEditorPane);
                }
            }
            if (toolbar == null) {
                toolbar = new JPanel();
            }
        }
        return toolbar;
    }

    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
        updateName();
    }

    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
    }

    @Override
    public void componentActivated() {
        super.componentActivated();
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
    }

    @Override
    public void componentClosed() {
        super.componentClosed();
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
    }

    public void run() {
        MultiViewElementCallback c = callback;
        if (c == null) {
            return;
        }
        TopComponent tc = c.getTopComponent();
        if (tc == null) {
            return;
        }
        super.updateName();
        tc.setName(this.getName());
        tc.setDisplayName(this.getDisplayName());
        tc.setHtmlDisplayName(this.getHtmlDisplayName());
    }

    @Override
    public Lookup getLookup() {
        return ((CacheConfigDataObject) ((CacheConfigEditorSupport) cloneableEditorSupport()).getDataObject()).getNodeDelegate().getLookup();
    }
}
