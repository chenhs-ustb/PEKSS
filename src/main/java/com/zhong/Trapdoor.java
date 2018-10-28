package com.zhong;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * 搜索的token
 */
class Trapdoor implements Serializable {
    private static final long serialVersionUID = 1L;
    BigInteger first;
    BigInteger second;

    public Trapdoor(BigInteger first, BigInteger second) {
        this.first = first;
        this.second = second;
    }

    public BigInteger getFirst() {
        return first;
    }

    public BigInteger getSecond() {
        return second;
    }
}