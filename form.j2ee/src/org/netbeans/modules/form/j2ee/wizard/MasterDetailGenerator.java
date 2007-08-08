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
 */

package org.netbeans.modules.form.j2ee.wizard;

import java.io.*;
import java.util.*;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.form.project.ClassPathUtils;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;

/**
 * Generator of master/detail form.
 *
 * @author Jan Stola
 */
public class MasterDetailGenerator {
    /** Name of the template for the label component. */
    private static final String LABEL_TEMPLATE = "LABEL_TEMPLATE"; // NOI18N
    /** Name of the template for the field component. */
    private static final String FIELD_TEMPLATE = "FIELD_TEMPLATE"; // NOI18N
    /** Name of the template for the master subbindigs. */
    private static final String MASTER_SUBBINDING_TEMPLATE = "MASTER_SUBBINDING_TEMPLATE"; // NOI18N
    /** Name of the template for the detail subbindigs. */
    private static final String DETAIL_SUBBINDING_TEMPLATE = "DETAIL_SUBBINDING_TEMPLATE"; // NOI18N
    /** Name of the template for the vertical layout. */
    private static final String V_LAYOUT_TEMPLATE = "V_LAYOUT_TEMPLATE"; // NOI18N
    /** Name of the template for the horizontal layout of labels. */
    private static final String LABEL_H_LAYOUT_TEMPLATE = "LABEL_H_LAYOUT_TEMPLATE"; // NOI18N
    /** Name of the template for the horizontal layout of fields. */
    private static final String FIELD_H_LAYOUT_TEMPLATE = "FIELD_H_LAYOUT_TEMPLATE"; // NOI18N
    /** Name of the sections that are valid only when the detail table is specified. */
    private static final String DETAIL_ONLY = "DETAIL_ONLY"; // NOI18N
    /** Name of the sections that are valid only when the detail table is not specified. */
    private static final String MASTER_ONLY = "MASTER_ONLY"; // NOI18N
    /** Name of the sections that are valid only when the template is created on JDK1.6. */
    private static final String JDK6ONLY = "JDK6ONLY"; // NOI18N
    /** Form file. */
    private FileObject formFile;
    /** Java file. */
    private FileObject javaFile;
    /** Class name of the master entity. */
    private String masterClass;
    /** Class name of the detail entity. */
    private String detailClass;
    /** Name of the master entity. */
    private String masterEntity;
    /** Name of the detail entity. */
    private String detailEntity;
    /** Name of the join property. */
    private String joinProperty;
    /** Name of the join collection property. */
    private String joinCollectionProperty;    
    /** Name of the persistence unit. */
    private String unit;
    /** Columns of the master table. */
    private List<String> masterColumns;
    /** Columns of the detail table. */
    private List<String> detailColumns;
    /** Types of columns in master table. */
    private List<String> masterColumnTypes;
    /** Types of columns in detail table. */
    private List<String> detailColumnTypes;

    /**
     * Creates new <code>MasterDetailGenerator</code>.
     *
     * @param formFile form file.
     * @param javaFile java file.
     * @param masterClass class name of the master entity.
     * @param detailClass class name of the detail entity.
     * @param masterEntity name of the master entity.
     * @param detailEntity name of the detail entity.
     * @param joinProperty name of the join/fk property.
     * @param joinCollectionProperty name of the join/fk collection property.
     * @param unit name of the persistence unit.
     */
    MasterDetailGenerator(FileObject formFile, FileObject javaFile,
            String masterClass, String detailClass, String masterEntity, String detailEntity,
            String joinProperty, String joinCollectionProperty, String unit) {
        this.formFile = formFile;
        this.javaFile = javaFile;
        this.masterClass = masterClass;
        this.detailClass = detailClass;
        this.masterEntity = masterEntity;
        this.detailEntity = detailEntity;
        this.joinProperty = joinProperty;
        this.joinCollectionProperty = joinCollectionProperty;
        this.unit = unit;
    }

    /**
     * Sets columns of the master table.
     *
     * @param masterColumns columns of the master table.
     */
    void setMasterColumns(List<String> masterColumns) {
        this.masterColumns = masterColumns;
    }

    void setMasterColumnTypes(List<String> masterColumnTypes) {
        this.masterColumnTypes = masterColumnTypes;
    }

    /**
     * Sets columns of the detail table.
     *
     * @param detailColumns columns of the detail table.
     */
    void setDetailColumns(List<String> detailColumns) {
        this.detailColumns = detailColumns;
    }

