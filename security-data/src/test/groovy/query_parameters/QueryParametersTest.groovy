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

package query_parameters

import io.jmix.securitydata.constraint.PredefinedQueryParameters
import org.springframework.beans.factory.annotation.Autowired
import test_support.SecurityDataSpecification

class QueryParametersTest extends SecurityDataSpecification {

    @Autowired
    io.jmix.core.security.Authenticator authenticator

    @Autowired
    PredefinedQueryParameters predefinedQueryParameters

    def "test UserDetails attributes"() {
        when:
        def value = authenticator.withSystem {
            return predefinedQueryParameters.getParameterValue('current_user_username')
        }

        then:
        value == 'system'

        when:
        value = authenticator.withSystem {
            return predefinedQueryParameters.getParameterValue('current_user_enabled')
        }

        then:
        value == true
    }
}
