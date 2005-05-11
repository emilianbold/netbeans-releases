/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.beans.beaninfo;

import java.util.*;

import org.openide.nodes.Node;
import org.openide.ErrorManager;

import org.netbeans.modules.beans.*;
import org.netbeans.jmi.javamodel.*;

import javax.jmi.reflect.JmiException;

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

    private String brackets = "]"; // NOI18N
    /**
    * Creates empty BiFeature.
    */
    public BiFeature( Pattern pattern ) {
        this(pattern.getName());
    }

    public BiFeature(org.netbeans.jmi.javamodel.Method me) throws JmiException {
        this(me.getName());
        displayName = "\"\""; // NOI18N
    }

    protected BiFeature() {        
        this("beanDescriptor");//NOI18N GenerateBeanInfoAction.getString("CTL_NODE_DescriptorDisplayName");
    }
    
    private BiFeature(String name) {
        this.name = name;
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
    void analyzeCustomization ( Collection code ) throws GenerateBeanException {
        setIncluded( false );
        
        Iterator it = code.iterator();
        String n = getBracketedName();

        String stNew = n + "=new"; // NOI18N
        String stExpert = n + "." + TEXT_EXPERT; // NOI18N
        String stHidden = n + "." + TEXT_HIDDEN; // NOI18N
        String stPreferred = n + "." + TEXT_PREFERRED; // NOI18N
        String stDisplayName = n + "." + TEXT_DISPLAY_NAME; // NOI18N
        String stShortDescription = n + "." + TEXT_SHORT_DESCRIPTION; // NOI18N
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

    public static final class Descriptor extends BiFeature {
        JavaClass element;
        String customizer;
        private String beanName;

        Descriptor( JavaClass ce ) throws GenerateBeanException {
            this.element = ce;
            this.beanName = initBeanName(this.element);
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
//            StringBuffer sb = new StringBuffer( 100 );

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
        
        private static String initBeanName(JavaClass element) throws GenerateBeanException {
            try {
                return element.getName();
            } catch(JmiException e) {
                throw new GenerateBeanException(e);
            }
        }
        
        public String getBeanName() {
            return this.beanName;
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

        private String declaringClassName;
        private String getterName;
        private String setterName;

        Property( PropertyPattern pp ) throws GenerateBeanException {
            super( pp );
            mode = pp.getMode();
            pattern = pp;

            try {
                assert JMIUtils.isInsideTrans();
                declaringClassName = pattern.getDeclaringClass().getName();
                NamedElement ne = pattern.getGetterMethod(); 
                getterName = ne == null? null: ne.getName();
                ne = pattern.getSetterMethod(); 
                setterName = ne == null? null: ne.getName();
            } catch (JmiException e) {
                throw new GenerateBeanException(e);
            }
        }

        protected final String getDeclaringClassName() {
            return declaringClassName;
        }

        protected final String getGetterName() {
            return getterName;
        }

        protected final String getSetterName() {
            return setterName;
        }

        public boolean isBound() {
            return bound;
        }

        String getBracketedName() {
            return "[PROPERTY_" + getName() + "]"; // NOI18N
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
        
//        protected final String get

        /** Returns the call to constructor of PropertyDescriptor */
        String getCreationString () {
            StringBuffer sb = new StringBuffer( 100 );

            sb.append( "new PropertyDescriptor ( " ); // NOI18N
            sb.append( "\"" + this.getName() + "\", " ); // NOI18N
            sb.append( declaringClassName + ".class, " ); // NOI18N

            if ( getterName != null && getMode() != PropertyPattern.WRITE_ONLY )
                sb.append( "\"" + getterName + "\", " ); // NOI18N
            else
                sb.append( "null, "); // NOI18N

            if ( setterName != null && getMode() != PropertyPattern.READ_ONLY )
                sb.append( "\"" + setterName + "\" )" ); // NOI18N
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
            String stBound = n + "." + TEXT_BOUND; // NOI18N
            String stConstrained = n + "." + TEXT_CONSTRAINED; // NOI18N
            String stPropertyEditor = n + "." + TEXT_PROPERTY_EDITOR; // NOI18N
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

    public static final class IdxProperty extends Property {

        private boolean niGetter;
        private boolean niSetter;

        IdxPropertyPattern pattern;
        private String indexedGetterName;
        private String indexedSetterName;

        IdxProperty( IdxPropertyPattern pp ) throws GenerateBeanException {
            super( pp );
            pattern = pp;

            niGetter = hasNiGetter();
            niSetter = hasNiSetter();
            try {
                assert JMIUtils.isInsideTrans();
                NamedElement ne = pattern.getIndexedGetterMethod(); 
                indexedGetterName = ne == null? null: ne.getName();
                ne = pattern.getIndexedSetterMethod(); 
                indexedSetterName = ne == null? null: ne.getName();
            } catch (JmiException e) {
                throw new GenerateBeanException(e);
            }
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
        String getCreationString () {
            StringBuffer sb = new StringBuffer( 100 );

            sb.append( "new IndexedPropertyDescriptor ( " ); // NOI18N
            sb.append( "\"" + this.getName() + "\", " ); // NOI18N
            sb.append( getDeclaringClassName() + ".class, " ); // NOI18N

            if ( getGetterName() != null && niGetter )
                sb.append( "\"" + getGetterName() + "\", " ); // NOI18N
            else
                sb.append( "null, "); // NOI18N

            if ( getSetterName() != null && niSetter )
                sb.append( "\"" + getSetterName() + "\", " ); // NOI18N
            else
                sb.append( "null, "); // NOI18N

            if ( indexedGetterName != null && getMode() != PropertyPattern.WRITE_ONLY )
                sb.append( "\"" + indexedGetterName + "\", " ); // NOI18N
            else
                sb.append( "null, "); // NOI18N

            if ( indexedSetterName != null && getMode() != PropertyPattern.READ_ONLY )
                sb.append( "\"" + indexedSetterName + "\" )" ); // NOI18N
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

    public static final class EventSet extends BiFeature implements Comparator {

        EventSetPattern pattern;

        private static final String TEXT_UNICAST = "setUnicast"; // NOI18N
        private static final String TEXT_IN_DEFAULT = "setInDefaultEventSet"; // NOI18N

        private boolean isInDefaultEventSet = true;
        private String creationString;

        EventSet( EventSetPattern esp ) throws GenerateBeanException {
            super( esp );
            pattern = esp;
            creationString = initCreationString();
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
            return "[EVENT_" + getName() + "]"; // NOI18N
        }

        public int compare(Object o1, Object o2) {
            if (!(o1 instanceof org.netbeans.jmi.javamodel.Method) ||
                    !(o2 instanceof org.netbeans.jmi.javamodel.Method))
                throw new IllegalArgumentException();
            org.netbeans.jmi.javamodel.Method m1 = (org.netbeans.jmi.javamodel.Method) o1;
            org.netbeans.jmi.javamodel.Method m2 = (org.netbeans.jmi.javamodel.Method) o2;

            return m1.getName().compareTo(m2.getName());
        }

        /** Returns the call to constructor of EventSetDescriptor */
        String getCreationString () {
            return creationString;
        }
        
        private String initCreationString () throws GenerateBeanException {
            assert JMIUtils.isInsideTrans();
            try {
                StringBuffer sb = new StringBuffer( 100 );
            
                List/*<Method>*/ listenerMethods;

                try {
                    Type listenerType = pattern.getType();
                    JavaClass listener = (JavaClass) listenerType;
                    listenerMethods = new ArrayList(JMIUtils.getMethods(listener));
                    Collections.sort(listenerMethods, this);
                } catch (IllegalStateException e) {
                    ErrorManager.getDefault().notify(e);
                    listenerMethods = Collections.EMPTY_LIST;
                }

                sb.append( "new EventSetDescriptor ( " ); // NOI18N
                sb.append( pattern.getDeclaringClass().getName() + ".class, " ); // NOI18N
                sb.append( "\"" + this.getName() + "\", " ); // NOI18N
                sb.append( pattern.getType().getName() + ".class, " ); // NOI18N
                sb.append( "new String[] {" ); // NOI18N
                int i = 0;
                for (Iterator it = listenerMethods.iterator(); it.hasNext();) {
                    org.netbeans.jmi.javamodel.Method method = (org.netbeans.jmi.javamodel.Method) it.next();
                    if (i++ > 0) {
                        sb.append(", "); // NOI18N
                    }
                    sb.append( "\"" + method.getName() + "\"" ); // NOI18N
                }
                sb.append( "}, "); // NOI18N
                sb.append( "\"" + pattern.getAddListenerMethod().getName() + "\", " ); // NOI18N
                sb.append( "\"" + pattern.getRemoveListenerMethod().getName() + "\" )" ); // NOI18N

                return sb.toString();
            } catch (JmiException e) {
                throw new GenerateBeanException(e);
            }
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
//            String stUnicast = new String( n + "."  + TEXT_UNICAST ); // NOI18N
            String stInDefault = n + "." + TEXT_IN_DEFAULT; // NOI18N
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

    public static final class Method extends BiFeature {
        org.netbeans.jmi.javamodel.Method element;
        private String varName;
        private String toolTip;
        private org.netbeans.jmi.javamodel.Method me;
        private static Map PRIMITIVE_2_CLASS;
        private String creationString;

        Method( org.netbeans.jmi.javamodel.Method me, PatternAnalyser pa ) throws GenerateBeanException {
            super( me );
            element = me;
            this.me = me;
            toolTip = initToolTip(this.element);
            creationString = initCreationString(this.element);
        }
        
        String getBracketedName() {
            return "[METHOD_" + getName() + "]"; // NOI18N
        }
        
        private static String findPrimitiveClass(String primitiveType) {
            if (PRIMITIVE_2_CLASS == null) {
                Map m = new HashMap();
                m.put("int", "Integer.TYPE"); // NOI18N
                m.put("boolean", "Boolean.TYPE"); // NOI18N
                m.put("char", "Character.TYPE"); // NOI18N
                m.put("long", "Long.TYPE"); // NOI18N
                m.put("short", "Short.TYPE"); // NOI18N
                m.put("byte", "Byte.TYPE"); // NOI18N
                m.put("float", "Float.TYPE"); // NOI18N
                m.put("double", "Double.TYPE"); // NOI18N
                PRIMITIVE_2_CLASS = Collections.unmodifiableMap(m);
            }
            return (String) PRIMITIVE_2_CLASS.get(primitiveType);
        }
        
        private static String getTypeClass(Type type) throws JmiException {
            assert JMIUtils.isInsideTrans();
            if (type instanceof PrimitiveType) {
                return findPrimitiveClass(type.getName());
            } else if (type instanceof Array) { // Generic
                return resolveArrayClass((Array) type);
            } else if (type instanceof ParameterizedType) { // Generic
                return ((ParameterizedType) type).getDefinition().getName() + ".class"; // NOI18N
            } else if (type instanceof ClassDefinition) { // Class
                return ((ClassDefinition) type).getName() + ".class"; // NOI18N
            } else {
                throw new IllegalStateException("Unknown type" + type); // NOI18N
            }
        }

        private static String resolveArrayClass(Array array) {
            Type type = array;
            int i = 0;
            for (;type instanceof Array; i++) {
                type = ((Array) type).getType();
            }
            if (type instanceof ParameterizedType) {
                char[] brackets = new char[i * 2];
                for (int j = 0; j < brackets.length; j++) {
                    brackets[j] = '[';
                    brackets[++j] = ']';
                }
                return ((ParameterizedType) type).getDefinition().getName() + String.valueOf(brackets) + ".class"; // NOI18N
            } else {
                return array.getName() + ".class"; // NOI18N
            }
        }

        public String getToolTip() {
            return this.toolTip;
        }
        
        private static String initToolTip(org.netbeans.jmi.javamodel.Method element) throws GenerateBeanException {
            assert JMIUtils.isInsideTrans();
            try {
                StringBuffer sb = new StringBuffer( 100 );
                sb.append( element.getName() + "("); // NOI18N
            
                List/*<Parameter>*/ parameters = element.getParameters();
            
                int i = 0;
                for (Iterator iterator = parameters.iterator(); iterator.hasNext();) {
                    Parameter param = (Parameter) iterator.next();
                    if (i++ > 0)
                        sb.append(", "); // NOI18N
                    try {
                        sb.append(param.getType().getName());
                    } catch (NullPointerException e) {
                        ErrorManager.getDefault().annotate(e, "method: " + element); // NOI18N
                        ErrorManager.getDefault().annotate(e, "i: " + i); // NOI18N
                        ErrorManager.getDefault().annotate(e, "param: " + param); // NOI18N
                        if (param != null)
                            ErrorManager.getDefault().annotate(e, "type: " + param.getType()); // NOI18N
                        throw e;
                    }
                }
            
                sb.append(")"); // NOI18N
                return sb.toString();
            } catch (JmiException e) {
                throw new GenerateBeanException(e);
            }
        }
        
        org.netbeans.jmi.javamodel.Method getElement() {
            return element;
        }
        
        // Returns the call to constructor of MethodDescriptor 
        String getCreationString () {
            return creationString;
        }
        
        private static String initCreationString (org.netbeans.jmi.javamodel.Method element) throws GenerateBeanException {
            assert JMIUtils.isInsideTrans();
            try {
                StringBuffer sb = new StringBuffer( 100 );
                sb.append( "new MethodDescriptor ( " ); // NOI18N
                //sb.append( "Class.forName(\"" + this.element.getDeclaringClass().getName().getFullName() + "\").getMethod(\"" + this.element.getName().getFullName() + "\", "); // NOI18N
                sb.append( element.getDeclaringClass().getName() + ".class.getMethod(\"" + element.getName() + "\", "); // NOI18N
                sb.append( "new Class[] {"); // NOI18N
            
                List/*<Parameter>*/ parameters = element.getParameters();
            
                int i = 0;
                for (Iterator it = parameters.iterator(); it.hasNext();) {
                    Parameter param = (Parameter) it.next();
                    if (i++ > 0)
                        sb.append(", "); // NOI18N
                    sb.append(getTypeClass(param.getType())); // NOI18N
                }
            
                sb.append("}))"); // NOI18N
                return sb.toString();
            } catch (JmiException e) {
                throw new GenerateBeanException(e);
            }
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
        void analyzeCustomization ( Collection code ) throws GenerateBeanException {
            assert JMIUtils.isInsideTrans();
            if (me != null) {
                // find the method identifier
                String creation = (String) BiAnalyser.normalizeText(this.getCreationString()).toArray()[0];
                Iterator it = code.iterator();
                int index;
                
                while( it.hasNext() ) {
                    String statement = (String) it.next();
                    if ((index = statement.indexOf(creation)) > -1) {
                        this.varName = statement.substring(statement.indexOf("methods[METHOD_") + 15, index - 2); // NOI18N
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
