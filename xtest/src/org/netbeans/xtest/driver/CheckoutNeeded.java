/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.xtest.driver;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.types.*;
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.io.File;

/**
 * @author  Michal Zlamal
 */
public class CheckoutNeeded extends Task {
    String modules = null;
    String repos = null;
    String action = "checkout";             //NOI18N
    boolean quiet = true;

    /** List of modules to checkout devided by comma (,) */
    public void setModules( String modules ) {
        this.modules = modules;
    }
    
    /** List of CVS Repositories with assigned local directory. Repo and dir is
     * separated by '-'.
     * example: "cvs.netbeans.org:/cvs-c:\\src\\nb_all,othercvs:/cvs-c:\\src\\xy_all"
     */
    public void setRepos( String repos ) {
        this.repos = repos;
    }
 
    public void setQuiet( boolean quiet ) {
        this.quiet = quiet;
    }
    
    public void setAction( String action ) {
        this.action = action;
    }

    private class Repository {
        String directory = null;
        String root = null;
    }
    
    private class Module {
        String name = null;
        String branch = null;
    }
   
    public void execute() throws BuildException {
        if (modules == null) throw new BuildException("You must tell what modules to checkout");            //NOI18N
        if (repos == null) throw new BuildException("You mast specify repository directories and roots");   //NOI18N

        Hashtable repositories = new Hashtable();
      
        StringTokenizer tokenizer = new StringTokenizer( repos, "," );
        
        while (tokenizer.hasMoreElements()) {
            String repo = tokenizer.nextToken();
            String directory = repo.substring( repo.indexOf('-')+1);
            repo = repo.substring( 0, repo.indexOf('-') );
            
//          System.out.println( "Repository - " + repo + " to dir " + directory);
            
            repositories.put( repo, directory );
        }
        
        tokenizer = new StringTokenizer(modules,",");
        while (tokenizer.hasMoreElements()) {
            String module = tokenizer.nextToken();          
            String branches = this.getProject().getProperty( module + ".branch" ); //NOI18N
            if (branches == null) throw new BuildException("Module "+module+" hasn't specified branches");  //NOI18N
            StringTokenizer branchTokens = new StringTokenizer( branches, "," );
            while (branchTokens.hasMoreElements()) {
                String repository = branchTokens.nextToken();
                String branch = repository.substring(repository.indexOf('{')+1, repository.indexOf('}'));
                repository = repository.substring(0,repository.indexOf('{'));
                String directory = (String) repositories.get( repository );
                
                log( module + " " + repository + ":" + branch + " in to " + directory ); //NOI18N
                
                Cvs cvs = (Cvs) this.getProject().createTask( "cvs" ); //NOI18N
                cvs.setOwningTarget( this.getOwningTarget() );
                cvs.setCvsRoot( ":pserver:anoncvs@" + repository ); //NOI18N
                
                File destfile = new File(directory);
                if (!destfile.exists()) destfile.mkdirs();
                cvs.setDest(destfile);
                
                if (!branch.equals("trunk")) //NOI18N
                    cvs.setTag( branch );
                cvs.setPackage( module );
                cvs.setQuiet( quiet );
                
                cvs.setCommand( "-z6 " + action ); //NOI18N
                
                cvs.execute();
                
                log( module + " OK" ); //NOI18N
                
            }
        }
    }
}