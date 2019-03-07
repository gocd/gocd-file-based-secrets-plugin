package cd.go.plugin.secret.filebased.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Optional;

public abstract class Field {
    private String key;

    @Expose
    @SerializedName("display-name")
    protected String displayName;

    @Expose
    @SerializedName("display-order")
    protected String displayOrder;

    @Expose
    @SerializedName("required")
    protected Boolean required;

    public Field(String key, String displayName, String displayOrder, Boolean required) {
        this.key = key;

        this.displayName = displayName;
        this.displayOrder = displayOrder;
        this.required = required;
    }


    public abstract Optional<String> validate(String inputValue);

    public String getKey() {
        return key;
    }
}
