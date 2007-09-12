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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.bpel.model.api.BpelEntity;

import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.decoration.Decoration;
import org.netbeans.modules.bpel.design.decoration.DecorationProvider;
import org.netbeans.modules.bpel.design.decoration.DecorationProviderFactory;
import org.netbeans.modules.bpel.design.decoration.GlowDescriptor;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.12.06
 */
public final class DiagramDecorator extends DecorationProvider
  implements DecorationProviderFactory
{
  /**{@inheritDoc}*/
  public DecorationProvider createInstance(DesignView view) {
    return new DiagramDecorator(view);
  }

  /**{@inheritDoc}*/
  public DiagramDecorator() {}

  private DiagramDecorator(DesignView view) {
    super(view);
    myHighlightedEntities = new ArrayList<BpelEntity>();
  }

  /**{@inheritDoc}*/
  @Override
  public Decoration getDecoration(BpelEntity component)
  {
    if ( !myIsClearSelection && mySelectedEntity == component) {
      return GREEN_DECORATION; // glow
    }
    if ( !myIsClearHighlighting && myHighlightedEntities.contains(component)) {
      return YELLOW_DECORATION; // highlight
    }
    return null;
  }
  
  /**{@inheritDoc}*/
  @Override
  public void release()
  {
    mySelectedEntity = null;
    myHighlightedEntities = null;
  }

  void select(Component component) {
    if ( !(component instanceof BpelEntity)) {
      return;
    }
    BpelEntity entity = (BpelEntity) component;

    if (mySelectedEntity != null) {
      myIsClearSelection = true;
      fireDecorationChanged(mySelectedEntity);
    }
    myIsClearSelection = false;
    mySelectedEntity = entity;
    fireDecorationChanged(mySelectedEntity);
  }

  void clearHighlighting() {
    myIsClearHighlighting = true;
    myIsClearSelection = true;

    for (BpelEntity entity : myHighlightedEntities) {
      fireDecorationChanged(entity);
    }
    myHighlightedEntities = new ArrayList<BpelEntity>();

    myIsClearHighlighting = false;
    myIsClearSelection = false;
  }

  void highlight(Component component, boolean highlighted) {
    if ( !(component instanceof BpelEntity)) {
      return;
    }
    BpelEntity entity = (BpelEntity) component;

    if (highlighted) {
      myHighlightedEntities.add(entity);
    }
    else {
      myHighlightedEntities.remove(entity);
    }
    myIsClearSelection = !highlighted;
    fireDecorationChanged(entity);
  }

  static DiagramDecorator getDecorator(DesignView view) {
    List<DecorationProvider> providers = view.getDecorationManager().getProviders();

    for (DecorationProvider provider : providers) {
      if (provider instanceof DiagramDecorator) {
        return (DiagramDecorator) provider;
      }
    }
    return null;
  }

  private boolean myIsClearSelection;
  private boolean myIsClearHighlighting;
  private BpelEntity mySelectedEntity;
  private List<BpelEntity> myHighlightedEntities;

  private static final Decoration GREEN_DECORATION =
    new Decoration(new GlowDescriptor(new Color(56, 216, 120), 20));

  private static final Decoration YELLOW_DECORATION =
    new Decoration(new GlowDescriptor(new Color(255, 255, 0), 20));
}
