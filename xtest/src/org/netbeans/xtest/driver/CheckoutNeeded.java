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
import java.util.HashSet;
import java.util.Iterator;
import org.xml.sax.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

/**
 * @author  Michal Zlamal
 */
public class CheckoutNeeded extends Task {
    String modules = null;
    String repos = null;
    String action = "checkout";             //NOI18N
    boolean quiet = true;
    
    private Hashtable repositories = new Hashtable();

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
    
    public void checkout(String modules) {
        StringTokenizer tokenizer = new StringTokenizer(modules,",");
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
                if (repository.indexOf("@") != -1)    //NOI18N
                    cvs.setCvsRoot( repository );
                else
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

    public void execute() throws BuildException {
        if (repos == null) throw new BuildException("You mast specify repository directories and roots");   //NOI18N
        
        StringTokenizer tokenizer = new StringTokenizer( repos, "," );
        
        while (tokenizer.hasMoreElements()) {
            String repo = tokenizer.nextToken();
            String directory = repo.substring( repo.indexOf('-')+1);
            repo = repo.substring( 0, repo.indexOf('-') );
//          System.out.println( "Repository - " + repo + " to dir " + directory);
            repositories.put( repo, directory ); 
        }
        
        if (modules == null) modules = findModules();
        
        checkout(modules);
    }
    
    private String findModules() {
       Hashtable module_branches = new Hashtable();
        
       HashSet modules_set = new HashSet();
       HashSet instances = InstancePropertiesParser.getPostfixes(getProject());
       Iterator it = instances.iterator();
       while (it.hasNext()) {
          String postfix = (String)it.next();   
          String config = getProject().getProperty(InstancePropertiesParser.CONFIG + postfix);
          String testroot = getProject().getProperty(InstancePropertiesParser.TEST_ROOT + postfix);
          String instance = getProject().getProperty(InstancePropertiesParser.INSTANCE + postfix);
          String cvs_root = getProject().getProperty(InstancePropertiesParser.CVS_ROOT + postfix);
          String cvs_workdir = getProject().getProperty(InstancePropertiesParser.CVS_WORKDIR + postfix);
          String master_config = getProject().getProperty(InstancePropertiesParser.MASTER_CONFIG + postfix);
          if (testroot==null) testroot = "";
          else 
              if (!testroot.endsWith("/"))
                  testroot = testroot+"/";
          if (config == null) throw new BuildException("Property '"+InstancePropertiesParser.CONFIG+postfix+"' is not set.");
          if (instance == null) throw new BuildException("Property '"+InstancePropertiesParser.INSTANCE+postfix+"' is not set.");
          if (cvs_root == null) throw new BuildException("Property '"+InstancePropertiesParser.CVS_ROOT+postfix+"' is not set.");
          if (cvs_workdir == null) throw new BuildException("Property '"+InstancePropertiesParser.CVS_WORKDIR+postfix+"' is not set.");
          
          String instance_dir = cvs_workdir + File.separator + instance.replace('/',File.separatorChar);
          if (master_config == null) master_config = instance_dir + File.separator + "master-config.xml";
          
          if (getProject().getProperty(instance+".branch") == null) 
              getProject().setProperty(instance+".branch",cvs_root);
          checkout(instance);
          
          readMasterConfig(modules_set,new File(master_config),config,testroot);
          log("Master config read: "+modules_set);
          Iterator mod = modules_set.iterator();
          while (mod.hasNext()) {
             String module = (String)mod.next();   
             if (getProject().getProperty(module+"/test.branch") == null) {
                 String branches = (String)module_branches.get(module);
                 if (branches == null)
                     module_branches.put(module,cvs_root);
                 else 
                     branches = branches + "," + cvs_root;
             }
          }
       }
       
       Iterator mb = module_branches.entrySet().iterator();
       while (mb.hasNext()) {
           Map.Entry entry = (Map.Entry)mb.next();
           String name = (String)entry.getKey()+"/test.branch";
           getProject().setProperty(name,(String)entry.getValue());
       }
          
       StringBuffer buff = new StringBuffer();
       Iterator ms = modules_set.iterator();
       while (ms.hasNext()) {
            if (buff.length() != 0) buff.append(",");
            buff.append((String)ms.next());
            buff.append("/test");
       }
       return buff.toString();
    }
    
    private void readMasterConfig(HashSet set, File file, String config, String prefix) {
        Document doc = null;
        StringBuffer buff = new StringBuffer();
        
        log("Reading config "+config+" from "+file.getAbsolutePath());
        
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            db.setEntityResolver(new org.netbeans.xtest.XTestEntityResolver());
            doc = db.parse(file);
        } catch (SAXException saxe) {
            throw new BuildException( saxe.getMessage() );
        } catch (ParserConfigurationException pce) {
            throw new BuildException( pce.getMessage() );
        } catch (IOException ioe) {
            throw new BuildException( ioe.getMessage() );
        }
        
        boolean found = false;
        NodeList nl = doc.getElementsByTagName( "config" );
        Node node = null;
        for (int i = 0; i < nl.getLength(); i++ ) {
            node = nl.item(i);
            String c_name = node.getAttributes().getNamedItem( "name" ).getNodeValue();
            if (c_name.equals(config)) {
                found = true;
                break;
            }
        }
        if (!found) throw new BuildException("Configuration '"+config+"' was not found in "+file.getAbsolutePath(), location);
        Node modules_attrib = node.getAttributes().getNamedItem( "modules" );
        if (modules_attrib != null) {
            fillHashSet(set,prefix,modules_attrib.getNodeValue());
        }
        NodeList nl2 = node.getChildNodes();
        Node node2 = null;
        for (int i = 0; i < nl2.getLength(); i++ ) {
            node2 = nl2.item(i);
            String module_name = node2.getNodeName();
            if (module_name.equals("module")) {
                set.add(prefix + node2.getAttributes().getNamedItem( "name" ).getNodeValue());
            }
            if (module_name.equals("testtype")) {
                fillHashSet(set,prefix,node2.getAttributes().getNamedItem( "modules" ).getNodeValue());
            }

        }
    }
    
    private void fillHashSet(HashSet set, String prefix, String list) {
       StringTokenizer tokens = new StringTokenizer(list,",");
       while (tokens.hasMoreTokens()) 
           set.add(prefix + tokens.nextToken());
    }

}