/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package ajswing;

import javax.swing.JComponent;
import java.awt.EventQueue;
import java.awt.Component;
import java.awt.Container;

aspect SwingThreadRuleCheck
{
    pointcut swingMethods(JComponent comp) : target(comp) && execution(public * javax.swing..*(..));
    pointcut threadsafeMethods() :
        execution(* *..repaint(..))
        || execution(* *..revalidate(..))
        || execution(* *..get*(..))
        || execution(* *..is*(..))
        || execution(* *..putClientProperty(..))
        || execution(* *..reshape(..))
        || execution(* *..addNotify())
        || execution(* *..setVisible(..))
        || execution(* *..add*Listener(..))
        || execution(* *..remove*Listener(..));

    before(JComponent comp) : ! threadsafeMethods()
                              && swingMethods(comp)
                              && ! if (EventQueue.isDispatchThread())
                              && if (comp.isShowing())
                                  && ! cflow(call(public void java.awt.Window.pack()))
    {
        if (thisJoinPointStaticPart.getSignature().getDeclaringType().getName().startsWith("javax.swing.")) {
            System.err.println("SwingThreadRuleCheck: Swing Single-Thread rule is being violated");
            Thread.dumpStack();
        }
    }
}
