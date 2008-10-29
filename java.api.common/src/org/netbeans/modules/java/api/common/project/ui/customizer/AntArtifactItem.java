package org.netbeans.modules.java.api.common.project.ui.customizer;

import java.awt.Component;
import java.net.URI;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;

/**
 * Pair of AntArtifact and one of jars it produces.
 * 
 * @since org.netbeans.modules.java.api.common/1 1.5
 */
public final class AntArtifactItem {

    private AntArtifact artifact;
    private URI artifactURI;

    public AntArtifactItem(AntArtifact artifact, URI artifactURI) {
        super();
        this.artifact = artifact;
        this.artifactURI = artifactURI;
    }

    public static AntArtifactItem[] showAntArtifactItemChooser( String[] artifactTypes, Project master, Component parent ) {
        return AntArtifactChooser.showDialog(artifactTypes, master, parent);
    }

    public AntArtifact getArtifact() {
        return artifact;
    }

    public URI getArtifactURI() {
        return artifactURI;
    }

    @Override
    public String toString() {
        return artifactURI.toString();
    }
}
