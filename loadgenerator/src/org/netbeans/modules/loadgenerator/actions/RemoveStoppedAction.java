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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.loadgenerator.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.netbeans.modules.loadgenerator.spi.Engine;
import org.netbeans.modules.loadgenerator.spi.ProcessInstance;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 *
 * @author Jaroslav Bachorik
 */
public class RemoveStoppedAction extends AbstractAction {
  private static final String ICON = NbBundle.getMessage(RemoveStoppedAction.class, "RemoveStoppedAction_Icon"); // NOI18N
  private static final String PROP_ENABLED = "enabled"; // NOI18N
  
  private Engine engine = null;
  
  private PropertyChangeListener listener = new PropertyChangeListener() {
    public void propertyChange(PropertyChangeEvent evt) {
      // revert the boolean - true = disabled, false = enabled
      firePropertyChange(PROP_ENABLED, !((Boolean)evt.getOldValue()).booleanValue(), !((Boolean)evt.getNewValue()).booleanValue());
    }
  };
  
  /** Creates a new instance of RemoveStoppedAction */
  public RemoveStoppedAction(final Engine engine) {
    super(java.util.ResourceBundle.getBundle("org/netbeans/modules/loadgenerator/actions/Bundle").getString("Remove_All_Stopped"), new ImageIcon(ImageUtilities.loadImage(ICON)));
    this.engine = engine;
    this.engine.addPropertyChangeListener(ProcessInstance.STATE, WeakListeners.propertyChange(listener, engine));
  }

  public void actionPerformed(ActionEvent e) {
    engine.cleanup();
  }
  
  public boolean isEnabled() {
    return engine.canCleanup();
  }
}