    void setDetailColumnTypes(List<String> detailColumnTypes) {
        this.detailColumnTypes = detailColumnTypes;
    }

    /**
     * Generates the master/detail form.
     *
     * @throws IOException if the generation fails.
     */
    void generate() throws IOException {
        String formEncoding = "UTF-8"; // NOI18N
        String javaEncoding = FileEncodingQuery.getDefaultEncoding().name();
        String form = read(formFile, formEncoding);
        String java = read(javaFile, javaEncoding);
        Map<String,String> replacements = replacements();
        for (Map.Entry<String,String> entry : replacements.entrySet()) {
            form = form.replace(entry.getKey(), entry.getValue());
            java = java.replace(entry.getKey(), entry.getValue());
        }
        form = generateMasterColumns(form);

        if (detailEntity == null) {
            form = generateLabels(form);
            form = generateFields(form);
            form = generateVLayout(form);
            form = generateLabelsHLayout(form);
            form = generateFieldsHLayout(form);
            form = deleteSections(form, DETAIL_ONLY, false, false);
            form = deleteSections(form, MASTER_ONLY, true, false);
            java = deleteSections(java, DETAIL_ONLY, false, true);
        } else {
            form = generateDetailColumns(form);
            java = deleteSections(java, DETAIL_ONLY, true, true);
            form = deleteSections(form, MASTER_ONLY, false, false);
            form = deleteSections(form, DETAIL_ONLY, true, false);
        }
        java = deleteSections(java, JDK6ONLY, ClassPathUtils.isJava6ProjectPlatform(javaFile), true);

        write(formFile, form, formEncoding);
        write(javaFile, java, javaEncoding);
    }

    /**
     * Generates the content specified by <code>MASTER_SUBBINDING_TEMPLATE</code>.
     *
     * @param result the data being regenerated.
     * @return result of the generation.
     */
    private String generateMasterColumns(String result) {
        String template = findTemplate(result, MASTER_SUBBINDING_TEMPLATE);
        int index = result.indexOf(template);
        result = result.substring(0, index) + result.substring(index+template.length());
        template = uncomment(template, false);

        StringBuilder sb = new StringBuilder();
        int i = 0;
        Iterator<String> iter = masterColumnTypes.iterator();
        for (String column : masterColumns) {
            String binding = template.replace("_index_", ""+i++); // NOI18N
            binding = binding.replace("_fieldName_", column); // NOI18N
            String type = iter.next();
            if (type == null) { // fallback - shouldn't happen - means corrupted entity
                type = "Object.class"; // NOI18N
            }
            binding = binding.replace("_fieldType_", type); // NOI18N
            sb.append(binding);
        }
        StringBuilder rsb = new StringBuilder(result);
        rsb.insert(index, sb.toString());
        return rsb.toString();
    }

    /**
     * Generates the content specified by <code>DETAIL_SUBBINDING_TEMPLATE</code>.
     *
     * @param result the data being regenerated.
     * @return result of the generation.
     */
    private String generateDetailColumns(String result) {
        String template = findTemplate(result, DETAIL_SUBBINDING_TEMPLATE);
        int index = result.indexOf(template);
        result = result.substring(0, index) + result.substring(index+template.length());
        template = uncomment(template, false);

        StringBuilder sb = new StringBuilder();
        int i = 0;
        Iterator<String> iter = detailColumnTypes.iterator();
        for (String column : detailColumns) {
            String binding = template.replace("_index_", ""+i++); // NOI18N
            binding = binding.replace("_fieldName_", column); // NOI18N
            binding = binding.replace("_fieldType_", iter.next()); // NOI18N
            sb.append(binding);
        }
        StringBuilder rsb = new StringBuilder(result);
        rsb.insert(index, sb.toString());
        return rsb.toString();
    }

    /**
     * Generates the content specified by <code>LABEL_TEMPLATE</code>.
     *
     * @param result the data being regenerated.
     * @return result of the generation.
     */
    private String generateLabels(String result) {
        String template = findTemplate(result, LABEL_TEMPLATE);
        int index = result.indexOf(template);
        result = result.substring(0, index) + result.substring(index+template.length());
        template = uncomment(template, false);

        StringBuilder sb = new StringBuilder();
        for (String column : detailColumns) {
            String binding = template.replace("_labelName_", columnToLabelName(column)); // NOI18N
            binding = binding.replace("_labelText_", capitalize(column)); // NOI18N
            sb.append(binding);
        }
        StringBuilder rsb = new StringBuilder(result);
        rsb.insert(index, sb.toString());
        return rsb.toString();
    }

