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



package org.netbeans.modules.uml.ui.controls.doccontrol;

import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;

/**
 * @author sumitabhk
 *
 */
public class DocUtils
{

	/**
	 *
	 */
	public DocUtils()
	{
		super();
	}

	/**
	 *
	 * ConvertToTags converts '\n' in string to <BR> and "\n\n" to temporary <P>,
	 *	and strips carriage returns.
	 *
	 *
	 * @param BSTR*[in] pbsText
	 *
	 * @return HRESULT
	 *
	 */
	public static String convertToTags(String text)
	{
		if (text == null || text.length() == 0)
			return "<html><head><head><body></body></html>";

		text = text.replaceAll("\n", "<br>");
		text = text.replaceAll("<div><br>", "<div>");
		text = text.replaceAll("<br>    </div><br>", "</div>\n");
		text = text.replaceAll("<br>    </div>", "</div>");
		text = text.replaceAll("</p><br>", "</div>");

		// // text = text.replaceAll("\r\n" , "");
		// int pos = text.indexOf('\r');
		// while (pos >= 0)
		// {
		// if(text.charAt(pos+1) == '\n')
		// {
		// //we found a \r\n sequence - replace it with <br>
		// text = text.substring(0, pos) + text.substring(pos+2);
		// }
		// else if (pos > 0 && text.charAt(pos-1) == '\n')
		// {
		// //we found a \n\r sequence - replace it with <br>
		// text = text.substring(0, pos-1) + "<br>" + text.substring(pos+1);
		// }
		// else
		// {
		// text = text.substring(0, pos) + "<br>" + text.substring(pos+1);
		// }
		//		
		// pos = text.indexOf('\r');
		// }
		//		
		// int index = text.indexOf('\n');
		// while(index >= 0)
		// {
		// if(text.charAt(index+1)=='<')
		// {
		// text = text.substring(0,index)+text.substring(index+1);
		// }
		// else if((index > 0) && index-3>=0 && text.indexOf("<p>")==index-3)
		// {
		// text = text.substring(0,index)+text.substring(index+1);
		// while(text.charAt(index)=='\n')
		// {
		// text = text.substring(0,index)+"<BR>"+text.substring(index+1);
		// index = index+4;
		// }
		// }
		// else if(text.charAt(index+1)==' ')
		// {
		// int inc = 1;
		// while(((index + inc) < text.length()) &&
		// (text.charAt(index+inc)==' '))
		// {
		// inc=inc+1;
		// }
		//		
		// if(((index + inc) >= text.length()) ||
		// (text.charAt(index+inc)!='<'))
		// {
		// text = text.substring(0,index)+"<BR>"+text.substring(index+1);
		// }
		// else
		// {
		// text = text.substring(0,index)+text.substring(index+1);
		// }
		// }
		// else if(((index+1) >= text.length() - 1) ||
		// (text.charAt(index+1) != '<'))
		// {
		// text = text.substring(0,index)+"<BR>"+text.substring(index+1);
		// }
		// else
		// {
		// text = text.substring(0,index)+text.substring(index+1);
		// }
		// index = text.indexOf('\n');
		// }
		// text = text.replaceAll(" "," ");
		return "<html><head><head><body>" + text + "</body></html>";
	}

	public static String convertFromTags(String text) {
            String retText = text;
            //this string should contain the html header and body tags too, we need to just return body tag
            if (text != null && text.length() > 0) {
                int pos = text.indexOf("<body>");
                if (pos >= 0) {
                    // int lastPos = text.indexOf("</body>");
                    int lastPos = text.lastIndexOf("</body>");
                    if (lastPos >= 0) {
                        retText = text.substring(pos + 6, lastPos);
                        retText = retText.replaceAll("</body>", "</div>");
                        retText = retText.replaceAll("<body", "<div");
                        // now replace all <br> with \n depending on the platform
                        int pos2 = retText.indexOf("<br>");
                        while (pos2 >= 0) {
                            retText = retText.substring(0, pos2) + "\n" + retText.substring(pos2+4);
                            pos2 = retText.indexOf("<br>");
                        }
                    }
                }
            }
            
            // Translate content
            int i = 0, j = 0;
            String start = "&#", end = ";", htmlStr = "", translatedStr = "";
            while (true) {
                //looking for pattern like &#261; to escape for chars.
                i = retText.indexOf(start);
                j = retText.indexOf(end,i);
                
                if (i > -1 && j > -1 && (i+1) < j) {
                    String tmp = retText.substring(i) ;
                    tmp = retText.substring(i,j);
                    htmlStr = retText.substring(i, j + 1);
                    char[] temp = new char[1];
                    temp[0] = (char) Integer.parseInt(retText.substring(i + 2, j));
                    translatedStr = new String(temp);
                    retText = StringUtilities.replaceSubString(retText, htmlStr, translatedStr);
                } else {
                    break;
                }
            }
            if (retText.startsWith("\n   "))
                retText = retText.substring(4);
            if (retText.endsWith("\n  "))
                retText = retText.substring(0,retText.length()-2);
            retText=retText.replaceAll(">\n      ",">");
            retText=retText.replaceAll("\n    <div","<div");
            retText = retText.replaceAll("\n    </div>", "</div>");
            retText = retText.replaceAll("</div>\n", "</div>");
            return retText;
        }

	public static String normalizeHTML(String text)
	{
		String result = text;
		String begin = "";
		String end = "";

		int first = text.indexOf("<body>");
		if (first > 0)
		{
			begin = text.substring(0, first + 6);
			int last = text.lastIndexOf("</body>");
			if (last>0)
			{
				end = text.substring(last, text.length());
				result = text.substring(first + 6, last);
			}
			else
			{
				result = text.substring(first + 6, text.length());
			}
			// result = result.replaceAll("\n", "<br>");
			result = result.replaceAll("<body align", "<div align");
			result = result.replaceAll("</body>", "</div>");
			result = begin + result + end;
		}
                
                result = result.trim();
                if(result.startsWith("<p") == true)
                {
                    int beginIndex = result.indexOf('>') + 1;
                    if(result.endsWith("</p>") == true)
                    {
                       result = result.substring(beginIndex, result.length() - 4); 
                    }
                    else
                    {
                        result = result.substring(beginIndex);
                    }
                }
		return result;
	}
}
