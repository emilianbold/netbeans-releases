/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s): theanuradha@netbeans.org
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.repository;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 *
 * @author mkleint
 * @author Anuradha
 */
public class GroupListChildren extends Children.Keys implements ChangeListener {

    public static final Object LOADING = new Object();
   private RepositoryInfo info;
    public static Node createLoadingNode() {
        AbstractNode nd = new AbstractNode(Children.LEAF){

            @Override
            public Image getIcon(int arg0) {
                return Utilities.loadImage("org/netbeans/modules/maven/repository/wait.gif"); //NOI18N
            }
         
        };
        nd.setName("Loading"); //NOI18N
        nd.setDisplayName(NbBundle.getMessage(GroupListChildren.class, "Node_Loading"));
        return nd;
    }
    private List keys;

    public GroupListChildren(RepositoryInfo info) {
        this.info = info;
        
    }

    /** Creates a new instance of GroupListChildren */
    

    protected Node[] createNodes(Object key) {
        if (LOADING == key) {
            return new Node[]{createLoadingNode()};
        }
        String groupId = (String) key;
        return new Node[]{new GroupNode(info,groupId)};
    }

    @Override
    protected void addNotify() {
        super.addNotify();
        refreshGroups();
        info.addChangeListener(WeakListeners.change(this, info));
    }
    
    public void refreshGroups() {
        setKeys(Collections.singletonList(LOADING));
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                keys = new ArrayList(RepositoryQueries.getGroups(info));
                setKeys(keys);
            }
        });
    }

    @Override
    protected void removeNotify() {
        super.removeNotify();
        keys = Collections.EMPTY_LIST;
        setKeys(Collections.EMPTY_LIST);
    }

    public void stateChanged(ChangeEvent e) {
        refreshGroups();
    }
}
