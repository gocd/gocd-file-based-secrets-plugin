package cd.go.plugin.secret.filebased.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Optional;

public abstract class Field {
    @Expose
    @SerializedName("key")
    protected String key;

    @Expose
    @SerializedName("metadata")
    protected Metadata metadata;

    public Field() {}

    public Field(String key, String displayName, boolean required, boolean secure) {
        this.key = key;
        this.metadata = new Metadata(required, secure, displayName);
    }


    public abstract Optional<String> validate(String inputValue);

    public String getKey() {
        return key;
    }
}
