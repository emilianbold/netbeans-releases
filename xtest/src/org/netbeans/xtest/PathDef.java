/*
 * PathDef.java
 *
 * Created on May 6, 2001, 4:06 PM
 */

package org.netbeans.xtest;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;

import java.util.LinkedList;
import java.util.Iterator;

/**
 *
 * @author  vs124454
 * @version 
 */
public class PathDef extends Task {
    private LinkedList elements = new LinkedList();
    private String id;
    
    public Path createPath() {
        Path p = new Path(project);
        elements.add(p);
        return p;
    }
    
    public void setUseId(String id) {
        this.id = id;
    }
    
    public void execute () throws BuildException {
        if (null == id)
            throw new BuildException("Set attribute 'useid'.");

        Path path = new Path(project);
        Iterator i = elements.iterator();
        while(i.hasNext()) {
            Path p = (Path)i.next();
            path.append(p);
        }
        
        project.addReference(id, path);
    }
}
