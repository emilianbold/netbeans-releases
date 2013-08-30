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

import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.openide.util.Lookup;
import org.openide.util.lookup.ProxyLookup;

/**
 * Category provider that delegates to a different category provider - if the Maven
 * project POM contains the desired plugin.
 *
 * @author  S. Aubrecht
 */
final class ProxyCategoryProvider implements ProjectCustomizer.CompositeCategoryProvider {

    private final Map args;

    public ProxyCategoryProvider() {
        this( new HashMap(0) );
    }

    private ProxyCategoryProvider( Map args ) {
        this.args = args;
    }

    static ProjectCustomizer.CompositeCategoryProvider create( Map args ) {
        return new ProxyCategoryProvider( args );
    }

    @Override
    public ProjectCustomizer.Category createCategory( Lookup lkp ) {
        if( isSupportedProject( lkp ) ) {
            MavenCategoryProvider theProvider = createProvider( lkp );
            if( null != theProvider ) {
                return theProvider.createCategory( lkp );
            }
        }
        return null;
    }

    @Override
    public JComponent createComponent( ProjectCustomizer.Category ctgr, Lookup lkp ) {
        MavenCategoryProvider theProvider = createProvider( lkp );
        if( null != theProvider ) {
            return theProvider.createComponent( ctgr, lkp );
        }
        return null;
    }

    private boolean isSupportedProject( Lookup lkp ) {
        Project prj = lkp.lookup( Project.class );
        NbMavenProject nbMaven = prj.getLookup().lookup( NbMavenProject.class );
        if( null == nbMaven )
            return false;
        String groupId = getGroupId();
        String artifactId = getArtifactId();
        if( null == groupId || null == artifactId )
            return false;
        return null != PluginPropertyUtils.getPluginVersion( nbMaven.getMavenProject(), groupId, artifactId );
    }

    private MavenCategoryProvider createProvider( Lookup lkp ) {
        MavenCategoryProvider res = ( MavenCategoryProvider) args.get( "categoryProvider" );
        Project prj = lkp.lookup( Project.class );
        res.setPluginParameters( MavenPluginParameters.create( new ProxyLookup( lkp, prj.getLookup() ), getGroupId(), getArtifactId()));
        return res;
    }

    private String getGroupId() {
        return (String) args.get( "groupId" ); //NOI18N
    }

    private String getArtifactId() {
        return (String) args.get( "artifactId" ); //NOI18N
    }
}
