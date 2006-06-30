/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
        Object ref = getProject().getReferences().get(id);
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
        
        patternset.append(defaultPatterns, getProject());
        while(i.hasNext()) {
            PatternSet p = (PatternSet)i.next();
            patternset.append(p, getProject());
        }
        
        getProject().getReferences().put(id, patternset);
    }
}
