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

import java.util.Optional;

public enum RequestFromServer {
    REQUEST_GET_PLUGIN_ICON(Constants.REQUEST_PREFIX + ".get-icon"),
    REQUEST_SECRETS_LOOKUP(Constants.REQUEST_PREFIX + ".secrets-lookup"),
    REQUEST_GET_CONFIG_METADATA(String.join(".", Constants.REQUEST_PREFIX, Constants._SECRETS_CONFIG_METADATA, "get-metadata")),
    REQUEST_GET_CONFIG_VIEW(String.join(".", Constants.REQUEST_PREFIX, Constants._SECRETS_CONFIG_METADATA, "get-view")),
    REQUEST_VALIDATE_CONFIG(String.join(".", Constants.REQUEST_PREFIX, Constants._SECRETS_CONFIG_METADATA, "validate")),
    REQUEST_VERIFY_CONNECTION(String.join(".", Constants.REQUEST_PREFIX, Constants._SECRETS_CONFIG_METADATA, "verify-connection"));


    private final String requestName;

    RequestFromServer(String requestName) {
        this.requestName = requestName;
    }

    public static Optional<RequestFromServer> fromString(String requestName) {
        if (requestName != null) {
            for (RequestFromServer requestFromServer : RequestFromServer.values()) {
                if (requestName.equalsIgnoreCase(requestFromServer.requestName)) {
                    return Optional.of(requestFromServer);
                }
            }
        }
        return Optional.empty();
    }

    public String requestName() {
        return requestName;
    }

    private interface Constants {
        String REQUEST_PREFIX = "go.cd.secrets";
        String _SECRETS_CONFIG_METADATA = "secrets-config";
    }
}
