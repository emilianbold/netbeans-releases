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
import java.util.List;

import org.openide.DialogDisplayer;

import org.openide.nodes.Node;
import org.openide.NotifyDescriptor;
import org.openide.ErrorManager;
import org.netbeans.jmi.javamodel.*;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;

import javax.jmi.reflect.JmiException;


/** Class representing JavaBeans IndexedProperty.
 * @author Petr Hrebejk
 */
public final class IdxPropertyPattern extends PropertyPattern {

    /** Getter method of this indexed property */
    protected Method indexedGetterMethod = null;
    /** Setter method of this indexed property */
    protected Method indexedSetterMethod = null;

    /** Holds the indexed type of the property resolved from methods. */
    protected Type indexedType;

    /** Creates new IndexedPropertyPattern just one of the methods indexedGetterMethod
     * and indexedSetterMethod may be null. 
     * @param patternAnalyser patternAnalyser which creates this Property.
     * @param getterMethod getterMethod may be <CODE>null</CODE>.
     * @param setterMethod setterMethod may be <CODE>null</CODE>.
     * @param indexedGetterMethod getterMethod of the property or <CODE>null</CODE>.
     * @param indexedSetterMethod setterMethod of the property or <CODE>null</CODE>.
     * @throws IntrospectionException If specified methods do not follow beans Property rules.
     */  
    public IdxPropertyPattern( PatternAnalyser patternAnalyser,
                               Method getterMethod, Method setterMethod,
                               Method indexedGetterMethod, Method indexedSetterMethod )
    throws IntrospectionException, JmiException {

        super ( patternAnalyser, getterMethod, setterMethod );

        this.indexedGetterMethod = indexedGetterMethod;
        this.indexedSetterMethod = indexedSetterMethod;

        findIndexedPropertyType();
        if (this.type == null && this.indexedType != null) {
            JavaModelPackage jmodel = JavaMetamodel.getManager().getJavaExtent(this.indexedType);
            this.type = jmodel.getArray().resolveArray(this.indexedType);
        }
        name = findIndexedPropertyName();
    }

    /** Creates new IndexedPropertyPattern.
     * @param patternAnalyser patternAnalyser which creates this IndexedProperty.
     */
    private IdxPropertyPattern( PatternAnalyser patternAnalyser ) {
        super( patternAnalyser );
    }

    /** Creates new indexed property pattern with extended options
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
     * @param niGetter Non-indexed getter method
     * @param niWithReturn Generate return statement in non-indexed getter?
     * @param niSetter Non-indexed setter method
     * @param niWithSet Generate set field statement in non-indexed setter?
     * @throws JmiException If the Property can't be created in the source.
     * @return Newly created PropertyPattern.
     */
    static IdxPropertyPattern create( PatternAnalyser patternAnalyser,
                                      String name, String type,
                                      int mode, boolean bound, boolean constrained,
                                      boolean withField, boolean withReturn,
                                      boolean withSet, boolean withSupport,
                                      boolean niGetter, boolean niWithReturn,
                                      boolean niSetter, boolean niWithSet ) throws JmiException, GenerateBeanException {

        return create(patternAnalyser, name, type, mode, bound, constrained, withField, withReturn, withSet, withSupport, niGetter, niWithReturn, niSetter, niWithSet, false, false );
    }
    /** Creates new indexed property pattern with extended options
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
     * @param niGetter Non-indexed getter method
     * @param niWithReturn Generate return statement in non-indexed getter?
     * @param niSetter Non-indexed setter method
     * @param niWithSet Generate set field statement in non-indexed setter?
     * @param useSupport use change support without prompting
     * @param fromField signalize that all action are activatet on field
     * @throws JmiException If the Property can't be created in the source.
     * @return Newly created PropertyPattern.
     */
    static IdxPropertyPattern create( PatternAnalyser patternAnalyser,
                                      String name, String type,
                                      int mode, boolean bound, boolean constrained,
                                      boolean withField, boolean withReturn,
                                      boolean withSet, boolean withSupport,
                                      boolean niGetter, boolean niWithReturn,
                                      boolean niSetter, boolean niWithSet,
                                      boolean useSupport, boolean fromField ) throws JmiException, GenerateBeanException {

        assert JMIUtils.isInsideTrans();
        IdxPropertyPattern ipp = new IdxPropertyPattern( patternAnalyser );

        ipp.name = name;
        ipp.type = null;
        ipp.indexedType = patternAnalyser.findType(type);

        // Set the non-indexed type when needed
        if ( withField || withSupport || niGetter || niSetter ) {
            JavaModelPackage jmodel = JavaMetamodel.getManager().getJavaExtent(ipp.indexedType);
            ipp.type = jmodel.getArray().resolveArray(ipp.indexedType);
        }

        // Generate field
        if ( ( withField || withSupport ) && !fromField ) {
            if ( ipp.type != null ) {
                try {
                    ipp.generateField( true );
                } catch (GenerateBeanException e) {
                    DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(
                            PatternNode.getString("MSG_Cannot_Create_Field"),
                            NotifyDescriptor.WARNING_MESSAGE));
                }
            }
        }


