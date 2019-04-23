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

import cd.go.plugin.secret.filebased.executors.LookupSecretsRequestExecutor;
import cd.go.plugin.secret.filebased.model.SecretsConfiguration;
import com.github.bdpiparva.plugin.base.dispatcher.RequestDispatcher;
import com.github.bdpiparva.plugin.base.dispatcher.RequestDispatcherBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.Arrays;

@Extension
public class FileBasedSecretsPlugin implements GoPlugin {
    private RequestDispatcher requestDispatcher;

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
        requestDispatcher = RequestDispatcherBuilder
                .forSecret(goApplicationAccessor)
                .icon("/plugin-icon.png", "image/png")
                .configMetadata(SecretsConfiguration.class)
                .configView("/secrets.template.html")
                .validateSecretConfig()
                .lookup(new LookupSecretsRequestExecutor())
                .build();
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) throws UnhandledRequestTypeException {
        return requestDispatcher.dispatch(request);
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier("secrets", Arrays.asList("1.0"));
    }
}
