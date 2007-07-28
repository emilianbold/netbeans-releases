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


package org.netbeans.modules.uml.reporting.dataobjects;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import org.netbeans.modules.uml.common.Util;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IEdgeMapLocation;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphicExportDetails;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphicMapLocation;
import org.netbeans.modules.uml.core.metamodel.diagrams.ILabelMapLocation;
import org.netbeans.modules.uml.core.metamodel.diagrams.INodeMapLocation;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.reporting.ReportTask;
import org.netbeans.modules.uml.ui.controls.drawingarea.UIDiagram;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeDiagram;
import org.netbeans.modules.uml.ui.swing.drawingarea.SaveAsGraphicKind;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl
 */
public class DiagramData extends ElementDataObject {
    private IProxyDiagram pProxyDiagram;
    private String dir;
    private static int VIEWPORT_WIDTH = 1000;
    private static int VIEWPORT_HEIGHT = 700;
    private static int ZOOM_WIDTH = 120;
    private static String FULL_DIAGRAM_FILE_SUFFIX = "_full.html"; // NOI18N
    private int fitToScaleIndex =0;
    private int full_size_index = 0;
//    private double[] s = {0.5, 1};  // predefined fixed scales
    
    /** Creates a new instance of DiagramData */
    public DiagramData() {
    }
    
    public DiagramData(ITreeDiagram diagram) {
        this.pProxyDiagram = diagram.getDiagram();
        setElement(diagram.getData().getDiagram().getDiagram());
    }
    
    
    public IDiagram getDiagram() {
        return pProxyDiagram.getDiagram();
    }
    
