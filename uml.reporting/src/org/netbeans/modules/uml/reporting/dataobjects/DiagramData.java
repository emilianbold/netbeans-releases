/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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


package org.netbeans.modules.uml.reporting.dataobjects;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.diagrams.EdgeMapLocation;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphicExportDetails;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphicMapLocation;
import org.netbeans.modules.uml.core.metamodel.diagrams.ILabelMapLocation;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.NodeMapLocation;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.drawingarea.UIDiagram;
import org.netbeans.modules.uml.reporting.ReportTask;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeDiagram;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl
 */
public class DiagramData extends ElementDataObject
{
    private IProxyDiagram pProxyDiagram;
    private String dir;
    private static int VIEWPORT_WIDTH = 1000;
    private static int VIEWPORT_HEIGHT = 700;
    private static int ZOOM_WIDTH = 120;
    private static String FIT_TO_WINDOW_DIAGRAM_FILE_SUFFIX = "_fit.html"; // NOI18N
    private int fitToScaleIndex =0;
    private int full_size_index = 0;
    
    /** Creates a new instance of DiagramData */
    public DiagramData()
    {
    }
    
    public DiagramData(ITreeDiagram diagram)
    {
        this.pProxyDiagram = diagram.getDiagram();
        setElement(diagram.getData().getDiagram().getDiagram());
    }
    
    
    public IDiagram getDiagram()
    {
        return pProxyDiagram.getDiagram();
    }
    
    private double getFitToWindowScale(IDiagram diagram)
    {
        if (diagram instanceof UIDiagram)
        {
            int width = ((UIDiagram)diagram).getFrameWidth();
            int height = ((UIDiagram)diagram).getFrameHeight();
            double scale1 = VIEWPORT_WIDTH/(double)width;
            double scale2 = VIEWPORT_HEIGHT/(double)height;
            return scale1 > scale2 ? scale2 : scale1 ;
        }
        return 1;
    }
   
    private double getCurrentZoom(IDiagram diagram)
    {
        if (diagram instanceof UIDiagram)
        {
            return ((UIDiagram)diagram).getCurrentZoom();
        }
        return 1;
    }
    
