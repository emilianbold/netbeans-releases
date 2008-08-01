/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
        return nodes != null && getReferenceable(nodes) != null; 
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
        if (!enable(nodes)) {
            return;
        }
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
