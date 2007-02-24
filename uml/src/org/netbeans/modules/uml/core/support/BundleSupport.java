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
 * BundleSupport.java
 *
 * Created on June 25, 2004, 9:45 AM
 */

package org.netbeans.modules.uml.core.support;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 *
 * @author  Trey Spiva
 */
public class BundleSupport
{
   private ResourceBundle m_Bundle = null;

   /** Creates a new instance of BundleSupport */
   public BundleSupport(String bundle)
   {
      m_Bundle = ResourceBundle.getBundle(bundle);
   }

   public String getString(String key)
   {
      return m_Bundle.getString(key);
   }

   public String getString(String key, Object[] arguments)
   {
      String pattern = getString(key);
      
      return MessageFormat.format(pattern, arguments);
   }
   
   public String getString(String key, Object arg)
   {
      String pattern = getString(key);
      
      Object[] arguments = { arg };
      return MessageFormat.format(pattern, arguments);
   }
   
   public String getString(String key, Object arg1, Object arg2)
   {
      String pattern = getString(key);
      
      Object[] arguments = { arg1, arg2 };
      return MessageFormat.format(pattern, arguments);
   }
   
}
