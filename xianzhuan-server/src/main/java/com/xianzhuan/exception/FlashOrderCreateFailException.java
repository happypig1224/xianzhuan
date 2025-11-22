package com.xianzhuan.exception;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
public class FlashOrderCreateFailException extends RuntimeException {
    public FlashOrderCreateFailException(String message) {
        super(message);
    }
}