    /**
     * Generates the content specified by <code>FIELD_TEMPLATE</code>.
     *
     * @param result the data being regenerated.
     * @return result of the generation.
     */
    private String generateFields(String result) {
        String template = findTemplate(result, FIELD_TEMPLATE);
        int index = result.indexOf(template);
        result = result.substring(0, index) + result.substring(index+template.length());
        template = uncomment(template, false);

        StringBuilder sb = new StringBuilder();
        for (String column : detailColumns) {
            String binding = template.replace("_textFieldName_", columnToFieldName(column)); // NOI18N
            binding = binding.replace("_fieldName_", column); // NOI18N
            sb.append(binding);
        }
        StringBuilder rsb = new StringBuilder(result);
        rsb.insert(index, sb.toString());
        return rsb.toString();
    }

    /**
     * Generates the content specified by <code>V_LAYOUT_TEMPLATE</code>.
     *
     * @param result the data being regenerated.
     * @return result of the generation.
     */
    private String generateVLayout(String result) {
        String template = findTemplate(result, V_LAYOUT_TEMPLATE);
        int index = result.indexOf(template);
        result = result.substring(0, index) + result.substring(index+template.length());
        template = uncomment(template, false);

        StringBuilder sb = new StringBuilder();
        for (String column : detailColumns) {
            String binding = template.replace("_labelName_", columnToLabelName(column)); // NOI18N
            binding = binding.replace("_textFieldName_", columnToFieldName(column)); // NOI18N
            sb.append(binding);
        }
        StringBuilder rsb = new StringBuilder(result);
        rsb.insert(index, sb.toString());
        return rsb.toString();
    }

    /**
     * Generates the content specified by <code>LABEL_H_LAYOUT_TEMPLATE</code>.
     *
     * @param result the data being regenerated.
     * @return result of the generation.
     */
    private String generateLabelsHLayout(String result) {
        String template = findTemplate(result, LABEL_H_LAYOUT_TEMPLATE);
        int index = result.indexOf(template);
        result = result.substring(0, index) + result.substring(index+template.length());
        template = uncomment(template, false);

        StringBuilder sb = new StringBuilder();
        for (String column : detailColumns) {
            String binding = template.replace("_labelName_", columnToLabelName(column)); // NOI18N
            sb.append(binding);
        }
        StringBuilder rsb = new StringBuilder(result);
        rsb.insert(index, sb.toString());
        return rsb.toString();
    }

    /**
     * Generates the content specified by <code>FIELD_H_LAYOUT_TEMPLATE</code>.
     *
     * @param result the data being regenerated.
     * @return result of the generation.
     */
    private String generateFieldsHLayout(String result) {
        String template = findTemplate(result, FIELD_H_LAYOUT_TEMPLATE);
        int index = result.indexOf(template);
        result = result.substring(0, index) + result.substring(index+template.length());
        template = uncomment(template, false);

        StringBuilder sb = new StringBuilder();
        for (String column : detailColumns) {
            String binding = template.replace("_textFieldName_", columnToFieldName(column)); // NOI18N
            sb.append(binding);
        }
        StringBuilder rsb = new StringBuilder(result);
        rsb.insert(index, sb.toString());
        return rsb.toString();
    }

    /**
     * Removes sections with the specified name from the given text.
     *
     * @param result text to remove the sections from.
     * @param sectionName name of the sections to remove.
     * @param commentsOnly determines whether to remove the whole secion
     * or just the comment tags.
     * @param java determines whether the section is marked by Java or HTML/XML comment.
     * @return text with the specified sections removed.
     */
    private static String deleteSections(String result, String sectionName, boolean commentsOnly, boolean java) {
        String section = comment(sectionName, java);
        int begin;
        if (commentsOnly) {
            while ((begin = result.indexOf(section)) != -1) {
                result = result.substring(0, begin) + result.substring(begin+section.length());
            }
        } else {
            while ((begin = result.indexOf(section)) != -1) {
                int end = result.indexOf(section, begin+1);
                assert (end != -1);
                result = result.substring(0, begin) + result.substring(end+section.length());
            }            
        }
        return result;
    }

    /**
     * Comments (e.g. adds the HTML/XML or Java comment) around the given text.
     *
     * @param comment comment that should be wrapped.
     * @param java determines whether to use Java or HTML/XML comments.
     * @return wrapped comment.
     */
    private static String comment(String comment, boolean java) {
        String open = java ? "/*" : "<!--"; // NOI18N
        String close = java ? "*/" : "-->"; // NOI18N
        return open + " " + comment + " " + close; // NOI18N
    }
    
