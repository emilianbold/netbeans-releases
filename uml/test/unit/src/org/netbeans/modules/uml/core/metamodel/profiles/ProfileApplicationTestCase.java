
/*
 * Created on Sep 17, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.profiles;

import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;


/**
 * @author aztec
 *
 */
public class ProfileApplicationTestCase extends AbstractUMLTestCase
{
    
    /**
     *
     */
    public ProfileApplicationTestCase()
    {
        super();
    }
    
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(ProfileApplicationTestCase.class);
    }
    
    public void testSetImportedProfile()
    {
        ProfileApplication app = (ProfileApplication)FactoryRetriever.instance().createType("ProfileApplication", null);
        //app.prepareNode(DocumentFactory.getInstance().createElement(""));
        IProfile prof = (IProfile)FactoryRetriever.instance().createType("Profile", null);
        //prof.prepareNode(DocumentFactory.getInstance().createElement(""));
        
        project.addPackageImport(app, null);
        app.setImportedProfile(prof);
        IProfile profGot = app.getImportedProfile();
        assertEquals(prof, profGot);
    }
}



