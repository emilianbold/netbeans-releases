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

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.kenai.ui.spi.BuildAccessor;
import org.netbeans.modules.kenai.ui.spi.MessagingAccessor;
import org.netbeans.modules.kenai.ui.treelist.TreeListNode;
import org.netbeans.modules.kenai.ui.spi.ProjectAccessor;
import org.netbeans.modules.kenai.ui.spi.ProjectHandle;
import org.netbeans.modules.kenai.ui.spi.QueryAccessor;
import org.netbeans.modules.kenai.ui.spi.SourceAccessor;
import org.openide.util.NbBundle;

/**
 * Project's root node
 *
 * @author S. Aubrecht
 */
public class ProjectNode extends TreeListNode {

    private final ProjectHandle project;
    private final ProjectAccessor accessor;

    private JPanel component = null;
    private JLabel lbl = null;
    private LinkButton btnDetails = null;

    private final Object LOCK = new Object();

    public ProjectNode( ProjectHandle project ) {
        super( true, null );
        this.project = project;
        this.accessor = ProjectAccessor.getDefault();
    }

    ProjectHandle getProject() {
        return project;
    }

    ProjectAccessor getAccessor() {
        return accessor;
    }

    protected List<TreeListNode> createChildren() {
        ArrayList<TreeListNode> children = new ArrayList<TreeListNode>();
        if( null != MessagingAccessor.getDefault() )
            children.add( new MessagingNode(this, project) );
        if( null != BuildAccessor.getDefault() )
            children.add( new BuildListNode(this) );
        if( null != QueryAccessor.getDefault() )
            children.add( new QueryListNode(this) );
        if( null != SourceAccessor.getDefault() )
            children.add( new SourceListNode(this) );
        return children;
    }

    @Override
    protected JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus) {
        synchronized( LOCK ) {
            if( null == component ) {
                component = new JPanel( new GridBagLayout() );
                component.setOpaque(false);
                lbl = new JLabel(project.getDisplayName());
                component.add( lbl, new GridBagConstraints(0,0,1,1,0.0,0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,3), 0,0) );

                component.add( new JLabel(), new GridBagConstraints(2,0,1,1,1.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0) );
                btnDetails = new LinkButton(NbBundle.getMessage(ProjectNode.class, "LBL_ProjectDetails"), accessor.getDetailsAction(project)); //NOI18N
                component.add( btnDetails, new GridBagConstraints(3,0,1,1,0.0,0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,3,0,0), 0,0) );
            }
            lbl.setForeground(foreground);
            btnDetails.setForeground(foreground, isSelected);
            return component;
        }
    }

    @Override
    public ActionListener getDefaultAction() {
        return accessor.getDefaultAction(project);
    }

    @Override
    public Action[] getPopupActions() {
        return accessor.getPopupActions(project);
    }
}
