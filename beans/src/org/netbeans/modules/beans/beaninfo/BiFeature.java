/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.beans.beaninfo;

import java.util.*;

import org.openide.src.MethodElement;
import org.openide.src.ClassElement;
import org.openide.nodes.Node;
import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

import org.netbeans.modules.beans.Pattern;
import org.netbeans.modules.beans.PropertyPattern;
import org.netbeans.modules.beans.IdxPropertyPattern;
import org.netbeans.modules.beans.EventSetPattern;
import org.netbeans.modules.beans.PatternAnalyser;

/** The basic class representing features included in BeanInfo.
* 
* @author Petr Hrebejk
*/
public abstract class BiFeature extends Object implements IconBases, Node.Cookie, Comparable {

    /** generated Serialized Version UID */
    //static final long serialVersionUID = -8680621542479107034L;

    // Function names for code generation and reconition
    private static final String TEXT_EXPERT = "setExpert"; // NOI18N
    private static final String TEXT_HIDDEN = "setHidden"; // NOI18N
    private static final String TEXT_PREFERRED = "setPreferred"; // NOI18N
    private static final String TEXT_DISPLAY_NAME = "setDisplayName"; // NOI18N
    private static final String TEXT_SHORT_DESCRIPTION = "setShortDescription"; // NOI18N


    // variables ..........................................................................
    private String displayName = null;
    private boolean expert = false;
    private boolean hidden = false;
    private String name = null;
    private boolean preferred  = false;
    private String shortDescription = null;
    private boolean included = true;

    private String brackets = "]";
    /**
    * Creates empty BiFeature.
    */
    public BiFeature( Pattern pattern ) {
        name = pattern.getName();
    }

    public BiFeature( MethodElement me ) {
        displayName = "\"\"";
        name = me.getName().getName();
    }

    public BiFeature( ClassElement ce ) {        
        name = "beanDescriptor";//NOI18N GenerateBeanInfoAction.getString("CTL_NODE_DescriptorDisplayName");
    }

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

    abstract String getBracketedName();

    String getBrackets(){
        return brackets;
    }
    
    void setBrackets(String brackets){
        this.brackets = brackets;
    }

    public boolean isIncluded() {
        return included;
    }

    public void setIncluded(boolean included) {
        this.included = included;
    }
    
    public String getToolTip() {
        return this.getName();
    }

    /** Generates collection of strings which customize the feature */
    Collection getCustomizationStrings () {
        ArrayList col = new ArrayList();
        StringBuffer sb = new StringBuffer( 100 );

        if ( expert ) {
            sb.setLength( 0 );
            sb.append( TEXT_EXPERT ).append( " ( true )" ); // NOI18N
            col.add( sb.toString() );
        }
        if ( hidden ) {
            sb.setLength( 0 );
            sb.append( TEXT_HIDDEN ).append( " ( true )" ); // NOI18N
            col.add( sb.toString() );
        }
        if ( preferred ) {
            sb.setLength( 0 );
            sb.append( TEXT_PREFERRED ).append( " ( true )" ); // NOI18N
            col.add( sb.toString() );
        }
        if ( displayName != null && displayName.trim().length() > 0) {
            sb.setLength( 0 );
            sb.append( TEXT_DISPLAY_NAME ).append( " ( "); // NOI18N
            sb.append( displayName ).append( " )" ); // NOI18N
            col.add( sb.toString() );
        }
        if ( shortDescription != null && shortDescription.trim().length() > 0 ) {
            sb.setLength( 0 );
            sb.append( TEXT_SHORT_DESCRIPTION ).append( " ( "); // NOI18N
            sb.append( shortDescription ).append( " )" ); // NOI18N
            col.add( sb.toString() );
        }

        return col;
    }


