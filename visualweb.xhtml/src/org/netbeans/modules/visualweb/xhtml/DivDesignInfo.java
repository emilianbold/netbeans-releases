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
package org.netbeans.modules.visualweb.xhtml;

import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import org.netbeans.modules.visualweb.xhtml.*;

/** DesignInfo for the Div component
 *
 * @author Tor Norbye
 */
public class DivDesignInfo extends XhtmlDesignInfo {

    public Class getBeanClass() {
        return Div.class;
    }

    public Result beanCreatedSetup(DesignBean bean) {
        // Set width and height to 120 pixels
        if (bean.getChildBeanCount() == 0) {
            try {
                // set the style property
                DesignProperty styleProp = bean.getProperty("style"); // NOI18N
                if (styleProp != null) {
                    String size = "width: 120px; height: 120px"; // NOI18N
                    String style = (String)styleProp.getValue();
                    // Special case: don't override width already set
                    // Need to improve this a bit. Currently special cased
                    // in the designer to deal with jsp:includes wrapped in
                    // a <div> when dropped in a grid layout area
                    if (style != null && style.length() > 0) {
                        styleProp.setValue(style + "; " + size);
                    } else {
                        styleProp.setValue(size);
                    }
                }
            }
            catch (Exception x) {
                x.printStackTrace();
            }
        }
        return Result.SUCCESS;
    }
}
