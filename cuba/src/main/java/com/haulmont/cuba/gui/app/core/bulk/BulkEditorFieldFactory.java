/*
 * Copyright 2020 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.cuba.gui.app.core.bulk;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.UiComponents;

import javax.annotation.Nullable;
import java.sql.Time;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BulkEditorFieldFactory {

    protected UiComponents componentsFactory = AppBeans.get(UiComponents.class);
    protected Messages messages = AppBeans.get(Messages.class);

    protected static final int MAX_TEXTFIELD_STRING_LENGTH = 255;

    @Nullable
    public Field createField(Datasource datasource, MetaProperty property) {
        // TODO: dynamic attributes
        /*if (DynamicAttributesUtils.isDynamicAttribute(property)) {
            CategoryAttribute attribute = DynamicAttributesUtils.getCategoryAttribute(property);
            if (attribute.getDataType().equals(PropertyType.ENUMERATION)
                    && BooleanUtils.isNotTrue(attribute.getIsCollection())) {
                return createEnumField(datasource, property);
            } else if (BooleanUtils.isTrue(attribute.getIsCollection())) {
                return createListEditorField(datasource, property);
            }
        }*/

        if (property.getRange().isDatatype()) {
            Class type = property.getRange().asDatatype().getJavaClass();
            if (type.equals(String.class)) {
                return createStringField(datasource, property);
            } else if (type.equals(Boolean.class)) {
                return createBooleanField(datasource, property);
            } else if (type.equals(java.time.LocalDate.class)) {
                return createDateField(datasource, property);
            } else if (type.equals(java.sql.Date.class) || type.equals(Date.class)) {
                return createDateField(datasource, property);
            } else if (type.equals(Time.class)) {
                return createTimeField(datasource, property);
            } else if (Number.class.isAssignableFrom(type)) {
                return createNumberField(datasource, property);
            }
        } else if (property.getRange().isClass()) {
            return createEntityField(datasource, property);
        } else if (property.getRange().isEnum()) {
            return createEnumField(datasource, property);
        }
        return null;
    }

    protected Field createStringField(Datasource datasource, MetaProperty property) {
        Integer textLength = (Integer) property.getAnnotations().get("length");
        boolean isLong = textLength == null || textLength > MAX_TEXTFIELD_STRING_LENGTH;

        TextInputField textField;
        if (!isLong) {
            textField = componentsFactory.create(TextField.class);
        } else {
            TextArea textArea = componentsFactory.create(TextArea.class);
            textArea.setRows(3);
            textField = textArea;
        }

        textField.setDatasource(datasource, property.getName());

        if (textLength != null) {
            ((TextInputField.MaxLengthLimited) textField).setMaxLength(textLength);
        }

        return textField;
    }

    protected Field createBooleanField(final Datasource datasource, MetaProperty property) {
        LookupField lookupField = componentsFactory.create(LookupField.class);
        lookupField.setDatasource(datasource, property.getName());

        Map<String, Object> options = new HashMap<>();
        options.put(messages.getMessage("boolean.yes"), Boolean.TRUE);
        options.put(messages.getMessage("boolean.no"), Boolean.FALSE);

        lookupField.setOptionsMap(options);

        return lookupField;
    }

    protected Field createDateField(Datasource datasource, MetaProperty property) {
        Class type = property.getRange().asDatatype().getJavaClass();

        DateField dateField = componentsFactory.create(DateField.class);
        dateField.setDatasource(datasource, property.getName());

        if (type.equals(Date.class)) {
            dateField.setResolution(DateField.Resolution.MIN);
            dateField.setDateFormat(messages.getMainMessage("dateTimeFormat"));
        } else if (type.equals(java.sql.Date.class)) {
            dateField.setResolution(DateField.Resolution.SEC);
            dateField.setDateFormat(messages.getMainMessage("dateFormat"));
        } else if (type.equals(java.time.LocalDate.class)) {
            dateField.setResolution(DateField.Resolution.DAY);
            dateField.setDateFormat(messages.getMainMessage("dateFormat"));
        } else {
            throw new RuntimeException("Unknown type for " + property);
        }

        return dateField;
    }

    protected Field createTimeField(Datasource datasource, MetaProperty property) {
        TimeField timeField = componentsFactory.create(TimeField.class);
        timeField.setDatasource(datasource, property.getName());
        timeField.setShowSeconds(true);
        return timeField;
    }

    protected Field createNumberField(Datasource datasource, MetaProperty property) {
        TextField textField = componentsFactory.create(TextField.class);
        textField.setDatasource(datasource, property.getName());
        return textField;
    }

    protected Field createEntityField(Datasource datasource, MetaProperty property) {
        PickerField pickerField = componentsFactory.create(PickerField.class);
        pickerField.addLookupAction();
        pickerField.addClearAction();

        pickerField.setDatasource(datasource, property.getName());

        return pickerField;
    }

    protected Field createEnumField(Datasource datasource, MetaProperty property) {
        LookupField lookupField = componentsFactory.create(LookupField.class);
        lookupField.setDatasource(datasource, property.getName());

        return lookupField;
    }

    /*protected Field createListEditorField(Datasource datasource, MetaProperty property) {
        DynamicAttributeCustomFieldGenerator generator = new DynamicAttributeCustomFieldGenerator();

        //noinspection UnnecessaryLocalVariable
        ListEditor editor = (ListEditor) generator.generateField(datasource, property.getName());
        return editor;
    }*/
}