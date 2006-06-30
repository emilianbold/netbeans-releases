/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.beans;

import java.lang.reflect.Modifier;
import java.util.*;
import java.beans.IntrospectionException;

import org.openide.nodes.Node;
import org.openide.filesystems.FileObject;
import org.openide.ErrorManager;
import org.netbeans.jmi.javamodel.*;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;

import javax.jmi.reflect.JmiException;

/** Analyses the ClassElement trying to find source code patterns i.e.
 * properties or event sets;
 *
 * @author Petr Hrebejk
 */

public final class PatternAnalyser extends Object implements Node.Cookie {

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

    private JavaClass classElement;
    
    private JavaClass referenceClassElement;
    
    private boolean analyzed = false;

    private boolean ignore;

    /** Creates new analyser for ClassElement
     */
    public PatternAnalyser( JavaClass classElement ) {
        this.classElement = classElement;
    }

    /** Contructor for ClassElements which do not exists on the disk.
     * @param referenceClassElement some ClassElements which contains the 
     *        dataObjectCookie.
     */
    public PatternAnalyser( JavaClass classElement, JavaClass referenceClassElement ) {
        this( classElement );
        this.referenceClassElement = referenceClassElement;   
    }
    
    public synchronized void analyzeAll() {

        if ( ignore ) {
            return;
        }
        
        analyzed = true;

        propertyPatterns = new HashMap( PROPERTIES_RESERVE );
        idxPropertyPatterns = new HashMap();        // Initial size 11
        eventSetPatterns = new HashMap();           // Initial size 11

        // Analyse patterns

        try {
            JMIUtils.beginTrans(false);
            try {
                if (!classElement.isValid()) {
                    return;
                }
                resolveMethods();
                resolveFields();

                // Compare old and new patterns to resolve changes
                resolveChangesOfProperties();
                resolveChangesOfIdxProperties();
                resolveChangesOfEventSets();
            } finally {
                JMIUtils.endTrans();
            }
        } catch (JmiException e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
        }
    }
    
