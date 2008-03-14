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
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.ToolTipManager;

import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.ext.logging.api.Trace;

import org.netbeans.modules.bpel.design.decoration.components.AbstractGlassPaneButton;
import org.netbeans.modules.bpel.design.selection.EntitySelectionModel;
import org.netbeans.modules.bpel.design.actions.GoToLoggingAction;
import org.netbeans.modules.soa.ui.nodes.InstanceRef;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.08.15
 */
public final class LoggingButton extends AbstractGlassPaneButton {

    private static final long serialVersionUID = 1L;

    public LoggingButton(final DesignView designView, final ExtensibleElements element) {
      super(ICON);
//    super(ICON, "", true, new ActionListener() {
//      public void actionPerformed(ActionEvent event) {
////        String content = event.getSource().toString().trim();
////        try {
////          element.setDocumentation(content);
////        }
////        catch (VetoException e) {
////          e.printStackTrace();
////        }
//      }
//    });
    myElement = element;
    myDesignView = designView;
////    setAction(SystemAction.get(GoToLoggingAction.class));
    setAction(new AbstractAction() {

          private static final long serialVersionUID = 1657345834023L;

          public void actionPerformed(ActionEvent event) {
              EntitySelectionModel selectionModel = myDesignView != null 
                      ? myDesignView.getSelectionModel() : null;
              if (selectionModel == null || myElement == null) {
                  return;
              }
              selectionModel.setSelected(element);
                  GoToLoggingAction gotoAction = new GoToLoggingAction(myDesignView);
                  gotoAction.actionPerformed(event);
          }
        });


    addTitle(ICON, TITLE, Color.BLUE);
    ToolTipManager.sharedInstance().registerComponent(this);
  }

  @Override
  public String getToolTipText() {
    String text = null;
    List<Trace> traceElements = myElement.getChildren(Trace.class);
    return traceElements != null && traceElements.size() == 1
            ? NbBundle.getMessage(LoggingButton.class, "LBL_LoggingButtonTooltip") : null;
  }

    @Override
    public void actionPerformed(ActionEvent e) {
        // do nothing
    }

  private ExtensibleElements myElement;
  private DesignView  myDesignView;

  private static final String TITLE = NbBundle.getMessage(LoggingButton.class, "LBL_LoggingButtonTitle"); // NOI18N
  private static final Icon ICON = new ImageIcon(LoggingButton.class.getResource("resources/logging.png"));
}