        // Ensure property change support field and methods exist
        String supportName = null;
        String vetoSupportName = null;

        if ( withSupport ) {

            boolean boundSupport = bound;
            boolean constrainedSupport = constrained;

            
            if( !useSupport ){
                if( boundSupport )
                    if( ( supportName = EventSetInheritanceAnalyser.showInheritanceEventDialog(EventSetInheritanceAnalyser.detectPropertyChangeSupport(  ipp.getDeclaringClass()), "PropertyChangeSupport")) != null ) // NOI18N
                        boundSupport = false;
                if( constrainedSupport )
                    if( ( vetoSupportName = EventSetInheritanceAnalyser.showInheritanceEventDialog(EventSetInheritanceAnalyser.detectVetoableChangeSupport(  ipp.getDeclaringClass()), "VetoableChangeSupport")) != null ) // NOI18N
                        constrainedSupport = false;
            }
            else {
                if( boundSupport )
                    if( ( supportName = EventSetInheritanceAnalyser.getInheritanceEventSupportName(EventSetInheritanceAnalyser.detectPropertyChangeSupport(  ipp.getDeclaringClass()), "PropertyChangeSupport")) != null ) // NOI18N
                        boundSupport = false;
                if( constrainedSupport )
                    if( ( vetoSupportName = EventSetInheritanceAnalyser.getInheritanceEventSupportName(EventSetInheritanceAnalyser.detectVetoableChangeSupport(  ipp.getDeclaringClass()), "VetoableChangeSupport")) != null ) // NOI18N
                        constrainedSupport = false;
            }
            
            if ( boundSupport )
                supportName = BeanPatternGenerator.supportField( ipp.getDeclaringClass() );
            if ( constrainedSupport )
                vetoSupportName = BeanPatternGenerator.vetoSupportField( ipp.getDeclaringClass() );

            if ( boundSupport )
                BeanPatternGenerator.supportListenerMethods( ipp.getDeclaringClass(), supportName );
            if ( constrainedSupport )
                BeanPatternGenerator.vetoSupportListenerMethods( ipp.getDeclaringClass(), vetoSupportName );
        }

