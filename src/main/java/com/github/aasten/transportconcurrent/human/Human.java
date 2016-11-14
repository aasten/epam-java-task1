package com.github.aasten.transportconcurrent.human;

import com.github.aasten.transportconcurrent.system.Role;

public interface Human {
    Attention getAttention();
    Role getRole();
}
