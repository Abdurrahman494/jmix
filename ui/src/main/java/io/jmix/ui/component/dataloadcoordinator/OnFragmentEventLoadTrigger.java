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

package io.jmix.ui.component.dataloadcoordinator;

import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.sys.UiControllerReflectionInspector;

@StudioElement(
        caption = "OnFragmentEvent Trigger",
        xmlElement = "onFragmentEvent",
        icon = "io/jmix/ui/icon/facet/onFragmentEventLoadTrigger.svg"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "type", type = PropertyType.ENUMERATION, required = true,
                        options = {"Init", "AfterInit", "Attach"})
        }
)
public class OnFragmentEventLoadTrigger extends OnFrameOwnerEventLoadTrigger {

    public OnFragmentEventLoadTrigger(ScreenFragment screenFragment, UiControllerReflectionInspector reflectionInspector, DataLoader loader, Class eventClass) {
        super(screenFragment, reflectionInspector, loader, eventClass);
    }
}