        if ( mode == READ_WRITE || mode == READ_ONLY ) {
            if( (fromField && withReturn) || !fromField )
                ipp.generateIndexedGetterMethod( BeanPatternGenerator.idxPropertyGetterBody( name, withReturn ), true );
            if ( ipp.type != null && niGetter )
                ipp.generateGetterMethod( BeanPatternGenerator.propertyGetterBody( name, niWithReturn), true );
        }
        if ( mode == READ_WRITE || mode == WRITE_ONLY ) {
            /*
            ipp.generateIndexedSetterMethod( BeanPatternGenerator.idxPropertySetterBody( name, ipp.getType(),
                bound, constrained, withSet, withSupport, supportName, vetoSupportName ), constrained, true );
            */
            if( (fromField && withSet) || !fromField )
                ipp.generateIndexedSetterMethod( BeanPatternGenerator.idxPropertySetterBody( name, ipp.getIndexedType(),
                                                 bound, constrained, withSet, withSupport, supportName, vetoSupportName ), constrained, true );

            if ( ipp.type != null && niSetter )
                ipp.generateSetterMethod( BeanPatternGenerator.propertySetterBody( name, ipp.getType(),
                                          bound, constrained, niWithSet, withSupport, supportName, vetoSupportName ), constrained, true );
        }
        return ipp;
    }



    /** Gets the name of IdxPropertyPattern
     * @return Name of the Indexed Property
     */
    public Type getIndexedType() {
        return indexedType;
    }

    /** Sets the name of IdxPropertyPattern
     * @param name New name of the property.
     * @throws JmiException If the modification of source code is impossible.
     */
    public void setName(String name) throws IllegalArgumentException, JmiException {
        String oldName = this.name;
        super.setName( name );

        name = capitalizeFirstLetter( name );

        if ( indexedGetterMethod != null ) {
            String idxGetterMethodID = ( indexedGetterMethod.getName().startsWith("get") ? // NOI18N
                                           "get" : "is" ) + name ; // NOI18N
            indexedGetterMethod.setName( idxGetterMethodID );
            String oldGetterComment = MessageFormat.format( PatternNode.getString( "COMMENT_IdxPropertyGetter" ),
                                           new Object[] { oldName } );
            String newGetterComment = MessageFormat.format( PatternNode.getString( "COMMENT_IdxPropertyGetter" ),
                                           new Object[] { getName() } );
            String indexedGetterJavadoc = indexedGetterMethod.getJavadocText();
            if (indexedGetterJavadoc != null &&
                    oldGetterComment.trim().equals(indexedGetterJavadoc.trim())) {
                indexedGetterMethod.setJavadocText( newGetterComment );
            }
        }
        if ( indexedSetterMethod != null ) {
            String idxSetterMethodID = "set" + name; // NOI18N
            indexedSetterMethod.setName( idxSetterMethodID );
            String oldSetterComment = MessageFormat.format( PatternNode.getString( "COMMENT_IdxPropertySetter" ),
                                           new Object[] { oldName, oldName } );
            String newSetterComment = MessageFormat.format( PatternNode.getString( "COMMENT_IdxPropertySetter" ),
                                           new Object[] { getName(), getName() } );
            String indexedSetterJavadoc = indexedSetterMethod.getJavadocText();
            if (indexedSetterJavadoc != null &&
                    oldSetterComment.trim().equals(indexedSetterJavadoc.trim())) {
                indexedSetterMethod.setJavadocText( newSetterComment );
            }
        }
        
        // change body and javadoc of idx accessors if the field has been changed
        if ( estimatedField != null && estimatedField.getName().equals(getName())) {
            int mode = getMode();
            if ( mode == READ_WRITE || mode == READ_ONLY ) {
                String existingGetterBody = indexedGetterMethod.getBodyText().trim();
                String oldGetterBody1 = BeanPatternGenerator.idxPropertyGetterBody( oldName, true, true ).trim();
                String oldGetterBody2 = BeanPatternGenerator.idxPropertyGetterBody( oldName, true, false ).trim();
                if (existingGetterBody.equals(oldGetterBody1)) {
                    indexedGetterMethod.setBodyText(BeanPatternGenerator.idxPropertyGetterBody( getName(), true, true));
                } else if (existingGetterBody.equals(oldGetterBody2)) {
                    indexedGetterMethod.setBodyText(BeanPatternGenerator.idxPropertyGetterBody( getName(), true, false));
                }
            }
            if ( mode == READ_WRITE || mode == WRITE_ONLY ) {
                String existingSetterBody = indexedSetterMethod.getBodyText().trim();
                String oldSetterBody = BeanPatternGenerator.idxPropertySetterBody (oldName, this.type, false, false, true, false, null, null).trim();
                if (existingSetterBody.equals(oldSetterBody)) {
                    indexedSetterMethod.setBodyText(BeanPatternGenerator.idxPropertySetterBody (getName(), getType(), false, false, true, false, null, null));

                    if ( indexedSetterMethod != null ) {
                        List/*<Parameter>*/ params = indexedSetterMethod.getParameters();
                        Parameter param = (Parameter) params.get(1);
                        param.setName(Introspector.decapitalize( name ));
                    }
                }
            }
        }
    }




    /** Returns the indexed getter method
     * @return Getter method of the property
     */
    public Method getIndexedGetterMethod() {
        return indexedGetterMethod;
    }

    /** Returns the indexed setter method
     * @return Getter method of the property
     */
    public Method getIndexedSetterMethod() {
        return indexedSetterMethod;
    }

    /** Sets the non-indexed type of IdxPropertyPattern
     * @param type New non-indexed type of the indexed property
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
        if ( this.type != null && this.type.equals( type ) )
            return;

        // Remember the old type & old indexed type
        Type oldIndexedType = this.indexedType;
        Type oldType = this.type;

        if ( oldType == null ) {
            this.type = type;
            oldType = type;
            int mode = getMode();
            if ( mode == READ_WRITE || mode == READ_ONLY ) {
                try {
                    generateGetterMethod();
                } catch (GenerateBeanException e) {
                    ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
                }
            }
            if ( mode == READ_WRITE || mode == WRITE_ONLY ) {
                try {
                    generateSetterMethod();
                } catch (GenerateBeanException e) {
                    ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
                }
            }
        }
        else
            // Change the type
            super.setType( type );

        // Test if the idexedType is the type of array and change it if so
        if ( type instanceof Array && oldType instanceof Array && oldIndexedType.equals(((Array) oldType).getType()) ) {
            Type newType = ((Array) type).getType();

            // Set the type  to new type
            setIndexedTypeImpl( newType, false );
        }
    }

    /** Sets the indexed type of IdxPropertyPattern
     * @param type New indexed type of the indexed property
     * @throws JmiException If the modification of source code is impossible
     */
    public void setIndexedType(Type type) throws JmiException {
        JMIUtils.beginTrans(true);
        boolean rollback = true;
        try {
            setIndexedTypeImpl(type, true);
            rollback = false;
        } finally {
            JMIUtils.endTrans(rollback);
        }
    }
    
    private void setIndexedTypeImpl(Type type, boolean changeType) throws JmiException {
        assert JMIUtils.isInsideTrans();
        if ( this.indexedType.equals( type ) )
            return;

        // Remember the old type & old indexed type
        Type oldIndexedType = this.indexedType;
        Type oldType = this.type;

        // Change the indexed type
        if (indexedGetterMethod != null ) {
            indexedGetterMethod.setType( type );
        }
        if (indexedSetterMethod != null ) {
            List/*<Parameter>*/ params = indexedSetterMethod.getParameters();
            if ( params.size() > 1 ) {
                Parameter param = (Parameter) params.get(1);
                param.setType( type );

                String body = indexedSetterMethod.getBodyText();
                
                //test if body contains change support
                if( body != null && ( body.indexOf(PropertyPattern.PROPERTY_CHANGE) != -1 || body.indexOf(PropertyPattern.VETOABLE_CHANGE) != -1 ) ) {
                    String mssg = MessageFormat.format( PatternNode.getString( "FMT_ChangeMethodBody" ),
                                                        new Object[] { setterMethod.getName() } );
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation ( mssg, NotifyDescriptor.YES_NO_OPTION );
                    DialogDisplayer.getDefault().notify( nd );
                    if( nd.getValue().equals( NotifyDescriptor.YES_OPTION ) ) {
                        String newBody = regeneratePropertySupport( indexedSetterMethod.getBodyText(), null, param.getName(), type, oldType );
                        if( newBody != null )
                            indexedSetterMethod.setBodyText(newBody);
                    }
                }
            }
        }

        // Test if the old type of getter and seter was an array of indexedType
        // if so change the type of that array.
        if (changeType) {
            JavaModelPackage jmodel = JavaMetamodel.getManager().getJavaExtent(type);
            Type newArrayType = jmodel.getArray().resolveArray(type);
            super.setType( newArrayType );
        }

        indexedType = type;
    }

    /**
     * @param methodBody old method body
     * @param name of property
     * @param type new type of property value
     * @param oldType old type of property value
     * @return null if no change is possible or new body if it is
     */
    // XXX seems that noone uses this
    private String regenerateIdxPropertySupport( String methodBody, String name, Type type, Type oldType ){
        if( methodBody == null )
            return null;
        
        int first = -1;
        String propertyStyle = PropertyActionSettings.getDefault().getPropStyle();
        
        //will search for line containing property support or field
        String oldVarLine = oldType.toString() + " old" + Pattern.capitalizeFirstLetter( name ) + " = " + propertyStyle + name; // NOI18N
        if( (first = methodBody.indexOf( oldVarLine )) == -1 )
            return null;

        if( first == -1 )
            return null;
        
        //find end of statement
        int last = methodBody.indexOf(';', first);
        if( first >= last )
            return null;
        
        StringBuffer newBody = new StringBuffer(100);
        newBody.append( type.toString() );
        newBody.append( " old" ).append( Pattern.capitalizeFirstLetter( name ) ); // NOI18N
        newBody.append( " = " ).append( propertyStyle ).append( name ); // NOI18N            

        StringBuffer sb = new StringBuffer(methodBody);
        sb.delete(first, last);
        sb.insert(first, newBody);
        return sb.toString();        
    }

    /** Returns the mode of the property {@link PropertyPattern#READ_WRITE READ_WRITE},
     * {@link PropertyPattern#READ_ONLY READ_ONLY} or {@link PropertyPattern#WRITE_ONLY WRITE_ONLY}
     * @return Mode of the property
     */
    public int getMode() {
        if ( indexedSetterMethod != null && indexedGetterMethod != null )
            return READ_WRITE;
        else if ( indexedGetterMethod != null && indexedSetterMethod == null )
            return READ_ONLY;
        else if ( indexedSetterMethod != null && indexedGetterMethod == null )
            return WRITE_ONLY;
        else
            return super.getMode();
    }

    /** Sets the property to be writable
     * @param mode New Mode {@link PropertyPattern#READ_WRITE READ_WRITE}, 
     *   {@link PropertyPattern#READ_ONLY READ_ONLY} or {@link PropertyPattern#WRITE_ONLY WRITE_ONLY}
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
                        generateGetterMethod();
                    if (setterMethod == null)
                        generateSetterMethod();
                    if (indexedGetterMethod == null)
                        generateIndexedGetterMethod();
                    if (indexedSetterMethod == null)
                        generateIndexedSetterMethod();
                    break;
                case READ_ONLY:
                    if (getterMethod == null)
                        generateGetterMethod();
                    if (indexedGetterMethod == null)
                        generateIndexedGetterMethod();

                    if (setterMethod != null || indexedSetterMethod != null) {
                        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(PatternNode.getString("MSG_Delete_Setters") + PatternNode.getString("MSG_Continue_Confirm"), NotifyDescriptor.YES_NO_OPTION);
                        DialogDisplayer.getDefault().notify(nd);
                        if (nd.getValue().equals(NotifyDescriptor.YES_OPTION)) {
                            if (setterMethod != null)
                                deleteSetterMethod();
                            if (indexedSetterMethod != null)
                                deleteIndexedSetterMethod();
                        }
                    }
                    break;
                case WRITE_ONLY:
                    if (setterMethod == null)
                        generateSetterMethod();
                    if (indexedSetterMethod == null)
                        generateIndexedSetterMethod();
                    if (getterMethod != null || indexedGetterMethod != null) {
                        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(PatternNode.getString("MSG_Delete_Getters") + PatternNode.getString("MSG_Continue_Confirm"), NotifyDescriptor.YES_NO_OPTION);
                        DialogDisplayer.getDefault().notify(nd);
                        if (nd.getValue().equals(NotifyDescriptor.YES_OPTION)) {
                            if (getterMethod != null)
                                deleteGetterMethod();
                            if (indexedGetterMethod != null)
                                deleteIndexedGetterMethod();
                        }
                    }
                    break;
            }
            rollback = false;
        } finally {
            JMIUtils.endTrans(rollback);
        }

    }

    /** Gets the cookie of the first available inderxed method
     * @param cookieType Class of the Cookie
     * @return Cookie of indexedGetter or indexedSetter MethodElement
     */
    public Node.Cookie getCookie( Class cookieType ) {
        return super.getCookie(cookieType);
    }

    /** Destroys methods associated methods with the pattern in source
     */
    public void destroy() throws JmiException {
        assert JMIUtils.isInsideTrans();
        deleteIndexedSetterMethod();
        deleteIndexedGetterMethod();
        super.destroy();
    }

    // Utitlity methods -------------------------------------------------------------------

    /** Package private constructor. Merges two property descriptors. Where they
     * conflict, gives the second argument (y) priority over the first argumnet (x).
     * @param x The first (lower priority) PropertyPattern.
     * @param y The second (higher priority) PropertyPattern.
     */
    IdxPropertyPattern( PropertyPattern x, PropertyPattern y ) throws JmiException {
        super( x, y );
        assert JMIUtils.isInsideTrans();
        if ( x instanceof IdxPropertyPattern ) {
            IdxPropertyPattern ix = (IdxPropertyPattern)x;
            indexedGetterMethod = ix.indexedGetterMethod;
            indexedSetterMethod = ix.indexedSetterMethod;
            indexedType = ix.indexedType;
            type = type == null? ix.type: type;
        }
        if ( y instanceof IdxPropertyPattern ) {
            IdxPropertyPattern iy = (IdxPropertyPattern)y;
            if ( iy.indexedGetterMethod != null )
                indexedGetterMethod = iy.indexedGetterMethod;
            if ( iy.indexedSetterMethod != null )
                indexedSetterMethod = iy.indexedSetterMethod;
            indexedType = iy.indexedType;
            type = type == null? iy.type: type;
        }
        name  = findIndexedPropertyName();
    }

    /** Resolves the indexed type of the property from type of getter and setter.
     * Chcecks for conformance to Beans design patterns.
     * @throws IntrospectionException if the property doesnt folow the design patterns
     */
    private void findIndexedPropertyType() throws IntrospectionException, JmiException {

        assert JMIUtils.isInsideTrans();
        indexedType = null;

        if ( indexedGetterMethod != null ) {
            List/*<Parameter>*/ params = indexedGetterMethod.getParameters();
            if ( params.size() != 1 ) {
                throw new IntrospectionException( "bad indexed read method arg count" ); // NOI18N
            }
            Parameter param = (Parameter) params.get(0);
            if ( !JMIUtils.isPrimitiveType(param.getType(), PrimitiveTypeKindEnum.INT) ) {
                throw new IntrospectionException( "not int index to indexed read method" ); // NOI18N
            }
            indexedType = indexedGetterMethod.getType();
            if ( JMIUtils.isPrimitiveType(indexedType, PrimitiveTypeKindEnum.VOID) ) {
                throw new IntrospectionException( "indexed read method return void" ); // NOI18N
            }
        }

        if (indexedSetterMethod != null ) {
            List/*<Parameter>*/ params = indexedSetterMethod.getParameters();
            if ( params.size() != 2 ) {
                throw new IntrospectionException( "bad indexed write method arg count" ); // NOI18N
            }
            Parameter param1 = (Parameter) params.get(0);
            if ( !JMIUtils.isPrimitiveType(param1.getType(), PrimitiveTypeKindEnum.INT) ) {
                throw new IntrospectionException( "non int index to indexed write method" ); // NOI18N
            }
            Parameter param2 = (Parameter) params.get(1);
            if (indexedType != null && !indexedType.equals( param2.getType() ) ) {
                throw new IntrospectionException(
                    "type mismatch between indexed read and write methods" ); // NOI18N
            }
            indexedType = param2.getType();
        }

        //type = indexedType;

        Type propType = getType();
        if ( propType != null &&
                (!(propType instanceof Array) || !indexedType.equals(((Array) propType).getType()))) {
            throw new IntrospectionException(
                "type mismatch between property type and indexed type" ); // NOI18N
        }
    }

    /** Based on names of indexedGetter and indexedSetter resolves the name
     * of the indexed property.
     * @return Name of the indexed property
     */ 
    String findIndexedPropertyName() throws JmiException {
        assert JMIUtils.isInsideTrans();
        String superName = findPropertyName();

        if ( superName == null ) {
            String methodName = null;

            if ( indexedGetterMethod != null )
                methodName = indexedGetterMethod.getName();
            else if ( indexedSetterMethod != null )
                methodName = indexedSetterMethod.getName();
            else
                throw new InternalError( "Indexed property with all methods == null" ); // NOI18N

            return methodName.startsWith( "is" ) ? // NOI18N
                   Introspector.decapitalize( methodName.substring(2) ) :
                   Introspector.decapitalize( methodName.substring(3) );
        }
        else
            return superName;
    }

    // METHODS FOR GENERATING AND DELETING METHODS AND FIELDS--------------------


    /** Generates non-indexed getter method without body and without Javadoc comment.
     * @throws GenerateBeanException if modification of source code is impossible.
     */
    void generateGetterMethod() throws GenerateBeanException, JmiException {
        if ( type != null )
            super.generateGetterMethod();
    }

    /** Generates non-indexed setter method without body and without Javadoc comment.
     * @throws GenerateBeanException If modification of source code is impossible.
     */
    void generateSetterMethod() throws GenerateBeanException, JmiException {
        if ( type != null )
            super.generateSetterMethod();
    }

    /** Generates indexed getter method without body and without Javadoc comment.
     * @throws GenerateBeanException If modification of source code is impossible.
     */
    void generateIndexedGetterMethod() throws GenerateBeanException, JmiException {
        generateIndexedGetterMethod( null, false );
    }

    /** Generates indexed getter method with body and optionaly with Javadoc comment.
     * @param body Body of the method
     * @param javadoc Generate Javadoc comment?
     * @throws GenerateBeanException If modification of source code is impossible.
     */
    void generateIndexedGetterMethod( String body, boolean javadoc ) throws GenerateBeanException, JmiException {
        assert JMIUtils.isInsideTrans();
        JavaClass declaringClass = getDeclaringClass();
        JavaModelPackage jmodel = JavaMetamodel.getManager().getJavaExtent(declaringClass);
        Method newGetter = jmodel.getMethod().createMethod();
        Parameter newParameter = jmodel.getParameter().createParameter();
        newParameter.setName("index"); // NOI18N
        newParameter.setType(jmodel.getType().resolve("int")); // NOI18N

        newGetter.setName( "get" + capitalizeFirstLetter( getName() ) ); // NOI18N
        newGetter.setType( indexedType );
        newGetter.setModifiers( Modifier.PUBLIC );
        newGetter.getParameters().add(newParameter);
        if ( declaringClass.isInterface() ) {
            newGetter.setBodyText( null );
        }
        else if ( body != null )
            newGetter.setBodyText( body );

        if ( javadoc ) {
            String comment = MessageFormat.format( PatternNode.getString( "COMMENT_IdxPropertyGetter" ),
                                                   new Object[] { getName() } );
            newGetter.setJavadocText( comment );
        }

        //System.out.println ("Generating getter" ); // NOI18N

        if ( declaringClass == null )
            throw new GenerateBeanException();
        else {
            //System.out.println ( "Adding getter method" ); // NOI18N
            declaringClass.getFeatures().add( newGetter );
            indexedGetterMethod = newGetter;
        }
    }

    /** Generates indexed setter method without body and without Javadoc comment.
     * @throws GenerateBeanException If modification of source code is impossible.
     */
    void generateIndexedSetterMethod() throws GenerateBeanException, JmiException {
        generateIndexedSetterMethod(null, false, false );
    }

    /** Generates indexed setter method with body and optionaly with Javadoc comment.
     * @param body Body of the method
     * @param javadoc Generate Javadoc comment?
     * @param constrained Is the property constrained?
     * @throws GenerateBeanException If modification of source code is impossible.
     */
    void generateIndexedSetterMethod( String body, boolean constrained, boolean javadoc ) throws GenerateBeanException, JmiException {
        assert JMIUtils.isInsideTrans();
        JavaClass declaringClass = getDeclaringClass();
        JavaModelPackage jmodel = JavaMetamodel.getManager().getJavaExtent(declaringClass);
        Method newSetter = jmodel.getMethod().createMethod();
        Parameter newParamIndex = jmodel.getParameter().createParameter();
        newParamIndex.setName("index"); // NOI18N
        newParamIndex.setType(jmodel.getType().resolve("int")); // NOI18N
        Parameter newParamValue = jmodel.getParameter().createParameter();
        newParamValue.setName(name);
        newParamValue.setType(indexedType);

        newSetter.setName( "set" + capitalizeFirstLetter( getName() ) ); // NOI18N
        newSetter.setType(jmodel.getType().resolve("void")); // NOI18N
        newSetter.setModifiers( Modifier.PUBLIC );
        List/*<Parameter>*/ params = newSetter.getParameters();
        params.add(newParamIndex);
        params.add(newParamValue);
        
        if ( constrained ) {
            MultipartId propVetoEx = jmodel.getMultipartId().
                    createMultipartId("java.beans.PropertyVetoException", null, null); // NOI18N
            if (propVetoEx == null) 
                throw new GenerateBeanException("cannot resolve java.beans.PropertyVetoException"); // NOI18N
            newSetter.getExceptionNames().add(propVetoEx);
        }
        if ( declaringClass.isInterface() ) {
            newSetter.setBodyText( null );
        }
        else if ( body != null ) {
            newSetter.setBodyText( body );
        }

        if ( javadoc ) {
            String comment = MessageFormat.format( PatternNode.getString( "COMMENT_IdxPropertySetter" ),
                                                   new Object[] { getName(), name } );
            if ( constrained )
                comment = comment + PatternNode.getString( "COMMENT_Tag_ThrowsPropertyVeto" );
            newSetter.setJavadocText( comment );
        }

        if ( declaringClass == null )
            throw new GenerateBeanException();
        else {
            declaringClass.getFeatures().add(newSetter);
            indexedSetterMethod = newSetter;
        }
    }


    /** Deletes the indexed getter method in source
     * @throws JmiException If modification of source code is impossible.
     */
    void deleteIndexedGetterMethod() throws JmiException {
        assert JMIUtils.isInsideTrans();
        if ( indexedGetterMethod == null )
            return;

        JavaClass declaringClass = getDeclaringClass();
        declaringClass.getFeatures().remove( indexedGetterMethod );
        indexedGetterMethod = null;
    }

    /** Deletes the indexed setter method in source
     * @throws JmiException If modification of source code is impossible.
     */
    void deleteIndexedSetterMethod() throws JmiException {
        assert JMIUtils.isInsideTrans();
        if ( indexedSetterMethod == null )
            return;

        JavaClass declaringClass = getDeclaringClass();
        declaringClass.getFeatures().remove( indexedSetterMethod );
        indexedSetterMethod = null;
    }

    // Property change support ----------------------------------

    /** Sets the properties to values of other indexed property pattern. If the
     * properties change fires PropertyChange event.
     * @param src Source IdxPropertyPattern it's properties will be copied.
     */
    void copyProperties( IdxPropertyPattern src ) throws JmiException {
        assert JMIUtils.isInsideTrans();
        boolean changed = !src.getIndexedType().equals( getIndexedType() ) ||
                          !( src.getType() == null ? getType() == null : src.getType().equals( getType() ) ) ||
                          !src.getName().equals( getName() ) ||
                          !(src.getMode() == getMode()) ||
                          !(src.getEstimatedField() == null ? estimatedField == null : src.getEstimatedField().equals( estimatedField ) );

        if ( src.getIndexedGetterMethod() != indexedGetterMethod )
            indexedGetterMethod = src.getIndexedGetterMethod();
        if ( src.getIndexedSetterMethod() != indexedSetterMethod )
            indexedSetterMethod = src.getIndexedSetterMethod();

        if ( src.getGetterMethod() != getterMethod ) {
            changed = true;
            getterMethod = src.getGetterMethod();
        }
        if ( src.getSetterMethod() != setterMethod ) {
            changed = true;
            setterMethod = src.getSetterMethod();
        }
        if ( src.getEstimatedField() != estimatedField )
            estimatedField = src.getEstimatedField();

        if ( changed ) {
            try {
                type = findPropertyType();
                findIndexedPropertyType();
            }
            catch ( java.beans.IntrospectionException e ) {
                // User's error
            }
            name = findIndexedPropertyName();

            // XXX cannot be fired inside mdr transaction; post to dedicated thread or redesigne somehow
            firePropertyChange( new java.beans.PropertyChangeEvent( this, null, null, null ) );
        }
    }

}
