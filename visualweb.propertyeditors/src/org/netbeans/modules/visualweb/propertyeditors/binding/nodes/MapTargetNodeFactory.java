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
package org.netbeans.modules.visualweb.propertyeditors.binding.nodes;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Map;
import com.sun.rave.designtime.DesignBean;
import org.netbeans.modules.visualweb.propertyeditors.binding.BindingTargetNode;
import org.netbeans.modules.visualweb.propertyeditors.binding.BindingTargetNodeFactory;
import org.netbeans.modules.visualweb.propertyeditors.binding.PropertyBindingHelper;
import org.netbeans.modules.visualweb.propertyeditors.util.Bundle;

public class MapTargetNodeFactory implements BindingTargetNodeFactory {

    private static final Bundle bundle = Bundle.getBundle(MapTargetNodeFactory.class);

    public boolean supportsTargetClass(Class targetClass) {
        return Map.class.isAssignableFrom(targetClass);
    }

    public BindingTargetNode createTargetNode(BindingTargetNode parent, DesignBean bean, PropertyDescriptor[] propPath, Object propInstance) {
        return new MapTargetNode(parent, bean, propPath, propInstance);
    }

    public class MapTargetNode extends PropertyTargetNode {
        public MapTargetNode(BindingTargetNode parent, DesignBean bean, PropertyDescriptor[] propPath, Object propInstance) {
            super(parent, bean, propPath, propInstance);
        }
        public void lazyLoadCustomTargetNodes() {
            if (propInstance == null) {
                propInstance = PropertyBindingHelper.getPropInstance(bean, propPath);
            }
            if (propInstance instanceof Map) {
                Map map = (Map)propInstance;
                Object[] keys = map.keySet().toArray();
                for (int i = 0; i < keys.length; i++) {
                    if (keys[i] instanceof String) {
                        super.add(new KeyNode(this, bean, propPath, map, keys[i]));
                    }
                }
            }
        }

        public class KeyNode extends BindingTargetNode {
            protected DesignBean bean;
            protected PropertyDescriptor[] propPath;
            protected Map map;
            protected Object key;
            public KeyNode(BindingTargetNode parent, DesignBean bean, PropertyDescriptor[] propPath, Map map, Object key) {
                super(parent);
                this.bean = bean;
                this.propPath = propPath;
                this.map = map;
                this.key = key;
            }
            public boolean lazyLoad() {
//                try {
//                    BeanInfo bi = Introspector.getBeanInfo(getTargetTypeClass());
//                    PropertyDescriptor[] pds = bi.getPropertyDescriptors();
//                    for (int i = 0; pds != null && i < pds.length; i++) {
//                        if (pds[i].getReadMethod() != null) {
//                            PropertyDescriptor[] newPath = new PropertyDescriptor[propPath.length + 1];
//                            System.arraycopy(propPath, 0, newPath, 0, propPath.length);
//                            newPath[newPath.length - 1] = pds[i];
//                            super.add(_createTargetNode(this, bean, newPath, null));
//                        }
//                    }
//                }
//                catch (Exception x) {
//                    x.printStackTrace();
//                }
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
