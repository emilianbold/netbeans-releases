/*
 * MethodNode.java
 *
 * Created on May 3, 2004, 6:37 PM
 */

package org.netbeans.modules.visualweb.ejb.nodes;
import org.netbeans.modules.visualweb.api.designerapi.DesignerServiceHack;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectClassPathExtender;
import com.sun.rave.designtime.BeanCreateInfo;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DisplayItem;
import com.sun.rave.designtime.Result;
import org.netbeans.modules.visualweb.ejb.actions.AddDataProviderToPageAction;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModel;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbInfo;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodInfo;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodParam;
import org.netbeans.modules.visualweb.ejb.load.EjbLoaderHelper;
import org.netbeans.modules.visualweb.ejb.util.InvalidParameterNameException;
import org.netbeans.modules.visualweb.ejb.util.MethodParamValidator;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyEditor;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.actions.PropertiesAction;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import org.openide.nodes.Sheet.Set;
import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.ExTransferable;


/**
 * The node representing a business method in an EJB
 *
 * @author  cao
 */
public class MethodNode extends AbstractNode /*implements RavePaletteItemSetCookie*/
{
    private MethodInfo methodInfo;
    private EjbGroup ejbGroup;
    private EjbInfo ejbInfo;
    
//    private DataProviderPaletteItem beanPaletteItem;
    
    public MethodNode( EjbGroup ejbGrp, MethodInfo mInfo, EjbInfo ejbInfo ) 
    {
        super( Children.LEAF );
        
        ejbGroup= ejbGrp;
        methodInfo = mInfo;
        this.ejbInfo = ejbInfo;

        setName( methodInfo.getName() );
        setDisplayName( methodInfo.getName() );
        setShortDescription( methodInfo.toString() );
    }
    
