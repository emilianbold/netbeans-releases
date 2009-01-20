/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.dlight.core.stack.model.support;

import org.netbeans.dlight.core.stack.model.support.impl.StackSupportStorageProvider;
import org.openide.util.Lookup;

/**
 * This class is responsible 
 * @author masha
 */
public final class StackSupportProvider {
  private static StackSupportProvider instance = null;
  private StackSupportStorageProvider stackStorageProvider = null;
  
  private StackSupportProvider(){
    stackStorageProvider = Lookup.getDefault().lookup(StackSupportStorageProvider.class);  
  }

  public static StackSupportProvider getInstance(){
    if (instance == null){
      instance = new StackSupportProvider();
    }
    return instance;
  }
 
  public final StackSupport stackSupport(){
    if (stackStorageProvider == null){
      return null;
    }
    return stackStorageProvider.stackSupport();
  }
}
;