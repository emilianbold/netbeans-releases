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
 * The Original Software is the Accelerators module.
 * The Initial Developer of the Original Software is Andrei Badea.
 * Portions Copyright 2005-2006 Andrei Badea.
 * All Rights Reserved.
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
 *
 * Contributor(s): Petr Hrebejk
 */

package org.netbeans.modules.jumpto.file;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.beans.BeanInfo;
import java.util.Comparator;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/** Contains interesting information about file found in the search.
 *
 * @author Petr Hrebejk
 */
public class FileDescription {

//    static long time;
    
    public static final String SEARCH_IN_PROGRES = NbBundle.getMessage(FileDescription.class, "TXT_SearchingOtherProjects"); // NOI18N
    
    private final FileObject fileObject;
    private final String relativePath;
    private final Project project; // Project the file belongs to
    
    private Icon icon;
    private String projectName;
    private Icon projectIcon;
    private boolean prefered;
    
    private static final String EMPTY_STRING = ""; // NOI18N
    
    public FileDescription(FileObject file, String relativePath, Project project, boolean prefered) {
        this.fileObject = file;
        this.relativePath = relativePath;
        this.project = project;
        this.prefered = prefered;
    }
       
    public String getName() {
        return fileObject.getNameExt(); // NOI18N
    }
    
    public synchronized Icon getIcon() {
                
        if ( icon == null ) {
            DataObject od = getDataObject();
            Image i = od.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
            icon = new ImageIcon( i );
        }
        
        return icon;
    }
    
    public String getRelativePath() {
        return relativePath;
    }
    
    public synchronized String getProjectName() {
        if ( projectName == null ) {
            initProjectInfo();
        }        
        return projectName;
    }
    
    public synchronized Icon getProjectIcon() {
        if ( projectIcon == null ) {
            initProjectInfo();
        }        
        return projectIcon;
    }
    
//    public synchronized boolean isVisible() {
//
//        long t = System.currentTimeMillis();
//
//        if ( fileObject == null ) {
//            fileObject = FileUtil.toFileObject(file);
//        }
//        boolean visible = fileObject == null ? false : VisibilityQuery.getDefault().isVisible(fileObject);
//        if ( !visible ) {
//            addTime( t );
//            return false;
//        }
//
//        // XXX PERF needs to cache parents.
//        while( fileObject.getParent() != null ) {
//            fileObject = fileObject.getParent();
//            if ( fileObject.equals(sourceGroup.getRootFolder() ) ) {
//                addTime( t );
//                return true;
//            }
//            if ( !VisibilityQuery.getDefault().isVisible(fileObject)  ) {
//                addTime( t );
//                return false;
//            }
//        }
//        addTime( t );
//        return true;
//    }
//
//    private void addTime( long t ) {
////        time += System.currentTimeMillis() - t;
////        System.out.println("isVisible time " + time);
//    }
    
    public void open() {
        
        DataObject od = getDataObject();
        
        if ( od != null ) {
        
            EditCookie ec = (EditCookie) od.getCookie(EditCookie.class);

            if (ec != null) {
                ec.edit();
            }
            else {
                OpenCookie oc = od.getCookie( OpenCookie.class );
                if ( oc != null ) {
                    oc.open();
                }
            }
        }

    }
    
    public FileObject getFileObject() {        
        return fileObject;
    }
    
    private DataObject getDataObject() {
        try     {
            org.openide.filesystems.FileObject fo = getFileObject();
            return org.openide.loaders.DataObject.find(fo);
        }
        catch (DataObjectNotFoundException ex) {
            return null;
        }
    }
    
    private void initProjectInfo() {
        ProjectInformation pi = ProjectUtils.getInformation( project );
        projectName = pi.getDisplayName();
        projectIcon = pi.getIcon();
    }
    
    // Innerclasses ------------------------------------------------------------
    
    public static class FDComarator implements Comparator<FileDescription> {

        private boolean usePrefered;
        private boolean caseSensitive;
                
