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
 * ModelElementNewType.java
 *
 * Created on January 19, 2005, 2:29 PM
 */

package org.netbeans.modules.uml.project.ui;

import java.io.IOException;
import org.openide.util.datatransfer.NewType;

/**
 * Describes a modeling meta data type that can be created.
 *
 * @author  Trey Spiva
 */
public class ModelElementNewType extends NewType
{
   private String mMetaName = "";

   /**
    * Creates a new ModelElementNewType.  The NewType instance will be able to
    * create a new Modeling Meta Data instance for the specified type.
    *
    * @param name The meta data type to be created.
    */
   public ModelElementNewType(String name)
   {
      mMetaName = name;
   }
   
   /**
    * Display name for the creation action. This should be presented as an 
    * item in a menu.
    */
   public String getName()
   {
      return mMetaName;
   }
   
   /**
    * Create the object.  The type that is created is model element meta datatype.
    */
   public void create() throws IOException
   {
      
   }
   
}
