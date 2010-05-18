/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.vmd.componentssupport.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import org.openide.ErrorManager;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author avk
 */
public class IconUtils {

    private static final String MSG_WRN_ICON_EMPTY 
                                              = "MSG_NoIcon";          // NOI18N 
    private static final String MSG_WRN_ICON_WRONG_SIZE 
                                              = "MSG_WrongIconSize";          // NOI18N 
    private static final String TITLE_ICON_DIALOG 
                                              = "TITLE_IconDialog";          // NOI18N 
    private static final String LBL_ICON_INFO 
                                              = "LBL_IconInfo";          // NOI18N 


    /**
     * looks for list of files with the same name prefix and extension as given, but with one 
     * of the following suffixes: "24", "16". Looks in the same dir.
     * @param iconPath known icon path to use as a pattern
     * @return list of files if the exists.
     */
    public static Set<File> getPossibleIcons(final String iconPath) {
        String[] resultSuffixes = { "16", "24", "" }; // NOI18N
        Set<File> results = new HashSet<File>();
        
        File icon = new File(iconPath);
        String iconName = icon.getName();
        int idx = iconName.lastIndexOf('.');
        String name = (idx != -1) ? iconName.substring(0,idx) : iconName;
        String extension = (idx != -1) ? iconName.substring(idx+1) : null;
        boolean hasSuffix = (name.endsWith("24")) || (name.endsWith("16"));//NOI18N
        name = hasSuffix ? name.substring(0,name.length()-2) : name;
        for (int i = 0; i < resultSuffixes.length; i++) {
            String resultSuffix = resultSuffixes[i];
            String resultName = name + resultSuffix;
            if (extension != null) {
                resultName = resultName + '.' + extension;
            }
            File f = new File(icon.getParentFile(),resultName);
            if (f.exists()) {
                results.add(f);
            }
        }        
        return results;
    }

    
    
    /**
     * @param icon file representing icon
     * @param expectedWidth expected width
     * @param expectedHeight expected height
     * @return true if icon corresponds to expected dimension
     */
    public static boolean isValidIcon(final File icon, int expectedWidth, int expectedHeight) {
        Dimension iconDimension = getIconDimension(icon);
        return (expectedWidth == iconDimension.getWidth() &&
                expectedHeight == iconDimension.getHeight());
    }

    public static String getNoIconMessage(int width, int height){
            return NbBundle.getMessage(IconUtils.class, 
                    MSG_WRN_ICON_EMPTY, new Object[]{width, height});
    }
    
    public static String getIconDimensionMessage(File icon, 
            int expectedWidth, int expectedHeight) 
    {
        Dimension real = new Dimension(getIconDimension(icon));
        if (real.height == expectedHeight && real.width == expectedWidth) {
            return "";
        }
        return NbBundle.getMessage(IconUtils.class,
                MSG_WRN_ICON_WRONG_SIZE, new Object[]{
                    real.width,
                    real.height,
                    expectedWidth,
                    expectedHeight
                });
    }

    /**
     * @param icon file representing icon
     * @return width and height of icon encapsulated into {@link java.awt.Dimension}
     */
    public static Dimension getIconDimension(final File icon) {
        try {
            ImageIcon imc = new ImageIcon(icon.toURI().toURL());
            return new Dimension(imc.getIconWidth(), imc.getIconHeight());
        } catch (MalformedURLException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return new Dimension(-1, -1);
    }
    
    /**
     * Returns an instance of {@link javax.swing.JFileChooser} permitting
     * selection only a regular <em>icon</em>.
     */
    public static JFileChooser getIconFileChooser() {        
        if (iconChooser != null) {
            JFileChooser choose = iconChooser.get();
            if (choose != null) {
                return choose;
            }
        }
        final JFileChooser chooser = new IconFileChooser();        
        iconChooser = new WeakReference<JFileChooser>(chooser);
        return chooser;
    }

    /**
     * tries to set the selected file according to currently existing data.
     * Will se it only if the String represents a file path that exists.
     */
    public static JFileChooser getIconFileChooser(String oldValue) {
        JFileChooser chooser = getIconFileChooser();
        String iconText = oldValue.trim();
        if ( iconText.length() > 0) {
            File fil = new File(iconText);
            if (fil.exists()) {
                chooser.setSelectedFile(fil);
            }
        }
        return chooser;
    }
    
    private static class IconFileChooser extends JFileChooser {
        private final JTextField iconInfo = new JTextField();        
        private  IconFileChooser() {
            JPanel accessoryPanel = getAccesoryPanel(iconInfo);
            setDialogTitle(NbBundle.getMessage(IconUtils.class, TITLE_ICON_DIALOG));
            setAccessory(accessoryPanel);
            setAcceptAllFileFilterUsed(false);
            setFileSelectionMode(JFileChooser.FILES_ONLY);
            setMultiSelectionEnabled(false);
            addChoosableFileFilter(new IconFilter());
            setFileView(new FileView() {
                public @Override Icon getIcon(File f) {
                    // Show icons right in the chooser, to make it easier to find
                    // the right one.
                    if (f.getName().endsWith(".gif") || f.getName().endsWith(".png")) { // NOI18N
                        Icon icon = new ImageIcon(f.getAbsolutePath());
                        if (icon.getIconWidth() == 16 && icon.getIconHeight() == 16) {
                            return icon;
                        }
                    }
                    return null;
                }
                public @Override String getName(File f) {
                    File f2 = getSelectedFile();
                    if (f2 != null && (f2.getName().endsWith(".gif") || f2.getName().endsWith(".png"))) { // NOI18N
                        Icon icon = new ImageIcon(f2.getAbsolutePath());
                        StringBuffer sb = new StringBuffer();
                        sb.append(f2.getName()).append(" [");//NOI18N
                        sb.append(icon.getIconWidth()).append('x').append(icon.getIconHeight());
                        sb.append(']');
                        setApproveButtonToolTipText(sb.toString());
                        iconInfo.setText(sb.toString());
                    } else {
                        iconInfo.setText("");
                    }
                    return super.getName(f);
                }
                
            });            
        }
        
        private static JPanel getAccesoryPanel(final JTextField iconInfo) {
            iconInfo.setColumns(15);
            iconInfo.setEditable(false);
            
            JPanel accessoryPanel = new JPanel();
            JPanel inner = new JPanel();
            JLabel iconInfoLabel = new JLabel();
            accessoryPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 0));
            
            inner.setLayout(new GridLayout(2, 1, 0, 6));
            
            iconInfoLabel.setLabelFor(iconInfo);
            Mnemonics.setLocalizedText(iconInfoLabel, 
                    NbBundle.getMessage(IconUtils.class, "LBL_IconInfo"));
            inner.add(iconInfoLabel);
            
            inner.add(iconInfo);
            
            accessoryPanel.add(inner);
            return accessoryPanel;
        }
    }

    private static final class IconFilter extends FileFilter {
        public boolean accept(File pathname) {
            return pathname.isDirectory() ||
                    pathname.getName().toLowerCase(Locale.ENGLISH).endsWith("gif") || // NOI18N
                    pathname.getName().toLowerCase(Locale.ENGLISH).endsWith("png"); // NOI18N
        }
        public String getDescription() {
            return "*.gif, *.png"; // NOI18N
        }
    }
    
    private static Reference<JFileChooser> iconChooser;
}
