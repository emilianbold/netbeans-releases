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
import java.text.MessageFormat;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;

import org.openide.DialogDisplayer;

import org.openide.nodes.Node;
import org.openide.NotifyDescriptor;
import org.openide.util.Utilities;
import org.netbeans.jmi.javamodel.*;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;

import javax.jmi.reflect.JmiException;

/** EventSetPattern: This class holds the information about used event set pattern
 * in code.
 * @author Petr Hrebejk
 *
 * 
 *  PENDING: Add Pattern class hierarchy (abstract classes || interfaces )
 */
public final class EventSetPattern extends Pattern {

    static final String[] WELL_KNOWN_LISTENERS =  new String[] {
                "java.awt.event.ActionListener", // NOI18N
                "java.awt.event.ContainerListener", // NOI18N
                "java.awt.event.FocusListener", // NOI18N
                "java.awt.event.ItemListener", // NOI18N
                "java.awt.event.KeyListener", // NOI18N
                "java.awt.event.MouseListener", // NOI18N
                "java.awt.event.MouseMotionListener", // NOI18N
                "java.awt.event.WindowListener", // NOI18N
                "java.beans.PropertyChangeListener", // NOI18N
                "java.beans.VetoableChangeListener", // NOI18N
                "javax.swing.event.CaretListener", // NOI18N
                "javax.swing.event.ChangeListener", // NOI18N
                "javax.swing.event.DocumentListener", // NOI18N
                "javax.swing.event.HyperlinkListener", // NOI18N
                "javax.swing.event.MenuListener", // NOI18N
                "javax.swing.event.MouseInputListener", // NOI18N
                "javax.swing.event.PopupMenuListener", // NOI18N
                "javax.swing.event.TableColumnModelListener", // NOI18N
                "javax.swing.event.TableModelListener", // NOI18N
                "javax.swing.event.TreeModelListener", // NOI18N
                "javax.swing.event.UndoableEditListener" // NOI18N
            };


    protected Method addListenerMethod = null;
    protected Method removeListenerMethod = null;

    private Type type;
    private boolean isUnicast = false;

    /** holds the decapitalized name */
    protected String name;

    /** Creates new PropertyPattern one of the methods may be null */
    public EventSetPattern( PatternAnalyser patternAnalyser,
                            Method addListenerMethod, Method removeListenerMethod ) {
        super( patternAnalyser );

        if ( addListenerMethod == null || removeListenerMethod == null  )
            throw new NullPointerException();

        this.addListenerMethod = addListenerMethod;
        this.removeListenerMethod = removeListenerMethod;

        isUnicast = testUnicast();
        findEventSetType();
        name = findEventSetName();
    }

    private EventSetPattern( PatternAnalyser patternAnalyser ) {
        super( patternAnalyser );
    }

    /** Creates new pattern from result of dialog */

    static EventSetPattern create( PatternAnalyser patternAnalyser,
                                   String type,
                                   int implementation,
                                   boolean fire,
                                   boolean passEvent,
                                   boolean isUnicast ) {
        
        assert JMIUtils.isInsideTrans();
        EventSetPattern esp = new EventSetPattern( patternAnalyser );
        
        esp.type = patternAnalyser.findType(type);

        if ( esp.type == null || !(esp.type instanceof JavaClass)) {
            return null;
        }

        //System.out.println( "Type " + esp.type.toString() ); // NOI18N


        esp.name = Introspector.decapitalize( ((JavaClass) esp.type).getSimpleName() );
        esp.isUnicast = isUnicast;

        String listenerList = null;

        if ( implementation == 1 ) {
            if ( isUnicast )
                BeanPatternGenerator.unicastListenerField( esp.getDeclaringClass(), esp.type );
            else
                BeanPatternGenerator.listenersArrayListField( esp.getDeclaringClass(), esp.type );
        }
        else if ( implementation == 2 && !isUnicast ) {
            listenerList = BeanPatternGenerator.eventListenerListField( esp.getDeclaringClass(), esp.type );
        }


        if ( isUnicast ) {
            esp.generateAddListenerMethod( BeanPatternGenerator.ucAddBody( esp.type, implementation ), true );
            esp.generateRemoveListenerMethod( BeanPatternGenerator.ucRemoveBody( esp.type, implementation ), true );
        }
        else {
            esp.generateAddListenerMethod( BeanPatternGenerator.mcAddBody( esp.type, implementation, listenerList ), true );
            esp.generateRemoveListenerMethod( BeanPatternGenerator.mcRemoveBody( esp.type, implementation, listenerList ), true );
        }

        if ( fire ) {
            JavaClass listener = (JavaClass) esp.type;

            List/*<Method>*/ methods = JMIUtils.getMethods(listener);
            boolean isInterface = listener.isInterface();
            for (Iterator it = methods.iterator(); it.hasNext();) {
                Method method = (Method) it.next();
                if ( ((method.getModifiers() & Modifier.PUBLIC) != 0 ) ||
                     (isInterface && (method.getModifiers() & (Modifier.PROTECTED | Modifier.PRIVATE)) == 0)
                   ) {
                    if ( isUnicast )
                        BeanPatternGenerator.unicastFireMethod( esp.getDeclaringClass(), esp.type,
                                                                method, implementation, passEvent );
                    else
                        BeanPatternGenerator.fireMethod( esp.getDeclaringClass(), esp.type,
                                                         method, implementation, listenerList, passEvent );
                }
            }
        }


        return esp;
    }

