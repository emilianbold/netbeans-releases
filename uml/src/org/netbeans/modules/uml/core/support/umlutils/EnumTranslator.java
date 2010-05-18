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

package org.netbeans.modules.uml.core.support.umlutils;

import java.util.StringTokenizer;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class EnumTranslator implements IEnumTranslator{
  public EnumTranslator() {
  }

  /**
   * Interpret the enumeration value
   *
   *
   * @param enumNum[in]					The value to look up in the enumeration
   * @param enumStrs[in]					A "|" delimited string of enumeration values
   * @param value[out]						The enumeration value that matches the enumNum
   * @param enumValues[in, optional]	A "|" delimited string representing the numeric values of the items
   *												in the enumeration, if not provided, assume it starts with 0, and is
   *												sequential.
   *
   * @return HRESULT
   */
  public String translateFromEnum( int enumNum, String enumStrs, String enumValues )
  {
    //
    // enumStrings is a list of "|" delimited strings that represent the values of the enumeration
    // eg. public|private|protected
    String retVal = null;
    //
    // If enumValues is empty, we will assume that the starting enumeration value equates to 0, and
    // is sequential
    //
    if (enumValues == null || enumValues.length() == 0)
    {
      //
      // we need break up the list and figure out based on the enumNum
      // which string should be displayed
      //
      //if (enumStrs == null || enumStrs.length() > 0)
      {
        int pos = enumStrs.indexOf("|");
        if (pos < 0)
        {
          retVal = enumStrs;
        }
        else
        {
        	StringTokenizer tokenizer = new StringTokenizer(enumStrs, "|");
        	int i=0;
        	while (tokenizer.hasMoreTokens())
        	{
        		String token = tokenizer.nextToken();
        		if (i == enumNum)
        		{
        			retVal = token;
        			break;
        		}
        		i++;
        	}
        }
      }
    }
    else
    {
      // the enumeration values are not sequential starting from 0
      // so we first need to find the passed in enumNum in the enumValues string
      if (enumValues.indexOf("|") >= 0)
      {
		int pos = 0;
		//String[] vals = enumValues.split("|");
                StringTokenizer tokenizer = new StringTokenizer(enumValues, "|");
                int count = tokenizer.countTokens();
		if (count > 0)
		{
		  for(int i=0; i < count; i++)
		  {
			String str = tokenizer.nextToken();
			if (Integer.parseInt(str) == enumNum)
			{
			  break;
			}
			pos++;
		  }
		}

                // With JDK 1.5 there seems to be a bug with using the "|" as 
                // a delimiter in the string split method.  So, I will do the 
                // split myself.
		// String[] strs = enumStrs.split("|");                
                StringTokenizer strTokenizer = new StringTokenizer(enumStrs, "|");
                
                int strCount = strTokenizer.countTokens();
                String[] strs = new String[strCount];
                for(int index = 0; index < strCount; index++)
                {
                    strs[index] = strTokenizer.nextToken();
                }
                
		if (strs != null && strs.length > pos)
		{
		  retVal = strs[pos];
		}
      }
      else
      {
      	retVal = enumStrs;
      }
    }
    return retVal;
  }

  /**
   * Determine the enumeration value
   *
   *
   * @param value[in]						The string to find in the enumeration
   * @param enumStrs[in]					A "|" delimited string of enumeration values
   * @param enumNum[out]					The value in the enumeration
   * @param enumValues[in, optional]	A "|" delimited string representing the numeric values of the items
   *												in the enumeration, if not provided, assume it starts with 0, and is
   *												sequential.
   *
   * @return HRESULT
   */
  public int translateToEnum( String value, String enumStrs, String enumValues )
  {
    int retEnum = 0;
    //
    // enumStrings is a list of "|" delimited strings that represent the values of the enumeration
    // eg. public|private|protected
    //
    // If enumValues is empty, we will assume that the starting enumeration value equates to 0, and
    // is sequential
    //
    if (enumValues != null && enumValues.length() == 0)
    {
      String[] values = enumValues.split("|");
      if (values != null && values.length >0 )
      {
        for (int i=0; i<values.length; i++)
        {
          String str = values[i];
          if (str.equals(value))
          {
            retEnum = i;
            break;
          }
        }
      }
    }
    else
    {
      // the enumeration values are not sequential starting from 0
      // so we first need to find the passed in value in the enumValues string
      StringTokenizer tokenizer = new StringTokenizer(enumStrs, "|");
      int count = tokenizer.countTokens();
      
//      String[] strs = enumStrs.split("|");
//      if (strs != null && strs.length>0)
      {
        int pos = 0;
//        for (int i=0; i<strs.length; i++)
		while (tokenizer.hasMoreTokens())
        {
          String str = tokenizer.nextToken();//strs[i];
          if (str.equals(value))
          {
            break;
          }
          pos++;
        }
        
        if (enumValues != null)
        {
			StringTokenizer tokenizer2 = new StringTokenizer(enumValues, "|");
			//String[] values = enumValues.split("|");
			//if (values != null && values.length >= pos )
			if (tokenizer2.countTokens() >= pos)
			{
				int j = 0;
				while (tokenizer2.hasMoreTokens())
				{
					String toke = tokenizer2.nextToken();
					if (j == pos)
					{
						retEnum = Integer.parseInt(toke);
						break;
					}
					j++;
				}
			  //retEnum = Integer.parseInt(values[pos]);
			}
        }
        else
        {
        	retEnum = pos;
        }
      }
    }
    return retEnum;
  }
}
