
/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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


package org.netbeans.modules.uml.ui.swing.drawingarea;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicFileChooserUI;
import com.tomsawyer.drawing.TSDGraphManager;
import com.tomsawyer.drawing.geometry.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstRect;
import com.tomsawyer.drawing.geometry.TSRect;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSEEdgeLabel;
import com.tomsawyer.editor.TSEGraph;
import com.tomsawyer.editor.TSEGraphImageEncoder;
import com.tomsawyer.editor.TSEGraphManager;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.TSEHitTesting;
import com.tomsawyer.editor.TSELineGrid;
import com.tomsawyer.editor.TSELocalization;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.TSENodeLabel;
import com.tomsawyer.editor.TSEObject;
import com.tomsawyer.editor.TSEObjectUI;
import com.tomsawyer.editor.TSEPointGrid;
import com.tomsawyer.editor.TSEWindowTool;
import com.tomsawyer.editor.TSTransform;
import com.tomsawyer.editor.complexity.TSEFoldingManager;
import com.tomsawyer.editor.complexity.TSEHidingManager;
import com.tomsawyer.editor.complexity.TSENestingManager;
import com.tomsawyer.editor.dialog.TSDoublePercentDocument;
import com.tomsawyer.editor.dialog.TSFileChooser;
import com.tomsawyer.editor.dialog.TSFileFilter;
import com.tomsawyer.editor.event.TSEEventManager;
import com.tomsawyer.editor.event.TSESelectionChangeEvent;
import com.tomsawyer.editor.event.TSESelectionChangeListener;
import com.tomsawyer.editor.event.TSEViewportChangeEvent;
import com.tomsawyer.editor.event.TSEViewportChangeListener;
import com.tomsawyer.editor.export.TSESaveAsImageDialog;
import com.tomsawyer.editor.service.TSEAllOptionsServiceInputData;
import com.tomsawyer.graph.TSEdge;
import com.tomsawyer.graph.TSGraph;
import com.tomsawyer.graph.TSGraphManager;
import com.tomsawyer.graph.TSGraphObject;
import com.tomsawyer.graph.TSNode;
import com.tomsawyer.graph.event.TSGraphChangeEvent;
import com.tomsawyer.graph.event.TSGraphChangeEventData;
import com.tomsawyer.graph.event.TSGraphChangeListener;
import com.tomsawyer.service.TSServiceOutputData;
import com.tomsawyer.service.layout.TSLayoutConstants;
import com.tomsawyer.service.layout.jlayout.TSHierarchicalLayoutInputTailor;
import com.tomsawyer.service.layout.jlayout.TSJLayoutConstants;
import com.tomsawyer.service.layout.jlayout.TSLayoutInputTailor;
import com.tomsawyer.service.layout.jlayout.TSOrthogonalLayoutInputTailor;
import com.tomsawyer.service.layout.jlayout.client.TSLayoutProxy;
import com.tomsawyer.util.TSLicenseManager;
import com.tomsawyer.util.TSObject;
import com.tomsawyer.util.TSProperty;
import com.tomsawyer.xml.editor.TSEEnumerationTable;
import com.tomsawyer.xml.editor.TSEVisualizationXMLReader;
import com.tomsawyer.xml.editor.TSEVisualizationXMLWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.prefs.Preferences;
import org.dom4j.Document;
import org.dom4j.Node;
import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.common.generics.ETTripleT;
import org.netbeans.modules.uml.common.generics.IteratorT;
import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.eventframework.EventBlocker;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ICreationFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.DiagramEnums;
import org.netbeans.modules.uml.core.metamodel.diagrams.DiagramValidation;
import org.netbeans.modules.uml.core.metamodel.diagrams.GraphObjectValidation;
import org.netbeans.modules.uml.core.metamodel.diagrams.IBroadcastAction;
import org.netbeans.modules.uml.core.metamodel.diagrams.ICoreRelationshipDiscovery;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDelayedAction;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidateKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidateResponse;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidateResult;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidation;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidationResult;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDrawingToolKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphObjectValidation;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphicExportDetails;
import org.netbeans.modules.uml.core.metamodel.diagrams.ILayoutKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IPresentationElementToDeleteAction;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.ISynchStateKind;
import org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessageConnector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.scm.ISCMDiagramItem;
import org.netbeans.modules.uml.core.scm.ISCMItem;
import org.netbeans.modules.uml.core.scm.ISCMItemGroup;
import org.netbeans.modules.uml.core.scm.SCMFeatureKind;
import org.netbeans.modules.uml.core.support.Debug;
import org.netbeans.modules.uml.core.support.umlmessagingcore.MsgCoreConstants;
import org.netbeans.modules.uml.core.support.umlmessagingcore.UMLMessagingHelper;
import org.netbeans.modules.uml.core.support.umlsupport.ETDeviceRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.FileExtensions;
import org.netbeans.modules.uml.core.support.umlsupport.FileSysManip;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;
import org.netbeans.modules.uml.core.support.umlutils.PropertyElements;
import org.netbeans.modules.uml.core.workspacemanagement.ITwoPhaseCommit;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.ui.controls.drawingarea.CachedDiagrams;
import org.netbeans.modules.uml.ui.controls.drawingarea.DiagramAreaEnumerations;
import org.netbeans.modules.uml.ui.controls.drawingarea.DiagramValidator;
import org.netbeans.modules.uml.ui.controls.drawingarea.DrawEnginesToResetAction;
import org.netbeans.modules.uml.ui.controls.drawingarea.DrawingAreaEventHandler;
import org.netbeans.modules.uml.ui.controls.drawingarea.GetHelper;
import org.netbeans.modules.uml.ui.controls.drawingarea.IDiagramValidator;
import org.netbeans.modules.uml.ui.controls.drawingarea.IDrawEnginesToResetAction;
import org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher;
import org.netbeans.modules.uml.ui.controls.drawingarea.IElementBroadcastAction;
import org.netbeans.modules.uml.ui.controls.drawingarea.IExecutableAction;
import org.netbeans.modules.uml.ui.controls.drawingarea.ISimpleBroadcastAction;
import org.netbeans.modules.uml.ui.controls.drawingarea.ISimpleElementsAction;
import org.netbeans.modules.uml.ui.controls.drawingarea.ISimpleElementsAction.SEAK;
import org.netbeans.modules.uml.ui.controls.drawingarea.ITopographyChangeAction;
import org.netbeans.modules.uml.ui.controls.drawingarea.ITransformAction;
import org.netbeans.modules.uml.ui.controls.drawingarea.IUIDiagram;
import org.netbeans.modules.uml.ui.controls.drawingarea.SimpleBroadcastAction;
import org.netbeans.modules.uml.ui.controls.drawingarea.SimpleElementsAction;
import org.netbeans.modules.uml.ui.controls.drawingarea.TopographyChangeAction;
import org.netbeans.modules.uml.ui.controls.drawingarea.TransformAction;
import org.netbeans.modules.uml.ui.products.ad.application.ApplicationView;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETCompartment;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.ADCoreEngine;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.DrawingAreaContextMenuSorter;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETContainerDrawEngine;
import org.netbeans.modules.uml.ui.support.applicationmanager.NodePresentation;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETEdge;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETEdgeLabel;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETGraph;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETGraphManager;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETNode;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETNodeLabel;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETEGraphImageEncoder;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericEdgeLabelUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericEdgeUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericGraphUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeLabelUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETNullGraphUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETUIFactory;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge;
import org.netbeans.modules.uml.ui.support.ADTransferable;
import org.netbeans.modules.uml.ui.support.CreationFactoryHelper;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.DragAndDropSupport;
import org.netbeans.modules.uml.ui.support.ElementReloader;
import org.netbeans.modules.uml.ui.support.PresentationReferenceHelper;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.QuestionResponse;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogResultKind;
import org.netbeans.modules.uml.ui.support.SwingPreferenceQuestionDialog;
import org.netbeans.modules.uml.ui.support.applicationmanager.DiagramKeyboardAccessProvider;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface;
import org.netbeans.modules.uml.ui.support.applicationmanager.NameCollisionListener;
import org.netbeans.modules.uml.ui.support.applicationmanager.PresentationTypeDetails;
import org.netbeans.modules.uml.ui.support.applicationmanager.ProductGraphPresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.TSGraphObjectKind;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.archivesupport.ProductArchiveImpl;
import org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageDialogKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageResultKindEnum;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuSorter;
import org.netbeans.modules.uml.ui.support.diagramsupport.DiagramTypesManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.ETLayoutStyleMap;
import org.netbeans.modules.uml.ui.support.diagramsupport.IDiagramTypesManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.drawingproperties.IColorProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingPropertyProvider;
import org.netbeans.modules.uml.ui.support.drawingproperties.IFontProperty;
import org.netbeans.modules.uml.ui.support.helpers.ETSmartWaitCursor;
import org.netbeans.modules.uml.ui.support.helpers.GUIBlocker;
import org.netbeans.modules.uml.ui.support.helpers.IGUIBlocker;
import org.netbeans.modules.uml.ui.support.helpers.IGUIBlocker.GBK;
import org.netbeans.modules.uml.ui.support.helpers.UserInputBlocker;
import org.netbeans.modules.uml.ui.support.messaging.IZoomDialog;
import org.netbeans.modules.uml.ui.support.messaging.ZoomDialog;
import org.netbeans.modules.uml.ui.support.umltsconversions.RectConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETPointEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETRectEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IEventManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IToolTipData;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ResourceMgr;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.support.visitors.ETDrawEngineTypesMatchVistor;
import org.netbeans.modules.uml.ui.support.visitors.ETFindObjectVisitor;
import org.netbeans.modules.uml.ui.support.visitors.ETGraphObjectTraversal;
import org.netbeans.modules.uml.ui.support.visitors.IETGraphObjectVisitor;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingQuestionDialogImpl;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.ADCreateNodeState;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.ADDrawingAreaSelectState;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.ADMoveSelectedState;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.ADReconnectEdgeState;
import org.netbeans.modules.uml.ui.swing.testbed.addin.menu.Separator;
import org.netbeans.modules.uml.ui.swing.testbed.addin.menu.TestBedMenuManager;
import org.netbeans.modules.uml.ui.swing.trackbar.JTrackBar;
import org.netbeans.modules.uml.util.DummyCorePreference;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/*
 * Main Display Control for all UML Diagram Types.
 */

/*
 * Main Display Control for all UML Diagram Types.
 */
public class ADDrawingAreaControl extends ApplicationView
    implements IDrawingPropertyProvider, IDrawingAreaControl, ActionListener,
        ADParameterReader, ITwoPhaseCommit, MouseWheelListener, KeyListener
{
	static
        {
            //JM: Fix for Bug#6299069
//            TSELocalization.setBundlePath("com.tomsawyer.module.resources.");
            TSELocalization.setBundlePath("org.netbeans.modules.uml.tomsawyer.");
        }
   private Hashtable<String, String> m_CachedPreferences = new Hashtable<String, String>();
   private boolean m_isDirty = false;
   private INamespace m_Namespace = null;
   private String m_PreCommitFileName = "";
   private String m_FileName = "";
   private Vector m_ViewsReadWriteFromETLFile = new Vector();
   private String m_Name = "";
   private String m_Alias = "";
   private String m_Documentation = "";
   private IDiagramEngine m_DiagramEngine = null;
   private double m_nZoomLevelFromArchive = 0.0;
   private double m_OnDrawZoom = 1.0f;
   private Point m_CenterFromArchive;
   private Vector m_AssociatedDiagrams = new Vector();
   private HashMap m_AssociatedElements = new HashMap(); //its a HashMap<String, Vector<String>>
   private IDiagram m_Diagram = null;
   private String m_DiagramKindDisplayName = "";
   private String m_DiagramXmiid = "";
   private boolean m_LoadedFromFile = false;
   private boolean m_DiagramSavedWithAliasOn = false;
   private boolean m_ReadOnly = false;
   private boolean m_AbortDiagramLoad = false;
   private boolean m_ItemsSelected = false;
   private boolean m_TooltipsEnabled = false;
   protected DrawingAreaInitString m_NodeInitializationString = new DrawingAreaInitString();
   protected DrawingAreaInitString m_EdgeInitializationString = new DrawingAreaInitString();
   
   private IProductArchive m_ProductArchive = null;
   private IElement modelElement;
   
   private ADDrawingAreaResourceBundle resourceBundle = null;
   private ADDrawingAreaActions actions = null;
   protected ADDrawingAreaPrinter m_printer = null;
   
   private static final String BUNDLE_NAME = "org.netbeans.modules.uml.ui.swing.drawingarea.Bundle"; //$NON-NLS-1$
   private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
   
   /// Listener to the name collision event
   private NameCollisionListener m_NameCollisionListener = null;
   
   /// Handler for name collision events
   private DrawingAreaCollisionHandler m_CollisionHandler = null;
   
   /**
    * This field is the next index number to be assigned to untitled documents.
    */
   private int nextNewDocIndex = 1;
   
   /**
    * This variable holds the graph window, the main component of this example editor.
    */
   private ADGraphWindow m_GraphWindow;
   
   
   /**
    * This variable stores the installed layout server.
    */
  // private TSLayoutServer layoutServer = null;
    private TSLayoutProxy m_layoutProxy = null;
   
   /**
    * This variable stores a reference to the overview window.
    */
   private ETDiagramOverviewWindow overviewWindow = null;
   
   /**
    * Layout Properties Dialog
    */
   protected ETLayoutPropertiesDialog layoutPropertiesDialog = null;
   
   /**
    * Drawing Properties Dialog
    */
   protected ETDrawingPreferencesDialog graphPreferencesDialog = null;
   
   /**
    * This variable stores the coordinates and size of the overview window.
    */
   private Rectangle overviewBounds = new Rectangle(0, 0, ETDiagramOverviewWindow.WIDTH, ETDiagramOverviewWindow.HEIGHT);
   
   /**
    * This variable is set to true when the bounds of the overview
    * window have been set.
    */
   private boolean overviewBoundsCreated;
   
   /**
    * The track bar used by the diagram.  The track bar implemementation is
    * determined by the diagram engine.
    */
   private JTrackBar m_TrackBar = null;
   
   private Insets windowInsets = new Insets(12, 12, 29, 29);
   private TSEGraph m_graph;
   private WindowHandler windowHandler;
   private MouseHandler mouseHandler;
   private JToolBar mainToolBar;
   private JToolBar umlToolBar;
   private JPanel northPanel;
   private JPanel centerPanel;
   private TSELineGrid lineGrid;
   private TSEPointGrid pointGrid;
   private ADComboBox zoomComboBox;
   private String graphFileName;
   private TSFileChooser fileChooser;
   
   protected boolean autoFitInWindow;
   
   protected IDrawingAreaEventDispatcher m_drawingAreaDispatcher;
   private DrawingAreaEventHandler m_DrawingAreaEventsSink = null;
   
   private WeakReference m_EditCompartment = null;
   
   private IPresentationTypesMgr m_PresentationTypesMgr = null;
   
//   private PopupMenuExtender  m_ContextMenu        = null;
   private TestBedMenuManager m_ContextMenuManager = null;
   private JMenu              m_ContextPopup       = new JMenu();
//   private StructuredSelectionProvider m_SelectionProvider = new StructuredSelectionProvider();
   private boolean m_showToolbars = true;
   private MyGraphChangeListener trackBarModifyListener = null;
   private ViewportChangeListener trackBarChangeListener = null;
   private boolean populating = false;

   // Sun Issue 6184441:  It appears that TS is setting up the clipboard
   // then sending use a the delete command, which we were then clearing
   // the clipboard.  We use m_bCutting to indicate we are cutting so that
   // we don't clear the clipboard.
   // @see cut()
   // @see clearClipboard()
   private boolean m_bCutting = false;
   
   private TSEGraphManager m_graphManager;

   private TSLayoutInputTailor m_layoutInputTailor;
   private TSHierarchicalLayoutInputTailor m_hierarchicalInputTailor;
   private TSOrthogonalLayoutInputTailor m_orthogonalLayoutInputTailor;
   
   private TSServiceOutputData m_serviceOutputData;
   private TSEAllOptionsServiceInputData m_allOptionsServiceInputData;
   
   private TSEVisualizationXMLWriter xmlWriter;
   private TSEVisualizationXMLReader xmlReader;
   private Hashtable serviceInputDataTable = new Hashtable();
   private ETNode m_lastSelectedNode = null;
   private IETLabel m_lastSelectedLabel = null;
   private IETGraphObject m_graphObj = null;
   private List m_selectedNodesGroup = new ArrayList();
   private List tempList = new ArrayList(); //temp list for use in fireSelectEvent
   private String mSelectedPaletteBttn;
   public static String DIRTYSTATE = "dirty"; // NOI18N;
   private DiagramKeyboardAccessProvider kbAccessProvider = null;

   public ADDrawingAreaControl()
   {
      
      super("org.netbeans.modules.uml.view.drawingarea");
      m_drawingAreaDispatcher = null;
      m_DiagramKindDisplayName = "Diagram";
      
      try
      {
         init();
         registerContextMenu(true);
      } catch (Exception e)
      {
         e.printStackTrace();
      }
      this.setEnabled(true);      
      
   }
   
   public void actionPerformed(ActionEvent event)
   {
      String action = event.getActionCommand();
   }
   
   private void init() throws Exception
   {
      m_PresentationTypesMgr = CreationFactoryHelper.getPresentationTypesMgr();
      
      //invoke TS licensing system
      this.initializeTSLicense();
      
      // create the user interface
      this.createGUI();
      
      this.getGraphWindow().setAllowRedraw(false);
      
      // install the layout server
      this.installLayoutServer();
      
      // set default values
      this.setDefaults();
      
      //initialize event sinks
      initEventSink();
      
      addComponetListeners();
      
      // Create the name collision listener
      m_NameCollisionListener = new NameCollisionListener();
      m_CollisionHandler = new DrawingAreaCollisionHandler();
      
      if (m_NameCollisionListener != null && m_CollisionHandler != null)
      {
         m_NameCollisionListener.setHandler(m_CollisionHandler);
         m_CollisionHandler.setDrawingArea(this);
      }
      this.getGraphWindow().setAllowRedraw(true);
   }
   
   private void initEventSink()
   {
      if (m_DrawingAreaEventsSink == null)
      {
         m_DrawingAreaEventsSink = new DrawingAreaEventHandler();
      }
      if (m_DrawingAreaEventsSink != null)
      {
         DispatchHelper helper = new DispatchHelper();
         helper.registerForWSElementEvents(m_DrawingAreaEventsSink);
         helper.registerForElementModifiedEvents(m_DrawingAreaEventsSink);
         helper.registerForLifeTimeEvents(m_DrawingAreaEventsSink);
         helper.registerForClassifierFeatureEvents(m_DrawingAreaEventsSink);
         helper.registerForTransformEvents(m_DrawingAreaEventsSink);
         helper.registerForTypedElementEvents(m_DrawingAreaEventsSink);
         helper.registerForRelationEvents(m_DrawingAreaEventsSink);
         helper.registerForNamedElementEvents(m_DrawingAreaEventsSink);
         helper.registerForExternalElementEventsSink(m_DrawingAreaEventsSink);
         helper.registerForAffectedElementEvents(m_DrawingAreaEventsSink);
         helper.registerForAssociationEndEvents(m_DrawingAreaEventsSink);
         helper.registerForStereotypeEventsSink(m_DrawingAreaEventsSink);
         helper.registerForSCMEvents(m_DrawingAreaEventsSink);
         helper.registerElementDisposalEvents(m_DrawingAreaEventsSink);
         helper.registerForNamespaceModifiedEvents(m_DrawingAreaEventsSink);
         helper.registerForAttributeEvents(m_DrawingAreaEventsSink);
         helper.registerForOperationEvents(m_DrawingAreaEventsSink);
         helper.registerForParameterEvents(m_DrawingAreaEventsSink);
         helper.registerForBehavioralFeatureEvents(m_DrawingAreaEventsSink);
         helper.registerForRedefinableElementModifiedEvents(m_DrawingAreaEventsSink);
         helper.registerForDynamicsEvents(m_DrawingAreaEventsSink);
         helper.registerForFeatureEvents(m_DrawingAreaEventsSink);
         helper.registerForInitEvents(m_DrawingAreaEventsSink);
         helper.registerForProjectEvents(m_DrawingAreaEventsSink);
         helper.registerForActivityEdgeEvents(m_DrawingAreaEventsSink);
         m_DrawingAreaEventsSink.setDrawingAreaControl(this);
      }
   }
   
   private void revokeEventSinks()
   {
      if (m_DrawingAreaEventsSink != null)
      {
         try
         {
            DispatchHelper helper = new DispatchHelper();
            helper.revokeWSElementSink(m_DrawingAreaEventsSink);
            helper.revokeElementModifiedSink(m_DrawingAreaEventsSink);
            helper.revokeLifeTimeSink(m_DrawingAreaEventsSink);
            helper.revokeClassifierFeatureSink(m_DrawingAreaEventsSink);
            helper.revokeTransformSink(m_DrawingAreaEventsSink);
            helper.revokeTypedElementSink(m_DrawingAreaEventsSink);
            helper.revokeRelationSink(m_DrawingAreaEventsSink);
            helper.revokeNamedElementSink(m_DrawingAreaEventsSink);
            helper.revokeExternalElementEventsSink(m_DrawingAreaEventsSink);
            helper.revokeAffectedElementEvents(m_DrawingAreaEventsSink);
            helper.revokeAssociationEndEvents(m_DrawingAreaEventsSink);
            helper.revokeStereotypeEventsSink(m_DrawingAreaEventsSink);
            helper.revokeSCMSink(m_DrawingAreaEventsSink);
            helper.revokeElementDisposalEventsSink(m_DrawingAreaEventsSink);
            helper.revokeNamespaceModifiedSink(m_DrawingAreaEventsSink);
            helper.revokeAttributeSink(m_DrawingAreaEventsSink);
            helper.revokeOperationSink(m_DrawingAreaEventsSink);
            helper.revokeParameterSink(m_DrawingAreaEventsSink);
            helper.revokeBehavioralFeatureSink(m_DrawingAreaEventsSink);
            helper.revokeRedefinableElementModifiedEvents(m_DrawingAreaEventsSink);
            helper.revokeDynamicsSink(m_DrawingAreaEventsSink);
            helper.revokeFeatureSink(m_DrawingAreaEventsSink);
            helper.revokeInitSink(m_DrawingAreaEventsSink);
            helper.revokeProjectSink(m_DrawingAreaEventsSink);
            helper.revokeActivityEdgeSink(m_DrawingAreaEventsSink);
         } catch (InvalidArguments e)
         {
            // TODO Auto-generated catch block


            e.printStackTrace();
         }
      }
   }
   
   /**
    * This method installs a layout proxy.
    */
   
   private void initializeTSLicense() {
      try {
             // Initialize licensing. 
              TSLicenseManager.initTSSLicensing();
              Debug.out.println(" In try of initializeTSLicense...");
          } catch (Exception e) {
              Debug.out.println(" Exception in TSLicenseManager.initTSSLicensing() ");
              e.printStackTrace();
          } 
   }
   private void installLayoutServer()
   {
      try
      {               
          // Create the graph drawing.
          m_graphManager = getGraphManager();          
          m_graph = getCurrentGraph();
          
          // Create the layout proxy.
          m_layoutProxy = new TSLayoutProxy();
          
          // Create a service input data object.          
          m_allOptionsServiceInputData = new TSEAllOptionsServiceInputData(this.getGraphManager());
          
          // Create an input tailor object for the graph manager and one for the graph.          
          m_layoutInputTailor = new TSLayoutInputTailor(m_allOptionsServiceInputData);
          
          // Set the graph manager to be laid out.
          m_layoutInputTailor.setGraphManager(m_graphManager);
          
          // Set layout as the current operation.
          m_layoutInputTailor.setAsCurrentOperation();
          
          // Create a service output data object.
          m_serviceOutputData = new TSServiceOutputData();

         
         //this.layoutServer = new ADLocalLayoutProxy();
      } catch (Throwable t)
      {
         t.printStackTrace();
      }
   }
   
   
   /**
    * This method creates the graphical user interface.
    */
   private void createGUI()
   {
      // set the layout
      this.setLayout(new BorderLayout());
      
      this.northPanel = new JPanel(new BorderLayout());
//      this.northPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
      this.add(this.northPanel, BorderLayout.NORTH);
      
      this.centerPanel = new JPanel(new BorderLayout());
//      this.centerPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
      this.add(this.centerPanel, BorderLayout.CENTER);
      
      ETSmartWaitCursor waitCursor = new ETSmartWaitCursor();
      // create the window handler
      this.windowHandler = new WindowHandler();
      
      this.setGraphFileName(null);
      this.initGraphWindow();
      this.setChanged(false);
      
      ADDrawingAreaControl.registerNewUIs();
      // create key commands
      this.registerKeyCommands(this);
     
      this.requestFocusInWindow();
      waitCursor.stop();
   }
   //JM: added this method to register the ui classes for TSEVisualizationXMLReader   
   static void registerNewUIs()
   {
      TSEEnumerationTable.getTable().addUIName(
            ETUIFactory.GENERIC_NODE_UI,
            ETGenericNodeUI.class);
      TSEEnumerationTable.getTable().addUIName(
            ETUIFactory.GENERIC_NODE_LABEL_UI,
            ETGenericNodeLabelUI.class);
      TSEEnumerationTable.getTable().addUIName(
            "relationEdge",
            RelationEdge.class);
      TSEEnumerationTable.getTable().addUIName(
            "basicEdge",
            ETGenericEdgeUI.class);
      TSEEnumerationTable.getTable().addUIName(
            ETUIFactory.GENERIC_EDGE_LABEL_UI,
            ETGenericEdgeLabelUI.class);
      //TSEEnumerationTable.getTable().addUIName(
      //	"etpNode",
      //	ETGenericPathNodeUI.class);
      TSEEnumerationTable.getTable().addUIName(
            ETUIFactory.GENERIC_GRAPH_UI,
            ETGenericGraphUI.class);
   }

   /**
    * This method sets whether the graph has been modified since
    * the last time it was saved or loaded.
    */
   public void setChanged(boolean changed)
   {
      if (this.getGraphWindow() != null)
         this.getGraphWindow().setChanged(changed);
   }
   
   /**
    * This method sets the default state, zoom level, node and edge
    * UIs, and various other defaults.
    */
   private void setDefaults()
   {
      // set the default state to select
      this.getActions().onSwitchToSelect();
      
      // turn on the tool tips
      getGraphWindow().setToolTipShown(true);
      
      // create the line and point grids.
      this.lineGrid = new TSELineGrid();
      this.pointGrid = new TSEPointGrid();
      
      // turn off the grid.
      this.getActions().onGridType(ADDrawingAreaConstants.GRID_TYPE + ".none", false);
      
      // set its spacing.
      this.getActions().onGridSize(ADDrawingAreaConstants.GRID_SIZE + ".10", false);
      
      // set auto fit in window to false
      this.setAutoFitInWindow(false);
   }
   
   
   /**
    * This method sets all user specified properties for the
    * node or edge UI specified by the user.
    */
   private void setUIAttributes(String type, TSEObjectUI ui)
   {
      for (int attr = 1;; ++attr)
      {
         String name = this.getResources().getStringResource(type + ".property." + attr + ".name");
         String value = this.getResources().getStringResource(type + ".property." + attr + ".value");
         if (name == null || value == null)
         {
            return;
         }
         ui.setProperty(new TSProperty(name, value));
      }
   }
   
   
    /**
    * This method registers all the key actions to the related keys.
    */
   public void registerKeyCommands(JComponent component)
   {
       kbAccessProvider = DiagramKeyboardAccessProvider.getInstance(this);
       kbAccessProvider.registerKeyCommands(component);
   }
   
   /**
    * This method unregisters some keys, which are used to zoom and
    * scroll, when "auto fit in window" is on. It simply binds them
    * to a dummy command.
    */
   public void unregisterKeyCommands(JComponent component)
   {
       DiagramKeyboardAccessProvider kbAccessProvider = 
                DiagramKeyboardAccessProvider.getInstance(this);
       kbAccessProvider.unregisterKeyCommands(component);
   }
    
   /**
    * This method opens a file dialog and prompts the user to select
    * the name of the graph. This method returns null if the user
    * cancels the dialog.
    */
   private List getFileNames(boolean load)
   {
      List fileNames;
      
      fileNames = this.getLocalFiles(load);
      
      if (fileNames == null)
      {
         return (null);
      }
      String plainExtension = ".tsv";
      
      Iterator iter = fileNames.iterator();
      
      while (iter.hasNext())
      {
         String fileName = iter.next().toString();
         
         if (fileName.indexOf('.') < 0)
         {
            fileName += plainExtension;
         }
      }
      
      return fileNames;
   }
   
   /**
    * This method returns a local file name.
    */
   private List getLocalFiles(boolean load)
   {
      // stores the return code of the file chooser
      int rc;
      
      if (this.getGraphFileName() != null && !load)
      {
         this.getFileChooser().setSelectedFileAndFilter(new File(this.getGraphFileName()));
      } else
      {
         this.getFileChooser().setSelectedFile(new File(""));
      }
      
      if (load)
      {
         this.getFileChooser().setDialogTitle(RESOURCE_BUNDLE.getString("IDS_OPEN"));
         this.getFileChooser().cancelSelection();
         rc = this.getFileChooser().showOpenDialog(this);
      } else
      {
         this.getFileChooser().setDialogTitle(RESOURCE_BUNDLE.getString("IDS_SAVEAS"));
         
         rc = this.getFileChooser().showSaveDialog(this);
      }
      
      if (rc != JFileChooser.APPROVE_OPTION)
      {
         return (null);
      } else
      {
         File[] files = this.getFileChooser().getSelectedFiles();
         
         if (files == null)
         {
            return (null);
         } else
         {
            if (files.length == 0)
            {
               // this is a workaround for Swing bug 4528663
               
               if (this.getFileChooser().getUI() instanceof BasicFileChooserUI)
               {
                  BasicFileChooserUI ui = (BasicFileChooserUI) this.getFileChooser().getUI();
                  ui.getApproveSelectionAction().actionPerformed(null);
               }
               
               files = new File[]
               { this.getFileChooser().getSelectedFile()};
            }
            
            List paths = new LinkedList();
            
            for (int i = 0; i < files.length; i++)
            {
               File selectedFile = files[i];
               
               // the path contains the full path name to the choosen
               // file
               
               String path = selectedFile.getAbsolutePath();
               
               // the filename is without path.
               String fileName = selectedFile.getName();
               
               // determine what extension to add to the filesname
               // using our specific file filter
               
               if (this.getFileChooser().getFileFilter() instanceof TSFileFilter)
               {
                  // the file filter contains the extension that
                  // should be applied to the filename
                  
                  TSFileFilter filter = (TSFileFilter) this.getFileChooser().getFileFilter();
                  
                  // if the choosen file has no extension
                  // adding the extension is enough
                  
                  if (fileName.indexOf('.') < 0)
                  {
                     path += "." + filter.getExtension();
                  } else if (load)
                  {
                     // if the chosen extension in the file filter
                     // is different from the extension the file has
                     // it should be replaced.

                     String plainExtension = ".tsv"; 
                     String zipExtension = ".tsvz";
                      
                     // replace only known extensions
                     // like this plain extension
                     
                     if (path.endsWith(plainExtension))
                     {
                        // cut the extension of the path
                        path = path.substring(0, path.length() - plainExtension.length());
                     }
                     
                     // replace only known extensions
                     // like this compressed extension
                     
                     if (path.endsWith(zipExtension))
                     {
                        // cut the extension of the path
                        path = path.substring(0, path.length() - plainExtension.length());
                     }
                     
                     // add the chosen extension
                     path += "." + filter.getExtension();
                  }
               }
               
               paths.add(path);
            }
            
            return paths;
         }
      }
   }
   
   /**
    * This method returns the file chooser dialog used by this
    * application.
    */
   private TSFileChooser getFileChooser()
   {
      if (this.fileChooser == null)
      {
         this.fileChooser = new TSFileChooser(System.getProperty("user.dir"));
         
         TSFileFilter extensionFilter;
         extensionFilter = new TSFileFilter("tsv", "Tom Sawyer Visualiztion (*.tsv)");
         this.fileChooser.addChoosableFileFilter(extensionFilter);        
         this.fileChooser.addChoosableFileFilter(new TSFileFilter("tsvz", "Compressed Tom Sawyer Visualiztion (*.tsvz)"));
         this.fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
         
         // allow multiple file selection only if using multiple windows
         this.fileChooser.setMultiSelectionEnabled(false);
         
         this.fileChooser.removeChoosableFileFilter(this.fileChooser.getAcceptAllFileFilter());
         this.fileChooser.setFileFilter(extensionFilter);
         TSELocalization.setComponentOrientation(this.fileChooser);
      }
      
      this.fileChooser.rescanCurrentDirectory();
      return (this.fileChooser);
   }
   
   
   
   /**
    * Set up the resource manager for this drawing area
    *
    * @param pProductArchive [in] The archive (etlp) that contains resource manager information on
    * colors and fonts.
    */
   private void loadResourceManager(IProductArchive prodArchive)
   {
      ETSmartWaitCursor wait = new 	ETSmartWaitCursor();
      ResourceMgr pMgr = ResourceMgr.instance((IDrawingAreaControl)this, prodArchive);
      if (prodArchive != null)
      {
         pMgr.readFromArchive(prodArchive);
      }
      wait.stop();
   }
   
   /**
    * Load from a file
    *
    * @param sFilename [in] The full path filename to load (this can be an etl or etlp file)
    * @param pFileCode [out,retval] Tells how the load went.
    */
   
   public static class DABlocker
   {

      static boolean orig = false;
       
      public DABlocker()
      {
      }
      public static void startBlocking()
      {
         ++m_Instances;
         
         if(m_Instances == 1)
         {
            orig = EventBlocker.startBlocking();
         }
      }
      public static void stopBlocking()
      {
         --m_Instances;
         
         if(m_Instances == 0)
         {
            EventBlocker.stopBlocking(orig);
         }
      }
      
      public static boolean getIsDisabled()
      {
         return (m_Instances != 0);
      }
      
      private static int m_Instances = 0;
   }
   
   /**
    * Load from a file
    *
    * @param sFilename [in] The full path filename to load (this can be an etl or etlp file)
    * @param pFileCode [out,retval] Tells how the load went.
    */
 public int load(String filename)
   {
      final ETSmartWaitCursor waitCursor = new ETSmartWaitCursor();
      
      int result = -1;
      IOSemaphore.startInstance();
      try
      {
         setPopulating(true);
         
         boolean prodArchiveLoadedOK = false;
         if (!m_LoadedFromFile)
         {
            m_LoadedFromFile = true;
            
            // kill modified events from getting to the diagram prematurely
            //DAEventBlocker blocker = new DAEventBlocker();
            
            try
            {
               DABlocker.startBlocking();
               // Don't go through put here, just do it directly
               m_FileName = filename;
               
               TSGraphManager pGraphMgr = getCurrentGraphManager();
               if (pGraphMgr != null)
               {
                  IProductArchive pArchive = new ProductArchiveImpl();
                  
                  // Verify we have a etlp file
                  String etlpFilename = FileSysManip.ensureExtension(filename, FileExtensions.DIAGRAM_PRESENTATION_EXT);
                  File file = new File(filename);
                  if (file.exists())
                  {
                     prodArchiveLoadedOK = pArchive.load(etlpFilename);
                  }
                  setFileName(filename, false);
                  
                  // If the product archive was loaded ok then continue with the loading process
                  if (prodArchiveLoadedOK)
                  {
                     boolean readGMFOk = true; 
                     
                     IProduct prod = getProduct();
                     if (prod != null)
                     {
                        IDiagram pDia = getDiagram();
                        prod.setSerializingDiagram(pDia);
                        
                        // Tell TomSawyer to load the file
                        if (pGraphMgr instanceof TSDGraphManager)
                        {
                            try {
                                //JM : trying to split it for TS related gmf and xml files
                                boolean isFileTSVxml = true; // It file a TSVisualization xml file
                                
                                Reader reader = null;
                                if ( isFileTSVxml) {
                                    
                                    FileInputStream fileReader = new FileInputStream(filename);
                                    InputStreamReader inpReader = new InputStreamReader(fileReader);
                                    reader = new BufferedReader(inpReader);
                                    if( reader.ready() ) {
                                        try {
                                            xmlReader = new TSEVisualizationXMLReader(reader);
                                            xmlReader.setGraphManager(pGraphMgr);
                                            xmlReader.setServiceInputData(m_allOptionsServiceInputData);
                                            xmlReader.setPreferences(this.getGraphWindow().getPreferences());                                            
                                            xmlReader.setStrict(true);
                                            
                                            xmlReader.read();
                                        } catch (Exception ex) {
                                            Debug.out.println(" Exception in xmlREading... So, it is NOT a TSVisulatization file.. it might be GMF...");
                                            isFileTSVxml = false; // not TSV .. might be in gmf format..
                                        }
                                        
                                        if ( !isFileTSVxml ) { // it is a gmf file..
                                            
                                            reader = new BufferedReader(
                                                    new InputStreamReader(
                                                    new FileInputStream(filename),
                                                    "UTF-8"));
                                            
                                            if( reader.ready() ) {
                                                TSLayoutInputTailor tailor = new TSLayoutInputTailor( this.getServiceInputData((TSEGraphManager)pGraphMgr));
                                                tailor.setGraphManager((TSEGraphManager)pGraphMgr);
                                                
                                                try {
                                                    ((TSEGraphManager)pGraphMgr).readGMF(reader, this.getServiceInputData((TSEGraphManager)pGraphMgr));                                                   
                                                } catch(Exception e) {                                                   
                                                    e.printStackTrace();
                                                }
                                            }
                                            isFileTSVxml = true; //setting back to true to make TSVisualziation default
                                        }
                                        ((TSDGraphManager)pGraphMgr).setMainDisplayGraph(this.getGraph());
                                        ((TSEGraphManager)pGraphMgr).setGraphWindow(this.getGraphWindow());
                                        //now I need to load all the readwrite items
                                        postReadGMFFile(pArchive);
                                    } else {
                                        if (getGraphWindow() == null) {
                                            this.initGraphWindow();
                                        }
                                    }
                                    reader = null;
                                }
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                                sendErrorMessage("Failed to load %f");
                                readGMFOk = false;
                                if (pGraphMgr instanceof ETGraphManager) {
                                    ETGraphManager pETGraphMgr = (ETGraphManager)  pGraphMgr;
                                    pETGraphMgr.onFailedToLoadDiagram();
                                }
                            }
                           catch (FileNotFoundException e)
                           {
                              e.printStackTrace();
                              sendErrorMessage("Unable to locate the file %f");
                              
                              readGMFOk = false;
                              if (pGraphMgr instanceof ETGraphManager)
                              {
                                 ETGraphManager pETGraphMgr = (ETGraphManager)  pGraphMgr;
                                 pETGraphMgr.onFailedToLoadDiagram();
                              }
                           }
                           catch (IOException e)
                           {
                              e.printStackTrace();
                              sendErrorMessage("Failed to load %f");
                              
                              readGMFOk = false;
                              if (pGraphMgr instanceof ETGraphManager)
                              {
                                 ETGraphManager pETGraphMgr = (ETGraphManager)  pGraphMgr;
                                 pETGraphMgr.onFailedToLoadDiagram();
                              }
                           }
                           catch (RuntimeException e)
                           {
                              e.printStackTrace();
                              sendErrorMessage("Failed to load %f");
                              
                              readGMFOk = false;
                              if (pGraphMgr instanceof ETGraphManager)
                              {
                                 ETGraphManager pETGraphMgr = (ETGraphManager)  pGraphMgr;
                                 pETGraphMgr.onFailedToLoadDiagram();
                              }
                           }
                        }
                        //result = pGraphMgr.load(filename, false, false);
                        recalculateCRCs();
                        prod.setSerializingDiagram(null);
                     }
                     
                     
                     long timeStart = System.currentTimeMillis();
                     
                     // set up the resource manager for this drawing
                     loadResourceManager(pArchive);
                     
                     long timeFinish = System.currentTimeMillis();
                     long tsDelta = timeFinish - timeStart;
                     ETSystem.out.println("ADDrawingAreaControl.loadResourceManager() completed in ( " + StringUtilities.timeToString(tsDelta, 3) + " )");
                     
                     
                     timeStart = System.currentTimeMillis();
                     
                     readFromArchive(pArchive);
                                          
                     timeFinish = System.currentTimeMillis();
                     tsDelta = timeFinish - timeStart;
                     ETSystem.out.println("ADDrawingAreaControl.readFromArchive() completed in ( " + StringUtilities.timeToString(tsDelta, 3) + " )");
                     
                     if (m_AbortDiagramLoad)
                     {
                        // The window was closed during the open process.  This can sometimes
                        // happen during SCC when it gets checked out.
                        return result;
                     }
                     
                     JTrackBar bar = getTrackBar();
                     if (readGMFOk && bar != null)
                     {
                        bar.initialize();
                        bar.load(pArchive);
                     }
                  }
               }
            }
            finally
            {
               DABlocker.stopBlocking();
            }
            
            if (prodArchiveLoadedOK)
            {
               if (m_DiagramEngine != null)
               {
                  m_DiagramEngine.setupLayoutSettings(false);
               }
               
               // Tell the outside world that this drawing area is open for business
               IDiagram pDia = getDiagram();
               if (getDrawingAreaDispatcher() != null && pDia != null)
               {
                  IEventPayload payload = getDrawingAreaDispatcher().createPayload("DiagramOpened");
                  getDrawingAreaDispatcher().fireDrawingAreaOpened(pDia, payload);
               }
            }
         }
      }
      finally
      {
         IOSemaphore.stopInstance();
      }
      
      // Set the diagram zoom level, but give the graphwindow a chance to process msg's first.
      Runnable runnable = new Runnable()
      {
          public void run()
          {
              TSEGraph graph = getGraph();
              TSEGraphWindow window = getGraphWindow();
              if (graph != null && window != null)
              {
                  graph.setBoundsUpdatingEnabled(true);
//                  graph.updateBounds();
                  window.setZoomLevel(m_nZoomLevelFromArchive, false);
                  if (m_CenterFromArchive != null)
                  {
                      TSConstPoint point = new TSConstPoint(m_CenterFromArchive.getX(), m_CenterFromArchive.getY());
                      getGraphWindow().centerPointInWindow(point, true);   
                  }
                  else
                  {
                      window.centerGraph(true);
                  }
              }
            getResources().setLayoutStyle(getLayoutStyle());
            setPopulating(false);
            waitCursor.stop();
         }
      };
      SwingUtilities.invokeLater(runnable);
      
      
      return result;
   }
   
   
   private void postReadGMFFile(IProductArchive pArchive)
   {
      try
      {
         IElementLocator locator = new ElementLocator();
         ETGraph graph = (ETGraph)getGraph();
         
         
         IteratorT < ETNode > iter = new IteratorT(graph.nodes());
         for (ETNode node = iter.next(); node != null; node = iter.next())
         {
            ETGenericNodeUI nodeUI = (ETGenericNodeUI)node.getUI();
            
            nodeUI.setDrawingArea(this);
            addReadWriteItem(node);
         }
         
         
         IteratorT < ETEdge > edgeIter = new IteratorT(graph.edges());
         int edgeIndex = 0;
         for (ETEdge edge = edgeIter.next(); edge != null; edge = edgeIter.next())
         {
            TSEObjectUI tseUI = edge.getUI();
            edgeIndex++;
            if (tseUI != null && tseUI instanceof ETGenericEdgeUI)
            {
               try
               {
                  ETGenericEdgeUI ui = (ETGenericEdgeUI)edge.getUI();
                  ui.setDrawingArea(this);
                  addReadWriteItem(edge);
               }
               catch (Exception e)
               {
                  // Discard the edge that failed to read.
                  this.getGraph().discard(edge);
                  // Continue reading the rest of the edges.
                  edgeIter.reset(this.getGraph().edges(), edgeIndex--);
               }
            }
         }
         
         IteratorT < ETEdgeLabel > edgeLabelIter = new IteratorT(graph.edgeLabels());
         for (ETEdgeLabel edgeLabel = edgeLabelIter.next(); edgeLabel != null; edgeLabel = edgeLabelIter.next())
         {
            ETGenericEdgeLabelUI edgeLabelUI = (ETGenericEdgeLabelUI)edgeLabel.getUI();
            
            edgeLabelUI.setDrawingArea(this);
            addReadWriteItem(edgeLabel);
         }
         
         IteratorT < ETNodeLabel > nodeLabelIter = new IteratorT(graph.nodeLabels());
         for (ETNodeLabel nodeLabel = nodeLabelIter.next(); nodeLabel != null; nodeLabel = nodeLabelIter.next())
         {
            TSEObjectUI ui = nodeLabel.getUI();
            if (ui instanceof ETGenericNodeLabelUI)
            {
                ETGenericNodeLabelUI nodeLabelUI = (ETGenericNodeLabelUI)ui;
            
                nodeLabelUI.setDrawingArea(this);
            }
            addReadWriteItem(nodeLabel);
            
         }
         
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
   
   private void postWriteGMFFile(IProductArchive pArchive)
   {
      try
      {
         IElementLocator locator = new ElementLocator();
         ETGraph graph = (ETGraph)getGraph();
         
         IteratorT < ETNode > iter = new IteratorT(graph.nodes());
         for (ETNode node = iter.next(); node != null; node = iter.next())
         {
            addReadWriteItem(node);
         }
         
         IteratorT < ETEdge > edgeIter = new IteratorT(graph.edges());
         for (ETEdge edge = edgeIter.next(); edge != null; edge = edgeIter.next())
         {
            addReadWriteItem(edge);
         }
         
         IteratorT < ETEdgeLabel > edgeLabelIter = new IteratorT(graph.edgeLabels());
         for (ETEdgeLabel edgeLabel = edgeLabelIter.next(); edgeLabel != null; edgeLabel = edgeLabelIter.next())
         {
            addReadWriteItem(edgeLabel);
         }
         
         IteratorT < ETNodeLabel > nodeLabelIter = new IteratorT(graph.nodeLabels());
         for (ETNodeLabel nodeLabel = nodeLabelIter.next(); nodeLabel != null; nodeLabel = nodeLabelIter.next())
         {
            addReadWriteItem(nodeLabel);
         }
         
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
   
   /**
    * Recalculates the file crcs
    *
    * @return true if the recalculation was done successfully
    */
   private boolean recalculateCRCs()
   {
      boolean didRecalculate = false;
      if (m_FileName != null && m_FileName.length() > 0)
      {
         String etlpFile = FileSysManip.ensureExtension(m_FileName, FileExtensions.DIAGRAM_PRESENTATION_EXT);
         String etldFile = FileSysManip.ensureExtension(m_FileName, FileExtensions.DIAGRAM_LAYOUT_EXT);
         
         if (etlpFile != null && etldFile != null)
         {
            File fileETLP = new File(etlpFile);
            File fileETLD = new File(etldFile);
            if (!fileETLP.canWrite() && !fileETLD.canWrite())
            {
               setReadOnly(true);
               
               // Go back to select mode
               enterMode(IDrawingToolKind.DTK_SELECTION);
            }
         }
      }
      return didRecalculate;
   }
   
   
   /**
    * This method switches the graph window to the create node state.
    */
   public void onSwitchToCreateNodes()
   {
      setGraphState(new ADCreateNodeState());
      
   }
   
   public void setGraphState(TSEWindowTool tool)
   {
       getGraphWindow().switchTool(tool);
   }
   
   /**
    * This method sets the Auto Fit In Window option.
    */
   public void setAutoFitInWindow(boolean autoFitInWindow)
   {
      this.autoFitInWindow = autoFitInWindow;
      
      if (this.autoFitInWindow)
      {
         getGraphWindow().fitInWindow(false);
      }
   }
   
   /**
    * This method creates toolbars.
    * It requests them from the resource bundle which knows how
    * to create toolbars from the resources.
    */
   private void createToolbars()

   {
      // create the toolbars used by the application
      this.mainToolBar = this.addToolBar("toolbar.main");
   }
   
   private void initGraphWindow()
   {
      this.initGraphWindow(new ADGraphWindow(this, null, true));
      m_GraphWindow.hasFileName = false;
   }
   
   public void addGraphWindow(ADGraphWindow newGraphWindow)
   {
      if (m_GraphWindow == null)
      {
         m_GraphWindow = newGraphWindow;
      }
      
      if (m_GraphWindow != null)
      {
         m_GraphWindow.addMouseWheelListener(this);
               
         this.graphFileName = m_GraphWindow.getGraphFileName();
         
         this.centerPanel.add(m_GraphWindow);
         
         this.requestFocusInWindow();
      }
   }
  
   /**
    * This method returns the title for the next new document
    */
   private String nextNewDocTitle()
   {
      String title = this.getResources().getString("file.noname") + this.nextNewDocIndex + "." + this.getResources().getStringResource("file.plain.extension");
      this.nextNewDocIndex++;
      
      return title;
   }
   
   public String getGraphFileName()
   {
      return this.graphFileName;
   }
   
   public void setGraphFileName(String filename)
   {
      this.graphFileName = filename;
   }
   
   /**
    * This method builds a toolbar from the specified resource and
    * adds it to the appropriate panel.
    */
   public JToolBar addToolBar(String name)
   {
      JToolBar toolBar = this.getResources().createToolBar(name, this.getActions());
      
      if (toolBar == null)
      {
         return toolBar;
      }
      
      addToolBar(name, toolBar);
      
      return toolBar;
   }
   
   /**
    * @param name
    * @param toolBar
    */
   protected JToolBar addToolBar(String name, JToolBar toolBar)
   {
      // place it in the panel specified by its location resource.
      Object location = this.getResources().getObjectResource(name + ".location");
      
      JPanel panel;
      panel = this.northPanel;
      
      // put it in the panel's border layout region specified by its sublocation resource.
      
      Object subLocation = this.getResources().getObjectResource(name + ".sublocation");
      
      if (subLocation == null)
      {
         subLocation = BorderLayout.CENTER;
      }
      if (m_showToolbars)
      {
         panel.add(toolBar, subLocation);
      }
      return toolBar;
   }
   
   public ADComboBox getZoomComboBox()
   {
      if (this.zoomComboBox == null)
      {
         String[] zoomLevels = new String[]
         { "400%", "200%", "100%", "75%", "50%", "25%", RESOURCE_BUNDLE.getString("IDS_ZOOMTOFIT") };
         
         this.zoomComboBox = new ADComboBox(zoomLevels);
         this.zoomComboBox.setEditable(true);
         this.zoomComboBox.setActionCommand(ADDrawingAreaConstants.ZOOM_CHANGE);
         
//         Dimension comboBoxSize = new Dimension(90, 25);
//         this.zoomComboBox.setPreferredSize(comboBoxSize);
         
         JTextField zoomField = (JTextField) this.zoomComboBox.getEditor().getEditorComponent();
         
         zoomField.setDocument(new TSDoublePercentDocument(Double.POSITIVE_INFINITY));
      }
      
      return this.zoomComboBox;
   }
   
   public ADDrawingAreaResourceBundle getResources()
   {
      if (this.resourceBundle == null)
      {
         this.resourceBundle = (ADDrawingAreaResourceBundle) ResourceBundle.getBundle("org.netbeans.modules.uml.ui.swing.drawingarea.ADDrawingAreaBundle");
         
         this.resourceBundle.setParameterReader(this);
         this.resourceBundle.setDrawingArea(this);
      }
      
      return this.resourceBundle;
   }
   
   public String getParameter(String key)
   {
      return null;
   }
   
   /**
    * This method returns the graph manager that this application is
    * displaying.
    */
   public TSEGraphManager getGraphManager()
   {
      // We need to be careful of recursion, the ADGraphWindow tries to get our graph manager.
      // TODO: We should move all the graph and file management stuff into the graph window and or
      // the graph manager.
      return getGraphWindow().getGraphManager();
   }
   
   /**
    * This method returns the main display graph of the selected graph window
    * of this application..
    */
   public TSEGraph getGraph()
   {
      ADGraphWindow window = getGraphWindow();
      return window != null ? window.getGraph() : null;
   }
   
   //JM: to get the layoutInputTailor object to use in ADDrawingAreaActions:
   public TSLayoutInputTailor getLayoutInputTailor() {
       return this.m_layoutInputTailor != null ? m_layoutInputTailor : null;
   }
   
   /**
    * This method returns the selected graph window this application operates
    * on.
    */
   public ADGraphWindow getGraphWindow()
   {
      return m_GraphWindow;
   }
   
   /**
    * This method returns whether the Auto Fit In Window option is on or
    * off.
    */
   public boolean isAutoFitInWindow()
   {
      return (this.autoFitInWindow);
   }
   
   /**
    * This class implements a window handler. It manages window
    * events, such as the window being closed.
    */
   class WindowHandler extends WindowAdapter
   {
   }
   
   public boolean hasGraphWindow()
   {
      return (this.m_GraphWindow != null);
   }
   
    /*
     * Factory function, creates the drawingarea actions.
     */
   protected ADDrawingAreaActions createDrawingAreaActions()
   {
      return new ADDrawingAreaActions(this);
   }
   /**
    * This method returns the unique instance of ADEditorActions. The
    * ADEditorActions object is allocated on the first call to this
    * method. This method will allocate the ADEditorActions object if
    * it has not yet been created.
    */
   public ADDrawingAreaActions getActions()
   {
      if (this.actions == null)
      {
         this.actions = createDrawingAreaActions();
      }
      
      return this.actions;
   }
   
   public Frame getOwnerFrame()
   {
      return JOptionPane.getFrameForComponent(SwingUtilities.getRootPane(this));
   }
   
   public Frame getMainFrame()
   {
      IProxyUserInterface ui = ProductHelper.getProxyUserInterface();
      return ui != null ? ui.getWindowHandle() : null;
   }
   
   public IProductArchive getProductArchive()
   {
      if (this.m_ProductArchive != null)
      {
         return m_ProductArchive;
      }else
      {
         m_ProductArchive =  new ProductArchiveImpl();
         return m_ProductArchive;
      }
   }
   
   /**
    * This method performs a layout on the graph.
    */
   public void onApplyLayout(String command)
   {
      // show the wait cursor
      ETSmartWaitCursor waitCursor = new ETSmartWaitCursor();
      try
      {         
         // disable user actions for the duration of the layout
         this.getGraphWindow().setUserActionEnabled(false);
         
         // if the command specified the layout style we need
         // to adjust layout properties. Otherwise we use the
         // style that is already set
         
         String layoutStyle = null;
         //boolean incremental = false;
         
         if (!command.equals(ADDrawingAreaConstants.APPLY_LAYOUT))
         {
             layoutStyle = command.substring(ADDrawingAreaConstants.APPLY_LAYOUT.length() + 1).toUpperCase(); 
         }
         
         if (layoutStyle == null)
         {
            //layoutStyle = this.getGraph().getLayoutStyle();
         }
         
         // we save the original layout style, so that when the ongoing one
         // fails, we can set it back.
         int originalLayoutStyle = this.m_layoutInputTailor.getLayoutStyle(this.getGraph());
         
         if (layoutStyle.indexOf(ADDrawingAreaConstants.INCREMENTAL_LAYOUT) >= 0)
         {
            layoutStyle = layoutStyle.substring(0, layoutStyle.lastIndexOf("."));
            //incremental = true;
            m_layoutInputTailor.setIncrementalLayout(true);
         } 
         else {
             m_layoutInputTailor.setIncrementalLayout(false);
         }
         int layoutStyleInt = new Integer(layoutStyle).intValue();
         setLayoutStyle(layoutStyleInt);                  
         
      } catch (Exception e)
      {
         e.printStackTrace();
         
         // apparently layout server cannot be reached
         this.m_layoutProxy = null;
         
         // tell the user about it
         JOptionPane.showMessageDialog(
         this,
         this.getResources().getStringResource("dialog.layoutError.message"),
         this.getResources().getStringResource("dialog.layoutError.title"),
         JOptionPane.ERROR_MESSAGE);
      }
      finally
      {
         // show the default cursor
         waitCursor.stop();
      }
      
      // re-enable user actions
      this.getGraphWindow().setUserActionEnabled(true);
   }
   
   /**
    * This method creates and or toggles displays an overview window.
    */
   public void onShowOverviewWindow()
   {
      // position the overview window on the top right of the graph
      // window.
      boolean currentlyVisible = this.getIsOverviewWindowOpen();
      if (!this.overviewBoundsCreated)
      {
         int x = getGraphWindow().getLocationOnScreen().x + getGraphWindow().getSize().width - ETDiagramOverviewWindow.WIDTH - this.windowInsets.right;
         
         int y = getGraphWindow().getLocationOnScreen().y + this.windowInsets.top;
         this.overviewBounds = new Rectangle(x, y, ETDiagramOverviewWindow.WIDTH, ETDiagramOverviewWindow.HEIGHT);         
         this.overviewBoundsCreated = true;
      }
      
      if (this.overviewWindow == null)
      {
         this.overviewWindow = new ETDiagramOverviewWindow(this.getOwnerFrame(), this.getResources().getStringResource("dialog.overviewWindow.title"), this.getGraphWindow());
         
         this.overviewWindow.setBounds(this.overviewBounds);
      }
      
      // disable the move/zoom state of the overview window when the autoFitInWindow option is on.
      if (overviewWindow !=null)
      {
         this.overviewWindow.setGraphWindow(getGraphWindow());
         
         this.overviewWindow.setVisible(!currentlyVisible);
         if (!currentlyVisible)
         {
            // Enable it
            //this.overviewWindow.getOverviewComponent().setStateEnabled(!this.isAutoFitInWindow()); //JM       
             this.overviewWindow.getOverviewComponent().setToolEnabled(true);
         }
         //this.refresh(true);
      }
      
   }
   
    /*
     * Synchronizes the secondard windows with the main display area.
     */
   public void updateSecondaryWindows()
   {
      // notify the overview window that we have changed the graph window.
      
      if (this.isOverviewWindowOpen() && getGraphWindow().isUserActionEnabled())
      {
         ((TSGraph)this.getGraph()).fireEvent(new TSEViewportChangeEvent(TSEViewportChangeEvent.ZOOM, null)); 
         ((TSGraph)this.getGraph()).fireEvent(new TSEViewportChangeEvent(TSEViewportChangeEvent.PAN, null)); 
      }
   }
   
   public ETEdge addEdgeForType(String metaType, ETNode sourceNode, ETNode targetNode, boolean bSelected, boolean bDeselectAllOthers) throws ETException
   {
      ETEdge retVal = null;
      
      IPresentationTypesMgr mgr = getPresentationTypesMgr();
      if (mgr != null)
      {
         String initString = mgr.getMetaTypeInitString(metaType, getDiagramKind());
         
         TSEdge edge = addEdge(initString, sourceNode, targetNode, bSelected, bDeselectAllOthers);
         
         if (edge instanceof ETEdge)
         {
            retVal = (ETEdge) edge;
         }
      }
      
      return retVal;
   }
   
   /**
    * Adds an edge to the diagram.
    */
   public TSEdge addEdge(String edgeInitString, TSNode pSourceNode, TSNode pTargetNode, boolean bSelect, boolean bDeselectAllOthers) throws ETException
   {
      TSEdge retVal = null;
      
      TSEGraph graph = getGraph();
      if (graph != null)
      {
         retVal = graph.addEdge(pSourceNode, pTargetNode);
         if (retVal instanceof ETEdge)
         {
            ETEdge gEdge = (ETEdge) retVal;
            
            IPresentationTypesMgr mgr = getPresentationTypesMgr();
            PresentationTypeDetails details = mgr.getInitStringDetails(edgeInitString, getDiagramKind());
            
            int delimiter = edgeInitString.indexOf(' ');
            String edgeUIClass = delimiter > 0 ? edgeInitString.substring(0, delimiter) : edgeInitString;
            ETGenericEdgeUI edgeUI = ETUIFactory.createEdgeUI(edgeUIClass, edgeInitString, details.getEngineName(), this);
            gEdge.setUI(edgeUI);
            
            onPostAddEdge((IETEdge)gEdge);
            
            if ((bSelect == true) || (bDeselectAllOthers == true))
            {
               selectAndFireEvents(retVal, bSelect, bDeselectAllOthers);
            }
         }
      }
      
      return retVal;
   }
   
   /**
    * Adds an edge to the diagram.
    *
    * @param edgeInitString The initialization string for the edge.
    * @param pSourceNode The source for the edge.
    * @param pTargetNode The target for the edge.
    * @param bSelect Should we select this new graph object?
    * @param bDeselectAllOthers Should we deselect all other objects?
    * @param pElementToAssignToEdge The element to assign to the edge
    * @return The created edge.  <code>null</code> if no edge is created.
    */
   public TSEdge addEdge(String edgeInitString, TSNode pSourceNode, TSNode pTargetNode, boolean bSelect, boolean bDeselectAllOthers, IElement elementToAssignToEdge) throws ETException
   {
      TSEdge retVal = null;
      
      if (edgeInitString.length() > 0)
      {
         IElement pPrevElement = this.getModelElement();
         setModelElement(elementToAssignToEdge);
         retVal = addEdge(edgeInitString, pSourceNode, pTargetNode, bSelect, bDeselectAllOthers);
         setModelElement(pPrevElement);
      }
      
      return retVal;
   }
   
   /**
    *
    * Helper to determine if class compartments are currently selected.  If so then cut/copy/paste
    * operations are prohibited.
    *
    * @return TRUE if a single class node containing selected compartments is selected
    *
    */
   
   private boolean areCompartmentsSelected()
   {
      boolean bSelected = false;
      ETList < IPresentationElement > pSelected = getSelected();
      
      long count = pSelected != null ? pSelected.size() : 0;
      // 2 possible situations exist, they could be cutting entire nodes or in the case of
      // a class diagram, could be cutting just a couple compartments
      // the test is if a single
      
      // special case, if a single node has been selected, check it for selected compartments
      if (count == 1)
      {
         IETGraphObject pProduct = TypeConversions.getETGraphObject(pSelected.get(0));
         
         if( pProduct != null)
         {
            ETList <IElement > pDragElements  = pProduct.getDragElements();
            bSelected =  pDragElements != null && pDragElements.size() > 0;
         }
      }
      return bSelected;
   }
   
   
   public void cut()
   {
      // Return if we're blocked
      if (UserInputBlocker.getIsDisabled(GBK.DIAGRAM_DELETION))
      {
         //ATLASSERT(0 &&_T("CAxDrawingAreaControl::Cut blocked\n") );
         return;
      }
      
      if (!m_ReadOnly)
      {
         // Need to clear all the clipboards of the other diagrams
         IProxyDiagramManager pManager = ProxyDiagramManager.instance();
         ISimpleBroadcastAction pAction = new SimpleBroadcastAction();
         
         /// W2662, Cutting when compartments are selected is temporarily disabled
         if( !areCompartmentsSelected() )
         {
            if (pManager != null && pAction != null)
            {
               pAction.setKind(DiagramAreaEnumerations.SBK_CLEAR_CLIPBOARD);
               pManager.broadcastToAllOpenDiagrams(pAction);
               
               // Since TS doesn't have copy and paste working between diagrams yet
               // we also copy the selected elements onto the IProduct clipboard.
               // If a paste is performed we first check to see if we've got something in the TS clipboard,
               // if not then check the proxy diagram clip.
               IProduct pProduct = getProduct();
               
               if (pProduct != null)
               {
                  // Clear it out first
                  pProduct.setCrossDiagramClipboard("");
                  
                  ETList <IPresentationElement> pSelected = getSelected();
                  
                  // Notify the track bar that a change has been made
                  if( m_TrackBar != null )
                  {
                     try
                     {
                        m_TrackBar.removePresentationElements( pSelected );
                     }
                     catch( Exception e )
                     {
                        e.printStackTrace();
                     }
                  }
                  
                ADTransferable transferable = new ADTransferable("DRAGGEDITEMS");
               
               transferable.addModelAndPresentationElements(pSelected);
               transferable.addGenericElement(Integer.toString(this.hashCode()));
               String text = null;
               
               try
               {
                    text = (String)transferable.getTransferData(ADTransferable.ADDataFlavor.stringFlavor);
               }
               catch(UnsupportedFlavorException e)
               {
                   throw new AssertionError("Unsupported Flavor "+e.toString());
               }
               catch(IOException e)
               {
                   throw new AssertionError("IOException "+e.toString());
               }
                  
                  if (text != null && text.length() > 0)
                  {
                     // Put these items on the clip
                     pProduct.setCrossDiagramClipboard(text);
                  }
               }
            }
            
            // The GET will copy to TS internal clip and the windows clip.
            
            // Sun Issue 6184441:  It appears that TS is setting up the clipboard
            // then sending use a the delete command, which we were then clearing
            // the clipboard.  We use m_bCutting to indicate we are cutting so that
            // we don't clear the clipboard.
            // @see clearClipboard()
            
            m_bCutting = true;
            GetHelper.cut(this.getGraphWindow());
            m_bCutting = false;
            
            this.setIsDirty(true);
         }
         else
         {
            //::MessageBeep((UINT)-1);
         }
      }
   }
   
   public void copy()
   {
       // Need to clear all the clipboards of the other diagrams
       IProxyDiagramManager pManager = ProxyDiagramManager.instance();
       ISimpleBroadcastAction pAction = new SimpleBroadcastAction();
       
       boolean bHandled = false;
       
       ETList < TSGraphObject > selectedObjs = null;
       if(getGraph() instanceof ETGraph)
       {
           ETGraph etGraph =  (ETGraph) getGraph();
           selectedObjs = etGraph.getSelectedObjects(false, false);
       }
       
       // First fire it off to the diagram engine
       if (m_DiagramEngine != null)
       {
           bHandled = m_DiagramEngine.preCopy();
       }
       
       // Allow the engine to cancel the copy
       if (!bHandled)
       {
           if (pManager != null && pAction != null)
           {
               pAction.setKind(DiagramAreaEnumerations.SBK_CLEAR_CLIPBOARD);
               
               pManager.broadcastToAllOpenDiagrams(pAction);
               
               ETElementManager manager = new ETElementManager(this);
               
               // The GET will copy to TS internal clip and the windows clip.
               manager.onPreCopy();
               GetHelper.copy(getGraphWindow());
               manager.onPostCopy();
               
               // Since TS doesn't have copy and paste working between diagrams yet
               // we also copy the selected elements onto the IProduct clipboard.
               // If a paste is performed we first check to see if we've got something in the TS clipboard,
               // if not then check the proxy diagram clip.
               IProduct pProduct = getProduct();
               
               if (pProduct != null)
               {
                   // Clear it out first
                   pProduct.setCrossDiagramClipboard("");
                   
                   ETList < IPresentationElement > pSelected = getSelected();
                   
                   ADTransferable transferable = new ADTransferable("DRAGGEDITEMS");
                   
                   transferable.addModelAndPresentationElements(pSelected);
                   transferable.addGenericElement(Integer.toString(this.hashCode()));
                   String text = null;
                   
                   try
                   {
                       text = (String)transferable.getTransferData(ADTransferable.ADDataFlavor.stringFlavor);
                   }
                   catch(UnsupportedFlavorException e)
                   {
                       throw new AssertionError("Unsupported Flavor "+e.toString());
                   }
                   catch(IOException e)
                   {
                       throw new AssertionError("IOException "+e.toString());
                   }
                   
                   if (text != null && text.length() > 0)
                   {
                       // Put these items on the clip
                       pProduct.setCrossDiagramClipboard(text);
                   }
               }
           }
           
           if(selectedObjs != null)
           {
               Iterator < TSGraphObject > iter = selectedObjs.iterator();
               while(iter.hasNext() == true)
               {
                   TSGraphObject obj = iter.next();
                   TSGraph graph = obj.getOwnerGraph();
                   if(graph != null)
                   {
                       TSGraphManager manager = graph.getOwnerGraphManager();
                       if(manager instanceof TSEGraphManager)
                       {
                           TSEGraphManager tseManager = (TSEGraphManager)manager;
                           tseManager.setGraphWindow(getGraphWindow());
                       }
                   }
               }
           }
           
           if (m_DiagramEngine != null)
           {
               m_DiagramEngine.postCopy();
           }
       }
       
   }
   
   
   public void paste()
   {
      
      int nDiagramKind = getDiagramKind();
      
      if (nDiagramKind != DiagramEnums.DK_SEQUENCE_DIAGRAM)
      {
         // The XML fragment that's on the ADProduct has a generic element
         // which is the hashcode of the diagram.  We use this hashcode to
         // determine if this is a cross diagram paste or intra diagram paste.

         IProduct pProduct = getProduct();
            
         if (pProduct != null && m_DiagramEngine != null)
         {
            // Clear it out first
            String clip = pProduct.getCrossDiagramClipboard();
            if ( (clip != null) && (!clip.equals("")) )
            {
               Document pDoc = XMLManip.loadXML(clip);
               if (pDoc != null)
               {
                  String query = "//DRAGGEDITEMS/GENERICELEMENT[@DESCSTRING=\"";
                  query += Integer.toString(this.hashCode());
                  query += "\"]";
                  
                  Node root = pDoc.selectSingleNode(query);
                  if (root != null)
                  {
                     // This adproduct paste information is from us, so just use
                     // the TS clipboard.
                     GetHelper.paste(getGraphWindow());
                  }
                  else
                  {
                     // Use cross diagram paste since last diagram to copy/cut was
                     // not us.
                     m_DiagramEngine.beginCrossDiagramPaste();
                  }
               }
            }
         }
      }
   }
   
   public void onDeleteSelected()
   {
      if(!getReadOnly())
	  getGraphWindow().deleteSelected();
   }
   
   public void duplicate()
   {
      getGraphWindow().duplicate();
   }
   
   public void onClearAll()
   {
      //getGraphWindow().clearAll(true);
       getGraphWindow().clearAll();
   }
   
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#itemsOnClipboard(boolean)
     */
   public boolean itemsOnClipboard()
   {
      boolean bItemsOnClipboard = GetHelper.itemsOnClipboard( getGraphWindow() );

      if ( ! bItemsOnClipboard )
      {
         // No items in our clipboard.  Is there a copy String on the product?
         // Since TS doesn't have copy and paste working between diagrams yet
         // we also copy the selected elements onto the IProduct clipboard.
         // If a paste is performed we first check to see if we've got something in the TS clipboard,
         // if not then check the proxy diagram clip.
         
         // Sun Issue 6184447:  Implemented code from C++ to get new diagrams
         // do enable the Paste menu item properly.
         IProduct product = getProduct();
         if (product != null)
         {
            String sClipString = product.getCrossDiagramClipboard();
            if (sClipString.length() > 0)
            {
               bItemsOnClipboard = true;
            }
         }
      }
      
      return bItemsOnClipboard;
   }
   
   /**
    * Get the type of this drawing
    *
    * @param pVal [out,retval] Returns the type of this diagram as a DiagramKind
    */
   public int getDiagramKind()
   {
      return getDiagramKind(getDiagramKind2());
   }
   
   /**
    * Get the type of this drawing
    *
    * Returns the type of this diagram as a DiagramKind, given a diagram display name.
    */
   protected int getDiagramKind(String diagramKindDisplayName)
   {
      Integer fromName = DiagramTypeMap.getDiagramType(diagramKindDisplayName);
      return fromName != null ? fromName.intValue() : 0;
   }
   
   /**
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl#Filename()
    */
   public String getFilename()
   {
      return m_FileName;
   }
   
   /**
    * Fit the current diagram to the window.
    */
   public void fitInWindow()
   {
      this.getActions().onFitInWindow();
   }
   
   /**
    * Does this graph have edges?
    */
   public long hasEdges(boolean bHasEdges)
   {
      long count = this.getGraph().numberOfEdges();
      if (count > 0)
      {
         bHasEdges = true;
      } else
      {
         bHasEdges = false;
      }
      return count;
   }
   
   /**
    * Does this graph have edges, labels or nodes?
    */
   public long hasGraphObjects(boolean bHasGraphObjects)
   {
      TSEGraph pGraph = getGraph();
      
      long count = pGraph != null ? pGraph.numberOfEdges() + pGraph.numberOfLabels() + pGraph.numberOfNodes() : 0;
      bHasGraphObjects = count > 0 ? true : false;
      return count;
   }
   
   /**
    * Does this graph have labels?
    */
   public long hasLabels(boolean bHasLabels)
   {
      long count = this.getGraph().numberOfLabels();
      bHasLabels = count > 0 ? true : false;
      return count;
   }
   
   /**
    * Does this graph have nodes?

    */
   public long hasNodes(boolean bHasNodes)
   {
      long count = this.getGraph().numberOfNodes();
      bHasNodes = count > 0 ? true : false;
      return count;
   }
   
   /**
    * Shows the print setup dialog
    */
   public void loadPrintSetupDialog()
   {
      this.getActions().onPrintSetup();
   }
   
   /**
    * Get the name of this drawing.
    */
   public String getName()
   {
      return m_Name;
   }
   
   /**
    * Set the name of this drawing.
    */
   public void setName(String newVal)
   {
      //		this.setGraphFileName(newVal);
      
      // Use the diagram manager to verify the name
      IProxyDiagramManager pDiaMgr =  ProxyDiagramManager.instance();
      String validDiaName = "";
      boolean isCorrect = false;
      
      // Let the diagram manager verify this name is ok
      validDiaName = newVal; //pDiaMgr.getValidDiagramName(newVal);
      
      boolean isSame = true;
      boolean fireEvents = true;
      if (m_Name != null && m_Name.length() > 0)
      {
         // If the namespace is being changed then fire an event
         if (validDiaName != null && validDiaName.length() > 0)
         {
            if (!m_Name.equals(validDiaName))
            {
               isSame = false;
            }
         }
         else
         {
            isSame = false;
         }
      }
      else if (validDiaName != null && validDiaName.length() > 0)
      {
         isSame = false;
         fireEvents = false;
      }
      
      if (!isSame)
      {
         boolean proceed = true;
         
         // Let folks know that the name is being changed.
         if (getDrawingAreaDispatcher() != null && fireEvents)
         {
            proceed = fireDrawingAreaPrePropertyChange(DiagramAreaEnumerations.DAPK_NAME);
         }
         
         if (proceed)
         {
            m_Name = validDiaName;
            if (fireEvents)
            {
               setIsDirty(true);               
               // Let folks know that the name has changed.
               fireDrawingAreaPropertyChange(DiagramAreaEnumerations.DAPK_NAME);
            }
            
            setDiagramNodeNameAndOwner();
         }
      }
   }
   
   /**
    * Prints this graph
    *
    * @param bShowDialog Set to true to show the print dialog to the user.
    */
   public void printGraph(boolean showDialog)
   {
      ADDrawingAreaPrinter helper = getDrawingAreaPrinter();
      if (getDiagramEngine() != null)
      {
         getDiagramEngine().prePrint(helper.getPrintSetup());
      }
      
      Vector titleVector = new Vector();
      titleVector.add(getName());
      
      helper.getPrintSetup().setCaption(titleVector);
      helper.print(showDialog);
      
      if (getDiagramEngine() != null)
      {
         getDiagramEngine().postPrint(helper.getPrintSetup());
      }
   }
   
   /**
    * Zoom the diagram.
    */
   public void zoom(double scaleFactor)
   {
      String strZoom = (new Double( 100. * scaleFactor )).toString();
      this.getActions().onZoom( strZoom );
   }
   
   /**
    * Zoom in
    */
   public void zoomIn()
   {
      this.getActions().onZoomIn();
   }
   
   /**
    * Zoom out
    */
   public void zoomOut()
   {
      this.getActions().onZoomOut();
   }
   
    /*
     * Is the overview window open.
     */
   public boolean isOverviewWindowOpen()
   {
      return this.overviewWindow != null ? this.overviewWindow.isVisible() : false;
   }
   
    /*
     *
     */
   public void overviewWindow(boolean showIt)
   {
      if (isOverviewWindowOpen() != showIt)
      {
         onShowOverviewWindow();	// This guy toggles.
      }
   }
   
   /**
    * Puts the drawing area into print preview mode
    *
    * @param sTitle The title of the print job - seen on the pages.
    * @param bCanMoveParent Not sure!!
    */
   public void printPreview(String title, boolean canMoveParent)
   {
      ADDrawingAreaPrinter helper = getDrawingAreaPrinter();
      if (getDiagramEngine() != null)
      {
         getDiagramEngine().prePrint(helper.getPrintSetup());
      }
      
      Vector titleVector = new Vector();
      titleVector.add(title);
      
      helper.getPrintSetup().setCaption(titleVector);
      helper.onPrintPreview();
      
      if (getDiagramEngine() != null)
      {
         getDiagramEngine().postPrint(helper.getPrintSetup());
      }
   }
   
   /**
    * Returns true if the IElement being dropped is valid for this diagram
    *
    * @param pMovingElement [in] The element being dropped
    * @return true if pMovingElement is valid to drop on this diagram
    */
   protected boolean isValidDropElement(IElement pMovingElement)
   {
       boolean bIsValid = false;
       
       try
       {
           IDiagramTypesManager pManager = DiagramTypesManager.instance();
           if (pMovingElement != null && pManager != null)
           {
               String sElementType = pMovingElement.getElementType();
               String sShortDisplayName = pManager.getShortDiagramTypeName(getDiagramKind2());
               
               // Make sure our diagram type is in the list, or ALL is in the list.
               bIsValid = ValidDropTargets.instance().isValidDropTarget(sElementType, sShortDisplayName);
           }
       }
       catch (Exception e)
       {
           e.printStackTrace();
           bIsValid = false;
       }
       return bIsValid;
   }
   
    /*
     *  (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl#processOnDropElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
   public IElement processOnDropElement(IElement pElementBeingDropped)
   {
       if (getDiagramEngine() == null)
           return pElementBeingDropped;
       
       ETPairT<Boolean,IElement> processedElement = m_DiagramEngine.processOnDropElement(pElementBeingDropped);
       IElement pDrawEngineChangedElement = processedElement.getParamTwo();
       boolean bCancelThisElement = processedElement.getParamOne().booleanValue();
       
       // If we aren't canceled and the diagram engine didn't change the metatype being created
       // then use the type passed into us.
       if (bCancelThisElement == false && pDrawEngineChangedElement == null)
       {
           pDrawEngineChangedElement = pElementBeingDropped;
       }
       // This is a fix for bug#5110404 [should not allow dragging a package to a sequence diagram]
       //Commenting out to fix bug#6283146
          /*else if (bCancelThisElement == true)
      {
            pElementBeingDropped = null;
      }*/
       
       // Now make sure that the projecttreeengine.etc file says that this diagram is valid, the exception
       // are attributes and operations which are allowed to be dropped on other classes
       IFeature pFeature = pDrawEngineChangedElement instanceof IFeature ? (IFeature) pDrawEngineChangedElement : null;
       boolean validDropElement = isValidDropElement(pDrawEngineChangedElement);
       
       if ((bCancelThisElement == true) ||
           (pFeature == null &&
           pDrawEngineChangedElement != null &&
           validDropElement == false))
       {
           String sElementType;
           String sMessageString;
           
           if(pDrawEngineChangedElement!=null)
               sElementType = pDrawEngineChangedElement.getElementType();
           else
               sElementType = pElementBeingDropped.getElementType();
           
           if (!validDropElement) 
           {
               sMessageString = sElementType.concat(" "+RESOURCE_BUNDLE.getString("DROP_CANCELED"));
               sendMessage(MsgCoreConstants.MT_INFO, sMessageString);
           }
           pDrawEngineChangedElement = null;
           pElementBeingDropped = null;
       }
       return pDrawEngineChangedElement != null ? pDrawEngineChangedElement : pElementBeingDropped;
       
   }
   
   public void switchToDefaultState()
   {
       getResources().setDefault();
       setGraphState(getGraphWindow().getDefaultTool());
   }
   
   /**
    * This method initializes the window that displays and manipulates the graph.
    */
   private void initGraphWindow(ADGraphWindow newGraphWindow)
   {
      if (this.m_GraphWindow != null)
         return;	// Only do this once.
      
      this.m_GraphWindow = newGraphWindow;
      
      // register a graph change listener with the window which
      // is responsible for setting the enabled state of all buttons
            
      trackBarModifyListener = new MyGraphChangeListener(); 
      //adding listeners - JM
      getGraphManager().getEventManager().addGraphChangeListener(getGraphManager(), this.getActions());  
      getGraphManager().getEventManager().addGraphChangeListener(getGraphManager(), trackBarModifyListener, TSGraphChangeEvent.ANY_DISCARDED ); 
      ((TSEEventManager)getGraphManager().getEventManager()).addSelectionChangeListener(getGraphManager(), this.getActions());      
      ((TSEEventManager)getGraphManager().getEventManager()).addViewportChangeListener(getGraphWindow(), this.getActions());
      
      // specify the default state the graph window restores on reset
      // operations
      try
      {
         ADDrawingAreaSelectState tool = new ADDrawingAreaSelectState();         
         tool.setReconnectEdgeTool(new ADReconnectEdgeState(tool));         
         getGraphWindow().setDefaultTool(tool); 
         
      } catch (Exception e)
      {
      }
      
      this.m_GraphWindow.setupETDefaultDrawingPreferences();
      
      
      // see if the user has specified the undo limit, and if so
      // set it on the graph window
      try
      {
         int limit = Integer.parseInt(this.getResources().getStringResource("editor.undo.limit"));
         getGraphWindow().setUndoLimit(limit);
      } catch (Exception e)
      {
         ETSystem.out.println("Unable to set requested undo limit.");
      }
      
      this.addGraphWindow(getGraphWindow());
      addKeyListener(this);
      
      // add a mouse listener to the canvas that requests focus for the root pane whenever it is clicked on.
      
      getGraphWindow().getCanvas().addMouseListener(new MouseHandler(this));
      
      
      //add a key listener too - this will be used to edit the name of the dropped classes
      getGraphWindow().addKeyListener(new KeyHandler());
      getGraphWindow().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.SHIFT_DOWN_MASK), "Edit");
      getGraphWindow().getActionMap().put("Edit", new TestAction());
      
      // now, when the canvas receives the focus, transfer it to the root pane.
      
      getGraphWindow().getCanvas().addFocusListener(new FocusAdapter()
      {
         public void focusGained(FocusEvent event)
         {
            requestFocus();
         }
      });
      
//      getGraphWindow().setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
      
      trackBarChangeListener = new ViewportChangeListener();            
      ((TSEEventManager)(this.getGraphWindow().getGraphManager().getEventManager())).addViewportChangeListener(getGraphWindow(), trackBarChangeListener);
   }
   
   public void showNotImplementedMessage()
   {
      JOptionPane.showMessageDialog(
      this,
      this.getResources().getStringResource("dialog.notImplemented.message"),
      this.getResources().getStringResource("dialog.notImplemented.title"),
      JOptionPane.INFORMATION_MESSAGE);
   }
   
   private void showErrorMessage(String message)
   {
      JOptionPane.showMessageDialog(this, message, this.getResources().getStringResource("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
   }
   
   /**
    * Reload the model element based on the ids
    *
    * @param sModelElementID [in] The XMIID of the model element to reload
    * @param sTopLevelID [in] The XMIID of the toplevel model element where sModelElementID can be found.
    * @param pModelElement [out] The found model element
    * @param pFoundProject [out] The toplevel project this model element is found in.
    */
   private IElement reloadModelElement(String sModelElementID, IProject pFoundProject)
   {
      IApplication app = ProductHelper.getApplication();
      if (app != null)
      {
         IElementLocator locator = new ElementLocator();
         if (pFoundProject != null)
         {
            return locator.findElementByID(pFoundProject, sModelElementID);
         }
      }
      return null;
   }
   
   private IProject reloadModelElement(String sTopLevelID)
   {
      IApplication app = ProductHelper.getApplication();
      return app != null ? app.getProjectByID(sTopLevelID) : null;
   }
   
   /**
    * Populate associated diagrams and elements list
    *
    * @param pArchive [in] The archive (etlp file) that contains the presentation element information.
    */
   private void populateAssociatedDiagramsAndElementsList(IProductArchive pArchive)
   {
      ETList < IProductArchiveElement > associatedDiagrams = pArchive.getAllTableEntries(IProductArchiveDefinitions.ASSOCIATED_DIAGRAMS_STRING);
      ETList < IProductArchiveElement > associatedElements = pArchive.getAllTableEntries(IProductArchiveDefinitions.ASSOCIATED_ELEMENTS_STRING);
      
      // Populate the associated diagrams
      if (associatedDiagrams != null)
      {
         int count = associatedDiagrams.size();
         for (int i = 0; i < count; i++)
         {
            IProductArchiveElement pEle = associatedDiagrams.get(i);
            String xmiid = pEle.getID();
            if (xmiid != null && xmiid.length() > 0)
            {
               addAssociatedDiagram(xmiid);
            }
         }
      }
      
      // Populate the associated elements
      if (associatedElements != null)
      {
         int count = associatedElements.size();
         for (int i = 0; i < count; i++)
         {
            IProductArchiveElement pEle = associatedElements.get(i);
            String xmiid = pEle.getID();
            String sTopLevelXMIID = pEle.getAttributeString(IProductArchiveDefinitions.TOPLEVELID_STRING);
            if (xmiid != null && xmiid.length() > 0 && sTopLevelXMIID != null && sTopLevelXMIID.length() > 0)
            {
               addAssociatedElement(sTopLevelXMIID, xmiid);
            }
         }
      }
   }
   
   /**
    * Reloads the diagram data from the product archive
    *
    * @param pArchive [in] The archive (etlp file) that contains the presentation element information.
    */
   private void readFromArchive(IProductArchive pArchive)
   {
      // Block the delayed actions
      //DelayedActionBlocker blocker = new DelayedActionBlocker();
      if (getGraphWindow() != null)
      {
         ///////////////////////////////////////////////////////////
         // We loaded ok, lets get the diagram information
         ///////////////////////////////////////////////////////////
         IProductArchiveElement pArchEle = pArchive.getElement(IProductArchiveDefinitions.DIAGRAMINFO_STRING);
         if (pArchEle != null)
         {
            // Get the diagram attributes
            long diaKind = pArchEle.getAttributeLong(IProductArchiveDefinitions.DRAWINGKIND_STRING);
            String sDiaKind = pArchEle.getAttributeString(IProductArchiveDefinitions.DRAWINGKIND2_STRING);
            m_DiagramXmiid = pArchEle.getAttributeString(IProductArchiveDefinitions.DIAGRAM_XMIID);
            String sMEID = pArchEle.getAttributeString(IProductArchiveDefinitions.NAMESPACE_MEID);
            String sTopLevelID = pArchEle.getAttributeString(IProductArchiveDefinitions.NAMESPACE_TOPLEVELID);
            m_Name = pArchEle.getAttributeString(IProductArchiveDefinitions.DIAGRAMNAME_STRING);
            m_Alias = pArchEle.getAttributeString(IProductArchiveDefinitions.DIAGRAMALIAS_STRING);
            m_Documentation = pArchEle.getAttributeString(IProductArchiveDefinitions.DIAGRAMNAME_DOCS);
            m_nZoomLevelFromArchive = pArchEle.getAttributeDouble(IProductArchiveDefinitions.DIAGRAM_ZOOM);
            double nPageCenterX = pArchEle.getAttributeDouble(IProductArchiveDefinitions.DIAGRAM_XPOS);
            double nPageCenterY = pArchEle.getAttributeDouble(IProductArchiveDefinitions.DIAGRAM_YPOS);
            m_DiagramSavedWithAliasOn = pArchEle.getAttributeBool(IProductArchiveDefinitions.LAST_SHOWALIAS_STATE);
            //m_LoadedDiagramVersion = pArchEle.getAttributeDouble(IProductArchiveDefinitions.DIAGRAMVERSION_STRING);
            boolean diaIsStub = pArchEle.getAttributeBool(IProductArchiveDefinitions.DIAGRAM_ISSTUB_STRING);

            if (m_nZoomLevelFromArchive == 0.0)
            {
               m_nZoomLevelFromArchive = 1.0;
            }
            
            m_CenterFromArchive = new Point((int) nPageCenterX, (int) nPageCenterY);
            
            // Set the diagram Kind, using the string first so we can eventually get rid of the
            // enumeration
            if (sDiaKind != null && sDiaKind.length() > 0)
            {
               setDiagramKind2(sDiaKind);
            } else
            {
               sDiaKind = CachedDiagrams.getNewDiagramType((int) diaKind);
               setDiagramKind2(sDiaKind);
            }
            
            // Reconnect to the model element
            if (sMEID != null && sMEID.length() > 0 && sTopLevelID != null && sTopLevelID.length() > 0)
            {
               IProject pFoundProj = reloadModelElement(sTopLevelID);
               IElement pFoundModelEle = reloadModelElement(sMEID, pFoundProj);
               if (pFoundModelEle instanceof INamespace)
               {
                  setNamespace((INamespace) pFoundModelEle);
               } else if (pFoundProj != null)
               {
                  setNamespace(pFoundProj);
               } else
               {
                  //throw and exception of InvalidArguments
               }
            } else
            {
               //throw and exception of InvalidArguments
            }
            
            // Load the associated diagrams and model elements
            populateAssociatedDiagramsAndElementsList(pArchive);
            
            if (!diaIsStub)
            {
               // We have a real diagram, not one created by the CDFS stub process
               if (m_DiagramEngine != null)
               {
                  m_DiagramEngine.readFromArchive(pArchive, pArchEle);
               }
               
               // Get the diagram that will be set on each of the objects
               IDiagram pDia = getDiagram();
               
               // Now load the objects into the product archive.  The m_ViewsReadWriteFromETLFile list was
               // created during the read process of the etl file.
               if (m_ViewsReadWriteFromETLFile != null)
               {
                  int count = m_ViewsReadWriteFromETLFile.size();
                  for (int i = 0; i < count; i++)
                  {
                     IETGraphObject obj = (IETGraphObject) m_ViewsReadWriteFromETLFile.get(i);
                     
                     // Set the diagram and load from the archive
                     obj.setDiagram(pDia);
                     obj.load(pArchive);
                  }
                  
                  // Notification of post load.  This is where presentation elements and their
                  // owners are reattached
                  for (int i = 0; i < count; i++)
                  {
                     IETGraphObject obj = (IETGraphObject) m_ViewsReadWriteFromETLFile.get(i);
                     
                     obj.postLoad();
                  }
               }
               
               // Now validate all the link ends, node draw engines, and the
               // labels on the links
               // 91395 Do we really need to correct diagram (get rid of bogus edges etc) 
               // without user's knowledge? Shouldn't we just display it in a state when 
               // user saved it?
               // The answer is yes we do.  See issue 91395.  
               boolean temp = postLoadVerification();
               //Jyothi: #91395 Inform the user that the diagram has changed.
                     if (temp) {
                         SwingUtilities.invokeLater(new Runnable() {
                             public void run() {
                                 System.err.println("The diagram is being modified based on the changes in the model");
                                 NotifyDescriptor d =
                                         new NotifyDescriptor.Message(
                                         "The diagram is being modified based on the changes in the model",
                                         NotifyDescriptor.INFORMATION_MESSAGE);
                                 DialogDisplayer.getDefault().notify(d);
                             }
                         });                                                 
                     }
 
               // Clear our load list
               m_ViewsReadWriteFromETLFile.clear();
            } else
            {
               if (m_ReadOnly)
               {
                  // See if the user wants to checkout by sending out a predirty state change event
                  checkForSCCCheckout();
               }
               if (m_ReadOnly)
               {
                  if (!m_AbortDiagramLoad)
                  {
                     // We have a readonly diagram - we can't open this stub.  Tell the user                     
                  }
               } else
               {
                  // Handle the stub diagram
                  initializeNewDiagram();
                  // Handle any CDFS that needs to happen
                  handleDelayedCDFS(pArchive);
               }
               
            }
         }
      }
   }
   
   /**
    * @param pArchive
    */
   private void handleDelayedCDFS(IProductArchive archive)
   {
      ETList < IProductArchiveElement > allElements;
      ETList < IElement > elements = null;
      long count = 0;
      ElementReloader reloader = new ElementReloader();
      
      allElements = archive.getAllTableEntries( IProductArchiveDefinitions.DIAGRAM_CDFS_STRING );
      
      for (Iterator iter = allElements.iterator(); iter.hasNext();)
      {
         IProductArchiveElement foundElement = (IProductArchiveElement)iter.next();
         
         String sXMIID = foundElement.getID();
         String sTopLevelXMIID = foundElement.getAttributeString( IProductArchiveDefinitions.TOPLEVELID_STRING );
         boolean bIgnoreThisElementForCDFS = foundElement.getAttributeBool( IProductArchiveDefinitions.DIAGRAM_IGNOREFORCDFS_STRING );
         if ( (sXMIID.length() > 0) &&
         (sTopLevelXMIID.length() > 0) &&
         (!bIgnoreThisElementForCDFS) )
         {
            IElement tempElement = reloader.getElement( sTopLevelXMIID, sXMIID );
            if (tempElement != null)
            {
               if( null == elements )
               {
                  elements = new ETArrayList< IElement >();
               }
               elements.add(tempElement);
            }
         }
      }
      
      // If we've got a list then CDFS the list.  Go ahead and post the action
      // if if there aren't any elements so we'll save on opening in the event
      
      // Fix J633:  For some reason in the java code elements get created from the namespace
      //            that were not being created in the C++ code.   To fix this issue, we
      //            assume that if there are no elements from the stub diagram then
      //            we don't need to CDFS.
      if( elements != null )
      {
         ISimpleElementsAction action = new CDFSAction(this);
         assert (action != null);
         if (action != null)
         {
            if (elements != null)
            {
               action.setElements(elements);
            }
            
            DelayedExecutor exe = new DelayedExecutor(this, action);
            ETGraphManager mgr =(ETGraphManager) this.getGraphManager();
            SwingUtilities.invokeLater(exe);
         }
      }
      else
      {
         // Fix J936:  Since we do not call the delayed action above,
         //            we need to save the diagram here.
         setIsDirty( true );
         // why implicit save here? How would user discard the changes later?
//         save();
      }
   }
   
   public class DelayedExecutor implements Runnable
   {
      IDrawingAreaControl m_Control = null;
      ISimpleElementsAction m_Action = null;
      
      public DelayedExecutor(IDrawingAreaControl ctrl,
      ISimpleElementsAction action)
      {
         m_Control = ctrl;
         m_Action = action;
      }
      
      public void run()
      {
         if((m_Action != null) && (m_Control != null))
         {
            m_Action.execute(m_Control);
         }
      }
   }
   
   /**
    *
    */
   private void checkForSCCCheckout()
   {
      // TODO Auto-generated method stub
      
   }
   
   /**
    *
    */
   private boolean postLoadVerification()
   {
      boolean modifiedDiagram = false;
      
      ETList<IETGraphObject> etGraphObjects = getAllItems6();
      
      int count = etGraphObjects != null ? etGraphObjects.getCount() : 0;
      
      boolean deletedPEFound = false;
      boolean peRemoved = false;
      boolean drawEnginesInvalid = false;
      
      for(int index=0; index < count; index++)
      {
         IETGraphObject etGraphObject = etGraphObjects.item(index);
         IPresentationElement presentationElement = TypeConversions.getPresentationElement(etGraphObject);
         
         IEdgePresentation edgePresentation = presentationElement instanceof IEdgePresentation ? (IEdgePresentation)presentationElement : null;
         INodePresentation nodePresentation = presentationElement instanceof INodePresentation ? (INodePresentation)presentationElement : null;
         
         if(edgePresentation != null && !edgePresentation.validateLinkEnds() && !edgePresentation.reconnectLinkToValidNodes())
         {
            // Delete the bogus edge
            postDeletePresentationElement(presentationElement);
            peRemoved = true;
         }
         else if (nodePresentation != null)
         {
            IETGraphObject nodeETGraphObject = etGraphObject;
            
            IGraphObjectValidation graphObjectValidation = new GraphObjectValidation();
            
            if(graphObjectValidation != null)
            {
               graphObjectValidation.addValidationKind(IDiagramValidateKind.DVK_VALIDATE_DRAWENGINE);
               
               nodeETGraphObject.validate(graphObjectValidation);
               
               int nResult = graphObjectValidation.getValidationResult(IDiagramValidateKind.DVK_VALIDATE_DRAWENGINE);
               
               if(nResult == IDiagramValidateResult.DVR_INVALID)
               {
                  resetDrawEngine2(nodeETGraphObject);
                  drawEnginesInvalid = true;
               }
            }
         }
         else if (presentationElement == null)
         {
            TSEEdge edge = TypeConversions.getOwnerEdge(etGraphObject);
            if(edge != null)
            {
               TSNode sourceNode = edge.getSourceNode();
               TSNode targetNode = edge.getTargetNode();
               
               IPresentationElement sourceElement = TypeConversions.getPresentationElement(sourceNode);
               IPresentationElement targetElement = TypeConversions.getPresentationElement(targetNode);
               
               if(sourceElement != null && targetElement != null)
               {
                  // TODO I don't think this logic is in here or goes here or something
                  ISimpleElementsAction action = new SimpleElementsAction();
                  
                  if(action != null)
                  {
                     action.add(sourceElement);
                     action.add(targetElement);
                     action.setKind(DiagramAreaEnumerations.SEAK_DISCOVER_RELATIONSHIPS);
                     postDelayedAction(action);
                  }
               }
            }
            if(etGraphObject != null)
            {
               ETList<IETGraphObject> deleteThese = new ETArrayList<IETGraphObject>();
               deleteThese.add(etGraphObject);
               
               removeGraphObjects(deleteThese, false);
               modifiedDiagram = true;
            }
         }
         
         if (peRemoved == false && etGraphObject != null)
         {
//            boolean wasDeleted = etGraphObject.getWasModelElementDeleted();
            
            if(etGraphObject.getWasModelElementDeleted())
            {
               ETList<IETGraphObject> deleteThese = new ETArrayList<IETGraphObject>();
               deleteThese.add(etGraphObject);
               
               removeGraphObjects(deleteThese, false);
               modifiedDiagram = true;
               
                    /*
                    TSEEdge tseEdge = TypeConversions.getOwnerEdge(etGraphObject);
                    TSENode tseNode = TypeConversions.getOwnerNode(etGraphObject);
                     
                    if(tseEdge != null) {
                        postDeletePresentationElement(tseEdge);
                        deletedPEFound = true;
                    }
                    else if (tseNode != null) {
                        postDeletePresentationElement(tseNode);
                        deletedPEFound = true;
                    }
                     */
            }
            else
            {
               IDrawEngine drawEngine = etGraphObject.getEngine();
               if(drawEngine != null)
               {
                  ILabelManager labelManager = drawEngine.getLabelManager();
                  if(labelManager != null)
                  {
                     labelManager.resetLabelsText();
                  }
               }
            }
         }
      }
      
      etGraphObjects = null;
      
      if(deletedPEFound)
      {
         //			SendMessage(MT_DEBUG, IDS_FOUNDDELETEDPES);
         setIsDirty(true);
      }
      if(peRemoved)
      {
         //			SendMessage(MT_DEBUG,IDS_PRESENTATIONELEMENTSWEREREMOVED);
         setIsDirty(true);
      }
      
      if(drawEnginesInvalid)
      {
         //			SendMessage(MT_DEBUG,IDS_DRAWENGINESREST);
         setIsDirty(true);
      }
      return modifiedDiagram;
   }
   
   /**
    * Adds an associated diagram to our list
    *
    * @param sDiagramXMIID [in] The xmiid of the diagram to associate to.
    */
   public void addAssociatedDiagram(String sDiagramXMIID)
   {
      if (!m_ReadOnly && sDiagramXMIID != null && sDiagramXMIID.length() > 0)
      {
         if (!sDiagramXMIID.equals(m_DiagramXmiid))
         {
            boolean found = false;
            if (m_AssociatedDiagrams != null)
            {
               int count = m_AssociatedDiagrams.size();
               for (int i = 0; i < count; i++)
               {
                  String str = (String) m_AssociatedDiagrams.get(i);
                  if (str.equals(sDiagramXMIID))
                  {
                     found = true;
                     break;
                  }
               }
            } else
            {
               m_AssociatedDiagrams = new Vector();
            }
            if (!found)
            {
               m_AssociatedDiagrams.add(sDiagramXMIID);
               if (!IOSemaphore.isIOHappenning())
               {
                  setIsDirty(true);
               }
            }
         }
      }
      
   }
   
   /**
    * Adds an associated diagram to our list
    *
    * @param pDiagram [in] The diagram we should associate to
    */
   public void addAssociatedDiagram2(IProxyDiagram pDiagram)
   {
      if (pDiagram != null)
      {
         String xmiid = pDiagram.getXMIID();
         if (!m_ReadOnly && xmiid != null && xmiid.length() > 0)
         {
            addAssociatedDiagram(xmiid);
         }
      }
   }
   
   /**
    * Adds an associated element to our list
    *
    * @param sTopLevelElementXMIID [in] The elements toplevel id
    * @param sModelElementXMIID [in] The element we should associate to
    */
   public void addAssociatedElement(String xmiid, String meID)
   {
      if (!m_ReadOnly && xmiid != null && xmiid.length() > 0 && meID != null && meID.length() > 0)
      {
         Object obj = m_AssociatedElements.get(xmiid);
         if (obj == null)
         {
            Vector meidCol = new Vector();
            meidCol.add(meID);
            m_AssociatedElements.put(xmiid, meidCol);
            if (!IOSemaphore.isIOHappenning())
            {
               setIsDirty(true);
            }
         }
         else
         {
            Vector meidCol = (Vector)obj;
            
            //see if this element already exists
            boolean foundEle = false;
            int count = meidCol.size();
            for (int i=0; i<count; i++)
            {
               String str = (String)meidCol.get(i);
               if (str != null && str.equals(meID))
               {
                  foundEle = true;
                  break;
               }
            }
            
            if (!foundEle)
            {
               meidCol.add(meID);
               if (!IOSemaphore.isIOHappenning())
               {
                  setIsDirty(true);
               }
            }
         }
      }
   }
   
   /**
    * Adds an associated element to our list
    *
    * @param pElement [in] The element we should associate to
    */
   public void addAssociatedElement2(IElement pElement)
   {
      if (!m_ReadOnly && pElement != null)
      {
         String topId = pElement.getTopLevelId();
         String xmiid = pElement.getXMIID();
         if (topId != null && topId.length() > 0 && xmiid != null && xmiid.length() > 0)
         {
            addAssociatedElement(topId, xmiid);
         }
      }
      
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#addPresentationElementToTrackBar(com.embarcadero.describe.foundation.IPresentationElement)
     */
   public void addPresentationElementToTrackBar(IPresentationElement pElement)
   {
      // Make sure the graph window is valid, there is a lot of posting going
      // on in the Webreport, (Kevin)
      if(getTrackBar() != null && this.getGraphWindow() != null)
      {
         getTrackBar().addPresentationElement(pElement);
      }
      
   }

   
   /**
    * Adds an item to the ETL read/write list.
    *
    * @param pItem [in] The graph object to add to our list of items that need to be serialized.
    */
   public void addReadWriteItem(IETGraphObject pItem)
   {
      // We need to make sure that the object views all are loaded from the IProductArchive.  Hidden and
      // folded nodes aren't in our list of graph objects on the anchor view.  We have to call buildFoldGraphList(true)
      // buildNestedGraphList(true) and for each graph get the .hideGraph property.  Them combine all those
      // objects for a list of every node.  What a pain!  Rather then that we keep track of what TS has serialized
      // in and use that to reload our objects.
      if (pItem != null)
      {
         boolean found = false;
         if (m_ViewsReadWriteFromETLFile != null)
         {
            int count = m_ViewsReadWriteFromETLFile.size();
            for (int i=0; i<count; i++)
            {
               IETGraphObject obj = (IETGraphObject)m_ViewsReadWriteFromETLFile.get(i);
               if (obj.equals(pItem))

               {
                  found = true;
                  break;
               }
            }
         }
         else
         {
            m_ViewsReadWriteFromETLFile = new Vector();
         }
         if (!found)
         {
            m_ViewsReadWriteFromETLFile.add(pItem);
         }
      }
   }
   
   /**
    * Begins the editing context so if there's a name collision we can handle it.
    *
    * @param pCompartment [in] The compartment being edited
    */
   public void beginEditContext(ICompartment pCompartment)
   {
      if (m_CollisionHandler != null && m_NameCollisionListener != null)
      {
         m_NameCollisionListener.setEnabled(true);
         m_CollisionHandler.setCompartment(pCompartment);
      }
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#beginOLEDrag(com.tomsawyer.graph.TSGraphObject)
     */
   public void beginOLEDrag(TSGraphObject graphObject)
   {
      // TODO Auto-generated method stub
      
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#centerPresentationElement(com.embarcadero.describe.foundation.IPresentationElement, boolean, boolean)
     */
   public void centerPresentationElement(IPresentationElement pPresentationElement,
   boolean bSelectIt,
   boolean bDeselectAllOthers)
   {
      if( null == getGraphWindow() )
      {
         return;
      }
      
      IETGraphObject tsObject = TypeConversions.getETGraphObject(pPresentationElement);
      if(tsObject != null)
      {
         if (bDeselectAllOthers)
         {
            getGraphWindow().deselectAll(false);
         }
         
         if(bSelectIt)
         {
            getGraphWindow().selectObject(tsObject.getObject(), true);
         }
         
         TSConstPoint goToPoint = null;
         if(tsObject.isNode() == true)
         {

            if (tsObject instanceof ETNode)
            {
               ETNode node = (ETNode)tsObject;
               goToPoint = node.getCenter();                              
            }
         }
         else if(tsObject.isEdge() == true)
         {
            if (tsObject instanceof ETNode)
            {
               ETEdge edge = (ETEdge)tsObject;
               goToPoint = edge.getSourcePoint();
            }
         }
         
         if (goToPoint != null)
         {
            getGraphWindow().centerPointInWindow(goToPoint, true);
         }
      }
      
   }
   
   /**
    * Centers and,optionally, selects the following presentation element on the drawing area
    *
    * @param sXMIID [in] The xmiid of the presentation element to center
    * @param bSelectIt [in] true if we should select it.
    * @param bDeselectAllOthers [in] true if we should deselect all others.
    */
   public void centerPresentationElement2(String sXMIID,
   boolean bSelectIt,
   boolean bDeselectAllOthers)
   {
      IPresentationElement pe = this.findPresentationElement(sXMIID);
      if (pe != null)
      {
         centerPresentationElement(pe, bSelectIt, bDeselectAllOthers);
      }
   }
   
   //JM: wrote this method to select presentation object in the graph
   public void selectPresentationElement(IPresentationElement pPresentationElement,
   boolean bSelectIt,
   boolean bDeselectAllOthers) {
       
       if( null == getGraphWindow() ) {
           return;
       }
       IETGraphObject tsObject = TypeConversions.getETGraphObject(pPresentationElement);
       if(tsObject != null) {
           if (bDeselectAllOthers) {
               getGraphWindow().deselectAll(false);
           }
           if(bSelectIt) {
               getGraphWindow().selectObject(tsObject.getObject(), true);
               //tell the world that selection has changed
               ETGraph etGraph = getGraph() instanceof ETGraph ? (ETGraph) getGraph() : null;
               if (etGraph != null) {
                   this.fireSelectEvent(etGraph.getSelectedObjects(false, false));
                   this.refresh(true);
               }
           }
       }
   }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#changeEdgeDescription(java.lang.String)
     */
   public void changeEdgeDescription(String edgeUI)
   {
      splitEdgeViewDescription(edgeUI);
      
      ETGenericEdgeUI ui = null;
      String type = edgeUI;
      
      TSEGraphWindow graph = getGraphWindow();
      
      if (graph != null)
      {
         IPresentationTypesMgr mgr = getPresentationTypesMgr();
         PresentationTypeDetails details = mgr.getInitStringDetails(edgeUI, getDiagramKind());
         
         int delimiter = edgeUI.indexOf(' ');
         String edgeUIClass = edgeUI.substring(0, delimiter);
         String drawEngineClass = details.getEngineName();
         
         if ((edgeUIClass != null) && (edgeUIClass.length() > 0) && (drawEngineClass != null) && (drawEngineClass.length() > 0))
         {
            try
            {
               ui = ETUIFactory.createEdgeUI(edgeUIClass, edgeUI, drawEngineClass, this);
               //             ui.setDrawEngineClass(drawEngineClass);
               //             ui.setDrawingArea(control);
            } catch (ETException e)
            {
               //            this.showErrorMessage(e.getMessage());
            }
            
            // add user specified properties
            this.setUIAttributes(type, ui);
            
            if (ui != null)
            {
               graph.setCurrentEdgeUI(ui);
            }
            
            graph.setEdgeCreatedWithLabel("true".equals(getResources().getStringResource(type + ".labeled"))); //$NON-NLS-1$ //$NON-NLS-2$
         }
      }
   }
   
   /**
    * Clears the TS clipboard
    */
   public void clearClipboard()
   {
      // Sun Issue 6184441:  It appears that TS is setting up the clipboard
      // then sending use a the delete command, which we were then clearing
      // the clipboard.  We use m_bCutting to indicate we are cutting so that
      // we don't clear the clipboard.
      // @see cut()
            
      if( !m_bCutting )
      {
         GetHelper.clearClipboard(this.getGraphWindow());
      } 
      
   }
   
   /**
    * Paste the items from the clipboard assumed to be from another diagram
    */
   public void crossDiagramPaste(Point location)
   {
      
      // Since TS doesn't have copy and paste working between diagrams yet
      // we also copy the selected elements onto the IProduct clipboard.
      // If a paste is performed we first check to see if we've got something in the TS clipboard,
      // if not then check the proxy diagram clip.
      IProduct pProduct = getProduct();
      
      if (pProduct != null)
      {
         // Get the clip string from the product.
         String sClipString = pProduct.getCrossDiagramClipboard();
         
         if (sClipString != null && sClipString.length() > 0)
         {
            String tempString = sClipString;
            TSConstPoint pPoint = m_GraphWindow.getWorldPoint(location);
            IApplication pApplication = ProductHelper.getApplication();
            



            if (pPoint != null && pApplication != null)
            {
               // DragAndDropSupport dragAndDropSupport;
               ETList < IElement > pModelElements = new ETArrayList < IElement > ();
               ETList < IPresentationElement > pPresentationElements = new ETArrayList < IPresentationElement > ();
               ETList < String > diagramLocations = new ETArrayList < String > ();
               
               DragAndDropSupport.getElementsOnClipboard(tempString, pApplication, "DRAGGEDITEMS", pModelElements, pPresentationElements, diagramLocations);
               
               clonePresentationElements(pPoint, pPresentationElements);
            }
         }
      }
   }
   
   /**
    * Clones these presentation elements
    *
    * @param pCurrentPoint [in] Our current location
    * @param pPEs [in] The presentation elements to be cloned and dropped on to the drawing area.
    */
   private void clonePresentationElements(TSConstPoint pCurrentPoint, ETList<IPresentationElement> pPEs)
   {
        int toCloneCount = 0;
        double x = 0;
        double y = 0;
        IDiagram pDiagram = getDiagram();
        IDiagram pSourceDiagram = null;
        TSGraph pGraph = getGraph();

        if (pCurrentPoint != null && pPEs != null && pGraph != null)
        {
            toCloneCount = pPEs.getCount();
            x = pCurrentPoint.getX();    
            y = pCurrentPoint.getY();
        }

        if (toCloneCount!= 0)
        {
            // Get the selected node bounding rectangle,
            // so we don't have to invalidate the entire drawing area
            TSRect pInvalidateRect = null;
            TSConstRect rectBounding = new TSRect(0,0,0,0);

            // Create a union of the presentation elements view bounding rects
            for (int i = 0 ; i < toCloneCount ; i++)
            {
                IPresentationElement pThisPE = pPEs.item(i);
                if (pThisPE != null)
                {
                    TSENode pTSENode = TypeConversions.getOwnerNode(pThisPE);
                    if (pTSENode != null)
                    {
                        TSConstRect thisBounds = (TSConstRect) pTSENode.getBounds();

                        if (i == 0)
                        {
                            rectBounding = thisBounds;
                        }
                        else
                        {
                            rectBounding = rectBounding.union(thisBounds);
                        }
                    }

                    if(pSourceDiagram == null && pThisPE instanceof IProductGraphPresentation)
                    {
                        // Get the source diagram from this presentation elment
                        IProductGraphPresentation pGraphPresentation = (IProductGraphPresentation)pThisPE;
                        pSourceDiagram = pGraphPresentation.getDiagram();
                    }
                }
            }

            // We have to keep track of new presentation elements and how they related to the old
            // presentation elements.  Specifically we use this when creating edges and we need to
            // know the new source and target nodes.
            ETList<IPresentationElement> oldPEs = new ETArrayList<IPresentationElement>();
            ETList<IPresentationElement> newPEs = new ETArrayList<IPresentationElement>();

            // See now what the offset is from the bounding rect of all the PEs to the
            // current point being the center.
            TSConstPoint boundingCenter = rectBounding.getCenter();
            double centerX = boundingCenter.getX();
            double centerY = boundingCenter.getY();

            double deltaX = x - centerX;
            double deltaY = y - centerY;

            // This loop clones all node presentation elements.  We need to do nodes first
            // because the edges need their source and target nodes before we create them.
            for (int i = 0 ; i < toCloneCount ; i++)
            {
                IPresentationElement pThisPE = (IPresentationElement)pPEs.item(i);
                INodePresentation nodePE = null;
                if(pThisPE instanceof INodePresentation)
                    nodePE = (INodePresentation)pThisPE;

                if (nodePE!=null)
                {
                    boolean bIsAllowed = false;

                    IDrawEngine pThisDrawEngine = TypeConversions.getDrawEngine(pThisPE);
                    TSENode pTSENode = TypeConversions.getOwnerNode(nodePE);
                    IETNode etnode = TypeConversions.getETNode(nodePE);
                    IElement pElement = TypeConversions.getElement(nodePE);

                    if (pElement!=null)
                    {
                        bIsAllowed = isAllowedOnDiagram(pElement);
                    }

                    if (pThisDrawEngine!=null && pTSENode!=null && etnode!=null && bIsAllowed)
                    {
                        long nWidth   = nodePE.getWidth();
                        long nHeight  = nodePE.getHeight();
                        int nXCenter = nodePE.getCenter().getX();
                        int nYCenter = nodePE.getCenter().getY();

                        IETPoint pCenterPoint = new ETPoint(nXCenter+(int)deltaX,
                            nYCenter+(int)deltaY);

                        IETNode pCreatedNode = etnode.createNodeCopy(pDiagram, pCenterPoint);
                        etnode.setDiagram(getDiagram());

                        // Add this to our list of presentation elements
                        IPresentationElement pCreatedPE = TypeConversions.getPresentationElement(pCreatedNode);
                        
                        if(pCreatedPE instanceof IProductGraphPresentation)
                        {
                            IProductGraphPresentation pgp= (IProductGraphPresentation)pCreatedPE;
                            pgp.reconnectPresentationElement(TypeConversions.getElement(pThisPE));
                        }
                        
                        // retain the size and the center point of the node the same
                        // as those of the souce node.
                        if (pCreatedPE instanceof NodePresentation) 
                        {
                            NodePresentation nodePre = (NodePresentation) pCreatedPE;
                            long newWidth = nodePre.getWidth();
                            long newHeight = nodePre.getHeight();
                            IETPoint newCenterPoint = nodePre.getCenter();
                            // Make sure to keep the center point of the node the same as the source node
                            nodePre.getTSNode().setCenter(
                                    (double)pCenterPoint.getX(), (double)pCenterPoint.getY());
                            if (newWidth != nWidth || newHeight != nHeight) 
                            {
                                // set the size to the size of the source node
                                nodePre.resize(nWidth, nHeight, false);

                            }   
                        }
                        
                        oldPEs.add(nodePE);
                        newPEs.add(pCreatedPE);
                    }
                }
            }
            
            // Now create the edges
            for (int i = 0 ; i < toCloneCount ; i++)
            {
                IPresentationElement pThisPE = pPEs.item(i);

                IEdgePresentation pEdgePE = null;
                if(pThisPE instanceof IEdgePresentation)
                    pEdgePE = (IEdgePresentation)pThisPE;

                if (pEdgePE!=null)
                {
                    // Get the edges from and to node
                    INodePresentation pFromPENode = pEdgePE.getEdgeFromPresentationElement();
                    INodePresentation pToPENode = pEdgePE.getEdgeToPresentationElement();

                    IPresentationElement pCreatedFromPENode = null;
                    IPresentationElement pCreatedToPENode = null;
                    IDrawEngine pThisDrawEngine = TypeConversions.getDrawEngine(pThisPE);

                    // Translate the old source and target to the new presentation element nodes
                    // that should be used for the new source and target
                    if (pThisDrawEngine != null && pFromPENode != null && pToPENode != null)
                    {
                        for (int listIndex = 0; listIndex < oldPEs.getCount(); listIndex++)
                        {
                            if (pCreatedFromPENode == null && pFromPENode == oldPEs.get(listIndex))
                            {
                                pCreatedFromPENode = newPEs.get(listIndex);
                            }
                            else if (pCreatedToPENode == null && pToPENode == oldPEs.get(listIndex))
                            {
                                pCreatedToPENode = newPEs.get(listIndex);
                            }

                            // Break when we find the source and targets
                            if (pCreatedFromPENode != null && pCreatedToPENode != null)
                            {
                                break;
                            }
                        }
                    }

                    if (pCreatedFromPENode != null && pCreatedToPENode != null ||
                            pFromPENode == pToPENode)
                    {
                        if (pFromPENode == pToPENode) {
                            pCreatedToPENode = pCreatedFromPENode;
                        }
                        TSEEdge pTSEEdge = TypeConversions.getOwnerEdge(pEdgePE,false);
                        IETEdge pETEdge = TypeConversions.getETEdge(pTSEEdge);
                        
                        
                        if (pTSEEdge != null && pETEdge != null)
                        {
                            TSConstPoint sourcePoint = (TSConstPoint) pTSEEdge.getSourcePoint();
                            
                            IETPoint pNewSourcePoint = 
                                new ETPoint((int)(Math.round(sourcePoint.getX()+deltaX)),(int)(Math.round(sourcePoint.getY()+deltaY)));
                            
                            IETEdge pCreatedEdge = pETEdge.createEdgeCopy(pDiagram, pNewSourcePoint,pCreatedFromPENode, pCreatedToPENode);
                            pCreatedEdge.setDiagram(getDiagram());

                            // Add this to our list of presentation elements
                            IPresentationElement pCreatedPE = TypeConversions.getPresentationElement(pCreatedEdge);
                            
                            if(pCreatedPE instanceof IProductGraphPresentation)
                            {
                                IProductGraphPresentation pgp= (IProductGraphPresentation)pCreatedPE;
                                pgp.reconnectPresentationElement(TypeConversions.getElement(pThisPE));
                            }
                           
                            // take care of the bends (i.e. path nodes) if any
                            int noOfPathNodes = pTSEEdge.numberOfPathNodes();
                            if (noOfPathNodes > 0 ) {
                                TSConstPoint pNewBendPoint = null;
                                List bendPointList = new ArrayList();
                                Iterator bendPointIter = pTSEEdge.bendPoints().iterator();
                                while (bendPointIter.hasNext()) {
                                    TSConstPoint bendPoint = (TSConstPoint) bendPointIter.next();
                                    pNewBendPoint = new TSConstPoint (bendPoint.getX()+deltaX, bendPoint.getY()+deltaY);
                                    bendPointList.add(pNewBendPoint);
                                }
                                ((TSEEdge)pCreatedEdge).reroute(bendPointList);
                            }
                            oldPEs.add(pEdgePE);
                            newPEs.add(pCreatedPE);
                        }
                    }
                }
            }

            // Now restore presentation references.  If I find a partial presentation reference I'll
            // delete the node I just created if it's a referred element.  This will stop someone from
            // copying just the port on the component diagram and doing a paste without its
            // component diagram.
            for (int i = 0 ; i < toCloneCount ; i++)
            {
                IPresentationElement pThisPE = pPEs.item(i);
                INodePresentation pNodePE = null;
                if(pThisPE instanceof INodePresentation)
                    pNodePE = (INodePresentation)pThisPE;

                if (pNodePE!=null)
                {
                    ETList<IPresentationElement> pReferrencingElements 
                        = PresentationReferenceHelper.getAllReferencingElements(pNodePE);

                    int count = 0;
                    if (pReferrencingElements!= null)
                    {
                        count = pReferrencingElements.getCount();
                    }

                    if (count != 0)
                    {
                        // This pNodePE is a child of some other object in a presentationreference relationship
                        // make sure the parent is there or post a delete of this PE we just created.
                        boolean bDone = false;
                        for (int j = 0; j < count && bDone == false ; j++)

                        {
                            IPresentationElement pRefPE = pReferrencingElements.item(j);
                            if (pRefPE!=null)
                            {
                                boolean bIsInList = pPEs.isInList(pRefPE);
                                if (!bIsInList)

                                {
                                    for(int listIndex = 0; listIndex < oldPEs.getCount(); listIndex++)
                                    {
                                        if (pNodePE == oldPEs.get(listIndex))
                                        {
                                            // Find the node we just created that matches pNodePE and whack that guy.
                                            postDeletePresentationElement(newPEs.get(listIndex));
                                            oldPEs.remove(listIndex);
                                            newPEs.remove(listIndex);
                                            bDone = true;
                                            break;
                                        }
                                    }
                                }
                                else
                                {
                                    // Find the parent and child in our created list and restore the presentation reference
                                    // relationship
                                    IPresentationElement pParentPE = null;
                                    IPresentationElement pChildPE = null;

                                    for (int listIndex = 0; listIndex < oldPEs.getCount(); ++listIndex)
                                    {
                                        if (pParentPE == null && pRefPE == oldPEs.get(listIndex))
                                        {
                                            pParentPE = newPEs.get(listIndex);
                                        }
                                        else if (pChildPE == null && pNodePE == oldPEs.get(listIndex))


                                        {
                                            pChildPE = newPEs.get(listIndex);
                                        }

                                        if (pParentPE != null && pChildPE != null)

                                        {
                                            break;
                                        }
                                    }

                                    if (pParentPE != null && pChildPE != null)
                                    {
                                        PresentationReferenceHelper.createPresentationReference(pParentPE, pChildPE);
                                    }
                                    else if (pNodePE!=null)
                                    {
                                        for (int listIndex = 0; listIndex < oldPEs.getCount();++listIndex)
                                        {
                                            if (pNodePE == oldPEs.get(listIndex))
                                            {
                                                // The parent is gone.  Don't leave the child hanging, just delete it
                                                postDeletePresentationElement(pRefPE);
                                                oldPEs.remove(listIndex);
                                                newPEs.remove(listIndex);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Now tell each of the created presentation elements that they have just been pasted

            ETElementManager manager = new ETElementManager(this);
            for (int listIndex = 0; listIndex < newPEs.getCount(); ++listIndex)
            {
                if (newPEs.get(listIndex)!=null)
                {
                    // Fire the event
                    manager.dispatchToETGraphObject(newPEs.get(listIndex), IGraphEventKind.GEK_POST_CROSS_DIAGRAM_PASTE);
                }
            }

            // Now go through all the nodes and begin containment on them if necessary.  This will
            // change the namespace of the just dropped element to that of the container
            if (m_DiagramEngine!=null)
            {
                for (int listIndex = 0; listIndex < newPEs.getCount(); ++listIndex)
                {
                    if(newPEs.get(listIndex) instanceof INodePresentation)
                    {
                        INodePresentation nodePresentation = (INodePresentation)newPEs.get(listIndex);
                        m_DiagramEngine.postAddObjectHandleContainment(nodePresentation);
                    }
                }
            }
        }
        refresh(true);
    }

   
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#delayedLayoutStyle(int, boolean)
     */
   public void delayedLayoutStyle(int nLayoutStyle, boolean bIgnoreContainment)
   {
      // TODO Auto-generated method stub
      
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#deleteChildGraph(com.tomsawyer.graph.TSGraphObject)
     */
   public void deleteChildGraph(TSGraphObject pGraphObject)
   {
      // TODO Auto-generated method stub
      
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#deleteSelected(boolean)
     */
   public void deleteSelected(boolean bAskUser)
   {
      if(getReadOnly() == false )
      {
         handleKeyDown(KeyEvent.VK_DELETE, 0, bAskUser);
      }
   }
   
   /**
    * Delete these items, the model elements might be deleted as well
    * depending on the state of the bAffectModelDataAsWell flag.
    *
    * @param nodeVector The nodes to delete.
    * @param edgeVector The edges to delete.
    * @param edgeLabelVector The edge labels to delete.
    * @param nodeLabelVector The node labels to delete.
    * @param bAffectModelDataAsWell Set to true to delete model
    *                               elements as well.
    */
   protected void deleteTheseItems(ETList<TSENode> selectedNodes,
   ETList<TSEEdge> selectedEdges,
   ETList<TSENodeLabel> selectedNodeLabels,
   ETList<TSEEdgeLabel> selectedEdgeLabels,
   boolean affeModelDataAsWell)
   {
      try
      {
         
         // This flag is used in case we have disconnected elements.  In that case there
         // is no PE and we need to refresh the entire diagram.
         boolean  bCouldntGetPresentationElement = false;
         
         for (Iterator < TSENode > iter = selectedNodes.iterator(); iter.hasNext();)
         {
            INodePresentation pres = TypeConversions.getNodePresentation(iter.next());
            if(pres != null)
            {
               pres.invalidate();
            }
            else
            {
               bCouldntGetPresentationElement = true;
            }
         }
         
         for (Iterator < TSEEdge > iter = selectedEdges.iterator(); iter.hasNext();)
         {
            IEdgePresentation pres = TypeConversions.getEdgePresentation(iter.next());
            if(pres != null)
            {
               pres.invalidate();
            }
            else
            {
               bCouldntGetPresentationElement = true;
            }
         }
         
         for (Iterator < TSENodeLabel > iter = selectedNodeLabels.iterator(); iter.hasNext();)
         {
            ILabelPresentation pres = TypeConversions.getLabelPresentation(iter.next());
            if(pres != null)
            {
               pres.invalidate();
            }
            else
            {
               bCouldntGetPresentationElement = true;
            }
         }
         
         for (Iterator < TSEEdgeLabel > iter = selectedEdgeLabels.iterator(); iter.hasNext();)
         {
            ILabelPresentation pres = TypeConversions.getLabelPresentation(iter.next());
            if(pres != null)
            {
               pres.invalidate();
            }
            else
            {
               bCouldntGetPresentationElement = true;
            }
         }
         
         if(bCouldntGetPresentationElement == true)
         {
            refresh(true);
         }
         
         removeNodeLabels(selectedNodeLabels, affeModelDataAsWell);
         removeEdgeLabels(selectedEdgeLabels, affeModelDataAsWell);
         removeEdges(selectedEdges, affeModelDataAsWell);
         removeNodes(selectedNodes, affeModelDataAsWell);

		 //JM: Cleaning up both clipboards after delete to Fix Bug#6286590
		 this.clearClipboard(); 
         this.getProduct().setCrossDiagramClipboard("");

         refresh(true);
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }
   
   /*
    * Returns true if the object is on the diagram.
    */
   public boolean getIsOnDiagram(ITSGraphObject pObject)
   {
      if (pObject instanceof IETGraphObject)
      {
         return !visit(new ETFindObjectVisitor((IETGraphObject)pObject));
      }
      return false;
   }
   
    /*
     * Removes any kind of ITSGraphObject from the diagram.
     */
   protected void removeGraphObjects(List objects, boolean affectModelElement)
   {
      try
      {
         IteratorT<ITSGraphObject> iter = new IteratorT<ITSGraphObject>(objects);
         while (iter.hasNext())
         {
            final ITSGraphObject curObj = iter.next();
            if (affectModelElement == true)
            {
               // The user wants to affect a model element deletion so tell the
               // graph object to affect the model data file - in most cases the
               // element is deleted, in some cases something more exciting
               // happens (ie nested link removes the child from the namespace).
               affectModelElement((TSGraphObject)curObj.getObject());
            }
            
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    //If its still on the diagram delete it
                    if (getIsOnDiagram(curObj))
                    {
                       curObj.delete();
                       refresh(true);
                    }
                }
            });
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }
   
   protected void removeNodeLabels(ETList<TSENodeLabel> labels,
   boolean affectModelElement)
   {
      this.removeGraphObjects(labels, affectModelElement);
   }
   
   protected void removeEdgeLabels(ETList<TSEEdgeLabel> labels, boolean affectModelElement)
   {
      this.removeGraphObjects(labels, affectModelElement);
   }
   
   protected void removeEdges(ETList<TSEEdge> edges, boolean affectModelElement)
   {
      this.removeGraphObjects(edges, affectModelElement);
   }
   
   protected void removeNodes(ETList<TSENode> nodes,  boolean affectModelElement)
   {
      this.removeGraphObjects(nodes, affectModelElement);
   }
   
   protected void affectModelElement(TSGraphObject obj)
   {
      IETGraphObject gObj = TypeConversions.getETGraphObject(obj);
      if(gObj != null)
      {
         gObj.affectModelElementDeletion();
      }
      
      handlePreDeleteObject(obj);
   }
   
   /**
    * Handles the pre delete event without checking for the use input block.
    *
    * @param graphObj The graph object that is being deleted.
    */
   protected boolean handlePreDeleteObject(TSGraphObject graphObj)
   {
      boolean retVal = false;
      
      if(getReadOnly() == true)
      {
         retVal = true;
      }
      else if(graphObj != null)
      {
         // Empty out the clipboard in case this element is sitting in there
         clearClipboard();
         
         if (graphObj instanceof IETNode)
         {
            IETNode node = (IETNode)graphObj;
            
            // Notify all the connected edges that they are about to be deleted,
            // since TS is not sending those edges
            List inEdges = node.getInEdges();
            for (Iterator iter = inEdges.iterator(); iter.hasNext();)
            {
               TSEEdge curEdge = (TSEEdge)iter.next();
               sendPreDeleteLinkEvents(curEdge);
            }
            
            List outEdges = node.getOutEdges();
            for (Iterator iter = outEdges.iterator(); iter.hasNext();)
            {
               TSEEdge curEdge = (TSEEdge)iter.next();
               sendPreDeleteLinkEvents(curEdge);
            }
         }
         
         // Remove the presentation object
         IPresentationElement presentationElement = TypeConversions.getPresentationElement(graphObj);
         if(presentationElement != null)
         {
            removePresentationElement(presentationElement);
            
            JTrackBar bar = getTrackBar();
            if(bar != null)
            {
               bar.removePresentationElement(presentationElement);
            }
         }
         
         setIsDirty(true);
      }
      
      
      return retVal;
   }
   
   /**
    * Calls OnPreDeleteLink on the to and from nodes, if available
    *
    * @param pObject [in] The object, which is a TS edge, that is about to be deleted
    */
   protected void sendPreDeleteLinkEvents(TSEEdge graphObject)
   {
      if(graphObject != null)
      {
         IEdgePresentation presElement = TypeConversions.getEdgePresentation(graphObject);
         if(presElement != null)
         {
            ETPairT<INodePresentation, INodePresentation> nodes = presElement.getEdgeFromAndToPresentationElement();
            
            INodePresentation fromNode = nodes.getParamOne();
            if(fromNode != null)
            {
               fromNode.onPreDeleteLink(presElement, true);
            }
            
            INodePresentation toNode = nodes.getParamTwo();
            if(toNode != null)
            {
               toNode.onPreDeleteLink(presElement, false);
            }
         }
      }
   }
   
   /**
    * Removes this presentation element from all of its associated model elements.
    *
    * @param presentationElement The presentation element to be removed.
    */
   protected void removePresentationElement(IPresentationElement presentationElement)
   {
      if(presentationElement != null)
      {
         ETList< IElement >  subjects = presentationElement.getSubjects();
         if(subjects != null)
         {
            for (Iterator < IElement > iter = subjects.iterator(); iter.hasNext();)
            {
               IElement curElement = iter.next();
               if(curElement != null)
               {
                  curElement.removePresentationElement(presentationElement);
               }
            }
         }
      }
   }
   
   /**
    * Gather up all the selected items into vectors
    *
    * @param selectedNodes The selected nodes.
    * @param selectedEdges The selected edges.
    * @param selectedNodeLabels The selected node labels.
    * @param selectedEdgeLabels The selected edge labels.
    */
   protected void gatherSelectedItems(ETList<TSENode> selectedNodes,
   ETList<TSEEdge> selectedEdges,
   ETList<TSENodeLabel> selectedNodeLabels,
   ETList<TSEEdgeLabel> selectedEdgeLabels)
   {
      TSEGraph graph = getGraph();
      if(graph != null)
      {
         List nodes = graph.selectedNodes();
         if(nodes != null)
         {
            selectedNodes.addAll(nodes);
         }
         
         List edges = graph.selectedEdges();
         if(edges != null)
         {
            selectedEdges.addAll(edges);
         }
         
         List edgeLabels = graph.selectedEdgeLabels();
         if(edgeLabels != null)
         {
            selectedEdgeLabels.addAll(edgeLabels);
         }
         
         List nodeLabels = graph.selectedNodeLabels();
         if(nodeLabels != null)
         {
            selectedNodeLabels.addAll(nodeLabels);
         }
      }
      
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#deviceToLogicalPoint(com.embarcadero.describe.umlsupport.IETPoint)
    */
   public IETPoint deviceToLogicalPoint( IETPoint device )
   {
      IETPoint etPoint = null;
      
      if( device != null )
   {
         etPoint = deviceToLogicalPoint( device.getX(), device.getY() );
      }
      
      return etPoint;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#deviceToLogicalPoint(com.embarcadero.describe.umlsupport.IETPoint)
     */
   public IETPoint deviceToLogicalPoint(int x, int y)
   {
      IETPoint etPoint = null;
      
      ADGraphWindow wnd = getGraphWindow();
      if( wnd != null )
      {
         etPoint = new ETPointEx( wnd.getNonalignedWorldPoint( new Point( x, y )) );
      }
      
      return etPoint;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#deviceToLogicalRect(com.embarcadero.describe.umlsupport.IETRect)
     */
   public IETRect deviceToLogicalRect(IETRect pDevice)
   {
      return deviceToLogicalRect((int) pDevice.getLeft(),
      (int) pDevice.getTop(),
      (int) pDevice.getWidth(),
      (int) pDevice.getHeight());
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#deviceToLogicalRect(com.embarcadero.describe.umlsupport.IETRect)
     */
   public IETRect deviceToLogicalRect(double left, double top, double width, double height)
   {
      if(getGraphWindow() != null)
      {
         TSTransform transform = getGraphWindow().getTransform();
         if (transform != null)
         {
            return new ETRectEx(transform.boundsToWorld((int) left,
            (int) top,
            (int) width,
            (int) height));
         }
      }
      
      return null;
   }
   
   /**
    * Alerts the nodes/edges/labels that their underlying model element has gone to that great model in the sky
    *
    * @param pTargets [in] A list of all the guys that need to know that a model element has been deleted.
    */
   public void elementDeleted(INotificationTargets pTargets)
   {
      
      if (pTargets != null)
      {
         // Tell the listeners about the event we're about to dispatch
         this.fireChangeNotificationTranslator(pTargets);
         
         ETList < IPresentationElement > pPEs = pTargets.getPresentationElementsToNotify();
         
         if (pPEs != null)
         {
            try
            {
               IteratorT < IPresentationElement > iter = new IteratorT < IPresentationElement > (pPEs);
               while (iter.hasNext())
               {
                  IPresentationElement presentationElement = iter.next();
                  if (presentationElement != null)
                  {
                     IETGraphObject etGraphObject = TypeConversions.getETGraphObject(presentationElement);
                     if (etGraphObject != null)
                     {
                        etGraphObject.modelElementDeleted(pTargets);
                        setIsDirty(true);
                        //etGraphObject.delete();
                     }
                  }
               }
            } catch (Exception e)
            {
               e.printStackTrace();
            }
         }
      }
      
      // Fix J1427:  This refresh call is not in the C++, but seems to be necessary.
      //             I (BDB) have added the invokeLater() to delay the action after the
      //             lifeline draw engines have validated themselves when the user deletes
      //             an operation from the project tree that is the invoked operation of
      //             a message on an SQD.
      SwingUtilities.invokeLater( new Runnable()
      {
         public void run()
         {
            refresh(false);
         }
      } );
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#elementModified(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
     */
   public void elementModified(INotificationTargets pTargets)
   {
      // setLastChangeTime();
      fireChangeNotificationTranslator(pTargets);
      
      IElement modelElement = pTargets.getChangedModelElement();
      ETList < IPresentationElement > presentationElements = pTargets.getPresentationElementsToNotify();
      
      if(presentationElements != null)
      {
         try
         {
            IteratorT<IPresentationElement> iter = new IteratorT<IPresentationElement>(presentationElements);
            while(iter.hasNext())
            {
               IPresentationElement presentationElement = iter.next();
               
               if(presentationElement != null)
               {
                  IETGraphObject etGraphObject = TypeConversions.getETGraphObject(presentationElement);
                  if(etGraphObject != null)
                  {
                     if (presentationElement instanceof IGraphPresentation)
                     {
                        IGraphPresentation gPresentation = (IGraphPresentation)presentationElement;
                        gPresentation.invalidate();
                     }
                     etGraphObject.modelElementHasChanged(pTargets);
                     setIsDirty(true);
                     
                     GraphObjectValidation graphObjectValidation = new GraphObjectValidation();
                     graphObjectValidation.addValidationKind(IDiagramValidateKind.DVK_VALIDATE_LINKENDS);
                     graphObjectValidation.addValidationKind(IDiagramValidateKind.DVK_VALIDATE_CONNECTIONTOELEMENT);                                   
                     
                     int result = graphObjectValidation.getValidationResult(IDiagramValidateKind.DVK_VALIDATE_LINKENDS);
                     if(result == IDiagramValidateResult.DVR_INVALID)
                     {
                        if(presentationElement instanceof IEdgePresentation)
                        {
                           IEdgePresentation edgePresentation = (IEdgePresentation)presentationElement;
                           boolean successfullyConnected = edgePresentation.reconnectLinkToValidNodes();
                           if(!successfullyConnected)
                           {
                              postDeletePresentationElement(presentationElement);                              
                           }
                        }
                     }
                     
                     result = graphObjectValidation.getValidationResult(IDiagramValidateKind.DVK_VALIDATE_CONNECTIONTOELEMENT);
                     if(result == IDiagramValidateResult.DVR_INVALID)
                     {
                        postDeletePresentationElement(presentationElement);
                     }
                  }
               }
            }
         } catch(InvalidArguments e)
         {
            e.printStackTrace();
         }
      }
      //this.refresh(false);
   }
   
   void fireChangeNotificationTranslator(INotificationTargets targets)
   {
      IDiagram diagram = getDiagram();
      
      if(diagram != null && m_drawingAreaDispatcher != null)
      {
         IEventPayload payload = m_drawingAreaDispatcher.createPayload("ChangeNotificationTranslator");
         
         m_drawingAreaDispatcher.fireGetNotificationTargets(diagram,targets,payload);
      }
      
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#elementTransformed(com.embarcadero.describe.coreinfrastructure.IClassifier)
     */
   public void elementTransformed(IClassifier classifier)
   {
      ETList<IPresentationElement> pPresentationElements = getAllItems2(classifier);
      int count = 0;
      if (pPresentationElements != null)
      {
         count = pPresentationElements.size();
      }
      
      for (int i = 0; i < count ; i++)
      {
         IPresentationElement pPresentationElement = pPresentationElements.get(i);
         
         if (pPresentationElement != null)
         {
            IETGraphObject pETGraphObject = TypeConversions.getETGraphObject(pPresentationElement);
            if (pETGraphObject != null)
            {
               if (m_PresentationTypesMgr != null)
               {
                  int nDiagramKind = getDiagramKind();
                  String sInitString = m_PresentationTypesMgr.getMetaTypeInitString(classifier, nDiagramKind);
                  if (sInitString != null && sInitString.length() > 0)
                  {
                     resetDrawEngine(pETGraphObject, sInitString);
                  }
                  else
                  {
                     postDeletePresentationElement(pPresentationElement);
                  }
               }
               setIsDirty(true);
            }
         }
      }
   }
   
   /**
    * Ends the editing context so if there's a name collision we can handle it.
    */
   public void endEditContext()
   {
      if (m_CollisionHandler != null && m_NameCollisionListener != null)
      {
         m_NameCollisionListener.setEnabled(false);
         m_CollisionHandler.setCompartment(null);
      }
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#endOnDrawZoom()
     */
   public void endOnDrawZoom()
   {
      // TODO Auto-generated method stub
      
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#enterMode(int)
     */
   public void enterMode(int nDrawingToolKind)
   {
      if (getDiagramEngine() != null)
      {
         getDiagramEngine().enterMode(nDrawingToolKind);
         
         // For these tools selection reset the state on any 'select' state listeners.
         // These result in the drawing area state being changed to 'select'
         if(nDrawingToolKind == IDrawingToolKind.DTK_SELECTION ||
            nDrawingToolKind == IDrawingToolKind.DTK_ZOOM ||
            nDrawingToolKind == IDrawingToolKind.DTK_MOUSE_ZOOM ||
            nDrawingToolKind == IDrawingToolKind.DTK_PAN ||
            nDrawingToolKind == IDrawingToolKind.DTK_EDGENAV_MOUSE ) {
           setSelectStateOnPalette();  
         }
             
      }
   }
   
   /**
    * Puts the drawing area into a specific mode based on the sButtonID.
    * sButtonID is a key in the PresentationTypes.etc file.
    *
    * @param sButtonID The button string in the presentation types file.  A
    *                  lookup is performed tofigure out the TS init string.
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#enterModeFromButton(java.lang.String)
    */
   public void enterModeFromButton(String sButtonID)
   {
      boolean handled = false;
      
      // save edit and deselect
      saveEditCompartment();
      
      //getGraphWindow().deselectAll(false);
      
      if (getDiagramEngine() != null)
      {
         handled = getDiagramEngine().enterModeFromButton(sButtonID);
      }
      
      if (handled == false)
      {
         IPresentationTypesMgr mgr = getPresentationTypesMgr();
         if (mgr != null)
         {
            String initStr = mgr.getButtonInitString(sButtonID, getDiagramKind());
            PresentationTypeDetails details = mgr.getInitStringDetails(initStr, getDiagramKind());
            
            int objectKind = details.getObjectKind();
            if (objectKind != TSGraphObjectKind.TSGOK_INVALID && initStr.length() > 0)
            {
               switch (objectKind)
               {
                  case TSGraphObjectKind.TSGOK_NODE :
                     setNodeDescription(initStr);
                     break;
                  case TSGraphObjectKind.TSGOK_EDGE :
                     setEdgeDescription(initStr);
                     break;
                  case TSGraphObjectKind.TSGOK_NODE_DECORATOR :
                     setMode(ADDrawingAreaConstants.CREATE_NODE_DECORATOR_CMD, initStr);
                     break;
                  case TSGraphObjectKind.TSGOK_NODE_RESIZE :
                     //setMode(ADDrawingAreaConstants.NODE_RESIZE_CMD, initStr);
                     setNodeDescription(initStr);
                     break;
                  default :
                     sendDebugMessage("Unknown object kind");
               }
            }
         }
      }
   }
   
   public void setSelectedPaletteButton (String sButtonID) 
   {
       mSelectedPaletteBttn = sButtonID;
   }
   
   public String getSelectedPaletteButton () 
   {
       return mSelectedPaletteBttn;
   }
   
   private void saveEditCompartment()
   {
      if (getEditCompartment() != null)
      {
         getEditCompartment().save();
         m_EditCompartment = null;
      }
   }
   
   
   public void onMoveForward() {
	  executeStackingCommand(IDrawingAreaControl.SOK_MOVEFORWARD, true);
   }

   public void onMoveToFront() {
	  executeStackingCommand(IDrawingAreaControl.SOK_MOVETOFRONT, true);
   }

   public void onMoveBackward() {
	  executeStackingCommand(IDrawingAreaControl.SOK_MOVEBACKWARD, true);
   }

   public void onMoveToBack() {
	  executeStackingCommand(IDrawingAreaControl.SOK_MOVETOBACK, true);
   }
   
   public void executeStackingCommand(int pStackingCommand, boolean pRedraw)
   {
      if (this.isStackingCommandAllowed(pStackingCommand))
      {
         ETList < IPresentationElement > selectedObjects = this.getSelected();
         this.executeStackingCommand(selectedObjects, pStackingCommand, pRedraw);
      }
   }
   
   public void executeStackingCommand(IPresentationElement pPresentationElement, int pStackingCommand, boolean pRedraw)
   {
      if (this.isStackingCommandAllowed(pStackingCommand))
      {
         if (pPresentationElement != null)
         {
            
            ETList < IPresentationElement > peList = new ETArrayList < IPresentationElement > ();
            
            peList.add(pPresentationElement);
            this.executeStackingCommand(peList, pStackingCommand, pRedraw);
         }
      }
   }
   
   public void executeStackingCommand(ETList<IPresentationElement> pPresentationElements, int pStackingCommand, boolean pRedraw)
   {
      
      if (this.isStackingCommandAllowed(pStackingCommand))
      {     
         if (pPresentationElements != null)
         {
            TSEGraph graph = this.getGraph(); 
            
            //1. create the smallest bounding rect of the passed in elements
            TSConstRect selBounds = this.buildMinBoundingRect(pPresentationElements);
            if (selBounds == null)
               return;
            
            //2. get all the visible nodes with bounds that intersect the targe bounding rect
            List overlappingNodes = graph.getNodesTouchingBounds(selBounds, null); 
            
            //3. get the selected nodes out of the overlapping nodes and save them to prevSelected list
            List prevSelected = new ArrayList();
            prevSelected.addAll(this.getSelected(overlappingNodes));
            
            // Fix IZ=95094 - Move to back affects not selected elements
            // The root cause of this issue is if there are other nodes intersecting with the 
            // bounding rect of the container but not intersecting with the selected nodes, 
            // redrawing the container node will cover up these nodes. 
            // Need to get all the nodes that intersect the extended bounding rect and  
            // redraw them all.
            // 4. find the smallest bounding rect that covers all the overlapping nodes
            ETList<IPresentationElement> overlappingPEs = new ETArrayList <IPresentationElement>();
            Iterator listIter = overlappingNodes.iterator();
            while (listIter.hasNext())
            {
               TSNode graphObj = (ETNode) listIter.next();
               IPresentationElement pe = TypeConversions.getPresentationElement(graphObj);
               overlappingPEs.add(pe);
            }
            selBounds = buildMinBoundingRect(overlappingPEs);
            
            //5. get all nodes that touch the extended bounding rec
            List extendedOverlappingNodes = graph.getNodesTouchingBounds(selBounds, null); 

            // make sure the passed in elements are selected
            Iterator peIter = pPresentationElements.iterator();
            while (peIter.hasNext())
            {
               IPresentationElement pe = (IPresentationElement) peIter.next();
               if (pe instanceof INodePresentation)
               {
                  TSENode element = ((INodePresentation) pe).getTSNode();
                  element.setSelected(true);
               }
            }
            
            // Fixed IZ=95091 - Element moved to back is still shown above container
            // Cause: contained objects in a container is being redrawn 
            // as a result of the Refresh() call at the end of this method;
            // Set the flag not to redraw objects contained in a container if any.
            ignoreDrawContainedObject(overlappingNodes, true);
            
            if (pStackingCommand == IDrawingAreaControl.SOK_MOVETOFRONT)
            {
               List tmpList = new ArrayList();
               Iterator desiredStackListIter = extendedOverlappingNodes.iterator();
               while (desiredStackListIter.hasNext())
               {
                  TSENode stackedObj = (TSENode) desiredStackListIter.next();
                  if (stackedObj.isSelected())
                  {
                     tmpList.add(stackedObj);
                  }
               }
               
               Collections.reverse(tmpList);
               Iterator tmpIter = tmpList.iterator();
               while (tmpIter.hasNext())
               {
                  TSENode tmpObj = (TSENode) tmpIter.next();
                  graph.remove(tmpObj);
                  graph.insert(tmpObj);
               }
            } 
            else if (pStackingCommand == IDrawingAreaControl.SOK_MOVETOBACK)
            {
               Collections.reverse(extendedOverlappingNodes);
               Iterator desiredStackListIter = extendedOverlappingNodes.iterator();
               while (desiredStackListIter.hasNext())
               {
                  TSENode stackedNode = (TSENode) desiredStackListIter.next();
                  if (!stackedNode.isSelected()) {
                     graph.remove(stackedNode);
                     graph.insert(stackedNode);
                  }
               }
            }
            else if (pStackingCommand == IDrawingAreaControl.SOK_MOVEFORWARD)
            {
               List tmpList = new ArrayList();
               if (((TSENode)extendedOverlappingNodes.get(0)).isSelected())
                  return;
               Collections.reverse(extendedOverlappingNodes);
               Iterator desiredStackListIter = extendedOverlappingNodes.iterator();
               while (desiredStackListIter.hasNext())
               {
                  TSENode stackedObj = (TSENode) desiredStackListIter.next();
                  if (stackedObj.isSelected())
                  {
                     tmpList.add(stackedObj);
                  } else
                  {
                     graph.remove(stackedObj);
                     graph.insert(stackedObj);
                     
                     Iterator tmpIter = tmpList.iterator();
                     while (tmpIter.hasNext())
                     {
                        TSENode tmpObj = (TSENode) tmpIter.next();
                        graph.remove(tmpObj);
                        graph.insert(tmpObj);
                     }
                     tmpList.clear();
                  }
               }
            } 
            else if (pStackingCommand == IDrawingAreaControl.SOK_MOVEBACKWARD)
            {
               List bakList = new ArrayList();
               List tmpList = new ArrayList();
               Iterator desiredStackListIter = extendedOverlappingNodes.iterator();
               while (desiredStackListIter.hasNext())
               {
                  TSENode stackedObj = (TSENode) desiredStackListIter.next();
                  if (stackedObj.isSelected())
                  {
                     tmpList.add(stackedObj);
                  } else
                  {
                     bakList.add(stackedObj);
                     
                     Iterator tmpIter = tmpList.iterator();
                     while (tmpIter.hasNext())
                     {
                        TSENode tmpObj = (TSENode) tmpIter.next();
                        bakList.add(tmpObj);
                     }
                     tmpList.clear();
                  }
               }
               
               Collections.reverse(bakList);
               Iterator bakIter = bakList.iterator();
               while (bakIter.hasNext())
               {
                  TSENode stackedObj = (TSENode) bakIter.next();
                  if (stackedObj.isSelected())
                  {  // if the selected node is next to the last node in overlappingNodes list,
                     // do not redraw it.
                     int index = overlappingNodes.indexOf(stackedObj);
                     if (index >= (overlappingNodes.size()-2))
                     {
                        continue;
                     }
                  }
                  graph.remove(stackedObj);
                  graph.insert(stackedObj);
               }
            }
            
            // Reselect originally selected objects
            Iterator reselectIter = prevSelected.iterator();
            while (reselectIter.hasNext())
            {
               TSENode graphObj = (TSENode) reselectIter.next();
               graphObj.setSelected(true);
            }
            
            if (pRedraw)
            {
               this.refresh(false);
            }
         }
      }
   }
   
   private void ignoreDrawContainedObject(List overlappingNodes, boolean ignore)
   {
      Iterator listIter = overlappingNodes.iterator();
      while (listIter.hasNext())
      {
         TSENode stackedObj = (TSENode) listIter.next();
         if (stackedObj != null )
         {
            ETGenericNodeUI nodeUI = (ETGenericNodeUI) stackedObj.getNodeUI();
            IDrawEngine objDrawEngine = nodeUI.getDrawEngine();
            if (objDrawEngine instanceof ETContainerDrawEngine)
            {
               ((ETContainerDrawEngine)objDrawEngine).setIgnoreDrawContained(ignore);
            }
         }
      }
   }

   
   // Used by ExecuteStackingCommand to calculate min bounding rect of specfied elements
   private TSConstRect buildMinBoundingRect(List pPresentationElements)
   {
      TSConstRect retValue = null;
      if (pPresentationElements != null)
      {
         Iterator peIterator = pPresentationElements.iterator();
         while (peIterator.hasNext())
         {
            IPresentationElement pe = (IPresentationElement) peIterator.next();
            
            if (pe instanceof IGraphPresentation)
            {
               IETGraphObject element = ((IGraphPresentation) pe).getETGraphObject();
               if (element != null)
               {
                  if (retValue == null)
                  {
                     retValue = element.getBounds();
                  }
                  else
                  {
                     retValue = retValue.union(element.getBounds());
                  }
               }
            }
         }
      }
      return retValue;
   }
   
   //	Used by ExecuteStackingCommand
   private List getSelected(List graphObjects)
   {
      List retValue = new ArrayList();
      if (graphObjects != null)
      {
         Iterator Iter = graphObjects.iterator();
         while (Iter.hasNext())
         {
            IETGraphObject obj = (IETGraphObject) Iter.next();
            if (obj.isNode() && obj.isSelected())
            {
               retValue.add(obj);
            }
         }
      }
      return retValue;
   }
   
   
    /*
     * Returns the presentation element on the drawing area control with the specified xml id
     *
     * @param sXMLID [in] The presentation element to search for on the diagram.
     * @param pPresentationElement [out,retval] The found presentation element.
     */
   public IPresentationElement findPresentationElement(String sXMLID)
   {
      return GetHelper.findPresentationElement(getGraphWindow(), getDiagram(), sXMLID);
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#fireDrawingAreaContextMenuSelected(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem)
     */
   public void fireDrawingAreaContextMenuSelected(IProductContextMenu contextMenu, IProductContextMenuItem selectedItem)
   {
      // TODO Auto-generated method stub
      
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#fireTooltipEvent(com.embarcadero.describe.foundation.IPresentationElement, org.netbeans.modules.uml.ui.support.viewfactorysupport.IToolTipData)
     */
   public void fireTooltipEvent(IPresentationElement pPE, IToolTipData pTooltip)
   {
      // TODO Auto-generated method stub
      
   }
   
   /**
    * Gets the diagram alias
    *
    * @param pVal [out,retval] The alias of the diagram.
    */
   public String getAlias()
   {
      return m_Alias != null && m_Alias.length() > 0 ? m_Alias : getName();
   }
   
   /**
    * Return all the objects on the diagram that are of the indicated type
    *
    * @param sType The element type for the query
    * @return The presentation elements of this type
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getAllByType(java.lang.String)
    */
   public ETList<IPresentationElement> getAllByType(String sType)
   {
      return GetHelper.getAllByType(getGraphWindow(), sType);
   }
   
    /*
     * Returns a list presentation elements from the search that intersect pRect, null it none found.
     */
   protected ETList<IPresentationElement> getObjectsViaRect(IETRect pRect, boolean bTouchingRect, List searchList)
   {
      ETList < IPresentationElement > objectIntersectingRect = new ETArrayList < IPresentationElement > ();
      
      if (searchList != null && pRect != null)
      {
         TSConstRect tsRect = RectConversions.etRectToTSRect(pRect);
         Iterator < IETGraphObject > Iter = searchList.iterator();
         while (Iter.hasNext())
         {
            IETGraphObject graphObj = Iter.next();
            IPresentationElement nodePE = TypeConversions.getPresentationElement(graphObj);
            
            if (nodePE != null && (bTouchingRect && graphObj.getBounds().intersects(tsRect)) ||
            (!bTouchingRect && tsRect.contains(graphObj.getBounds())))
            {
               objectIntersectingRect.add(nodePE);
            }
         }
      }
      return objectIntersectingRect.getCount() > 0 ? objectIntersectingRect : null;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getAllEdgesViaRect(com.embarcadero.describe.umlsupport.IETRect, boolean)
     */
   public ETList<IPresentationElement> getAllEdgesViaRect(IETRect pRect, boolean bTouchingRect)
   {
      TSEGraph pGraph = getGraph();
      return pGraph != null ? getObjectsViaRect(pRect, bTouchingRect, pGraph.edges()) : null;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getAllElementsByDrawEngineType(java.lang.String)
     */
   public ETList<IPresentationElement> getAllElementsByDrawEngineType(String sType)
   {
      try
      {
         if (sType != null && sType.length() > 0)
         {
            ETList < IPresentationElement > pFoundPresentationElements = new ETArrayList < IPresentationElement > ();
            
            visit(new ETDrawEngineTypesMatchVistor(pFoundPresentationElements, sType));
            
            return pFoundPresentationElements.size() > 0 ? pFoundPresentationElements : null;
         }
      } catch (Exception e)
      {
         e.printStackTrace();
      }
      return null;
   }
   
    /*
     * Returns true if all graphobjects were visited.
     */
   public boolean visit(IETGraphObjectVisitor visitor)
   {
      ETGraphObjectTraversal traversal = new ETGraphObjectTraversal((ETGraph)this.getGraph());
      traversal.addVisitor(visitor);
      return traversal.traverse();
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getAllElementsByType(java.lang.String)
     */
    public ETList<IElement> getAllElementsByType(String sType)
   {
      ETList < IElement > elementsOfType = new ETArrayList < IElement > ();
      
      TSEGraph pGraph = getGraph();
      if (pGraph != null)
      {
         addAllByType( elementsOfType, sType, pGraph.nodes() );
         addAllByType( elementsOfType, sType, pGraph.edges() );
      }
      
      return elementsOfType.size() > 0 ? elementsOfType : null;
   }
   
   protected boolean addAllByType( ETList<IElement> elements, String sType, List c )
   {
      if (c != null)
      {
         Iterator < IETGraphObject > iter = c.iterator();
         while (iter.hasNext())
         {
            IElement element = TypeConversions.getElement( iter.next() );
            if( element != null )
            {
               String elementType = element.getElementType();
               if ( (elementType != null) &&
                     elementType.equals( sType ) )
               {
                  elements.add(element);
               }
            }
         }
         return true;
      }
      return false;
   }
   
   /**
    * Returns all the graph objects
    * The stacking order of the output list is from bottom to top.
    *
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getAllGraphObjects()
    */
   public ETList<TSObject> getAllGraphObjects()
   {
      ETGraph graph = getGraph() instanceof ETGraph ? (ETGraph)getGraph() : null;
      return graph != null ? graph.getAllGraphObjects() : null;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getAllItems()
     */
   public ETList<IPresentationElement> getAllItems()
   {
      return GetHelper.getAllItems( m_GraphWindow, getDiagram() );
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getAllItems2(com.embarcadero.describe.foundation.IElement)
     */
   public ETList<IPresentationElement> getAllItems2(IElement pModelElement)
   {
      return GetHelper.getAllItems2( m_GraphWindow, getDiagram(), pModelElement );
   }
   
    /*
     * Returns a list of all the model elements on the diagram.
     */
   public ETList<IElement>  getAllModelElements()
   {
      final ETList < IElement > elements  = new ETArrayList < IElement >();
      visit(new IETGraphObjectVisitor()
      {
         public boolean visit(IETGraphObject object)
         {
            IElement pElement = TypeConversions.getElement(object.getPresentationElement());
            if (pElement != null)
            {
               elements.add(pElement);
            }
            return true;
         }
      });
      
      return elements.size() > 0 ? elements : null;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getAllItems3()
     */
   public ETList<IElement> getAllItems3()
   {
      return getAllModelElements();
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getAllItems4()
     */
   public ETList<IETGraphObject> getAllItems4()
   {
      TSEGraph tsGraph = getGraph();
      ETGraph graph = (tsGraph instanceof ETGraph) ? (ETGraph)tsGraph : null;
      return (graph != null) ? graph.getAllETGraphObjects() : null;
   }
   
    /*
     * Retuns a list of all the node and edge labels on the diagram.
     */
   public ETList<IETLabel> getAllLabels()
   {
      ETGraph graph = getGraph() instanceof ETGraph ? (ETGraph)getGraph() : null;
      ETList < IETLabel > retVal = new ETArrayList < IETLabel >();
      if (graph != null)
      {
         List nodeLabels = graph.nodeLabels();
         if (nodeLabels != null)
         {
            retVal.addAll(nodeLabels);
         }
         
         List edgeLabels = graph.edgeLabels();
         if (edgeLabels != null)
         {
            retVal.addAll(edgeLabels);
         }
      }
      return retVal;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getAllItems5()
     */
   public ETList<IETLabel> getAllItems5()


   {
      return getAllLabels();
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getAllItems6()
     */
   public ETList<IETGraphObject> getAllItems6()
   {
      // In the Java version we don't have IETElement they are graphObjects.
      return getAllItems4();
   }
   
   public ETList<IPresentationElement> getAllItems(String topLevelId, String meid)
   {
      ElementReloader reloader = new ElementReloader();
      IElement foundEle = reloader.getElement(topLevelId, meid);
      return foundEle != null ? getAllItems2(foundEle) : null;
   }
   
   /**
    * Returns a list of all the node presentation elements that represent the IElement
    *
    * @param pModelElement [in] The model element for which we need presentation elements for
    * @param pFoundObjects [out,retval] The presentation elements on this diagram which represent pModelElement
    */
   public ETList<IPresentationElement> getAllNodeItems(IElement pModelElement)
   {
      TSEGraph graph = getCurrentGraph();
      try
      {
         
         if (graph != null)
         {
            ETList < IPresentationElement > pees = getAllItems2(pModelElement);
            if (pees != null)
            {
               int count = pees.size();
               if (count > 0)
               {
                  ETList<IPresentationElement> retObj = new ETArrayList<IPresentationElement>();
                  for (int i=0; i<count; i++)
                  {
                     IPresentationElement pPE = pees.get(i);
                     if (pPE instanceof INodePresentation)
                     {
                        retObj.add((INodePresentation)pPE);
                     }
                  }
                  return retObj;
               }
            }
            
            return pees.size() > 0 ? pees : null;
         }
      } catch (Exception e)
      {
         ETSystem.out.println(e.toString());
      }
      return null;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getAllNodesViaRect(com.embarcadero.describe.umlsupport.IETRect, boolean)
     */
   public ETList<IPresentationElement> getAllNodesViaRect(IETRect pRect, boolean bTouchingRect)
   {
      TSEGraph pGraph = getGraph();
      return pGraph != null ? getObjectsViaRect(pRect, bTouchingRect, pGraph.nodes()) : null;
   }
   
   /**
    * Are the tooltips enabled?
    *
    * @param bEnabled [out,retval] true if tooltips are enabled.
    */
   public boolean getAreTooltipsEnabled()
   {
      return m_TooltipsEnabled;
   }
   
   /**
    * Returns the diagrams associated with this diagram
    *
    * @param pDiagrams [out,retval] The diagrams associated with this diagram.
    */
   public ETList<IProxyDiagram> getAssociatedDiagrams()
   {
      ETList < IProxyDiagram > retObj = new ETArrayList < IProxyDiagram > ();
      if (m_AssociatedDiagrams != null)
      {
         IProxyDiagramManager proxyMgr = ProxyDiagramManager.instance();
         int count = m_AssociatedDiagrams.size();
         for (int i = 0; i < count; i++)
         {
            String diaName = (String) m_AssociatedDiagrams.get(i);
            //IProxyDiagram dia = proxyMgr.getDiagram(diaName);
            IProxyDiagram dia = proxyMgr.getDiagramForXMIID(diaName);
            if (dia != null)
            {
               retObj.add(dia);
            }
         }
      }
      return retObj;
   }
   
   /**
    * Returns a list of the elements associated with this diagram
    *
    * @param pElements [out,retval] The elements associated with this diagram
    */
   public ETList<IElement> getAssociatedElements()
   {
      ETList < IElement > retObj = new ETArrayList < IElement > ();
      if (m_AssociatedElements != null)
      {
         ElementReloader reloader = new ElementReloader();
         Set keys = m_AssociatedElements.keySet();
         Collection vals = m_AssociatedElements.values();
         Iterator<String> iter = keys.iterator();
         Iterator<String> iter2 = vals.iterator();
         
         while (iter.hasNext())
         {
            //get all the meIds associated with this
            Object obj = iter2.next();
            String xmiid = (String)iter.next();
            if (obj != null && obj instanceof Vector)
            {
               Vector meidCol = (Vector)obj;
               int count = meidCol.size();
               for (int i=0; i<count; i++)
               {
                  String str = (String)meidCol.get(i);
                  
                  // xmiid, and meId
                  IElement foundEle = reloader.getElement(xmiid, str);
                  if (foundEle != null)
                  {
                     retObj.add(foundEle);
                  }
               }
            }
         }
      }
      return retObj.size() > 0 ? retObj : null;
   }
   
   //JM: getCurrentGraph() is not giving the correct value after we close and open the projects and/or ide.
   // hence getting the selected graph from the graph manager.
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getCurrentGraph()
     */
//   public TSEGraph getCurrentGraph()
//   {
//      // We need to store the graph for the web report.
//      if (this.m_graph == null && m_GraphWindow != null)
//      {
//         m_graph = m_GraphWindow.getGraph();
//      }
//      return this.m_graph;
//   }
    public TSEGraph getCurrentGraph()
   {
       m_graph = this.getGraphManager().selectedGraph();
       return m_graph;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getCurrentGraphManager()
     */
   public TSGraphManager getCurrentGraphManager()
   {
      return getGraphWindow() != null ? getGraphWindow().getGraphManager() : null;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getCurrentZoom()
     */
   public double getCurrentZoom()
   {
      double zoom = 1.0;
      
      if( getGraphWindow() != null )
      {
         zoom = getGraphWindow().getZoomLevel();
      }
      
      return zoom;
   }
   
   /**
    * Returns the IDiagram that represents this active x control
    *
    * @param pDiagram [out,retval] Returns the diagram that represents this activex control.  The IDiagram
    * acts as a proxy for the control.
    */
   public IDiagram getDiagram()
   {
      IDiagram retDia = null;
      if (m_Diagram == null && getDiagramKind2().length() > 0)
      {
         IDiagramTypesManager pMgr = DiagramTypesManager.instance();
         String diaType = pMgr.getUMLType(getDiagramKind2());
         if (diaType == null)
         {
            diaType = "";
         }
         
         if (diaType.length() > 0)
         {
            Object obj = FactoryRetriever.instance().createType(diaType, null);
            if (obj instanceof IDiagram)
            {
               m_Diagram = (IDiagram) obj;
            }
            
            if (m_Diagram != null)
            {
               retDia = m_Diagram;
               setDiagramNodeNameAndOwner();
               
               // Set the backpointer on the IDiagram to point to this active x control
               if (m_Diagram instanceof IUIDiagram)
               {
                  IUIDiagram axDia = (IUIDiagram) m_Diagram;
                  axDia.setDrawingArea(this);
               }
            }
         }
         
         if (m_DiagramXmiid != null && m_DiagramXmiid.length() > 0 && m_Diagram != null)
         {
            m_Diagram.setXMIID(m_DiagramXmiid);
         }
      } else if (m_Diagram != null)
      {
         retDia = m_Diagram;
      }
      
      return retDia;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getDiagramEngine()
     */
   public IDiagramEngine getDiagramEngine()
   {
      return m_DiagramEngine;
   }
   
   public String getDiagramKind2()
   {
      return m_DiagramKindDisplayName;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getDiagramWindowHandle()
     */
   public int getDiagramWindowHandle()
   {
      // TODO Auto-generated method stub
      return 0;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getDocumentation()
     */
   public String getDocumentation()
   {
      return m_Documentation;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getEdgeDescription()
     */
   public String getEdgeDescription()
   {
      // TODO Auto-generated method stub
      return null;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getExtremeZoomValues(double, double)
     */
   public ETPairT<Double, Double> getExtremeZoomValues()
   {
      // Don't let the user zoom to far in when we are drawing on the sequenece diagram.
      if (getDiagramKind() != DiagramEnums.DK_SEQUENCE_DIAGRAM && this.getDiagram() != null)
      {
         return getDiagram().getExtremeZoomValues();
      }
      else
      {
         return new ETPairT<Double, Double>(new Double(0.05), new Double(4.0));
      }
   }
   
   // Watch out! this does not work
   public TSGraphObject getGraphObjectAtMouse(int logicalX, int logicalY)
   {
      TSGraphObject retObj = null;
      if (getGraphWindow() != null)
      {
          TSEWindowTool tool = getGraphWindow().getCurrentTool();
         if (tool != null)
         {
            //convert to world points
            Point p = getGraphWindow().getCanvas().getLocationOnScreen();
            TSConstPoint newPoint = new TSConstPoint((double)logicalX + p.x, (double)logicalY + p.y);
            retObj = (TSGraphObject)tool.getHitTesting().getGraphObjectAt(new TSConstPoint(newPoint), this.getGraph(), true);
         }
      }
      return retObj;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getGridSize()
     */
   public int getGridSize()
   {
      // TODO Auto-generated method stub
      return 0;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getGridType()
     */
   public int getGridType()
   {
      // TODO Auto-generated method stub
      return 0;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getHasChildGraph(com.tomsawyer.graph.TSNode)
     */
   public boolean getHasChildGraph(TSNode pNode)
   {
      // TODO Auto-generated method stub
      return false;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getHasParentGraph()
     */
   public boolean getHasParentGraph()
   {
      // TODO Auto-generated method stub
      return false;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getHasSelected(boolean)
     */
   public boolean getHasSelected(boolean bDeep)
   {
	   return this.getGraphManager().selectedGraph().hasSelected();
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getHasSelectedEdges(boolean)
     */
   public boolean getHasSelectedEdges(boolean bDeep)
   {
      TSEGraph pGraph = this.getCurrentGraph();
      return pGraph != null ? pGraph.hasSelectedEdges() : false;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getHasSelectedLabels(boolean)
     */
   public boolean getHasSelectedLabels(boolean bDeep)
   {
      TSEGraph pGraph = this.getCurrentGraph();
      return pGraph != null ? pGraph.hasSelectedEdgeLabels() || pGraph.hasSelectedNodeLabels() : false;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getHasSelectedNodes(boolean)
     */
   public boolean getHasSelectedNodes(boolean bDeep)
   {
	   return this.getGraphManager().selectedGraph().hasSelectedNodes();
   }
   
   /**
    * IsDirty is true when there is data that needs to be saved
    *
    * @param bIsDirty [out,retval] true if this diagram needs to be saved.
    */
   public boolean getIsDirty()
   {
      return m_isDirty;
   }
   
   /**
    * Returns true if the model element is displayed in the diagram, NOT whether its in the viewport.
    *
    * @param pModelElement [in] The model element to query
    * @param bIsDisplayed [out,retval] true if this model element is displayed.
    */
   public boolean getIsDisplayed(IElement pModelElement)
   {
      final IElement pSearchForElement = pModelElement;
      boolean visitedAll = visit(new IETGraphObjectVisitor()
      {
         public boolean visit(IETGraphObject object)
         {
            IElement pElement = TypeConversions.getElement(object.getPresentationElement());
            if (pElement != null && pElement.isSame(pSearchForElement))
            {
               return false; // Break the traversal.
            }
            return true;
         }
      });
      return !visitedAll;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getIsGraphPreferencesDialogOpen()
     */
   public boolean getIsGraphPreferencesDialogOpen()
   {
      return graphPreferencesDialog != null && graphPreferencesDialog.isVisible();
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getIsLayoutPropertiesDialogOpen()
     */
   public boolean getIsLayoutPropertiesDialogOpen()
   {
      return layoutPropertiesDialog != null && layoutPropertiesDialog.isVisible();
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getIsOverviewWindowOpen()
     */
   public boolean getIsOverviewWindowOpen()
   {
      return this.isOverviewWindowOpen();
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getLastSelectedButton()
     */
   public int getLastSelectedButton()
   {
      // TODO Auto-generated method stub
      return 0;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getLayoutRunning()
     */
   public boolean getLayoutRunning()
   {
      // TODO Auto-generated method stub
      return false;
   }
   
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getMidPoint(com.tomsawyer.graph.TSEdge, com.embarcadero.describe.umlsupport.IETPoint)
     */
   public IETPoint getMidPoint(TSEdge pEdge)
   {
      return GetHelper.getMidPoint((IETEdge)pEdge);
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getModelElement()
     */
   public IElement getModelElement()
   {
      return modelElement;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getModeLocked()
     */
   public boolean getModeLocked()
   {
      // TODO Auto-generated method stub
      return false;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getNamespace()
     */
   public INamespace getNamespace()
   {
      if (m_Namespace == null)
      {
         //get the project namespace
         IApplication pApp = ProductHelper.getApplication();
         if (pApp != null)
         {
            if (pApp.getNumOpenedProjects() > 0)
            {
               m_Namespace = pApp.getProjects().get(0);
            }
            //namespace = pApp.getProjectByFileName(s_sProjectLocation);
         }
      }
      return m_Namespace;
   }
   
   /**
    * Returns the the namespace to use when elements are created on the diagram.  Usually this
    * is the same as the namespace of the diagram
    *
    * @param pNamespace [out,retval] The namespace that owns this diagram.
    */
   public INamespace getNamespaceForCreatedElements()
   {
      return m_DiagramEngine != null ? m_DiagramEngine.getNamespaceForCreatedElements() : getNamespace();
   }
   
   /**
    * Sets / Gets the name or alias of this element.
    */
   public String getNameWithAlias()
   {
      return ProductHelper.getShowAliasedNames() ? getAlias() : getName();
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getNodeDescription()
     */
   public String getNodeDescription()
   {
      StringBuffer nodeDesc = new StringBuffer(m_NodeInitializationString.m_TSInitializationString);
      
      if(m_NodeInitializationString.m_ObjectInitializersString.length() > 0)
      {
         nodeDesc.append(" ");
         nodeDesc.append(m_NodeInitializationString.m_ObjectInitializersString);
      }
      
      return nodeDesc.toString();
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getOverviewWindowRect(int, int, int, int)
     */
   public IETRect getOverviewWindowRect()
   {
      if (this.overviewWindow != null)
         return new ETRect(overviewWindow.getBounds());
      else if (this.overviewBounds != null)
      {
         return new ETRect(overviewBounds);
      }
      return null;
      
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getPreferenceValue(java.lang.String, java.lang.String)
     */
   public String getPreferenceValue(String sPath, String sName)
   {
       throw new UnsupportedOperationException ("this should never happen.") ;
//      String sValue = "";
//      boolean bFoundInCache = false;
//      if(sPath != null && sName != null)
//      {
//         if (sPath.equals("Diagrams"))
//         {
//            if (m_CachedPreferences.get(sName) != null)
//            {
//               sValue = m_CachedPreferences.get(sName);
//               
//               bFoundInCache = true;
//            }
//         }
//      }
//      
//      if (!bFoundInCache)
//      {
//         IPreferenceManager2 pMgr = ProductHelper.getPreferenceManager();
//         if (pMgr != null)
//         {
//            String sData = pMgr.getPreferenceValue( sPath, sName);
//            if (sData != null && sData.length() > 0)
//            {
//               sValue = sData;
//            }
//         }
//         
//         // Add to our cache if the pref hive is Diagrams
//         if (sPath.equals("Diagrams"))
//         {
//            m_CachedPreferences.put(sName, sValue);
//         }
//      }
//      return sValue;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getPresentationTypesMgr()
     */
   public IPresentationTypesMgr getPresentationTypesMgr()
   {
      return m_PresentationTypesMgr;
   }
   
   /**
    * Returns the current toplevel IProject for this diagram
    *
    * @param pProject [out,retval] The toplevel project of the namespace owning this diagram.
    */
   public IProject getProject()
   {
      return m_Namespace != null ? m_Namespace.getProject() : null;
   }
   
   /**
    * Returns the proxy diagram for this diagram
    *
    * @param pProxyDiagram [out,retval] The proxy diagram representing this diagram.
    */
   public IProxyDiagram getProxyDiagram()
   {
      return getProxyDiagram(m_FileName);
   }
   
   /**
    * Uses a IProxyDiagramManager to determine the IProxyDiagram from a full path filename
    *
    * @param bsFilename [in] Path to the file containing the diagram
    * @param pProxyDiagram [out] The proxy diagram
    *
    * @return HRESULT
    */
   public IProxyDiagram getProxyDiagram(String filename)
   {
      return ProxyDiagramManager.instance().getDiagram(filename);
   }
   
   /**
    * Gets the fully qualified diagram name
    *
    * @param pVal [out,retval] The fully qualified name of the diagram.
    */
   public String getQualifiedName()
   {
      String retVal = "";
      String diaName = getName();
      INamespace space = getNamespace();
      boolean includeProjName = ProductHelper.useProjectInQualifiedName();
      if (diaName != null && diaName.length() > 0 && space != null)
      {
         boolean isProject = false;
         if (space instanceof IProject)
         {
            isProject = true;
         }
         
         if (!includeProjName && isProject)
         {
            retVal = diaName;
         }
         else
         {
            retVal = space.getQualifiedName();
            retVal += "::";
            retVal += diaName;
         }
      }
      return retVal;
   }
   
   /**
    * Is this diagram readonly?
    *
    * @param pVal [out,retval] true if the diagram is readonly
    */
   public boolean getReadOnly()
   {
      return m_ReadOnly;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getRelationshipDiscovery()
     */
   public ICoreRelationshipDiscovery getRelationshipDiscovery()
   {
      return getDiagramEngine() != null ? getDiagramEngine().getRelationshipDiscovery() : null;
   }
   
    /*
     *	Retuns a list of selected presentation elements.
     */
   public ETList<IPresentationElement> getSelected()
   {
      ETGraph etGraph = getGraph() instanceof ETGraph ? (ETGraph) getGraph() : null;
      if (etGraph != null)
      {
         ETList < IPresentationElement > selectdPes = new ETArrayList < IPresentationElement > ();
         ETList < TSGraphObject > selected = etGraph.getSelectedObjects(false, false);
         if (selected != null)
         {
            Iterator < TSGraphObject > iter = selected.iterator();
            while (iter.hasNext())
            {
               TSGraphObject tsObj = iter.next();
               IETGraphObject obj = tsObj instanceof IETGraphObject ? (IETGraphObject) tsObj : null;
               IPresentationElement pe = obj != null ? obj.getPresentationElement() : null;
               if (pe != null)
                  selectdPes.add(pe);
            }
         }
         return selectdPes.size() > 0 ? selectdPes : null;
      }
      return null;
   }
   
   public ETList<IPresentationElement> getSelectedPresentionNodes()
   {
      ETGraph etGraph = getGraph() instanceof ETGraph
          ? (ETGraph) getGraph() 
          : null;
      
      if (etGraph != null)
      {
         ETList<IPresentationElement> selectdPes = 
             new ETArrayList<IPresentationElement>();
         
         ETList<TSGraphObject> selected = 
             etGraph.getSelectedObjects(false, false);
         
         if (selected != null)
         {
            Iterator<TSGraphObject> iter = selected.iterator();
            
            while (iter.hasNext())
            {
               TSGraphObject tsObj = iter.next();
               
               IETGraphObject obj = tsObj instanceof IETGraphObject 
                   ? (IETGraphObject)tsObj 
                   : null;
               
               IPresentationElement pe = obj != null 
                   ? obj.getPresentationElement() 
                   : null;
               
               if (pe != null && pe instanceof INodePresentation)
                  selectdPes.add(pe);
            }
         }
         
         return selectdPes.size() > 0 ? selectdPes : null;
      }
      
      return null;
   }
   
   
   public void deselectAll()
   {
       ETList<IETGraphObject> graphObjects = getSelected3();
       
       if (graphObjects == null || graphObjects.size() == 0)
           return;
       
       for (IETGraphObject graphObj: graphObjects)
       {
           graphObj.setSelected(false);
           graphObj.invalidate();
       }
       
       getGraph().deselectAll();
       //invalidate();
       refresh(true);
   }
 
   public void selectThese(List<IETGraphObject> graphObjects)
   {
       if (graphObjects == null || graphObjects.size() == 0)
           return;
       
       for (IETGraphObject graphObj: graphObjects)
       {
           graphObj.setSelected(true);
       }
       
       getGraphWindow().drawGraph();
       fireSelectEvent(graphObjects);
       refresh(true);
   }
   
   
   /**
    * Returns a list of the selected items (nodes and edges).
    */
   public  ETList<IETGraphObject> getSelectedNodesAndEdges()
   {
      ETGraph etGraph = getGraph() instanceof ETGraph ? (ETGraph) getGraph() : null;
      if (etGraph != null)
      {
         ETList < IETGraphObject > selected = new ETArrayList < IETGraphObject > ();
         List selectedNodes = etGraph.selectedNodes();
         if (selectedNodes != null)
            selected.addAll(selectedNodes);
         
         List selectedEdges = etGraph.selectedEdges();
         if (selectedEdges != null)
            selected.addAll(selectedEdges);
         
         return selected.size() > 0 ? selected : null;
      }
      return null;
   }

   /**
    * Returns a list of the selected items (nodes and edges).
    */
   public  ETList<IETGraphObject> getSelectedNodes()
   {
      ETGraph etGraph = getGraph() instanceof ETGraph ? (ETGraph) getGraph() : null;

      if (etGraph != null)
      {
         ETList<IETGraphObject> selected = new ETArrayList<IETGraphObject>();
         List selectedNodes = etGraph.selectedNodes();

         if (selectedNodes != null)
            selected.addAll(selectedNodes);
         
         return selected.size() > 0 ? selected : null;
      }

      return null;
   }

   
   public ETList<IPresentationElement> getSelectedNodes2()
   {
      ETGraph etGraph = getGraph() instanceof ETGraph 
          ? (ETGraph) getGraph() 
          : null;

      if (etGraph != null)
      {
         ETList<IPresentationElement> selected = 
             new ETArrayList<IPresentationElement>();
         
         List selectedNodes = etGraph.selectedNodes();

         if (selectedNodes != null)
            selected.addAll(selectedNodes);
         
         return selected.size() > 0 ? selected : null;
      }

      return null;
   }
   
   /**
    * Returns a list of the selected items (nodes and edges).
    */
   public ETList<IETGraphObject> getSelected2()
   {
      return getSelectedNodesAndEdges();
   }
   
   /**
    * Returns a list of the selected items (nodes, edges and nodelabels and edgeLabels).
    */
   public ETList<IETGraphObject> getSelected3()
   {
      ETGraph etGraph = getGraph() instanceof ETGraph ? (ETGraph) getGraph() : null;
      
      if (etGraph != null)
      {
         // Get the selected nodes and edges.
         ETList < IETGraphObject > selected = getSelected2();
         selected = selected != null ? selected : new ETArrayList < IETGraphObject > ();
         
         // Now tack on the labels.
         List selectedNodeLabels = etGraph.selectedNodeLabels();
         if (selectedNodeLabels != null)
            selected.addAll(selectedNodeLabels);
         
         List selectedEdgeLabels = etGraph.selectedEdgeLabels();
         if (selectedEdgeLabels != null)
            selected.addAll(selectedEdgeLabels);
         
         return selected.size() > 0 ? selected : null;
      }
      return null;
   }
   
   public ETList<IElement> getSelectedElements()
   {
      ETList < IPresentationElement > selected = getSelected();
      if (selected != null)
      {
         ETList < IElement > elements = new ETArrayList < IElement > ();
         Iterator < IPresentationElement > iter = selected.iterator();
         while (iter.hasNext())
         {
            elements.addAll(iter.next().getSubjects());
         }
         return elements.size() > 0 ? elements : null;
      }
      return null;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getSelected4()
     */
   public ETList<IElement> getSelected4()
   {
      return getSelectedElements();
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getSelectedByType(java.lang.String)
     */
   public ETList<IPresentationElement> getSelectedByType(String sType)
   {
      // TODO Auto-generated method stub
      return null;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getSelectedLabels()
     */
   public ETList<IETLabel> getSelectedLabels()
   {
      ETGraph etGraph = getGraph() instanceof ETGraph ? (ETGraph) getGraph() : null;
      
      if (etGraph != null)
      {
         // Get the selected nodes and edges.
         ETList < IETLabel > selected = new ETArrayList < IETLabel > ();
         // Now tack on the labels.
         List selectedNodeLabels = etGraph.selectedNodeLabels();
         if (selectedNodeLabels != null)
            selected.addAll(selectedNodeLabels);
         
         List selectedEdgeLabels = etGraph.selectedEdgeLabels();
         if (selectedEdgeLabels != null)
            selected.addAll(selectedEdgeLabels);
         
         return selected.size() > 0 ? selected : null;
      }
      return null;
      
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getShowGrid()
     */
   public boolean getShowGrid()
   {
      // TODO Auto-generated method stub
      return false;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getTrackBar()
     */
   public JTrackBar getTrackBar()
   {
      return m_TrackBar;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getWindowHandle()
     */
   public int getWindowHandle()
   {
      // TODO Auto-generated method stub
      return 0;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#goToChildGraph(com.tomsawyer.graph.TSNode)
     */
   public void goToChildGraph(TSNode pNode)
   {
      // TODO Auto-generated method stub
      
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#goToParentGraph()
     */
   public void goToParentGraph()
   {
      // TODO Auto-generated method stub
      
   }
   
   /**
    * This method creates the drawing preferences dialog.
    */
   public void createGraphPreferencesDialog()
   {
      String title = RESOURCE_BUNDLE.getString("IDS_GRAPH_PROPERTIES_DLG_TITLE");
      
      this.graphPreferencesDialog =
      new ETDrawingPreferencesDialog(this.getOwnerFrame(),
      title,
      //this.getGraphWindow().getPreferences());
      this.getGraphWindow());
      
      this.graphPreferencesDialog.setLocationRelativeTo(
      SwingUtilities.getRootPane(this));
      
      this.graphPreferencesDialog.addWindowListener(
      this.windowHandler);
   }
   
   
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#graphPreferencesDialog(boolean)
     */
   public void graphPreferencesDialog(boolean bShow)
   {
      if (bShow && this.graphPreferencesDialog == null)
      {
         this.createGraphPreferencesDialog();
      }
      
      if (graphPreferencesDialog != null)
      {
         graphPreferencesDialog.setVisible(bShow);
      }
      this.repaint();
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#handleDeepSyncBroadcast(com.embarcadero.describe.foundation.IElement[], boolean)
     */
   public void handleDeepSyncBroadcast(IElement[] pElements, boolean bSizeToContents)
   {
      // TODO Auto-generated method stub
      
   }
   
    /*
     *	Returns true if it was handled.
     */
   public boolean onDeleteKeyDown(boolean bAskUserAboutDelete)
   {
      // Make sure we are not in move selected mode, or the app will crash becuase the selection
      // Lists should not change will in this mode.
      if (!(getGraphWindow().getCurrentTool() instanceof ADMoveSelectedState))
      {
         ETElementManager manager = new ETElementManager(this);
         manager.onPreDeleteGatherSelected();
         
         if(getDiagramEngine() != null)
         {
            IDiagramEngine engine = getDiagramEngine();
            if(engine.preHandleDeleteKey() == false)
            {
               // Guys please see public ETList < TSGraphObject > ETGraph.getSelectedObjects()
               // This is a mess.
               ETArrayList < TSENode > selectedNodes = new ETArrayList();
               ETArrayList < TSEEdge > selectedEdges = new ETArrayList();
               ETArrayList < TSENodeLabel > selectedNodeLabels = new ETArrayList();
               ETArrayList < TSEEdgeLabel > selectedEdgeLabels = new ETArrayList();
               gatherSelectedItems(selectedNodes, selectedEdges,
               selectedNodeLabels, selectedEdgeLabels);
               
               //verify that something is selected
               if (selectedNodes.size() > 0 || selectedEdges.size() > 0 ||
               selectedNodeLabels.size() > 0 || selectedEdgeLabels.size() > 0)
               {
                  // TODO:  In C++ this was a stored registry value
                  boolean bDefaultDeleteModelElements = true;
                  
                  ETTripleT<Integer, Boolean, Boolean> deleteWithAlsoResult = null;
                  DataVerificationResults results = null;
                  
                  // Determine if there are any combined fragments in the selected nodes
                  if( containsModelElement( selectedNodes, ICombinedFragment.class ))
                  {
                     // Ask the user if he wants to delete the objects, and
                     // possibly also the message associated with the message connectors
                     IPreferenceQuestionDialog cpQuestionDialog = new SwingPreferenceQuestionDialog();
                     if ( cpQuestionDialog != null )
                     {
                        String strKey = "Default";
                        String strPath = "Diagrams|SequenceDiagram";
                        String strName = "UML_ShowMe_Delete_Combined_Fragment_Messages";
                        String strAlsoQuestion = RESOURCE_BUNDLE.getString( "IDS_ALSO_DELETE_CF_MESSAGES" );
                        deleteWithAlsoResult = cpQuestionDialog.displayDeleteWithAlso(
                        strKey,
                        strPath,
                        strName,
                        bDefaultDeleteModelElements,
                        strAlsoQuestion );
                        
                        Boolean bDeleteMessagesAlso = (Boolean)deleteWithAlsoResult.getParamThree();
                        if( bDeleteMessagesAlso.booleanValue() )
                        {
                           selectCombinedFragmentMessages( selectedNodes );
                        }
                     }
                  }
                  //	Determine if there are any message connectors in the selected edges
                  else if(containsModelElement( selectedEdges, IMessageConnector.class))
                  {
                     // Ask the user if he wants to delete the objects, and
                     // possibly also the message associated with the message connectors
                     IPreferenceQuestionDialog cpQuestionDialog = new SwingPreferenceQuestionDialog();
                     if ( cpQuestionDialog != null )
                     {
                        String bsKey = "Default";
                        String bsPath = "Diagrams|CollaborationDiagram";
                        String bsName = "UML_ShowMe_Delete_Connector_Messages";
                        String bsAlsoQuestion = RESOURCE_BUNDLE.getString("IDS_ALSO_DELETE_MESSAGES");
                        deleteWithAlsoResult = cpQuestionDialog.displayDeleteWithAlso(
                        bsKey,
                        bsPath,
                        bsName,
                        bDefaultDeleteModelElements,
                        bsAlsoQuestion);
                        Boolean bDeleteMessagesAlso = (Boolean)deleteWithAlsoResult.getParamThree();
                        if(bDeleteMessagesAlso.booleanValue())
                        {
                           selectMessageConnectorMessages(selectedEdges, selectedEdgeLabels);
                        }
                        
                        boolean canceled = true;
                        Integer val = (Integer)deleteWithAlsoResult.getParamOne();
                        if (val.intValue() > 0)
                        {
                           canceled = true;
                        }
                        else
                        {
                           canceled = false;
                        }
                        Boolean affectModel = (Boolean)deleteWithAlsoResult.getParamTwo();
                        results = new DataVerificationResults(canceled, affectModel.booleanValue());
                     }
                  }
                  else
                  {
                     results = engine.verifyDataDeletion(selectedNodes,
                     selectedEdges,
                     selectedNodeLabels,
                     selectedEdgeLabels);
                  }
                  
                  if( deleteWithAlsoResult != null )
                  {
                     Integer val = (Integer)deleteWithAlsoResult.getParamOne();
                     boolean canceled = (val.intValue() > 0);
                     Boolean affectModel = (Boolean)deleteWithAlsoResult.getParamTwo();
                     results = new DataVerificationResults(canceled, affectModel.booleanValue());
                  }
                  
                  if(results.isCancelAction() == false)
                  {
                     manager.onPreDelete();
                     deleteTheseItems(selectedNodes,
                     selectedEdges,
                     selectedNodeLabels,
                     selectedEdgeLabels,
                     results.isAffectModelElement());
                     
                     // Tell the world that the select list has changed.
                     postSelectEvent();
                     //Also clear  m_lastSelectedNode & lastSelectedlabel
                     m_lastSelectedNode = null;
                     m_lastSelectedLabel = null;
                  }
                  else
                  {
                     manager.onDeleteCancelled();
                  }
               }
            }
         }
         this.refresh(true);
         return true;
      }
      return false;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#handleKeyDown(int, int, boolean)
     */
   public boolean handleKeyDown(int nKeyCode, int nShift, boolean bAskUserAboutDelete)
   {
      boolean handled = false;
      if (!handled)
      {
         ETElementManager manager = new ETElementManager(this);
         handled = manager.onKeyDown(nKeyCode, nShift);
      }
      
      if (!handled && !getReadOnly())
      {
         // If no one handled it, then see if its a VK_DELETE, in that case
         // we need to handle the delete ourselves.  With TS 5.1 we need to move this,
         // in 5.0 TS has a bug where the predelete is fired AFTER all edges have been
         // removed!
         if (nKeyCode == KeyEvent.VK_DELETE)
         {
            handled = onDeleteKeyDown(bAskUserAboutDelete);
         }
         
         //Jyothi: cycle through nodes when tab/shift-tab is pressed
         if (nKeyCode == KeyEvent.VK_TAB) {
//             System.err.println(" tab key pressed !");
             handled = onTabKeyDown(nShift);
         }
         
         //cycle through edges when shift up/down is pressed
         if ((nShift == 0) && ((nKeyCode == KeyEvent.VK_UP) || (nKeyCode == KeyEvent.VK_DOWN))) {
//             System.err.println(" shift up/down pressed! ");
             handled = onShiftUpDown(nKeyCode, nShift);
         }
         
         //cycle through the lables when shift pgup/pgdown is pressed
         if ((nShift == 0) && ((nKeyCode == KeyEvent.VK_PAGE_UP) || (nKeyCode == KeyEvent.VK_PAGE_DOWN))) {
             handled = onShiftPageUpDown(nKeyCode, nShift);
         }
         
      }
      
      return handled;
   }
   

   public boolean handleCharTyped(char ch) {
       boolean handled = false;
       ETElementManager manager = new ETElementManager(this);
       handled = manager.onCharTyped(ch);
       return handled;
   }


   private boolean isArrowKey(int keyCode)
   {
       return (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN ||
           keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_LEFT);
   }
   // cycle thru all labels in the diagram
   private boolean onShiftPageUpDown(int nKeyCode, int shiftDown) {
       IETLabel nextLabel = null;
       int nextIndex = 0;
       
       ETList<IETLabel> labelsList = getAllLabels();
       if (labelsList == null || labelsList.size() <= 0) {
           return false;
       }
       //Take care of the case where labels are selected by mouse and m_lastSelectedLabel is not initialized.
       if (m_lastSelectedLabel == null)
           m_lastSelectedLabel = getLastSelectedLabel();
       
       //getnext label in the labelsList and select it..
       if (labelsList != null && labelsList.size() > 0) {
           if (labelsList.contains(m_lastSelectedLabel)) {
               if (nKeyCode == KeyEvent.VK_PAGE_UP) {
                   nextIndex = (labelsList.indexOf(m_lastSelectedLabel))+1;
                   if (nextIndex >= labelsList.size())
                       nextIndex = 0;
               } else if (nKeyCode == KeyEvent.VK_PAGE_DOWN) {
                   nextIndex = (labelsList.indexOf(m_lastSelectedLabel))-1;
                   if (nextIndex < 0)
                       nextIndex = labelsList.size()-1;
               }
               nextLabel = (IETLabel)labelsList.get(nextIndex);
               selectLabel(nextLabel);
           } else if (m_lastSelectedLabel != null) {
               selectLabel(m_lastSelectedLabel);
           } else {
               // since m_lastSelectedLabel is null, we need a default value
               m_lastSelectedLabel = (labelsList.get(0));
               //if there is no selected element on the diagram, then have a default one selected
               if (getSelectedLabels() == null) {
                   selectLabel(m_lastSelectedLabel);
               }
           }
       }
       return true;
   }
   
   private void selectLabel(IETLabel nextLabel) {
       getGraphWindow().deselectAll(true);
       setSelectedCompartments(false);
       if (nextLabel != null) {
           m_graphObj = nextLabel;
           if (m_graphObj instanceof ETNodeLabel) {
               getGraphWindow().selectObject((ETNodeLabel)nextLabel, true);
               this.fireSelectEvent((ETNodeLabel)nextLabel);
               m_lastSelectedLabel = nextLabel;
           } else if (m_graphObj instanceof ETEdgeLabel) {
               getGraphWindow().selectObject((ETEdgeLabel)nextLabel, true);
               this.fireSelectEvent((ETEdgeLabel)nextLabel);
               m_lastSelectedLabel = nextLabel;
           }
       }
   }

   private boolean onShiftUpDown(int nKeyCode, int shiftDown) {
       //first get the selected node
       ETEdge nextEdge = null;
       int nextIndex = 0;
       ETNode selectedNode = getLastSelectedNode();
       ETEdge selectedEdge = getSelectedEdge();
       
       if ((selectedNode == null)&&(selectedEdge != null)) {
           selectedNode = m_lastSelectedNode;
       }
       if (selectedNode != null) {
//           System.err.println("selectedNode is NOT null... so get its edges.. ");
           //get all the edges of this node..
           ETList <IETEdge> edgeList = selectedNode.getEdges();
           if (edgeList != null && edgeList.size() > 0) {
               if (selectedEdge != null) {
                   if (edgeList.contains(selectedEdge)) {
                       if (nKeyCode == KeyEvent.VK_UP) {
                           nextIndex = (edgeList.indexOf(selectedEdge))+1;
                           if (nextIndex >= edgeList.size())
                               nextIndex = 0;
                       } else if (nKeyCode == KeyEvent.VK_DOWN) {
                           nextIndex = (edgeList.indexOf(selectedEdge))-1;
                           if (nextIndex < 0)
                               nextIndex = edgeList.size()-1;
                       }
                       nextEdge = (ETEdge)edgeList.get(nextIndex);
                   }
               } else { //selectedEdge is null
                   nextEdge = (ETEdge)edgeList.get(0);
               }
               getGraphWindow().deselectAll(true);
               setSelectedCompartments(false);
               // Select objects in the selected group, m_selectedNodesGroup. 
               // These objects are added to the group via shift+/-
               getGraphWindow().selectGroup(m_selectedNodesGroup, true);
               if (nextEdge != null) {
                   m_graphObj = nextEdge;
                   getGraphWindow().selectObject(nextEdge, true);
                   this.fireSelectEvent(nextEdge);
               }
           }
       }
       return true;
   }

   private boolean onTabKeyDown(int shiftDown )
   {
       //begin traversal..
       ETNode nextNode = null;
       int nextIndex = 0;
       
       TSEGraph graph = this.getGraph();
       List nodeList = graph.nodes();
       if (nodeList == null || nodeList.size() <= 0)
       {
           return false;
       }
       
       if (graph.hasSelectedNodes()) {
           
           //Take care of the case where nodes are selected by mouse and m_lastSelectedNode is not initialized.
           if (m_lastSelectedNode == null)
               m_lastSelectedNode = getLastSelectedNode();
           
           //get next node in the nodeList and select it..
           if (m_lastSelectedNode != null && nodeList.contains(m_lastSelectedNode))
           {
               if (shiftDown == 1)
               { // shift is NOT down.. so tab forward..
                   nextIndex = (nodeList.indexOf(m_lastSelectedNode))+1;
                   if (nextIndex >= nodeList.size())
                       nextIndex = 0;
               }
               else if (shiftDown == 0)
               { //shift is down.. so tab backwards..
                   nextIndex = (nodeList.indexOf(m_lastSelectedNode))-1;
                   if (nextIndex < 0)
                   {
                       nextIndex = nodeList.size()-1;
                   }
               }
               nextNode = (ETNode)nodeList.get(nextIndex);
               // 1. deselect all nodes and and compartments if any
               getGraphWindow().deselectAll(true);
               setSelectedCompartments(false);
               // 2. select objects in the selected group, m_selectedNodesGroup. 
               // These objects are added to the group via shift+/-
               getGraphWindow().selectGroup(m_selectedNodesGroup, true);
               // 3. select the next object (must be done after calling selectGroup() in step2
               // to maintain the correct order of selection)
               getGraphWindow().selectObject(nextNode, true);
               // 4. fire selecteEvent for the nextNode
               this.fireSelectEvent(nextNode);
               //Fix for #88831
               IETGraphObjectUI nodeUI = nextNode.getETUI();
               if (nodeUI != null && !nodeUI.isOnTheScreen(nodeUI.getDrawInfo().getTSEGraphics())) { //node is NOT visible on the screen, so center it.
                   IPresentationElement presElt = TypeConversions.getPresentationElement((TSObject)nextNode);
                   centerPresentationElement(presElt, true, false); 
               }
               m_lastSelectedNode = nextNode;               
               }
       
       }
       else if (m_lastSelectedNode != null) 
       {
           getGraphWindow().deselectAll(true);
           setSelectedCompartments(false);
           getGraphWindow().selectObject(m_lastSelectedNode, true);
           this.fireSelectEvent(m_lastSelectedNode);
       }
       else
       {
           // since selectedNode is null, we need a default value
           m_lastSelectedNode = (ETNode)(nodeList.get(0));
           //if there is no selected element on the diagram, then have a default one selected
           if (getSelectedEdge() == null)
           {
               getGraphWindow().deselectAll(true);
               setSelectedCompartments(false);
               getGraphWindow().selectObject(m_lastSelectedNode, true);
               this.fireSelectEvent(m_lastSelectedNode);
           }
       }
       m_graphObj = m_lastSelectedNode;
       return true;
   }
   
   //we just need a single selected node
   private ETNode getLastSelectedNode() {
      ETNode lastSelectedNode = null;
      if (getGraph().hasSelectedNodes()) {
        List selectedNodeList = this.getGraph().selectedNodes();
        if (selectedNodeList != null) {
            int count = selectedNodeList.size();
            if (count > 0) {
                lastSelectedNode = (ETNode)selectedNodeList.get(count-1);
            }
        }
      }
      return lastSelectedNode;
   }
   
   private ETEdge getSelectedEdge() {
       ETEdge selectedEdge = null;
       if (getGraph().hasSelectedEdges()) {
           List selectedEdgeList = getGraph().selectedEdges();
           if (selectedEdgeList != null && selectedEdgeList.size() > 0) {
               selectedEdge = (ETEdge)selectedEdgeList.get(0);
//               System.err.println(" selected edge = "+selectedEdge);
           }
       }
       return selectedEdge;
   }
   
   private IETLabel getLastSelectedLabel() {
       
       IETLabel lastSelectedLabel = null;
       ETList <IETLabel> labelList = getAllLabels();
       if (labelList != null && labelList.size() > 0) {
           lastSelectedLabel = labelList.get(labelList.size() - 1);
       }
       return lastSelectedLabel;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#hasChildren(com.embarcadero.describe.foundation.IPresentationElement, boolean)
     */
   public boolean hasChildren(IPresentationElement pPE, boolean bHidden)
   {
      // TODO Auto-generated method stub
      return false;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#hasParents(com.embarcadero.describe.foundation.IPresentationElement, boolean)
     */
   public boolean hasParents(IPresentationElement pPE, boolean bHidden)
   {
      // TODO Auto-generated method stub
      return false;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#hide(com.embarcadero.describe.foundation.IPresentationElement, int, boolean)
     */
   public void hide(IPresentationElement pPE, int numLevels, boolean bChildren)
   {
      GetHelper.hide(this.getGraphWindow(), pPE,numLevels,bChildren);
      setIsDirty(true);
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#unhide(com.embarcadero.describe.foundation.IPresentationElement, int, boolean)
     */
   public void unhide(IPresentationElement pPE, int numLevels, boolean bChildren)
   {
      GetHelper.unhide(this.getGraphWindow(), pPE,numLevels,bChildren);
      setIsDirty(true);
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#getLayoutStyle()
     */
   public int getLayoutStyle()
   {
       return ETLayoutStyleMap.getLayoutStyle(m_DiagramKindDisplayName, getGraph(), m_layoutInputTailor );
   }
   
    /*
     * Converts an ILayoutKind into a string for communication with the layout server.
   
   public String getLayoutCommandString(int ilayoutKind)
   {
      return ETLayoutStyleMap.getLayoutCommandString(ilayoutKind, getGraph(), this.m_layoutInputTailor);
   }
    */ 
   /**
    * Immediately puts the layout style on the GET, doesn't do a post.  Here's the email I sent
    * to TS support on 5-28-02 documenting the problem (PSIRR).
    *
    * I've noticed something that is worries me.  Certain commands to TS will crash the GET
    * (ie changing topology while in an event).  I now have a case where running the windows
    * message pump produces different layouts.  I have a menu button that creates a diagram then
    * looks in a tree to see what's selected.  Based on what's selected nodes/edges are created.
    * After creating the diagram and the node/edges it then puts the GET into orthogonal layout
    * style.
    *
    * Watch the AVI first.  If I set the layout style immediately after creating the diagram and
    * edges I get what you see in the AVI - disconnected edges pointing off into outer space.
    * If I instead post the layout command onto the windows messages stack and then execute the
    * command after allowing the message queue to finish I get the picture below.  The only
    * difference is delaying execution of the layout command by using the windows message pump -
    * that's all it takes to create the layout seen below and the layout at the end of the avi.
    *
    * @param nLayoutStyle [in] The new layout style.
    * @param bSilent [in] true to skip asking the user if it's ok
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#immediatelySetLayoutStyle(int, boolean)
    */
   public void immediatelySetLayoutStyle(int nLayoutStyle, boolean bSilent)
   {
       if (getGraphWindow() != null && !IOSemaphore.isIOHappenning())
       {
           // First ask a question since this operation cannot be undone
           if (bSilent || askOkToLayoutDiagram())
           {
               // TS fits the diagram to the window after the layout.  We preserve the zoom level here
               double curZoom = getCurrentZoom();
               
               // Let folks know that the layout is being changed.
               if (fireDrawingAreaPrePropertyChange(DiagramAreaEnumerations.DAPK_LAYOUT))
               {
                   boolean handled = false;
                   if (m_DiagramEngine != null)
                   {
                       handled = m_DiagramEngine.preDoLayout(nLayoutStyle);
                   }
                   
                   if (!handled)
                   {
                       boolean incremental = (nLayoutStyle == ILayoutKind.LK_INCREMENTAL_LAYOUT);
                       
                       int layoutStyle = nLayoutStyle;
                       
                       // Performing the layout with commands.
                       boolean hasNodes = false;
                       if (this.hasNodes(hasNodes) > 0)
                       {
                           m_allOptionsServiceInputData.addAsListener(this.getGraphManager());
                           int tsLayoutStyle = ETLayoutStyleMap.mapLayoutKind2TsLayout(nLayoutStyle);
                           
                           //JM: fix for Bug#6383449
//                           if ((tsLayoutStyle == TSLayoutConstants.LAYOUT_STYLE_NO_STYLE) && (incremental == true))
                           if (incremental)
                           {
                               m_layoutInputTailor.setIncrementalLayout(true);
                               getGraphWindow().transmit(new ADLayoutCommand(getGraphWindow(), this.m_layoutProxy, this.m_allOptionsServiceInputData));
                           }
                           else
                           {
                               // 83132, set direction according to uml convention
                               if (tsLayoutStyle == TSLayoutConstants.LAYOUT_STYLE_HIERARCHICAL)
                               {
                                   TSHierarchicalLayoutInputTailor tailor = new TSHierarchicalLayoutInputTailor(m_allOptionsServiceInputData, this.getGraph());
                                   tailor.setLevelDirection(TSJLayoutConstants.DIRECTION_BOTTOM_TO_TOP);
                               }
                               getGraphWindow().transmit(new ADLayoutCommand(getGraphWindow(), this.m_layoutProxy, this.m_allOptionsServiceInputData, tsLayoutStyle));
                           }
                       }
                   }
                   
                   if (m_DiagramEngine != null)
                   {
                       m_DiagramEngine.postDoLayout();
                   }
                   
                   // Let folks know that the layout is being changed.
                   fireDrawingAreaPropertyChange(DiagramAreaEnumerations.DAPK_LAYOUT);
               }
               zoom(curZoom);
           }
       }
   }
   
   /**
    * Ask if it's ok to layout the diagram
    *
    * @return true if it's ok to layout the diagram
    */
   private boolean askOkToLayoutDiagram()
   {
      int resultKind = SimpleQuestionDialogResultKind.SQDRK_RESULT_NO;
      
      // get preference for displaying a small amount of empty lists
      //kris richards - "AskBeforeLayout" pref expuged. Set to true.
      Preferences prefs = NbPreferences.forModule (DummyCorePreference.class) ;
      
      if (prefs.getBoolean ("UML_Ask_Before_Layout", true))
      {
         // If we don't have any graph objects then don't bother asking
         boolean hasObjs = GetHelper.hasGraphObjects(m_GraphWindow);
         if (hasObjs)
         {
            // Determine if the user wants to layout the diagram
            IQuestionDialog dialog = new SwingQuestionDialogImpl();
            
            String title = RESOURCE_BUNDLE.getString("IDS_LAYOUTQUESTION_TITLE");
            String message = RESOURCE_BUNDLE.getString("IDS_CHANGELAYOUT");
            String checkbox = RESOURCE_BUNDLE.getString("IDS_DONTASKAGAIN");
            QuestionResponse result = dialog.displaySimpleQuestionDialogWithCheckbox(MessageDialogKindEnum.SQDK_YESNO,
            MessageIconKindEnum.EDIK_ICONWARNING,
            message,
            checkbox,
            title,
            MessageResultKindEnum.SQDRK_RESULT_YES,
            false);
            
            if(result.isChecked() == true)
            {
               prefs.putBoolean ("UML_Ask_Before_Layout", false );
            }
            resultKind = result.getResult();
         }
      }
      else
      {
         resultKind = SimpleQuestionDialogResultKind.SQDRK_RESULT_YES;
      }
      
      return resultKind == SimpleQuestionDialogResultKind.SQDRK_RESULT_YES;
   }
   
   protected void setPreferenceValue(String sLocID, String sID, String sValue)
   {
      IPreferenceManager2 mgr = ProductHelper.getPreferenceManager();
      if(mgr != null)
      {
         String sPath = sLocID != null && sLocID.length() > 0  ? sLocID : "";
         String sName = sID != null && sID.length() > 0 ? sID : "";
         
         // Change our cache if the pref hive is Diagrams
         if (sLocID != null && sLocID.equals("Diagrams"))
         {
            m_CachedPreferences.put(sID, sValue);
         }
         
         mgr.setPreferenceValue(sPath, sName, sValue);
      }
   }
   
   /**
    * Initializes a newly created diagram.  This also adds it to the current
    * namespace.
    *
    * @param pNamespace [in] The namespace for this diagram
    * @param sName [in] The name of the diagram
    * @param nKind [in] The kind of the diagram.
    */
   public void initializeNewDiagram(INamespace pNamespace, String sName, int pKind)
   {
      ETSmartWaitCursor waitCursor = new ETSmartWaitCursor();
      setDiagramKind(pKind);
      setNamespace(pNamespace);
      String fulFileName = getFullFileName(sName);
      setName(sName);
      
      if (fulFileName != null && fulFileName.length() > 0)
      {
         setFileName(fulFileName, true);
//         setIsDirty(true);
      }
      
      initializeNewDiagram();
      waitCursor.stop();
   }
   
   /**
    * Initializes a new diagram.  Called by the other InitializeNewDiagram.  Use this when
    * you've already set the namespace, name and type of the diagram
    */
   private void initializeNewDiagram()
   {
      // set up an empty resource manager for this drawing
      loadResourceManager(null);
      
      // Fire the precreate trigger
      if (getDrawingAreaDispatcher() != null)
      {
         boolean proceed;
         IDiagram pDiagram = getDiagram();
         
         if (m_drawingAreaDispatcher != null)
         {
            IEventPayload payload = m_drawingAreaDispatcher.createPayload("FireDrawingAreaPreCreated");
            proceed = m_drawingAreaDispatcher.fireDrawingAreaPreCreated(this, payload);
         }
         else
            proceed = true;
         
         if (proceed)
         {
            IEventPayload payload = m_drawingAreaDispatcher.createPayload("FireDrawingAreaPostCreated");
            m_drawingAreaDispatcher.fireDrawingAreaPostCreated(this, payload);
            
            if (m_DiagramEngine != null)
            {
               m_DiagramEngine.initializeNewDiagram();
               
               // Fix J1714:  This code was not needed in C++,
               // but we implement it here to ensure that there is
               // a default layout style for the diagram.
               
               // Set the default layout style without setting the diagram dirty
               /* JM
               getGraph().setLayoutStyle( TSDGraph.ORTHOGONAL );
               */
               // Setup our default layout settings
               m_DiagramEngine.setupLayoutSettings(true);
            }
            
            // Tell the outside world that this drawing area is open for business
            payload = m_drawingAreaDispatcher.createPayload("DiagramOpened");
            m_drawingAreaDispatcher.fireDrawingAreaOpened(pDiagram, payload);
         }
         zoom(1.0);
      }
   }
   
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#invertSelection()
     */
   public void invertSelection()
   {
      
      ETGraph etGraph = getGraph() instanceof ETGraph ? (ETGraph) getGraph() : null;
      if (etGraph != null)
      {
         visit(new IETGraphObjectVisitor()
         {
            public boolean visit(IETGraphObject object)
            {
               // Toggle the selection.
               object.invalidate();
               object.setSelected(!object.isSelected());
               return true;
            }
         });
         
         this.fireSelectEvent(etGraph.getSelectedObjects(false, false));
         this.refresh(true);
      }
   }
   
   /**
    * Is this model element type allowed on this diagram?
    *
    * @param pElement [in] The element to query
    * @param bIsAllowed [out,retval] true if the element is allowed on this diagram type.
    */
   public boolean isAllowedOnDiagram(IElement pElement)
   {
      boolean isAllowed = false;
      if (pElement != null && m_PresentationTypesMgr != null && m_PresentationTypesMgr.validateFile())
      {
         // Get the element type
         String elemType = pElement.getElementType();
         
         // Get the type of this diagram
         int diaKind = getDiagramKind();
         
         String viewDesc = m_PresentationTypesMgr.getMetaTypeInitString(elemType, diaKind);
         if (viewDesc != null && viewDesc.length() > 0)
         {
            isAllowed = true;
         }
      }
      return isAllowed;
   }
   
   /**
    * Is this an associated diagram?
    *
    * @param sDiagramXMIID [in] The diagram xmiid
    * @param bIsAssociated [out,retval] TRUE if the diagram is associated with this diagram.
    */
   public boolean isAssociatedDiagram(String sDiagramXMIID)
   {
      boolean isAssociated = false;
      if (sDiagramXMIID != null && sDiagramXMIID.length() > 0)
      {
         if (m_AssociatedDiagrams != null)
         {
            int count = m_AssociatedDiagrams.size();
            for (int i=0; i<count; i++)
            {
               String str = (String)m_AssociatedDiagrams.get(i);
               if (sDiagramXMIID.equals(str))
               {
                  isAssociated = true;
                  break;
               }
            }
         }
      }
      return isAssociated;
   }
   
   /**
    * Is this an associated diagram?
    *
    * @param pDiagram [in] The diagram
    * @param bIsAssociated [out,retval] TRUE if the diagram is associated with this diagram.
    */
   public boolean isAssociatedDiagram2(IProxyDiagram pDiagram)
   {
      if (pDiagram != null)
      {
         String xmiid = pDiagram.getXMIID();
         if (xmiid != null && xmiid.length() > 0)
         {
            return isAssociatedDiagram(xmiid);
         }
      }
      return false;
   }
   
   /**
    * Is this an associated element?
    *
    * @param sModelElementXMIID [in] The model element id
    * @param bIsAssociated [out,retval] TRUE if the model element is associated with this diagram.
    */
   public boolean isAssociatedElement(String sModelElementXMIID)
   {
      if (sModelElementXMIID != null && sModelElementXMIID.length() > 0)
      {
         if (m_AssociatedElements != null)
         {
            Collection col = m_AssociatedElements.values();
            Iterator iter = col.iterator();
            while (iter.hasNext())
            {
               Object obj = iter.next();
               if (obj != null && obj instanceof Vector)
               {
                  Vector meidCol = (Vector)obj;
                  if (meidCol.contains(sModelElementXMIID))
                  {
                     return true;
                  }
               }
            }
         }
      }
      return false;
   }
   
   /**
    * Is this an associated element?
    *
    * @param pElement [in] The model element
    * @param bIsAssociated [out,retval] TRUE if the model element is associated with this diagram.
    */
   public boolean isAssociatedElement2(IElement pElement)
   {
      boolean isAssociated = false;
      if (pElement != null)
      {
         String xmiid = pElement.getXMIID();
         if (xmiid != null && xmiid.length() > 0)
         {
            return isAssociatedElement(xmiid);
         }
      }
      return false;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#isSame(com.embarcadero.describe.diagrams.IDiagram)
     */
   public boolean isSame(IDiagram pDiagram)
   {
      if (pDiagram != null)
      {
         String filename = getFilename();
         return filename != null && filename.equals(pDiagram.getFilename());
      }
      return false;
   }
   
   public boolean isStackingCommandAllowed(int pStackingCommand)
   {
      return true;
   }
   
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#killTooltip()
     */
   public void killTooltip()
   {
      // TODO Auto-generated method stub
      
   }
   
   protected void createLayoutPropertiesDialog()
   {
        /*
        Object tag = this.getGraphWindow().getGraph().getTag();
        String name;
         
        if (tag != null && !"".equals(tag.toString().trim()))
        {
            name = tag.toString();
        }
        else
        {
            name = this.getFilename();
        }
         */
      String title = RESOURCE_BUNDLE.getString("IDS_LAYOUT_PROPERTIES_DLG_TITLE");
      
      this.layoutPropertiesDialog =
      new ETLayoutPropertiesDialog(getOwnerFrame(),
      title,
      this.getGraphWindow(),
      this.m_layoutProxy,
      this.m_allOptionsServiceInputData); // this should probably be changed to TSEServiceInputData...
      
      this.layoutPropertiesDialog.setLocationRelativeTo(
      SwingUtilities.getRootPane(this));
      
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#layoutPropertiesDialog(boolean)
     */
   public void layoutPropertiesDialog(boolean bShow)
   {
      if (bShow)
      {
         if (this.layoutPropertiesDialog == null)
         {
            this.createLayoutPropertiesDialog();
         }
      }
      
      if (layoutPropertiesDialog != null)
      {
         layoutPropertiesDialog.setVisible(bShow);
         layoutPropertiesDialog.repaint();
      }
      this.refresh(true);
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#logicalToDevicePoint(com.embarcadero.describe.umlsupport.IETPoint)
     */
   public IETPoint logicalToDevicePoint(IETPoint pLogical)
   {
      IETPoint retVal = null;
      
      if(getGraphWindow() != null)
      {
         TSTransform transform = getGraphWindow().getTransform();
         if(transform != null)
         {
            Point p = transform.pointToDevice(pLogical.getX(), pLogical.getY());
            
            retVal = new ETPoint(p.x,p.y);
         }
      }
      
      return retVal;
   }
   
   /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#logicalToDevicePoint(com.embarcadero.describe.umlsupport.IETPoint)
     */
   public IETPoint logicalToDevicePoint(double x, double y)
   {
      IETPoint retVal = null;
      
      if(getGraphWindow() != null)
      {
         TSTransform transform = getGraphWindow().getTransform();
         if(transform != null)
         {
            Point p = transform.pointToDevice(x, y);
            
            retVal = new ETPoint(p.x,p.y);
         }
      }
      
      return retVal;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#logicalToDeviceRect(com.embarcadero.describe.umlsupport.IETRect)
     */
   public IETRect logicalToDeviceRect(IETRect pLogical)
   {
      return logicalToDeviceRect(pLogical.getLeft(),
      pLogical.getTop(),
      pLogical.getRight(),
      pLogical.getBottom());
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#logicalToDeviceRect(com.embarcadero.describe.umlsupport.IETRect)
    */
   public IETRect logicalToDeviceRect(double left, double top, double right, double bottom)
   {
      if(getGraphWindow() != null)
      {
         TSTransform transform = getGraphWindow().getTransform();
         if(transform != null)
         {
            return new ETDeviceRect(transform.boundsToDevice(left,
            bottom,
            right,
            top));
         }
      }
      
      return null;
   }
   
   
   /**
    * Goes to a custom zoom setting by bringing up a dialog and allowing the
    * user to type in a zoom zetting.
    */
   public void onCustomZoom()
   {
      double currentZoom = getCurrentZoom();
      
      IZoomDialog pZoomDialog = new ZoomDialog(this.getOwnerFrame());
      
      if (pZoomDialog != null)
      {
         double nSelectedZoom = currentZoom;
         
         boolean bFitToPage = false;
         
         ETPairT < Double, Boolean > result = pZoomDialog.display(currentZoom);
         if (result != null)
         {
            nSelectedZoom = ((Double)result.getParamOne()).doubleValue();
            bFitToPage = ((Boolean)result.getParamTwo()).booleanValue();
         }
         
         if (bFitToPage)
         {
            this.fitInWindow();
         }
         else if (currentZoom != nSelectedZoom)
         {
            zoom(nSelectedZoom);
         }
      }
      
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#postDelayedAction(com.embarcadero.describe.diagrams.IDelayedAction)
     */
   public void postDelayedAction(IDelayedAction pAction)
   {
      onHandleDelayedAction( pAction );
   }
   
    /*
     * Safely deletes all the presentation elements in the list.
     */
   public void postDeletePresentationElements(ETList<IPresentationElement> pes)
   {
      try
      {
         if(pes!=null)
         {
            IteratorT<IPresentationElement> iter = new IteratorT<IPresentationElement>(pes);
            
            while (iter.hasNext())
            {
               // This can be trouble if the pe is a child presentation that was deleted
               // already so make sure it's on the diagram.
               IPresentationElement pe = iter.next();
               
               if (getIsOnDiagram(TypeConversions.getETGraphObject(pe)))
               {
                  postDeletePresentationElement(pe);

                  iter.remove();
               }
            }
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#postDeletePresentationElement(com.embarcadero.describe.foundation.IPresentationElement)
     */
   public void postDeletePresentationElement(final IPresentationElement pPE)
   {
      if (pPE != null)
      {
         // Fix J1170:  This call needs to be delayed so the diagram gets refreshed after all changes
         //             to the presentation elements.  In this case it was lifelines on the SQD.
         SwingUtilities.invokeLater( new Runnable()
         {
            public void run()
            {
               postDelayedAction(new PresentationElementToDeleteAction(pPE));
            }
         } );
      }
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#postDeletePresentationElement(com.tomsawyer.graph.TSGraphObject)
     */
   public void postDeletePresentationElement(TSGraphObject pGraphObject)
   {
      
      // PRINTSCOPEKEY(DrawingAreaDebug); // Used to print the scope in debug.
      
      if (pGraphObject == null)
         return;
      
      try
      {
         IPresentationElementToDeleteAction pDeleteAction = new PresentationElementToDeleteAction(pGraphObject);
         if (pDeleteAction != null)
         {
            postDelayedAction(pDeleteAction);
         }
      }
      catch ( Exception e )
      {
         e.printStackTrace();
      }
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#postEditLabel(org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation)
     */
   public void postEditLabel(ILabelPresentation pPE)
   {
      if( null == pPE ) throw new IllegalArgumentException();
      
      ISimplePresentationAction action = new SimplePresentationAction();
      if (action != null)
      {
         action.add(pPE);
         action.setKind( DiagramAreaEnumerations.SPAK_EDITLABEL );
         postDelayedAction(action);
      }
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#postInvalidate(com.embarcadero.describe.foundation.IPresentationElement)
     */
   public void postInvalidate(IPresentationElement pPresentationElement)
   {
      if (UserInputBlocker.getIsDisabled(GBK.DIAGRAM_INVALIDATE))
      {
         return;
      }
      
      if (pPresentationElement instanceof ProductGraphPresentation)
      {
         ProductGraphPresentation graphPE = (ProductGraphPresentation)pPresentationElement;
         
         this.refreshRect(graphPE.getBoundingRect());
      }
      
      //      if (pPresentationElement instanceof IGraphPresentation)
      //      {
      //         IGraphPresentation graphPresentation = (IGraphPresentation)pPresentationElement;
      //
      //         IETRect currentRect = graphPresentation.getViewBoundingRect();
      //         if (currentRect && m_InvalidateBeforeRect && pControl)
      //         {
      //            currentRect.unionWith(m_InvalidateBeforeRect);
      //
      //            // RefreshRect is expecting the y-axis to be inverted.
      //            currentRect.normalizeRect(true);
      //
      //            refreshRect(currentRect);
      //         }
      //         else
      //         {
      //            graphPresentation.invalidate();
      //         }
      //      }
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#postSelectEvent()
     */
   public void postSelectEvent()
   {
      postSimpleDelayedAction(DiagramAreaEnumerations.SPAK_SELECT_EVENT);
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#postSimpleDelayedAction(int)
     */
   public void postSimpleDelayedAction(int nKind)
   {
      postDelayedAction(new SimplePresentationAction(nKind));
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#postSimplePresentationDelayedAction(com.embarcadero.describe.foundation.IPresentationElement, int)
     */
   public void postSimplePresentationDelayedAction(IPresentationElement pPE, int nKind)
   {
      ISimplePresentationAction simpleAction = new SimplePresentationAction(nKind);
      simpleAction.add(pPE);
      
      postDelayedAction(simpleAction);
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#postSimplePresentationDelayedAction(com.embarcadero.describe.foundation.IPresentationElement, int)
    */
   public void postSimplePresentationDelayedAction( ETList<IPresentationElement> pPEs, int nKind)
   {
      ISimplePresentationAction simpleAction = new SimplePresentationAction(nKind);
      simpleAction.setPresentationElements(pPEs);
      
      postDelayedAction(simpleAction);
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#preferencesChanged(com.embarcadero.describe.umlutils.IPropertyElement[])
     */
   // Font and Color are dropped from 6.2 due to new resource management
   public boolean preferencesChanged(IPropertyElement[] pProperties) {
       if (m_ReadOnly) {
           return false;
       }
       
       IDrawingAreaControl pAxDrawingArea = (IDrawingAreaControl)this;
       boolean bDidChange = false;
       PropertyElements pPropertyElements = new PropertyElements(pProperties);
       
       // Now see if aliased or show tagged values has changed
       IPropertyElement pAliasPropertyElement = pPropertyElements.getElement("", "ShowAliasedNames", false);
       
       // Now see if aliased or show tagged values has changed
       //kris richards - "DisplayTVs" pref expunged. Set to true.
       //IPropertyElement pDisplayTVsPropertyElement = pPropertyElements.getElement("", "DisplayTVs", false);
       
       // Process the diagram preferences
       ETList<IPropertyElement> diagramProps = new ETArrayList<IPropertyElement>();
       
       ETList<IPropertyElement> pPropertyElementList = pPropertyElements.getList();
       int nCount = pPropertyElementList.size();
       for(int i = 0; i < nCount; i++ ) {
           IPropertyElement pElement = pPropertyElementList.get(i);
           if( pElement != null) {
               IPropertyElement pParentElement = null;
               String sParentName = "";
               
               pParentElement = pElement.getParent();
               if (pParentElement != null) {
                   sParentName = pParentElement.getName();
                   
                   if (sParentName.equals("Diagrams")) {
                       // Its something else
                       diagramProps.add(pElement);
                   }
               }
           }
       }
       
       // Process the diagram preferences
       for(int j = 0; j < diagramProps.size(); j++) {
           IPropertyElement pElement = diagramProps.get(j);
           if( pElement != null) {
               String sName = pElement.getName();
               String sValue = pElement.getValue();
               if( sName.length() > 0 && sValue.length() > 0) {
                   m_CachedPreferences.put(sName, sValue);
                   bDidChange = true;
               }
           }
       }
       
       // Handle the alias change if necessary.  Check ResizeOnAliasToggle preference to
       // see if we need to resize them too.
       if (pAliasPropertyElement != null) {
           syncElements(false);
           
           //kris richards - "AskBeforeLayout" pref expuged. Set to true.
           Preferences prefs = NbPreferences.forModule(ADDrawingAreaControl.class) ;
           
           //ResizeOnAliasToggle converted to UML_Resize_with_Show_Aliases_Mode
           // with default false.
           if (prefs.getBoolean("UML_Resize_with_Show_Aliases_Mode", false)) {
               // Resize all the elements
               // WHY ARE WE SETTING THIS TO FALSE IF CONDITION IS TRUE?
               sizeToContents(false);
           }
           bDidChange = true;
       }
       //kris richards - "DisplayTVs" pref expunged. Set to true. So element not null.
       else //if (pDisplayTVsPropertyElement != null)
       {
           syncElements(false);
           bDidChange = true;
       }
       
       if( bDidChange ) {
           // properties have been set, mark this diagram dirty and invalidate
           setIsDirty(true);
           refresh(false);
       }
       
       return true;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#pumpMessages(boolean)
     */
   public void pumpMessages(boolean bJustDrawingMessages)
   {
      Thread.yield();
   }
   
   /**
    * Ask the user what to do about a name collision
    *
    * @param pCompartmentBeingEdited [in] The compartment being edited
    * @param pElement [in] The element being renamed
    * @param sProposedName [in] The new name
    * @param pCollidingElements [in] A list of elements this name collides with
    * @param pCell [in] The result cell.  Used to cancel the rename.
    */
   public void questionUserAboutNameCollision(ICompartment pCompartmentBeingEdited, INamedElement pElement, String sProposedName, ETList<INamedElement> pCollidingElements, IResultCell pCell)
   {
      if (m_DiagramEngine != null)
      {
         boolean bContinue = m_DiagramEngine.questionUserAboutNameCollision(pCompartmentBeingEdited,
         pElement,
         sProposedName,
         pCollidingElements);
         if (!bContinue)
         {
            // Engine cancelled it
            pCell.setContinue(false);
         }
      }
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#receiveBroadcast(com.embarcadero.describe.diagrams.IBroadcastAction)
     */
   public void receiveBroadcast(IBroadcastAction pAction)
   {
      if (pAction != null)
      {
         // Block the delayed actions
         //CDelayedActionBlocker blocker(_T("ReceiveBroadcast"));
         if (pAction instanceof ISimpleBroadcastAction)
         {
            ISimpleBroadcastAction pSimple = (ISimpleBroadcastAction)pAction;
            int nKind = pSimple.getKind();
            switch (nKind)
            {
               case DiagramAreaEnumerations.SBK_DEEP_SYNC :
               {
                  syncElements(false);
               }
               break;
               case DiagramAreaEnumerations.SBK_TOOLTIPS_ON :
               {
                  setEnableTooltips(true);
               }
               break;
               case DiagramAreaEnumerations.SBK_TOOLTIPS_OFF :
               {
                  setEnableTooltips(false);
               }
               break;
               case DiagramAreaEnumerations.SBK_CLEAR_CLIPBOARD :
               {
                  clearClipboard();
               }
               break;
                    /* TODO
                    case DiagramAreaEnumerations.SBK_FLUSH_DELAYEDACTION_QUEUE :
                        {
                            if (m_hWnd)
                            {
                                ::PostMessage(m_hWnd, WM_DELAYEDACTION,0,0);
                            }
                        }
                        break;
                     */
            }
         }
         else if (pAction instanceof IElementBroadcastAction)
         {
            IElementBroadcastAction pElementAction = (IElementBroadcastAction)pAction;
            ETList<IElement> pElements = pElementAction.getModelElements();
            int nKind = pElementAction.getKind();
            switch (nKind)
            {
               case DiagramAreaEnumerations.EBK_DEEP_SYNC :
               case DiagramAreaEnumerations.EBK_DEEP_SYNC_AND_RESIZE :
               {
                  ISimpleElementsAction pElementsAction = new SimpleElementsAction();
                  if (pElementsAction != null)
                  {
                     pElementsAction.setElements(pElements);
                     if (nKind == DiagramAreaEnumerations.EBK_DEEP_SYNC)
                     {
                        pElementsAction.setKind(DiagramAreaEnumerations.SEAK_DEEPSYNC_BROADCAST);
                     }
                     else
                     {
                        pElementsAction.setKind(DiagramAreaEnumerations.SEAK_DEEPSYNC_AND_RESIZE_BROADCAST);
                     }
                     postDelayedAction(pElementsAction);
                  }
               }
               break;
            }
         }
      }
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#reconnectLink(com.embarcadero.describe.foundation.IPresentationElement, com.embarcadero.describe.foundation.IPresentationElement, com.embarcadero.describe.foundation.IPresentationElement)
     */
   public boolean reconnectLink(IPresentationElement pLink, IPresentationElement pOldNode, IPresentationElement pNewNode)
   {
      // TODO Auto-generated method stub
      return false;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#refresh(boolean)
     */
   public void refresh(boolean bPostMessage)
   {
      TSEGraphWindow window = getGraphWindow();
      if (window != null)
      {
         DrawingAreaRefreshHelper.refreshDrawingArea(this, !bPostMessage);
      }
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#refreshRect(com.embarcadero.describe.umlsupport.IETRect)
     */
   public void refreshRect(IETRect pRefreshRect)
   {
      TSEGraphWindow window = getGraphWindow();
      if(window!= null)
      {
         if (pRefreshRect != null)
         {
            window.addInvalidRegion(RectConversions.etRectToTSRect(pRefreshRect));
            DrawingAreaRefreshHelper.refreshDrawingArea(this, false);
         }
         else
         {
            DrawingAreaRefreshHelper.refreshDrawingArea(this, true);
         }
      }
   }
   
   /**
    * Removes an associated diagram from our list
    *
    * @param sDiagramXMIID [in] The xmiid of the diagram to remove.
    */
   public void removeAssociatedDiagram(String sDiagramXMIID)
   {
      if (!m_ReadOnly && sDiagramXMIID != null && sDiagramXMIID.length() > 0)
      {
         if (m_AssociatedDiagrams != null)
         {
            int count = m_AssociatedDiagrams.size();
            for (int i = 0; i < count; i++)
            {
               String str = (String) m_AssociatedDiagrams.get(i);
               if (str.equals(sDiagramXMIID))
               {
                  m_AssociatedDiagrams.remove(i);
                  setIsDirty(true);
                  break;
               }
            }
         }
      }
      
   }
   
   /**
    * Removes an associated diagram from our list
    *
    * @param pDiagram [in] The diagram we should remove
    */
   public void removeAssociatedDiagram2(IProxyDiagram pDiagram)
   {
      if (pDiagram != null)
      {
         String xmiid = pDiagram.getXMIID();
         if (!m_ReadOnly && xmiid != null && xmiid.length() > 0)
         {
            removeAssociatedDiagram(xmiid);
         }
      }
      
   }
   
   /**
    * Removes an associated element from our list
    *
    * @param sTopLevelElementXMIID [in] The elements toplevel id
    * @param sModelElementXMIID [in] The element we should remove
    */
   public void removeAssociatedElement(String topXMIID, String meID)
   {
      if (!m_ReadOnly && topXMIID != null && topXMIID.length() > 0 && meID != null && meID.length() > 0)
      {
         if (m_AssociatedElements != null)
         {
            Object obj = m_AssociatedElements.get(topXMIID);
            if (obj != null)
            {
               // Found it so erase
               if (obj instanceof Vector)
               {
                  Vector meidCol = (Vector)obj;
                  meidCol.remove(meID);
                  
                  if (meidCol.size() == 0)
                  {
                     m_AssociatedElements.remove(topXMIID);
                  }
               }
               setIsDirty(true);
            }
         }
      }
      
   }
   
   /**
    * Removes an associated element from our list
    *
    * @param pElement [in] The element we should remove
    */
   public void removeAssociatedElement2(IElement pElement)
   {
      if (pElement != null)
      {
         String topId = pElement.getTopLevelId();
         String xmiid = pElement.getXMIID();
         if (!m_ReadOnly && topId != null && topId.length() > 0 && xmiid != null && xmiid.length() > 0)
         {
            removeAssociatedElement(topId, xmiid);
         }
      }
      
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#removeElements(com.embarcadero.describe.foundation.IPresentationElement[])
     */
   public void removeElements(ETList<IPresentationElement> pItemsToRemove)
   {
      ETList<TSENode> nodeList = new ETArrayList<TSENode>();
      ETList<TSEEdge> edgeList = new ETArrayList<TSEEdge>();
      ETList<TSENodeLabel> nodeLabelList = new ETArrayList<TSENodeLabel>();
      ETList<TSEEdgeLabel> edgeLabelList = new ETArrayList<TSEEdgeLabel>();
      
      int count = 0;
      if(pItemsToRemove != null)
         count = pItemsToRemove.getCount();
      
      if(count > 0)
      {
         for(int index = 0;index < count; index++)
         {
            IPresentationElement pe = pItemsToRemove.item(index);
            
            if(pe != null)
            {
               TSENode node = TypeConversions.getOwnerNode(pe);
               TSEEdge edge = TypeConversions.getOwnerEdge(pe,false);
//                TSEEdgeLabel edgeLabel = TypeConversions.getEdgeLabel(pe);
               
               if(node != null)
               {
                  nodeList.addIfNotInList(node);
               }
               
               if(edge != null) {
                   edgeList.addIfNotInList(edge);
               }
//               if(edgeLabel != null) {
//                   edgeLabelList.addIfNotInList(edgeLabel);
//               }
               INodePresentation nodePresentation = null;
               if(pe instanceof INodePresentation)
                  nodePresentation = (INodePresentation)pe;
               
               IEdgePresentation edgePresentation = null;
               if(pe instanceof IEdgePresentation)
                  edgePresentation = (IEdgePresentation)pe;
               
//               if(nodePresentation != null)
//                  nodePresentation.setTSNode(null);
//               else if(edgePresentation != null)
//                  edgePresentation.setTSEdge(null);
            }
         }
         
         pItemsToRemove = null;
         
         deleteTheseItems(nodeList,edgeList,nodeLabelList,edgeLabelList,false);
         
         // Fix J1170:  Added this refresh section of code from C++
         // It's possible that the delete just whacked this diagram.  If the element that
         // got deleted was the owner of this diagram then ... we're gone to!
         // TODO if( isWindow() )
         {
            refresh( false );
         }
      }
   }
   
   /*
    * In the C++ code this routine is used to delay the actions.
    * In the Java code we just call the actions themselves, and if needed they delay themselves
    *
    * There is a lot of code on the C++ side that is not implemented here.
    */
   protected boolean onHandleDelayedAction( IDelayedAction action )
   {
      // This code is similar to the C++ code, but it did not seem to help CDFS during Web Report,
      // so I'm (BDB) leaving it out for now.
      //      try
      //      {
      //         while (DABlocker.getIsDisabled() || IOSemaphore.isIOHappenning())
      //         {
      //            Thread.sleep(500);
      //         }
      //      }
      //      catch( InterruptedException e )
      //      {
      //         e.printStackTrace();
      //      }
      
      boolean  handled = m_DiagramEngine != null ? m_DiagramEngine.handleDelayedAction( action ) : false;
      if( ! handled )
      {
         if (action instanceof IExecutableAction)
         {
            IExecutableAction selfExecutableAction = (IExecutableAction)action;
            
            // The presentation action handles all events.  If the diagram engine wants to
            // handle some it has the opportunity at the top of this loop.
            selfExecutableAction.execute( this );
         }
      }
      
      return handled;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#resetDrawEngine(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject, java.lang.String)
     */
   public void resetDrawEngine(IETGraphObject pETGraphObject, String sNewInitString)
   {
      IDrawEnginesToResetAction pResetAction = new DrawEnginesToResetAction();
      if (pResetAction != null)
      {
         pResetAction.init2(pETGraphObject, sNewInitString);
         postDelayedAction(pResetAction);
      }
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#resetDrawEngine2(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject)
     */
   public void resetDrawEngine2(IETGraphObject pETElement)
   {
      // TODO Auto-generated method stub
      
   }
   
   /**
    * Save this diagram.
    */
   public void save()
   {
       if (getIsDirty())
       {
           Mutex m = new Mutex();
           
           m.writeAccess(new Mutex.Action()
           {
               public Object run()
               {
                   preCommit();
                   commit();
                   return null;
               }
           });
       }
       getDiagram().setIsDirty(false);
   }
   
   /**
    * Allows the diagram to perform some cleanup before the diagram is actually
    * closed.
    */
   public synchronized void preClose()
   {
      onDestroy();
      notifyAll();
   }
   
   /*
    * Hides and releases references to the secondary windows, ie the overview, layout properties, drawing, and the Preferences.
    */
   protected void closeAllSecondaryWindows()
   {
      if (this.isOverviewWindowOpen())
      {
         this.overviewWindow(false);
      }
      
      if (getIsGraphPreferencesDialogOpen())
      {
         this.graphPreferencesDialog(false);
      }
      
      if (getIsLayoutPropertiesDialogOpen())
      {
         layoutPropertiesDialog(false);
      }
   }
   
   /**
    * Handles the destruction of this control.
    */
   protected void onDestroy()
   {
      IOSemaphore.startInstance();
      try
      {
	 if (kbAccessProvider != null) {
	     if (kbAccessProvider.getDiagramDrawingCtrl() == this) {
		 kbAccessProvider.setDiagramDrawingCtrl(null);
	     }
	     kbAccessProvider = null;
	 }									      
         // Use a null graph ui, so no threads can draw the graph.
         this.getCurrentGraph().setUI(new ETNullGraphUI());
         
         closeAllSecondaryWindows();
         m_GraphWindow.setVisible(false);
         releaseTSResource();
         
         if(m_DiagramEngine != null)
         {
            m_DiagramEngine.revokeAccelerators();
            m_DiagramEngine.detach();
         }
         
         if(m_drawingAreaDispatcher != null)
         {
            IEventPayload payload = m_drawingAreaDispatcher.createPayload("DiagramClosed");
            m_drawingAreaDispatcher.fireDrawingAreaClosed(m_Diagram, m_isDirty, payload);
         }
         
         // Tell the product that this drawing area no longer exists
         if (m_Diagram != null)
         {
            IProduct prod = getProduct();
            if (prod != null)
            {
               prod.removeDiagram(m_Diagram);
            }
         }
         
         if(m_CollisionHandler != null)
         {
            m_CollisionHandler.setDrawingArea(null);
         }
         
         m_NameCollisionListener = null;
         
         //         m_Drop.revoke();
         
         registerAllSinks(false);
         m_DrawingAreaEventsSink = null;
                  
         m_Namespace = null;
         modelElement = null;
         m_drawingAreaDispatcher = null;
         
         revokeResourceManager();
         
         if (trackBarChangeListener != null)
         {
            ((TSEEventManager)this.getGraphManager().getEventManager()).removeViewportChangeListener(this.getGraphWindow(), trackBarChangeListener);
         }
         
         if(trackBarModifyListener != null)
         {
            ((TSEEventManager)this.getGraphManager().getEventManager()).removeGraphChangeListener(this.getGraphWindow(), trackBarModifyListener);
         }
         
         //m_GraphWindow.removeGraphChangeListener(getActions());
         
         m_GraphWindow = null;
         m_Diagram = null;
          
         //JM: cleaning up service input data table..
         if (this.serviceInputDataTable != null && this.getGraphWindow() != null) {
             this.serviceInputDataTable.remove(this.getGraphWindow().getGraphManager()); 
          }         
          
         //         m_PresentationsTypesMgr = null;

      }
      finally
      {
         IOSemaphore.stopInstance();
      }
   }
   
   /**
    * Remove the resource manager for this drawing area
    */
   protected void revokeResourceManager()
   {
      ResourceMgr mgr = ResourceMgr.instance(this);
      if(mgr != null)
      {
         mgr.revoke();
      }
   }
   
   /**
    * Release the Tom Sawyer references to the drawing area control.
    */
   protected void releaseTSResource()
   {
      // Remove all our presentation elements, the only difference is that the IETGraphObject.delete gets invoked,
      // If we want we can just remove this, TSS can handle is alot faster then we can.  (Kevin)
      ETList < IPresentationElement > presentationElements = getAllItems();
      if(presentationElements != null)
      {
         for (Iterator < IPresentationElement > iter = presentationElements.iterator(); iter.hasNext();)
         {
            GetHelper.removePresentationElement(iter.next());
         }
      }
      
      getGraphWindow().resetGraphWindow();
      // Now whack the graph.
      //getGraphWindow().getGraphManager().emptyTopology(); //JM: This is not required..
      
      TSEGraphManager originalManager = getGraphManager();
      if(originalManager != null)
      {
         TSENestingManager.discardManager(originalManager);
         TSEFoldingManager.discardManager(originalManager);
         TSEHidingManager.discardManager(originalManager);
      }
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#saveAsGraphic(java.lang.String, int)
     */
   public boolean saveAsGraphic(String sFilename, int nKind)
   {
      return GetHelper.saveAsGraphic(this.getGraphWindow(), sFilename, nKind);
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#saveAsGraphic2(java.lang.String, int, com.embarcadero.describe.diagrams.IGraphicExportDetails)
     */
   public IGraphicExportDetails saveAsGraphic2(String sFilename, int nKind)
   {
      return GetHelper.saveAsGraphic2(this.getGraphWindow(), sFilename, nKind);
      
   }
   
   public IGraphicExportDetails saveAsGraphic2(String sFilename, int nKind, double scale)
   {
      return GetHelper.saveAsGraphic2(this.getGraphWindow(), sFilename, nKind, scale);
   }
 
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#selectAll(boolean)
     */
   public void selectAll(boolean bSelect)
   {
      if (this.getGraphWindow() != null)
      {
         if (bSelect)
         {
            this.getGraphWindow().selectAll(true);
         }
         else
         {
            this.getGraphWindow().deselectAll(true);
         }
         
         ETGraph etGraph = getGraph() instanceof ETGraph ? (ETGraph)getGraph() : null;
         
         if (etGraph != null)
         {
            this.fireSelectEvent(etGraph.getSelectedObjects(false, false));
            
         }
      }
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#selectAllSimilar()
     */
   public void selectAllSimilar()
   {
      
      ETList < IETGraphObject > selected = getSelected3();
      ETGraph etGraph = selected != null && getGraph() instanceof ETGraph ? (ETGraph) getGraph() : null;
      if (etGraph != null)
      {
         ETList < IETGraphObject > allObjects = etGraph.getAllETGraphObjects();
         
         allObjects.removeThese(selected);
         Iterator<IETGraphObject> selectedIter = selected.iterator();
         while (selectedIter.hasNext())
         {
            IETGraphObject selectedObject = selectedIter.next();
            IDrawEngine selectedDrawEngine = selectedObject.getEngine();
            
            String sDrawEngineType = selectedDrawEngine != null ? selectedDrawEngine.getDrawEngineMatchID() : null;
            if (sDrawEngineType != null && sDrawEngineType.length() > 0)
            {
               Iterator<IETGraphObject> allObjectsIter = allObjects.iterator();
               while (allObjectsIter.hasNext())
               {
                  IETGraphObject graphObject = allObjectsIter.next();
                  if (!graphObject.isSelected())
                  {
                     IDrawEngine nonSelectedDrawEngine = graphObject.getEngine();
                     String nonSelectedMatchID = nonSelectedDrawEngine != null ? nonSelectedDrawEngine.getDrawEngineMatchID() : null;
                     
                     if (nonSelectedMatchID != null && nonSelectedMatchID.equals(sDrawEngineType))
                     {
                        graphObject.setSelected(true);
                        graphObject.invalidate();
                     }
                  }
               }
            }
         }
         
         this.fireSelectEvent(etGraph != null ? etGraph.getSelectedObjects(false, false) : null);
         this.refresh(true);
      }
   }
   
   /**
    * Puts the diagram alias
    *
    * @param newVal [in] The new alias of the diagram.
    */
   public void setAlias(String newVal)
   {
      boolean isSame = true;
      boolean fireEvents = true;
      
      if (m_Alias != null && m_Alias.length() > 0)
      {
         // If the namespace is being changed then fire an event
         if (newVal != null && newVal.length() > 0)
         {
            if (!m_Alias.equals(newVal))
            {
               isSame = false;
            }
         }
         else
         {
            isSame = false;
         }
      }
      else if (newVal != null && newVal.length() > 0)
      {
         isSame = false;
         fireEvents = false;
      }
      
      if (!isSame)
      {
         boolean proceed = true;
         
         // Let folks know that the alias is being changed.
         if (getDrawingAreaDispatcher() != null && fireEvents)
         {
            proceed = fireDrawingAreaPrePropertyChange(DiagramAreaEnumerations.DAPK_ALIAS);
         }
         
         if (proceed)
         {
            m_Alias = newVal;
            if (fireEvents)
            {
               setIsDirty(true);
               
               // Let folks know that the alias has changed.
               fireDrawingAreaPropertyChange(DiagramAreaEnumerations.DAPK_ALIAS);
            }
         }
      }
   }
   
   /**
    * Set the type of this drawing
    *
    * @param newVal [in] The new type of this diagram (ie DK_CLASS_DIAGRAM)
    */
   public void setDiagramKind(int value)
   {
      IDiagramTypesManager diaMgr = DiagramTypesManager.instance();
      m_DiagramKindDisplayName = diaMgr.getDiagramTypeName(value);
      if (m_DiagramKindDisplayName != null && m_DiagramKindDisplayName.length() > 0)
      {
         if (m_DiagramEngine != null)
         {
            m_DiagramEngine.detach();
            m_DiagramEngine = null;
         }
         
         String engineType = diaMgr.getDiagramEngine(m_DiagramKindDisplayName);
         if (engineType != null && engineType.length() > 0)
         {
            ICreationFactory pCreatFact = FactoryRetriever.instance().getCreationFactory();
            Object obj = pCreatFact.retrieveEmptyMetaType("DiagramEngines", engineType, null);
            
            if (obj != null)
            {
               if (obj instanceof IDiagramEngine)
               {
                  m_DiagramEngine = (IDiagramEngine) obj;
               }
               
               if (m_DiagramEngine != null)
               {
                  m_DiagramEngine.attach(this);
                  
                  // Set the quickkeys
                  m_DiagramEngine.setQuickKeys(getGraphWindow());
                  
                  // Register any accelerators

                  m_DiagramEngine.registerAccelerators();
                  
                  // Go back to select draw mode
                  enterMode(IDrawingToolKind.DTK_SELECTION);
               }
            }
         }
      }
      
      // Since the diagram kind has changed we should reset the IDiagram, if we
      // have one, because it may not represent the correct diagram type anymore.
      m_Diagram = null;
//      if((m_Diagram != null) && (m_Diagram.getDiagramKind() != value))
//      {
//          m_Diagram = null;
//      }
      
      // Now set up any track bar based on the diagram type
      initializeTrackBar();
      
      if (m_showToolbars)
      {
          this.createToolbars();
      }
   }

   /**
    * Set the type of this drawing.  This routine converts the string to a DiagramKind (ie
    * "Class Diagram" to DK_CLASS_DIAGRAM).
    *
    * @param newVal [in] Sets the kind of this diagram.
    */
   public void setDiagramKind2(String value)
   {
      setDiagramKind(DiagramTypesManager.instance().getDiagramKind(value));
   }
   
   /**
    * Sets the documentation associated with this diagram
    *
    * @param sDocs [in] The new documentation for this diagram.
    */
   public void setDocumentation(String value)
   {
      boolean isSame = false;
      
      // If the documentation is being changed then fire an event
      if (m_Documentation.equals(value))
      {
         isSame = true;
      }
      
      if (!isSame)
      {


         // Let folks know that the documentation is being changed.
         boolean proceed = fireDrawingAreaPrePropertyChange(DiagramAreaEnumerations.DAPK_DOCUMENTATION);
         
         if (proceed)
         {
            m_Documentation = value;
            setIsDirty(true);
            
            fireDrawingAreaPropertyChange(DiagramAreaEnumerations.DAPK_DOCUMENTATION);
         }
      }
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#setEdgeDescription(java.lang.String)
     */
   public void setEdgeDescription(String value)
   {
      setMode(ADDrawingAreaConstants.ADD_EDGE_CMD, value);
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#setEnableTooltips(boolean)
     */
   public void setEnableTooltips(boolean bEnable)
   {
      // TODO Auto-generated method stub
      
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#setFocus()
     */
   public void setFocus()
   {
      //requestFocus(); 
       super.requestFocusInWindow();
   }
    
   public boolean isFocused () 
   {
       return isFocusOwner();
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#setGridSize(int)
     */
   public void setGridSize(int value)
   {
      // TODO Auto-generated method stub
      
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#setGridType(int)
     */
   public void setGridType(int value)
   {
      // TODO Auto-generated method stub
      
   }
   
   /**
    * Set IsDirty true when there is data that needs to be saved
    *
    * @param bIsDirty [in] Set the diagram as dirty.
    */
   public void setIsDirty(boolean value)
   {
       if (m_isDirty == value)
           return;
       
      IProxyUserInterface pProxyUserInterface = ProductHelper.getProxyUserInterface();
      if (pProxyUserInterface != null)
      {
         IDiagram dia = getDiagram();
         if (dia != null)
         {
            boolean proceed = true;
            
            // Let folks know that the dirty flag is being changed.
            if (getDrawingAreaDispatcher() != null)
            {
                IEventPayload payload = m_drawingAreaDispatcher.createPayload("FireDrawingAreaPrePropertyChange");
                IProxyDiagram pProxy = getProxyDiagram();
                if (pProxy != null)
                {
                    proceed = m_drawingAreaDispatcher.fireDrawingAreaPrePropertyChange(pProxy, DiagramAreaEnumerations.DAPK_DIRTYSTATE, payload);
                }
            }
            
            if (proceed)
            {
                
                pProxyUserInterface.dirtyStateChanged(dia, value);
                
                fireDrawingAreaPropertyChange(IDrawingAreaPropertyKind.DAPK_DIRTYSTATE);
                firePropertyChange(DIRTYSTATE, m_isDirty, value);
                m_isDirty = value;
            }
            
            
            // Tell the namespace that it has a child that's dirty
            if (m_Namespace != null)
            {
                IProject pProject = m_Namespace.getProject();
                if (pProject != null)
                {
                    // Just let the Project know that one of its children is dirty.
                    // In effect however, the .etd file or event the .etx file that the
                    // diagram is appearing in is unchanged.
                    pProject.setChildrenDirty(m_isDirty);
                }
            }
         }
      }
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#setLastSelectedButton(int)
     */
   public void setLastSelectedButton(int value)
   {
      // TODO Auto-generated method stub
      
   }
   
   /**
    * Sets the drawing area to a new layout style.
    *
    * @param nLayoutStyle [in] The new layout style for this diagram.
    */
   public void setLayoutStyle(int nLayoutStyle) 
   {
       ETSystem.out.println("m_ReadOnly = " + m_ReadOnly);
       if (!m_ReadOnly) 
       {
           ITopographyChangeAction pAction = new TopographyChangeAction();
           pAction.setKind(DiagramAreaEnumerations.TAK_LAYOUTCHANGE);
           pAction.setLayoutStyle(nLayoutStyle);
           postDelayedAction(pAction);
           getResources().setLayoutStyle(nLayoutStyle);
       }
   }
   
   /**
    * Sets the drawing area to a new layout style.  This routine does it without asking the user
    * if the layout is ok.
    *
    * @param nLayoutStyle [in] The new layout style for this diagram.
    */
   public void setLayoutStyleSilently(int nLayoutStyle)
   {
      if (!m_ReadOnly)
      {
         ITopographyChangeAction pAction = new TopographyChangeAction();
         pAction.setKind(DiagramAreaEnumerations.TAK_LAYOUTCHANGE_SILENT);
         pAction.setLayoutStyle(nLayoutStyle);
         postDelayedAction(pAction);
         getResources().setLayoutStyle(nLayoutStyle);
      }
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#setModelElement(com.embarcadero.describe.foundation.IElement)
     */
   public void setModelElement(IElement value)
   {
      modelElement = value;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#setModeLocked(boolean)
     */
   public void setModeLocked(boolean value)
   {
      // TODO Auto-generated method stub
      
   }
   
   /**
    * Initializes the diagram with a namespace
    *
    * @param pNamespace [in] The new namespace for this diagram.
    */
   public void setNamespace(INamespace pNamespace)
   {
      boolean isSame = true;
      boolean fireEvents = true;
      
      if (m_Namespace != null)
      {
         // If the namespace is being changed then fire an event
         if (pNamespace != null)
         {
            isSame = m_Namespace.isSame(pNamespace);
         }
         else
         {
            isSame = false;
         }
      }
      else if (pNamespace != null)
      {
         isSame = false;
         fireEvents = false;
      }
      
      if (!isSame)
      {
         boolean proceed = true;
         if (fireEvents)
         {
            proceed = fireDrawingAreaPrePropertyChange(DiagramAreaEnumerations.DAPK_NAMESPACE);
         }
         
         if (proceed)
         {
            m_Namespace = pNamespace;
            
            if (fireEvents)
            {
               //No need to set dirty state for nameSpace change. The change apply right away.
               //setIsDirty(true);
               
               // Let folks know that the namespace has changed.
               fireDrawingAreaPropertyChange(DiagramAreaEnumerations.DAPK_NAMESPACE);
               
               // Tell the product elements about the events
               //ETElementManager = new ETElementManager(this);
               //manager.onDiagramNamespaceChanged();
            }
            
            setDiagramNodeNameAndOwner();
         }
      }
   }
   
   /**
    * Sets / Gets the name or alias of this element.
    */
   public void setNameWithAlias(String value)
   {
      if (ProductHelper.getShowAliasedNames())
      {
         setAlias(value);
      } else
      {
         setName(value);
      }
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#setNodeDescription(java.lang.String)
     */
   public void setNodeDescription(String value)
   {
      setMode(ADDrawingAreaConstants.ADD_NODE_CMD, value);
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#setOnDrawZoom(double)
     */
   public void setOnDrawZoom(double nOnDrawZoom)
   {
      m_OnDrawZoom = nOnDrawZoom;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#setOverviewWindowRect(int, int, int, int)
     */
   public void setOverviewWindowRect(int nLeft, int nTop, int nWidth, int nHeight)
   {
      // TODO Auto-generated method stub
      
   }
   
   /**
    * Is this diagram readonly?
    *
    * @param newVal [in] true to make the diagram readonly
    */
   public void setReadOnly(boolean value)
   {
      if (m_ReadOnly != value)
      {
         // Let folks know that the reaonly is being changed - can't cancel this
         fireDrawingAreaPrePropertyChange(DiagramAreaEnumerations.DAPK_READONLY);
         
         // Set the readonly flag, the cancel flag is ignored
         m_ReadOnly = value;
         
         // Register or unregister for all sinks
         registerAllSinks(m_ReadOnly ? false : true);
         
         // Send out the post event
         fireDrawingAreaPropertyChange(DiagramAreaEnumerations.DAPK_READONLY);
      }
   }
   
   /**
    * Registers/Unregisters all sinks
    *
    * @param bDoRegister [in] true to register, false to revoke
    */
   private void registerAllSinks(boolean doRegister)
   {
      if (doRegister)
      {
         initEventSink();
      } else
      {
         revokeEventSinks();
      }
   }
   
   private boolean fireDrawingAreaPrePropertyChange(int propKind)
   {
      return fireDrawingAreaPrePropertyChange("FireDrawingAreaPrePropertyChange",propKind);
   }
   
   
   
   /**
    * Fires the pre property change event
    */
   private boolean fireDrawingAreaPrePropertyChange(String payload, int propKind)
   {
      boolean proceed = true;
      if (getDrawingAreaDispatcher() != null && payload != null)
      {
         IEventPayload ePayload = m_drawingAreaDispatcher.createPayload(payload);
         IProxyDiagram dia = getProxyDiagram();
         if (dia != null)
         {
            proceed = m_drawingAreaDispatcher.fireDrawingAreaPrePropertyChange(dia, propKind, ePayload);
         }
      }
      return proceed;
   }
   
   private void fireDrawingAreaPropertyChange(int iDrawingAreaPropertyKind)
   {
      fireDrawingAreaPropertyChange("DrawingAreaPostPropertyChange", iDrawingAreaPropertyKind);
   }
   
   /**
    * Fires the property change event
    */
   private void fireDrawingAreaPropertyChange(String payload, int propKind)
   {
      if (getDrawingAreaDispatcher() != null && payload != null)
      {
         IEventPayload ePayload = m_drawingAreaDispatcher.createPayload(payload);
         IProxyDiagram dia = getProxyDiagram();
         if (dia != null)
         {
            m_drawingAreaDispatcher.fireDrawingAreaPostPropertyChange(dia, propKind, ePayload);
         }
      }
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#setShowGrid(boolean)
     */
   public void setShowGrid(boolean value)
   {
      // TODO Auto-generated method stub
      
   }
   
   protected TSESaveAsImageDialog saveAsImageDialog = null;
   
   public String getExportAsImageTitle()
   {
      String title = new String(RESOURCE_BUNDLE.getString("IDS_EXPORTASIMAGE"));
      return title;
   }
   
   
   //JM: Fix for Bug#6283632 - begin
   public void showImageDialog() {
       if (this.saveAsImageDialog == null) {
           SunSaveAsImageDialog myDialog = new SunSaveAsImageDialog(
                   getOwnerFrame(),
                   getExportAsImageTitle(),
                   this.getGraphWindow(),
                   TSESaveAsImageDialog.JPG_FORMAT | TSESaveAsImageDialog.SVG_FORMAT | TSESaveAsImageDialog.PNG_FORMAT );
           this.saveAsImageDialog = myDialog;
       }
       this.saveAsImageDialog.setVisible(!this.saveAsImageDialog.isVisible());       
   }

   class SunSaveAsImageDialog extends TSESaveAsImageDialog {     
       
       public SunSaveAsImageDialog(Frame ownerFrame, String title, ADGraphWindow graphWindow, int flags ) {
           super(ownerFrame, title, graphWindow, flags);
           if (super.type == null) {
               super.type = "jpg";
           }   
           
           // reset the text for the fileName field
           String defaultFileName = RESOURCE_BUNDLE.getString("default.fileName");
           
           File defaultFile = new File(
               System.getProperty("user.home")+
               File.separatorChar+ defaultFileName +"."+type);
           
            try
            {
                
                super.fileName.setText(defaultFile.getCanonicalPath());
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
           
           //Jyothi:
           KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
           Action escapeAction = new AbstractAction() {
               public void actionPerformed(ActionEvent e) {
                   dispose();
               }
           };
           super.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(escape, "ESCAPE");
           super.getRootPane().getActionMap().put("ESCAPE", escapeAction);
       
           fileName.getDocument().addDocumentListener(new DocumentListener()
           {
               public void changedUpdate(DocumentEvent e)
               {
                   documentChanged();
               }
               public void insertUpdate(DocumentEvent e)
               {
                   documentChanged();
               }
               public void removeUpdate(DocumentEvent e)
               {
                   documentChanged();
               }
               private void documentChanged()
               {  
                   if (fileName.getText().trim().equals(""))
                   {
                       disable(okButton);
                       return;
                   }
                   
                   File file = new File(fileName.getText());
                   
                   if (!file.exists())
                       enable(okButton);
                   else if (file.isFile() && file.canWrite())
                       enable(okButton); 
                   else 
                       disable(okButton);
               }
           });
           
       }
       
       // provide custom image encoder to write image files #82394
       public TSEGraphImageEncoder newGraphImageEncoder(TSEGraphWindow window)
       {
           return new ETEGraphImageEncoder(window);
       }
       
       // override TSESaveAsImageDialog.onOK to workaround #82394
       public boolean onOK()
       {
           if (!"jpg".equals(type))  // NOI18N
               return super.onOK();
           
           boolean success = true;
           try
           {
               File file = new File(fileName.getText());
               if (file.exists() && !overWriteConfirm())
                   return false;
                
               boolean visibleAreaOnly = visible.isSelected();
               int zoomType = TSEGraphWindow.FIT_IN_WINDOW;
               
               if (actual.isSelected())
                   zoomType = TSEGraphWindow.ACTUAL_SIZE;
               else if (custom.isSelected())
                   zoomType = TSEGraphWindow.CUSTOM_SIZE;
               else if (zoomLevel.isSelected())
                   zoomType = TSEGraphWindow.CURRENT_ZOOM_LEVEL;
               
               boolean grid = drawGrid.isSelected();
               boolean selectedOnly = selected.isSelected();
               float quality = qualityField.parse(qualityField.getText());
               
               
               FileImageOutputStream fio = new FileImageOutputStream(new File(fileName.getText()));
               ETEGraphImageEncoder encoder = new ETEGraphImageEncoder(getGraphWindow());
               encoder.write("jpg", fio, visibleAreaOnly, zoomType, grid,      // NOI18N
                       selectedOnly, quality, width.parse(width.getText()),
                       height.parse(height.getText()));                    
           }
           catch(Exception e)
           {
               success = false;
               ErrorManager.getDefault().notify(e);
           }
           return success;
       }
       
       
   }
   //JM: Fix for Bug#6283632 - end
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#sizeToContents(boolean)
     */
   public void sizeToContents(boolean bJustSelectedElements)
   {
      
      ETList < IPresentationElement > pPresentationElements = null;
      
      if (bJustSelectedElements)
      {
         pPresentationElements = this.getSelected();
      } else
      {
         pPresentationElements = getAllItems();
      }
      
      int count = 0;
      if (pPresentationElements != null)
      {
         
         Iterator < IPresentationElement > iter = pPresentationElements.iterator();
         while (iter.hasNext())
         {
            IPresentationElement pPresentationElement = iter.next();
            if (pPresentationElement != null && pPresentationElement instanceof INodePresentation)
            {
               
               INodePresentation pNodePres = (INodePresentation) pPresentationElement;
               
               pNodePres.sizeToContents();
               pNodePres.invalidate();
               
            }
         }
         
      }
      // need to call refresh here because the drawing are does not refresh properly
      //when the newly sized node is smaller than the original size
      
      // Kevins Comments about the comment above, element invalidate only add there regions
      // to the Invalidation Region it doesn't repaint the window.  Also someone changed the drawEngine
      // Invalidation to repaint the window which is not what we want
      this.refresh(true);
   }
   
   public final static int RESIZE_ANCHOR_POINT_CENTER = 0;
   public final static int RESIZE_ANCHOR_POINT_TOPLEFT = 1;
   public final static int RESIZE_ANCHOR_POINT_TOPRIGHT = 2;
   public final static int RESIZE_ANCHOR_POINT_BOTTOMLEFT = 3;
   public final static int RESIZE_ANCHOR_POINT_BOTTOMRIGHT = 4;
   
   private static int savedAnchorPoint = RESIZE_ANCHOR_POINT_CENTER;
   
   /** (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#sizeToContents(boolean)
    */
   public boolean resizeDimensions()
   {
       ETList<IPresentationElement> presElements = null;
       presElements = getSelectedPresentionNodes();
       
       if (presElements == null)
           return false;
       
       double initHeight = 0;
       double initWidth = 0;
       
       if (presElements.size() == 1 &&
           presElements.get(0) instanceof INodePresentation)
       {
           INodePresentation nodePres = (INodePresentation)presElements.get(0);
           initHeight = nodePres.getHeight();
           initWidth = nodePres.getWidth();
       }
       
       ResizeElementsPanel resizePanel = new ResizeElementsPanel(
           Math.round(initHeight), Math.round(initWidth), 
           savedAnchorPoint, presElements);
       
       ResizeElementsDescriptor resizeDesc = new ResizeElementsDescriptor(
           resizePanel, // inner pane
           NbBundle.getMessage(ADDrawingAreaControl.class,
                "LBL_ResizeElementsDialog_Title"), // NOI18N
           true, // modal flag
           DialogDescriptor.OK_CANCEL_OPTION, // button option type
           DialogDescriptor.OK_OPTION, // default button
           DialogDescriptor.DEFAULT_ALIGN, // button alignment
           new HelpCtx("uml_resize_elements_dialog_box"), // NOI18N
           resizePanel); // button action listener
       
       resizePanel.getAccessibleContext().setAccessibleName(NbBundle
           .getMessage(ADDrawingAreaControl.class, "ACSN_ResizeElementsDialog")); // NOI18N
       resizePanel.getAccessibleContext().setAccessibleDescription(NbBundle
           .getMessage(ADDrawingAreaControl.class, "ACSD_ResizeElementsDialog")); // NOI18N
       
       resizePanel.requestFocus();
       
       Object result = DialogDisplayer.getDefault().notify(resizeDesc);
       
       if (result == DialogDescriptor.OK_OPTION)
       {
           initHeight = resizePanel.getResizeHeight();
           initWidth = resizePanel.getResizeWidth();
           savedAnchorPoint = resizePanel.getAnchorPoint();

           double top = 0;
           double bottom = 0;
           double right = 0;
           double left = 0;

           switch (savedAnchorPoint)
           {
               case RESIZE_ANCHOR_POINT_TOPLEFT:
                   bottom = -1D * initHeight;
                   right = initWidth;
                   break;

               case RESIZE_ANCHOR_POINT_TOPRIGHT:
                   bottom = -1D * initHeight;
                   left = -1D * initWidth;
                   break;

               case RESIZE_ANCHOR_POINT_BOTTOMLEFT:
                   top = initHeight;
                   right = initWidth;
                   break;

               case RESIZE_ANCHOR_POINT_BOTTOMRIGHT:
                   top = initHeight;
                   left = -1D * initWidth;
                   break;

               default: // center
                   top = initHeight / 2D;
                   bottom = initHeight / 2D * -1D;
                   left = initWidth / 2D * -1D;
                   right = initWidth / 2D;
           }

           TSTransform transform = getGraphWindow().getTransform();

           for (IPresentationElement presElem: presElements)
           {
               if (presElem != null && presElem instanceof INodePresentation)
               {
                   INodePresentation nodePres = (INodePresentation)presElem;
                   TSENode tsNode = nodePres.getTSNode();
                   nodePres.invalidate();

                   switch (savedAnchorPoint)
                   {
                       case RESIZE_ANCHOR_POINT_TOPLEFT:
                           tsNode.setBounds(
                               tsNode.getLeft(), // left
                               tsNode.getTop() + bottom, // bottom
                               tsNode.getLeft() + right, // right
                               tsNode.getTop()); // top
                           break;

                       case RESIZE_ANCHOR_POINT_TOPRIGHT:
                           tsNode.setBounds(
                               tsNode.getRight() + left, // left
                               tsNode.getTop() + bottom, // bottom
                               tsNode.getRight(), // right
                               tsNode.getTop()); // top
                           break;

                       case RESIZE_ANCHOR_POINT_BOTTOMLEFT:
                           tsNode.setBounds(
                               tsNode.getLeft(), // left
                               tsNode.getBottom(), // bottom
                               tsNode.getLeft() + right, // right
                               tsNode.getBottom() + top); // top
                           break;

                       case RESIZE_ANCHOR_POINT_BOTTOMRIGHT:
                           tsNode.setBounds(
                               tsNode.getRight() + left, // left
                               tsNode.getBottom(), // bottom
                               tsNode.getRight(), // right
                               tsNode.getBottom() + top); // top
                           break;

                       default: // center
                           tsNode.setBounds(
                               tsNode.getCenterX() + left, // left
                               tsNode.getCenterY() + top, // bottom
                               tsNode.getCenterX() + right, // right
                               tsNode.getCenterY() + bottom); // top
                   }

                   // TS always tells the node it was resized interactively
                   // when we do it programmatically we don't want that
                   if (nodePres.getDrawEngine() != null)
                       nodePres.getDrawEngine().onResized();
               
                   // Update the orginal size.
                   tsNode.setOriginalSize(tsNode.getWidth(), tsNode.getHeight());
                   nodePres.invalidate();
               } // if instanceof INodePresentation
           } // for

           // need to call refresh here because the drawing are does not refresh
           // properly when the newly sized node is smaller than the original size
           refresh(true);
       } // if "OK" response from Resize dialog
       
       requestFocus();
       return true;
   }
   

   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#sizeToContentsWithTrackBar(com.embarcadero.describe.foundation.IPresentationElement)
     */
   public void sizeToContentsWithTrackBar(IPresentationElement pElement)
   {
      if(pElement != null)
      {
         IETGraphObject object = TypeConversions.getETGraphObject(pElement);
         if(object != null)
         {
            object.sizeToContents();
            
            if(getTrackBar() != null)
            {
               getTrackBar().resize(pElement);
            }
         }
      }
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#sortNodesLeftToRight(com.embarcadero.describe.foundation.IPresentationElement[])
     */
   public ETList<IPresentationElement> sortNodesLeftToRight(ETList<IPresentationElement> pUnsortedList)
   {
      // TODO Auto-generated method stub
      return null;
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#syncElements(boolean)
     */
   public void syncElements(boolean bOnlySelectedElements)
   {
      IDiagramValidation pLocalValidation = new DiagramValidation();
      if (pLocalValidation != null)
      {
         // Validate both nodes and edges
         pLocalValidation.setValidateNodes(true);
         pLocalValidation.setValidateLinks(true);
         
         // In this case, don't validate anything but deep, and make it a force
         pLocalValidation.addValidationKind(IDiagramValidateKind.DVK_VALIDATE_RESYNC_DEEP);
         pLocalValidation.addValidationKind(IDiagramValidateKind.DVK_VALIDATE_CONNECTIONTOELEMENT);
         
         validateDiagram(bOnlySelectedElements, pLocalValidation);
      }
   }
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#transform(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject, java.lang.String)
     */
   public void transform(IETGraphObject pETGraphObject, String sToElement)
   {
      if (sToElement != null && sToElement.length() > 0)
      {
         ITransformAction pTransformAction = new TransformAction();
         if (pTransformAction != null)
         {
            IPresentationElement pPE = TypeConversions.getPresentationElement(pETGraphObject);
            if (pPE != null)
            {
               pTransformAction.setPresentationElement(pPE);
               pTransformAction.setNewElementType(sToElement);
               postDelayedAction(pTransformAction);
               postDelayedAction(new ReSelectionAction());
            }
         }
      }
   }
   

   class ReSelectionAction implements IExecutableAction, IDelayedAction
   {
       public String getDescription()
       {
	   return "ReSelectionAction";
       }
       
       public void execute(IDrawingAreaControl pControl)
       {
	   if (pControl.getGraphWindow().getGraph() != null 
	       && pControl.getGraphWindow().getGraph() instanceof ETGraph)
	   {
	       ETGraph etGraph =  (ETGraph) pControl.getGraphWindow().getGraph();
	       ETList < TSGraphObject > selectedObjs = etGraph.getSelectedObjects(false, false);
	       if (selectedObjs != null) 
	       {
		   pControl.fireSelectEvent(selectedObjs);
	       }
	   }
       }
   }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#unfoldNode(com.tomsawyer.graph.TSNode)
     */
   public void unfoldNode(TSNode pCurrentNode)
   {
      // TODO Auto-generated method stub
      
   }
   
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#validateDiagram(boolean, com.embarcadero.describe.diagrams.IDiagramValidation)
     */
   public IDiagramValidationResult validateDiagram(boolean bOnlySelectedElements, IDiagramValidation pDiagramValidation)
   {
      IDiagramValidationResult localResult = null;
      
      if(!m_ReadOnly)
      {
         
         IDiagramValidation localValidation = null;
         
         if(pDiagramValidation == null)
         {
            localValidation = new DiagramValidation();
            
            localValidation.setValidateNodes(true);
            localValidation.setValidateLinks(true);
            
            localValidation.addValidationKind(IDiagramValidateKind.DVK_VALIDATE_ALL);
            localValidation.removeValidationKind(IDiagramValidateKind.DVK_VALIDATE_RESYNC_DEEP);
            
            localValidation.addValidationResponse(IDiagramValidateResponse.DVRSP_VALIDATE_ALL);
         }
         else
         {
            localValidation = pDiagramValidation;
         }
         
         IDiagram diagram = getDiagram();
         if(diagram != null)
         {
            IDiagramValidator validator = new DiagramValidator();
            if(validator != null)
            {
               localResult = validator.validateDiagram(diagram,bOnlySelectedElements,localValidation);
               
               if(m_TrackBar != null)
               {
                  m_TrackBar.updateAllCarNames();
               }
            }
         }
      }
      
      return localResult;
   }
   
   public void onDrop(DropTargetDropEvent event, TSEObject graphObject)
   {
      if ( !m_ReadOnly &&
      event.isDataFlavorSupported(ADTransferable.ADDataFlavor) )
      {
         event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK);
         
         IPresentationElement peDroppedOn = null;
         IDrawEngine drawEngine = null;
         ICompartment compartmentDroppedOn = null;
          
         ADTransferable.ADTransferData eventData = null;
         
         TSConstPoint dropPoint = getGraphWindow().getNonalignedWorldPoint(event.getLocation());
         
         try
         {  
            eventData = (ADTransferable.ADTransferData) event.getTransferable().getTransferData(ADTransferable.ADDataFlavor);
            
            double dx = 0;
            double dy = 0;
            
            if(graphObject != null)
            {
               IGraphPresentation etElement = TypeConversions.getETElement((TSObject)graphObject);
               peDroppedOn = TypeConversions.getPresentationElement((TSObject)graphObject);
               
               if(peDroppedOn != null)
               {
                  drawEngine = TypeConversions.getDrawEngine(peDroppedOn);                  
                  if(drawEngine != null)
                  {                     
                     IETPoint pt = new ETPointEx(dropPoint);
                     compartmentDroppedOn = drawEngine.getCompartmentAtPoint(pt);
                  }
               }
            }
            
            ETList < IElement > additionalDropElement = new ETArrayList < IElement >();
            boolean cancel = fireDrawingAreaContextOnDrop(true,
                                                          peDroppedOn,
                                                          compartmentDroppedOn,
                                                          eventData,
                                                          additionalDropElement);
            
            // Add the additional model elements to the collection of model elements
            // that need to be processed.
            ArrayList < IElement > modelElements = eventData.getModelElements();
            for(Iterator < IElement > addIter = additionalDropElement.iterator(); addIter.hasNext();)
            {
               IElement curElement = addIter.next();
               if(modelElements.contains(curElement) == false)
               {
                  modelElements.add(curElement);
               }
            }
            
            IteratorT < IElement > iter = new IteratorT(modelElements);
            IElement element = iter.next();
            for (; element != null; element = iter.next())
            {
               // TODO: Need to process diagrams.
               
               IElement processedElement = processOnDropElement(element);
               if(processedElement == null)
                  continue;
               
               // TODO: Handle dropping on an existing node.
               
               ETNode newNode = addNode(processedElement, new ETPointEx(dropPoint.getX() + dx, dropPoint.getY() - dy), !iter.hasNext());
               dx = dx + 10;
               dy = dy + 10;
               if (newNode != null)
               {
                  newNode.sizeToContents();
               }
               
               //					nodeVerification.createAndVerify(this.getDiagram(), (IETNode) newNode, nameSpace);
               //					if (newNode == null) {
               //						event.dropComplete(false);
               //						break;
               //					}
               
               //onPostAddNode((ETNode)newNode);
               
               setModelElement(null);
            }
            this.refresh(true);
            // Here's where relatoinship discovery happens
            if (m_DiagramEngine != null)
            {
               ETList < IElement > pList = new ETArrayList<IElement>();
               if(eventData.getModelElements() != null)
               {
                  pList.addAll(eventData.getModelElements());
                  m_DiagramEngine.postOnDrop(pList, true);
               }
            }
         }
         catch (Exception e)
         {
            e.printStackTrace();
            event.dropComplete(false);
         }
         finally
         {
            fireDrawingAreaContextOnDrop(false,
                                         peDroppedOn,
                                         compartmentDroppedOn,
                                         eventData,
                                         null);
         }
      }
   }
   
   /**
    * Fires the context menu on drop events
    *
    * @param bFirePreEvent <code>true</code> if this is a pre drop event
    * @param pPresentationElementDroppedOn The presentation element dropped upon
    * @param pCompartmentDroppedOn The compartment dropped upon
    * @param sClipString The string currently on the clipboard
    * @param pAdditionalDropElements Additional elements the user wants to treat as dropped.
    * @return Set to <code>true</code> to cancel the event (pre only)
    */
   protected boolean fireDrawingAreaContextOnDrop(boolean firePreEvent,
                                                  IPresentationElement pPresentationElementDroppedOn,
                                                  ICompartment pCompartmentDroppedOn,
                                                  ADTransferable.ADTransferData dropData,
                                                  ETList<IElement> pAdditionalDropElements)
   {
      boolean retVal = false;
      
      IDiagram diagram = getDiagram();
      
      IDrawingAreaEventDispatcher dispatcher = getDrawingAreaDispatcher();
      if((dispatcher != null) && (dropData != null))
      {
         IDrawingAreaDropContext context = new DrawingAreaDropContext(dropData, 
                                                                      pPresentationElementDroppedOn,
                                                                      pCompartmentDroppedOn);
         if(firePreEvent == true)
         {
            IEventPayload payload = dispatcher.createPayload("DrawingAreaPreDrop");
            dispatcher.fireDrawingAreaPreDrop(diagram, context, payload);
            retVal = context.getCancel();
            
            ETList < IElement > additional = context.getAdditionalDropElements();
            pAdditionalDropElements.addAll(additional);
         }
         else
         {
            IEventPayload payload = dispatcher.createPayload("DrawingAreaPostDrop");
            dispatcher.fireDrawingAreaPostDrop(diagram, context, payload);
         }
         
      }
      
      return retVal;
   }
   
   protected String getNodeElementUIClass(IElement element)
   {
      String elementType = element.getElementType();
      return ETUIFactory.GENERIC_NODE_UI;
   }
   
   /**
    * Adds a node to the diagram.
    *
    * @param element The model element that is to be added to the diagram.
    * @param pt The center location for the node.
    * @param repaint <code>true</code> if the diagram is to be refreshed,
    *                <code>false</code> if the diagram is not to be refreshed.
    */
   protected ETNode addNode(IElement element, IETPoint pt, boolean repaint) throws ETException
   {
      ETNode retVal = addNode(element, pt);
      
      if (repaint == true)
      {
         refresh(true);
      }
      
      return retVal;
   }
   
   /**
    * Adds a node to the diagram.
    *
    * @param nodeInitString The initialization string for the node.
    * @param location The center location for the node.
    * @param bSelect Should we select this new graph object?
    * @param bDeselectAllOthers Should we deselect all other objects?
    * @param pElementToAssignToNode The element to assign the node to
    * @return The created node.  NULL if no node is created.
    */
   public ETNode addNode(IElement element, IETPoint pt) throws ETException
   {
      IPresentationTypesMgr mgr = getPresentationTypesMgr();
      if (mgr != null)
      {
         TSNode node = addNode(mgr.getMetaTypeInitString(element, getDiagramKind()), pt, false, false, element);
         return node instanceof ETNode ? (ETNode)node : null;
      }
      else
         return null;
      
   }
   
   /**
    * Adds a node to the diagram.  The type of the node is specified by the
    * metatype name.
    *
    * @param metaDataType The type of node to create.
    * @param location The location of the new node.
    * @param bSelect <code>true</code> if the diagram is to be selected.
    * @param bDeselectAllOthers <code>true</code> if all selected nodes are to
    *                           be deselected.
    * @return The new node.
    */
   public ETNode addNodeForType(String metaDataType, IETPoint location, boolean bSelect, boolean bDeselectAllOthers) throws ETException
   {
      IPresentationTypesMgr mgr = getPresentationTypesMgr();
      if (mgr != null)
      {
         TSNode node = addNode(mgr.getMetaTypeInitString(metaDataType, getDiagramKind()), location, false, false);
         return node instanceof ETNode ? (ETNode)node : null;
      }
      else
         return null;
   }
   
   /**
    * Adds a node to the diagram.  The initstring be used to determine the
    * UI object and the draw engine used to present the node.
    *
    * @param nodeInitString The initialization string.
    * @param location The location of the new node.
    * @param bSelect <code>true</code> if the diagram is to be selected.
    * @param bDeselectAllOthers <code>true</code> if all selected nodes are to
    *                           be deselected.
    * @return The new node.
    */
   public TSNode addNode(String nodeInitString, IETPoint location, boolean bSelect, boolean bDeselectAllOthers) throws ETException
   {
      TSNode retVal = null;
      
      TSEGraph graph = getGraph();
      if (graph != null)
      {
         IGUIBlocker blocker = null;
         try
         {
            blocker = new GUIBlocker( GBK.DIAGRAM_CONTAINMENT );
            
	    IPresentationTypesMgr mgr = getPresentationTypesMgr();
	    PresentationTypeDetails details = mgr.getInitStringDetails(nodeInitString, getDiagramKind());
               
	    if ( details != null) 
	    {
		int graphKind = details.getObjectKind();
		if ( graphKind != TSGraphObjectKind.TSGOK_EDGE ) 
		{		       
		    retVal = graph.addNode();
		    if (retVal instanceof ETNode)
		    {
			ETNode gNode = (ETNode) retVal;
			gNode.setCenter(location.getX(), location.getY());
			
			int delimiter = nodeInitString.indexOf(' ');
			String nodeUIClass = delimiter > 0 ? nodeInitString.substring(0, delimiter) : nodeInitString;
			ETGenericNodeUI nodeUI = ETUIFactory.createNodeUI(nodeUIClass, nodeInitString, details.getEngineName(), this);
			gNode.setUI(nodeUI);
               
			onPostAddNode((IETNode)gNode);
			if ((bSelect == true) || (bDeselectAllOthers == true))
			{
			    selectAndFireEvents(retVal, bSelect, bDeselectAllOthers);
			}
		    }
		}
	    }
         }
         finally
         {
            blocker.clearBlockers();
         }
      }
      
      return retVal;
   }
   
   /**
    * Adds a node to the diagram.
    *
    * @param nodeInitString The initialization string for the node.
    * @param location The center location for the node.
    * @param bSelect Should we select this new graph object?
    * @param bDeselectAllOthers Should we deselect all other objects?
    * @param pElementToAssignToNode The element to assign the node to
    * @return The created node.  NULL if no node is created.
    */
   public TSNode addNode(String nodeInitString, IETPoint location, boolean bSelect, boolean bDeselectAllOthers, IElement pElementToAssignToNode) throws ETException
   {
      TSNode retVal = null;
      
      if (nodeInitString.length() > 0)
      {
         // Save the current model element before changing it.
         IElement pPrevElement = this.getModelElement();
         setModelElement(pElementToAssignToNode);
         
         retVal = addNode(nodeInitString, location, bSelect, bDeselectAllOthers);
         
         // Restore it.
         this.setModelElement(pPrevElement);
      }
      
      return retVal;
   }
   
    /*
     * Gets called after an addNode call, the node reprents the node just added to the diagram.
     */
   protected void onPostAddNode(IETNode node)
   {
      onPostAddObject(node);
   }
   
   protected void onPostAddEdge(IETEdge edge)
   {
      onPostAddObject(edge);
   }
   
   protected void onPostAddObject(IETGraphObject object)
   {
      if (object != null)
      {
         postAddObject(object, object.isNode());	// Only resize the nodes.
      }
   }
   
   public boolean showAccessiblePopupMenu()
   {
       return showPopupMenu(null);
   }
   
   private boolean showPopupMenu(MouseEvent e)
   {
       ETList < IPresentationElement > selected = getSelected();
       if (selected ==null)
       {
           fireSelectEvent(new ArrayList()); // to select diagram itself
       }
       
       boolean retVal = true;
       if(e != null)
       {
           retVal = e.isPopupTrigger();
       }
       
       ADGraphWindow graphWindow = getGraphWindow();
       if (graphWindow != null)
       {
           TSEWindowTool curState = graphWindow.getCurrentTool();
           TSEWindowTool defaultState = graphWindow.getDefaultTool();
           if ((retVal == true) && (curState == defaultState))
           {
               if (m_ContextMenuManager.getRemoveAllWhenShown() == true)
               {
                   m_ContextMenuManager.removeAll();
                   m_ContextPopup.removeAll();
               }
               
               Point eventPoint = null;
               if(e != null)
               {
                   eventPoint = e.getPoint();
               }
               
               m_ContextMenuManager.setLocation(eventPoint);
               
               TSEHitTesting hitTest = graphWindow.getHitTesting();
               m_ContextMenuManager.setContextObject(null); // represents Diagram
               TSEObject contextObj = null;
               if ((hitTest != null) && (eventPoint != null))
               {
                  TSConstPoint pt = graphWindow.getNonalignedWorldPoint(eventPoint);
                  contextObj = hitTest.getGraphObjectAt(pt, graphWindow.getGraph(), true);
                  if ( contextObj != null)
                  {
                     m_ContextMenuManager.setContextObject(contextObj);
                     if (Utilities.isUnix() || Utilities.isMac())
                     {
                        selectAndFireEvents(((TSGraphObject)contextObj), true, true);
                     }
                  }
               }
               else if((e == null) && (selected != null))
               {
                   // Make the last selected object the context object.
                   IPresentationElement pres = selected.get(selected.size() - 1);
                   contextObj = (TSEObject)TypeConversions.getTSObject(pres);
                   m_ContextMenuManager.setContextObject(contextObj);
               }
               
               menuAboutToShow(m_ContextMenuManager);
               m_ContextMenuManager.createMenuBar();
               JPopupMenu menu = m_ContextPopup.getPopupMenu();
               
               // The (0, 0) location will be used when the diagram is selected.
               int x = 0;
               int y = 0;
               if(e != null)
               {
                   x = e.getX();
                   y = e.getY();
               }
               else if(contextObj != null)
               {
                   double logicalX = contextObj.getLeft();
                   double logicalY = contextObj.getTop();
                   
                   TSTransform transform = getGraphWindow().getTransform();
                   if(transform != null)
                   {
                       x = transform.xToDevice(logicalX);
                       y = transform.yToDevice(logicalY);
                   }
               }
               
               menu.show(this, x, y);
           }
           
           if(retVal)
           {
               setSelectStateOnPalette();
           }
       }
       
       return retVal;
   }

    /*
     * Inner keyboard Listener.
     *
     */
   private class KeyHandler implements KeyListener
   {
      public void keyTyped(KeyEvent e)
      {
         handleKey(e);
      }
      
      public void keyPressed(KeyEvent e)
      {
         handleKey(e);
      }
      
      public void keyReleased(KeyEvent e)
      {
         handleKey(e);
      }
      
      private void handleKey(KeyEvent e)
      {
         ETList<IPresentationElement> elems = getSelected();
         if (elems != null)
         {
            int count = elems.size();
            if (count == 1)
            {
               IPresentationElement pEle = elems.get(0);
               IElement modEle = pEle.getFirstSubject();
               if (modEle != null)
               {
                  IDrawEngine engine = TypeConversions.getDrawEngine(pEle);
                  if (engine != null)
                  {
                     ICompartment compartment = engine.getDefaultCompartment();
                     if (compartment != null && compartment instanceof ETCompartment)
                     {
                        compartment.editCompartment(false, e.getKeyCode(), e.isShiftDown() ? 0 : 1, 0);
                     }
                  }
               }
            }
         }
      }
   }
   
   private class TestAction extends AbstractAction
   {
      public void actionPerformed(ActionEvent e)
      {
         ETSystem.out.println("hello");
      }
   }
   
   private class MouseHandler extends MouseAdapter
   {
      private JPanel m_View = null;
      public MouseHandler(JPanel view)
      {
         m_View = view;
      }
      
      public void mousePressed(MouseEvent event)
      {
         //if I was editing something on drawing area, need to commit that
         saveEditCompartment();
         
         requestFocus();
         showPopupMenu(event);        
      }
      
      public void mouseReleased(MouseEvent event)
      {
         // Some Operating Systems will show the popupmenu on the mouse
         // down and some will show it on the mouse up.
         showPopupMenu(event);
      }    
      
//      public boolean showAccessiblePopupMenu()
//      {
//          return showPopupMenu(null);
//      }
//      
//      private boolean showPopupMenu(MouseEvent e)
//      {
//          ETList < IPresentationElement > selected = getSelected();
//          if (selected ==null)
//          {
//              fireSelectEvent(new ArrayList()); // to select diagram itself
//          }
//          
//          boolean retVal = true;
//          if(e != null)
//          {
//              retVal = e.isPopupTrigger();
//          }
//          
//          ADGraphWindow graphWindow = getGraphWindow();
//          if (graphWindow != null)
//          {
//              TSEWindowTool curState = graphWindow.getCurrentTool();
//              TSEWindowTool defaultState = graphWindow.getDefaultTool();
//              if ((retVal == true) && (curState == defaultState))
//              {
//                  if (m_ContextMenuManager.getRemoveAllWhenShown() == true)
//                  {
//                      m_ContextMenuManager.removeAll();
//                      m_ContextPopup.removeAll();
//                  }
//                  
//                  m_ContextMenuManager.setLocation(e.getPoint());
//                  
//                  TSEHitTesting hitTest = graphWindow.getHitTesting();
//                  m_ContextMenuManager.setContextObject(null); // represents Diagram
//                  if ((hitTest != null) && (e != null)) 
//                  {
//                      TSConstPoint pt = graphWindow.getNonalignedWorldPoint(e.getPoint());
//                      m_ContextMenuManager.setContextObject(hitTest.getGraphObjectAt(pt, graphWindow.getGraph(), true));
//                  }
//                  else if((e == null) && (selected != null))
//                  { 
//                      // Make the last selected object the context object.
//                      m_ContextMenuManager.setContextObject(selected.get(selected.size() - 1));
//                  }
//                  
//                  menuAboutToShow(m_ContextMenuManager);  
//                  m_ContextMenuManager.createMenuBar();
//                  JPopupMenu menu = m_ContextPopup.getPopupMenu();
//                  menu.show(m_View, e.getX(), e.getY());
//              }
//              else
//              {
//                  super.mousePressed(e);
//              }
//              
//              if(retVal)
//              {
//                  setSelectStateOnPalette();
//              }
//          }
//          
//          return retVal;
//      }
   }
   
   public void registerContextMenu(String id,
                                   boolean clearBeforeShow)
   {
      m_ContextMenuManager = new TestBedMenuManager();
      m_ContextMenuManager.setRemoveAllWhenShown(clearBeforeShow);
      
      m_ContextMenuManager.setMenuItem(m_ContextPopup);
//      m_ContextMenu = new PopupMenuExtender(id, m_ContextMenuManager, prov, this);
   }
   
   public void createViewControl(JPanel parent)
   {
      parent.setLayout(new BorderLayout());
   }
   
   public void menuAboutToShow(IMenuManager manager)
   {
      
      //create our menu items
      addDefaultMenuItems(manager);
      
      //Now tell the sinks that the context menu should be created.
      fireDrawingAreaContextMenuPrepare(manager);
      
      //tell the diagram engine that context menu is about to appear
      if (m_DiagramEngine != null)
      {
         m_DiagramEngine.onContextMenu(manager);
      }
      
      // The following does not make sence when multiple objects are selected because it always sends the event
      // to the first object in the list which does not signify anything.
      // Instead send the event to the object under the cursor.
      //--------------------------
      //now tell the nodes that context menu is about to appear
      ETGraph etGraph = (ETGraph)getGraph();
      
      Object targetObj = manager.getContextObject();
      TSGraphObject graphObj = (targetObj instanceof TSGraphObject) ? (TSGraphObject)targetObj : null;
      if (graphObj != null)
      {
         IETGraphObject etElem = TypeConversions.getETGraphObject(graphObj);
         if (etElem != null)
         {
            etElem.onContextMenu(manager);
         }
      }
      else
      {
         manager.setContextObject(etGraph);
      }
      
      addAddinMenus(manager);
      
      // Sort it before shows up
      IProductContextMenuSorter pProductContextMenuSorter = new DrawingAreaContextMenuSorter();
      pProductContextMenuSorter.sort(manager);
      
      //JM: Printing context menu items...
//      printContextMenuItems(manager);
      
      // Now tell the sinks that the menu has been created and they can override the implementations.
      fireDrawingAreaContextMenuPrepared(manager);
   }
   
//   private void printContextMenuItems(IMenuManager manager) {
//       IContributionItem[] pContributionItems = manager.getItems();
//       if (pContributionItems != null) {
//           int count = pContributionItems.length;
//           for (int i = 0 ; i < count ; i++) {
//		IContributionItem pTempMenuItem = pContributionItems[i];
//                System.out.println(" menu Item = "+pTempMenuItem.getLabel());
//           }
//       }
//   }
   
   private void addAddinMenus(IMenuManager mgr)
   {
	   mgr.add(new Separator());
	   
	   Action[] actions = getActionsFromRegistry("contextmenu/uml/diagram");
	   
	   for(Action curAction : actions)
	   {
		   if (curAction == null)
			   // Make Sure the Seperators are kept.
			   mgr.add(new Separator());
		   
		   else if (curAction.isEnabled())
			   mgr.add(new BaseActionWrapper(curAction));
	   }
   }
   
   
   /**
	 * The registry information that is retrieved from layer files to build
	 * the list of actions supported by this node.
	 *
	 * @param path The registry path that is used for the lookup.
	 * @return The list of actions in the path.  null will be used if when
	 *         seperators can be placed.
	 */
	protected Action[] getActionsFromRegistry(String path)
	{
		ArrayList<Action> actions = new ArrayList<Action>();
		FileSystem system = Repository.getDefault().getDefaultFileSystem();
		
		try
		{
			if (system != null)
			{
				FileObject lookupDir = system.findResource(path);
				
				if (lookupDir != null)
				{
					FileObject[] children = lookupDir.getChildren();
					
					for (FileObject curObj : children)
					{
						try
						{
							DataObject dObj = DataObject.find(curObj);
							
							if (dObj != null)
							{
								InstanceCookie cookie = (InstanceCookie)dObj
										.getCookie(InstanceCookie.class);
								
								if (cookie != null)
								{
									Object obj = cookie.instanceCreate();
									
									if (obj instanceof Action)
									{
										actions.add((Action)obj);
									}									
									else if (obj instanceof JSeparator)
                           {
										actions.add(null);
                           }
								}
							} // dObj != null
						}
						
						catch(ClassNotFoundException e)
						{
							// Unable to create the instance for some reason.  So the
							// do not worry about adding the instance to the list.
						}
					} // for-each FileObject
				} // if lookupDir != null
			} // if system != null
		}
		
		catch(DataObjectNotFoundException e)
		{
			// Basically Bail at this time.
		}
		
		catch(IOException ioE)
		{

		}

		Action[] retVal = new Action[actions.size()];
		actions.toArray(retVal);
		return retVal;
	}
   
   private void addDefaultMenuItems(IMenuManager mgr)
   {
      //no default items
   }
   
   /**
    * Fires the context menu pre display event
    */
   private void fireDrawingAreaContextMenuPrepare(IMenuManager contextMenu)
   {
      IDiagram dia = getDiagram();
      if (getDrawingAreaDispatcher() != null && dia != null)
      {
         IEventPayload payload = m_drawingAreaDispatcher.createPayload("DrawingAreaContextMenuPrepare");
         m_drawingAreaDispatcher.fireDrawingAreaContextMenuPrepare(dia, null, payload);
      }
   }
   
   /**
    * Fires the context menu post display event.  This allows sinks to put their own implementation on the buttons.
    */
   private void fireDrawingAreaContextMenuPrepared(IMenuManager contextMenu)
   {
      IDiagram dia = getDiagram();
      if (getDrawingAreaDispatcher() != null && dia != null)
      {
         IEventPayload payload = m_drawingAreaDispatcher.createPayload("DrawingAreaContextMenuPrepared");
         m_drawingAreaDispatcher.fireDrawingAreaContextMenuPrepared(dia, null, payload);
      }
   }
   
    /*
     * Returns the selected compartments for the graph object.
     */
   public ETList<ICompartment> getSelectedCompartments(TSGraphObject pGraphObject)
   {
      IETGraphObject obj = pGraphObject instanceof IETGraphObject ? (IETGraphObject) pGraphObject : null;
      
      if (obj != null && obj.getEngine() != null)
      {
         return obj.getEngine().getSelectedCompartments();
      }
      return null;
   }
   
   public void setSelectedCompartments(boolean state) 
   {
       ETList <ICompartment> selectedCompartments = null;
       ICompartment selectedCompartment = null;
//       ETList <IETGraphObject> graphObjects =  getSelectedNodes();
       ETList <TSObject> graphObjects = getAllGraphObjects();
       if (graphObjects == null)
           return;
       
       for (TSObject graphObj: graphObjects)
       {
           if (graphObj != null && graphObj instanceof IETNode)
           {               
               IDrawEngine drawEngine = TypeConversions.getDrawEngine(graphObj);               
               if (drawEngine != null) {
                   selectedCompartments = drawEngine.getSelectedCompartments();
                   if (selectedCompartments != null && selectedCompartments.size() > 0) {
//                       selectedCompartment = (ICompartment) selectedCompartments.iterator().next();
//                       selectedCompartment.setSelected(false);
                       
                       Iterator < ICompartment > iter = selectedCompartments.iterator();
                       while (iter.hasNext()) {
                           selectedCompartment = (ICompartment)iter.next();
                           selectedCompartment.setSelected(false);
                       }
                   }
                   
               }
           }
       }
   }
   
    /*
     * Returns true if the graph object is selected.
     */
   public boolean isSelected(TSGraphObject pGraphObject)
   {
      if (pGraphObject instanceof ITSGraphObject)
      {
         ITSGraphObject obj = (ITSGraphObject) pGraphObject;
         return obj.isSelected();
      } else
         return false;
   }
   
   public IElement getIElement2(ETEdge object, IElementLocator pLocator)
   {
      if (object == null)
         return null;
      
      if (pLocator == null)
         pLocator = new ElementLocator();
      
      ETGenericEdgeUI ui = (ETGenericEdgeUI) object.getUI();
      if (ui != null)
         return pLocator.findElementByID(ui.getTopLevelMEIDValue(), ui.getMeidValue());
      else
         return null;
   }
   
   public IElement getIElement(ETNode object, IElementLocator pLocator)
   {
      if (object == null)
         return null;
      
      if (pLocator == null)
         pLocator = new ElementLocator();
      
      ETGenericNodeUI nodeUI = (ETGenericNodeUI) object.getUI();
      if (nodeUI != null)
         return pLocator.findElementByID(nodeUI.getTopLevelMEIDValue(), nodeUI.getMeidValue());
      else
         return null;
   }
   
   public IElement getIElement(ETNode node)
   {
      return getIElement(node, null);
   }
   
    /*
     * Sets the selection state on a graph object
     */
   protected void setSelect(TSGraphObject pGraphObject, boolean bSelect)
   {
      if (pGraphObject instanceof ITSGraphObject)
      {
         ITSGraphObject obj = (ITSGraphObject)pGraphObject;
         obj.setSelected(bSelect);
      }
   }
   
    /*
     * getDrawingAreaDispatcher is a demand load accessor to the drawArea Event Dispatcher.
     */
   public IDrawingAreaEventDispatcher getDrawingAreaDispatcher()
   {
      if (m_drawingAreaDispatcher == null)
      {
         DispatchHelper helper = new DispatchHelper();
         m_drawingAreaDispatcher = helper.getDrawingAreaDispatcher();
      }
      return m_drawingAreaDispatcher;
   }
   
   /**
    * Selects the element and fires the events
    *
    * @param pGraphObject The graph object to select or unselect
    * @param bSelect Set to true to select this guy, otherwise it's an unselect.
    * @param bDeselectAllOthers Should we deselect all other objects?
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl#selectAndFireEvents(com.tomsawyer.graph.TSGraphObject, boolean, boolean)
    */
   public void selectAndFireEvents(TSGraphObject pGraphObject, boolean bSelect, boolean bDeselectAllOthers)
   {
      if (bDeselectAllOthers)
      {
         // Save the currently selected objects.
         ETList < IPresentationElement > unselectedItems = this.getSelected();
         
         // Remove the select state from all objects.
         getGraphWindow().deselectAll(false);
         
         if (this.getActions().getEnableSelectionEvents() == false)
         {
            //				getDrawingAreaDispatcher().fireUnselect(this.getDiagram(),
            //					unselectedItems,
            //					getDrawingAreaDispatcher().createPayload("DrawingAreaSelChanged"));
            
         }
      }
      
      if (pGraphObject != null)
      {
         // Don't send the un/select event twice
         
         if (bSelect && !isSelected(pGraphObject) || !bSelect && isSelected(pGraphObject))
         {
            setSelect(pGraphObject, bSelect);
            
            if (bSelect)
            {
               this.fireSelectEvent(pGraphObject);
            } else
            {
               this.fireUnselectEvent(pGraphObject);
            }
         }
      }
      
   }
   
    /*
     * Disables the Drawing area action listener from Sending Selection and deselection events.
     * We need this because sometime we don't want to notify that objects selection state has changed.
     */
   public void disableSelectionChangeEvents()
   {
      this.getActions().setEnableSelectionEvents(false);
   }
   
    /*
     * Enables the drawing area action listener so it will fire selection and deselection change events.
     */
   public void enableSelectionChangeEvents()
   {
      this.getActions().setEnableSelectionEvents(true);
   }
   
    /*
     * This method just fires the selection notification it doesn't change the state of the graph object.
     */
   public void fireSelectEvent(TSGraphObject pGraphObject)
   {
      IEventPayload payload = getDrawingAreaDispatcher().createPayload("DrawingAreaSelChanged");
//      ETList < IPresentationElement > selectedItems = this.getSelected();
      
      //this.getDrawingAreaDispatcher().fireSelect(this.getDiagram(),null,null,//getSelectedCompartments(pGraphObject),
      //			payload);
      
      //Jyothi:For some reason the contents of this method have been commented out.. 
      //I need to use it for A11y tab select feature.. hence this workaround      
      tempList.add(pGraphObject);
      fireSelectEvent(tempList);
      tempList.clear();
   }
   
    /*
     * This method just fires the selection notification it doesn't change the state of the graph object.
     */
   
   //NOTE: The selection change event is fired in ADDrawingAreaSelectState.onMouseClicked()
   public void fireSelectEvent(List pSelectedGraphObjs)
   {
      ETArrayList < IPresentationElement > presentationElementList = new ETArrayList < IPresentationElement > ();
      ETList <ICompartment> selectedCompartments = null;
      ICompartment selectedCompartment = null;
      
      IEventPayload payload = getDrawingAreaDispatcher().createPayload("DrawingAreaSelChanged");
      
      try
      {
         if (pSelectedGraphObjs != null)
         {
            IDrawEngine drawEngine = null;
            Iterator < TSEObject > iter = pSelectedGraphObjs.iterator();
            while (iter.hasNext())
            {
               TSEObject graphObject = iter.next();
               IETGraphObjectUI ui = graphObject.getUI() instanceof IETGraphObjectUI ? (IETGraphObjectUI) graphObject.getUI() : null;
               if (ui != null)
               {
                  drawEngine = ui.getDrawEngine();
                  if (drawEngine != null)
                  {
                     presentationElementList.add(drawEngine.getPresentation());
                  }
               }
            }
            if (drawEngine != null)
            {
               selectedCompartments = drawEngine.getSelectedCompartments();
               if (selectedCompartments != null && selectedCompartments.size() > 0)
               {
                  selectedCompartment = (ICompartment) selectedCompartments.iterator().next();
               }
            }
         }
         //Jyothi: clear m_selectedNodesGroup when you click on the diagram using mouse
         else {
             clearSelectedNodesGroup();
             // Deselect all selected compartments.
             setSelectedCompartments(false);
         }
         //even if nothing is selected I need to fire the event.
         this.getDrawingAreaDispatcher().fireSelect(this.getDiagram(), presentationElementList, selectedCompartment, payload);
         
         //        ETElementManager mgr = new ETElementManager(this);
         //        mgr.onPostSelect(presentationElementList);
         
         IDiagramValidator diagramValidator = new DiagramValidator();
         for (Iterator < IPresentationElement > iter = presentationElementList.iterator(); iter.hasNext();)
         {
            IPresentationElement element = iter.next();
            IETGraphObject etGraphObject = TypeConversions.getETGraphObject(element);
            if(etGraphObject != null)
            {
              diagramValidator.doPostSelectValidation(m_Diagram, etGraphObject);
            }
         }
         
         
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      
      // ETSystem.out.println("ADDrawignAreaControl.fireSelectEvent()");
   }
   
    /*
     * This method just fires the deslection notification it doesn't change the state of the graph object.
     */
   public void fireUnselectEvent(List pGraphObjects)
   {
      if (pGraphObjects != null)
      {
         Iterator < TSGraphObject > iter = pGraphObjects.iterator();
         while (iter.hasNext())
         {
            fireUnselectEvent(iter.next());
         }
      }
   }
   
    /*
     * This method just fires the deslection notification it doesn't change the state of the graph object.
     */
   public void fireUnselectEvent(TSGraphObject pGraphObject)
   {
      IEventPayload payload = getDrawingAreaDispatcher().createPayload("DrawingAreaSelChanged");
      ETList < IPresentationElement > selectedItems = this.getSelected();
      
        /*
            if (pElement != null)
            {
                IPresentationElement pElement = getPresentationElement(pGraphObject);
                IPresentationElement[] unselectedItems = new IPresentationElement[1];
                unselectedItems[0] = pElement;
            }
         */
      //		getDrawingAreaDispatcher().fireUnselect(this.getDiagram(),
      //												selectedItems,
      //												payload);
      
   }
   
   /**
    * ITwoPhaseCommit interface.
    * Take the temp files generated in the precommit and move them into the original
    * files to commit them.
    */
   public void commit()
   {
      if (m_PreCommitFileName.length() > 0)
      {
         String tempFile = m_PreCommitFileName;
         String tempETLFile = StringUtilities.ensureExtension(tempFile, FileExtensions.DIAGRAM_PRECOMMIT_LAYOUT_EXT);

         String tempETLPFile = StringUtilities.ensureExtension(tempFile, FileExtensions.DIAGRAM_PRECOMMIT_PRESENTATION_EXT);
         String realTOMFile = StringUtilities.ensureExtension(tempFile, FileExtensions.DIAGRAM_LAYOUT_EXT);
         String realPRSFile = StringUtilities.ensureExtension(tempFile, FileExtensions.DIAGRAM_PRESENTATION_EXT);
         
         m_PreCommitFileName = "";
         
         // Make sure both of the temp files are there
         File tempETL = new File(tempETLFile);
         File tempETLP = new File(tempETLPFile);
         File realTOM = new File(realTOMFile);
         File realPRS = new File(realPRSFile);
         
         if (tempETL.exists() && tempETLP.exists())
         {
            try
            {
               if (realTOM.exists())
               {
                  realTOM.delete();
               }
               if (realPRS.exists())
               {
                  realPRS.delete();
               }
               boolean rename1 = tempETL.renameTo(realTOM);
               boolean rename2 = tempETLP.renameTo(realPRS);
               if (rename1 && rename2)
               {
                  setIsDirty(false);
                  
                  // Let folks know that the diagram has been saved
                  if (getDrawingAreaDispatcher() != null)
                  {
                     IEventPayload payload = m_drawingAreaDispatcher.createPayload("DrawingAreaPostSave");
                     IProxyDiagram pProxy = getProxyDiagram();
                     if (pProxy != null)
                     {
                        m_drawingAreaDispatcher.fireDrawingAreaPostSave(pProxy, payload);
                     }
                  }
               } else
               {
                  //ASSERT(0 && "could not find commit temporary files");
               }
            } catch (Exception e)
            {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
         } else
         {
            //ASSERT(0 && "could not find commit temporary files");
         }
      }
   }
   
   /**
    * Creates a fullpath filename for this diagram
    *
    * @param sName [out,retval] The full path filename of this diagram.
    */
   private String getFullFileName(String name)
   {
      String retFileName = null;
      // If the user enters a filename then use that as the proposed filename.
      // If the filename is not absolute then we use the project directory
      // as the location of the file.  If, for some reason, the filename argument
      // is 0 then we use the name of the diagram as the name of the file and the
      // workspace location for the directory.
      if (name != null && name.length() > 0)
      {
         String formatStr = null;
         long timeInMillis = System.currentTimeMillis();
         
         // To avoid conflicts between filenames, esp for large groups that are
         // using an SCC to manage their model we append a timestamp to the
         // file name.
         retFileName = name + "_" + timeInMillis;
      } else
      {
         retFileName = m_Name;
      }
      
      // Make sure we have a legal file.  If it is just a name then add the
      // .etld extension and put it in the same spot as the workspace.
      String buffer = retFileName;
      String drive = null;
      int pos = buffer.indexOf(":");
      if (pos >= 0)
      {
         drive = buffer.substring(0, pos);
      }
      
      if (drive == null && m_Namespace != null)
      {
         // Assume we don't have a path and create one from the project directory
         IProject proj = m_Namespace.getProject();
         if (proj != null)
         {
            String fileName = proj.getFileName();
            if (fileName != null && fileName.length() > 0)
            {
               try
               {
                  fileName = (new File(fileName)).getCanonicalPath();
                  int posSlash = fileName.lastIndexOf(File.separator);
                  if (posSlash >= 0)
                  {
                     fileName = fileName.substring(0, posSlash + 1);
                     fileName += buffer;
                     buffer = fileName;
                  }
               }
               catch (Exception e)
               {
               }
            }
         }
      }
      
      if (!buffer.endsWith(FileExtensions.DIAGRAM_LAYOUT_EXT))
      {
         buffer += FileExtensions.DIAGRAM_LAYOUT_EXT;
      }
      
      return buffer;
   }
   
   /**
    * Returns the current product
    *
    * @param pManager [out,retval] Our current product
    */
   public IProduct getProduct()
   {
      return ProductHelper.getProduct();
   }
   
    /*
     * Returns the logical view port rectangle, (ie the inner interactive rect in the overview window).
     */
   public IETRect getLogicalViewPortRect()
   {
      ADGraphWindow graphWindow = this.getGraphWindow();
      if (graphWindow != null)
      {
          return new ETRectEx(graphWindow.getWorldBounds());
      }
      return null;
   }
   
   /**
    * ITwoPhaseCommit interface.  Creates a temporary file in preparation for
    * committing.
    */
   public void preCommit()
   {
      boolean retVal = true;
      if (m_isDirty)
      {
//          getGraph().updateBounds();
          m_nZoomLevelFromArchive = this.getCurrentZoom();
          // Save the viewport center.
          IETRect logicalViewPort = getLogicalViewPortRect();
          
          if (logicalViewPort != null)
          {
              m_CenterFromArchive =logicalViewPort.getCenterPoint();
          }
                    
          boolean proceed = true;
          
          // Let folks know that the diagram is being saved
          if (getDrawingAreaDispatcher() != null)
          {
              IEventPayload payload = m_drawingAreaDispatcher.createPayload("DrawingAreaPreSave");
              IProxyDiagram pProxy = getProxyDiagram();
              if (pProxy != null)
              {
                  m_drawingAreaDispatcher.fireDrawingAreaPreSave(pProxy, payload);
              }
          }
         
         if (proceed)
         {
            m_PreCommitFileName = ""; 
            if (m_FileName.length() > 0)
            {
               // Create the file names for our two diagram files
               String fileName = m_FileName;
               String tempETLDFilename = StringUtilities.ensureExtension(fileName, FileExtensions.DIAGRAM_PRECOMMIT_LAYOUT_EXT);
               String tempETLPFilename = StringUtilities.ensureExtension(fileName, FileExtensions.DIAGRAM_PRECOMMIT_PRESENTATION_EXT);
               
               saveETLDAndETLPFiles(tempETLDFilename, tempETLPFilename);
               m_PreCommitFileName = tempETLDFilename;
               // dirty state should be changed after commit()
//               setIsDirty(false);
            } else
            {
               retVal = false;
            }
         } else
         {
            retVal = false;
         }
      }
    
   }
   
   /**
    * Part of the ITwoPhaseCommit interface.  Is this drawing dirty?
    *
    * @param bIsDirty [out,retval] true if the diagram needs to be saved.
    */
   public boolean isDirty()
   {
      return m_isDirty;
   }
   
   /**
    * Saves associated diagrams and elements list
    *
    * @param pArchive [in] The archive (etlp file) we're saving to
    */
   private void saveAssociatedDiagramsAndElementsList(IProductArchive prodArchive)
   {
      int count = m_AssociatedDiagrams.size();
      for (int i = 0; i < count; i++)
      {
         String name = (String) m_AssociatedDiagrams.elementAt(i);
         prodArchive.insertIntoTable(IProductArchiveDefinitions.ASSOCIATED_DIAGRAMS_STRING, name);
      }
      
      count = m_AssociatedElements.size();
      Iterator iter1 = m_AssociatedElements.keySet().iterator();
      Iterator iter2 = m_AssociatedElements.values().iterator();
      int nKey = 0;
      for (int i = 0; i < count; i++)
      {
         //need to get the key from m_AssociatedElements
         String value = (String) iter1.next();
         
         Object obj = iter2.next();
         if (obj instanceof Vector)
         {
            Vector meidCol = (Vector)obj;
            int meidCount = meidCol.size();

            for (int j=0; j<meidCount; j++)
            {
               String name = (String)meidCol.get(j);
               
               ETPairT<IProductArchiveElement, Integer> val = prodArchive.insertIntoTable(IProductArchiveDefinitions.ASSOCIATED_ELEMENTS_STRING, name);
               nKey = ((Integer)val.getParamTwo()).intValue();
               IProductArchiveElement foundElement = val.getParamOne();
               
               if (foundElement != null)
               {
                  foundElement.addAttributeString(IProductArchiveDefinitions.TOPLEVELID_STRING, value);
               }
            }
         }
      }
   }
   
   /**
    * Save this drawing to the etl and etlp files
    *
    * @param sTOMFilename [in] The etl file to save our TS information to.
    * @param sPRSFilename [in] The etlp file to save our presentation information to.
    * @param pFileCode [out,retval] A code indicating the result of the save operation.
    */
   private void saveETLDAndETLPFiles(String sTOMFilename, String sPRSFilename)
   {
      TSGraphManager pGraphMgr = getCurrentGraphManager();
      
      if (pGraphMgr != null)
      {
         //IProductArchive prodArchive = getProductArchive();
         //new ProductArchiveImpl();
         IProductArchive prodArchive = new ProductArchiveImpl();
         
         // Save the persistent track bar information
         if( getTrackBar() != null )
         {
            getTrackBar().save( prodArchive );
         }
         
         // Tell Tom Sawyer to save
         IProduct prod = getProduct();
         if (prod != null)
         {
            IDiagram dia = getDiagram();
            prod.setSerializingDiagram(dia);
            try
            {
               FileWriter writer = new FileWriter(sTOMFilename);
  
               xmlWriter = new TSEVisualizationXMLWriter(writer);
               xmlWriter.setGraphManager(this.getGraphManager());              
               xmlWriter.setServiceInputData(this.m_allOptionsServiceInputData);
               xmlWriter.setPreferences(this.getGraphWindow().getPreferences()); 
               xmlWriter.setUsingTemplates(false);
               xmlWriter.write();               
               postWriteGMFFile(prodArchive);
               writer.close();
               
            } catch (IOException e)
            {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
            prod.setSerializingDiagram(null);
         }
         
         // Save to the archive file
         writeToArchive(prodArchive);
         
         // Everything is working so save the product archive to file
         if (prodArchive != null)
         {
            boolean saveWorked = false;
            saveWorked = prodArchive.save(sPRSFilename);
         }
      }
   }
   
   /**
    * Saves resource manager data for this drawing area
    *
    * @param pProductArchive [in] Saves the resource manager information (fonts and colors) into the
    * product archive (etlp file).
    */
   private void saveResourceManager(IProductArchive prodArchive)
   {
      ResourceMgr pMgr = ResourceMgr.instance((IDrawingAreaControl)this);
      if (pMgr != null && prodArchive != null)
      {
         pMgr.writeToArchive(prodArchive);
      }
   }
   
   /**
    * Sets the IDiagrams xmlnode's name attribute
    */
   private void setDiagramNodeNameAndOwner()
   {
      IDiagram pDiagram = this.getDiagram();
      
      Node node = pDiagram != null ? pDiagram.getNode() : null;
      if (node != null)
      {
         XMLManip.setAttributeValue(node, "name", m_Name);
      }
      
      if (m_Namespace != null)
      {
         pDiagram.setOwner(m_Namespace);
      }
   }
   
   /**
    * Sets the filename for this drawing area.  If this filename is not in
    * the current workspace then a new element is created in the workspace.
    *
    * @param newVal [in] The new filename
    * @param bNewDiagram [in] Set to true if this is a new diagram.
    */
   public void setFileName(String fileName, boolean newFile)
   {
      if (newFile)
      {
         IProxyDiagramManager diaMan = ProxyDiagramManager.instance();
         String newFileName = null;
         if (m_Namespace != null)
         {
            newFileName = diaMan.verifyUniqueDiagramName(m_Namespace, fileName);
            if (newFileName != null && newFileName.length() > 0)
            {
               m_FileName = newFileName;
            } else
            {
               m_FileName = fileName;
            }
         }
      } else
      {
         m_FileName = fileName;
      }
      
      if (m_FileName != null && m_FileName.length() > 0)
      {
         IDiagram pDiagram = getDiagram();
         IProduct pProd = getProduct();
         if (pProd != null && pDiagram != null)
         {
            // Make sure the diagram is in the workspace
            pProd.addDiagram(pDiagram);
         }
      }
   }
   
   /**
    * Saves the diagram data to the product archive
    *
    * @param pArchive [in] The archive where we need to save presentation information.
    */
   private void writeToArchive(IProductArchive prodArchive)
   {
      if (getGraphWindow() != null)
      {
         // save resource info
         saveResourceManager(prodArchive);
         
         // Save our associated diagrams and elements
         saveAssociatedDiagramsAndElementsList(prodArchive);
         
         // Save this diagrams stuff
         IProductArchiveElement pCreatedElement = prodArchive.createElement(IProductArchiveDefinitions.DIAGRAMINFO_STRING);
         if (pCreatedElement != null)
         {
            if (m_DiagramEngine != null)
            {
               m_DiagramEngine.writeToArchive(prodArchive, pCreatedElement);
            }
            
            String diaKind = getDiagramKind2();
            
            // Add the current zoom
            pCreatedElement.addAttributeDouble(IProductArchiveDefinitions.DIAGRAM_ZOOM, m_nZoomLevelFromArchive);
            
            // Add the current page center
            pCreatedElement.addAttributeDouble(IProductArchiveDefinitions.DIAGRAM_XPOS, m_CenterFromArchive.x);
            pCreatedElement.addAttributeDouble(IProductArchiveDefinitions.DIAGRAM_YPOS, m_CenterFromArchive.y);
            
            // Add the name of the diagram
            pCreatedElement.addAttributeString(IProductArchiveDefinitions.DIAGRAMNAME_STRING, m_Name);
            
            // Add the alias of the diagram
            pCreatedElement.addAttributeString(IProductArchiveDefinitions.DIAGRAMALIAS_STRING, m_Alias);
            
            // Add the documentation of the diagram
            pCreatedElement.addAttributeString(IProductArchiveDefinitions.DIAGRAMNAME_DOCS, m_Documentation);
            
            // Add the kind of the diagram
            pCreatedElement.addAttributeString(IProductArchiveDefinitions.DRAWINGKIND2_STRING, diaKind);
            
            // Save the state of the alias flag
            pCreatedElement.addAttributeBool(IProductArchiveDefinitions.LAST_SHOWALIAS_STATE, ProductHelper.getShowAliasedNames() ? true : false);
            
            // Add the namespace that owns this diagram
            if (m_Namespace != null)
            {
               String xmiid = m_Namespace.getXMIID();
               String topLevelXmiid = m_Namespace.getTopLevelId();
               
               if (xmiid != null && topLevelXmiid != null && xmiid.length() > 0 && topLevelXmiid.length() > 0)
               {
                  pCreatedElement.addAttributeString(IProductArchiveDefinitions.NAMESPACE_TOPLEVELID, topLevelXmiid);
                  pCreatedElement.addAttributeString(IProductArchiveDefinitions.NAMESPACE_MEID, xmiid);
               }
            }
            
            // Add the id of the diagram
            IDiagram dia = getDiagram();
            if (dia != null)
            {
               String diaXmiid = dia.getXMIID();
               if (diaXmiid != null && diaXmiid.length() > 0)
               {
                  pCreatedElement.addAttributeString(IProductArchiveDefinitions.DIAGRAM_XMIID, diaXmiid);
               }
            }
            
            // Save the dll version information
            
            // Now save all the nodes to the product archive
            int count = m_ViewsReadWriteFromETLFile.size();
            for (int i = 0; i < count; i++)
            {
               IETGraphObject obj = (IETGraphObject) m_ViewsReadWriteFromETLFile.elementAt(i);
               obj.save(prodArchive);
            }
            m_ViewsReadWriteFromETLFile.clear();
         }
      }
   }
   
   public TSELineGrid getLineGrid()
   {
      return lineGrid;
   }
   
   public ETDiagramOverviewWindow getOverviewWindow()
   {
      return overviewWindow;
   }
   
   public TSEPointGrid getPointGrid()
   {
      return pointGrid;
   }
   
    /*
     * The State tools call this after a new object has been added to the diagram.
     */
   public void onInteractiveObjCreated(TSEObjectUI ui)
   {
      if (ui instanceof IETGraphObjectUI)
      {
         IETGraphObjectUI graphUI = (IETGraphObjectUI)ui;
         postAddObject(graphUI.getTSObject(), false);
         
         //         IDrawEngine engine = graphUI.getDrawEngine();
         //         if(engine != null)
         //         {
         //            addPresentationElementToTrackBar(engine.getPresentation());
         //         }
      }
   }
   
   public void setEditCompartment(ETCompartment editCtrl)
   {
      if (editCtrl != null)
      {
         m_EditCompartment = new WeakReference( editCtrl );
      }
      else
      {
         m_EditCompartment = null;
      }
   }
   
   private ETCompartment getEditCompartment()
   {
      return (m_EditCompartment != null) ? (ETCompartment)m_EditCompartment.get() : null;
   }
   
   //**************************************************
   // Helper Methods
   //**************************************************
   
   /**
    * Sends a debug message to the message service.  The String may have some
    * embedded strings which are intepreted as follows.
    *
    * %n - Name of the drawing area
    * %f - Filename of the drawing area
    * %F - Temp Filename of the drawing area
    * %p - The temp name of the presentation data
    * %P - The temp name of the presentation data
    * %w - The name of the workspace
    *
    * @param messageType The type of message to send (ie error, debug...)
    * @param message The message string to send.  See the format codes in
    *                the desc of this function.
    */
   protected void sendDebugMessage(String message)
   {
      sendMessage(MsgCoreConstants.MT_DEBUG, message);
   }
   
   /**
    * Sends a debug message to the message service.  The String may have some
    * embedded strings which are intepreted as follows.
    *
    * %n - Name of the drawing area
    * %f - Filename of the drawing area
    * %F - Temp Filename of the drawing area
    * %p - The temp name of the presentation data
    * %P - The temp name of the presentation data
    * %w - The name of the workspace
    *
    * @param messageType The type of message to send (ie error, debug...)
    * @param message The message string to send.  See the format codes in
    *                the desc of this function.
    */
   protected void sendErrorMessage(String message)
   {
      sendMessage(MsgCoreConstants.MT_ERROR, message);
   }
   
   /**
    * Sends a message to the message service.  The String may have some
    * embedded strings which are intepreted as follows.
    *
    * %n - Name of the drawing area
    * %f - Filename of the drawing area
    * %F - Temp Filename of the drawing area
    * %p - The temp name of the presentation data
    * %P - The temp name of the presentation data
    * %w - The name of the workspace
    *
    * @param messageType The type of message to send (ie error, debug...)
    * @param message The message string to send.  See the format codes in
    *                the desc of this function.
    */
   protected void sendMessage(int type, String message)
   {
      String finalMessage = message;
      
      finalMessage.replaceAll("%n", getName());
      finalMessage.replaceAll("%f", FileExtensions.DIAGRAM_LAYOUT_EXT);
      finalMessage.replaceAll("%F", FileExtensions.DIAGRAM_PRECOMMIT_LAYOUT_EXT);
      finalMessage.replaceAll("%p", FileExtensions.DIAGRAM_PRESENTATION_EXT);
      finalMessage.replaceAll("%P", FileExtensions.DIAGRAM_PRECOMMIT_PRESENTATION_EXT);
      
      // Replace the %w in the message with the name of the workspacetoReplace( _T( "%n" ));
      if (finalMessage.indexOf("%w") >= 0)
      {
         IWorkspace ws = ProductHelper.getWorkspace();
         if (ws != null)
         {
            finalMessage.replaceAll("%w", ws.getName());
         }
      }
      
      UMLMessagingHelper helper = new UMLMessagingHelper();
      helper.sendMessage(type, finalMessage);
      
   }
   
   /**
    * @param i
    * @param type
    */
   protected void setMode(int mode, String viewDescription)
   {
      if (getDiagramEngine() != null)
      {
         IDiagramEngine engine = getDiagramEngine();
         
         switch (mode)
         {
            case ADDrawingAreaConstants.CREATE_NODE_DECORATOR_CMD :
               splitNodeViewDescription(viewDescription);
               engine.enterMode2("NODE_DECORATOR", "", "", viewDescription);
               break;
            case ADDrawingAreaConstants.NODE_RESIZE_CMD :
               splitNodeViewDescription(viewDescription);
               engine.enterMode2("NODE_RESIZE", "", "", viewDescription);
               break;
            case ADDrawingAreaConstants.ADD_NODE_CMD :
            {
               splitNodeViewDescription(viewDescription);
               //               int delimiter = viewDescription.indexOf(' ');
               //               if(delimiter >= 0)
               //               {
               //                  String TSTypeName = viewDescription.substring(0, delimiter);
               //                  String uiClass    = viewDescription.substring(delimiter + 1);
               //                  engine.enterMode2("ADD_NODE", "", "", uiClass);
               //               }
               engine.enterMode2("ADD_NODE", "", "", viewDescription);
               break;
            }
            case ADDrawingAreaConstants.ADD_EDGE_CMD :
            {
               splitEdgeViewDescription(viewDescription);
               //               int delimiter = viewDescription.indexOf(' ');
               //               if(delimiter >= 0)
               //               {
               //                  String TSTypeName = viewDescription.substring(0, delimiter);
               //                  String uiClass    = viewDescription.substring(delimiter + 1);
               //                  engine.enterMode2("ADD_EDGE", "", "", uiClass);
               //               }
               engine.enterMode2("ADD_EDGE", "", "", viewDescription);
               break;
            }
         }
      }
   }
   
    /*
     * Returns the Printer class.
     */
   public ADDrawingAreaPrinter getDrawingAreaPrinter()
   {
      if (m_printer == null)
      {
         m_printer = new ADDrawingAreaPrinter(getGraphWindow(), getResources());
      }
      
      return m_printer;
   }
   
   /**
    * Get the current graph view on the control
    */
   protected void initializeTrackBar()

   {
      if (m_DiagramEngine != null)
      {
         m_TrackBar = m_DiagramEngine.initializeTrackBar();
      }
      
      if (m_TrackBar != null)
      {
         //         m_TrackBar.setDiagram(diagram);
         centerPanel.add(m_TrackBar, BorderLayout.NORTH);
      }
   }
   
   protected ETList<IPresentationElement> buildPresentationList(ETList<IETGraphObject> objects)
   {
      ETArrayList < IPresentationElement > retVal = new ETArrayList < IPresentationElement >();
      
      for (Iterator < IETGraphObject > iter = objects.iterator(); iter.hasNext();)
      {
         IETGraphObject curObject = iter.next();
         retVal.add(curObject.getPresentationElement());
         
      }
      
      return retVal;
   }
   
   public void onGraphEvent(int pGraphEventKind, IETPoint pStartPoint, IETPoint pEndPoint, ETList<IETGraphObject> affectedObjects)
   {
      switch (pGraphEventKind)
      {
         case IGraphEventKind.GEK_POST_SELECT :
            // Send the event to the graph
            ((ETGraph)this.getCurrentGraph()).onGraphEvent(pGraphEventKind, pStartPoint, pEndPoint, affectedObjects);
            break;
            
         case IGraphEventKind.GEK_POST_CREATE :
            // Send the event to the graph
            ((ETGraph)this.getCurrentGraph()).onGraphEvent(pGraphEventKind, pStartPoint, pEndPoint, affectedObjects);
            break;
            
         case IGraphEventKind.GEK_PRE_MOVE :
            this.onPreMoveObjects(affectedObjects, pStartPoint, pEndPoint, false);
            break;
            
         case IGraphEventKind.GEK_POST_MOVE :
            this.onPostMoveObjects(affectedObjects, pStartPoint, pEndPoint, false);
            break;
            
         case IGraphEventKind.GEK_PRE_RESIZE :
            handlePreResize(affectedObjects);
            break;
            
         case IGraphEventKind.GEK_POST_RESIZE :
            handlePostResize(affectedObjects);
            break;
            
         case IGraphEventKind.GEK_PRE_LAYOUT :
            handlePreLayout(affectedObjects);
            break;
            
         case IGraphEventKind.GEK_POST_LAYOUT :
            handlePostLayout(affectedObjects);
            break;
            
         case IGraphEventKind.GEK_POST_DELETE :
            handleDeleteObject(affectedObjects);
            break;
      }
   }
   
   protected void handlePreResize(ETList<IETGraphObject> affectedObjects)
   {
      if(getReadOnly())
         return;
      
      JTrackBar bar = getTrackBar();
      IDiagramEngine engine = getDiagramEngine();
      for (Iterator < IETGraphObject > iter = affectedObjects.iterator(); iter.hasNext();)
      {
         boolean handled = false;
         IETGraphObject graphObj = iter.next();
         
         if(bar != null)
         {
            bar.resize(graphObj.getPresentationElement());
         }
         
         if((engine != null) && (graphObj instanceof TSGraphObject))
         {
            handled = engine.onPreResizeObjects((TSGraphObject)graphObj);
         }
         
         if(!handled)
         {
            graphObj.onGraphEvent(IGraphEventKind.GEK_PRE_RESIZE);
         }
      }
   }
   
   protected void handlePreLayout(ETList<IETGraphObject> affectedObjects)
   {
      if(getReadOnly())
         return;
      
      for (Iterator < IETGraphObject > iter = affectedObjects.iterator(); iter.hasNext();)
      {
         IETGraphObject graphObj = iter.next();
         
         graphObj.onGraphEvent(IGraphEventKind.GEK_PRE_LAYOUT);
      }
   }
   
   protected void handlePostLayout(ETList<IETGraphObject> affectedObjects)
   {
      if(getReadOnly())
         return;
      
      for (Iterator < IETGraphObject > iter = affectedObjects.iterator(); iter.hasNext();)
      {
         IETGraphObject graphObj = iter.next();
         
         graphObj.onGraphEvent(IGraphEventKind.GEK_POST_LAYOUT);
      }
   }
   
   
   protected void handlePostResize(ETList<IETGraphObject> affectedObjects)
   {
      if( getReadOnly()== false)
      {
         JTrackBar bar = getTrackBar();
         IDiagramEngine engine = getDiagramEngine();
         for (Iterator < IETGraphObject > iter = affectedObjects.iterator(); iter.hasNext();)
         {
            boolean handled = false;
            IETGraphObject graphObj = iter.next();
            
            if(bar != null)
            {
               bar.resize(graphObj.getPresentationElement());
            }
            
            if((engine != null) && (graphObj instanceof TSGraphObject))
            {
               handled = engine.onPostResizeObjects((TSGraphObject)graphObj);
               
               if(!handled)
               {
                  graphObj.onGraphEvent(IGraphEventKind.GEK_POST_RESIZE);
               }
            }
			setIsDirty(true);			
         }
      }
   }
   
   protected void handleDeleteObject(ETList<IETGraphObject> affectedObjects)
   {
      if(affectedObjects != null)
      {
         // Empty out the clipboard in case this element is sitting in there
         clearClipboard();
         
         for (Iterator < IETGraphObject > iter = affectedObjects.iterator(); iter.hasNext();)
         {
            IETGraphObject curObject = iter.next();
            
            if (curObject instanceof ETNode)
            {
               ETNode curNode = (ETNode)curObject;
               
               // Notify all the connected edges that they are about to be deleted,
               // since TS is not sending those edges
               sendDeleteLinkEvents(curNode.inEdges());
               sendDeleteLinkEvents(curNode.outEdges());
            }
            
            IPresentationElement element = TypeConversions.getPresentationElement(curObject);
            
            if((element != null) && (getTrackBar() != null))
            {
               ((JTrackBar)getTrackBar()).removePresentationElement(element);
            }
         }
         
         setIsDirty(true);         
      }
   }
   
   protected void sendDeleteLinkEvents(List links)
   {
      if(links != null)
      {
         for (Iterator iter = links.iterator(); iter.hasNext();)
         {
            TSObject curEdge = (TSObject)iter.next();
            sendDeleteLinkEvents(curEdge);
         }
      }
   }
   
   protected void sendDeleteLinkEvents(TSObject link)
   {
      if(link != null)
      {
         IPresentationElement element = TypeConversions.getPresentationElement(link);
         if (element instanceof IEdgePresentation)
         {
            IEdgePresentation edgePresentation = (IEdgePresentation)element;
            IETGraphObject edgeObject = TypeConversions.getETGraphObject(edgePresentation);
            
            IDrawEngine fromEngine = TypeConversions.getDrawEngine(edgePresentation.getFromGraphObject());
            IEventManager fromManager = fromEngine.getEventManager();
            if(fromManager != null)
            {
               fromManager.onPreDeleteLink(edgeObject, true);
            }
            
            IDrawEngine toEngine = TypeConversions.getDrawEngine(edgePresentation.getToGraphObject());
            IEventManager toManager = toEngine.getEventManager();
            if(toManager != null)
            {
               toManager.onPreDeleteLink(edgeObject, true);
            }
         }
      }
   }
   
   /**
    * Attaches the TS node to our UML metamodel.  If this is a drag and drop operation then the m_ModelElement will be
    * non-zero.  If this is a drop of a new object then m_ModelElement will be zero.  Depending on the state, Attach or Create
    * is called on the IETElement which this node is implementing.
    */
   public void postAddObject(ITSGraphObject graphObj, boolean resize)
   {
      if (m_ReadOnly)
      {
         postDeletePresentationElement((TSGraphObject)graphObj);
      }
      else
      {
         setIsDirty(true);
         
         // m_bAbortDiagramLoad will be set to TRUE if the
         // diagram is checked out of SCC and needs to be
         // closed and reopened.
         if (m_AbortDiagramLoad)
         {
//            m_isDirty = false;
             setIsDirty(false);
         }
         else if (graphObj != null)
         {
            // We have a bridge being added.  Some bridges create other elements so
            // fire this off to a handler
            ETElementManager mgr = new ETElementManager(this);
            mgr.handlePostAddObject((TSGraphObject)graphObj, false);
            if (m_DiagramEngine != null)
            {
               m_DiagramEngine.postAddObject((TSGraphObject)graphObj);
            }
            
            // This flag is used to tell us when we should fire an event because we went from
            // something selected to nothing selected.  It helps us avoid firing events when the user
            // clicks on the background with nothing selected.
            m_ItemsSelected = true;
            
            // Tell the outside world about the create
            IPresentationElement pPE = TypeConversions.getPresentationElement((TSGraphObject)graphObj);
            if (pPE != null)
            {
               //fireDrawingAreaPresentationElementAction(pPE, PEA_CREATED);
            }
            
            // Make sure the model element is set for the graphObj.
            if (graphObj.getETUI() != null)
            {
               IElement modelElement = pPE != null ? pPE.getFirstSubject() : null;
               
               if (modelElement == null)
                  modelElement = this.getModelElement();
               
               if (modelElement != null)
                  graphObj.getETUI().setModelElement(modelElement);
               
               // Finally init the draw engine.
               IDrawEngine de = graphObj.getETUI().getDrawEngine();
               if (de != null)
               {
                  try
                  {
                     // If we don't call init here, we have problems with the inplace text editor, drop
                     // a class then just start typing.
                     //if (!de.isInitialized())
                     {
                        de.init();
                     }
                     
                     if (graphObj instanceof IETGraphObject)
                     {
                        IETGraphObject etGraphObj = (IETGraphObject)graphObj;
                        etGraphObj.setSynchState(ISynchStateKind.SSK_IN_SYNCH_DEEP);
                     }
                  }
                  catch(Exception e)
                  {
                     e.printStackTrace();
                  }
               }
            }
         }
      }
   }
   
   /**
    * Fired when something happens on a presentation element (ie doubleclick).
    */
   private void fireDrawingAreaPresentationElementAction(IPresentationElement pPE, int nAction)
   {
      if (getDrawingAreaDispatcher() != null)
      {
         IEventPayload payload = m_drawingAreaDispatcher.createPayload("DrawingAreaPresentationElementAction");
         IDiagram pDia = getDiagram();
         if (pDia != null)
         {
            //m_drawingAreaDispatcher.fireDrawingAreaPresentationElementAction(pDia, pPE, nAction, payload);
         }
      }
   }
   
    /*
     *
     */
   public String getNodeInitString()
   {
      return "";//m_NodeInitString;
   }
   
   public String getEdgeInitString()
   {
      return "";//m_EdgeInitString;
   }
   
   /**
    * A key has been typed.  The diagram is marked as dirty.
    * @param e
    */
   public void keyTyped(KeyEvent e)
   {
       if(getReadOnly() == false) {
           if (e.isControlDown() ) {
               if (Character.toUpperCase(e.getKeyChar()) == KeyEvent.VK_S) {
//                   setIsDirty(false);
               }
           }
           else
           {
	       if ( ! (e.isAltDown() || e.isMetaDown())) 
	       {
		   char ch = e.getKeyChar();		   
		   if ((int)ch != 8       // don't need backspace key_typed 
		       && (int)ch != 127  // don't need delete key_typed, see 4904441 for example
		       && (int)ch != 10)  // don't need enter key_typed either
		   {
		       handleCharTyped(e.getKeyChar());
//                       setIsDirty(true);
		   }
	       }
               
           }
      }
   }
   
   public void keyPressed(KeyEvent e)
   {
      boolean handled = false;
      if (getReadOnly() == false)
      {
         //if the key was typed while one of the presentation elements was selected,
         //we want to go in edit mode, if its not an accelerator key
         boolean foundAccelKey = false;
         
         // Accelerator keys don't get propagated down, neither do keys 
         // when an accelerator is depressed
         if (e.isControlDown() || e.isAltDown() || e.isMetaDown())
         {
            foundAccelKey = true;
         }
         
         int keyCode = e.getKeyCode();

// conover - this is not the right way to map keys: see ADDrawingAreaActions
//         // resize node when Alt-Shift-<arrow> is pressed
//         if (isArrowKey(keyCode) && e.isAltDown() && e.isShiftDown()) 
//         {
//             // System.err.println(" shift up/down pressed! ");
//             onHandleResize(keyCode);
//         }
         
         // In order for the NetBeans Edit | Delete menu to be enabled I have
         // to use the NetBeans action mechanism.  So, if I andle the Delete Key 
         // here, I will be trying to delete the node twice.  Therefore, 
         // I am not going to handle the keystroke here.  In the future I will
         // need to make all of the diagram actions NetBeans actions.  That
         // way they will be able to play in the NetBeans accelerator 
         // mechanism.
         // else 
         if ((!foundAccelKey) && (keyCode != KeyEvent.VK_DELETE))
         {
             handled = handleKeyDown(keyCode, e.isShiftDown() ? 0 : 1, false);
         }
         
         //Jyothi: A11y work - multiple selection of nodes on a diagram to enable creation of edge using keyboard 
         // (ctrl Shift +/-) to add to the group or remove from the group of selected nodes.
         else if (e.isControlDown() && e.isShiftDown() && (e.getKeyCode() == KeyEvent.VK_EQUALS)) {
             addToSelectedGraphObjectsGroup(m_graphObj);              
             getGraphWindow().selectGroup(m_selectedNodesGroup, true);
	     handled = true;
         }
         
         else if (e.isControlDown() && e.isShiftDown() && (e.getKeyCode() == KeyEvent.VK_MINUS)) {             
             removeFromSelectedGraphObjectsGroup(m_graphObj);
             getGraphWindow().selectGroup(m_selectedNodesGroup, true);
	     handled = true;
         }
      }
   }
   
   public void keyReleased(KeyEvent e)
   {
       if ((getReadOnly() == false))
       {    
           if (getTrackBar()!= null)
           {
               getTrackBar().onKeyUp(e);
           }
       }
       
       int keyCode = e.getKeyCode();
       if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT ||
           keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN)
       {
           if (e.getModifiersEx() == InputEvent.CTRL_DOWN_MASK)
           {   
               getActions().onPostMoveObjects();
           }
       }
   }
   
   protected void onHandleCancel()
   {
       // exit element creation mode and reset cursor to "selection arrow"
       switchToDefaultState();
       
       // and refresh the palette so that the selected tool gets unselected
       setSelectStateOnPalette();
       
       // reset some members
       if (tempList != null)
       {
           tempList.clear();
       }
       
       m_lastSelectedNode = null;
       m_lastSelectedLabel = null;
       
       clearSelectedNodesGroup();
       
       // Deselect all selected compartments.
       setSelectedCompartments(false);

       // Remove the select state from all objects.
       getGraphWindow().deselectAll(true);
       
       refresh(true);
       // Need to fire a selectedEvent with no selected object to 
       // get the property sheet default to the diagram itselt
       ETList < TSGraphObject > selectedObjects = null;
       this.fireSelectEvent(selectedObjects);
   }
   
   protected void onHandleResize(int resizeCommand)
   {
       double wide = 0D;
       double tall = 0D;
       
       switch (resizeCommand)
       {
           // make it wider
           case ADDrawingAreaConstants.NODE_RESIZE_WIDER_CMD:
               wide = 5D;
               break;
               
           // make it thinner
           case ADDrawingAreaConstants.NODE_RESIZE_THINNER_CMD:
               wide = -5D;
               break;
               
           // make it taller
           case ADDrawingAreaConstants.NODE_RESIZE_TALLER_CMD:
               tall = 5D;
               break;
               
           // make it shorter
           case ADDrawingAreaConstants.NODE_RESIZE_SHORTER_CMD:
               tall = -5D;
               break;
               
           default:
               return;
       }
       
       ETList<IETGraphObject> graphObjects = null;
       graphObjects = getSelectedNodes();
       
       if (graphObjects == null)
           return;
       
       for (IETGraphObject graphObj: graphObjects)
       {
           if (graphObj != null && graphObj instanceof IETNode)
           {
               ETNode etNode = (ETNode)graphObj;
               
               INodePresentation nodePres =
                   (INodePresentation)etNode.getPresentationElement();
               
               TSENode tsNode = nodePres.getTSNode();
               
               // if not change width, then skip setting the width
               if (wide != 0)
               {
                   double curwidth = tsNode.getWidth();
                   
                   // prevent setting width less than 5
                   if ((curwidth + wide) >= 5)
                       tsNode.setWidth(curwidth + wide);

                   // if width is already 5, do nothing
                   else if (curwidth != 5)
                       tsNode.setWidth(5D);
               }
               
               // if not change height, then skip setting the height
               else if (tall != 0)
               {
                   double curheight = tsNode.getHeight();
                   
                   // prevent setting height less than 5
                   if ((curheight + tall) >= 5)
                       tsNode.setHeight(curheight + tall);
                   
                   // if height is already 5, do nothing
                   else if (curheight != 5)
                       tsNode.setHeight(5D);
               }
           }
       }
       
       // need to call refresh here because the drawing are does not refresh
       // properly when the newly sized node is smaller than the original size
       refresh(true);
   }
   
    
   
   /**
    * Listen to the mouse wheel events.  Scrolls the window when the user moves
    * the mouse wheel.  If the user also has the control key pressed then the
    * zoom will be affected.
    */
   public void mouseWheelMoved(MouseWheelEvent e)
   {
      if ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) == InputEvent.ALT_DOWN_MASK)
      {
         // I want the scrolling of the mouse wheel to zoom in and the
         // down scroll to zoom out.
         double newZoomFactor = (-e.getWheelRotation() * 0.1);
         getGraphWindow().zoom(newZoomFactor, true);
      } else
      {
         int unitsToScroll = 1;
         if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL)
         {
            unitsToScroll = e.getUnitsToScroll() * 10;
         } else
         {
         }
         
         getGraphWindow().scrollBy(0, unitsToScroll, true);
      }
      //      int rotation = e.getWheelRotation();
      
   }
   
   //public void finalize()
   //{
   //   	super.finalize();
   // }
   
   //public class GraphChangeListener implements TSEGraphChangeListener
   public class MyGraphChangeListener implements TSGraphChangeListener
   {
       
       public void graphChanged(TSGraphChangeEvent event)
       {
           if( (event.getType() == TSGraphChangeEvent.NODE_REMOVED) ||  (event.getType() == TSGraphChangeEvent.NODE_DISCARDED) )
           {
               ETList < IETGraphObject > deletedObject = new ETArrayList < IETGraphObject > ();
               
               TSGraphChangeEventData data = (TSGraphChangeEventData)event.getData();
               Object obj = data.getSource();
               if(obj instanceof TSObject)
               {
                   deletedObject.add(TypeConversions.getETGraphObject((TSObject)obj));
                   onGraphEvent(IGraphEventKind.GEK_POST_DELETE, null, null, deletedObject);
               }
           }
       }
   }

   public class SelectionChangeListener implements TSESelectionChangeListener {
       public void selectionChanged(TSESelectionChangeEvent event) {
//           Debug.out.println(" ADDrawingAreaControl : SelectionChangeListener class : selectionChanged -- in selectionChanged ");
       }
       
   }
   
   //JM: added this to listen to zoom events 
   public class ViewportChangeListener implements TSEViewportChangeListener {
       public void viewportChanged(TSEViewportChangeEvent event) {
//           Debug.out.println(" !!!!!!!!!!!!ADDrawingAreaControl : ViewportChangeListener class : viewportChanged -- in viewportChanged ");
           if ((event.getType() == TSEViewportChangeEvent.ZOOM) || (event.getType() == TSEViewportChangeEvent.PAN)) {
               JTrackBar bar = (JTrackBar)getTrackBar();
               if(bar != null)
               {
                  bar.onPostScrollZoom();
               }
               fireDrawingAreaPropertyChange(IDrawingAreaPropertyKind.DAPK_ZOOM);
           }
       }       
   }
   
   protected String splitNodeViewDescription(String desc)
   {
      return m_NodeInitializationString.splitViewDescription(desc);
   }
   
   protected String splitEdgeViewDescription(String desc)
   {
      return m_EdgeInitializationString.splitViewDescription(desc);
   }
   
   // IDrawingPropertyProvider
   public ETList<IDrawingProperty> getDrawingProperties()
   {
      ETList <IDrawingProperty> pProperties = null;
      
      IDrawingAreaControl pAxDrawingArea = (IDrawingAreaControl)this;
      IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)this;
      ResourceMgr pMgr = ResourceMgr.instance( pAxDrawingArea );
      if (pMgr != null && pProvider != null)
      {
         pProperties = pMgr.getDrawingProperties(pProvider);
      }
      
      return pProperties;
   }
   

   public void saveColor(String sDrawEngineType, String sResourceName, int nColor)
   {
      IDrawingAreaControl pAxDrawingArea = (IDrawingAreaControl)this;
      IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)this;
      ResourceMgr pMgr = ResourceMgr.instance( pAxDrawingArea );
      if (pMgr != null && pProvider != null)
      {
         setIsDirty(true);
         pMgr.saveColor(sDrawEngineType, sResourceName, nColor);
      }
   }
   
   public void saveColor2(IColorProperty pProperty)
   {
      IDrawingAreaControl pAxDrawingArea = (IDrawingAreaControl)this;
      IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)this;
      ResourceMgr pMgr = ResourceMgr.instance( pAxDrawingArea );
      if (pMgr != null && pProvider != null)
      {
         setIsDirty(true);
         pMgr.saveColor2(pProperty);
      }
   }
   
   public void saveFont(  String sDrawEngineName,
   String sResourceName,
   String sFaceName,
   int nHeight,
   int nWeight,
   boolean bItalic,
   int nColor)
   {
      IDrawingAreaControl pAxDrawingArea = (IDrawingAreaControl)this;
      IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)this;
      ResourceMgr pMgr = ResourceMgr.instance( pAxDrawingArea );
      if (pMgr != null && pProvider != null)
      {
         setIsDirty(true);
         pMgr.saveFont(sDrawEngineName,
         sResourceName,
         sFaceName,
         nHeight,
         nWeight,
         bItalic,
         nColor);
      }
   }
   
   public void saveFont2(IFontProperty pProperty)
   {
      IDrawingAreaControl pAxDrawingArea = (IDrawingAreaControl)this;
      IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)this;
      ResourceMgr pMgr = ResourceMgr.instance( pAxDrawingArea );
      if (pMgr != null && pProvider != null)
      {
         setIsDirty(true);
         pMgr.saveFont2(pProperty);
      }
   }
   
   public void resetToDefaultResource( String sDrawEngineName,
   String sResourceName,
   String sResourceType)
   {
      IDrawingAreaControl pAxDrawingArea = (IDrawingAreaControl)this;
      IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)this;
      ResourceMgr pMgr = ResourceMgr.instance( pAxDrawingArea );
      if (pMgr != null && pProvider != null)
      {
         setIsDirty(true);
         pMgr.resetToDefaultResource( sDrawEngineName, sResourceName, sResourceType);
         refresh(false);
      }
   }
   
   public void resetToDefaultResources()
   {
      IDrawingAreaControl pAxDrawingArea = (IDrawingAreaControl)this;
      IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)this;
      ResourceMgr pMgr = ResourceMgr.instance( pAxDrawingArea );
      if (pMgr != null && pProvider != null)
      {
         setIsDirty(true);
         pMgr.resetToDefaultResources();
         
         ETList<IPresentationElement> pPresentationElements = getAllItems();
         int numItems = 0;
         if (pPresentationElements != null)
         {
            numItems = pPresentationElements.size();
         }
         for (int i = 0 ; i < numItems ; i++)
         {
            IPresentationElement pThisPE = pPresentationElements.get(i);
            IDrawEngine pDrawEngine = TypeConversions.getDrawEngine(pThisPE);
            
            IDrawingPropertyProvider pProvider1 = (IDrawingPropertyProvider)pDrawEngine;
            if (pProvider1 != null)
            {
               pProvider1.resetToDefaultResources();
               
               // Now size to contents if we need to
               if (pThisPE instanceof INodePresentation)
               {
                  INodePresentation pNodePE = (INodePresentation)pThisPE;
                  pNodePE.sizeToContents();
               }
               else
               {
                  // Now size to contents if we need to
                  if (pThisPE instanceof ILabelPresentation)
                  {
                     ILabelPresentation pLabelPE = (ILabelPresentation)pThisPE;
                     //pLabelPE.sizeToContents();
                  }
               }
            }
         }
         
         refresh(false);
      }
   }
   
   public void resetToDefaultResources2(String sDrawEngineName)
   {
      IDrawingAreaControl pAxDrawingArea = (IDrawingAreaControl)this;
      IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)this;
      ResourceMgr pMgr = ResourceMgr.instance( pAxDrawingArea );
      if (pMgr != null && pProvider != null)
      {
         setIsDirty(true);
         pMgr.resetToDefaultResources2(sDrawEngineName);
         
         ETList<IPresentationElement> pPresentationElements = getAllItems();
         int numItems = 0;
         if (pPresentationElements != null)
         {
            numItems = pPresentationElements.size();
         }
         for (int i = 0 ; i < numItems ; i++)
         {
            IPresentationElement pThisPE = pPresentationElements.get(i);
            IDrawEngine pDrawEngine = TypeConversions.getDrawEngine(pThisPE);
            
            IDrawingPropertyProvider pProvider1 = (IDrawingPropertyProvider)pDrawEngine;
            if (pProvider1 != null)
            {
               pProvider1.resetToDefaultResources();
               
               // Now size to contents if we need to
               INodePresentation pNodePE = (INodePresentation)pThisPE;
               if (pNodePE != null)
               {
                  pNodePE.sizeToContents();
               }
            }
         }
         
         refresh(false);
      }
   }
   
   public void dumpToFile(String sFile, boolean bDumpChildren, boolean bAppendToExistingFile)
   {
      IDrawingAreaControl pAxDrawingArea = (IDrawingAreaControl)this;
      IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)this;
      ResourceMgr pMgr = ResourceMgr.instance( pAxDrawingArea );
      if (pMgr != null && pProvider != null)
      {
         setIsDirty(true);
         pMgr.dumpToFile(sFile, bAppendToExistingFile);
         
         if (bDumpChildren)
         {
            ETList<IPresentationElement> pPresentationElements = getAllItems();
            int numItems = 0;
            if (pPresentationElements != null)
            {
               numItems = pPresentationElements.size();
            }
            
            for (int i = 0 ; i < numItems ; i++)
            {
               IPresentationElement pThisPE = pPresentationElements.get(i);
               IProductGraphPresentation pGraphPE = (IProductGraphPresentation)pThisPE;
               if (pGraphPE != null)
               {
                  IDrawEngine pEngine = pGraphPE.getDrawEngine();
                  IDrawingPropertyProvider pProvider1 = (IDrawingPropertyProvider)pEngine;
                  if (pProvider1 != null)
                  {
                     pProvider1.dumpToFile(sFile, true, true);
                  }
               }
            }
         }
      }
   }
   
   public boolean displayFontDialog(IFontProperty pProperty)
   {
      IDrawingAreaControl pAxDrawingArea = (IDrawingAreaControl)this;
      IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)this;
      ResourceMgr pMgr = ResourceMgr.instance( pAxDrawingArea );
      if (pMgr != null && pProvider != null)
      {
         setIsDirty(true);
         return pMgr.displayFontDialog(pProperty);
      }
      else
      {
         return false;
      }
   }
   
   public boolean displayColorDialog(IColorProperty pProperty)
   {
      IDrawingAreaControl pAxDrawingArea = (IDrawingAreaControl)this;
      IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)this;
      ResourceMgr pMgr = ResourceMgr.instance( pAxDrawingArea );
      if (pMgr != null && pProvider != null)
      {
         setIsDirty(true);
         return pMgr.displayColorDialog(pProperty);
      }
      else
      {
         return false;
      }
   }
   
   public void invalidateProvider()
   {
      refresh(true);
   }
   
    /*
     * Executes the Relationship Disovery Command.
     */
   public void executeRelationshipDiscovery()
   {
      ISimpleElementsAction action = new SimpleElementsAction();
      if (action != null)
      {
         ETList < IElement > pAllFirstSubjects = getSelected4();
         action.setKind(ISimpleElementsAction.SEAK.DISCOVER_RELATIONSHIPS);
         action.setElements(pAllFirstSubjects);
         postDelayedAction(action);
      }
   }
   
   public JToolBar getToolbar()
   {
      createToolbars();
      return mainToolBar;
   }
   
   public void setShowDefaultToolbar(boolean bShow)
   {
      m_showToolbars = bShow;
   }

   /**
    * Fired after a paste operation occurs
    */
   public void onPostPaste(TSGraphObject pastedToObject, List nodeList, List edgeList, List nodeLabelList, List edgeLabelList, boolean handled)
   {
      ETElementManager manager = new ETElementManager(this);
      manager.onPostPaste(nodeList, edgeList, nodeLabelList, edgeLabelList);
      
      setIsDirty(true);
      //clear the last selected node after paste to continue tab-traversal
      m_lastSelectedNode = null;
      m_lastSelectedLabel = null;
   }
   
    /*
     *
     * @author KevinM
     *
     * Listens for visibility changes on this component, it hides or display???s the secondary windows.
     *
     */
   protected class DrawingVisibilityListener implements ComponentListener
   {
      class SecondaryWindowVisibility extends Object
      {
         public boolean overviewVisible;
         public boolean drawingPropertiesVisible;
         public boolean layoutPropertiesVisible;
      };
      
      
      /**
       * Invoked when the component's size changes.
       */
      public void componentResized(ComponentEvent e)
      {
      }
      
      /**
       * Invoked when the component's position changes.
       */
      public void componentMoved(ComponentEvent e)
      {
         
      }
      
      /**
       * Invoked when the component has been made visible.
       */
      public void componentShown(ComponentEvent e)
      {
         ETSystem.out.println("componentShown");
         restoreSecondaryWindows();
      }
      
      /**
       * Invoked when the component has been made invisible.
       */
      public void componentHidden(ComponentEvent e)
      {
         ETSystem.out.println("componentHidden");
         populateVisibityData();
         closeAllSecondaryWindows();
      }
      
         /*
          * Saves the window state for each Secondary window.
          */
      protected void populateVisibityData()
      {
         if (visibilityData == null)
         {
            visibilityData = new SecondaryWindowVisibility();
            visibilityData.overviewVisible = isOverviewWindowOpen();
            visibilityData.drawingPropertiesVisible = getIsGraphPreferencesDialogOpen();
            visibilityData.layoutPropertiesVisible = getIsLayoutPropertiesDialogOpen();
         }
      }
      
         /*
          * Restores the secondary windows if they were visible before this component was hidden.
          */
      protected void restoreSecondaryWindows()
      {
         if (visibilityData != null)
         {
            overviewWindow(visibilityData.overviewVisible);
            graphPreferencesDialog(visibilityData.drawingPropertiesVisible);
            layoutPropertiesDialog(visibilityData.layoutPropertiesVisible);
            visibilityData = null;
         }
      }
      
      protected SecondaryWindowVisibility visibilityData = null;
   };
   
    /*
     * Registers for ComponentListener events.
     */
   public void addComponetListeners()
   {
      this.addComponentListener(new DrawingVisibilityListener());
   }
   
   /**
    * Fired before any nodes,edges or labels are moved
    *
    * This routine is responsible for notifying the objects that are about to be moved about that fact.  The
    * notification comes through the IETElement interface.
    */
   protected void onPreMoveObjects(ETList<IETGraphObject> affectedObjects, IETPoint pStartPoint, IETPoint pEndPoint, boolean handled)
   {
      // Return if we're blocked
      if (UserInputBlocker.getIsDisabled(GBK.DIAGRAM_MOVEMENT) && handled)
      {
         handled = true;
         return;
      }
      
      if (this.m_ReadOnly)
      {
         handled = true;
         return;
      }
      
      // In case the object is moved by keyboard, there are no mouse points.
      // In that case, x interval and y interval are set to zero.
      int xInterval = 0;
      int yInterval = 0;
      
      if (pStartPoint!= null &&  pEndPoint != null)
      {
          xInterval = pEndPoint.getX() - pStartPoint.getX();
          yInterval = pEndPoint.getY() - pStartPoint.getY();
      }
      
      // Notify the engine
      if (m_DiagramEngine != null)
      {
         m_DiagramEngine.onPreMoveObjects(affectedObjects, xInterval, yInterval);
      }
      
      if (handled == false)
      {
         ETElementManager manager = new ETElementManager(this);
         
         ETList <IPresentationElement> pTempSelectedList = new ETArrayList <IPresentationElement> ();
         
         pTempSelectedList = GetHelper.addToPresentationElements(affectedObjects, pTempSelectedList);
         manager.onPreMove(pTempSelectedList);
      }
   }
   
   /**
    * Fired after any nodes, edges, or labels are moved
    *
    * This routine is responsible for two things.
    * 1.  Notify the track bar, if there is one about the move so it can update the cars.
    * 2.  Notify the objects being moved that is is about to be moved.  Do this through the IETElement interfaces.
    *
    * @param nodeList [in] A list of nodes that were moved
    * @param edgeList [in] A list of edges that were moved
    * @param labelList [in] A list of labels that were moved
    * @param dx [in] The x location of the cursor
    * @param dy [in] The y location of the cursor
    * @param handled [out] Set to VARIANT_TRUE to cancel the move
    */
   protected void  onPostMoveObjects(ETList<IETGraphObject> affectedObjects, IETPoint pStartPoint, IETPoint pEndPoint, boolean handled)
   {
       
       ETList < IPresentationElement > pTempSelectedList = new ETArrayList <IPresentationElement> ();
       pTempSelectedList = GetHelper.addToPresentationElements(affectedObjects, pTempSelectedList);
       
       // First tell the trackbar that stuff has moved
       if (getTrackBar() != null && pStartPoint != null && pEndPoint != null)
       {
           getTrackBar().moveObjects(buildPresentationList(affectedObjects), pEndPoint.getX() - pStartPoint.getX());
       }
       if (handled == false)
       {
           // In case the object is moved by keyboard, there are no mouse points.
           // In that case, x interval and y interval are set to zero.
           int xInterval = 0;
           int yInterval = 0;
           
           if (pStartPoint != null && pEndPoint != null)
           {
               xInterval = pEndPoint.getX() - pStartPoint.getX();
               yInterval = pEndPoint.getY() - pStartPoint.getY();
           }
           
           // Originally I tried to send the product element the OnPostResize event here.  But
           // if the product element does anything with connectors (ie deleting them) then
           // TS crashed.  So I figure this is a bad place to be making those changes.  Instead
           // we post event to handle it in a better spot.
           
           PostMoveDetails pDetails = new PostMoveDetails(pTempSelectedList, xInterval, yInterval);
           this.onHandlePostMove(pDetails);
       }
       setIsDirty(true);
   }
   
   
   /**
    * Originally I tried to send the product element the OnPostMove event from TS.  But
    * if the product element does anything with connectors (ie deleting them) then
    * TS crashed.  So I figure this is a bad place to be making those changes.  Instead
    * we post event to handle it in a better spot.  This routine handles the post message.
    */
   protected void onHandlePostMove(PostMoveDetails pDetails)
   {
      if (pDetails != null)
      {
         ETElementManager manager = new ETElementManager(this);
         
         manager.onPostMove(pDetails.m_PresentationElements);
         
         // Notify the engine
         if (m_DiagramEngine != null)
         {
            m_DiagramEngine.delayedPostMoveObjects(pDetails.m_PresentationElements, pDetails.m_dx, pDetails.m_dy);
         }
         
         //delete pDetails;
      }
      
   }
   
   // This interior class is used to store presentation elements that have been moved.  During
   // post move TS has not yet layed out the edges so any edge calculations are wrong!  We delay
   // sending out postmoves to our IETElements through this class and a windows message
   private class PostMoveDetails
   {
      ETList<IPresentationElement> m_PresentationElements = null;
      int m_dx;
      int m_dy;
      
      public PostMoveDetails(ETList<IPresentationElement> pPresentationElements, int dx, int dy)
      {
         m_PresentationElements = pPresentationElements;
         m_dx = dx;
         m_dy = dy;
      }
   }
   
   /**
    * Returns true if any of the TS objects in the input collection are associated with the input class type
    */
   protected boolean containsModelElement( Collection list, Class interfacetype)
   {
      boolean containsModelElement = false;
      
      if( list.size() > 0 )
      {
         for (Iterator iter = list.iterator(); iter.hasNext();)
         {
            TSObject object = (TSObject)iter.next();
            
            IElement element = TypeConversions.getElement( object );
            if (element != null && interfacetype.isAssignableFrom(element.getClass()))
            {
               containsModelElement = true;
               break;
            }
         }
      }
      
      return containsModelElement;
   }
   
   /**
    * Adds any message labels associated with message connector edges
    */
   protected void selectMessageConnectorMessages(ETList<TSEEdge> redgeVector, ETList<TSEEdgeLabel> redgeLabelVector)
   {
      if( redgeVector.size() > 0 )
      {
         for (int i = 0; i < redgeVector.size(); i++)
         {
            IElement element = TypeConversions.getElement(redgeVector.get(i));
            if (element instanceof IMessageConnector)
            {
               ETEdge edge = (ETEdge)redgeVector.get(i);
               ETList<IETLabel> pLabelList = edge.getLabels();
               if(pLabelList != null)
               {
                  for(int index = 0; index < pLabelList.size(); index++ )
                  {
                     TSEEdgeLabel pLabel = (TSEEdgeLabel)(pLabelList.get(index));
                     if(pLabel != null)
                     {
                        redgeLabelVector.add(pLabel);
                     }
                  }
               }
            }
         }
         
         // Fix W7815:  Remove any duplicate edge labels, because the edge's label may also have been selected
         for (int j = redgeLabelVector.size() - 1; j >= 0; j--)
         {
            TSEEdgeLabel pLabel = redgeLabelVector.get(j);
            for (int k = 0; k < j; k ++)
            {
               if(redgeLabelVector.get(j).equals(pLabel))
               {
                  redgeLabelVector.remove(pLabel);
                  break;
               }
            }
         }
      }
   }
   
    /*
     * Set when the diagrma is creating itself from selected elements.
     */
   public void setPopulating(boolean busy)
   {
      populating = busy;
   }
   
    /*
     * Returns if the is busy populating
     */
   public boolean getPopulating()
   {
      return populating || !getGraphWindow().getAllowRedraw();
   }
   
   /**
    * Adds any message edges contained inside any of the nodes that are combined fragments
    */
   protected void selectCombinedFragmentMessages( ETArrayList<TSENode> nodes )
   {
      if( nodes.size() > 0 )
      {
         for (Iterator iter = nodes.iterator(); iter.hasNext();)
         {
            TSENode tseNode = (TSENode)iter.next();
            
            IElement element = TypeConversions.getElement( tseNode );
            if (element instanceof ICombinedFragment)
            {
               ICombinedFragment combinedFragment = (ICombinedFragment)element;
               
               IDrawEngine drawEngine = TypeConversions.getDrawEngine( tseNode );
               if ( drawEngine != null )
               {
                  IETRect etRect = drawEngine.getLogicalBoundingRect( false );
                  if( etRect != null )
                  {
                     ETList< IPresentationElement > pes = getAllEdgesViaRect( etRect, false );
                     if( pes != null )
                     {
                        for (Iterator iterator = pes.iterator(); iterator.hasNext();)
                        {
                           IPresentationElement pe = (IPresentationElement)iterator.next();
                           
                           IElement innerElement = TypeConversions.getElement( pe );
                           if (innerElement instanceof IMessage)
                           {
                              IMessage message = (IMessage)innerElement;
                              
                              // We can't simply select the edges,
                              // because the lifeline pieces don't get deleted
                              message.delete();
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }
   
   /**
    * SCC notification.  We use this to see if this diagram got checked in/out
    *
    * @param kind [in] The kind of action that was performed (ie FK_CHECK_OUT)
    * @param files [in] A list of files that were acted upon.
    */
   public void onFeatureExecuted(int kind, ISCMItemGroup files)
   {
      String originalFilename = m_FileName;
      String tempETLFilename = FileSysManip.ensureExtension(originalFilename, FileExtensions.DIAGRAM_LAYOUT_EXT);
      String tempETLPFilename = FileSysManip.ensureExtension(originalFilename, FileExtensions.DIAGRAM_PRESENTATION_EXT);
      
      if(kind == SCMFeatureKind.FK_GET_LATEST_VERSION ||
         kind == SCMFeatureKind.FK_CHECK_IN ||
         kind == SCMFeatureKind.FK_CHECK_OUT ||
         kind == SCMFeatureKind.FK_UNDO_CHECK_OUT ||
         kind == SCMFeatureKind.FK_ADD_TO_SOURCE_CONTROL)
      {
         boolean thisDiagramWasAffected = false;
         int count = files.getCount();
         for(int index = 0; index < count; index++)
         {
            ISCMItem thisItem = files.item(index);
            if(thisItem instanceof ISCMDiagramItem)
            {
               ISCMDiagramItem diagramItem = (ISCMDiagramItem)thisItem;
               String layoutFile = diagramItem.getLayoutFile();
               
               if((layoutFile != null) && (layoutFile.length() > 0))
               {
                  if((layoutFile.equalsIgnoreCase(tempETLFilename) == true) ||
                     (layoutFile.equalsIgnoreCase(tempETLPFilename) == true))
                  {
                     thisDiagramWasAffected = true;
                  }
               }
            }
         }
         
         if(thisDiagramWasAffected == true)
         {
            recalculateCRCs();
         }
      }
   }
   
   /**
    * Re-establishes the link between the model element and presentation elements.  This is called
    * in response to a ExternalElementLoaded event where SCC moves the IElement from one DOM to another,
    * in doing so it deletes all the presentation element data from the IElement.  We need to go through
    * our presentation elements and reattach them.
    *
    * @param pVerElement  The element whose presentation elements should be re-established.
    */
   public void reestablishPresentationElementOwnership(IVersionableElement pVerElement)
   {
      if(pVerElement != null)
      {
         ISimpleElementsAction pAction = new SimpleElementsAction();
         if(pVerElement instanceof IElement)
         {
            pAction.add((IElement)pVerElement);
            pAction.setKind(SEAK.RECONNECT_PRESENTATION_ELEMENTS);
            postDelayedAction(pAction);
         }
      }
   } 
   
   private IDrawingAreaSelectStateEnteredSink drawingAreaSelectStateEnteredSink;
   
   /** This method removes any selections on the palette.
    *  This is used when the user right clicks on the canvas 
    *  or press the ESC key or selects a button on the toolbar 
    *  which sets the state of the graph window to select
    */
   public void setSelectStateOnPalette() {
         if(drawingAreaSelectStateEnteredSink != null) {
             drawingAreaSelectStateEnteredSink.onSelectToolSelected();
             //reset the selected button
             this.setSelectedPaletteButton(null);
         }
   }
   
   public void addDrawingAreaToolSelectionSink(IDrawingAreaSelectStateEnteredSink listener) {
       this.drawingAreaSelectStateEnteredSink = listener;
   }
   
   public IDrawingAreaSelectStateEnteredSink getDrawingAreaSelectStateEnteredSink () {
       return drawingAreaSelectStateEnteredSink;
   }
   
   //JM:
   /**
    * This method returns the service input data associated with the
    * specified graph manager of the graph window.
    */
   public TSEAllOptionsServiceInputData getServiceInputData(
           TSEGraphWindow graphWindow) {
       TSEAllOptionsServiceInputData result = null;
       
       if (graphWindow != null) {
           result = this.getServiceInputData(graphWindow.getGraphManager());
       }
       
       return result;
   }
   
   /**
    * This method returns the service input data associated with the
    * specified graph manager.
    */
   public TSEAllOptionsServiceInputData getServiceInputData(
           TSEGraphManager graphManager) {
       TSEAllOptionsServiceInputData result = null;
       
       if (this.getGraphWindow() != null) {
           if (this.serviceInputDataTable.containsKey(graphManager)) {
               result = (TSEAllOptionsServiceInputData)
               this.serviceInputDataTable.get(graphManager);
           } else {
               result = new TSEAllOptionsServiceInputData(graphManager);
               this.serviceInputDataTable.put(graphManager, result);
               TSLayoutInputTailor tailor = new TSLayoutInputTailor(result);
               tailor.setGraphManager(graphManager);
           }
       }
       
       return result;
   }
   
   private void addToSelectedGraphObjectsGroup(IETGraphObject obj) {
       if ((obj != null) && (m_selectedNodesGroup!=null) && (!m_selectedNodesGroup.contains(obj))) {
           m_selectedNodesGroup.add(obj);
       }
   }
   
   private void removeFromSelectedGraphObjectsGroup(IETGraphObject obj) {
       if ((obj != null)&& (m_selectedNodesGroup.contains(obj))) {
           m_selectedNodesGroup.remove(obj);
       }
   }
   
   public List getSelectedNodesGroup() {       
       return m_selectedNodesGroup;
   }
   
   private void clearSelectedNodesGroup() {
       m_selectedNodesGroup.clear();
   }
   
   private void printSelectedNodesGroup() {
       if ((m_selectedNodesGroup!=null) ) {
       System.err.println(" m_selectedNodesGroup size = "+m_selectedNodesGroup.size());
       }
       System.err.println("group = "+m_selectedNodesGroup);
   }
      
   
//   public IETPoint getCenterPoint()
//   {
//        return new ETPoint(
//            (int)getLogicalViewPortRect().getCenterX(), 
//            (int)getLogicalViewPortRect().getCenterY());
//   }

   
//    public ArrayList<ETNode> addNodeToCenter(ArrayList<IElement> elements)
//    {
//        if (elements == null || elements.size() == 0)
//            return null;
//        
//        ETNode addedNode = null;
//        double nudge = 0D;
//        final double nudgeBy = 25;
//        ArrayList<ETNode> addedNodes = new ArrayList<ETNode>(elements.size());
//        
//        for (IElement element: elements)
//        {
//            addedNode = addNodeToCenter(element);
//            
//            if (addedNode == null)
//                continue;
//            
//            addedNode.setCenterX(addedNode.getCenterX() + nudge);
//            addedNode.setCenterY(addedNode.getCenterY() - nudge);
//            
//            addedNodes.add(addedNode);
//            
//            nudge+= nudgeBy;
//        }
//
//        invalidate();
//        refresh(true);
//        return addedNodes;
//    }

                        
    public List<IETGraphObject> addNodeToCenter(List<IElement> elements)
    {
        if (elements == null || elements.size() == 0)
            return null;
        
        List<IETGraphObject> addedNodes = 
            new ArrayList<IETGraphObject>(elements.size());
        
        ADCoreEngine coreEngine = null;
        
        if (getDiagramEngine() instanceof ADCoreEngine)
            coreEngine = (ADCoreEngine)getDiagramEngine();
        
        for (IElement element: elements)
        {
            
            // Fix for issue # 89538.  The element needs to be proceesed before being added.
            IElement processedElement = processOnDropElement(element);
            if(processedElement == null)
            {
                continue;
            }
            
            IETPoint pCenterPoint = coreEngine.getLogicalCenter();
            if (pCenterPoint != null)
            {
                int x = pCenterPoint.getX();
                int y = pCenterPoint.getY();
                int xOffset = coreEngine.getAcceleratorOffset().x;
                int yOffset = coreEngine.getAcceleratorOffset().y;
                
                pCenterPoint.setPoints(x + xOffset, y + yOffset);
                
                try
                {
                    ETNode pCreatedNode = addNode(processedElement, pCenterPoint, true);
                    
                    if (pCreatedNode != null)
                    {
                        pCreatedNode.sizeToContents();
                        addedNodes.add((IETGraphObject)pCreatedNode);
                    }
                    setModelElement(null);
                }
                
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                
                // Increment the accelerator offset in case the
                //  user hits the accel key again
                coreEngine.getAcceleratorOffset()
                .setLocation(xOffset + 20, yOffset - 20);
            }
        }

        invalidate();
        refresh(true);
        return addedNodes;
    }

   public final static int ALIGN_LEFT = 0;
   public final static int ALIGN_HCENTER = 1;
   public final static int ALIGN_RIGHT = 2;
   public final static int ALIGN_TOP = 3;
   public final static int ALIGN_VCENTER = 4;
   public final static int ALIGN_BOTTOM = 5;

   public boolean alignLeft()
   {
       return align(ALIGN_LEFT);
   }
   
    
    public boolean alignHorizontalCenter() 
    {
       return align(ALIGN_HCENTER);
    }

    public boolean alignRight() 
    {
       return align(ALIGN_RIGHT);
    }

    public boolean alignTop() 
    {
       return align(ALIGN_TOP);
    }

    public boolean alignVerticalCenter() 
    {
       return align(ALIGN_VCENTER);
    }

    public boolean alignBottom() 
    {
       return align(ALIGN_BOTTOM);
    }
    
    
    private boolean align(int alignHow)
    {
       ETList<IPresentationElement> presElements = null;
       presElements = getSelectedPresentionNodes();
       
       if (presElements == null || presElements.size() < 2)
           return false;

       // get the first selected node to align to
       TSENode anchorNode = null;
       for (IPresentationElement presElem: presElements)
       {
           if (presElem != null && presElem instanceof INodePresentation)
           {
               
               break; // we just need the first selected presentation node
           }
       }

       // loop through selected elements and align them
       for (IPresentationElement presElem: presElements)
       {
           if (presElem != null && presElem instanceof INodePresentation)
           {
               INodePresentation nodePres = (INodePresentation)presElem;
               TSENode tsNode = nodePres.getTSNode();

               if (anchorNode == null)
               {
                   anchorNode = tsNode;
                   continue; // this is the node to align to
               }

               nodePres.invalidate();
               tsNode.setCenter(calculateNewCenter(
                   anchorNode, tsNode, alignHow));
                nodePres.invalidate();
           } // if instanceof INodePresentation
       } // for
       
       // need to call refresh here because the drawing area does not refresh
       // properly when elements are moved
       refresh(true);
       
       requestFocus();
       return true;
    }

    private TSConstPoint calculateNewCenter(
        TSENode anchorNode, TSENode alignNode, int alignHow)
    {
        switch(alignHow)
        {
            case ALIGN_LEFT:
                return new TSConstPoint(
                    anchorNode.getCenterX() - 
                        ((anchorNode.getWidth() - alignNode.getWidth()) / 2), 
                    alignNode.getCenterY());
                
            case ALIGN_HCENTER:
                return new TSConstPoint(
                    anchorNode.getCenterX(), 
                    alignNode.getCenterY());
                
            case ALIGN_RIGHT:
                return new TSConstPoint(
                    anchorNode.getCenterX() + 
                        ((anchorNode.getWidth() - alignNode.getWidth()) / 2), 
                    alignNode.getCenterY());
                
            case ALIGN_TOP:
                return new TSConstPoint(
                    alignNode.getCenterX(),
                    anchorNode.getCenterY() +
                        ((anchorNode.getHeight() - alignNode.getHeight()) / 2));
                
            case ALIGN_VCENTER:
                return new TSConstPoint(
                    alignNode.getCenterX(), 
                    anchorNode.getCenterY());
                
            case ALIGN_BOTTOM:
                return new TSConstPoint(
                    alignNode.getCenterX(),
                    anchorNode.getCenterY() -
                        ((anchorNode.getHeight() - alignNode.getHeight()) / 2));
                
        }
        
        // if all else fails, return the same center so it doesn't move at all
        // should never reach here, though
        return alignNode.getCenter();
    }

    public final static int DISTRIBUTE_LEFT_EDGE = 0;
    public final static int DISTRIBUTE_HCENTER = 1;
    public final static int DISTRIBUTE_RIGHT_EDGE = 2;
    public final static int DISTRIBUTE_TOP_EDGE = 3;
    public final static int DISTRIBUTE_VCENTER = 4;
    public final static int DISTRIBUTE_BOTTOM_EDGE = 5;

    public boolean distributeLeftEdge()
    {
        return distribute(DISTRIBUTE_LEFT_EDGE);
    }

    public boolean distributeHorizontalCenter()
    {
        return distribute(DISTRIBUTE_HCENTER);
    }
    
    public boolean distributeRightEdge()
    {
        return distribute(DISTRIBUTE_RIGHT_EDGE);
    }

    public boolean distributeTopEdge()
    {
        return distribute(DISTRIBUTE_TOP_EDGE);
    }

    public boolean distributeVerticalCenter()
    {
        return distribute(DISTRIBUTE_VCENTER);
    }
    
    public boolean distributeBottomEdge()
    {
        return distribute(DISTRIBUTE_BOTTOM_EDGE);
    }

    
    private boolean distribute(int distributeHow)
    {
       ETList<IPresentationElement> presElements = null;
       presElements = getSelectedPresentionNodes();
       
       if (presElements == null || presElements.size() < 3)
           return false;

       List<Double> sortedCoords = new ArrayList<Double>(presElements.size());
       List<INodePresentation> distributeThese = 
           new ArrayList<INodePresentation>(presElements.size());

       // loop through selected elements and align them
       for (IPresentationElement presElem: presElements)
       {
           if (presElem != null && presElem instanceof INodePresentation)
           {
               INodePresentation nodePres = (INodePresentation)presElem;
               TSENode tsNode = nodePres.getTSNode();
               
               double coord = getDistributeCoord(distributeHow, tsNode);
               int i = 0;

               // put the X coords in sorted order, at the same time
               // use that sorting to sort the distributeThese list
               // in the same order
               for (Double curX: sortedCoords)
               {
                   if (coord < coord)
                       break;

                   i++;
               }

               sortedCoords.add(i, coord);
               distributeThese.add(i, nodePres);
           } // if instanceof INodePresentation
       } // for

       // after filtering out non-nodes, do we still have 
       // 3 nodes to do distribution
       if (sortedCoords.size() < 3)
           return false;
       
       // calculate the width of the two outermost node centers
       double spread = 
           sortedCoords.get(0) - sortedCoords.get(distributeThese.size()-1);

       // calculate the inteval distance to distribute the nodes
       double intervalDist = Math.abs(spread / (distributeThese.size() - 1));
       
       // move node 2 through n-1 to their new distributed intervals
       // node 1 and n are already in the right location
       double baseCoord = sortedCoords.get(0);
       
       for (int j=1; j < distributeThese.size()-1; j++)
       {
           distributeNode(
               distributeHow, 
               distributeThese.get(j), 
               (baseCoord + j * intervalDist));
       }

       // need to call refresh here because the drawing area does not refresh
       // properly when elements are moved
       refresh(true);
       
       requestFocus();
       return true;
    }
    
    private double getDistributeCoord(int distribType, TSENode node)
    {
        switch(distribType)
        {
            case DISTRIBUTE_LEFT_EDGE:
                return node.getCenterX() - node.getWidth()/2;
                
            case DISTRIBUTE_HCENTER:
                return node.getCenterX();
                
            case DISTRIBUTE_RIGHT_EDGE:
                return node.getCenterX() + node.getWidth()/2;
        
            case DISTRIBUTE_TOP_EDGE:
                return node.getCenterY() - node.getHeight()/2;

            case DISTRIBUTE_VCENTER:
                return node.getCenterY();

            case DISTRIBUTE_BOTTOM_EDGE:
                return node.getCenterY() + node.getHeight()/2;
        }
        
        return 0D;
    }
    
    private void distributeNode(
        int distribType, INodePresentation node, double distributeTo)
    {
        node.invalidate();
        
        switch(distribType)
        {
            case DISTRIBUTE_LEFT_EDGE:
                node.getTSNode().setCenterX(distributeTo + (node.getWidth()/2));
                break;
                
            case DISTRIBUTE_HCENTER:
                node.getTSNode().setCenterX(distributeTo);
                break;
                
            case DISTRIBUTE_RIGHT_EDGE:
                node.getTSNode().setCenterX(distributeTo - (node.getWidth()/2));
                break;
        
            case DISTRIBUTE_TOP_EDGE:
                node.getTSNode().setCenterY(distributeTo + (node.getHeight()/2));
                break;

            case DISTRIBUTE_VCENTER:
                node.getTSNode().setCenterY(distributeTo);
                break;

            case DISTRIBUTE_BOTTOM_EDGE:
                node.getTSNode().setCenterY(distributeTo - (node.getHeight()/2));
                break;
        }
        
        node.invalidate();
    }

}
