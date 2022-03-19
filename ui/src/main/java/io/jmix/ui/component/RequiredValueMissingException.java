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

package io.jmix.ui.component;

public class RequiredValueMissingException extends ValidationException
        implements ValidationException.HasRelatedComponent {

    private Component component;

    public RequiredValueMissingException() {
    }

    public RequiredValueMissingException(String message) {
        super(message);
    }

    public RequiredValueMissingException(String message, Component component) {
        super(message);
        this.component = component;
    }

    @Override
    public Component getComponent() {
        return component;
    }
}