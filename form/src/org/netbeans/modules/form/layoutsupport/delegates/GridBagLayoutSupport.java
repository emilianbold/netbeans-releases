/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.layoutsupport.delegates;

import java.awt.*;
import java.beans.*;
import java.util.*;
import java.lang.reflect.*;

import org.openide.nodes.Node;
import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;

import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.codestructure.*;
import org.netbeans.modules.form.FormProperty;

/**
 * Support class for GridBagLayout. This is an example of support for layout
 * managers with complex layout constraints for which rather special code
 * structure must be managed - GridBagConstraints require to be set up
 * field by field.
 *
 * @author Tran Duc Trung, Tomas Pavek
 */

public class GridBagLayoutSupport extends AbstractLayoutSupport
{
    private static final int X_AXIS = 1;
    private static final int Y_AXIS = 2;

    private static Constructor constrConstructor;

    /** Gets the supported layout manager class - GridBagLayout.
     * @return the class supported by this delegate
     */
    public Class getSupportedClass() {
        return GridBagLayout.class;
    }

    /** Returns a class of customizer for GridBagLayout.
     * @return layout customizer class
     */
    public Class getCustomizerClass() {
        return GridBagCustomizer.class;
    }

    /** Creates an instance of customizer for GridBagLayout.
     * @return layout customizer class
     */
    public Component getSupportCustomizer() {
        GridBagCustomizer customizer = new GridBagCustomizer();
        customizer.setObject(this);
        return customizer;
    }

