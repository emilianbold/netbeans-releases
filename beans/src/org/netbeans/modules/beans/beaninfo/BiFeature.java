/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.beans.beaninfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.openide.src.MethodElement;
import org.openide.nodes.Node;

import com.netbeans.developer.modules.beans.Pattern;
import com.netbeans.developer.modules.beans.PropertyPattern;
import com.netbeans.developer.modules.beans.IdxPropertyPattern;
import com.netbeans.developer.modules.beans.EventSetPattern;

/** The basic class representing features included in BeanInfo.
* 
* @author Petr Hrebejk
*/
abstract class BiFeature extends Object implements IconBases, Node.Cookie {
   
  /** generated Serialized Version UID */
  //static final long serialVersionUID = -8680621542479107034L;

  // Function names for code generation and reconition
  private static final String TEXT_EXPERT = "setExpert";
  private static final String TEXT_HIDDEN = "setHidden";
  private static final String TEXT_PREFERRED = "setPreferred";
  private static final String TEXT_DISPLAY_NAME = "setDisplayName";
  private static final String TEXT_SHORT_DESCRIPTION = "setShortDescription";


  // variables ..........................................................................
  private String displayName = null;
  private boolean expert = false;
  private boolean hidden = false;
  private String name = null;
  private boolean preferred  = false;
  private String shortDescription = null;
  private boolean included = true;
 
  
  /**
  * Creates empty BiFeature.
  */
  public BiFeature( Pattern pattern ) {
    name = pattern.getName();
  }

  /*
  public BiFeature( MethodElement me ) {
    displayName = "\"\""
    name = me.getName().getName();
  }
  */

  abstract String getCreationString();


  // Definition of properties ....................................................................

