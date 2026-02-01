

package org.puregxl.ElasticExecutor.core.Enum;

import lombok.Getter;


@Getter
public enum ConfigFileTypeEnum {

    /**
     * PROPERTIES
     */
    PROPERTIES("properties"),

    /**
     * YML
     */
    YML("yml"),

    /**
     * YAML
     */
    YAML("yaml");

    private final String value;

    ConfigFileTypeEnum(String value) {
        this.value = value;
    }

    public static ConfigFileTypeEnum of(String value) {
        for (ConfigFileTypeEnum configFileTypeEnum : ConfigFileTypeEnum.values()) {
            if (configFileTypeEnum.value.equals(value)) {
                return configFileTypeEnum;
            }
        }
        return PROPERTIES;
    }
}
