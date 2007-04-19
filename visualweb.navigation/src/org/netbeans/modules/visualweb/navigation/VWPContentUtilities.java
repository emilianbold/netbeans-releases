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
