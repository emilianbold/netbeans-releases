/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.dlight.core.ui.components;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JComboBox;

/**
 *
 * @author ak119685
 */
class DLightTargetSelectionDialogLayout implements LayoutManager {

  static int COMBO_IDX = 0;
  static int BTN_IDX = 1;
  Dimension origin = new Dimension(400, 56);

  public void addLayoutComponent(String name, Component comp) {
  }

  public void removeLayoutComponent(Component comp) {
  }

  public Dimension preferredLayoutSize(Container parent) {
    return origin;
  }

  public Dimension minimumLayoutSize(Container parent) {
    return origin;
  }

  public void layoutContainer(Container parent) {
    Rectangle b = parent.getBounds();
    int inset = 8;
    int vOffset = 20;
    int hOffset = 0;
    int btnWSize = 80;

    if (parent.getComponentCount() > 1) {
      JButton btn = (JButton) parent.getComponent(BTN_IDX);
      int btnHSize = (int) btn.getPreferredSize().getHeight();
      btn.setBounds(b.width - inset - btnWSize, vOffset, btnWSize, btnHSize);
      hOffset = inset + btnWSize;
    }

    JComboBox combo = (JComboBox) parent.getComponent(COMBO_IDX);
    int comboHSize = (int) combo.getPreferredSize().getHeight() + 1;
    combo.setBounds(inset, vOffset, b.width - 2 * inset - hOffset, comboHSize);

  }
}
