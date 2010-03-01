/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.form.layoutsupport;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.form.RADProperty;

/**
 * This class is used internally to provide default support for any layout
 * manager class. The layout manager is handled as a JavaBean, no component
 * constraints are supported, as well as no drag&drop and no arranging
 * features.
 *
 * @author Tomas Pavek
 */

class DefaultLayoutSupport extends AbstractLayoutSupport {

    private Class layoutClass;

    public DefaultLayoutSupport(Class layoutClass) {
        this.layoutClass = layoutClass;
    }

    @Override
    public Class getSupportedClass() {
        return layoutClass;
    }

    @Override
    public void addComponentsToContainer(Container container,
                                         Container containerDelegate,
                                         Component[] components,
                                         int index)
    {
        // for better robustness catch exceptions that might occur because
        // the default support does not deal with constraints
        try {
            super.addComponentsToContainer(container,
                                           containerDelegate,
                                           components,
                                           index);
        }
        catch (RuntimeException ex) { // just ignore
            ex.printStackTrace();
        }
    }

    /**
     * Derives changed properties from the instance in the meta layout.
     * The instance in the meta layout is the default instance from
     * the container. This can differ from the default instance of the layout class.
     * For example, VerticalLayout in SwingX library has gap property.
     * The default value of this property is 0, but the default value
     * of gap property of layout obtained from the default instance
     * of JXTaskContainer is 14.
     * 
     * @param metaLayout information about the layout.
     */
    @Override
    protected void deriveChangedPropertiesFromInstance(MetaLayout metaLayout) {
        Map<String,Object> map = new HashMap<String,Object>();
        for (RADProperty prop : metaLayout.getAllBeanProperties()) {
            if (prop.canRead() && prop.canWrite()) {
                try {
                    map.put(prop.getName(), prop.getValue());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        try {
            metaLayout.setInstance(createDefaultLayoutInstance());
            for (RADProperty prop : metaLayout.getAllBeanProperties()) {
                if (prop.canRead() && prop.canWrite() && map.containsKey(prop.getName())) {
                    try {
                        prop.setValue(map.get(prop.getName()));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Cloning method - creates a new instance of this layout support, just
     * not initialized yet.
     * @return new instance of this layout support
     */
    @Override
    protected AbstractLayoutSupport createLayoutSupportInstance() {
        return new DefaultLayoutSupport(layoutClass);
    }
}
