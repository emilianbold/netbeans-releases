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

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Enumeration;
import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.openide.nodes.Node;
import org.openide.src.ClassElement;
import org.openide.src.MethodElement;
import org.openide.src.FieldElement;
import org.openide.src.MethodParameter;
import org.openide.src.Type;
import org.openide.src.Identifier;

/** Analyses the ClassElement trying to find source code patterns i.e.
 * properties or event sets;
 *
 * @author Petr Hrebejk
 */

public class PatternAnalyser extends Object implements Node.Cookie {

    private static final int    PROPERTIES_RESERVE = 11;
    private static final String GET_PREFIX = "get"; // NOI18N
    private static final String SET_PREFIX = "set"; // NOI18N
    private static final String IS_PREFIX = "is"; // NOI18N
    private static final String ADD_PREFIX = "add"; // NOI18N
    private static final String REMOVE_PREFIX = "remove"; // NOI18N

    /* Collections which are returned by getters/setters */
    private ArrayList currentPropertyPatterns = new ArrayList();
    private ArrayList currentIdxPropertyPatterns = new ArrayList();
    private ArrayList currentEventSetPatterns =  new ArrayList();

    /* Temporary collections used for analysing */
    private HashMap propertyPatterns;
    private HashMap idxPropertyPatterns;
    private HashMap eventSetPatterns;

    private ClassElement classElement;

    private boolean ignore;

    /** Creates new analyser for ClassElement
     */
    public PatternAnalyser( ClassElement classElement ) {
        this.classElement = classElement;
    }

    public void analyzeAll() {

        if ( ignore ) {
            return;
        }

        int methodCount = classElement.getMethods().length;
        propertyPatterns = new HashMap( methodCount / 2 + PROPERTIES_RESERVE );
        idxPropertyPatterns = new HashMap();        // Initial size 11
        eventSetPatterns = new HashMap();           // Initial size 11

        // Analyse patterns

        resolveMethods();
        resolveFields();

        // Compare old and new patterns to resolve changes
        resolveChangesOfProperties();
        resolveChangesOfIdxProperties();
        resolveChangesOfEventSets();
    }

    void setIgnore( boolean ignore ) {
        this.ignore = ignore;
    }

    /*
    void resolvePropertyChanges( ) {
      HashMap oldPropertyPatterns = new HashMap( propertyPatterns );
      HashMap newPropertyPatterns = new HashMap();  
      
      // All compare levels
      for( int level = 0; true; level++ ) {
        // Go through 
      }
      
}
    */

    public Collection getPropertyPatterns() {
        return currentPropertyPatterns;
    }

    public Collection getIdxPropertyPatterns() {
        return currentIdxPropertyPatterns;
    }

    public Collection getEventSetPatterns() {
        return currentEventSetPatterns;
    }

    /** Gets the classelemnt of this pattern analyser */
    public ClassElement getClassElement() {
        return classElement;
    }

    /** This method analyses the ClassElement for "property patterns".
    * The method is analogous to JavaBean Introspector methods for classes
    * without a BeanInfo.
    */
    public void resolveMethods() {

        // First get all methods in classElement
        MethodElement[] methods = classElement.getMethods();

        // Temporary structures for analysing EventSets
        Hashtable adds = new Hashtable();
        Hashtable removes = new Hashtable();

        // Analyze each method
        for ( int i = 0; i < methods.length ; i++ ) {
            MethodElement method = methods[i];

            String name = method.getName().getName();

            if ( name.startsWith( GET_PREFIX ) || name.startsWith( SET_PREFIX ) || name.startsWith( IS_PREFIX ) ) {
                PropertyPattern pp = analyseMethodForProperties( method );
                if ( pp != null )
                    addProperty( pp );
            }
            if ( name.startsWith( ADD_PREFIX ) || name.startsWith( REMOVE_PREFIX ) )  {
                analyseMethodForEventSets( method, adds, removes );
            }
        }
        // Resolve the temporay structures of event sets

        // Now look for matching addFooListener+removeFooListener pairs.
        Enumeration keys = adds.keys();

        while (keys.hasMoreElements()) {
            String compound = (String) keys.nextElement();
            // Skip any "add" which doesn't have a matching remove // NOI18N
            if (removes.get (compound) == null ) {
                continue;
            }
            // Method name has to end in Listener
            if (compound.indexOf( "Listener:" ) <= 0 ) { // NOI18N
                continue;
            }

            /*
            String listenerName = compound.substring( 0, compound.indexOf( ':' ) );
            String eventName = Introspector.decapitalize( listenerName.substring( 0, listenerName.length() - 8 ));
            */
            MethodElement addMethod = (MethodElement)adds.get(compound);
            MethodElement removeMethod = (MethodElement)removes.get(compound);
            Type argType = addMethod.getParameters()[0].getType();

            // Check if the argument is a subtype of EventListener
            //try {
            //if (!Introspector.isSubclass( argType.toClass(), java.util.EventListener.class ) ) {
            //if (!java.util.EventListener.class.isAssignableFrom( argType.toClass() ) ) {
            if ( !isSubclass(
                        ClassElement.forName( argType.getClassName().getFullName() ),
                        ClassElement.forName( "java.util.EventListener" ) ) ) // NOI18N
                continue;
            /*
              }
        }
            catch ( java.lang.ClassNotFoundException ex ) {
              continue;
        }
            */

            EventSetPattern esp;

            try {
                esp = new EventSetPattern( this, addMethod, removeMethod );
            }
            catch ( IntrospectionException ex ) {
                esp = null;
            }

            if (esp != null)
                addEventSet( esp );
        }
    }