    /** This method is called when switching layout - giving an opportunity to
     * convert the previous constrainst of components to constraints of the new
     * layout (this layout). Conversion from AbsoluteConstraints to
     * GridBagConstraints is implemented here.
     * @param previousConstraints [input] layout constraints of components in
     *                                    the previous layout
     * @param currentConstraints [output] array of converted constraints for
     *                                    the new layout - to be filled
     * @param components [input] real components in a real container having the
     *                           previous layout
     */
    public void convertConstraints(LayoutConstraints[] previousConstraints,
                                   LayoutConstraints[] currentConstraints,
                                   Component[] components)
    {
        if (previousConstraints == null || currentConstraints == null
                || components == null)
            return;

        int MAX_VALUE = 99999;
        int MIN_VALUE = -99999;

        int[] axisX = new int[previousConstraints.length + 1];
        int[] axisY = new int[previousConstraints.length + 1];
        int[] crossingsX = new int[previousConstraints.length + 1];
        int[] crossingsY = new int[previousConstraints.length + 1];
        int axisXnumber = 1;
        int axisYnumber = 1;

        for (int i=0; i < axisX.length; i++) {
            axisX[i] = MAX_VALUE;
            axisY[i] = MAX_VALUE;
        }

        // define the most left and right components.
        int minX = MAX_VALUE;
        int maxX = MIN_VALUE;
        int minY = MAX_VALUE;
        int maxY = MIN_VALUE;

        int mostLeft = 0;
        int mostRight = 0;
        int mostTop = 0;
        int mostBottom = 0;

        for (int i=0; i < components.length; i++) {
            int x = components[i].getBounds().x;
            int x1 = x + components[i].getBounds().width;
            int y = components[i].getBounds().y;
            int y1 = y + components[i].getBounds().height;
            if (x < minX) {
                mostLeft = i;
                minX = x;
            }
            if (x1 > maxX) {
                mostRight = i;
                maxX = x1;
            }
            if (y < minY) {
                mostTop = i;
                minY = y;
            }
            if (y1 > maxY) {
                mostBottom = i;
                maxY = y1;
            }
        }
        // define basic axises, all right axises, but not if it's most right one...
        if (components.length > 1) {
            axisX[0] = MIN_VALUE;
            axisY[0] = MIN_VALUE;
            for (int i=0; i < components.length; i++) {
                int x1 = components[i].getBounds().x + components[i].getBounds().width;
                if (x1 != maxX) {
                    axisX[axisXnumber] = x1;
                    axisXnumber++;
                }
                int y1 = components[i].getBounds().y + components[i].getBounds().height;
                if (y1!= maxY) {
                    axisY[axisYnumber] = y1;
                    axisYnumber++;
                }
            }
            Arrays.sort(axisX);
            Arrays.sort(axisY);

            // define basic crossings (i.e. number of components which are
            // crossed by an axis); the algorithm is trying to minimize the
            // crossings
            for (int i=1; i < axisXnumber; i++)
                crossingsX[i] = getCrossings(components, X_AXIS, axisX[i]);
            for (int i=1; i < axisYnumber; i++)
                crossingsY[i] = getCrossings(components, Y_AXIS, axisY[i]);

            // shift basic axis if the number of crossings for new place is lower
            for (int i=1; i < axisXnumber; i++) {
                for (int j=0; j < components.length; j++) {
                    if (j != mostLeft) {
                        int x = components[j].getBounds().x;
                        int x1 = x + components[j].getBounds().width;
                        if (x < axisX[i] && x > axisX[i-1]
                            && crossingsX[i] > getCrossings(components, X_AXIS, x)
                            && x != minX) {
                            axisX[i] = x;
                            crossingsX[i] = getCrossings(components, X_AXIS, x);
                        }
                        if (x1 > axisX[i] && x1 < axisX[i+1]
                            && crossingsX[i] > getCrossings(components, X_AXIS, x1)) {
                            axisX[i] = x1;
                            crossingsX[i] = getCrossings(components, X_AXIS, x1);
                        }
                    }
                }
            }

            for (int i=1; i < axisYnumber; i++) {
                for (int j=0; j < components.length; j++) {
                    if (j != mostTop) {
                        int y = components[j].getBounds().y;
                        int y1 = y + components[j].getBounds().height;
                        if (y < axisY[i] && y > axisY[i-1]
                            && crossingsY[i] > getCrossings(components, Y_AXIS, y)
                            && y != minY) {
                            axisY[i] = y;
                            crossingsY[i] = getCrossings(components, Y_AXIS, y);
                        }
                        if (y1 > axisY[i] && y1 < axisY[i+1]
                            && crossingsY[i] > getCrossings(components, Y_AXIS, y1)) {
                            axisY[i] = y1;
                            crossingsY[i] = getCrossings(components, Y_AXIS, y1);
                        }
                    }
                }
            }

            // checking validity of all axis
            // checking if any axis is doubled (2 same axis)
            int removedX = 0;
            for (int i=1; i < axisXnumber; i++) {
                if (axisX[i] == axisX[i+1]) {
                    axisX[i] = MAX_VALUE;
                    removedX++;
                }
            }
            if (removedX > 0) {
                Arrays.sort(axisX);
                axisXnumber = axisXnumber - removedX;
            }
            int removedY = 0;
            for (int i=1; i < axisYnumber; i++) {
                if (axisY[i] == axisY[i+1]) {
                    axisY[i] = MAX_VALUE;
                    removedY++;
                }
            }
            if (removedY > 0) {
                Arrays.sort(axisY);
                axisYnumber = axisYnumber - removedY;
            }
            // checking if any axis is redundand (i.e. no component is
            // fixing size of this axis)
            int last = axisX[0];
            removedX = 0;
            for (int i=1; i < axisXnumber; i++) {
                boolean removing = true;
                for (int j=0; j < components.length; j++) {
                    int x = components[j].getBounds().x;
                    int x1 = x + components[j].getBounds().width;
                    if (x < axisX[i] && x >= last && x1 <= axisX[i]) {
                        removing = false;
                        break;
                    }
                }
                last = axisX[i];
                if (removing) {
                    axisX[i] = MAX_VALUE;
                    removedX++;
                }
            }
            if (removedX > 0) {
                Arrays.sort(axisX);
                axisXnumber = axisXnumber - removedX;
            }
            last = axisY[0];
            removedY = 0;
            for (int i=1; i < axisYnumber; i++) {
                boolean removing = true;
                for (int j=0; j < components.length; j++) {
                    int y = components[j].getBounds().y;
                    int y1 = y + components[j].getBounds().height;
                    if (y < axisY[i] && y >= last && y1 <= axisY[i]) {
                        removing = false;
                        break;
                    }
                }
                last = axisY[i];
                if (removing) {
                    axisY[i] = MAX_VALUE;
                    removedY++;
                }
            }
            if (removedY > 0) {
                Arrays.sort(axisY);
                axisYnumber = axisYnumber - removedY;
            }
            // removing most right and bottom axises if they are invalid
            if (axisX[axisXnumber-1] == maxX)
                axisXnumber--;
            if (axisY[axisYnumber-1] == maxY)
                axisYnumber--;
        }

        // seting first and last axis to proper values (i.e to form size)
        axisX[0]=0;
        axisX[axisXnumber] = components[0].getParent().getSize().width;
        axisY[0]=0;
        axisY[axisYnumber] = components[0].getParent().getSize().height;

        // define constraints based on axis
        for (int i=0; i < components.length; i++) {
            GridBagConstraints cons = new GridBagConstraints();
            int gridX = 0;
            int gridY = 0;
            int gridWidth = 1;
            int gridHeight = 1;
            int left = 0;
            int right = 0;
            int top = 0;
            int bottom = 0;
            int x = components[i].getBounds().x;
            int x1 = x + components[i].getBounds().width;
            int y = components[i].getBounds().y;
            int y1 = y + components[i].getBounds().height;
            for (int j=1; j < axisXnumber+1; j++) {
                if (x< axisX[j] && x>= axisX[j-1]) {
                    gridX = j-1;
                    left = x - axisX[j-1];
                }
                if (x1<= axisX[j] && x1 > axisX[j-1]) {
                    gridWidth = j-gridX;
                    right = axisX[j] - x1;
                }
            }
            for (int j=1; j < axisYnumber+1; j++) {
                if (y< axisY[j] && y>= axisY[j-1]) {
                    gridY = j-1;
                    top = y - axisY[j-1];      
                }
                if (y1<= axisY[j] && y1 > axisY[j-1]) {
                    gridHeight = j-gridY;
                    bottom = axisY[j] - y1;
                }
            }
            // checking whether the preffered size must be adjusted
            cons.ipadx = 0;
            cons.ipady = 0;
            if(components[i].getWidth() > 0)
                cons.ipadx = components[i].getWidth() - components[i].getPreferredSize().width;
            if(components[i].getHeight() > 0)
                cons.ipady = components[i].getHeight() - components[i].getPreferredSize().height;
            // storing calculated values
            cons.gridx = gridX;
            cons.gridy = gridY;
            cons.gridwidth = gridWidth;
            cons.gridheight = gridHeight;
            cons.insets = new Insets(top, left, bottom, right);
            cons.fill = GridBagConstraints.NONE;

            currentConstraints[i] = new GridBagLayoutConstraints(cons);
        }

        // the old algorithm
/*        for (int i=0, n=currentConstraints.length; i < n; i++) {
            if (currentConstraints[i] == null
                && previousConstraints[i] instanceof
                    AbsoluteLayoutSupport.AbsoluteLayoutConstraints)
            {
                Rectangle iBounds = components[i].getBounds();
                Dimension prefSize = components[i].getPreferredSize();
                GridBagConstraints constr = new GridBagConstraints();

                int gx = 0, gy = 0, gw = 1, gh = 1;
                int ix1 = iBounds.x;
                int iy1 = iBounds.y;
                int ix2 = ix1 + iBounds.width;
                int iy2 = iy1 + iBounds.height;
                int fromX = 0, fromY = 0;

                for (int j=0; j < n; j++) {
                    Rectangle jBounds = components[j].getBounds();
                    int jx2 = jBounds.x + jBounds.width;
                    int jy2 = jBounds.y + jBounds.height;
                    if (jx2 <= ix1) {
                        gx++;
                        fromX = Math.max(fromX, jx2);
                    }
                    if (jy2 <= iy1) {
                        gy++;
                        fromY = Math.max(fromY, jy2);
                    }
                    if (jx2 > ix1 && jx2 < ix2) gw++;
                    if (jy2 > iy1 && jy2 < iy2) gh++;
                }

                constr.gridx = gx;
                constr.gridy = gy;
                constr.gridwidth = gw;
                constr.gridheight = gh;
                constr.insets = new Insets(iy1 - fromY, ix1 - fromX, 0, 0);
                constr.fill = GridBagConstraints.BOTH;
                constr.ipadx = iBounds.width - prefSize.width;
                constr.ipady = iBounds.height - prefSize.height;

                currentConstraints[i] = new GridBagLayoutConstraints(constr);
            }
        } */
    }

