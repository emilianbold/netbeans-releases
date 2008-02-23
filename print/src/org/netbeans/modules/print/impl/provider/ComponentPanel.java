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
package org.netbeans.modules.print.impl.provider;

import java.awt.Dimension;
import java.awt.Graphics;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import static org.netbeans.modules.print.impl.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.02.22
 */
final class ComponentPanel extends JPanel {

  ComponentPanel(List<JComponent> components) {
    myComponents = sort(components);

    myHeight = 0;
    myWidth = 0;

    myWidths = new int [components.size()];
//out();
    for (int i=0; i < myComponents.size(); i++) {
      JComponent component = myComponents.get(i);
      Dimension dimension = getDimension(component);
//out("see: " + component.getClass().getName() + " " + component.getClientProperty(Integer.class));

      int height;
      int width;

      if (dimension == null) {
        height = component.getHeight();
        width = component.getWidth();
      }
      else {
        height = dimension.height;
        width = dimension.width;
      }
      myWidths [i] = width;
      myWidth += width;

      if (height > myHeight) {
        myHeight = height;
      }
    }
//out();
//out("w: " + myWidth);
//out("h: " + myHeight);
  }

  @Override
  public void print(Graphics g)
  {
    int x = 0;

    for (int i=0; i < myComponents.size(); i++) {
      JComponent component = myComponents.get(i);
      g.translate(x, 0);
      component.print(g);
      x += myWidths [i];
    }
  }

  @Override
  public int getWidth()
  {
    return myWidth;
  }

  @Override
  public int getHeight()
  {
    return myHeight;
  }

  private Dimension getDimension(JComponent component) {
    Object object = component.getClientProperty(Dimension.class);
      
    if (object instanceof Dimension) {
      return (Dimension) object;
    }
    return null;
  }

  private List<JComponent> sort(List<JComponent> components) {
    Collections.sort(components, new Comparator<JComponent>() {
      public int compare(JComponent component1, JComponent component2) {
        int weight1 = getInteger(component1).intValue();
        int weight2 = getInteger(component2).intValue();

        if (weight1 < weight2) {
          return -1;
        }
        if (weight1 == weight2) {
          return 0;
        }
        return 1;
      }

      private Integer getInteger(JComponent component) {
        Object object = component.getClientProperty(java.lang.Integer.class);

        if (object instanceof Integer) {
          return (Integer) object;
        }
        return Integer.MIN_VALUE;
      }
    });

    return components;
  }

  private int myWidth;
  private int myHeight;
  private int [] myWidths;
  private List<JComponent> myComponents;
}