    /**
     * Uncomments (e.g. removes the HTML comment tags) from the given text.
     *
     * @param comment comment that should be unwrapped.
     * @param java determines whether to use Java or HTML/XML comments.
     * @return unwrapped comment.
     */
    private static String uncomment(String comment, boolean java) {
        String open = java ? "/*" : "<!--"; // NOI18N
        String close = java ? "*/" : "-->"; // NOI18N
        int begin = comment.indexOf(close); // NOI18N
        int end = comment.lastIndexOf(open); // NOI18N
        return comment.substring(begin + open.length(), end);
    }

    /**
     * Returns name of the label that corresponds to the given column.
     *
     * @param column name of the column.
     * @return name of the label that corresponds to the given column.
     */
    private static String columnToLabelName(String column) {
        return column + "Label"; // NOI18N
    }

    /**
     * Returns name of the field that corresponds to the given column.
     *
     * @param column name of the column.
     * @return name of the field that corresponds to the given column.
     */
    private static String columnToFieldName(String column) {
        return column + "Field"; // NOI18N
    }

    /**
     * Reads the content of the file.
     *
     * @param file file whose content should be read.
     * @return the content of the file.
     * @throws IOException when the reading fails.
     */
    private static String read(FileObject file, String encoding) throws IOException {
        InputStream is = file.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, encoding));
        StringBuilder sb = new StringBuilder();
        String s;
        while ((s=br.readLine()) != null) {
            sb.append(s).append('\n');
        }
        br.close();
        return sb.toString();
    }

    /**
     * Writes the content of the file.
     *
     * @param file file whose content should be written.
     * @param content new content of the file.
     * @throws IOException when the writing fails.
     */
    private static void write(FileObject file, String content, String encoding) throws IOException {
        FileLock lock = file.lock();
        try {
            OutputStream os = file.getOutputStream(lock);
            os.write(content.getBytes(encoding));
            os.close();
        } finally {
            lock.releaseLock();
        }
    }

    /**
     * Returns map of general replacements.
     *
     * @return map of general replacements.
     */
    private Map<String,String> replacements() {
        Map<String,String> map = new HashMap<String,String>();
        map.put("_masterClass_", masterClass); // NOI18N
        map.put("_masterEntity_", masterEntity); // NOI18N
        char masterInitial = Character.toLowerCase(masterEntity.charAt(0));
        map.put("_unitName_", unit); // NOI18N
        if (detailClass != null) {
            map.put("_detailClass_", detailClass); // NOI18N
            map.put("_detailEntity_", detailEntity); // NOI18N
            char detailInitial = Character.toLowerCase(detailEntity.charAt(0));
            map.put("_detailEntityInitial_", Character.toString(detailInitial)); // NOI18N
            if (detailInitial == masterInitial) {
                masterInitial = Character.toUpperCase(masterInitial);
            }
            map.put("_joinCollection_", joinCollectionProperty); // NOI18N
            map.put("_joinCollectionCapital_", Character.toUpperCase(joinCollectionProperty.charAt(0)) + joinCollectionProperty.substring(1)); // NOI18N
            map.put("_joinCapital_", Character.toUpperCase(joinProperty.charAt(0)) + joinProperty.substring(1)); // NOI18N
        }
        map.put("_masterEntityInitial_", Character.toString(masterInitial)); // NOI18N
        return map;
    }

    /**
     * Finds the content of the template.
     *
     * @param where where the template should be found.
     * @param templateName name of the template.
     * @return the content of the template.
     */
    private static String findTemplate(String where, String templateName) {
        String template = comment(templateName, false);
        int index1 = where.indexOf(template);
        int index2 = where.lastIndexOf(template);
        return where.substring(index1, index2 + template.length());
    }

    /**
     * Trasformation aka userName -> User Name.
     *
     * @param title title to transform.
     * @return transformed title.
     */
    private static String capitalize(String title) {
        StringBuilder builder = new StringBuilder(title);
        boolean lastWasUpper = false;
        for (int i = 0; i < builder.length(); i++) {
            char aChar = builder.charAt(i);
            if (i == 0) {
                builder.setCharAt(i, Character.toUpperCase(aChar));
                lastWasUpper = true;
            } else if (Character.isUpperCase(aChar)) {
                if (!lastWasUpper) {
                    builder.insert(i, ' ');
                }
                lastWasUpper = true;
                i++;
            } else {
                lastWasUpper = false;
            }
        }
        return builder.toString();
    }

}
