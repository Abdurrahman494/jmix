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

package io.jmix.ui.widget;

import com.vaadin.ui.CheckBox;
import io.jmix.ui.widget.client.checkbox.JmixCheckBoxState;

public class JmixCheckBox extends CheckBox {

    public JmixCheckBox() {
    }

    public JmixCheckBox(String caption) {
        this();
        setCaption(caption);
    }

    @Override
    protected JmixCheckBoxState getState() {
        return (JmixCheckBoxState) super.getState();
    }

    @Override
    protected JmixCheckBoxState getState(boolean markAsDirty) {
        return (JmixCheckBoxState) super.getState(markAsDirty);
    }

    public boolean isCaptionManagedByLayout() {
        return getState(false).captionManagedByLayout;
    }

    public void setCaptionManagedByLayout(boolean captionManagedByLayout) {
        if (isCaptionManagedByLayout() != captionManagedByLayout) {
            getState().captionManagedByLayout = captionManagedByLayout;
        }
    }
}