    private static int getCrossings(Component[] components, int axis, int value) {
        int number = 0;
        if (axis == X_AXIS) {
            for (int i=0; i < components.length; i++) {
                int x = components[i].getBounds().x;
                int x1 = x+ components[i].getBounds().width;
                if (x < value && x1 > value)
                    number++;
            }
        }
        else {
            for (int i=0; i < components.length; i++) {
                int y = components[i].getBounds().y;
                int y1 = y + components[i].getBounds().height;
                if (y < value && y1 > value)
                    number++;
            }
        }
        return number;
    }

    // --------

    /** This method is called from readComponentCode method to read layout
     * constraints of a component from code (GridBagConstraints in this case).
     * @param constrExp CodeExpression object of the constraints (taken from
     *        add method in the code)
     * @param constrCode CodeGroup to be filled with the relevant constraints
     *        initialization code
     * @param compExp CodeExpression of the component for which the constraints
     *        are read (not needed here)
     * @return LayoutConstraints based on information read form code
     */
    protected LayoutConstraints readConstraintsCode(CodeExpression constrExp,
                                                    CodeGroup constrCode,
                                                    CodeExpression compExp)
    {
        GridBagLayoutConstraints constr = new GridBagLayoutConstraints();
        // reading is done in GridBagLayoutConstraints
        constr.readCodeExpression(constrExp, constrCode);
        return constr;
    }

