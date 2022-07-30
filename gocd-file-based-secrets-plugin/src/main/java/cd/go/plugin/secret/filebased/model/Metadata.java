/*
 * Copyright 2022 Thoughtworks, Inc.
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

package cd.go.plugin.secret.filebased.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

class Metadata {
    @Expose
    @SerializedName("required")
    private boolean required;

    @Expose
    @SerializedName("secure")
    private boolean secure;

    @Expose
    @SerializedName("display_name")
    private String displayName;

    public Metadata() {}

    public Metadata(boolean required, boolean secure, String displayName) {
        this.required = required;
        this.secure = secure;
        this.displayName = displayName;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isSecure() {
        return secure;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Metadata metadata = (Metadata) o;

        if (required != metadata.required) return false;
        if (secure != metadata.secure) return false;
        return displayName != null ? displayName.equals(metadata.displayName) : metadata.displayName == null;
    }

    @Override
    public int hashCode() {
        int result = (required ? 1 : 0);
        result = 31 * result + (secure ? 1 : 0);
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        return result;
    }
}