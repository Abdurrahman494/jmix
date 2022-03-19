/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.security;

import javax.annotation.Nullable;

/**
 * Stores id of subcomponent and UI permission value which will be applied to this subcomponent
 * or ids of subcomponent and its action and UI permission value which will be applied to subcomponent's action.
 */
public class UiPermissionDescriptor {

    private UiPermissionValue permissionValue;
    private String screenId;
    private String subComponentId;

    private String actionHolderComponentId;
    private String actionId;

    public UiPermissionDescriptor(UiPermissionValue permissionValue, String screenId, String subComponentId) {
        this.permissionValue = permissionValue;
        this.screenId = screenId;
        this.subComponentId = subComponentId;
    }

    public UiPermissionDescriptor(UiPermissionValue permissionValue, String screenId, String actionHolderComponentId,
                                  String actionId) {
        this.permissionValue = permissionValue;
        this.screenId = screenId;

        this.actionHolderComponentId = actionHolderComponentId;
        this.actionId = actionId;
    }

    public UiPermissionValue getPermissionValue() {
        return permissionValue;
    }

    public String getScreenId() {
        return screenId;
    }

    @Nullable
    public String getSubComponentId() {
        return subComponentId;
    }

    @Nullable
    public String getActionHolderComponentId() {
        return actionHolderComponentId;
    }

    @Nullable
    public String getActionId() {
        return actionId;
    }
}