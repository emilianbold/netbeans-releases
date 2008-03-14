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

package org.netbeans.api.db.sample;

import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.sample.SampleDbProviderManager;
import org.netbeans.spi.db.sample.SampleDbProvider;

/**
 *
 * @author David
 */
public class SampleDbManager {
    public enum Status {
        DISCONNECTED, CONNECTING, CREATINGDB, CREATINGSCHEMA, CREATINGTABLES, 
            INITIALIZINGDATA, COMPLETED, ERROR
    }
        
    
    private static ConcurrentHashMap<String, SampleDbManager> controllers = 
            new ConcurrentHashMap<String, SampleDbManager>();
    
    private SampleDbProvider provider;
    private String driverName;
        
    public static SampleDbManager getController(String driverName) {
        SampleDbManager controller = controllers.get(driverName);
        
        if ( controller == null ) {
            controller = new SampleDbManager(driverName);
            controllers.put(driverName, controller);
        }
        
        return controller;
    }
    
    public SampleDbManager(String driverName) {
        this.driverName = driverName;
        provider = getProvider(driverName);
    }
    
    private SampleDbProvider getProvider(String driverName) {
        SampleDbProvider[] providers =  
                SampleDbProviderManager.getDefault().getProviders(driverName);
        
        if ( providers == null ) {
            return null;
        }
        
        return providers[0];        
    }
    
    public void dropDatabase(String sampleName, String host, String port,
            String user, String password) throws DatabaseException {
        provider.dropDatabase(sampleName, host, port, user, password);
    }
    
    
    /**
     * Add a listener to changes in the status of the controller
     * as it progresses through creating a sample database.  This
     * is useful for a progress bar
     * 
     * @param listener 
     *  the listener to add
     */
    /* LATER...
    public void addChangeListener(ChangeListener listener) {
    }
    
    public Status getStatus() {
        return null;
    }

    protected void notifyStateChange(ChangeEvent e) {
        for ( ChangeListener listener : changeListeners ) {
            listener.stateChanged(e);
        }
    }
    */
    
    
    
}
