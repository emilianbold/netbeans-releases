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
import java.beans.PropertyEditorSupport;
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
    /** Property hidden constant
 */
    public static final String PROP_HIDDEN = "hidden"; // NOI18N
    /** Property name constant */
    public static final String PROP_NAME = "name"; // NOI18N
    /** Property preffered constant */
    public static final String PROP_PREFERRED = "preferred"; // NOI18N
    /** Property short description constant */
    public static final String PROP_SHORT_DESCRIPTION = "shortDescription"; // NOI18N
    /** Property include constant */
    public static final String PROP_INCLUDED = "included"; // NOI18N
    /** Property include constant */
    public static final String PROP_CUSTOMIZER = "customizer"; // NOI18N

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

    //private static SystemAction [] staticActions;

    // feature mode asociated to this node
    protected BiFeature  biFeature = null;
    
    //analyser
    protected BiAnalyser  biAnalyser = null;

    static javax.swing.GrayFilter grayFilter = null;
    
    static{
        grayFilter = new javax.swing.GrayFilter(true, 5);
    }
    
    // constructors .......................................................................

    /**
    * Creates empty BreakpointContext.
    */
    public BiFeatureNode (final BiFeature biFeature, BiAnalyser biAnalyser) {
        super (Children.LEAF);
        this.biFeature = biFeature;
        this.biAnalyser = biAnalyser;
        setDisplayName (getName ());
        setShortDescription(biFeature.getToolTip());
        setIconBase( biFeature.getIconBase(false) );
        init ();
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (BiFeatureNode.class);
    }
    
    public java.awt.Image getIcon( int type ){        
        if( biFeature instanceof BiFeature.Descriptor  && biAnalyser.isNullDescriptor() ) {
            //setIconBase( biFeature.getIconBase(true));
            return grayFilter.createDisabledImage(super.getIcon(type));
        }
        if( ( biFeature instanceof BiFeature.Property || biFeature instanceof BiFeature.IdxProperty ) && biAnalyser.isNullProperties() ) {
            //setIconBase( biFeature.getIconBase(true));
            return grayFilter.createDisabledImage(super.getIcon(type));
        }
        if( biFeature instanceof BiFeature.EventSet && biAnalyser.isNullEventSets() ) {
            //setIconBase( biFeature.getIconBase(true));
            return grayFilter.createDisabledImage(super.getIcon(type));
        }
        if( biFeature instanceof BiFeature.Method && biAnalyser.isNullMethods() ) {
            //setIconBase( biFeature.getIconBase(true));
            return grayFilter.createDisabledImage(super.getIcon(type));
        }
        //setIconBase( biFeature.getIconBase(false));
        return super.getIcon(type);
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
                       return (!(biFeature instanceof BiFeature.Descriptor )) ? biFeature.getName () : ((BiFeature.Descriptor)biFeature).getBeanName();
                   }
                   public void setValue (Object val) throws IllegalAccessException {
                       throw new IllegalAccessException(GenerateBeanInfoAction.getString("MSG_Cannot_Write"));
                   }
               });

        ps.put( BiNode.createProperty ( biFeature, Boolean.TYPE,
                                        PROP_EXPERT, 
                                        GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_EXPERT ),
                                        GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_EXPERT ),
                                        "isExpert", "setExpert" ));
        ps.put( BiNode.createProperty ( biFeature, Boolean.TYPE,
                                        PROP_HIDDEN, 
                                        GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_HIDDEN ),
                                        GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_HIDDEN ),
                                        "isHidden", "setHidden" ));
        ps.put( BiNode.createProperty ( biFeature, Boolean.TYPE,
                                        PROP_PREFERRED, 
                                        GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_PREFERRED ),
                                        GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_PREFERRED ),
                                        "isPreferred", "setPreferred" ));

        ps.put(new CodePropertySupportRW(
                   PROP_DISPLAY_NAME,
                   String.class,
                   GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_DISPLAY_NAME ),
                   GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_DISPLAY_NAME )
               ) {
                   public Object getValue () {
                       return biFeature.getDisplayName() != null ? biFeature.getDisplayName() : "null";
                   }
                   public void setValue (Object val) throws
                       IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                       try {
                           if( "null".equals((String)val) )
                                val = null;
                           biFeature.setDisplayName ( (String)val );
                       } catch (ClassCastException e) {
                           throw new IllegalArgumentException ();
                       }
                   }
               });

        ps.put(new CodePropertySupportRW (//PropertySupport.ReadWrite (
                   PROP_SHORT_DESCRIPTION,
                   String.class,
                   GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_SHORT_DESCRIPTION ),
                   GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_SHORT_DESCRIPTION )
               ) {
                   public Object getValue () {
                       String toRet = biFeature.getShortDescription () != null ? biFeature.getShortDescription () : "null";
                       return toRet;
                   }
                   public void setValue (Object val) throws
                       IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                       try {
                           if( "null".equals((String)val) )
                                val = null;
                           biFeature.setShortDescription ( (String)val );
                       } catch (ClassCastException e) {
                           throw new IllegalArgumentException ();
                       }
                   }
                });
        // Add special properties according to type of feature
        if( ! (biFeature instanceof BiFeature.Descriptor) )
            addIncludedProperty( ps );
        else
            addCustomizerProperty( sheet );

        if ( biFeature instanceof BiFeature.Property || biFeature instanceof BiFeature.IdxProperty )
            addExpertProperty( sheet );
        else if ( biFeature instanceof BiFeature.EventSet )
            addExpertEventSet( sheet );

        // and set new sheet
        setSheet(sheet);
    }

    protected void addIncludedProperty( Sheet.Set ps ) {    
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
                           setIconBase( biFeature.getIconBase(false) );
                       } catch (ClassCastException e) {
                           throw new IllegalArgumentException ();
                       }
                   }
               });
    }
    
    protected void addCustomizerProperty( Sheet sheet ) {
        Sheet.Set ps = Sheet.createExpertSet();
    
          ps.put(new CodePropertySupportRW(//PropertySupport.ReadWrite (
          //ps.put(new PropertySupport.ReadWrite (
               PROP_DISPLAY_NAME,
                   String.class,
                   GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_CUSTOMIZER ),
                   GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_CUSTOMIZER )
               ) {
                   public Object getValue () {
                       String toRet = ((BiFeature.Descriptor)biFeature).getCustomizer() != null ? ((BiFeature.Descriptor)biFeature).getCustomizer() : "null";
                       return toRet;
                   }
                   public void setValue (Object val) throws
                       IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                       try {
                           if( "null".equals((String)val) )
                                val = null;
                           ((BiFeature.Descriptor)biFeature).setCustomizer( (String)val );
                       } catch (ClassCastException e) {
                           throw new IllegalArgumentException ();
                       }
                   }
               });
        sheet.put( ps );
    }

    protected void addExpertProperty( Sheet sheet ) {
        Sheet.Set ps = Sheet.createExpertSet();

        ps.put( BiNode.createProperty ( (BiFeature.Property)biFeature, Boolean.TYPE,
                                        PROP_BOUND, 
                                        GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_BOUND ),
                                        GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_BOUND ),
                                        "isBound", "setBound" ));
        ps.put( BiNode.createProperty ( (BiFeature.Property)biFeature, Boolean.TYPE,
                                        PROP_CONSTRAINED, 
                                        GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_CONSTRAINED ),
                                        GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_CONSTRAINED ),
                                        "isConstrained", "setConstrained" ));
        ps.put(new PropertySupport (
                   PROP_MODE,
                   Integer.TYPE,    //int.class !!!!????,
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
                           setIconBase( biFeature.getIconBase(false) );
                       } catch (ClassCastException e) {
                           throw new IllegalArgumentException ();
                       }
                   }
                   public PropertyEditor getPropertyEditor () {
                       return new org.netbeans.modules.beans.ModePropertyEditor();
                   }
               });
        ps.put( BiNode.createProperty ( (BiFeature.Property)biFeature, String.class,
                                        PROP_EDITOR_CLASS, 
                                        GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_EDITOR_CLASS ),
                                        GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_EDITOR_CLASS ),
                                        "getPropertyEditorClass", "setPropertyEditorClass" ));

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

        ps.put( BiNode.createProperty ( (BiFeature.EventSet)biFeature, Boolean.TYPE,
                                        PROP_UNICAST, 
                                        GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_UNICAST ),
                                        GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_UNICAST ),
                                        "isUnicast", null ));

        ps.put( BiNode.createProperty ( (BiFeature.EventSet)biFeature, Boolean.TYPE,
                                        PROP_IN_DEFAULT_EVENTSET, 
                                        GenerateBeanInfoAction.getString ("PROP_Bi_" + PROP_IN_DEFAULT_EVENTSET ),
                                        GenerateBeanInfoAction.getString ("HINT_Bi_" + PROP_IN_DEFAULT_EVENTSET ),
                                        "isInDefaultEventSet", "setInDefaultEventSet" ));

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
        SystemAction [] staticActions = null;
        //if (staticActions == null) {
            if( ! (biFeature instanceof BiFeature.Descriptor) ){
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
            else {                
                staticActions = new SystemAction[0];
            }
        //}
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
        setIconBase( biFeature.getIconBase(false) );
    }

    /** Includes/excludes the pattern from bean info */
    public void include( boolean value ) {
        if (( value && biFeature.isIncluded() ) ||
                ( !value && !biFeature.isIncluded() ) )
            return;

        biFeature.setIncluded ( value );
        firePropertyChange( PROP_INCLUDED, new Boolean( !biFeature.isIncluded() ), new Boolean( biFeature.isIncluded() ) );
        setIconBase( biFeature.getIconBase(false) );
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

    BiAnalyser getBiAnalyser () {
        return biAnalyser;
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
        return (!(biFeature instanceof BiFeature.Descriptor )) ? biFeature.getName () : ((BiFeature.Descriptor)biFeature).getBeanName();
        //return biFeature.getName();
    }
    
    public void iconChanged(){
        if( biFeature instanceof BiFeature.Descriptor  && biAnalyser.isNullDescriptor() ) {
            setIconBase( biFeature.getIconBase(true));            
        }
        else if( ( biFeature instanceof BiFeature.Property || biFeature instanceof BiFeature.IdxProperty ) && biAnalyser.isNullProperties() ) {
            setIconBase( biFeature.getIconBase(true));            
        }
        else if( biFeature instanceof BiFeature.EventSet && biAnalyser.isNullEventSets() ) {
            setIconBase( biFeature.getIconBase(true));            
        }
        else if( biFeature instanceof BiFeature.Method && biAnalyser.isNullMethods() ) {
            setIconBase( biFeature.getIconBase(true));            
        }
        else setIconBase( biFeature.getIconBase(false)); 
        
        fireIconChange();
    }
    
    abstract class CodePropertySupportRW extends PropertySupport.ReadWrite
    {
        CodePropertySupportRW(String name, Class type,
                              String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        public PropertyEditor getPropertyEditor() {
            return new PropertyEditorSupport() {
                public java.awt.Component getCustomEditor() {
                    return new CustomCodeEditor(CodePropertySupportRW.this);
                }

                public boolean supportsCustomEditor() {
                    return true;
                }
            };            
        }
    }
}

/*
 * Log
 *
 */
