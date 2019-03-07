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

import cd.go.plugin.secret.filebased.model.SecretsConfiguration;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.io.File;

public class VerifyConnectionRequestExecutor {
    public GoPluginApiResponse execute(GoPluginApiRequest request) {
        SecretsConfiguration secretsConfiguration = SecretsConfiguration.fromJSON(request.requestBody());
        if (new File(secretsConfiguration.getSecretsFilePath()).exists())  {
            return DefaultGoPluginApiResponse.success("Connection successful");
        }
        return DefaultGoPluginApiResponse.error("Connection failed");
    }
}
