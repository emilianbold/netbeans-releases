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

import javax.faces.component.UIColumn;
import javax.faces.component.html.HtmlOutputText;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.impl.BasicDisplayAction;

public class HtmlDataTableAddColumnAction extends BasicDisplayAction {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(
        HtmlDataTableAddColumnAction.class);

    protected DesignBean table;

    public HtmlDataTableAddColumnAction(DesignBean table) {
        super(bundle.getMessage("addCol")); //NOI18N
        this.table = table;
    }

    public Result invoke() {
        DesignContext context = table.getDesignContext();

        DesignBean column = context.createBean(UIColumn.class.getName(), table, null);
        DesignBean input = context.createBean(HtmlOutputText.class.getName(), column, null);
        DesignProperty vp = input.getProperty("value"); //NOI18N

        return Result.SUCCESS;
    }
}
