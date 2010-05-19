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

package org.netbeans.modules.wsdlextensions.scheduler.configeditor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.wsdlextensions.scheduler.utils.Utils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  sunsoabi_edwong
 */
public class SimpleDateFormatChooser extends JPanel {
    private static final long serialVersionUID = -3975722844450562931L;
    
    private DateTimeFormatComboBoxModel yearComboModel;
    private DateTimeFormatComboBoxModel monthComboModel;
    private DateTimeFormatComboBoxModel dayComboModel;
    private DateTimeFormatComboBoxModel hourComboModel;
    private DateTimeFormatComboBoxModel minuteComboModel;
    private DateTimeFormatComboBoxModel secondComboModel;
    private DateTimeFormatComboBoxModel millisecondComboModel;
    private DateTimeFormatComboBoxModel otherComboModel;
    private DateTimeFormatComboBoxModel standardComboModel;
    private DefaultComboBoxModel formatComboModel;
    
    private boolean userAddingFmt = false;
    
    public static final String PLAIN_TEXT_ITEM_PREFIX =
            "LBL_PLAIN_TEXT_ITEM_";                                     //NOI18N
    
    private static final String SAMPLE_TIMEZONE = "US/Pacific";         //NOI18N
    private static final Date SAMPLE_DATE;
    static {
        Calendar cal = Calendar.getInstance();
        cal.set(1982, 1, 3, 4, 5, 6);
        cal.set(Calendar.MILLISECOND, 78);
        cal.setTimeZone(TimeZone.getTimeZone(SAMPLE_TIMEZONE));
        SAMPLE_DATE = cal.getTime();
    }
    
    /** Creates new form SimpleDateFormatChooser */
    public SimpleDateFormatChooser() {
        super();
            
        yearComboModel = new DateTimeFormatComboBoxModel(new String[][] {
                {PLAIN_TEXT_ITEM_PREFIX + "SELECT", "LBL_SELECT_HINT"}, //NOI18N
                {"yy", "SDF_yy"},                                       //NOI18N
                {"yyyy", "SDF_yyyyy"},                                  //NOI18N
            });
        monthComboModel = new DateTimeFormatComboBoxModel(new String[][] {
                {PLAIN_TEXT_ITEM_PREFIX + "SELECT", "LBL_SELECT_HINT"}, //NOI18N
                {"M", "SDF_M"},                                         //NOI18N
                {"MM", "SDF_MM"},                                       //NOI18N
                {"MMM", "SDF_MMM"},                                     //NOI18N
                {"MMMM", "SDF_MMMM"},                                   //NOI18N
            });
        dayComboModel = new DateTimeFormatComboBoxModel(new String[][] {
                {PLAIN_TEXT_ITEM_PREFIX + "SELECT", "LBL_SELECT_HINT"}, //NOI18N
                {"d", "SDF_d"},                                         //NOI18N
                {"dd", "SDF_dd"},                                       //NOI18N
                {"D", "SDF_D"},                                         //NOI18N
                {"DDD", "SDF_DDD"},                                     //NOI18N
                {"F", "SDF_F"},                                         //NOI18N
                {"EEE", "SDF_EEE"},                                     //NOI18N
                {"EEEE", "SDF_EEEE"},                                   //NOI18N
            });
        hourComboModel = new DateTimeFormatComboBoxModel(new String[][] {
                {PLAIN_TEXT_ITEM_PREFIX + "SELECT", "LBL_SELECT_HINT"}, //NOI18N
                {"h", "SDF_h"},                                         //NOI18N
                {"hh", "SDF_hh"},                                       //NOI18N
                {"K", "SDF_K"},                                         //NOI18N
                {"KK", "SDF_KK"},                                       //NOI18N
                {"k", "SDF_k"},                                         //NOI18N
                {"kk", "SDF_kk"},                                       //NOI18N
                {"H", "SDF_H"},                                         //NOI18N
                {"HH", "SDF_HH"},                                       //NOI18N
            });
        minuteComboModel = new DateTimeFormatComboBoxModel(new String[][] {
                {PLAIN_TEXT_ITEM_PREFIX + "SELECT", "LBL_SELECT_HINT"}, //NOI18N
                {"m", "SDF_m"},                                         //NOI18N
                {"mm", "SDF_mm"},                                       //NOI18N
            });
        secondComboModel = new DateTimeFormatComboBoxModel(new String[][] {
                {PLAIN_TEXT_ITEM_PREFIX + "SELECT", "LBL_SELECT_HINT"}, //NOI18N
                {"s", "SDF_s"},                                         //NOI18N
                {"ss", "SDF_ss"},                                       //NOI18N
            });
        millisecondComboModel = new DateTimeFormatComboBoxModel(new String[][] {
                {PLAIN_TEXT_ITEM_PREFIX + "SELECT", "LBL_SELECT_HINT"}, //NOI18N
                {"S", "SDF_S"},                                         //NOI18N
                {"SSS", "SDF_SSS"},                                     //NOI18N
            });
        otherComboModel = new DateTimeFormatComboBoxModel(new String[][] {
                {PLAIN_TEXT_ITEM_PREFIX + "SELECT", "LBL_SELECT_HINT"}, //NOI18N
                {"G", "SDF_G"},                                         //NOI18N
                {"w", "SDF_w"},                                         //NOI18N
                {"W", "SDF_W"},                                         //NOI18N
                {"a", "SDF_a"},                                         //NOI18N
                {"z", "SDF_z"},                                         //NOI18N
                {"zzzz", "SDF_zzzz"},                                   //NOI18N
                {"Z", "SDF_Z"},                                         //NOI18N
            });
        standardComboModel = new DateTimeFormatComboBoxModel(new String[][] {
                {PLAIN_TEXT_ITEM_PREFIX + "SELECT", "LBL_SELECT_HINT"}, //NOI18N
                {(new SimpleDateFormat()).toPattern(), "SDF_DEFAULT_LOCALE"},
                {"yyyy-MM-dd'T'HH:mm:ss.SSSZ", "SDF_W3C_XML_DATETIME"},
            });
        formatComboModel = new DefaultComboBoxModel();
        
        initComponents();
        postInitComponents();
        
        reset();
    }
    
