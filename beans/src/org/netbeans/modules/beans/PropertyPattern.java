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

package org.netbeans.modules.beans;

import java.io.*;
import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.openide.src.ClassElement;
import org.openide.src.FieldElement;
import org.openide.src.MethodElement;
import org.openide.src.MemberElement;
import org.openide.src.MethodParameter;
import org.openide.src.Type;
import org.openide.src.SourceException;
import org.openide.src.Identifier;
import org.openide.src.ElementFormat;
import org.openide.nodes.Node;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;

//import org.netbeans.modules.java.support.AutoCommenter;

/** Class representing a JavaBeans Property
 * @author Petr Hrebejk
 */
public class PropertyPattern extends Pattern {

    /** Constant for READ/WRITE mode of properties */
    public static final int READ_WRITE = 1;
    /** Constant for READ ONLY mode of properties */
    public static final int READ_ONLY = 2;
    /** Constant for WRITE ONLY mode of properties */
    public static final int WRITE_ONLY = 4;
    
    /** Constant for PropertyChange */
    static final String PROPERTY_CHANGE = "firePropertyChange";
    /** Constant for VetoableChange */
    static final String VETOABLE_CHANGE = "fireVetoableChange";
    
    /** Getter method of this property */
    protected MethodElement getterMethod = null;
    /** Setter method of this property */
    protected MethodElement setterMethod = null;
    /** Field which probably belongs to this property */
    protected FieldElement  estimatedField = null;

    /** Holds the type of the property resolved from methods. */
    protected Type type;
    /** Holds the decapitalized name. */
    protected String name;


    /** Creates new PropertyPattern one of the methods may be null.
     * @param patternAnalyser patternAnalyser which creates this Property.
     * @param getterMethod getterMethod of the property or <CODE>null</CODE>.
     * @param setterMethod setterMethod of the property or <CODE>null</CODE>.
     * @throws IntrospectionException If specified methods do not follow beans Property rules.
     */
    public PropertyPattern( PatternAnalyser patternAnalyser,
                            MethodElement getterMethod, MethodElement setterMethod )
    throws IntrospectionException {

        super( patternAnalyser );

        this.getterMethod = getterMethod;
        this.setterMethod = setterMethod;

        type = findPropertyType();
        name = findPropertyName();
    }

    /** Creates new PropertyPattern.
     * @param patternAnalyser patternAnalyser which creates this Property.
     */
    PropertyPattern( PatternAnalyser patternAnalyser ) {
        super( patternAnalyser );
    }

    /** Creates new PropertyPattern.
     * @param patternAnalyser patternAnalyser which creates this Property.
     * @param name Name of the Property.
     * @param type Type of the Property.
     * @throws SourceException If the Property can't be created in the source.
     * @return Newly created PropertyPattern.
     */
    static PropertyPattern create( PatternAnalyser patternAnalyser,
                                   String name, String type ) throws SourceException {

        PropertyPattern pp = new PropertyPattern( patternAnalyser );

        pp.name = name;
        pp.type = Type.parse( type );

        pp.generateGetterMethod();
        pp.generateSetterMethod();

        return pp;
    }

    /** Creates new property pattern with extended options
     * @param patternAnalyser patternAnalyser which creates this Property.
     * @param name Name of the Property.
     * @param type Type of the Property.
     * @param mode {@link #READ_WRITE Mode} of the new property.
     * @param bound Is the Property bound?
     * @param constrained Is the property constrained?
     * @param withField Should be the private field for this property genareted?
     * @param withReturn Generate return statement in getter?
     * @param withSet Generate seter statement for private field in setter.
     * @param withSupport Generate PropertyChange support?
     * @throws SourceException If the Property can't be created in the source.
     * @return Newly created PropertyPattern.
     */
    static PropertyPattern create( PatternAnalyser patternAnalyser,
                                   String name, String type,
                                   int mode, boolean bound, boolean constrained,
                                   boolean withField, boolean withReturn,
                                   boolean withSet, boolean withSupport ) throws SourceException {

        return create(patternAnalyser, name, type, mode, bound, constrained, withField, withReturn, withSet, withSupport, false, false);
    }
    