    boolean isAnalyzed() {
        return analyzed;
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

    public Collection/*<PropertyPattern>*/ getPropertyPatterns() {
        return currentPropertyPatterns;
    }

    public Collection/*<IdxPropertyPattern>*/ getIdxPropertyPatterns() {
        return currentIdxPropertyPatterns;
    }

    public Collection/*<EventSetPattern>*/ getEventSetPatterns() {
        return currentEventSetPatterns;
    }

    /** Gets the classelemnt of this pattern analyser */
    public JavaClass getClassElement() {
        return classElement;
    }

    /** This method analyses the ClassElement for "property patterns".
    * The method is analogous to JavaBean Introspector methods for classes
    * without a BeanInfo.
    */
    private void resolveMethods() throws JmiException {

        assert JMIUtils.isInsideTrans();
        // First get all methods in classElement
        List/*<Method>*/ methods = JMIUtils.getMethods(classElement);

        // Temporary structures for analysing EventSets
        Map adds = new HashMap();
        Map removes = new HashMap();

        // Analyze each method
        for ( Iterator it = methods.iterator(); it.hasNext() ; ) {
            Method method = (Method) it.next();
            String name = method.getName();
            int len = name.length();

            if ( (name.startsWith(GET_PREFIX) && len>GET_PREFIX.length())
              || (name.startsWith(SET_PREFIX) && len>SET_PREFIX.length())
              || (name.startsWith(IS_PREFIX) && len>IS_PREFIX.length()) ) {
                PropertyPattern pp = analyseMethodForProperties( method );
                if ( pp != null )
                    addProperty( pp );
            }
            if ( (name.startsWith(ADD_PREFIX) && len>ADD_PREFIX.length()) 
              || (name.startsWith(REMOVE_PREFIX) && len>REMOVE_PREFIX.length()) )  {
                analyseMethodForEventSets( method, adds, removes );
            }
        }
        // Resolve the temporay structures of event sets

        // Now look for matching addFooListener+removeFooListener pairs.
        Iterator keys = adds.keySet().iterator();

        while (keys.hasNext()) {
            String compound = (String) keys.next();
            // Skip any "add" which doesn't have a matching remove // NOI18N
            if (removes.get (compound) == null ) {
                continue;
            }
            // Method name has to end in Listener
            if (compound.indexOf( "Listener:" ) <= 0 ) { // NOI18N
                continue;
            }

            Method addMethod = (Method) adds.get(compound);
            Method removeMethod = (Method) removes.get(compound);
            List/*<Parameter>*/ params = addMethod.getParameters();
            Type argType = ((Parameter) params.get(0)).getType();

            // Check if the argument is a subtype of EventListener
            if ( !(argType instanceof ClassDefinition) || // filter out primitive types + arrays
                    !isSubclass((ClassDefinition) argType, findClassElement("java.util.EventListener")) ) // NOI18N
                continue;

            EventSetPattern esp = new EventSetPattern( this, addMethod, removeMethod );
            addEventSet( esp );
        }
    }

    private void resolveFields() throws JmiException {
        assert JMIUtils.isInsideTrans();
        // Analyze fields
        List/*<Field>*/ fields = JMIUtils.getFields(classElement);
        String propertyStyle = PropertyActionSettings.getDefault().getPropStyle();
        
        for ( Iterator it = fields.iterator(); it.hasNext();  ) {
            Field field = (Field) it.next();

            if ( ( field.getModifiers() & Modifier.STATIC ) != 0 )
                continue;
            
            //System.out.println("Property style " + propertyStyle);   
            String fieldName = field.getName();
            //System.out.println("Field name1 " + fieldName);
            if( fieldName.startsWith(propertyStyle) ){
                fieldName = fieldName.substring(1);
                //System.out.println("Field name2 " + fieldName);
            }
            
            PropertyPattern pp = (PropertyPattern)propertyPatterns.get( fieldName );
            if ( pp == null )
                pp = (PropertyPattern)idxPropertyPatterns.get( fieldName );
            if ( pp == null )
                continue;
            Type ppType = pp.getType();
            if ( ppType != null && ppType.equals( field.getType() ) )
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


    static ArrayList resolveChanges( Collection current, Map created, LevelComparator comparator ) throws JmiException {
        JMIUtils.isInsideTrans();
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

    PropertyPattern analyseMethodForProperties( Method method ) throws JmiException {
        assert JMIUtils.isInsideTrans();
        // Skip static methods as Introspector does.
        int modifiers = method.getModifiers();
        if ( Modifier.isStatic( modifiers ) )
            return null;

        String name = method.getName();
        Parameter[] params = (Parameter[]) method.getParameters().toArray(new Parameter[0]);
        Type returnType = method.getType();

        PropertyPattern pp = null;

        try {
            if ( params.length == 0 ) {
                if (name.startsWith( GET_PREFIX )) {
                    // SimpleGetter
                    pp = new PropertyPattern( this, method, null);
                }
                else if ( JMIUtils.isPrimitiveType( returnType, PrimitiveTypeKindEnum.BOOLEAN) && name.startsWith( IS_PREFIX )) {
                    // Boolean getter
                    pp = new PropertyPattern( this, method, null );
                }
            }
            else if ( params.length == 1 ) {
                if ( JMIUtils.isPrimitiveType(params[0].getType(), PrimitiveTypeKindEnum.INT) && name.startsWith( GET_PREFIX )) {
                    pp = new IdxPropertyPattern( this, null, null, method, null );
                }
                else if ( JMIUtils.isPrimitiveType(returnType, PrimitiveTypeKindEnum.VOID) && name.startsWith( SET_PREFIX )) {
                    pp = new PropertyPattern( this, null, method );
                    // PENDING vetoable => constrained
                }
            }
            else if ( params.length == 2 ) {
                if ( JMIUtils.isPrimitiveType(params[0].getType(), PrimitiveTypeKindEnum.INT) && name.startsWith( SET_PREFIX )) {
                    pp = new IdxPropertyPattern( this, null, null, null, method );
                    // PENDING vetoable => constrained
                }
            }
        }
        catch (IntrospectionException ex) {
            // PropertyPattern constructor found some differencies from design patterns.
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            pp = null;
        }

        return pp;
    }

    /** Method analyses class methods for EventSetPatterns
     */
    void analyseMethodForEventSets( Method method, Map adds, Map removes ) throws JmiException {
        assert JMIUtils.isInsideTrans();
        // Skip static methods
        int modifiers = method.getModifiers();
        if ( Modifier.isStatic( modifiers ) )
            return;

        String name = method.getName();
        Parameter params[] = (Parameter[]) method.getParameters().toArray(new Parameter[0]);
        Type returnType = method.getType();

        if ( params.length == 1 && JMIUtils.isPrimitiveType(returnType, PrimitiveTypeKindEnum.VOID) ) {
            Type paramType = params[0].getType();
            if (paramType instanceof JavaClass) {
                JavaClass lsnrType = (JavaClass) paramType;
                if (name.startsWith(ADD_PREFIX) && name.substring(3).equals(lsnrType.getSimpleName())) {
                    String compound = name.substring(3) + ":" + lsnrType.getName(); // NOI18N
                    adds.put( compound, method );
                } else if (name.startsWith(REMOVE_PREFIX) && name.substring(6).equals(lsnrType.getSimpleName())) {
                    String compound = name.substring(6) + ":" + lsnrType.getName(); // NOI18N
                    removes.put( compound, method );
                }
            }
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
        if (  opt != null && npt != null && !opt.equals(npt) ) {
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
        String key = esp.getName() + esp.getType().getName();
        EventSetPattern old = (EventSetPattern)eventSetPatterns.get( key );


        if ( old == null ) {
            eventSetPatterns.put( key, esp);
            return;
        }

        EventSetPattern composite = new EventSetPattern( old, esp );
        eventSetPatterns.put( key, composite );
    }

    // XXX can be replaced with ClassDefinition.isSubTypeOf()
    static boolean isSubclass(ClassDefinition a, ClassDefinition b) {

        if (a == null || b == null) {
            return false;
        }
        assert JMIUtils.isInsideTrans();
        
        return a.isSubTypeOf(b);

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
    
    public static FileObject fileObjectForElement( Element element ) {
        return JavaMetamodel.getManager().getFileObject(element.getResource());
    }
    
    public static JavaClass findClassElement( String name, Pattern pattern ) {
        return pattern.patternAnalyser.findClassElement( name );
    }


    public FileObject findFileObject () {
        return fileObjectForElement( referenceClassElement != null ? referenceClassElement : classElement );
    }

    JavaClass findClassElement( String name ) {
        Type t = findType(name);
        if (t instanceof JavaClass) {
            return (JavaClass) t;
        } else {
            return null;
        }
    }
    
    Type findType(String name) throws JmiException {
        assert JMIUtils.isInsideTrans();
        JavaClass jc = referenceClassElement != null? referenceClassElement: classElement;
        Type t = null;
        if (jc.isValid()) {
            t = JavaMetamodel.getManager().getJavaExtent(jc).getType().resolve(name);
        }

        return t;
    }
    
}
