/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.maven.workspace.reader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.WorkspaceReader;
import org.sonatype.aether.repository.WorkspaceRepository;

/**
 *
 * @author mkleint
 */
public class IDEWorkspaceReader implements WorkspaceReader {

    private final WorkspaceRepository repo = new WorkspaceRepository("ide");
    private final Map<String, File> mappings;

    public IDEWorkspaceReader() {
        mappings = new HashMap<String, File>();
        String mapp = System.getenv("netbeansProjectMappings");
        if (mapp != null) {
            StringTokenizer st = new StringTokenizer(mapp, ",");
            while (st.hasMoreTokens()) {
                String tok = st.nextToken();
                StringTokenizer st2 = new StringTokenizer(tok, "=");
                if (st2.hasMoreTokens()) {
                    String gav = st2.nextToken();
                    if (st2.hasMoreElements()) {
                        String file = st2.nextToken();
                        File f = new File(file);
                        if (f.exists()) {
                            mappings.put(gav, new File(file));
                        }
                    }
                }
            }
        }

    }

    public WorkspaceRepository getRepository() {
        return repo;
    }

    public File findArtifact(Artifact artifact) {
        File f = mappings.get(artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getBaseVersion());
        if (f != null) {
            if ("pom".equals(artifact.getExtension())) {
                System.out.println("artifact pom=" + artifact + " " + new File(f, "pom.xml"));
                return new File(f, "pom.xml");
            }
            if ("jar".equals(artifact.getExtension()) && "".equals(artifact.getClassifier())) {
                System.out.println("artifact jar=" + artifact + " " + new File(f, "target/classes"));
                return new File(new File(f, "target"), "classes");
            }
            if ("jar".equals(artifact.getExtension()) && "tests".equals(artifact.getClassifier())) {
                System.out.println("artifact test jar=" + artifact + " " + new File(f, "target/test-classes"));
                return new File(new File(f, "target"), "test-classes");
            }
        }
        return null;
    }

    public List<String> findVersions(Artifact artifact) {
        String id = artifact.getGroupId() + ":" + artifact.getArtifactId() + ":";
        List<String> toRet = new ArrayList<String>();
        for (String s : mappings.keySet()) {
            if (s.startsWith(id)) {
                toRet.add(s.substring(id.length()));
                System.out.println("ver=" + s.substring(id.length()));
            }
        }
        return toRet;
    }
}
