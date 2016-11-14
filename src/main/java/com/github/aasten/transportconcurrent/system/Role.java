package com.github.aasten.transportconcurrent.system;

public interface Role {
    void setBehaviour(Behaviour<?> behaviour);
    Behaviour<?> getCurrentBehaviour();
}
