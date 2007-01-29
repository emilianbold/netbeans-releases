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

import javax.swing.SwingUtilities;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sun.rave.designtime.DisplayAction;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignInfo;
import com.sun.rave.designtime.Customizer2;
import com.sun.rave.designtime.CustomizerResult;
import com.sun.rave.designtime.markup.MarkupDesignBean;

/**
 * DesignInfo for the Jsp_Directive_Include component
 *
 * @author Tor Norbye
 */
public class Jsp_Directive_IncludeDesignInfo extends XhtmlDesignInfo {

    public Class getBeanClass() {
        return Jsp_Directive_Include.class;
    }

    public Result beanCreatedSetup(DesignBean bean) {
        return new CustomizerResult(bean, new FragmentCustomizer("file")); //NOI18N
    }

    public DisplayAction[] getContextItems(DesignBean bean) {
        return new DisplayAction[] {
            new FragmentCustomizerAction(bean),
        };
    }

    public Result beanDeletedCleanup(DesignBean bean) {
        DesignBean p = bean.getBeanParent();
        java.lang.Object pi = p.getInstance();

        if (pi instanceof Div) {
            final DesignContext context = bean.getDesignContext();
            final String pin = p.getInstanceName();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    DesignBean pb = context.getBeanByName(pin);
                    if (pb != null) {
                        java.lang.Object pbi = pb.getInstance();
                        if ((pbi instanceof Div) &&
                            pb.getChildBeanCount() == 0) {

                            context.deleteBean(pb);
                        }
                    }
                }
            });
        }
        return Result.SUCCESS;
    }
}