  public String getDisplayName() {
    return displayName;
  }
  
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }
  
  public boolean isExpert() {
    return expert;
  }
  
  public void setExpert(boolean expert) {
    this.expert = expert;
  }
  
  public boolean isHidden() {
    return hidden;
  }
  
  public void setHidden(boolean hidden) {
    this.hidden = hidden;
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public boolean isPreferred() {
    return preferred;
  }
  
  public void setPreferred(boolean preferred) {
    this.preferred = preferred;
  }
  
  public String getShortDescription() {
    return shortDescription;
  }
  
  public void setShortDescription(String shortDescription) {
    this.shortDescription = shortDescription;
  }
  
  public boolean isIncluded() {
    return included;
  }
  
  public void setIncluded(boolean included) {
    this.included = included;
  }
  
  /** Generates collection of strings which customize the fheature */
  Collection getCustomizationStrings () {
    ArrayList col = new ArrayList();
    StringBuffer sb = new StringBuffer( 100 );

    if ( expert ) {
      sb.setLength( 0 );
      sb.append( TEXT_EXPERT ).append( " ( true )" );
      col.add( sb.toString() );
    }
    if ( hidden ) {
      sb.setLength( 0 );
      sb.append( TEXT_HIDDEN ).append( " ( true )" );
      col.add( sb.toString() );
    }
    if ( preferred ) {
      sb.setLength( 0 );
      sb.append( TEXT_PREFERRED ).append( " ( true )" );
      col.add( sb.toString() );
    }
    if ( displayName != null && displayName.trim().length() > 0) {
      sb.setLength( 0 );
      sb.append( TEXT_DISPLAY_NAME ).append( " ( ");
      sb.append( displayName ).append( " )" );
      col.add( sb.toString() );
    }
    if ( shortDescription != null && shortDescription.trim().length() > 0 ) {
      sb.setLength( 0 );
      sb.append( TEXT_SHORT_DESCRIPTION ).append( " ( ");
      sb.append( shortDescription ).append( " )" );
      col.add( sb.toString() );
    }

    return col;
  }

    
  /** Analyzes the bean info code for all customizations */
  void analyzeCustomization ( Collection code ) {
    setIncluded( false );

    Iterator it = code.iterator();

    String stNew = new String( getName() + "]=new" );
    String stExpert = new String( getName() + "]." + TEXT_EXPERT );
    String stHidden = new String( getName() + "]." + TEXT_HIDDEN );
    String stPreferred = new String( getName() + "]." + TEXT_PREFERRED );
    String stDisplayName = new String( getName() + "]." + TEXT_DISPLAY_NAME );
    String stShortDescription = new String( getName() + "]." + TEXT_SHORT_DESCRIPTION );

    while( it.hasNext() ) {
      String statement = ( String ) it.next();
    
      if ( statement.indexOf( stNew ) != -1 ) {
        setIncluded( true );
        analyzeCreationString( statement ); // Implemented in descendants
        continue;
      }
      if ( statement.indexOf( stExpert ) != -1 ) {
        setExpert( true );
        continue;
      }
      if ( statement.indexOf( stHidden ) != -1 ) { 
        setHidden( true );
        continue;
      }
      if ( statement.indexOf( stPreferred ) != -1 ) {
        setPreferred( true );
        continue;
      }
      if ( statement.indexOf( stDisplayName ) != -1 ) {
        String[] params = BiAnalyser.getParameters( statement );
        if ( params.length > 0 )
          setDisplayName( params[0] );
        continue;
      }
      if ( statement.indexOf( stShortDescription ) != -1 ) {
        String[] params = BiAnalyser.getParameters( statement );
        if ( params.length > 0 )
          setShortDescription( params[0] );
        continue;
      }
      analyzeCustomizationString( statement ); // Implemented in descendants
    }
  }

  /** gets the current icon base for the feature */
  abstract String getIconBase();

  abstract void analyzeCreationString( String statement );
  abstract void analyzeCustomizationString( String statement );
  
  static class Property extends BiFeature {
    
    private PropertyPattern pattern;
    
    private static final String TEXT_BOUND = "setBound";
    private static final String TEXT_CONSTRAINED = "setConstrained";
    private static final String TEXT_PROPERTY_EDITOR = "setPropertyEditorClass";
    
    private boolean bound;
    private boolean constrained;
    private int mode;
    private String propertyEditorClass;
    
    Property( PropertyPattern pp ) {
      super( pp );
      mode = pp.getMode();
      pattern = pp;
    }
    
    public boolean isBound() {
      return bound;
    }
    
    public void setBound(boolean bound) {
      this.bound = bound;
    }
    
    public boolean isConstrained() {
      return constrained;
    }
  
    public void setConstrained(boolean constrained) {
      this.constrained = constrained;
    }

    public int getMode() {
      return mode;
    }
  
    public void setMode(int mode) {
      this.mode = mode;
    }

    public boolean modeChangeable() {
      return pattern.getMode() == PropertyPattern.READ_WRITE;
    }
     
    public String getPropertyEditorClass() {
      return propertyEditorClass;
    }
    
    public void setPropertyEditorClass(String propertyEditorClass) {
      this.propertyEditorClass = propertyEditorClass;
    } 
    
    /** Returns the call to constructor of PropertyDescriptor */
    String getCreationString () {
      StringBuffer sb = new StringBuffer( 100 );

      sb.append( "new PropertyDescriptor ( " );
      sb.append( "\"" + this.getName() + "\", " );
      sb.append( pattern.getDeclaringClass().getName().getName() + ".class, " );

      if ( pattern.getGetterMethod() != null && getMode() != PropertyPattern.WRITE_ONLY )
        sb.append( "\"" + pattern.getGetterMethod().getName().getName() + "\", " );
      else 
        sb.append( "null, ");

      if ( pattern.getSetterMethod() != null && getMode() != PropertyPattern.READ_ONLY )
        sb.append( "\"" + pattern.getSetterMethod().getName().getName() + "\" )" );
      else
        sb.append( "null )");

      return sb.toString();
    }

    String getIconBase() {
      if ( mode == PropertyPattern.READ_ONLY )
        return BIF_PROPERTY_RO + ( included ? "S" : "N" );
      else if ( mode == PropertyPattern.WRITE_ONLY )
        return BIF_PROPERTY_WO + ( included ? "S" : "N" );
      else
        return BIF_PROPERTY_RW + ( included ? "S" : "N" );

    }
    
    Collection getCustomizationStrings () {
      Collection col = super.getCustomizationStrings();
      StringBuffer sb = new StringBuffer( 100 );

      if ( bound ) {
        sb.setLength( 0 );
        sb.append( TEXT_BOUND ).append( " ( true )" );
        col.add( sb.toString() );
      }
      if ( constrained ) {
        sb.setLength( 0 );
        sb.append( TEXT_CONSTRAINED ).append( " ( true )" );
        col.add( sb.toString() );
      }
      if ( propertyEditorClass != null && propertyEditorClass.trim().length() > 0 ) {
        sb.setLength( 0 );
        sb.append( TEXT_PROPERTY_EDITOR ).append( " ( ");
        sb.append( propertyEditorClass ).append( " )" );
        col.add( sb.toString() );
      }
      
      return col;
    }  
    
    void analyzeCustomizationString( String statement ) {
          
      String stBound = new String( this.getName() + "]." + TEXT_BOUND );
      String stConstrained = new String( this.getName() + "]." + TEXT_CONSTRAINED );
      String stPropertyEditor = new String( this.getName() + "]." + TEXT_PROPERTY_EDITOR );  
        
      if ( statement.indexOf( stBound ) != -1 ) {
        setBound( true );
        return;
      }

      if ( statement.indexOf( stConstrained ) != -1 ) {
        setConstrained( true );
        return;
      }

      if ( statement.indexOf( stPropertyEditor ) != -1 ) {
        String[] params = BiAnalyser.getParameters( statement );
        if ( params.length > 0 )
          setPropertyEditorClass( params[0] );
        return;
      }
    }
    
    void analyzeCreationString( String statement ) {
    
      String[] params = BiAnalyser.getParameters( statement );

            // Analyses if there is mode restriction in the existing BeanInfo
      if ( params.length == 4 && mode == PropertyPattern.READ_WRITE ) {
        if ( params[2].equals( "null" ) )
          mode = PropertyPattern.WRITE_ONLY;
        else if ( params[3].equals( "null" ) )
          mode = PropertyPattern.READ_ONLY;          
      }
    }
  }
  
  static class IdxProperty extends Property {
    
    private boolean niGetter;
    private boolean niSetter;
    
    IdxPropertyPattern pattern;

    IdxProperty( IdxPropertyPattern pp ) {
      super( pp );
      pattern = pp;
      
      niGetter = hasNiGetter();
      niSetter = hasNiSetter();
    }
    
    boolean isNiGetter() {
      return niGetter;
    }
    
    void setNiGetter( boolean niGetter ) {
      this.niGetter = hasNiGetter() ? niGetter : false;
    }

    boolean isNiSetter() {
      return niSetter;
    }
    
    void setNiSetter( boolean niSetter ) {
      this.niGetter = hasNiSetter() ? niSetter : false;
    }

   
    boolean hasNiGetter() {
      return pattern.getGetterMethod() != null;
    }
    
    boolean hasNiSetter() {
      return pattern.getGetterMethod() != null;
    }
    
    /** Returns the call to constructor of IndexedPropertyDescriptor */
    String getCreationString ()  {

      StringBuffer sb = new StringBuffer( 100 );

      sb.append( "new IndexedPropertyDescriptor ( " );
      sb.append( "\"" + this.getName() + "\", " );
      sb.append( pattern.getDeclaringClass().getName().getName() + ".class, " );

      if ( pattern.getGetterMethod() != null && niGetter )
        sb.append( "\"" + pattern.getGetterMethod().getName().getName() + "\", " );
      else 
        sb.append( "null, ");

      if ( pattern.getSetterMethod() != null && niSetter )
        sb.append( "\"" + pattern.getSetterMethod().getName().getName() + "\", " );
      else
        sb.append( "null, ");

      if ( pattern.getIndexedGetterMethod() != null && getMode() != PropertyPattern.WRITE_ONLY )
        sb.append( "\"" + pattern.getIndexedGetterMethod().getName().getName() + "\", " );
      else 
        sb.append( "null, ");

      if ( pattern.getIndexedSetterMethod() != null && getMode() != PropertyPattern.READ_ONLY )
        sb.append( "\"" + pattern.getIndexedSetterMethod().getName().getName() + "\" )" );
      else
        sb.append( "null )");
 
      return sb.toString();
      }

      String getIconBase() {
        if ( getMode() == PropertyPattern.READ_ONLY )
          return BIF_IDXPROPERTY_RO + ( included ? "S" : "N" );
        else if ( getMode() == PropertyPattern.WRITE_ONLY )
          return BIF_IDXPROPERTY_WO + ( included ? "S" : "N" );
        else
          return BIF_IDXPROPERTY_RW + ( included ? "S" : "N" );
      }

      void analyzeCreationString( String statement ) {
        String[] params = BiAnalyser.getParameters( statement );

              // Analyses if there is mode restriction in the existing BeanInfo
        if ( params.length == 6 && getMode() == PropertyPattern.READ_WRITE ) {
          if ( params[4].equals( "null" ) )
            setMode( PropertyPattern.WRITE_ONLY );
          else if ( params[5].equals( "null" ) )
            setMode( PropertyPattern.READ_ONLY );          

             // Analayses if there is restriction on non indexed getter or setter
        if ( hasNiGetter() && params[2].equals( null ) )
          niGetter = false;
        if ( hasNiGetter() && params[3].equals( null ) )
          niSetter = false;

        }
      }

  }
  
  static class EventSet extends BiFeature {

    EventSetPattern pattern;
    
    private static final String TEXT_UNICAST = "setUnicast";
    private static final String TEXT_IN_DEFAULT = "setInDefaultEventSet";
    
    private boolean isInDefaultEventSet = true;

    EventSet( EventSetPattern esp ) {
      super( esp );
      pattern = esp;
    }

    public boolean isUnicast() {
      return pattern.isUnicast();
    }
    
    public boolean isInDefaultEventSet() {
      return isInDefaultEventSet;
    }
    
    public void setInDefaultEventSet( boolean isInDefaultEventSet ) {
      this.isInDefaultEventSet = isInDefaultEventSet;
    }
  
    /** Returns the call to constructor of EventSetDescriptor */
    String getCreationString () {
      StringBuffer sb = new StringBuffer( 100 );

      sb.append( "new EventSetDescriptor ( " );
      sb.append( pattern.getDeclaringClass().getName().getName() + ".class, " );
      sb.append( "\"" + this.getName() + "\", " );
      sb.append( pattern.getType().toString() + ".class, " );
      sb.append( "new String[0], " );
      sb.append( "\"" + pattern.getAddListenerMethod().getName().getName() + "\", " );
      sb.append( "\"" + pattern.getRemoveListenerMethod().getName().getName() + "\" )" );

      return sb.toString();
    }

    String getIconBase() {
      if ( isUnicast() )
        return BIF_EVENTSET_UNICAST + ( included ? "S" : "N" );
      else
        return BIF_EVENTSET_MULTICAST + ( included ? "S" : "N" );
    }

    Collection getCustomizationStrings () {
      Collection col = super.getCustomizationStrings();
      StringBuffer sb = new StringBuffer( 100 );

      if ( isUnicast() ) {
        sb.setLength( 0 );
        sb.append( TEXT_UNICAST ).append( " ( true )" );
        col.add( sb.toString() );
      }
      if ( !isInDefaultEventSet ) {
        sb.setLength( 0 );
        sb.append( TEXT_IN_DEFAULT ).append( " ( false )" );
        col.add( sb.toString() );
      }
      
      return col;
    }
    
    void analyzeCustomizationString( String statement ) {
          
      String stUnicast = new String( this.getName() + "]." + TEXT_UNICAST );
      String stInDefault = new String( this.getName() + "]." + TEXT_IN_DEFAULT ); 
      /*
      if ( statement.indexOf( stUnicast ) != -1 ) {
        setUnicast( true );
        return;
      }
      */
      if ( statement.indexOf( stInDefault ) != -1 ) {
        setInDefaultEventSet( false );
        return;
      }
    }
    
    void analyzeCreationString( String statement ) {
    }
    
  }
 
  /*
  static class Method extends BiFeature {
    MethodElement element;

    Method( MethodElement me ) {
      super( me );
      element = me;
    }

    // Returns the call to constructor of MethodDescriptor 
    String getCreationString () {
      StringBuffer sb = new StringBuffer( 100 );
 
      return sb.toString();
    }

  }
  */


}
/*
 * Log
 *
 */
