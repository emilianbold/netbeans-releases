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
package org.netbeans.modules.vmd.midp.components;

import org.netbeans.modules.vmd.api.model.VersionDescriptor;
import org.netbeans.modules.vmd.api.model.DesignComponent;

import java.util.Collection;
import java.util.Set;

/**
 * @author David Kaspar
 */
public abstract class MidpVersionDescriptor implements VersionDescriptor {

    public static final VersionDescriptor FOREVER = new MidpVersionDescriptor () {

        public boolean isCompatibleWith (VersionDescriptor descriptor) {
            return true;
        }

    };

    public static final VersionDescriptor MIDP = new MidpVersionDescriptor () {

        public boolean isCompatibleWith (VersionDescriptor descriptor) {
            return descriptor == MIDP  ||  descriptor == MIDP_1  ||  descriptor == MIDP_2;
        }

    };

    public static final VersionDescriptor MIDP_1 = new MidpVersionDescriptor () {

        public boolean isCompatibleWith (VersionDescriptor descriptor) {
            return descriptor == MIDP  ||  descriptor == MIDP_1;
        }

    };

    public static final VersionDescriptor MIDP_2 = new MidpVersionDescriptor () {

        public boolean isCompatibleWith (VersionDescriptor descriptor) {
            return descriptor == MIDP  ||  descriptor == MIDP_2;
        }

    };

    private MidpVersionDescriptor () {
    }

    public boolean isCompatibleWith (Collection<String> abilities) {
        return false; // TODO
    }

    // TODO - change this signature
    public Set<String> getPreliminaryConvertMessages (DesignComponent component, Collection<String> oldAbilities, Collection<String> newAbilities) {
        return null; // TODO
    }

    // TODO - how to deal with deleting of unusable components
    public void convertComponent (DesignComponent component, Collection<String> oldAbilities, Collection<String> newAbilities) {
        // TODO
    }

}
