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

import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.NodeAction;
import static org.netbeans.modules.print.api.PrintUI.*;

import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.search.api.SearchManagerAccess;

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

  static synchronized void performAction(Node node) {
    SearchManagerAccess.getManager().getUI(getBpelModel(node), null, true);
  }

  @Override
  protected boolean enable(Node [] nodes)
  {
    if (nodes == null || nodes.length != 1) {
      return false;
    }
    return getBpelModel(nodes [0]) != null;
  }

  private static BpelModel getBpelModel(Node node) {
    DataObject dataObject = getDataObject(node);

    if (dataObject instanceof Lookup.Provider) {
      Lookup.Provider provider = (Lookup.Provider) dataObject;

      // hot fix for 100277. todo as search provider
      try {
        return (BpelModel) provider.getLookup().lookup(BpelModel.class);
      }
      catch (IllegalStateException e) {
        return null;
      }
    }
    return null;
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

  private org.netbeans.modules.bpel.search.impl.ui.Search mySearch;
}
