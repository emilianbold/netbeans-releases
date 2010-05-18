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
 * RowSetSelection.java
 *
 * Created on June 5, 2005, 4:19 PM
 */

package  org.netbeans.modules.visualweb.dataconnectivity.customizers ;

import org.netbeans.modules.visualweb.dataconnectivity.DataconnectivitySettings;
import org.netbeans.modules.visualweb.dataconnectivity.Log;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProject;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.faces.FacesDesignProject;
import com.sun.sql.rowset.CachedRowSetX;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;


/**
 * This class determines the rowset to be used with a dataprovider.
 * Based on the DesignContext and the tablename, we first compile a
 * list of all matching rowsets (same tablename, etc) and also possible
 * request/session/application beans.
 * it also constructs a panel to allow picking one of the rowsets.
 *
 * @author  jfbrown
 */
public class RowSetSelection extends javax.swing.JPanel {

    private final String tableName;
    private final String bareTableName;
    private final String user ;
    private final String url ;
    private final String command ;
    private final DesignContext curContext ;

    private String dataSourceName ; // Short name
    private String fullDataSourceName ; // full name (with jfbe

    private static final String selectText = NbBundle.getMessage(RowSetSelection.class, "RowSelectButton_label");
    private static final String createText = NbBundle.getMessage(RowSetSelection.class, "RowCreateButton_label");

    /****
     * when something is selected by the user, one of these two will be set.
     */
    public DesignBean selectedDesignBean = null ;
    public DesignContext createContext = null ;

    private static String javacomp = "java:comp/env/jdbc/" ;

    /**
     * Create the thing.  No UI is created by this constructor.
     * Rather, we just search for rowsets and dataproviders.
     */
    public RowSetSelection( DesignContext currentContext, String tableName, String dataSrcName, String user, String url, String command) {

        this.curContext = currentContext ;
        this.tableName = tableName ;

        int bt = tableName.lastIndexOf('.') ;
        if ( bt >= 0 ) {
            this.bareTableName = tableName.substring(bt+1) ;
        } else {
            this.bareTableName = tableName ;
        }

        if ( dataSrcName != null  ) {
            if ( dataSrcName.startsWith(javacomp)) dataSrcName = dataSrcName.substring(javacomp.length()) ;
        }
        this.dataSourceName = dataSrcName ;
        this.fullDataSourceName = "java:comp/env/jdbc/" + dataSourceName ; // NOI18N

        this.user = user ;
        this.url = url ;
        this.command = command ;

        findRowSets() ;

    }

    // For performance improvement. No need to get all the contexts in the project
    private DesignContext[] getDesignContexts(DesignContext context){
        DesignProject designProject = context.getProject();
        DesignContext[] contexts;
        if (designProject instanceof FacesDesignProject) {
            contexts = ((FacesDesignProject)designProject).findDesignContexts(new String[] {
                "request",
                "session",
                "application"
            });
        } else {
            contexts = new DesignContext[0];
        }
        DesignContext[] designContexts = new DesignContext[contexts.length + 1];
        designContexts[0] = context;
        System.arraycopy(contexts, 0, designContexts, 1, contexts.length);
        return designContexts;
    }
    
