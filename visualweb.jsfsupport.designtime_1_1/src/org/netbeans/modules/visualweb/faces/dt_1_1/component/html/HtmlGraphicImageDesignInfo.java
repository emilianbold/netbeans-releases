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
package org.netbeans.modules.visualweb.faces.dt_1_1.component.html;

import com.sun.rave.designtime.*;
import org.netbeans.modules.visualweb.faces.dt.HtmlDesignInfoBase;
import org.netbeans.modules.visualweb.faces.dt.std.URLCustomizer;
import javax.faces.component.html.HtmlGraphicImage;

public class HtmlGraphicImageDesignInfo
    extends HtmlDesignInfoBase
    implements DesignInfo
{
    public Class getBeanClass() { return HtmlGraphicImage.class; }

    public Result beanCreatedSetup(DesignBean bean) {
        return new CustomizerResult(bean, new URLCustomizer());
    }

    public boolean acceptLink(DesignBean targetBean, DesignBean sourceBean, Class sourceClass) {
        return false;
    }

    /** Won't get called unless acceptLink() returns true
     */
    public Result linkBeans(DesignBean targetBean, DesignBean sourceBean) {
        return null;
    }

}