    void resolveFields() {
        // Analyze fields
        FieldElement fields[] = classElement.getFields();

        for ( int i = 0; i < fields.length; i++ ) {
            FieldElement field=fields[i];

            if ( ( field.getModifiers() & Modifier.STATIC ) != 0 )
                continue;

            PropertyPattern pp = (PropertyPattern)propertyPatterns.get( field.getName().getName() );
            if ( pp == null )
                pp = (PropertyPattern)idxPropertyPatterns.get( field.getName().getName() );
            if ( pp == null )
                continue;
            Type ppType = pp.getType();
            if ( ppType != null && pp.getType().compareTo( field.getType(), false ) )
                pp.setEstimatedField( field );
        }
    }

    private void resolveChangesOfProperties( ) {
        currentPropertyPatterns = resolveChanges( currentPropertyPatterns, propertyPatterns, LevelComparator.PROPERTY );
    }

    private void resolveChangesOfIdxProperties( ) {
        currentIdxPropertyPatterns = resolveChanges( currentIdxPropertyPatterns, idxPropertyPatterns, LevelComparator.IDX_PROPERTY );
    }

    private void resolveChangesOfEventSets() {
        currentEventSetPatterns = resolveChanges( currentEventSetPatterns, eventSetPatterns, LevelComparator.EVENT_SET );
    }


    static ArrayList resolveChanges( Collection current, Map created, LevelComparator comparator ) {
        ArrayList old = new ArrayList( current );
        ArrayList cre = new ArrayList( created.size() );
        cre.addAll( created.values() );
        ArrayList result = new ArrayList( created.size() + 5 );


        for ( int level = 0; level <= comparator.getLevels(); level ++ ) {
            Iterator itCre = cre.iterator();
            while ( itCre.hasNext() ) {
                Pattern crePattern = (Pattern) itCre.next();
                Iterator itOld = old.iterator();
                while ( itOld.hasNext() ) {
                    Pattern oldPattern = (Pattern) itOld.next();
                    if ( comparator.compare( level, oldPattern, crePattern ) ) {
                        itOld.remove( );
                        itCre.remove( );
                        comparator.copyProperties(oldPattern, crePattern );
                        result.add( oldPattern );
                        break;
                    }
                }
            }
        }
        result.addAll( cre );
        return result;
    }

    /** Analyses one method for property charcteristics */

    PropertyPattern analyseMethodForProperties( MethodElement method ) {
        // Skip static methods as Introspector does.
        int modifiers = method.getModifiers();
        if ( Modifier.isStatic( modifiers ) )
            return null;

        String name = method.getName().getName();
        MethodParameter[] params = method.getParameters();
        int paramCount = params == null ? 0 : params.length;
        Type returnType = method.getReturn();

        PropertyPattern pp = null;

        try {
            if ( paramCount == 0 ) {
                if (name.startsWith( GET_PREFIX )) {
                    // SimpleGetter
                    pp = new PropertyPattern( this, method, null);
                }
                else if ( returnType.compareTo( Type.BOOLEAN, false ) && name.startsWith( IS_PREFIX )) {
                    // Boolean getter
                    pp = new PropertyPattern( this, method, null );
                }
            }
            else if ( paramCount == 1 ) {
                if ( params[0].getType().compareTo( Type.INT, false ) && name.startsWith( GET_PREFIX )) {
                    pp = new IdxPropertyPattern( this, null, null, method, null );
                }
                else if ( returnType.compareTo( Type.VOID, false ) && name.startsWith( SET_PREFIX )) {
                    pp = new PropertyPattern( this, null, method );
                    // PENDING vetoable => constrained
                }
            }
            else if ( paramCount == 2 ) {
                if ( params[0].getType().compareTo( Type.INT, false ) && name.startsWith( SET_PREFIX )) {
                    pp = new IdxPropertyPattern( this, null, null, null, method );
                    // PENDING vetoable => constrained
                }
            }
        }
        catch (IntrospectionException ex) {
            // PropertyPattern constructor found some differencies from design patterns.
            pp = null;
        }

        return pp;
    }

