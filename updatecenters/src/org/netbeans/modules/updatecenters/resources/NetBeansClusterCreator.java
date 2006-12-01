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

package org.netbeans.modules.updatecenters.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.modules.autoupdate.AutoupdateClusterCreator;


/** Modifies the etc/netbeans.conf if necessary.
 * 
 * @author  Jaroslav Tulach
 */
public final class NetBeansClusterCreator extends AutoupdateClusterCreator {
    protected File findCluster(String clusterName) {
        File[] parent = new File[1];
        File conf = findConf(parent, new ArrayList<File>());
        return conf != null && conf.isFile() && conf.canWrite() ? new File(parent[0], clusterName) : null;
    }
    
    private static File findConf(File[] parent, List<? super File> clusters) {
        StringTokenizer tok = new StringTokenizer(System.getProperty("netbeans.dirs"), File.pathSeparator); // NOI18N
        while (tok.hasMoreElements()) {
            File cluster = new File(tok.nextToken());
            clusters.add(cluster);
            if (!cluster.exists()) {
                continue;
            }
            
            
            
            if (parent[0] == null) {
                parent[0] = cluster.getParentFile();
            }
            
            if (!parent[0].equals(cluster.getParentFile())) {
                // we can handle only case when all clusters are in
                // the same directory these days
                return null;
            }
        }
        
        return new File(new File(parent[0], "etc"), "netbeans.clusters");
    }
    
    protected File[] registerCluster(String clusterName, File cluster) throws IOException {
        File[] parent = new File[1];
        List<File> clusters = new ArrayList<File>();
        File conf = findConf(parent, clusters);
        assert conf != null;
        clusters.add(cluster);
        OutputStream os = new FileOutputStream(conf, true);
        os.write('\n');
        os.write(clusterName.getBytes());
        os.write('\n');
        os.close();
        return clusters.toArray(new File[0]);
    }
}
