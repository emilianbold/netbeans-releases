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

package org.netbeans.modules.mobility.end2end.util;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.mobility.e2e.classdata.ClassData;
import org.netbeans.modules.mobility.e2e.classdata.ClassDataRegistry;
import org.netbeans.modules.mobility.e2e.classdata.MethodData;
import org.netbeans.modules.mobility.e2e.classdata.MethodParameter;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Jirka Prazak
 */

public class ServiceNodeManager {

    final static String DEFAULT_PACKAGE = "<default package>"; // NOI18N
    public final static String NODE_VALIDITY_ATTRIBUTE = "isValid"; //NOI18N

    private static ClassDataRegistry activeProfileRegistry;

    public static Node getRootNode( final Project project ) {
        try {
            final FileObject rootFile = project.getProjectDirectory();
            final DataObject dataObject = DataObject.find( rootFile );
            final Node rootNode = new DataNode( dataObject, createNodesForProject( project ) ) {
                public boolean canRename() {
                    return false;
                }
            };
            rootNode.setDisplayName( ProjectUtils.getInformation( project ).getDisplayName() );
            rootNode.setName( ProjectUtils.getInformation( project ).getName() );
            return rootNode;
        } catch ( Exception e ) {
            ErrorManager.getDefault().notify( e );
            return new AbstractNode( Children.LEAF );

        }
    }

    /**
     * Creates node subtrees for the given set of packages
     *
     * @param registry ClassDataRegistry to generate children for
     * @return children The child nodes representing classes in each package
     */
    private static Children createPackageNodes( ClassDataRegistry registry ) {
        final Children result = new Children.Array();
        Set<String> packages = registry.getBasePackages();
        Node[] nodes = new Node[packages.size()];

        int i = 0;
        for ( String packageName : packages ) {
            nodes[i] = new PackageNode( packageName, createClassNodes( registry.getBaseClassesForPackage( packageName ) ) );
            int nOfInvalidClasses=0;
            for (Node node:nodes[i].getChildren().getNodes()) {
                Boolean isValid=(Boolean) node.getValue( NODE_VALIDITY_ATTRIBUTE);
                if (!isValid.booleanValue())
                    nOfInvalidClasses++;
            }
            if (nOfInvalidClasses==nodes[i].getChildren().getNodes().length)
                nodes[i].setValue( NODE_VALIDITY_ATTRIBUTE, Boolean.FALSE);
            else
                nodes[i].setValue( NODE_VALIDITY_ATTRIBUTE, Boolean.TRUE);
            i++;
        }

        result.add( nodes );
        return result;
    }

    private static Children createClassNodes( final Set<ClassData> classes ) {
        final Children result = new Children.Array();
        Node[] nodes = new Node[classes.size()];

        int i = 0;

        for ( ClassData clsData : classes ) {
            nodes[i] = new ClassDataNode( clsData.getName(), createMethodNodes( clsData.getMethods() ), clsData );
            int nOfInvalidMethods=0;
            for (Node node: nodes[i].getChildren().getNodes()) {
                Boolean isValid=(Boolean) node.getValue( NODE_VALIDITY_ATTRIBUTE);
                if (!isValid.booleanValue())
                    nOfInvalidMethods++;
            }
            if (nOfInvalidMethods==nodes[i].getChildren().getNodes().length)
                nodes[i].setValue( NODE_VALIDITY_ATTRIBUTE, Boolean.FALSE);
            else
                nodes[i].setValue( NODE_VALIDITY_ATTRIBUTE, Boolean.TRUE);
            i++;
        }

        result.add( nodes );
        return result;

    }

