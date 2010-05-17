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
package org.netbeans.modules.visualweb.faces.dt.binding;

import java.awt.*;
import com.sun.rave.designtime.*;
import com.sun.rave.designtime.impl.*;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;

public class BindingsCustomizer extends BasicCustomizer2 {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(BindingsCustomizer.class);

    public BindingsCustomizer() {
        setDisplayName(bundle.getMessage("propBindings")); //NOI18N
        setApplyCapable(false);
    }
    public BindingsCustomizer(DesignBean designBean) {
        this();
        this.designBean = designBean;
        setDisplayName(bundle.getMessage("propBindingsPattern", designBean.getInstanceName())); //NOI18N
    }
    public BindingsCustomizer(DesignProperty prop) {
        this(prop.getDesignBean());
        this.prop = prop;
        setDisplayName(bundle.getMessage("propBindingPattern", designBean.getInstanceName(), prop.getPropertyDescriptor().getName())); //NOI18N
    }

    protected DesignProperty prop = null;
    protected BindingPanel panel = null;
    public Component getCustomizerPanel(DesignBean bean) {
        panel = new BindingPanel();
        if (prop != null) {
            panel.setSourceProperty(prop);
        }
        else {
            panel.setSourceBean(bean);
        }
        panel.setCustomizer(this);
        return panel;
    }

    public Result applyChanges() {
//        if (panel != null) {
//            panel.doApplyExpr();
//        }
        return Result.SUCCESS;
    }

    public boolean isModified() {
        if (panel != null) {
            return panel.isModified();
        }
        return super.isModified();
    }
}