        public FDComarator(boolean usePrefered, boolean caseSensitive ) {
            this.usePrefered = usePrefered;
            this.caseSensitive = caseSensitive;
        }
        
        public int compare(FileDescription o1, FileDescription o2) {
            
            // If prefered prefer prefered
            if ( usePrefered ) {
                if ( o1.prefered && !o2.prefered ) {
                    return -1;
                }
                if ( !o1.prefered && o2.prefered ) {
                    return 1;
                }
            }
            
            // File name
            int cmpr = compareStrings( o1.getName(), o2.getName(), caseSensitive );
            if ( cmpr != 0 ) {
                return cmpr;
            }
            
            // Project name
            cmpr = compareStrings( o1.getProjectName(), o2.getProjectName(), caseSensitive );            
            if ( cmpr != 0 ) {
                return cmpr;
            }
            
            // Relative location
            cmpr = compareStrings( o1.getRelativePath(), o2.getRelativePath(), caseSensitive );
                        
            return cmpr;
           
        }
        
        private int compareStrings(String s1, String s2, boolean caseSensitive) {
            if( s1 == null ) {
                s1 = EMPTY_STRING;
            }
            if ( s2 == null ) {
                s2 = EMPTY_STRING;
            }
            
            
            return caseSensitive ? s1.compareTo( s2 ) : s1.compareToIgnoreCase( s2 );
        }        
    }

    private static class RendererComponent extends JPanel {
	private FileDescription fd;

	void setDescription(FileDescription fd) {
	    this.fd = fd;
	    putClientProperty(TOOL_TIP_TEXT_KEY, null);
	}

	@Override
	public String getToolTipText() {
	    String text = (String) getClientProperty(TOOL_TIP_TEXT_KEY);
	    if( text == null ) {
                if( fd != null) {
                    text = FileUtil.getFileDisplayName(fd.getFileObject());
                }
                putClientProperty(TOOL_TIP_TEXT_KEY, text);
	    }
	    return text;
	}
    }

    public static class Renderer extends DefaultListCellRenderer implements ChangeListener {
        
        public static Icon WAIT_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/jumpto/resources/wait.gif", false); // NOI18N
        
        
        private RendererComponent rendererComponent;
        private JLabel jlName = new JLabel();
        private JLabel jlPath = new JLabel();
        private JLabel jlPrj = new JLabel();
        private int DARKER_COLOR_COMPONENT = 5;
        private int LIGHTER_COLOR_COMPONENT = 80;        
        private Color fgColor;
        private Color fgColorLighter;
        private Color bgColor;
        private Color bgColorDarker;
        private Color bgSelectionColor;
        private Color fgSelectionColor;
        private Color bgColorGreener;
        private Color bgColorDarkerGreener;
        
        private JList jList;
    
        private boolean colorPrefered;
        
        public Renderer( JList list ) {
            
            jList = list;
            
            Container container = list.getParent();
            if ( container instanceof JViewport ) {
                ((JViewport)container).addChangeListener(this);
                stateChanged(new ChangeEvent(container));
            }
            
            rendererComponent = new RendererComponent();
            rendererComponent.setLayout(new BorderLayout());
            rendererComponent.add( jlName, BorderLayout.WEST );
            rendererComponent.add( jlPath, BorderLayout.CENTER);
            rendererComponent.add( jlPrj, BorderLayout.EAST );
            
            
            jlName.setOpaque(false);
            jlPath.setOpaque(false);
            jlPrj.setOpaque(false);
            
            jlName.setFont(list.getFont());
            jlPath.setFont(list.getFont());
            jlPrj.setFont(list.getFont());
            
            
            jlPrj.setHorizontalAlignment(RIGHT);
            jlPrj.setHorizontalTextPosition(LEFT);
            
            // setFont( list.getFont() );            
            fgColor = list.getForeground();
            fgColorLighter = new Color( 
                                   Math.min( 255, fgColor.getRed() + LIGHTER_COLOR_COMPONENT),
                                   Math.min( 255, fgColor.getGreen() + LIGHTER_COLOR_COMPONENT),
                                   Math.min( 255, fgColor.getBlue() + LIGHTER_COLOR_COMPONENT)
                                  );
                            
            bgColor = list.getBackground();
            bgColorDarker = new Color(
                                    Math.abs(bgColor.getRed() - DARKER_COLOR_COMPONENT),
                                    Math.abs(bgColor.getGreen() - DARKER_COLOR_COMPONENT),
                                    Math.abs(bgColor.getBlue() - DARKER_COLOR_COMPONENT)
                            );
            bgSelectionColor = list.getSelectionBackground();
            fgSelectionColor = list.getSelectionForeground();
            
            
            bgColorGreener = new Color( 
                                    Math.abs(bgColor.getRed() - 20),
                                    Math.min(255, bgColor.getGreen() + 10 ),
                                    Math.abs(bgColor.getBlue() - 20) );
                    
                    
            bgColorDarkerGreener = new Color( 
                                    Math.abs(bgColorDarker.getRed() - 35),
                                    Math.min(255, bgColorDarker.getGreen() + 5 ),
                                    Math.abs(bgColorDarker.getBlue() - 35) );
        }
        
