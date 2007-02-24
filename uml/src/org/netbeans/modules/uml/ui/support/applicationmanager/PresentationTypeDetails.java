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



package org.netbeans.modules.uml.ui.support.applicationmanager;

/**
 * PresentationTypeDetails is used to specify the details about a presentation type.
 *
 * @author Trey Spiva
 */
public class PresentationTypeDetails
{
   private String m_MetaType   = "";
   private String m_EngineName = "";
   private int m_ObjectKind     = TSGraphObjectKind.TSGOK_INVALID; // TSGraphObjectKind

   public PresentationTypeDetails()
   {
   }

   public PresentationTypeDetails(String metaType, String engineName, int kind)
   {
      setMetaType(metaType);
      setEngineName(engineName);
      setObjectKind(kind);
   }
   /**
    * Retreives the name of the drawing engine that supports the 
    * presentation type.
    * 
    * @return The draw engine name.
    */
   public String getEngineName()
   {
      return m_EngineName;
   }

   /**
    * Sets the name of the drawing engine that supports the 
    * presentation type.
    * 
    * @return The draw engine name.
    * @param value The name of the draw engine.
    */
   public void setEngineName(String value)
   {
      m_EngineName = value;
   }

   /**
    * Retrieves The name of the metatype of the presentation type.  The 
    * metatype is the type of the model data that a presentation 
    * element represents.
    * 
    * @return The meta data type.
    */
   public String getMetaType()
   {
      return m_MetaType;
   }

   /**
    * Sets The name of the metatype of the presentation type.  The 
    * metatype is the type of the model data that a presentation 
    * element represents.
    * 
    * @param value The meta data type.
    */
   public void setMetaType(String value)
   {
      m_MetaType = value;
   }

   /**
    * Retrieves the type of the object.  The valid value are one of the 
    * TSGraphObjectKind values.
    * 
    * @return The object type.
    */
   public int getObjectKind()
   {
      return m_ObjectKind;
   }

   /**
    * Sets the type of the object.  The valid value are one of the 
    * TSGraphObjectKind values.
    * 
    * @param value The object type.
    */
   public void setObjectKind(int value)
   {
      m_ObjectKind = value;
   }

}
