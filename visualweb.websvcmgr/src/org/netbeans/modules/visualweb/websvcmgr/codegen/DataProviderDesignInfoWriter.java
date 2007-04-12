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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.visualweb.websvcmgr.codegen;

import org.netbeans.modules.visualweb.websvcmgr.util.Util;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;

/**
 *
 * @author  cao
 */
public class DataProviderDesignInfoWriter extends java.io.PrintWriter {

    private DataProviderInfo dataProviderInfo;

    public DataProviderDesignInfoWriter(Writer writer, DataProviderInfo dataProviderInfo ){
        super(writer);
        this.dataProviderInfo = dataProviderInfo;
    }

    public void writeClass() {

        // package
        println( "package " + dataProviderInfo.getPackageName() + ";" );

        // comments
        println( "/**" );
        println( " * Source code created on " + new Date() );
        println( " */" );
        println();

        // Import
        println( "import com.sun.rave.designtime.*;" );
        println( "import com.sun.rave.designtime.impl.*;" );
        println( "import com.sun.rave.designtime.faces.*;" );
        println( "import java.util.ArrayList;" );
        println();

        // Start class
        String designInfoClassName = dataProviderInfo.getClassName() + "DesignInfo";
        String clientWrapperName = dataProviderInfo.getClientWrapperClassName();
        println( "public class " + designInfoClassName + " extends BasicDesignInfo {" );
        println();

        // Constructor
        println( "    public " + designInfoClassName + "() {"  );
        println( "        super( " + clientWrapperName + ".class );" );
        println( "    } ");
        println();

        // Method - beanCreatedSetup()

        String fullPackageWrapperClassName = dataProviderInfo.getPackageName() + "." + clientWrapperName;

        String clientWrapperBeanPropName = clientWrapperName;
        if( !Util.isAcronyn( clientWrapperName ) )
            clientWrapperBeanPropName = Util.decapitalize(clientWrapperName);

        println( "    public Result beanCreatedSetup( DesignBean designBean ) {" );
        println( "        String currentScope = (String)designBean.getDesignContext().getContextData( Constants.ContextData.SCOPE );" );
        println( "        ArrayList clientBeans = new ArrayList();" );
        //println( "        DesignContext[] contexts = designBean.getDesignContext().getProject().getDesignContexts();" );
        // For performance improvement. No need to get all the contexts in the project
        println( "        DesignProject designProject = designBean.getDesignContext().getProject();" );
        println( "        DesignContext[] ctxs;" );
        println( "        if (designProject instanceof FacesDesignProject) {" );
        println( "            ctxs = ((FacesDesignProject)designProject).findDesignContexts(new String[] {" );
        println( "                \"request\"," );
        println( "                \"session\"," );
        println( "                \"application\"" );
        println( "            });" );
        println( "        } else {" );
        println( "            ctxs = new DesignContext[0];" );
        println( "         }" );
        println( "        DesignContext[] contexts = new DesignContext[ctxs.length + 1];" );
        println( "        contexts[0] = designBean.getDesignContext();" );
        println( "        System.arraycopy(ctxs, 0, contexts, 1, ctxs.length);" );
        println( "        for( int i = 0; i < contexts.length; i ++ ) {" );
        println( "            DesignBean[] beans = contexts[i].getBeansOfType( " + clientWrapperName + ".class );" );
        println( "            for( int bi = 0; bi < beans.length; bi ++ ) {" );
        println();
        println( "                // Filter out the ones in the same scope unless it is in the same backing bean" );
        println( "                String sourceBeanScope = (String)beans[bi].getDesignContext().getContextData( Constants.ContextData.SCOPE );" );
        println( "                if( currentScope.equals( sourceBeanScope ) && !designBean.getBeanParent().getInstanceName().equals( beans[bi].getBeanParent().getInstanceName() ) )" );
        println( "                    continue;" );
        println();
        println( "                // Filter out the ones in smaller scopes." );
        println( "                // For example, if the current scope is in application, then only the ones in the application scope can be referred " );
        println( "                if( currentScope.equals( \"application\" ) && !sourceBeanScope.equals( \"application\" ) )" );
        println( "                    continue;" );
        println();
        println( "                // If the current scope is in session, then only the ones in application and session can be referred." );
        println( "                // In another words, the ones from request are not good" );
        println( "                if( currentScope.equals( \"session\" ) && sourceBeanScope.equals( \"request\" ) )" );
        println( "                    continue;" );
        println();
        println( "                clientBeans.add( beans[bi] );" );
        println( "            }" );
        println( "        }" );
        //println( "        System.out.println( \"++++++++++ num of DesignBean of type" + clientWrapperName + ": \" + clientBeans.size() );" );
        println( "        if( clientBeans.size() == 0 ) { " );
        println( "            if( designBean.getDesignContext().canCreateBean( \"" + fullPackageWrapperClassName + "\", designBean.getBeanParent(), null ) ) {" );
        println( "                DesignBean clientBean = designBean.getDesignContext().createBean( \"" + fullPackageWrapperClassName + "\", designBean.getBeanParent(),  null );" );
        println( "                designBean.getProperty( \"" + clientWrapperBeanPropName + "\").setValue( clientBean.getInstance() );" );;
        println( "                return Result.SUCCESS;" );
        println( "            }" );
        println( "            else {" );
        println();
        println( "                ResultMessage message = new ResultMessage( ResultMessage.TYPE_CRITICAL, \"No GreeterClient instance\", \"No GreeterClient instance found and failed to create one\" );" );
        println( "                return new Result( true, message );" );
        println( "             }" );
        println( "         } else if( clientBeans.size() == 1 ) {" );
        println( "             DesignBean selectedBean = (DesignBean)clientBeans.get(0);" );
        println( "             if( designBean.getBeanParent().getInstanceName().equals( selectedBean.getBeanParent().getInstanceName() ) ) {" );
        println( "                 designBean.getProperty( \"" + clientWrapperBeanPropName + "\").setValue( ((DesignBean)clientBeans.get(0)).getInstance() );" );
        println( "             }" );
        println( "             else" );
        println( "             {" );
        println( "                 String pInstanceName = capitalize( selectedBean.getBeanParent().getInstanceName() );" );
        println( "                 String selectedInstanceName = capitalize( selectedBean.getInstanceName() );" );
        println();
        println( "                  String sourceBeanStr = \"get\" + pInstanceName + \"().get\" + selectedInstanceName + \"()\"; // NOI18N" );
        //println( "                  System.out.println( \".....sourceBeanStr: \" + sourceBeanStr );" );
        println( "                  designBean.getProperty( \"" + clientWrapperBeanPropName + "\").setValueSource( sourceBeanStr );" );
        println( "             }" );
        println();
        println( "             return Result.SUCCESS;" );
        println( "         } else {// More than one found" );
        println( "             DesignProperty prop = designBean.getProperty( \"" + clientWrapperBeanPropName + "\" );" );
        println( "             Customizer2 dpCustomizer = new DataClassInstanceCustomizer( \"DataProvider customizer\", designBean, prop, (DesignBean[])clientBeans.toArray(new DesignBean[0]) ); ");
        println( "             return new CustomizerResult( designBean, dpCustomizer );");
        println( "         }" );
        println( "     }" );
        println();
        println( "     private String capitalize(String name) {" );
        println( "         if( name == null || name.length() == 0 )" );
        println( "              return name;" );
        println();
        println( "         char chars[] = name.toCharArray();" );
        println( "         chars[0] = Character.toUpperCase(chars[0]);" );
        println( "         return new String(chars);" );
        println( "    }" );
        println();

        // TODO for now, have this class in each DesignInfo class. Need to move to a jar file
        createDataClassInstanceCustomizer();

        // End of class
        println( "}" );
    }

