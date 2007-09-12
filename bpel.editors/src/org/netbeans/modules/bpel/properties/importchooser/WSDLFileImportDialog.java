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
package org.netbeans.modules.bpel.properties.importchooser;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;

import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceCustomizer;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDataNode;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDecorator;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceNode;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.12.14
 */
public final class WSDLFileImportDialog
  extends ExternalReferenceCustomizer<Definitions>
{
  /**{@inheritDoc}*/
  public WSDLFileImportDialog(WSDLModel model) {
    super(model.getDefinitions(), null);
  }
                          
  /**{@inheritDoc}*/
  public WSDLModel getModel() {
    return myModel;
  }

  @Override
  protected String getReferenceLocation()
  {
    return null;
  }
  
  @Override
  protected String getNamespace()
  {
    return null;
  }
  
  @Override
  protected String getPrefix()
  {
    return null;
  }

  @Override
  protected String getTargetNamespace(Model model)
  {
    return null;
  }
  
  @Override
  protected Map<String, String> getPrefixes(Model model)
  {
    return new HashMap<String, String>();
  }

  @Override
  protected ExternalReferenceDecorator getNodeDecorator()
  {
    if (myDecorator == null) {
      myDecorator = new Decorator();
    }
    return myDecorator;
  }

  @Override
  protected String generatePrefix()
  {
    return null;
  }
  
  /**{@inheritDoc}*/
  @Override
  public boolean mustNamespaceDiffer()
  {
    return false;
  }

  /**{@inheritDoc}*/
  @Override
  public void propertyChange(PropertyChangeEvent event)
  {
//System.out.println();
//System.out.println("property change: " + event.getPropertyName());
    if ( !ExplorerManager.PROP_SELECTED_NODES.equals(event.getPropertyName())) {
      super.propertyChange(event);
//System.out.println("  not selection");
      return;
    }
    myModel = null;
    setSaveEnabled(false);
    Object value = event.getNewValue();

    if ( !(value instanceof Node[])) {
//System.out.println("  not new nodes: " + value.getClass().getName());
      return;
    }
    Node[] nodes = (Node[]) value;

    if (nodes.length != 1) {
//System.out.println("  nodes != 1");
      return;
    }
    Node node = nodes[0];

    if ( !(node instanceof ExternalReferenceNode)) {
//System.out.println("  node is not ExternalReferenceNode");
      return;
    }
    Model model = ((ExternalReferenceNode) node).getModel();

    if ( !(model instanceof WSDLModel)) {
//System.out.println("  is not WSDL model");
      return;
    }
    myModel = (WSDLModel) model;
//System.out.println("  OK: " + myModel);
    setSaveEnabled(true);
  }

  /**{@inheritDoc}*/
  public HelpCtx getHelpCtx() {
    return HelpCtx.DEFAULT_HELP;
  }
  
  // ------------------------------------------------------------
  private class Decorator implements ExternalReferenceDecorator {

    public String validate(ExternalReferenceNode node) {
      return null;
    }

    public String annotate(ExternalReferenceNode node) {
      return null;
    }

    public ExternalReferenceDataNode createExternalReferenceNode(Node original) {
      return WSDLFileImportDialog.this.createExternalReferenceNode(original);
    }

    public String generatePrefix(ExternalReferenceNode node) {
      return null;
    }

    public Utilities.DocumentTypesEnum getDocumentType() {
      return Utilities.DocumentTypesEnum.wsdl;
    }

    public String getHtmlDisplayName(String name, ExternalReferenceNode node) {
      return name;
    }

    public String getNamespace(Model model) {
      return null;
    }

    public boolean isAcceptable(ExternalReferenceNode node) {
      return true;
    }

    private ExternalReferenceCustomizer myCustomizer;
  }

  private WSDLModel myModel;
  private ExternalReferenceDecorator myDecorator;
}
