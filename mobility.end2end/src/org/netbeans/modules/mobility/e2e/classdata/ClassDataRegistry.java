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

package org.netbeans.modules.mobility.e2e.classdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.mobility.javon.JavonProfileProvider;
import org.netbeans.modules.mobility.javon.JavonSerializer;
import org.netbeans.modules.mobility.javon.Traversable;
import org.openide.util.Lookup;

/**
 * @author Michal Skvor
 */
public class ClassDataRegistry {

    public static final String DEFAULT_PROFILE = "default";//NOI18N
    public static final String ALL_JAVA_PROFILE = "alljava";//NOI18N

    private List<ClasspathInfo> classpaths;

    private Map<String, ClassData> typeMap;
    private Map<String, ClassData> baseClasses;
    private Set<String> basePackages;

    private Map<ClassData, JavonSerializer> serializerRegistry = new HashMap<ClassData, JavonSerializer>();

    // All types from parameters and return types
    private Set<ClassData> registeredTypes = new HashSet<ClassData>();
    // Types used in parameters
    private Set<ClassData> parameterTypes = new HashSet<ClassData>();
    // Types used as return types from methods
    private Set<ClassData> returnTypes = new HashSet<ClassData>();
    ;

    private Map<ClassData, Integer> idMapping = new HashMap<ClassData, Integer>();

    private JavonProfileProvider profileProvider;

    /**
     * Inheritance comparator for making right order of inheritance in
     * ClassData structure. Children are higher in the structure than  their
     * parents for proper treatment of instanceof command in the serialization
     * chain.
     */
    private final static Comparator<ClassData> inheritanceComparator = new Comparator<ClassData>() {
        public int compare( final ClassData c1, final ClassData c2 ) {
            if ( c1.isArray() || c2.isArray() ) return 0;
            if ( c1.isPrimitive() || c2.isPrimitive() ) return 0;

            if ( c1.getParent().getFullyQualifiedName().equals( c2.getFullyQualifiedName() ) ) {
                return 1;
            } else if ( c2.getParent().getFullyQualifiedName().equals( c1.getFullyQualifiedName() ) ) {
                return -1;
            }
            return 0;
        }
    };

    /**
     * Factory method which returns instance of the ClassDataRegistry
     *
     * @param profileName name of the profile
     * @param classpaths  list of classpaths in which the search will be realized
     * @return ClassData registry
     */
    public static ClassDataRegistry getRegistry( String profileName, List<ClasspathInfo> classpaths ) {
        return new ClassDataRegistry( profileName, classpaths );
    }

    private ClassDataRegistry( String profileName, List<ClasspathInfo> classpaths ) {
        this.classpaths = classpaths;

        Lookup.Result<JavonProfileProvider> providersResult =
                Lookup.getDefault().lookup( new Lookup.Template<JavonProfileProvider>(
                        JavonProfileProvider.class ) );
        List<JavonProfileProvider> providers = new ArrayList( providersResult.allInstances() );

        for ( JavonProfileProvider provider : providers ) {
            if ( profileName.equals( provider.getName() ) ) {
                profileProvider = provider;
                break;
            }
        }
        if ( profileProvider == null ) {
            System.err.println( "Cannot find profile - " + profileName );
            return;
        }
    }

    /**
     * Get ClassData structure for given class name.
     *
     * @param fqn fully qualified name of the class
     * @return ClassData structure or null when the class is not to be found
     */
    public ClassData getClassData( String fqn ) {
        if ( typeMap == null ) {
            updateClassDataTree();
        }
        return typeMap.get( fqn );
    }

    /**
     * Returns map of all classes present on given ClasspathInfos
     *
     * @return map of classes
     */
    public Map<String, ClassData> getBaseClasses() {
        if ( baseClasses == null ) {
            updateClassDataTree();
        }
        return Collections.unmodifiableMap( baseClasses );
    }

    /**
     * Returns set of all packages present on given ClasspathInfos
     *
     * @return Set<String> of packages
     */

    public Set<String> getBasePackages() {
        if ( basePackages == null )
            updateClassDataTree();
        return Collections.unmodifiableSet( basePackages );
    }

    public boolean isRegisteredType(ClassData clsData) {
        if ( baseClasses==null)
            updateClassDataTree();
        return this.registeredTypes.contains( clsData );
    }

    /**
     * Returns set of all classes present in a specified package
     *
     * @param String packageName
     * @return Set<ClassData> classes
     */

