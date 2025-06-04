package com.example.stumanager.util;

import lombok.Data;

@Data
public class AjaxResult {
    private boolean success;
    private String message;
    private String imgurl;
    private String type;
}