    /** Gets the name of PropertyPattern */
    public String getName() {
        return name;
    }

    /** Sets the name of PropertyPattern */
    public void setName( String name ) throws IllegalArgumentException, JmiException {
        if ( !Utilities.isJavaIdentifier( name ) || name.indexOf( "Listener" ) <= 0 ) // NOI18N
            throw new IllegalArgumentException( "Invalid event source name" ); // NOI18N

        name = capitalizeFirstLetter( name );

        String addMethodID = "add" + name; // NOI18N
        String removeMethodID = "remove" + name; // NOI18N

        JMIUtils.beginTrans(true);
        boolean rollback = true;
        try {
            if (addListenerMethod.isValid() && removeListenerMethod.isValid()) {
                addListenerMethod.setName( addMethodID );
                removeListenerMethod.setName( removeMethodID );
                this.name = Introspector.decapitalize( name );
            }
            rollback = false;
        } finally {
            JMIUtils.endTrans(rollback);
        }
    }

    /** Test if the name is valid for given pattern */
    protected static boolean isValidName( String str ) {
        if ( Utilities.isJavaIdentifier(str) == false )
            return false;

        if (str.indexOf( "Listener" ) <= 0 ) // NOI18N
            return false;

        return true;
    }

    /** Returns the mode of the property READ_WRITE, READ_ONLY or WRITE_ONLY */
    public boolean isUnicast() {
        return isUnicast;
    }

    /** Sets the property to be unicast or multicast */
    public void setIsUnicast( boolean b ) throws JmiException {
        if ( b == isUnicast) {
            return;
        }
        
        JMIUtils.beginTrans(true);
        boolean rollback = true;
        try {
            if (!addListenerMethod.isValid()) {
                return; 
            }
            List/*<MultipartId>*/ exs = addListenerMethod.getExceptionNames();

            if (b) {
                JavaModelPackage jmodel = JavaMetamodel.getManager().getJavaExtent(addListenerMethod);
                MultipartId tooManyId = jmodel.getMultipartId().
                        createMultipartId("java.util.TooManyListenersException", null, null); // NOI18N
                exs.add(tooManyId);
            } else {
                JavaClass tooMany = patternAnalyser.findClassElement("java.util.TooManyListenersException"); // NOI18N
                assert tooMany != null;
                List/*<MultipartId>*/ remove = new LinkedList/*<MultipartId>*/();
                for (Iterator it = exs.iterator(); it.hasNext();) {
                    MultipartId exId = (MultipartId) it.next();
                    JavaClass ex = (JavaClass) exId.getElement();
                    if (tooMany.isSubTypeOf(ex)) {
                        remove.add(exId);
                    }
                }
                exs.removeAll(remove);
            }
            
            this.isUnicast = b;
            rollback = false;
        } finally {
            JMIUtils.endTrans(rollback);
        }
    }

    /** Returns the getter method */
    public Method getAddListenerMethod() {
        return addListenerMethod;
    }

    /** Returns the setter method */
    public Method getRemoveListenerMethod() {
        return removeListenerMethod;
    }

    /** Gets the type of property */
    public Type getType() {
        return type;
    }

