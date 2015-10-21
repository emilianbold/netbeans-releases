/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.ui.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.modules.docker.DockerInstance;
import org.netbeans.modules.docker.DockerContainer;
import org.netbeans.modules.docker.remote.DockerRemote;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author Petr Hejl
 */
public class DockerContainersChildFactory extends ChildFactory<DockerContainer> implements Refreshable {

    private static final Comparator<DockerContainer> COMPARATOR = new Comparator<DockerContainer>() {
        
        @Override
        public int compare(DockerContainer o1, DockerContainer o2) {
            return o1.getId().compareTo(o2.getId());
        }
    };

    private final DockerInstance instance;

    public DockerContainersChildFactory(DockerInstance instance) {
        this.instance = instance;
    }

    @Override
    protected Node createNodeForKey(DockerContainer key) {
        return new DockerContainerNode(key);
    }

    @Override
    protected boolean createKeys(List<DockerContainer> toPopulate) {
        DockerRemote facade = new DockerRemote(instance);
        List<DockerContainer> containers = new ArrayList<>(facade.getContainers());
        Collections.sort(containers, COMPARATOR);
        toPopulate.addAll(containers);
        return true;
    }

    public final void refresh() {
        refresh(false);
    }

}
