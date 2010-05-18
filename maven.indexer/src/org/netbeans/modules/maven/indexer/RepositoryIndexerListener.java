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
 * Contributor(s): theanuradha@netbeans.org
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.indexer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.ArtifactScanningListener;
import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.index.ScanningResult;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 *
 * @author Anuradha G
 */
public class RepositoryIndexerListener implements ArtifactScanningListener {

    private final IndexingContext indexingContext;
    private final NexusIndexer nexusIndexer;
    private long tstart;
    
    private int count;
   private ProgressHandle handle;
    
    private RepositoryInfo ri;
    /*Debug*/
    private final boolean DEBUG = false;
     private InputOutput io;
    private OutputWriter writer;
    public RepositoryIndexerListener(NexusIndexer nexusIndexer, IndexingContext indexingContext) {
        this.indexingContext = indexingContext;
        this.nexusIndexer = nexusIndexer;
        ri = RepositoryPreferences.getInstance().getRepositoryInfoById(indexingContext.getId());

        if (DEBUG) {
            io = IOProvider.getDefault().getIO("Indexing " +(ri!=null? ri.getName():indexingContext.getId()), true); //NOI18N
            writer = io.getOut();
        }
    }

    public void scanningStarted(IndexingContext ctx) {
        handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(RepositoryIndexerListener.class, "LBL_Indexing") + (ri!=null? ri.getName() : indexingContext.getId()));
        if (DEBUG) {
            writer.println("Indexing Repo   : " + (ri!=null? ri.getName():ctx.getId())); //NOI18N
            writer.println("Index Directory : " + ctx.getIndexDirectory().toString());//NOI18N
            writer.println("--------------------------------------------------------");//NOI18N
            writer.println("Scanning started at " + SimpleDateFormat.getInstance().format(new Date()));//NOI18N
        }
        handle.start();
        handle.switchToIndeterminate();
        tstart = System.currentTimeMillis();
    }

    public void artifactDiscovered(ArtifactContext ac) {
        count++;


        ArtifactInfo ai = ac.getArtifactInfo();

        if (DEBUG) {
            if ("maven-plugin".equals(ai.packaging)) {//NOI18N
                writer.printf("Plugin: %s:%s:%s - %s %s\n", //NOI18N
                        ai.groupId,
                        ai.artifactId,
                        ai.version,
                        ai.prefix,
                        "" + ai.goals);//NOI18N
            }


            // ArtifactInfo ai = ac.getArtifactInfo();
            writer.printf("  %6d %s\n", count, formatFile(ac.getPom()));//NOI18N
        }
        handle.progress(ac.getArtifactInfo().groupId + ":" 
                      + ac.getArtifactInfo().artifactId + ":" 
                      + ac.getArtifactInfo().version);

    }

    public void artifactError(ArtifactContext ac, Exception e) {
        if (DEBUG) {
            writer.printf("! %6d %s - %s\n", count, formatFile(ac.getPom()), e.getMessage());//NOI18N

            writer.printf("         %s\n", formatFile(ac.getArtifact()));//NOI18N
            e.printStackTrace(writer);
        }

    }

    private String formatFile(File file) {
        return file.getAbsolutePath().substring(indexingContext.getRepository().getAbsolutePath().length() + 1);
    }

    public void scanningFinished(IndexingContext ctx, ScanningResult result) {
        if (DEBUG) {
            writer.println("Scanning ended at " + SimpleDateFormat.getInstance().format(new Date())); //NOI18N

            if (result.hasExceptions()) {
                writer.printf("Total scanning errors: %s\n", result.getExceptions().size()); //NOI18N
            }

            writer.printf("Total files scanned: %s\n", result.getTotalFiles()); //NOI18N

            long t = System.currentTimeMillis() - tstart;

            long s = t / 1000L;

            if (t > 60 * 1000) {
                long m = t / 1000L / 60L;

                writer.printf("Total time: %d min %d sec\n", m, s - (m * 60)); //NOI18N
            } else {
                writer.printf("Total time: %d sec\n", s); //NOI18N

            }
        }
        
        handle.finish();
   }
}
