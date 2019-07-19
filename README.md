# GoCD File Based Secrets Plugin

This is a file based secrets plugin which implements the GoCD [Secret Plugin](https://plugin-api.gocd.org/current/secrets) endpoint. This plugin allows retrieving of secrets that are stored in encrypted files.

For comprehensive details about Secret Management in GoCD please refer to the [documentation](https://docs.gocd.org/current/configuration/secrets_management.html)

## Building the code base

To build the jar, run `./gradlew clean test assemble`

## Installation

- This plugin comes bundled along with the GoCD server, hence a separate installation is not required.
- GoCD introduced support for Secrets Management in v19.6.0, in order to use this plugin your GoCD version should >= 19.6.0.

## Usage instructions
  
  1. Download the plugin jar from the [GitHub Releases page](https://github.com/gocd/gocd-file-based-secrets-plugin)
  2. Execute the `init` command to initialize the secret database. Although it's optional but it is recommended to 
  store your secret file under CONFIG_DIR. Doing this will make secrets database file part of the backup process. 
  The CONFIG_DIR is typically /etc/go on Linux and C:\Program Files\Go Server\config on Windows. 

  ```shell
  java -jar gocd-file-based-secrets-plugin-$VERSION$.jar init -f secret.db
  ```
  3. Add/Update a secret:
  ```shell
  java -jar gocd-file-based-secrets-plugin-$VERSION$.jar add -f secret.db -n my-password -v
  ```
  4. Show the value of the secret:
  ```shell
  java -jar gocd-file-based-secrets-plugin-$VERSION$.jar show -f secret.db -n my-password
  ```
  5. Show all secret keys:
  ```shell
  java -jar gocd-file-based-secrets-plugin-$VERSION$.jar keys -f secret.db
  ```
  6. Remove a secret:
  ```shell
  java -jar gocd-file-based-secrets-plugin-$VERSION$.jar remove -f secret.db -n my-password
  ```

## Configuration

The plugin needs to be configured to use the secrets database file. 

The configuration can be added directly to the `config.xml` using the `<secretConfig>` configuration.

* Example Configuration

    ```xml
    <secretConfigs>
      <secretConfig id="Env1Secrets" pluginId="cd.go.secrets.file-based-plugin">
          <description>All secrets for env1</description>
           <configuration>
              <property>
                  <key>SecretsFilePath</key>
                  <value>/godata/config/secretsDatabase.json</value>
              </property>
          </configuration>
          <rules>
              <allow action="refer" type="environment">env_*</allow>
              <deny action="refer" type="pipeline_group">my_group</deny>
              <allow action="refer" type="pipeline_group">other_group</allow>
          </rules>
      </secretConfig>
    </secretConfigs>
    ```
`<rules>` tag defines where this secretConfig is allowed/denied to be referred. For more details about rules and examples refer the GoCD Secret Management [documentation](https://docs.gocd.org/current/configuration/secrets_management.html)

* The plugin can also be configured to use multiple secret database files if required:

    ```xml
    <secretConfigs>
      <secretConfig id="Env1Secrets" pluginId="cd.go.secrets.file-based-plugin">
          <description>All secrets for env1</description>
           <configuration>
              <property>
                  <key>SecretsFilePath</key>
                  <value>/godata/config/secretsDatabase.json</value>
              </property>
          </configuration>
          <rules>
              <allow action="refer" type="environment">env_*</allow>
              <deny action="refer" type="pipeline_group">my_group</deny>
              <allow action="refer" type="pipeline_group">other_group</allow>
          </rules>
      </secretConfig>
      <secretConfig id="Env2Secrets" pluginId="cd.go.secrets.file-based-plugin">
          <description>All secrets for env1</description>
           <configuration>
              <property>
                  <key>SecretsFilePath</key>
                  <value>/godata/config/secretsDatabase_env2.json</value>
              </property>
          </configuration>
          <rules>
              <allow action="refer" type="environment">env_*</allow>
              <deny action="refer" type="pipeline_group">my_group</deny>
              <allow action="refer" type="pipeline_group">other_group</allow>
          </rules>
      </secretConfig>
    </secretConfigs>
    ```

## Troubleshooting

### Verify Connection

For a given secret config verify if the file database can be accessed by the plugin. The *Secrets Configuration* page under *Admin > Security* gives an option to verify connection.

### Enable Debug Logs

#### If you are on GoCD version 19.6 and above:

Edit the file `wrapper-properties.conf` on your GoCD server and add the following options. The location of the `wrapper-properties.conf` can be found in the [installation documentation](https://docs.gocd.org/current/installation/installing_go_server.html) of the GoCD server.

```properties
# We recommend that you begin with the index `100` and increment the index for each system property
wrapper.java.additional.100=-Dplugin.cd.go.secrets.file-based-plugin.log.level=debug
```

If you're running with GoCD server 19.6 and above on docker using one of the supported GoCD server images, set the environment variable `GOCD_SERVER_JVM_OPTIONS`:

```shell
docker run -e "GOCD_SERVER_JVM_OPTIONS=-Dplugin.cd.go.secrets.file-based-plugin.log.level=debug" ...
```

#### If you are on GoCD version 19.5 and lower:
* On Linux:

    Enabling debug level logging can help you troubleshoot an issue with this plugin. To enable debug level logs, edit the file `/etc/default/go-server` (for Linux) to add:

    ```shell
    export GO_SERVER_SYSTEM_PROPERTIES="$GO_SERVER_SYSTEM_PROPERTIES -Dplugin.cd.go.secrets.file-based-plugin.log.level=debug"
    ```

    If you're running the server via `./server.sh` script:

    ```shell
    $ GO_SERVER_SYSTEM_PROPERTIES="-Dplugin.cd.go.secrets.file-based-plugin.log.level=debug" ./server.sh
    ```

* On windows:

    Edit the file `config/wrapper-properties.conf` inside the GoCD Server installation directory (typically `C:\Program Files\Go Server`):

    ```
    # config/wrapper-properties.conf
    # since the last "wrapper.java.additional" index is 15, we use the next available index.
    wrapper.java.additional.16=-Dplugin.cd.go.secrets.file-based-plugin.log.level=debug
    ```

## License

```plain
Copyright 2019 ThoughtWorks, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