    private void postInitComponents() {
        comYear.setRenderer(new DateTimeFormatComboBoxRenderer(
                yearComboModel));
        comMonth.setRenderer(new DateTimeFormatComboBoxRenderer(
                monthComboModel));
        comDay.setRenderer(new DateTimeFormatComboBoxRenderer(
                dayComboModel));
        comHour.setRenderer(new DateTimeFormatComboBoxRenderer(
                hourComboModel));
        comMinute.setRenderer(new DateTimeFormatComboBoxRenderer(
                minuteComboModel));
        comSecond.setRenderer(new DateTimeFormatComboBoxRenderer(
                secondComboModel));
        comMillisecond.setRenderer(new DateTimeFormatComboBoxRenderer(
                millisecondComboModel));
        comOther.setRenderer(new DateTimeFormatComboBoxRenderer(
                otherComboModel));
        comStandard.setRenderer(new DateTimeFormatComboBoxRenderer(
                standardComboModel));
    }
    
    public void reset() {
        reset(true);
    }
    
    public void reset(boolean result) {
        comYear.setSelectedIndex(0);
        comMonth.setSelectedIndex(0);
        comDay.setSelectedIndex(0);
        comHour.setSelectedIndex(0);
        comMinute.setSelectedIndex(0);
        comSecond.setSelectedIndex(0);
        comMillisecond.setSelectedIndex(0);
        comOther.setSelectedIndex(0);
        comStandard.setSelectedIndex(0);
        if (result) {
            comFormat.setSelectedItem(null);
            txfSample.setText(null);
        }
        comFormat.requestFocusInWindow();
        if ((comFormat.getEditor().getEditorComponent()
                instanceof JTextComponent)
                && (comFormat.getSelectedItem() != null)) {
            ((JTextComponent) comFormat.getEditor().getEditorComponent())
                    .setCaretPosition(comFormat.getSelectedItem()
                            .toString().length());
        }
    }
    
    public String getSimpleDateFormatPattern() {
        return (String) comFormat.getSelectedItem();
    }
    
    public void setSimpleDateFormatPattern(String format) {
        comFormat.setSelectedItem(format);
    }
    
