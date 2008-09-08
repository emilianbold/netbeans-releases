/*
 * $Id$
 */

package org.apache.maven.archetype.metadata;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Date;

/**
 * Class AbstractArchetypeDescriptor.
 * 
 * @version $Revision$ $Date$
 */
public class AbstractArchetypeDescriptor implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field fileSets.
     */
    private java.util.List fileSets;

    /**
     * Field modules.
     */
    private java.util.List modules;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addFileSet.
     * 
     * @param fileSet
     */
    public void addFileSet(FileSet fileSet)
    {
        if ( !(fileSet instanceof FileSet) )
        {
            throw new ClassCastException( "AbstractArchetypeDescriptor.addFileSets(fileSet) parameter must be instanceof " + FileSet.class.getName() );
        }
        getFileSets().add( fileSet );
    } //-- void addFileSet(FileSet) 

    /**
     * Method addModule.
     * 
     * @param moduleDescriptor
     */
    public void addModule(ModuleDescriptor moduleDescriptor)
    {
        if ( !(moduleDescriptor instanceof ModuleDescriptor) )
        {
            throw new ClassCastException( "AbstractArchetypeDescriptor.addModules(moduleDescriptor) parameter must be instanceof " + ModuleDescriptor.class.getName() );
        }
        getModules().add( moduleDescriptor );
    } //-- void addModule(ModuleDescriptor) 

    /**
     * Method getFileSets.
     * 
     * @return java.util.List
     */
    public java.util.List getFileSets()
    {
        if ( this.fileSets == null )
        {
            this.fileSets = new java.util.ArrayList();
        }
        
        return this.fileSets;
    } //-- java.util.List getFileSets() 

    /**
     * Method getModules.
     * 
     * @return java.util.List
     */
    public java.util.List getModules()
    {
        if ( this.modules == null )
        {
            this.modules = new java.util.ArrayList();
        }
        
        return this.modules;
    } //-- java.util.List getModules() 

    /**
     * Method removeFileSet.
     * 
     * @param fileSet
     */
    public void removeFileSet(FileSet fileSet)
    {
        if ( !(fileSet instanceof FileSet) )
        {
            throw new ClassCastException( "AbstractArchetypeDescriptor.removeFileSets(fileSet) parameter must be instanceof " + FileSet.class.getName() );
        }
        getFileSets().remove( fileSet );
    } //-- void removeFileSet(FileSet) 

    /**
     * Method removeModule.
     * 
     * @param moduleDescriptor
     */
    public void removeModule(ModuleDescriptor moduleDescriptor)
    {
        if ( !(moduleDescriptor instanceof ModuleDescriptor) )
        {
            throw new ClassCastException( "AbstractArchetypeDescriptor.removeModules(moduleDescriptor) parameter must be instanceof " + ModuleDescriptor.class.getName() );
        }
        getModules().remove( moduleDescriptor );
    } //-- void removeModule(ModuleDescriptor) 

    /**
     * Set the fileSets field.
     * 
     * @param fileSets
     */
    public void setFileSets(java.util.List fileSets)
    {
        this.fileSets = fileSets;
    } //-- void setFileSets(java.util.List) 

    /**
     * Set the modules field.
     * 
     * @param modules
     */
    public void setModules(java.util.List modules)
    {
        this.modules = modules;
    } //-- void setModules(java.util.List) 


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
