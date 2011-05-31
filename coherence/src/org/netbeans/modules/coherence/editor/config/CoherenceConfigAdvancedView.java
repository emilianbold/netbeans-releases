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
package org.netbeans.modules.coherence.editor.config;

import org.netbeans.modules.coherence.xml.coherence.Coherence;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.jdesktop.beansbinding.BindingGroup;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.awt.UndoRedo;
import org.openide.explorer.ExplorerManager;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author Andrew Hopkinson (Oracle A-Team)
 */
public class CoherenceConfigAdvancedView extends JPanel implements MultiViewDescription, MultiViewElement,
        ExplorerManager.Provider, DocumentListener, PropertyChangeListener, TableModelListener, ListSelectionListener {

    /** Creates new form CoherenceConfigAdvancedView */
    public CoherenceConfigAdvancedView() {
        initComponents();
    }

    /*
     * =========================================================================
     * START: Custom Code
     * =========================================================================
     */
    public CoherenceConfigAdvancedView(CoherenceConfigEditorSupport support) {
        this();
        this.support = support;
    }
    /*
     * Inner Class
     */
    /*
     * Properties
     */
    private static final Logger logger = Logger.getLogger(CoherenceConfigGeneralView.class.getCanonicalName());
    private CoherenceConfigEditorSupport support = null;
    private ExplorerManager em = null;
    private BindingGroup bindingGroup;
    /*
     * Methods
     */
    private void initialise() {

    }
    private void refresh() {
        refresh(getCoherence());
    }
    private void refresh(Coherence coherence) {

    }
    private void serialize() {

    }
    private Coherence getCoherence() {
        return ((CoherenceConfigDataObject)support.getDataObject()).getCoherence();
    }
    /*
     * Overrides
     */
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }

    @Override
    public String getDisplayName() {
        return "Advanced";
    }

    @Override
    public Image getIcon() {
        return ImageUtilities.loadImage(org.openide.util.NbBundle.getMessage(CoherenceConfigGeneralView.class, "CoherenceConfig.file.icon"));
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public String preferredID() {
        return this.getClass().getSimpleName();
    }

    @Override
    public MultiViewElement createElement() {
        em = new ExplorerManager();
        em.addPropertyChangeListener(this);
        try {
            refresh();
            support.openDocument().addDocumentListener(this);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "*** APH-I3 : Failed to Create Element ", ex);
        }
        return this;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        refresh();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        refresh();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        refresh();
    }

    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return new JToolBar();
    }

    @Override
    public Action[] getActions() {
        return support.getDataObject().getNodeDelegate().getActions(false);
    }

    @Override
    public Lookup getLookup() {
        return ((CoherenceConfigDataObject) support.getDataObject()).getNodeDelegate().getLookup();
    }

    @Override
    public UndoRedo getUndoRedo() {
        return null;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback mvec) {
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
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
    public void propertyChange(PropertyChangeEvent evt) {
        logger.log(Level.INFO, "*** APH-I1 : propertyChanged ".concat(evt.getPropertyName()));
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        logger.log(Level.INFO, "*** APH-I1 : tableChanged ".concat(e.getColumn() + ""));
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
    }
    /*
     * =========================================================================
     * End: Custom Code
     * =========================================================================
     */

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents



    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
