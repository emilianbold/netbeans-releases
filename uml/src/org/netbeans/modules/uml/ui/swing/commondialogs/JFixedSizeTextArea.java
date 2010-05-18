/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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



package org.netbeans.modules.uml.ui.swing.commondialogs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import javax.swing.JTextArea;
import javax.swing.text.Document;

/**
 *
 * @author Trey Spiva
 */
public class JFixedSizeTextArea extends JTextArea
{
   public JFixedSizeTextArea()
   {
      super();
   }

   public JFixedSizeTextArea(String text)
   {
      super(text);
   }

   public JFixedSizeTextArea(int rows, int columns)
   {
      super(rows, columns);
   }


   public JFixedSizeTextArea(String text, int rows, int columns)
   {
      super(text, rows, columns);
   }

   
   public JFixedSizeTextArea(Document doc)
   {
      super(doc);
   }

   /**
    * @param doc
    * @param text
    * @param rows
    * @param columns
    */
   public JFixedSizeTextArea(Document doc, String text, int rows, int columns)
   {
      super(doc, text, rows, columns);
   }

   /* (non-Javadoc)
    * @see javax.swing.text.JTextComponent#setText(java.lang.String)
    */
   public void setText(String t)
   {
      super.setText(t);
      
      setRows(getMaxRows(t));
      setColumns(getMaxCharacters(t));
      setVisible(true);
   }

   /**
    * Retreives the maximumn number of characters that are required to display 
    * the texts.
    * 
    * @param t The text that is to be displayed.
    * @return The number of characters.
    */
   protected int getMaxCharacters(String t)
   {
      int retVal = 0;
      
      BufferedReader reader = new BufferedReader(new StringReader(t));
      try
      {
         String line = reader.readLine();
         while(line != null)
         {
            if(line.length() > retVal)
            {
               retVal = line.length();
            }
            
            line = reader.readLine();
         }
      }
      catch (IOException e)
      {
      }
      
      return (retVal / 2) + 1;
   }
	/**
	 * Retreives the maximumn number of characters that are required to display 
	 * the texts.
	 * 
	 * @param t The text that is to be displayed.
	 * @return The number of characters.
	 */
	protected int getMaxRows(String t)
	{
		int retVal = 0;
      
		BufferedReader reader = new BufferedReader(new StringReader(t));
		try
		{
			String line = reader.readLine();
			while(line != null)
			{
				retVal++;
				line = reader.readLine();
			}
		}
		catch (IOException e)
		{
		}
		return retVal;
	}
   
   

}
