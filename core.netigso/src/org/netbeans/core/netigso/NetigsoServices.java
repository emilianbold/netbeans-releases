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

package org.netbeans.core.netigso;

import java.util.logging.Level;
import org.netbeans.core.startup.MainLookup;
import org.openide.util.lookup.InstanceContent;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class NetigsoServices
implements ServiceListener, InstanceContent.Convertor<ServiceReference, Object> {
    public NetigsoServices(Framework f) {
        for (ServiceReference ref : f.getRegisteredServices()) {
            MainLookup.register(ref, this);
        }
        f.getBundleContext().addServiceListener(this);
    }

    @Override
    public void serviceChanged(ServiceEvent ev) {
        final ServiceReference ref = ev.getServiceReference();
        if (ev.getType() == ServiceEvent.REGISTERED) {
            MainLookup.register(ref, this);
        }
        if (ev.getType() == ServiceEvent.UNREGISTERING) {
            MainLookup.unregister(ref, this);
        }
    }

    @Override
    public Object convert(ServiceReference obj) {
        return obj.getBundle().getBundleContext().getService(obj);
    }

    @Override
    public Class<? extends Object> type(ServiceReference obj) {
        String[] arr = (String[])obj.getProperty(Constants.OBJECTCLASS);
        if (arr.length > 0) {
            try {
                return (Class<?>)obj.getBundle().loadClass(arr[0]);
            } catch (ClassNotFoundException ex) {
                Netigso.LOG.log(Level.INFO, "Cannot load service class", arr[0]); // NOI18N
            }
        }
        return Object.class;
    }

    @Override
    public String id(ServiceReference obj) {
        return (String) obj.getProperty(Constants.SERVICE_ID);
    }

    @Override
    public String displayName(ServiceReference obj) {
        return (String) obj.getProperty(Constants.SERVICE_DESCRIPTION);
    }
}
