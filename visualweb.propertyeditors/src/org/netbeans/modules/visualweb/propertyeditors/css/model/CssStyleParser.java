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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.propertyeditors.css.model;

import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.markup.MarkupDesignBean;
import com.sun.rave.designtime.markup.MarkupDesignContext;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Parser to parse the Style properties
 * @author  Winston Prakash
 */
public class CssStyleParser {
    
    CssStyleData cssStyleDate = null;
   
    DesignProperty designProperty = null;
    
    public CssStyleParser(){
        this(new CssStyleData());
    }
    
    public CssStyleParser(CssStyleData styleDate){
        this.cssStyleDate = styleDate;
    }
    
    public CssStyleParser(CssStyleData styleDate, DesignProperty designProperty){
        this.cssStyleDate = styleDate;
        this.designProperty = designProperty;
    }
    
    /**
     * Parse the CSS Style String in to separate CSS Style properties
     */
    public  CssStyleData parse(String cssStyleString){
        // If the design property is not null, then parse the CSS style String
        // using  DesignContext.convertCssStyleToMap
        if(designProperty != null){
            cssStyleDate.setDesignProperty(designProperty);
            MarkupDesignBean liveBean = (MarkupDesignBean)designProperty.getDesignBean();
            MarkupDesignContext liveContext = (MarkupDesignContext) liveBean.getDesignContext();
            Map styleMap = liveContext.convertCssStyleToMap(cssStyleString);
            for (Iterator iter = styleMap.keySet().iterator(); iter.hasNext();){
                String propertyName = (String) iter.next();
                String propertyValue = (String) styleMap.get(propertyName);           
                cssStyleDate.addProperty(propertyName, propertyValue);
            }
        }else{
            cssStyleString = cssStyleString.replaceAll("&quot;", "\""); //NOI18N
            StringTokenizer styleProperties = new StringTokenizer(cssStyleString,";"); //NOI18N
            while(styleProperties.hasMoreTokens()){
                String styleProperty = styleProperties.nextToken();
                String propertyName = styleProperty.substring(0,styleProperty.indexOf(":")).trim(); //NOI18N
                String propertyValue = styleProperty.substring(styleProperty.indexOf(":")+1).trim(); //NOI18N
                cssStyleDate.addProperty(propertyName, propertyValue);
            }
        }
        return cssStyleDate;
    }
    
    public static void main(String[] args){
        String cssStyleString = "background-color: red; border-left-color: rgb(255, 204, 204); border-right-color: rgb(255, 204, 204); border-top-color: rgb(255, 255, 102); border-bottom-color: rgb(255, 204, 204); border-left-style: double; border-right-style: double; border-top-style: solid; border-bottom-style: double; border-left-width: 44px; border-right-width: 44px; border-top-width: 44px; border-bottom-width: 44px; margin-left: 10px; margin-right: 10px; margin-top: 10px; margin-bottom: 10px; left: 72px; top: 48px; padding-left: 10px; padding-right: 10px; padding-top: 10px; padding-bottom: 10px; position: absolute "; //NOI18N
        System.out.println(cssStyleString);
        CssStyleParser styleParser = new CssStyleParser();
        CssStyleData styleData = styleParser.parse(cssStyleString);
        System.out.println(styleData.toString());
    }
    
}