    /** Analyzes the bean info code for all customizations */
    void analyzeCustomization ( Collection code ) {
        setIncluded( false );
        
        Iterator it = code.iterator();
        String n = getBracketedName();

        String stNew = new String( n + "=new" ); // NOI18N
        String stExpert = new String( n + "." + TEXT_EXPERT ); // NOI18N
        String stHidden = new String( n + "." + TEXT_HIDDEN ); // NOI18N
        String stPreferred = new String( n + "." + TEXT_PREFERRED ); // NOI18N
        String stDisplayName = new String( n + "." + TEXT_DISPLAY_NAME ); // NOI18N
        String stShortDescription = new String( n + "." + TEXT_SHORT_DESCRIPTION ); // NOI18N
        while( it.hasNext() ) {
            String statement = ( String ) it.next();

            if ( statement.indexOf( stNew ) != -1 ) {
                setIncluded( true );
                analyzeCreationString( statement ); // Implemented in descendants
                continue;
            }
            if ( statement.indexOf( stExpert ) != -1 ) {
                this.setExpert( true );
                continue;
            }
            if ( statement.indexOf( stHidden ) != -1 ) {
                this.setHidden( true );
                continue;
            }
            if ( statement.indexOf( stPreferred ) != -1 ) {
                this.setPreferred( true );
                continue;
            }
            if ( statement.indexOf( stDisplayName ) != -1 ) {
                String param = BiAnalyser.getArgumentParameter( statement );
                
                if ( param != null )
                    this.setDisplayName( param );
                continue;
            }
            if ( statement.indexOf( stShortDescription ) != -1 ) {
                String param = BiAnalyser.getArgumentParameter( statement );
                
                if ( param != null )
                    this.setShortDescription( param );
                continue;
            }
            analyzeCustomizationString( statement ); // Implemented in descendants
        }
    }

    /** gets the current icon base for the feature */
    abstract String getIconBase( boolean defaultIcon );

    abstract void analyzeCreationString( String statement );
    abstract void analyzeCustomizationString( String statement );

    public static class Descriptor extends BiFeature {
        ClassElement element;
        private String varName;
        private ClassElement ce;
        String customizer;
        
        Descriptor( ClassElement ce ) {
            super( ce );
            element = ce;
            this.ce = ce;
        }

        /** Returns the call to constructor of PropertyDescriptor */
        String getCreationString () {
            StringBuffer sb = new StringBuffer( 100 );

            sb.append( "new BeanDescriptor  ( " ); // NOI18N
            sb.append( getBeanName() + ".class , " ); // NOI18N
            sb.append( String.valueOf(getCustomizer()) + " )"); // NOI18N

            return sb.toString();
        }

        String getIconBase( boolean defaultIcon ) {
            //now there be no icon !!!
            //if( defaultIcon )
            //    return null;
            //else                
            //    return null;    // NOI18N
            return BIF_DESCRIPTOR; // NOI18N
        }
        

        Collection getCustomizationStrings () {
            Collection col = super.getCustomizationStrings();
            StringBuffer sb = new StringBuffer( 100 );

            return col;
        }

        void analyzeCustomizationString( String statement ) {
        }

        void analyzeCreationString( String statement ) {
            int beg = statement.indexOf( ',' );
            int end = statement.lastIndexOf( ')' );

            if ( beg != -1 && end != -1 && ( ++beg < end ) )
                setCustomizer( statement.substring( beg, end ) );
            else
                setCustomizer( null );
        }

        String getBracketedName() {
            return getName();
        }

        String getBrackets(){
            return "";
        }
        
        public String getCustomizer(){
            return customizer;
        }

        public void setCustomizer(String customizer){
            this.customizer = customizer;
        }
        
        //overrides BiFeature.isIncluded(), this property is always included ( disabled by setting get from Introspection )
        public boolean isIncluded() {
            return true;
        }
        
        public String getBeanName(){
            return element.getName().getName();
        }
    }
    
    public static class Property extends BiFeature {

        private PropertyPattern pattern;

        private static final String TEXT_BOUND = "setBound"; // NOI18N
        private static final String TEXT_CONSTRAINED = "setConstrained"; // NOI18N
        private static final String TEXT_PROPERTY_EDITOR = "setPropertyEditorClass"; // NOI18N

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

        String getBracketedName() {
            return "[PROPERTY_" + getName() + "]";
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

            sb.append( "new PropertyDescriptor ( " ); // NOI18N
            sb.append( "\"" + this.getName() + "\", " ); // NOI18N
            sb.append( pattern.getDeclaringClass().getName().getName() + ".class, " ); // NOI18N

            if ( pattern.getGetterMethod() != null && getMode() != PropertyPattern.WRITE_ONLY )
                sb.append( "\"" + pattern.getGetterMethod().getName().getName() + "\", " ); // NOI18N
            else
                sb.append( "null, "); // NOI18N

            if ( pattern.getSetterMethod() != null && getMode() != PropertyPattern.READ_ONLY )
                sb.append( "\"" + pattern.getSetterMethod().getName().getName() + "\" )" ); // NOI18N
            else
                sb.append( "null )"); // NOI18N

            return sb.toString();
        }

        String getIconBase( boolean defaultIcon ) {
            if( defaultIcon ) { 
                return BIF_PROPERTY_RW + "S"; // NOI18N
            }
            else {
                if ( mode == PropertyPattern.READ_ONLY )
                    return BIF_PROPERTY_RO + ( this.isIncluded() ? "S" : "N" ); // NOI18N
                else if ( mode == PropertyPattern.WRITE_ONLY )
                    return BIF_PROPERTY_WO + ( this.isIncluded() ? "S" : "N" ); // NOI18N
                else
                    return BIF_PROPERTY_RW + ( this.isIncluded() ? "S" : "N" ); // NOI18N
            }
        }

