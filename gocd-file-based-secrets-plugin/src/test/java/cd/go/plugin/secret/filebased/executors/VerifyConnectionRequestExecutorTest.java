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

import cd.go.plugin.secret.filebased.db.SecretsDatabase;
import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VerifyConnectionRequestExecutorTest {

    @Test
    void shouldReturnFailureWhenFileDoesNotExist() {
        GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("SecretsFilePath", "foo");

        when(request.requestBody()).thenReturn(new Gson().toJson(requestMap));

        GoPluginApiResponse execute = new VerifyConnectionRequestExecutor().execute(request);

        assertThat(execute.responseCode()).isEqualTo(500);
        assertThat(execute.responseBody()).isEqualTo("Connection failed");
    }

    @Test
    void shouldReturnConnectionSuccessWhenPathExists(@TempDir Path tempDir) throws NoSuchAlgorithmException, IOException {
        File file = new File(tempDir.toFile(), UUID.randomUUID().toString().substring(0, 8));
        new SecretsDatabase().saveTo(file);

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("SecretsFilePath", file.getAbsolutePath());

        GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestBody()).thenReturn(new Gson().toJson(requestMap));

        GoPluginApiResponse execute = new VerifyConnectionRequestExecutor().execute(request);

        assertThat(execute.responseCode()).isEqualTo(200);
        assertThat(execute.responseBody()).isEqualTo("Connection successful");
    }
}
