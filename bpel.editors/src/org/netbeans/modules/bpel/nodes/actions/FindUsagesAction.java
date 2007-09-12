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
package org.netbeans.modules.bpel.nodes.actions;

import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.xml.refactoring.ui.WhereUsedQueryUI;
import org.openide.nodes.Node;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.windows.TopComponent;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.10.27
 */
public class FindUsagesAction extends CookieAction {
    private static final long serialVersionUID = 1L;
    private static final Class[] COOKIE_ARRAY =
            new Class[] { };

    public FindUsagesAction() {
        super();
    }

    public boolean enable(Node[] nodes) {
        return nodes != null && nodes.length == 1 && nodes[0] instanceof BpelNode;
    }

    protected Referenceable getReferenceable(Node[] nodes) {
      if (nodes.length != 1) {
        return null;
      }
      Node node = nodes [0];

      if ( !(node instanceof BpelNode)) {
        return null;
      }
      Object object = ((BpelNode) node).getReference();

      if (object instanceof Referenceable) {
        return (Referenceable) object;
      }
      return null;
    }

    public String getName() {
        return "Find Usages"; // NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }
    
    protected boolean asynchronous() {
        return false;
    }
        
    public void performAction(Node[] nodes) {
       assert nodes.length==1:
            "Length of nodes array should be 1";
        Referenceable ref = getReferenceable(nodes);
        assert ref != null:"The node's NamedReferenceable should not be null";
        WhereUsedQueryUI ui = new WhereUsedQueryUI(ref);
        TopComponent activetc = TopComponent.getRegistry().getActivated();

        if (activetc instanceof CloneableEditorSupport.Pane) {
           UI.openRefactoringUI(ui, activetc);
        } else {
            UI.openRefactoringUI(ui);
        }
    }
 
    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    protected Class[] cookieClasses() {
        return COOKIE_ARRAY;
    }
}