    /** Called from createComponentCode method, creates code for a component
     * layout constraints (opposite to readConstraintsCode).
     * @param constrCode CodeGroup to be filled with constraints code
     * @param constr layout constraints metaobject representing the constraints
     * @param compExp CodeExpression object representing the component; not
     *        needed here
     * @return created CodeExpression representing the layout constraints
     */
    protected CodeExpression createConstraintsCode(CodeGroup constrCode,
                                                   LayoutConstraints constr,
                                                   CodeExpression compExp,
                                                   int index)
    {
        if (!(constr instanceof GridBagLayoutConstraints))
            return null;

        // the code creation is done in GridBagLayoutConstraints
        return ((GridBagLayoutConstraints)constr).createCodeExpression(
                                            getCodeStructure(), constrCode);
    }

    /** This method is called to get a default component layout constraints
     * metaobject in case it is not provided (e.g. in addComponents method).
     * @return the default LayoutConstraints object for the supported layout;
     *         null if no component constraints are used
     */
    protected LayoutConstraints createDefaultConstraints() {
        return new GridBagLayoutConstraints();
    }

    // -----------------

    /** LayoutConstraints implementation class for GridBagConstraints.
     * GridBagConstraints class is special in that it requires more code
     * statements for initialization (setting up the individual fields).
     *
     * There are two possible code variants: simple and complex.
     * In the simple situation, no parameter of GridBagConstraints is set, so
     * the code looks like:
     *   container.add(component, new GridBagConstraints());
     *
     * In the complex situation, there are some parameters set - this requires
     * additional code statement for each parameter, and also a variable to
     * be used for the constraints object. Then the code looks like:
     *   GridBagConstraints gridBagConstraints;
     *   ...
     *   gridBagConstraints = new GridBagConstraints();
     *   gridBagConstraints.gridx = 1;
     *   gridBagConstraints.gridy = 2;
     *   container.add(component, gridBagConstraints);
     */
    public static class GridBagLayoutConstraints implements LayoutConstraints {
        private GridBagConstraints constraints;

        private GridBagConstraints defaultConstraints = new GridBagConstraints();

        private Property[] properties;

        private CodeExpression constraintsExpression;
        private CodeGroup constraintsCode; // set of all relevant statements
        private CodeStatement[] propertyStatements; // statements for properties

        private static Constructor constrConstructor;

        private static final int variableType = CodeVariable.LOCAL
                                         | CodeVariable.EXPLICIT_DECLARATION;
        private static final int variableMask = CodeVariable.SCOPE_MASK
                                         | CodeVariable.DECLARATION_MASK;
        private static final String defaultVariableName = "gridBagConstraints"; // NOI18N

        public GridBagLayoutConstraints() {
            constraints = new GridBagConstraints();
        }

        public GridBagLayoutConstraints(GridBagConstraints constraints) {
            this.constraints = constraints;
        }

        public Node.Property[] getProperties() {
            if (properties == null) {
                createProperties();
                reinstateProperties();
            }
            return properties;
        }

        public Object getConstraintsObject() {
            return constraints;
        }

        public LayoutConstraints cloneConstraints() {
            return new GridBagLayoutConstraints((GridBagConstraints)
                                                constraints.clone());
        }

        // -------

