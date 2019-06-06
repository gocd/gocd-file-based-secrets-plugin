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
import cd.go.plugin.secret.filebased.model.LookupSecretRequest;
import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
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
import java.util.Collections;
import java.util.UUID;

import static cd.go.plugin.secret.filebased.model.SecretsConfiguration.SECRETS_FILE_PATH_PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

class FileBasedSecretsPluginTest {

    private static final String LOOKUP_SECRET_REQUEST_NAME = "go.cd.secrets.secrets-lookup";

    private static final String REQUEST_VALIDATE_CONFIG = "go.cd.secrets.secrets-config.validate";

    private FileBasedSecretsPlugin secretsPlugin;

    private File databaseFile;

    @BeforeEach
    void setup(@TempDir Path tempDir) throws GeneralSecurityException, IOException {
        this.databaseFile = new File(tempDir.toFile(), UUID.randomUUID().toString().substring(0, 8));
        new SecretsDatabase()
                .addSecret("secret-key", "secret-value")
                .addSecret("username", "foo")
                .addSecret("password", "bar").saveTo(databaseFile);


        secretsPlugin = new FileBasedSecretsPlugin();
        secretsPlugin.initializeGoApplicationAccessor(mock(GoApplicationAccessor.class));
    }

    @Nested
    class ValidateConfig {

        @Test
        void shouldBeValidWhenSecretConfigPathIsValid(@TempDir File testDir) throws UnhandledRequestTypeException, IOException {
            File secretFile = new File(testDir, "secret.db");
            secretFile.createNewFile();
            DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest("secrets", "1.0", REQUEST_VALIDATE_CONFIG);
            request.setRequestBody(new Gson().toJson(Collections.singletonMap(SECRETS_FILE_PATH_PROPERTY, secretFile.getAbsolutePath())));

            GoPluginApiResponse response = secretsPlugin.handle(request);

            assertThat(response.responseCode()).isEqualTo(200);
            assertThat(response.responseBody()).isEqualTo("[]");
        }

        @Test
        void shouldBeErrorWhenSecretFilePathIsNotProvided() throws UnhandledRequestTypeException {
            DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest("secrets", "1.0", REQUEST_VALIDATE_CONFIG);
            request.setRequestBody(new Gson().toJson(Collections.singletonMap(SECRETS_FILE_PATH_PROPERTY, "")));

            GoPluginApiResponse response = secretsPlugin.handle(request);

            assertThat(response.responseCode()).isEqualTo(200);
            assertThat(response.responseBody()).isEqualTo("[{\"key\":\"SecretsFilePath\",\"message\":\"SecretsFilePath must not be blank.\"}]");
        }

        @Test
        void shouldBeErrorWhenSecretFileDoesNotExist(@TempDir File testDir) throws UnhandledRequestTypeException, JSONException {
            File noneExistingFile = new File(testDir, "none-existing-file");
            DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest("secrets", "1.0", REQUEST_VALIDATE_CONFIG);
            request.setRequestBody(new Gson().toJson(Collections.singletonMap(SECRETS_FILE_PATH_PROPERTY, noneExistingFile.getAbsolutePath())));

            GoPluginApiResponse response = secretsPlugin.handle(request);

            assertThat(response.responseCode()).isEqualTo(200);

            String expected = "[\n" +
                    "  {\n" +
                    "    \"key\": \"SecretsFilePath\",\n" +
                    "    \"message\": \"" + String.format("No secret config file at path '%s'.", noneExistingFile.getAbsolutePath()) + "\"\n" +
                    "  }\n" +
                    "]";

            JSONAssert.assertEquals(expected, response.responseBody(), true);
        }

        @Test
        void shouldBeErrorWhenSecretFileIsDirectory(@TempDir File testDir) throws UnhandledRequestTypeException, JSONException {
            File notAFile = new File(testDir, "this-is-dir");
            notAFile.mkdir();
            DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest("secrets", "1.0", REQUEST_VALIDATE_CONFIG);
            request.setRequestBody(new Gson().toJson(Collections.singletonMap(SECRETS_FILE_PATH_PROPERTY, notAFile.getAbsolutePath())));

            GoPluginApiResponse response = secretsPlugin.handle(request);

            assertThat(response.responseCode()).isEqualTo(200);

            String expected = "[\n" +
                    "  {\n" +
                    "    \"key\": \"SecretsFilePath\",\n" +
                    "    \"message\": \"" + String.format("Secret config file path '%s' is not a normal file.", notAFile.getAbsolutePath()) + "\"\n" +
                    "  }\n" +
                    "]";

            JSONAssert.assertEquals(expected, response.responseBody(), true);
        }
    }

    @Nested
    class LookupSecret {

        @Test
        void shouldReturnValidResponseOnlyIfAllKeysArePresent() throws UnhandledRequestTypeException, JSONException {
            DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest("secrets", "1.0", LOOKUP_SECRET_REQUEST_NAME);
            LookupSecretRequest lookupSecretRequest = new LookupSecretRequest(databaseFile.getAbsolutePath(), Arrays.asList("secret-key", "username", "password"));

            request.setRequestBody(lookupSecretRequest.toJSON());
            GoPluginApiResponse response = secretsPlugin.handle(request);

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
        void shouldReturnNotFoundErrorResponseIfOneOrMoreSecretsWithGivenKeyAreNotPresent() throws UnhandledRequestTypeException, JSONException {
            DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest("secrets", "1.0", LOOKUP_SECRET_REQUEST_NAME);

            LookupSecretRequest lookupSecretRequest = new LookupSecretRequest(databaseFile.getAbsolutePath(), Arrays.asList("non-exiting-key"));

            request.setRequestBody(lookupSecretRequest.toJSON());
            GoPluginApiResponse response = secretsPlugin.handle(request);

            assertThat(response.responseCode()).isEqualTo(404);
            assertEquals("{\"message\":\"Secrets with keys [non-exiting-key] not found.\"}", response.responseBody(), true);
        }
    }
}
