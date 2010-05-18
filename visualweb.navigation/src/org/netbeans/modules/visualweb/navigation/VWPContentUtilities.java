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
/*
 * VWPContentUtilities.java
 *
 * Created on April 15, 2007, 10:50 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.visualweb.navigation;

import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;

/**
 *
 * @author joelle
 */
public class VWPContentUtilities {
    
     
    
    public static final int BUTTON = 1;
    public static final int HYPERLINK = 2;
    public static final int IMAGE_HYPERLINK = 3;
    
    private static final String buttonClass_bh = "com.sun.rave.web.ui.component.Button";
    private static final  String hyperlinkClass_bh = "com.sun.rave.web.ui.component.Hyperlink";
    private static final  String imageHyperlinkClass_bh = "com.sun.rave.web.ui.component.ImageHyperlink";

    private static final  String buttonClass_ws = "com.sun.webui.jsf.component.Button";
    private static final  String hyperlinkClass_ws = "com.sun.webui.jsf.component.Hyperlink";
    private static final  String imageHyperlinkClass_ws = "com.sun.webui.jsf.component.ImageHyperlink";

    public final static String getBeanClassName(String javaeePlatform, int type) {
        switch (type){
        case BUTTON:
            if ((javaeePlatform != null) && JsfProjectUtils.JAVA_EE_5.equals(javaeePlatform)){
                return buttonClass_ws;
            }else{
                return buttonClass_bh;
            }
        case HYPERLINK:
            if ((javaeePlatform != null) && JsfProjectUtils.JAVA_EE_5.equals(javaeePlatform)){
                return hyperlinkClass_ws;
            }else{
                return hyperlinkClass_bh;
            }
        case IMAGE_HYPERLINK:
            if ((javaeePlatform != null) && JsfProjectUtils.JAVA_EE_5.equals(javaeePlatform)){
                return imageHyperlinkClass_ws;
            }else{
                return imageHyperlinkClass_bh;
            }
        }
        return null;
    }
    
}
