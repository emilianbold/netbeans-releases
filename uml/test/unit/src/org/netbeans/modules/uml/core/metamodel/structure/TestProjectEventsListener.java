package org.netbeans.modules.uml.core.metamodel.structure;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
/**
 *
 */
public class TestProjectEventsListener extends ProjectEventsAdapter
{
    
    public TestProjectEventsListener()
    {
        super();
    }
    
    /**
     * Fired whenever the passed in Project's Mode has been changed.
     */
    public void onModeModified( IProject pProject, IResultCell cell )
    {
        ProjectTestCase.callingModeModified = true;
    }
    
    /**
     * Fired whenever the passed in Project's Mode property is about to change.
     */
    public void onPreModeModified( IProject pProject, String newValue, IResultCell cell )
    {
        ProjectTestCase.callingPreModeModified = true;
    }
    
    
}


