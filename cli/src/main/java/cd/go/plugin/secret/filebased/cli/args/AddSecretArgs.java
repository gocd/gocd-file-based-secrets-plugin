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

package cd.go.plugin.secret.filebased.cli.args;

import cd.go.plugin.secret.filebased.db.SecretsDatabase;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.function.Consumer;

@Parameters(commandDescription = "Adds a secret.", commandNames = "add")
public class AddSecretArgs extends HasNameArgs {

    @Parameter(names = {"--value", "-v"}, required = true, description = "The value of the secret.", password = true)
    public String secret;

    public void execute(Consumer<Integer> exitter) throws IOException, GeneralSecurityException {
        SecretsDatabase.readFrom(databaseFile)
                .addSecret(key, secret)
                .saveTo(databaseFile);

        System.err.println("Added secret named " + key + ".");
    }
}
