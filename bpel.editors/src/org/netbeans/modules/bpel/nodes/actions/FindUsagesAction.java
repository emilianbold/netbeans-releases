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

import org.openide.nodes.Node;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.bpel.nodes.BpelNode;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.10.27
 */
public class FindUsagesAction extends org.netbeans.modules.xml.refactoring.actions.FindUsagesAction {
    private static final long serialVersionUID = 1L;
//    public static final String FINDUSAGES_KEYSTROKE = 
//            NbBundle.getMessage(FindUsagesAction.class,"ACT_FindUsagesAction");// NOI18N

    public FindUsagesAction() {
        super();
//        putValue(FindUsagesAction.ACCELERATOR_KEY, 
//                KeyStroke.getKeyStroke(FINDUSAGES_KEYSTROKE));
    }

    public void performAction(Node[] nodes) {
        super.performAction(nodes);
    }
    
    @Override
    public boolean enable(Node[] nodes) {
        return super.enable(nodes);
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
}
