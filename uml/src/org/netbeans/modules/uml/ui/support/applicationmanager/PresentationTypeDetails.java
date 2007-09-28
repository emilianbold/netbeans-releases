/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