    // Create the popup menu:
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get( AddDataProviderToPageAction.class ),
            SystemAction.get( PropertiesAction.class ),
        };
    }
    
    public Action getPreferredAction() {
        // Whatever is most relevant to a user:
        return SystemAction.get(PropertiesAction.class);
    }
    
    public Image getIcon(int type){
        return getMethodIcon();
    }
    
    public Image getOpenedIcon(int type){
        return getMethodIcon();
    }
    
    private Image getMethodIcon() {
        if( !methodInfo.getReturnType().isVoid() )
        {
            Image image1 = Utilities.loadImage("org/netbeans/modules/visualweb/ejb/resources/methodPublic.gif");
            Image image2 = Utilities.loadImage("org/netbeans/modules/visualweb/ejb/resources/table_dp_badge.png");
            int x = image1.getWidth(null) - image2.getWidth(null);
            int y = image1.getHeight(null) - image2.getHeight(null);
            return Utilities.mergeImages( image1, image2, x, y);
        }
        else
            return Utilities.loadImage("org/netbeans/modules/visualweb/ejb/resources/methodPublic.gif");
    }
    
    public HelpCtx getHelpCtx() 
    {
        return new HelpCtx("projrave_ui_elements_server_nav_ejb_node");
    }
    
    public MethodInfo getMethodInfo()
    {
        return this.methodInfo;
    }
    
    protected Sheet createSheet() 
    {
        Sheet sheet = super.createSheet();
        Set ss = sheet.get("methodInfo"); // NOI18N
        
        if (ss == null) {
            ss = new Set();
            ss.setName("methodInfo");  // NOI18N
            ss.setDisplayName( NbBundle.getMessage(MethodNode.class, "METHOD_INFORMATION") );
            ss.setShortDescription( NbBundle.getMessage(MethodNode.class, "METHOD_INFORMATION") );
            sheet.put(ss);
        }
        
        // Method Return
        Set returnSet = sheet.get("methodReturn"); // NOI18N
        if (returnSet == null) {
            returnSet = new Sheet.Set();
            returnSet.setName("methodReturn"); // NOI18N
            returnSet.setDisplayName( NbBundle.getMessage(MethodNode.class, "METHOD_RETURN") );
            returnSet.setShortDescription(NbBundle.getMessage(MethodNode.class, "METHOD_RETURN") );
            sheet.put(returnSet);
        }

        returnSet.put( new PropertySupport.ReadOnly( "returnType", // NOI18N
                        String.class,
                        NbBundle.getMessage(MethodNode.class, "RETURN_TYPE"),
                        NbBundle.getMessage(MethodNode.class, "RETURN_TYPE") ) {
                            
            public Object getValue() {
                return methodInfo.getReturnType().getClassName();
            }
        });
        
        if( methodInfo.getReturnType().isCollection() )
        {
            returnSet.put( new PropertySupport.ReadWrite( "elementType", // NOI18N
                            String.class,
                            NbBundle.getMessage(MethodNode.class, "RETURN_COL_ELEM_TYPE"),
                            NbBundle.getMessage(MethodNode.class, "RETURN_COL_ELEM_TYPE") ) {
                                
                 public PropertyEditor getPropertyEditor () {
                     //TODO
                     return null;
                       
                 }

                public Object getValue() {
                    String className = methodInfo.getReturnType().getElemClassName();
                    if( className == null )
                        className = NbBundle.getMessage(MethodNode.class, "RETURN_COL_ELEM_TYPE_NOT_SPECIFIED");
                        
                    return className;
                }

                public void setValue(Object val) {
                    String className = (String)val;
                    
                    // Make sure it is not the original <not specified> or a bunch of space or nothing
                    if( className == null || className.trim().length() == 0 ||
                        className.equals( NbBundle.getMessage(MethodNode.class, "RETURN_COL_ELEM_TYPE_NOT_SPECIFIED") ) )
                        className = null;
                    
                    // Make sure that the element class specified by the user is a valid one
                    try {
                        URLClassLoader classloader = EjbLoaderHelper.getEjbGroupClassLoader( ejbGroup );
                        Class c = Class.forName( className, true,  classloader );
                    }catch ( java.lang.ClassNotFoundException ce )
                    {
                        NotifyDescriptor d = new NotifyDescriptor.Message( "Class " + className + " not found", /*NbBundle.getMessage(MethodNode.class, "PARAMETER_NAME_NOT_UNIQUE", name ),*/ NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify( d );
                        return;
                    }
                    
                    methodInfo.getReturnType().setElemClassName( className );
                    EjbDataModel.getInstance().touchModifiedFlag();
                }
            });
        }
        
        // Signature
        ss.put( new PropertySupport.ReadOnly( "signature", // NOI18N
                                          String.class,
                                          NbBundle.getMessage(EjbGroupNode.class, "METHOD_SIGNATURE"),
                                          NbBundle.getMessage(EjbGroupNode.class, "METHOD_SIGNATURE") ) {
            public Object getValue() {
                return methodInfo.toString();
            }
        });

                   
        // Method name     
        ss.put( new PropertySupport.ReadOnly( "name", // NOI18N
                                          String.class,
                                          NbBundle.getMessage(EjbGroupNode.class, "METHOD_NAME"),
                                          NbBundle.getMessage(EjbGroupNode.class, "METHOD_NAME") ) {
            public Object getValue() {
                return methodInfo.getName();
            }
        });
        
        
        // Exceptions
        ss.put( new PropertySupport.ReadOnly( "exceptions", // NOI18N
                                          String.class,
                                          NbBundle.getMessage(EjbGroupNode.class, "EXCEPTIONS"),
                                          NbBundle.getMessage(EjbGroupNode.class, "EXCEPTIONS") ) {
            public Object getValue() {
                return methodInfo.getExceptionsAsOneStr();
            }
        });
        
        // Parameters tree
        Set paramSet = sheet.get("parameters"); // NOI18N
        if (paramSet == null) {
            paramSet = new Sheet.Set();
            paramSet.setName("parameters"); // NOI18N
            paramSet.setDisplayName( NbBundle.getMessage(MethodNode.class, "METHOD_PARAMETERS")); // NOI18N
            paramSet.setShortDescription( NbBundle.getMessage(MethodNode.class, "METHOD_PARAMETERS")); // NOI18N
            sheet.put(paramSet);
        }
        
        if( methodInfo.getParameters() != null && methodInfo.getParameters().size() != 0 )
        {
            for( int i = 0; i < methodInfo.getParameters().size(); i ++ )
            {
                final MethodParam p = (MethodParam)methodInfo.getParameters().get( i );
                
                // Parameter name     
                paramSet.put( new PropertySupport.ReadWrite( p.getName(),
                                                  String.class,
                                                  NbBundle.getMessage(MethodNode.class, "PARAMETER_NAME") ,
                                                  NbBundle.getMessage(MethodNode.class, "PARAMETER_NAME") ) {
                    public Object getValue() {
                        return p.getName();
                    }
                    
                    public void setValue(Object val) {
                        
                        String name = (String)val;
                        
                        if( name == null || name.trim().length() == 0 )
                            return;
                        else
                            name = name.trim();
                        
                        // Make sure it is a legal parameter name
                        try 
                        {
                            MethodParamValidator.validate( name );
                        } 
                        catch( InvalidParameterNameException e ) 
                        {
                            NotifyDescriptor d = new NotifyDescriptor.Message( e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notify( d );
                            
                            return;
                        }
                        
                        // Possible that the user didn't change at all
                        if( name.equals( p.getName() ) )
                            return;
                        else
                        {
                            // If the user did change the name, then need to make sure 
                            // the name is not used by the other parameters for the same method
                            if( !methodInfo.isParamNameUnique( name ) )
                            {
                                NotifyDescriptor d = new NotifyDescriptor.Message( NbBundle.getMessage(MethodNode.class, "PARAMETER_NAME_NOT_UNIQUE", name ), NotifyDescriptor.ERROR_MESSAGE);
                                DialogDisplayer.getDefault().notify( d );
                                return;
                            }
                        }
                        
                        
                        p.setName( name );
                        EjbDataModel.getInstance().touchModifiedFlag();
                    }
                });

                // Parameter type
                paramSet.put( new PropertySupport.ReadOnly( p.getName() + "ParameteType", // NOI18N
                                                  String.class,
                                                  NbBundle.getMessage(MethodNode.class, "PARAMETER_TYPE"),
                                                  NbBundle.getMessage(MethodNode.class, "PARAMETER_TYPE") ) {
                    public Object getValue() {
                        return p.getType();
                    }
                });
            }
        }
          
        return sheet;
    }
    
    
   // Methods for Drag and Drop (not used for copy / paste at this point)
    
   public boolean canCopy() {
       return isMethodDroppable();
    }
    
    public boolean canCut() {
        return isMethodDroppable();
    }
    
    private boolean isMethodDroppable() {
        if( methodInfo.getReturnType().isVoid() )
           return false;
       else
           return true;
    }
    
    public Transferable clipboardCopy() {
        
        if( !isMethodDroppable() )
            return null;
        
//        // If the bean palette item is not initialized, lets create one and add the lib references needed by the palette item to the project
//        if( beanPaletteItem == null ) {
//            beanPaletteItem = new DataProviderPaletteItem( ejbGroup, methodInfo );
//        }
        if (ejbGroup == null || methodInfo == null) {
            try {
                return super.clipboardCopy();
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }

        // Add to, do not replace, the default node copy flavor:
        try {
            ExTransferable transferable = ExTransferable.create(super.clipboardCopy());
            transferable.put( 
		// XXX TODO Shouldn't be used this flavor directly, rather providing
		// specific flavors of objects transfered, not PaletteItem -> get rid of the dep.
//                new ExTransferable.Single(PaletteItemTransferable.FLAVOR_PALETTE_ITEM) {
                new ExTransferable.Single(FLAVOR_METHOD_DISPLAY_ITEM) {
                    protected Object getData() {
//                        return beanPaletteItem;
                        return new MethodBeanCreateInfo(ejbGroup, methodInfo);
                    }
                } );
            
//            // Register with the designer so that it can be dropped/linked to the appropriate component
//            DesignerServiceHack.getDefault().registerTransferable(transferable);
            
            return transferable;
        } 
        catch (Exception ioe) 
        {
            System.err.println("MethodNode.clipboardCopy: Error");
            ioe.printStackTrace();
            return null;
        }
    }
    
    private static final DataFlavor FLAVOR_METHOD_DISPLAY_ITEM = new DataFlavor(
            DataFlavor.javaJVMLocalObjectMimeType + "; class=" + DisplayItem.class.getName(), // NOI18N
            "Ejb Method Display Item"); // XXX Localize
    
    private static class MethodBeanCreateInfo implements BeanCreateInfo {
        
        private final EjbGroup ejbGroup;
        private final MethodInfo methodInfo;
        
        public MethodBeanCreateInfo(EjbGroup ejbGroup, MethodInfo methodInfo) {
            this.ejbGroup = ejbGroup;
            this.methodInfo = methodInfo;
        }
        
        
        public String getBeanClassName() {
//            return item.getBeanClassName();
            return methodInfo.getDataProvider();
        }

        public Result beanCreatedSetup(DesignBean designBean) {
            // XXX Hack Jar ref adding.
            addJarRef();
            return null;
        }
        
        private void addJarRef() {
            FileObject fileObject = DesignerServiceHack.getDefault().getCurrentFile();
            Project project = FileOwnerQuery.getOwner( fileObject );

            // Add lib refs to the project
            Map allLibDefs = getLibraryDefinitions(); 
            EjbLibReferenceHelper.addLibRefsToProject( project, allLibDefs ); 

            // Add archive refs to the project
            Map refArchives = getReferenceArchives();
            EjbLibReferenceHelper.addArchiveRefsToProject( project, refArchives );

            // Add/update this ejb group to the ejb ref xml in the porject
            EjbLibReferenceHelper.addToEjbRefXmlToProject( project, ejbGroup );
        }

        public String getDisplayName() {
//            return item.getDisplayName();
            return methodInfo.getDataProvider();
        }

        public String getDescription() {
            return null;
        }

        public Image getLargeIcon() {
            return null;
        }

        public Image getSmallIcon() {
            return null;
        }

        public String getHelpKey() {
            return null;
        }
        
        /**
         * @return a map of (Library, JsfProjectClassPathExtender.LibraryRole[] ) 
         */
        private Map getLibraryDefinitions() {
            Map libdefs = new HashMap();
            Library ejb20LibDef = EjbLibReferenceHelper.getEjb20LibDef();
            libdefs.put( ejb20LibDef, new JsfProjectClassPathExtender.LibraryRole[] {JsfProjectClassPathExtender.LIBRARY_ROLE_DESIGN, JsfProjectClassPathExtender.LIBRARY_ROLE_DEPLOY} );
            return libdefs;
        }

        /**
         * @return a map of (jar path, JsfProjectClassPathExtender.LibraryRole[])
         */
        private Map getReferenceArchives() {
            Map jars = new HashMap();
            jars.put( ejbGroup.getClientWrapperBeanJar(), new JsfProjectClassPathExtender.LibraryRole[] {JsfProjectClassPathExtender.LIBRARY_ROLE_DESIGN, JsfProjectClassPathExtender.LIBRARY_ROLE_DEPLOY} );
            jars.put( ejbGroup.getDesignInfoJar(), new JsfProjectClassPathExtender.LibraryRole[] { JsfProjectClassPathExtender.LIBRARY_ROLE_DESIGN } );
            for( Iterator iter = ejbGroup.getClientJarFiles().iterator(); iter.hasNext(); ) { 
                jars.put( iter.next(), new JsfProjectClassPathExtender.LibraryRole[] {JsfProjectClassPathExtender.LIBRARY_ROLE_DESIGN, JsfProjectClassPathExtender.LIBRARY_ROLE_DEPLOY} );
            }
            return jars;
        }
    } // End of MethodBeanCreateInfo.
    
    
//    public String[] getClassNames() {
//        // Need to return a class name here if the method does not return void. 
//        // Otherwise, the cursor will not change to droppable over the designer
//        // after click on an ejb node (bug 6273520)
//        if( !methodInfo.getReturnType().isVoid() )
//            return new String[] { "com.sun.data.provider.impl.MethodResultTableDataProvider" };
//        else
//            return new String[0];
//    }
//    
//    public boolean hasPaletteItems() {
//        return true;
//    }
//    
//    public Cookie getCookie (Class type) {
//        if (type == RavePaletteItemSetCookie.class) {
//            // Don't know why this wasn't automatic - I implement
//            // Node.Cookie. This is automatic for data objects - not for
//            // nodes I guess?
//            return this;
//        } else {
//            return super.getCookie(type);
//        }
//    }
}
