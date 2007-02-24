/*
 * StaticDependencyStateHandler.java
 *
 * Created on January 11, 2007, 2:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.netbeans.modules.uml.core.reverseengineering.reframework.IDependencyEvent;

/**
 *
 * @author krichard
 */
public class StaticDependencyStateHandler extends DependencyStateHandler
{
   public StaticDependencyStateHandler(String language)
   {
      super(language);
   }
   
   protected IDependencyEvent buildDependencyEvent()
   {   
       createTokenDescriptor("Static Dependency", -1, -1, -1, "true", 0);
       IDependencyEvent event = super.buildDependencyEvent();
       
       return event;
   }
}
