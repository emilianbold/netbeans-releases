/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.ui;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiService.Type;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.team.ide.spi.IDEProject;
import org.netbeans.modules.team.ide.spi.IDEServices;
import org.netbeans.modules.team.ide.spi.ProjectServices;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.netbeans.modules.team.ui.spi.SourceAccessor;
import org.netbeans.modules.team.ui.spi.SourceHandle;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Milan Kubec, Jan Becicka
 */
@ServiceProvider(service=SourceAccessor.class)
public class SourceAccessorImpl extends SourceAccessor<KenaiProject> {
    private static SourceAccessor instance;

    public static SourceAccessor getDefault() {
        if(instance == null) {
            instance = new SourceAccessorImpl();
        }
        return instance;
    }

    private Map<SourceHandle,ProjectAndFeature> handlesMap = new HashMap<SourceHandle,ProjectAndFeature>();

    @Override
    public Class<KenaiProject> type() {
        return KenaiProject.class;
    }
    
    @Override
    public List<SourceHandle> getSources(ProjectHandle<KenaiProject> prjHandle) {

        KenaiProject project = prjHandle.getTeamProject();
        List<SourceHandle> handlesList = new ArrayList<SourceHandle>();

        if (project != null) {
            try {
                for (KenaiFeature feature : project.getFeatures(Type.SOURCE)) {
                    SourceHandle srcHandle = new SourceHandleImpl(prjHandle, feature);
                    handlesList.add(srcHandle);
                    handlesMap.put(srcHandle, new ProjectAndFeature(prjHandle.getTeamProject(), feature, ((SourceHandleImpl) srcHandle).getExternalScmType()));
                }
            } catch (KenaiException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return handlesList.isEmpty() ? Collections.<SourceHandle>emptyList() : handlesList;

    }

    @Override
    public Action getOpenSourcesAction(SourceHandle srcHandle) {
        return new GetSourcesFromKenaiAction(handlesMap.get(srcHandle), srcHandle);
    }

    @Override
    public Action getDefaultAction(SourceHandle srcHandle) {
        return new GetSourcesFromKenaiAction(handlesMap.get(srcHandle), srcHandle);
    }

    @Override
    public Action getDefaultAction(final IDEProject ideProject) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProjectServices projects = Lookup.getDefault().lookup(ProjectServices.class);
                if (!projects.openProject(ideProject.getURL())) {
                    ideProject.notifyDeleted();
                }
            }
        };
    }

    @Override
    public Action getOpenOtherAction(final SourceHandle src) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProjectServices projects = Lookup.getDefault().lookup(ProjectServices.class);
                projects.openOtherProject(src.getWorkingDirectory());
            }
        };
    }

    @Override
    public Action getOpenFavoritesAction(final SourceHandle src) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IDEServices ide = Lookup.getDefault().lookup(IDEServices.class);
                ide.openInFavorites(src.getWorkingDirectory());
             }
        };
    }

    public static class ProjectAndFeature {

        public KenaiProject kenaiProject;
        public KenaiFeature feature;
        public String externalScmType;

        public ProjectAndFeature(KenaiProject name, KenaiFeature ftr, String externalScmType) {
            kenaiProject = name;
            feature = ftr;
            this.externalScmType=externalScmType;
        }
    }

}
