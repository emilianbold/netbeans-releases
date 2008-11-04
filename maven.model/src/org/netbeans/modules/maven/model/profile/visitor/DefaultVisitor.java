/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.model.profile.visitor;

import org.netbeans.modules.maven.model.profile.Activation;
import org.netbeans.modules.maven.model.profile.ActivationCustom;
import org.netbeans.modules.maven.model.profile.ActivationFile;
import org.netbeans.modules.maven.model.profile.ActivationOS;
import org.netbeans.modules.maven.model.profile.ActivationProperty;
import org.netbeans.modules.maven.model.profile.ModelList;
import org.netbeans.modules.maven.model.profile.Profile;
import org.netbeans.modules.maven.model.profile.ProfilesComponent;
import org.netbeans.modules.maven.model.profile.ProfilesComponentVisitor;
import org.netbeans.modules.maven.model.profile.ProfilesExtensibilityElement;
import org.netbeans.modules.maven.model.profile.ProfilesRoot;
import org.netbeans.modules.maven.model.profile.Properties;
import org.netbeans.modules.maven.model.profile.Repository;
import org.netbeans.modules.maven.model.profile.RepositoryPolicy;
import org.netbeans.modules.maven.model.profile.StringList;



/**
 * Default shallow visitor.
 *
 * @author mkleint
 */
public class DefaultVisitor implements ProfilesComponentVisitor {
        
    public void visit(ProfilesRoot target) {
        visitComponent(target);
    }

    public void visit(Repository target) {
        visitComponent(target);
    }

    public void visit(RepositoryPolicy target) {
        visitComponent(target);
    }

    public void visit(Profile target) {
        visitComponent(target);
    }

    public void visit(Activation target) {
        visitComponent(target);
    }

    public void visit(ActivationProperty target) {
        visitComponent(target);
    }

    public void visit(ActivationOS target) {
        visitComponent(target);
    }

    public void visit(ActivationFile target) {
        visitComponent(target);
    }

    public void visit(ActivationCustom target) {
        visitComponent(target);
    }


    public void visit(ProfilesExtensibilityElement target) {
        visitComponent(target);
    }

    public void visit(ModelList target) {
        visitComponent(target);
    }
    
    public void visit(Properties target) {
        visitComponent(target);
    }

    protected void visitComponent(ProfilesComponent target) {
    }

    public void visit(StringList target) {
        visitComponent(target);
    }


}