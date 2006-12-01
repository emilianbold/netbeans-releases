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
 *
 */
package org.netbeans.installer.downloader.services;

import org.netbeans.installer.downloader.DownloadListener;


/**
 * @author Danila_Dugurov
 */
public abstract class EmptyQueueListener implements DownloadListener {

  public void pumpingUpdate(String id) {}

  public void pumpingStateChange(String id) {}

  public void pumpingAdd(String id) {}

  public void pumpingDelete(String id) {}

  public void queueReset() {}

  public void pumpsInvoke() {}

  public void pumpsTerminate() {}
}