    ArrayList mCommands = new ArrayList() ;
    ArrayList mTables = new ArrayList() ;
    ArrayList cBeans_thisPage = new ArrayList() ;
    ArrayList cBeans_request = new ArrayList() ;
    ArrayList cBeans_session = new ArrayList() ;
    ArrayList cBeans_application = new ArrayList() ;
    ArrayList<DesignBean> alRowSets = new ArrayList<DesignBean>(); 
    private void findRowSets() {


        String thisScope = (String)curContext.getContextData(Constants.ContextData.SCOPE) ;  

        //DesignContext[] contexts = curContext.getProject().getDesignContexts() ;
 	DesignContext[] contexts = getDesignContexts(curContext);
        
        for ( int i = 0 ; i < contexts.length ; i++  ) {
            String cScope = (String)contexts[i].getContextData(Constants.ContextData.SCOPE) ;
            Log.log("RSS:  examining " + contexts[i].getDisplayName() + " , " + cScope ) ;
            if ( compareScopes(thisScope,  cScope) < 0 ) {
                // ignore if "same" scope.
                continue ;
            }
            if ( SCOPE_PAGE.equals(thisScope) && contexts[i] != curContext ) {
                // ignore if PAGE and not self.
                continue ;
            }
            if ( SCOPE_REQUEST.equals(thisScope) && SCOPE_REQUEST.equals(cScope) ) {
                // If request Scope, loop context is also request
                if ( contexts[i] != curContext &&  (contexts[i].getDisplayName().indexOf("RequestBean") < 0) ) {
                    // ignore if REQUEST and (not self or not RequestBean)
                    continue ;
                }
                if ( contexts[i] == curContext && contexts[i].getDisplayName().indexOf("RequestBean") < 0) {
                    cBeans_thisPage.add(curContext) ;
                }
            }
            DesignBean[] rowsets = contexts[i].getBeansOfType( CachedRowSetX.class ) ;                       
            for ( int j = 0 ; j < rowsets.length ; j++ ) {                    
                if (alRowSets.contains(rowsets[j])) {
                    continue;
                }

                alRowSets.add(rowsets[j]);               
                int matchVal = compareRowSet( rowsets[j]) ;
                if ( Log.isLoggable() ) {
                    Log.log( "RSS: " + contexts[i].getDisplayName() + "." + rowsets[j].getInstanceName()+ " match="+matchVal) ;
                }
                if ( matchVal == 1 ) {
                    mTables.add( rowsets[j] ) ;
                } else if ( matchVal == 2 ) {
                    mCommands.add( rowsets[j]) ;
                }
            }

            // add non-page "create" contexts            
            if ( contexts[i].getDisplayName().indexOf("RequestBean") >= 0) {      
                if (!cBeans_request.contains(contexts[i])) {
                    cBeans_request.add(contexts[i]);
                }
            } else if (contexts[i].getDisplayName().indexOf("SessionBean") >= 0) {
                if (!cBeans_session.contains(contexts[i])) {
                    cBeans_session.add(contexts[i]);
                }
            } else if (contexts[i].getDisplayName().indexOf("ApplicationBean") >= 0) {
                if (!cBeans_application.contains(contexts[i])) {
                    cBeans_application.add(contexts[i]);
                }
            }             
        }
    }
    public boolean hasMatchingRowSets() {
        boolean nomatches =  ( mCommands.size() == 0 && mTables.size() == 0 ) ;
        return ! nomatches ;
    }
    /***
     * returns an array of DesignContext objects of the given scope or higher.
     */
    public Object[] getCreateBeans(String scope) {
        if ( scope == null ) scope = SCOPE_SESSION ; // default
        if ( scope.equals( SCOPE_REQUEST )) return cBeans_request.toArray() ;
        if ( scope.equals( SCOPE_SESSION )) return cBeans_session.toArray() ;
        if ( scope.equals( SCOPE_APPLICATION )) return cBeans_application.toArray() ;
        throw new IllegalArgumentException("RSS arg eror:  scope is '" + scope + "'") ; // NOI18N
    }