    public void addSimpleDateFormatPattern(String format) {
        if (indexOfSimpleDateFormatPattern(format) < 0) {
            formatComboModel.addElement(format);
        }
    }
    
    public int indexOfSimpleDateFormatPattern(String format) {
        return formatComboModel.getIndexOf(format);
    }
    
    public void showDialog(final String currPattern, final JComboBox comboBox,
            String title) {
        reset(false);
        final DialogDescriptor dd = new DialogDescriptor(this, title);
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                final DateTimeFormatComboBoxModel dateTimeFormatComboBoxModel =
                        (DateTimeFormatComboBoxModel) comboBox.getModel();
                final Object ans = DialogDisplayer.getDefault().notify(dd);
                Utils.callFromEDT(false, new Runnable() {
                    public void run() {
                        boolean revert = true;
                        if (DialogDescriptor.OK_OPTION.equals(ans)) {
                            String nuPattern = getSimpleDateFormatPattern();
                            if (!Utils.isEmpty(nuPattern)) {
                                int nuIdx = dateTimeFormatComboBoxModel
                                        .indexOfSimpleDateFormat(nuPattern);
                                if (-1 == nuIdx) {
                                    dateTimeFormatComboBoxModel
                                            .addSimpleDateFormat(nuPattern);
                                } else {
                                    dateTimeFormatComboBoxModel
                                            .setSelectedItem(nuIdx);
                                }
                                
                                nuIdx = indexOfSimpleDateFormatPattern(
                                        nuPattern);
                                if (-1 == nuIdx) {
                                    addSimpleDateFormatPattern(nuPattern);
                                }
                                
                                revert = false;
                            }
                        }

                        if (revert) {
                            int oldIdx = (currPattern != null)
                                ? dateTimeFormatComboBoxModel
                                    .indexOfSimpleDateFormat(currPattern)
                                : -1;
                            if (oldIdx > -1) {
                                dateTimeFormatComboBoxModel
                                        .setSelectedItem(oldIdx);
                            } else {
                                // Reset back to default
                                oldIdx = dateTimeFormatComboBoxModel
                                    .indexOfSimpleDateFormat(DefineTriggersPanel
                                        .getDefaultDateFormat());
                                dateTimeFormatComboBoxModel
                                        .setSelectedItem(oldIdx);
                            }
                        }
                    }
                });
            }
        });
    }
    
    private static class DateFormatSample {
        private String sample;
        private String pattern;
        private String description;
        
        public DateFormatSample(String sample, String pattern,
                String description) {
            super();
            this.sample = sample;
            this.pattern = pattern;
            this.description = description;
        }
        
        public String getSample() {
            return sample;
        }
        
        public String getPattern() {
            return pattern;
        }
        
        public String getDescription() {
            return description;
        }

        @Override
        public boolean equals(Object obj) {
            return ((obj instanceof DateFormatSample)
                    && (pattern != null
                        ? pattern.equals(((DateFormatSample) obj).pattern)
                        : ((DateFormatSample) obj).pattern == null));
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 79 * hash + (pattern != null ? pattern.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return NbBundle.getMessage(SimpleDateFormatChooser.class,
                    "STR_FOR_EXAMPLE",                                  //NOI18N
                    NbBundle.getMessage(SimpleDateFormatChooser.class,
                            (getDescription() != null) ? getDescription()
                                        : "LBL_CUSTOM"),                //NOI18N
                    getSample());
        }
    }
    
    public static class DateTimeFormatComboBoxModel
            extends DefaultComboBoxModel {
        private static final long serialVersionUID = 7649343665435985272L;
        
        private List<SimpleDateFormat> simpleDateFormats =
                new ArrayList<SimpleDateFormat>();
        private List<String> simpleDateFormatMeanings =
                new ArrayList<String>();
        
        public DateTimeFormatComboBoxModel(String[][] sdfPatterns) {
            super(new Vector<String>());
            
            for (String ps[] : sdfPatterns) {
                addSimpleDateFormatPattern(ps[0], ps[1]);
                simpleDateFormatMeanings.add(ps[1]);
            }
        }
        
        public Object addSimpleDateFormatPattern(String pattern,
                String description) {
            Object item = prepareSimpleDateFormatPattern(pattern, description);
            addElement(item);
            return item;
        }
        
        public Object prepareSimpleDateFormatPattern(String pattern,
                String description) {
            Object item = null;
            if (pattern.startsWith(PLAIN_TEXT_ITEM_PREFIX)) {
                simpleDateFormats.add(null);
                item = NbBundle.getMessage(SimpleDateFormatChooser.class,
                        pattern);
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                simpleDateFormats.add(sdf);
                item = new DateFormatSample(formatSampleDate(sdf),
                        pattern, description);
            }
            return item;
        }
        
        public static String formatSampleDate(SimpleDateFormat sdf) {
            sdf.setTimeZone(TimeZone.getTimeZone(SAMPLE_TIMEZONE));
            return sdf.format(SAMPLE_DATE);
        }

        @Override
        public void setSelectedItem(Object anObject) {
            super.setSelectedItem(anObject);
        }
        
        public void setSelectedItem(int idx) {
            if ((idx < 0) || (idx >= getSize())) {
                setSelectedItem(null);
            } else {
                setSelectedItem(getElementAt(idx));
            }
        }
        
        public int getSelectedIndex() {
            Object obj = getSelectedItem();
            return (obj != null) ? getIndexOf(obj) : -1;
        }
        
        public SimpleDateFormat getSelectedSimpleDateFormat() {
            int selectedIndex = getSelectedIndex();
            return ((selectedIndex < 0)
                    || (selectedIndex >= getSize())) ? null
                            : simpleDateFormats.get(selectedIndex);
        }
        
        public String getDateTimeSample(int index) {
            return getElementAt(index).toString();
        }
        
        public String getSimpleDateFormatPattern(int index) {
            return ((index < 0)
                    || (index >= simpleDateFormats.size())
                    || (null == simpleDateFormats.get(index)))
                            ? null : simpleDateFormats.get(index).toPattern();
        }
        
        public String getSimpleDateFormatMeaning(int index) {
            return ((index < 0)
                    || (index >= simpleDateFormatMeanings.size()))
                            ? "LBL_CUSTOM"                              //NOI18N
                            : simpleDateFormatMeanings.get(index);
        }
        
        public int indexOfSimpleDateFormat(String sdfmt) {
            int i = -1;
            for (SimpleDateFormat s : simpleDateFormats) {
                i++;
                if (null == s) {
                    continue;
                }
                if (s.toPattern().equals(sdfmt)) {
                    return i;
                }
            }
            return -1;
        }
        
        public void addSimpleDateFormat(String sdfmt) {
            int existing = indexOfSimpleDateFormat(sdfmt);
            if (existing != -1) {
                setSelectedItem(existing);
                return;
            }
            Object item = addSimpleDateFormatPattern(sdfmt, null);
            setSelectedItem(item);
        }
        
        public void insertSimpleDateFormatAt(String sdfmt, int index) {
            int existing = indexOfSimpleDateFormat(sdfmt);
            if (existing != -1) {
                setSelectedItem(index);
                return;
            }
            Object item = prepareSimpleDateFormatPattern(sdfmt, null);
            insertElementAt(item, index);
        }
    }
    
    public static class DateTimeFormatComboBoxRenderer
            extends BasicComboBoxRenderer {
        private static final long serialVersionUID = -5959271442020622101L;
        
        private DateTimeFormatComboBoxModel dateTimeFormatComboBoxModel;

        public DateTimeFormatComboBoxRenderer(DateTimeFormatComboBoxModel
                dateTimeFormatComboBoxModel) {
            super();
            
            this.dateTimeFormatComboBoxModel = dateTimeFormatComboBoxModel;
        }
        
        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
                if (index >= 0) {
                    String pattern = dateTimeFormatComboBoxModel
                            .getSimpleDateFormatPattern(index);
                    if (pattern != null) {
                        String tooltip = NbBundle.getMessage(
                                    SimpleDateFormatChooser.class,
                                    "LBL_SIMPLEDATEFORMAT_PATTERN",     //NOI18N
                                    pattern);
                        list.setToolTipText(tooltip);
                    } else {
                        list.setToolTipText(null);
                    }
                }
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setFont(list.getFont());
            setText((value == null) ? "" : value.toString());           //NOI18N
            setHorizontalAlignment(SwingConstants.LEFT);
            return this;
        }
        
        public Dimension getMaxListCellRendererDim() {
            if (null == dateTimeFormatComboBoxModel) {
                return null;
            }
            Dimension result = null;
            for (int i = 0; i < dateTimeFormatComboBoxModel.getSize(); i++) {
                String text = dateTimeFormatComboBoxModel.getElementAt(i)
                        .toString();
                JLabel label = new JLabel(text);
                Dimension dim = label.getPreferredSize();
                result = (result != null) ? Utils.max(result, dim) : dim;
            }
            return result;
        }
    }

    private void addSimpleDateFormatToken(final Object source) {
        if (userAddingFmt) {
            return;
        }
        
        try {
            userAddingFmt = true;
            String nuFormat = null;
            if (source == comYear) {
                if (comYear.getSelectedIndex() > 0) {
                    nuFormat = yearComboModel.getSimpleDateFormatPattern(
                            comYear.getSelectedIndex());
                }
            } else if (source == comMonth) {
                if (comMonth.getSelectedIndex() > 0) {
                    nuFormat = monthComboModel.getSimpleDateFormatPattern(
                            comMonth.getSelectedIndex());
                }
            } else if (source == comDay) {
                if (comDay.getSelectedIndex() > 0) {
                    nuFormat = dayComboModel.getSimpleDateFormatPattern(
                            comDay.getSelectedIndex());
                }
            } else if (source == comHour) {
                if (comHour.getSelectedIndex() > 0) {
                    nuFormat = hourComboModel.getSimpleDateFormatPattern(
                            comHour.getSelectedIndex());
                }
            } else if (source == comMinute) {
                if (comMinute.getSelectedIndex() > 0) {
                    nuFormat = minuteComboModel.getSimpleDateFormatPattern(
                            comMinute.getSelectedIndex());
                }
            } else if (source == comSecond) {
                if (comSecond.getSelectedIndex() > 0) {
                    nuFormat = secondComboModel.getSimpleDateFormatPattern(
                            comSecond.getSelectedIndex());
                }
            } else if (source == comMillisecond) {
                if (comMillisecond.getSelectedIndex() > 0) {
                    nuFormat = millisecondComboModel.getSimpleDateFormatPattern(
                            comMillisecond.getSelectedIndex());
                }
            } else if (source == comOther) {
                if (comOther.getSelectedIndex() > 0) {
                    nuFormat = otherComboModel.getSimpleDateFormatPattern(
                            comOther.getSelectedIndex());
                }
            } else if (source == comStandard) {
                if (comStandard.getSelectedIndex() > 0) {
                    nuFormat = standardComboModel.getSimpleDateFormatPattern(
                            comStandard.getSelectedIndex());
                }
            }

            if (nuFormat != null) {
                String format = getSimpleDateFormatPattern();
                if (null == format) {
                    format = "";                                        //NOI18N
                }
                String before = format;
                String after = "";                                      //NOI18N
                int caret = -1;
                if (comFormat.getEditor().getEditorComponent()
                        instanceof JTextComponent) {
                    caret = ((JTextComponent) comFormat.getEditor()
                            .getEditorComponent()).getCaretPosition();
                    before = format.substring(0, caret);
                    after = format.substring(caret);
                }
                format = before + nuFormat + after;
                comFormat.setSelectedItem(format);
                comFormat.requestFocusInWindow();
                if (caret != -1) {
                    ((JTextComponent) comFormat.getEditor()
                            .getEditorComponent()).setCaretPosition(
                                    caret + nuFormat.length());
                }
            }
        } finally {
            userAddingFmt = false;
        }
    }

    private void updateSimpleDateFormatSample() {
        Utils.callFromEDT(true, new Runnable() {
            public void run() {
                String format = getSimpleDateFormatPattern();
                if ((null == format) || (format.trim().length() == 0)) {
                    txfSample.setText(null);
                } else {
                    String sample = DateTimeFormatComboBoxModel
                            .formatSampleDate(new SimpleDateFormat(format));
                    txfSample.setText(sample);
                }
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblStyle = new javax.swing.JLabel();
        sepStyle = new javax.swing.JSeparator();
        lblYear = new javax.swing.JLabel();
        comYear = new javax.swing.JComboBox();
        lblMonth = new javax.swing.JLabel();
        comMonth = new javax.swing.JComboBox();
        lblDay = new javax.swing.JLabel();
        comDay = new javax.swing.JComboBox();
        lblHour = new javax.swing.JLabel();
        comHour = new javax.swing.JComboBox();
        lblMinute = new javax.swing.JLabel();
        comMinute = new javax.swing.JComboBox();
        lblSecond = new javax.swing.JLabel();
        comSecond = new javax.swing.JComboBox();
        lblMillisecond = new javax.swing.JLabel();
        comMillisecond = new javax.swing.JComboBox();
        lblOther = new javax.swing.JLabel();
        comOther = new javax.swing.JComboBox();
        lblStandard = new javax.swing.JLabel();
        comStandard = new javax.swing.JComboBox();
        lblResult = new javax.swing.JLabel();
        sepResult = new javax.swing.JSeparator();
        lblFormat = new javax.swing.JLabel();
        comFormat = new javax.swing.JComboBox();
        lblSample = new javax.swing.JLabel();
        txfSample = new javax.swing.JTextField();
        lblLiteralQuoting = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(lblStyle, NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblStyle.text")); // NOI18N

        lblYear.setLabelFor(comYear);
        org.openide.awt.Mnemonics.setLocalizedText(lblYear, NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblYear.text")); // NOI18N
        lblYear.setToolTipText(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblYear.toolTipText")); // NOI18N

        comYear.setModel(yearComboModel);
        comYear.setToolTipText(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblYear.toolTipText")); // NOI18N
        comYear.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comYearItemStateChanged(evt);
            }
        });

        lblMonth.setLabelFor(comMonth);
        org.openide.awt.Mnemonics.setLocalizedText(lblMonth, NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblMonth.text")); // NOI18N
        lblMonth.setToolTipText(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblMonth.toolTipText")); // NOI18N

        comMonth.setModel(monthComboModel);
        comMonth.setToolTipText(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblMonth.toolTipText")); // NOI18N
        comMonth.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comMonthItemStateChanged(evt);
            }
        });

        lblDay.setLabelFor(comDay);
        org.openide.awt.Mnemonics.setLocalizedText(lblDay, NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblDay.text")); // NOI18N
        lblDay.setToolTipText(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblDay.toolTipText")); // NOI18N

        comDay.setModel(dayComboModel);
        comDay.setToolTipText(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblDay.toolTipText")); // NOI18N
        comDay.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comDayItemStateChanged(evt);
            }
        });

        lblHour.setLabelFor(comHour);
        org.openide.awt.Mnemonics.setLocalizedText(lblHour, NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblHour.text")); // NOI18N
        lblHour.setToolTipText(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblHour.toolTipText")); // NOI18N

        comHour.setModel(hourComboModel);
        comHour.setToolTipText(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblHour.toolTipText")); // NOI18N
        comHour.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comHourItemStateChanged(evt);
            }
        });

        lblMinute.setLabelFor(comMinute);
        org.openide.awt.Mnemonics.setLocalizedText(lblMinute, NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblMinute.text")); // NOI18N
        lblMinute.setToolTipText(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblMinute.toolTipText")); // NOI18N

        comMinute.setModel(minuteComboModel);
        comMinute.setToolTipText(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblMinute.toolTipText")); // NOI18N
        comMinute.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comMinuteItemStateChanged(evt);
            }
        });

        lblSecond.setLabelFor(comSecond);
        org.openide.awt.Mnemonics.setLocalizedText(lblSecond, NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblSecond.text")); // NOI18N
        lblSecond.setToolTipText(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblSecond.toolTipText")); // NOI18N

        comSecond.setModel(secondComboModel);
        comSecond.setToolTipText(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblSecond.toolTipText")); // NOI18N
        comSecond.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comSecondItemStateChanged(evt);
            }
        });

        lblMillisecond.setLabelFor(comMillisecond);
        org.openide.awt.Mnemonics.setLocalizedText(lblMillisecond, NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblMillisecond.text")); // NOI18N
        lblMillisecond.setToolTipText(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblMillisecond.toolTipText")); // NOI18N

        comMillisecond.setModel(millisecondComboModel);
        comMillisecond.setToolTipText(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblMillisecond.toolTipText")); // NOI18N
        comMillisecond.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comMillisecondItemStateChanged(evt);
            }
        });

        lblOther.setLabelFor(comOther);
        org.openide.awt.Mnemonics.setLocalizedText(lblOther, NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblOther.text")); // NOI18N
        lblOther.setToolTipText(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblOther.toolTipText")); // NOI18N

        comOther.setModel(otherComboModel);
        comOther.setToolTipText(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblOther.toolTipText")); // NOI18N
        comOther.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comOtherItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblStandard, NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblStandard.text")); // NOI18N
        lblStandard.setToolTipText(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.comStandard.toolTipText")); // NOI18N

        comStandard.setModel(standardComboModel);
        comStandard.setToolTipText(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.comStandard.toolTipText")); // NOI18N
        comStandard.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comStandardItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblResult, NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblResult.text")); // NOI18N

        lblFormat.setLabelFor(comFormat);
        org.openide.awt.Mnemonics.setLocalizedText(lblFormat, NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblFormat.text")); // NOI18N
        lblFormat.setToolTipText(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblFormat.toolTipText")); // NOI18N

        comFormat.setEditable(true);
        comFormat.setModel(formatComboModel);
        comFormat.setToolTipText(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblFormat.toolTipText")); // NOI18N
        comFormat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comFormatActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblSample, NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblSample.text")); // NOI18N
        lblSample.setToolTipText(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblSample.toolTipText")); // NOI18N

        txfSample.setEditable(false);
        txfSample.setText(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.txfSample.text")); // NOI18N
        txfSample.setToolTipText(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblSample.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblLiteralQuoting, NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblLiteralQuoting.text")); // NOI18N
        lblLiteralQuoting.setToolTipText(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblLiteralQuoting.toolTipText")); // NOI18N
        lblLiteralQuoting.setEnabled(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(lblResult)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(sepResult, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 566, Short.MAX_VALUE))
                            .add(layout.createSequentialGroup()
                                .add(lblStyle)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(sepStyle, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE))
                            .add(layout.createSequentialGroup()
                                .add(10, 10, 10)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(lblYear)
                                            .add(lblHour))
                                        .add(31, 31, 31)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                            .add(comHour, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .add(comYear, 0, 132, Short.MAX_VALUE)))
                                    .add(layout.createSequentialGroup()
                                        .add(lblMillisecond)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(comMillisecond, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .add(18, 18, 18)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(lblMinute)
                                    .add(lblMonth)
                                    .add(lblOther))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(comMinute, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(comOther, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(comMonth, 0, 138, Short.MAX_VALUE))
                                .add(18, 18, 18)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(lblSecond)
                                    .add(lblDay)
                                    .add(lblStandard))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(comSecond, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(comStandard, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(comDay, 0, 133, Short.MAX_VALUE)))))
                    .add(layout.createSequentialGroup()
                        .add(20, 20, 20)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(lblSample)
                            .add(lblFormat))
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, txfSample, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
                            .add(lblLiteralQuoting)
                            .add(layout.createSequentialGroup()
                                .add(comFormat, 0, 534, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(lblStyle)
                    .add(sepStyle, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lblYear)
                            .add(comYear, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lblHour)
                            .add(comHour, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lblMillisecond)
                            .add(comMillisecond, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lblMonth)
                            .add(comMonth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lblMinute)
                            .add(comMinute, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lblOther)
                            .add(comOther, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(28, 28, 28)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(lblSecond)
                                    .add(comSecond, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(lblDay)
                                .add(comDay, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(lblStandard)
                            .add(comStandard, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .add(11, 11, 11)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(lblResult)
                    .add(sepResult, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblLiteralQuoting)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(comFormat, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblFormat))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(txfSample, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblSample))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblYear.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblYear.text")); // NOI18N
        lblYear.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblYear.toolTipText")); // NOI18N
        lblMonth.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblMonth.text")); // NOI18N
        lblMonth.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblMonth.toolTipText")); // NOI18N
        lblDay.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblDay.text")); // NOI18N
        lblDay.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblDay.toolTipText")); // NOI18N
        lblHour.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblHour.text")); // NOI18N
        lblHour.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblHour.toolTipText")); // NOI18N
        lblMinute.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblMinute.text")); // NOI18N
        lblMinute.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblMinute.toolTipText")); // NOI18N
        lblSecond.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblSecond.text")); // NOI18N
        lblSecond.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblSecond.toolTipText")); // NOI18N
        lblMillisecond.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblMillisecond.text")); // NOI18N
        lblMillisecond.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblMillisecond.toolTipText")); // NOI18N
        lblOther.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblOther.text")); // NOI18N
        lblOther.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblOther.toolTipText")); // NOI18N
        lblStandard.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblStandard.text")); // NOI18N
        lblStandard.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.comStandard.toolTipText")); // NOI18N
        lblFormat.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblFormat.text")); // NOI18N
        lblFormat.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblFormat.toolTipText")); // NOI18N
        lblSample.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblSample.text")); // NOI18N
        lblSample.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblSample.toolTipText")); // NOI18N
        lblLiteralQuoting.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblLiteralQuoting.text")); // NOI18N
        lblLiteralQuoting.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SimpleDateFormatChooser.class, "SimpleDateFormatChooser.lblLiteralQuoting.toolTipText")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void comFormatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comFormatActionPerformed
    updateSimpleDateFormatSample();
}//GEN-LAST:event_comFormatActionPerformed

private void comYearItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comYearItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        addSimpleDateFormatToken(evt.getSource());
    }
}//GEN-LAST:event_comYearItemStateChanged

private void comMonthItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comMonthItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        addSimpleDateFormatToken(evt.getSource());
    }
}//GEN-LAST:event_comMonthItemStateChanged

private void comDayItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comDayItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        addSimpleDateFormatToken(evt.getSource());
    }
}//GEN-LAST:event_comDayItemStateChanged

private void comHourItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comHourItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        addSimpleDateFormatToken(evt.getSource());
    }
}//GEN-LAST:event_comHourItemStateChanged

private void comMinuteItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comMinuteItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        addSimpleDateFormatToken(evt.getSource());
    }
}//GEN-LAST:event_comMinuteItemStateChanged

private void comSecondItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comSecondItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        addSimpleDateFormatToken(evt.getSource());
    }
}//GEN-LAST:event_comSecondItemStateChanged

private void comMillisecondItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comMillisecondItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        addSimpleDateFormatToken(evt.getSource());
    }
}//GEN-LAST:event_comMillisecondItemStateChanged

private void comOtherItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comOtherItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        addSimpleDateFormatToken(evt.getSource());
    }
}//GEN-LAST:event_comOtherItemStateChanged

private void comStandardItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comStandardItemStateChanged
    if (evt.getStateChange() == ItemEvent.SELECTED) {
        addSimpleDateFormatToken(evt.getSource());
    }
}//GEN-LAST:event_comStandardItemStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox comDay;
    private javax.swing.JComboBox comFormat;
    private javax.swing.JComboBox comHour;
    private javax.swing.JComboBox comMillisecond;
    private javax.swing.JComboBox comMinute;
    private javax.swing.JComboBox comMonth;
    private javax.swing.JComboBox comOther;
    private javax.swing.JComboBox comSecond;
    private javax.swing.JComboBox comStandard;
    private javax.swing.JComboBox comYear;
    private javax.swing.JLabel lblDay;
    private javax.swing.JLabel lblFormat;
    private javax.swing.JLabel lblHour;
    private javax.swing.JLabel lblLiteralQuoting;
    private javax.swing.JLabel lblMillisecond;
    private javax.swing.JLabel lblMinute;
    private javax.swing.JLabel lblMonth;
    private javax.swing.JLabel lblOther;
    private javax.swing.JLabel lblResult;
    private javax.swing.JLabel lblSample;
    private javax.swing.JLabel lblSecond;
    private javax.swing.JLabel lblStandard;
    private javax.swing.JLabel lblStyle;
    private javax.swing.JLabel lblYear;
    private javax.swing.JSeparator sepResult;
    private javax.swing.JSeparator sepStyle;
    private javax.swing.JTextField txfSample;
    // End of variables declaration//GEN-END:variables

}