    /** Creates new property pattern with extended options
     * @param patternAnalyser patternAnalyser which creates this Property.
     * @param name Name of the Property.
     * @param type Type of the Property.
     * @param mode {@link #READ_WRITE Mode} of the new property.
     * @param bound Is the Property bound?
     * @param constrained Is the property constrained?
     * @param withField Should be the private field for this property genareted?
     * @param withReturn Generate return statement in getter?
     * @param withSet Generate seter statement for private field in setter.
     * @param withSupport Generate PropertyChange support?
     * @param useSupport use change support without prompting
     * @param fromField signalize that all action are activatet on field
     * @throws SourceException If the Property can't be created in the source.
     * @return Newly created PropertyPattern.
     */
    static PropertyPattern create( PatternAnalyser patternAnalyser,
                                   String name, String type,
                                   int mode, boolean bound, boolean constrained,
                                   boolean withField, boolean withReturn,
                                   boolean withSet, boolean withSupport, 
                                   boolean useSupport, boolean fromField ) throws SourceException {

        PropertyPattern pp = new PropertyPattern( patternAnalyser );

        pp.name = name;
        pp.type = Type.parse( type );

        // Generate field
        if ( ( withField || withSupport ) && !fromField ) {
            try {
                pp.generateField( true );
            } catch (SourceException e) {
                TopManager.getDefault().notify(
                    new NotifyDescriptor.Message(
                        PatternNode.getString("MSG_Cannot_Create_Field"),
                        NotifyDescriptor.WARNING_MESSAGE));
            }
        }

        // Ensure property change support field and methods exist
        String supportName = null;
        String vetoSupportName = null;

        if ( withSupport ) {
            // i try to generate support - try to look up if not in the parent defined
            
            boolean boundSupport = bound;
            boolean constrainedSupport = constrained;
            
            if( !useSupport ){
                if( boundSupport )
                    if( ( supportName = EventSetInheritanceAnalyser.showInheritanceEventDialog(EventSetInheritanceAnalyser.detectPropertyChangeSupport(  pp.getDeclaringClass()), "PropertyChangeSupport")) != null )
                        boundSupport = false;
                if( constrainedSupport )
                    if( ( vetoSupportName = EventSetInheritanceAnalyser.showInheritanceEventDialog(EventSetInheritanceAnalyser.detectVetoableChangeSupport(  pp.getDeclaringClass()), "VetoableChangeSupport")) != null )
                        constrainedSupport = false;
            }
            else{
                if( boundSupport )
                    if( ( supportName = EventSetInheritanceAnalyser.getInheritanceEventSupportName(EventSetInheritanceAnalyser.detectPropertyChangeSupport(  pp.getDeclaringClass()), "PropertyChangeSupport")) != null )
                        boundSupport = false;
                if( constrainedSupport )
                    if( ( vetoSupportName = EventSetInheritanceAnalyser.getInheritanceEventSupportName(EventSetInheritanceAnalyser.detectVetoableChangeSupport(  pp.getDeclaringClass()), "VetoableChangeSupport")) != null )
                        constrainedSupport = false;                
            }

            if ( boundSupport )
                supportName = BeanPatternGenerator.supportField( pp.getDeclaringClass() );
            if ( constrainedSupport )
                vetoSupportName = BeanPatternGenerator.vetoSupportField( pp.getDeclaringClass() );

            if ( boundSupport )
                BeanPatternGenerator.supportListenerMethods( pp.getDeclaringClass(), supportName );
            if ( constrainedSupport )
                BeanPatternGenerator.vetoSupportListenerMethods( pp.getDeclaringClass(), vetoSupportName );
        }

        if ( mode == READ_WRITE || mode == READ_ONLY )
            pp.generateGetterMethod( BeanPatternGenerator.propertyGetterBody( name, withReturn ), true );

        if ( mode == READ_WRITE || mode == WRITE_ONLY )
            pp.generateSetterMethod( BeanPatternGenerator.propertySetterBody( name, pp.getType(),
                                     bound, constrained, withSet, withSupport, supportName, vetoSupportName ), constrained, true );

        return pp;
    }

    /** Gets the name of PropertyPattern
     * @return Name of the Property
     */
    public String getName() {
        return name;
    }

