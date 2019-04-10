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

package cd.go.plugin.secret.filebased.cli;

import cd.go.plugin.secret.filebased.cli.args.*;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import java.util.function.Consumer;

public class Main {

    private final String[] argv;

    public Main(String... argv) {
        this.argv = argv;
    }

    public static void main(String[] argv) {
        new Main(argv).run((exitStatus) -> System.exit(exitStatus));
    }

    void run(Consumer<Integer> exitter) {
        RootArgs rootArgs = new RootArgs();
        InitArgs initArgs = new InitArgs();
        AddSecretArgs addSecretArgs = new AddSecretArgs();
        RemoveSecretArgs removeSecretArgs = new RemoveSecretArgs();
        ShowSecretArgs showSecretArgs = new ShowSecretArgs();
        ShowAllSecretKeysArgs keysArgs = new ShowAllSecretKeysArgs();

        JCommander cmd = JCommander.newBuilder()
                .addObject(rootArgs)
                .addCommand(initArgs)
                .addCommand(addSecretArgs)
                .addCommand(removeSecretArgs)
                .addCommand(showSecretArgs)
                .addCommand(keysArgs)
                .build();

        String parsedCommand = null;

        try {
            cmd.parse(argv);
            parsedCommand = cmd.getParsedCommand();

            if (rootArgs.help || parsedCommand == null) {
                printUsageAndExit(cmd, parsedCommand, 1, exitter);
            }

            switch (parsedCommand) {
                case "init":
                    initArgs.execute(exitter);
                    break;
                case "add":
                    addSecretArgs.execute(exitter);
                    break;
                case "remove":
                    removeSecretArgs.execute(exitter);
                    break;
                case "show":
                    showSecretArgs.execute(exitter);
                    break;
                case "keys":
                    keysArgs.execute(exitter);
                    break;
                default:
                    throw new UnsupportedOperationException(parsedCommand);
            }
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            printUsageAndExit(cmd, parsedCommand, 1, exitter);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            exitter.accept(-1);
        }
    }

    private static void printUsageAndExit(JCommander cmd, String parsedCommand, int statusCode, Consumer<Integer> exitter) {
        StringBuilder out = new StringBuilder();
        if (parsedCommand == null) {
            cmd.usage(out);
        } else {
            cmd.usage(parsedCommand, out);
        }
        System.err.println(out.toString());
        exitter.accept(statusCode);
    }
}
