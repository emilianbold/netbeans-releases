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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.jmeter.module.nodes;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.apache.jmeter.module.actions.ManageJMeterProcessAction;
import org.apache.jmeter.module.cookies.JMeterProcessCookie;
import org.apache.jmeter.module.loadgenerator.spi.impl.ProcessDescriptor;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Jaroslav Bachorik
 */
public class ProcessNode extends AbstractNode {
  
  /** Creates a new instance of ProcessNode */
  public ProcessNode(final ProcessDescriptor descriptor) {
    super(Children.LEAF);
    getCookieSet().add(new JMeterProcessCookie(descriptor));
  }

  public Action[] getActions(boolean b) {
    return new Action[] {ManageJMeterProcessAction.findObject(ManageJMeterProcessAction.class, true)};
  }
}
