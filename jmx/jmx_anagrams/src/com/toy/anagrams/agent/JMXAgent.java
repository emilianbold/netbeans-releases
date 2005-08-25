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


