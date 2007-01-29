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

/** DesignInfo for the Hr component
 *
 * @author Tor Norbye
 */
public class HrDesignInfo extends XhtmlDesignInfo {

    public Class getBeanClass() {
        return Hr.class;
    }

    public Result beanCreatedSetup(DesignBean bean) {
        // Set size="1" and "width=100%" on it
        try {
            // I used to set the style attribute to width: 120px;
            // height: 120px here, but then if the user went to the
            // property sheet and set the "width" attribute, it would be
            // overridden by the style attribute! So set the width
            // directly and rely on the non css presentational attribute
            // support in the designer
            DesignProperty widthProp = bean.getProperty("width"); // NOI18N
            if (widthProp != null) {
                widthProp.setValue("100%"); // NOI18N
            }
            /*
            DesignProperty sizeProp = bean.getProperty("size"); // NOI18N
            if (sizeProp != null) {
                sizeProp.setValue("1"); // NOI18N
            }
            */
        }
        catch (Exception x) {
            x.printStackTrace();
        }
        return Result.SUCCESS;
    }
}
