/*
 * WSDesignNavigatorHint.java
 *
 * Created on April 9, 2007, 6:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.design.navigator;

import org.netbeans.spi.navigator.NavigatorLookupHint;

/**
 *
 * @author rico
 */
public class WSDesignNavigatorHint implements NavigatorLookupHint{
    
    /** Creates a new instance of WSDesignNavigatorHint */
    public WSDesignNavigatorHint() {
    }
    
    public String getContentType() {
        return "webservice/design"; // NOI18N
    }

}