    private double getFitToWindowScale(IDiagram diagram) {
        if (diagram instanceof UIDiagram) {
            double width = ((UIDiagram)diagram).getFrameWidth();
            double height = ((UIDiagram)diagram).getFrameHeight();
            double scale1 = VIEWPORT_WIDTH/width;
            double scale2 = VIEWPORT_HEIGHT/height;
            return scale1 > scale2 ? scale2 : scale1 ;
        }
        return 1;
    }
    
    
    private void createFullDiagramFile(IDiagram pDiagram, IGraphicExportDetails pDetails) {
        if (pDiagram != null && pDetails != null) {
            String id = pDiagram.getXMIID();
            String id2 = ReportTask.convertID(id);
            // get the jpg file for the diagram
            String fullname = pDiagram.getFilename();
            String name = StringUtilities.getFileName(fullname);
            String jpg = name;
            jpg += "_" + full_size_index + ReportTask.IMAGE_EXT;
            // will place the name and documentation for the diagram at the top of the html
            String diagName = pDiagram.getName();
            String doc = pDiagram.getDocumentation();
            String filename = getDirectoryPath() + File.separator + name;
            String full_filename = filename + FULL_DIAGRAM_FILE_SUFFIX;
            
            StringBuilder page = new StringBuilder();
            String str;
            
            page.append(getHTMLHeader());
            page.append(getNavBar());
            
            if (diagName != null && diagName.length() > 0) 
            {
                page.append("<HR><H2>" + pDiagram.getDiagramKind2() + " " + diagName + "</H2>"); // NOI18N
                page.append("<P>" + doc + "</P>\r\n"); // NOI18N
            
                page.append("<P ALIGN=\"CENTER\"><A HREF=\"" + name + // NOI18N
                    ReportTask.HTML_EXT + "\"><IMG SRC=\"" + // NOI18N
                    ReportTask.getPathToReportRoot(pDiagram) + // NOI18N
                    "images/fit-to-window.png\" BORDER=n></A>&nbsp;</P>"); // NOI18N
                
                page.append("<HR>\r\n"); // NOI18N
            }
            
            IETRect pMainRect = pDetails.getGraphicBoundingRect();
            if (pMainRect != null) {
                int nMainRectLeft = pMainRect.getLeft();
                int nMainRectBottom = pMainRect.getTop(); // The y axis is opposite of the TS coordinates
                int nMainRectTop = pMainRect.getBottom();
                int nMainRectRight = pMainRect.getRight();
                
                str = "<IMG SRC=\"" + jpg + "\" USEMAP=\"#MAP0-0\" BORDER=0 COORDS=\"" +  // NOI18N
                    nMainRectLeft + nMainRectTop + nMainRectRight + nMainRectBottom + "\">"; // NOI18N
                
                page.append(str);
                page.append("<MAP NAME=\"MAP0-0\">"); // NOI18N
                
                // Process each item in the graphic
                if (pDetails != null) {
                    // the information about the graphic is stored
                    // in these map locations
                    ETList < IGraphicMapLocation > pLocations = pDetails.getMapLocations();
                    if (pLocations != null) {
                        // loop through the map locations
                        int count = pLocations.size();
                        for (int x = 0; x < count; x++) {
                            // the map location can either be a node, a label, or
                            // an edge
                            // need to process all of the nodes and labels first
                            // because the links go from center to center and in
                            // order to get the map right in the jpg those need
                            // to be listed after the nodes, because first entry
                            // in wins
                            IGraphicMapLocation pGMLoc = pLocations.get(x);
                            if (pGMLoc != null) {
                                // see if we have a node or a label
                                if (pGMLoc instanceof INodeMapLocation) {
                                    INodeMapLocation pLoc = (INodeMapLocation) pGMLoc;
                                    // create the line in the jpg map for this node
                                    String str2 = createLineForNode(pDiagram, nMainRectBottom, pLoc);
                                    page.append(str2);
                                } else if (pGMLoc instanceof ILabelMapLocation) {
                                    ILabelMapLocation pLabel = (ILabelMapLocation) pGMLoc;
                                    // create the line in the jpg map for this label
                                    String str2 = createLineForLabel(nMainRectBottom, pLabel);
                                    page.append(str2);
                                }
                            }
                        }
                        // we have processed all of the nodes and labels, now loop through
                        // the map locations again and do the links (edges)
                        for (int x = 0; x < count; x++) {
                            IGraphicMapLocation pGMLoc = pLocations.get(x);
                            if (pGMLoc != null) {
                                // is it an edge
                                if (pGMLoc instanceof IEdgeMapLocation) {
                                    IEdgeMapLocation pEdgeLoc = (IEdgeMapLocation) pGMLoc;
                                    // create the line in the jpg map for the link
                                    String str2 = createLineForLink(nMainRectBottom, pEdgeLoc);
                                    page.append(str2);
                                }
                            }
                        }
                    }
                }
            }
            
            page.append("<HR>\r\n"); // NOI18N
            page.append(getNavBar());
            page.append("</BODY>\r\n"); // NOI18N
            page.append("</HTML>"); // NOI18N
            
            if (id2 == null || id2.length() == 0) {
                id2 = name;
            }
            makePage(full_filename, page.toString());
        }
    }
    
    
    /**
     * Format a string representing the node's points so that the jpg can include
     * a map to go to a hyperlink when flying over the node in the jpg
     *
     *
     * @param nMainRectBottom[in]		The long value for the bottom of the jpg map
     * @param pLoc[in]					The object representing the information for the jpg
     *
     * @return CComBSTR			The string representing the HTML
     *
     */
    private String createLineForNode(IDiagram diagram, int nMainRectBottom, INodeMapLocation pLoc) {
        String str = "";
        if (nMainRectBottom > 0 && pLoc != null) {
            // Get the basic graphic map information
            String currID = pLoc.getElementXMIID();
            String type = pLoc.getElementType();
            
            String id2 = ReportTask.convertID(currID);
            
            String name = pLoc.getName();
            
            String currID2 = ReportTask.convertID(currID);
            
            // Get the node specific stuff
            IETRect pRect = pLoc.getLocation();
            if (pRect != null) {
                int nRectLeft = pRect.getLeft();
                int nRectTop = pRect.getTop(); // Y coordinates are flipped
                int nRectBottom = pRect.getBottom();
                int nRectRight = pRect.getRight();
                long nTempRectLeft = nRectLeft;
                long nTempRectTop = nRectTop;
                long nTempRectRight = nRectRight;
                long nTempRectBottom = nRectBottom;
                
                if (displayLink(pLoc.getElement())) {
                    // COORDS= "x1,y1,x2,y2" Where x1,y1 are the coordinates of the
                    // upper-left corner of the rectangle and x2,y2 are the coordinates
                    // of the lower-right coordinates of the rectangle.
                    str = "<AREA SHAPE=\"RECT\" COORDS=\"" + nTempRectLeft + ", " + // NOI18N
                        nTempRectTop + ", " + nTempRectRight + ", " + nTempRectBottom + // NOI18N
                        "\" HREF=\"" + ReportTask.getPathToReportRoot(diagram) + // NOI18N
                        ReportTask.getLinkTo(pLoc.getElement()) + "\" ALT=\"" + name + "\">"; // NOI18N
                }
            }
        }
        return str;
    }
    
    
    /**
     * Format a string representing the label's points so that the jpg can include
     * a map to go to a hyperlink when flying over the label in the jpg
     *
     *
     * @param nMainRectBottom[in]		The long value for the bottom of the jpg map
     * @param pLoc[in]					The object representing the information for the jpg
     *
     * @return CComBSTR			The string representing the HTML
     *
     */
    private String createLineForLabel(int nMainRectBottom, ILabelMapLocation pLoc) {
        String str = "";
        if (nMainRectBottom > 0 && pLoc != null) {
            // Get the basic graphic map information
            String currID = pLoc.getElementXMIID();
            String name = pLoc.getName();
            
            String currID2 = ReportTask.convertID(currID);
            
            // Get the node specific stuff
            IETRect pRect = pLoc.getLocation();
            if (pRect != null) {
                int nRectLeft = pRect.getLeft();
                int nRectTop = pRect.getTop(); // Y coordinates are flipped
                int nRectBottom = pRect.getBottom();
                int nRectRight = pRect.getRight();
                
                long nTempRectLeft = nRectLeft;
                long nTempRectTop = nRectTop;
                long nTempRectRight =nRectRight;
                long nTempRectBottom = nRectBottom;
                
                if (displayLink(pLoc.getElement())) {
                    // COORDS= "x1,y1,x2,y2" Where x1,y1 are the coordinates of the
                    // upper-left corner of the rectangle and x2,y2 are the coordinates
                    // of the lower-right coordinates of the rectangle.
                    str = "<AREA SHAPE=\"RECT\" COORDS=\"" + nTempRectLeft + ", " + // NOI18N
                        nTempRectTop + ", " + nTempRectRight + ", " + nTempRectBottom + // NOI18N
                        "\" HREF=\"" + ReportTask.getPathToReportRoot(getDiagram()) + // NOI18N
                        ReportTask.getLinkTo(pLoc.getElement())  + "\" ALT=\"" + name + "\">"; // NOI18N
                }
            }
        }
        return str;
    }
    
    
    /**
     * Format a string representing the link's points so that the jpg can include
     * a map to go to a hyperlink when flying over the link in the jpg
     *
     *
     * @param nMainRectBottom[in]		The long value for the bottom of the jpg map
     * @param pLoc[in]					The object representing the information for the jpg
     *
     * @return CComBSTR			The string representing the HTML
     *
     */
    private String createLineForLink(int nMainRectBottom, IEdgeMapLocation pEdgeLoc) {
        String str = ""; // NOI18N
        if (nMainRectBottom > 0 && pEdgeLoc != null) {
            // Get the basic graphic map information
            String currID = pEdgeLoc.getElementXMIID();
            String name = pEdgeLoc.getName();
            
            String currID2 = ReportTask.convertID(currID);
            
            if (displayLink(pEdgeLoc.getElement())) {
                // Get the node specific stuff
                ETList < IETPoint > pPointsList = pEdgeLoc.getPoints();
                if (pPointsList != null && pPointsList.size() > 0) {
                    str = "<AREA SHAPE=\"POLY\" COORDS=\""; // NOI18N
                    int ptCount = pPointsList.size();
                    
                    for (int i = 0; i < ptCount; i++) {
                        IETPoint pPoint = pPointsList.item(i);
                        Integer x = new Integer(pPoint.getX());
                        Integer y = new Integer(pPoint.getY());
                        str += x.toString() + ","; // NOI18N
                        str += y.toString();
                        
                        if (i + 1 != ptCount)
                            str += ","; // NOI18N
                    }
                    str += "\" HREF=\"" + ReportTask.getPathToReportRoot(getDiagram()) + // NOI18N
                        ReportTask.getLinkTo(pEdgeLoc.getElement()) + "\" ALT=\"" + name + "\">"; // NOI18N
                    
                }
            }
        }
        return str;
    }
    
    
    