    /** Sets the name of PropertyPattern
     * @param name New name of the property.
     * @throws SourceException If the modification of source code is impossible.
     */
    public void setName( String name ) throws SourceException {

        if ( !Utilities.isJavaIdentifier( name )  )
            throw new SourceException( "Invalid event source name" ); // NOI18N

        name = capitalizeFirstLetter( name );

        if ( getterMethod != null ) {
            Identifier getterMethodID = Identifier.create(( getterMethod.getName().getName().startsWith("get") ? // NOI18N
                                        "get" : "is" ) + name ); // NOI18N
            getterMethod.setName( getterMethodID );
        }
        if ( setterMethod != null ) {
            Identifier setterMethodID = Identifier.create( "set" + name ); // NOI18N

            setterMethod.setName( setterMethodID );
        }

        this.name = Introspector.decapitalize( name );

        // Ask if to set the estimated field
        if ( estimatedField != null ) {
            ElementFormat fmt = new ElementFormat ("{m} {t} {n}"); // NOI18N
            String mssg = MessageFormat.format( PatternNode.getString( "FMT_ChangeFieldName" ),
                                                new Object[] { fmt.format (estimatedField) } );
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation ( mssg, NotifyDescriptor.YES_NO_OPTION );
            if ( TopManager.getDefault().notify( nd ).equals( NotifyDescriptor.YES_OPTION ) ) {
                estimatedField.setName( Identifier.create( Introspector.decapitalize( name ) ) );
            }
        }

    }

    /** Returns the mode of the property {@link #READ_WRITE READ_WRITE}, {@link #READ_ONLY READ_ONLY}
     *  or {@link #WRITE_ONLY WRITE_ONLY}
     * @return Mode of the property
     */
    public int getMode() {

        if ( setterMethod != null && getterMethod != null )
            return READ_WRITE;
        else if ( getterMethod != null && setterMethod == null )
            return READ_ONLY;
        else if ( setterMethod != null && getterMethod == null )
            return WRITE_ONLY;
        else
            return 0;
    }

    /** Sets the property to be writable
     * @param mode New Mode {@link #READ_WRITE READ_WRITE}, {@link #READ_ONLY READ_ONLY}
     *  or {@link #WRITE_ONLY WRITE_ONLY}
     * @throws SourceException If the modification of source code is impossible.
     */
    public void setMode( int mode ) throws SourceException {

        if ( getMode() == mode )
            return;

        switch ( mode ) {
        case READ_WRITE:
            if ( getterMethod == null )
                generateGetterMethod();
            if ( setterMethod == null )
                generateSetterMethod();
            break;
        case READ_ONLY:
            if ( getterMethod == null )
                generateGetterMethod();
            if ( setterMethod != null ) {
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation ( PatternNode.getString("MSG_Delete_Setter") + PatternNode.getString("MSG_Continue_Confirm"), NotifyDescriptor.YES_NO_OPTION );
                TopManager.getDefault().notify( nd );
                if( nd.getValue().equals( NotifyDescriptor.YES_OPTION ) ) {
                    deleteSetterMethod();
                }
            }
            break;
        case WRITE_ONLY:
            if ( setterMethod == null )
                generateSetterMethod();
            if ( getterMethod != null ) {
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation ( PatternNode.getString("MSG_Delete_Getter") + PatternNode.getString("MSG_Continue_Confirm"), NotifyDescriptor.YES_NO_OPTION );
                TopManager.getDefault().notify( nd );
                if( nd.getValue().equals( NotifyDescriptor.YES_OPTION ) ) {
                    deleteGetterMethod();
                }
            }
            break;
        }
    }

    /** Returns the getter method
     * @return Getter method of the property
     */
    public MethodElement getGetterMethod() {
        return getterMethod;
    }

    /** Returns the setter method
     * @return Setter method of the property
     */
    public MethodElement getSetterMethod() {
        return setterMethod;
    }

    /** Gets the type of property
     * @return Type of the property
     */
    public Type getType() {
        return type;
    }