    /** Method analyses cass methods for EventSetPatterns
     */
    void analyseMethodForEventSets( MethodElement method, Map adds, Map removes ) {
        // Skip static methods
        int modifiers = method.getModifiers();
        if ( Modifier.isStatic( modifiers ) )
            return;

        String name = method.getName().getName();
        MethodParameter params[] = method.getParameters();
        Type returnType = method.getReturn();

        if ( name.startsWith( ADD_PREFIX ) && params.length == 1 && returnType == Type.VOID ) {
            String compound = name.substring(3) + ":" + params[0].getType(); // NOI18N
            adds.put( compound, method );
        }
        else if ( name.startsWith( REMOVE_PREFIX ) && params.length == 1 && returnType == Type.VOID ) {
            String compound = name.substring(6) + ":" + params[0].getType(); // NOI18N
            removes.put( compound, method );
        }

    }
    // Utility methods --------------------------------------------------------------------

    /** Adds the new property. Or generates composite property if property
     *  of that name already exists. It puts the property in the right HashMep
     * according to type of property idx || not idx
     */

    private void addProperty( PropertyPattern pp ) {
        boolean isIndexed = pp instanceof IdxPropertyPattern;
        HashMap hm = isIndexed ? idxPropertyPatterns : propertyPatterns;
        String name = pp.getName();

        PropertyPattern old = (PropertyPattern)propertyPatterns.get(name);
        if ( old == null )
            old = (PropertyPattern)idxPropertyPatterns.get(name);

        if (old == null) {  // There is no other property of that name
            hm.put(name, pp);
            return;
        }

        // If the property type has changed, use new property pattern
        Type opt = old.getType();
        Type npt = pp.getType();
        if (  opt != null && npt != null && !opt.compareTo(npt, false) ) {
            hm.put( name, pp );
            return;
        }

        PropertyPattern composite;
        boolean isOldIndexed = old instanceof IdxPropertyPattern;

        if  (isIndexed || isOldIndexed ) {
            if ( isIndexed && !isOldIndexed ) {
                propertyPatterns.remove( old.getName() ); // Remove old from not indexed
            }
            else if ( !isIndexed && isOldIndexed ) {
                idxPropertyPatterns.remove( old.getName() ); // Remove old from indexed
            }
            composite = new IdxPropertyPattern( old, pp );
            idxPropertyPatterns.put( name, composite );
        }
        else {
            composite = new PropertyPattern( old, pp );
            propertyPatterns.put( name, composite );
        }

        // PENDING : Resolve types of getters and setters to pair correctly
        // methods with equalNames.
        /*
        MethodElement getter = pp.getGetterMethod() == null ?
          old.getGetterMethod() : pp.getGetterMethod();
        MethodElement setter = pp.getSetterMethod() == null ?
          old.getSetterMethod() : pp.getSetterMethod();

        PropertyPattern composite = isIndexed ?
          new IdxPropertyPattern ( getter, setter ) :
          new PropertyPattern( getter, setter );
        hm.put( pp.getName(), composite );
        */

    }

    /** adds an eventSetPattern */

    void addEventSet( EventSetPattern esp ) {
        String key = esp.getName() + esp.getType();
        EventSetPattern old = (EventSetPattern)eventSetPatterns.get( key );


        if ( old == null ) {
            eventSetPatterns.put( key, esp);
            return;
        }

        EventSetPattern composite = new EventSetPattern( old, esp );
        eventSetPatterns.put( key, composite );
    }