        /** This method creates code expression for the constraints. It's
         * called from the delegate's createConstraintsCode method.
         * @param codeStructure CodeStructure in which the expression will be
         *        created
         * @param constrCode CodeGroup to be filled with all the initialization
         *        statements
         * @return CodeExpression representing the constraints
         */
        private CodeExpression createCodeExpression(CodeStructure codeStructure,
                                                    CodeGroup constrCode)
        {
            this.constraintsCode = constrCode;
            propertyStatements = null;

            // GridBagConstraints is created by a simple constructor...
            constraintsExpression = codeStructure.createExpression(
                                        getConstraintsConstructor(),
                                        CodeStructure.EMPTY_PARAMS);
            // ...but the additionlly it requires to create the initialization
            // code statements
            updateCodeExpression();

            return constraintsExpression;
        }

        /** This method reads CodeExpression object representing the
         * constraints and also all its initialization statements which are
         * mapped to the constraints properties. It's called from the
         * delegate's readConstraintsCode method.
         * @param constrExp CodeExpression of the constraints
         * @param constrCode CodeGroup to be filled with recognize
         *        initialization statements
         */
        private void readCodeExpression(CodeExpression constrExp,
                                        CodeGroup constrCode)
        {
            constraintsExpression = constrExp;
            constraintsCode = constrCode;
            propertyStatements = null;

//            constrExp.setOrigin(CodeStructure.createOrigin(
//                                        getConstraintsConstructor(),
//                                        CodeStructure.EMPTY_PARAMS));

            getProperties(); // ensure properties are created

            boolean isAnyChanged = false;

            Iterator it = CodeStructure.getDefinedStatementsIterator(constrExp);
            while (it.hasNext()) {
                // go through all the statements of constraints code expression
                CodeStatement statement = (CodeStatement) it.next();
                for (int j=0; j < properties.length; j++) {
                    Property prop = properties[j];
                    if (prop.field.equals(statement.getMetaObject())) {
                        // this statement represents a GridBagConstraints field
                        // assignment, we map the corresponding property to it
                        FormCodeSupport.readPropertyStatement(
                                            statement, prop, false);
                        setPropertyStatement(j, statement);
                        if (prop.isChanged()) { // this is a non-default value
                            constrCode.addStatement(statement);
                            isAnyChanged = true;
                        }
                        break;
                    }
                }
            }

            setupVariable(isAnyChanged);
        }

        /** This method updates the constraints code according to the
         * properties. This is called at the beginning - when the constraints
         * code expression is created - and then after each change of the
         * constraints properties. This keeps the code consistent with the
         * properties.
         */
        private void updateCodeExpression() {
            if (constraintsCode == null || constraintsExpression == null)
                return;

            constraintsCode.removeAll();

            getProperties(); // ensure properties are created

            boolean isAnyChanged = false;
            for (int i=0; i < properties.length; i++)
                // for each changed property, add the corresponding statement
                // to the code (constraintsCode - instance of CodeGroup)
                if (properties[i].isChanged()) {
                    constraintsCode.addStatement(getPropertyStatement(i));
                    isAnyChanged = true;
                }

            setupVariable(isAnyChanged);
        }

        /** This method returns the code statement corresponding to property
         * of given index. The statement is created if it does not exist yet.
         * @param index index of required statement
         */
        private CodeStatement getPropertyStatement(int index) {
            if (propertyStatements == null)
                propertyStatements = new CodeStatement[properties.length];

            CodeStatement propStatement = propertyStatements[index];
            if (propStatement == null) {
                CodeExpression propExp =
                    constraintsExpression.getCodeStructure().createExpression(
                        FormCodeSupport.createOrigin(properties[index]));

                // statement is field assignment; the property code expression
                // represents the assigned value
                propStatement = CodeStructure.createStatement(
                                    constraintsExpression,
                                    properties[index].field,
                                    propExp);

                propertyStatements[index] = propStatement;
            }
            return propStatement;
        }

        /** Sets the code statement read form code for given property index.
         * @param index index of the corresponding property
         * @param propStatement CodeStatement to be set
         */
        private void setPropertyStatement(int index,
                                          CodeStatement propStatement)
        {
            if (propertyStatements == null)
                propertyStatements = new CodeStatement[properties.length];
            propertyStatements[index] = propStatement;
        }