    /** Sets the type of propertyPattern
     * @param type New type of the property
     * @throws SourceException If the modification of source code is impossible
     */
    public void setType(Type type) throws SourceException {
        if ( this.type.compareTo( type, true ) )
            return;

        if (getterMethod != null ) {
            if ( this.type.compareTo( Type.BOOLEAN, false ) ) {
                getterMethod.setName( Identifier.create( "get" + capitalizeFirstLetter( getName() ) ) ); // NOI18N
            }
            else if ( type.compareTo( Type.BOOLEAN, false ) ) {
                String mssg = MessageFormat.format( PatternNode.getString( "FMT_ChangeToIs" ),
                                                    new Object[] { capitalizeFirstLetter( getName() ) } );
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation ( mssg, NotifyDescriptor.YES_NO_OPTION );
                TopManager.getDefault().notify( nd );
                if( nd.getValue().equals( NotifyDescriptor.YES_OPTION ) ) {
                    getterMethod.setName( Identifier.create( "is" + capitalizeFirstLetter( getName() ) ) ); // NOI18N
                }
            }
            getterMethod.setReturn( type );
        }

        if (setterMethod != null ) {
            MethodParameter[] params = setterMethod.getParameters();
            if ( params.length > 0 ) {
                Type oldType = params[0].getType();
                params[0].setType( type );
                setterMethod.setParameters( params );
                
                String body = setterMethod.getBody();
                System.out.println("PropertyPattern " + setterMethod);
                //test if body contains change support
                if( body != null && ( body.indexOf(PROPERTY_CHANGE) != -1 || body.indexOf(VETOABLE_CHANGE) != -1 ) ) {
                    String mssg = MessageFormat.format( PatternNode.getString( "FMT_ChangeMethodBody" ),
                                                        new Object[] { setterMethod.getName().getName() } );
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation ( mssg, NotifyDescriptor.YES_NO_OPTION );
                    TopManager.getDefault().notify( nd );
                    if( nd.getValue().equals( NotifyDescriptor.YES_OPTION ) ) {
                        String newBody = regeneratePropertySupport( setterMethod.getBody(), null, params[0].getName(), type, oldType );
                        if( newBody != null )
                            setterMethod.setBody(newBody);

                        newBody = regeneratePropertySupport( setterMethod.getBody(), PROPERTY_CHANGE, params[0].getName(), type, oldType );
                        if( newBody != null )
                            setterMethod.setBody(newBody);

                        newBody = regeneratePropertySupport( setterMethod.getBody(), VETOABLE_CHANGE, params[0].getName(), type, oldType );
                        if( newBody != null )
                            setterMethod.setBody(newBody);
                    }
                }
            }
        }

        this.type = type;

        // Ask if to change estimated field Type

        if ( estimatedField != null ) {
            ElementFormat fmt = new ElementFormat ("{m} {t} {n}"); // NOI18N
            String mssg = MessageFormat.format( PatternNode.getString( "FMT_ChangeFieldType" ),
                                                new Object[] { fmt.format (estimatedField) } );
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation ( mssg, NotifyDescriptor.YES_NO_OPTION );
            if ( TopManager.getDefault().notify( nd ).equals( NotifyDescriptor.YES_OPTION ) ) {
                estimatedField.setType(type);
            }
            else {
                estimatedField = null;
            }
        }

    }