    private boolean makePage(String fileName, String content) {
        boolean result = false;
        File f = new File(fileName);
        try {
            FileOutputStream fo = new FileOutputStream(f);
            OutputStreamWriter writer = new OutputStreamWriter(fo);
            writer.write(content);
            writer.flush();
            writer.close();
            result = true;
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
        return result;
    }
    
    
    private String getDirectoryPath() {
        return dir;
    }
    
    
    private boolean createFileForDiagram(IDiagram pDiagram, IGraphicExportDetails pDetails, String imageString) {
        if (pDiagram == null || pDetails == null)
            return false;
        
        createFullDiagramFile(pDiagram, pDetails);
        
        String id = pDiagram.getXMIID();
        String id2 = ReportTask.convertID(id);
        // get the jpg file for the diagram
        String fullname = pDiagram.getFilename();
        String name = StringUtilities.getFileName(fullname);
        String jpg = name;
        jpg += ReportTask.IMAGE_EXT;
        // will place the name and documentation for the diagram at the top of the html
        String diagName = pDiagram.getName();
        String filename = getDirectoryPath();
        filename = filename + File.separator + name + ReportTask.HTML_EXT;
        String fullDiagramFile = name + FULL_DIAGRAM_FILE_SUFFIX;
        
        StringBuilder page = new StringBuilder();
        page.append("<HTML>\n"); // NOI18N
        page.append("	<HEAD>\n"); // NOI18N
        
        page.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=" +  // NOI18N
            System.getProperty("file.encoding") + "\">\n"); // NOI18N
        
        String str = "<TITLE>" + diagName + "</TITLE>\n"; // NOI18N
        page.append(str + "\n"); // NOI18N
        String scriptPath = getJavaScriptPath(pDiagram);
        String script = getScript(scriptPath, name, imageString);
        page.append(script + "\n"); // NOI18N
        page.append("	</HEAD>\n"); // NOI18N
        String body = getBody(scriptPath, diagName, fullDiagramFile);
        page.append(body + "\n"); // NOI18N
        page.append("</HTML>"); // NOI18N
        
        return makePage(filename, page.toString());
    }
    
    
    private String getJavaScriptPath(IDiagram diagram) {
        String path =".."; // NOI18N
        IPackage pkg = diagram.getOwningPackage();
        while (!pkg.equals(pkg.getProject())) {
            path = path + "/.."; // NOI18N
            pkg=pkg.getOwningPackage();
        }
        return path;
    }
    
    
    private String getScript(String javascriptPath, String diagramName, String imageString) {
        String script = "";
        
        StringBuilder buffer = ReportTask.readTemplate("org/netbeans/modules/uml/reporting/templates/script_template.html"); // NOI18N
        script = buffer.toString();
        script = script.replace("VIEWPORT_WIDTH", String.valueOf(VIEWPORT_WIDTH)); // NOI18N
        script = script.replace("VIEWPORT_HEIGHT", String.valueOf(VIEWPORT_HEIGHT)); // NOI18N
        script = script.replace("ZOOM_WIDTH", String.valueOf(ZOOM_WIDTH)); // NOI18N
        script = script.replace("LEFT_POSITION", String.valueOf((VIEWPORT_WIDTH-ZOOM_WIDTH)/2)); // NOI18N
        script = script.replaceAll("DIAGRAM_IMAGE", diagramName); // NOI18N
        script = script.replaceAll("SCRIPT_PATH", javascriptPath); // NOI18N
        script = script.replaceAll("IMAGESTRING", imageString); // NOI18N
        script = script.replaceAll("FIT_SCALE_INDEX", String.valueOf(fitToScaleIndex)); // NOI18N
        return script;
    }
    
    
    private String getBody(String scriptPath, String diagramName, String fileName) {
        String body = "";
        StringBuilder buffer = ReportTask.readTemplate("org/netbeans/modules/uml/reporting/templates/body_template.html"); // NOI18N
        body = buffer.toString();
        body = body.replaceAll("SCRIPT_PATH", scriptPath); // NOI18N
        body = body.replaceAll("%DIAGRAM_NAME%", getDiagram().getDiagramKind2() + " " + diagramName); // NOI18N
        
        body = body.replaceAll("%DIAGRAM_DOC%", getDiagram().getDocumentation()); // NOI18N
        
        body = body.replaceAll("FULL_DIAGRAM_HTML", fileName); // NOI18N
        body = body.replace("%BRAND%", NbBundle.getMessage(DiagramData.class, "brand")); // NOI18N
        body = body.replaceAll("%OVERVIEW%", // NOI18N
                NbBundle.getMessage(ElementDataObject.class, "Header_Overview")); // NOI18N
        body = body.replaceAll("%PACKAGE%", // NOI18N
                NbBundle.getMessage(ElementDataObject.class, "Header_Package")); // NOI18N
        body = body.replaceAll("%ELEMENT%", // NOI18N
                NbBundle.getMessage(ElementDataObject.class, "Header_Element")); // NOI18N
        body = body.replaceAll("%DIAGRAM%", // NOI18N
                NbBundle.getMessage(ElementDataObject.class, "Header_Diagram")); // NOI18N
        body = body.replaceAll("%HELP%", // NOI18N
                NbBundle.getMessage(ElementDataObject.class, "Header_Help")); // NOI18N
        return body;
    }
    
    
    public boolean toReport(File file) {
        this.dir = file.getAbsolutePath();
        
        // get the IDiagram from the ProxyDiagram (NULL if the diagram is closed)
        if (pProxyDiagram != null) {
            IDiagram pDiagram = pProxyDiagram.getDiagram();
            boolean closeIt =  !pProxyDiagram.isOpen() || pDiagram == null;
            Boolean closeTheDiagram = new Boolean(closeIt);
            
            if (pDiagram == null) {
                pDiagram = ReportTask.loadDiagram(pProxyDiagram.getFilename());
            }
            
            if (pDiagram != null) {
                // the diagram is open, so create its jpeg file
                // first get the file that we are going to create
                
                String filename = pDiagram.getFilename();
                String name = StringUtilities.getFileName(filename);
                
                double fitScale = getFitToWindowScale(pDiagram);
             
                double currentZoom = pDiagram.getCurrentZoom();
                currentZoom = currentZoom>1?1:currentZoom;
                double[] scales = {0.5*currentZoom, currentZoom, fitScale};
                Arrays.sort(scales);
                for (int i=0; i<scales.length; i++)
                {
                    if (scales[i]==fitScale)
                        fitToScaleIndex = i;
                    else if (scales[i]==currentZoom)
                        full_size_index = i;
                }

                StringBuilder imageString = new StringBuilder();
                IGraphicExportDetails details = null; // the image to be used for full diagram page
                
                for (int i=0;i<scales.length; i++) {
                    String imageName = getDirectoryPath() + File.separator + name + "_" + i + ReportTask.IMAGE_EXT;
                    try {
                        IGraphicExportDetails pDetails = pDiagram.saveAsGraphic2(imageName, SaveAsGraphicKind.SAFK_PNG, scales[i]);
                        if (pDetails!=null) {
                            int width = (int) (pDetails.getFrameBoundingRect().getWidth() * scales[i]);
                            int height = (int) (pDetails.getFrameBoundingRect().getHeight() * scales[i]);
                            imageString.append("               { 'path' : '" + name + "_" + i + ReportTask.IMAGE_EXT + "' , 'width' : " + width + " , 'height' : " + height + " }, "); // NOI18N
                            imageString.append("\n"); // NOI18N
                            if (i == full_size_index) {
                                details = pDetails;
                            }
                        }
                    } catch (OutOfMemoryError e) {
                        
                        Util.forceGC();
                        ErrorManager.getDefault().notify(e);
                    }
                }
                
                return createFileForDiagram(pDiagram, details, imageString.toString());
            }
        }
        return false;
    }
}
