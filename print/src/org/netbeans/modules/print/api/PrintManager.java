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
 * <p class="nonnormative">
 * The Print Manager is powerful functionality to preview and
 * send data out to printer. Print action from <code>File</code>
 * menu (<code>Ctrl+Alt+Shift+P</code> shortcut) invokes the Print Preview
 * dialog. The Print Preview dialog provides page layout, the set of options
 * including font, color, header, footer, printer settings such as paper size
 * and orientation, number of copies, margins, collation and system properties.</p>
 *
 * There are several ways to enable printing for a custom data:<p>
 *
 * If the data is a Swing component which extends {@linkplain javax.swing.JComponent}
 * and shown in a {@link org.openide.windows.TopComponent}, the key
 * {@linkplain #PRINT_PRINTABLE} with value <code>"Boolean.TRUE"</code>
 * in the component must be set as a client property, see example:
 *
 * <blockquote><pre>
 * public class CustomComponent extends javax.swing.JComponent {
 *   public CustomComponent() {
 *     ...
 *     putClientProperty("print.printable", Boolean.TRUE); // NOI18N
 *   }
 *   ...
 * }</pre></blockquote>
 *
 * The key {@linkplain #PRINT_NAME} is used to specify the name of the component
 * which will be printed in the header/footer:
 *
 * <blockquote><pre>
 * putClientProperty("print.name", &lt;name&gt;); // NOI18N</pre></blockquote>
 *
 * If the key is not set at all, the display name of the top
 * component is used by default. The content of the header/footer
 * can be adjusted in the Print Options dialog.<p>
 *
 * If the custom data is presented by several components, all of them can
 * be enabled for print preview. The key {@linkplain #PRINT_WEIGHT} is used for
 * this purpose, all visible and printable components are sorted by weight
 * and shown in the Print Preview dialog from the left to right:
 *
 * <blockquote><pre>
 * putClientProperty("print.weight", &lt;weight&gt;); // NOI18N</pre></blockquote>
 *
 * If the custom data is presented by another classes, a provider
 * {@linkplain org.netbeans.modules.print.spi.PrintProvider} should be implemented
 * and put in the lookup of the top component where the custom data lives.
 *
 * @see org.netbeans.modules.print.spi.PrintProvider
 *
 * @author Vladimir Yaroslavskiy
 * @version 2005.12.12
 */
public final class PrintManager {

  /**
   * This key indicates the name of the component being printed.
   * By default, the name is shown in the left part of the header.
   */
  public static final String PRINT_NAME = "print.name"; // NOI18N

  /**
   * This key indicates the weight of the component being printed.
   * The value of the key must be Integer. All visible and printable
   * components are sorted by weight and shown in the Print Preview
   * dialog from the left to right.
   */
  public static final String PRINT_WEIGHT = "print.weight"; // NOI18N

  /**
   * This key indicates whether the component is printable. To be printable
   * the value Boolean.TRUE must be set as a client property of the component.
   */
  public static final String PRINT_PRINTABLE = "print.printable"; // NOI18N

  /**
   * Creates a new instance of <code>PrintManager</code>.
   */
  private PrintManager() {}

  /**
   * Returns Print action. See example how to put
   * the Print action on custom Swing tool bar:
   *
   * <blockquote><pre>
   * JToolBar toolbar = new JToolBar();
   * ...
   * // print
   * toolbar.addSeparator();
   * toolbar.add(PrintManager.printAction());
   * ...</pre></blockquote>
   *
   * How does Print Manager decide what to print?<p>
   *
   * At first, the manager searches {@linkplain org.netbeans.modules.print.spi.PrintProvider}
   * in the lookup of the active top component {@link org.openide.windows.TopComponent}.
   * If a print provider is found, it is used by the print manager for print preview.
   * Otherwise, it tries to obtain printable components (marked as {@linkplain #PRINT_PRINTABLE})
   * among the descendants of the active top component. All found printable components
   * are passed into the Print Preview dialog.<p>
   * 
   * If there are no printable components, printable data are retrieved from the selected
   * nodes {@link org.openide.nodes.Node} of the active top component: the manager
   * searches {@linkplain org.netbeans.modules.print.spi.PrintProvider} in the lookups
   * of the nodes. All pages {@linkplain org.netbeans.modules.print.spi.PrintPage},
   * taken from found providers, are displayed in the preview dialog.<p>
   * 
   * If nodes don't have print providers in lookups, the manager gets the cookie
   * {@link org.openide.cookies.EditorCookie} from the {@link org.openide.loaders.DataObject}
   * of the nodes. The {@linkplain javax.swing.text.StyledDocument} documents, returned by
   * the editor cookies, contain printing information (text, font, color). This information
   * is shown in the print preview. So, any textual documents (java sources, html, xml, plain
   * text etc.) are printable by default.
   * 
   * @return Print action
   */
  public static Action printAction() {
    return org.netbeans.modules.print.impl.action.PrintAction.DEFAULT;
  }
}
