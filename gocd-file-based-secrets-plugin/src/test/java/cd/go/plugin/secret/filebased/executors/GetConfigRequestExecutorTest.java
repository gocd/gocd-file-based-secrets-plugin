/*
 * Copyright 2019 ThoughtWorks, Inc.
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

package cd.go.plugin.secret.filebased.executors;

import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.assertj.core.api.Assertions.*;

class GetConfigRequestExecutorTest {
    @Test
    void shouldReturnSecretsConfig() throws JSONException {
        GoPluginApiResponse response = new GetConfigRequestExecutor().execute();

        assertThat(response.responseCode()).isEqualTo(200);

        JSONAssert.assertEquals("[\n" +
                "  {\n" +
                "    \"key\": \"SecretsFilePath\",\n" +
                "    \"metadata\": {\n" +
                "      \"secure\": true,\n" +
                "      \"display_name\": \"Secrets file path\",\n" +
                "      \"required\": true\n" +
                "    }\n" +
                "  }\n" +
                "]", response.responseBody(), true);
    }
}