    private static Children createMethodNodes( final List<MethodData> methods ) {
        final Children result = new Children.Array();
        Node[] nodes = new Node[methods.size()];

        int i = 0;
        boolean isValid=true;

        for ( MethodData mthData : methods ) {
            // check if the method return type is supported by any serializer available in this registry
            if ( !activeProfileRegistry.isRegisteredType( mthData.getReturnType()))
                isValid=false;
            StringBuffer nodeText = new StringBuffer( mthData.getReturnType().getName()+" "+mthData.getName() + "(" );
            int j = 0;
            for ( MethodParameter mthParam : mthData.getParameters() ) {
                // check whether or not the param. type is supported by any serializer available in this registry
                if (!activeProfileRegistry.isRegisteredType( mthParam.getType()))
                    isValid=false;
                nodeText.append( mthParam.getType().getName() + " " + mthParam.getName() );
                if ( j < mthData.getParameters().size() - 1 )
                    nodeText.append( "," );
                j++;
            }
            nodeText.append( ")" );
            nodes[i] = new MethodDataNode( mthData.getName(), nodeText.toString() , mthData);
            nodes[i].setValue(NODE_VALIDITY_ATTRIBUTE,Boolean.valueOf( isValid));
            i++;
            isValid=true;
        }

        result.add( nodes );
        return result;
    }


    private static Children createNodesForProject( final Project p ) {
        final Sources s = ProjectUtils.getSources( p );
        final SourceGroup[] groups = s.getSourceGroups( JavaProjectConstants.SOURCES_TYPE_JAVA );

        // Add all paths to the ClasspathInfo structure
        List<ClasspathInfo> classpaths = new ArrayList();
        for ( SourceGroup sg : s.getSourceGroups( JavaProjectConstants.SOURCES_TYPE_JAVA ) ) {
            if ( !sg.getName().equals( "${test.src.dir}" ) )
                classpaths.add( ClasspathInfo.create( sg.getRootFolder() ) );
        }
        // Get the registry for all available classes
        ClassDataRegistry registry = ClassDataRegistry.getRegistry( ClassDataRegistry.ALL_JAVA_PROFILE, classpaths );
        activeProfileRegistry = ClassDataRegistry.getRegistry( getActiveProfile(), classpaths);

        // Create the nodes
        final ArrayList<Node> nodesList = new ArrayList<Node>( groups.length );

        return createPackageNodes( registry );
    }

    private static String getActiveProfile() {
        return ClassDataRegistry.DEFAULT_PROFILE;
    }


    static class ClassDataNode extends AbstractNode {

        final Image CLASS_BADGE = Utilities.loadImage( "org/netbeans/spi/java/project/support/ui/packageBadge.gif" );

        protected ClassDataNode( String className, Children children, ClassData clsData ) {
            super( children , Lookups.singleton( clsData));

            setName( className );
            setDisplayName( className );
        }

        public Image getIcon( @SuppressWarnings( "unused" )
        final int type ) {
            return CLASS_BADGE;
        }

        public Image getOpenedIcon( @SuppressWarnings( "unused" )
        final int type ) {
            return CLASS_BADGE;
        }

    }

    static class PackageNode extends AbstractNode {

        final Image PACKAGE_BADGE = Utilities.loadImage( "org/netbeans/spi/java/project/support/ui/packageBadge.gif" ); // NOI18N

        protected PackageNode( String packageName, Children children ) {
            super( children );

            setName( packageName );
            setDisplayName( packageName );
        }

        public Image getIcon( @SuppressWarnings( "unused" )
        final int type ) {
            return PACKAGE_BADGE;
        }

        public Image getOpenedIcon( @SuppressWarnings( "unused" )
        final int type ) {
            return PACKAGE_BADGE;
        }
    }

    static class MethodDataNode extends AbstractNode {

        final Image METHOD_BADGE = Utilities.loadImage( "org/netbeans/spi/java/project/support/ui/packageBadge.gif" ); //NOI18N

        protected MethodDataNode( String methodName, String methodDescription, MethodData mthData ) {
            super( Children.LEAF , Lookups.singleton( mthData));

            setName( methodName );
            setDisplayName( methodDescription );
        }

        public Image getIcon( @SuppressWarnings( "unused" ) final int type ) {
            return METHOD_BADGE;
        }

        public Image getOpenedIcon( @SuppressWarnings( "unused" ) final int type ) {
            return METHOD_BADGE;
        }

    }
}
