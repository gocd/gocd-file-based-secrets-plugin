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

import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONAssert.*;

class ValidateConfigRequestExecutorTest {

    @Test
    void shouldReturnErrorSecretFilePathIsEmpty() throws JSONException {
        GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestBody()).thenReturn(new Gson().toJson(Collections.singletonMap("SecretsFilePath", "")));

        GoPluginApiResponse response = new ValidateConfigRequestExecutor().execute(request);

        assertThat(response.responseCode()).isEqualTo(DefaultGoPluginApiResponse.VALIDATION_FAILED);
        assertEquals("[\n" +
                "  {\n" +
                "    \"key\": \"SecretsFilePath\",\n" +
                "    \"message\": \"Secrets file path must not be blank\"\n" +
                "  }\n" +
                "]", response.responseBody(), true);
    }

    @Test
    void shouldNotReturnErrorIfValidationIsSuccessful(@TempDir Path tempDirectory) throws IOException {
        File secretsFile = tempDirectory.resolve("some-secrets-file").toFile();
        secretsFile.createNewFile();

        GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("SecretsFilePath", secretsFile.getAbsolutePath());

        when(request.requestBody()).thenReturn(new Gson().toJson(requestMap));

        GoPluginApiResponse response = new ValidateConfigRequestExecutor().execute(request);
        assertThat(response.responseCode()).isEqualTo(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE);
        assertThat(response.responseBody()).isEqualTo("[]");
    }
}