        /** This method sets up the variable for constraints code expression.
         * The variable is needed only there's some property change (i.e.
         * there's some statement in which the variable would be used). Once
         * the variable is created, it's used for all the GridBagConstraints
         * in the form.
         */
        private void setupVariable(boolean anyChangedProperty) {
            CodeStructure codeStructure =
                constraintsExpression.getCodeStructure();
            CodeVariable var = constraintsExpression.getVariable();

            if (anyChangedProperty) { // there should be a variable
                if (var == null) { // no variable currently used
                    var = findVariable(); // find and reuse variable
                    if (var == null) { // create a new variable
                        var = codeStructure.createVariableForExpression(
                                                constraintsExpression,
                                                variableType,
                                                defaultVariableName);
                    }
                    else { // attach the constraints expression to the variable
                        codeStructure.attachExpressionToVariable(
                                          constraintsExpression, var);
                    }
                }
                // add variable assignment code
                constraintsCode.addStatement(
                                  0, var.getAssignment(constraintsExpression));
            }
            else { // no variable needed
                codeStructure.removeExpressionFromVariable(
                                  constraintsExpression);
            }
        }

        private CodeVariable findVariable() {
            CodeStructure codeStructure =
                constraintsExpression.getCodeStructure();

            // first try "gridBagConstraints" name - this succeeds in most
            // cases (unless the name is used elsewhere or not created yet)
            CodeVariable var = codeStructure.getVariable(defaultVariableName);
            if (var != null
                    && (var.getType() & variableMask) == variableType
                    && GridBagConstraints.class.equals(var.getDeclaredType()))
                return var;

            // try to find variable of corresponding type (time expensive)
            Iterator it = codeStructure.getVariablesIterator(
                                            variableType,
                                            variableMask,
                                            GridBagConstraints.class);
            while (it.hasNext()) {
                var = (CodeVariable) it.next();
                if (var.getName().startsWith(defaultVariableName))
                    return var;
            }

            return null;
        }

        private void createProperties() {
            properties = new Property[] {
                new Property("gridx", // NOI18N
                             Integer.TYPE,
                             getBundle().getString("PROP_gridx"), // NOI18N
                             getBundle().getString("HINT_gridx"), // NOI18N
                             GridPosEditor.class),

                new Property("gridy", // NOI18N
                             Integer.TYPE,
                             getBundle().getString("PROP_gridy"), // NOI18N
                             getBundle().getString("HINT_gridy"), // NOI18N
                             GridPosEditor.class),

                new Property("gridwidth", // NOI18N
                             Integer.TYPE,
                             getBundle().getString("PROP_gridwidth"), // NOI18N
                             getBundle().getString("HINT_gridwidth"), // NOI18N
                             GridSizeEditor.class),

                new Property("gridheight", // NOI18N
                             Integer.TYPE,
                             getBundle().getString("PROP_gridheight"), // NOI18N
                             getBundle().getString("HINT_gridheight"), // NOI18N
                             GridSizeEditor.class),

                new Property("fill", // NOI18N
                             Integer.TYPE,
                             getBundle().getString("PROP_fill"), // NOI18N
                             getBundle().getString("HINT_fill"), // NOI18N
                             FillEditor.class),

                new Property("ipadx", // NOI18N
                             Integer.TYPE,
                             getBundle().getString("PROP_ipadx"), // NOI18N
                             getBundle().getString("HINT_ipadx"), // NOI18N
                             null),

                new Property("ipady", // NOI18N
                              Integer.TYPE,
                              getBundle().getString("PROP_ipady"), // NOI18N
                              getBundle().getString("HINT_ipady"), // NOI18N
                              null),

                new Property("insets", // NOI18N
                             Insets.class,
                             getBundle().getString("PROP_insets"), // NOI18N
                             getBundle().getString("HINT_insets"), // NOI18N
                             null),

                new Property("anchor", // NOI18N
                             Integer.TYPE,
                             getBundle().getString("PROP_anchor"), // NOI18N
                             getBundle().getString("HINT_anchor"), // NOI18N
                             AnchorEditor.class),

                new Property("weightx", // NOI18N
                             Double.TYPE,
                             getBundle().getString("PROP_weightx"), // NOI18N
                             getBundle().getString("HINT_weightx"), // NOI18N
                             null),

                new Property("weighty", // NOI18N
                             Double.TYPE,
                             getBundle().getString("PROP_weighty"), // NOI18N
                             getBundle().getString("HINT_weighty"), // NOI18N
                             null)
            };
        }

        private void reinstateProperties() {
            try {
                for (int i=0; i < properties.length; i++) {
                    FormProperty prop = (FormProperty) properties[i];
                    prop.reinstateProperty();
                }
            }
            catch(IllegalAccessException e1) {} // should not happen
            catch(InvocationTargetException e2) {} // should not happen
        }

