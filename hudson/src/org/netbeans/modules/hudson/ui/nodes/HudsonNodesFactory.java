/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.hudson.ui.nodes;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.hudson.impl.HudsonJobImpl;
import org.netbeans.modules.hudson.impl.HudsonViewImpl;

/**
 * Hudson node factory
 *
 * @author Michal Mocnak
 */
public class HudsonNodesFactory {
    
    /** Init lock */
    private static final Object LOCK_INIT = new Object();
    
    /** The only instance of the hudson nodes factory in the system */
    private static HudsonNodesFactory defaultInstance;
    
    /** All caches map */
    private final Map<Object,Map<String,HudsonViewNode>> viewMaps = new HashMap<Object,Map<String,HudsonViewNode>>();
    private final Map<Object,Map<String,HudsonJobNode>> jobMaps = new HashMap<Object,Map<String,HudsonJobNode>>();
    
    private HudsonNodesFactory() {}
    
    public static HudsonNodesFactory getDefault() {
        synchronized(LOCK_INIT) {
            // a static object to synchronize on
            if (null == defaultInstance)
                defaultInstance = new HudsonNodesFactory();
            
            return defaultInstance;
        }
    }
    
    public HudsonViewNode getHudsonViewNode(Object parent, HudsonViewImpl view) {
        // Get cache map
        Map<String,HudsonViewNode> cache = viewMaps.get(parent);
        
        // If there is no cache map create a new one
        if (cache == null) {
            cache = new HashMap<String, HudsonViewNode>();
            viewMaps.put(parent, cache);
        }
        
        // Try to get HudsonViewNode from cache
        HudsonViewNode v = cache.get(view.getUrl());
        
        // If there is no item in cache create a new one
        if (null == v) {
            v = new HudsonViewNode(view);
            
            // Ant put it into the cache
            cache.put(view.getUrl(), v);
        } else {
            // Update Hudson View
            v.setHudsonView(view);
        }
        
        return v;
    }
    
    public HudsonJobNode getHudsonJobNode(Object parent, HudsonJobImpl job) {
        // Get cache map
        Map<String,HudsonJobNode> cache = jobMaps.get(parent);
        
        // If there is no cache map create a new one
        if (cache == null) {
            cache = new HashMap<String, HudsonJobNode>();
            jobMaps.put(parent, cache);
        }
        
        // Try to get HudsonJobNode from cache
        HudsonJobNode j = cache.get(job.getUrl());
        
        // If there is no item in cache create a new one
        if (null == j) {
            j = new HudsonJobNode(job);
            
            // Ant put it into the cache
            cache.put(job.getUrl(), j);
        } else {
            // Update Hudson View
            j.setHudsonJob(job);
        }
        
        return j;
    }
}