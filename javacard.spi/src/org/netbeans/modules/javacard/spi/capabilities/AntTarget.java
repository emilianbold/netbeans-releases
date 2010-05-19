/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javacard.spi.capabilities;

import org.netbeans.modules.javacard.spi.ActionNames;

/**
 * Targets which may be passed to <code>AntTargetInterceptor.on*InvokeTarget()</code>
 * @author Tim Boudreau
 */
public enum AntTarget {

    /** The Ant target to load (but, in the case of applets, not instantiate)
     * a project's bytecode onto a device
     */
    LOAD, 
    /** The Ant target to unload 
     * a project's bytecode onto a device
     */
    UNLOAD,
    /** The Ant target to create an instance of a project on a device
     */
    INSTANTIATE,
    /** The Ant target to destroy an instance of a project on a device
     */
    UNINSTANTIATE,
    /** The Ant target to debug a project on a device
     */
    DEBUG,
    /** The Ant target to profile an instance of a project on a device (not
     * currently supported as of Java Card RI 3.0.2)
     */
    PROFILE;

    /**
     * Get an AntTarget instance for the specified build script target.
     * @param command The build script target (e.g. create, load, debug...)
     * @return The AntTarget corresponding to that target, if one exists,
     * or null if not
     */
    public static AntTarget forName(String command) {
        if (ActionNames.COMMAND_JC_CREATE.equals(command)) {
            return INSTANTIATE;
        } else if (ActionNames.COMMAND_JC_DELETE.equals(command)) {
            return UNINSTANTIATE;
        } else if (ActionNames.COMMAND_JC_LOAD.equals(command)) {
            return LOAD;
        } else if (ActionNames.COMMAND_JC_UNLOAD.equals(command)) {
            return UNLOAD;
        } else if ("debug".equals(command)) { //NOI18N
            return DEBUG;
        } else if ("profile".equals(command)) { //NOI18N
            return PROFILE;
        }
        return null;
    }
}
