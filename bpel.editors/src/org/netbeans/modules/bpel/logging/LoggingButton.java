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
package org.netbeans.modules.bpel.logging;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.ToolTipManager;

import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.ext.logging.api.Trace;
import org.netbeans.modules.bpel.model.api.events.VetoException;

import org.netbeans.modules.bpel.design.decoration.components.AbstractGlassPaneButton;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.08.15
 */
public final class LoggingButton extends AbstractGlassPaneButton {

  public LoggingButton(final ExtensibleElements element) {
    super(ICON, "", true, new ActionListener() {
      public void actionPerformed(ActionEvent event) {
//        String content = event.getSource().toString().trim();
//        try {
//          element.setDocumentation(content);
//        }
//        catch (VetoException e) {
//          e.printStackTrace();
//        }
      }
    });
    myElement = element;
    addTitle(ICON, TITLE, Color.BLUE);
    ToolTipManager.sharedInstance().registerComponent(this);
  }

  @Override
  public String getToolTipText() {
    String text = null;
    List<Trace> traceElements = myElement.getChildren(Trace.class);
    if (traceElements != null && traceElements.size() == 1) {
        Trace trace = traceElements.get(0);
        text = "There is trace element here";
    }

    if (text != null) {
        return "<html><body>" + text + "</body></html>"; // NOI18N
    } else {
        return null;
    }
    
  }

  private ExtensibleElements myElement;

  private static final String TITLE = "Logging and Alerting"; // todo bundle
  private static final Icon ICON = new ImageIcon(LoggingButton.class.getResource("resources/logging.png"));
}
