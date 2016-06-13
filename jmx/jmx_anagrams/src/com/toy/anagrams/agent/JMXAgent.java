/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005, 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 */
/*
 * JMXAgent.java
 *
 * Created on May 11, 2005, 9:28 AM
 */

package com.toy.anagrams.agent;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;

import com.toy.anagrams.mbeans.AnagramsStats;

/**
 * JMX agent class.
 * You may use the New JMX MBean wizard to create a Managed Bean.
 * @author jfdenise
 */
public class JMXAgent {
    private AnagramsStats mbean;

    public AnagramsStats getAnagramsStats() {
        return mbean;
    }

    /**
     * Instantiate and register your MBeans.
     */
    public void init() throws Exception {

        //TODO Add your MBean registration code here

        // Instantiate Anagrams MBean
        mbean =
            new AnagramsStats();
        ObjectName mbeanName = new ObjectName ("anagrams.toy.com:type=AnagramsStats");
        //Register the Anagrams MBean
        getMBeanServer().registerMBean(mbean, mbeanName);
    }

    /**
     * Returns an agent singleton.
     */
    public synchronized static JMXAgent getDefault() throws Exception {
        if(singleton == null) {
            singleton = new JMXAgent();
            singleton.init();
        }
        return singleton;
    }
    
    public MBeanServer getMBeanServer() {
        return mbs;
    }
    
    // Platform MBeanServer used to register your MBeans
    private final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    
    // Singleton instance
    private static JMXAgent singleton;
}