    /****
     * Now build the panel showing the matches.
     * first, list the command matches.
     * then, list the tableName matches.
     * then, list the request/session/application context instances.
     */
    Color roBgColor = null ;
    private void buildSelectionPanel() {
        TextField tf = new TextField() ;
        tf.setEditable(false) ;
        roBgColor = tf.getBackground() ;

        initComponents();

        titleLabel.setBackground(roBgColor) ;

        GridBagConstraints constraints = new GridBagConstraints(
            GridBagConstraints.RELATIVE, 0 , 1,1, // int gridx, int gridy, int gridwidth, int gridheight,
            0.0, 0.0,   // double weightx, double weighty,
            GridBagConstraints.CENTER, GridBagConstraints.NONE, // int anchor, int fill,
            new java.awt.Insets(5,5,5,5), 0, 0)  ; // Insets insets, int ipadx, int ipady)    
        GridBagConstraints seperatorConstraints = new GridBagConstraints(
            GridBagConstraints.RELATIVE, 0 , 3,1, // int gridx, int gridy, int gridwidth, int gridheight,
            1.0, 0.0,   // double weightx, double weighty,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, // int anchor, int fill,
            new java.awt.Insets(5,5,5,5), 0, 0)  ; // Insets insets, int ipadx, int ipady)

        AbstractButton defaultButton = null ;
        BeanColumnPanel defaultBeanColumn = null ;
        int currentRow = -1 ;
        int numberOfBeans = 0 ;
        for ( int i = 0 ; i < 7 ; i++ ) {
            ArrayList curList = null ;
            boolean select = false ;
            boolean separator = false ;
            switch(i) {
                case 0:  curList = mCommands ; select = true ; break ;
                case 1:  curList = mTables ;  select = true ; break ;
                case 2:  separator = true  ; break ;
                case 3:  curList = cBeans_thisPage ; select = false ; break ;
                case 4:  curList = cBeans_request ; select = false ; break ;
                case 5:  curList = cBeans_session ; select = false ; break ;
                case 6:  curList = cBeans_application ; select = false ;break ;
            }
            if ( separator ) {
                // add a seperator
               seperatorConstraints.gridy = ++currentRow ;
               selectionPanel.add( new JSeparator(), seperatorConstraints) ;
               continue ;
            }
            if ( curList == null ) {
               continue ;
            }
            for ( int j = 0, max=curList.size() ; j < max ; j++ ) {
                constraints.gridy = ++currentRow ;
                constraints.weightx = 0 ;  
                constraints.fill = GridBagConstraints.NONE ; // reset

                AbstractButton doButton = null ;
                if ( select ) {
                    doButton = makeButtonColumn(selectText) ;
                } else {
                    doButton = makeButtonColumn(createText ) ;
                }
                
                BeanColumnPanel beanColumn = null ;
                JComponent commandColumn = null ;
                if ( select ) {
                    DesignBean db = (DesignBean)curList.get(j) ;
                    beanColumn = makeBeanColumn(db.getDesignContext(), db, doButton  ) ;
                    try {
                        if ( db.getInstance() instanceof CachedRowSetX ) {
                            CachedRowSetX rs = (CachedRowSetX)db.getInstance() ;
                            commandColumn = makeCommandColumn( rs.getCommand() ) ;

                        } else {
                            throw new RuntimeException("Internal error:  not a CachedRowSetX.") ; // NOI18N
                        }
                    } catch( Exception se) { // catch runtime exceptions.
                        commandColumn =  makeCommandColumn( se.getLocalizedMessage() ) ;
                        doButton.setEnabled(false) ;
                    }
                } else {
                    beanColumn = makeBeanColumn( ((DesignContext)curList.get(j)), null, doButton) ;
                    commandColumn = makeCommandColumn( command ) ;
                }

                if ( doButton.isEnabled() && defaultButton == null ) {
                    defaultButton = doButton ;
                    defaultBeanColumn = beanColumn ;
                }
                selectionPanel.add( doButton , constraints ) ;
                selectionPanel.add( beanColumn, constraints ) ;
                constraints.weightx = 1.0 ;
                constraints.fill = GridBagConstraints.BOTH ;
                selectionPanel.add(commandColumn, constraints ) ;
                numberOfBeans += 1 ;
                doButton.addActionListener( new SelectionListener( beanColumn ) );
            }
        }

        if ( numberOfBeans < 1 ) {
            // nowhere to add it - Should never be here.
            selectionPanel.add( new JLabel("No beans with rowsets.") ) ; // NOI18N
        } else {
            // DataconnecitivityUIUtils.equalizeButtonPreferredSizes( (JComponent[])beanList.toArray(new JPanel[beanList.size()]) , 0 ,0 ) ;
            defaultButton.setSelected(true) ;
            setSelectedBean( defaultBeanColumn ) ;
        }
        // add something to fill the bottom.
        constraints.gridy = ++currentRow ;
        constraints.weightx = 0 ; constraints.weighty = 1.0 ;
        constraints.gridwidth = 4 ;
        constraints.fill = GridBagConstraints.BOTH ; // reset
        selectionPanel.add( new JPanel(), constraints ) ;
        
    }


    public class SelectionListener implements ActionListener {
        BeanColumnPanel beanpanel ;
        public SelectionListener( BeanColumnPanel bp) {
            beanpanel = bp ;
        }
        public void actionPerformed(ActionEvent e) {
            if ( ((AbstractButton)e.getSource()).isEnabled() ) {
                setSelectedBean( beanpanel ) ;
            }
        }
    }

