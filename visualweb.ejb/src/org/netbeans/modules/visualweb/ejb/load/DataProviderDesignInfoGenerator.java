/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

/*
 * DesignInfoGenerator.java
 *
 * Created on March 8, 2005, 12:16 AM
 */

package org.netbeans.modules.visualweb.ejb.load;

import org.netbeans.modules.visualweb.ejb.datamodel.MethodInfo;
import org.netbeans.modules.visualweb.ejb.util.Util;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Date;

/**
 *
 * @author  cao
 */
public class DataProviderDesignInfoGenerator {

    private String clientWrapperName;
    private String fullBeanClassName;
    private MethodInfo method;

    public DataProviderDesignInfoGenerator(String clientWrapperName, String fullBeanClassName, MethodInfo method) {
        this.clientWrapperName = clientWrapperName;
        this.fullBeanClassName = fullBeanClassName;
        this.method = method;
    }

    public ClassDescriptor[] generateClass( String srcDir ) throws EjbLoadException
    {
        // Two class will be generated- DesignInfo and DataClassInstanceCustomizer
        ClassDescriptor[] classDescriptors = new ClassDescriptor[2];

        // Declare it outside the try-catch so that the file name can be logged in case of exception
        File javaFile = null;

        try
        {
            // Figure out the package name, class name and directory/file name

            String beanClassName = Util.getClassName( fullBeanClassName );
            String packageName = Util.getPackageName( fullBeanClassName );
            String designInfoClassName = beanClassName + "DesignInfo";

            String classDir = packageName.replace( '.', File.separatorChar );
            File dirF = new File( srcDir + File.separator + classDir );
            if( !dirF.exists() )
            {
                if( !dirF.mkdirs() )
                    System.out.println( ".....failed to make dir" + srcDir + File.separator + classDir );
            }

            String designInfoClassFile =  designInfoClassName + ".java";
            javaFile = new File( dirF, designInfoClassFile );
            javaFile.createNewFile();

            classDescriptors[0] = new ClassDescriptor(
                     designInfoClassName,
                     packageName,
                     javaFile.getAbsolutePath(),
                     classDir + File.separator + designInfoClassFile );

            // For the inner class
            classDescriptors[1] = new ClassDescriptor(
                     designInfoClassName + "$DataClassInstanceCustomizer",
                     packageName,
                     new File( dirF, designInfoClassName + "$DataClassInstanceCustomizer.class" ).getAbsolutePath(),
                     classDir + File.separator + designInfoClassName + "$DataClassInstanceCustomizer.class",
                     true );

            // Generate java code

            PrintWriter out = new PrintWriter( new FileOutputStream(javaFile) );

            // pacage
            if( packageName != null && packageName.length() != 0 )
            {
                out.println( "package " + packageName + ";" );
                out.println();
            }

            // comments
            out.println( "/**" );
            out.println( " * Source code created on " + new Date() );
            out.println( " */" );
            out.println();

            // Import
            out.println( "import com.sun.rave.designtime.*;" );
            out.println( "import com.sun.rave.designtime.impl.*;" );
            out.println( "import com.sun.rave.designtime.faces.*;" );
            out.println( "import java.util.ArrayList;" );
            out.println();

            // Start class
            out.println( "public class " + designInfoClassName + " extends BasicDesignInfo {" );
            out.println();

            // Constructor
            out.println( "    public " + designInfoClassName + "() {"  );
            out.println( "        super( " + clientWrapperName + ".class );" );
            out.println( "    } ");
            out.println();

            // Method - beanCreatedSetup()

            String fullPackageWrapperClassName = packageName + "." + clientWrapperName;

            String clientWrapperBeanPropName = clientWrapperName;
            if( !Util.isAcronyn( clientWrapperName ) )
                clientWrapperBeanPropName = Util.decapitalize(clientWrapperName);

            out.println( "    public Result beanCreatedSetup( DesignBean designBean ) {" );
            out.println( "        String currentScope = (String)designBean.getDesignContext().getContextData( Constants.ContextData.SCOPE );" );
            //out.println( "        System.out.println( \"++++++++++ currentScope: \" + currentScope );" );
            out.println( "        ArrayList clientBeans = new ArrayList();" );
            //out.println( "        DesignContext[] contexts = designBean.getDesignContext().getProject().getDesignContexts();" );
            // For performance improvement. No need to get all the contexts in the project
            out.println( "        DesignProject designProject = designBean.getDesignContext().getProject();" );
            out.println( "        DesignContext[] ctxs;" );
            out.println( "        if (designProject instanceof FacesDesignProject) {" );
            out.println( "            ctxs = ((FacesDesignProject)designProject).findDesignContexts(new String[] {" );
            out.println( "                \"request\"," );
            out.println( "                \"session\"," );
            out.println( "                \"application\"" );
            out.println( "            });" );
            out.println( "        } else {" );
            out.println( "            ctxs = new DesignContext[0];" );
            out.println( "        }" );
            
            // fix duplicate beans in customizer when dropping DataProvider onto a Session/Request/Application bean
            out.println("");
            out.println("        boolean duplicate = false;");
            out.println("        DesignContext currentDesignContext = designBean.getDesignContext();");
            out.println("        for ( int i = 0; i < ctxs.length; i++) {");
            out.println("            if (ctxs[i] == currentDesignContext) {");
            out.println("                duplicate = true;");
            out.println("                break;");
            out.println("            }");
            out.println("        }");
            out.println("        DesignContext[] contexts;");
            out.println("        if (duplicate) {");
            out.println("            contexts = ctxs;");
            out.println("        }else {");
            out.println("            contexts = new DesignContext[ctxs.length + 1];");
            out.println("            contexts[0] = designBean.getDesignContext();");
            out.println("            System.arraycopy(ctxs, 0, contexts, 1, ctxs.length);");
            out.println("        }");
            out.println("");
            
            out.println( "        for( int i = 0; i < contexts.length; i ++ ) {" );
            out.println( "            DesignBean[] beans = contexts[i].getBeansOfType( " + fullPackageWrapperClassName + ".class );" );
            out.println( "            for( int bi = 0; bi < beans.length; bi ++ ) { " );
            out.println();
            out.println( "                // Filter out the ones in the same scope unless it is in the same backing bean" );
            out.println( "                String sourceBeanScope = (String)beans[bi].getDesignContext().getContextData( Constants.ContextData.SCOPE );" );
            //out.println( "                System.out.println( \"++++++++++ sourceBeanScope: \" + sourceBeanScope );" );
            out.println( "                if( currentScope.equals( sourceBeanScope ) && !designBean.getBeanParent().getInstanceName().equals( beans[bi].getBeanParent().getInstanceName() ) )" );
            //out.println( "                {" );
            //out.println( "                    System.out.println( \"++++++++++ skipping bean from sourceBeanScope: \" + sourceBeanScope + \"-\" + beans[bi].getBeanParent().getInstanceName() );" );
            out.println( "                    continue;" );
            //out.println( "                }" );
            out.println();
            out.println( "                // Filter out the ones in smaller scopes." );
            out.println( "                // For example, if the current scope is in application, then only the ones in the application scope can be referred " );
            out.println( "                if( currentScope.equals( \"application\" ) && !sourceBeanScope.equals( \"application\" ) )" );
            out.println( "                    continue;" );
            out.println();
            out.println( "                // If the current scope is in session, then only the ones in application and session can be referred." );
            out.println( "                // In another words, the ones from request are not good" );
            out.println( "                if( currentScope.equals( \"session\" ) && sourceBeanScope.equals( \"request\" ) )" );
            out.println( "                    continue;" );
            out.println();
            //out.println( "                System.out.println( \"++++++++++ adding source bean from scope: \" + sourceBeanScope + \"-\" + beans[bi].getBeanParent().getInstanceName() );" );
            out.println( "                clientBeans.add( beans[bi] );" );
            out.println( "            }" );
            out.println( "        }" );
            //out.println( "        System.out.println( \"++++++++++ num of DesignBean of type " + clientWrapperName + ": \" + clientBeans.size() );" );
            out.println( "        if( clientBeans.size() == 0 ) { " );
            out.println( "            if( designBean.getDesignContext().canCreateBean( \"" + fullPackageWrapperClassName + "\", designBean.getBeanParent(), null ) ) {" );
            out.println( "                DesignBean clientBean = designBean.getDesignContext().createBean( \"" + fullPackageWrapperClassName + "\", designBean.getBeanParent(),  null );" );
            out.println( "                designBean.getProperty( \"" + clientWrapperBeanPropName + "\").setValue( clientBean.getInstance() );" );
            out.println( "                return Result.SUCCESS;" );
            out.println( "            }" );
            out.println( "            else {" );
            out.println();
            out.println( "                ResultMessage message = new ResultMessage( ResultMessage.TYPE_CRITICAL, \"No " + fullPackageWrapperClassName + " instance\", \"No " + fullPackageWrapperClassName + " instance found and failed to create one\" );" );
            out.println( "                return new Result( true, message );" );
            out.println( "             }" );
            out.println( "         } else if( clientBeans.size() == 1 ) {" );
            out.println( "             DesignBean selectedBean = (DesignBean)clientBeans.get(0);" );
            out.println( "             designBean.getProperty( \"" + clientWrapperBeanPropName + "\").setValue( ((DesignBean)clientBeans.get(0)).getInstance() );" );
            out.println( "             return Result.SUCCESS;" );
            out.println( "         } else {// More than one found" );
            out.println( "             DesignProperty prop = designBean.getProperty( \"" + clientWrapperBeanPropName + "\" );" );
            out.println( "             DesignBean defaultBean = (DesignBean)clientBeans.get(0);");
            out.println( "             prop.setValue( defaultBean.getInstance() );");
            out.println();
            out.println( "             Customizer2 dpCustomizer = new DataClassInstanceCustomizer( \"DataProvider customizer\", designBean, prop, (DesignBean[])clientBeans.toArray(new DesignBean[0]) ); ");
            out.println( "             return new CustomizerResult( designBean, dpCustomizer );");
            out.println( "         }" );
            out.println( "     }" );
            out.println();
            out.println( "     private String capitalize(String name) {" );
            out.println( "         if( name == null || name.length() == 0 )" );
            out.println( "              return name;" );
            out.println();
            out.println( "         char chars[] = name.toCharArray();" );
            out.println( "         chars[0] = Character.toUpperCase(chars[0]);" );
            out.println( "         return new String(chars);" );
            out.println( "    }" );
            out.println();

            // TODO for now, have this class in each DesignInfo class. Need to move to a jar file
            createDataClassInstanceCustomizer( out );

            // End of class
            out.println( "}" );

            out.flush();
            out.close();

            return classDescriptors;
        }
        catch( java.io.FileNotFoundException ex )
        {
            // Log error
            /*String errMsg = "Error occurred when trying to generate the wrapper bean class for EJB " + ejbName
                            + ". Could not find file " + javaFile.getAbsolutePath();
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.ClientBeanInfoGenerator" ).log( errMsg );*/
            ex.printStackTrace();

            // Throw up as a SYSTEM_ERROR
            throw new EjbLoadException( ex.getMessage() );
        }
        catch( java.io.IOException ex )
        {
            // Log error
            /*String errMsg = "Error occurred when trying to generate the wrapper bean class for EJB " + ejbName
                            + ". Could not create file " + javaFile.getAbsolutePath();
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.ClientBeanInfoGenerator" ).log( errMsg );*/
            ex.printStackTrace();

            // Throw up as a SYSTEM_ERROR
            throw new EjbLoadException( ex.getMessage() );
        }
    }

