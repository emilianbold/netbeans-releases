/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.beans.beaninfo;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.beans.BeanInfo;
import java.beans.PropertyEditor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;

import org.openide.util.actions.SystemAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

/** The basic node for representing features included in BanInfo. It recognizes
* the type of the BiFeature and creates properties according to it.
* @author Petr Hrebejk
*/
class BiFeatureNode extends AbstractNode implements Node.Cookie {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -8680621542479107034L;

    // static variables ...........................................................................

    private static final String ICON_BASE =
        "/org/netbeans/modules/beans/resources/dvoj"; // NOI18N

    /** Property display name constant */
    public static final String PROP_DISPLAY_NAME = "displayName"; // NOI18N
    /** Property expert constant */
    public static final String PROP_EXPERT = "expert"; // NOI18N
    /** Property hidden constant */
    public static final String PROP_HIDDEN = "hidden"; // NOI18N
    /** Property name constant */
    public static final String PROP_NAME = "name"; // NOI18N
    /** Property preffered constant */
    public static final String PROP_PREFERRED = "preferred"; // NOI18N
    /** Property short description constant */
    public static final String PROP_SHORT_DESCRIPTION = "shortDescription"; // NOI18N
    /** Property include constant */
    public static final String PROP_INCLUDED = "included"; // NOI18N

    /** Property bound constant */
    public static final String PROP_BOUND = "bound"; // NOI18N
    /** Property constrained constant */
    public static final String PROP_CONSTRAINED = "constrained"; // NOI18N
    /** Property mode constant */
    public static final String PROP_MODE = "mode"; // NOI18N
    /** Property editor class constant */
    public static final String PROP_EDITOR_CLASS = "propertyEditorClass"; // NOI18N
    /** Property non-indexed getter constant */
    public static final String PROP_NI_GETTER = "niGetter"; // NOI18N
    /** Property non-indexed setter constant */
    public static final String PROP_NI_SETTER = "niSetter"; // NOI18N

    /** Property unicast constant */
    public static final String PROP_UNICAST = "unicast"; // NOI18N
    /** Property in default event set constant */
    public static final String PROP_IN_DEFAULT_EVENTSET = "inDefaultEventSet"; // NOI18N


    // variables ..........................................................................

    private static SystemAction [] staticActions;

    // feature mode asociated to this node
    private BiFeature  biFeature = null;

    // constructors .......................................................................

    /**
    * Creates empty BreakpointContext.
    */
    public BiFeatureNode (final BiFeature biFeature) {
        super (Children.LEAF);
        this.biFeature = biFeature;
        setDisplayName (getName ());
        setShortDescription(biFeature.getToolTip());
        setIconBase( biFeature.getIconBase() );
        init ();
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (BiFeatureNode.class);
    }

    /** Setter for parent node. Is protected for subclasses. Fires info about
    * change of the parent.
    *
    * @param n new parent node
    */
    /*
    protected void setParentNode (Node n) {
      super.setParentNode (n);
}
    */
    private void init () {
        createProperties ();
        getCookieSet().add ( this );
        /*
        breakpoint.addPropertyChangeListener (new PropertyChangeListener () {
          public void propertyChange (PropertyChangeEvent e) {
            String s = e.getPropertyName ();
            parameterChanged (e);
          }
    });
        */
    }

    public Node.Cookie getCookie( Class type ) {
        if ( type == BiFeatureNode.class )
            return this;

        return getCookieSet().getCookie( type );
    }

