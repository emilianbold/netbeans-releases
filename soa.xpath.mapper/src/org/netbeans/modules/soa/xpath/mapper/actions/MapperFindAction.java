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
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.xpath.mapper.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.openide.actions.FindAction;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author AlexanderPermyakov
 */
public class MapperFindAction extends NodeAction {
    private Mapper mapper;
    
    public MapperFindAction() {
        super();
    }
    
    public void initialize(Mapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected void performAction(Node[] arg0) {
        if (mapper == null) { return; }
        
        Action action = mapper.getActionMap().get(SystemAction.
                get(FindAction.class).getActionMapKey());
        if (action == null) {return; }
        
        action.actionPerformed(new ActionEvent(mapper, 0, "Mapper-Find"));
    }

    @Override
    protected boolean enable(Node[] arg0) {
        return true;
    }

    @Override
    public String getName() {
        return SystemAction.get(FindAction.class).getName();
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return SystemAction.get(FindAction.class).getHelpCtx();
    }

}
