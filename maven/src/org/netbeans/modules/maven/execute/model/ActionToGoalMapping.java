/*
 * $Id$
 */

package org.netbeans.modules.maven.execute.model;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Date;

/**
 * Class ActionToGoalMapping.
 * 
 * @version $Revision$ $Date$
 */
public class ActionToGoalMapping implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Supported packaging type that this this project maps to. 
     *                     Useful for custom non-standard
     * packingings to get the same behaviour as the supported ones.
     *                     Supported types: jar, war, ejb, ear
     *                     @deprecated is ethe
     * netbeans.hint.packaging POM property instead.
     */
    private String packaging;

    /**
     * Field actions.
     */
    private java.util.List actions;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addAction.
     * 
     * @param netbeansActionMapping
     */
    public void addAction(NetbeansActionMapping netbeansActionMapping)
    {
        if ( !(netbeansActionMapping instanceof NetbeansActionMapping) )
        {
            throw new ClassCastException( "ActionToGoalMapping.addActions(netbeansActionMapping) parameter must be instanceof " + NetbeansActionMapping.class.getName() );
        }
        getActions().add( netbeansActionMapping );
    } //-- void addAction(NetbeansActionMapping) 

    /**
     * Method getActions.
     * 
     * @return java.util.List
     */
    public java.util.List getActions()
    {
        if ( this.actions == null )
        {
            this.actions = new java.util.ArrayList();
        }
        
        return this.actions;
    } //-- java.util.List getActions() 

    /**
     * Get supported packaging type that this this project maps to.
     * 
     *                     Useful for custom non-standard
     * packingings to get the same behaviour as the supported ones.
     *                     Supported types: jar, war, ejb, ear
     *                     @deprecated is ethe
     * netbeans.hint.packaging POM property instead.
     * 
     * @return String
     */
    public String getPackaging()
    {
        return this.packaging;
    } //-- String getPackaging() 

    /**
     * Method removeAction.
     * 
     * @param netbeansActionMapping
     */
    public void removeAction(NetbeansActionMapping netbeansActionMapping)
    {
        if ( !(netbeansActionMapping instanceof NetbeansActionMapping) )
        {
            throw new ClassCastException( "ActionToGoalMapping.removeActions(netbeansActionMapping) parameter must be instanceof " + NetbeansActionMapping.class.getName() );
        }
        getActions().remove( netbeansActionMapping );
    } //-- void removeAction(NetbeansActionMapping) 

    /**
     * Set the actions field.
     * 
     * @param actions
     */
    public void setActions(java.util.List actions)
    {
        this.actions = actions;
    } //-- void setActions(java.util.List) 

    /**
     * Set supported packaging type that this this project maps to.
     * 
     *                     Useful for custom non-standard
     * packingings to get the same behaviour as the supported ones.
     *                     Supported types: jar, war, ejb, ear
     *                     @deprecated is ethe
     * netbeans.hint.packaging POM property instead.
     * 
     * @param packaging
     */
    public void setPackaging(String packaging)
    {
        this.packaging = packaging;
    } //-- void setPackaging(String) 


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
