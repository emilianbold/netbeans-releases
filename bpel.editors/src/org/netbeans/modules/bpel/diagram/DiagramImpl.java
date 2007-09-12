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
package org.netbeans.modules.bpel.diagram;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JComponent;

import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.model.patterns.CompositePattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;

import org.netbeans.modules.bpel.editors.api.Diagram;
import org.netbeans.modules.bpel.editors.api.DiagramElement;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.01.11
 */
public final class DiagramImpl implements Diagram {

  public DiagramImpl(DesignView view) {
    myView = view;
  }

  public JComponent getComponent() {
    return myView;
  }

  public List<DiagramElement> getElements(boolean useSelection) {
    DiagramModel model = myView.getModel();
    List<DiagramElement> elements = new ArrayList<DiagramElement>();

    travel(getRoot(model, useSelection), elements, ""); // NOI18N

    return elements;
  }

  private void travel(Pattern pattern, List<DiagramElement> elements, String indent) {
    if (pattern == null) {
      return;
    }
    travel(pattern.getElements(), elements, indent);

    if (pattern instanceof CompositePattern) {
      CompositePattern composite = (CompositePattern) pattern;
      travel(composite.getBorder(), elements, indent);
      Collection<Pattern> patterns = composite.getNestedPatterns();

      for (Pattern patern : patterns) {
        travel(patern, elements, indent + "  "); // NOI18N
      }
    }
  }

  private void travel(Collection<VisualElement> visual, List<DiagramElement> elements, String indent) {
    for (VisualElement element : visual) {
      travel(element, elements, indent);
    }
  }

  private void travel(VisualElement element, List<DiagramElement> elements, String indent) {
    if (element != null) {
//out(indent + " see: " + element);
      elements.add(new DiagramElementImpl(element));
    }
  }

  private Pattern getRoot(DiagramModel model, boolean useSelection) {
    if (useSelection) {
      return model.getView().getSelectionModel().getSelectedPattern();
    }
    return model.getRootPattern();
  }

  public void clearHighlighting() {
    DiagramDecorator.getDecorator(myView).clearHighlighting();
  }

  private void out() {
    System.out.println();
  }

  private void out(Object object) {
    System.out.println(object);
  }

  private DesignView myView;
}
