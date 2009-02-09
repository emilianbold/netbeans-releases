/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.core.stack.api.impl;

import org.netbeans.modules.dlight.core.stack.api.FunctionMetric;
import org.netbeans.modules.dlight.core.stack.api.FunctionMetric.FunctionMetricConfiguration;

/**
 *
 * @author mt154047
 */
public abstract  class FunctionMetricAccessor {
  private static volatile FunctionMetricAccessor DEFAULT;

  public static FunctionMetricAccessor getDefault(){
    FunctionMetricAccessor a = DEFAULT;
    if (a!= null){
      return a;
    }

    try{
      Class.forName(FunctionMetric.class.getName(), true, FunctionMetric.class.getClassLoader());//
    }catch(Exception e){

    }
    return DEFAULT;
  }

  public static void setDefault(FunctionMetricAccessor accessor){
    if (DEFAULT != null){
      throw new IllegalStateException();
    }
    DEFAULT = accessor;
  }

  public FunctionMetricAccessor(){

  }

  public abstract FunctionMetric createNew(FunctionMetricConfiguration functionMetricConfiguration);
}