    private void createFullDiagramFile(IDiagram pDiagram, IGraphicExportDetails pDetails)
    {
        if (pDiagram != null && pDetails != null)
        {
            // get the jpg file for the diagram
            String fullname = pDiagram.getFilename();
            String name = StringUtilities.getFileName(fullname);
            String jpg = name;
            jpg += "_" + full_size_index + ReportTask.IMAGE_EXT;
            // will place the name and documentation for the diagram at the top of the html
            String diagName = pDiagram.getName();
            String doc = pDiagram.getDocumentation() == null? "" : pDiagram.getDocumentation();
            String filename = getDirectoryPath() + File.separator + name + ReportTask.HTML_EXT;
            
            StringBuilder page = new StringBuilder();
            String str;
            
            page.append(getHTMLHeader());
            page.append(getNavBar());
            
            if (diagName != null && diagName.length() > 0)
            {
                page.append("<HR><H2>" + pDiagram.getDiagramKindAsString() + " " + diagName + "</H2>"); // NOI18N
                page.append("<P>" + doc + "</P>\r\n"); // NOI18N
                
                page.append("<P ALIGN=\"CENTER\"><A HREF=\"" + name + FIT_TO_WINDOW_DIAGRAM_FILE_SUFFIX + // NOI18N
                        "\"><IMG SRC=\"" + // NOI18N
                        ReportTask.getPathToReportRoot(pDiagram) + // NOI18N
                        "images/fit-to-window.png\" BORDER=n></A>&nbsp;</P>"); // NOI18N
                
                page.append("<HR>\r\n"); // NOI18N
            }
            
            IETRect pMainRect = pDetails.getGraphicBoundingRect();
            if (pMainRect != null)
            {          
                str = "<IMG SRC=\"" + jpg + "\" USEMAP=\"#MAP0-0\" BORDER=0>"; // NOI18N
                
                page.append(str);
                page.append("<MAP NAME=\"MAP0-0\">"); // NOI18N
                
                // Process each item in the graphic
                if (pDetails != null)
                {
                    // the information about the graphic is stored
                    // in these map locations
                    ETList < IGraphicMapLocation > pLocations = pDetails.getMapLocations();
                    if (pLocations != null)
                    {
                        // loop through the map locations
                        int count = pLocations.size();
                        for (int x = 0; x < count; x++)
                        {
                            // the map location can either be a node, a label, or
                            // an edge
                            // need to process all of the nodes and labels first
                            // because the links go from center to center and in
                            // order to get the map right in the jpg those need
                            // to be listed after the nodes, because first entry
                            // in wins
                            IGraphicMapLocation pGMLoc = pLocations.get(x);
                            if (pGMLoc != null)
                            {
                                // see if we have a node or a label
                                if (pGMLoc instanceof NodeMapLocation)
                                {
                                    NodeMapLocation pLoc = (NodeMapLocation) pGMLoc;
                                    // create the line in the jpg map for this node
                                    String str2 = createLineForNode(pDiagram, pLoc);
                                    page.append(str2);
                                }
                                else if (pGMLoc instanceof ILabelMapLocation)
                                {
                                    ILabelMapLocation pLabel = (ILabelMapLocation) pGMLoc;
                                    // create the line in the jpg map for this label
                                    String str2 = createLineForLabel(pLabel);
                                    page.append(str2);
                                }
                            }
                        }
                        // we have processed all of the nodes and labels, now loop through
                        // the map locations again and do the links (edges)
                        for (int x = 0; x < count; x++)
                        {
                            IGraphicMapLocation pGMLoc = pLocations.get(x);
                            if (pGMLoc != null)
                            {
                                // is it an edge
                                if (pGMLoc instanceof EdgeMapLocation)
                                {
                                    EdgeMapLocation pEdgeLoc = (EdgeMapLocation) pGMLoc;
                                    // create the line in the jpg map for the link
                                    String str2 = createLineForLink(pEdgeLoc);
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
            
            makePage(filename, page.toString());
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
    private String createLineForNode(IDiagram diagram, NodeMapLocation pLoc)
    {
        String str = "";
        if (pLoc != null)
        {
            // Get the basic graphic map information
            String name = pLoc.getName();
            
            // Get the node specific stuff
            Rectangle pRect = pLoc.getLocation();
            if (pRect != null)
            {
                int nTempRectLeft = pRect.x;
                int nTempRectTop = pRect.y;
                int nTempRectRight = pRect.x + pRect.width;
                int nTempRectBottom = pRect.y + pRect.height;
                
                if (displayLink(pLoc.getElement()))
                {
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
    private String createLineForLabel(ILabelMapLocation pLoc)
    {
        String str = "";
        if (pLoc != null)
        {
            // Get the basic graphic map information
            String name = pLoc.getName();
            
            // Get the node specific stuff
            IETRect pRect = pLoc.getLocation();
            if (pRect != null)
            {
                int nRectLeft = pRect.getLeft();
                int nRectTop = pRect.getTop(); // Y coordinates are flipped
                int nRectBottom = pRect.getBottom();
                int nRectRight = pRect.getRight();
                
                long nTempRectLeft = nRectLeft;
                long nTempRectTop = nRectTop;
                long nTempRectRight =nRectRight;
                long nTempRectBottom = nRectBottom;
                
                if (displayLink(pLoc.getElement()))
                {
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
    private String createLineForLink(EdgeMapLocation pEdgeLoc)
    {
        String str = ""; // NOI18N
        if (pEdgeLoc != null)
        {
            String name = pEdgeLoc.getName();
            
            if (displayLink(pEdgeLoc.getElement()))
            {
                List < Point > pPointsList = pEdgeLoc.getPoints();
                if (pPointsList != null && pPointsList.size() > 0)
                {
                    str = "<AREA SHAPE=\"POLY\" COORDS=\""; // NOI18N
                    int ptCount = pPointsList.size();
                    
                    for (int i = 0; i < ptCount; i++)
                    {
                        Point point = pPointsList.get(i);
                        str += point.x + ","; // NOI18N
                        str += point.y;
                        
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
    
    
    
    private boolean makePage(String fileName, String content)
    {
        boolean result = false;
        File f = new File(fileName);
        try
        {
            FileOutputStream fo = new FileOutputStream(f);
            OutputStreamWriter writer = new OutputStreamWriter(fo, ENCODING);
            writer.write(content);
            writer.flush();
            writer.close();
            result = true;
        }
        catch (IOException e)
        {
            Logger.getLogger(ElementDataObject.class.getName()).log(
                    Level.SEVERE, getDiagram().getDiagramKindAsString() + " - " + getDiagram().getName(), e);
        }
        return result;
    }
    
    
    private String getDirectoryPath()
    {
        return dir;
    }
    
    
    private boolean createFilesForDiagram(IDiagram pDiagram, IGraphicExportDetails pDetails, String imageString)
    {
        if (pDiagram == null || pDetails == null)
            return false;
        
        createFullDiagramFile(pDiagram, pDetails);
        
        // get the jpg file for the diagram
        String fullname = pDiagram.getFilename();
        String name = StringUtilities.getFileName(fullname);
        
        // will place the name and documentation for the diagram at the top of the html
        String diagName = pDiagram.getName();
        String filename = getDirectoryPath();
        filename = filename + File.separator + name + FIT_TO_WINDOW_DIAGRAM_FILE_SUFFIX;
        
        StringBuilder page = new StringBuilder();
        page.append("<HTML>\n"); // NOI18N
        page.append("	<HEAD>\n"); // NOI18N
        
        page.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=" +  // NOI18N
//                System.getProperty("file.encoding") + "\">\n"); // NOI18N
                ENCODING + "\">\n"); // NOI18N
        
        String str = "<TITLE>" + diagName + "</TITLE>\n"; // NOI18N
        page.append(str + "\n"); // NOI18N
        String scriptPath = getJavaScriptPath(pDiagram);
        String script = getScript(scriptPath, name, imageString);
        page.append(script + "\n"); // NOI18N
        page.append("	</HEAD>\n"); // NOI18N
        String body = getBody(scriptPath, diagName, name + ReportTask.HTML_EXT);
        page.append(body + "\n"); // NOI18N
        page.append("</HTML>"); // NOI18N
        
        return makePage(filename, page.toString());
    }
    
    
    private String getJavaScriptPath(IDiagram diagram)
    {
        String path =".."; // NOI18N
        IPackage pkg = diagram.getOwningPackage();
        assert pkg!=null: "invalid package for diagram " + diagram.getName(); // NOI18N 
        while (pkg != null && !pkg.equals(pkg.getProject()))
        {
            path = path + "/.."; // NOI18N
            pkg=pkg.getOwningPackage();
        }
        return path;
    }
    
    
    private String getScript(String javascriptPath, String diagramName, String imageString)
    {
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
    
    
    private String getBody(String scriptPath, String diagramName, String fileName)
    {
        String body = "";
        StringBuilder buffer = ReportTask.readTemplate("org/netbeans/modules/uml/reporting/templates/body_template.html"); // NOI18N
        body = buffer.toString();
        body = body.replaceAll("SCRIPT_PATH", scriptPath); // NOI18N
        body = body.replaceAll("%DIAGRAM_NAME%", getDiagram().getDiagramKindAsString() + " " + diagramName); // NOI18N
        
        body = body.replaceAll("%DIAGRAM_DOC%", getDiagram().getDocumentation() == null? "": getDiagram().getDocumentation()); // NOI18N
        
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
    
    
    public boolean toReport(File file)
    {
        this.dir = file.getAbsolutePath();
        
        // get the IDiagram from the ProxyDiagram (NULL if the diagram is closed)
        if (pProxyDiagram != null)
        {
            IDiagram pDiagram = pProxyDiagram.getDiagram();
            
            if (pDiagram == null)
            {
                pDiagram = ReportTask.loadDiagram(pProxyDiagram.getFilename());
            }
            
            if (pDiagram != null)
            {
                // the diagram is open, so create its jpeg file
                // first get the file that we are going to create
                
                String filename = pDiagram.getFilename();
                String name = StringUtilities.getFileName(filename);
                
                double fitScale = getFitToWindowScale(pDiagram);
                
                double currentZoom = getCurrentZoom(pDiagram);                                
                double[] scales = {currentZoom, currentZoom >= 1 ? 0.5: 1, fitScale};
                Arrays.sort(scales);
                for (int i=0; i<scales.length; i++)
                {
                    if (scales[i]==fitScale)
                        fitToScaleIndex = i;
                    if (scales[i]==currentZoom)
                        full_size_index = i;
                }
                
                StringBuilder imageString = new StringBuilder();
                IGraphicExportDetails details = null; // the image to be used for full diagram page
                
                for (int i=0;i<scales.length; i++)
                {
                    String imageName = getDirectoryPath() + File.separator + name + "_" + i + ReportTask.IMAGE_EXT;
                    IGraphicExportDetails pDetails = pDiagram.saveAsGraphic(imageName, 0, scales[i]);
                    if (pDetails!=null)
                    {
                        int width = (int) (pDetails.getFrameBoundingRect().getWidth());
                        int height = (int) (pDetails.getFrameBoundingRect().getHeight());
                        imageString.append("               { 'path' : '" + name + "_" + i + ReportTask.IMAGE_EXT + "' , 'width' : " + width + " , 'height' : " + height + " }, "); // NOI18N
                        imageString.append("\n"); // NOI18N
                        if (i == full_size_index)
                        {
                            details = pDetails;
                        }
                    }
                }
                
                return createFilesForDiagram(pDiagram, details, imageString.toString());
            }
        }
        return false;
    }
}
