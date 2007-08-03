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
 *
 */
package org.netbeans.modules.vmd.midp.converter.wizard;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.vmd.api.flow.visual.FlowNodeDescriptor;
import org.netbeans.modules.vmd.api.flow.visual.FlowScene;
import org.netbeans.modules.vmd.api.io.providers.DocumentSerializer;
import org.netbeans.modules.vmd.api.io.providers.IOSupport;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.categories.PointsCategoryCD;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.components.points.MobileDeviceCD;
import static org.netbeans.modules.vmd.midp.converter.wizard.ConverterUtil.getBoolean;
import org.netbeans.modules.mobility.project.J2MEProjectGenerator;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.w3c.dom.Node;

import javax.lang.model.element.TypeElement;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author David Kaspar
 */
public class Converter {

    public static ArrayList<String> convert (final FileObject inputJavaFile, final FileObject inputDesignFile, String outputFileName) {
        final ArrayList<String> errors = new ArrayList<String> ();
        try {
            DataFolder folder = DataFolder.findFolder (inputJavaFile.getParent ());

            DataObject input = DataObject.find (inputJavaFile);
            EditorCookie editorCookie = input.getCookie (EditorCookie.class);
            final StyledDocument styledDocument = editorCookie.openDocument ();

            final Node rootNode = XMLUtil.getRootNode (inputDesignFile);
            if (! "1.3".equals (XMLUtil.getAttributeValue (rootNode, "version"))) { // NOI18N
                Debug.warning ("Unsupported version of the design file. The design has to saved in NetBeans 5.5 or newer."); // NOI18N
                return errors;
            }
            final List<ConverterItem> items = getConverterItems (rootNode);

            DataObject template = DataObject.find (Repository.getDefault ().getDefaultFileSystem ().findResource ("Templates/MIDP/ConverterVisualMIDlet.java")); // NOI18N
            final DataObject outputDesign = template.createFromTemplate (folder, outputFileName);
            DocumentSerializer serializer = IOSupport.getDocumentSerializer (outputDesign);
            serializer.waitDocumentLoaded ();
            final DesignDocument document = serializer.getDocument ();

            EditorCookie outputEditorCookie = outputDesign.getCookie (EditorCookie.class);
            final StyledDocument outputStyledDocument = outputEditorCookie.openDocument ();

            ConverterCustom.loadItemsToRegistry (items, document);

            final HashMap<String,ConverterItem> id2item = new HashMap<String, ConverterItem> ();
            for (ConverterItem item : items)
                id2item.put (item.getID (), item);

            document.getTransactionManager ().writeAccess (new Runnable() {
                public void run () {
                    for (ConverterItem item : items)
                        convert (id2item, item, document);

                    try {
                        ConverterCode.convertCode (items, styledDocument, outputStyledDocument, document, inputJavaFile.getName (), outputDesign.getPrimaryFile ().getName ());
                    } catch (BadLocationException e) {
                        Exceptions.printStackTrace (e);
                    }
                }
            });

            for (ConverterItem item : items)
                if (! item.isUsed ())
                    Debug.warning ("Unrecognized component: " + item.getTypeID ()); // NOI18N

//            final Map<DesignComponent, Point> flowNodes = getFlowNodes (id2item, rootNode);
//            if (! flowNodes.isEmpty ()) {
                SwingUtilities.invokeAndWait (new Runnable() {
                    public void run () {
                        document.getTransactionManager ().writeAccess (new Runnable() {
                            public void run () {
                                FlowScene scene = FlowScene.getFlowSceneForDocument (document);
                                for (FlowNodeDescriptor node : scene.getNodes ()) {
//                                    Point point = flowNodes.get (node.getRepresentedComponent ());
//                                    if (point != null) {
//                                        Widget widget = scene.findWidget (node);
//                                        if (widget != null)
//                                            widget.setPreferredLocation (point);
//                                    }
                                        Widget widget = scene.findWidget (node);
                                        if (widget != null)
                                            widget.setPreferredLocation (null);
                                }
                            }
                        });
                    }
                });
//            }

            IOSupport.forceUpdateCode (outputDesign);
            CloneableEditorSupport cloneableEditorSupport = IOSupport.getCloneableEditorSupport (outputDesign);
            cloneableEditorSupport.saveDocument ();
            cloneableEditorSupport.close ();

            final String[] classNames = new String[2];
            JavaSource.forDocument (styledDocument).runUserActionTask (new Task<CompilationController>() {
                public void run (CompilationController parameter) throws Exception {
                    parameter.toPhase (JavaSource.Phase.ELEMENTS_RESOLVED);
                    ClassTree classTree = ConverterCode.findMainClass (parameter);
                    TypeElement element = (TypeElement) parameter.getTrees ().getElement (TreePath.getPath (parameter.getCompilationUnit (), classTree));
                    classNames[0] = element.getQualifiedName ().toString ();
                }
            }, true);
            JavaSource.forDocument (outputStyledDocument).runUserActionTask (new Task<CompilationController>() {
                public void run (CompilationController parameter) throws Exception {
                    parameter.toPhase (JavaSource.Phase.ELEMENTS_RESOLVED);
                    ClassTree classTree = ConverterCode.findMainClass (parameter);
                    TypeElement element = (TypeElement) parameter.getTrees ().getElement (TreePath.getPath (parameter.getCompilationUnit (), classTree));
                    classNames[1] = element.getQualifiedName ().toString ();
                }
            }, true);

            Project project = FileOwnerQuery.getOwner (inputJavaFile);
            if (project != null) {
                AntProjectHelper helper = project.getLookup ().lookup (AntProjectHelper.class);
                if (helper != null)
                    J2MEProjectGenerator.copyMIDletProperty (project, helper, classNames[0], classNames[1]);
            }
        } catch (Exception e) {
            Exceptions.printStackTrace (e);
        }

        return errors;
    }

//    private static Map<DesignComponent, Point> getFlowNodes (HashMap<String, ConverterItem> id2item, Node rootNode) {
//        HashMap<DesignComponent, Point> map = new HashMap<DesignComponent, Point> ();
//        Node flowNode = XMLUtil.getChild (rootNode, "FlowDocument"); // NOI18N
//        String version = XMLUtil.getAttributeValue (flowNode, "version"); // NOI18N
//        if (! "1.1".equals (version)) // NOI18N
//            return Collections.emptyMap ();
//        for (Node node : XMLUtil.getChildren (flowNode, "Node")) { // NOI18N
//            String id = XMLUtil.getAttributeValue (node, "id"); // NOI18N
//            ConverterItem item = id2item.get (id);
//            if (item == null  ||  ! item.isUsed ())
//                continue;
//            String loc = XMLUtil.getAttributeValue (node, "location"); // NOI18N
//            int i = loc.indexOf (','); // NOI18N
//            if (i < 0)
//                continue;
//            int x = Integer.parseInt (loc.substring (0, i));
//            int y = Integer.parseInt (loc.substring (i + 1));
//            map.put (item.getRelatedComponent (), new Point (x, y));
//        }
//        return map;
//    }

