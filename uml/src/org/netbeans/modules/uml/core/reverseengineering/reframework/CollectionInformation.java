/*
 * CollectionInformation.java
 *
 * Created on April 11, 2007, 1:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.uml.core.reverseengineering.reframework;

import java.util.ArrayList;

/**
 *
 * @author treyspiva
 */
public class CollectionInformation
{
    private String typeName = "";
    private ArrayList < String > collectionNames = new ArrayList < String >();
    
    /** Creates a new instance of CollectionInformation */
    public CollectionInformation(String typeName)
    {
        setTypeName(typeName);
    }

    public String getTypeName()
    {
        return typeName;
    }
    
    public void setTypeName(String typeName)
    {
        this.typeName = typeName;
    }
    
    public void addCollectionName(String name)
    {
        collectionNames.add(0, name);
    }
    
    public String getCollectionForRange(int range)
    {
        return collectionNames.get(range);
    }
    
    public long getNumberOfRanges()
    {
        return collectionNames.size();
    }
}
