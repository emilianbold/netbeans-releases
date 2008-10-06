/*
 * $Id$
 */

package org.apache.maven.archetype.metadata;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Date;

/**
 * Class ModuleDescriptor.
 * 
 * @version $Revision$ $Date$
 */
public class ModuleDescriptor extends AbstractArchetypeDescriptor 
implements java.io.Serializable
{


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field id.
     */
    private String id;

    /**
     * Field dir.
     */
    private String dir;

    /**
     * Field name.
     */
    private String name;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Get the dir field.
     * 
     * @return String
     */
    public String getDir()
    {
        return this.dir;
    } //-- String getDir() 

    /**
     * Get the id field.
     * 
     * @return String
     */
    public String getId()
    {
        return this.id;
    } //-- String getId() 

    /**
     * Get the name field.
     * 
     * @return String
     */
    public String getName()
    {
        return this.name;
    } //-- String getName() 

    /**
     * Set the dir field.
     * 
     * @param dir
     */
    public void setDir(String dir)
    {
        this.dir = dir;
    } //-- void setDir(String) 

    /**
     * Set the id field.
     * 
     * @param id
     */
    public void setId(String id)
    {
        this.id = id;
    } //-- void setId(String) 

    /**
     * Set the name field.
     * 
     * @param name
     */
    public void setName(String name)
    {
        this.name = name;
    } //-- void setName(String) 


    private String modelEncoding = "UTF-8";

    /**
     * Set an encoding used for reading/writing the model.
     *
     * @param modelEncoding the encoding used when reading/writing the model.
     */
    public void setModelEncoding( String modelEncoding )
    {
        this.modelEncoding = modelEncoding;
    }

    /**
     * @return the current encoding used when reading/writing this model.
     */
    public String getModelEncoding()
    {
        return modelEncoding;
    }
}
