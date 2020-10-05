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

package io.jmix.ui.app.bulk;

import io.jmix.core.metamodel.model.MetaProperty;

import java.util.List;
import java.util.Map;

/**
 * Field sorter for bulk editor window.
 */
@FunctionalInterface
public interface FieldSorter {

    /**
     * Sorts properties from bulk editor window.
     *
     * @param properties properties from bulk editor window to be sort
     * @return map with metaProperties and their indexes
     */
    Map<MetaProperty, Integer> sort(List<MetaProperty> properties);
}
