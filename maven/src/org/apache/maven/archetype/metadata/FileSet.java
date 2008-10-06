/*
 * $Id$
 */

package org.apache.maven.archetype.metadata;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Date;

/**
 * Class FileSet.
 * 
 * @version $Revision$ $Date$
 */
public class FileSet implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field filtered.
     */
    private boolean filtered = false;

    /**
     * Field packaged.
     */
    private boolean packaged = false;

    /**
     * Field encoding.
     */
    private String encoding;

    /**
     * Field directory.
     */
    private String directory;

    /**
     * Field includes.
     */
    private java.util.List includes;

    /**
     * Field excludes.
     */
    private java.util.List excludes;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addExclude.
     * 
     * @param string
     */
    public void addExclude(String string)
    {
        if ( !(string instanceof String) )
        {
            throw new ClassCastException( "FileSet.addExcludes(string) parameter must be instanceof " + String.class.getName() );
        }
        getExcludes().add( string );
    } //-- void addExclude(String) 

    /**
     * Method addInclude.
     * 
     * @param string
     */
    public void addInclude(String string)
    {
        if ( !(string instanceof String) )
        {
            throw new ClassCastException( "FileSet.addIncludes(string) parameter must be instanceof " + String.class.getName() );
        }
        getIncludes().add( string );
    } //-- void addInclude(String) 

    /**
     * Get the directory field.
     * 
     * @return String
     */
    public String getDirectory()
    {
        return this.directory;
    } //-- String getDirectory() 

    /**
     * Get the encoding field.
     * 
     * @return String
     */
    public String getEncoding()
    {
        return this.encoding;
    } //-- String getEncoding() 

    /**
     * Method getExcludes.
     * 
     * @return java.util.List
     */
    public java.util.List getExcludes()
    {
        if ( this.excludes == null )
        {
            this.excludes = new java.util.ArrayList();
        }
        
        return this.excludes;
    } //-- java.util.List getExcludes() 

    /**
     * Method getIncludes.
     * 
     * @return java.util.List
     */
    public java.util.List getIncludes()
    {
        if ( this.includes == null )
        {
            this.includes = new java.util.ArrayList();
        }
        
        return this.includes;
    } //-- java.util.List getIncludes() 

    /**
     * Get the filtered field.
     * 
     * @return boolean
     */
    public boolean isFiltered()
    {
        return this.filtered;
    } //-- boolean isFiltered() 

    /**
     * Get the packaged field.
     * 
     * @return boolean
     */
    public boolean isPackaged()
    {
        return this.packaged;
    } //-- boolean isPackaged() 

    /**
     * Method removeExclude.
     * 
     * @param string
     */
    public void removeExclude(String string)
    {
        if ( !(string instanceof String) )
        {
            throw new ClassCastException( "FileSet.removeExcludes(string) parameter must be instanceof " + String.class.getName() );
        }
        getExcludes().remove( string );
    } //-- void removeExclude(String) 

    /**
     * Method removeInclude.
     * 
     * @param string
     */
    public void removeInclude(String string)
    {
        if ( !(string instanceof String) )
        {
            throw new ClassCastException( "FileSet.removeIncludes(string) parameter must be instanceof " + String.class.getName() );
        }
        getIncludes().remove( string );
    } //-- void removeInclude(String) 

    /**
     * Set the directory field.
     * 
     * @param directory
     */
    public void setDirectory(String directory)
    {
        this.directory = directory;
    } //-- void setDirectory(String) 

    /**
     * Set the encoding field.
     * 
     * @param encoding
     */
    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    } //-- void setEncoding(String) 

    /**
     * Set the excludes field.
     * 
     * @param excludes
     */
    public void setExcludes(java.util.List excludes)
    {
        this.excludes = excludes;
    } //-- void setExcludes(java.util.List) 

    /**
     * Set the filtered field.
     * 
     * @param filtered
     */
    public void setFiltered(boolean filtered)
    {
        this.filtered = filtered;
    } //-- void setFiltered(boolean) 

    /**
     * Set the includes field.
     * 
     * @param includes
     */
    public void setIncludes(java.util.List includes)
    {
        this.includes = includes;
    } //-- void setIncludes(java.util.List) 

    /**
     * Set the packaged field.
     * 
     * @param packaged
     */
    public void setPackaged(boolean packaged)
    {
        this.packaged = packaged;
    } //-- void setPackaged(boolean) 


    {
        filtered = true;
        packaged = true;
    }

    public String toString ()
    {
        return
            getDirectory () + " (" +
                (isFiltered () ? "Filtered" : "Copied") +
                "-" +
                (isPackaged () ? "Packaged" : "Flat") +
            ") [" +
                hidden.org.codehaus.plexus.util.StringUtils.join ( getIncludes ().iterator (), ", " ) +
                " -- " +
                hidden.org.codehaus.plexus.util.StringUtils.join ( getExcludes ().iterator (), ", " ) +
            "]";

    }
                    
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
