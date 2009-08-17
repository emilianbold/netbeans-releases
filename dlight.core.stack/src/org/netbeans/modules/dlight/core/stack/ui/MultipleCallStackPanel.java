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

package org.netbeans.modules.dlight.core.stack.ui;

import java.util.List;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.util.Lookup;

/**
 *
 * @author mt154047
 */
public final class MultipleCallStackPanel extends JPanel implements ExplorerManager.Provider, Lookup.Provider{
    private final ExplorerManager manager = new ExplorerManager();
    private final MultipleCallStackRootNode rootNode = new MultipleCallStackRootNode();
    private final BeanTreeView treeView;
    private Lookup lookup;



    private MultipleCallStackPanel(){
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        manager.setRootContext(rootNode);//NOI18N
        lookup = ExplorerUtils.createLookup(manager, new ActionMap());
        treeView = new BeanTreeView();
        treeView.setRootVisible(false);
        add(treeView);

    }

    public static final  MultipleCallStackPanel createInstance(){
        return new MultipleCallStackPanel();
    }

    public void clean(){
        rootNode.removeAll();
        treeView.setRootVisible(false);
    }

    @Override
    public void addNotify() {
      //  rootNode.update();
        super.addNotify();
        treeView.expandAll();
        
    }


    public void expandAll(){
        treeView.expandAll();
    }

    public void setRootVisible(String rootName){
        treeView.setRootVisible(true);
        rootNode.setDisplayName(rootName);
    }

    public final void add(String rootName, Icon icon, List<FunctionCall> stack){
        rootNode.add(new StackRootNode(null, icon, rootName, stack));
    }

    public final void add(String rootName, boolean isRootVisible, List<FunctionCall> stack){
        treeView.setRootVisible(false);
        rootNode.add(new StackRootNode(null, rootName, stack));
    }

    public void update(){

    }
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    public Lookup getLookup() {
        return lookup;
    }


 
}