    /** Sets the type of property */
    public void setType( Type newType ) throws JmiException {
        int state = 0;
        JMIUtils.beginTrans(true);
        boolean rollback = true;
        try {
            if (this.type.equals(newType) || !newType.isValid())
                return;

            if (!(newType instanceof JavaClass) ||
                    !PatternAnalyser.isSubclass((JavaClass) newType,
                            patternAnalyser.findClassElement("java.util.EventListener"))) { // NOI18N
                
                state = 1;
            } else {
                String newTypeName = ((JavaClass) newType).getSimpleName();
                List/*<Parameter>*/  params = addListenerMethod.getParameters();
                params.clear();
                params.add(newType);

                params = removeListenerMethod.getParameters();
                params.clear();
                params.add(newType);

                // Ask if we have to change the bame of the methods
                String msg = MessageFormat.format(PatternNode.getString("FMT_ChangeEventSourceName"), // NOI18N
                        new Object[]{capitalizeFirstLetter(newTypeName)});
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_OPTION);
                if (DialogDisplayer.getDefault().notify(nd).equals(NotifyDescriptor.YES_OPTION)) {
                    setName(newTypeName);
                }

                this.type = newType;
            }

            rollback = false;
        } finally {
            JMIUtils.endTrans(rollback);
        }
        switch(state) {
            case 1:
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(PatternNode.getString("MSG_InvalidListenerInterface"), // NOI18N
                        NotifyDescriptor.ERROR_MESSAGE));
                break;
                
        }
    }

    /** Gets the cookie of the first available method */
    public Node.Cookie getCookie( Class cookieType ) {
        return super.getCookie(cookieType);
    }

    public void destroy() throws JmiException {
        assert JMIUtils.isInsideTrans();
        if ( addListenerMethod != null && addListenerMethod.isValid() ) {
            addListenerMethod.refDelete();
        }

        if ( removeListenerMethod != null && removeListenerMethod.isValid() ) {
            removeListenerMethod.refDelete();
        }
        
        //** BOB - Matula
        
        // delete associated "fire" methods
        JavaClass declaringClass = getDeclaringClass();
        JavaClass listener = (JavaClass) type;
        boolean canDelete = false;

        if ( listener != null ) {
            List methods = JMIUtils.getMethods(listener);
            List sourceMethods = JMIUtils.getMethods(declaringClass);
            String method;
            String typeName = listener.getSimpleName();
            
            for (Iterator it = methods.iterator(); it.hasNext();) {
                Method lsnrMethod = (Method) it.next();
                method = "fire" + // NOI18N
                        Pattern.capitalizeFirstLetter(typeName) +
                        Pattern.capitalizeFirstLetter(lsnrMethod.getName());
                if (Modifier.isPublic(lsnrMethod.getModifiers())) {
                    for (Iterator it2 = sourceMethods.iterator(); it2.hasNext();) {
                        Method srcMethod = (Method) it2.next();
                        if (srcMethod.getName().equals(method)) {
                            if (!canDelete) {
                                // Ask, if the fire methods can be deleted
                                String mssg = MessageFormat.format( PatternNode.getString( "FMT_DeleteFire" ),
                                                                    new Object[0] );
                                NotifyDescriptor nd = new NotifyDescriptor.Confirmation ( mssg, NotifyDescriptor.YES_NO_OPTION );
                                if ( DialogDisplayer.getDefault().notify( nd ).equals( NotifyDescriptor.NO_OPTION ) ) {
                                    return;
                                } else {
                                    canDelete = true;
                                }
                            }
                            srcMethod.refDelete();
                        }
                    }
                }
            }
        }
        //** EOB - Matula
    }

    // Utility methods --------------------------------------------------------------------

    /*
    * Package-private constructor
    * Merge two event set descriptors.  Where they conflict, give the
    * second argument (y) priority over the first argument (x).
    *
    * @param x  The first (lower priority) EventSetDescriptor
    * @param y  The second (higher priority) EventSetDescriptor
    */

    EventSetPattern( EventSetPattern x, EventSetPattern y) {
        super( y.patternAnalyser );
        //super(x,y);

        /*
        listenerMethodDescriptors = x.listenerMethodDescriptors;
        if (y.listenerMethodDescriptors != null) {
         listenerMethodDescriptors = y.listenerMethodDescriptors;
    }
        if (listenerMethodDescriptors == null) {
         listenerMethods = y.listenerMethods;
    }
        */
        addListenerMethod = y.addListenerMethod;
        removeListenerMethod = y.removeListenerMethod;
        isUnicast = y.isUnicast;
        type = y.type;
        name = y.name;

        /*
        if (!x.inDefaultEventSet || !y.inDefaultEventSet) {
         inDefaultEventSet = false;
    }
        */
    }


    /** Finds the Type of property.
     */
    private void findEventSetType() {
        assert JMIUtils.isInsideTrans();
        type = ((Parameter) addListenerMethod.getParameters().get(0)).getType();
    }

    /** Decides about the name of the event set from names of the methods */
    private String findEventSetName() {
        assert JMIUtils.isInsideTrans();
        String compound = addListenerMethod.getName().substring(3);
        return name = Introspector.decapitalize( compound );
    }


    /** Test if this EventSet pattern is unicast */
    private boolean testUnicast() {
        if (findTooManyListenersException() != null)
            return true;
        else
            return false;
    }

    /** @return The identifier for java.util.TooManyListenersException if the addListener
     * method throws it or null if not. 
     */
    JavaClass findTooManyListenersException() {
        assert JMIUtils.isInsideTrans();
        JavaModelPackage model = JavaMetamodel.getManager().getJavaExtent(this.addListenerMethod);
        Type t = model.getType().resolve("java.util.TooManyListenersException"); // NOI18N
        if (t instanceof UnresolvedClass || !(t instanceof JavaClass)) {
            return null;
        }
        JavaClass tooMany = (JavaClass) t;

        List/*<JavaClass>*/ exs = addListenerMethod.getExceptions();
        for (Iterator it = exs.iterator(); it.hasNext();) {
            JavaClass ex = (JavaClass) it.next();
            if (ex.isSubTypeOf(tooMany)) {
                return ex;
            }
        }

        return null;
    }

    void generateAddListenerMethod ( String body, boolean javadoc ) throws JmiException {
        assert JMIUtils.isInsideTrans();
        JavaClass declaringClass = getDeclaringClass();
        if ( declaringClass == null )
            throw new IllegalStateException("Missing declaring class"); // NOI18N
        
        JavaModelPackage jmodel = JavaMetamodel.getManager().getJavaExtent(declaringClass);
        Method newMethod = jmodel.getMethod().createMethod();
        int modifiers = Modifier.PUBLIC | Modifier.SYNCHRONIZED;
        Parameter newParameter = jmodel.getParameter().createParameter();
        newParameter.setName("listener"); // NOI18N
        newParameter.setType(type);

        newMethod.setName( "add" + capitalizeFirstLetter( getName() ) ); // NOI18N
        newMethod.setTypeName(jmodel.getMultipartId().createMultipartId("void", null, null)); // NOI18N
        List/*<Parameter>*/ params = newMethod.getParameters();
        params.add(newParameter);

        if ( declaringClass.isInterface() ) {
            modifiers &= ~Modifier.SYNCHRONIZED;    // synchronized modifier is not allowed in interface
        } else if ( body != null )
            newMethod.setBodyText( body );
        newMethod.setModifiers( modifiers );
        if ( isUnicast ) {
            MultipartId tooManyLsnrs = jmodel.getMultipartId().
                    createMultipartId("java.util.TooManyListenersException", null, null); // NOI18N
            newMethod.getExceptionNames().add(tooManyLsnrs);
        }
        if ( javadoc ) {
            String comment = MessageFormat.format( PatternNode.getString( "COMMENT_AddListenerMethod" ),
                    new Object[] { ((JavaClass) type).getSimpleName() } );
            newMethod.setJavadocText( comment );
        }

        declaringClass.getContents().add(newMethod);
        addListenerMethod = newMethod;
    }

    void generateRemoveListenerMethod( String body, boolean javadoc ) throws JmiException {
        assert JMIUtils.isInsideTrans();
        JavaClass declaringClass = getDeclaringClass();
        if ( declaringClass == null )
            throw new IllegalStateException("Missing declaring class"); // NOI18N
        
        JavaModelPackage jmodel = JavaMetamodel.getManager().getJavaExtent(declaringClass);
        Method newMethod = jmodel.getMethod().createMethod();
        int modifiers = Modifier.PUBLIC | Modifier.SYNCHRONIZED;
        Parameter newParameter = jmodel.getParameter().createParameter();
        newParameter.setName("listener"); // NOI18N
        newParameter.setType(type);

        newMethod.setName( "remove" + capitalizeFirstLetter( getName() ) ); // NOI18N
        newMethod.setTypeName(jmodel.getMultipartId().createMultipartId("void", null, null)); // NOI18N
        List/*<Parameter>*/ params = newMethod.getParameters();
        params.add(newParameter);
        
        if ( declaringClass.isInterface() ) {
            modifiers &= ~Modifier.SYNCHRONIZED;    // synchronized modifier is not allowed in interface
        } else if ( body != null )
            newMethod.setBodyText( body );
        newMethod.setModifiers( modifiers );
        if ( javadoc ) {
            String comment = MessageFormat.format( PatternNode.getString( "COMMENT_RemoveListenerMethod" ),
                    new Object[] { ((JavaClass) type).getSimpleName() } );
            newMethod.setJavadocText( comment );
        }

        declaringClass.getContents().add(newMethod);
        removeListenerMethod = newMethod;
    }

    // Property change support -------------------------------------------------------------------------

    void copyProperties( EventSetPattern src ) throws JmiException {
        assert JMIUtils.isInsideTrans();
        boolean changed = !src.getType().equals( getType() ) ||
                          !src.getName().equals( getName() ) ||
                          !(src.isUnicast() == isUnicast());

        if ( src.getAddListenerMethod() != addListenerMethod )
            addListenerMethod = src.getAddListenerMethod();
        if ( src.getRemoveListenerMethod() != removeListenerMethod )
            removeListenerMethod = src.getRemoveListenerMethod();

        if ( changed ) {

            isUnicast = testUnicast();

            findEventSetType();
            isUnicast = testUnicast();
            name = findEventSetName();
            
            // XXX cannot be fired inside mdr transaction; post to dedicated thread or redesigne somehow
            firePropertyChange( new java.beans.PropertyChangeEvent( this, null, null, null ) );
        }

    }

}
