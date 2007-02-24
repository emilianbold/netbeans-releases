/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * UMLProjectMetadataListener.java
 *
 * Created on April 30, 2005, 7:51 AM
 */

package org.netbeans.modules.uml.project;

import org.netbeans.modules.uml.project.ui.customizer.UMLProjectProperties;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectListener;

/**
 *
 * @author Administrator
 */
public class UMLProjectMetadataListener implements PropertyChangeListener,AntProjectListener
{
   private UMLProject mAssociatedProject = null;
   
   /** Creates a new instance of UMLProjectMetadataListener */
   public UMLProjectMetadataListener(UMLProject project)
   {
      mAssociatedProject = project;
   }

   /**
    * React to changes originating from customizer modification to project metadata
    */
    public void propertyChange(PropertyChangeEvent evt)
    {
       if(UMLProjectProperties.MODELING_MODE.equals(evt.getPropertyName()) == true)
       {
          mAssociatedProject.setProjectMode((String)evt.getNewValue());
       }
    }
    
    /**
     * React to changes originating from customizer modification to project metadata
     */
    public void configurationXmlChanged(AntProjectEvent ev)
    {
    }
    
    public void propertiesChanged(AntProjectEvent ev)
    {
       //Handled by propertyChange
    }
   
}
