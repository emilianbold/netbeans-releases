/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.makeproject;

import java.io.File;
import org.netbeans.api.project.ProjectManager;
import org.openide.util.Mutex;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.cnd.api.utils.CndFileVisibilityQuery;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 * SharabilityQueryImplementation for j2seproject with multiple sources
 */
public class MakeSharabilityQuery implements SharabilityQueryImplementation {

    private File baseDirFile;
    private String baseDir;
    private int baseDirLength;
    private boolean privateShared;
    private ConfigurationDescriptorProvider projectDescriptorProvider;
    private static final boolean IGNORE_BINARIES = CndUtils.getBoolean("cnd.vcs.ignore.binaries", true);

    MakeSharabilityQuery(ConfigurationDescriptorProvider projectDescriptorProvider, File baseDirFile) {
        this.projectDescriptorProvider = projectDescriptorProvider;
        this.baseDirFile = baseDirFile;
        this.baseDir = baseDirFile.getPath();
        this.baseDirLength = this.baseDir.length();
        privateShared = false;
    }

    /**
     * Check whether a file or directory should be shared.
     * If it is, it ought to be committed to a VCS if the user is using one.
     * If it is not, it is either a disposable build product, or a per-user
     * private file which is important but should not be shared.
     * @param file a file to check for sharability (may or may not yet exist)
     * @return one of {@link org.netbeans.api.queries.SharabilityQuery}'s constants
     */
    @Override
    public int getSharability(final File file) {
        //ConfigurationDescriptor configurationDescriptor = projectDescriptorProvider.getConfigurationDescriptor();
        //if (configurationDescriptor != null && configurationDescriptor.getModified()) {
        //    // Make sure all sharable files are saved on disk
        //    // See IZ http://www.netbeans.org/issues/show_bug.cgi?id=153504
        //    configurationDescriptor.save();
        //}
        Integer ret = ProjectManager.mutex().readAccess(new Mutex.Action<Integer>() {

            @Override
            public Integer run() {
                synchronized (MakeSharabilityQuery.this) {
                    if (IGNORE_BINARIES && CndFileVisibilityQuery.getDefault().isIgnored(file))  {
                        return SharabilityQuery.NOT_SHARABLE;
                    }
                    boolean sub = file.getPath().startsWith(baseDir);
                    if (!sub) {
                        return Integer.valueOf(SharabilityQuery.UNKNOWN);
                    }
                    if (file.getPath().equals(baseDir)) {
                        return Integer.valueOf(SharabilityQuery.MIXED);
                    }
                    if (file.getPath().length() <= baseDirLength + 1) {
                        return Integer.valueOf(SharabilityQuery.UNKNOWN);
                    }
                    String subString = file.getPath().substring(baseDirLength + 1);
                    if (subString.equals(MakeConfiguration.NBPROJECT_FOLDER)) // NOI18N
                    {
                        return Integer.valueOf(SharabilityQuery.MIXED);
                    } else if (subString.equals("Makefile")) // NOI18N
                    {
                        return Integer.valueOf(SharabilityQuery.SHARABLE);
                    } else if (subString.equals(MakeConfiguration.NBPROJECT_FOLDER + File.separator + MakeConfiguration.CONFIGURATIONS_XML)) // NOI18N
                    {
                        return Integer.valueOf(SharabilityQuery.SHARABLE);
                    } else if (subString.equals(MakeConfiguration.NBPROJECT_FOLDER + File.separator + "private")) // NOI18N
                    {
                        return Integer.valueOf(privateShared ? SharabilityQuery.SHARABLE : SharabilityQuery.NOT_SHARABLE); // see IZ 121796, IZ 109580 and IZ 109573
                    } else if (subString.equals(MakeConfiguration.NBPROJECT_FOLDER + File.separator + "project.properties")) // NOI18N
                    {
                        return Integer.valueOf(SharabilityQuery.SHARABLE);
                    } else if (subString.equals(MakeConfiguration.NBPROJECT_FOLDER + File.separator + MakeConfiguration.PROJECT_XML)) // NOI18N
                    {
                        return Integer.valueOf(SharabilityQuery.SHARABLE);
                    } else if (subString.startsWith(MakeConfiguration.NBPROJECT_FOLDER + File.separator + "Makefile-")) // NOI18N
                    {
                        return Integer.valueOf(SharabilityQuery.SHARABLE);
                    } else if (subString.startsWith(MakeConfiguration.NBPROJECT_FOLDER + File.separator + "Package-")) // NOI18N
                    {
                        return Integer.valueOf(SharabilityQuery.SHARABLE);
                    } else if (subString.startsWith(MakeConfiguration.NBPROJECT_FOLDER + File.separator + "qt-")) // NOI18N
                    {
                        return Integer.valueOf(subString.endsWith(".pro")? SharabilityQuery.SHARABLE : SharabilityQuery.NOT_SHARABLE); // NOI18N
                    } else if (subString.startsWith(MakeConfiguration.BUILD_FOLDER + File.separator)) // NOI18N
                    {
                        return Integer.valueOf(SharabilityQuery.NOT_SHARABLE);
                    } else if (subString.startsWith(MakeConfiguration.DIST_FOLDER + File.separator)) // NOI18N
                    {
                        return Integer.valueOf(SharabilityQuery.NOT_SHARABLE);
                    }
                    return Integer.valueOf(SharabilityQuery.UNKNOWN);
                }
            }
        });
        return ret.intValue();
    }

    public void setPrivateShared(boolean privateShared) {
        this.privateShared = privateShared;
    }

    public boolean getPrivateShared() {
        return privateShared;
    }
}
