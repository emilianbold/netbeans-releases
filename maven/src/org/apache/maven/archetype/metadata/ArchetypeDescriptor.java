/*
 * $Id$
 */

package org.apache.maven.archetype.metadata;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Date;

/**
 * Class ArchetypeDescriptor.
 * 
 * @version $Revision$ $Date$
 */
public class ArchetypeDescriptor extends AbstractArchetypeDescriptor 
implements java.io.Serializable
{


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field name.
     */
    private String name;

    /**
     * Field partial.
     */
    private boolean partial = false;

    /**
     * Field requiredProperties.
     */
    private java.util.List requiredProperties;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addRequiredProperty.
     * 
     * @param requiredProperty
     */
    public void addRequiredProperty(RequiredProperty requiredProperty)
    {
        if ( !(requiredProperty instanceof RequiredProperty) )
        {
            throw new ClassCastException( "ArchetypeDescriptor.addRequiredProperties(requiredProperty) parameter must be instanceof " + RequiredProperty.class.getName() );
        }
        getRequiredProperties().add( requiredProperty );
    } //-- void addRequiredProperty(RequiredProperty) 

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
     * Method getRequiredProperties.
     * 
     * @return java.util.List
     */
    public java.util.List getRequiredProperties()
    {
        if ( this.requiredProperties == null )
        {
            this.requiredProperties = new java.util.ArrayList();
        }
        
        return this.requiredProperties;
    } //-- java.util.List getRequiredProperties() 

    /**
     * Get the partial field.
     * 
     * @return boolean
     */
    public boolean isPartial()
    {
        return this.partial;
    } //-- boolean isPartial() 

    /**
     * Method removeRequiredProperty.
     * 
     * @param requiredProperty
     */
    public void removeRequiredProperty(RequiredProperty requiredProperty)
    {
        if ( !(requiredProperty instanceof RequiredProperty) )
        {
            throw new ClassCastException( "ArchetypeDescriptor.removeRequiredProperties(requiredProperty) parameter must be instanceof " + RequiredProperty.class.getName() );
        }
        getRequiredProperties().remove( requiredProperty );
    } //-- void removeRequiredProperty(RequiredProperty) 

    /**
     * Set the name field.
     * 
     * @param name
     */
    public void setName(String name)
    {
        this.name = name;
    } //-- void setName(String) 

    /**
     * Set the partial field.
     * 
     * @param partial
     */
    public void setPartial(boolean partial)
    {
        this.partial = partial;
    } //-- void setPartial(boolean) 

    /**
     * Set the requiredProperties field.
     * 
     * @param requiredProperties
     */
    public void setRequiredProperties(java.util.List requiredProperties)
    {
        this.requiredProperties = requiredProperties;
    } //-- void setRequiredProperties(java.util.List) 


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
