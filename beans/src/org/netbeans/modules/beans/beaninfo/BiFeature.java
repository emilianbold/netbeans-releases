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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.openide.src.MethodElement;
import org.openide.src.ClassElement;
import org.openide.nodes.Node;

import org.netbeans.modules.beans.Pattern;
import org.netbeans.modules.beans.PropertyPattern;
import org.netbeans.modules.beans.IdxPropertyPattern;
import org.netbeans.modules.beans.EventSetPattern;

/** The basic class representing features included in BeanInfo.
* 
* @author Petr Hrebejk
*/
abstract class BiFeature extends Object implements IconBases, Node.Cookie {

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

        String stNew = new String( getName() + getBrackets() + "=new" ); // NOI18N
        String stExpert = new String( getName() + getBrackets() + "." + TEXT_EXPERT ); // NOI18N
        String stHidden = new String( getName() + getBrackets() + "." + TEXT_HIDDEN ); // NOI18N
        String stPreferred = new String( getName() + getBrackets() + "." + TEXT_PREFERRED ); // NOI18N
        String stDisplayName = new String( getName() + getBrackets() + "." + TEXT_DISPLAY_NAME ); // NOI18N
        String stShortDescription = new String( getName() + getBrackets() + "." + TEXT_SHORT_DESCRIPTION ); // NOI18N
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

    static class Descriptor extends BiFeature {
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
            if( defaultIcon )
                return null;
            else                
                return null;    // NOI18N
            //return BIF_DESCRIPTOR + "S"; // NOI18N
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
    
    static class Property extends BiFeature {

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

            String stBound = new String( this.getName() + "]." + TEXT_BOUND ); // NOI18N
            String stConstrained = new String( this.getName() + "]." + TEXT_CONSTRAINED ); // NOI18N
            String stPropertyEditor = new String( this.getName() + "]." + TEXT_PROPERTY_EDITOR ); // NOI18N

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
                if ( params[2].equals( "null" ) ) // NOI18N
                    mode = PropertyPattern.WRITE_ONLY;
                else if ( params[3].equals( "null" ) ) // NOI18N
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

    static class EventSet extends BiFeature {

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

        /** Returns the call to constructor of EventSetDescriptor */
        String getCreationString () {
            StringBuffer sb = new StringBuffer( 100 );
            
            org.openide.src.MethodElement listenerMethods[];

            try {
                org.openide.src.Type listenerType = pattern.getType();
                org.openide.src.ClassElement listener = org.openide.src.ClassElement.forName(listenerType.getClassName().getFullName());
                listenerMethods = listener.getMethods();
            } catch (IllegalStateException e) {
                org.openide.TopManager.getDefault().notifyException( e );
                listenerMethods = new org.openide.src.MethodElement[0];
            }

            sb.append( "new EventSetDescriptor ( " ); // NOI18N
            sb.append( pattern.getDeclaringClass().getName().getFullName() + ".class, " ); // NOI18N
            sb.append( "\"" + this.getName() + "\", " ); // NOI18N
            try {
                sb.append( pattern.getType().getClassName().getFullName() + ".class, " ); // NOI18N
            } catch (IllegalStateException e) {
                org.openide.TopManager.getDefault().notifyException( e );
                listenerMethods = new org.openide.src.MethodElement[0];
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

            String stUnicast = new String( this.getName() + "]." + TEXT_UNICAST ); // NOI18N
            String stInDefault = new String( this.getName() + "]." + TEXT_IN_DEFAULT ); // NOI18N
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

    static class Method extends BiFeature {
      MethodElement element;
      private String varName;
      private MethodElement me;
      
      Method( MethodElement me ) {
        super( me );
        element = me;
        this.me = me;
      }
      
        private static String getSignature(org.openide.src.ClassElement cls) {
            org.openide.src.Identifier n = cls.getName();
            if (!cls.isInner()) {
                return n.getFullName();
            }

            StringBuffer sb = new StringBuffer(n.getFullName());
            org.openide.src.ClassElement c = cls;
            int index = sb.length();

            while (true) {
                index -= n.getSourceName().length() + 1;
                sb.setCharAt(index, '$');
                c = c.getDeclaringClass();
                if (!c.isInner())
                    break;
                n = c.getName();
            } 
            return sb.toString();
        }
        
      private static String getVMClassName(org.openide.src.Type type) {
        try {
            if (!type.isArray()) {
                String fqn = type.getClassName().getFullName();
                org.openide.src.ClassElement cls = org.openide.src.ClassElement.forName(fqn);
                if (cls == null)
                    return fqn;
                return getSignature(cls);    	    
            }

            int depth = 0;
            org.openide.src.Type t = type;

            do {
                ++depth;
                t = t.getElementType();
            } while (t.isArray());		

            StringBuffer sb = new StringBuffer(depth + 1);
            for (int i = 0; i < depth; i++) {
                sb.append('[');
            }
            if (t.isPrimitive()) {
                sb.append(getPrimitiveCode(t));
            } else {
                sb.append('L');
                sb.append(getVMClassName(t));
                sb.append(';');
            }
            return sb.toString();
        } catch (Exception e) {
            org.openide.TopManager.getDefault().notifyException(e);
            return "";
        }
      }
      
        private static String getPrimitiveCode(org.openide.src.Type type) {
            if (type.equals(type.INT)) return "I";
            else if (type.equals(type.BOOLEAN)) return "Z";
            else if (type.equals(type.CHAR)) return "C";
            else if (type.equals(type.LONG)) return "J";
            else if (type.equals(type.SHORT)) return "S";
            else if (type.equals(type.BYTE)) return "B";
            else if (type.equals(type.FLOAT)) return "F";
            else if (type.equals(type.DOUBLE)) return "D";
            else return "V";
        }

        private static String getTypeClass(org.openide.src.Type type) {
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
                  org.openide.TopManager.getDefault().notifyException(e);
                  return type.toString() + ".class";
              }
          } else /*(type.isArray())*/ {
              return "Class.forName(\"" + getVMClassName(type) + "\")";
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
            sb.append( "Class.forName(\"" + this.element.getDeclaringClass().getName().getFullName() + "\").getMethod(\"" + this.element.getName().getFullName() + "\", "); // NOI18N
            sb.append( "new Class[] {"); // NOI18N
            
            org.openide.src.MethodParameter[] parameters = this.element.getParameters();
            
            for (int i = 0; i < parameters.length; i ++) {
                try {
                    sb.append(getTypeClass(parameters[i].getType())); // NOI18N
                } catch (Exception e) {
                    org.openide.TopManager.getDefault().notifyException( e );
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
}
/*
 * Log
 *
 */
