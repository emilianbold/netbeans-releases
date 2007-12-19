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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.rt.providers.impl.absent;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.php.rt.spi.providers.CommandProvider;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.netbeans.modules.php.rt.spi.providers.ProjectConfigProvider;
import org.netbeans.modules.php.rt.spi.providers.UiConfigProvider;
import org.netbeans.modules.php.rt.spi.providers.WebServerProvider;
import org.openide.nodes.Node;

/**
 *
 * Fake Provider 
 * is used to show user commands in the case 
 * when real Provider for project is not specified.
 * @author avk
 */
public class AbsentServerProvider implements WebServerProvider{

    public AbsentServerProvider() {
        myConfig = null;
        myCommandProvider = new AbsentCommandProvider(this);
        myProjectConfig = new AbsentProjectConfigProvider(this);
    }

    public List<Host> getHosts() {
        return myHosts;
    }

    public UiConfigProvider getConfigProvider() {
        return myConfig;
    }

    public CommandProvider getCommandProvider() {
        return myCommandProvider;
    }

    public ProjectConfigProvider getProjectConfigProvider() {
        return myProjectConfig;
    }

    public Node createNode(Host host) {
        return null;
    }

    public String getTypeName() {
        // shouldn't be used
        return null;
    }

    public String getDescription() {
        // shouldn't be used
        return null;
    }

    public Host findHost(String id) {
        return null;
    }
    
    private List<Host> myHosts = new ArrayList<Host>();

    private UiConfigProvider myConfig;

    private ProjectConfigProvider myProjectConfig;

    private CommandProvider myCommandProvider;
}
