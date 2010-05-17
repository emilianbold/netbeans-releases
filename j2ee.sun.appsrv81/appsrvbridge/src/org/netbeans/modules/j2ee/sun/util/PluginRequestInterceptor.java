/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.j2ee.sun.util;

import java.util.Set;
import java.io.IOException;


import com.sun.appserv.management.util.jmx.MBeanServerConnection_Hook;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;

import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;

import org.openide.util.NbBundle;

/**
 *
 */
public class PluginRequestInterceptor
        extends MBeanServerConnection_Hook {

    final MBeanServerConnection_Hook.Hook myHook;
    
    /**
     *
     *
     */
    public PluginRequestInterceptor(
            final SunDeploymentManagerInterface sunDplymtIntrface,
            final MBeanServerConnection connection) {
        super(connection);
        this.myHook = new DebugCheckerHook(sunDplymtIntrface);
    }
    

    /**
     *
     *
     */
    Hook getHook() {
        return this.myHook;
    }
    
    
    /**
     *
     */
    public void setAttribute(ObjectName name, Attribute attribute)
            throws InstanceNotFoundException, AttributeNotFoundException,
            InvalidAttributeValueException, MBeanException,
            ReflectionException, IOException {
        getHook().preHook(null);
        super.setAttribute(name, attribute);
    }


    /**
     *
     *
     */
    public AttributeList setAttributes(ObjectName name, AttributeList attributes)
            throws InstanceNotFoundException, ReflectionException, IOException {    
       getHook().preHook(null);
       return super.setAttributes(name, attributes);
    }
     
    
    /**
     *
     *
     */
    public Object getAttribute(ObjectName name, String attribute)
            throws MBeanException, AttributeNotFoundException,
            InstanceNotFoundException, ReflectionException,
            IOException {         
        getHook().preHook(null);
        return super.getAttribute(name, attribute);
    }


    /**
     *
     */
    public AttributeList getAttributes(ObjectName name, String[] attributes)
            throws InstanceNotFoundException, ReflectionException,
            IOException {
        getHook().preHook(null);
        return super.getAttributes(name, attributes);
    }
     
     
    /**
     *
     */
    public MBeanInfo getMBeanInfo(ObjectName name)
            throws InstanceNotFoundException, IntrospectionException,
            ReflectionException, IOException {
        getHook().preHook(null);
        return super.getMBeanInfo(name);
    }

     
    
    /**
     *
     *
     */
    public Object invoke(ObjectName name, String operationName,
            Object params[], String signature[])
            throws InstanceNotFoundException, MBeanException,
            ReflectionException, IOException {
       getHook().preHook(null);
       return super.invoke(name, operationName, params, signature);
    }

    
    /**
     *
     *
     */
    public Set queryNames(ObjectName name, QueryExp query)
            throws IOException {
        getHook().preHook(null);
        return super.queryNames(name, query);
    }

    
    /**
     *
     *
     */
    public Set queryMBeans(ObjectName name, QueryExp query)
            throws IOException {
        getHook().preHook(null);
        return super.queryMBeans(name, query);
    }
    
    
    /**
     *
     *
     */
    class DebugCheckerHook extends MBeanServerConnection_Hook.HookImpl {

        private SunDeploymentManagerInterface sunDpmtMgr;
        
        /**
         *
         */
        public DebugCheckerHook(
                final SunDeploymentManagerInterface sunDplymtIntrface) {  
            this.sunDpmtMgr = sunDplymtIntrface;
        }

        
        /**
         *
         */
        public long preHook(final String methodName) {
            return preHook(methodName, new Object[] {});
        }

        
        /**
         * 
         */
        public long preHook(final String methodName, final Object[] args) {
            if(sunDpmtMgr.isSuspended()) {
                GUIUtils.showInformation(
                    getLocalizedString("server_dbg_mode_notify"));
                throw new RuntimeException(
                    getLocalizedString("server_dbg_mode_notify"));
            } 
            return super.preHook(methodName, args);
        }
        
    } //end inner class
    
         
    /**
     *
     *
     */
    private static String getLocalizedString(final String bundleStrProp) {
        return NbBundle.getMessage(PluginRequestInterceptor.class, 
                bundleStrProp);
    }
}
