package org.example.agentback.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyUser {
    private int id;
    private String userName;
    private String userPassword;
    private String userEmail;
}