    /** Creates properties for this node */
    private void createProperties () {

        // default sheet with "properties" property set // NOI18N
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = sheet.get(Sheet.PROPERTIES);

        ps.put(new PropertySupport.ReadOnly (
                   PROP_NAME,
                   String.class,
                   GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_NAME ),
                   GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_NAME )
               ) {
                   public Object getValue () {
                       return biFeature.getName ();
                   }
                   public void setValue (Object val) throws IllegalAccessException {
                       throw new IllegalAccessException(GenerateBeanInfoAction.getString("MSG_Cannot_Write"));
                   }
               });

        ps.put(new PropertySupport.ReadWrite (
                   PROP_EXPERT,
                   Boolean.TYPE,
                   GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_EXPERT ),
                   GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_EXPERT )
               ) {
                   public Object getValue () {
                       return new Boolean( biFeature.isExpert () );
                   }
                   public void setValue (Object val) throws
                       IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                       try {
                           biFeature.setExpert ( ((Boolean)val).booleanValue() );
                       } catch (ClassCastException e) {
                           throw new IllegalArgumentException ();
                       }
                   }
               });
        ps.put(new PropertySupport.ReadWrite (
                   PROP_HIDDEN,
                   Boolean.TYPE,
                   GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_HIDDEN ),
                   GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_HIDDEN )
               ) {
                   public Object getValue () {
                       return new Boolean( biFeature.isHidden () );
                   }
                   public void setValue (Object val) throws
                       IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                       try {
                           biFeature.setHidden ( ((Boolean)val).booleanValue() );
                       } catch (ClassCastException e) {
                           throw new IllegalArgumentException ();
                       }
                   }
               });
        ps.put(new PropertySupport.ReadWrite (
                   PROP_PREFERRED,
                   Boolean.TYPE,
                   GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_PREFERRED ),
                   GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_PREFERRED )
               ) {
                   public Object getValue () {
                       return new Boolean( biFeature.isPreferred () );
                   }
                   public void setValue (Object val) throws
                       IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                       try {
                           biFeature.setPreferred ( ((Boolean)val).booleanValue() );
                       } catch (ClassCastException e) {
                           throw new IllegalArgumentException ();
                       }
                   }
               });
        ps.put(new PropertySupport.ReadWrite (
                   PROP_DISPLAY_NAME,
                   String.class,
                   GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_DISPLAY_NAME ),
                   GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_DISPLAY_NAME )
               ) {
                   public Object getValue () {
                       return biFeature.getDisplayName ();
                   }
                   public void setValue (Object val) throws
                       IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                       try {
                           biFeature.setDisplayName ( (String)val );
                       } catch (ClassCastException e) {
                           throw new IllegalArgumentException ();
                       }
                   }
               });

        ps.put(new PropertySupport.ReadWrite (
                   PROP_SHORT_DESCRIPTION,
                   String.class,
                   GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_SHORT_DESCRIPTION ),
                   GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_SHORT_DESCRIPTION )
               ) {
                   public Object getValue () {
                       return biFeature.getShortDescription ();
                   }
                   public void setValue (Object val) throws
                       IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                       try {
                           biFeature.setShortDescription ( (String)val );
                       } catch (ClassCastException e) {
                           throw new IllegalArgumentException ();
                       }
                   }
               });
        ps.put(new PropertySupport.ReadWrite (
                   PROP_INCLUDED,
                   Boolean.TYPE,
                   GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_INCLUDED ),
                   GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_INCLUDED )
               ) {
                   public Object getValue () {
                       return new Boolean( biFeature.isIncluded () );
                   }
                   public void setValue (Object val) throws
                       IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                       try {
                           biFeature.setIncluded ( ((Boolean)val).booleanValue() );
                           setIconBase( biFeature.getIconBase() );
                       } catch (ClassCastException e) {
                           throw new IllegalArgumentException ();
                       }
                   }
               });

        // Add special properties according to type of feature


        if ( biFeature instanceof BiFeature.Property || biFeature instanceof BiFeature.IdxProperty )
            addExpertProperty( sheet );
        else if ( biFeature instanceof BiFeature.EventSet )
            addExpertEventSet( sheet );

        // and set new sheet
        setSheet(sheet);
    }

    private void addExpertProperty( Sheet sheet ) {
        Sheet.Set ps = Sheet.createExpertSet();

        ps.put(new PropertySupport.ReadWrite (
                   PROP_BOUND,
                   Boolean.TYPE,
                   GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_BOUND ),
                   GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_BOUND )
               ) {
                   public Object getValue () {
                       return new Boolean( ((BiFeature.Property)biFeature).isBound () );
                   }
                   public void setValue (Object val) throws
                       IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                       try {
                           ((BiFeature.Property)biFeature).setBound(((Boolean)val).booleanValue() );
                       } catch (ClassCastException e) {
                           throw new IllegalArgumentException ();
                       }
                   }
               });
        ps.put(new PropertySupport.ReadWrite (
                   PROP_CONSTRAINED,
                   Boolean.TYPE,
                   GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_CONSTRAINED ),
                   GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_CONSTRAINED )
               ) {
                   public Object getValue () {
                       return new Boolean(((BiFeature.Property)biFeature).isConstrained () );
                   }
                   public void setValue (Object val) throws
                       IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                       try {
                           ((BiFeature.Property)biFeature).setConstrained ( ((Boolean)val).booleanValue() );
                       } catch (ClassCastException e) {
                           throw new IllegalArgumentException ();
                       }
                   }
               });
        ps.put(new PropertySupport (
                   PROP_MODE,
                   int.class,
                   GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_MODE ),
                   GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_MODE ),
                   true,
                   ((BiFeature.Property)biFeature).modeChangeable()
               ) {
                   public Object getValue () {
                       return new Integer( ((BiFeature.Property)biFeature).getMode() );
                   }
                   public void setValue (Object val) throws
                       IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                       try {
                           ((BiFeature.Property)biFeature).setMode ( ((Integer)val).intValue() );
                           setIconBase( biFeature.getIconBase() );
                       } catch (ClassCastException e) {
                           throw new IllegalArgumentException ();
                       }
                   }
                   public PropertyEditor getPropertyEditor () {
                       return new org.netbeans.modules.beans.ModePropertyEditor();
                   }
               });
        ps.put(new PropertySupport.ReadWrite (
                   PROP_EDITOR_CLASS,
                   String.class,
                   GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_EDITOR_CLASS ),
                   GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_EDITOR_CLASS )
               ) {
                   public Object getValue () {
                       return ((BiFeature.Property)biFeature).getPropertyEditorClass ();
                   }
                   public void setValue (Object val) throws
                       IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                       try {
                           ((BiFeature.Property)biFeature).setPropertyEditorClass ( (String)val );
                       } catch (ClassCastException e) {
                           throw new IllegalArgumentException ();
                       }
                   }
               });

        if ( biFeature instanceof BiFeature.IdxProperty ) {
            ps.put(new PropertySupport (
                       PROP_NI_GETTER,
                       Boolean.TYPE,
                       GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_NI_GETTER ),
                       GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_NI_GETTER ),
                       true,
                       ((BiFeature.IdxProperty)biFeature).hasNiGetter()
                   ) {
                       public Object getValue () {
                           return new Boolean( ((BiFeature.IdxProperty)biFeature).isNiGetter () );
                       }
                       public void setValue (Object val) throws
                           IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                           if ( !((BiFeature.IdxProperty)biFeature).hasNiGetter() )
                               throw new IllegalAccessException ();
                           try {
                               ((BiFeature.IdxProperty)biFeature).setNiGetter ( ((Boolean)val).booleanValue() );
                           } catch (ClassCastException e) {
                               throw new IllegalArgumentException ();
                           }
                       }
                   });
            ps.put(new PropertySupport (
                       PROP_NI_SETTER,
                       Boolean.TYPE,
                       GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_NI_SETTER ),
                       GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_NI_SETTER ),
                       true,
                       ((BiFeature.IdxProperty)biFeature).hasNiSetter()
                   ) {
                       public Object getValue () {
                           return new Boolean( ((BiFeature.IdxProperty)biFeature).isNiSetter () );
                       }
                       public void setValue (Object val) throws
                           IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                           if ( !((BiFeature.IdxProperty)biFeature).hasNiSetter() )
                               throw new IllegalAccessException ();
                           try {
                               ((BiFeature.IdxProperty)biFeature).setNiSetter ( ((Boolean)val).booleanValue() );
                           } catch (ClassCastException e) {
                               throw new IllegalArgumentException ();
                           }
                       }
                   });
        }

        sheet.put( ps );
    }

    void addExpertEventSet ( Sheet sheet ) {
        Sheet.Set ps = Sheet.createExpertSet();

        ps.put(new PropertySupport.ReadOnly (
                   PROP_UNICAST,
                   Boolean.TYPE,
                   GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_UNICAST ),
                   GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_UNICAST )
               ) {
                   public Object getValue () {
                       return new Boolean( ((BiFeature.EventSet)biFeature).isUnicast () );
                   }
                   public void setValue (Object val) throws
                       IllegalAccessException {
                       throw new IllegalAccessException(GenerateBeanInfoAction.getString("MSG_Cannot_Write"));
                   }
               });
        ps.put(new PropertySupport.ReadWrite (
                   PROP_IN_DEFAULT_EVENTSET,
                   Boolean.TYPE,
                   GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_IN_DEFAULT_EVENTSET ),
                   GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_IN_DEFAULT_EVENTSET )
               ) {
                   public Object getValue () {
                       return new Boolean( ((BiFeature.EventSet)biFeature).isInDefaultEventSet () );
                   }
                   public void setValue (Object val) throws
                       IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                       try {
                           ((BiFeature.EventSet)biFeature).setInDefaultEventSet(((Boolean)val).booleanValue() );
                       } catch (ClassCastException e) {
                           throw new IllegalArgumentException ();
                       }
                   }
               });


        sheet.put( ps );

    }
    // implementation of Node ..........................................................

    /** Getter for set of actions that should be present in the
    * popup menu of this node. This set is used in construction of
    * menu returned from getContextMenu and specially when a menu for
    * more nodes is constructed.
    *
    * @return array of system actions that should be in popup menu
    */
    public SystemAction[] getActions () {
        if (staticActions == null) {
            staticActions = new SystemAction[] {
                                SystemAction.get (BiToggleAction.class),
                                null
                                /*
                                SystemAction.get (DeleteAction.class),
                                null,
                                SystemAction.get (ToolsAction.class),
                                SystemAction.get (PropertiesAction.class),
                                */
                            };
        }
        return staticActions;
    }

    /**
    * the feature cannot be removed it can only be disabled from BeanInfo
    *
    * @return <CODE>true</CODE>
    */
    public boolean canDestroy () {
        return false;
    }

    /**
    * Deletes breakpoint and removes the node too.
    * Ovverrides destroy() from abstract node.
    */
    public void destroy () throws IOException {
        // remove node
        // super.destroy ();
    }

    /** It has default action - it is the toggle of value for include to bean info
    * @return <CODE>true</CODE>
    */
    public boolean hasDefaultAction () {
        return true;
    }

    /** Toggles the selection of bi feature */
    public void toggleSelection() {
        biFeature.setIncluded ( !biFeature.isIncluded() );
        firePropertyChange( PROP_INCLUDED, new Boolean( !biFeature.isIncluded() ), new Boolean( biFeature.isIncluded() ) );
        setIconBase( biFeature.getIconBase() );
    }

    /** Includes/excludes the pattern from bean info */
    public void include( boolean value ) {
        if (( value && biFeature.isIncluded() ) ||
                ( !value && !biFeature.isIncluded() ) )
            return;

        biFeature.setIncluded ( value );
        firePropertyChange( PROP_INCLUDED, new Boolean( !biFeature.isIncluded() ), new Boolean( biFeature.isIncluded() ) );
        setIconBase( biFeature.getIconBase() );
    }


    /** Executes default action.
    */
    public void invokeDefaultAction() {
        //System.out.println ( "Default action"); // NOI18N
        toggleSelection();
    }

    // private methods .....................................................................

    /**
    * Returns the associated BiFature
    */
    BiFeature getBiFeature () {
        return biFeature;
    }

    /**
    * Sets display name and fires property change.
    */
    /*
    void parameterChanged (PropertyChangeEvent e) {
      setDisplayName (getName ());
      firePropertyChange (e.getPropertyName (), e.getOldValue (), e.getNewValue ());
}
    */

    /**
    * Returns human presentable name of this breakpoint containing
    * informations about lineNumber, className e.t.c.
    *
    * @return human presentable name of this breakpoint.
    */
    public String getName () {
        return biFeature.getName();
    }
}

/*
 * Log
 *
 */
