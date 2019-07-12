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

import cd.go.plugin.base.executors.secrets.LookupExecutor;
import cd.go.plugin.secret.filebased.db.BadSecretException;
import cd.go.plugin.secret.filebased.db.SecretsDatabase;
import cd.go.plugin.secret.filebased.model.LookupSecretRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

public class LookupSecretsRequestExecutor extends LookupExecutor<LookupSecretRequest> {

    public static final int NOT_FOUND_ERROR_CODE = 404;

    @Override
    protected GoPluginApiResponse execute(LookupSecretRequest lookupSecretsRequest) {
        List<Map<String, String>> responseList = new ArrayList<>();

        File secretsFile = new File(lookupSecretsRequest.getSecretsFilePath());
        List<String> unresolvedKeys = new ArrayList<>();

        try {
            SecretsDatabase secretsDatabase = SecretsDatabase.readFrom(secretsFile);
            for (String key : lookupSecretsRequest.getKeys()) {
                String secret = secretsDatabase.getSecret(key);
                if (secret != null) {
                    Map<String, String> response = new HashMap<>();
                    response.put("key", key);
                    response.put("value", secret);
                    responseList.add(response);
                } else {
                    unresolvedKeys.add(key);
                }
            }

            if (unresolvedKeys.isEmpty()) {
                return DefaultGoPluginApiResponse.success(GSON.toJson(responseList));
            }

            Map<String, String> response = Collections.singletonMap("message", String.format("Secrets with keys %s not found.", unresolvedKeys));
            return new DefaultGoPluginApiResponse(NOT_FOUND_ERROR_CODE, GSON.toJson(response));
        } catch (IOException | GeneralSecurityException | BadSecretException e) {
            Map<String, String> errorMessage = Collections.singletonMap("message", "Error while looking up secrets: " + e.getMessage());
            return DefaultGoPluginApiResponse.error(GSON.toJson(errorMessage));
        }
    }

    @Override
    protected LookupSecretRequest parseRequest(String body) {
        return LookupSecretRequest.fromJSON(body);
    }
}
