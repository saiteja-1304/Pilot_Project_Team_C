package com.example.assessment.exception;

public class SetNameExist extends RuntimeException {
    public SetNameExist(String setName) {
        super("Set Name Already Exists "+ setName);
    }

}
