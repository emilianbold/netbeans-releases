/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.vmd.api.model;

import org.w3c.dom.Node;

import java.util.List;

/**
 * Used for deserialization of 3rd-party defined nodes in a component descriptor serialized as an xml file in component registry.
 * An implementation of this class should be registered in global lookup.
 *
 * @author David Kaspar
 */
public abstract class PresenterDeserializer {

    private String projectType;

    /**
     * Creates a PresenterDeserializer releated to specified project type.
     * @param projectType the project type
     */
    protected PresenterDeserializer (String projectType) {
        this.projectType = projectType;
    }

    /**
     * Returns the related project type.
     * @return the project type
     */
    public final String getProjectType () {
        return projectType;
    }

    /**
     * Called to deserialize a node in a xml file into a presenter descriptor.
     * @param node the node in the xml file
     * @return the presenter descriptor
     */
    public abstract PresenterFactory deserialize (Node node);

    /**
     * Describes presenters and should create a list of presenters when asked.
     */
    public static abstract class PresenterFactory {

        /**
         * Called to create presenters from descriptor when an instance of a component descriptor is going to be created.
         * @param descriptor the component descriptor
         * @return the list of presenters
         */
        public abstract List<Presenter> createPresenters (ComponentDescriptor descriptor);

    }

}
