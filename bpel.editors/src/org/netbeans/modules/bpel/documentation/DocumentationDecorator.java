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
package org.netbeans.modules.bpel.documentation;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;

import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.decoration.ComponentsDescriptor;
import org.netbeans.modules.bpel.design.decoration.Decoration;
import org.netbeans.modules.bpel.design.decoration.DecorationProvider;
import org.netbeans.modules.bpel.design.decoration.DecorationProviderFactory;
import org.netbeans.modules.bpel.design.decoration.Descriptor;
import org.netbeans.modules.bpel.design.selection.DiagramSelectionListener;
import static org.netbeans.modules.soa.ui.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.08.13
 */
public final class DocumentationDecorator extends DecorationProvider
  implements DecorationProviderFactory, DiagramSelectionListener {

  public DocumentationDecorator() {}

  public DecorationProvider createInstance(DesignView view) {
    return new DocumentationDecorator(view);
  }

  private DocumentationDecorator(DesignView view) {
    super(view);
    getDesignView().getSelectionModel().addSelectionListener(this);
  }

  @Override
  public Decoration getDecoration(BpelEntity entity) {
    String documentation = getDocumentation(entity);
//out();
//out("entity: " + entity);
//out(" docum: " + documentation);

    if (entity != mySelectedElement && documentation == null) {
      return null;
    }
    DocumentationButton button =
      new DocumentationButton((ExtensibleElements) entity, documentation);

    ComponentsDescriptor descriptor = new ComponentsDescriptor();
    descriptor.add(button, ComponentsDescriptor.RIGHT_TB);

    return new Decoration(new Descriptor[]{descriptor});
  }

  public void selectionChanged(BpelEntity oldSelection, BpelEntity newSelection) {
//out("selection changed");
    if (newSelection instanceof ExtensibleElements) {
      mySelectedElement = (ExtensibleElements) newSelection;
    }
    else {
      mySelectedElement = null;
    }
    fireDecorationChanged();
  }

  @Override
  public void release() {
    mySelectedElement = null;
    getDesignView().getSelectionModel().removeSelectionListener(this);
  }

  private String getDocumentation(BpelEntity entity) {
    if ( !(entity instanceof ExtensibleElements)) {
      return null;
    }
    return ((ExtensibleElements) entity).getDocumentation();
  }

  private ExtensibleElements mySelectedElement;
}
