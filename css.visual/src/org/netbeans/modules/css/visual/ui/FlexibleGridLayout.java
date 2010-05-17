/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.css.visual.ui;
import java.awt.*;

// Grid Layout which allows components of differrent sizes
public class FlexibleGridLayout extends GridLayout 
{
  public FlexibleGridLayout() {
    this(1, 0, 0, 0);
  }

  public FlexibleGridLayout(int rows, int cols) {
    this(rows, cols, 0, 0);
  }

  public FlexibleGridLayout(int rows, int cols, int hgap, int vgap) {
    super(rows, cols, hgap, vgap);
  }

  public Dimension preferredLayoutSize(Container parent) {
	//System.err.println("preferredLayoutSize");
    synchronized (parent.getTreeLock()) {
      Insets insets = parent.getInsets();
      int ncomponents = parent.getComponentCount();
      int nrows = getRows();
      int ncols = getColumns();
      if (nrows > 0) {
        ncols = (ncomponents + nrows - 1) / nrows;
      } 
      else {
        nrows = (ncomponents + ncols - 1) / ncols;
      }
      int[] w = new int[ncols];
      int[] h = new int[nrows];
      for (int i = 0; i < ncomponents; i ++) {
        int r = i / ncols;
        int c = i % ncols;
        Component comp = parent.getComponent(i);
        Dimension d = comp.getPreferredSize();
        if (w[c] < d.width) {
          w[c] = d.width;
        }
        if (h[r] < d.height) {
          h[r] = d.height;
        }
      }
      int nw = 0;
      for (int j = 0; j < ncols; j ++) {
        nw += w[j];
      }
      int nh = 0;
      for (int i = 0; i < nrows; i ++) {
        nh += h[i];
      }
      return new Dimension(insets.left + insets.right + nw + (ncols-1)*getHgap(), 
          insets.top + insets.bottom + nh + (nrows-1)*getVgap());
    }
  }

  public Dimension minimumLayoutSize(Container parent) {
	System.err.println("minimumLayoutSize");
    synchronized (parent.getTreeLock()) {
      Insets insets = parent.getInsets();
      int ncomponents = parent.getComponentCount();
      int nrows = getRows();
      int ncols = getColumns();
      if (nrows > 0) {
        ncols = (ncomponents + nrows - 1) / nrows;
      } 
      else {
        nrows = (ncomponents + ncols - 1) / ncols;
      }
      int[] w = new int[ncols];
      int[] h = new int[nrows];
      for (int i = 0; i < ncomponents; i ++) {
        int r = i / ncols;
        int c = i % ncols;
        Component comp = parent.getComponent(i);
        Dimension d = comp.getMinimumSize();
        if (w[c] < d.width) {
          w[c] = d.width;
        }
        if (h[r] < d.height) {
          h[r] = d.height;
        }
      }
      int nw = 0;
      for (int j = 0; j < ncols; j ++) {
        nw += w[j];
      }
      int nh = 0;
      for (int i = 0; i < nrows; i ++) {
        nh += h[i];
      }
      return new Dimension(insets.left + insets.right + nw + (ncols-1)*getHgap(), 
          insets.top + insets.bottom + nh + (nrows-1)*getVgap());
    }
  }

  public void layoutContainer(Container parent) {
    //System.err.println("layoutContainer");
    synchronized (parent.getTreeLock()) {
      Insets insets = parent.getInsets();
      int ncomponents = parent.getComponentCount();
      int nrows = getRows();
      int ncols = getColumns();
      if (ncomponents == 0) {
        return;
      }
      if (nrows > 0) {
        ncols = (ncomponents + nrows - 1) / nrows;
      } 
      else {
        nrows = (ncomponents + ncols - 1) / ncols;
      }
      int hgap = getHgap();
      int vgap = getVgap();
	  // scaling factors      
      Dimension pd = preferredLayoutSize(parent);
      double sw = (1.0 * parent.getWidth()) / pd.width;
      double sh = (1.0 * parent.getHeight()) / pd.height;
      // scale
      int[] w = new int[ncols];
      int[] h = new int[nrows];
      for (int i = 0; i < ncomponents; i ++) {
        int r = i / ncols;
        int c = i % ncols;
        Component comp = parent.getComponent(i);
        Dimension d = comp.getPreferredSize();
        d.width = (int) (sw * d.width);
        d.height = (int) (sh * d.height);
        if (w[c] < d.width) {
          w[c] = d.width;
        }
        if (h[r] < d.height) {
          h[r] = d.height;
        }
      }
      for (int c = 0, x = insets.left; c < ncols; c ++) {
        for (int r = 0, y = insets.top; r < nrows; r ++) {
          int i = r * ncols + c;
          if (i < ncomponents) {
            parent.getComponent(i).setBounds(x, y, w[c], h[r]);
          }
          y += h[r] + vgap;
        }
        x += w[c] + hgap;
      }
    }
  }  
}
