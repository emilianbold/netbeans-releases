/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo;

import java.beans.*;
import java.util.ResourceBundle;
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
        private static BeanDescriptor descr;
        private static PropertyDescriptor[] prop;
        static {
            Class c;
            try {
                c = Class.forName("org.openide.text.IndentEngine$Default");
            } catch (Exception e) {
                throw new IllegalStateException();
            }
            descr = new BeanDescriptor (c);
            ResourceBundle bundle = NbBundle.getBundle(IndentEngine.class);

            descr.setDisplayName (bundle.getString("LAB_IndentEngineDefault"));
            descr.setShortDescription (bundle.getString("HINT_IndentEngineDefault"));
        }

        public BeanDescriptor getBeanDescriptor () {
            return descr;
        }

        public BeanInfo[] getAdditionalBeanInfo () {
            try {
                return new BeanInfo[] { Introspector.getBeanInfo (org.openide.text.IndentEngine.class) };
            } catch (IntrospectionException ie) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                    ie.printStackTrace ();
                return null;
            }
        }

    }
}

/*
* Log
*  4    Gandalf   1.3         1/13/00  Jaroslav Tulach I18N
*  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         10/1/99  Jesse Glick     Cleanup of service type 
*       name presentation.
*  1    Gandalf   1.0         6/28/99  Jaroslav Tulach 
* $
*/
