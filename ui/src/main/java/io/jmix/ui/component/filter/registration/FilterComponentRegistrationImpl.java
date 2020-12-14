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

package io.jmix.ui.component.filter.registration;

import io.jmix.core.annotation.Internal;
import io.jmix.ui.component.FilterComponent;
import io.jmix.ui.component.filter.converter.FilterConverter;
import io.jmix.ui.entity.FilterCondition;

@Internal
public class FilterComponentRegistrationImpl implements FilterComponentRegistration {

    protected final Class<? extends FilterComponent> componentClass;
    protected final Class<? extends FilterCondition> modelClass;
    protected final Class<? extends FilterConverter<? extends FilterComponent, ? extends FilterCondition>> converterClass;
    protected final String editScreenId;

    public FilterComponentRegistrationImpl(Class<? extends FilterComponent> componentClass,
                                           Class<? extends FilterCondition> modelClass,
                                           Class<? extends FilterConverter<? extends FilterComponent, ? extends FilterCondition>> converterClass,
                                           String editScreenId) {
        this.componentClass = componentClass;
        this.modelClass = modelClass;
        this.converterClass = converterClass;
        this.editScreenId = editScreenId;
    }

    @Override
    public Class<? extends FilterComponent> getComponentClass() {
        return componentClass;
    }

    @Override
    public Class<? extends FilterCondition> getModelClass() {
        return modelClass;
    }

    @Override
    public Class<? extends FilterConverter<? extends FilterComponent, ? extends FilterCondition>> getConverterClass() {
        return converterClass;
    }

    @Override
    public String getEditScreenId() {
        return editScreenId;
    }
}
