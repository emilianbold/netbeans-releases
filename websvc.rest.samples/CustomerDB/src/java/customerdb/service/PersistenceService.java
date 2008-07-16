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

package customerdb.service;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

/**
 * Utility class for dealing with persistence.
 *
 * @author PeterLiu
 */
public class PersistenceService {
    private static String DEFAULT_PU = "CustomerDBPU";
    
    private static ThreadLocal<PersistenceService> instance = new ThreadLocal<PersistenceService>() {
        @Override
        protected PersistenceService initialValue() {
            return new PersistenceService();
        }
    };
    
    private EntityManager em;
    
    private UserTransaction utx;
    
    private PersistenceService() {
        try {
            this.em = (EntityManager) new InitialContext().lookup("java:comp/env/persistence/" + DEFAULT_PU);
            this.utx = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
        } catch (NamingException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Returns an instance of PersistenceService.
     *
     * @return an instance of PersistenceService
     */
    public static PersistenceService getInstance() {
        return instance.get();
    }
    
    
    private static void removeInstance() {
        instance.remove();
    }
    
    /**
     * Returns an instance of EntityManager.
     *
     * @return an instance of EntityManager
     */
    public EntityManager getEntityManager() {
        return em;
    }
    
    /**
     * Begins a resource transaction.
     */
    public void beginTx() {
        try {
            utx.begin();
            em.joinTransaction();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Commits a resource transaction.
     */
    public void commitTx() {
        try {
            utx.commit();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Rolls back a resource transaction.
     */
    public void rollbackTx() {
        try {
            utx.rollback();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Closes this instance.
     */
    public void close() {
        removeInstance();
    }
}
