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
 * Contributor(s): theanuradha@netbeans.org
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.indexer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import hidden.org.codehaus.plexus.util.IOUtil;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.ArtifactScanningListener;
import org.sonatype.nexus.index.IndexUtils;
import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.scan.ScanningResult;

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
    private boolean debug;
     private InputOutput io;
    private OutputWriter writer;
    private boolean createZip;
    public RepositoryIndexerListener(NexusIndexer nexusIndexer, IndexingContext indexingContext, boolean createZip) {
        this.indexingContext = indexingContext;
        this.nexusIndexer = nexusIndexer;
        this.createZip = createZip;
         ri = RepositoryPreferences.getInstance().getRepositoryInfoById(indexingContext.getId());

        if (debug) {
            io = IOProvider.getDefault().getIO("Indexing " +(ri!=null? ri.getName():indexingContext.getId()), true);
            writer = io.getOut();
        }
    }

    public void scanningStarted(IndexingContext ctx) {
        handle = ProgressHandleFactory.createHandle("Indexing Repo   : " + (ri!=null? ri.getName() : indexingContext.getId()));
        if (debug) {
            writer.println("Indexing Repo   : " + (ri!=null? ri.getName():ctx.getId()));
            writer.println("Index Directory : " + ctx.getIndexDirectory().toString());
            writer.println("--------------------------------------------------------");
            writer.println("Scanning started at " + SimpleDateFormat.getInstance().format(new Date()));
        }
        handle.start();
        handle.switchToIndeterminate();
        tstart = System.currentTimeMillis();
    }

    public void artifactDiscovered(ArtifactContext ac) {
        count++;


        ArtifactInfo ai = ac.getArtifactInfo();

        if (debug) {
            if ("maven-plugin".equals(ai.packaging)) {
                writer.printf("Plugin: %s:%s:%s - %s %s\n", //
                        ai.groupId,
                        ai.artifactId,
                        ai.version,
                        ai.prefix,
                        "" + ai.goals);
            }


            // ArtifactInfo ai = ac.getArtifactInfo();
            writer.printf("  %6d %s\n", count, formatFile(ac.getPom()));
        }
        handle.progress(ac.getArtifactInfo().groupId + ":" 
                      + ac.getArtifactInfo().artifactId + ":" 
                      + ac.getArtifactInfo().version);

    }

    public void artifactError(ArtifactContext ac, Exception e) {
        if (debug) {
            writer.printf("! %6d %s - %s\n", count, formatFile(ac.getPom()), e.getMessage());

            writer.printf("         %s\n", formatFile(ac.getArtifact()));
            e.printStackTrace(writer);
        }

    }

    private String formatFile(File file) {
        return file.getAbsolutePath().substring(indexingContext.getRepository().getAbsolutePath().length() + 1);
    }

    public void scanningFinished(IndexingContext ctx, ScanningResult result) {
        if (debug) {
            writer.println("Scanning ended at " + SimpleDateFormat.getInstance().format(new Date()));

            if (result.hasExceptions()) {
                writer.printf("Total scanning errors: %s\n", result.getExceptions().size());
            }

            writer.printf("Total files scanned: %s\n", result.getTotalFiles());

            long t = System.currentTimeMillis() - tstart;

            long s = t / 1000L;

            if (t > 60 * 1000) {
                long m = t / 1000L / 60L;

                writer.printf("Total time: %d min %d sec\n", m, s - (m * 60));
            } else {
                writer.printf("Total time: %d sec\n", s);

            }
        }
        
        if (createZip) {
            SimpleDateFormat df = new SimpleDateFormat( IndexingContext.INDEX_TIME_FORMAT );
            Properties info = new Properties();
            info.setProperty( IndexingContext.INDEX_FILE, df.format( ctx.getTimestamp() ) );
            writeIndexInfo( info );
            createIndexArchive();
        }
        handle.finish();

    }
    
        private void writeIndexInfo( Properties info ) 
        {
            File indexInfo = new File( indexingContext.getIndexDirectoryFile(), IndexingContext.INDEX_FILE + ".properties" );
          
            OutputStream os = null;

            try
            {
                os = new FileOutputStream( indexInfo );
                
                info.store(os, null);
            } 
            catch ( IOException e ) 
            {
                System.err.println( "Unable to create index info; " + e.getMessage() );
  
                if ( debug )
                {
                    e.printStackTrace();
                }
            }
            finally
            {
                IOUtil.close( os );
            }
        }
    
    
    
    private void createIndexArchive()
        {
            //File indexArchive = new File( indexingContext.getId() + ".zip" );
            File indexArchive = new File( indexingContext.getIndexDirectoryFile(), IndexingContext.INDEX_FILE + ".zip" );

            OutputStream os = null;

            try
            {
                os = new BufferedOutputStream( new FileOutputStream( indexArchive ), 4096 );

                IndexUtils.packIndexArchive( indexingContext, os );
            }
            catch ( IOException e )
            {
                System.err.println( "Unable to create index archive; " + e.getMessage() );

                if ( debug )
                {
                    e.printStackTrace();
                }
            }
            finally
            {
                if ( os != null )
                {
                    try
                    {
                        os.close();
                    }
                    catch ( IOException e )
                    {
                        System.err.println( "Unable to close zip output stream; " + e.getMessage() );
                    }
                }
            }

            System.err.printf( "Index archive size: %.2f Mb\n", ( indexArchive.length() / 1024f / 1024f ) );
        }
}

       

