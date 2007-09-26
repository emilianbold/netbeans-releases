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

package org.netbeans.api.autoupdate;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.autoupdate.services.InstallSupportImpl;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;

/**
 * @author Radek Matous
 */
public final class InstallSupport {
    InstallSupport () {
        impl = new InstallSupportImpl (this);
    }
    
    public Validator doDownload (ProgressHandle progress/*or null*/, boolean isGlobal) throws OperationException {
        if (impl.doDownload (progress, isGlobal)) {
            return new Validator ();
        } else {
            return null;
        }
    }

    public Installer doValidate (Validator validator, ProgressHandle progress/*or null*/) throws OperationException {
        if (impl.doValidate (validator, progress)) {
            return new Installer ();
        } else {
            return null;
        }
    }

    public Restarter doInstall (Installer installer ,ProgressHandle progress/*or null*/) throws OperationException {
        Boolean restart = impl.doInstall (installer, progress);
        if (restart == null /*was problem*/ || ! restart.booleanValue ()) {
            return null;
        } else {
            return new OperationSupport.Restarter ();
        }
    }
    
    public void doCancel () throws OperationException {
        // finds and deletes possible downloaded files
        impl.doCancel ();
    }

    public void doRestart(Restarter restarter,ProgressHandle progress/*or null*/) throws OperationException {
        impl.doRestart (restarter, progress);
    }

    public void doRestartLater(Restarter restarter) {
        impl.doRestartLater(restarter);
    }
    
    public String getCertificate(Installer validator, UpdateElement uElement) {
        return impl.getCertificate (validator, uElement);
    }

    public boolean isTrusted(Installer validator, UpdateElement uElement) {
        return impl.isTrusted (validator, uElement);
    }

    public boolean isSigned(Installer validator, UpdateElement uElement) {
        return impl.isSigned (validator, uElement);
    }

    public OperationContainer<InstallSupport> getContainer() {return container;}
    
    //just tokens for passing it further to guarantee the order of operations
    public static final class Validator {private Validator() {}}
    public static final class Installer {private Installer() {}}

    //end of API - next just impl details
    private OperationContainer<InstallSupport> container;
    void setContainer(OperationContainer<InstallSupport> c) {container = c;}
    private InstallSupportImpl impl;
}