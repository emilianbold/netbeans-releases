/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.project.ant;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

// XXX may also need displayName field (any default? or only in SimpleAntArtifact?)

/**
 * Represents one artifact of an Ant build.
 * For example, if a build script is known to generate a JAR of a certain name
 * as a result of running a certain target, this object will name that JAR
 * and point to the script and target responsible for creating it. You can use
 * this information to add an <samp>&lt;ant&gt;</samp> task to another project
 * which will generate that JAR as a dependency before using it.
 * @see org.netbeans.spi.project.support.ant.SimpleAntArtifact
 * @author Jesse Glick
 */
public abstract class AntArtifact {
    
    /**
     * Empty constructor for use from subclasses.
     */
    protected AntArtifact() {}
    
    /**
     * Standard artifact type representing a JAR file, presumably
     * used as a Java library of some kind.
     */
    public static final String TYPE_JAR = "jar";
    
    /**
     * Get the type of the build artifact.
     * This can refer to both the physical content type or format;
     * and to the intended category of usage.
     * Typically a given client (e.g. superproject) will be interested
     * in only a certain artifact type for a certain purpose, e.g.
     * inclusion in a Java classpath.
     * <p>
     * Particular type identifiers should be agreed upon between
     * providers and clients.
     * {@link #TYPE_JAR} is predefined for convenience.
     * Others may be defined as needed; for example, tag library JARs,
     * WARs, EJB JARs, deployment descriptor fragments, etc.
     * XXX format - NMTOKEN maybe
     * @return the type (format or usage) of the build artifact
     */
    public abstract String getType();
    
    /**
     * Get a location for the Ant script that is able to produce this artifact.
     * The name <samp>build.xml</samp> is conventional.
     * @return the location of an Ant project file (might not currently exist)
     */
    public abstract File getScriptLocation();
    
    /**
     * Get the name of the Ant target that is able to produce this artifact.
     * E.g. <samp>jar</samp> would be conventional for {@link #TYPE_JAR} artifacts.
     * @return an Ant target name
     */
    public abstract String getTargetName();
    
    /**
     * Get the name of an Ant target that will delete this artifact.
     * Typically this should be <samp>clean</samp>.
     * The target may delete other build products as well.
     * @return an Ant target name
     */
    public abstract String getCleanTargetName();
    
    /**
     * Get the location of the build artifact relative to the Ant script.
     * For example, <samp>dist/mylib.jar</samp>.
     * @return a URI to the build artifact, resolved relative to {@link #getScriptLocation};
     *         may be either relative, or an absolute <code>file</code>-protocol URI
     */
    public abstract URI getArtifactLocation();
    
    /**
     * Convenience method to find the actual artifact, if it currently exists.
     * Uses {@link #getScriptFile} or {@link #getScriptLocation} and resolves {@link #getArtifactLocation} from it.
     * Note that a project which has been cleaned more recently than it has been built
     * will generally not have the build artifact on disk and so this call may easily
     * return null. If you do not rely on the actual presence of the file but just need to
     * refer to it abstractly, use {@link #getArtifactLocation} instead.
     * @return the artifact file on disk, or null if it could not be found
     */
    public final FileObject getArtifactFile() {
        URI artifactLocation = getArtifactLocation();
        assert !artifactLocation.isAbsolute() ||
            (!artifactLocation.isOpaque() && "file".equals(artifactLocation.getScheme())) // NOI18N
            : artifactLocation;
        URL artifact;
        try {
            // XXX this should probably use something in PropertyUtils?
            artifact = getScriptLocation().toURI().resolve(getArtifactLocation()).normalize().toURL();
        } catch (MalformedURLException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return null;
        }
        FileObject fo = URLMapper.findFileObject(artifact);
        if (fo != null) {
            assert FileUtil.toFile(fo) != null : fo;
            return fo;
        } else {
            return null;
        }
    }
    
    /**
     * Convenience method to find the actual script file, if it currently exists.
     * Uses {@link #getScriptLocation}.
     * The script must exist on disk (Ant cannot run scripts from NetBeans
     * filesystems unless they are represented on disk).
     * @return the Ant build script file, or null if it could not be found
     */
    public final FileObject getScriptFile() {
        FileObject fo = FileUtil.toFileObject(getScriptLocation());
        assert fo == null || FileUtil.toFile(fo) != null : fo;
        return fo;
    }
    
    /**
     * Find the project associated with this script, if any.
     * The default implementation uses {@link #getScriptLocation} and {@link FileOwnerQuery},
     * but subclasses may override that to return something else.
     * @return the associated project, or null if there is none or it could not be located
     */
    public Project getProject() {
        return FileOwnerQuery.getOwner(getScriptLocation().toURI());
    }
    
}
