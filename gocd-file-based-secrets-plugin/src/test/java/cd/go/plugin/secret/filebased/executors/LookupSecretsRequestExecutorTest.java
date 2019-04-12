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
import cd.go.plugin.secret.filebased.model.LookupSecretRequest;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

class LookupSecretsRequestExecutorTest {
    private File databaseFile;

    @BeforeEach
    void setup(@TempDir Path tempDir) throws GeneralSecurityException, IOException {
        this.databaseFile = new File(tempDir.toFile(), UUID.randomUUID().toString().substring(0, 8));
        new SecretsDatabase()
                .addSecret("secret-key", "secret-value")
                .addSecret("username", "foo")
                .addSecret("password", "bar").saveTo(databaseFile);
    }

    @Test
    void shouldLookupSecrets() throws JSONException {
        GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestBody()).thenReturn(new LookupSecretRequest(databaseFile.getAbsolutePath(),
                Arrays.asList("secret-key", "username", "password")).toJSON());

        GoPluginApiResponse response = new LookupSecretsRequestExecutor().execute(request);

        assertThat(response.responseCode()).isEqualTo(200);
        assertEquals("[\n" +
                "  {\n" +
                "    \"key\": \"secret-key\",\n" +
                "    \"value\": \"secret-value\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"username\",\n" +
                "    \"value\": \"foo\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"password\",\n" +
                "    \"value\": \"bar\"\n" +
                "  }\n" +
                "]", response.responseBody(), true);
    }

    @Test
    void shouldReturnEmptyResponseWhenSecretsAreNotPresent() throws JSONException {
        GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestBody()).thenReturn(
                new LookupSecretRequest(databaseFile.getAbsolutePath(), Arrays.asList("randomKey1", "randomKey2")).toJSON());

        GoPluginApiResponse response = new LookupSecretsRequestExecutor().execute(request);

        assertThat(response.responseCode()).isEqualTo(404);
        assertEquals("{\"message\":\"Secrets with keys [randomKey1, randomKey2] not found.\"}", response.responseBody(), true);
    }

}