    /**
     * @param methodBody old method body
     * @param changeType  .. propertyChange, vetoableChange or null if need to change only support field 
     * @param name of property
     * @param type new type of property value
     * @param oldType old type of property value
     * @return null if no change is possible or new body if it is
     */
    protected String regeneratePropertySupport( String methodBody, String changeType, String name, org.openide.src.Type type, org.openide.src.Type oldType ){
        if( methodBody == null )
            return null;
        
        int first = -1;
        boolean post_index = false;
        boolean pre_index  = false;
        String propertyStyle = PropertyActionSettings.getDefault().getPropStyle();
        
        if( oldType.isArray() )
            oldType = getPrimitiveType(oldType);
        //will search for line containing property support or field
        if( changeType != null ){
            if( (first = methodBody.indexOf(changeType)) == -1 )
                return null; 
        }
        else{
            String oldVarLine = " old" + Pattern.capitalizeFirstLetter( name ) + " = " + propertyStyle + name;
            if( (first = methodBody.indexOf( (oldType.toString() + oldVarLine  + ";") )) == -1 ) {   //non indexed
                if( (first = methodBody.indexOf( (oldType.toString() + oldVarLine  + "[index];") )) == -1 ) {  //indexed
                    if( (first = methodBody.indexOf( (oldType.toString() + "[]" + oldVarLine  + ";") )) == -1 ) {  //indexed
                        return null;
                    }
                    else 
                        pre_index = true;
                }
                else 
                    post_index = true;
            }
        }

        if( first == -1 )
            return null;
        
        //find end of statement
        int last = methodBody.indexOf(';', first);
        if( first >= last )
            return null;
        
        StringBuffer newBody = new StringBuffer(100);
        if( changeType != null ){
            newBody.append( changeType + " (\"").append( name ).append( "\", " ); // NOI18N

            if ( type.isPrimitive() ) {            
                newBody.append( "new ").append( BeanPatternGenerator.getWrapperClassName( type )).append( " (" ); // NOI18N
                newBody.append( "old" ).append( Pattern.capitalizeFirstLetter( name ) ); // NOI18N
                newBody.append( "), " ); // NOI18N
                newBody.append( "new ").append( BeanPatternGenerator.getWrapperClassName( type )).append( " (" ); // NOI18N
                newBody.append( name ).append( "))" ); // NOI18N
            }
            else {
                newBody.append( "old" ).append( Pattern.capitalizeFirstLetter( name ) ); // NOI18N
                newBody.append( ", " ).append( name ).append( ")" ); // NOI18N
            }
        }
        else{
            newBody.append( type.toString() );
            //if( pre_index ){
            //    newBody.append( "[]" );
            //}
            newBody.append( " old" ).append( Pattern.capitalizeFirstLetter( name ) ); // NOI18N
            newBody.append( " = " ).append( propertyStyle ).append( name ); // NOI18N            
            if( post_index ){
                newBody.append( "[index]" );
            }
        }

        StringBuffer sb = new StringBuffer(methodBody);
        sb.delete(first, last);
        sb.insert(first, newBody);
        return sb.toString();        
    }
    
    private static org.openide.src.Type getPrimitiveType(org.openide.src.Type type){        
        if( type.isArray() ){
            return getPrimitiveType( type.getElementType() );
        }
        else{
            return type;
        }        
    }

    /** Gets the cookie of the first available method
     * @param cookieType Class of the Cookie
     * @return Cookie of Getter or Setter MethodElement
     */
    public Node.Cookie getCookie( Class cookieType ) {
        if ( getterMethod != null )
            return getterMethod.getCookie( cookieType );

        if ( setterMethod != null )
            return setterMethod.getCookie( cookieType );

        return null;
    }

    /** Gets the estimated field
     * @return Field which (probably) belongs to the property.
     */
    public FieldElement getEstimatedField( ) {
        return estimatedField;
    }

    /** Sets the estimated field
     * @param field Field for the property
     */
    void setEstimatedField( FieldElement field ) {
        estimatedField = field;
    }

    /** Destroys methods associated methods with the pattern in source
     * @throws SourceException If modification of source is impossible
     */
    public void destroy() throws SourceException {
        if ( estimatedField != null ) {
            ElementFormat fmt = new ElementFormat ("{m} {t} {n}"); // NOI18N
            String mssg = MessageFormat.format( PatternNode.getString( "FMT_DeleteField" ),
                                                new Object[] { fmt.format (estimatedField) } );
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation ( mssg, NotifyDescriptor.YES_NO_OPTION );
            if ( TopManager.getDefault().notify( nd ).equals( NotifyDescriptor.YES_OPTION ) ) {
                deleteEstimatedField();
            }
        }

        deleteGetterMethod();
        deleteSetterMethod();
    }

    // UTILITY METHODS ----------------------------------------------------------

