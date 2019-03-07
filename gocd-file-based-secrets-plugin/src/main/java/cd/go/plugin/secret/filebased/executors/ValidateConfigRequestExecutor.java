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

import cd.go.plugin.secret.filebased.FileBasedSecretsPlugin;
import cd.go.plugin.secret.filebased.model.Field;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class ValidateConfigRequestExecutor {
    public GoPluginApiResponse execute(GoPluginApiRequest request) {
        Map<String, String> errorMap = new HashMap();
        Map<String, String> configMap = new GsonBuilder().create().fromJson(request.requestBody(), new TypeToken<Map<String, String>>(){}.getType());

        configMap.entrySet().stream().forEach(entry -> {
            Field field = GetConfigRequestExecutor.FIELDS.get(entry.getKey());
            Optional<String> validationError = field.validate(entry.getValue());
            validationError.ifPresent(error -> errorMap.put(field.getKey(), error));
        });

        return new DefaultGoPluginApiResponse(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE, FileBasedSecretsPlugin.GSON.toJson(Collections.singletonMap("errors", errorMap)));
    }
}
