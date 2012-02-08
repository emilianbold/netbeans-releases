/*
 * Copyright (c) 2011, Oracle. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Oracle nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package embedded.telephonedirectory.client;

import org.glassfish.embeddable.CommandResult;
import org.glassfish.embeddable.CommandRunner;
import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.embeddable.archive.ScatteredArchive;

/**
 * This is a standalone client program that embeds GlassFish.
 * <p/>
 * After embedding GlassFish, JDBC pool and resource is created
 * programatically using org.glassfish.embeddable.CommandRunner API.
 * <p/>
 * JDBC pool is created with embedded datasource.
 * Hence, database is also embedded as part of this program itself.
 * <p/>
 * Once the JDBC pool and resource required for the application is created,
 * the application is deployed using org.glassfish.embeddable.Deployer API.
 * <p/>
 * Once the user finishes accessing the application, the application is
 * undeployed and embedded GlassFish is stopped.
 * 
 * @author Bhavanishankar Sapaliga
 * @author Sakshi Jain 
 * @author Bhakti Mehta
 */
public class EmbeddedTelephoneDirectory {
    
    private File basedir = null;

    /**
     * Main method of this standalone program.
     * Creates new instance of EmbeddedTelephoneDirectory and invokes run() method.
     *
     * @param args No parameter is required.
     */
    public static void main(String[] args) {
        try {
            new EmbeddedTelephoneDirectory().run();
        } catch (GlassFishException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Embeds GlassFish, Creates JDBC pool/resource, Deploys application.
     * <p/>
     * Pseudocode that outlines what is done here (see the java source file for full code).
     * <p/>
     * <blockquote><pre>
     *      GlassFIsh glassfish = GlassFishRuntime.bootstrap().newGlassFish(); // create embedded GlassFish instance
     *      glassfish.start();  // start embedded GlassFish
     *      glassfish.getCommandRunner().run(...); // create JDBC pool/resource with embedded datasource.
     *      glassfish.getDeployer().deploy(...): // deploy the web application.
     *      // access the application
     *      glassfish.getDeployer().undeploy(...); // undeploy the web application
     *      glassfish.dispose(); // stop/dispose the embedded GlassFish.
     * </pre></blockquote>
     *
     * @throws GlassFishException
     */
    public void run() throws GlassFishException {
        GlassFish glassfish = null;
        try {
            /**
             * Start GlassFish in embedded mode to run at 9590 http port.
             */
            GlassFishProperties glassfishProperties = new GlassFishProperties();
            int httpPort = 9590;
            glassfishProperties.setPort("http-listener", httpPort);
            glassfish = GlassFishRuntime.bootstrap().newGlassFish(glassfishProperties);
            glassfish.start();

            addShutdownHook(glassfish); // Shutdown GlassFish when Ctrl+C pressed from terminal.

            /**
             * Programatically create JDBC connection pool and resource with
             * embedded datasource.
             */
            CommandRunner cr = glassfish.getCommandRunner();
            CommandResult result = cr.run("create-jdbc-connection-pool",
                    "--datasourceclassname=org.apache.derby.jdbc.EmbeddedDataSource",
                    "--restype=javax.sql.DataSource",
                    "--property=DatabaseName=embedded-samples:connectionAttributes=;create\\=true",
                    "telephone_directory_pool");
            System.out.println(result.getOutput());
            result = cr.run("create-jdbc-resource",
                    "--connectionpoolid=telephone_directory_pool",
                    "jdbc/__telephone_directory");
            System.out.println(result.getOutput());

            String contextRoot = "td";
            
            Deployer deployer = glassfish.getDeployer();
            
            URI archive = null;
            try {
                archive = createScatteredArchive();
            } catch (IOException ex) {
                Logger.getLogger(EmbeddedTelephoneDirectory.class.getName()).log(Level.SEVERE, null, ex);
            }
            /*
             * Deploy the application by specifying the custom contextroot.
             */
            String appName = deployer.deploy(archive, "--contextroot=" + contextRoot);

            System.out.println("Deployed [ " + appName + " ]");
            System.out.println("\nAccess the application at http://localhost:" +
                    httpPort + "/" + contextRoot + "\n");

            waitForQuitCommand();

            if (appName != null) {
                deployer.undeploy(appName);
            }   
            cleanup();
            
        } finally {
            if (glassfish != null) {
                glassfish.dispose();
            }
        }
    }
    
    
    private void cleanup(){
        File embeddedsamples = new File(basedir, "embedded-samples");
        cleanup(embeddedsamples);
        embeddedsamples.delete();

        File derbylog = new File(basedir, "derby.log");
        cleanup(derbylog);
    }
    
    private void cleanup(File f){
        
        if (f.isDirectory()) {
            List<File> files = Arrays.asList(f.listFiles());
            boolean delete = false;
            for (File file : files) {
                if (file.isDirectory()) {
                    cleanup(file);
                }
                delete = file.delete();
               
            }
        } else {
            f.delete();
        }
        
    }

    private static void waitForQuitCommand() {
        while (true) {
            System.out.println("Type quit when you finish accessing the application");
            String command = null;
            try {
                command = new BufferedReader(
                        new InputStreamReader(System.in)).readLine();
            } catch (Exception ex) {
                ex.printStackTrace();
                break;
            }
            if (command != null && command.trim().equalsIgnoreCase("quit")) {
                break;
            }
        }
    }

    private void addShutdownHook(final GlassFish gf) {
        Runtime.getRuntime().addShutdownHook(
                new Thread("GlassFish Shutdown Hook") {
                    public void run() {
                        try {
                            if (gf != null) {
                                gf.dispose();
                            }
                        } catch (Exception ex) {
                        }
                    }
                });
    }

    private URI createScatteredArchive() throws IOException  {
        
        
        File currentDirectory = new File(System.getProperty("user.dir"));
        
        if (currentDirectory.getName().equals("dist")){
            basedir = currentDirectory.getParentFile();
        } else {
            basedir = currentDirectory;
        }
        // Create a scattered web application.
        ScatteredArchive archive = new ScatteredArchive("embedded-telephone_directory", ScatteredArchive.Type.WAR, new File(basedir,"resources"));
        
        try {
            // target/classes directory contains my complied servlets
            archive.addClassPath(new File(new File((basedir),"target"), "classes"));
            
        } catch (IOException ex) {
            Logger.getLogger(EmbeddedTelephoneDirectory.class.getName()).log(Level.SEVERE, null, ex);
        }

        return archive.toURI();
    }
}

