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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.ruby.rubyproject;

import org.netbeans.api.ruby.platform.RubyInstallation;
import org.openide.modules.ModuleInstall;


/**
 * Module install for Ruby projects.
 * Used to initialize executable permissions on the JRuby binaries.
 *
 * @author Tor Norbye
 */
public class RubyProjectModuleInstaller extends ModuleInstall {
    /** 
     * @todo This should really be {@link #installed} instead,
     * but there's a problm where modules installed via AutoUpdate
     * doesn't run this code.
     * See http://www.netbeans.org/issues/show_bug.cgi?id=95965
     */
    public void restored() {
        // On install, ensure that the JRuby bits are executable
        RubyInstallation.getInstance().ensureExecutable();
    }
}