    public Set<ClassData> getClassesForPackage(String packageName) {
        if ( basePackages == null )
            updateClassDataTree();
        HashSet<ClassData> result=new HashSet<ClassData>();

        Set<String> fqClassNames=this.baseClasses.keySet();
        for (String fqClassName : fqClassNames) {
            if (fqClassName.startsWith( packageName))
                result.add( this.baseClasses.get( fqClassName));
        }
        return result;
    }

    /**
     * Return serializer for given type
     *
     * @param ClassData type
     * @return serializer
     */

    public JavonSerializer getTypeSerializer( ClassData type ) {
        if ( serializerRegistry == null ) {
            updateClassDataTree();
        }
        return serializerRegistry.get( type );
    }

    /**
     * Rescans all classpaths and updates map of ClassData
     */
    public void updateClassDataTree() {
        typeMap = new HashMap();
        baseClasses = new HashMap<String, ClassData>();
        basePackages = new HashSet<String>();

        // Gather all instance names
        Set<String> instanceNames = new HashSet<String>();
        registeredTypes = new HashSet<ClassData>();
        for ( ClasspathInfo cpi : classpaths ) {
            try {
                // Traverse the tree of classes
                TraversingTask tt = new TraversingTask( profileProvider, cpi );
                JavaSource.create( cpi ).runUserActionTask( tt, true );

                // Assign numbers to all registered types for serialization
                registeredTypes.addAll( returnTypes );
                registeredTypes.addAll( parameterTypes );


                for ( ClassData cd : registeredTypes ) {
                    instanceNames.add( serializerRegistry.get( cd ).instanceOf( cd ) );
                }
                // Assign id to all types
                idMapping = new HashMap<ClassData, Integer>();
                for ( ClassData cd : registeredTypes ) {
                    int id = 1;
                    for ( String instanceName : instanceNames ) {
                        if ( serializerRegistry.get( cd ).instanceOf( cd ).equals( instanceName ) ) {
                            break;
                        }
                        id++;
                    }
                    System.err.println( " - " + cd.getName() + " = " + id );
                    idMapping.put( cd, id );
                    id++;
                }
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Return all registered types
     *
     * @return
     */
    public Set<ClassData> getRegisteredTypes() {
        if ( baseClasses==null)
            updateClassDataTree();
        return Collections.unmodifiableSet( registeredTypes );
    }

    /**
     * Return all return types from service methods
     *
     * @return Set of return ClassData types
     */
    public Set<ClassData> getReturnTypes() {
        if ( baseClasses == null )
            updateClassDataTree();
        return Collections.unmodifiableSet( returnTypes );
    }

    /**
     * Return all parameter types from service methods
     *
     * @return Set of parameter ClassData types
     */
    public Set<ClassData> getParameterTypes() {
        if ( baseClasses == null)
            updateClassDataTree();
        return Collections.unmodifiableSet( parameterTypes );
    }

    public int getRegisteredTypeId( ClassData type ) {
        if ( baseClasses==null)
            updateClassDataTree();
        return idMapping.get( type );
    }

    /**
     * Traverses all types on the ClasspathInfo path and each type represents as
     * ClassData structure for next processing
     */
    private final class TraversingTask implements CancellableTask<CompilationController>, Traversable {

        private ClasspathInfo cpi;

        private JavonProfileProvider profileProvider;

        private final static int RETURN_TYPES = 1;
        private final static int PARAMETERS_TYPES = 2;
        private int status;
        
        public TraversingTask( JavonProfileProvider profileProvider, ClasspathInfo cpi ) {
            this.profileProvider = profileProvider;
            this.cpi = cpi;

//            baseClasses = new HashMap<String, ClassData>();
//            basePackages = new HashSet<String>();
//            typeMap = new HashMap();

            serializerRegistry = new HashMap<ClassData, JavonSerializer>();

//            parameterTypes = new HashSet<ClassData>();
//            returnTypes = new HashSet<ClassData>();
        }
        
        public void cancel() {
        }

        public void run( CompilationController parameter ) throws Exception {
            Set<ElementHandle<TypeElement>> elements = cpi.getClassIndex().getDeclaredTypes( "", NameKind.PREFIX, EnumSet.of( SearchScope.SOURCE ) );
            for ( ElementHandle<TypeElement> eh : elements ) {
                TypeElement te = eh.resolve( parameter );
                if( te == null ) continue;
                ClassData cd = getServiceType( te.asType() );
                // Skip unsupported types
                if ( cd == null ) continue;
                typeMap.put( cd.getFullyQualifiedName(), cd );
                baseClasses.put( cd.getFullyQualifiedName(), cd );
                // Add package if needed
                basePackages.add( getPackage( cd ) );
            }
        }

        private String getPackage( ClassData cd ) {
            int index = cd.getFullyQualifiedName().lastIndexOf( '.' );
            if ( index > 0 )
                return cd.getFullyQualifiedName().substring( 0, index );
            else
                return "<default package>";
        }

        private ClassData getServiceType( TypeMirror type ) {
            if ( TypeKind.DECLARED == type.getKind() ) {
                TypeElement clazz = (TypeElement) ( (DeclaredType) type ).asElement();

                int packageLength = clazz.getQualifiedName().toString().length() - clazz.getSimpleName().toString().length() - 1;
                String packageName = "";
                if ( packageLength > 0 )
                    packageName = clazz.getQualifiedName().toString().substring( 0, packageLength );

                ClassData serviceType = new ClassData( packageName, clazz.getSimpleName().toString(), true, false );

                // Test methods
                for ( ExecutableElement e : ElementFilter.methodsIn( clazz.getEnclosedElements() ) ) {
                    if ( e.getModifiers().contains( Modifier.PUBLIC ) ) {
                        status = RETURN_TYPES;
                        boolean validReturnType = false, validParameters = true;
                        Map<String, ClassData> typeCache = new HashMap<String, ClassData>();
                        ClassData returnClass = traverseType( e.getReturnType(), typeCache );
                        if ( returnClass != null ) {
                            returnTypes.add( returnClass );
                            validReturnType = true;
                        }
                        List<MethodParameter> parameters = new ArrayList();
                        status = PARAMETERS_TYPES;
                        for ( VariableElement var : e.getParameters() ) {
                            typeCache = new HashMap<String, ClassData>();
                            ClassData paramClass = traverseType( var.asType(), typeCache );
                            if ( paramClass == null ) {
                                validParameters = false;
                                break;
                            }
                            parameterTypes.add( paramClass );
                            //System.err.println(" - parameter:" + paramClass.getName());
                            MethodParameter parameter = new MethodParameter( var.getSimpleName().toString(), paramClass );
                            parameters.add( parameter );
                        }
                        if ( validReturnType && validParameters ) {
                            MethodData method = new MethodData( e.getSimpleName().toString(),
                                    returnClass, parameters );
                            serviceType.addMethod( method );
                        }
                    }
                }
//                System.err.print( this.displayClassData( serviceType ) );
                return serviceType;
            }
            return null;
        }

        public ClassData traverseType( TypeMirror type, Map<String, ClassData> typeCache ) {
            for ( JavonSerializer serializer : profileProvider.getSerializers() ) {
                if ( serializer.isTypeSupported( this, type, typeCache ) ) {
                    ClassData cd = serializer.getType( this, type, typeCache );
                    serializerRegistry.put( cd, serializer );
                    return cd;
                }
            }
            return null;
        }

        public boolean isTypeSupported( TypeMirror type, Map<String, ClassData> typeCache ) {
            for ( JavonSerializer serializer : profileProvider.getSerializers() ) {
                if ( serializer.isTypeSupported( this, type, typeCache ) ) {
                    return true;
                }
            }
            return false;
        }

        public void registerType( ClassData type, JavonSerializer serializer ) {
            if ( status == PARAMETERS_TYPES ) {
                parameterTypes.add( type );
            } else if ( status == RETURN_TYPES ) {
                returnTypes.add( type );
            }
            serializerRegistry.put( type, serializer );
        }

        private String displayClassData( ClassData clsData ) {
            StringBuffer result = new StringBuffer( clsData.getFullyQualifiedName() + "\n\n" );

            for ( FieldData fe : clsData.getFields() ) {
                result.append( fe.getModifier() + " " + fe.getType() + " " + fe.getName() + "\n" );
            }
            result.append( "\n" );
            for ( MethodData me : clsData.getMethods() ) {
                result.append( me.getReturnType() + " " + me.getName() + "(" );
                int i = 0;
                for ( MethodParameter mp : me.getParameters() ) {
                    result.append( mp.getType().getFullyQualifiedName() + " " + mp.getName() );
                    if ( i < me.getParameters().size() - 1 )
                        result.append( "," );
                    else
                        result.append( ")\n" );
                    i++;
                }
            }
            return result.toString();
        }
    }
}
