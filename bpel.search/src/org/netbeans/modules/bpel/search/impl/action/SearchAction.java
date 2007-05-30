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
package org.netbeans.modules.bpel.search.impl.action;

import java.io.IOException;

import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.schema.model.SchemaModel;

import org.netbeans.modules.xml.xam.ui.ModelCookie;

import org.netbeans.modules.xml.xam.ui.search.api.SearchManagerAccess;
import org.netbeans.modules.xml.xam.ui.search.api.SearchTarget;
import org.netbeans.modules.bpel.search.impl.util.Util;

import static org.netbeans.modules.print.ui.PrintUI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.11.13
 */
public final class SearchAction extends NodeAction {

  @Override
  protected void performAction(Node [] nodes)
  { 
    performAction(nodes [0]);
  }

  private static synchronized void performAction(Node node) {
    Model model = getModel(node);
    SearchManagerAccess.getManager().getUI(model, getTargets(model), null, true);
  }

  private static SearchTarget [] getTargets(Model model) {
    if (model instanceof BpelModel) {
      return Target.BPEL;
    }
    if (model instanceof WSDLModel) {
      return Target.WSDL;
    }
    if (model instanceof SchemaModel) {
      return Target.SCHEMA;
    }
    return new SearchTarget [] {};
  }

  @Override
  protected boolean enable(Node [] nodes)
  {
    if (nodes == null || nodes.length != 1) {
      return false;
    }
    return getModel(nodes [0]) != null;
  }

  private static Model getModel(Node node) {
    Model model = node.getLookup().lookup(BpelModel.class);

    if (model != null) {
      return model;
    }
    DataObject data = getDataObject(node);

    if (data == null) {
      return null;
    }
    ModelCookie cookie = data.getCookie(ModelCookie.class);

    if (cookie == null) {
      return null;
    }
    try {
      return cookie.getModel();
    } 
    catch (IOException e) {
      return null;
    }
  }

  private static DataObject getDataObject(Node node) {
    return (DataObject) node.getLookup().lookup(DataObject.class);
  }

  @Override
  protected boolean asynchronous()
  {
    return false;
  }

  /**{@inheritDoc}*/
  @Override
  public String getName()
  {
    return i18n(SearchAction.class, "CTL_Search_Action"); // NOI18N
  }
  
  /**{@inheritDoc}*/
  @Override
  public HelpCtx getHelpCtx()
  {
    return HelpCtx.DEFAULT_HELP;
  }

  @Override
  protected String iconResource()
  {
    return "org/netbeans/modules/bpel/search/impl/util/image/search.gif"; // NOI18N
  }

  // -----------------------------------------------------------------------------------------
  public static final class Manager
    extends org.netbeans.modules.print.ui.PrintUI.NodeAction
  {
    /**{@inheritDoc}*/
    public Manager() {
      super(
        icon(Util.class, "search"), // NOI18N
        i18n(Manager.class, "TLT_Search_Action")); // NOI18N
    }

    @Override     
    protected void actionPerformed(Node node)
    {
      performAction(node);
    }
  }

  private org.netbeans.modules.bpel.search.impl.ui.Search mySearch;
}
