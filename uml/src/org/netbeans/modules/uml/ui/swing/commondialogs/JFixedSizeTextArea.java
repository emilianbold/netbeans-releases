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
