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
