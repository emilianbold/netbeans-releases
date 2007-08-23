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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared;

import java.awt.Image;
import java.io.IOException;
import java.util.Collection;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.openide.filesystems.FileObject;

/**
 * This interfaces provides a strategy for working with component methods.
 * 
 * @author  rico
 * @author Chris Webster
 */
public interface ComponentMethodViewStrategy {
    
  /**
   * Get the badge for the method (if any) depending on its presence in the interfaces
   * 
   * @param me method from one of the interfaces in the collection
   * @param interfaces Collection of interfaces, one of which has me
   * @return Image of the appropriate badge or null 
   */  
  public Image getBadge(MethodModel me, Collection interfaces);
  
  /**
   * Get the icon for the method. The general implementation of this
   * method determines the icon based on the interface the method is present in
   * along with the signature.
   * 
   * @param me method from one of the collection intefaces
   * @param interfaces interfaces which represent the client
   * view of a component. 
   * @return Image, this method should not return null
   */
  public Image getIcon(MethodModel me, Collection interfaces);
  
  /**
   * Delete the method from the implementation class
   * 
   * @param me method from one of the interfaces in the collection
   * @param implClass Implementation class where the corresponding method will be deleted
   * @param implClassFO file object conataing implementation class; can be null (e.g. EJB from library)
   * @param interfaces Collection of interfaces, one of which has me
   */
  public void deleteImplMethod(MethodModel me, String implClass, FileObject implClassFO, Collection interfaces) throws IOException;
  
  /**
   * Open MethodElement in the implementation class
   * 
   * @param me method from one of the interfaces in the collection
   * @param implClass Implementation class where the corresponding method will be opened
   * @param implClassFO file object conataing implementation class; can be null (e.g. EJB from library)
   * @param interfaces Collection of interfaces, one of which has me
   * @return The OpenCookie of the corresponding method in the implementation class implClass.
   */
  void openMethod(MethodModel me, String implClass, FileObject implClassFO, Collection interfaces); 
  
}
