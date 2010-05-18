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

import java.beans.*;
import java.util.*;
import javax.swing.tree.*;
import com.sun.rave.designtime.*;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;

public class MapTargetNodeFactory implements TargetNodeFactory {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(MapTargetNodeFactory.class);

    public boolean supportsTargetClass(Class targetClass) {
        return Map.class.isAssignableFrom(targetClass);
    }

    public BindingTargetNode createTargetNode(DefaultTreeModel treeModel, DesignBean bean, PropertyDescriptor[] propPath, Object propInstance) {
        return new MapTargetNode(treeModel, bean, propPath, propInstance);
    }

    public class MapTargetNode extends BindingTargetNode.PropertyTargetNode {
        public MapTargetNode(DefaultTreeModel treeModel, DesignBean bean, PropertyDescriptor[] propPath, Object propInstance) {
            super(treeModel, bean, propPath, propInstance);
        }
        public void lazyLoadCustomTargetNodes() {
            if (propInstance == null) {
                propInstance = getPropInstance(bean, propPath);
            }
            if (propInstance instanceof Map) {
                Map map = (Map)propInstance;
                Object[] keys = map.keySet().toArray();
                for (int i = 0; i < keys.length; i++) {
                    if (keys[i] instanceof String) {
                        super.add(new KeyNode(treeModel, bean, propPath, map, keys[i]));
                    }
                }
            }
        }

        public class KeyNode extends BindingTargetNode {
            protected DesignBean bean;
            protected PropertyDescriptor[] propPath;
            protected Map map;
            protected Object key;
            public KeyNode(DefaultTreeModel treeModel, DesignBean bean, PropertyDescriptor[] propPath, Map map, Object key) {
                super(treeModel);
                this.bean = bean;
                this.propPath = propPath;
                this.map = map;
                this.key = key;
            }
            public boolean lazyLoad() {
                try {
                    BeanInfo bi = Introspector.getBeanInfo(getTargetTypeClass());
                    PropertyDescriptor[] pds = bi.getPropertyDescriptors();
                    for (int i = 0; pds != null && i < pds.length; i++) {
                        if (pds[i].getReadMethod() != null) {
                            PropertyDescriptor[] newPath = new PropertyDescriptor[propPath.length + 1];
                            System.arraycopy(propPath, 0, newPath, 0, propPath.length);
                            newPath[newPath.length - 1] = pds[i];
                            super.add(_createTargetNode(treeModel, bean, newPath, null));
                        }
                    }
                }
                catch (Exception x) {
//                    x.printStackTrace();
                }
                return true;
            }
            public boolean isValidBindingTarget() {
                return true;
            }
            public String getBindingExpressionPart() {
                return "" + key; //NOI18N
            }
            public Class getTargetTypeClass() {
                return map.get(key).getClass();
            }
            public String getDisplayText(boolean enableNode) {
                String tn = getTargetTypeDisplayName();
                StringBuffer sb = new StringBuffer();
                sb.append("<html>");  //NOI18N
                if (!enableNode) {
                    sb.append("<font color=\"gray\">");  //NOI18N
                }
                sb.append(bundle.getMessage("key"));  //NOI18N
                sb.append(" ");  //NOI18N
                if (enableNode) {
                    sb.append("<b>");  //NOI18N
                }
                sb.append(key);
                if (enableNode) {
                    sb.append("</b>");  //NOI18N
                }
                sb.append(" &nbsp; <font size=\"-1\"><i>");  //NOI18N
                sb.append(tn);
                sb.append("</i></font>");  //NOI18N
                if (!enableNode) {
                    sb.append("</font>");  //NOI18N
                }
                sb.append("</html>");  //NOI18N
                return sb.toString();
            }
        }
    }
}
