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
package org.netbeans.modules.dlight.core.ui.components;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.netbeans.modules.dlight.management.api.DLightSession;
import org.netbeans.modules.dlight.spi.indicator.Indicator;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
final class DLightIndicatorsTopComponent extends TopComponent {

  private static DLightIndicatorsTopComponent instance;
  private DLightSession session;
  /** path to the icon used by the component and its open action */
  static final String ICON_PATH = "org/netbeans/modules/dlight/core/ui/resources/indicators_small.png";
  private static final String PREFERRED_ID = "DLightIndicatorsTopComponent";
  private boolean isInitialized = false;

  DLightIndicatorsTopComponent() {
    setSession(null);
    init();
  }

  @Override
  public void open() {
    if (!isInitialized) {
      init();
    }
    super.open();
  }

  private void init() {
    if (isInitialized){
        return;
    }
    setName(NbBundle.getMessage(DLightIndicatorsTopComponent.class, "CTL_DLightIndicatorsTopComponent"));
    setToolTipText(NbBundle.getMessage(DLightIndicatorsTopComponent.class, "HINT_DLightIndicatorsTopComponent"));
    setIcon(ImageUtilities.loadImage(ICON_PATH, true));
    isInitialized = true;
  }

  public void setSession(DLightSession session) {
    this.session = session;
    removeAll();
    setLayout(new BorderLayout());
    if (session != null) {
//            JPanel indicatorsPane = new JPanel(new RowLayoutManager());
      JPanel indicatorsPane = new JPanel();
      indicatorsPane.setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.gridx = 0;
      c.fill = GridBagConstraints.NONE;
      c.anchor = GridBagConstraints.WEST;
      c.insets = new Insets(6, 6, 6, 6);
      int row = 0;
      for (Indicator indicator : session.getIndicators()) {
        JComponent indicatorComponent = indicator.getComponent();
        c.gridy = row++;
        indicatorsPane.add(indicatorComponent, c);
      }
      add(indicatorsPane, BorderLayout.NORTH);
    } else {
      add(new JLabel("<Empty>"));
    }

    repaint();
  }

  /**
   * Gets default instance. Do not use directly: reserved for *.settings files only,
   * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
   * To obtain the singleton instance, use {@link #findInstance}.
   */
  public static synchronized DLightIndicatorsTopComponent getDefault() {
    if (instance == null) {
      instance = new DLightIndicatorsTopComponent();
    }
    return instance;
  }

  /**
   * Obtain the DLightIndicatorsTopComponent instance. Never call {@link #getDefault} directly!
   */
  public static synchronized DLightIndicatorsTopComponent findInstance() {
    TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
    if (win == null) {
      Logger.getLogger(DLightIndicatorsTopComponent.class.getName()).warning(
          "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
      return getDefault();
    }
    if (win instanceof DLightIndicatorsTopComponent) {
      return (DLightIndicatorsTopComponent) win;
    }
    Logger.getLogger(DLightIndicatorsTopComponent.class.getName()).warning(
        "There seem to be multiple components with the '" + PREFERRED_ID +
        "' ID. That is a potential source of errors and unexpected behavior.");
    return getDefault();
  }

  @Override
  public int getPersistenceType() {
    return TopComponent.PERSISTENCE_NEVER;
  }

  @Override
  public void componentOpened() {

  }

  @Override
  public void componentClosed() {
    // TODO add custom code on component closing
    }

  /** replaces this in object stream */
  @Override
  public Object writeReplace() {
    return new ResolvableHelper();
  }

  @Override
  protected String preferredID() {
    return PREFERRED_ID;
  }

  static final class ResolvableHelper implements Serializable {

    private static final long serialVersionUID = 1L;

    public Object readResolve() {
      return DLightIndicatorsTopComponent.getDefault();
    }
  }

}