        Collection getCustomizationStrings () {
            Collection col = super.getCustomizationStrings();
            StringBuffer sb = new StringBuffer( 100 );

            if ( bound ) {
                sb.setLength( 0 );
                sb.append( TEXT_BOUND ).append( " ( true )" ); // NOI18N
                col.add( sb.toString() );
            }
            if ( constrained ) {
                sb.setLength( 0 );
                sb.append( TEXT_CONSTRAINED ).append( " ( true )" ); // NOI18N
                col.add( sb.toString() );
            }
            if ( propertyEditorClass != null && propertyEditorClass.trim().length() > 0 ) {
                sb.setLength( 0 );
                sb.append( TEXT_PROPERTY_EDITOR ).append( " ( "); // NOI18N
                sb.append( propertyEditorClass ).append( " )" ); // NOI18N
                col.add( sb.toString() );
            }

            return col;
        }

        void analyzeCustomizationString( String statement ) {
            String n = getBracketedName();
            String stBound = new String( n + "." + TEXT_BOUND ); // NOI18N
            String stConstrained = new String( n + "." + TEXT_CONSTRAINED ); // NOI18N
            String stPropertyEditor = new String( n + "." + TEXT_PROPERTY_EDITOR ); // NOI18N
            int peIndex;
            
            if ( statement.indexOf( stBound ) != -1 ) {
                setBound( true );
                return;
            }

            if ( statement.indexOf( stConstrained ) != -1 ) {
                setConstrained( true );
                return;
            }

            peIndex = statement.indexOf( stPropertyEditor );
            if ( peIndex != -1 ) {
                String paramString = statement.substring(peIndex + stPropertyEditor.length());
                String[] params = BiAnalyser.getParameters( paramString );
                if ( params.length > 0 )
                    setPropertyEditorClass( params[0] );
                return;
            }
        }

        void analyzeCreationString( String statement ) {

            String[] params = BiAnalyser.getParameters( statement );

            // Analyses if there is mode restriction in the existing BeanInfo
            if ( params.length == 4 && mode == PropertyPattern.READ_WRITE ) {
                if ( params[2].equals( "null" ) ) // NOI18N
                    mode = PropertyPattern.WRITE_ONLY;
                else if ( params[3].equals( "null" ) ) // NOI18N
                    mode = PropertyPattern.READ_ONLY;
            }
        }
    }

    public static class IdxProperty extends Property {

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

            sb.append( "new IndexedPropertyDescriptor ( " ); // NOI18N
            sb.append( "\"" + this.getName() + "\", " ); // NOI18N
            sb.append( pattern.getDeclaringClass().getName().getName() + ".class, " ); // NOI18N

            if ( pattern.getGetterMethod() != null && niGetter )
                sb.append( "\"" + pattern.getGetterMethod().getName().getName() + "\", " ); // NOI18N
            else
                sb.append( "null, "); // NOI18N

            if ( pattern.getSetterMethod() != null && niSetter )
                sb.append( "\"" + pattern.getSetterMethod().getName().getName() + "\", " ); // NOI18N
            else
                sb.append( "null, "); // NOI18N

            if ( pattern.getIndexedGetterMethod() != null && getMode() != PropertyPattern.WRITE_ONLY )
                sb.append( "\"" + pattern.getIndexedGetterMethod().getName().getName() + "\", " ); // NOI18N
            else
                sb.append( "null, "); // NOI18N

            if ( pattern.getIndexedSetterMethod() != null && getMode() != PropertyPattern.READ_ONLY )
                sb.append( "\"" + pattern.getIndexedSetterMethod().getName().getName() + "\" )" ); // NOI18N
            else
                sb.append( "null )"); // NOI18N

            return sb.toString();
        }

        String getIconBase( boolean defaultIcon ) {
            if( defaultIcon ) {
                return BIF_IDXPROPERTY_RW + "S"; // NOI18N
            }
            else {
                if ( getMode() == PropertyPattern.READ_ONLY )
                    return BIF_IDXPROPERTY_RO + ( this.isIncluded() ? "S" : "N" ); // NOI18N
                else if ( getMode() == PropertyPattern.WRITE_ONLY )
                    return BIF_IDXPROPERTY_WO + ( this.isIncluded() ? "S" : "N" ); // NOI18N
                else
                    return BIF_IDXPROPERTY_RW + ( this.isIncluded() ? "S" : "N" ); // NOI18N
            }
        }

