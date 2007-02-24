package org.netbeans.modules.uml.core.metamodel.structure;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
/**
 *
 */
public class TestFileNameChangeListener extends ArtifactEventsSinkAdapter
{
    
    public TestFileNameChangeListener()
    {
        super();
    }
    
    public void onPreFileNameModified( IArtifact pArtifact, String newFileName, IResultCell cell )
    {
        ArtifactTestCase.callingPreModified = true;
    }
    
    public void onFileNameModified( IArtifact pArtifact, String newFileName, IResultCell cell )
    {
        ArtifactTestCase.callingModified = true;
    }
}


