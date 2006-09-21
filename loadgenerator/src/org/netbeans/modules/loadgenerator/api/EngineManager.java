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

package org.netbeans.modules.loadgenerator.api;

import java.util.Collection;
import org.netbeans.modules.loadgenerator.spi.*;

/**
 *
 * @author Jaroslav Bachorik
 */
public interface EngineManager {
  public Collection<Engine> findEngines();

  public Collection<Engine> findEngines(final String extension);

//  public List<ILoadGeneratorInstance> findProviderInstances(final Class<? extends ILoadGeneratorInstance> providerClass);
  
  public void startProcess(final ProcessInstance process);

  public ProcessInstance startNewProcess(final Engine engine);
  
  public void stopProcess(final ProcessInstance process, final boolean force);
}