        void analyzeCreationString( String statement ) {
            String[] params = BiAnalyser.getParameters( statement );

            // Analyses if there is mode restriction in the existing BeanInfo
            if ( params.length == 6 && getMode() == PropertyPattern.READ_WRITE ) {
                if ( params[4].equals( "null" ) ) // NOI18N
                    setMode( PropertyPattern.WRITE_ONLY );
                else if ( params[5].equals( "null" ) ) // NOI18N
                    setMode( PropertyPattern.READ_ONLY );

                // Analayses if there is restriction on non indexed getter or setter
                if ( hasNiGetter() && params[2].equals( null ) )
                    niGetter = false;
                if ( hasNiGetter() && params[3].equals( null ) )
                    niSetter = false;

            }
        }

    }

    public static class EventSet extends BiFeature implements Comparator {

        EventSetPattern pattern;

        private static final String TEXT_UNICAST = "setUnicast"; // NOI18N
        private static final String TEXT_IN_DEFAULT = "setInDefaultEventSet"; // NOI18N

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

        /**
         * MUST be consistent w/ generator in BiAnalyser.
         * @return
         */
        String getBracketedName() {
            return "[EVENT_" + getName() + "]";
        }

        public int compare(Object o1, Object o2) {
            if (!(o1 instanceof MethodElement) || !(o2 instanceof MethodElement))
                throw new IllegalArgumentException();
            MethodElement m1 = (MethodElement)o1;
            MethodElement m2 = (MethodElement)o2;

            return m1.getName().getName().compareTo(m2.getName().getName());
        }

        /** Returns the call to constructor of EventSetDescriptor */
        String getCreationString () {
            StringBuffer sb = new StringBuffer( 100 );
            
            MethodElement[] listenerMethods;

            try {
                org.openide.src.Type listenerType = pattern.getType();
                org.openide.src.ClassElement listener = PatternAnalyser.findClassElement(listenerType.getClassName().getFullName(), pattern);
                listenerMethods = listener.getMethods();
                Arrays.sort(listenerMethods, this);
            } catch (IllegalStateException e) {
                ErrorManager.getDefault().notify(e);
                listenerMethods = new MethodElement[0];
            }

            sb.append( "new EventSetDescriptor ( " ); // NOI18N
            sb.append( pattern.getDeclaringClass().getName().getFullName() + ".class, " ); // NOI18N
            sb.append( "\"" + this.getName() + "\", " ); // NOI18N
            try {
                sb.append( pattern.getType().getClassName().getFullName() + ".class, " ); // NOI18N
            } catch (IllegalStateException e) {
                ErrorManager.getDefault().notify(e);
                listenerMethods = new MethodElement[0];
            }
            sb.append( "new String[] {" ); // NOI18N
            for (int i = 0; i < listenerMethods.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append( "\"" + listenerMethods[i].getName() + "\"" ); // NOI18N
            }
            sb.append( "}, "); // NOI18N
            sb.append( "\"" + pattern.getAddListenerMethod().getName().getName() + "\", " ); // NOI18N
            sb.append( "\"" + pattern.getRemoveListenerMethod().getName().getName() + "\" )" ); // NOI18N

            return sb.toString();
        }

        String getIconBase( boolean defaultIcon ) {
            if( defaultIcon ) { 
                if ( isUnicast() )
                    return BIF_EVENTSET_UNICAST + "S"; // NOI18N
                else
                    return BIF_EVENTSET_MULTICAST + "S"; // NOI18N
            }
            else {
                if ( isUnicast() )
                    return BIF_EVENTSET_UNICAST + ( this.isIncluded() ? "S" : "N" ); // NOI18N
                else
                    return BIF_EVENTSET_MULTICAST + ( this.isIncluded() ? "S" : "N" ); // NOI18N
            }
        }

        Collection getCustomizationStrings () {
            Collection col = super.getCustomizationStrings();
            StringBuffer sb = new StringBuffer( 100 );

            if ( isUnicast() ) {
                sb.setLength( 0 );
                sb.append( TEXT_UNICAST ).append( " ( true )" ); // NOI18N
                col.add( sb.toString() );
            }
            if ( !isInDefaultEventSet ) {
                sb.setLength( 0 );
                sb.append( TEXT_IN_DEFAULT ).append( " ( false )" ); // NOI18N
                col.add( sb.toString() );
            }

            return col;
        }

        void analyzeCustomizationString( String statement ) {
            String n = getBracketedName();
            String stUnicast = new String( n + "."  + TEXT_UNICAST ); // NOI18N
            String stInDefault = new String( n + "." + TEXT_IN_DEFAULT ); // NOI18N
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

    public static class Method extends BiFeature {
      MethodElement element;
      private String varName;
      private MethodElement me;
      private PatternAnalyser pa;

      Method( MethodElement me, PatternAnalyser pa ) {
        super( me );
        element = me;
        this.me = me;
        this.pa = pa;
      }

        String getBracketedName() {
            return "[METHOD_" + getName() + "]";
        }
        
        private static String getTypeClass(org.openide.src.Type type, PatternAnalyser pa) {
          if (type.isPrimitive()) {
              if (type.equals(type.INT)) return "Integer.TYPE";
              else if (type.equals(type.BOOLEAN)) return "Boolean.TYPE";
              else if (type.equals(type.CHAR)) return "Character.TYPE";
              else if (type.equals(type.LONG)) return "Long.TYPE";
              else if (type.equals(type.SHORT)) return "Short.TYPE";
              else if (type.equals(type.BYTE)) return "Byte.TYPE";
              else if (type.equals(type.FLOAT)) return "Float.TYPE";
              else /*(type.equals(type.DOUBLE))*/ return "Double.TYPE";
          } else if (type.isClass()) {
              try {
                  return type.getClassName().getFullName() + ".class";
              } catch (Exception e) {
                  ErrorManager.getDefault().notify(e);
                  return type.toString() + ".class";
              }
          } else /*(type.isArray())*/ {
                 return "Class.forName(\"" + type.getVMClassName(pa.findFileObject()) + "\")";
          }
      }
      
      public String getToolTip() {
            StringBuffer sb = new StringBuffer( 100 );
            sb.append( this.element.getName().getFullName() + "("); // NOI18N
            
            org.openide.src.MethodParameter[] parameters = this.element.getParameters();
            
            for (int i = 0; i < parameters.length; i ++) {
                sb.append(parameters[i].getType().getFullString());
                if (i < (parameters.length - 1)) sb.append(", "); // NOI18N
            }
            
            sb.append(")"); // NOI18N
            return sb.toString();
      }
      
      MethodElement getElement() {
        return element;
      }

      // Returns the call to constructor of MethodDescriptor 
        String getCreationString () {
            StringBuffer sb = new StringBuffer( 100 );
            sb.append( "new MethodDescriptor ( " ); // NOI18N
            //sb.append( "Class.forName(\"" + this.element.getDeclaringClass().getName().getFullName() + "\").getMethod(\"" + this.element.getName().getFullName() + "\", "); // NOI18N
            sb.append( this.element.getDeclaringClass().getName().getFullName() + ".class.getMethod(\"" + this.element.getName().getFullName() + "\", "); // NOI18N
            sb.append( "new Class[] {"); // NOI18N
            
            org.openide.src.MethodParameter[] parameters = this.element.getParameters();
            
            for (int i = 0; i < parameters.length; i ++) {
                try {
                    sb.append(getTypeClass(parameters[i].getType(), pa)); // NOI18N
                } catch (Exception e) {
                    ErrorManager.getDefault().notify(e);
                }
                if (i < (parameters.length - 1)) sb.append(", "); // NOI18N
            }
            
            sb.append("}))"); // NOI18N
            return sb.toString();
        }

        String getIconBase( boolean defaultIcon ) {
            if( defaultIcon )
                return BIF_METHOD + "S"; // NOI18N
            else
                return BIF_METHOD + (this.isIncluded() ? "S" : "N"); // NOI18N
        }

        void analyzeCustomizationString( String statement ) {
        }

        void analyzeCreationString( String statement ) {
        }
        
        /** Analyzes the bean info code for all customizations */
        void analyzeCustomization ( Collection code ) {
            if (me != null) {
                // find the method identifier
                String creation = (String) BiAnalyser.normalizeText(this.getCreationString()).toArray()[0];
                Iterator it = code.iterator();
                int index;

                while( it.hasNext() ) {
                    String statement = (String) it.next();
                    if ((index = statement.indexOf(creation)) > -1) {
                        this.varName = statement.substring(statement.indexOf("methods[METHOD_") + 15, index - 2);
                        break;
                    }
                }

                me = null;
            }
            
            String realName = this.getName();
            this.setName(varName);
            super.analyzeCustomization(code);
            this.setName(realName);
        }

    }

    public int compareTo(Object other) {
        if (!(other instanceof BiFeature))
            return -1;
        BiFeature bf = (BiFeature)other;
        return getName().compareToIgnoreCase(bf.getName());
    }
}
/*
 * Log
 *
 */
