/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.util;

import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.POMComponentFactory;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.openide.util.Lookup;


/**
 * Convenience methods to retrieve and set plugin parameters in Maven POM.
 * 
 * @author S. Aubrecht
 */
public final class MavenPluginParameters {

    private final Lookup lkp;
    private final String groupId;
    private final String artifactId;

    private MavenPluginParameters( Lookup lkp, String groupId, String artifactId ) {
        this.lkp = lkp;
        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    /**
     * Creates a new instance for the given group and artifact id.
     * @param lkp
     * @param groupId
     * @param artifactId
     * @return
     */
    static MavenPluginParameters create( Lookup lkp, String groupId, String artifactId ) {
        return new MavenPluginParameters( lkp, groupId, artifactId );
    }

    /**
     * Retrieve Maven plugin version info.
     * @return Version number for the given plugin or null.
     */
    public String getPluginVersion() {
        NbMavenProject nbMaven = lkp.lookup( NbMavenProject.class );

        return PluginPropertyUtils.getPluginVersion( nbMaven.getMavenProject(), groupId, artifactId );
    }

    /**
     * Retrieve Maven plugin configuration property.
     * @param paramName Plugin parameter name.
     * @param expressionProperty User property name.
     * @return Parameter value or null if the parameter isn't defined.
     */
    public String getParameter( String paramName, String expressionProperty ) {
        NbMavenProject nbMaven = lkp.lookup( NbMavenProject.class );
        MavenProject prj = nbMaven.getMavenProject();
            return PluginPropertyUtils.getPluginProperty( prj, groupId, artifactId, paramName, null, expressionProperty );
    }

    /**
     * Change/add property for the given Maven plugin.
     * @param paramName Parameter name
     * @param propertyValue Parameter value
     */
    public void setParameter( String paramName, String propertyValue ) {
        ModelHandle2 mh = lkp.lookup( ModelHandle2.class);
        NbMavenProject nbMaven = lkp.lookup( NbMavenProject.class );
        mh.addPOMModification( new ParameterOperation( nbMaven, groupId, artifactId, paramName, propertyValue));
    }


    private static class ParameterOperation implements ModelOperation<POMModel> {

        private final NbMavenProject nbMaven;
        private final String groupId;
        private final String artifactId;
        private final String name;
        private final String value;

        public ParameterOperation( NbMavenProject nbMaven, String groupId, String artifacId, String paramName, String paramValue ) {
            this.nbMaven = nbMaven;
            this.groupId = groupId;
            this.artifactId = artifacId;
            this.name = paramName;
            this.value = paramValue;
        }

        @Override
        public void performOperation( POMModel model ) {
            //new approach, assume all plugins conform to the new setting.
            POMComponentFactory fact = model.getFactory();

            //check if compiler/resources plugins are configured and update them to ${project.source.encoding expression
            Build bld = model.getProject().getBuild();
            if( bld == null ) {
                bld = fact.createBuild();
                model.getProject().setBuild( bld );
                return;
            }

            Plugin plugin = bld.findPluginById( groupId, artifactId );
            if( plugin == null ) {
                plugin = fact.createPlugin();
                plugin.setArtifactId( artifactId );
                plugin.setGroupId( groupId );
                plugin.setVersion( PluginPropertyUtils.getPluginVersion( nbMaven.getMavenProject(), groupId, artifactId ) );
                bld.addPlugin( plugin );
            }
            Configuration conf = plugin.getConfiguration();
            if( conf == null ) {
                conf = fact.createConfiguration();
                plugin.setConfiguration( conf );
            }
            if( conf != null ) {
                conf.setSimpleParameter( name, value );
            }
        }
    };
}
