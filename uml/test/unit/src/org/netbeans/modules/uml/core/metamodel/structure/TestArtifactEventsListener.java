package org.netbeans.modules.uml.core.metamodel.structure;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
/**
 *
 */
public class TestArtifactEventsListener extends ArtifactEventsSinkAdapter
{
    public void onPreFileNameModified( IArtifact pArtifact, String newFileName, IResultCell cell )
    {
        ArtifactTestCase.callingPreModified = true;
    }
    
    public void onFileNameModified( IArtifact pArtifact, String newFileName, IResultCell cell )
    {
        ArtifactTestCase.callingModified = true;
    }
    
    /**
     * Fired whenever the passed in Artifact's contents are about to become dirty.
     */
    public void onPreDirty( IArtifact pArtifact, IResultCell cell )
    {
        SourceFileArtifactTestCase.firePreDirtyCalled = true;
    }
    
    /**
     * Fired whenever the passed in Artifact's contents are dirty.
     */
    public void onDirty( IArtifact pArtifact, IResultCell cell )
    {
        SourceFileArtifactTestCase.fireDirtyCalled = true;
    }
}


