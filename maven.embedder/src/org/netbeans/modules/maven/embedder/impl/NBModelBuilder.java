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
package org.netbeans.modules.maven.embedder.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.InputSource;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.DefaultModelBuilder;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingResult;

/**
 * take the results from the default implementation of ModelBuild and record the 
 * profile ids from all raw models, for use in configurations..
 * @author mkleint
 */
public class NBModelBuilder extends DefaultModelBuilder {

    private static final String NETBEANS_PROFILES = "____netbeans.profiles";
    
    @Override
    public ModelBuildingResult build(ModelBuildingRequest request) throws ModelBuildingException {
        ModelBuildingResult toRet = super.build(request);
        Model eff = toRet.getEffectiveModel();
        InputSource source = new InputSource();
        source.setLocation("");
        InputLocation location = new InputLocation(-1, -1, source);
        eff.setLocation(NETBEANS_PROFILES, location);
        for (String id : toRet.getModelIds()) {
            Model mdl = toRet.getRawModel(id);
            for (Profile p : mdl.getProfiles()) {
                source.setLocation(source.getLocation() + "|" + p.getId());
            }
        }
        return toRet;
    }
    
    public static Set<String> getAllProfiles(Model mdl) {
        InputLocation location = mdl.getLocation(NETBEANS_PROFILES);
        HashSet<String> toRet = new HashSet<String>();
        if (location != null) {
            String s = location.getSource().getLocation();
            if (!s.isEmpty()) {
                s = s.substring(1);
                toRet.addAll(Arrays.asList(s.split("\\|")));
            }
            return toRet;
        }
        return null;
    }
    
}
