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
/*
 * NbPatternset.java
 *
 * Created on May 2, 2001, 7:04 PM
 */

package org.netbeans.xtest;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

import java.util.Vector;
import java.util.Iterator;
import java.io.File;

/** This Ant's task merges together nested patternsets and patternset
 * refereced by the id ettribute. If patternset denoted by id attribute
 * doesn't exist new one is created and added among project references.
 *
 * @author <a href="mailto:vitezslav.stejskal@czech.sun.com">Vitezslav Stejskal</a> 
 * @version 1.0
 */
public class NbPatternset extends Task {
    private PatternSet defaultPatterns = new PatternSet();
    private Vector additionalPatterns = new Vector();
    private String id;
    
    public void setUseId(String id) {
        this.id = id;
    }
    
    public PatternSet createPatternSet() {
        PatternSet patterns = new PatternSet();
        additionalPatterns.addElement(patterns);
        return patterns;
    }

    /**
     * add a name entry on the include list
     */
    public PatternSet.NameEntry createInclude() {
        return defaultPatterns.createInclude();
    }
    
    /**
     * add a name entry on the exclude list
     */
    public PatternSet.NameEntry createExclude() {
        return defaultPatterns.createExclude();
    }

    /**
     * Sets the set of include patterns. Patterns may be separated by a comma
     * or a space.
     *
     * @param includes the string containing the include patterns
     */
    public void setIncludes(String includes) {
        defaultPatterns.setIncludes(includes);
    }

    /**
     * Sets the set of exclude patterns. Patterns may be separated by a comma
     * or a space.
     *
     * @param excludes the string containing the exclude patterns
     */
    public void setExcludes(String excludes) {
        defaultPatterns.setExcludes(excludes);
    }

    /**
     * Sets the name of the file containing the includes patterns.
     *
     * @param incl The file to fetch the include patterns from.  
     */
     public void setIncludesfile(File incl) throws BuildException {
         defaultPatterns.setIncludesfile(incl);
     }

    /**
     * Sets the name of the file containing the includes patterns.
     *
     * @param excl The file to fetch the exclude patterns from.  
     */
     public void setExcludesfile(File excl) throws BuildException {
         defaultPatterns.setExcludesfile(excl);
     }

    public void execute () throws BuildException {
        if (null == id)
            throw new BuildException("The id attribute must be set.");
        
        // try to get reference
        PatternSet patternset;
        Object ref = project.getReferences().get(id);
        if (null != ref) {
            if (!(ref instanceof PatternSet)) {
                log("Object denoted by " + id + " isn't valid PatternSet.", Project.MSG_VERBOSE);
                return;
            }
            patternset = (PatternSet)ref;
        }
        else
            patternset = new PatternSet();
        
        Iterator i = additionalPatterns.iterator();
        
        patternset.append(defaultPatterns, project);
        while(i.hasNext()) {
            PatternSet p = (PatternSet)i.next();
            patternset.append(p, project);
        }
        
        project.getReferences().put(id, patternset);
    }
}
