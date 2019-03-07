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

package cd.go.plugin.secret.filebased;

import cd.go.plugin.secret.filebased.db.SecretsDatabase;
import cd.go.plugin.secret.filebased.executors.RequestFromServer;
import cd.go.plugin.secret.filebased.model.LookupSecretRequest;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.request.DefaultGoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.UUID;

class FileBasedSecretsPluginTest {

    private File databaseFile;

    @BeforeEach
    void setup(@TempDir Path tempDir) throws GeneralSecurityException, IOException {
        this.databaseFile = new File(tempDir.toFile(), UUID.randomUUID().toString().substring(0, 8));
        new SecretsDatabase().addSecret("secret-key", "secret-value").saveTo(databaseFile);
    }

    @Nested
    class LookupSecret {
        @Test
        void shouldReturnValuesIfKeyIsPresent() throws UnhandledRequestTypeException, JSONException {
            DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest("secrets", "1.0", RequestFromServer.REQUEST_SECRETS_LOOKUP.requestName());
            LookupSecretRequest lookupSecretRequest = new LookupSecretRequest(databaseFile.getAbsolutePath(), Arrays.asList("secret-key", "non-existing-key"));

            request.setRequestBody(lookupSecretRequest.toJSON());
            GoPluginApiResponse response = new FileBasedSecretsPlugin().handle(request);

            JSONAssert.assertEquals(response.responseBody(), "{\"secret-key\":\"secret-value\"}", false);
        }

        @Test
        void shouldReturnEmptyResponseIfSecretWithGivenKeyIsNotPresent() throws UnhandledRequestTypeException, JSONException {
            DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest("secrets", "1.0", RequestFromServer.REQUEST_SECRETS_LOOKUP.requestName());

            LookupSecretRequest lookupSecretRequest = new LookupSecretRequest(databaseFile.getAbsolutePath(), Arrays.asList("non-exiting-key"));

            request.setRequestBody(lookupSecretRequest.toJSON());
            GoPluginApiResponse response = new FileBasedSecretsPlugin().handle(request);

            JSONAssert.assertEquals(response.responseBody(), "{}", false);
        }
    }
}
