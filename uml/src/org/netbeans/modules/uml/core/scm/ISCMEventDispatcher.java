/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

/*
 * ISCMEventDispatcher.java
 *
 * Created on August 11, 2004, 10:08 AM
 */

package org.netbeans.modules.uml.core.scm;

import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;

/**
 *
 * @author  Trey Spiva
 */
public interface ISCMEventDispatcher extends IEventDispatcher
{
   /**
    * Registers an event sink to handle SCM events.
    */
   public void registerForSCMEvents( ISCMEventsSink handler );

   /**
    * Removes a sink listening for SCM events.
    */
   public void revokeSCMSink(ISCMEventsSink handler);

   /**
    * Calling this method will result in the firing of any listeners who
    * register for SCM events.
    *
    * @param kind The feature to execute.  The value must be one of the
    *             SCMFeatureKind values.
    * @param files The subjects of the feature.
    * @param options The SCM options.
    * @param payload The event payload.
    * @see SCMFeatureKind
    */
   public boolean firePreFeatureExecuted( int kind,
                                          ISCMItemGroup files,
                                          ISCMOptions options,
                                          IEventPayload payload);

   /**
    * Calling this method will result in the firing of any listeners who
    * register for SCM events.
    *
    * @param kind The feature to execute.  The value must be one of the
    *             SCMFeatureKind values.
    * @param files The subjects of the feature.
    * @param options The SCM options.
    * @param payload The event payload.
    * @see SCMFeatureKind
    */
   public void fireFeatureExecuted( int kind,
                                    ISCMItemGroup files,
                                    ISCMOptions pOptions,
                                    IEventPayload payload );
}
