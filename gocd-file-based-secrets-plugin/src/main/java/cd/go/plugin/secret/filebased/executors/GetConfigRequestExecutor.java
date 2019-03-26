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
import cd.go.plugin.secret.filebased.model.FilePathField;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cd.go.plugin.secret.filebased.model.SecretsConfiguration.SECRETS_FILE_PATH_PROPERTY;


public class GetConfigRequestExecutor {
    public static final Gson GSON = new GsonBuilder().serializeNulls().create();

    public static final List<Field> FIELD_LIST = Arrays.asList(
            new FilePathField(SECRETS_FILE_PATH_PROPERTY, "Secrets file path",
                    true,
                    true)
    );

    public static final Map<String, Field> FIELDS = FIELD_LIST.stream().collect(Collectors.toMap(Field::getKey, Function.identity()));

    public GoPluginApiResponse execute() {
        return DefaultGoPluginApiResponse.success(GSON.toJson(FIELD_LIST));
    }
}
