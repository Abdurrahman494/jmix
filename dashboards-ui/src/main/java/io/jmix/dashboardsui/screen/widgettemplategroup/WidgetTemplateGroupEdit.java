/*
 * Copyright 2021 Haulmont.
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
package io.jmix.dashboardsui.screen.widgettemplategroup;

import io.jmix.dashboards.entity.WidgetTemplateGroup;
import io.jmix.ui.action.list.AddAction;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@UiController("dshbrd_WidgetTemplateGroup.edit")
@UiDescriptor("widget-template-group-edit.xml")
@EditedEntityContainer("widgetTemplateGroupDc")
public class WidgetTemplateGroupEdit extends StandardEditor<WidgetTemplateGroup> {
    @Autowired
    @Qualifier("widgetTemplatesTable.add")
    protected AddAction add;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        add.setScreenId("dshbrd_WidgetTemplate.browse");//todo
    }
}