        private static Constructor getConstraintsConstructor() {
            if (constrConstructor == null) {
                try {
                    constrConstructor =
                        GridBagConstraints.class.getConstructor(new Class[0]);
                }
                catch (NoSuchMethodException ex) { // should not happen
                    ex.printStackTrace();
                }
            }
            return constrConstructor;
        }

        // ---------

        /** Property implementation for GridBagLayoutConstraints. Each property
         * is tied to one field of GridBagConstraints. After a change in
         * property, updateCodeExpression is called to reflect the change in
         * the code.
         */
        private final class Property extends FormProperty {
            private Field field;
            private Class propertyEditorClass;

            Property(String name, Class type,
                     String displayName, String shortDescription,
                     Class propertyEditorClass)
            {
                super("GridBagLayoutConstraints "+name, type, // NOI18N
                      displayName, shortDescription);
                this.propertyEditorClass = propertyEditorClass;
                try {
                    field = GridBagConstraints.class.getField(name);
                }
                catch (NoSuchFieldException ex) { // should not happen
                    ex.printStackTrace();
                }
            }

            public Object getTargetValue() {
                try {
                    return field.get(constraints);
                }
                catch (Exception ex) { // should not happen
                    ex.printStackTrace();
                    return null;
                }
            }

            public void setTargetValue(Object value) {
                try {
                    field.set(constraints, value);
                }
                catch (Exception ex) { // should not happen
                    ex.printStackTrace();
                }
                    
            }

            public boolean supportsDefaultValue () {
                return true;
            }

            public Object getDefaultValue() {
                try {
                    return field.get(defaultConstraints);
                }
                catch (Exception ex) { // should not happen
                    ex.printStackTrace();
                    return null;
                }
            }

            public PropertyEditor getExpliciteEditor() {
                if (propertyEditorClass == null)
                    return null;
                try {
                    return (PropertyEditor) propertyEditorClass.newInstance();
                }
                catch (Exception ex) { //should not happen
                    ex.printStackTrace();
                    return null;
                }
            }

            protected void propertyValueChanged(Object old, Object current) {
                if (isChangeFiring())
                    updateCodeExpression();
                super.propertyValueChanged(old, current);
            }

            public void setPropertyContext(
                org.netbeans.modules.form.FormPropertyContext ctx)
            { // disabling this method due to limited persistence
            } // capabilities (compatibility with previous versions)
        }
    }

    // ------------
    // property editors for properties of GridBagLayoutConstraints

    private abstract static class GridBagConstrEditor extends PropertyEditorSupport {
        String[] tags;
        Integer[] values;
        String[] javaInitStrings;
        boolean otherValuesAllowed;

        public String[] getTags() {
            return tags;
        }

        public String getAsText() {
            Object value = getValue();
            for (int i=0; i < values.length; i++)
                if (values[i].equals(value))
                    return tags[i];

            return otherValuesAllowed && value != null ?
                       value.toString() : null;
        }

        public void setAsText(String str) {
            for (int i=0; i < tags.length; i++)
                if (tags[i].equals(str)) {
                    setValue(values[i]);
                    return;
                }

            if (otherValuesAllowed)
                try {
                    setValue(new Integer(Integer.parseInt(str)));
                } 
                catch (NumberFormatException e) {} // ignore
        }

        public String getJavaInitializationString() {
            Object value = getValue();
            for (int i=0; i < values.length; i++)
                if (values[i].equals(value))
                    return javaInitStrings[i];

            if (!otherValuesAllowed)
                return javaInitStrings[0];
            return value != null ? value.toString() : null;
        }
    }

    public static final class GridPosEditor extends GridBagConstrEditor
                                        implements EnhancedPropertyEditor {
        public GridPosEditor() {
            tags = new String[] {
                getBundle().getString("VALUE_relative") // NOI18N
            };
            values = new Integer[] {
                new Integer(GridBagConstraints.RELATIVE)
            };
            javaInitStrings = new String[] {
                "java.awt.GridBagConstraints.RELATIVE" // NOI18N
            };
            otherValuesAllowed = true;
        }

        public Component getInPlaceCustomEditor() {
            return null;
        }
        public boolean hasInPlaceCustomEditor() {
            return false;
        }
        public boolean supportsEditingTaggedValues() {
            return true;
        }
    }

