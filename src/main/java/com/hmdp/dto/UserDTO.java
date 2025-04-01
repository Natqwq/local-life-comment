package com.hmdp.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class UserDTO {
    private Long id;
    private String nickName;
    private String icon;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id.toString());
        map.put("nickName", nickName);
        map.put("icon", icon);
        return map;
    }
}
