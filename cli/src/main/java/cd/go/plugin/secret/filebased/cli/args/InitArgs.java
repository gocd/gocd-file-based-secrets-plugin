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
import com.beust.jcommander.Parameters;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.function.Consumer;

@Parameters(commandDescription = "Initialize the secret database file", commandNames = "init")
public class InitArgs extends DatabaseFileArgs {
    public void execute(Consumer<Integer> exitter) throws NoSuchAlgorithmException, IOException {
        FileUtils.write(databaseFile, new SecretsDatabase().toJSON(), StandardCharsets.UTF_8);
        System.err.println("Initialized secret database file in " + databaseFile);
    }
}