    private static List<ConverterItem> getConverterItems (Node rootNode) {
        ArrayList<ConverterItem> components = new ArrayList<ConverterItem> ();
        Node documentNode = XMLUtil.getChild (rootNode, "DesignDocument"); // NOI18N
        for (Node componentNode : XMLUtil.getChildren (documentNode, "DesignComponent")) { // NOI18N
            String typeid = XMLUtil.getAttributeValue (componentNode, "typeid"); // NOI18N
            typeid = convertTypeIDFromString (typeid);
            ConverterItem item = new ConverterItem (
                    XMLUtil.getAttributeValue (componentNode, "uid"), // NOI18N
                    XMLUtil.getAttributeValue (componentNode, "id"), // NOI18N
                    typeid // NOI18N
            );
            for (Node propertyNode : XMLUtil.getChildren (componentNode, "Property")) { // NOI18N
                item.addProperty (
                        XMLUtil.getAttributeValue (propertyNode, "name"), // NOI18N
                        XMLUtil.getAttributeValue (propertyNode, "value") // NOI18N
                );
            }
            for (Node containerPropertyNode : XMLUtil.getChildren (componentNode, "ContainerProperty")) { // NOI18N
                String name = XMLUtil.getAttributeValue (containerPropertyNode, "name"); // NOI18N
                item.initContainerProperty (name); // NOI18N
                for (Node itemNode : XMLUtil.getChildren (containerPropertyNode, "ContainerPropertyItem")) // NOI18N
                    item.addContainerPropertyItem (name, XMLUtil.getAttributeValue (itemNode, "value")); // NOI18N
            }
            components.add (item);
        }
        return components;
    }