    private void createDataClassInstanceCustomizer() {
        println( "    /**" );
        println( "     * This customizer for setting the data class instance on a data provider" );
        println( "     */" );
        println( "    public class DataClassInstanceCustomizer extends BasicCustomizer2 {" );
        println( "        // The bean where the property from" );
        println( "        protected DesignBean targetBean;" );
        println();
        println( "        // The design property to be set" );
        println( "        protected DesignProperty targetProperty;" );
        println();
        println( "        // The bean instance from one the following design beans will be set to the target property" );
        println( "        protected DesignBean[] sourceBeans;" );
        println();
        println( "        // The List to display the bean instance to be selected from" );
        println( "        private javax.swing.JList beanInstancesList;" );
        println();
        println( "        /**" );
        println( "         * Constructor" );
        println( "         *" );
        println( "         * @param name The title of the customizer" );
        println( "         * @param targetProperty The property this customizer is designed to set" );
        println( "         * @param sourceBeans" );
        println( "         */" );
        println( "        public DataClassInstanceCustomizer(String name, DesignBean targetBean, DesignProperty targetProperty, DesignBean[] sourceBeans) { " );
        println( "            super( null, name ); " );
        println( "            setApplyCapable( false );" );
        println( "            this.targetProperty = targetProperty;" );
        println( "            this.targetBean = targetBean;" );
        println( "            this.sourceBeans = sourceBeans;" );
        println( "        }" );
        println();
        println( "        protected java.awt.Component createCustomizerPanel() {" );
        println();
        println( "            javax.swing.JPanel panel = new javax.swing.JPanel();" );
        println();
        println( "            java.awt.GridBagConstraints gridBagConstraints;" );
        println();
        println( "            javax.swing.JScrollPane listScrollPane = new javax.swing.JScrollPane();" );
        println( "            beanInstancesList = new javax.swing.JList();" );
        println( "            javax.swing.JLabel label = new javax.swing.JLabel();" );
        println();
        println( "            panel.setLayout(new java.awt.GridBagLayout());" );
        println();
        println( "            beanInstancesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);" );
        println( "            beanInstancesList.setVisibleRowCount(4);" );
        println( "            listScrollPane.setViewportView(beanInstancesList);" );
        println();
        println( "            gridBagConstraints = new java.awt.GridBagConstraints();" );
        println( "            gridBagConstraints.gridx = 0;" );
        println( "            gridBagConstraints.gridy = 1;" );
        println( "            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;" );
        println( "            gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 12);" );
        println( "            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;" );
        println( "            gridBagConstraints.weightx = 1.0;" );
        println( "            gridBagConstraints.weighty = 1.0;" );
        println( "            panel.add(listScrollPane, gridBagConstraints);" );
        println();
        println( "            label.setText(\"Select a " + dataProviderInfo.getClientWrapperClassName() + " instance to set on data provider instance \" + targetBean.getInstanceName() );" );
        println( "            gridBagConstraints = new java.awt.GridBagConstraints();" );
        println( "            gridBagConstraints.gridx = 0;" );
        println( "            gridBagConstraints.gridy = 0;" );
        println( "            gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;" );
        println( "            gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);" );
        println( "            gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;" );
        println( "            panel.add(label, gridBagConstraints);" );
        println();
        println( "            String[] beanInstanceNames = new String[ sourceBeans.length ];" );
        println( "            for( int i = 0; i < sourceBeans.length; i ++ ) {" );
        println( "                String parentInstance = sourceBeans[i].getBeanParent().getInstanceName();" );
        println( "                beanInstanceNames[i] = sourceBeans[i].getInstanceName() + \"(\" + parentInstance + \")\";" );
        println( "            }" );
        println( "            beanInstancesList.setListData( beanInstanceNames );" );
        println();
        println(              "// Have the first one selected by default" );
        println( "            beanInstancesList.setSelectedIndex(0);" );
        println();
        println( "            return panel;" );
        println( "        }" );
        println();
        println( "        public Result applyChanges() {" );
        println();
        println( "            int index = beanInstancesList.getSelectedIndex();" );
        println( "            DesignBean selectedBean = sourceBeans[index];" );
        println( "            if( designBean.getBeanParent().getInstanceName().equals( selectedBean.getBeanParent().getInstanceName() ) ) {" );
        println( "                targetProperty.setValue( sourceBeans[index].getInstance() );" );
        println( "            }" );
        println( "            else {" );
        println( "                String pInstanceName = capitalize( selectedBean.getBeanParent().getInstanceName() );" );
        println( "                String selectedInstanceName = capitalize( selectedBean.getInstanceName() );" );
        println();
        println( "                String sourceBeanStr = \"get\" + pInstanceName + \"().get\" + selectedInstanceName + \"()\"; // NOI18N" );
        //println( "                System.out.println( \".....sourceBeanStr: \" + sourceBeanStr );" );
        println( "                targetProperty.setValueSource( sourceBeanStr );" );
        println( "            }" );
        println();
        println( "            return Result.SUCCESS;" );
        println( "        }" );
        println();
        println("         public DisplayAction[] getContextItems(DesignBean designBean) {");
        println("             return new DisplayAction[0];");
        println("         }" );
        println( "    }" );
    }

}
