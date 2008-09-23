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
package org.netbeans.modules.visualweb.insync;

import com.sun.rave.designtime.markup.MarkupDesignBean;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.Customizer2;
import javax.swing.JDialog;
import org.netbeans.modules.visualweb.insync.models.FacesModel;

/**
 * Class responsible for handling live customizers. It will display a live customizer, and also
 * suppress document updates on the bean's element (and its descendants) while the dialog is
 * visible.
* @todo Transfer the plugin thing to the DesignerService?
 *
 * @author Tor Norbye
 */
public class CustomizerDisplayer
    implements ActionListener, PropertyChangeListener {

    /**
     * Interface by listeners to batch editing operations of a DesignBean. The intended use for this
     * is for example the designer listening to a DataTable getting customized; when we apply the
     * changes in the data table, we want only a single designer update of the table, not continuous
     * incremental updates as the data table customizer walks over the table structure and adds and
     * deletes rows.
     */
    public interface BatchListener {
        /** The given DesignBean is about to be batch modified */
        public void beanModifyBegin(MarkupDesignBean bean, FacesModel model);
        /** The given DesignBean is done getting modified */
        public void beanModifyEnd(MarkupDesignBean bean, FacesModel model);
    }

    private DesignBean bean;
    private Customizer2 customizer;
    private String help;
    private DialogDescriptor descriptor;
    private JButton applyButton;
    private JButton closeButton;
    private FacesModel model;

    public CustomizerDisplayer(DesignBean bean, Customizer2 customizer, String help,
                                   FacesModel model) {
        this.bean = bean;
        this.customizer = customizer;
        this.help = help;
        this.model = model;
    }

    /**
     * Show the given customizer
     *
     * @param bean The bean to be customized
     * @param customizer The customizer to customize it with
     * @param help A help topic id, if any
     */
    public void show() {
        if (customizer == null) {
            return;
        }
        Component panel = customizer.getCustomizerPanel(bean);
        if (panel == null) {
            return;
        }

        HelpCtx helpCtx = null;
        if ((help != null) && (help.length() > 0)) {
            helpCtx = new HelpCtx(help);
        }

        Object[] options = null;
        Object[] closingOptions = null;
        if (customizer.isApplyCapable()) {
            applyButton = new JButton(NbBundle.getMessage(CustomizerDisplayer.class, "Apply")); // NOI18N
            applyButton.setMnemonic(NbBundle.getMessage(CustomizerDisplayer.class, "Apply_mnemonic").charAt(0));
            applyButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerDisplayer.class, "APPLY_ACCESS_NAME"));
            applyButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerDisplayer.class, "APPLY_ACCESS_DESC"));
            boolean enabled = customizer.isModified(); // in case changes aren't cached
            applyButton.setEnabled(enabled);

            options = new Object[] {
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.CANCEL_OPTION,
                applyButton,
            };
            closingOptions = new Object[] {
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.CANCEL_OPTION };
        } else {
            closeButton = new JButton(NbBundle.getMessage(CustomizerDisplayer.class, "Close")); // NOI18N
            closeButton.setMnemonic(NbBundle.getMessage(CustomizerDisplayer.class, "Close_mnemonic").charAt(0));
            closeButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerDisplayer.class, "CLOSE_ACCESS_NAME"));
            closeButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerDisplayer.class, "CLOSE_ACCESS_DESC"));
            options = new Object[] {
                closeButton
            };
            closingOptions = options;
        }

        descriptor = new DialogDescriptor(
            panel,
            customizer.getDisplayName(), // title
            true, // modal
            options,
            DialogDescriptor.OK_OPTION, // default
            DialogDescriptor.DEFAULT_ALIGN,
            helpCtx,
            this
        );
        descriptor.setClosingOptions(closingOptions);
        customizer.addPropertyChangeListener(this);        
        JDialog dialog = (JDialog) DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleDescription(customizer.getDisplayName());        
        dialog.show();
    }

    public final void propertyChange(PropertyChangeEvent evt) {
        // Update valid state for each of the buttons
        boolean enabled = customizer.isModified();
        if (applyButton != null) {
            applyButton.setEnabled(enabled);
        }
    }

    /** Invoked when the user presses a button, or presses Esc, etc. */
    public void actionPerformed(ActionEvent evt) {
        if (descriptor == null) {
            return; // impossible?
        }
        Object chosen = descriptor.getValue();
        if (chosen == DialogDescriptor.OK_OPTION ||
            chosen == closeButton ||
            chosen == applyButton) {
            try {
                startTask();
                Result r = customizer.applyChanges();
                ResultHandler.handleResult(r, model);
            } finally {
                finishTask();
            }
            if (applyButton != null) {
                boolean enabled = customizer.isModified(); // in case changes aren't cached
                applyButton.setEnabled(enabled);
            }
        } else {
            // Could be cancel, could be "escape", etc.
        }
        if (chosen == DialogDescriptor.OK_OPTION ||
            chosen == closeButton ||
            chosen == DialogDescriptor.CANCEL_OPTION) {
            // Dialog is not coming back:
            customizer.removePropertyChangeListener(this);
        }
    }

    private UndoEvent undoEvent = null;

    private void startTask() {
        undoEvent = model.writeLock(NbBundle.getMessage(CustomizerDisplayer.class, "Customize")); // NOI18N
        if (bean instanceof MarkupDesignBean) {
            batchModifyBean((MarkupDesignBean)bean, true);
        }
    }

    private void finishTask() {
        if (undoEvent != null) {
            model.writeUnlock(undoEvent);
            undoEvent = null;
        }
        if (bean instanceof MarkupDesignBean) {
            batchModifyBean((MarkupDesignBean)bean, false);
        }
    }

    /**
     * Notify clients that the given bean is about to be "batch modified". This allows for example
     * the designer to filter editing events in a more clever way.
     *
     * @param bean
     * @param b
     * @todo Generated comment
     */
    private void batchModifyBean(MarkupDesignBean bean, boolean b) {
        if (batchListener != null) {
            if (b) {
                batchListener.beanModifyBegin(bean, model);
            } else {
                batchListener.beanModifyEnd(bean, model);
            }
        }
    }

    /**
     * Register a listener to be notified when a DesignBean is about to be batch modified.
     *
     * @todo Switch over the a listener list, with add and remove listener methods, etc.
     */
    public static void setBatchListener(BatchListener batchListener) {
        if (CustomizerDisplayer.batchListener != null) {
            throw new UnsupportedOperationException("There can only be one batch listener"); // NOI18N - internal error only
        }
        CustomizerDisplayer.batchListener = batchListener;
    }

    private static BatchListener batchListener;

}
