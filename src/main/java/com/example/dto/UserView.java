package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserView {

    private int id;

    private String username;
    private String fullName;

}