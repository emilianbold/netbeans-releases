package org.netbeans.modules.team.server.ui.common;

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */



import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.modules.team.server.ui.spi.DashboardProvider;
import org.netbeans.modules.team.server.ui.spi.MemberAccessor;
import org.netbeans.modules.team.server.ui.spi.MemberHandle;
import org.netbeans.modules.team.server.ui.spi.MessagingHandle;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.netbeans.modules.team.commons.treelist.LeafNode;
import org.netbeans.modules.team.commons.treelist.TreeListNode;
import org.openide.util.NbBundle;

/**
 * Node for project's sources section.
 *
 * @author Jan Becicka
 */
public class MemberListNode<P> extends SectionNode {

    private MessagingHandle msg;
    private PropertyChangeListener l;
    private static final String PROP_MEMBERS = "members"; // NOI18N
    private final DashboardProvider<P> dashboard;

    public MemberListNode( TreeListNode parent, ProjectHandle project, DashboardProvider<P> dashboard ) {
        super(getText(dashboard.getMessagingAccessor().getMessaging(project)),
             parent, project, PROP_MEMBERS); //NOI18N
        this.dashboard = dashboard;
        msg = dashboard.getMessagingAccessor().getMessaging(project);
        msg.addPropertyChangeListener(l=new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (MessagingHandle.PROP_ONLINE_COUNT.equals(evt.getPropertyName())) {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            if (lblName!=null) {
                                lblName.setText(getText(msg));
                            }
                        }
                    });
                }
            }
        });
    }

    private static String getText(MessagingHandle msg) {
        int count = msg.getOnlineCount();
        if (count>=0) {
            return NbBundle.getMessage(MemberListNode.class, "LBL_MembersOnline", msg.getOnlineCount());
        } else {
            return NbBundle.getMessage(MemberListNode.class, "LBL_Members");
        }
    }

    @Override
    protected List<TreeListNode> createChildren() {
        ArrayList<TreeListNode> res = new ArrayList<TreeListNode>(20);
        MemberAccessor<P> accessor = dashboard.getMemberAccessor();
        List<MemberHandle> sources = accessor.getMembers(project);
        for (MemberHandle s : sources) {
            LeafNode n = dashboard.createMemberNode(s, this);
            if(n != null) {
                res.add(n);
            }
        }
        return res;
    }

    @Override
    protected void dispose() {
        super.dispose();
        if (msg!=null) {
            msg.removePropertyChangeListener(l);
        }
    }
}
