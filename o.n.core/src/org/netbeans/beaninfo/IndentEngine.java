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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo;

import java.beans.*;
import java.util.ResourceBundle;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
*
* @author jtulach
*/
public abstract class IndentEngine {
    private IndentEngine () {}
    /** Object that provides beaninfo for {@link DebuggerType.Default}.
    *
    * @author Jaroslav Tulach
    */
    public static class DefaultBeanInfo extends SimpleBeanInfo {

        public BeanDescriptor getBeanDescriptor () {
            Class c;
            try {
                c = Class.forName("org.openide.text.IndentEngine$Default"); // NOI18N
            } catch (Exception e) {
                throw new IllegalStateException();
            }
            BeanDescriptor descr = new BeanDescriptor (c);
            ResourceBundle bundle = NbBundle.getBundle(IndentEngine.class);

            descr.setDisplayName (bundle.getString("LAB_IndentEngineDefault"));
            descr.setShortDescription (bundle.getString("HINT_IndentEngineDefault"));
            return descr;
        }

        public BeanInfo[] getAdditionalBeanInfo () {
            try {
                return new BeanInfo[] { Introspector.getBeanInfo (org.openide.text.IndentEngine.class) };
            } catch (IntrospectionException ie) {
                Exceptions.printStackTrace(ie);
                return null;
            }
        }

    }
}
