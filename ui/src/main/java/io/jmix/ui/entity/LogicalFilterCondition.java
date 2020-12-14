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

package io.jmix.ui.entity;

import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.ui.component.LogicalFilterComponent;

import javax.persistence.Convert;
import java.util.ArrayList;
import java.util.List;

@JmixEntity(name = "ui_LogicalFilterCondition")
@SystemLevel
public abstract class LogicalFilterCondition extends FilterCondition {

    @JmixProperty
    @Convert(converter = GroupFilterOperationConverter.class)
    protected LogicalFilterComponent.Operation operation;

    @JmixProperty
    protected List<FilterCondition> ownFilterConditions = new ArrayList<>();

    public void setOwnFilterConditions(List<FilterCondition> ownFilterConditions) {
        this.ownFilterConditions = ownFilterConditions;
    }

    public LogicalFilterComponent.Operation getOperation() {
        return operation;
    }

    public void setOperation(LogicalFilterComponent.Operation operation) {
        this.operation = operation;
    }

    public List<FilterCondition> getOwnFilterConditions() {
        return ownFilterConditions;
    }
}
