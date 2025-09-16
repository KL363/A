package org.example.agentback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class MyResult {
    private int code;
    private String message;
    private Object data;

    public static MyResult success(Object data) {
        return new MyResult(200, "success", data);
    }
    public static MyResult success() {
        return new MyResult(200, "success", null);
    }
    public static MyResult error(String message) {
        return new MyResult(500, message, null);
    }
}
