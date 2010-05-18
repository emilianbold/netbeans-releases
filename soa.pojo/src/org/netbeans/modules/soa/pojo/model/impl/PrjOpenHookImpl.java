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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.soa.pojo.model.impl;

import java.math.BigDecimal;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.soa.pojo.schema.POJOProvider;
import org.netbeans.modules.soa.pojo.schema.POJOProviders;
import org.netbeans.modules.soa.pojo.schema.POJOs;
import org.netbeans.modules.soa.pojo.schema.Pojo;
import org.netbeans.modules.soa.pojo.util.NBPOJOConstants;
import org.netbeans.modules.soa.pojo.util.Util;
import org.netbeans.modules.soa.pojo.wizards.POJOHelper;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;

/**
 *
 * @author gpatil
 */
//@ProjectServiceProvider(service = ProjectOpenedHook.class, projectType = {
//    "org-netbeans-modules-java-j2seproject"
//})
public class PrjOpenHookImpl extends ProjectOpenedHook {

    private final Project prj;

    public PrjOpenHookImpl(Project project) {
        this.prj = project;
    }

    @Override
    protected void projectClosed() {
    }

    @Override
    protected void projectOpened() {
//        Runnable run = new Runnable() {
//            public void run() {
//            }
//        };
//        RequestProcessor.getDefault().post(run);

        try {
            // Migrate older config file, note older config version.
            // Migrate the builder script if older config version was not latest
            //     and # of services > 0.
            // XXX TODO scan for services, if new ones found add to the list.

            boolean migrateCfg = false;
            BigDecimal nVersion = NBPOJOConstants.LATEST_CFG_VERSION;
            POJOs pojos = Util.getPOJOs(prj);
            if (pojos != null) {
                BigDecimal pVersion = pojos.getVersion();
                if (pVersion == null){
                    pVersion = new BigDecimal(0);
                }

                // Old style, migrate.
                if (pojos.sizePojo() > 0) {
                    migrateCfg = true;
                    POJOHelper.unregisterOldPOJOBuildScript(prj);
                    POJOHelper.registerPOJOBuildScript(prj);

                    Pojo[] ops = pojos.getPojo();
                    POJOProviders ps = new POJOProviders();
                    POJOProvider po = null;
                    pojos.setPOJOProviders(ps);
                    pojos.setPojo(null);

                    for (Pojo p : ops) {
                        po = new POJOProvider();
                        po.setClassName(p.getClassName());
                        po.setEpName(p.getEpName());
                        po.setOrigWsdlLocation(p.getOrigWsdlLocation());
                        po.setOrigWsdlLocationType(p.getOrigWsdlLocationType());
                        po.setPackage(p.getPackage());
                        po.setUpdateWsdlDuringBuild(p.isUpdateWsdlDuringBuild());
                        po.setWsdlLocation(p.getWsdlLocation());
                        ps.addPOJOProvider(po);
                    }
                }

                POJOProviders pps = pojos.getPOJOProviders();
                if ((pps != null) && (pps.sizePOJOProvider() > 0)) {
                    if (NBPOJOConstants.LATEST_CFG_VERSION.compareTo(pVersion) > 0) {
                        migrateCfg = true;
                        POJOHelper.getPOJOBuildFO(prj, true, true);
                    } else {
                        POJOHelper.getPOJOBuildFO(prj, true, false);
                    }
                }
            }

            if (migrateCfg) {
                POJOHelper.unregisterPOJOAntExt(prj);
                //cfg file name is already changed by Util.getPOJOs(prj);
                //rename old build script file name and remove and re-register.
                pojos.setVersion(nVersion);
                Util.savePOJOs(prj, pojos);
                POJOHelper.unregisterPOJOAntExt(prj);
                POJOHelper.setProjPros(prj);
                POJOHelper.registerPOJOBuildScript(prj);
                Util.fireCfgFileChangedEvent(prj, pojos);
            }

            // Update privat.properties for headless run.
            ProjectManager.mutex().writeAccess(
                    new Mutex.Action() {
                        public Object run() {
                            POJOHelper.setPrivateProjPros(prj);
                            return null;
                        }
                    });
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }
}
