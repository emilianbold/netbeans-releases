/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.Image;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.awt.UndoRedo;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 * A tab in branding editor window.
 *
 * @author S. Aubrecht
 */
abstract class AbstractBrandingPanel extends TopComponent implements MultiViewElement, MultiViewDescription {

    private MultiViewElementCallback callback;
    private Lookup lkp;
    private final JToolBar toolbar;
    private final BasicBrandingModel model;
    private BrandingEditor editor;
    private final String displayName;
    private boolean brandingValid = true;
    private String errMessage = null;

    /**
     * C'tor
     * @param displayName Tab's display name.
     * @param model Branding model
     */
    protected AbstractBrandingPanel( String displayName, BasicBrandingModel model ) {
        this.displayName = displayName;
        this.model = model;
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setFocusable(false);
        toolbar.addSeparator();
    }

    final void init( BrandingEditor editor, Lookup lkp ) {
        this.editor = editor;
        this.lkp = lkp;
        toolbar.add(editor.getSaveAction());
        toolbar.add(editor.createErrorLabel());
    }

    @Override
    public final JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public final JComponent getToolbarRepresentation() {
        return toolbar;
    }

    @Override
    public Action[] getActions() {
        if( null != callback )
            return callback.createDefaultActions();
        return new Action[0];
    }

    @Override
    public Lookup getLookup() {
        return lkp;
    }

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
    }

    @Override
    public void componentShowing() {
    }

    @Override
    public void componentHidden() {
    }

    @Override
    public void componentActivated() {
    }

    @Override
    public void componentDeactivated() {
    }

    @Override
    public UndoRedo getUndoRedo() {
        return null;
    }

    @Override
    public final void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
    }

    @Override
    public CloseOperationState canCloseElement() {
        if( null == editor || !editor.isModified() )
            return CloseOperationState.STATE_OK;
        return MultiViewFactory.createUnsafeCloseState(null, editor.getSaveAction(), null);
    }

    protected final BasicBrandingModel getBranding() {
        return model;
    }

    public abstract void store();

    protected final void setErrorMessage( String errMessage ) {
        this.errMessage = errMessage;
        notifyEditor();
    }

    final String getErrorMessage() {
        return errMessage;
    }

    protected final void setValid( boolean valid ) {
        this.brandingValid = valid;
        notifyEditor();
    }

    final boolean isBrandingValid() {
        return brandingValid && null == errMessage;
    }

    protected final void setModified() {
        editor.setModified();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Image getIcon() {
        return null == editor ? null : editor.getIcon();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }

    @Override
    public final String preferredID() {
        return getClass().getName();
    }

    @Override
    public final MultiViewElement createElement() {
        return this;
    }

    private void notifyEditor() {
        if( null == editor )
            return;
        editor.onBrandingValidation();
    }
}
