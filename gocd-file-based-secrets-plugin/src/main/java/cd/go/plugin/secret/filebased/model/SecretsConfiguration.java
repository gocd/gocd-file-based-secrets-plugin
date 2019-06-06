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

package cd.go.plugin.secret.filebased.model;

import com.github.bdpiparva.plugin.base.annotations.Property;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SecretsConfiguration {

    public static final String SECRETS_FILE_PATH_PROPERTY = "SecretsFilePath";

    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    @Expose
    @SerializedName(SECRETS_FILE_PATH_PROPERTY)
    @Property(name = SECRETS_FILE_PATH_PROPERTY, required = true)
    private String secretsFilePath;

    public SecretsConfiguration() {
    }

    SecretsConfiguration(String secretsFilePath) {
        this.secretsFilePath = secretsFilePath;
    }

    public static SecretsConfiguration fromJSON(String requestBody) {
        return GSON.fromJson(requestBody, SecretsConfiguration.class);
    }

    public String getSecretsFilePath() {
        return secretsFilePath;
    }

}