    private BeanColumnPanel selectedPanel = null ;
    private void setSelectedBean( BeanColumnPanel beanPanel ) {
         selectedPanel = beanPanel ;
    }
    public DesignContext getSelectedDesignContext() {
        return selectedPanel.context ;
    }
    public DesignBean getSelectedRowSetBean() {
        return selectedPanel.rowSetBean ;
    }
    public String getSelectedRowSetName() {
        return selectedPanel.rowsetName.getText() ;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        selections = new javax.swing.ButtonGroup();
        titleLabel = new javax.swing.JTextArea();
        jSeparator1 = new javax.swing.JSeparator();
        rowsetPane = new javax.swing.JScrollPane();
        selectionPanel = new javax.swing.JPanel();
        bottomLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RowSetSelection.class, "RowSetDialogTitle"));
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RowSetSelection.class, "RowSetDialogTitle"));
        titleLabel.setEditable(false);
        titleLabel.setLineWrap(true);
        titleLabel.setText(org.openide.util.NbBundle.getMessage(RowSetSelection.class, "RowSetTitle1_label", new Object[] {bareTableName}));
        titleLabel.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(titleLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(jSeparator1, gridBagConstraints);

        selectionPanel.setLayout(new java.awt.GridBagLayout());

        rowsetPane.setViewportView(selectionPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(rowsetPane, gridBagConstraints);

        bottomLabel.setText(org.openide.util.NbBundle.getMessage(RowSetSelection.class, "RowSetFooter1_label", new Object[] {}));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(bottomLabel, gridBagConstraints);
        bottomLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RowSetSelection.class, "RowSetFooter1_label"));

    }
    // </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bottomLabel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JScrollPane rowsetPane;
    private javax.swing.JPanel selectionPanel;
    private javax.swing.ButtonGroup selections;
    private javax.swing.JTextArea titleLabel;
    // End of variables declaration//GEN-END:variables

    public AbstractButton  makeButtonColumn(String text /*,ActionListener al)*/ ) {
        JRadioButton xx = new JRadioButton(text) ;
        xx.setBorder(new EmptyBorder(5,5, 0,0)) ;
        // xx.setMnemonic(text.charAt(0));
        xx.getAccessibleContext().setAccessibleDescription(xx.getText());
        selections.add( xx ) ;
        addToTabOrder( xx ) ;
        return xx ;
    }

    public BeanColumnPanel makeBeanColumn( DesignContext context, DesignBean rowSetBean, AbstractButton columnButton ) {
        BeanColumnPanel jp = new BeanColumnPanel(context, rowSetBean) ;
        JLabel xxLabel = new JLabel(context.getDisplayName()) ;
        xxLabel.getAccessibleContext().setAccessibleDescription(xxLabel.getText() );
        xxLabel.getAccessibleContext().setAccessibleName(xxLabel.getText());
        jp.add(xxLabel) ;
        JComponent xxField = null ;
        if ( rowSetBean != null ) {
            xxField = new JLabel(rowSetBean.getInstanceName()) ;
            xxField.getAccessibleContext().setAccessibleDescription( rowSetBean.getInstanceName() );
            xxField.getAccessibleContext().setAccessibleName( rowSetBean.getInstanceName() );
        } else {
            String defaultName = getRowSetNameForContext(context, null) ;
            JTextField tf = new JTextField(defaultName) ;
            tf.setColumns(15) ;

            // PlainDocument nameDoc = new PlainDocument() ;
            tf.getDocument().addDocumentListener( new DocListener( columnButton, jp) ) ;
            // tf.setDocument(nameDoc) ;
            xxLabel.setLabelFor(xxField);
            addToTabOrder( tf ) ;
   
           // either set the tooltip or the a11y description 
           tf.setName(context.getDisplayName());
           tf.getAccessibleContext().setAccessibleDescription(context.getDisplayName());
   
            xxField = (JComponent)tf ;
            jp.rowsetName = tf ;
        }
        jp.add(xxField) ;
        return jp ;
    }

    /***
     * this class holds the file name (context) and bean name (rowset name)
     */
    public class BeanColumnPanel extends JPanel {
        public DesignContext context = null ; // never be null
        public DesignBean rowSetBean = null ; // may be null
        public JTextField rowsetName = null ;
        public BeanColumnPanel( DesignContext ctx, DesignBean designBean ) {
            super(new GridLayout(0,1)) ;
            this.context = ctx ;
            this.rowSetBean = designBean ;
            String bStuff = "Bean to Use or Create" ; //NOI18N
            this.getAccessibleContext().setAccessibleName(bStuff) ;
            this.getAccessibleContext().setAccessibleDescription(bStuff);
        }
    }
    /***
     * Construct a valid rowset name (unique within the context if a context is
     * provided.
     * if tablename is not provided, start with "bareTableName"+DataconnectivitySettings.getRsSuffix(), otherwise
     * start with provided tableName.
     * Within the provided context, make sure there are no other rowsets with
     * the same name
     */
    private String getRowSetNameForContext(DesignContext context, String testTableName) {
        DesignBean[] dbeans = null ;
        if ( context != null ) {
            dbeans = context.getBeansOfType(CachedRowSetX.class) ;
        }
        if ( testTableName == null ) {
            testTableName = bareTableName.toLowerCase().replaceAll(" ", "") + DataconnectivitySettings.getRsSuffix() ;
        }
        String lookForName = testTableName ;
        if ( dbeans != null ) {
            for ( int i = 0 ; i < 333 ; i++ ) {
                if ( i > 0 ) lookForName = testTableName + Integer.toString(i) ;
                boolean exists = false ;
                for ( int j = 0 ; j < dbeans.length ; j++ ) {
                    if ( lookForName.equals(dbeans[j].getInstanceName()))  {
                        exists = true ;
                        break ;
                    }
                }
                if ( ! exists ) {
                    break ;
                }
            }
        }
        return lookForName ;
    }

    public JComponent makeDataSourceColumn(String tableName, String ds, String user, String url) {
        java.awt.LayoutManager lm = new GridLayout(0,1) ;
        JPanel jp = new JPanel(lm) ;
        jp.add( new JLabel(tableName)) ;
        jp.add( new JLabel(ds)) ;
        jp.add( new JLabel(user)) ;
        return jp ;
    }

    public JComponent makeCommandColumn(String text ) {
        return new RowSetSelectionQuery( text ) ;
    }

    private static final String SCOPE_PAGE = "page" ; // ??unused??
    private static final String SCOPE_REQUEST = "request" ;
    private static final String SCOPE_SESSION = "session" ;
    private static final String SCOPE_APPLICATION = "application" ;

    // navigate up the scope hierarchy
    private static String getNextScope( String curScope ) {
        if ( curScope.equals(SCOPE_PAGE)) return SCOPE_REQUEST ;
        if ( curScope.equals(SCOPE_REQUEST)) return SCOPE_SESSION ;
        if ( curScope.equals(SCOPE_SESSION)) return SCOPE_APPLICATION ;
        return null ;
    }

    /***
     * compare scopes
     * returns <0 if scope1 < scope2
     *  0 if scope1 == scope2
     *  >0 if scope1 > scope2
     * -99 if either scope1 or scope2 is "unknown".
     **/
    private static int  compareScopes( String scope1, String scope2 ) {

        if ( scope1.equals(SCOPE_PAGE)) {
            if ( scope2.equals(SCOPE_PAGE)) {
                return 0 ;
            } else if ( scope2.equals(SCOPE_REQUEST)) {
                return 1 ;
            } else if ( scope2.equals(SCOPE_SESSION)) {
                return 2 ;
            } else if ( scope2.equals(SCOPE_APPLICATION)) {
                return 3 ;
            }
            return -99 ;
        }
        if ( scope1.equals(SCOPE_REQUEST)) {
            if ( scope2.equals(SCOPE_PAGE)) {
                return -1 ;
            } else if ( scope2.equals(SCOPE_REQUEST)) {
                return 0 ;
            } else if ( scope2.equals(SCOPE_SESSION)) {
                return 1 ;
            } else if ( scope2.equals(SCOPE_APPLICATION)) {
                return 2 ;
            }
            return -99 ;
        }
        if ( scope1.equals(SCOPE_SESSION)) {
            if ( scope2.equals(SCOPE_PAGE)) {
                return -2 ;
            } else if ( scope2.equals(SCOPE_REQUEST)) {
                return -1 ;
            } else if ( scope2.equals(SCOPE_SESSION)) {
                return 0 ;
            } else if ( scope2.equals(SCOPE_APPLICATION)) {
                return 1 ;
            }
            return -99 ;
        }
        if ( scope1.equals(SCOPE_APPLICATION)) {
            if ( scope2.equals(SCOPE_PAGE)) {
                return -3 ;
            } else if ( scope2.equals(SCOPE_REQUEST)) {
                return -2 ;
            } else if ( scope2.equals(SCOPE_SESSION)) {
                return -1 ;
            } else if ( scope2.equals(SCOPE_APPLICATION)) {
                return 0 ;
            }
            return -99 ;
        }
        // assume PAGE scope if scope1 is not known.
        if ( scope2.equals(SCOPE_PAGE)) {
            return 0 ;
        } else if ( scope2.equals(SCOPE_REQUEST)) {
            return 1 ;
        } else if ( scope2.equals(SCOPE_SESSION)) {
            return 2 ;
        } else if ( scope2.equals(SCOPE_APPLICATION)) {
            return 3 ;
        }
        return -99 ;
    }
    /***
     * compare a design bean to the table of this instance.
     * return an int representing how well it matches.
     * < 0 = bad, does not match dataSourceName, url, or username.
     * 0 = bad, dataSourceName match, but no table or command match.
     * 1 = good, tableName matches
     * 2 = excellent, command matches
     */
    private int compareRowSet( DesignBean rowset ) {

        if ( ! DataconnectivitySettings.checkRowsets() ) return -3 ;

        // first compare dataSourceName
        DesignProperty prop = rowset.getProperty("dataSourceName"); // NOI18N
        if ( prop == null || fullDataSourceName == null ) {
            return -2 ;
        }
        if ( ! fullDataSourceName.equals(prop.getValue())) {
            return -2 ;
        }

        // compare url - if not equal, return -1
        // TODO:  skip for EA

        // compare username - if not equal, return -1
        // TODO:  skip for EA

        // compare command - if equal, return 2
        prop = rowset.getProperty("command"); // NOI18N
        if ( prop != null && command != null ) {
            if ( command.equals(prop.getValue() ) ) {
                return 2 ;
            } else {
                if ( prop.getValue() == null ) {
                // check for NULL command.
                    return -2 ;
                } else if ( "".equals((String)prop.getValue()) ) {
                    return -2 ;
                }
            }
        }

        // compare tableName - if equal, return 1
        prop = rowset.getProperty("tableName"); // NOI18N
        if ( prop != null && tableName != null ) {
            if ( tableName.equals(prop.getValue() ) ) {
                return 1 ;
            }
        }
        if ( prop != null && bareTableName != null ) {
            if ( bareTableName.equals(prop.getValue() ) ) {
                return 1 ;
            }
        }

        // bad, but at least dataSource stuff matches.
        return 0 ;
    }

    private JDialog dialog;
    private static JButton okButton =  new JButton( NbBundle.getMessage(RowSetSelection.class, "RowSetDialogAcceptButton")) ;
    private static JButton cancelButton =  new JButton( NbBundle.getMessage(RowSetSelection.class, "RowSetDialogCancelButton")) ;

