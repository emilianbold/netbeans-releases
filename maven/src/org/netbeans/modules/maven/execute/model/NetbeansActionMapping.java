/*
 * $Id$
 */

package org.netbeans.modules.maven.execute.model;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Date;

/**
 * Class NetbeansActionMapping.
 * 
 * @version $Revision$ $Date$
 */
public class NetbeansActionMapping implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field actionName.
     */
    private String actionName;

    /**
     * Field displayName.
     */
    private String displayName;

    /**
     * Field recursive.
     */
    private boolean recursive = true;

    /**
     * Field packagings.
     */
    private java.util.List<String> packagings;

    /**
     * Field goals.
     */
    private java.util.List<String> goals;

    /**
     * Field properties.
     */
    private java.util.Properties properties;

    /**
     * Field activatedProfiles.
     */
    private java.util.List<String> activatedProfiles;

    private String basedir;

    private String preAction;

    private String reactor;

      //-----------/
     //- Methods -/
    //-----------/


    public String getReactor() {
        return reactor;
    }

    public void setReactor(String reactor) {
        this.reactor = reactor;
    }

    public String getBasedir() {
        return basedir;
    }

    public void setBasedir(String basedir) {
        this.basedir = basedir;
    }

    public String getPreAction() {
        return preAction;
    }

    public void setPreAction(String preAction) {
        this.preAction = preAction;
    }

    /**
     * Method addActivatedProfile.
     * 
     * @param string
     */
    public void addActivatedProfile(String string)
    {
        getActivatedProfiles().add( string );
    } //-- void addActivatedProfile(String) 

    /**
     * Method addGoal.
     * 
     * @param string
     */
    public void addGoal(String string)
    {
        getGoals().add( string );
    } //-- void addGoal(String) 

    /**
     * Method addPackaging.
     * 
     * @param string
     */
    public void addPackaging(String string)
    {
        getPackagings().add( string );
    } //-- void addPackaging(String) 

    /**
     * Method addProperty.
     * 
     * @param key
     * @param value
     */
    public void addProperty(String key, String value)
    {
        getProperties().put( key, value );
    } //-- void addProperty(String, String) 

    /**
     * Get the actionName field.
     * 
     * @return String
     */
    public String getActionName()
    {
        return this.actionName;
    } //-- String getActionName() 

    /**
     * Method getActivatedProfiles.
     * 
     * @return java.util.List
     */
    public java.util.List<String> getActivatedProfiles()
    {
        if ( this.activatedProfiles == null )
        {
            this.activatedProfiles = new java.util.ArrayList<String>();
        }
        
        return this.activatedProfiles;
    } //-- java.util.List getActivatedProfiles() 

    /**
     * Get the displayName field.
     * 
     * @return String
     */
    public String getDisplayName()
    {
        return this.displayName;
    } //-- String getDisplayName() 

    /**
     * Method getGoals.
     * 
     * @return java.util.List
     */
    public java.util.List<String> getGoals()
    {
        if ( this.goals == null )
        {
            this.goals = new java.util.ArrayList<String>();
        }
        
        return this.goals;
    } //-- java.util.List getGoals() 

    /**
     * Method getPackagings.
     * 
     * @return java.util.List
     */
    public java.util.List<String> getPackagings()
    {
        if ( this.packagings == null )
        {
            this.packagings = new java.util.ArrayList<String>();
        }
        
        return this.packagings;
    } //-- java.util.List getPackagings() 

    /**
     * Method getProperties.
     * 
     * @return java.util.Properties
     */
    public java.util.Properties getProperties()
    {
        if ( this.properties == null )
        {
            this.properties = new java.util.Properties();
        }
        
        return this.properties;
    } //-- java.util.Properties getProperties() 

    /**
     * Get the recursive field.
     * 
     * @return boolean
     */
    public boolean isRecursive()
    {
        return this.recursive;
    } //-- boolean isRecursive() 

    /**
     * Method removeActivatedProfile.
     * 
     * @param string
     */
    public void removeActivatedProfile(String string)
    {
        getActivatedProfiles().remove( string );
    } //-- void removeActivatedProfile(String) 

    /**
     * Method removeGoal.
     * 
     * @param string
     */
    public void removeGoal(String string)
    {
        getGoals().remove( string );
    } //-- void removeGoal(String) 

    /**
     * Method removePackaging.
     * 
     * @param string
     */
    public void removePackaging(String string)
    {
        getPackagings().remove( string );
    } //-- void removePackaging(String) 

    /**
     * Set the actionName field.
     * 
     * @param actionName
     */
    public void setActionName(String actionName)
    {
        this.actionName = actionName;
    } //-- void setActionName(String) 

    /**
     * Set the activatedProfiles field.
     * 
     * @param activatedProfiles
     */
    public void setActivatedProfiles(java.util.List<String> activatedProfiles)
    {
        this.activatedProfiles = activatedProfiles;
    } //-- void setActivatedProfiles(java.util.List) 

    /**
     * Set the displayName field.
     * 
     * @param displayName
     */
    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    } //-- void setDisplayName(String) 

    /**
     * Set list of goals and phases to execute, order is important.
     * 
     * @param goals
     */
    public void setGoals(java.util.List<String> goals)
    {
        this.goals = goals;
    } //-- void setGoals(java.util.List) 

    /**
     * Set packaging types that this action mapping works for. *
     * for any.
     * 
     * @param packagings
     */
    public void setPackagings(java.util.List<String> packagings)
    {
        this.packagings = packagings;
    } //-- void setPackagings(java.util.List) 

    /**
     * Set the properties field.
     * 
     * @param properties
     */
    public void setProperties(java.util.Properties properties)
    {
        this.properties = properties;
    } //-- void setProperties(java.util.Properties) 

    /**
     * Set the recursive field.
     * 
     * @param recursive
     */
    public void setRecursive(boolean recursive)
    {
        this.recursive = recursive;
    } //-- void setRecursive(boolean) 


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
