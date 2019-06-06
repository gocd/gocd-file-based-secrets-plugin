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

import com.github.bdpiparva.plugin.base.validation.ValidationResult;
import com.github.bdpiparva.plugin.base.validation.Validator;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Map;

import static cd.go.plugin.secret.filebased.model.SecretsConfiguration.SECRETS_FILE_PATH_PROPERTY;

public class SecretFilePathValidator implements Validator {

    @Override
    public ValidationResult validate(Map<String, String> requestBody) {
        ValidationResult validationResult = new ValidationResult();
        String filePath = requestBody.get(SECRETS_FILE_PATH_PROPERTY);
        if (StringUtils.isBlank(filePath)) {
            return addErrorAndReturn(validationResult, "SecretsFilePath must not be blank.");
        }

        File secretFile = new File(filePath);

        if (!secretFile.exists()) {
            return addErrorAndReturn(validationResult, String.format("No secret config file at path '%s'.", filePath));
        }

        if (secretFile.isDirectory()) {
            return addErrorAndReturn(validationResult, String.format("Secret config file path '%s' is not a normal file.", filePath));
        }

        if (!secretFile.canRead()) {
            return addErrorAndReturn(validationResult, String.format("Unable to read secret config file '%s', check permissions.", filePath));
        }

        return validationResult;
    }

    private ValidationResult addErrorAndReturn(ValidationResult validationResult, String message) {
        validationResult.add(SECRETS_FILE_PATH_PROPERTY, message);
        return validationResult;
    }
}