    public static final class GridSizeEditor extends GridBagConstrEditor
                                        implements EnhancedPropertyEditor {
        public GridSizeEditor() {
            tags = new String[] {
                getBundle().getString("VALUE_relative"), // NOI18N
                getBundle().getString("VALUE_remainder") // NOI18N
            };
            values = new Integer[] {
                new Integer(GridBagConstraints.RELATIVE),
                new Integer(GridBagConstraints.REMAINDER)
            };
            javaInitStrings = new String[] {
                "java.awt.GridBagConstraints.RELATIVE", // NOI18N
                "java.awt.GridBagConstraints.REMAINDER" // NOI18N
            };
            otherValuesAllowed = true;
        }

        public Component getInPlaceCustomEditor() {
            return null;
        }
        public boolean hasInPlaceCustomEditor() {
            return false;
        }
        public boolean supportsEditingTaggedValues() {
            return true;
        }
    }

    public static final class FillEditor extends GridBagConstrEditor {
        public FillEditor() {
            tags = new String[] {
                getBundle().getString("VALUE_fill_none"), // NOI18N
                getBundle().getString("VALUE_fill_horizontal"), // NOI18N
                getBundle().getString("VALUE_fill_vertical"), // NOI18N
                getBundle().getString("VALUE_fill_both") // NOI18N
            };
            values = new Integer[] {
                new Integer(GridBagConstraints.NONE),
                new Integer(GridBagConstraints.HORIZONTAL),
                new Integer(GridBagConstraints.VERTICAL),
                new Integer(GridBagConstraints.BOTH)
            };
            javaInitStrings = new String[] {
                "java.awt.GridBagConstraints.NONE", // NOI18N
                "java.awt.GridBagConstraints.HORIZONTAL", // NOI18N
                "java.awt.GridBagConstraints.VERTICAL", // NOI18N
                "java.awt.GridBagConstraints.BOTH" // NOI18N
            };
            otherValuesAllowed = false;
        }
    }

    public static final class AnchorEditor extends GridBagConstrEditor {
        public AnchorEditor() {
            tags = new String[] {
                getBundle().getString("VALUE_anchor_center"), // NOI18N
                getBundle().getString("VALUE_anchor_north"), // NOI18N
                getBundle().getString("VALUE_anchor_northeast"), // NOI18N
                getBundle().getString("VALUE_anchor_east"), // NOI18N
                getBundle().getString("VALUE_anchor_southeast"), // NOI18N
                getBundle().getString("VALUE_anchor_south"), // NOI18N
                getBundle().getString("VALUE_anchor_southwest"), // NOI18N
                getBundle().getString("VALUE_anchor_west"), // NOI18N
                getBundle().getString("VALUE_anchor_northwest"), // NOI18N
            };
            values = new Integer[] {
                new Integer(GridBagConstraints.CENTER),
                new Integer(GridBagConstraints.NORTH),
                new Integer(GridBagConstraints.NORTHEAST),
                new Integer(GridBagConstraints.EAST),
                new Integer(GridBagConstraints.SOUTHEAST),
                new Integer(GridBagConstraints.SOUTH),
                new Integer(GridBagConstraints.SOUTHWEST),
                new Integer(GridBagConstraints.WEST),
                new Integer(GridBagConstraints.NORTHWEST)
            };
            javaInitStrings = new String[] {
                "java.awt.GridBagConstraints.CENTER", // NOI18N
                "java.awt.GridBagConstraints.NORTH", // NOI18N
                "java.awt.GridBagConstraints.NORTHEAST", // NOI18N
                "java.awt.GridBagConstraints.EAST", // NOI18N
                "java.awt.GridBagConstraints.SOUTHEAST", // NOI18N
                "java.awt.GridBagConstraints.SOUTH", // NOI18N
                "java.awt.GridBagConstraints.SOUTHWEST", // NOI18N
                "java.awt.GridBagConstraints.WEST", // NOI18N
                "java.awt.GridBagConstraints.NORTHWEST" // NOI18N
            };
            otherValuesAllowed = false;
        }
    }

    // ------
    // temporary hacks for GridBagCustomizer and GridBagControlCenter

    static ResourceBundle getBundleHack() {
        return getBundle(); // from AbstractLayoutSupport
    }

    LayoutSupportContext getLayoutSupportHack() {
        return super.getLayoutContext();
    }
}
