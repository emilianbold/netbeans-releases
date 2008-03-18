/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.javaee;

import java.awt.Image;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport.ServerIcon;
import org.netbeans.spi.glassfish.Decorator;
import org.netbeans.spi.glassfish.DecoratorFactory;
import org.openide.util.Utilities;

/**
 *
 * @author Peter Williams   
 */
public class JavaEEDecoratorFactory implements DecoratorFactory {

    private static DecoratorFactory singleton = new JavaEEDecoratorFactory();
    
    private JavaEEDecoratorFactory() {
    }
    
    public static DecoratorFactory getDefault() {
        return singleton;
    }
    
    // ------------------------------------------------------------------------
    //  DecoratorFactor implementation
    // ------------------------------------------------------------------------
    public boolean isTypeSupported(String type) {
        return decoratorMap.containsKey(type);
    }

    public Decorator getDecorator(String type) {
        return decoratorMap.get(type);
    }

    public Map<String, Decorator> getAllDecorators() {
        return Collections.unmodifiableMap(decoratorMap);
    }

    // ------------------------------------------------------------------------
    //  Internals...
    // ------------------------------------------------------------------------
    
    private static final String JDBC_RESOURCE_ICON = 
            "org/netbeans/modules/j2ee/hk2/resources/JDBCResource.gif"; // NOI18N
    private static final String CONNECTION_POOL_ICON = 
            "org/netbeans/modules/j2ee/hk2/resources/ConnectionPool.gif"; // NOI18N
    
    public static Decorator J2EE_APPLICATION_FOLDER = new Decorator() {
        @Override public boolean isRefreshable() { return true; }
        @Override public boolean canDeployTo() { return true; }
        @Override public Image getIcon(int type) { return UISupport.getIcon(ServerIcon.EAR_FOLDER); }
        @Override public Image getOpenedIcon(int type) { return UISupport.getIcon(ServerIcon.EAR_OPENED_FOLDER); }
    };
    
    public static Decorator J2EE_APPLICATION = new Decorator() {
        @Override public boolean canUndeploy() { return true; }
        @Override public boolean canShowBrowser() { return true; }
        @Override public Image getIcon(int type) { return UISupport.getIcon(ServerIcon.EAR_ARCHIVE); }
    };
    
    public static Decorator WEB_APPLICATION = new Decorator() {
        @Override public boolean canUndeploy() { return true; }
        @Override public boolean canShowBrowser() { return true; }
        @Override public Image getIcon(int type) { return UISupport.getIcon(ServerIcon.WAR_ARCHIVE); }
    };
    
    public static Decorator EJB_JAR = new Decorator() {
        @Override public boolean canUndeploy() { return true; }
        @Override public boolean canShowBrowser() { return true; }
        @Override public Image getIcon(int type) { return UISupport.getIcon(ServerIcon.EJB_ARCHIVE); }
    };
    
    public static Decorator JDBC_MANAGED_DATASOURCES = new Decorator() {
        @Override public boolean canUndeploy() { return true; }
        @Override public Image getIcon(int type) { return Utilities.loadImage(JDBC_RESOURCE_ICON); }
    };
    
    public static Decorator JDBC_NATIVE_DATASOURCES = new Decorator() {
        @Override public boolean canUndeploy() { return true; }
        @Override public Image getIcon(int type) { return Utilities.loadImage(JDBC_RESOURCE_ICON); }
    };
    
    public static Decorator CONNECTION_POOLS = new Decorator() {
        @Override public boolean canUndeploy() { return true; }
        @Override public Image getIcon(int type) { return Utilities.loadImage(CONNECTION_POOL_ICON); }
    };

    private static Map<String, Decorator> decoratorMap = new HashMap<String, Decorator>();
    
    static {
        // !PW XXX need to put in correct strings, then define as static 
        //   (export in Decorator API, for lack of better place)
        decoratorMap.put("web_ContractProvider", WEB_APPLICATION);
        decoratorMap.put("ejb_ContractProvider", EJB_JAR);
        decoratorMap.put("ear_ContractProvider", J2EE_APPLICATION);
        decoratorMap.put("JDBC Resource", JDBC_MANAGED_DATASOURCES);
        decoratorMap.put("JDBC Connection Pool", CONNECTION_POOLS);
    };
    
}
