package org.netbeans.modules.uml.core.metamodel.structure;


/**
 *
 */
public class TestSourceFileArtifactEventsListener extends
    SourceFileArtifactEventsAdapter
{
    /**
     * Called when a range of text is deleted from an ISourceFileArtifact's source code
     */
    public void onRangeDeleted( int rangeStart, int rangeEnd, String deletedText )
    {
    }
    
    /**
     * Called when the source file artifact has been renamed
     */
    public void onSourceFileNameChanged( String oldName, String newName )
    {
    }
    
    /**
     * Called when a range of text in an ISourceFileArtifact's source code is replaced with another piece of text.
     */
    public void onRangeModified( int rangeStart, int rangeEnd, String originalText, String newText )
    {
    }
    /**
     * Called when text is inserted into an ISourceFileArtifact's source code
     */
    public void onTextInserted( int fileOffset, String insertedText )
    {
    }
}


