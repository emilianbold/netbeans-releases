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

package org.netbeans.modules.groovy.support.spi;

/**
 * Provides an ability to change project settings when some groovy file is created.
 * This enables to change build script in Ant based projects, pom.xml in Maven
 * based projects etc.
 *
 * @since 1.22
 * @author Martin Janicek
 */
public interface GroovyExtender {

    /**
     * Check if groovy has been already activated for the project.
     *
     * @return true if the groovy is already active, false if groovy is not active yet
     */
    public boolean isActive();

    /**
     * Called when groovy is activated for the project. (e.g. when new Groovy file
     * is created). Implementator should change project configuration with respect
     * to groovy source files (e.g. change the ant build script and use groovyc
     * instead of javac, update pom.xml in maven etc.)
     *
     * @return true if activation were successful, false otherwise
     */
    public boolean activate();

    /**
     * Called when groovy is deactivated for a certain project. This is an inverse
     * action to the {@code activate} method. Implementator should make opposite steps
     * in the project configuration (e.g. remove maven-groovy-plugin and related groovy
     * dependencies from pom.xml, change the ant build script to use javac again etc.)
     *
     * @return true if deactivation were successful, false otherwise
     */
    public boolean deactivate();
}