    static boolean isSubclass(ClassElement a, ClassElement b) {

        if (a == null || b == null) {
            return false;
        }

        if (a.getName().compareTo( b.getName(), false ) ) {
            return true;
        }

        for ( ClassElement x = a;
                x != null;
                x = x.getSuperclass() == null ? null : ClassElement.forName( x.getSuperclass().getFullName() )  ){
            if (x.getName().compareTo( b.getName(), false ) ) {
                return true;
            }
            if (b.isInterface()) {
                Identifier interfaces[] = x.getInterfaces();
                for (int i = 0; i < interfaces.length; i++) {
                    ClassElement interfaceElement = ClassElement.forName( interfaces[i].getFullName() );
                    if (isSubclass(interfaceElement, b)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Inner Classes --- comparators for patterns -------------------------------------------------


    abstract static class LevelComparator {

        abstract boolean compare( int level, Pattern p1, Pattern p2 );
        abstract int getLevels();
        abstract void copyProperties( Pattern p1, Pattern p2 );

        static LevelComparator PROPERTY = new LevelComparator.Property();
        static LevelComparator IDX_PROPERTY = new LevelComparator.IdxProperty();
        static LevelComparator EVENT_SET = new LevelComparator.EventSet();

        static class Property extends LevelComparator {

            boolean compare( int level, Pattern p1, Pattern p2 ) {

                switch ( level ) {
                case 0:
                    return ((PropertyPattern)p1).getGetterMethod() == ((PropertyPattern)p2).getGetterMethod() &&
                           ((PropertyPattern)p1).getSetterMethod() == ((PropertyPattern)p2).getSetterMethod() ;
                case 1:
                    return ((PropertyPattern)p1).getGetterMethod() == ((PropertyPattern)p2).getGetterMethod();
                case 2:
                    return ((PropertyPattern)p1).getSetterMethod() == ((PropertyPattern)p2).getSetterMethod();
                default:
                    return false;
                }
            }

            int getLevels() {
                return 2;
            }

            void copyProperties( Pattern p1, Pattern p2 ) {
                ((PropertyPattern) p1).copyProperties( (PropertyPattern)p2 );
            }
        }

        static class IdxProperty extends LevelComparator {

            boolean compare( int level, Pattern p1, Pattern p2 ) {

                switch ( level ) {
                case 0:
                    return ((IdxPropertyPattern)p1).getIndexedGetterMethod() == ((IdxPropertyPattern)p2).getIndexedGetterMethod() &&
                           ((IdxPropertyPattern)p1).getIndexedSetterMethod() == ((IdxPropertyPattern)p2).getIndexedSetterMethod() ;
                case 1:
                    return ((IdxPropertyPattern)p1).getIndexedGetterMethod() == ((IdxPropertyPattern)p2).getIndexedGetterMethod();
                case 2:
                    return ((IdxPropertyPattern)p1).getIndexedSetterMethod() == ((IdxPropertyPattern)p2).getIndexedSetterMethod();
                default:
                    return false;
                }
            }

            int getLevels() {
                return 2;
            }

            void copyProperties( Pattern p1, Pattern p2 ) {
                ((IdxPropertyPattern) p1).copyProperties( (IdxPropertyPattern)p2 );
            }
        }

        static class EventSet extends LevelComparator {

            boolean compare( int level, Pattern p1, Pattern p2 ) {

                switch ( level ) {
                case 0:
                    return ((EventSetPattern)p1).getAddListenerMethod() == ((EventSetPattern)p2).getAddListenerMethod() ||
                           ((EventSetPattern)p1).getRemoveListenerMethod() == ((EventSetPattern)p2).getRemoveListenerMethod() ;
                    /*
                    case 1:  
                      return ((EventSetPattern)p1).getAddListenerMethod() == ((EventSetPattern)p2).getAddListenerMethod();
                    case 2: 
                      return ((EventSetPattern)p1).getRemoveListenerMethod() == ((EventSetPattern)p2).getRemoveListenerMethod();
                    */
                default:
                    return false;
                }
            }

            int getLevels() {
                return 0;
            }

            void copyProperties( Pattern p1, Pattern p2 ) {
                ((EventSetPattern) p1).copyProperties( (EventSetPattern)p2 );
            }
        }
    }
}

/*
 * Log
 *  10   Gandalf   1.9         1/13/00  Petr Hrebejk    i18n mk3
 *  9    Gandalf   1.8         1/12/00  Petr Hrebejk    i18n  
 *  8    Gandalf   1.7         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         8/2/99   Petr Hrebejk    EventSetNode chilfren & 
 *       EventSets types with src. code fixed
 *  6    Gandalf   1.5         7/29/99  Petr Hrebejk    Fix - change 
 *       ReadOnly/WriteOnly to ReadWrite mode diddn't registered the added 
 *       methods properly
 *  5    Gandalf   1.4         7/26/99  Petr Hrebejk    Better implementation of
 *       patterns resolving
 *  4    Gandalf   1.3         7/21/99  Petr Hrebejk    Field and Method 
 *       listeners moved to PatternChildren
 *  3    Gandalf   1.2         7/20/99  Ian Formanek    compilable version
 *  2    Gandalf   1.1         7/9/99   Petr Hrebejk    Factory chaining fix
 *  1    Gandalf   1.0         6/28/99  Petr Hrebejk    
 * $ 
 */ 