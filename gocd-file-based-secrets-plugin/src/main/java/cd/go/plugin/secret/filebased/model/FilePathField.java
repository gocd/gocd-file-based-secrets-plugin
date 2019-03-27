package cd.go.plugin.secret.filebased.model;

import java.io.File;
import java.util.Optional;

public class FilePathField extends NonBlankField {
    public FilePathField(String key, String displayName, boolean required, boolean secure) {
        super(key, displayName, required, secure);
    }

    @Override
    public Optional<String> validate(String filePath) {
        Optional<String> validationError = super.validate(filePath);

        if (validationError.isPresent()) {
            return validationError;
        }

        if (!new File(filePath).exists()) {
            return Optional.of(
                    String.format("'%s' must contain a valid file path", metadata.getDisplayName())
            );
        }
        return Optional.empty();
    }
}