    /** Package private constructor. Merges two property descriptors. Where they
     * conflict, gives the second argument (y) priority over the first argumnet (x).
     * @param x The first (lower priority) PropertyPattern.
     * @param y The second (higher priority) PropertyPattern.
     */
    PropertyPattern( PropertyPattern x, PropertyPattern y ) {
        super( y.patternAnalyser );

        // Figure out the merged getterMethod
        MethodElement xr = x.getterMethod;
        MethodElement yr = y.getterMethod;
        getterMethod = xr;

        // Normaly give priority to y's getterMethod
        if ( yr != null ) {
            getterMethod = yr;
        }
        // However, if both x and y reference read method in the same class,
        // give priority to a boolean "is" method over boolean "get" method. // NOI18N
        if ( xr != null && yr != null &&
                xr.getDeclaringClass() == yr.getDeclaringClass() &&
                xr.getReturn().compareTo( Type.BOOLEAN, false ) &&
                yr.getReturn().compareTo( Type.BOOLEAN, false ) &&
                xr.getName().getName().indexOf("is") == 0 && // NOI18N
                yr.getName().getName().indexOf("get") == 0 ) { // NOI18N
            getterMethod = xr;
        }

        setterMethod = x.getSetterMethod();
        if ( y.getSetterMethod() != null ) {
            setterMethod = y.getSetterMethod();
        }

        // PENDING bound and constrained
        /*
        bound = x.bound | y.bound;
        constrained = x.constrained | y.constrained
        */

        try {
            type = findPropertyType();
        }
        catch ( IntrospectionException ex ) {
            //System.out.println (x.getName() + ":" +  y.getName()); // NOI18N
            //System.out.println (x.getType() + ":" + y.getType() ); // NOI18N
            throw new InternalError( "Mixing invalid PropertyPattrens" + ex ); // NOI18N
        }

        name = findPropertyName();
    }

    /** Resolves the type of the property from type of getter and setter.
     * @throws IntrospectionException if the property doesnt folow the design patterns
     * @return The type of the property.
     */
    Type findPropertyType() throws IntrospectionException {
        Type resolvedType = null;

        if ( getterMethod != null ) {
            if ( getterMethod.getParameters().length != 0 ) {
                throw new IntrospectionException( "bad read method arg count" ); // NOI18N
            }
            resolvedType = getterMethod.getReturn();
            if ( resolvedType.compareTo( Type.VOID, false ) ) {
                throw new IntrospectionException( "read method " + getterMethod.getName().getName() + // NOI18N
                                                  " returns void" ); // NOI18N
            }
        }
        if ( setterMethod != null ) {
            MethodParameter params[] = setterMethod.getParameters();
            if ( params.length != 1 ) {
                throw new IntrospectionException( "bad write method arg count" ); // NOI18N
            }
            if ( resolvedType != null && !resolvedType.compareTo( params[0].getType(), false ) ) {
                throw new IntrospectionException( "type mismatch between read and write methods" ); // NOI18N
            }
            resolvedType = params[0].getType();
        }
        return resolvedType;
    }

    /** Based on names of getter and setter resolves the name of the property.
     * @return Name of the property
     */
    String findPropertyName() {

        String methodName = null;

        if ( getterMethod != null )
            methodName = getterMethod.getName().getName() ;
        else if ( setterMethod != null )
            methodName = setterMethod.getName().getName() ;
        else {
            return null;
        }

        return  methodName.startsWith( "is" ) ? // NOI18N
                Introspector.decapitalize( methodName.substring(2) ) :
                Introspector.decapitalize( methodName.substring(3) );
    }

    // METHODS FOR GENERATING AND DELETING METHODS AND FIELDS--------------------

    /** Generates getter method without body and without Javadoc comment.
     * @throws SourceException If modification of source code is impossible.
     */
    void generateGetterMethod() throws SourceException {
        generateGetterMethod( null, false );
    }

    /** Generates getter method with body and optionaly with Javadoc comment.
     * @param body Body of the method
     * @param javadoc Generate Javadoc comment?
     * @throws SourceException If modification of source code is impossible.
     */
    void generateGetterMethod( String body, boolean javadoc ) throws SourceException {

        ClassElement declaringClass = getDeclaringClass();
        MethodElement newGetter = new MethodElement();

        newGetter.setName( Identifier.create( (type == Type.BOOLEAN ? "is" : "get") + capitalizeFirstLetter( getName() ) ) ); // NOI18N
        newGetter.setReturn( type );
        newGetter.setModifiers( Modifier.PUBLIC );

        if ( declaringClass.isInterface() ) {
            newGetter.setBody( null );
        }
        else if ( body != null ) {
            newGetter.setBody( body );
        }

        if ( javadoc ) {
            String comment = MessageFormat.format( PatternNode.getString( "COMMENT_PropertyGetter" ),
                                                   new Object[] { getName() } );
            newGetter.getJavaDoc().setRawText( comment );
        }

        if ( declaringClass == null ) {
            //System.out.println ("nodecl - gen getter"); // NOI18N
            throw new SourceException();
        }
        else {
            declaringClass.addMethod( newGetter );
            getterMethod = declaringClass.getMethod( newGetter.getName(), getParameterTypes( newGetter ) );
        }

    }

