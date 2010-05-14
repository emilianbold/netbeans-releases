/*
 * DefaultClassLoaderManager.java
 *
 * Created on May 19, 2006, 1:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.encoder.netbeans;

import com.sun.encoder.ClassLoaderManager;
import org.openide.util.Lookup;

/**
 *
 * @author jyang
 */
public class NBMClassLoaderManager implements ClassLoaderManager {
//public class NBMClassLoaderManager {
    /** Creates a new instance of DefaultClassLoaderManager */
    public NBMClassLoaderManager() {
    }
    
    /**
     * Returns ClassLoader of EncoderFactory.class.
     */
    public ClassLoader getEncoderClassLoader() {
        return (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class); 
    }
}
