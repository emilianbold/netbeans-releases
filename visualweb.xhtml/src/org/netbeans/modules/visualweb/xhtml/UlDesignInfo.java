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

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import org.openide.util.NbBundle;
import org.netbeans.modules.visualweb.xhtml.*;

/** DesignInfo for the Ul component
 *
 * @author Tor Norbye
 */
public class UlDesignInfo extends XhtmlDesignInfo {

    public Class getBeanClass() {
        return Ul.class;
    }

    public Result beanCreatedSetup(DesignBean bean) {
        try {
            DesignContext context = bean.getDesignContext();
            // Add some initial items in the list
            for (int i = 0; i < 3; i++) {
                DesignBean li = context.createBean(Li.class.getName(), bean, null);
                addTextChild(li,
                             NbBundle.getMessage(XhtmlDesignInfo.class, "ListItem", new Integer(i))); // NOI18N
            }
        }
        catch (Exception x) {
            x.printStackTrace();
        }
        return Result.SUCCESS;
    }
}