    /** Generates setter method without body and without Javadoc comment.
     * @throws SourceException If modification of source code is impossible.
     */
    void generateSetterMethod() throws SourceException {
        generateSetterMethod( null, false, false );
    }

    /** Generates setter method with body and optionaly with Javadoc comment.
     * @param body Body of the method
     * @param javadoc Generate Javadoc comment?
     * @param constrained Is the property constrained?
     * @throws SourceException If modification of source code is impossible.
     */
    void generateSetterMethod( String body, boolean constrained, boolean javadoc ) throws SourceException {
        ClassElement declaringClass = getDeclaringClass();
        MethodElement newSetter = new MethodElement();

        newSetter.setName( Identifier.create( "set" + capitalizeFirstLetter( getName() ) ) ); // NOI18N
        newSetter.setReturn( Type.VOID );
        newSetter.setModifiers( Modifier.PUBLIC );
        newSetter.setParameters( ( new MethodParameter[] { new MethodParameter( name, type, false ) } ));
        if ( constrained )
            newSetter.setExceptions( ( new Identifier[] { Identifier.create( "java.beans.PropertyVetoException" ) } ) ); // NOI18N

        if ( declaringClass.isInterface() ) {
            newSetter.setBody( null );
        }
        else if ( body != null ) {
            newSetter.setBody( body );
        }

        if ( javadoc ) {
            String comment = MessageFormat.format( PatternNode.getString( "COMMENT_PropertySetter" ),
                                                   new Object[] { getName(), name } );
            if ( constrained )
                comment = comment + PatternNode.getString( "COMMENT_Tag_ThrowsPropertyVeto" );
            newSetter.getJavaDoc().setRawText( comment );
        }


        if ( declaringClass == null ) {
            //System.out.println ("nodecl - gen setter"); // NOI18N
            throw new SourceException();
        }
        else {
            declaringClass.addMethod( newSetter );
            setterMethod = declaringClass.getMethod( newSetter.getName(), getParameterTypes( newSetter ) );
        }
    }

    /** Generates fied for the property. No javadoc comment is generated.
     * @throws SourceException If modification of source code is impossible.
     */
    void generateField() throws SourceException {
        generateField( false );
    }

    /** Generates fied for the property.
     * @param javadoc Generate javadoc comment?
     * @throws SourceException If modification of source code is impossible.
     */
    void generateField( boolean javadoc ) throws SourceException {
        ClassElement declaringClass = getDeclaringClass();
        FieldElement newField = new FieldElement();

        String name = getName();
        if( PropertyActionSettings.getDefault().getPropStyle().equals(PropertyActionSettings.GENERATE_UNDERSCORED))
            name = PropertyActionSettings.GENERATE_UNDERSCORED + name;
        newField.setName( Identifier.create( Introspector.decapitalize( name ) ) );
        newField.setType( type );
        newField.setModifiers( Modifier.PRIVATE );
        if ( javadoc ) {
            String comment = MessageFormat.format( PatternNode.getString( "COMMENT_PropertyField" ),
                                                   new Object[] { name } );
            newField.getJavaDoc().setRawText( comment );
        }
        if ( declaringClass == null ) {
            //System.out.println ("nodecl - gen setter"); // NOI18N
            throw new SourceException();
        }
        else {
            if( declaringClass.getField(newField.getName()) == null ){
                declaringClass.addField( newField );
                estimatedField = declaringClass.getField( newField.getName() );
            }
            else{
                FieldElement fe = declaringClass.getField(newField.getName());
                if( (fe.getModifiers() & Modifier.STATIC) != 0 )    //static can not be accessed via property
                    throw new SourceException();
                if( !fe.getType().getFullString().equals(newField.getType().getFullString()) )  //type not equal
                    throw new SourceException();
            }
        }
    }

