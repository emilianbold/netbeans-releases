/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
