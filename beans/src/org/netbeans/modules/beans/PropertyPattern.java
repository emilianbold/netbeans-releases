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

package org.netbeans.modules.beans;

import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.text.Format;
import java.util.List;
import java.util.Iterator;

import org.openide.DialogDisplayer;

import org.openide.nodes.Node;
import org.openide.NotifyDescriptor;
import org.openide.util.Utilities;
import org.netbeans.jmi.javamodel.*;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
import org.netbeans.modules.java.ui.nodes.SourceNodes;

import javax.jmi.reflect.JmiException;


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
    static final String PROPERTY_CHANGE = "firePropertyChange"; // NOI18N
    /** Constant for VetoableChange */
    static final String VETOABLE_CHANGE = "fireVetoableChange"; // NOI18N
    
    /** Getter method of this property */
    protected Method getterMethod = null;
    /** Setter method of this property */
    protected Method setterMethod = null;
    /** Field which probably belongs to this property */
    protected Field  estimatedField = null;

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
                            Method getterMethod, Method setterMethod )
    throws IntrospectionException, JmiException {

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
     * @throws JmiException If the Property can't be created in the source.
     * @return Newly created PropertyPattern.
     */
    static PropertyPattern create( PatternAnalyser patternAnalyser,
                                   String name, String type,
                                   int mode, boolean bound, boolean constrained,
                                   boolean withField, boolean withReturn,
                                   boolean withSet, boolean withSupport ) throws JmiException, GenerateBeanException {

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
     * @throws JmiException If the Property can't be created in the source.
     * @return Newly created PropertyPattern.
     */
    static PropertyPattern create( PatternAnalyser patternAnalyser,
                                   String name, String type,
                                   int mode, boolean bound, boolean constrained,
                                   boolean withField, boolean withReturn,
                                   boolean withSet, boolean withSupport, 
                                   boolean useSupport, boolean fromField ) throws JmiException, GenerateBeanException {

        assert JMIUtils.isInsideTrans();
        PropertyPattern pp = new PropertyPattern( patternAnalyser );

        pp.name = name;
        pp.type = patternAnalyser.findType(type);

        // Generate field
        if ( ( withField || withSupport ) && !fromField ) {
            try {
                pp.generateField( true );
            } catch (GenerateBeanException e) {
                DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(
                        PatternNode.getString("MSG_Cannot_Create_Field"), // NOI18N
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
                    if( ( supportName = EventSetInheritanceAnalyser.showInheritanceEventDialog(EventSetInheritanceAnalyser.detectPropertyChangeSupport(  pp.getDeclaringClass()), "PropertyChangeSupport")) != null ) // NOI18N
                        boundSupport = false;
                if( constrainedSupport )
                    if( ( vetoSupportName = EventSetInheritanceAnalyser.showInheritanceEventDialog(EventSetInheritanceAnalyser.detectVetoableChangeSupport(  pp.getDeclaringClass()), "VetoableChangeSupport")) != null ) // NOI18N
                        constrainedSupport = false;
            }
            else{
                if( boundSupport )
                    if( ( supportName = EventSetInheritanceAnalyser.getInheritanceEventSupportName(EventSetInheritanceAnalyser.detectPropertyChangeSupport(  pp.getDeclaringClass()), "PropertyChangeSupport")) != null ) // NOI18N
                        boundSupport = false;
                if( constrainedSupport )
                    if( ( vetoSupportName = EventSetInheritanceAnalyser.getInheritanceEventSupportName(EventSetInheritanceAnalyser.detectVetoableChangeSupport(  pp.getDeclaringClass()), "VetoableChangeSupport")) != null ) // NOI18N
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
            pp.generateGetterMethod( BeanPatternGenerator.propertyGetterBody( name, withReturn, !fromField ), true );

        if ( mode == READ_WRITE || mode == WRITE_ONLY )
            pp.generateSetterMethod( BeanPatternGenerator.propertySetterBody( name, pp.getType(),
                                     bound, constrained, withSet, withSupport, supportName, vetoSupportName, !fromField ), constrained, true );

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
     * @throws JmiException If the modification of source code is impossible.
     */
    public void setName( String name ) throws IllegalArgumentException, JmiException {
        if ( !Utilities.isJavaIdentifier( name )  )
            throw new IllegalArgumentException( "Invalid event source name" ); // NOI18N
        
        JMIUtils.beginTrans(true);
        boolean rollback = true;
        try {
            setNameImpl(name);
            rollback = false;
        } finally {
            JMIUtils.endTrans(rollback);
        }
    }

    private void setNameImpl( String name ) throws JmiException {
        assert JMIUtils.isInsideTrans();
        String oldName = this.name;
        this.name = name;
        name = capitalizeFirstLetter( name );

        if ( getterMethod != null ) {
            String getterMethodID = ( getterMethod.getName().startsWith("get") ? // NOI18N
                                        "get" : "is" ) + name ; // NOI18N
            getterMethod.setName( getterMethodID );
            String oldGetterComment = MessageFormat.format( PatternNode.getString( "COMMENT_PropertyGetter" ),
                                           new Object[] { oldName } );
            String newGetterComment = MessageFormat.format( PatternNode.getString( "COMMENT_PropertyGetter" ),
                                           new Object[] { getName() } );
            String javadocText = getterMethod.getJavadocText();
            if (javadocText != null &&
                    oldGetterComment.trim().equals(javadocText.trim())) {
                getterMethod.setJavadocText( newGetterComment );
            }
        }
        if ( setterMethod != null ) {
            String setterMethodID = "set" + name; // NOI18N
            setterMethod.setName( setterMethodID );
            String oldSetterComment = MessageFormat.format( PatternNode.getString( "COMMENT_PropertySetter" ),
                                           new Object[] { oldName, oldName } );
            String newSetterComment = MessageFormat.format( PatternNode.getString( "COMMENT_PropertySetter" ),
                                           new Object[] { getName(), getName() } );
            String javadocText = setterMethod.getJavadocText();
            if (javadocText != null &&
                oldSetterComment.trim().equals(javadocText.trim())) {
                setterMethod.setJavadocText( newSetterComment );
            }
        }
        
        // Ask if to set the estimated field
        if ( estimatedField != null ) {
            String oldFieldComment = MessageFormat.format( PatternNode.getString( "COMMENT_PropertyField" ),
                                                   new Object[] { oldName } );
            String newFieldComment = MessageFormat.format( PatternNode.getString( "COMMENT_PropertyField" ),
                                                   new Object[] { getName() } );
            String javadocText = estimatedField.getJavadocText();
            if (javadocText != null &&
                oldFieldComment.trim().equals(javadocText.trim())) {
                estimatedField.setJavadocText(newFieldComment);
            }
                                                   
            int mode = getMode();
            Format fmt = SourceNodes.createElementFormat ("{m} {t} {n}"); // NOI18N
            String mssg = MessageFormat.format( PatternNode.getString( "FMT_ChangeFieldName" ),
                                                new Object[] { fmt.format (estimatedField) } );
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation ( mssg, NotifyDescriptor.YES_NO_OPTION );
            if ( DialogDisplayer.getDefault().notify( nd ).equals( NotifyDescriptor.YES_OPTION ) ) {
                String fieldName = Introspector.decapitalize(name);
                
                if( PropertyActionSettings.getDefault().getPropStyle().equals(PropertyActionSettings.GENERATE_UNDERSCORED))
                    fieldName = PropertyActionSettings.GENERATE_UNDERSCORED + fieldName;
                estimatedField.setName(fieldName);
                if ( mode == READ_WRITE || mode == READ_ONLY ) {
                    String existingGetterBody = getterMethod.getBodyText().trim();
                    String oldGetterBody1 = BeanPatternGenerator.propertyGetterBody( oldName, true, true ).trim();
                    String oldGetterBody2 = BeanPatternGenerator.propertyGetterBody( oldName, true, false ).trim();
                    if (existingGetterBody.equals(oldGetterBody1)) {
                        getterMethod.setBodyText(BeanPatternGenerator.propertyGetterBody( getName(), true, true));
                    } else if (existingGetterBody.equals(oldGetterBody2)) {
                        getterMethod.setBodyText(BeanPatternGenerator.propertyGetterBody( getName(), true, false));
                    }
                }
                if ( mode == READ_WRITE || mode == WRITE_ONLY ) {
                    String existingSetterBody = setterMethod.getBodyText().trim();
                    String oldSetterBody = BeanPatternGenerator.propertySetterBody (oldName, this.type, false, false, true, false, null, null).trim();
                    if (existingSetterBody.equals(oldSetterBody)) {
                        setterMethod.setBodyText(BeanPatternGenerator.propertySetterBody (getName(), getType(), false, false, true, false, null, null));
                        if ( setterMethod != null ) {
                            Parameter param = (Parameter) setterMethod.getParameters().get(0);
                            param.setName(Introspector.decapitalize( name ));
                        }
                    }
                }
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
     * @throws GenerateBeanException If the modification of source code is impossible.
     */
    public void setMode( int mode ) throws GenerateBeanException, JmiException {

        if ( getMode() == mode )
            return;

        JMIUtils.beginTrans(true);
        boolean rollback = true;
        try {
            switch (mode) {
                case READ_WRITE:
                    if (getterMethod == null)
                        generateGetterMethod(null, true);
                    if (setterMethod == null)
                        generateSetterMethod(null, false, true);
                    break;
                case READ_ONLY:
                    if (getterMethod == null)
                        generateGetterMethod(null, true);
                    if (setterMethod != null) {
                        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(PatternNode.getString("MSG_Delete_Setter") + PatternNode.getString("MSG_Continue_Confirm"), NotifyDescriptor.YES_NO_OPTION);
                        DialogDisplayer.getDefault().notify(nd);
                        if (nd.getValue().equals(NotifyDescriptor.YES_OPTION)) {
                            deleteSetterMethod();
                        }
                    }
                    break;
                case WRITE_ONLY:
                    if (setterMethod == null)
                        generateSetterMethod(null, false, true);
                    if (getterMethod != null) {
                        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(PatternNode.getString("MSG_Delete_Getter") + PatternNode.getString("MSG_Continue_Confirm"), NotifyDescriptor.YES_NO_OPTION);
                        DialogDisplayer.getDefault().notify(nd);
                        if (nd.getValue().equals(NotifyDescriptor.YES_OPTION)) {
                            deleteGetterMethod();
                        }
                    }
                    break;
            }
            rollback = false;
        } finally {
            JMIUtils.endTrans(rollback);
        }
    }

    /** Returns the getter method
     * @return Getter method of the property
     */
    public Method getGetterMethod() {
        return getterMethod;
    }

    /** Returns the setter method
     * @return Setter method of the property
     */
    public Method getSetterMethod() {
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
     * @throws JmiException If the modification of source code is impossible
     */
    public void setType(Type type) throws JmiException {
        JMIUtils.beginTrans(true);
        boolean rollback = true;
        try {
            setTypeImpl(type);
            rollback = false;
        } finally {
            JMIUtils.endTrans(rollback);
        }
    }
    
    private void setTypeImpl(Type type) throws JmiException {
        assert JMIUtils.isInsideTrans();
        if ( this.type.equals( type ) )
            return;

        if (getterMethod != null ) {
            if (JMIUtils.isPrimitiveType(this.type, PrimitiveTypeKindEnum.BOOLEAN)) {
                getterMethod.setName("get" + capitalizeFirstLetter( getName() ) ); // NOI18N
            } else if (JMIUtils.isPrimitiveType(type, PrimitiveTypeKindEnum.BOOLEAN)) {
                String mssg = MessageFormat.format( PatternNode.getString( "FMT_ChangeToIs" ),
                                                    new Object[] { capitalizeFirstLetter( getName() ) } );
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation ( mssg, NotifyDescriptor.YES_NO_OPTION );
                DialogDisplayer.getDefault().notify( nd );
                if( nd.getValue().equals( NotifyDescriptor.YES_OPTION ) ) {
                    getterMethod.setName( "is" + capitalizeFirstLetter( getName() ) ); // NOI18N
                }
            }
            getterMethod.setType( type );
        }

        if (setterMethod != null ) {
            List/*<Parameter>*/ params = setterMethod.getParameters();
            for (Iterator it = params.iterator(); it.hasNext();) {
                Parameter param = (Parameter) it.next();
                Type oldType = param.getType();
                param.setType( type );
                
                String body = setterMethod.getBodyText();
                //test if body contains change support
                if( body != null && ( body.indexOf(PROPERTY_CHANGE) != -1 || body.indexOf(VETOABLE_CHANGE) != -1 ) ) {
                    String mssg = MessageFormat.format( PatternNode.getString( "FMT_ChangeMethodBody" ),
                                                        new Object[] { setterMethod.getName() } );
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation ( mssg, NotifyDescriptor.YES_NO_OPTION );
                    DialogDisplayer.getDefault().notify( nd );
                    if( nd.getValue().equals( NotifyDescriptor.YES_OPTION ) ) {
                        String newBody = regeneratePropertySupport( setterMethod.getBodyText(), null, param.getName(), type, oldType );
                        if( newBody != null )
                            setterMethod.setBodyText(newBody);

                        newBody = regeneratePropertySupport( setterMethod.getBodyText(), PROPERTY_CHANGE, param.getName(), type, oldType );
                        if( newBody != null )
                            setterMethod.setBodyText(newBody);

                        newBody = regeneratePropertySupport( setterMethod.getBodyText(), VETOABLE_CHANGE, param.getName(), type, oldType );
                        if( newBody != null )
                            setterMethod.setBodyText(newBody);
                    }
                }
            }
        }

        this.type = type;

        // Ask if to change estimated field Type

        if ( estimatedField != null ) {
            Format fmt = SourceNodes.createElementFormat("{m} {t} {n}"); // NOI18N
            String mssg = MessageFormat.format( PatternNode.getString( "FMT_ChangeFieldType" ),
                                                new Object[] { fmt.format (estimatedField) } );
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation ( mssg, NotifyDescriptor.YES_NO_OPTION );
            if ( DialogDisplayer.getDefault().notify( nd ).equals( NotifyDescriptor.YES_OPTION ) ) {
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
    protected String regeneratePropertySupport( String methodBody, String changeType, String name, Type type, Type oldType ) throws JmiException {
        if( methodBody == null )
            return null;
        
        int first = -1;
        boolean post_index = false;
        boolean pre_index  = false;
        String propertyStyle = PropertyActionSettings.getDefault().getPropStyle();
        
        assert JMIUtils.isInsideTrans();
        if( oldType instanceof Array)
            oldType = getPrimitiveType((Array) oldType);
        //will search for line containing property support or field
        if( changeType != null ){
            if( (first = methodBody.indexOf(changeType)) == -1 )
                return null; 
        }
        else{
            String oldVarLine = " old" + Pattern.capitalizeFirstLetter( name ) + " = " + propertyStyle + name; // NOI18N
            if( (first = methodBody.indexOf( (oldType.getName() + oldVarLine  + ";") )) == -1 ) {   //non indexed // NOI18N
                if( (first = methodBody.indexOf( (oldType.getName() + oldVarLine  + "[index];") )) == -1 ) {  //indexed // NOI18N
                    if( (first = methodBody.indexOf( (oldType.getName() + "[]" + oldVarLine  + ";") )) == -1 ) {  //indexed // NOI18N
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

            if ( type instanceof PrimitiveType ) {            
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
            newBody.append( type.getName() );
            //if( pre_index ){
            //    newBody.append( "[]" );
            //}
            newBody.append( " old" ).append( Pattern.capitalizeFirstLetter( name ) ); // NOI18N
            newBody.append( " = " ).append( propertyStyle ).append( name ); // NOI18N            
            if( post_index ){
                newBody.append( "[index]" ); // NOI18N
            }
        }

        StringBuffer sb = new StringBuffer(methodBody);
        sb.delete(first, last);
        sb.insert(first, newBody);
        return sb.toString();        
    }
    
    private static Type getPrimitiveType(Array atype) throws JmiException {
        assert JMIUtils.isInsideTrans();
        Type type = atype.getType();
        while (type instanceof Array) {
            type = ((Array) type).getType();
        }
        return type;
    }

    /** Gets the cookie of the first available method
     * @param cookieType Class of the Cookie
     * @return Cookie of Getter or Setter MethodElement
     */
    public Node.Cookie getCookie( Class cookieType ) {
        return super.getCookie(cookieType);
    }

    /** Gets the estimated field
     * @return Field which (probably) belongs to the property.
     */
    public Field getEstimatedField( ) {
        return estimatedField;
    }

    /** Sets the estimated field
     * @param field Field for the property
     */
    void setEstimatedField( Field field ) {
        estimatedField = field;
    }

    /** Destroys methods associated methods with the pattern in source
     */
    public void destroy() throws JmiException {
        assert JMIUtils.isInsideTrans();
        if (estimatedField != null) {
            Format fmt = SourceNodes.createElementFormat("{m} {t} {n}"); // NOI18N
            String mssg = MessageFormat.format(PatternNode.getString("FMT_DeleteField"),
                    new Object[]{fmt.format(estimatedField)});
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(mssg, NotifyDescriptor.YES_NO_OPTION);
            if (DialogDisplayer.getDefault().notify(nd).equals(NotifyDescriptor.YES_OPTION)) {
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
    PropertyPattern( PropertyPattern x, PropertyPattern y ) throws JmiException {
        super( y.patternAnalyser );
        
        assert JMIUtils.isInsideTrans();

        // Figure out the merged getterMethod
        Method xr = x.getterMethod;
        Method yr = y.getterMethod;
        getterMethod = xr;

        // Normaly give priority to y's getterMethod
        if ( yr != null ) {
            getterMethod = yr;
        }
        
        // However, if both x and y reference read method in the same class,
        // give priority to a boolean "is" method over boolean "get" method. // NOI18N
        if ( xr != null && yr != null &&
                xr.getDeclaringClass() == yr.getDeclaringClass() &&
                JMIUtils.isPrimitiveType(xr.getType(), PrimitiveTypeKindEnum.BOOLEAN) &&
                JMIUtils.isPrimitiveType(yr.getType(), PrimitiveTypeKindEnum.BOOLEAN) &&
                xr.getName().indexOf("is") == 0 && // NOI18N
                yr.getName().indexOf("get") == 0 ) { // NOI18N
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
    Type findPropertyType() throws IntrospectionException, JmiException {
        
        assert JMIUtils.isInsideTrans();
        Type resolvedType = null;

        if ( getterMethod != null ) {
            if ( !getterMethod.getParameters().isEmpty() ) {
                throw new IntrospectionException( "bad read method arg count" ); // NOI18N
            }
            resolvedType = getterMethod.getType();
            if ( JMIUtils.isPrimitiveType(resolvedType, PrimitiveTypeKindEnum.VOID) ) {
                throw new IntrospectionException( "read method " + getterMethod.getName() + // NOI18N
                                                  " returns void" ); // NOI18N
            }
        }
        if ( setterMethod != null ) {
            List/*<Parameter>*/ params = setterMethod.getParameters();
            if ( params.size() != 1 ) {
                throw new IntrospectionException( "bad write method arg count" ); // NOI18N
            }
            Parameter param = (Parameter) params.get(0);
            if ( resolvedType != null && !resolvedType.equals( param.getType() ) ) {
                throw new IntrospectionException( "type mismatch between read and write methods" ); // NOI18N
            }
            resolvedType = param.getType();
        }
        return resolvedType;
    }

    /** Based on names of getter and setter resolves the name of the property.
     * @return Name of the property
     */
    String findPropertyName() throws JmiException {
        assert JMIUtils.isInsideTrans();
        String methodName = null;

        if ( getterMethod != null )
            methodName = getterMethod.getName() ;
        else if ( setterMethod != null )
            methodName = setterMethod.getName() ;
        else {
            return null;
        }

        return  methodName.startsWith( "is" ) ? // NOI18N
                Introspector.decapitalize( methodName.substring(2) ) :
                Introspector.decapitalize( methodName.substring(3) );
    }

    // METHODS FOR GENERATING AND DELETING METHODS AND FIELDS--------------------

    /** Generates getter method without body and without Javadoc comment.
     * @throws GenerateBeanException if modification of source code is impossible.
     */
    void generateGetterMethod() throws GenerateBeanException, JmiException {
        generateGetterMethod( null, false );
    }

    /** Generates getter method with body and optionaly with Javadoc comment.
     * @param body Body of the method
     * @param javadoc Generate Javadoc comment?
     * @throws GenerateBeanException if modification of source code is impossible.
     */
    void generateGetterMethod( String body, boolean javadoc ) throws GenerateBeanException, JmiException {
        assert JMIUtils.isInsideTrans();
        JavaClass declaringClass = getDeclaringClass();
        JavaModelPackage jmodel = JavaMetamodel.getManager().getJavaExtent(declaringClass);
        Method newGetter = jmodel.getMethod().createMethod();

        String namePrefix = JMIUtils.isPrimitiveType(type, PrimitiveTypeKindEnum.BOOLEAN) ? "is" : "get"; // NOI18N
        newGetter.setName( namePrefix + capitalizeFirstLetter( getName() ) );
        newGetter.setType( type );
        newGetter.setModifiers( Modifier.PUBLIC );

        if ( declaringClass.isInterface() ) {
            newGetter.setBody( null );
        }
        else if ( body != null ) {
            newGetter.setBodyText( body );
        }

        if ( javadoc ) {
            String comment = MessageFormat.format( PatternNode.getString( "COMMENT_PropertyGetter" ),
                                                   new Object[] { getName() } );
            newGetter.setJavadocText( comment );
        }

        if ( declaringClass == null ) {
            //System.out.println ("nodecl - gen getter"); // NOI18N
            throw new GenerateBeanException();
        }
        else {
            declaringClass.getFeatures().add(newGetter);
            getterMethod = newGetter;
        }

    }

    /** Generates setter method without body and without Javadoc comment.
     * @throws GenerateBeanException If modification of source code is impossible.
     */
    void generateSetterMethod() throws GenerateBeanException, JmiException {
        generateSetterMethod( null, false, false );
    }

    /** Generates setter method with body and optionaly with Javadoc comment.
     * @param body Body of the method
     * @param javadoc Generate Javadoc comment?
     * @param constrained Is the property constrained?
     * @throws GenerateBeanException If modification of source code is impossible.
     */
    void generateSetterMethod( String body, boolean constrained, boolean javadoc ) throws GenerateBeanException, JmiException {
        assert JMIUtils.isInsideTrans();
        JavaClass declaringClass = getDeclaringClass();
        JavaModelPackage jmodel = JavaMetamodel.getManager().getJavaExtent(declaringClass);
        Method newSetter = jmodel.getMethod().createMethod();

        newSetter.setName( "set" + capitalizeFirstLetter( getName() ) ); // NOI18N
        newSetter.setType( patternAnalyser.findType("void") ); // NOI18N
        newSetter.setModifiers( Modifier.PUBLIC );
        Parameter param = jmodel.getParameter().createParameter();
        param.setName(name);
        param.setType(type);
        newSetter.getParameters().add(param);
        if ( constrained ) {
            MultipartId exception = jmodel.getMultipartId().
                    createMultipartId("java.beans.PropertyVetoException", null, null); // NOI18N
            newSetter.getExceptionNames().add(exception);
        }

        if ( declaringClass.isInterface() ) {
            newSetter.setBody( null );
        }
        else if ( body != null ) {
            newSetter.setBodyText( body );
        }

        if ( javadoc ) {
            String comment = MessageFormat.format( PatternNode.getString( "COMMENT_PropertySetter" ),
                                                   new Object[] { getName(), name } );
            if ( constrained )
                comment = comment + PatternNode.getString( "COMMENT_Tag_ThrowsPropertyVeto" );
            newSetter.setJavadocText( comment );
        }


        if ( declaringClass == null ) {
            //System.out.println ("nodecl - gen setter"); // NOI18N
            throw new GenerateBeanException();
        }
        else {
            declaringClass.getFeatures().add(newSetter);
            setterMethod = newSetter;
        }
    }

    /** Generates fied for the property. No javadoc comment is generated.
     * @throws GenerateBeanException If modification of source code is impossible.
     */
    void generateField() throws GenerateBeanException, JmiException {
        generateField( false );
    }

    /** Generates field for the property.
     * @param javadoc Generate javadoc comment?
     * @throws GenerateBeanException If modification of source code is impossible.
     */
    void generateField( boolean javadoc ) throws GenerateBeanException, JmiException {
        assert JMIUtils.isInsideTrans();
        JavaClass declaringClass = getDeclaringClass();
        JavaModelPackage jmodel = JavaMetamodel.getManager().getJavaExtent(declaringClass);
        Field newField = jmodel.getField().createField();

        String name = getName();
        if( PropertyActionSettings.getDefault().getPropStyle().equals(PropertyActionSettings.GENERATE_UNDERSCORED))
            name = PropertyActionSettings.GENERATE_UNDERSCORED + name;
        name = Introspector.decapitalize( name );
        newField.setName( name );
        newField.setType( type );
        newField.setModifiers( Modifier.PRIVATE );
        if ( javadoc ) {
            String comment = MessageFormat.format( PatternNode.getString( "COMMENT_PropertyField" ),
                                                   new Object[] { name } );
            newField.setJavadocText( comment );
        }
        if ( declaringClass == null ) {
            //System.out.println ("nodecl - gen setter"); // NOI18N
            throw new IllegalStateException("Missing declaring class"); // NOI18N
        } else {
            String newFieldName = newField.getName();
            Field f = declaringClass.getField(newFieldName, false);
            if ( f == null ){
                declaringClass.getFeatures().add(newField);
                estimatedField = newField;
            } else{
                if( (f.getModifiers() & Modifier.STATIC) != 0 )    //static can not be accessed via property
                    throw new GenerateBeanException();
                if( !f.getType().equals(newField.getType()) )  //type not equal
                    throw new GenerateBeanException();
            }
        }
    }

    /** Deletes the estimated field in source
     * @throws JmiException If modification of source code is impossible.
     */ 
    void deleteEstimatedField() throws JmiException {

        if ( estimatedField == null )
            return;

        assert JMIUtils.isInsideTrans();
        JavaClass declaringClass = getDeclaringClass();
        declaringClass.getFeatures().remove(estimatedField);
        estimatedField = null;
    }


    /** Deletes the setter method in source
     * @throws JmiException If modification of source code is impossible.
     */
    void deleteGetterMethod() throws JmiException {

        if ( getterMethod == null )
            return;
        
        assert JMIUtils.isInsideTrans();
        JavaClass declaringClass = getDeclaringClass();
        declaringClass.getFeatures().remove(getterMethod);
        getterMethod = null;
    }

    /** Deletes the setter method in source
     * @throws JmiException If modification of source code is impossible.
     */
    void deleteSetterMethod() throws JmiException {

        if ( setterMethod == null )
            return;

        assert JMIUtils.isInsideTrans();
        JavaClass declaringClass = getDeclaringClass();
        declaringClass.getFeatures().remove(setterMethod);
        setterMethod = null;

    }

    // UTILITY METHODS ----------------------------------------------------------

    /** Sets the properties to values of other property pattern. If the
     * properties change fires PropertyChange event.
     * @param src Source PropertyPattern it's properties will be copied.
     */
    void copyProperties( PropertyPattern src ) throws JmiException {
        assert JMIUtils.isInsideTrans();
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

            // XXX cannot be fired inside mdr transaction; post to dedicated thread or redesigne somehow
            firePropertyChange( new java.beans.PropertyChangeEvent( this, null, null, null ) );
        }
    }
}
