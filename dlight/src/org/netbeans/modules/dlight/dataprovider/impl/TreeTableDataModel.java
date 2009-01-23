/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.dataprovider.impl;

import org.netbeans.modules.dlight.dataprovider.api.DataModelScheme;

/**
 *
 * @author masha
 */
public class TreeTableDataModel extends DataModelScheme{
 public static final TreeTableDataModel instance = new TreeTableDataModel();

  private TreeTableDataModel() {
    super("model:tree:table");
  }
}
