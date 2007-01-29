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
package org.netbeans.modules.visualweb.web.ui.dt.component.customizers;

import java.awt.Component;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.impl.BasicCustomizer2;
import org.netbeans.modules.visualweb.web.ui.dt.component.table.TableBindToDataPanel;
import org.netbeans.modules.visualweb.web.ui.dt.component.table.TableCustomizerMainPanel;
import org.netbeans.modules.visualweb.web.ui.dt.component.util.DesignMessageUtil;

/**
 * Customizer for the <code>Table</code> component.
 * @author Winston Prakash
 */

public class TableCustomizer extends BasicCustomizer2 {
    public TableCustomizer() {
        super(null, DesignMessageUtil.getMessage(TableCustomizerAction.class, "tableCustomizer.title"), null, null); //NOI18N
        setApplyCapable(true);
        setHelpKey("projrave_ui_elements_dialogs_data_table_layout_db"); //NOI18N
    }

    protected TableCustomizerMainPanel panel;

    public Component getCustomizerPanel(DesignBean bean) {
        this.setDisplayName(DesignMessageUtil.getMessage(TableCustomizerAction.class, "tableCustomizer.title") + " - " + bean.getInstanceName());
        panel = new TableCustomizerMainPanel(bean);
        return panel;
    }

    public boolean isModified() {
        if (panel != null) {
            return panel.isModified();
        }
        return false;
    }

    public Result applyChanges() {
        if (panel != null) {
            return panel.applyChanges();
        }
        return Result.SUCCESS;
    }
}