    private void createDataClassInstanceCustomizer( PrintWriter out )
    {
        out.println( "    /**" );
        out.println( "     * This customizer for setting the data class instance on a data provider" );
        out.println( "     */" );
        out.println( "    public class DataClassInstanceCustomizer extends BasicCustomizer2 {" );
        out.println( "        // The bean where the property from" );
        out.println( "        protected DesignBean targetBean;" );
        out.println();
        out.println( "        // The design property to be set" );
        out.println( "        protected DesignProperty targetProperty;" );
        out.println();
        out.println( "        // The bean instance from one the following design beans will be set to the target property" );
        out.println( "        protected DesignBean[] sourceBeans;" );
        out.println();
        out.println( "        // The List to display the bean instance to be selected from" );
        out.println( "        private javax.swing.JList beanInstancesList;" );
        out.println();
        out.println( "        /**" );
        out.println( "         * Constructor" );
        out.println( "         *" );
        out.println( "         * @param name The title of the customizer" );
        out.println( "         * @param targetProperty The property this customizer is designed to set" );
        out.println( "         * @param sourceBeans" );
        out.println( "         */" );
        out.println( "        public DataClassInstanceCustomizer(String name, DesignBean targetBean, DesignProperty targetProperty, DesignBean[] sourceBeans) { " );
        out.println( "            super( null, name ); " );
        out.println( "            setApplyCapable( false );" );
        out.println( "            this.targetProperty = targetProperty;" );
        out.println( "            this.targetBean = targetBean;" );
        out.println( "            this.sourceBeans = sourceBeans;" );
        out.println( "        }" );
        out.println();
        out.println( "        protected java.awt.Component createCustomizerPanel() {" );
        out.println();
        out.println( "            javax.swing.JPanel panel = new javax.swing.JPanel();" );
        out.println();
        out.println( "            java.awt.GridBagConstraints gridBagConstraints;" );
        out.println();
        out.println( "            javax.swing.JScrollPane listScrollPane = new javax.swing.JScrollPane();" );
        out.println( "            beanInstancesList = new javax.swing.JList();" );
        out.println( "            javax.swing.JLabel label = new javax.swing.JLabel();" );
        out.println();
        out.println( "            panel.setLayout(new java.awt.GridBagLayout());" );
        out.println();
        out.println( "            beanInstancesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);" );
        out.println( "            beanInstancesList.setVisibleRowCount(4);" );
        out.println( "            listScrollPane.setViewportView(beanInstancesList);" );
        out.println();
        out.println( "            gridBagConstraints = new java.awt.GridBagConstraints();" );
        out.println( "            gridBagConstraints.gridx = 0;" );
        out.println( "            gridBagConstraints.gridy = 1;" );
        out.println( "            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;" );
        out.println( "            gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);" );
        out.println( "            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;" );
        out.println( "            gridBagConstraints.weightx = 1.0;" );
        out.println( "            gridBagConstraints.weighty = 1.0;" );
        out.println( "            panel.add(listScrollPane, gridBagConstraints);" );
        out.println();
        out.println( "            label.setText(\"Select a " + clientWrapperName + " instance to set on data provider instance \" + targetBean.getInstanceName() );" );
        out.println( "            gridBagConstraints = new java.awt.GridBagConstraints();" );
        out.println( "            gridBagConstraints.gridx = 0;" );
        out.println( "            gridBagConstraints.gridy = 0;" );
        out.println( "            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;" );
        out.println( "            gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);" );
        out.println( "            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;" );
        out.println( "            panel.add(label, gridBagConstraints);" );
        out.println();
        out.println( "            String[] beanInstanceNames = new String[ sourceBeans.length ];" );
        out.println( "            for( int i = 0; i < sourceBeans.length; i ++ ) {" );
        out.println( "                String parentInstance = sourceBeans[i].getBeanParent().getInstanceName();" );
        out.println( "                beanInstanceNames[i] = sourceBeans[i].getInstanceName() + \"(\" + parentInstance + \")\";" );
        out.println( "            }" );
        out.println( "            beanInstancesList.setListData( beanInstanceNames );" );
        out.println();
        out.println(              "// Have the first one selected by default" );
        out.println( "            beanInstancesList.setSelectedIndex(0);" );
        out.println();
        out.println( "            return panel;" );
        out.println( "        }" );
        out.println();
        out.println( "        public Result applyChanges() {" );
        out.println();
        out.println( "            int index = beanInstancesList.getSelectedIndex();" );
        out.println( "            DesignBean selectedBean = sourceBeans[index];" );
        out.println( "            targetProperty.setValue( sourceBeans[index].getInstance() );" );
        out.println();
        out.println( "            return Result.SUCCESS;" );
        out.println( "        }" );
        out.println( "    }" );
    }

}
