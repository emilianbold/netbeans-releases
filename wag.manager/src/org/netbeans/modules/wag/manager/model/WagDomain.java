/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wag.manager.model;

import java.util.Collection;
import org.netbeans.modules.wag.manager.zembly.ZemblySession;

/**
 *
 * @author peterliu
 */
public class WagDomain extends WagItems<WagApi> implements Comparable<WagDomain>  {

    private static final String PROP_NAME = "domain";  //NOI18N

    private String name;
    private String path;

    public WagDomain(String name, String path) {
        super();
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getDisplayName() {
        return getName();
    }

    public String getDescription() {
        return getPath();
    }

    protected Collection<WagApi> loadItems() {
        return ZemblySession.getInstance().getContentRetriever().getApis(path);
    }

    protected String getPropName() {
        return PROP_NAME;
    }

    @Override
    public String toString() {
        return "name: " + name + " path: " + path;
    }

     @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WagDomain) {
            return name.equals(((WagDomain) obj).getName());
        }

        return false;
    }

    public int compareTo(WagDomain result) {
        return name.compareTo(result.getName());
    }

}
