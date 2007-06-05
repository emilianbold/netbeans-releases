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

package org.netbeans.spi.autoupdate;

import java.io.File;
import java.io.IOException;

/** Class that is supposed to be implemented by application 
 * providers that can control launcher in order to modify
 * the list of provided clusters.
 *
 * @since 1.2
 * @author  Jaroslav Tulach
 */
public abstract class AutoupdateClusterCreator extends Object {
    /** Finds the right cluster directory for given cluster name.
     * This method can return null if no such cluster name is known or 
     * understandable, otherwise it returns a file object representing
     * <b>not existing</b> directory that will be created later
     * to host hold the content of the cluster.
     * 
     * @param clusterName the name of the cluster the autoupdate client is searching for
     * @return null or File object of the cluster to be created
     */
    protected abstract File findCluster(String clusterName);
    
    /** Changes the launcher to know about the new cluster and 
     * use it next time the system starts.
     * 
     * @param clusterName the name of the cluster
     * @param cluster file previously returned by findCluster
     * @return the list of current cluster directories, including the newly added one
     * @exception IOException if the registration fails
     */
    protected abstract File[] registerCluster(String clusterName, File cluster) throws IOException;
}
