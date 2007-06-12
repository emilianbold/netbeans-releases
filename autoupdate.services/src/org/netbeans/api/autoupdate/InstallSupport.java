/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.autoupdate;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.autoupdate.services.InstallSupportImpl;

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
            return new Restarter ();
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
    public final class Validator {private Validator() {}}
    public final class Installer {private Installer() {}}
    public final class Restarter {private Restarter () {}}

    //end of API - next just impl details
    private OperationContainer<InstallSupport> container;
    void setContainer(OperationContainer<InstallSupport> c) {container = c;}
    private InstallSupportImpl impl;
}