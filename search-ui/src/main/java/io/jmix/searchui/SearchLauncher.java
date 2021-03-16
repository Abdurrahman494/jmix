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

package io.jmix.searchui;

import io.jmix.ui.screen.FrameOwner;

/**
 * Performs full text search and shows results window.
 */
public interface SearchLauncher {

    String NAME = "search_SearchLauncher";

    /**
     * Search for any objects by search term.
     *
     * @param origin     origin screen
     * @param searchTerm search term
     */
    void search(FrameOwner origin, String searchTerm);
}
