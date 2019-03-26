package cd.go.plugin.secret.filebased.model;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class NonBlankField extends Field {
    public NonBlankField(String key, String displayName, boolean required, boolean secure) {
        super(key, displayName, required, secure);
    }

    @Override
    public Optional<String> validate(String input) {
        if (StringUtils.isBlank(input)) {
            return Optional.of(
                    String.format("%s must not be blank", metadata.getDisplayName())
            );
        }
        return Optional.empty();
    }
}
