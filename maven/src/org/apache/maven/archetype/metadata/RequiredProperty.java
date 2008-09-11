/*
 * $Id$
 */

package org.apache.maven.archetype.metadata;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Date;

/**
 * Class RequiredProperty.
 * 
 * @version $Revision$ $Date$
 */
public class RequiredProperty implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field key.
     */
    private String key;

    /**
     * Field defaultValue.
     */
    private String defaultValue;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Get the defaultValue field.
     * 
     * @return String
     */
    public String getDefaultValue()
    {
        return this.defaultValue;
    } //-- String getDefaultValue() 

    /**
     * Get the key field.
     * 
     * @return String
     */
    public String getKey()
    {
        return this.key;
    } //-- String getKey() 

    /**
     * Set the defaultValue field.
     * 
     * @param defaultValue
     */
    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    } //-- void setDefaultValue(String) 

    /**
     * Set the key field.
     * 
     * @param key
     */
    public void setKey(String key)
    {
        this.key = key;
    } //-- void setKey(String) 


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
