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

package org.netbeans.modules.loadgenerator.spi;

import java.util.EventListener;

/**
 * This is a listener for a load generator's events
 * @author Jaroslav Bachorik
 */
public interface ProcessInstanceListener extends EventListener {
  /**
   * This event is fired when the load generator starts generating load
   */
  public void generatorStarted(final ProcessInstance provider);
  public void generatorStarted(final ProcessInstance provider, final String logPath);
  
  /**
   * This event is fired when the load generator ceases generating load
   */
  public void generatorStopped(final ProcessInstance provider);
  
  /**
   *  This event is fired when the instance is invalidate eg. by editing it externally or renaming it
   */
  public void instanceInvalidated(final ProcessInstance instance);
}
