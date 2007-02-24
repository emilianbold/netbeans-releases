
/*
 * Created on Oct 27, 2003
 *
 */
package org.netbeans.modules.uml.core;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import java.io.File;

import org.dom4j.Document;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.workspacemanagement.WorkspaceManagementException;
/**
 * @author aztec
 *
 */
public class QueryManagerTestCase extends AbstractUMLTestCase
{
    QueryManager queryMan = null;
    public QueryManagerTestCase()
    {
    }
    
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(QueryManagerTestCase.class);
    }
    
    protected void setUp()
    {
        queryMan = new QueryManager();
        
        createClass("C1");
        createClass("C2");
        
        project.setDirty(true);
        try
        {
            workspace.save();
        }
        catch (WorkspaceManagementException e)
        {
            e.printStackTrace();
        }
        new org.netbeans.modules.uml.core.QueryManager().establishCache(project);
        project.close();
    }
    
    public void testEstablishQueryCache()
    {
        File queryCacheFile = new File(
            new File(project.getFileName()).getParent(), ".QueryCache" );
        Document doc = XMLManip.getDOMDocument(queryCacheFile.toString());
        assertNotNull(doc);
        assertEquals("QueryCache", doc.getRootElement().getName());
    }
}