        public @Override Component getListCellRendererComponent( JList list,
                                                       Object value,
                                                       int index,
                                                       boolean isSelected,
                                                       boolean hasFocus) {
            
            // System.out.println("Renderer for index " + index );
            
            int height = list.getFixedCellHeight();
            int width = list.getFixedCellWidth() - 1;
            
            width = width < 200 ? 200 : width;
            
            // System.out.println("w, h " + width + ", " + height );
            
            Dimension size = new Dimension( width, height );
            rendererComponent.setMaximumSize(size);
            rendererComponent.setPreferredSize(size);
                        
            if ( isSelected ) {
                jlName.setForeground(fgSelectionColor);
                jlPath.setForeground(fgSelectionColor);
                jlPrj.setForeground(fgSelectionColor);
                rendererComponent.setBackground(bgSelectionColor);
            }
            else {
                jlName.setForeground(fgColor);
                jlPath.setForeground(fgColorLighter);
                jlPrj.setForeground(fgColor);                
                rendererComponent.setBackground( index % 2 == 0 ? bgColor : bgColorDarker );
            }
            
            if ( value instanceof FileDescription ) {
                FileDescription fd = (FileDescription)value;
                jlName.setIcon(fd.getIcon());
                jlName.setText(fd.getName());
                jlPath.setIcon(null);
                jlPath.setHorizontalAlignment(SwingConstants.LEFT);
                jlPath.setText(fd.getRelativePath().length() > 0 ? " (" + fd.getRelativePath() + ")" : " ()"); //NOI18N
                jlPrj.setText(fd.getProjectName());
                jlPrj.setIcon(fd.getProjectIcon());
                if ( !isSelected ) {
                    rendererComponent.setBackground( index % 2 == 0 ? 
                        ( fd.prefered && colorPrefered ? bgColorGreener : bgColor ) : 
                        ( fd.prefered && colorPrefered ? bgColorDarkerGreener : bgColorDarker ) );
                }
                rendererComponent.setDescription(fd);
            }
            else {
                jlName.setText( "" ); // NOI18M
                jlName.setIcon(null);
                jlPath.setIcon(Renderer.WAIT_ICON);
                jlPath.setHorizontalAlignment(SwingConstants.CENTER);
                jlPath.setText( value.toString() );
                jlPrj.setIcon(null);
                jlPrj.setText( "" ); // NOI18N
            }
            
            return rendererComponent;
        }
        
        public void stateChanged(ChangeEvent event) {
            
            JViewport jv = (JViewport)event.getSource();
            
            jlName.setText( "Sample" ); // NOI18N
            jlName.setIcon( new ImageIcon() );
            
            jList.setFixedCellHeight(jlName.getPreferredSize().height);
            jList.setFixedCellWidth(jv.getExtentSize().width);
        }
        
        public void setColorPrefered( boolean colorPrefered ) {
            this.colorPrefered = colorPrefered;
        }

     }
        

}
