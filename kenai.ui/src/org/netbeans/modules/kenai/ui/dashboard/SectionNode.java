/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.ui.dashboard;

import org.netbeans.modules.kenai.ui.treelist.*;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.modules.kenai.ui.spi.ProjectHandle;
import org.openide.awt.HtmlRenderer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Base class for nodes representing an expandable section under project's node.
 *
 * @author S. Aubrecht
 */
public abstract class SectionNode extends TreeListNode implements PropertyChangeListener {

    private final String displayName;
    private final String propertyName;
    JLabel lblName;
    private JLabel lblStatus;
    private JLabel lblError;
    private JPanel panel;
    protected final ProjectHandle project;

    /**
     * C'tor
     * @param displayName Node's title
     * @param parent Parent project
     * @param propertyName Name of the property to listen to on ProjectHandle. When
     * the property change is fired then children of this node are refreshed.
     */
    public SectionNode( String displayName, ProjectNode parent, String propertyName ) {
        super( true, parent );
        this.displayName = displayName;
        this.propertyName = propertyName;
        this.project = parent.getProject();
        project.addPropertyChangeListener(this);
    }

    @Override
    protected JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus) {
        if( null == panel ) {
            panel = new JPanel( new GridBagLayout() );
            panel.setOpaque(false);

            lblName = new TreeLabel(displayName);
            lblStatus = createProgressLabel(NbBundle.getMessage(SectionNode.class, "LBL_LoadingInProgress"));
            lblError = new TreeLabel();
            lblStatus.setVisible(false);
            lblError.setVisible(false);
            Image img = ImageUtilities.loadImage("org/netbeans/modules/kenai/ui/resources/error.png"); //NOI18N
            lblError.setIcon( new ImageIcon(img) );

            panel.add(lblName, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0) );
            panel.add(lblStatus, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0, 0) );
            panel.add(lblError, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0, 0) );
            panel.add(new JLabel(), new GridBagConstraints(3, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0) );
        }
        lblName.setForeground(foreground);
        if( isSelected )
            lblStatus.setForeground(foreground);
        else
            lblStatus.setForeground(ColorManager.getDefault().getDisabledColor());
        if( isSelected )
            lblError.setForeground(foreground);
        else
            lblError.setForeground(ColorManager.getDefault().getErrorColor());
        return panel;
    }

    public final void propertyChange(PropertyChangeEvent evt) {
        if( propertyName.equals( evt.getPropertyName() ) ) {
            refreshChildren();
        }
    }

    @Override
    protected void childrenLoadingFinished() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                lblStatus.setVisible(false);
                lblError.setVisible(false);
                fireContentChanged();
            }
        });
    }

    @Override
    protected void childrenLoadingStarted() {
        lblStatus.setVisible(true);
        lblError.setVisible(false);
        fireContentChanged();
    }

    @Override
    protected void childrenLoadingTimedout() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                lblError.setText(NbBundle.getMessage(SectionNode.class, "LBL_NotResponding")); //NOI18N
                lblError.setVisible(true);
                lblStatus.setVisible(false);
                fireContentChanged();
            }
        });
    }

    @Override
    protected void dispose() {
        super.dispose();
        project.removePropertyChangeListener(this);
    }
}
