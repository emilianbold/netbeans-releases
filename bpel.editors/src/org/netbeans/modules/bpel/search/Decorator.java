/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.search;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

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
public final class Decorator extends DecorationProvider implements DecorationProviderFactory {

  public Decorator() {}

  public DecorationProvider createInstance(DesignView view) {
    return new Decorator(view);
  }

  private Decorator(DesignView view) {
    super(view);
    myHighlightedEntities = new ArrayList<BpelEntity>();
  }

  @Override
  public Decoration getDecoration(BpelEntity entity) {
    if ( !myIsClearSelection && mySelectedEntity == entity) {
      return GREEN_DECORATION; // glow
    }
    if ( !myIsClearHighlighting && myHighlightedEntities.contains(entity)) {
      return YELLOW_DECORATION; // highlight
    }
    return null;
  }

  @Override
  public void release() {
    mySelectedEntity = null;
    myHighlightedEntities = null;
  }

  void select(BpelEntity entity) {
    if (mySelectedEntity != null) {
      myIsClearSelection = true;
    }
    myIsClearSelection = false;
    mySelectedEntity = entity;
    fireDecorationChanged();
  }

  void clearHighlighting() {
    myIsClearHighlighting = true;
    myIsClearSelection = true;

    fireDecorationChanged();
    
    myHighlightedEntities = new ArrayList<BpelEntity>();
    myIsClearHighlighting = false;
    myIsClearSelection = false;
  }

  void doHighlight(BpelEntity entity, boolean highlighted) {
    if (highlighted) {
      myHighlightedEntities.add(entity);
    }
    else {
      myHighlightedEntities.remove(entity);
    }
    myIsClearSelection = !highlighted;
    fireDecorationChanged();
  }

  static Decorator getDecorator(DesignView view) {
    List<DecorationProvider> providers = view.getDecorationManager().getProviders();

    for (DecorationProvider provider : providers) {
      if (provider instanceof Decorator) {
        return (Decorator) provider;
      }
    }
    return null;
  }
  private boolean myIsClearSelection;
  private boolean myIsClearHighlighting;
  private BpelEntity mySelectedEntity;
  private List<BpelEntity> myHighlightedEntities;
  private static final Decoration GREEN_DECORATION = new Decoration(new GlowDescriptor(new Color(56, 216, 120), 20));
  private static final Decoration YELLOW_DECORATION = new Decoration(new GlowDescriptor(new Color(255, 255, 0), 20));
}