    /** Deletes the estimated field in source
     * @throws SourceException If modification of source code is impossible.
     */ 
    void deleteEstimatedField() throws SourceException {

        if ( estimatedField == null )
            return;

        ClassElement declaringClass = getDeclaringClass();

        if ( declaringClass == null ) {
            //System.out.println ("nodecl"); // NOI18N
            throw new SourceException();
        }
        else {
            declaringClass.removeField( estimatedField );
            estimatedField = null;
        }
    }


    /** Deletes the setter method in source
     * @throws SourceException If modification of source code is impossible.
     */
    void deleteGetterMethod() throws SourceException {

        if ( getterMethod == null )
            return;

        ClassElement declaringClass = getDeclaringClass();

        if ( declaringClass == null ) {
            throw new SourceException();
        }
        else {
            declaringClass.removeMethod( getterMethod );
            getterMethod = null;
        }
    }

    /** Deletes the setter method in source
     * @throws SourceException If modification of source code is impossible.
     */
    void deleteSetterMethod() throws SourceException {

        if ( setterMethod == null )
            return;

        ClassElement declaringClass = getDeclaringClass();

        if ( declaringClass == null ) {
            throw new SourceException();
        }
        else {
            declaringClass.removeMethod( setterMethod );
            setterMethod = null;
        }

    }

    // UTILITY METHODS ----------------------------------------------------------

    /** Utility method resturns array of types of parameters of a method
     * @param methodElement Method which parameter types should be resolved
     * @return Array of types of parameters
     */
    static Type[] getParameterTypes( MethodElement methodElement ) {
        MethodParameter[] params = methodElement.getParameters();
        Type[] types = new Type[ params == null ? 0 : params.length ];

        for( int i = 0; i < params.length; i++ ) {
            types[i] = params[i].getType();
        }

        return types;
    }

    /** Sets the properties to values of other property pattern. If the
     * properties change fires PropertyChange event.
     * @param src Source PropertyPattern it's properties will be copied.
     */
    void copyProperties( PropertyPattern src ) {

        boolean changed = !src.getType().equals( getType() ) ||
                          !src.getName().equals( getName() ) ||
                          !(src.getMode() == getMode()) ||
                          !(src.getEstimatedField() == null ? estimatedField == null : src.getEstimatedField().equals( estimatedField ) );

        if ( src.getGetterMethod() != getterMethod )
            getterMethod = src.getGetterMethod();
        if ( src.getSetterMethod() != setterMethod )
            setterMethod = src.getSetterMethod();
        if ( src.getEstimatedField() != estimatedField )
            estimatedField = src.getEstimatedField();

        if ( changed ) {
            try {
                type = findPropertyType();
            }
            catch ( java.beans.IntrospectionException e ) {
            }
            name = findPropertyName();

            firePropertyChange( new java.beans.PropertyChangeEvent( this, null, null, null ) );
        }
    }
}

/*
 * Log
 *  13   Gandalf   1.12        1/15/00  Petr Hrebejk    BugFix 5386, 5385, 5393 
 *       and new WeakListener implementation
 *  12   Gandalf   1.11        1/13/00  Petr Hrebejk    i18n mk3
 *  11   Gandalf   1.10        1/12/00  Petr Hrebejk    i18n  
 *  10   Gandalf   1.9         1/4/00   Petr Hrebejk    Various bugfixes - 5036,
 *       5044, 5045
 *  9    Gandalf   1.8         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         10/10/99 Petr Hamernik   console debug messages 
 *       removed.
 *  7    Gandalf   1.6         9/13/99  Petr Hrebejk    Creating multiple 
 *       Properties/EventSet with the same name vorbiden. Forms made i18n
 *  6    Gandalf   1.5         7/29/99  Petr Hrebejk    Fix - change 
 *       ReadOnly/WriteOnly to ReadWrite mode diddn't registered the added 
 *       methods properly
 *  5    Gandalf   1.4         7/28/99  Petr Hrebejk    Property Mode change fix
 *  4    Gandalf   1.3         7/26/99  Petr Hrebejk    Better implementation of
 *       patterns resolving
 *  3    Gandalf   1.2         7/21/99  Petr Hrebejk    Bug fixes interface 
 *       bodies, is for boolean etc
 *  2    Gandalf   1.1         7/20/99  Petr Hrebejk    
 *  1    Gandalf   1.0         6/28/99  Petr Hrebejk    
 * $ 
 */ 