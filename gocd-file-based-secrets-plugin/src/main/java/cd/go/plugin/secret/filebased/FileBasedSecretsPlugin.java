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

import cd.go.plugin.secret.filebased.executors.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.AbstractGoPlugin;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.Arrays;
import java.util.Optional;

@Extension
public class FileBasedSecretsPlugin extends AbstractGoPlugin {
    public static final Gson GSON = new GsonBuilder().serializeNulls().create();
    public static Logger LOGGER = Logger.getLoggerFor(FileBasedSecretsPlugin.class);


    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) throws UnhandledRequestTypeException {
        Optional<RequestFromServer> requestFromServer = RequestFromServer.fromString(request.requestName());

        if (requestFromServer.isPresent()) {
            switch (requestFromServer.get()) {
                case REQUEST_GET_CONFIG_METADATA:
                    return new GetConfigRequestExecutor().execute();
                case REQUEST_GET_CONFIG_VIEW:
                    return new GetViewRequestExecutor().execute();
                case REQUEST_VALIDATE_CONFIG:
                    return new ValidateConfigRequestExecutor().execute(request);
                case REQUEST_SECRETS_LOOKUP:
                    return new LookupSecretsRequestExecutor().execute(request);
                case REQUEST_VERIFY_CONNECTION:
                    return new VerifyConnectionRequestExecutor().execute(request);
                case REQUEST_GET_PLUGIN_ICON:
                    return new GetIconRequestExecutor().execute();
                case PLUGIN_SETTINGS_GET_CONFIGURATION:
                case PLUGIN_SETTINGS_GET_VIEW:
                case PLUGIN_SETTINGS_VALIDATE_CONFIGURATION:
                    return null;
            }
        }

        throw new UnhandledRequestTypeException(request.requestName());
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier("secrets", Arrays.asList("1.0"));
    }
}
