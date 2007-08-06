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

import java.util.*;
import java.util.concurrent.Future;
import javax.lang.model.element.*;
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
 * @author Jirka Prazak, Michal Skvor
 */
public class ClassDataRegistry {

    public static final String DEFAULT_PROFILE = "default";//NOI18N
    public static final String ALL_JAVA_PROFILE = "alljava";//NOI18N

    private List<ClasspathInfo> classpaths;

    private Map<String, ClassData> typeMap = null;
    private Map<String, ClassData> baseClasses = null;

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
        if ( baseClasses == null ) {
            updateClassDataTree();
        }
        return baseClasses.get( fqn );
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
        if ( baseClasses == null )
            updateClassDataTree();

        Set<String> result=new HashSet<String>();

        for (ClassData clsData: baseClasses.values())
            result.add( Utils.getPackage( clsData));

        return Collections.unmodifiableSet( result );
    }

    public boolean isRegisteredType( ClassData clsData ) {
        if ( typeMap == null )
            updateClassDataTree();
        return this.typeMap.values().contains( clsData );
    }


    /**
     * Returns set of all classes present in a specified package registered in the ClassDataRegistry
     *
     * @param String packageName
     * @return Set<ClassData> classes
     */
    public Set<ClassData> getClassesForPackage( String packageName ) {
        if ( typeMap == null )
            updateClassDataTree();

        HashSet<ClassData> result = new HashSet<ClassData>();

        Set<String> fqClassNames = this.typeMap.keySet();
        for ( String fqClassName : fqClassNames )
            if ( fqClassName.startsWith( packageName ) )
                result.add( this.typeMap.get( fqClassName ) );

        return result;
    }

    /**
     * Returns set of all base classes present in a specified package registered in the ClassDataRegistry
     *
     * @param String packageName
     * @return Set<ClassData> base classes
     */
    public Set<ClassData> getBaseClassesForPackage( String packageName ) {
        if (baseClasses == null )
        updateClassDataTree();

        HashSet<ClassData> result = new HashSet<ClassData>();

        Set<String> fqClassNames = this.baseClasses.keySet();
        for (String fqClassName : fqClassNames)
            if ( fqClassName.startsWith( packageName ))
                result.add( this.baseClasses.get( fqClassName));

        return result;
    }

    /**
     * Return serializer for given type
     *
     * @param ClassData type
     * @return serializer
     */

    public JavonSerializer getTypeSerializer( ClassData type ) {
        return type.getSerializer();
    }

    /**
     * Rescans all classpaths and updates map of ClassData
     */
    public void updateClassDataTree() {
        if (typeMap==null)
            typeMap = new HashMap<String,ClassData>();
        if (baseClasses==null)
            baseClasses = new HashMap<String, ClassData>();

        // Gather all instance names
        Set<String> instanceNames = new HashSet<String>();
        int id = 1;
        for ( ClasspathInfo cpi : classpaths ) {
            try {
                // Traverse the tree of classes
                TraversingTask tt = new TraversingTask( profileProvider, cpi );
                JavaSource.create( cpi ).runWhenScanFinished( tt, false );

                for ( ClassData cd : typeMap.values()) {
                    if (cd.getSerializer()!=null)
                    instanceNames.add(cd.getSerializer().instanceOf( cd));
                }
                // Assign id to all types
                idMapping = new HashMap<ClassData, Integer>();
                for ( ClassData cd : typeMap.values() ) {
                    for ( String instanceName : instanceNames ) {
                        if ( cd.getSerializer().instanceOf( cd ).equals( instanceName ) ) {
                            break;
                        }
                        id++;
                    }
//                    System.err.println( " - " + cd.getName() + " = " + id );
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
        if ( typeMap == null )
            updateClassDataTree();
        return Collections.unmodifiableSet(new HashSet(typeMap.values()));
    }

    /**
     * Return all return types from service methods
     *
     * @return Set of return ClassData types
     */
    public Set<ClassData> getReturnTypes() {
        if ( typeMap == null )
            updateClassDataTree();
        Set<ClassData> result=new HashSet();

        for (ClassData clsData: typeMap.values()) {
            for (MethodData mthData : clsData.getMethods())
                result.add( mthData.getReturnType());
        }
        return Collections.unmodifiableSet( result );
    }

    /**
     * Return all parameter types from service methods
     *
     * @return Set of parameter ClassData types
     */
    public Set<ClassData> getParameterTypes() {
        if ( typeMap == null )
            updateClassDataTree();
        Set<ClassData> result=new HashSet();

        for (ClassData clsData:typeMap.values())
            for (MethodData mthData : clsData.getMethods())
                result.addAll( mthData.getReturnType().getParameterTypes());
        return Collections.unmodifiableSet( result );
    }

    public int getRegisteredTypeId( ClassData type ) {
        if ( typeMap == null )
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

            if (typeMap==null)
                typeMap=new HashMap<String,ClassData>();
        }

        public void cancel() {
        }

        public void run( CompilationController parameter ) throws Exception {
            Set<ElementHandle<TypeElement>> elements = cpi.getClassIndex().getDeclaredTypes( "", NameKind.PREFIX, EnumSet.of( SearchScope.SOURCE ) );
            for ( ElementHandle<TypeElement> eh : elements ) {
                TypeElement te = eh.resolve( parameter );
                ClassData clsData=null;
                if ( te != null )
                    clsData = getServiceType( te.asType() );
                // Skip unsupported types
                if ( clsData != null )
                    //typeMap.put( clsData.getFullyQualifiedName(), clsData );
                    baseClasses.put( clsData.getFullyQualifiedName(), clsData);
            }
        }

        //scans a given base class for all fields and service methods and adds their types to the type map if supported
        private ClassData getServiceType( TypeMirror type ) {
            if ( TypeKind.DECLARED == type.getKind() ) {
                TypeElement clazz = (TypeElement) ( (DeclaredType) type ).asElement();

                String packageName=Utils.getPackage( clazz);
                JavonSerializer serializer=Utils.findSupportingSerializer( type, this.profileProvider, this, new HashMap<String,ClassData>());

                // Process methods
                List<MethodData> methods=new ArrayList<MethodData>(0);
                for ( ExecutableElement e : ElementFilter.methodsIn( clazz.getEnclosedElements() ) ) {
                    if ( e.getModifiers().contains( Modifier.PUBLIC ) ) {
                        boolean validReturnType;
                        boolean validParameters=true;

                        Map<String, ClassData> typeCache = new HashMap<String, ClassData>();
                        ClassData returnClass = traverseType( e.getReturnType(), typeCache );

                        if ( returnClass != null ) {
                            typeMap.put( returnClass.getFullyQualifiedName(), returnClass );
                            validReturnType=true;
                        } else {
                            validReturnType=false;
                        }
                        List<MethodParameter> parameters = new ArrayList<MethodParameter>();
                        for ( VariableElement var : e.getParameters() ) {
                            typeCache = new HashMap<String, ClassData>();
                            ClassData paramClass = traverseType( var.asType(), typeCache );
                            if ( paramClass != null ) {
                                typeMap.put( paramClass.getFullyQualifiedName(), paramClass);
                                parameters.add( new MethodParameter( var.getSimpleName().toString(), paramClass));
                                validParameters=true;
                            } else {
                                validParameters=false;
                                break;
                            }
                        }
                        if ( validReturnType && validParameters ) {
                            methods.add( new MethodData(e.getSimpleName().toString(),returnClass,parameters));
                        }
                    }
                }

                // Process fields but do not add them to the type map as we do support direct access to fields
                List<FieldData> fields=new ArrayList<FieldData>(0);
                for (VariableElement ve: ElementFilter.fieldsIn( clazz.getEnclosedElements())) {

                    Map<String,ClassData> typeCache=new HashMap<String, ClassData>();
                    ClassData fieldType=traverseType( ve.asType(), typeCache);

                    if (fieldType!=null) {
                        FieldData fieldData=new FieldData(fieldType.getName(), fieldType);
                        if (ve.getModifiers().contains( Modifier.PUBLIC))
                            fieldData.setModifier( ClassData.Modifier.PUBLIC);
                        if (ve.getModifiers().contains( Modifier.PRIVATE))
                            fieldData.setModifier( ClassData.Modifier.PRIVATE);
                        fields.add(fieldData);
                    }
                }

                ClassData result=new ClassData(packageName, clazz.getSimpleName().toString(), false, fields, methods, serializer);
                System.err.print( this.displayClassData( result ) );
                return result;
            }
            return null;
        }

        public ClassData traverseType( TypeMirror type, Map<String, ClassData> typeCache ) {
            for ( JavonSerializer serializer : profileProvider.getSerializers() ) {
                if ( serializer.isTypeSupported( this, type, typeCache ) ) {
                    ClassData cd = serializer.getType( this, type, typeCache );                                        
                    return cd;
                }
            }
            return null;
        }

        public boolean isTypeSupported( TypeMirror type, Map<String, ClassData> typeCache ) {
            if ( Utils.findSupportingSerializer( type, this.profileProvider, this, typeCache ) != null )
                return true;
            else
                return false;
        }

        public JavonSerializer registerType(ClassData type) {
            if (type.getSerializer()==null)
                return null;
            else {
                typeMap.put( type.getFullyQualifiedName(), type);
                return type.getSerializer();
            }
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
