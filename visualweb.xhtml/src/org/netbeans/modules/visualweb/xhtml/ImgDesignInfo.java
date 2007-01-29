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
import com.sun.rave.designtime.Customizer2;
import com.sun.rave.designtime.CustomizerResult;
// URL Customizer should come from property editors - Winston
//<POST_MIGRATION>
//import com.sun.rave.std.URLCustomizer;
//</POST_MIGRATION>
import org.netbeans.modules.visualweb.xhtml.*;

/** DesignInfo for the Img component
 *
 * @author Tor Norbye
 */
public class ImgDesignInfo extends XhtmlDesignInfo {

    public Class getBeanClass() {
        return Img.class;
    }

    public Result beanCreatedSetup(DesignBean bean) {
        super.beanCreatedSetup(bean);
        //<POST_MIGRATION>
        //URLCustomizer customizer = new URLCustomizer();
        //return new CustomizerResult(bean, customizer);
        return CustomizerResult.SUCCESS;
        //</POST_MIGRATION>
    }
}
