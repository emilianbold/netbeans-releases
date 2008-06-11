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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
package org.netbeans.modules.print.api;

import javax.swing.Action;

/**
 * The Print manager is powerful functionality to preview and
 * send data out to printer. Print Preview action from <code>File</code>
 * menu (<code>Ctrl+Alt+Shift+P</code> shortcut) invokes Print Preview
 * dialog for selected nodes or opened views. The Print Preview dialog
 * provides page layout, the set of options including font, color, header,
 * footer, printer settings such as paper size and orientation, number
 * of copies, margins, collation and system properties.<p>
 *
 * There are several ways to enable print preview for a custom data:<p>
 *
 * If the data is a Swing component which extends
 * <code>javax.swing.JComponent</code> and is shown in a
 * <code>org.openide.windows.TopComponent</code>,
 * not <code>null</code> key <code>"java.awt.print.Printable.class"</code>
 * in the component should be set, see example:
 *
 * <blockquote><pre>
 * public class CustomComponent extends JComponent {
 *   public CustomComponent() {
 *     ...
 *     putClientProperty(java.awt.print.Printable.class, "&lt;name&gt;");
 *   }
 *   ...
 * }</pre></blockquote>
 *
 * The second argument in method <code>putClientProperty</code> is a
 * name, which is shown at the top/bottom of the print preview as the header/footer,
 * which can be changed in the Print Options dialog. If empty name is passed,
 * the display name of the top component is shown.<p>
 *
 * If the dimension of the custom component for printing differs from visual
 * dimension, specify this:
 *
 * <blockquote><pre>
 * putClientProperty(Dimension.class, new Dimension(printWidth, printHeight));</pre></blockquote>
 *
 * If custom data is presented by several components, all of them can be enabled
 * in print preview. For this purpose, the order of the components
 * (from the left to right) should be defined:
 *
 * <blockquote><pre>
 * putClientProperty(java.lang.Integer.class, new Integer(<weight>));</pre></blockquote>
 *
 * where <code>weight</code> is integer value.<p>
 *
 * If custom data is presented by another classes, print provider
 * (see <code>PrintProvider</code>) should be implemented and put it
 * into the lookup of a node, data object or top component where the
 * data lives.
 *
 * @see org.netbeans.modules.print.spi.PrintProvider
 *
 * @author Vladimir Yaroslavskiy
 * @version 2005.12.12
 */
public final class PrintManager {

  private PrintManager() {}

  /**
   * Returns Print Manager instance.
   * @return Print Manager instance
   */
  public static PrintManager getDefault() {
    return DEFAULT;
  }

  /**
   * Returns Print Preview action. See example how to put
   * Print Preview action on custom Swing tool bar:
   *
   * <blockquote><pre>
   * JToolBar toolbar = new JToolBar();
   * ...
   * // print preview
   * toolbar.addSeparator();
   * toolbar.add(PrintManager.getDefault().getPrintPreviewAction());
   * ...</pre></blockquote>
   *
   * @return Print Preview action
   */
  public Action getPrintPreviewAction() {
    return org.netbeans.modules.print.impl.action.PrintPreviewAction.DEFAULT;
  }

  private static final PrintManager DEFAULT = new PrintManager();
}