// for calculating return value ;
    private boolean makeStuff = false ;
    public boolean showDialog() {
        // Add a listener to the dialog's buttons
        this.buildSelectionPanel() ;

        okButton.getAccessibleContext().setAccessibleDescription(okButton.getText() ) ;
        cancelButton.getAccessibleContext().setAccessibleDescription(cancelButton.getText() ) ;
        // listener to the dialog's buttons
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {

                if (evt.getSource() == okButton) {
                    makeStuff = true ;
                } else {
                    makeStuff = false ;
                }
                dialog.dispose() ;
            }
        };
        DialogDescriptor dlg = new DialogDescriptor(this, NbBundle.getMessage(RowSetSelection.class, "RowSetDialogTitle", bareTableName), 
                true, listener );
        dlg.setOptions(new Object[] {
           okButton, cancelButton } );
        dlg.setHelpCtx( new org.openide.util.HelpCtx("projrave_ui_elements_server_nav_add_new_data_provider") ) ;
        dlg.setClosingOptions( null );

        dialog = (JDialog) DialogDisplayer.getDefault().createDialog(dlg);

        dialog.setResizable(true);
        dialog.pack();
        Dimension dSize = dialog.getSize() ;
        final double maxSize=575 ;
        if ( dSize.getHeight() > maxSize ) {
            dSize.setSize( dSize.getWidth(), maxSize ) ;
            dialog.setSize(dSize) ;
        }
        
        // HACK alert - fix tabbing order.  The only way to get the
        // help button is via looking up the children of the contentPane.
        Tabbing tabber = new Tabbing() ;
        Component[] comps = dialog.getContentPane().getComponents() ;
        if ( comps.length > 0 ) {
            Component comp = comps[1] ; /// JPanel with buttons
            if ( comp instanceof Container ) { // should always be true
                Component[] buttons = ((Container)comp).getComponents() ;
                for ( int j = 0 ; j < buttons.length ; j++ ) {
                    if ( buttons[j] instanceof JButton ) { // should always be true
                        tabber.addToTab( buttons[j]) ;
                    }
                }
            }
        }
        tabber.addToTab( rowsetPane ) ;
        dialog.setFocusTraversalPolicy( tabber ) ;
        
        dialog.setVisible(true);

        return makeStuff ;
    }

    /***
     * listener for the "roset Name" field.  When touched, select the
     * appropriate button.
     */
    public class DocListener implements javax.swing.event.DocumentListener {

        AbstractButton button = null ;
        BeanColumnPanel beanPanel = null ;
        public DocListener(AbstractButton jb, BeanColumnPanel beanColumnPanel) {
            button = jb ;
            beanPanel = beanColumnPanel ;
        }

        public void insertUpdate(DocumentEvent ev) {
            update(ev) ;
        }
        public void changedUpdate(DocumentEvent ev) {
            update(ev) ;
        }
        public void removeUpdate(DocumentEvent ev) {
            update(ev) ;
        }

        private void update(DocumentEvent ev){
            selections.setSelected( button.getModel(), true ) ;
            setSelectedBean(beanPanel) ;
        }
    }

    ArrayList panelTabOrderList = new ArrayList(12) ;
    protected void addToTabOrder( Component o) {
        panelTabOrderList.add(o) ;
    }
    public class Tabbing extends FocusTraversalPolicy {
        ArrayList tabOrderList = new ArrayList(15) ;
        public Tabbing() {
            tabOrderList = (ArrayList)panelTabOrderList.clone() ;
        }
        public void addToTab(Component o) {
            tabOrderList.add(o) ;
        }
        public java.awt.Component getComponentBefore(java.awt.Container focusCycleRoot, java.awt.Component aComponent) {
            for ( int i = 0 ; i < tabOrderList.size() ; i++ ) {
                if ( aComponent == tabOrderList.get(i) ) {
                    if ( i == 0 ) i = tabOrderList.size()-1 ;
                    else i = i - 1 ;
                    return (Component)tabOrderList.get(i) ;
                }
            }
            return (Component)tabOrderList.get(0) ;
        }

        public java.awt.Component getComponentAfter(java.awt.Container focusCycleRoot, java.awt.Component aComponent) {
            for ( int i = 0 ; i < tabOrderList.size() ; i++ ) {
                if ( aComponent == tabOrderList.get(i) ) {
                    if ( i == tabOrderList.size()-1 ) i = 0 ;
                    else i = i + 1 ;
                    return (Component)tabOrderList.get(i) ;
                }
            }
            return (Component)tabOrderList.get(tabOrderList.size()-1 ) ;
        }

        public java.awt.Component getLastComponent(java.awt.Container focusCycleRoot) {
            return (Component)tabOrderList.get(tabOrderList.size()-1 ) ;
        }

        public java.awt.Component getFirstComponent(java.awt.Container focusCycleRoot) {
            return (Component)tabOrderList.get(0) ;
        }

        public java.awt.Component getDefaultComponent(java.awt.Container focusCycleRoot) {
            return (Component)tabOrderList.get(0) ;
        }
        
    }
}
