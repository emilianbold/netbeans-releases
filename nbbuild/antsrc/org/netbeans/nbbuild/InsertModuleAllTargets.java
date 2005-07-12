/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.taskdefs.CallTarget;
import org.apache.tools.ant.taskdefs.Echo;
import org.apache.tools.ant.taskdefs.Property;

/**
 * Insert into the live project some all-* targets.
 * This means they do not need to manually written into nbbuild/build.xml.
 * Targets are only added for projectized modules which do not already
 * have all-* entries in the build script.
 * Generally, an all-* target for a projectized module in a named cluster depends on:
 * 1. 'init'
 * 2. The all-* targets for any module which it lists as <build-prerequisite/>s
 *    in project.xml, but which are not included in the same cluster as this module.
 * Therefore cluster dependencies still have to be manually set.
 * An all-* target for a projectized module without a specific cluster depends on:
 * 1. 'init'
 * 2. The all-* targets for any module which it lists as <build-prerequisite/>s
 *    in project.xml.
 * cluster.properties must have already been read for this task to work.
 * @author Jesse Glick
 */
public final class InsertModuleAllTargets extends Task {
    
    public InsertModuleAllTargets() {}

    public void execute() throws BuildException {
        try {
            Project project = getProject();
            Set/*<String>*/ existingTargets = project.getTargets().keySet();
            if (existingTargets.contains("all-openide/util")) {
                log("Already seem to have inserted targets into this project; will not do it twice", Project.MSG_VERBOSE);
                return;
            }
            Hashtable/*<String,String>*/ props = project.getProperties();
            Map/*<String,String>*/ clustersOfModules = new HashMap();
            Iterator it = props.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                String cluster = (String) pair.getKey();
                if (!cluster.startsWith("nb.cluster.") || cluster.endsWith(".depends") || cluster.endsWith(".dir")) {
                    continue;
                }
                String[] modules = ((String) pair.getValue()).split(", *");
                for (int i = 0; i < modules.length; i++) {
                    clustersOfModules.put(modules[i], cluster);
                }
            }
            ModuleListParser mlp = new ModuleListParser(props, ParseProjectXml.TYPE_NB_ORG, project);
            SortedMap/*<String,ModuleListParser.Entry>*/ entries = new TreeMap();
            it = mlp.findAll().iterator();
            while (it.hasNext()) {
                ModuleListParser.Entry entry = (ModuleListParser.Entry) it.next();
                String path = entry.getNetbeansOrgPath();
                assert path != null : entry;
                entries.put(path, entry);
            }
            it = entries.values().iterator();
            while (it.hasNext()) {
                ModuleListParser.Entry entry = (ModuleListParser.Entry) it.next();
                String path = entry.getNetbeansOrgPath();
                assert path != null : entry;
                String target = "all-" + path;
                if (existingTargets.contains(target)) {
                    log("Not adding target " + target + " because one already exists", Project.MSG_INFO);
                    continue;
                }
                String[] prereqsAsCnb = entry.getBuildPrerequisites();
                StringBuffer namedDeps = new StringBuffer("init");
                String myCluster = (String) clustersOfModules.get(path);
                for (int i = 0; i < prereqsAsCnb.length; i++) {
                    String cnb = prereqsAsCnb[i];
                    ModuleListParser.Entry other = mlp.findByCodeNameBase(cnb);
                    if (other == null) {
                        log("Cannot find build prerequisite " + cnb + " of " + entry, Project.MSG_WARN);
                        continue;
                    }
                    String otherPath = other.getNetbeansOrgPath();
                    assert otherPath != null : other;
                    String otherCluster = (String) clustersOfModules.get(otherPath);
                    if (myCluster == null || otherCluster == null || myCluster.equals(otherCluster)) {
                        namedDeps.append(",all-");
                        namedDeps.append(otherPath);
                    }
                }
                String namedDepsS = namedDeps.toString();
                log("Adding target " + target + " with depends=\"" + namedDepsS + "\"", Project.MSG_VERBOSE);
                Target t = new Target();
                t.setName(target);
                t.setLocation(getLocation());
                t.setDepends(namedDepsS);
                project.addTarget(t);
                Echo echo = (Echo) project.createTask("echo");
                echo.setMessage("Building " + path + "...");
                t.addTask(echo);
                CallTarget call = (CallTarget) project.createTask("antcall");
                call.setTarget("build-one-cluster-dependencies");
                call.setInheritAll(false);
                Property param = call.createParam();
                param.setName("one.cluster.dependencies");
                param.setValue((String) props.get(myCluster + ".depends"));
                param = call.createParam();
                param.setName("one.cluster.name");
                param.setValue("this-cluster");
                t.addTask(call);
                Ant ant = (Ant) project.createTask("ant");
                ant.setDir(project.resolveFile("../" + path));
                ant.setTarget("netbeans");
                t.addTask(ant);
            }
        } catch (IOException e) {
            throw new BuildException(e.toString(), e, getLocation());
        }
    }
    
}