    private static String convertTypeIDFromString (String string) {
        if (string == null)
            return null;
        int dimension = 0;
        if (string.charAt (0) == '#') {
            int pos = 1;
            for (;;) {
                char c;
                if (pos >= string.length ()) {
                    dimension = 0;
                    break;
                }
                c = string.charAt (pos ++);
                if (c == '#')
                    break;
                if (! Character.isDigit (c)) {
                    dimension = 0;
                    break;
                }
                dimension = dimension * 10 + (c - '0');
            }
            if (dimension > 0)
                string = string.substring (pos);
        }
        int i = string.indexOf (':');
        return i >= 0 ? string.substring (i + 1) : string;
    }

    private static void convert (HashMap<String, ConverterItem> id2item, ConverterItem item, DesignDocument document) {
        if (item.isUsed ())
            return;
        String id = item.getID ();
        String typeID = item.getTypeID ();

        if ("javax.microedition.lcdui.Command".equals (typeID)) // NOI18N
            ConverterResources.convertCommand (item, document);

        else if ("javax.microedition.lcdui.Alert".equals (typeID)) // NOI18N
            ConverterDisplayables.convertAlert (id2item, item, document);
        else if ("javax.microedition.lcdui.Form".equals (typeID)) // NOI18N
            ConverterDisplayables.convertForm (id2item, item, document);
        else if ("javax.microedition.lcdui.List".equals (typeID)) // NOI18N
            ConverterDisplayables.convertList (id2item, item, document);
        else if ("javax.microedition.lcdui.TextBox".equals (typeID)) // NOI18N
            ConverterDisplayables.convertTextBox (id2item, item, document);

        else if ("javax.microedition.lcdui.ChoiceGroup".equals (typeID)) // NOI18N
            ConverterItems.convertChoiceGroup (id2item, item, document);
        else if ("javax.microedition.lcdui.DateField".equals (typeID)) // NOI18N
            ConverterItems.convertDateField (id2item, item, document);
        else if ("javax.microedition.lcdui.Gauge".equals (typeID)) // NOI18N
            ConverterItems.convertGauge (id2item, item, document);
        else if ("javax.microedition.lcdui.Gauge-AlertIndicator_Helper".equals (typeID)) // NOI18N
            ConverterItems.convertAlertIndicator (id2item, item, document);
        else if ("javax.microedition.lcdui.ImageItem".equals (typeID)) // NOI18N
            ConverterItems.convertImageItem (id2item, item, document);
        else if ("javax.microedition.lcdui.Spacer".equals (typeID)) // NOI18N
            ConverterItems.convertSpacer (id2item, item, document);
        else if ("javax.microedition.lcdui.StringItem".equals (typeID)) // NOI18N
            ConverterItems.convertStringItem (id2item, item, document);
        else if ("javax.microedition.lcdui.TextField".equals (typeID)) // NOI18N
            ConverterItems.convertTextField (id2item, item, document);

        else if ("javax.microedition.lcdui.Font".equals (typeID)) // NOI18N
            ConverterResources.convertFont (item, document);
        else if ("javax.microedition.lcdui.Ticker".equals (typeID)) // NOI18N
            ConverterResources.convertTicker (item, document);
        else if ("javax.microedition.lcdui.Image".equals (typeID)) // NOI18N
            ConverterResources.convertImage (item, document);

        else if ("GROUP-org.netbeans.modules.mvd.model.midp2.Midp2ChoiceElementDC".equals (typeID)) // NOI18N
            ConverterElements.convertChoiceElement (id2item, item, document);
        else if ("GROUP-org.netbeans.modules.mvd.model.midp2.Midp2ListElementDC".equals (typeID)) // NOI18N
            ConverterElements.convertListElement (id2item, item, document);

        else if ("org.netbeans.microedition.util.SimpleCancellableTask".equals (typeID)) // NOI18N
            ConverterBuilt.convertSimpleCancellableTask (id2item, item, document);
        else if ("org.netbeans.microedition.lcdui.SimpleTableModel".equals (typeID)) // NOI18N
            ConverterBuilt.convertSimpleTableModel (id2item, item, document);
        else if ("org.netbeans.microedition.lcdui.SplashScreen".equals (typeID)) // NOI18N
            ConverterBuilt.convertSplashScreen (id2item, item, document);
        else if ("org.netbeans.microedition.lcdui.TableItem".equals (typeID)) // NOI18N
            ConverterBuilt.convertTableItem (id2item, item, document);
        else if ("org.netbeans.microedition.lcdui.WaitScreen".equals (typeID)) // NOI18N
            ConverterBuilt.convertWaitScreen (id2item, item, document);

        else if ("javax.microedition.m2g.SVGImage".equals (typeID)) // NOI18N
            ConverterSVG.convertImage (id2item, item, document);
        else if ("org.netbeans.microedition.svg.SVGAnimatorWrapper".equals (typeID)) // NOI18N
            ConverterSVG.convertPlayer (id2item, item, document);
        else if ("org.netbeans.microedition.svg.SVGMenu".equals (typeID)) // NOI18N
            ConverterSVG.convertMenu (id2item, item, document);
        else if ("GROUP-org.netbeans.modules.vmd.components.svg.SvgMenuElementDC".equals (typeID)) // NOI18N
            ConverterSVG.convertMenuElement (id2item, item, document);
        else if ("org.netbeans.microedition.svg.SVGSplashScreen".equals (typeID)) // NOI18N
            ConverterSVG.convertSplashScreen (id2item, item, document);
        else if ("org.netbeans.microedition.svg.SVGWaitScreen".equals (typeID)) // NOI18N
            ConverterSVG.convertWaitScreen (id2item, item, document);

        else if ("$MobileDevice".equals (id)) { // NOI18N
            DesignComponent pointsCategory = MidpDocumentSupport.getCategoryComponent(document, PointsCategoryCD.TYPEID);
            List<DesignComponent> list = DocumentSupport.gatherSubComponentsOfType(pointsCategory, MobileDeviceCD.TYPEID);
            DesignComponent mobileDevice = list.get (0);
            convertObject (item, mobileDevice);
        } else if ("$StartPoint".equals (id)) { // NOI18N
            DesignComponent pointsCategory = MidpDocumentSupport.getCategoryComponent(document, PointsCategoryCD.TYPEID);
            List<DesignComponent> list = DocumentSupport.gatherSubComponentsOfType(pointsCategory, MobileDeviceCD.TYPEID);
            DesignComponent mobileDevice = list.get (0);
            DesignComponent startEventSource = mobileDevice.readProperty (MobileDeviceCD.PROP_START).getComponent ();
            convertObject (item, startEventSource);
            ConverterActions.convertCommandActionHandler (id2item, item, startEventSource);
        }

        else if (ConverterCustom.isClassComponent (item)) {
            ConverterCustom.convertCustom (id2item, item, document);
        }
    }



    static ConverterItem convertConverterItem (HashMap<String, ConverterItem> id2item, String value, DesignDocument document) {
        ConverterItem item = id2item.get (value);
        if (item != null) {
            convert (id2item, item, document);
            if (item.isUsed ())
                return item;
        }
        return null;
    }

    static DesignComponent convertConverterItemComponent (HashMap<String, ConverterItem> id2item, String propertyValue, DesignDocument document) {
        ConverterItem item = convertConverterItem (id2item, propertyValue, document);
        return item != null ? item.getRelatedComponent () : null;
    }

    // Created: NO, Adds: NO
    static void convertObject (ConverterItem item, DesignComponent component) {
        item.setUsed (component);
    }

    // Created: NO, Adds: NO
    static void convertClass (ConverterItem item, DesignComponent component) {
        convertObject (item, component);
        item.setClass ();
        component.writeProperty (ClassCD.PROP_INSTANCE_NAME, MidpTypes.createStringValue (item.getID ()));
        Boolean lazy = getBoolean (item.getPropertyValue ("lazyInitialized")); // NOI18N
        component.writeProperty (ClassCD.PROP_LAZY_INIT, MidpTypes.createBooleanValue (lazy == null  ||  lazy));
    }

}
