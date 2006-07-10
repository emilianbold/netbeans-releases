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
