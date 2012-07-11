/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.c2c.tasks.spi;

import com.tasktop.c2c.internal.client.tasks.core.CfcRepositoryConnector;
import com.tasktop.c2c.internal.client.tasks.core.client.CfcClientData;
import com.tasktop.c2c.internal.client.tasks.core.client.ICfcClient;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;
import org.openide.util.Exceptions;

/** Provides access to extended methods not available on {@link AbstractRepositoryConnector}
 * but needed for connecting to C2C instance.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class C2CExtender {

    public static void repositoryRemoved(AbstractRepositoryConnector rc, TaskRepository r) {
        getDefault().spiRepositoryRemove(rc, r);
    }

    private C2CExtender() {
    }
    
    public static AbstractRepositoryConnector create() {
        return getDefault().spiCreate();
    }
    public static void assignTaskRepositoryLocationFactory(AbstractRepositoryConnector rc, TaskRepositoryLocationFactory taskRepositoryLocationFactory) {
        getDefault().spiAssignTaskRepositoryLocationFactory(rc, taskRepositoryLocationFactory);
    }
    
    public static C2CData getData(AbstractRepositoryConnector rc, TaskRepository r) {
        return getDefault().spiClientData(rc, r);
    }
        
    // will use Lookup
    private static C2CExtender getDefault() {
        return new C2CExtender();
    }

    
    //
    // impl
    // will be in separete module(s)
    //
    private AbstractRepositoryConnector spiCreate() {
        return new CfcRepositoryConnector();
    }

    private void spiAssignTaskRepositoryLocationFactory(AbstractRepositoryConnector rc, TaskRepositoryLocationFactory taskRepositoryLocationFactory) {
        CfcRepositoryConnector cfc = (CfcRepositoryConnector)rc;
        cfc.getClientManager().setTaskRepositoryLocationFactory(new TaskRepositoryLocationFactory());
    }
    
    private synchronized C2CData spiClientData(AbstractRepositoryConnector rc, TaskRepository taskRepository) {
        CfcRepositoryConnector cfc = (CfcRepositoryConnector)rc;
        ICfcClient client = cfc.getClientManager().getClient(taskRepository);
        CfcClientData clientData = client.getCalmClientData();

        if (!clientData.isInitialized()) {
            try {
                client.updateRepositoryConfiguration(new NullProgressMonitor());
            } catch (CoreException ex) {
                // XXX
                Exceptions.printStackTrace(ex);
            }
        }
        return new C2CData(clientData);
    }

    private void spiRepositoryRemove(AbstractRepositoryConnector rc, TaskRepository r) {
        CfcRepositoryConnector cfc = (CfcRepositoryConnector)rc;
        cfc.getClientManager().repositoryRemoved(r);
    }
}
