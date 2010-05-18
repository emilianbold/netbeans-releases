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
package org.netbeans.modules.visualweb.faces.dt.converter;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import java.awt.event.FocusAdapter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.Locale;
import javax.swing.JTextField;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
/*
 * NumberConverterCustomizerPanel.java
 *
 * Created on September 14, 2005, 5:53 PM
 */

/**
 * A panel for customizing the NumberConverter
 *
 * @author  jhoff, dongmei, gowri
 */
public class NumberConverterCustomizerPanel extends javax.swing.JPanel {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(NumberConverterCustomizerPanel.class);   // Used for Internationalizatio

    protected DesignProperty prop;
    protected DesignBean designBean;

    private Locale[] locales;

    // Supported types: number, currency, percent
    private static final String[] typeValues = { "number", "currency", "percent" }; // NOI18N
    private static final String[] typeDisplayNames = { NbBundle.getMessage(NumberConverterCustomizerPanel.class, "number"),
                                                       NbBundle.getMessage(NumberConverterCustomizerPanel.class, "currency"),
                                                       NbBundle.getMessage(NumberConverterCustomizerPanel.class, "percent") };

    public NumberConverterCustomizerPanel(DesignBean designBean) {

        this.designBean = designBean;
        String[] localeDisplayNames = getLocaleDisplayNames();

        // Initialize all the components in the panel
        initComponents();

        // Populate the pattern combo box with some sample patterns
        NumberFormat form1, form2, form3, form4;
        form1 = NumberFormat.getInstance();
        form2 = NumberFormat.getIntegerInstance();
        form3 = NumberFormat.getCurrencyInstance();
        form4 = NumberFormat.getPercentInstance();
        cmbPattern.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", ((DecimalFormat)form1).toPattern(), ((DecimalFormat)form2).toPattern(), ((DecimalFormat)form3).toPattern(), ((DecimalFormat)form4).toPattern() }));

        // Type combo box - Number, Currency, Percent
        cmbType.setModel( new javax.swing.DefaultComboBoxModel(typeDisplayNames) );

        // Populate the locale combo box
        cmbLocale.setModel( new javax.swing.DefaultComboBoxModel(localeDisplayNames) );

        // Populate the currency code
        cmbCurrencyCode.setModel( new javax.swing.DefaultComboBoxModel(ISO4217CurrencyCode.getDisplayNames() ) );

        // The Min/Max integer digits, Min/Max fractional digits
        String[] digitsArray = new String[] {"", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "30", "40", "50"};
        cmbMinInteger.setModel(new javax.swing.DefaultComboBoxModel(digitsArray));
        cmbMaxInteger.setModel(new javax.swing.DefaultComboBoxModel(digitsArray));
        cmbMinFractional.setModel(new javax.swing.DefaultComboBoxModel(digitsArray));
        cmbMaxFractional.setModel(new javax.swing.DefaultComboBoxModel(digitsArray));
        ((JTextField)cmbMinInteger.getEditor().getEditorComponent()).setHorizontalAlignment(JTextField.RIGHT);
        ((JTextField)cmbMaxInteger.getEditor().getEditorComponent()).setHorizontalAlignment(JTextField.RIGHT);
        ((JTextField)cmbMinFractional.getEditor().getEditorComponent()).setHorizontalAlignment(JTextField.RIGHT);
        ((JTextField)cmbMaxFractional.getEditor().getEditorComponent()).setHorizontalAlignment(JTextField.RIGHT);

        // Fill in the panel with the values from the designBean
        fillPanel();

        // The Example part of the panel
        cmbExample.setModel(new javax.swing.DefaultComboBoxModel(new String[] {"1234.56", "-1234.56", "123.4567", "0.123", "01234"}));

        cmbExample.getEditor().getEditorComponent().addFocusListener(new FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                cmbExampleFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                cmbExampleFocusLost(evt);
            }
        });
    }

    // Called by the Customizer
    public boolean isModified() {
        // TODO return true for now always
        return true;
    }

    /**
     * This method is called by the Customizer to get the customized values
     */
    public void customizerApply() {

        // Valid the user inputs first
        if( !validateUserInput() )
            return;

        // Now, set the user inputs to the design bean

        DesignProperty prop = null;

        if( rbType.isSelected() ) {
            // Type is selected
            prop = designBean.getProperty("type");    //NOI18N
            prop.setValue( typeValues[cmbType.getSelectedIndex()]);

            prop = designBean.getProperty("minIntegerDigits");    //NOI18N
            Integer minInt = getInteger((String)cmbMinInteger.getSelectedItem());
            if( minInt != null )
                prop.setValue( minInt );
            else
                prop.unset();

            prop = designBean.getProperty("maxIntegerDigits");    //NOI18N
            Integer maxInt = getInteger((String)cmbMaxInteger.getSelectedItem());
            if( maxInt != null )
                prop.setValue( maxInt );
            else
                prop.unset();

            prop = designBean.getProperty("minFractionDigits");    //NOI18N
            Integer minFrac = getInteger((String)cmbMinFractional.getSelectedItem());
            if( minFrac != null )
                prop.setValue(minFrac);
            else
                prop.unset();

            prop = designBean.getProperty("maxFractionDigits");    //NOI18N
            Integer maxFrac = getInteger((String)cmbMaxFractional.getSelectedItem());
            if( maxFrac != null )
                prop.setValue(maxFrac);
            else
                prop.unset();

            prop = designBean.getProperty("groupingUsed"); //NOI18N
            prop.setValue(new Boolean(cbUseGrouping.isSelected()));

            prop = designBean.getProperty("locale");    //NOI18N
            Locale selectedLocale = getLocaleBasedOnComboBoxIndex( cmbLocale.getSelectedIndex() );
            if( selectedLocale == null )
                prop.unset();
            else
                prop.setValue(selectedLocale);

            if( cmbType.getSelectedIndex() == 1) {    // currency
                if( rbSymbol.isSelected() ) {
                    String currencySymbol = txtEnterSymbol.getText().trim();

                    designBean.getProperty("currencySymbol").setValue( currencySymbol);    //NOI18N
                    designBean.getProperty("currencyCode").unset();    //NOI18N

                } else {
                    String currencyCode = null;

                    // If there is only one item in the combo box, then we can use the
                    // selected locale the figure out the currency code
                    if( cmbCurrencyCode.getModel().getSize() == 1 ) {
                        currencyCode = Currency.getInstance( selectedLocale ).getCurrencyCode();
                    } else {
                        //
                        int index = cmbCurrencyCode.getSelectedIndex();
                        currencyCode = ISO4217CurrencyCode.getCode( index );
                    }

                    designBean.getProperty("currencyCode").setValue( currencyCode);    //NOI18N
                    designBean.getProperty("currencySymbol").unset();    //NOI18N
                }
            } else {
                designBean.getProperty("currencyCode").unset();
                designBean.getProperty("currencySymbol").unset();
            }

            // Unset the pattern
            designBean.getProperty("pattern").unset();

        } else {
            // Pattern is used
            prop = designBean.getProperty("pattern");    //NOI18N
            prop.setValue(cmbPattern.getSelectedItem());

            // Unset the type related props
            designBean.getProperty("type").unset();
            designBean.getProperty("minIntegerDigits").unset();
            designBean.getProperty("maxIntegerDigits").unset();
            designBean.getProperty("minFractionDigits").unset();
            designBean.getProperty("maxFractionDigits").unset();
            designBean.getProperty("groupingUsed").unset();
            designBean.getProperty("currencyCode").unset();
            designBean.getProperty("currencySymbol").unset();
            designBean.getProperty("locale").unset();
        }

        // Integer Only applies to both Type and Pattern
        prop = designBean.getProperty("integerOnly");    //NOI18N
        prop.setValue(new Boolean(cbIntegerOnly.isSelected()));
    }

    private Locale getLocaleBasedOnComboBoxIndex( int index ) {
        // Since in the locale combo box, the first item is blank,
        // we need to adjust the index by 1 to the locale object

        if( index == 0 )
            return null;
        else
            return locales[index-1];
    }

    private Integer getInteger( String integerStr ) {
        if( integerStr != null && integerStr.trim().length() != 0 ) {
            int intNum = Integer.parseInt( integerStr.trim() );
            return new Integer( intNum );
        } else
            return null;
    }

    // Get the display names for all the available locales
    private String[] getLocaleDisplayNames() {
        locales = Locale.getAvailableLocales();

        // Sort the locales based on the display names
        Arrays.sort( locales, new LocaleComparator() );

        String[] displayNames = new String[locales.length+1];
        ArrayList currencyCodes = new ArrayList();

        // The first only is blank
        displayNames[0] = "";

        for( int i = 0; i < locales.length; i ++ ) {
            displayNames[i+1] = locales[i].getDisplayName();
        }

        return displayNames;
    }

       class LocaleComparator implements java.util.Comparator <Locale> {

           public int compare(Locale locale1, Locale locale2) {
               return locale1.getDisplayName().compareTo(locale2.getDisplayName());
           }
       }

    // Fill the panel with values from the design bean
    private void fillPanel() {

        DesignProperty prop = null;

        // Make sure the check is based on pattern, not type
        // Must be some bug from insync. The type is always set no matter what
        prop = designBean.getProperty("pattern");    //NOI18N
        if( prop.getValue() != null && ((String)prop.getValue()).trim().length() != 0 ) {
            // Pattern is used, then fill in the Pattern part of the panel
            rbPattern.setSelected( true );
            rbType.setSelected( false );
            prop = designBean.getProperty("pattern");    //NOI18N
            cmbPattern.setSelectedItem((String) prop.getValue());
        } else {
            /// Type is selected
            rbType.setSelected( true );
            rbPattern.setSelected( false );
        }

        // Fill in the type part of the panel
        fillTypePanel();

        // Integer Only applies to both Type and Pattern
        prop = designBean.getProperty("integerOnly");    //NOI18N
        Boolean bool = (Boolean)(prop.getValue());
        cbIntegerOnly.setSelected(bool.booleanValue());

        // Enable/disable the components based on what is selected
        enableTypePanel();
        enablePatternPanel();
    }

    // Fill in the type part of the panel. It is called when the type is selected
    private void fillTypePanel() {

        DesignProperty prop = null;

        // Which type is selected? Number? Currency? Or Percent
        String type = (String)designBean.getProperty( "type" ).getValue();
        int typeIndex = getTypeIndex( type );
        cmbType.setSelectedIndex( typeIndex );

        // Min/Max Integer Digits and Min/Max Fractional Digits
        Integer minInteger = (Integer)designBean.getProperty("minIntegerDigits").getValue();    //NOI18N
        cmbMinInteger.setSelectedItem( minInteger.toString() );

        Integer maxInteger = (Integer)designBean.getProperty("maxIntegerDigits").getValue();   //NOI18N
        cmbMaxInteger.setSelectedItem( maxInteger.toString() );

        Integer minFractional = (Integer)designBean.getProperty("minFractionDigits").getValue();    //NOI18N
        cmbMinFractional.setSelectedItem( minFractional.toString() );

        Integer maxFractional = (Integer)designBean.getProperty("maxFractionDigits").getValue();    //NOI18N
        cmbMaxFractional.setSelectedItem( maxFractional.toString() );

        // Use Grouping Separator
        Boolean groupingUsed = (Boolean)designBean.getProperty("groupingUsed").getValue(); // NOI18N
        cbUseGrouping.setSelected( groupingUsed.booleanValue() );

        // Which Locale is selected
        Locale locale = (Locale)designBean.getProperty("locale").getValue();    //NOI18N
        if( locale == null )
            locale = Locale.getDefault();

        cmbLocale.setSelectedItem( locale.getDisplayName() );

        // Fill in the currency symbol and code if possible
        if( typeIndex == 1 ) { // currency
            String currencyCode = (String)designBean.getProperty("currencyCode").getValue();
            String currencySymbol = (String)designBean.getProperty("currencySymbol").getValue();
            if( currencyCode != null ) {
                rbCurrencyCode.setSelected( true );
                cmbCurrencyCode.setSelectedItem( ISO4217CurrencyCode.getDisplayName(currencyCode));
            } else {
                rbSymbol.setSelected( true );
                txtEnterSymbol.setText( currencySymbol );
            }
        } else {
            // Fill in the symbol based on the locale selected
            try {
                Currency cur = Currency.getInstance( locale );
                cmbCurrencyCode.setSelectedItem( ISO4217CurrencyCode.getDisplayName(cur.getCurrencyCode()) );
                txtEnterSymbol.setText( cur.getSymbol() );
            } catch( IllegalArgumentException ie ) {
                // Happens if the locale does not have right country code
                // Then just use the defaults
            }

            // Have the currency code radio button selected by default
            rbCurrencyCode.setSelected( true );
            rbSymbol.setSelected( false );
        }

    }

    private int getTypeIndex( String typeValue ) {
        if( typeValue.equals( typeValues[0] ) ) {
            return 0;
        } else if( typeValue.equals( typeValues[1] ) ) {
            return 1;
        } else if( typeValue.equals( typeValues[2] ) ) {
            return 2;
        } else
            return 0;
    }

    // Validate the user entered values
    // This method will be called in customizerApply() when "Apply" is clicked and upateTestResult() when "Test" and
    // a new sample number is entered/selected
    private boolean validateUserInput() {
        boolean valid = true;
        StringBuffer msg = new StringBuffer();

        if( rbType.isSelected() ) {
            // First, make sure Min/Max integer/fractional digits are numbers (>=0)
            // Default min/max integer digits are 1/40 and min/max fractional digits are 0/3 if they are not set

            String numStr = null;
            int minInteger=0, maxInteger=0, minFractional=0, maxFractional=0;
            try {
                numStr = (String)cmbMinInteger.getSelectedItem();
                if( numStr != null && numStr.trim().length() != 0 )
                    minInteger = Integer.parseInt( numStr );
                else
                    minInteger = 1;
            } catch( NumberFormatException ne ) {
                valid = false;
                msg.append( NbBundle.getMessage(NumberConverterCustomizerPanel.class, "badMinIntegerDigits") );
            }

            try {
                numStr = (String)cmbMaxInteger.getSelectedItem();
                if( numStr != null && numStr.trim().length() != 0 )
                    maxInteger = Integer.parseInt( numStr );
                else
                    maxInteger = 40;
            } catch( NumberFormatException ne ) {
                valid = false;
                msg.append( NbBundle.getMessage(NumberConverterCustomizerPanel.class, "badMaxIntegerDigits") );
            }

            try {
                numStr = (String)cmbMaxFractional.getSelectedItem();
                if( numStr != null && numStr.trim().length() != 0 )
                    maxFractional = Integer.parseInt( numStr );
                else
                    maxFractional = 3;
            } catch( NumberFormatException ne ) {
                valid = false;
                msg.append( NbBundle.getMessage(NumberConverterCustomizerPanel.class, "badMaxFractionalDigits") );
            }

            try {
                numStr = (String)cmbMinFractional.getSelectedItem();
                if( numStr != null && numStr.trim().length() != 0 )
                    minFractional = Integer.parseInt( numStr );
                else
                    minFractional = 0;
            } catch( NumberFormatException ne ) {
                valid = false;
                msg.append( NbBundle.getMessage(NumberConverterCustomizerPanel.class, "badMinFractionalDigits") );
            }

            // TODO it is here is because insync take 0 as I want defaults
            if( maxInteger == 0 ) maxInteger = 40;
            if( minInteger == 0 ) minInteger = 1;
            if( maxFractional == 0 ) maxFractional = 3;
            if( minFractional == 0 ) minFractional = 0;

            // Now, make sure the max >= min
            if( maxInteger < minInteger ) {
                valid = false;
                msg.append( NbBundle.getMessage(NumberConverterCustomizerPanel.class, "badIntegerDigits") );
            }

            if( maxFractional < minFractional ) {
                valid = false;
                msg.append( NbBundle.getMessage(NumberConverterCustomizerPanel.class, "badFractionalDigits") );
            }

            // Currency Symbol
            if( cmbType.getSelectedIndex() == 1 && rbSymbol.isSelected() ) { // CURRENCY
                if( txtEnterSymbol.getText() == null || txtEnterSymbol.getText().trim().length() == 0 ) {
                    valid = false;
                    msg.append( NbBundle.getMessage(NumberConverterCustomizerPanel.class, "emptySymbol") );
                }
            }
        }

        if( rbPattern.isSelected() ) {

            // Make sure the pattern is not NULL and valid
            String pattern = (String)cmbPattern.getSelectedItem();

            if( pattern == null || pattern.trim().length() == 0 ) {
                valid = false;
                msg.append( NbBundle.getMessage(NumberConverterCustomizerPanel.class, "emptyPattern") );
            }

            try {
                ((DecimalFormat)DecimalFormat.getInstance()).applyPattern( ((String)cmbPattern.getSelectedItem()).trim() );
            } catch( IllegalArgumentException ie ) {
                valid = false;
                msg.append( NbBundle.getMessage(NumberConverterCustomizerPanel.class, "badPattern", pattern ) );
            }
        }

        if( !valid ) {
            // Notify the user
            NotifyDescriptor d = new NotifyDescriptor.Message( msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify( d );
        }

        return valid;
    }

    // Enable or disable the components in the type part of the panel appropriately based on the
    // selected values
    private void enableTypePanel() {
        boolean typeOn = rbType.isSelected();
        cmbType.setEnabled(typeOn);
        cmbMinInteger.setEnabled(typeOn);
        cmbMaxInteger.setEnabled(typeOn);
        cmbMinFractional.setEnabled(typeOn);
        cmbMaxFractional.setEnabled(typeOn);
        cmbLocale.setEnabled(typeOn);
        cbUseGrouping.setEnabled(typeOn);
        lblMinFractional.setEnabled(typeOn);
        lblMaxFractional.setEnabled(typeOn);
        lblMinInteger.setEnabled(typeOn);
        lblMaxInteger.setEnabled(typeOn);
        lblFractional.setEnabled(typeOn);
        lblInteger.setEnabled(typeOn);
        lblLocale.setEnabled(typeOn);
        lblChooseCurrency.setEnabled(typeOn);
        enableCurrencyCombos();
    }

    // Enable or disable the components in the pattern part of the panel appropriately based on the
    // selected values
    private void enablePatternPanel() {
        boolean patternOn = rbPattern.isSelected();
        cmbPattern.setEnabled(patternOn);
    }

    private void enableCurrencyCombos() {

        // The user can edit the symbol and select a currency code only if there is no locale specified
        // the specified locale doesn't have currency code and symbol
        boolean editableCurrency = false;

        // Update the currency code and symbol based on the selected locale if there is a locale is selected and the
        // selected locale has currency code and symbol
        Locale selectedLocale = getLocaleBasedOnComboBoxIndex( cmbLocale.getSelectedIndex() );
        if( selectedLocale == null ) {
            editableCurrency = true;
            // no locale specified, default to default locale
            selectedLocale = Locale.getDefault();
        }

        // We'll give our best guess what might be the currency code or symbol
        // The user can always modified from the UI
        String codeDisplayName = null;
        String symbol = null;

        try {
            // See whehther we can find the currency code/symbol for the selected locale
            Currency cur = Currency.getInstance( selectedLocale );
            codeDisplayName = ISO4217CurrencyCode.getDisplayName( cur.getCurrencyCode() );
            symbol = cur.getSymbol(selectedLocale);

        } catch( IllegalArgumentException e ) {

            // Happens if the locale does not support currency code and symbol
            editableCurrency = true;

            // Ok the selected locale or default locale is not good for the currency
            // Lets have a best guess... Usually the locale after has the right code/symbol
            // If not, the user can always change it in the UI
            for( int i = cmbLocale.getSelectedIndex(); i < locales.length; i ++ ) {
                try {
                    Locale locale = getLocaleBasedOnComboBoxIndex(i);
                    Currency cur = Currency.getInstance( locale );
                    codeDisplayName = ISO4217CurrencyCode.getDisplayName( cur.getCurrencyCode() );
                    symbol = cur.getSymbol(locale);

                    // Found a good one
                    break;
                } catch( IllegalArgumentException ie ) {
                    // Happens if the locale does not support currency code and symbol
                    // try the next locale
                    continue;
                } catch( java.lang.NullPointerException ne ) {
                    // Happens if the locale does not have two letter country code
                    // try the next locale
                    continue;
                }
            }
        }

        // Disable/enable the currency components properly

        boolean isTypeCurrency = ((cmbType.getSelectedIndex() == 1) && (rbType.isSelected()));
        rbCurrencyCode.setEnabled(isTypeCurrency);
        rbSymbol.setEnabled(isTypeCurrency);
        lblChooseCurrency.setEnabled(isTypeCurrency);
        cmbCurrencyCode.setEnabled(isTypeCurrency);
        txtEnterSymbol.setEnabled(isTypeCurrency);

        // If the locale has currency code and symbol, then the user can edit them.
        // Otherwise, the user has a choice to select a currency code or enter a symbol

        txtEnterSymbol.setText( symbol );
        cmbCurrencyCode.removeAllItems();

        if( !editableCurrency ) {
            cmbCurrencyCode.setModel( new javax.swing.DefaultComboBoxModel( new String[] {codeDisplayName}) );
            // Make the symbol not editable
            txtEnterSymbol.setEditable( false );
        } else {
            cmbCurrencyCode.setModel( new javax.swing.DefaultComboBoxModel(ISO4217CurrencyCode.getDisplayNames() ) );
            // Make the symbol editable
            txtEnterSymbol.setEditable( true );
        }

        cmbCurrencyCode.setSelectedItem( codeDisplayName );

        if( isTypeCurrency ) {
            if( rbCurrencyCode.isSelected() ) {
                txtEnterSymbol.setEnabled(false);
            } else {
                cmbCurrencyCode.setEnabled(false);
            }
        }
    }

    private void cmbExampleFocusLost(java.awt.event.FocusEvent evt) {
        // TODO add your handling code here:

        btnTest.setDefaultCapable(false);
    }

    // The sample result will be updated based on the selected type or pattern
    private void upateSampleResult() {

        // Valid the user inputs first
        if( !validateUserInput() )
            return;

        // Now wWe'll format the number based on the rules used by NumberConverter.getAsString().
        // Here is what the NumberConverter javadoc says:
        // The getAsString() method expects a value of type java.lang.Number (or a subclass), and creates a
        // formatted String according to the following algorithm:
        //
        //   o If the specified value is null, return a zero-length String.
        //   o If the specified value is a String, return it unmodified.
        //   o If the locale property is not null, use that Locale for managing formatting. Otherwise, use the Locale from the FacesContext.
        //   o If a pattern has been specified, its syntax must conform the rules specified by java.text.DecimalFormat.
        //     Such a pattern will be used to format, and the type property (along with related formatting options described
        //     in the next paragraph) will be ignored.
        //   o If a pattern has not been specified, formatting will be based on the type property, which formats the value
        //     as a currency, a number, or a percent. The format pattern for currencies, numbers, and percentages is determined
        //     by calling the percentages is determined by calling the getCurrencyInstance(), getNumberInstance(), or getPercentInstance()
        //     method of the java.text.NumberFormat class, passing in the selected Locale. In addition, the following properties will be
        //     applied to the format pattern, if specified:
        //       - If the groupingUsed property is true, the setGroupingUsed(true) method on the corresponding NumberFormat instance will be called.
        //       - The minimum and maximum number of digits in the integer and fractional portions of the result will be configured based
        //         on any values set for the maxFractionDigits, maxIntegerDigits, minFractionDigits, and minIntegerDigits properties.
        //       - If the type is set to currency, it is also possible to configure the currency symbol to be used, using either the
        //         currencyCode or currencySymbol properties. If both are set, the value for currencyCode takes precedence on a
        //         JDK 1.4 (or later) JVM; otherwise, the value for currencySymbol takes precedence.

        double sampleNumber = 0;

        try {
            sampleNumber = Double.parseDouble( (String)cmbExample.getSelectedItem() );
        } catch( NumberFormatException ne ) {

            txtResults.setText( NbBundle.getMessage(NumberConverterCustomizerPanel.class, "notANumber", (String)cmbExample.getSelectedItem()) );
            return;
        }

        if( rbType.isSelected() ) {

            Locale selectedLocale = getLocaleBasedOnComboBoxIndex(cmbLocale.getSelectedIndex());
            if( selectedLocale == null )
                selectedLocale = Locale.getDefault();

            // Format based on the type of the converter

            NumberFormat numberFormat;
            if( cmbType.getSelectedIndex() == 0  ) { // number
                numberFormat = NumberFormat.getNumberInstance( selectedLocale );
            } else if( cmbType.getSelectedIndex() == 1 ) { // currency
                numberFormat = NumberFormat.getCurrencyInstance( selectedLocale );

                Currency currency = null;
                if( rbCurrencyCode.isSelected() ) {
                    String currencyCode = null;

                    // If there is only one item in the combo box, then we can use the
                    // selected locale the figure out the currency code
                    if( cmbCurrencyCode.getModel().getSize() == 1 ) {
                        currencyCode = Currency.getInstance( selectedLocale ).getCurrencyCode();
                    } else {
                        //
                        int index = cmbCurrencyCode.getSelectedIndex();
                        currencyCode = ISO4217CurrencyCode.getCode( index );
                    }
                    currency = Currency.getInstance( currencyCode );
                    numberFormat.setCurrency( currency );
                } else {
                    DecimalFormat df = (DecimalFormat) numberFormat;
                    DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
                    dfs.setCurrencySymbol( txtEnterSymbol.getText().trim());
                    df.setDecimalFormatSymbols(dfs);
                }

                // What do I do if symbol is selected

            } else { // Must be PERCENT
                numberFormat = NumberFormat.getPercentInstance( selectedLocale );
            }

            Integer minIntegerDigits = getInteger( (String)cmbMinInteger.getSelectedItem() );
            Integer maxIntegerDigits = getInteger( (String)cmbMaxInteger.getSelectedItem() );
            Integer minFractionalDigits = getInteger( (String)cmbMinFractional.getSelectedItem() );
            Integer maxFractionalDigits = getInteger( (String)cmbMaxFractional.getSelectedItem() );

            // Because of a bug in insyn, 0 min/max integer/fractional digits will be considered as I want defaults for now

            if( minIntegerDigits != null && minIntegerDigits.intValue() != 0 )
                numberFormat.setMinimumIntegerDigits( minIntegerDigits.intValue() );

            if( maxIntegerDigits != null && maxIntegerDigits.intValue() != 0 )
                numberFormat.setMaximumIntegerDigits( maxIntegerDigits.intValue() );

            if( minFractionalDigits != null && minFractionalDigits.intValue() != 0 )
                numberFormat.setMinimumFractionDigits( minFractionalDigits.intValue() );

            if( maxFractionalDigits != null && maxFractionalDigits.intValue() != 0 )
                numberFormat.setMaximumFractionDigits( maxFractionalDigits.intValue() );

            numberFormat.setGroupingUsed( cbUseGrouping.isSelected() );
            numberFormat.setParseIntegerOnly( cbIntegerOnly.isSelected() );

            txtResults.setText( numberFormat.format( sampleNumber ));

        } else {
            // Pattern should be used to format the number
            DecimalFormat decimalFormat = (DecimalFormat)DecimalFormat.getInstance();
            decimalFormat.applyPattern( ((String)cmbPattern.getSelectedItem()).trim() );
            decimalFormat.setParseIntegerOnly( cbIntegerOnly.isSelected() );

            txtResults.setText( decimalFormat.format( sampleNumber ) );
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        typePatternGroup = new javax.swing.ButtonGroup();
        currencyGroup = new javax.swing.ButtonGroup();
        pnlTypePattern = new javax.swing.JPanel();
        cmbType = new javax.swing.JComboBox();
        cmbPattern = new javax.swing.JComboBox();
        rbType = new javax.swing.JRadioButton();
        rbPattern = new javax.swing.JRadioButton();
        pnlType = new javax.swing.JPanel();
        pnlFractional = new javax.swing.JPanel();
        lblMinFractional = new javax.swing.JLabel();
        lblMaxFractional = new javax.swing.JLabel();
        cmbMinFractional = new javax.swing.JComboBox();
        cmbMaxFractional = new javax.swing.JComboBox();
        pnlInteger = new javax.swing.JPanel();
        lblMinInteger = new javax.swing.JLabel();
        lblMaxInteger = new javax.swing.JLabel();
        cmbMinInteger = new javax.swing.JComboBox();
        cmbMaxInteger = new javax.swing.JComboBox();
        lblFractional = new javax.swing.JLabel();
        lblInteger = new javax.swing.JLabel();
        cbUseGrouping = new javax.swing.JCheckBox();
        pnlLocale = new javax.swing.JPanel();
        rbCurrencyCode = new javax.swing.JRadioButton();
        cmbLocale = new javax.swing.JComboBox();
        lblLocale = new javax.swing.JLabel();
        lblChooseCurrency = new javax.swing.JLabel();
        rbSymbol = new javax.swing.JRadioButton();
        txtEnterSymbol = new javax.swing.JTextField();
        cmbCurrencyCode = new javax.swing.JComboBox();
        cbIntegerOnly = new javax.swing.JCheckBox();
        pnlExample = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        lblExample = new javax.swing.JLabel();
        txtResults = new javax.swing.JTextField();
        lblResults = new javax.swing.JLabel();
        cmbExample = new javax.swing.JComboBox();
        btnTest = new javax.swing.JButton();
        txtExampleInstructions = new javax.swing.JTextPane();

        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });
        setLayout(new java.awt.GridBagLayout());

        pnlTypePattern.setLayout(new java.awt.GridBagLayout());

        cmbType.setMinimumSize(new java.awt.Dimension(100, 19));
        cmbType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbTypeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        pnlTypePattern.add(cmbType, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/faces/dt/converter/Bundle"); // NOI18N
        cmbType.getAccessibleContext().setAccessibleName(bundle.getString("type")); // NOI18N
        cmbType.getAccessibleContext().setAccessibleDescription(bundle.getString("typeDescription")); // NOI18N

        cmbPattern.setEditable(true);
        cmbPattern.setEnabled(false);
        cmbPattern.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbPatternActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        pnlTypePattern.add(cmbPattern, gridBagConstraints);
        cmbPattern.getAccessibleContext().setAccessibleName(bundle.getString("pattern")); // NOI18N
        cmbPattern.getAccessibleContext().setAccessibleDescription(bundle.getString("pattern")); // NOI18N

        typePatternGroup.add(rbType);
        rbType.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rbType, org.openide.util.NbBundle.getMessage(NumberConverterCustomizerPanel.class, "type")); // NOI18N
        rbType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbTypeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        pnlTypePattern.add(rbType, gridBagConstraints);
        rbType.getAccessibleContext().setAccessibleName(bundle.getString("type")); // NOI18N
        rbType.getAccessibleContext().setAccessibleDescription(bundle.getString("typeDescription")); // NOI18N

        typePatternGroup.add(rbPattern);
        org.openide.awt.Mnemonics.setLocalizedText(rbPattern, org.openide.util.NbBundle.getMessage(NumberConverterCustomizerPanel.class, "pattern")); // NOI18N
        rbPattern.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbPatternActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        pnlTypePattern.add(rbPattern, gridBagConstraints);
        rbPattern.getAccessibleContext().setAccessibleDescription(bundle.getString("pattern")); // NOI18N

        pnlType.setLayout(new java.awt.GridBagLayout());

        pnlFractional.setLayout(new java.awt.GridBagLayout());

        lblMinFractional.setLabelFor(cmbMinFractional);
        org.openide.awt.Mnemonics.setLocalizedText(lblMinFractional, org.openide.util.NbBundle.getMessage(NumberConverterCustomizerPanel.class, "minFractional")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        pnlFractional.add(lblMinFractional, gridBagConstraints);
        lblMinFractional.getAccessibleContext().setAccessibleName(bundle.getString("minFractional")); // NOI18N
        lblMinFractional.getAccessibleContext().setAccessibleDescription(bundle.getString("minFractional")); // NOI18N

        lblMaxFractional.setLabelFor(cmbMaxFractional);
        org.openide.awt.Mnemonics.setLocalizedText(lblMaxFractional, org.openide.util.NbBundle.getMessage(NumberConverterCustomizerPanel.class, "maxFractional")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        pnlFractional.add(lblMaxFractional, gridBagConstraints);
        lblMaxFractional.getAccessibleContext().setAccessibleName(bundle.getString("maxFractional")); // NOI18N
        lblMaxFractional.getAccessibleContext().setAccessibleDescription(bundle.getString("maxFractional")); // NOI18N

        cmbMinFractional.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.3;
        pnlFractional.add(cmbMinFractional, gridBagConstraints);

        cmbMaxFractional.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.3;
        pnlFractional.add(cmbMaxFractional, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        pnlType.add(pnlFractional, gridBagConstraints);

        pnlInteger.setLayout(new java.awt.GridBagLayout());

        lblMinInteger.setLabelFor(cmbMinInteger);
        org.openide.awt.Mnemonics.setLocalizedText(lblMinInteger, org.openide.util.NbBundle.getMessage(NumberConverterCustomizerPanel.class, "minInteger")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        pnlInteger.add(lblMinInteger, gridBagConstraints);
        lblMinInteger.getAccessibleContext().setAccessibleName(bundle.getString("minInteger")); // NOI18N
        lblMinInteger.getAccessibleContext().setAccessibleDescription(bundle.getString("minInteger")); // NOI18N

        lblMaxInteger.setLabelFor(cmbMaxInteger);
        org.openide.awt.Mnemonics.setLocalizedText(lblMaxInteger, org.openide.util.NbBundle.getMessage(NumberConverterCustomizerPanel.class, "maxInteger")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        pnlInteger.add(lblMaxInteger, gridBagConstraints);
        lblMaxInteger.getAccessibleContext().setAccessibleName(bundle.getString("maxInteger")); // NOI18N
        lblMaxInteger.getAccessibleContext().setAccessibleDescription(bundle.getString("maxInteger")); // NOI18N

        cmbMinInteger.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.3;
        pnlInteger.add(cmbMinInteger, gridBagConstraints);

        cmbMaxInteger.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.3;
        pnlInteger.add(cmbMaxInteger, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        pnlType.add(pnlInteger, gridBagConstraints);

        lblFractional.setLabelFor(cmbMinFractional);
        org.openide.awt.Mnemonics.setLocalizedText(lblFractional, org.openide.util.NbBundle.getMessage(NumberConverterCustomizerPanel.class, "fracDigits")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        pnlType.add(lblFractional, gridBagConstraints);
        lblFractional.getAccessibleContext().setAccessibleName(bundle.getString("fractionalDigits")); // NOI18N
        lblFractional.getAccessibleContext().setAccessibleDescription(bundle.getString("fractionalDigits")); // NOI18N

        lblInteger.setLabelFor(cmbMinInteger);
        org.openide.awt.Mnemonics.setLocalizedText(lblInteger, org.openide.util.NbBundle.getMessage(NumberConverterCustomizerPanel.class, "intDigits")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        pnlType.add(lblInteger, gridBagConstraints);
        lblInteger.getAccessibleContext().setAccessibleName(bundle.getString("integerDigits")); // NOI18N
        lblInteger.getAccessibleContext().setAccessibleDescription(bundle.getString("integerDigits")); // NOI18N

        cbUseGrouping.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(cbUseGrouping, org.openide.util.NbBundle.getMessage(NumberConverterCustomizerPanel.class, "groupingUsed")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        pnlType.add(cbUseGrouping, gridBagConstraints);
        cbUseGrouping.getAccessibleContext().setAccessibleDescription(bundle.getString("groupingUsed")); // NOI18N

        pnlLocale.setLayout(new java.awt.GridBagLayout());

        currencyGroup.add(rbCurrencyCode);
        rbCurrencyCode.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rbCurrencyCode, org.openide.util.NbBundle.getMessage(NumberConverterCustomizerPanel.class, "currencyCode")); // NOI18N
        rbCurrencyCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbCurrencyCodeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        pnlLocale.add(rbCurrencyCode, gridBagConstraints);
        rbCurrencyCode.getAccessibleContext().setAccessibleDescription(bundle.getString("currencyCode")); // NOI18N

        cmbLocale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbLocaleActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        pnlLocale.add(cmbLocale, gridBagConstraints);
        cmbLocale.getAccessibleContext().setAccessibleName(bundle.getString("locale")); // NOI18N
        cmbLocale.getAccessibleContext().setAccessibleDescription(bundle.getString("locale")); // NOI18N

        lblLocale.setLabelFor(cmbLocale);
        org.openide.awt.Mnemonics.setLocalizedText(lblLocale, org.openide.util.NbBundle.getMessage(NumberConverterCustomizerPanel.class, "locale")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        pnlLocale.add(lblLocale, gridBagConstraints);

        lblChooseCurrency.setLabelFor(cmbCurrencyCode);
        org.openide.awt.Mnemonics.setLocalizedText(lblChooseCurrency, org.openide.util.NbBundle.getMessage(NumberConverterCustomizerPanel.class, "chooseCurrency")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        pnlLocale.add(lblChooseCurrency, gridBagConstraints);
        lblChooseCurrency.getAccessibleContext().setAccessibleDescription(bundle.getString("currencyCode")); // NOI18N

        currencyGroup.add(rbSymbol);
        org.openide.awt.Mnemonics.setLocalizedText(rbSymbol, org.openide.util.NbBundle.getMessage(NumberConverterCustomizerPanel.class, "symbol")); // NOI18N
        rbSymbol.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbSymbolActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlLocale.add(rbSymbol, gridBagConstraints);
        rbSymbol.getAccessibleContext().setAccessibleDescription(bundle.getString("symbol")); // NOI18N

        txtEnterSymbol.setColumns(10);
        txtEnterSymbol.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        txtEnterSymbol.setMinimumSize(new java.awt.Dimension(35, 20));
        txtEnterSymbol.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtEnterSymbolActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(2, 10, 0, 0);
        pnlLocale.add(txtEnterSymbol, gridBagConstraints);
        txtEnterSymbol.getAccessibleContext().setAccessibleName(bundle.getString("symbol")); // NOI18N
        txtEnterSymbol.getAccessibleContext().setAccessibleDescription(bundle.getString("symbol")); // NOI18N

        cmbCurrencyCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbCurrencyCodeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
        pnlLocale.add(cmbCurrencyCode, gridBagConstraints);
        cmbCurrencyCode.getAccessibleContext().setAccessibleDescription(bundle.getString("currencyCode")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        pnlType.add(pnlLocale, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 30, 6, 10);
        pnlTypePattern.add(pnlType, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(cbIntegerOnly, org.openide.util.NbBundle.getMessage(NumberConverterCustomizerPanel.class, "intOnly")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        pnlTypePattern.add(cbIntegerOnly, gridBagConstraints);
        cbIntegerOnly.getAccessibleContext().setAccessibleDescription(bundle.getString("intOnly")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        add(pnlTypePattern, gridBagConstraints);

        pnlExample.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        pnlExample.add(jSeparator1, gridBagConstraints);

        lblExample.setLabelFor(cmbExample);
        org.openide.awt.Mnemonics.setLocalizedText(lblExample, org.openide.util.NbBundle.getMessage(NumberConverterCustomizerPanel.class, "example")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        pnlExample.add(lblExample, gridBagConstraints);
        lblExample.getAccessibleContext().setAccessibleDescription(bundle.getString("example")); // NOI18N

        txtResults.setEditable(false);
        txtResults.setText("1234.56");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        pnlExample.add(txtResults, gridBagConstraints);
        txtResults.getAccessibleContext().setAccessibleName(bundle.getString("results")); // NOI18N
        txtResults.getAccessibleContext().setAccessibleDescription(bundle.getString("results")); // NOI18N

        lblResults.setLabelFor(txtResults);
        org.openide.awt.Mnemonics.setLocalizedText(lblResults, org.openide.util.NbBundle.getMessage(NumberConverterCustomizerPanel.class, "results")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        pnlExample.add(lblResults, gridBagConstraints);
        lblResults.getAccessibleContext().setAccessibleDescription(bundle.getString("results")); // NOI18N

        cmbExample.setEditable(true);
        cmbExample.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbExampleActionPerformed(evt);
            }
        });
        cmbExample.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                cmbExampleFocusGained(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        pnlExample.add(cmbExample, gridBagConstraints);
        cmbExample.getAccessibleContext().setAccessibleName(bundle.getString("example")); // NOI18N
        cmbExample.getAccessibleContext().setAccessibleDescription(bundle.getString("example")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnTest, org.openide.util.NbBundle.getMessage(NumberConverterCustomizerPanel.class, "testText")); // NOI18N
        btnTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTestActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        pnlExample.add(btnTest, gridBagConstraints);
        btnTest.getAccessibleContext().setAccessibleDescription(bundle.getString("testText")); // NOI18N

        txtExampleInstructions.setBackground(getBackground());
        txtExampleInstructions.setBorder(null);
        txtExampleInstructions.setEditable(false);
        txtExampleInstructions.setText(bundle.getString("exampleInstructionsText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 11);
        pnlExample.add(txtExampleInstructions, gridBagConstraints);
        txtExampleInstructions.getAccessibleContext().setAccessibleName(bundle.getString("exampleInstructionsText")); // NOI18N
        txtExampleInstructions.getAccessibleContext().setAccessibleDescription(bundle.getString("exampleInstructionsText")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(pnlExample, gridBagConstraints);

        getAccessibleContext().setAccessibleName("NumberFormat");
        getAccessibleContext().setAccessibleDescription("Number Format...");
    }// </editor-fold>//GEN-END:initComponents

    private void btnTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTestActionPerformed
        // Update the result field
        upateSampleResult();
    }//GEN-LAST:event_btnTestActionPerformed

    private void cmbExampleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbExampleActionPerformed
        // Update the result field
        upateSampleResult();
    }//GEN-LAST:event_cmbExampleActionPerformed

    private void cmbPatternActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbPatternActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_cmbPatternActionPerformed

    private void txtEnterSymbolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtEnterSymbolActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_txtEnterSymbolActionPerformed

    private void cmbLocaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbLocaleActionPerformed
        // Update the currency symbol and code
        enableCurrencyCombos();
    }//GEN-LAST:event_cmbLocaleActionPerformed

    private void cmbCurrencyCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbCurrencyCodeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbCurrencyCodeActionPerformed

    private void rbCurrencyCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbCurrencyCodeActionPerformed
        enableCurrencyCombos();
    }//GEN-LAST:event_rbCurrencyCodeActionPerformed

    private void rbSymbolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbSymbolActionPerformed
        enableCurrencyCombos();
    }//GEN-LAST:event_rbSymbolActionPerformed

    private void cmbTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTypeActionPerformed
        enableCurrencyCombos();
    }//GEN-LAST:event_cmbTypeActionPerformed

    private void rbPatternActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbPatternActionPerformed
        if( rbPattern.isSelected() )
            rbType.setSelected( false );
        else
            rbType.setSelected( true );

        // Enable/disable the components appropriately
        enablePatternPanel();
        enableTypePanel();
    }//GEN-LAST:event_rbPatternActionPerformed

    private void rbTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbTypeActionPerformed
        if( rbType.isSelected() )
            rbPattern.setSelected( false );
        else
            rbPattern.setSelected( true );

        // Enable/disable the components appropriately
        enablePatternPanel();
        enableTypePanel();
    }//GEN-LAST:event_rbTypeActionPerformed

    private void cmbExampleFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_cmbExampleFocusGained

        //btnOK.setDefaultCapable(false);
        btnTest.setDefaultCapable(true);
        getRootPane().setDefaultButton(btnTest);
    }//GEN-LAST:event_cmbExampleFocusGained

    private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_formFocusGained


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnTest;
    private javax.swing.JCheckBox cbIntegerOnly;
    private javax.swing.JCheckBox cbUseGrouping;
    private javax.swing.JComboBox cmbCurrencyCode;
    private javax.swing.JComboBox cmbExample;
    private javax.swing.JComboBox cmbLocale;
    private javax.swing.JComboBox cmbMaxFractional;
    private javax.swing.JComboBox cmbMaxInteger;
    private javax.swing.JComboBox cmbMinFractional;
    private javax.swing.JComboBox cmbMinInteger;
    private javax.swing.JComboBox cmbPattern;
    private javax.swing.JComboBox cmbType;
    private javax.swing.ButtonGroup currencyGroup;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblChooseCurrency;
    private javax.swing.JLabel lblExample;
    private javax.swing.JLabel lblFractional;
    private javax.swing.JLabel lblInteger;
    private javax.swing.JLabel lblLocale;
    private javax.swing.JLabel lblMaxFractional;
    private javax.swing.JLabel lblMaxInteger;
    private javax.swing.JLabel lblMinFractional;
    private javax.swing.JLabel lblMinInteger;
    private javax.swing.JLabel lblResults;
    private javax.swing.JPanel pnlExample;
    private javax.swing.JPanel pnlFractional;
    private javax.swing.JPanel pnlInteger;
    private javax.swing.JPanel pnlLocale;
    private javax.swing.JPanel pnlType;
    private javax.swing.JPanel pnlTypePattern;
    private javax.swing.JRadioButton rbCurrencyCode;
    private javax.swing.JRadioButton rbPattern;
    private javax.swing.JRadioButton rbSymbol;
    private javax.swing.JRadioButton rbType;
    private javax.swing.JTextField txtEnterSymbol;
    private javax.swing.JTextPane txtExampleInstructions;
    private javax.swing.JTextField txtResults;
    private javax.swing.ButtonGroup typePatternGroup;
    // End of variables declaration//GEN-END:variables

}
